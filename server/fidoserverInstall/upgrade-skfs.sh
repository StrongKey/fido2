#!/bin/bash
#
###############################################################
# /**
# * Copyright StrongAuth, Inc. All Rights Reserved.
# *
# * Use of this source code is governed by the GNU Lesser General Public License v2.1
# * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
# */
###############################################################

#Add Maria and payara

. /etc/skfsrc

CURRENT_SKFS_BUILDNO=$(ls -1 $STRONGKEY_HOME/fido/Version* 2> /dev/null | sed -r 's|.*VersionFidoServer-||')

SCRIPT_HOME=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
SOFTWARE_HOME=/usr/local/software
STRONGKEY_HOME=/usr/local/strongkey

GLASSFISH_ADMIN_PASSWORD=adminadmin
MYSQL_ROOT_PASSWORD=BigKahuna
MARIA_SKFSDBUSER_PASSWORD=AbracaDabra
SERVICE_LDAP_BIND_PASS=Abcd1234!

GLASSFISH=payara-4.1.2.181.zip
MARIADB=mariadb-10.2.30-linux-glibc_214-x86_64.tar.gz
MARIAVER=mariadb-10.2.30-linux-glibc_214-x86_64
MARIATGT=mariadb-10.2.30
MARIA_HOME=$STRONGKEY_HOME/$MARIATGT
DBCLIENT=mariadb-java-client-2.2.6.jar

RPNAME=FIDOServer
RPID=strongkey.com
JWT_DN='CN=StrongKey KeyAppliance,O=StrongKey'
JWT_DURATION=30
JWT_KEYGEN_DN='/C=US/ST=California/L=Cupertino/O=StrongAuth/OU=Engineering'
JWT_CERTS_PER_SERVER=3
JWT_KEYSTORE_PASS=Abcd1234!
JWT_KEY_VALIDITY=365
SAKA_DID=1

ALLOW_USERNAME_CHANGE=false

LATEST_SKFS_BUILD=fidoserver.ear

UPDATE_GLASSFISH=Y
UPDATE_MARIADB=Y
ROLLBACK=Y

function check_exists {
for ARG in "$@"
do
    if [ ! -f $ARG ]; then
        >&2 echo -e "$ARG Not Found. Check to ensure the file exists in the proper location and try again."
        exit 1
    fi
done
}


# Check that the script is run as strongkey
if [ "$(whoami)" != "root" ]; then
        >&2 echo "$0 must be run as root"
        exit 1
fi

# Check that variables are set
if [ -z $GLASSFISH_HOME ]; then
        >&2 echo "Variable GLASSFISH_HOME not set correctly."
        exit 1
fi

# Check glassfish status
if ! ps -efww | grep "$GLASSFISH_HOME/modules/glassfish.ja[r]" &>/dev/null; then
        >&2 echo "Glassfish must be running in order to perform this upgrade"
        exit 1
fi

#get admin password
echo "$GLASSFISH_ADMIN_PASSWORD" > /tmp/password
while ! $GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password list . &> /dev/null; do
        echo -n "This upgrade requires the glassfish 'admin' password. Please enter the password now: "
        echo
        read -s GLASSFISH_ADMIN_PASSWORD
        echo "AS_ADMIN_PASSWORD=$GLASSFISH_ADMIN_PASSWORD" > /tmp/password
done

# install required packages
YUM_CMD=$(which yum  2>/dev/null)
APT_GET_CMD=$(which apt-get 2>/dev/null)

if [[ ! -z $YUM_CMD ]]; then
    yum -y install psmisc >/dev/null 2>&1
    systemctl restart rngd
elif [[ ! -z $APT_GET_CMD ]]; then
    apt-get update >/dev/null 2>&1
    apt install psmisc -y >/dev/null 2>&1
else
     echo "error can't install package 'psmisc'"
     exit 1;
fi

if [ ! -f $SCRIPT_HOME/$DBCLIENT ]; then
	echo -n "Downloading Mariadb JAVA Connector ... "
	wget https://downloads.mariadb.com/Connectors/java/connector-java-2.2.6/mariadb-java-client-2.2.6.jar -q
	echo "Successful"
fi

# Undeploy SKFS
echo
echo "Undeploying old skfs build..."
$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password undeploy fidoserver

