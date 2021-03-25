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
import java.security.interfaces.ECPublicKey;
import java.security.interfaces.RSAPublicKey;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1OctetString;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSet;
import org.bouncycastle.asn1.DERTaggedObject;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;

public class AndroidKeyAttestationStatement implements FIDO2AttestationStatement {
    private int alg;
    private byte[] signature;
    private ArrayList x5c = null;
    private final String attestationType = "basic";

    static {
        Security.addProvider(new BouncyCastleFipsProvider());
    }

    @Override
    public void decodeAttestationStatement(Object attStmt) {
        Map<String, Object> attStmtObjectMap = (Map<String, Object>) attStmt;
//        for (String key : attStmtObjectMap.keySet()) {
        for (Map.Entry<String,Object> entry : attStmtObjectMap.entrySet()) {
            String key = entry.getKey();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Key attstmt Packed: " + key);
            switch (key) {
                case "sig":
                    signature = (byte[]) entry.getValue();
                    break;
                case "x5c":
                    x5c = (ArrayList) entry.getValue();
                    break;
                case "alg":
                    alg = (int) (long) entry.getValue();
                    break;
                default :
                    break;
            }
        }
    }

    @Override
    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData) {
        try {
            //TODO PKIX validation
            Iterator x5cItr = x5c.iterator();
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");       //BCFIPS BER Parser is failing here, defaulting to base JAVA
            byte[] certByte = (byte[]) x5cItr.next();
            InputStream instr = new ByteArrayInputStream(certByte);
            X509Certificate attCert = (X509Certificate) certFactory.generateCertificate(instr);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Android-Key certificate: " + attCert);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Base64: " + Base64.getEncoder().encodeToString(attCert.getEncoded()));

            //Verify that sig is a valid signature over the concatenation of authenticatorData
            //and clientDataHash using the public key in the first certificate in x5c with the
            //algorithm specified in alg.
            PublicKey certPublicKey = attCert.getPublicKey();
            byte[] clientDataHash = SKFSCommon.getDigestBytes(Base64.getDecoder().decode(browserDataBase64), "SHA256");
            byte[] signedBytes = concatenateArrays(authData.getAuthDataDecoded(), clientDataHash);
            Signature verifySignature = Signature.getInstance(SKFSCommon.getAlgFromIANACOSEAlg(alg), "BCFIPS");
            verifySignature.initVerify(certPublicKey);
            verifySignature.update(signedBytes);
            if(!verifySignature.verify(signature)){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "Android-key signature failed to validate");
                return false;
            }

            //Verify that the public key in the first certificate in in x5c matches the
            //credentialPublicKey in the attestedCredentialData in authenticatorData.
            //(Implementation) The tests imply that these keys might not be formatted the same.
            //The keys should just be functionally the same. Conditionals check for functional
            //equivalence.
            PublicKey credentialPublicKey = authData.getAttCredData().getPublicKey();
            if(!certPublicKey.getAlgorithm().equals(credentialPublicKey.getAlgorithm())){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "Android-key provided public keys' algorithms do not match");
                return false;
            }

            if(certPublicKey.getAlgorithm().equals("RSA")){
                RSAPublicKey rsaCertPublicKey = (RSAPublicKey) certPublicKey;
                RSAPublicKey rsacredentialPublicKey = (RSAPublicKey) credentialPublicKey;
                if(!rsaCertPublicKey.getModulus().equals(rsacredentialPublicKey.getModulus())){
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key provided public keys' moduli do not match");
                    return false;
                }
                if(!rsaCertPublicKey.getPublicExponent().equals(rsacredentialPublicKey.getPublicExponent())){
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key provided public keys' exponents do not match");
                    return false;
                }
            }
            else if(certPublicKey.getAlgorithm().equals("EC")){
                ECPublicKey ecCertPublicKey = (ECPublicKey) certPublicKey;
                ECPublicKey eccredentialPublicKey = (ECPublicKey) credentialPublicKey;
                if (ecCertPublicKey.getParams().getCofactor() != eccredentialPublicKey.getParams().getCofactor()) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key provided public keys' cofactors do not match");
                    return false;
                }
                if (!ecCertPublicKey.getParams().getCurve().equals(eccredentialPublicKey.getParams().getCurve()) ) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key provided public keys' curves do not match");
                    return false;
                }
                if (!ecCertPublicKey.getParams().getGenerator().equals(eccredentialPublicKey.getParams().getGenerator())) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key provided public keys' generators do not match");
                    return false;
                }
                if (!ecCertPublicKey.getParams().getOrder().equals(eccredentialPublicKey.getParams().getOrder())) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key provided public keys' orders do not match");
                    return false;
                }
                if (!ecCertPublicKey.getW().equals(eccredentialPublicKey.getW())) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key provided public keys' points do not match");
                    return false;
                }
            }
            else{
                throw new UnsupportedOperationException("Android-key attestation contains unknown key type");
            }

            //Verify that in the attestation certificate extension data:
            String attestationOID = "1.3.6.1.4.1.11129.2.1.17";
            byte[] attestationBytes = attCert.getExtensionValue(attestationOID);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Android-key attestation bytes: " + Hex.toHexString(attestationBytes));

            //Format found here: https://source.android.com/security/keystore/attestation
            //  The value of the attestationChallenge field is identical to clientDataHash.
            //
            //  The AuthorizationList.allApplications field is not present, since
            //  PublicKeyCredential must be bound to the RP ID.
            //
            //  The AuthorizationList.allApplications field is not present, since
            //  PublicKeyCredential must be bound to the RP ID.
            //
            //  The value in the AuthorizationList.origin field is equal to KM_TAG_GENERATED.
            //
            //  The value in the AuthorizationList.purpose field is equal to KM_PURPOSE_SIGN.
            //
            //Constants found here: https://source.android.com/security/keystore/tags
            ASN1InputStream attestationOctetStream = null;
            try{
                ASN1OctetString attestationDERString = (ASN1OctetString) ASN1OctetString.fromByteArray(attestationBytes);
                attestationOctetStream = new ASN1InputStream(attestationDERString.getOctets());
                ASN1Sequence attestation = ASN1Sequence.getInstance(attestationOctetStream.readObject());
                byte[] attestationChallenge = ((ASN1OctetString) ASN1OctetString.fromByteArray(
                        attestation.getObjectAt(4).toASN1Primitive().getEncoded())).getOctets();
                ASN1Sequence softwareEnforced = (ASN1Sequence) attestation.getObjectAt(6).toASN1Primitive();
                ASN1Sequence teeEnforced = (ASN1Sequence) attestation.getObjectAt(7).toASN1Primitive();

                //Check both AuthorizationLists
                int ALLAPPLICATIONS = 600;
                int ORIGIN = 702;
                int PURPOSE = 1;
                int KM_TAG_GENERATED = 0;
                int KM_PURPOSE_SIGN = 2;
                boolean isAllApplicationsNotPresent = true;
                boolean isOriginEqualToKM_TAG_GENERATED = false;
                boolean isPurposeEqualToKM_PURPOSE_SIGN = false;
                for (Iterator<ASN1Encodable> iter = softwareEnforced.iterator(); iter.hasNext();) {
                    DERTaggedObject obj = (DERTaggedObject) iter.next();
                    if (obj.getTagNo() == ALLAPPLICATIONS) {
                        isAllApplicationsNotPresent = false;
                    } else if (obj.getTagNo() == ORIGIN) {
                        if (((ASN1Integer) obj.getObject()).getValue().intValue() == KM_TAG_GENERATED) {
                            isOriginEqualToKM_TAG_GENERATED = true;
                        }
                    } else if (obj.getTagNo() == PURPOSE) {
                        DERSet purposes = (DERSet) obj.getObject();
                        for (int i = 0; i < purposes.size(); i++) {
                            if (((ASN1Integer) purposes.getObjectAt(i)).getValue().intValue() == KM_PURPOSE_SIGN) {
                                isPurposeEqualToKM_PURPOSE_SIGN = true;
                            }

                        }
                    }
                }
                for (Iterator<ASN1Encodable> iter = teeEnforced.iterator(); iter.hasNext();) {
                    DERTaggedObject obj = (DERTaggedObject) iter.next();
                    if (obj.getTagNo() == ALLAPPLICATIONS) {
                        isAllApplicationsNotPresent = false;
                    } else if (obj.getTagNo() == ORIGIN) {
                        if (isOriginEqualToKM_TAG_GENERATED) {
                            throw new IllegalArgumentException("Android-key attestation has a conflicting origin");
                        }
                        if (((ASN1Integer) obj.getObject()).getValue().intValue() == KM_TAG_GENERATED) {
                            isOriginEqualToKM_TAG_GENERATED = true;
                        }
                    } else if (obj.getTagNo() == PURPOSE) {
                        if (isPurposeEqualToKM_PURPOSE_SIGN) {
                            throw new IllegalArgumentException("Android-key attestation has a conflicting purpose");
                        }
                        DERSet purposes = (DERSet) obj.getObject();
                        for (int i = 0; i < purposes.size(); i++) {
                            if (((ASN1Integer) purposes.getObjectAt(i)).getValue().intValue() == KM_PURPOSE_SIGN) {
                                isPurposeEqualToKM_PURPOSE_SIGN = true;
                            }

                        }
                    }
                }
                if(!Arrays.equals(attestationChallenge, clientDataHash)){
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key attestationChallenge does not match clientDataHash");
                    return false;
                }

                if (!isAllApplicationsNotPresent) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key attestation contains allApplications");
                    return false;
                }

                if (!isOriginEqualToKM_TAG_GENERATED) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key attestation origin is not GENERATED");
                    return false;
                }

                if (!isPurposeEqualToKM_PURPOSE_SIGN) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "Android-key attestation purpose does not include SIGNING");
                    return false;
                }
            }
            catch(NullPointerException ex){
                throw new IllegalArgumentException("Unexpected end of android-key attestation");
            } catch (IOException ex) {
                throw new IllegalArgumentException("Invalid android-key attestation");
            }
            finally {
                try {
                    attestationOctetStream.close();
                } catch (IOException ex) {
                    throw new IllegalArgumentException("Invalid android-key attestation");
                }
            }


        } catch (CertificateException | NoSuchProviderException | NoSuchAlgorithmException | UnsupportedEncodingException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(AndroidKeyAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return true;
    }

    @Override
    public ArrayList getX5c(){
        return x5c;
    }

    private byte[] concatenateArrays(byte[] array1, byte[] array2) {
        byte[] result = new byte[array1.length + array2.length];
        System.arraycopy(array1, 0, result, 0, array1.length);
        System.arraycopy(array2, 0, result, array1.length, array2.length);
        return result;
    }

    @Override
    public String getAttestationType() {
        return attestationType;
    }
}
