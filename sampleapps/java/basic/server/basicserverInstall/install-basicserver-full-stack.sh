#!/bin/bash
###################################################################################
# Copyright StrongAuth, Inc. All Rights Reserved.
#
# Use of this source code is governed by the Gnu Lesser General Public License 2.3.
# The license can be found at https://github.com/StrongKey/fido2/LICENSE
###################################################################################
# Uncomment to show detailed installation process
#SHOWALL=1

##########################################
##########################################
# Server Passwords
LINUX_PASSWORD=ShaZam123
MARIA_ROOT_PASSWORD=BigKahuna
MARIA_SKFSDBUSER_PASSWORD=AbracaDabra

XMXSIZE=512m
BUFFERPOOLSIZE=512m

##########################################
##########################################

# Flags to indicate if a module should be installed
INSTALL_GLASSFISH=Y
INSTALL_MARIA=Y
INSTALL_FIDO=Y

# Start Required Distributables
GLASSFISH=payara-4.1.2.181.zip
JEMALLOC=jemalloc-3.6.0-1.el7.x86_64.rpm
MARIA=mariadb-10.2.13-linux-x86_64.tar.gz
MARIACONJAR=mariadb-java-client-2.2.2.jar
# End Required Distributables

# Other vars
STRONGKEY_HOME=/usr/local/strongkey
SKFS_HOME=$STRONGKEY_HOME/skfs
GLASSFISH_HOME=$STRONGKEY_HOME/payara41/glassfish
GLASSFISH_CONFIG=$GLASSFISH_HOME/domains/domain1/config
MARIAVER=mariadb-10.2.13-linux-x86_64
MARIATGT=mariadb-10.2.13
MARIA_HOME=$STRONGKEY_HOME/$MARIATGT
SKFS_SOFTWARE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

function check_exists {
for ARG in "$@"
do
    if [ ! -f $ARG ]; then
        >&2 echo -e "\E[31m$ARG Not Found. Check to ensure the file exists in the proper location and try again.\E[0m"
        exit 1
    fi
done
}

function get_ip {
        # Try using getent if it is available, best option
        if ! getent hosts $1 2>/dev/null | awk '{print $1; succ=1} END{exit !succ}'; then

                # If we are here, likely don't have getent. Try reading /etc/hosts.
                if ! awk "/^[^#].*$1/ "'{ print $1; succ=1} END{exit !succ}' /etc/hosts; then

                        # Wasn't in /etc/hosts, try DNS
                        if ! dig +short +time=2 +retry=1 +tries=1 $1 | grep '.' 2>/dev/null; then

                                # Can't resolve IP
                                >&2 echo -e "\E[31mFQDN $1 not resolvable. Modify DNS or add a hosts entry and try again.\E[0m"
                                exit 1
                        fi
                fi
        fi
}

# install required packages
YUM_CMD=$(which yum  2>/dev/null)
APT_GET_CMD=$(which apt-get 2>/dev/null)

echo "Installing required linux packages ..."
if [[ ! -z $YUM_CMD ]]; then
    yum -y install unzip libaio java-1.8.0-openjdk ncurses-compat-libs curl >/dev/null 2>&1
elif [[ ! -z $APT_GET_CMD ]]; then
    apt-get update >/dev/null 2>&1
    apt install unzip libaio1 openjdk-8-jdk-headless daemon rng-tools curl -y >/dev/null 2>&1
    # modify rng tools to use dev urandom as the vm may not have a harware random number generator
    if ! grep -q "^HRNGDEVICE=/dev/urandom" /etc/default/rng-tools ; then
            echo "HRNGDEVICE=/dev/urandom" | sudo tee -a /etc/default/rng-tool
    fi
    systemctl restart rng-tools
else
   echo "error can't install packages"
   exit 1;
fi


# download required software
if [ ! -f $SKFS_SOFTWARE/$GLASSFISH ]; then
        echo "Downloading Payara ..."
        wget http://repo1.maven.org/maven2/fish/payara/distributions/payara/4.1.2.181/payara-4.1.2.181.zip -q
fi