# Check if upgrading to 4.3.0 is necessary
if [[ -z $($MYSQL_HOME/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "show index from fido_keys where column_name='fkid';") ]]; then
        echo "Upgrading to 4.3.0 (Prerequisite for other upgrades)..."

	# Stop Glassfish
	echo
	echo "Stopping Glassfish..."
	service glassfishd stop


	##Update MYSQL
	if [ "$MYSQL_HOME" != "$STRONGKEY_HOME/$MARIATGT" ]; then
		echo
		echo "Updating mariadb..."

		service mysqld stop

		tar zxf ${MARIADB} -C $STRONGKEY_HOME

		mv $STRONGKEY_HOME/$MARIAVER $STRONGKEY_HOME/$MARIATGT

		echo "Copying ibdata (This may take some time)..."
		if [ "$ROLLBACK" == 'N' ]; then
			mv  $MYSQL_HOME/ibdata $MYSQL_HOME/log $MYSQL_HOME/backups $STRONGKEY_HOME/$MARIATGT
		else
			cp -r  $MYSQL_HOME/ibdata $MYSQL_HOME/log $MYSQL_HOME/backups $STRONGKEY_HOME/$MARIATGT
		fi
		mkdir $STRONGKEY_HOME/$MARIATGT/binlog
		chown -R strongkey. $STRONGKEY_HOME/$MARIATGT
		cp /etc/my.cnf /etc/my.cnf.bak
		sed -i "s|$MYSQL_HOME|$STRONGKEY_HOME/$MARIATGT|" /etc/my.cnf
		sed -i "s|export MYSQL_HOME=.*|export MYSQL_HOME=$STRONGKEY_HOME/$MARIATGT|" /etc/skfsrc

		rm -f $GLASSFISH_HOME/lib/mariadb-java-client-*.jar
		cp $DBCLIENT $GLASSFISH_HOME/lib/

		chown -R strongkey. $GLASSFISH_HOME/lib/$DBCLIENT

		bash /etc/bashrc

		killall -9 mysqld mysqld_safe

		service mysqld start

		echo "Upgrading Mariadb (This may take some time)..."
		mysql_upgrade -u root -p$MYSQL_ROOT_PASSWORD --skip-version-check >/dev/null

		echo "Restarting Mariadb (This may take some time)..."
		service mysqld restart
	fi

	$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "create index fkid on fido_keys(sid,did,fkid);"

	policyRes=`$STRONGKEY_HOME/$MARIATGT/bin/mysql -s -N --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "select policy from fido_policies where certificate_profile_name = 'Default Policy'\G"`
	policyBase64=$(echo $policyRes | cut -d ' ' -f 15-)
	policyJSON=`echo $policyBase64 | python -m base64 -d`
	if [[ ! "$policyJSON" == "trusted_authenticators" ]]; then
		updatedPolicyJSON=$(echo $policyJSON | sed -r 's/(.*)}/\1,\"trusted_authenticators\":{\"aaguids\":[]}}/')
		updatedPolicyBase64=`echo $updatedPolicyJSON | python -m base64`
		$STRONGKEY_HOME/$MARIATGT/bin/mysql -s -N --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "update fido_policies set policy = '${updatedPolicyBase64}' where certificate_profile_name = 'Default Policy'\G"
	fi
fi

