/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 *
 * **********************************************
 *
 * 888b    888          888
 * 8888b   888          888
 * 88888b  888          888
 * 888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 * 888 Y88b888 d88""88b 888    d8P  Y8b 88K
 * 888  Y88888 888  888 888    88888888 "Y8888b.
 * 888   Y8888 Y88..88P Y88b.  Y8b.          X88
 * 888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * Generates a key-pair as part of the FIDO Registration ceremony.
 * See https://www.w3.org/TR/webauthn/#op-make-cred for details.
 */

package com.strongkey.sacl.crypto;

import android.content.ContextWrapper;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.roomdb.PreregisterChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.jce.provider.BouncyCastleProvider;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

@SuppressWarnings("DanglingJavadoc")
public class AuthenticatorMakeCredential {

    public final static String TAG = AuthenticatorMakeCredential.class.getSimpleName();

    /**
     * Returns a PublicKeyCredential object to the calling application - based on W3C standard at:
     * https://www.w3.org/TR/webauthn/#op-make-cred
     * Requirements for generating the new credential in the standard are as follows; all these
     * are encapsulated in the PreregisterChallenge object:
     *
     *      String clientDataHash,
     *      String rpid,
     *      String userid
     *      String displayName,
     *      boolean requireResidentKey,
     *      boolean requireUserPresence, (inverse of requireUserVerification)
     *      boolean requireUserVerification,
     *      JSONArray publicKeyCredentialParams,
     *      JSONArray excludeCredentials,
     *      Map extensions (if any)
     *
     * @param context Context for resource strings
     * @param preregisterChallenge PreregisterChallenge with necessary information for keygen
     * @param webserviceOrigin String containing the URL of the fully qualified domain name of the
     * site the app connects to. For example, if the app is communicating with
     * "https://demo.strongkey.com/mdba/rest/authenticateFidoKey" (the webserviceOrigin), this will
     * translate on the server to an RPID of "strongkey.com" (where the TLD is "com" and the +1
     * domain component is "strongkey").
     * @return PublicKeyCredential object, if successful
     */
    public static Object execute(ContextWrapper context,
                                 PreregisterChallenge preregisterChallenge,
                                 String webserviceOrigin) {

        // Get necessary values out of PreregisterChallenge
        int mId = preregisterChallenge.getId();
        long mUid = preregisterChallenge.getUid();
        int mDid = preregisterChallenge.getDid();
        String mRpid = preregisterChallenge.getRpid();
        String mUserid = preregisterChallenge.getUserid();
        String mUsername = preregisterChallenge.getUsername();
        String mDisplayname = preregisterChallenge.getDisplayName();
        String mChallenge = preregisterChallenge.getChallenge();

        try {
            /**
             *  d888
             * d8888
             *   888
             *   888
             *   888
             *   888
             *   888
             * 8888888
             *********************** STEP 1 ***********************
             * Step 1 - Create ClientData SHA256 Base64Url encoded string
             * Data structure for CollectedClientData is as follows:
             *  dictionary CollectedClientData {
             *     required DOMString           type;       // Hard-coded to "public-key"
             *     required DOMString           challenge;  // Sent by FIDO2 server
             *     required DOMString           origin;     // Must be identical to rpid (verified by SACL)
             *     TokenBinding                 tokenBinding; // Optional - empty for now
             * };
             */
            String rfc6454Origin = Common.getRfc6454Origin(webserviceOrigin);
            String tldOrigin = Common.getTldPlusOne(webserviceOrigin);
            if (!mRpid.equalsIgnoreCase(tldOrigin)) {
                Log.w(TAG, context.getString(R.string.origin_rpid_mismatch) +
                        " origin: " + tldOrigin + ", rpid: " + mRpid);
                return null;
            }
            String clientDataJsonString = Common.getB64UrlSafeClientDataString(Constants.FIDO_OPERATION.CREATE, mChallenge, rfc6454Origin);
            String clientDataHash = Common.getB64UrlSafeClientDataHash(Constants.FIDO_OPERATION.CREATE, mChallenge, rfc6454Origin);
            Log.v(TAG, "\nclientDataJsonString: " + clientDataJsonString + "\nCalculated Base64Urlsafe clientDataHash: " + clientDataHash);


            /**
             *  .d8888b.
             * d88P  Y88b
             *        888
             *      .d88P
             *  .od888P"
             * d88P"
             * 888"
             * 888888888
             *********************** STEP 2 ***********************
             * Step 2 - Generate the public-private key-pair using ECDSA (mostly)
             * https://developer.android.com/training/articles/keystore
             * https://developer.android.com/reference/android/security/keystore/KeyGenParameterSpec
             *
             *  returns JSONObject newkey = new JSONObject()
             *     .put(Constants.FIDO2_KEY_LABEL_KEYNAME, mKeyInfo.getKeystoreAlias())
             *     .put(Constants.FIDO2_KEY_LABEL_ORIGIN, mKeyOrigin)
             *     .put(Constants.FIDO2_KEY_LABEL_ALGORITHM, mAlgorithm)
             *     .put(Constants.FIDO2_KEY_LABEL_SIZE, mKeyInfo.getKeySize())
             *     .put(Constants.FIDO2_KEY_LABEL_USER_AUTH, mKeyInfo.isUserAuthenticationRequired())
             *     .put(Constants.FIDO2_KEY_LABEL_SEMODULE, mSecureHardware)
             *     .put(Constants.FIDO2_KEY_LABEL_HEX_PUBLIC_KEY, Hex.toHexString(mPublicKey.getEncoded()));
             */
            // First generate the credential ID
            String mCredentialId = Common.getNewCredentialId(mRpid, mUserid);
            if (mCredentialId == null)
                return null;
            Log.v(TAG, "CREDENTIALID=" + mCredentialId);

            JSONObject newkey = AndroidKeystoreKeyGeneration.execute(mCredentialId, clientDataHash, context);
            if (newkey == null) {
                Log.w(TAG, context.getString(R.string.aks_keygen_failed));
                return null;
            } else {
                Log.v(TAG, "newkey is not a NULL: " + newkey.toString());
                if (newkey.has("error")) {
                    Log.w(TAG, newkey.getJSONObject("error").toString(2));
                    return newkey;
                }
            }
            // No keygen errors
            Log.v(TAG, "Generated key-pair: " + newkey.toString());

            // Key-pair generated - create PublicKeyCredential object for persistence to RoomDB
            PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
            publicKeyCredential.setId(0);
            publicKeyCredential.setPrcId(mId);
            publicKeyCredential.setCounter(Constants.FIDO_COUNTER_ZERO);
            publicKeyCredential.setDid(mDid);
            publicKeyCredential.setUid(mUid);
            publicKeyCredential.setRpid(mRpid);
            publicKeyCredential.setUserid(mUserid);
            publicKeyCredential.setUsername(mUsername);
            publicKeyCredential.setDisplayName(mDisplayname);
            publicKeyCredential.setCredentialId(mCredentialId);
            publicKeyCredential.setClientDataJson(clientDataJsonString);
            publicKeyCredential.setType(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_TYPE);
            publicKeyCredential.setKeySize((int) newkey.get(Constants.FIDO2_KEY_LABEL_SIZE));
            publicKeyCredential.setKeyAlias((String) newkey.get(Constants.FIDO2_KEY_LABEL_KEYNAME));
            publicKeyCredential.setKeyOrigin(newkey.get(Constants.FIDO2_KEY_LABEL_ORIGIN).toString());
            publicKeyCredential.setSeModule((String) newkey.get(Constants.FIDO2_KEY_LABEL_SEMODULE));
            publicKeyCredential.setPublicKey(newkey.getString(Constants.FIDO2_KEY_LABEL_HEX_PUBLIC_KEY));
            publicKeyCredential.setKeyAlgorithm((String) newkey.get(Constants.FIDO2_KEY_LABEL_ALGORITHM));
            publicKeyCredential.setUserHandle(Common.urlEncode(newkey.toString().getBytes(StandardCharsets.UTF_8)));
            Log.v(TAG, "Built up publicKeyCredential: " + publicKeyCredential.toString());


            /**
             *  .d8888b.
             * d88P  Y88b
             *      .d88P
             *     8888"
             *      "Y8b.
             * 888    888
             * Y88b  d88P
             *  "Y8888P"
             *********************** STEP 3 ***********************
             * Step 3 - Create Attested Credential Data byte array
             * https://www.w3.org/TR/webauthn/#attested-credential-data
             * Array has to be CBOR map as follows:
             *      aaguid:  16 bytes
             *      credentialIdLength:  2 bytes with value: L
             *      credentialId:  L bytes
             *      credentialPublicKey:  Variable length - in CBOR, shown at link above
             */

            // First convert public-key string to PublicKey object
            byte[] pbkBytes = Hex.decode(publicKeyCredential.getPublicKey());
            byte[] cosePublicKey = new byte[0];
            int pbkLen = 0;
            X509EncodedKeySpec pbkSpec = new X509EncodedKeySpec(pbkBytes);
            try {
                PublicKey publicKey = KeyFactory.getInstance("EC", new BouncyCastleProvider()).generatePublic(pbkSpec);
                cosePublicKey = Common.coseEncodePublicKey(publicKey);
                pbkLen = cosePublicKey.length;
                Log.v(TAG, "COSE PublicKey [Len]: " + Hex.toHexString(cosePublicKey) + " ["+pbkLen+"]");
            } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
                ex.printStackTrace();
            }

            // Create byte array for attestedCredentialData
            byte[] credentialIdBytes = mCredentialId.getBytes("UTF-8");
            short cidblen = (short) credentialIdBytes.length;
            ByteBuffer twoBytes = ByteBuffer.allocate(2);
            twoBytes.putShort(cidblen);
            Log.v(TAG, "Allocating ByteBuffer with bytes: " + (16 + 2 + cidblen + pbkLen));
            ByteBuffer byteBuffer = ByteBuffer.allocate(16 + 2 + cidblen + pbkLen);
            byteBuffer.put(Hex.decode(Constants.WEBAUTHN_STRONGKEY_DEVP_AAGUID))
                      .put(twoBytes.array())
                      .put(credentialIdBytes)
                      .put(cosePublicKey);
            byte[] attestedCredentialData = byteBuffer.array();


            /**
             *     d8888
             *    d8P888
             *   d8P 888
             *  d8P  888
             * d88   888
             * 8888888888
             *       888
             *       888
             *********************** STEP 4 ***********************
             * Step 4 - Generate Authenticator Data
             * https://www.w3.org/TR/webauthn/#sec-authenticator-data
             * The byte-array for authenticatorData has the following structure:
             *
             * 32-bytes with SHA256 digest of RPID
             *  1-byte  with bit-flags providing information about UV, UP, etc.
             *  4-bytes with a signature counter
             *  L-bytes with attestedCredentialData - variable length
             *  N-bytes with extensions - variable length
             *
             */
            byte[] registrationFlags = Common.setFlags(Constants.ANDROID_KEYSTORE_DEFAULT_REGISTRATION_FLAGS);
            String[] extensions = {Constants.FIDO_EXTENSION_USER_VERIFICATION_METHOD};
            byte[] extensionOutput = Common.getCborExtensions(extensions, newkey.getString(Constants.FIDO2_KEY_LABEL_SEMODULE));
            int currentCounter = publicKeyCredential.getCounter();
            byteBuffer = ByteBuffer.allocate(37 + attestedCredentialData.length + extensionOutput.length);
            byteBuffer.put(Common.getSha256(mRpid))
                    .put(registrationFlags)
                    .put(Common.getCounterBytes(currentCounter + Constants.FIDO_COUNTER_ONE))
                    .put(attestedCredentialData)
                    .put(extensionOutput);
            byte[] authenticatorDataBytes = byteBuffer.array();
            publicKeyCredential.setCounter(currentCounter + Constants.FIDO_COUNTER_ONE);
            publicKeyCredential.setAuthenticatorData(Hex.toHexString(authenticatorDataBytes));
            Log.v(TAG, "Hex-encoded authenticatorData: " + Hex.toHexString(authenticatorDataBytes));


            /**
             * 888888888
             * 888
             * 888
             * 8888888b.
             *      "Y88b
             *        888
             * Y88b  d88P
             *  "Y8888P"
             *********************** STEP 4 ***********************
             * Step 5 - Final step - Get an AndroidKeystore Key attestation
             * https://www.w3.org/TR/webauthn/#android-key-attestation
             * Attestation JSON needs to be as follows - except for the signature (over the
             * concatenation of authenticatorData and clientDataHash) we have the other values
             * for this JSON in PublicKeyCredential:
             * {
             *   fmt: "android-key",
             *   attStmt: {
             *              alg: -7  // (for ECDSA, or -257 for RSA)
             *              sig: bytes  // (in what format?)
             *              x5c: [ credCert: bytes, * (caCert: bytes) ] // Array of certificate bytes
             *           }
             * }
             */
            JSONObject response = AndroidKeystoreAttestation.execute(authenticatorDataBytes,
                                                                           mCredentialId,
                                                                           clientDataHash,
                                                                           context);
            if (response != null) {
                JSONObject fidoAksAttestation = response.getJSONObject(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO);
                if (fidoAksAttestation != null) {
                    publicKeyCredential.setJsonAttestation(fidoAksAttestation.getJSONObject(
                            Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO_JSON_FORMAT).toString());
                    publicKeyCredential.setCborAttestation(fidoAksAttestation.getString(
                            Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO_CBOR_FORMAT));
                }
                return publicKeyCredential;
            }

        } catch (IOException | JSONException | NoSuchAlgorithmException je) {
            je.printStackTrace();
            try {
                Common.JsonError(TAG, "makeCredential", "exception", je.getLocalizedMessage());
            } catch (JSONException ex) {
                return null;
            }
        }
        // To satisfy compiler
        return null;
    }
}
