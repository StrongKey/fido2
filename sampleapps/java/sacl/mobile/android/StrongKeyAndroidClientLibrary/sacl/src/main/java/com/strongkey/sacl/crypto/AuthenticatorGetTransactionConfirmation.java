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
 * Copyright (c) 2001-2022 StrongAuth, Inc. (DBA StrongKey)
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
 * The coordinating class that generates a digital signature to a
 * transaction confirmation challenge from the StrongKey FIDO server.
 * While this class uses the same terminology as the WebAuth spec,
 * this is a native implementation from StrongKey based on concepts
 * originally defined in StrongKey's U2F Server implementation
 * (see https://sourceforge.net/projects/skce/ for details).
 */

package com.strongkey.sacl.crypto;

import android.content.ContextWrapper;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.AuthorizationSignature;
import com.strongkey.sacl.roomdb.PreauthenticateChallenge;
import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.Signature;

@SuppressWarnings("DanglingJavadoc")
public class AuthenticatorGetTransactionConfirmation {

    public final static String TAG = AuthenticatorGetTransactionConfirmation.class.getSimpleName();

    /**
     * Signs an assertion on a challenge sent by the FIDO server.  Process is defined on W3C site
     * at: https://www.w3.org/TR/webauthn-2/#sctn-op-get-assertion
     *
     * @param context Context for resource strings
     * @param preauthorizeChallenge PreauthorizeChallenge with necessary information
     * @param publicKeyCredential PublicKeyCredential object of the user
     * @param counter int value of the updated counter for the credential
     * @param webserviceOrigin String containing the URL of the fully qualified domain name of the
     * site the app connects to. For example, if the app is communicating with
     * "https://demo.strongkey.com/mdba/rest/authenticateFidoKey" (the webserviceOrigin), this will
     * translate on the server to an RPID of "strongkey.com" (where the TLD is "com" and the +1
     * domain component is "strongkey").
     * @param signature JCE Signature object. This is weird because Android's BiometricPrompt
     * requires that the Signature object be created within its cryptoObject context. So, this
     * Signature object was initialized with the authorized user's credentialId based on the
     * successful biometric device authentication performed by the user
     * @return JSONObject
     */
    public static Object execute(ContextWrapper context,
                                 PreauthorizeChallenge preauthorizeChallenge,
                                 PublicKeyCredential publicKeyCredential,
                                 int counter,
                                 String webserviceOrigin,
                                 Signature signature) {

        String MTAG = "execute";

        // Get necessary values out of PreregisterChallenge
        int mId = preauthorizeChallenge.getId();
        long mUid = preauthorizeChallenge.getUid();
        int mDid = preauthorizeChallenge.getDid();
        String mTxid = preauthorizeChallenge.getTxid();
        String mTxpayload = preauthorizeChallenge.getTxpayload();
        String mRpid = preauthorizeChallenge.getRpid();
        String mChallenge = preauthorizeChallenge.getChallenge();

        // Confirm that mRpid is not null in the preauthorize challenge - we must use
        // RFC 6454 origin for RPID if FIDO server did not send an RPID
        if (mRpid == null)
            mRpid = Common.getRfc6454Origin(webserviceOrigin);

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
            // Origin and RPID match
            String clientDataJsonString = Common.getB64UrlSafeClientDataString(Constants.FIDO_OPERATION.GET, mChallenge, rfc6454Origin);
            String clientDataHash = Common.getB64UrlSafeClientDataHash(Constants.FIDO_OPERATION.GET, mChallenge, rfc6454Origin);
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
             * Step 2 - Get the PublicKeyCredential for the user
             */
            String mCredentialId = publicKeyCredential.getCredentialId();
            Log.v(TAG, "Using credentialId: " + mCredentialId);


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
             * Step 3 - Create AuthenticationSignature object
             */
            AuthorizationSignature authorizationSignature = new AuthorizationSignature();
            authorizationSignature.setId(0);
            authorizationSignature.setPazId(mId);
            authorizationSignature.setDid(mDid);
            authorizationSignature.setUid(mUid);
            authorizationSignature.setRpid(mRpid);
            authorizationSignature.setTxid(mTxid);
            authorizationSignature.setTxpayload(mTxpayload);
            authorizationSignature.setChallenge(mChallenge);
            authorizationSignature.setCredentialId(mCredentialId);
            authorizationSignature.setClientDataJson(clientDataJsonString);
            Log.v(TAG, "Built up authorizationSignature object: " + authorizationSignature.toString());


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
             *  N-bytes with extensions - variable length
             *  N-bytes with clientDataHash
             *
             */
            byte[] authenticationFlags = Common.setFlags(Constants.ANDROID_KEYSTORE_DEFAULT_AUTHORIZATION_FLAGS);
            String[] extensions = {Constants.FIDO_EXTENSION_USER_VERIFICATION_METHOD};
            byte[] extensionOutput = Common.getCborExtensions(extensions, publicKeyCredential.getSeModule());
            int currentCounter = counter;
            ByteBuffer byteBuffer = ByteBuffer.allocate(37 + extensionOutput.length);
            byteBuffer.put(Common.getSha256(mRpid))
                    .put(authenticationFlags)
                    .put(Common.getCounterBytes(currentCounter))
                    .put(extensionOutput);
            byte[] authenticatorDataBytes = byteBuffer.array();
            authorizationSignature.setAuthenticatorData(Common.urlEncode(authenticatorDataBytes));
            Log.v(TAG, "Base64Url-encoded authenticatorData: " + authorizationSignature.getAuthenticatorData());


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
             * Step 5 - Final step - Get a digital signature
             * https://www.w3.org/TR/2019/REC-webauthn-1-20190304/#op-get-assertion
             */
            Object response = AndroidKeystoreDigitalSignature.execute(authenticatorDataBytes,
                                                                      mCredentialId,
                                                                      clientDataHash,
                                                                      context,
                                                                      signature);
            if (response != null) {
                // Did we get an JSON error
                if (response instanceof JSONObject) {
                    return response;
                }
//                else {
                    authorizationSignature.setSignature((String) response);
//                }
                return authorizationSignature;
            }

        } catch (IOException | JSONException je) {
            je.printStackTrace();
            try {
                Common.JsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, je.getLocalizedMessage());
            } catch (JSONException ex) {
                return null;
            }
        }
        // To satisfy compiler
        return null;
    }
}