# 4.3.0 upgrade finished, start upgrade to 4.4
if [ ! -f $STRONGKEY_HOME/skfs/keystores/jwtsigningtruststore.bcfks ]; then
        echo "Upgrading to 4.4"

	mkdir -p $STRONGKEY_HOME/upgrading
	cp -r $SCRIPT_HOME/keymanager $STRONGKEY_HOME
	cp -r $SCRIPT_HOME/skfsclient $STRONGKEY_HOME
        chown -R strongkey. $STRONGKEY_HOME

        # If upgrading from 4.3.0 to current, signing key regenration is required for EC
        if [[ -z $(keytool -list -v -keystore $STRONGKEY_HOME/skfs/keystores/signingkeystore.bcfks -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $STRONGKEY_HOME/keymanager/lib/bc-fips-1.0.1.jar -storepass Abcd1234! | grep "1-ec-zenc-signing-key") ]]; then
                java -jar $STRONGKEY_HOME/keymanager/keymanager.jar regeneratesigningkey $SAKA_DID $STRONGKEY_HOME/skfs/keystores/signingkeystore.bcfks $STRONGKEY_HOME/skfs/keystores/signingtruststore.bcfks Abcd1234! EC
        fi
	
        # MYSQL
	cd $SCRIPT_HOME/fidoserverSQL
	$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "update domains set skce_signingdn='CN=SKFS Signing Key,OU=DID 1,OU=SKFS EC Signing Certificate 1,O=StrongKey';";
	$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "drop table fido_policies;"
	$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "source fido_policies.sql;"
	$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "source configurations.sql"
	$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "alter table fido_keys drop column if exists khdigest;"
	$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "alter table fido_keys drop column if exists khdigest_type;"
	if [[ -z $(/usr/local/strongkey/mariadb-10.2.30/bin/mysql --user=skfsdbuser --password=AbracaDabra --database=skfs -e "select null from information_schema.columns where table_name = 'fido_keys' and table_schema = 'skfs' and column_name = 'signature_keytype';") ]]; then
		$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "alter table fido_keys add signature_keytype ENUM('RSA','EC','OTHER') NOT NULL;"
	fi

	# OPENDJ
	cd $STRONGKEY_HOME
        cat >> $STRONGKEY_HOME/upgrading/update.ldif <<- EOFUPDATELDIF
dn: cn=fidoadminuser,did=1,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
changetype: add
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
objectClass: top
userPassword: Abcd1234!
givenName: fidoadminuser
cn: fidoadminuser
sn: fidoadminuser

dn: cn=FidoRegAuthorized,did=1,ou=groups,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
objectClass: groupOfUniqueNames
objectClass: top
cn: FidoRegAuthorized
uniqueMember: cn=svcfidouser,did=1,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com

dn: cn=FidoSignAuthorized,did=1,ou=groups,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
objectClass: groupOfUniqueNames
objectClass: top
cn: FidoSignAuthorized
uniqueMember: cn=svcfidouser,did=1,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com

dn: cn=FidoAuthzAuthorized,did=1,ou=groups,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
objectClass: groupOfUniqueNames
objectClass: top
cn: FidoAuthzAuthorized
uniqueMember: cn=svcfidouser,did=1,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com