if [ ! -f $SKFS_SOFTWARE/$MARIA ]; then
        echo "Downloading MARIADB SERVER ..."
        wget https://downloads.mariadb.org/interstitial/mariadb-10.2.13/bintar-linux-x86_64/mariadb-10.2.13-linux-x86_64.tar.gz/from/http%3A//ftp.hosteurope.de/mirror/archive.mariadb.org/ -O mariadb-10.2.13-linux-x86_64.tar.gz -q
fi

if [ ! -f $SKFS_SOFTWARE/$MARIACONJAR ]; then
        echo "Downloading MARIADB JAVA CONNECTOR ..."
        wget https://downloads.mariadb.com/Connectors/java/connector-java-2.2.2/mariadb-java-client-2.2.2.jar -q
fi

if [ ! -f $SKFS_SOFTWARE/$JEMALLOC ]; then
        echo "Downloading JEMALLOC ..."
        wget https://download-ib01.fedoraproject.org/pub/epel/7/x86_64/Packages/j/jemalloc-3.6.0-1.el7.x86_64.rpm -q
fi


# Make sure we can resolve our own hostname
get_ip "$(hostname)" > /dev/null

# Check that the script is run as root
if [ $UID -ne 0 ]; then
        >&2 echo -e "\E[31m$0 must be run as root\E[0m"
        exit 1
fi

# Check that strongkey doesn't already exist
if $(id strongkey &> /dev/null); then
        >&2 echo -e "\E[31m'strongkey' user already exists. Run cleanup.sh and try again.\E[0m"
        exit 1
fi

# Check that all files are present
if [ $INSTALL_GLASSFISH = 'Y' ]; then
        check_exists $SKFS_SOFTWARE/$GLASSFISH
fi

if [ $INSTALL_MARIA = 'Y' ]; then
        check_exists $SKFS_SOFTWARE/$MARIA $SKFS_SOFTWARE/$JEMALLOC $SKFS_SOFTWARE/$MARIACONJAR
fi

# Make backup directory if not there
if [ -d /etc/org ]; then
        :
else
        mkdir /etc/org
        if [ -f /etc/bashrc ]; then
                cp /etc/bashrc /etc/org
        else
                cp /etc/bash.bashrc /etc/org
        fi
        cp /etc/sudoers /etc/org
fi

# Create the strongkey group and user, and add it to /etc/sudoers
groupadd strongkey
useradd -g strongkey -c"StrongKey" -d $STRONGKEY_HOME -m strongkey
echo strongkey:$LINUX_PASSWORD | /usr/sbin/chpasswd
cat >> /etc/sudoers <<-EOFSUDOERS
## SKFS permissions
Cmnd_Alias SKFS_COMMANDS = /usr/sbin/service glassfishd start, /usr/sbin/service glassfishd stop, /usr/sbin/service glassfishd restart, /usr/sbin/service mysqld start, /usr/sbin/service mysqld stop, /usr/sbin/service mysqld restart
strongkey ALL=SKFS_COMMANDS
EOFSUDOERS

##### Create skfsrc #####
cat > /etc/skfsrc << EOFSKFSRC
    export GLASSFISH_HOME=$GLASSFISH_HOME
        export MYSQL_HOME=$MARIA_HOME
   export STRONGKEY_HOME=$STRONGKEY_HOME
              export PATH=\$GLASSFISH_HOME/bin:\$MYSQL_HOME/bin:\$STRONGKEY_HOME/bin:/usr/lib64/qt-3.3/bin:/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin:/root/bin:/root/bin
alias str='cd $STRONGKEY_HOME'
alias dist='cd $STRONGKEY_HOME/dist'
alias aslg='cd $GLASSFISH_HOME/domains/domain1/logs'
alias ascfg='cd $GLASSFISH_HOME/domains/domain1/config'
alias tsl='tail --follow=name $GLASSFISH_HOME/domains/domain1/logs/server.log'
alias mys='mysql -u skfsdbuser -p\`dbpass 2> /dev/null\` skfs'
alias java='java -Djavax.net.ssl.trustStore=\$STRONGKEY_HOME/certs/cacerts '
EOFSKFSRC

if [ -f /etc/bashrc ]; then
        echo ". /etc/skfsrc" >> /etc/bashrc
else
        echo ". /etc/skfsrc" >> /etc/bash.bashrc
