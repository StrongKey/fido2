#!/bin/bash
###################################################################################
# /**
# * Copyright StrongAuth, Inc. All Rights Reserved.
# *
# * Use of this source code is governed by the GNU Lesser General Public License v2.1
# * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
# */
###################################################################################

PORT=8181
HOSTNAME=$1
GLASSFISH_KEYPATH="/domains/domain1/config/cacerts.jks"
JAVA_KEYPATH="/certs/cacerts"
PASSWORD="changeit"
USAGE="Usage: certimport.sh hostname -p<port> -k<JAVA | GLASSFISH | keystore path> -w<keystore password>
----------------------------------------------------------------------
       certimport.sh saka201 (defaults to JAVA_HOME keystore)
       certimport.sh expo1.strongauth.com -p8282 -kGLASSFISH (uses GLASSFISH_HOME to find keystore)
       certimport.sh deicda02 -k../config/cacerts.jks -wAbcd1234!"
KEY_PATH="$STRONGAUTH_HOME$JAVA_KEYPATH"

if [ -z $HOSTNAME ]; then
	echo "$USAGE"
	exit 1
fi

if [ $# -gt 4 ]; then
	echo "$USAGE"
	exit 1
fi

INPUT='^..'
for i in $2 $3 $4; do
	case `echo $i | cut -c1-2` in
	-p)
		PORT=`echo $i | cut -c3-`
		;;
	-k)
		case `echo $i | cut -c3-` in
			JAVA)
				KEY_PATH=$STRONGAUTH_HOME$JAVA_KEYPATH
			;;
			java)
				KEY_PATH=$STRONGAUTH_HOME$JAVA_KEYPATH
			;;
			GLASSFISH)
				KEY_PATH=$GLASSFISH_HOME$GLASSFISH_KEYPATH
			;;
			glassfish)
				KEY_PATH=$GLASSFISH_HOME$GLASSFISH_KEYPATH
			;;
			*)
				KEY_PATH=`echo $i | cut -c3-`
			;;
		esac
		;;
	-w)
		PASSWORD=`echo $i | cut -c3-`
		;;
	*)
		echo "$USAGE"
		exit 1
	esac
done

if [ ! -e $KEY_PATH ]; then
	echo -e "\E[31mKeystore could not be located at: \E[0m$KEY_PATH"
	exit 1
fi

if [ ! -w $KEY_PATH ]; then
	echo -e "\E[31mKeystore could not be written to at: \E[0m$KEY_PATH"
	exit 1
fi

openssl s_client -connect $HOSTNAME:$PORT </dev/null 2> /dev/null | sed -ne '/-BEGIN CERTIFICATE-/,/-END CERTIFICATE-/p' >$HOSTNAME.pem
grep "BEGIN CERTIFICATE" $HOSTNAME.pem >/dev/null
if [ ! $? -eq 0 ]; then
	echo -e "\E[31mUnable to connect to server at: \E[0m$HOSTNAME:$PORT"
	rm $HOSTNAME.pem
	exit 1
fi

keytool -list -keystore $KEY_PATH -storepass $PASSWORD -alias $HOSTNAME >/dev/null
if [ $? -eq 0 ]; then
	keytool -delete -noprompt -keystore $KEY_PATH -storepass $PASSWORD -alias $HOSTNAME
fi

RESULT=`keytool -importcert -noprompt -keystore $KEY_PATH -storepass $PASSWORD -alias $HOSTNAME -file $HOSTNAME.pem 2>&1`
if [ ! $? -eq 0 ]; then
	echo -e "\E[31m$RESULT at: \E[0m$KEY_PATH"
else
	echo "$RESULT at: $KEY_PATH"
fi

rm $HOSTNAME.pem
exit 0