dn: cn=FidoAdminAuthorized,did=1,ou=groups,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
objectClass: groupOfUniqueNames
objectClass: top
cn: FidoAdminAuthorized
uniqueMember: cn=fidoadminuser,did=1,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
EOFUPDATELDIF

        if [[ -z $(/usr/local/strongkey/OpenDJ-3.0.0/bin/ldapsearch -h localhost -p 1389 -D "cn=Directory Manager" -w 'Abcd1234!' -b "dc=strongauth,dc=com" "CN=*" | grep fidoadminuser) ]]; then
		$STRONGKEY_HOME/OpenDJ-3.0.0/bin/ldapmodify --filename $STRONGKEY_HOME/upgrading/update.ldif \
					  --hostName $(hostname) \
					  --port 1389 \
					  --bindDN 'cn=Directory Manager' \
					  --bindPassword "$SERVICE_LDAP_BIND_PASS" \
					  --trustAll \
					  --noPropertiesFile \
					  --defaultAdd > /dev/null
        fi

        startDate=$(date +%s)
	fidoPolicy=$(echo "{\"FidoPolicy\":{\"name\":\"DefaultPolicy\",\"copyright\":\"\",\"version\":\"1.0\",\"startDate\":\"${startDate}\",\"endDate\":\"1760103870871\",\"system\":{\"requireCounter\":\"mandatory\",\"integritySignatures\":false,\"userVerification\":[\"required\",\"preferred\",\"discouraged\"],\"userPresenceTimeout\":0,\"allowedAaguids\":[\"all\"],\"jwtKeyValidity\":${JWT_KEY_VALIDITY},\"jwtRenewalWindow\":30},\"algorithms\":{\"curves\":[\"secp256r1\",\"secp384r1\",\"secp521r1\",\"curve25519\"],\"rsa\":[\"rsassa-pkcs1-v1_5-sha256\",\"rsassa-pkcs1-v1_5-sha384\",\"rsassa-pkcs1-v1_5-sha512\",\"rsassa-pss-sha256\",\"rsassa-pss-sha384\",\"rsassa-pss-sha512\"],\"signatures\":[\"ecdsa-p256-sha256\",\"ecdsa-p384-sha384\",\"ecdsa-p521-sha512\",\"eddsa\",\"ecdsa-p256k-sha256\"]},\"attestation\":{\"conveyance\":[\"none\",\"indirect\",\"direct\",\"enterprise\"],\"formats\":[\"fido-u2f\",\"packed\",\"tpm\",\"android-key\",\"android-safetynet\",\"apple\",\"none\"]},\"registration\":{\"displayName\":\"required\",\"attachment\":[\"platform\",\"cross-platform\"],\"residentKey\":[\"required\",\"preferred\",\"discouraged\"],\"excludeCredentials\":\"enabled\"},\"authentication\":{\"allowCredentials\":\"enabled\"},\"authorization\":{\"maxdataLength\":256,\"preserve\":true},\"rp\":{\"id\":\"${RPID}\",\"name\":\"${RPNAME}\"},\"extensions\":{\"example.extension\":true},\"jwt\":{\"algorithms\":[\"ES256\",\"ES384\",\"ES521\"],\"duration\":${JWT_DURATION},\"required\":[\"rpid\",\"iat\",\"exp\",\"cip\",\"uname\",\"agent\"],\"signingCerts\":{\"DN\":\"${JWT_DN}\",\"certsPerServer\":${JWT_CERTS_PER_SERVER}}}}}" | /usr/bin/base64 -w 0)
        $STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "insert into FIDO_POLICIES values (1,1,1,'${fidoPolicy}','Active','',NOW(),NULL,NULL);"
        
	$SCRIPT_HOME/keygen-jwt.sh $JWT_KEYGEN_DN $($MARIA_HOME/bin/mysql -u skfsdbuser -p${MARIA_SKFSDBUSER_PASSWORD} skfs -B --skip-column-names -e "select count(fqdn) from servers;") $JWT_CERTS_PER_SERVER $SAKA_DID $JWT_KEYSTORE_PASS $JWT_KEY_VALIDITY
	chown strongkey. $STRONGKEY_HOME/skfs/keystores/jwtsigningtruststore.bcfks $STRONGKEY_HOME/skfs/keystores/jwtsigningkeystore.bcfks 

	touch $STRONGKEY_HOME/crypto/etc/crypto-configuration.properties
	echo "crypto.cfg.property.jwtsigning.certsperserver=$JWT_CERTS_PER_SERVER" >> $STRONGKEY_HOME/crypto/etc/crypto-configuration.properties
	chown strongkey. $STRONGKEY_HOME/crypto/etc/crypto-configuration.properties
	

        
        if [ $UPDATE_GLASSFISH = 'Y' ]; then
		echo "Upgrading to Payara 5"
                GLASSFISH_HOME=$STRONGKEY_HOME/payara5/glassfish
                GLASSFISH_CONFIG=$GLASSFISH_HOME/domains/domain1/config
                MARIACONJAR=mariadb-java-client-2.2.6.jar
                XMXSIZE=512m

                INSTALL_FIDO=Y
                
                cd $STRONGKEY_HOME/upgrading

                wget https://repo1.maven.org/maven2/fish/payara/distributions/payara/5.2020.7/payara-5.2020.7.zip
                unzip payara-5.2020.7.zip -d $STRONGKEY_HOME > /dev/null
                chown -R strongkey. $STRONGKEY_HOME/payara5

                service glassfishd stop
                /lib/systemd/systemd-sysv-install disable glassfishd
                rm /etc/init.d/glassfishd
                
                cp $SCRIPT_HOME/glassfishd $STRONGKEY_HOME/upgrading
                sed -i -e 's/payara41/payara5/g' $STRONGKEY_HOME/upgrading/glassfishd
                sed -i -e 's/Glassfish 4/Glassfish 5/g' $STRONGKEY_HOME/upgrading/glassfishd
                sed -i -e 's/payara41/payara5/g' /etc/skfsrc
                . /etc/skfsrc

                cp $STRONGKEY_HOME/upgrading/glassfishd /etc/init.d
                chmod 755 /etc/init.d/glassfishd
                /lib/systemd/systemd-sysv-install enable glassfishd

                rm $STRONGKEY_HOME/certs/$(hostname).der
                keytool -genkeypair -alias skfs -keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit -keypass changeit -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 3562 -dname "CN=$(hostname),OU=\"StrongKey FidoServer\"" &>/dev/null
                keytool -changealias -alias s1as -destalias s1as.original -keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit &>/dev/null
                keytool -changealias -alias skfs -destalias s1as -keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit &>/dev/null
                sed -ri 's|^(com.sun.enterprise.server.logging.GFFileHandler.rotationOnDateChange=).*|\1true|
                         s|^(com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=).*|\1200000000|' $GLASSFISH_CONFIG/logging.properties
                keytool -exportcert -alias s1as -file $STRONGKEY_HOME/certs/$(hostname).der --keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit &>/dev/null
                keytool -importcert -noprompt -alias $(hostname) -file $STRONGKEY_HOME/certs/$(hostname).der --keystore $STRONGKEY_HOME/certs/cacerts -storepass changeit &>/dev/null
                keytool -importcert -noprompt -alias $(hostname) -file $STRONGKEY_HOME/certs/$(hostname).der --keystore $GLASSFISH_CONFIG/cacerts.jks -storepass changeit &>/dev/null

                cp $SCRIPT_HOME/$MARIACONJAR $GLASSFISH_HOME/lib
                chown -R strongkey. $STRONGKEY_HOME
                service glassfishd start
                sleep 10

                $GLASSFISH_HOME/bin/asadmin set server.network-config.network-listeners.network-listener.http-listener-1.enabled=false
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=7200
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl3-tls-ciphers=+TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl2-enabled=false
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl3-enabled=false
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.tls-enabled=false
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.tls11-enabled=false
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.trace-enabled=false
                $GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.xpowered-by=false
                
                if [ $INSTALL_FIDO = 'Y' ]; then
                        $GLASSFISH_HOME/bin/asadmin create-jdbc-connection-pool \
                                --datasourceclassname org.mariadb.jdbc.MySQLDataSource \
                                --restype javax.sql.ConnectionPoolDataSource \
                                --isconnectvalidatereq=true \
                                --validationmethod meta-data \
                                --property ServerName=localhost:DatabaseName=skfs:port=3306:user=skfsdbuser:password=$MARIA_SKFSDBUSER_PASSWORD:DontTrackOpenResources=true \
                                SKFSPool
                        $GLASSFISH_HOME/bin/asadmin create-jdbc-resource --connectionpoolid SKFSPool jdbc/strongkeylite
                        $GLASSFISH_HOME/bin/asadmin set server.resources.jdbc-connection-pool.SKFSPool.max-pool-size=1000
                        $GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=1000
                        $GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=10
                fi
                
                $GLASSFISH_HOME/bin/asadmin delete-jvm-options $($GLASSFISH_HOME/bin/asadmin list-jvm-options | sed -n '/\(-XX:NewRatio\|-XX:MaxPermSize\|-XX:PermSize\|-client\|-Xmx\|-Xms\)/p' | sed 's|:|\\\\:|' | tr '\n' ':')
                $GLASSFISH_HOME/bin/asadmin create-jvm-options -Djtss.tcs.ini.file=$STRONGKEY_HOME/lib/jtss_tcs.ini:-Djtss.tsp.ini.file=$STRONGKEY_HOME/lib/jtss_tsp.ini:-Xmx${XMXSIZE}:-Xms${XMXSIZE}:-Djdk.tls.ephemeralDHKeySize=2048:-Dproduct.name="":-XX\\:-DisableExplicitGC

                if [ $INSTALL_FIDO = 'Y' ]; then
                        cat > $GLASSFISH_HOME/domains/domain1/docroot/app.json <<- EOFAPPJSON
{
  "trustedFacets" : [{
    "version": { "major": 1, "minor" : 0 },
    "ids": [
      "https://$(hostname)",
      "https://$(hostname):8181"
    ]
  }]
}
EOFAPPJSON

                        # Add other servers to app.json
                        for fqdn in $($MARIA_HOME/bin/mysql -u skfsdbuser -p${MARIA_SKFSDBUSER_PASSWORD} skfs -B --skip-column-names -e "select fqdn from servers;"); do
                                # Skip doing ourself again
                                if [ "$fqdn" == "$(hostname)" ]; then
                                        continue
                                fi
                                sed -i "/^\[/a \"           https://$fqdn:8181\"," $GLASSFISH_HOME/domains/domain1/docroot/app.json
                                sed -i "/^\[/a \"           https://$fqdn\"," $GLASSFISH_HOME/domains/domain1/docroot/app.json
                        done
                        chown strongkey $GLASSFISH_HOME/domains/domain1/docroot/app.json
                fi
                
                $STRONGKEY_HOME/bin/certimport.sh $(hostname) -k$STRONGKEY_HOME/certs/cacerts
                service glassfishd stop
		if [ "$ROLLBACK" == 'N' ]; then
                	rm -rf $STRONGKEY_HOME/payara41
		fi
        fi
        if [ $UPDATE_MARIADB = 'Y' ]; then
                echo "Upgrading to MariaDB-10.5.8"
                MARIA=mariadb-10.5.8-linux-x86_64.tar.gz
                MARIAVER=mariadb-10.5.8-linux-x86_64
                MARIATGT=mariadb-10.5.8
                MARIA_HOME=$STRONGKEY_HOME/$MARIATGT
                
                cd $STRONGKEY_HOME/upgrading

                # Make a DB dump and uninstall previous MariaDB
                $STRONGKEY_HOME/mariadb-10.2.30/bin/mysqldump -u root -p$MYSQL_ROOT_PASSWORD skfs > skfs_backup.sql
                service mysqld stop
                /lib/systemd/systemd-sysv-install disable mysqld
		mv /etc/my.cnf /etc/my.cnf.old
                rm /etc/init.d/mysqld
                if [ "$ROLLBACK" == 'N' ]; then
                	rm -rf $STRONGKEY_HOME/mariadb-10.2.30
		fi
                pkill -f mariadb-10.2.30

                wget https://downloads.mariadb.com/MariaDB/mariadb-10.5.8/bintar-linux-x86_64/mariadb-10.5.8-linux-x86_64.tar.gz
                tar zxf $STRONGKEY_HOME/upgrading/$MARIA -C $STRONGKEY_HOME

                sed -i 's|^mysqld_ld_preload=$|mysqld_ld_preload=/usr/lib64/libjemalloc.so.1|' $STRONGKEY_HOME/$MARIAVER/bin/mysqld_safe
                cp $STRONGKEY_HOME/$MARIAVER/support-files/mysql.server /etc/init.d/mysqld
                chmod 755 /etc/init.d/mysqld
                /lib/systemd/systemd-sysv-install enable mysqld
                mkdir $STRONGKEY_HOME/$MARIAVER/backups $STRONGKEY_HOME/$MARIAVER/binlog $STRONGKEY_HOME/$MARIAVER/log $STRONGKEY_HOME/$MARIAVER/ibdata
                mv $STRONGKEY_HOME/$MARIAVER $STRONGKEY_HOME/$MARIATGT
                chown -R strongkey. $STRONGKEY_HOME/$MARIATGT
                
                DBSIZE=10M
                SERVER_BINLOG=$STRONGKEY_HOME/$MARIATGT/binlog/skfs-binary-log
		BUFFERPOOLSIZE=$(grep -oP '(?<=innodb_buffer_pool_size)[A-Za-z0-9 _=]+' /etc/my.cnf.old | cut -d '=' -f 2 | grep -oP [A-Za-z0-9]+)

                cat > /etc/my.cnf <<-EOFMYCNF
