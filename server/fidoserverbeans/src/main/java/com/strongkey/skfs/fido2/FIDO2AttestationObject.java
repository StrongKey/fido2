/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.strongkey.cbor.jacob.CborDecoder;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class FIDO2AttestationObject {

    String attFormat;
    FIDO2AuthenticatorData authData;
    FIDO2AttestationStatement attStmt;

    public String getAttFormat() {
        return attFormat;
    }

    public FIDO2AuthenticatorData getAuthData() {
        return authData;
    }

    public FIDO2AttestationStatement getAttStmt() {
        return attStmt;
    }

    public void decodeAttestationObject(String attestationObject) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {
        byte[] authenticatorData = null;
        Object attestationStmt = null;

        ByteArrayInputStream m_bais = new ByteArrayInputStream(Base64.getUrlDecoder().decode(attestationObject));
        PushbackInputStream m_is = new PushbackInputStream(m_bais);
        CborDecoder m_stream = new CborDecoder(m_is);

         long len = m_stream.readMapLength();
         Map<String, Object> attObjectMap = new HashMap<>();
            for (long i = 0; len < 0 || i < len; i++) {
                String key = (String) SKFSCommon.readGenericItem(m_stream);
                if (len < 0 && (key == null)) {
                    // break read...
                    break;
                }
                Object value = SKFSCommon.readGenericItem(m_stream);
                attObjectMap.put(key, value);
            }
        
//        for (String key : attObjectMap.keySet()) {
        for (Map.Entry<String,Object> entry : attObjectMap.entrySet()) {
            String key = entry.getKey();
            if (key.equalsIgnoreCase("fmt")) {
                attFormat = entry.getValue().toString();
            } else if (key.equalsIgnoreCase("authData")) {
                authenticatorData = (byte[]) entry.getValue();
            } else if (key.equalsIgnoreCase("attStmt")) {
                attestationStmt = entry.getValue();
            }
        }
        authData = new FIDO2AuthenticatorData();
        authData.decodeAuthData(authenticatorData);

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "ATTFORMAT = "  +attFormat);
        switch (attFormat) {
            case "fido-u2f":
                attStmt = new U2FAttestationStatment();
                attStmt.decodeAttestationStatement(attestationStmt);
                break;

            case "packed":
                attStmt = new PackedAttestationStatement();
                attStmt.decodeAttestationStatement(attestationStmt);
                break;
            
            case "apple":
                attStmt = new AppleAttestationStatement();
                attStmt.decodeAttestationStatement(attestationStmt);
                break;

            case "tpm":
                attStmt = new TPMAttestationStatement();
                attStmt.decodeAttestationStatement(attestationStmt);
                break;

            case "android-key":
                attStmt = new AndroidKeyAttestationStatement();
                attStmt.decodeAttestationStatement(attestationStmt);
                break;

            case "android-safetynet":
                attStmt = new AndroidSafetynetAttestationStatement();
                attStmt.decodeAttestationStatement(attestationStmt);
                break;

            case "none":
                attStmt = new NoneAttestationStatement();
                attStmt.decodeAttestationStatement(attestationStmt);
                break;

            default:
                throw new IllegalArgumentException("Invalid attestation format");
        }

    }
}
