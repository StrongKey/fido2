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
# Copyright (c) 2001-2021 StrongAuth, Inc.
#
# $Date$
# $Revision$
# $Author$
# $URL$
#
################################################################

. /etc/bashrc

SERVICE_LDAP_BIND_PASS=Abcd1234!
OPERATION=$1
SAKA_DID=$2
USERNAME=$3
allgroups="FidoRegistrationService-AuthorizedServiceCredentials,FidoAuthenticationService-AuthorizedServiceCredentials,FidoAuthorizationService-AuthorizedServiceCredentials,FidoAdministrationService-AuthorizedServiceCredentials,FidoCredentialService-AuthorizedServiceCredentials,FidoPolicyManagementService-AuthorizedServiceCredentials,FidoMonitoringService-AuthorizedServiceCredentials"


##########################################
##########################################

usage() {
        echo "Usage: "
        echo "${0##*/} addUser <did> <username>"
        echo "${0##*/} addUserToGroup <did> <username> <group(s)>"
        echo "${0##*/} getUserGroups <did> <username>"
        echo "${0##*/} changeUserPassword <did> <username>"
        echo "${0##*/} deleteUser <did> <username>"
        echo "Options:"
        echo "did              The domain ID"
        echo "username         Username of the user or admin user"
        echo "group(s)         List of groups separated by commas"
}

if [ -z $OPERATION ] || [ -z $SAKA_DID ] || [ -z $USERNAME ]; then
        usage
        exit 1
fi

#Test If Default LDAP Password is Used
ldapwhoami -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" 2> /tmp/Error 1> /dev/null
ERROR=$(</tmp/Error)
rm /tmp/Error
if [ -n "$ERROR" ]; then
    echo "Enter LDAP Bind Password:"
    read -s SERVICE_LDAP_BIND_PASS
    echo ""
fi


if [ "$OPERATION" = "addUser" ]; then
    echo "Enter Password for New User:"
    PASSWORD=$(slappasswd -h {SSHA})
    if [ -z "$PASSWORD" ]; then
      exit 1
    fi



    cat > /tmp/ldapuser.ldif << LDAPUSER
dn: cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
changetype: add
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
objectClass: top
userPassword: $PASSWORD
givenName: $USERNAME
cn: $USERNAME
sn: $USERNAME
LDAPUSER
    ldapmodify -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" -f /tmp/ldapuser.ldif 2> /tmp/Error
    ERROR=$(</tmp/Error)
    rm /tmp/ldapuser.ldif /tmp/Error
    if [ -z "$ERROR" ]; then
        echo "Added User $USERNAME"
        #print out for groups and a prompt to run addusertogroup
        echo ""
        tput setaf 1; echo "This User is currently a Member of NO Groups!"
        tput sgr0; echo "Please run addUserToGroup and specify which of the following Groups you wish $USERNAME to be added to:"
        echo ""
        echo "$allgroups"
        echo ""
        exit 0
    else
        echo $ERROR
        exit 1
    fi
fi
if [ "$OPERATION" = "addAdmin" ]; then
    echo "Enter Password for New Admin:"
    PASSWORD=$(slappasswd -h {SSHA})
    if [ -z "$PASSWORD" ]; then
      exit 1
    fi


    cat > /tmp/ldapuser.ldif << LDAPUSER
dn: cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
changetype: add
objectClass: person
objectClass: organizationalPerson
objectClass: inetOrgPerson
objectClass: top
userPassword: $PASSWORD
givenName: $USERNAME
cn: $USERNAME
sn: $USERNAME

dn: cn=FidoAdminAuthorized,did=$SAKA_DID,ou=groups,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
changetype: modify
add: uniqueMember
uniqueMember: cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
LDAPUSER
    ldapmodify -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" -f /tmp/ldapuser.ldif 2> /tmp/Error
    ERROR=$(</tmp/Error)
    rm /tmp/ldapuser.ldif /tmp/Error
    if [ -z "$ERROR" ]; then
        echo "Added Admin $USERNAME"
        exit 0
    else
        echo $ERROR
        exit 1
    fi
