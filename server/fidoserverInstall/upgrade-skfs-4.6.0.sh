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

. /etc/skfsrc

CURRENT_SKFS_BUILDNO=$(ls -1 $STRONGKEY_HOME/fido/Version* 2> /dev/null | sed -r 's|.*VersionFidoServer-||')

SCRIPT_HOME=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

SOFTWARE_HOME=/usr/local/software
STRONGKEY_HOME=/usr/local/strongkey

GLASSFISH_ADMIN_PASSWORD=adminadmin
MARIA_ROOT_PASSWORD=BigKahuna
MARIA_SKFSDBUSER_PASSWORD=AbracaDabra
SERVICE_LDAP_BIND_PASS=Abcd1234!
SERVICE_LDAP_BASEDN='dc=strongauth,dc=com'

ROLLBACK=Y

# 4.6.0 Upgrade Variables
# RESPONSE_DETAIL
RESPONSE_DETAIL=false                   # Property to determine if webservices should return detailed information in response.
RESPONSE_DETAIL_WEBSERVICES=R,A         # Property to determine what webservices should return the detailed information. Default : R,A ( Reg / Auth ). Comma separated list of all the allowed web services example : R, or R,A or A and so on.
RESPONSE_DETAIL_FORMAT=default          # Property to determine the format for the returned response details. Allowed values : default | webauthn2

function check_exists {
for ARG in "$@"
do
    if [ ! -f $ARG ]; then
        >&2 echo -e "$ARG Not Found. Check to ensure the file exists in the proper location and try again."
        exit 1
    fi
done
}

# Check that the script is run as root
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

# Get GlassFish admin password
echo "$GLASSFISH_ADMIN_PASSWORD" > /tmp/password
while ! $GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password list . &> /dev/null; do
        echo -n "This upgrade requires the glassfish 'admin' password. Please enter the password now: "
        echo
        read -s GLASSFISH_ADMIN_PASSWORD
        echo "AS_ADMIN_PASSWORD=$GLASSFISH_ADMIN_PASSWORD" > /tmp/password
done

# Check that the SKFS is at least version 4.5.0
if [[ $CURRENT_SKFS_BUILDNO < "4.5.0" ]]; then
	>&2 echo "SKFS must be at least version 4.5.0 in order to upgrade using this script."
	exit 1
fi

# Determine which package manager is on the system
YUM_CMD=$(which yum  2>/dev/null)
APT_GET_CMD=$(which apt-get 2>/dev/null)

# Undeploy SKFS
echo
echo "Undeploying old skfs build..."
$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password undeploy fidoserver

