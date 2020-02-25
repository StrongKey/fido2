/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
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
        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);
        byte[] authenticatorData = null;
        Object attestationStmt = null;
        CBORParser parser = f.createParser(org.apache.commons.codec.binary.Base64.decodeBase64(attestationObject));
        Map<String, Object> attObjectMap = mapper.readValue(parser, new TypeReference<Map<String, Object>>() {
        });

        //Verify cbor is properly formatted cbor (no extra bytes)
        if(parser.nextToken() != null){
            throw new IllegalArgumentException("FIDO2AttestationObject contains invalid CBOR");
        }

        for (String key : attObjectMap.keySet()) {
            if (key.equalsIgnoreCase("fmt")) {
                attFormat = attObjectMap.get(key).toString();
            } else if (key.equalsIgnoreCase("authData")) {
                authenticatorData = (byte[]) attObjectMap.get(key);
            } else if (key.equalsIgnoreCase("attStmt")) {
                attestationStmt = attObjectMap.get(key);
            }
        }
        authData = new FIDO2AuthenticatorData();
        authData.decodeAuthData(authenticatorData);

        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
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
