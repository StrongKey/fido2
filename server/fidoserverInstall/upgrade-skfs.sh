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

SCRIPT_HOME=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
SOFTWARE_HOME=/usr/local/software
STRONGKEY_HOME=/usr/local/strongkey

GLASSFISH_ADMIN_PASSWORD=adminadmin
MYSQL_ROOT_PASSWORD=BigKahuna
MARIA_SKFSDBUSER_PASSWORD=AbracaDabra

GLASSFISH=payara-4.1.2.181.zip
MARIADB=mariadb-10.2.30-linux-glibc_214-x86_64.tar.gz
MARIAVER=mariadb-10.2.30-linux-glibc_214-x86_64
MARIATGT=mariadb-10.2.30
MARIA_HOME=$STRONGKEY_HOME/$MARIATGT
DBCLIENT=mariadb-java-client-2.2.6.jar

LATEST_SKFS_BUILD=fidoserver.ear

ROLLBACK='Y'

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

if [ ! -f $SCRIPT_HOME/$MARIADB ]; then
        echo -n "Downloading Mariadb Server ... "
        wget https://downloads.mariadb.com/MariaDB/mariadb-10.2.30/bintar-linux-glibc_214-x86_64/mariadb-10.2.30-linux-glibc_214-x86_64.tar.gz -q
        echo "Successful"
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

# Start Glassfish
echo
echo "Starting Glassfish..."
service glassfishd start

#adding sleep to ensure glassfish starts up correctly
sleep 10

# Deploy NEW SKFS
echo
echo "Deploying new skfs build..."

check_exists "$SCRIPT_HOME/$LATEST_SKFS_BUILD"

cp $SCRIPT_HOME/$LATEST_SKFS_BUILD /tmp
# Deploy SKFS
$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password deploy  /tmp/$LATEST_SKFS_BUILD

rm /tmp/$LATEST_SKFS_BUILD
rm /tmp/password

echo
echo "Restarting glassfish..."
service glassfishd restart

echo
echo "Upgrade finished!"

exit 0