fi

if [ "$OPERATION" = "addUserToGroup" ]; then
    USER_EXIST=$(ldapsearch -Y external -H ldapi:/// -b dc=strongauth,dc=com cn=$USERNAME -LLL 2> /dev/null)
    if [ -z "$USER_EXIST" ]; then
        echo "$USERNAME does not exist. Please run addUser."
        exit 1
    fi
    groups="$4"
    if [ -z $groups ];then
            usage
            exit 1
    fi
    IFS=','
    read -a groupsarr <<< "$groups"
    for group in "${groupsarr[@]}";
    do
        if [[ "$allgroups" == *"$group"* ]]; then
            cat > /tmp/ldapgroup.ldif << LDAPUSER
dn: cn=$group,did=$SAKA_DID,ou=groups,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
changetype: modify
add: uniqueMember
uniqueMember: cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
LDAPUSER
            ldapmodify -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" -f /tmp/ldapgroup.ldif 2> /tmp/Error
            ERROR=$(</tmp/Error)
            rm /tmp/ldapgroup.ldif /tmp/Error
            if [ -z "$ERROR" ]; then
                echo "Added User $USERNAME to Group $group"
            else
                echo $ERROR
                exit 1
            fi
        else
            echo "$group is not a valid Group for Users."
            exit 1
        fi
    done
    echo "Done!"
    exit 0

fi
if [ "$OPERATION" = "deleteUser" ]; then
    USER_EXIST=$(ldapsearch -Y external -H ldapi:/// -b dc=strongauth,dc=com cn=$USERNAME -LLL 2> /dev/null)
    if [ -z "$USER_EXIST" ]; then
        echo "$USERNAME does not exist"
        exit 1
    fi
    IFS=','
    read -a groupsarr <<< "$allgroups"
    for group in "${groupsarr[@]}";
    do
        cat > /tmp/ldapgroup.ldif << LDAPUSER
dn: cn=$group,did=$SAKA_DID,ou=groups,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
changetype: modify
delete: uniqueMember
uniqueMember: cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com
LDAPUSER
        ldapmodify -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" -f /tmp/ldapgroup.ldif 2> /tmp/Error 1> /dev/null
        ERROR=$(</tmp/Error)
        rm /tmp/ldapgroup.ldif /tmp/Error
        if [ -z "$ERROR" ]; then
            echo "Removed User $USERNAME from Group $group"
        fi
    done
    ldapdelete -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" "cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com"
    echo "Deleted User $USERNAME"
    exit 0
fi

if [ "$OPERATION" = "changeUserPassword" ] ; then

    USER_EXIST=$(ldapsearch -Y external -H ldapi:/// -b dc=strongauth,dc=com cn=$USERNAME -LLL 2> /dev/null)
    if [ -z "$USER_EXIST" ]; then
        echo "$USERNAME does not exist."
        exit 1
    fi

    PASSWORD_CHANGE_RESULT=$(ldappasswd -v -x -w  "$SERVICE_LDAP_BIND_PASS" -D "cn=Manager,dc=strongauth,dc=com" -S "cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com")
    if [ -z "$PASSWORD_CHANGE_RESULT" ]; then
      exit 1
    fi
    echo "Changed Password for $USERNAME"
    exit 0
fi
if [ "$OPERATION" = "getUserGroups" ]; then

    USER_EXIST=$(ldapsearch -Y external -H ldapi:/// -b dc=strongauth,dc=com cn=$USERNAME -LLL 2> /dev/null)
    if [ -z "$USER_EXIST" ]; then
        echo "$USERNAME does not exist."
        exit 1
    fi
    ldapsearch -LLL -Y external -H ldapi:/// -b "dc=strongauth,dc=com" "uniqueMember=cn=$USERNAME,did=$SAKA_DID,ou=users,ou=v2,ou=SKCE,ou=StrongAuth,ou=Applications,dc=strongauth,dc=com" 2> /dev/null | grep dn -A 1 --color=never
    exit 0
fi




usage
exit 1
