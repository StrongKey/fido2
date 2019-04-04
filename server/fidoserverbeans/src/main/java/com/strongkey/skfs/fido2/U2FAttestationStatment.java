/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido2;

import com.google.common.primitives.Bytes;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class U2FAttestationStatment implements FIDO2AttestationStatement {
    byte[] signature;
    ArrayList x5c = null;
    private final String attestationType = "basic";     //TODO support attca
    String validataPkix = skfsCommon.getConfigurationProperty("skfs.cfg.property.pkix.validate");
    String validataPkixMethod = skfsCommon.getConfigurationProperty("skfs.cfg.property.pkix.validate.method");

    @Override
    public void decodeAttestationStatement(Object attestationStmt) {
        Map<String, Object> attStmtObjectMap = (Map<String, Object>) attestationStmt;
        for (String key : attStmtObjectMap.keySet()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Key attstmt U2f: " + key);
            switch (key) {
                case "sig":
                    signature = (byte[]) attStmtObjectMap.get(key);
                    break;
                case "x5c":
                    x5c = (ArrayList) attStmtObjectMap.get(key);
                    break;
            }
        }
    }

    @Override
    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData) {
        ECKeyObject ecKeyObj = null;

        List<X509Certificate> certchain = new ArrayList<>();

        try {
            if(!Arrays.equals(authData.getAttCredData().getAaguid(), new byte[16])){
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "u2f AAGUID is not zero");
                return false;
            }
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    x5c.size());
            Iterator x5cItr = x5c.iterator();
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            byte[] certByte = (byte[]) x5cItr.next();
            InputStream instr = new ByteArrayInputStream(certByte);
            X509Certificate attCert = (X509Certificate) certFactory.generateCertificate(instr);
            
            PublicKey certPublicKey = attCert.getPublicKey();
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    certPublicKey.getAlgorithm());
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Signed Bytes Input: " + browserDataBase64);
            if (authData.getAttCredData().getFko() instanceof ECKeyObject) {
                ecKeyObj = (ECKeyObject) authData.getAttCredData().getFko();
            }
            byte[] signedBytes = Bytes.concat(new byte[]{0}, authData.getRpIdHash(), skfsCommon.getDigestBytes(Base64.getDecoder().decode(browserDataBase64), "SHA256"), authData.getAttCredData().getCredentialId(),
                    new byte[]{0x04}, ecKeyObj.getX(), ecKeyObj.getY());

            Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA", "BCFIPS");
            ecdsaSignature.initVerify(certPublicKey);
            ecdsaSignature.update(signedBytes);
            return ecdsaSignature.verify(signature);
//        return Boolean.FALSE;
        } catch (CertificateException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException | InvalidKeyException | SignatureException ex) {
            Logger.getLogger(U2FAttestationStatment.class.getName()).log(Level.SEVERE, null, ex);
        }
        return Boolean.FALSE;
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
