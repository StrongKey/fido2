/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.utilities.TPMConstants;
import com.strongkey.skfs.fido2.tpm.Marshal;
import com.strongkey.skfs.fido2.tpm.TPMAttest;
import com.strongkey.skfs.fido2.tpm.TPMECCParameters;
import com.strongkey.skfs.fido2.tpm.TPMECCUnique;
import com.strongkey.skfs.fido2.tpm.TPMPublicData;
import com.strongkey.skfs.fido2.tpm.TPMRSAParameters;
import com.strongkey.skfs.fido2.tpm.TPMRSAUnique;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
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
import java.util.Base64;
import java.util.Collection;
import java.util.HashMap;
import java.util.InputMismatchException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;

public class TPMAttestationStatement implements FIDO2AttestationStatement {

    private String version;
    private byte[] signature;
    private byte[] ecdaaKeyId;
    private ArrayList x5c = null;
    private TPMAttest certInfo;
    private TPMPublicData pubArea;
    private long alg;
    private final String attestationType = "attca"; //TODO implement ECDAA when the specification is finished

    static {
        Security.addProvider(new BouncyCastleFipsProvider());
    }

    @Override
    public void decodeAttestationStatement(Object attestationStmt) {
        Map<String, Object> attStmtObjectMap = (Map<String, Object>) attestationStmt;
//        for (String key : attStmtObjectMap.keySet()) {
        for (Map.Entry<String,Object> entry : attStmtObjectMap.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case "sig":
                    signature = (byte[]) attStmtObjectMap.get(key);
                    break;
                case "x5c":
                    x5c = (ArrayList) attStmtObjectMap.get(key);
                    break;
                case "ecdaaKeyId":
                    ecdaaKeyId = (byte[]) attStmtObjectMap.get(key);
                    break;
                case "certInfo":
                    certInfo = TPMAttest.unmarshal((byte[]) attStmtObjectMap.get(key));
                    break;
                case "ver":
                    version = (String) attStmtObjectMap.get(key);
                    break;
                case "pubArea":
                    pubArea = TPMPublicData.unmarshal((byte[]) attStmtObjectMap.get(key));
                    break;
                case "alg":
                    alg = (long) attStmtObjectMap.get(key);
                    break;
                default:
                    break;
            }
        }