[client]
socket                          = /usr/local/strongkey/$MARIATGT/log/mysqld.sock

[mysqld]
user                            = strongkey
lower_case_table_names          = 1
log-bin                         = $SERVER_BINLOG

[server]
basedir                         = /usr/local/strongkey/$MARIATGT
datadir                         = /usr/local/strongkey/$MARIATGT/ibdata
pid-file                        = /usr/local/strongkey/$MARIATGT/log/mysqld.pid
socket                          = /usr/local/strongkey/$MARIATGT/log/mysqld.sock
general_log                     = 0
general_log_file                = /usr/local/strongkey/$MARIATGT/log/mysqld.log
log-error                       = /usr/local/strongkey/$MARIATGT/log/mysqld-error.log
innodb_data_home_dir            = /usr/local/strongkey/$MARIATGT/ibdata
innodb_data_file_path           = ibdata01:$DBSIZE:autoextend
innodb_flush_method             = O_DIRECT
innodb_buffer_pool_size         = ${BUFFERPOOLSIZE}
innodb_log_file_size            = 512M
innodb_log_buffer_size          = 5M
innodb_flush_log_at_trx_commit  = 1
sync_binlog                     = 1
lower_case_table_names          = 1
max_connections                 = 1000
thread_cache_size               = 1000
expire_logs_days                = 10
EOFMYCNF

                
                sed -i -e 's/mariadb-10.2.30/mariadb-10.5.8/g' /etc/my.cnf
                sed -i -e 's/mariadb-10.2.30/mariadb-10.5.8/g' /etc/skfsrc
                . /etc/skfsrc

                MYSQL_CMD=$($MARIA_HOME/bin/mysql --version 2>/dev/null)
                if [[ ! -z $MYSQL_CMD ]]; then
                      :
                else
                      echo "mysql binary does not exist or cannot be executed."
                      exit 1
                fi
                
                cd $STRONGKEY_HOME/$MARIATGT
                scripts/mysql_install_db --basedir=`pwd` --datadir=`pwd`/ibdata &>/dev/null
                # Sleep till the database is created
                bin/mysqld_safe &>/dev/null &
                READY=`grep "ready for connections" $MARIA_HOME/log/mysqld-error.log | wc -l`
                while [ $READY -ne 1 ]
                do
                        echo -n .
                        sleep 3
                        READY=`grep "ready for connections" $MARIA_HOME/log/mysqld-error.log | wc -l`
                done
                echo done

                $MARIA_HOME/bin/mysql -u root mysql -e "set password for 'root'@localhost=password('$MYSQL_ROOT_PASSWORD');
                                                            delete from mysql.db where host = '%';
                                                            delete from mysql.user where user = '';
                                                            flush privileges;"
                $MARIA_HOME/bin/mysql -u root -p$MYSQL_ROOT_PASSWORD mysql -e "create database skfs;
                                                            grant all on skfs.* to skfsdbuser@localhost identified by '$MARIA_SKFSDBUSER_PASSWORD';
                                                            flush privileges;"
                $MARIA_HOME/bin/mysql -u root -p$MYSQL_ROOT_PASSWORD skfs < $STRONGKEY_HOME/upgrading/skfs_backup.sql
                
                chown -R strongkey. $STRONGKEY_HOME/$MARIATGT
                service mysqld start
        fi

	# Use newer fidoserver version
	cp $SCRIPT_HOME/fidoserver.ear /tmp