# Start upgrade to 4.6.0
if [[ $CURRENT_SKFS_BUILDNO < "4.6.0" ]]; then
	echo "Upgrading to 4.6.0"

	# Upgrade to Java 11
	echo "Installing JDK 11..."
	if [[ ! -z $YUM_CMD ]]; then
	    yum -y install java-11-openjdk >/dev/null 2>&1
	elif [[ ! -z $APT_GET_CMD ]]; then
	    apt-get update >/dev/null 2>&1
	    apt install openjdk-11-jdk -y >/dev/null 2>&1						# WIP
	else
	     echo "error can't install java 11"
	     exit 1;
	fi
	export JAVA_HOME=/usr/lib/jvm/jre-11
	export PATH=$JAVA_HOME/bin:$PATH
	update-alternatives --set java java-11-openjdk.x86_64
	sed -i "s|alias java=.*|alias java='java -Djavax.net.ssl.trustStore=$STRONGKEY_HOME/certs/cacerts -Djavax.net.ssl.trustStorePassword="changeit" '|" /etc/skfsrc
	. /etc/skfsrc

	mkdir -p $STRONGKEY_HOME/upgrade-4.6.0
	cd $STRONGKEY_HOME/upgrade-4.6.0

	# Upgrade to Payara 5.2021.6
	echo "Upgrading to Payara 5.2021.6..."
	GLASSFISH_HOME=$STRONGKEY_HOME/payara5/glassfish
	GLASSFISH_CONFIG=$GLASSFISH_HOME/domains/domain1/config

	service glassfishd stop
	wget https://repo1.maven.org/maven2/fish/payara/distributions/payara/5.2021.6/payara-5.2021.6.zip -q
	mv $STRONGKEY_HOME/payara5 $STRONGKEY_HOME/payara-5.2020.7
	unzip payara-5.2021.6.zip -d $STRONGKEY_HOME > /dev/null
	
	# Move files to new Payara version
	mv $GLASSFISH_CONFIG/domain.xml $GLASSFISH_CONFIG/domain.xml.bak
	cp $STRONGKEY_HOME/payara-5.2020.7/glassfish/domains/domain1/config/domain.xml $STRONGKEY_HOME/payara-5.2020.7/glassfish/domains/domain1/config/keystore.jks $STRONGKEY_HOME/payara-5.2020.7/glassfish/domains/domain1/config/logging.properties $GLASSFISH_CONFIG
	cp -r $STRONGKEY_HOME/payara-5.2020.7/glassfish/domains/domain1/docroot/* $GLASSFISH_HOME/domains/domain1/docroot/
	cp $STRONGKEY_HOME/payara-5.2020.7/glassfish/lib/*.jar $GLASSFISH_HOME/lib

	chown -R strongkey. $STRONGKEY_HOME/payara5
	if [ "$ROLLBACK" == 'N' ]; then
		rm -rf $STRONGKEY_HOME/payara-5.2020.7
	fi
	
	echo "AS_ADMIN_PASSWORD=" > /tmp/password
	echo "AS_ADMIN_NEWPASSWORD=$GLASSFISH_ADMIN_PASSWORD" >> /tmp/password
	$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password change-admin-password --domain_name domain1
	service glassfishd start
	chown -R strongkey. $STRONGKEY_HOME/payara5
	echo "AS_ADMIN_PASSWORD=$GLASSFISH_ADMIN_PASSWORD" > /tmp/password

	$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password enable-secure-admin --instancealias=s1as
	$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password set server.network-config.protocols.protocol.sec-admin-listener.ssl.ssl3-tls-ciphers=+TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA
	$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password set server.network-config.protocols.protocol.sec-admin-listener.ssl.ssl2-enabled=false
	$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password set server.network-config.protocols.protocol.sec-admin-listener.ssl.ssl3-enabled=false
	$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password set server.network-config.protocols.protocol.sec-admin-listener.http.trace-enabled=false
	$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password set server.network-config.protocols.protocol.sec-admin-listener.http.xpowered-by=false

	# Upgrade to MariaDB 10.6.8
	echo "Upgrading to MariaDB 10.6.8"
	MARIA_OLD=$STRONGKEY_HOME/mariadb-10.5.8
	MARIA_NEW=$STRONGKEY_HOME/mariadb-10.6.8
	service mysqld stop
	wget https://archive.mariadb.org/mariadb-10.6.8/bintar-linux-systemd-x86_64/mariadb-10.6.8-linux-systemd-x86_64.tar.gz -q
	tar zxf mariadb-10.6.8-linux-systemd-x86_64.tar.gz -C $STRONGKEY_HOME
	mv $STRONGKEY_HOME/mariadb-10.6.8-linux-systemd-x86_64 $MARIA_NEW

	echo "Copying ibdata (This may take some time)..."
	if [ "$ROLLBACK" == 'N' ]; then
		mv  $MARIA_OLD/ibdata $MARIA_OLD/log $MARIA_OLD/backups $MARIA_NEW
	else
		cp -r  $MARIA_OLD/ibdata $MARIA_OLD/log $MARIA_OLD/backups $MARIA_NEW
	fi
	mkdir $MARIA_NEW/binlog
	chown -R strongkey. $MARIA_NEW
	cp /etc/my.cnf /etc/my.cnf.bak
	sed -i "s|$MARIA_OLD|$MARIA_NEW|" /etc/my.cnf
	sed -i "s|export MYSQL_HOME=.*|export MYSQL_HOME=$MARIA_NEW|" /etc/skfsrc

	. /etc/skfsrc

	killall -9 mariadbd mysqld_safe

	service mysqld start

	echo "Upgrading Mariadb (This may take some time)..."
	mysql_upgrade -u root -p$MARIA_ROOT_PASSWORD --skip-version-check >/dev/null

	echo "Restarting Mariadb (This may take some time)..."
	service mysqld restart

	# 4.6.0 Upgrades
	echo "skfs.cfg.property.apple.rootca.url=$STRONGKEY_HOME/skfs/applerootca.crt

skfs.cfg.property.return.responsedetail=$RESPONSE_DETAIL
skfs.cfg.property.return.responsedetail.webservices=$RESPONSE_DETAIL_WEBSERVICES
skfs.cfg.property.return.responsedetail.format=$RESPONSE_DETAIL_FORMAT" >> $STRONGKEY_HOME/skfs/etc/skfs-configuration.properties

	$MARIA_NEW/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs -e "alter table fido_keys modify column status enum('Active','Inactive','Deleted','Revoked','Other');"

	mv $STRONGKEY_HOME/fido/VersionFidoServer-4.5.0 $STRONGKEY_HOME/fido/VersionFidoServer-4.6.0
fi # End of 4.6.0 Upgrade


# Start Glassfish
echo
echo "Starting Glassfish..."
service glassfishd restart

#adding sleep to ensure glassfish starts up correctly
sleep 10

# Deploy NEW SKFS
echo
echo "Deploying new skfs build..."

check_exists "$SCRIPT_HOME/fidoserver.ear"

cp $SCRIPT_HOME/fidoserver.ear /tmp
# Deploy SKFS
$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password deploy /tmp/fidoserver.ear

rm /tmp/fidoserver.ear
rm /tmp/password

echo
echo "Restarting glassfish..."
service glassfishd restart

echo
echo "Upgrade finished!"

exit 0