        if (!version.equals("2.0")) {
            throw new InputMismatchException("TPM version does not match specification");
        }
    }

    @Override
    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData) {
//        List<X509Certificate> certchain = new ArrayList<>();

        try {
            //Verify that the public key specified by the parameters and unique fields of pubArea is identical
            //to the credentialPublicKey in the attestedCredentialData in authenticatorData.
            switch (pubArea.getAlgType()) {
                case TPMConstants.TPM_ALG_RSA:
                    int tpmExponent = ((TPMRSAParameters) pubArea.getParameters()).getExponent();
                    tpmExponent = (tpmExponent == 0) ? 65537 : 0; //"When zero, indicates that the exponent is the default of 2^16 + 1"
                    int authenticatorDataExponent = getIntFromByteArray(((RSAKeyObject) authData.getAttCredData().getFko()).getE());
                    if (tpmExponent != authenticatorDataExponent) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                                "TPM Attestation Exponent mismatch: " + tpmExponent + " != " + authenticatorDataExponent);
                        return false;
                    }
                    byte[] tpmMod = ((TPMRSAUnique) pubArea.getUnique()).getData();
                    byte[] authenticatorDataMod = ((RSAKeyObject) authData.getAttCredData().getFko()).getN();
                    if (!Arrays.equals(tpmMod, authenticatorDataMod)) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                                "TPM Attestation Mod mismatch");
                        return false;
                    }
                    break;
                case TPMConstants.TPM_ALG_ECC:
                    String tpmCurve = SKFSCommon.getStringFromTPMECCCurveID(((TPMECCParameters) pubArea.getParameters()).getCurveID());
                    String authenticatorDataCurve = SKFSCommon.getCurveFromFIDOECCCurveID(((ECKeyObject) authData.getAttCredData().getFko()).getCrv());
                    if (!tpmCurve.equals(authenticatorDataCurve)) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                                "TPM Attestation Elliptic Curve mismatch");
                        return false;
                    }
                    byte[] tpmX = ((TPMECCUnique) pubArea.getUnique()).getX().getData();
                    byte[] tpmY = ((TPMECCUnique) pubArea.getUnique()).getY().getData();
                    byte[] authenticatorDataX = ((ECKeyObject) authData.getAttCredData().getFko()).getX();
                    byte[] authenticatorDataY = ((ECKeyObject) authData.getAttCredData().getFko()).getY();
                    if (!Arrays.equals(tpmX, authenticatorDataX)
                            || !Arrays.equals(tpmY, authenticatorDataY)) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                                "TPM Attestation Public point mismatch");
                        return false;
                    }
                    break;
                default:
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                                "TPM Attestation Unsupported Algorithm");
                    return false;
            }

            //Concatenate authenticatorData and clientDataHash to form attToBeSigned.
            byte[] attToBeSigned = concatenateArrays(authData.getAuthDataDecoded(), SKFSCommon.getDigestBytes(Base64.getDecoder().decode(browserDataBase64), "SHA256"));

            //Validate that certInfo is valid:
            //Verify that magic is set to TPM_GENERATED_VALUE.
            if (certInfo.getMagic() != TPMConstants.TPM_GENERATED_VALUE) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                                "TPM Attestation certInfo magic is not set to the correct value");
                return false;
            }

            //Verify that type is set to TPM_ST_ATTEST_CERTIFY.
            if (certInfo.getType() != TPMConstants.TPM_ST_ATTEST_CERTIFY) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                                "TPM Attestation certInfo type is not set to the correct value");
                return false;
            }

            //Verify that extraData is set to the hash of attToBeSigned using the hash algorithm employed in "alg".
            byte[] tpmExtraData = certInfo.getExtraData().getData();
            byte[] hashAttToBeSigned = SKFSCommon.getDigestBytes(attToBeSigned, SKFSCommon.getHashAlgFromIANACOSEAlg(alg));
            if (!Arrays.equals(tpmExtraData, hashAttToBeSigned)) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "TPM Attestation certInfo extraData is not set to the correct value");
                return false;
            }

            //Verify that attested contains a TPMS_CERTIFY_INFO structure as specified in [TPMv2-Part2] section 10.12.3,
            //whose name field contains a valid Name for pubArea, as computed using the algorithm in the nameAlg field
            //of pubArea using the procedure specified in [TPMv2-Part1] section 16.
            byte[] attestedName = certInfo.getAttested().getName().getData();
            short nameAlg = Marshal.stream16ToShort(attestedName);   //https://github.com/fido-alliance/conformance-tools-issues/issues/396
            byte[] pubAreaData = pubArea.getData();
            byte[] pubAreaName = concatenateArrays(Marshal.shortToStream(nameAlg), SKFSCommon.getDigestBytes(pubAreaData, SKFSCommon.getHashAlgFromTPMAlg(nameAlg)));
            if (!Arrays.equals(attestedName, pubAreaName)) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "attestedName: " + Hex.toHexString(attestedName));
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "pubAreaName: " + Hex.toHexString(pubAreaName));
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "certInfo attested is not set to the correct value");
                return false;
            }

            //If x5c is present, this indicates that the attestation type is not ECDAA. In this case:
            if (x5c != null) {
                //Verify the sig is a valid signature over certInfo using the attestation public key in x5c with the algorithm specified in alg.

                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        x5c.size());
                Iterator x5cItr = x5c.iterator();
                CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BCFIPS");
                byte[] certByte = (byte[]) x5cItr.next();
                X509Certificate certificate = null;
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "x5c base64 java: " + java.util.Base64.getEncoder().encodeToString(certByte));
                InputStream instr = new ByteArrayInputStream(certByte);
                X509Certificate attCert = (X509Certificate) certFactory.generateCertificate(instr);

