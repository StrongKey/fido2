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
SKFS_SOFTWARE=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)

MARIA_SKFSDBUSER_PASSWORD=AbracaDabra

# Create DB Tables
cd $SKFS_SOFTWARE/basicserverSQL
$STRONGKEY_HOME/$MARIATGT/bin/mysql --user=skfsdbuser --password=$MARIA_SKFSDBUSER_PASSWORD --database=skfs --quick < create.txt

# Give application server permission to read configuration file
chown -R strongkey:strongkey $STRONGKEY_HOME/webauthntutorial

# Deploy sample application
echo "Deploying StrongKey FidoServer ..."
cp $SKFS_SOFTWARE/basicserver.war /tmp
$GLASSFISH_HOME/bin/asadmin deploy /tmp/basicserver.war
