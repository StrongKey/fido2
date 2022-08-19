#!/bin/bash
###################################################################################
# /**  
# * Copyright StrongAuth, Inc. All Rights Reserved.
# *
# * Use of this source code is governed by the GNU Lesser General Public License v2.1
# * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
# */
###################################################################################
# Uncomment to show detailed installation process
#SHOWALL=1

STRONGKEY_HOME=/usr/local/strongkey
MARIATGT=mariadb-10.6.8
GLASSFISH_HOME=$STRONGKEY_HOME/payara5/glassfish
MARIA_HOME=$STRONGKEY_HOME/$MARIATGT
SFAECO_SOFTWARE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

MARIA_DEMODBUSER_PASSWORD=AbracaDabra
MARIA_ROOT_PASSWORD=BigKahuna

# Create DB
$MARIA_HOME/bin/mysql -u root mysql -p${MARIA_ROOT_PASSWORD} -e "create database sfa;
                                            grant all on sfa.* to sfaeco@localhost identified by '$MARIA_DEMODBUSER_PASSWORD';
                                            flush privileges;"

# Create DB Tables
cd $SFAECO_SOFTWARE/sfaecoSQL
$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=sfaeco --password=${MARIA_DEMODBUSER_PASSWORD} --database=sfa --quick < create.txt

# Create JDBC connection
$GLASSFISH_HOME/bin/asadmin create-jdbc-connection-pool \
        --datasourceclassname org.mariadb.jdbc.MySQLDataSource \
        --restype javax.sql.ConnectionPoolDataSource \
        --isconnectvalidatereq=true \
        --validationmethod meta-data \
        --property ServerName=localhost:DatabaseName=sfa:port=3306:user=sfaeco:password=$MARIA_DEMODBUSER_PASSWORD:DontTrackOpenResources=true \
        SFAPool
$GLASSFISH_HOME/bin/asadmin create-jdbc-resource --connectionpoolid SFAPool jdbc/sfa
$GLASSFISH_HOME/bin/asadmin set server.resources.jdbc-connection-pool.SFAPool.max-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=10

# Give application server permission to read configuration file
chown -R strongkey:strongkey $STRONGKEY_HOME/sfa

# Deploy sample application
echo "Deploying SFABOA Server ..."
cp $SFAECO_SOFTWARE/sfaeco-ear-1.0.ear /tmp
$GLASSFISH_HOME/bin/asadmin deploy /tmp/sfaeco-ear-1.0.ear
