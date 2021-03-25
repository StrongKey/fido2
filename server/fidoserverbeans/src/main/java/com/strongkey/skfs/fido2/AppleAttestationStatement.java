/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;

public class AppleAttestationStatement implements FIDO2AttestationStatement {
    private long alg;
    private byte[] signature;
    private byte[] ecdaaKeyId;
    private ArrayList x5c = null;
    private String attestationType = "self";
    String validataPkix = SKFSCommon.getConfigurationProperty("skfs.cfg.property.pkix.validate");
    String validataPkixMethod = SKFSCommon.getConfigurationProperty("skfs.cfg.property.pkix.validate.method");

    static {
        Security.addProvider(new BouncyCastleFipsProvider());
    }

    @Override
    public void decodeAttestationStatement(Object attestationStmt) {
        Map<String, Object> attStmtObjectMap = (Map<String, Object>) attestationStmt;
//        for (String key : attStmtObjectMap.keySet()) {
        for (Map.Entry<String,Object> entry : attStmtObjectMap.entrySet()) {
            String key = entry.getKey();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "Key attstmt Packed: " + key);
            switch (key) {
                case "x5c":
                    x5c = (ArrayList) attStmtObjectMap.get(key);
                    attestationType = "basic";
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData) {
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "ALG = " + alg);
        if (x5c != null) {
            try {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    x5c.size());
                Iterator x5cItr = x5c.iterator();
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BCFIPS");
                byte[] certByte = (byte[]) x5cItr.next();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "x5c base64 java: " + java.util.Base64.getEncoder().encodeToString(certByte));
                InputStream instr = new ByteArrayInputStream(certByte);
                X509Certificate attCert = (X509Certificate) certFactory.generateCertificate(instr);

                // Extracting value of the extension with OID 1.2.840.113635.100.8.2 in credCert
                
                ByteArrayInputStream inStream = new ByteArrayInputStream(attCert.getExtensionValue(SKFSConstants.APPLE_CREDENTIAL_CERT_EXTENSION_OID));
                ASN1InputStream asnInputStream = new ASN1InputStream(inStream);
                byte[] extensionOIDbytes = null;
                try {
                    DEROctetString derOctetString = (DEROctetString) asnInputStream.readObject();
                    inStream = new ByteArrayInputStream(derOctetString.getOctets());
                    asnInputStream = new ASN1InputStream(inStream);
                    ASN1Primitive derObject = asnInputStream.readObject();
                    if (derObject instanceof DLSequence) {
                        DLSequence s = (DLSequence) derObject;
                        DERTaggedObject taggedObject = (DERTaggedObject) s.getObjectAt(0);
                        DEROctetString oct = (DEROctetString) taggedObject.getObject();
                        extensionOIDbytes = oct.getOctets();
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AppleAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
                }
        
                PublicKey certPublicKey = attCert.getPublicKey();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "CERT ALGO = " + certPublicKey.getAlgorithm());

                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "Signed Bytes Input: " + browserDataBase64);

                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "authData.getAuthDataDecoded(): " + java.util.Base64.getEncoder().encodeToString(authData.getAuthDataDecoded()));

                //Verify that sig is a valid signature over the concatenation of authenticatorData and clientDataHash using the attestation public key in attestnCert with the algorithm specified in alg.
                byte[] encodedauthdata = authData.getAuthDataDecoded();
                byte[] browserdatabytes = SKFSCommon.getDigestBytes(java.util.Base64.getDecoder().decode(browserDataBase64), "SHA256");
                byte[] signedBytes = new byte[encodedauthdata.length + browserdatabytes.length];
                System.arraycopy(encodedauthdata,0,signedBytes,0         ,encodedauthdata.length);
                System.arraycopy(browserdatabytes,0,signedBytes,encodedauthdata.length,browserdatabytes.length);
                
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "signedBytes: " + java.util.Base64.getEncoder().encodeToString(signedBytes));

//Verify that nonce equals the value of the extension with OID 1.2.840.113635.100.8.2 in credCert.                
                boolean isnonceequal = Arrays.equals(SKFSCommon.getDigestBytes(signedBytes, "SHA256"), extensionOIDbytes);
      
                if(!isnonceequal){
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "browserDataBase64 = " + browserDataBase64);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "authData = " + Hex.toHexString(authData.getAuthDataDecoded()));
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "public key = " + certPublicKey);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "Failed to verify nonce");
                    return false;
                }

                
                //Verify that the credential public key equals the Subject Public Key of credCert.
                //cert public key certPublicKey
                PublicKey credpk = authData.getAttCredData().getPublicKey();
                if (!certPublicKey.equals(credpk)) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Failed to verify public key");
                    return Boolean.FALSE;
                }
                
                return true;
            } catch (CertificateException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
                Logger.getLogger(U2FAttestationStatment.class.getName()).log(Level.SEVERE, null, ex);
            }
            return Boolean.FALSE;
        } else {
            //not supported yet
            return Boolean.FALSE;
        }
    }

    @Override
    public ArrayList getX5c() {
        return x5c;
    }

    @Override
    public String getAttestationType() {
        return attestationType;
    }
}