fi

# Make needed directories
mkdir -p $STRONGKEY_HOME/certs $STRONGKEY_HOME/Desktop $STRONGKEY_HOME/dbdumps $STRONGKEY_HOME/lib $STRONGKEY_HOME/bin

##### Install Fido #####
if [ $INSTALL_FIDO = 'Y' ]; then

        echo "Installing Scripts..."

        cp $SKFS_SOFTWARE/certimport.sh $STRONGKEY_HOME/bin
        cp $STRONGKEY_HOME/bin/* $STRONGKEY_HOME/Desktop/

        chmod 700 $STRONGKEY_HOME/Desktop/*.sh

fi

##### MariaDB #####
if [ $INSTALL_MARIA = 'Y' ]; then
        echo "Installing MariaDB..."
        if [ $SHOWALL ]; then
                tar zxvf $SKFS_SOFTWARE/$MARIA -C $STRONGKEY_HOME
        else
                tar zxf $SKFS_SOFTWARE/$MARIA -C $STRONGKEY_HOME
        fi

        rpm -ivh $SKFS_SOFTWARE/$JEMALLOC &> /dev/null
        sed -i 's|^mysqld_ld_preload=$|mysqld_ld_preload=/usr/lib64/libjemalloc.so.1|' $STRONGKEY_HOME/$MARIAVER/bin/mysqld_safe
        cp $STRONGKEY_HOME/$MARIAVER/support-files/mysql.server /etc/init.d/mysqld
        chmod 755 /etc/init.d/mysqld
        /lib/systemd/systemd-sysv-install enable mysqld
        mkdir $STRONGKEY_HOME/$MARIAVER/backups $STRONGKEY_HOME/$MARIAVER/binlog $STRONGKEY_HOME/$MARIAVER/log $STRONGKEY_HOME/$MARIAVER/ibdata
        mv $STRONGKEY_HOME/$MARIAVER $STRONGKEY_HOME/$MARIATGT

        DBSIZE=10M
        SERVER_BINLOG=$STRONGKEY_HOME/$MARIATGT/binlog/skfs-binary-log

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
fi

##### Payara #####
if [ $INSTALL_GLASSFISH = 'Y' ]; then
        echo "Installing Payara..."
        if [ $SHOWALL ]; then
                unzip $SKFS_SOFTWARE/$GLASSFISH -d $STRONGKEY_HOME
        else
                unzip $SKFS_SOFTWARE/$GLASSFISH -d $STRONGKEY_HOME > /dev/null
        fi

        if [ -d /root/.gfclient ]; then
                rm -rf /root/.gfclient
        fi

        if [ -d $STRONGKEY_HOME/.gfclient ]; then
                rm -rf $STRONGKEY_HOME/.gfclient
        fi

        cp $SKFS_SOFTWARE/glassfishd /etc/init.d
        chmod 755 /etc/init.d/glassfishd
        /lib/systemd/systemd-sysv-install enable glassfishd

        keytool -genkeypair -alias skfs -keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit -keypass changeit -keyalg RSA -keysize 2048 -sigalg SHA256withRSA -validity 3562 -dname "CN=$(hostname),OU=\"Demo FIDO Application\"" &>/dev/null
        keytool -changealias -alias s1as -destalias s1as.original -keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit &>/dev/null
        keytool -changealias -alias skfs -destalias s1as -keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit &>/dev/null
        sed -ri 's|^(com.sun.enterprise.server.logging.GFFileHandler.rotationOnDateChange=).*|\1true|
                 s|^(com.sun.enterprise.server.logging.GFFileHandler.rotationLimitInBytes=).*|\1200000000|' $GLASSFISH_CONFIG/logging.properties
        keytool -exportcert -alias s1as -file $STRONGKEY_HOME/certs/$(hostname).der --keystore $GLASSFISH_CONFIG/keystore.jks -storepass changeit &>/dev/null
        keytool -importcert -noprompt -alias $(hostname) -file $STRONGKEY_HOME/certs/$(hostname).der --keystore $STRONGKEY_HOME/certs/cacerts -storepass changeit &>/dev/null
        keytool -importcert -noprompt -alias $(hostname) -file $STRONGKEY_HOME/certs/$(hostname).der --keystore $GLASSFISH_CONFIG/cacerts.jks -storepass changeit &>/dev/null

        ##### MariaDB JDBC Driver #####
        echo "Installing JDBC Driver..."
        cp $SKFS_SOFTWARE/$MARIACONJAR $GLASSFISH_HOME/lib
fi


##### Change ownership of files #####
chown -R strongkey:strongkey $STRONGKEY_HOME

##### Start MariaDB and Payara #####
echo -n "Creating $DBSIZE SKFS Internal Database..."
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
$MARIA_HOME/bin/mysql -u root mysql -e "update user set password=password('$MARIA_ROOT_PASSWORD') where user = 'root';
                                                    delete from mysql.db where host = '%';
                                                    delete from mysql.user where user = '';
                                                    create database skfs;
                                                    grant all on skfs.* to skfsdbuser@localhost identified by '$MARIA_SKFSDBUSER_PASSWORD';
                                                    flush privileges;"
cd $SKFS_SOFTWARE/basicserverSQL
$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs --quick < create.txt

touch $STRONGKEY_HOME/webauthntutorial/etc/webauthntutorial.properties
echo "webauthntutorial.cfg.property.apiuri=https://$(hostname):8181/api" > $STRONGKEY_HOME/webauthntutorial/etc/webauthntutorial.properties

chown strongkey:strongkey $STRONGKEY_HOME/webauthntutorial/etc/webauthntutorial.properties
service glassfishd start
sleep 10

##### Perform Payara Tasks #####
$GLASSFISH_HOME/bin/asadmin set server.network-config.network-listeners.network-listener.http-listener-1.enabled=false
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.request-timeout-seconds=7200
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl3-tls-ciphers=+TLS_ECDHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_DHE_RSA_WITH_AES_256_GCM_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA384,+TLS_ECDHE_RSA_WITH_AES_256_CBC_SHA,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA256,+TLS_DHE_RSA_WITH_AES_256_CBC_SHA
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl2-enabled=false
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.ssl3-enabled=false
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.tls-enabled=false
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.ssl.tls11-enabled=false
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.trace-enabled=false
$GLASSFISH_HOME/bin/asadmin set server.network-config.protocols.protocol.http-listener-2.http.xpowered-by=false
$GLASSFISH_HOME/bin/asadmin create-jdbc-connection-pool \
        --datasourceclassname org.mariadb.jdbc.MySQLDataSource \
        --restype javax.sql.ConnectionPoolDataSource \
        --isconnectvalidatereq=true \
        --validationmethod meta-data \
        --property ServerName=localhost:DatabaseName=skfs:port=3306:user=skfsdbuser:password=$MARIA_SKFSDBUSER_PASSWORD:DontTrackOpenResources=true \
        SKFSPool
$GLASSFISH_HOME/bin/asadmin create-jdbc-resource --connectionpoolid SKFSPool jdbc/skfs
$GLASSFISH_HOME/bin/asadmin set server.resources.jdbc-connection-pool.SKFSPool.max-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=10

$GLASSFISH_HOME/bin/asadmin delete-jvm-options $($GLASSFISH_HOME/bin/asadmin list-jvm-options | sed -n '/\(-XX:NewRatio\|-XX:MaxPermSize\|-XX:PermSize\|-client\|-Xmx\|-Xms\)/p' | sed 's|:|\\\\:|' | tr '\n' ':')
$GLASSFISH_HOME/bin/asadmin create-jvm-options -Djtss.tcs.ini.file=$STRONGKEY_HOME/lib/jtss_tcs.ini:-Djtss.tsp.ini.file=$STRONGKEY_HOME/lib/jtss_tsp.ini:-Xmx${XMXSIZE}:-Xms${XMXSIZE}:-server:-Djdk.tls.ephemeralDHKeySize=2048:-Dproduct.name="":-XX\\:-DisableExplicitGC

echo "Deploying StrongKey FidoServer ..."
cp $SKFS_SOFTWARE/basicserver.war /tmp
$GLASSFISH_HOME/bin/asadmin deploy /tmp/basicserver.war

rm /tmp/fidoserver.ear
echo "Done!"
