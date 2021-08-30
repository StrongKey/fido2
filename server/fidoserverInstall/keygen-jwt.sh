#!/bin/bash
SCRIPT_HOME=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
DN=$1 #example: /C=BR/ST=State/L=City/O=Company Inc./OU=IT
CLUSTER_SIZE=$2
NUMBER_OF_CERTS=$3
DID=$4
KEYSTORE_PASS=$5
CERT_VALIDITY=$6
KEY_PASS=$KEYSTORE_PASS
BCFIPS_PATH=$SCRIPT_HOME/keymanager/lib/bc-fips-1.0.1.jar
KEYSTORE_PATH=/usr/local/strongkey/skfs/keystores/jwtsigningkeystore.bcfks 
TRUSTSTORE_PATH=/usr/local/strongkey/skfs/keystores/jwtsigningtruststore.bcfks
OPENSSL_CNF_PATH=/etc/pki/tls/openssl.cnf



mkdir jwttmp

#creating the jwt Root certificate authority

#CA private key and cert
openssl ecparam -name secp521r1 -genkey -noout -out jwttmp/CA-key.$DID.pem 
openssl req -new -key jwttmp/CA-key.$DID.pem -x509 -days 1825 -out jwttmp/CA-cert.$DID.pem -subj "$DN/CN=JWT CA $DID" 

#packing private CA key and CA cert in p12
openssl pkcs12 -export -in jwttmp/CA-cert.$DID.pem -inkey jwttmp/CA-key.$DID.pem -passin pass:$KEY_PASS -name jwtCA.$DID -out jwttmp/jwtCA.$DID.p12 -passout pass:$KEY_PASS

#import CA p12 into keystore
keytool -importkeystore -deststorepass $KEYSTORE_PASS -destkeystore $KEYSTORE_PATH -srckeystore jwttmp/jwtCA.$DID.p12 -srcstorepass $KEY_PASS -srcstoretype PKCS12	-noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH
#import CA cert into truststore
keytool -import -alias jwtCA-$DID -file jwttmp/CA-cert.$DID.pem -keystore $TRUSTSTORE_PATH -storepass $KEYSTORE_PASS -noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH -trustcacerts 


echo
echo "JWT CA generated"

#creating the jwt signing certificates
for (( SID = 1; SID <= CLUSTER_SIZE ; SID++ ))
do

    for (( COUNT = 1 ; COUNT <= NUMBER_OF_CERTS; COUNT++ ))
    do
		#creating cert private key
        openssl ecparam -name secp521r1 -genkey | openssl ec -aes256 -out jwttmp/jwt.key-$SID.$DID.$COUNT.pem -passout pass:$KEY_PASS
		#creating csr 
		openssl req -new -config $OPENSSL_CNF_PATH -key jwttmp/jwt.key-$SID.$DID.$COUNT.pem -passin pass:$KEY_PASS -out jwttmp/jwtsigningkey-$SID.$DID.$COUNT.csr -subj "$DN/CN=JWT Signing Certificate $SID.$DID.$COUNT"
		#creating certificate signed by CA
		openssl x509 -req -days $CERT_VALIDITY -in jwttmp/jwtsigningkey-$SID.$DID.$COUNT.csr -CA jwttmp/CA-cert.$DID.pem -CAkey jwttmp/CA-key.$DID.pem -CAcreateserial -out jwttmp/jwtsigningcert-$SID.$DID.$COUNT.pem 
		#packing private key and cert in p12
		openssl pkcs12 -export -in jwttmp/jwtsigningcert-$SID.$DID.$COUNT.pem -inkey jwttmp/jwt.key-$SID.$DID.$COUNT.pem -passin pass:$KEY_PASS -name jwtsigningcert-$SID.$DID.$COUNT -out jwttmp/jwtsigningcert-$SID.$DID.$COUNT.p12 -passout pass:$KEY_PASS
		#importing p12 into keystore
		keytool -importkeystore -deststorepass $KEYSTORE_PASS -destkeystore $KEYSTORE_PATH -srckeystore jwttmp/jwtsigningcert-$SID.$DID.$COUNT.p12 -srcstorepass $KEY_PASS -srcstoretype PKCS12	-noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH	
		#importing cert into truststore
		keytool -import -alias jwtsigningcert-$SID.$DID.$COUNT -file jwttmp/jwtsigningcert-$SID.$DID.$COUNT.pem -keystore $TRUSTSTORE_PATH -storepass $KEYSTORE_PASS -noprompt -storetype BCFKS -providername BCFIPS -providerclass org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -provider org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider -providerpath $BCFIPS_PATH
    done

done



rm -r jwttmp

echo
echo "JWT signing keys generation finished!"

exit 0

