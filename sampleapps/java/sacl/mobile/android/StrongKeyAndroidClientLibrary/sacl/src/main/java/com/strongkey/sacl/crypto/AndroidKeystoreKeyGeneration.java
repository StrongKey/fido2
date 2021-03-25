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
 * Copyright (c) 2001-2020 StrongAuth, Inc. (DBA StrongKey)
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
 */

package com.strongkey.sacl.crypto;

import android.content.ContextWrapper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.security.keystore.StrongBoxUnavailableException;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;

@SuppressWarnings("DanglingJavadoc")
class AndroidKeystoreKeyGeneration {

    // TAGs for logging
    private static final String TAG = AndroidKeystoreKeyGeneration.class.getSimpleName();
    private static String MTAG = "generate";

    /******************************************************************
                                                  888
                                                  888
                                                  888
      .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     88888888   X88K   88888888 888      888  888 888    88888888
     Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
      "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *****************************************************************/

    /**
     * The code that tests the mobile device implementation for an SE or a TEE. Default values used
     * for key-generation constants are:
     *
     *      KEY_ALGORITHM_EC = ECDSA
     *      FIDO2_KEYSTORE_PROVIDER = AndroidKeystore
     *      PURPOSE_SIGN = Create a digital signature with the private key
     *      PURPOSE_VERIFY = Verify a digital signature with the public key
     *      FIDO2_KEY_ECDSA_CURVE = secp256r1, a NIST standard
     *      DIGEST_SHA256, _SHA384 and _SHA512 = Message digests for ECDSA signatures
     *      .setIsStrongBoxBacked(Boolean.TRUE) = Use a dedicated secure element - requires SDK 28 (Android P)
     *      .setUserAuthenticationRequired(Boolean.TRUE) = User must have device locked and require authentication to unlock
     *      FIDO2_USER_AUTHENTICATION_VALIDITY = 5 minutes
     *
     * If the hardware secure element is not available, the code will throw a StrongBoxUnavailableException,
     * upon which, the catch method will attempt to use the Trusted Execution Environment (TEE) to
     * generate the key-pair. Should usually work with TEE, but if there is no TEE, a key will be
     * generated anyway; the attestation validation will indicate if the key was generated in a
     * secure element, TEE or in software.
     *
     * @param mCredentialId String containing the FIDO credential ID
     * @param clientDataHash String containing a Base64Url encoded SHA256 digest of components
     * that make up the CollectedClientData JSON object described in the WebAuthn spec at
     * (https://www.w3.org/TR/webauthn/#dictdef-collectedclientdata)
     *
     *  dictionary CollectedClientData {
     *     required DOMString           type;       // Hard-coded to "public-key"
     *     required DOMString           challenge;  // Sent by FIDO2 server
     *     required DOMString           origin;     // Must be identical to rpid (verified by SACL)
     *     TokenBinding                 tokenBinding; // Optional - empty for now
     * };
     *
     * @param contextWrapper An instance of LocalContextWrapper passed in to be able to get to
     * resource files without convoluted code
     * @return JSONObject with the generated key's details, or error messages
     */

    protected static JSONObject execute(String mCredentialId, String clientDataHash, ContextWrapper contextWrapper)
    {
        // Local variables
        Constants.KEY_ORIGIN mKeyOrigin;            // Flag to indicate the origin of a key
        Constants.SECURITY_MODULE mSecurityModule;  // Flag to indicate if SE or TEE is present
        KeyPair mKeyPair;                           // The test key-pair being generated
        KeyPairGenerator mKeyPairGenerator;         // The key-pair generator

        // Generate key-pair in secure element, if available
        try {
            mKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeyPairGenerator.initialize(new KeyGenParameterSpec.Builder(mCredentialId, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec(Constants.FIDO2_KEY_ECDSA_CURVE))
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                    .setAttestationChallenge(Common.urlDecode(clientDataHash))
                    .setIsStrongBoxBacked(Boolean.TRUE) // TODO: Comment out for < Android 9, API 28 (Pie)
                    .setUserAuthenticationRequired(Boolean.TRUE)
                    .setUserAuthenticationValidityDurationSeconds(Constants.FIDO2_USER_AUTHENTICATION_VALIDITY * 60)
                    .build());
            mKeyPair = mKeyPairGenerator.generateKeyPair();
            mSecurityModule = Constants.SECURITY_MODULE.SECURE_ELEMENT;
            Log.i(TAG, contextWrapper.getString(R.string.message_keygen_success_se));
        } catch (IllegalStateException | InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            try {
                return Common.JsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        } catch (NoSuchMethodError | StrongBoxUnavailableException e) {
            // Failed to find a secure element; attempting to use TEE
            Log.w(TAG, contextWrapper.getString(R.string.message_keygen_failure_se));
            try {
                mKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, Constants.FIDO2_KEYSTORE_PROVIDER);
                mKeyPairGenerator.initialize(new KeyGenParameterSpec.Builder(mCredentialId, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec(Constants.FIDO2_KEY_ECDSA_CURVE))
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                        .setAttestationChallenge(Common.urlDecode(clientDataHash))
                        .setUserAuthenticationRequired(Boolean.TRUE)
                        .setUserAuthenticationValidityDurationSeconds(Constants.FIDO2_USER_AUTHENTICATION_VALIDITY * 60)
                        .build());
                mKeyPair = mKeyPairGenerator.generateKeyPair();
                mSecurityModule = Constants.SECURITY_MODULE.TRUSTED_EXECUTION_ENVIRONMENT;
                Log.i(TAG, contextWrapper.getString(R.string.message_keygen_success_tee));
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e2) {
                e2.printStackTrace();
                try {
                    return Common.JsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e2.getLocalizedMessage());
                } catch (JSONException e3) {
                    e3.printStackTrace();
                    return null;
                }
            }
        }

