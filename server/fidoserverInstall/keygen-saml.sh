#!/bin/bash
SCRIPT_HOME=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
DN=$1 #example: /C=BR/ST=State/L=City/O=Company Inc./OU=IT
CLUSTER_SIZE=$2
NUMBER_OF_CERTS=$3
DID=$4
KEYSTORE_PASS=$5
CERT_VALIDITY=$6
KEY_PASS=$KEYSTORE_PASS
BCFIPS_PATH=/usr/local/strongkey/keymanager/lib/bc-fips-1.0.2.1.jar
KEYSTORE_PATH=/usr/local/strongkey/skfs/keystores/samlsigningkeystore.bcfks 
TRUSTSTORE_PATH=/usr/local/strongkey/skfs/keystores/samlsigningtruststore.bcfks
OPENSSL_CNF_PATH=/etc/pki/tls/openssl.cnf

mkdir saml-keys

#creating the SAML Root certificate authority

#CA private key and cert
openssl genrsa -out saml-keys/CA-key.$DID.pem 2048
openssl req -new -key saml-keys/CA-key.$DID.pem -x509 -days 1825 -out saml-keys/CA-cert.$DID.pem -subj "$DN/CN=SAML CA" 

#packing private CA key and CA cert in p12
openssl pkcs12 -export -in saml-keys/CA-cert.$DID.pem -inkey saml-keys/CA-key.$DID.pem -passin pass:$KEY_PASS -name samlCA.$DID -out saml-keys/samlCA.$DID.p12 -passout pass:$KEY_PASS

#import CA p12 into keystore
keytool -importkeystore -deststorepass $KEYSTORE_PASS -destkeystore $KEYSTORE_PATH -srckeystore saml-keys/samlCA.$DID.p12 -srcstorepass $KEY_PASS -srcstoretype PKCS12	-noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH
#import CA cert into truststore
keytool -import -alias samlCA-$DID -file saml-keys/CA-cert.$DID.pem -keystore $TRUSTSTORE_PATH -storepass $KEYSTORE_PASS -noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH -trustcacerts 

echo
echo "SAML CA generated"

echo "
subjectKeyIdentifier=hash
authorityKeyIdentifier=keyid,issuer
keyUsage=digitalSignature" > saml-keys/saml_ext.cnf

#creating the SAML signing certificates
for (( SID = 1; SID <= CLUSTER_SIZE ; SID++ ))
do
    for (( COUNT = 1 ; COUNT <= NUMBER_OF_CERTS; COUNT++ ))
    do
	#creating cert private key and csr
	openssl req -new -newkey rsa:2048 -nodes -keyout saml-keys/saml.key-$SID-$DID-$COUNT.pem -out saml-keys/samlsigningkey-$SID-$DID-$COUNT.csr -subj "$DN/CN=SAML Signing Certificate $SID-$DID-$COUNT" -passout pass:$KEY_PASS -passin pass:$KEY_PASS
	#creating certificate signed by CA
	openssl x509 -req -days $CERT_VALIDITY -in saml-keys/samlsigningkey-$SID-$DID-$COUNT.csr -CA saml-keys/CA-cert.$DID.pem -CAkey saml-keys/CA-key.$DID.pem -CAcreateserial -out saml-keys/samlsigning-$SID-$DID-$COUNT.pem -extfile saml-keys/saml_ext.cnf
	#packing private key and cert in p12
	openssl pkcs12 -export -in saml-keys/samlsigning-$SID-$DID-$COUNT.pem -inkey saml-keys/saml.key-$SID-$DID-$COUNT.pem -passin pass:$KEY_PASS -name samlsigning-$SID-$DID-$COUNT -out saml-keys/samlsigning-$SID-$DID-$COUNT.p12 -passout pass:$KEY_PASS
	#importing p12 into keystore
	keytool -importkeystore -deststorepass $KEYSTORE_PASS -destkeystore $KEYSTORE_PATH -srckeystore saml-keys/samlsigning-$SID-$DID-$COUNT.p12 -srcstorepass $KEY_PASS -srcstoretype PKCS12	-noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH	
	#importing cert into truststore
	keytool -import -alias samlsigning-$SID-$DID-$COUNT -file saml-keys/samlsigning-$SID-$DID-$COUNT.pem -keystore $TRUSTSTORE_PATH -storepass $KEYSTORE_PASS -noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH
    done
done

echo
echo "SAML signing keys generation finished!"

exit 0

