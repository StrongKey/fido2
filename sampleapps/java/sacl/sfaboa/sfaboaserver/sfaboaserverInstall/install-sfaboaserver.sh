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
MARIATGT=mariadb-10.5.8
GLASSFISH_HOME=$STRONGKEY_HOME/payara5/glassfish
MARIA_HOME=$STRONGKEY_HOME/$MARIATGT
SKFS_SOFTWARE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

MARIA_DEMODBUSER_PASSWORD=AbracaDabra
MARIA_ROOT_PASSWORD=BigKahuna

# Create DB Tables
cd $SKFS_SOFTWARE/sfaboaserverSQL
$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=sfaeco --password=${MARIA_DEMODBUSER_PASSWORD} --database=sfa --quick < create.txt

# Give application server permission to read configuration file
chown -R strongkey:strongkey $STRONGKEY_HOME/sfaboa

# Deploy sample application
echo "Deploying SFABOA Server ..."
cp $SKFS_SOFTWARE/sfaboaserver.war /tmp
$GLASSFISH_HOME/bin/asadmin deploy --contextroot sfaboa --name sfaboa /tmp/sfaboaserver.war