        // Retrieve newly generated key as part of attestation process
        try {
            // Get information on the key-pair
            KeyInfo mKeyInfo;
            KeyStore mKeystore;

            // TODO: Check for local device authentication: PIN, Fingerprint, Face, etc.
            mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);
            assert mKeyPair != null;
            PrivateKey mPrivateKey = mKeyPair.getPrivate();
            PublicKey mPublicKey = mKeyPair.getPublic();
            KeyFactory factory;
            factory = KeyFactory.getInstance(mPrivateKey.getAlgorithm(), Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeyInfo = factory.getKeySpec(mPrivateKey, KeyInfo.class);
            Log.v(TAG, "ECDSA PublicKey Format: " + mPublicKey.getFormat());

            // Check the origin of the key - if Generated, it was generated inside AndroidKeystore
            // but not necessarily in hardware - emulators do support the AndroidKeystore in
            // software, so it can be misleading without attestation check
            assert mKeyInfo != null;
            switch (mKeyInfo.getOrigin()) {
                case KeyProperties.ORIGIN_GENERATED:
                    mKeyOrigin = Constants.KEY_ORIGIN.GENERATED;
                    break;
                case KeyProperties.ORIGIN_IMPORTED:
                    mKeyOrigin = Constants.KEY_ORIGIN.IMPORTED;
                    break;
                case KeyProperties.ORIGIN_UNKNOWN:
                default:
                    mKeyOrigin = Constants.KEY_ORIGIN.UNKNOWN;
            }

            // Log verbose key information
            String mAlgorithm = mPrivateKey.getAlgorithm() + " [" + Constants.FIDO2_KEY_ECDSA_CURVE + "]";
            String mSecureHardware = mKeyInfo.isInsideSecureHardware() + " [" + mSecurityModule + "]";
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_keyname) + mKeyInfo.getKeystoreAlias());
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_origin) + mKeyOrigin);
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_algorithm) + mAlgorithm);
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_size) + mKeyInfo.getKeySize());
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_userauth) + mKeyInfo.isUserAuthenticationRequired());
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_semodule) + mSecureHardware);

            JSONObject newkey = new JSONObject()
                    .put(Constants.FIDO2_KEY_LABEL_KEYNAME, mKeyInfo.getKeystoreAlias())
                    .put(Constants.FIDO2_KEY_LABEL_ORIGIN, mKeyOrigin)
                    .put(Constants.FIDO2_KEY_LABEL_ALGORITHM, mAlgorithm)
                    .put(Constants.FIDO2_KEY_LABEL_SIZE, mKeyInfo.getKeySize())
                    .put(Constants.FIDO2_KEY_LABEL_USER_AUTH, mKeyInfo.isUserAuthenticationRequired())
                    .put(Constants.FIDO2_KEY_LABEL_SEMODULE, mSecureHardware)
                    .put(Constants.FIDO2_KEY_LABEL_HEX_PUBLIC_KEY, Hex.toHexString(mPublicKey.getEncoded()));

            Log.v(TAG, "Newly generated FIDO key: " + newkey.toString(2));
            return newkey;

        } catch (KeyStoreException |
                CertificateException |
                IOException |
                NoSuchAlgorithmException |
                NoSuchProviderException |
                InvalidKeySpecException |
                JSONException e) {
            e.printStackTrace();
            try {
                return Common.JsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }
}
