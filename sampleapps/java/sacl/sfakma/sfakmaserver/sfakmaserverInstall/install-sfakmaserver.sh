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
SFAKMA_SOFTWARE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

MARIA_DEMODBUSER_PASSWORD=AbracaDabra
MARIA_ROOT_PASSWORD=BigKahuna

# Create DB
$MARIA_HOME/bin/mysql -u root mysql -p${MARIA_ROOT_PASSWORD} -e "create database sfakma;
                                            grant all on sfakma.* to sfakmadbuser@localhost identified by '$MARIA_DEMODBUSER_PASSWORD';
                                            flush privileges;"

# Create DB Tables
cd $SFAKMA_SOFTWARE/sfakmaserverSQL
$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=sfakmadbuser --password=${MARIA_DEMODBUSER_PASSWORD} --database=sfakma --quick < create.txt

# Create JDBC connection
$GLASSFISH_HOME/bin/asadmin create-jdbc-connection-pool \
        --datasourceclassname org.mariadb.jdbc.MySQLDataSource \
        --restype javax.sql.ConnectionPoolDataSource \
        --isconnectvalidatereq=true \
        --validationmethod meta-data \
        --property ServerName=localhost:DatabaseName=sfakma:port=3306:user=sfakmadbuser:password=$MARIA_DEMODBUSER_PASSWORD:DontTrackOpenResources=true \
        SFAKMAPool
$GLASSFISH_HOME/bin/asadmin create-jdbc-resource --connectionpoolid SFAKMAPool jdbc/sfakma
$GLASSFISH_HOME/bin/asadmin set server.resources.jdbc-connection-pool.SFAKMAPool.max-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=10

# Give application server permission to read configuration file
chown -R strongkey:strongkey $STRONGKEY_HOME/sfakma

# Deploy sample application
echo "Deploying SFAKMA Server ..."
cp $SFAKMA_SOFTWARE/sfakmaserver.war /tmp
$GLASSFISH_HOME/bin/asadmin deploy --contextroot sfakma --name sfakma /tmp/sfakmaserver.war