fi # End of 4.4.0 Upgrade

# 4.4.0 upgrade finished, start upgrade to 4.4.1
# Introduction of version file found in $STRONGKEY_HOME/fido
if [ -z "$CURRENT_SKFS_BUILDNO" ]; then
	echo "Upgrading to 4.4.1"
	echo "skfs.cfg.property.allow.changeusername=$ALLOW_USERNAME_CHANGE" >> $STRONGKEY_HOME/skfs/etc/skfs-configuration.properties
	chown -R strongkey:strongkey $STRONGKEY_HOME/skfs

	mkdir -p $STRONGKEY_HOME/fido
        touch $STRONGKEY_HOME/fido/VersionFidoServer-4.4.1
        chown -R strongkey:strongkey $STRONGKEY_HOME/fido
fi # End of 4.4.1 Upgrade

# 4.4.1 upgrade finished, start upgrade to 4.4.2
if [[ $CURRENT_SKFS_BUILDNO < "4.4.2" ]]; then
	echo "Upgrading to 4.4.2"

	# Extract OpenDJ ldif
	service opendjd stop
        export-ldif --includeBranch "dc=strongauth,dc=com" --backendID userRoot --ldifFile $STRONGKEY_HOME/SKFS-OpenDJ-export.ldif -e entryUUID -e createTimestamp -e pwdChangedTime -e creatorsName -i uniqueMember -i ou -i domainName -i description -i did -i givenName -i userPassword -i uid -i cn -i sn
	sed -i '/cn: FIDOUsers/i uniqueMember: cn=encryptdecrypt,did=1,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com' $STRONGKEY_HOME/SKFS-OpenDJ-export.ldif
	sed -i '/fidoinetorgperson/d' $STRONGKEY_HOME/SKFS-OpenDJ-export.ldif
	sed -i '/did:/{$!N;/\n.*userPassword/!P;D}' $STRONGKEY_HOME/SKFS-OpenDJ-export.ldif
	
	# Install OpenLDAP
	yum -y install openldap compat-openldap openldap-clients openldap-servers openldap-servers-sql openldap-devel >/dev/null 2>&1
        yum -y reinstall openldap compat-openldap openldap-clients openldap-servers openldap-servers-sql openldap-devel >/dev/null 2>&1
	
	# Start OpenLDAP
	systemctl start slapd
        systemctl enable slapd >/dev/null 2>&1

        OLDAPPASS=$(slappasswd -h {SSHA} -s $SERVICE_LDAP_BIND_PASS)

        sed -i "s|^olcRootPW: $|olcRootPW: $OLDAPPASS|" $SCRIPT_HOME/ldaprootpassword.ldif
        sed -i "s|^olcRootPW: $|olcRootPW: $OLDAPPASS|" $SCRIPT_HOME/db.ldif

	# Configure OpenLDAP
	/bin/ldapadd -Y EXTERNAL -H ldapi:/// -f $SCRIPT_HOME/ldaprootpassword.ldif >/dev/null 2>&1
        /bin/ldapmodify -Y EXTERNAL  -H ldapi:/// -f $SCRIPT_HOME/db.ldif >/dev/null 2>&1
        /bin/ldapmodify -Y EXTERNAL  -H ldapi:/// -f $SCRIPT_HOME/monitor.ldif >/dev/null 2>&1

        cp /usr/share/openldap-servers/DB_CONFIG.example /var/lib/ldap/DB_CONFIG
        chown ldap:ldap /var/lib/ldap/*
	
	/bin/ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/cosine.ldif >/dev/null 2>&1
        /bin/ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/nis.ldif >/dev/null 2>&1
        /bin/ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/inetorgperson.ldif >/dev/null 2>&1
        /bin/ldapadd -Y EXTERNAL -H ldapi:/// -f /etc/openldap/schema/core.ldif >/dev/null 2>&1

        cp $SCRIPT_HOME/local.ldif /etc/openldap/schema/
        /bin/ldapadd -x -H ldapi:/// -D "cn=config" -w $SERVICE_LDAP_BIND_PASS -f /etc/openldap/schema/local.ldif
        sleep 5
	
        /bin/ldapadd -x -w $SERVICE_LDAP_BIND_PASS -D "cn=Manager,dc=strongauth,dc=com" -f $STRONGKEY_HOME/SKFS-OpenDJ-export.ldif

        /bin/ldapmodify -Y external -H ldapi:/// -f add_slapdlog.ldif >/dev/null 2>&1
        systemctl force-reload slapd >/dev/null 2>&1
        cp $SCRIPT_HOME/10-slapd.conf /etc/rsyslog.d/
        service rsyslog restart

	# Stop OpenDJ from startup
	systemctl stop opendjd
	systemctl disable opendjd

	mkdir -p $STRONGKEY_HOME/skce/etc
        echo "ldape.cfg.property.service.ce.ldap.ldapurl=ldap://localhost:389" >> /usr/local/strongkey/skce/etc/skce-configuration.properties
        chown -R strongkey:strongkey $STRONGKEY_HOME/skce

	# Remove OpenDJ from PATH
	sed -i 's|$OPENDJ_HOME/bin:||' /etc/skfsrc
	
	mv $STRONGKEY_HOME/fido/VersionFidoServer-4.4.1 $STRONGKEY_HOME/fido/VersionFidoServer-4.4.2
fi # End of 4.4.2 Upgrade

# Start Glassfish
echo
echo "Starting Glassfish..."
service glassfishd restart

#adding sleep to ensure glassfish starts up correctly
sleep 10

# Deploy NEW SKFS
echo
echo "Deploying new skfs build..."

check_exists "$SCRIPT_HOME/$LATEST_SKFS_BUILD"

cp $SCRIPT_HOME/$LATEST_SKFS_BUILD /tmp
# Deploy SKFS
$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password deploy /tmp/$LATEST_SKFS_BUILD

rm /tmp/$LATEST_SKFS_BUILD
rm /tmp/password

echo
echo "Restarting glassfish..."
service glassfishd restart

echo
echo "Upgrade finished!"

exit 0
