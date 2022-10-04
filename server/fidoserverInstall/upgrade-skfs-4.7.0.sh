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

GLASSFISH_HOME=$STRONGKEY_HOME/payara5/glassfish
MARIA_HOME=$STRONGKEY_HOME/mariadb-10.6.8

GLASSFISH_ADMIN_PASSWORD=adminadmin
MARIA_ROOT_PASSWORD=BigKahuna
MARIA_SKFSDBUSER_PASSWORD=AbracaDabra
SERVICE_LDAP_BIND_PASS=Abcd1234!
SERVICE_LDAP_BASEDN='dc=strongauth,dc=com'

SAKA_DID=1

ROLLBACK=Y

# 4.7.0 Upgrade Variables
SAML_RESPONSE=false
SAML_CITRIX=false
SAML_DURATION=15
SAML_KEYGEN_DN='/C=US/ST=California/L=Cupertino/O=StrongAuth/OU=Engineering'
SAML_CERTS_PER_SERVER=3
SAML_TIMEZONE=UTC
SAML_KEYSTORE_PASS=Abcd1234!
SAML_KEY_VALIDITY=365

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

# Check that the SKFS is at least version 4.6.0
if [[ $CURRENT_SKFS_BUILDNO < "4.6.0" ]]; then
	>&2 echo "SKFS must be at least version 4.6.0 in order to upgrade using this script."
	exit 1
fi

# Determine which package manager is on the system
YUM_CMD=$(which yum  2>/dev/null)
APT_GET_CMD=$(which apt-get 2>/dev/null)

# Undeploy SKFS
echo
echo "Undeploying old skfs build..."
$GLASSFISH_HOME/bin/asadmin --user admin --passwordfile /tmp/password undeploy fidoserver

# Start upgrade to 4.7.0
if [[ $CURRENT_SKFS_BUILDNO < "4.7.0" ]]; then
	echo "Upgrading to 4.7.0"

	echo "
skfs.cfg.property.saml.response=$SAML_RESPONSE
skfs.cfg.property.saml.certsperserver=$SAML_CERTS_PER_SERVER
skfs.cfg.property.saml.timezone=$SAML_TIMEZONE
skfs.cfg.property.saml.citrix=$SAML_CITRIX
skfs.cfg.property.saml.assertion.duration=$SAML_DURATION
skfs.cfg.property.saml.issuer.entity.name=https://$(hostname)/" >> $STRONGKEY_HOME/skfs/etc/skfs-configuration.properties

	# Generate SAML keystores
        $SCRIPT_HOME/keygen-saml.sh $SAML_KEYGEN_DN $($MARIA_HOME/bin/mysql -u skfsdbuser -p${MARIA_SKFSDBUSER_PASSWORD} skfs -B --skip-column-names -e "select count(fqdn) from servers;") $SAML_CERTS_PER_SERVER $SAKA_DID $SAML_KEYSTORE_PASS $SAML_KEY_VALIDITY
	NUM_DOMAINS=$($MARIA_HOME/bin/mysql -u skfsdbuser -p${MARIA_SKFSDBUSER_PASSWORD} skfs -B --skip-column-names -e "select count(*) from domains;")
	for (( DID = 2; DID <= $NUM_DOMAINS ; DID++ ))
	do
		$SCRIPT_HOME/keygen-saml.sh $SAML_KEYGEN_DN $($MARIA_HOME/bin/mysql -u skfsdbuser -p${MARIA_SKFSDBUSER_PASSWORD} skfs -B --skip-column-names -e "select count(fqdn) from servers;") $SAML_CERTS_PER_SERVER $DID $SAML_KEYSTORE_PASS $SAML_KEY_VALIDITY
	done
        chown strongkey:strongkey $STRONGKEY_HOME/skfs/keystores/samlsigningtruststore.bcfks $STRONGKEY_HOME/skfs/keystores/samlsigningkeystore.bcfks

	mv $STRONGKEY_HOME/fido/VersionFidoServer-4.6.0 $STRONGKEY_HOME/fido/VersionFidoServer-4.7.0
fi # End of 4.7.0 Upgrade


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
