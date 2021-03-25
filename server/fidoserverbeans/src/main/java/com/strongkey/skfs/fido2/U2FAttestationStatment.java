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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class U2FAttestationStatment implements FIDO2AttestationStatement {
    byte[] signature;
    ArrayList x5c = null;
    private final String attestationType = "basic";     //TODO support attca
    String validataPkix = SKFSCommon.getConfigurationProperty("skfs.cfg.property.pkix.validate");
    String validataPkixMethod = SKFSCommon.getConfigurationProperty("skfs.cfg.property.pkix.validate.method");

    @Override
    public void decodeAttestationStatement(Object attestationStmt) {
        Map<String, Object> attStmtObjectMap = (Map<String, Object>) attestationStmt;
//        for (String key : attStmtObjectMap.keySet()) {
        for (Map.Entry<String,Object> entry : attStmtObjectMap.entrySet()) {
            String key = entry.getKey();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Key attstmt U2f: " + key);
            switch (key) {
                case "sig":
                    signature = (byte[]) attStmtObjectMap.get(key);
                    break;
                case "x5c":
                    x5c = (ArrayList) attStmtObjectMap.get(key);
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData) {
        ECKeyObject ecKeyObj = null;

        try {
            if(!Arrays.equals(authData.getAttCredData().getAaguid(), new byte[16])){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015",
                        "u2f AAGUID is not zero");
                return false;
            }
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    x5c.size());
            Iterator x5cItr = x5c.iterator();
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");

            byte[] certByte = (byte[]) x5cItr.next();
            InputStream instr = new ByteArrayInputStream(certByte);
            X509Certificate attCert = (X509Certificate) certFactory.generateCertificate(instr);

            PublicKey certPublicKey = attCert.getPublicKey();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    certPublicKey.getAlgorithm());
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Signed Bytes Input: " + browserDataBase64);
            if (authData.getAttCredData().getFko() instanceof ECKeyObject) {
                ecKeyObj = (ECKeyObject) authData.getAttCredData().getFko();
            } else{
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0003", "Invalid Key (NOT of type EC)");
                return Boolean.FALSE;
            }
            
            byte[] firstbyte = new byte[]{0};
            byte[] rpidhasharray = authData.getRpIdHash();
            byte[] browserdatabytearray = SKFSCommon.getDigestBytes(Base64.getDecoder().decode(browserDataBase64), "SHA256");
            byte[] credidarray = authData.getAttCredData().getCredentialId();
            byte[] fifthbyte = new byte[]{0x04};
            byte[] ecobjX = ecKeyObj.getX();
            byte[] ecobjY = ecKeyObj.getY();
            
            byte[] signedBytes = new byte[firstbyte.length + rpidhasharray.length + browserdatabytearray.length + credidarray.length + fifthbyte.length + ecobjX.length + ecobjY.length];

            int destinationPosition = 0;
            System.arraycopy(firstbyte, 0, signedBytes, destinationPosition, firstbyte.length);
            destinationPosition += fifthbyte.length;
            System.arraycopy(rpidhasharray, 0, signedBytes, destinationPosition, rpidhasharray.length);
            destinationPosition += rpidhasharray.length;
            System.arraycopy(browserdatabytearray, 0, signedBytes, destinationPosition, browserdatabytearray.length);
            destinationPosition += browserdatabytearray.length;
            System.arraycopy(credidarray, 0, signedBytes, destinationPosition, credidarray.length);
            destinationPosition += credidarray.length;
            System.arraycopy(fifthbyte, 0, signedBytes, destinationPosition, fifthbyte.length);
            destinationPosition += fifthbyte.length;
            System.arraycopy(ecobjX, 0, signedBytes, destinationPosition, ecobjX.length);
            destinationPosition += ecobjX.length;
            System.arraycopy(ecobjY, 0, signedBytes, destinationPosition, ecobjY.length);
  
            Signature ecdsaSignature = Signature.getInstance("SHA256withECDSA", "BCFIPS");
            ecdsaSignature.initVerify(certPublicKey);
            ecdsaSignature.update(signedBytes);
            return ecdsaSignature.verify(signature);
//        return Boolean.FALSE;
        } catch (CertificateException | NullPointerException| NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException | InvalidKeyException | SignatureException ex) {
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
