/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido2;

import com.strongkey.appliance.objects.JWT;
import com.strongkey.skce.utilities.PKIXChainValidation;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.security.cert.CertPath;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonArray;
import javax.json.JsonNumber;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

class AndroidSafetynetAttestationStatement implements FIDO2AttestationStatement {
    private String version = null;
    private byte[] response = null;
    private com.strongkey.appliance.objects.JWT jwt = null;
    private final String attestationType = "basic";



    static{
        Security.addProvider(new BouncyCastleFipsProvider());
    }

    @Override
    public void decodeAttestationStatement(Object attStmt) {
        Map<String, Object> attStmtObjectMap = (Map<String, Object>) attStmt;
//        for (String key : attStmtObjectMap.keySet()) {
        for (Map.Entry<String,Object> entry : attStmtObjectMap.entrySet()) {
            String key = entry.getKey();
            switch (key) {
                case "ver":
                    version = (String) entry.getValue();
                    break;
                case "response":
                    response = (byte[]) entry.getValue();
                    try {
                        jwt = new JWT(new String(response, "UTF-8"));
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(AndroidSafetynetAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    break;
                default :
                    break;
            }
        }
    }

    @Override
    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData) {
        try {
            //Verify JWT timestamp is valid
            JsonNumber timestampMs = jwt.getBody().getJsonNumber("timestampMs");
            Date now = new Date();
            if (timestampMs == null //timestampMS is missing
                    || timestampMs.longValue() > now.getTime() + (60 * 1000)        //timestampMS is in the future (some hardcoded buffer)  (TODO fix hardcode)
                    || timestampMs.longValue() < now.getTime() - (60 * 1000)) {     //timestampMS is older than 1 minute
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "JWT time stamp = " + timestampMs.longValue() + ", current time = " + now.getTime());
                throw new IllegalArgumentException("JWT has invalid timestampMs");
            }

            //Verify JWT certificate chain
            JsonArray x5c = jwt.getHeader().getJsonArray("x5c");
            if (x5c == null || x5c.isEmpty()) {
                throw new IllegalArgumentException("JWT missing x5c information");
            }
            if (x5c.size() < 2) {
                throw new IllegalArgumentException("JWT missing certificate chain");
            }
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BCFIPS");
            Base64.Decoder decoder = Base64.getDecoder();
            List<X509Certificate> certchain = new ArrayList<>();
            X509Certificate rootCert = null;
            for (int i = 0; i < x5c.size(); i++) {
                byte[] certBytes = decoder.decode(x5c.getString(i, null));
                ByteArrayInputStream instr = new ByteArrayInputStream(certBytes);
                X509Certificate certificate = (X509Certificate) certFactory.generateCertificate(instr);
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "certificate number " + i + " = " + certificate);
                if(i == x5c.size() - 1){
                    rootCert = certificate;
                }
                else{
                    certchain.add(certificate);
                }
            }
            if(rootCert == null){
                throw new IllegalArgumentException("JWT missing certificate chain root");
            }
            Set<TrustAnchor> trustAnchor = new HashSet<>();
            trustAnchor.add(new TrustAnchor(rootCert, null));
            CertPath certPath = CertificateFactory.getInstance("X.509", "BCFIPS").generateCertPath(certchain);
            if(!PKIXChainValidation.pkixvalidate(certPath, trustAnchor, false, false)){     //TODO check CRLs if they exist, otherwise don't
                throw new IllegalArgumentException("JWT failed PKIX validation");
            }

            //Verify JWT signature
            if (!jwt.verifySignature(certchain.get(0).getPublicKey())) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "JWT Signature verification failed!");
                return false;
            }

            //Verify that response is a valid SafetyNet response of version ver.
            if(version == null || version.isEmpty()){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "AndroidSafetynet missing version information");
                return false;
            }

            //Verify that the nonce in the response is identical to the SHA-256 hash of the concatenation of authenticatorData and clientDataHash.
            String nonce = jwt.getBody().getString("nonce", null);
            if(nonce == null || !Arrays.equals(decoder.decode(nonce), SKFSCommon.getDigestBytes(concatenateArrays(authData.getAuthDataDecoded(),
                    SKFSCommon.getDigestBytes(Base64.getDecoder().decode(browserDataBase64), "SHA256")), "SHA256"))){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "JWT has incorrect nonce");
                return false;
            }

            //Verify that the attestation certificate is issued to the hostname "attest.android.com" (see SafetyNet online documentation).
            String cn = getFirstCN(certchain.get(0).getSubjectDN().getName());
            if(cn == null || !cn.equals("attest.android.com")){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "JWT attestation certificate does not match the specification");
                return false;
            }

            //Verify that the ctsProfileMatch attribute in the payload of response is true.
            if(!jwt.getBody().getBoolean("ctsProfileMatch", false)){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "JWT attestation ctsProfileMatch does not match the specification");
                return false;
            }

            return true;
        } catch (UnsupportedEncodingException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException ex) {
            Logger.getLogger(AndroidSafetynetAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
        }

        return Boolean.FALSE;
    }

    @Override
    public ArrayList getX5c(){
        ArrayList<byte[]> result = new ArrayList();
        try{
            JsonArray jwtCerts = jwt.getHeader().getJsonArray("x5c");
            for(int certificateIndex = 0; certificateIndex < jwtCerts.size(); certificateIndex++){
                result.add(Base64.getDecoder().decode(jwtCerts.getString(certificateIndex)));
            }
        } catch (UnsupportedEncodingException ex) {
            return null;
        }

        return result;
    }

    private String getFirstCN(String principal){
        String[] attributes = principal.split(",");
        for(String attribute: attributes){
            if(attribute.startsWith("CN=")){
                return attribute.substring("CN=".length());
            }
        }

        return null;
    }

    private byte[] concatenateArrays(byte[] array1, byte[] array2){
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
