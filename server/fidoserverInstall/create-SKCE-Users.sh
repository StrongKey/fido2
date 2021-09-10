#!/bin/bash
#
###############################################################
#
# This program is free software; you can redistribute it and/or
# modify it under the terms of the GNU Lesser General Public
# License, as published by the Free Software Foundation and
# available at https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html,
# version 2.1.
#
# This program is distributed in the hope that it will be useful,
# but WITHOUT ANY WARRANTY; without even the implied warranty of
# MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
# GNU Lesser General Public License for more details.
#
# Copyright (c) 2001-2020 StrongAuth, Inc.
#
# $Date$
# $Revision$
# $Author$
# $URL$
#
################################################################

. /etc/bashrc

MYSQL_SKLES_PASSWORD=$(grep \"password\" /usr/local/strongkey/payara5/glassfish/domains/domain1/config/domain.xml | sed "s|.*value=\"\(.*\)\".*|\1|" | head -1)
SERVICE_LDAP_BIND_PASS=Abcd1234!
SAKA_DID=$1
SERVICE_LDAP_SVCUSER_PASS=$2
SKCE_LDIF_PATH=$3

##########################################
##########################################

usage() {
        echo "Usage: "
        echo "${0##*/} <did> <skce-user-pass> <skce-ldif-path>"
        echo "Options:"
        echo "did              The SKCE did to create."
        echo "skce-user-pass   The desired password for the default ldap users that"
        echo "skce-ldif-path   The full path to the skce.ldif file (This should be located in the SKFS installation directory)"
        echo "                 will be created."
}


if [ -z $SERVICE_LDAP_SVCUSER_PASS ] || [ -z $SAKA_DID ]; then
        usage
        exit 1
fi



function check_exists {
for ARG in "$@"
do
    if [ ! -f $ARG ]; then
        echo -e "\E[31m$ARG Not Found. Check to ensure the file exists in the proper location and try again."
        tput sgr0
        exit 1

    fi
done
}

check_exists $SKCE_LDIF_PATH

# Check that we can find mysql
if ! [ -f $MYSQL_HOME/bin/mysql ]; then
        echo "MYSQL_HOME not set or missing. Try refreshing shell variables and try again."
        exit 1
fi

# Check if passwords are correct
if ! $MYSQL_HOME/bin/mysql -u skfsdbuser -p${MYSQL_SKLES_PASSWORD} skfs -e "\c" &> /dev/null; then
        >&2 echo -e "\E[31mMySQL 'skfsdbuser' password is incorrect.\E[0m"
        exit 1
fi

# Check if the SAKA domain has been created
if ! $MYSQL_HOME/bin/mysql -u skfsdbuser -p${MYSQL_SKLES_PASSWORD} skfs -B --skip-column-names -e "select * from domains where did=$SAKA_DID;" | grep "$SAKA_DID" &> /dev/null; then
        >&2 echo -e "\E[31mYou must make a SAKA domain before you can make the SKCE domain.\E[0m"
        exit 1
fi

SLDNAME=${SERVICE_LDAP_BASEDN%%,dc*}
sed -r "s|dc: strongauth|dc: ${SLDNAME#dc=}|
        s|did: .*|did: ${SAKA_DID}|
        s|did=[0-9]+,|did=${SAKA_DID},|
        s|^ou: [0-9]+|ou: ${SAKA_DID}|
        s|(domain( id)*) [0-9]*|\1 ${SAKA_DID}|
        s|userPassword: .*|userPassword: $SERVICE_LDAP_SVCUSER_PASS|" $SKCE_LDIF_PATH > /tmp/skce.ldif

echo "Importing default users..."
ldapadd -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" -f /tmp/skce.ldif

rm /tmp/skce.ldif

exit 0