//                certchain.add(attCert);
                while (x5cItr.hasNext()) {
                    certByte = (byte[]) x5cItr.next();
                    instr = new ByteArrayInputStream(certByte);
                    certificate = (X509Certificate) certFactory.generateCertificate(instr);
//                    certchain.add(certificate);
                }

                PublicKey certPublicKey = attCert.getPublicKey();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        certPublicKey.getAlgorithm());

                byte[] signedBytes = certInfo.marshalData();

                boolean isValidSignature = cryptoCommon.verifySignature(signature, certPublicKey, signedBytes, SKFSCommon.getAlgFromIANACOSEAlg(alg));
                if (!isValidSignature) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "Failed to verify TPM signature");
                    return false;
                }

                //Verify that x5c meets the requirements in §8.3.1 TPM attestation statement certificate requirements.
                //8.3.1. TPM attestation statement certificate requirements
                //TPM attestation certificate MUST have the following fields/extensions:
                //  Version MUST be set to 3.
                if (attCert.getVersion() != 3) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "TPM attestation statement cetificate has an invalid version: " + attCert.getVersion());
                    return false;
                }

                //  Subject field MUST be set to empty.
                if (!attCert.getSubjectDN().getName().isEmpty()) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "TPM attestation statement cetificate has an invalid subject: " + attCert.getSubjectDN().getName());
                    return false;
                }

                //  The Subject Alternative Name extension MUST be set as defined in [TPMv2-EK-Profile] section 3.2.9.
                //      "The issuer MUST include TPM manufacturer, TPM part number and TPM firmware version,
                //      using the directoryName-form within the GeneralName structure...this extension MUST be
                //      critical if subjust is empty..."
                Collection<List<?>> subjectAlternativeNames = attCert.getSubjectAlternativeNames();
                if (subjectAlternativeNames == null) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "TPM attestation statement cetificate has an invalid subjectAlternativeNames: null");
                    return false;
                }

                Map<String, String> sanMap = new HashMap<>();

                //Need to parse names from list (TODO needs to be refactored)
                for (List<?> nameList : subjectAlternativeNames) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "Name: " + nameList.get(0));
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "Value: " + nameList.get(1));
                    int subjectAltNameType = (Integer) nameList.get(0);
                    if (subjectAltNameType == 4) {
                        String namesString = (String) nameList.get(1);
                        String[] names = namesString.split("\\,");
                        if(names.length < 3){
                            names = namesString.split("\\+");
                        }
                        for (String name : names) {
                            String[] namePair = name.split("\\=");
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                                    "name key: " + namePair[0]);
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                                    "name value: " + namePair[1]);
                            sanMap.put(namePair[0], namePair[1]);
                        }
                    }
                }

                if (!sanMap.containsKey("2.23.133.2.1")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "TPM attestation statement cetificate has an invalid subjectAlternativeNames: Missing Manufacturer");
                    return false;
                }

                if (!sanMap.containsKey("2.23.133.2.2")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "TPM attestation statement cetificate has an invalid subjectAlternativeNames: Missing Model");
                    return false;
                }

                if (!sanMap.containsKey("2.23.133.2.3")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "TPM attestation statement cetificate has an invalid subjectAlternativeNames: Missing Version");
                    return false;
                }

                //  The Extended Key Usage extension MUST contain the "joint-iso-itu-t(2) internationalorganizations(23) 133 tcg-kp(8) tcg-kp-AIKCertificate(3)" OID.
                List<String> keyUsage = attCert.getExtendedKeyUsage();
                if (!keyUsage.contains("2.23.133.8.3")) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "TPM attestation statement cetificate: missing extended key usage");
                    return false;
                }

                //The Basic Constraints extension MUST have the CA component set to false.
                if (attCert.getBasicConstraints() != -1) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "TPM attestation statement cetificate: Invalid Basic Constraints");
                    return false;
                }

                //If x5c contains an extension with OID 1 3 6 1 4 1 45724 1 1 4 (id-fido-gen-ce-aaguid) verify that the value of this extension matches the aaguid in authenticatorData.
                byte[] certAaguid = attCert.getExtensionValue("1.3.6.1.4.1.45724.1.1.4");
                if (certAaguid != null) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                            "Certificate contains aauid = " + Hex.toHexString(certAaguid));
                    if (!Arrays.equals(certAaguid, authData.getAttCredData().getAaguid())) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                            "TPM x5c's aaguid does not match");
                        return false;
                    }
                }

                return true;

            } else {
                //not supported yet
                return false;
            }

        } catch (NoSuchAlgorithmException | NoSuchProviderException
                | UnsupportedEncodingException | CertificateException ex) {
            Logger.getLogger(TPMAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Boolean.FALSE;
    }

    @Override
    public ArrayList getX5c() {
        return x5c;
    }

    /*
        Because of the awkward case in which the RSA exponent is stored
        as a byte of array of length 3, premade solutions for byte arrays to
        integer broke. This is a temporary solution to that problem.

        TODO figure out a different solution.
     */
    private static int getIntFromByteArray(byte[] bytes) {
        if (bytes.length > 4) {
            throw new InputMismatchException("Given array is larger than an Integer");
        }

        int result = 0;
        for (int i = 0; i < bytes.length; i++) {
            result |= Byte.toUnsignedInt(bytes[bytes.length - i - 1]) << 8 * i;
        }

        return result;
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
