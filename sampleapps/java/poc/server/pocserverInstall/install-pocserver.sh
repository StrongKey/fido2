#!/bin/bash
###################################################################################
# Copyright StrongAuth, Inc. All Rights Reserved.
#
# Use of this source code is governed by the Gnu Lesser General Public License 2.3.
# The license can be found at https://github.com/StrongKey/fido2/LICENSE
###################################################################################
# Uncomment to show detailed installation process
#SHOWALL=1

STRONGKEY_HOME=/usr/local/strongkey
MARIATGT=mariadb-10.2.13
GLASSFISH_HOME=$STRONGKEY_HOME/payara41/glassfish
MARIA_HOME=$STRONGKEY_HOME/$MARIATGT
SKFS_SOFTWARE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

MARIA_DEMODBUSER_PASSWORD=AbracaDabra
MARIA_ROOT_PASSWORD=BigKahuna

# Create DB
$MARIA_HOME/bin/mysql -u root mysql -p${MARIA_ROOT_PASSWORD} -e "create database demo;
                                            grant all on demo.* to demodbuser@localhost identified by '$MARIA_DEMODBUSER_PASSWORD';
                                            flush privileges;"

# Create DB Tables
cd $SKFS_SOFTWARE/pocserverSQL
$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=demodbuser --password=${MARIA_DEMODBUSER_PASSWORD} --database=demo --quick < create.txt

# Create JDBC connection
$GLASSFISH_HOME/bin/asadmin create-jdbc-connection-pool \
	--datasourceclassname org.mariadb.jdbc.MySQLDataSource \
	--restype javax.sql.ConnectionPoolDataSource \
	--isconnectvalidatereq=true \
	--validationmethod meta-data \
	--property ServerName=localhost:DatabaseName=demo:port=3306:user=demodbuser:password=$MARIA_DEMODBUSER_PASSWORD:DontTrackOpenResources=true \
	DemoPool
$GLASSFISH_HOME/bin/asadmin create-jdbc-resource --connectionpoolid DemoPool jdbc/demo
$GLASSFISH_HOME/bin/asadmin set server.resources.jdbc-connection-pool.DemoPool.max-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.max-thread-pool-size=1000
$GLASSFISH_HOME/bin/asadmin set server.thread-pools.thread-pool.http-thread-pool.min-thread-pool-size=10

# Give application server permission to read configuration file
chown -R strongkey:strongkey $STRONGKEY_HOME/webauthntutorial

# Deploy sample application
echo "Deploying StrongKey FidoServer ..."
cp $SKFS_SOFTWARE/pocserver.war /tmp
$GLASSFISH_HOME/bin/asadmin deploy /tmp/pocserver.war
