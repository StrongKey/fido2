/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
* **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * FIDO2 Attestation object - currently only supports the "packed" format
 */

package com.strongauth.skfs.fido2.artifacts;

import co.nstant.in.cbor.CborBuilder;
import com.strongkey.cbor.jacob.CborEncoder;
import java.io.ByteArrayOutputStream;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

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

    public FIDO2AttestationObject(String attFormat, FIDO2AuthenticatorData authData) {
        this.attFormat = attFormat;
        this.authData = authData;
    }

    public String encodeAttestationObject(byte[] encodedAuthData, byte[] tbs, String attestationType, PrivateKey pvtKey, PublicKey pubKey) {

//        System.out.println("ATTFORMAT = " + attFormat);
        switch (attFormat) {
//            case "fido-u2f":
//                attStmt = new U2FAttestationStatment();
//                attStmt.decodeAttestationStatement(attestationStmt);
//                break;
            case "packed":
                attStmt = new PackedAttestationStatement((long) -7, attestationType, null);
                break;
//            case "tpm":
//                attStmt = new TPMAttestationStatement();
//                attStmt.decodeAttestationStatement(attestationStmt);
//                break;
//            case "android-key":
//                attStmt = new AndroidKeyAttestationStatement();
//                attStmt.decodeAttestationStatement(attestationStmt);
//                break;
//            case "android-safetynet":
//                attStmt = new AndroidSafetynetAttestationStatement();
//                attStmt.decodeAttestationStatement(attestationStmt);
//                break;
//            case "none":
//                attStmt = new NoneAttestationStatement();
//                attStmt.decodeAttestationStatement(attestationStmt);
//                break;
            default:
                throw new IllegalArgumentException("Invalid attestation type");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
        CborEncoder cbe = new CborEncoder(baos);
        try {
            if (attStmt.getAttestationType().equalsIgnoreCase("self")) {
                System.out.println("SELF");
                cbe.writeMapStart(3);
                // First element
                cbe.writeTextString("fmt");
                cbe.writeTextString(attFormat);

                // Second element
                cbe.writeTextString("authData");
                cbe.writeByteString(encodedAuthData);

                // Third element
                cbe.writeTextString("attStmt");
                cbe.writeMapStart(2);

                // First sub-element
                cbe.writeTextString("alg");
                cbe.writeInt(attStmt.getAlg());
                
                // SEcond sub-element
                cbe.writeTextString("sig");
                cbe.writeByteString(attStmt.signwithCredentialKey(pvtKey, tbs));
        
        
                new co.nstant.in.cbor.CborEncoder(baos2).encode(new CborBuilder()
                        .addMap()
                        .put("authData", encodedAuthData)
                        .put("fmt", attFormat)
                        .putMap("attStmt")
                        .put("alg", attStmt.getAlg())
                        .put("sig", attStmt.signwithCredentialKey(pvtKey, tbs))
                        .end()
                        .end()
                        .build()
                );
            } else if (attStmt.getAttestationType().equalsIgnoreCase("basic")) {
                System.out.println("BASIC");
                
                cbe.writeMapStart(3);
                // First element
                cbe.writeTextString("authData");
                cbe.writeByteString(encodedAuthData);

                // Second element
                cbe.writeTextString("fmt");
                cbe.writeTextString(attFormat);

                // Third element
                cbe.writeTextString("attStmt");
                cbe.writeMapStart(3);

                // First sub-element
                cbe.writeTextString("alg");
                cbe.writeInt(attStmt.getAlg());
                
                // SEcond sub-element
                cbe.writeTextString("sig");
                cbe.writeByteString(attStmt.signwithCredentialKey(pvtKey, tbs));
                
                // Third sub-element
                cbe.writeTextString("x5c");
                cbe.writeArrayStart(1);
                
                // First sub-sub-element
                cbe.writeByteString(attStmt.getX5c());
                
                new co.nstant.in.cbor.CborEncoder(baos2).encode(new CborBuilder()
                        .addMap()
                        .put("authData", encodedAuthData)
                        .put("fmt", attFormat)
                        .putMap("attStmt")
                        .put("alg", attStmt.getAlg())
                        .put("sig", attStmt.signwithAttestationKey(tbs))
                        .putArray("x5c")
                        .add(attStmt.getX5c())
                        .end()
                        .end()
                        .end()
                        .build()
                );
            } else {
                Logger.getLogger(FIDO2AttestationObject.class.getName()).
                    log(Level.SEVERE, null, "Invalid Attestation Type: " +
                            attStmt.getAttestationType());
                return null;
            }

            byte[] encodedAttObject = baos.toByteArray();
            byte[] encodedAttObject2 = baos2.toByteArray();
            
//            System.out.println("********************");
//            System.out.println(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(encodedAttObject));
//            System.out.println("");
//            System.out.println(java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(encodedAttObject2));
//            System.out.println("********************");
            
            return java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(encodedAttObject2);

        } catch (Exception ex) {
            Logger.getLogger(FIDO2AttestationObject.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
