#!/bin/bash
###################################################################################
# /**
# * Copyright StrongAuth, Inc. All Rights Reserved.
# *
# * Use of this source code is governed by the GNU Lesser General Public License v2.1
# * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
# */
###################################################################################

LOGNAME=/root/strongkey_logs/cleanup-saka-$(date +%s)
IFS="
"

if ! [ -d /root/strongkey_logs ]; then
        mkdir /root/strongkey_logs
fi

echo "Stopping SKFS services..." | tee -a $LOGNAME
service mysqld restart
service mysqld stop
service glassfishd stop

if [ -d /etc/openldap ]; then
        echo "Uninstalling OpenLDAP..." | tee -a $LOGNAME
	systemctl stop slapd
	systemctl disable slapd
	yum -y remove openldap-servers openldap-clients >/dev/null 2>&1
	rm -rf /var/lib/ldap
	userdel ldap
	rm -rf /etc/openldap
fi

echo "Restoring original system files..." | tee -a $LOGNAME
if [ -f /etc/bashrc ]; then
        sed -i '/skfsrc/d' /etc/bashrc
        cp /etc/org/bashrc /etc
else
        sed -i '/skfsrc/d' /etc/bash.bashrc
        cp /etc/org/bash.bashrc /etc
fi
cp /etc/org/sudoers /etc

#clean up rng option
if [ -f /etc/default/rng-tools ]; then
        sed -i "/^HRNGDEVICE=\/dev\/urandom/d" /etc/default/rng-tools
fi

echo "Removing SKFS configuration files..." | tee -a $LOGNAME
/lib/systemd/systemd-sysv-install disable mysqld
/lib/systemd/systemd-sysv-install disable glassfishd

rm /etc/my.cnf
rm /etc/skfsrc
rm /etc/init.d/mysqld
rm /etc/init.d/glassfishd

echo "Removing User..." | tee -a $LOGNAME
userdel -r strongkey

if $(id strongkey &> /dev/null); then
	echo -e "\E[31m'strongkey' user not fully removed. Kill all processes owned by 'strongkey' and try again." | tee -a $LOGNAME
        tput sgr0
        exit 1
fi

echo "Done!" | tee -a $LOGNAME

exit 0
