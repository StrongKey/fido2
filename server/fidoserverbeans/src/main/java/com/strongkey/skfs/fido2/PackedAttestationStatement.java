/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InvalidNameException;
import javax.naming.ldap.LdapName;
import javax.naming.ldap.Rdn;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;

public class PackedAttestationStatement implements FIDO2AttestationStatement {
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
                case "sig":
                    signature = (byte[]) attStmtObjectMap.get(key);
                    break;
                case "x5c":
                    x5c = (ArrayList) attStmtObjectMap.get(key);
                    attestationType = "basic";
                    break;
                case "ecdaaKeyId":
                    ecdaaKeyId = (byte[]) attStmtObjectMap.get(key);
                    attestationType = "ecdaa";
                    break;
                case "alg":
                    alg = (long) attStmtObjectMap.get(key);
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

                PublicKey certPublicKey = attCert.getPublicKey();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "CERT ALGO = " + certPublicKey.getAlgorithm());

                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "Signed Bytes Input: " + browserDataBase64);

                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "authData.getAuthDataDecoded(): " + java.util.Base64.getEncoder().encodeToString(authData.getAuthDataDecoded()));
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "signature: " + java.util.Base64.getEncoder().encodeToString(signature));

                //Verify that sig is a valid signature over the concatenation of authenticatorData and clientDataHash using the attestation public key in attestnCert with the algorithm specified in alg.
                byte[] encodedauthdata = authData.getAuthDataDecoded();
                byte[] browserdatabytes = SKFSCommon.getDigestBytes(java.util.Base64.getDecoder().decode(browserDataBase64), "SHA256");
                byte[] signedBytes = new byte[encodedauthdata.length + browserdatabytes.length];
//                byte[] signedBytes = Bytes.concat(authData.getAuthDataDecoded(), SKFSCommon.getDigestBytes(java.util.Base64.getDecoder().decode(browserDataBase64), "SHA256"));
                System.arraycopy(encodedauthdata,0,signedBytes,0         ,encodedauthdata.length);
                System.arraycopy(browserdatabytes,0,signedBytes,encodedauthdata.length,browserdatabytes.length);
                
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                    "signedBytes: " + java.util.Base64.getEncoder().encodeToString(signedBytes));
                boolean isValidSignature = cryptoCommon.verifySignature(signature, certPublicKey, signedBytes, SKFSCommon.getAlgFromIANACOSEAlg(alg));
                if(!isValidSignature){
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "browserDataBase64 = " + browserDataBase64);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "authData = " + Hex.toHexString(authData.getAuthDataDecoded()));
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "public key = " + certPublicKey);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "Signature = " + Hex.toHexString(signature));
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "Failed to verify Packed signature");
                    return false;
                }

                //Verify that attestnCert meets the requirements in §8.2.1 Packed Attestation Statement Certificate Requirements.
                //  Version MUST be set to 3 (which is indicated by an ASN.1 INTEGER with value 2).
                if(attCert.getVersion() != 3){
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "Attestation Certificate (Packed) Failure: Version");
                    return false;
                }

                //  Subject field MUST be set to:
                String subjectDN = attCert.getSubjectDN().getName();
                Map<String, String> subjectFieldMap = new HashMap<>();
                try{
                    LdapName ldapDN = new LdapName(subjectDN);
                    for (Rdn rdn : ldapDN.getRdns()) {
                        subjectFieldMap.put(rdn.getType(), rdn.getValue().toString());
                    }
                }
                catch (InvalidNameException ex) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Unable to parse subjectDN");
                    return false;
                }


                //      Subject-C ISO 3166 code specifying the country where the Authenticator vendor is incorporated (PrintableString)
                //TODO ensure string is an ISO 3166 country code
                if (!subjectFieldMap.containsKey("C")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Attestation Certificate (Packed) Failure: Missing Country ");
                    return false;
                }

                //      Subject-O Legal name of the Authenticator vendor (UTF8String)
                if (!subjectFieldMap.containsKey("O")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Attestation Certificate (Packed) Failure: Missing Organization");
                    return false;
                }

                //      Subject-OU Literal string “Authenticator Attestation” (UTF8String)
                if (!subjectFieldMap.containsKey("OU") || !subjectFieldMap.get("OU").equals("Authenticator Attestation")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Attestation Certificate (Packed) Failure: Missing OU");
                    return false;
                }

                //      Subject-CN A UTF8String of the vendor’s choosing
                if (!subjectFieldMap.containsKey("CN")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Attestation Certificate (Packed) Failure: Invalid CN");
                    return false;
                }

                //The Basic Constraints extension MUST have the CA component set to false.
                if (attCert.getBasicConstraints() != -1) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Packed attestation statement cetificate: Invalid Basic Constraints");
                    return false;
                }

                //If attestnCert contains an extension with OID 1.3.6.1.4.1.45724.1.1.4 (id-fido-gen-ce-aaguid) verify that the value of this extension matches the aaguid in authenticatorData.
                byte[] certAaguidExtension = attCert.getExtensionValue("1.3.6.1.4.1.45724.1.1.4");
                if (certAaguidExtension != null) {
                    //Note that an X.509 Extension encodes the DER-encoding of the value in an OCTET STRING. Thus, the AAGUID MUST be wrapped in two OCTET STRINGS to be valid.
                    //Remove 2 OCTET String wrappers
                    byte[] certAaguid = Arrays.copyOfRange(certAaguidExtension, 4, certAaguidExtension.length);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "Certificate contains aaguid = " + Hex.toHexString(certAaguid));
                    if (!Arrays.equals(certAaguid, authData.getAttCredData().getAaguid())) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Packed x5c's aaguid does not match");
                        return false;
                    }
                }

                return true;
            } catch (CertificateException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
                Logger.getLogger(U2FAttestationStatment.class.getName()).log(Level.SEVERE, null, ex);
            }
            return Boolean.FALSE;
        } else if (ecdaaKeyId != null) {
            //not supported yet
            return false;
        } else {
            try {
                //Self attestation

                //Validate that alg matches the algorithm of the credentialPublicKey in authenticatorData.
                if (alg != authData.getAttCredData().getFko().getAlg()) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Attestation Statement algorithm does not match Authenticator Data algorithm");
                    return false;
                }
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-2001"),
                        "CERT ALGO = " + authData.getAttCredData().getPublicKey().getAlgorithm());
                //Verify that sig is a valid signature over the concatenation of authenticatorData and clientDataHash using the credential public key with alg.
                
                byte[] encodedauthdata = authData.getAuthDataDecoded();
                byte[] browserdatabytes = SKFSCommon.getDigestBytes(java.util.Base64.getDecoder().decode(browserDataBase64), "SHA256");
                byte[] signedBytes = new byte[encodedauthdata.length + browserdatabytes.length];
                
                System.arraycopy(encodedauthdata,0,signedBytes,0         ,encodedauthdata.length);
                System.arraycopy(browserdatabytes,0,signedBytes,encodedauthdata.length,browserdatabytes.length);
                
//                byte[] signedBytes = Bytes.concat(authData.getAuthDataDecoded(), SKFSCommon.getDigestBytes(java.util.Base64.getDecoder().decode(browserDataBase64), "SHA256"));
                Signature verifySignature = Signature.getInstance(SKFSCommon.getAlgFromIANACOSEAlg(alg), "BCFIPS");
                verifySignature.initVerify(authData.getAttCredData().getPublicKey());
                verifySignature.update(signedBytes);

                //If successful, return attestation type Self and empty attestation trust path.
                return verifySignature.verify(signature);
            } catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException | InvalidKeyException | SignatureException ex) {
                Logger.getLogger(PackedAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
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
