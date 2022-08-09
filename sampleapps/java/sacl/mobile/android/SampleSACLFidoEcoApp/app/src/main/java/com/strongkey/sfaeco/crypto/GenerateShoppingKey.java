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
 * Generates a key-pair for shopping demo - to be moved to SACL
 * eventually
 */

package com.strongkey.sfaeco.crypto;

import android.content.ContextWrapper;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.util.Log;

import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.LocalContextWrapper;
import com.strongkey.sfaeco.utilities.SfaConstants;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.Hex;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.math.BigInteger;
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
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Date;

import javax.security.auth.x500.X500Principal;

public class GenerateShoppingKey {

    // TAGs for logging
    private static final String TAG = GenerateShoppingKey.class.getSimpleName();
    private static String MTAG = "generate";


    private static final String CERT_RDN = "OU=StrongKey eStore, OU=For TEST Use Only, O=StrongKey";
    private static final String SIGN_ALGORITHM = "SHA256withECDSA";
    private static final long VALID = 730;

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

    public static boolean execute(String alias, String commonName, LocalContextWrapper context) {
        KeyStore mKeystore = null;
        JSONObject jsonObject = null;

        // TODO: Check for local device authentication: PIN, Fingerprint, Face, etc.
        try {
            mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);
            if (mKeystore.containsAlias(alias)) {
                Log.v(TAG, "Shopping key exists: " + alias);
                return true;
            }

            jsonObject = generateSigningKey(alias, commonName, context);
            if (jsonObject != null) {
                if (jsonObject.has("error")) {
                    return false;
                }
            }
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException e) {
            e.printStackTrace();
        }
        return true;
    }

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
     * @param mCredentialId String containing the FIDO credential ID (aka KeyStore alias)
     *
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
     * @param commonName String X500 Common Name for the self-signed certificate. Rest of the
     * DN is defined in the certificate-generating class
     * @param contextWrapper An instance of LocalContextWrapper passed in to be able to getSignatureObject to
     * resource files without convoluted code
     * @return JSONObject with the generated key's details, or error messages
     */

    private static JSONObject generateSigningKey(String mCredentialId, String commonName, ContextWrapper contextWrapper)
    {
        // Local variables
        Constants.KEY_ORIGIN mKeyOrigin;            // Flag to indicate the origin of a key
        Constants.SECURITY_MODULE mSecurityModule;  // Flag to indicate if SE or TEE is present
        KeyPair mKeyPair;                           // The test key-pair being generated
        KeyPairGenerator mKeyPairGenerator;         // The key-pair generator

        // Set certificate parameters for the new key-pair
        X500Principal x500Principal = new X500Principal(commonName.concat(",").concat(CERT_RDN));
        BigInteger sno = BigInteger.valueOf(new Date().getTime());
        Date startfrom = new Date();
        Date validto = new Date(startfrom.getTime() + (VALID * 24 * 60 * 60 * 1000));

        // Generate key-pair in secure element, if available
        try {
            mKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeyPairGenerator.initialize(new KeyGenParameterSpec.Builder(mCredentialId, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec(Constants.FIDO2_KEY_ECDSA_CURVE))
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                    .setUserAuthenticationRequired(Boolean.TRUE)
                    .setUserAuthenticationValidityDurationSeconds(SfaConstants.FIDO2_USER_AUTHENTICATION_ALWAYS)
                    .setCertificateSubject(x500Principal)
                    .setCertificateSerialNumber(sno)
                    .setCertificateNotBefore(startfrom)
                    .setCertificateNotAfter(validto)
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
        } catch (NoSuchMethodError e) {
            // Failed to find a secure element; attempting to use TEE
            Log.w(TAG, contextWrapper.getString(R.string.message_keygen_failure_se));
            try {
                mKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, Constants.FIDO2_KEYSTORE_PROVIDER);
                mKeyPairGenerator.initialize(new KeyGenParameterSpec.Builder(mCredentialId, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec(Constants.FIDO2_KEY_ECDSA_CURVE))
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                        .setUserAuthenticationRequired(Boolean.TRUE)
                        .setUserAuthenticationValidityDurationSeconds(SfaConstants.FIDO2_USER_AUTHENTICATION_ALWAYS)
                        .setCertificateSubject(x500Principal)
                        .setCertificateSerialNumber(sno)
                        .setCertificateNotBefore(startfrom)
                        .setCertificateNotAfter(validto)
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
            X509Certificate certificate = (X509Certificate) mKeystore.getCertificate(mCredentialId);
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
            Log.v(TAG, "X509Certificate: " + certificate);

            JSONObject newkey = new JSONObject()
                    .put(Constants.FIDO2_KEY_LABEL_KEYNAME, mKeyInfo.getKeystoreAlias())
                    .put(Constants.FIDO2_KEY_LABEL_ORIGIN, mKeyOrigin)
                    .put(Constants.FIDO2_KEY_LABEL_ALGORITHM, mAlgorithm)
                    .put(Constants.FIDO2_KEY_LABEL_SIZE, mKeyInfo.getKeySize())
                    .put(Constants.FIDO2_KEY_LABEL_USER_AUTH, mKeyInfo.isUserAuthenticationRequired())
                    .put(Constants.FIDO2_KEY_LABEL_SEMODULE, mSecureHardware)
                    .put(Constants.FIDO2_KEY_LABEL_HEX_PUBLIC_KEY, Hex.encodeHex(mPublicKey.getEncoded()));

            Log.v(TAG, "Newly generated Signing key: " + newkey.toString(2));
            return newkey;

        } catch (KeyStoreException | CertificateException | IOException | NoSuchAlgorithmException
                | NoSuchProviderException | InvalidKeySpecException | JSONException e) {
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
