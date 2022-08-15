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
 * Generates a FIDO digital signature for authentication or a
 * transaction confirmation. If authentication, the Signature
 * parameter must be NULL; for transaction authorization, this must
 * be a non-NULL, previously initialized for signing using a specific
 * key after biometric authentication to the device by the user.
 */

package com.strongkey.sacl.crypto;

import android.content.ContextWrapper;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONException;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

@SuppressWarnings("DanglingJavadoc")
class AndroidKeystoreDigitalSignature {

    // TAGs for logging
    private static final String TAG = AndroidKeystoreDigitalSignature.class.getSimpleName();
    private static String MTAG = "execute";

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
     * Generates a FIDO digital signature over the concatenated authenticatorData and clientDataHash
     * using the private key of mCredentialId
     *
     * @param authenticatorDataBytes byte[] with calculated information for signing with PrivateKey
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
     * @param signatureObject JCE Signature object, previously initialized by Android BiometricPrompt
     * for transaction authorization; or NULL for just FIDO authentication
      * @return Object Either a digital signature object or a JSONError
     */

    protected static Object execute(byte[] authenticatorDataBytes,
                                    String mCredentialId,
                                    String clientDataHash,
                                    ContextWrapper contextWrapper,
                                    Signature signatureObject) {
        // Local variables
        Constants.KEY_ORIGIN mKeyOrigin;                   // Flag to indicate the origin of a key
        Constants.SECURITY_MODULE mSecurityModule = null;  // Flag to indicate if SE or TEE is present
        Certificate[] mCertificateChain;                   // Certificate chain to be attested
        PrivateKey mPrivateKey = null;                     // PrivateKey object
        KeyInfo mKeyInfo = null;                           // Information on PrivateKey


        // Retrieve newly generated key as part of attestation process
        try {
            // TODO: Check for local device authentication: PIN, Fingerprint, Face, etc.
            KeyStore mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);

            // Find key inside keystore
            Log.v(TAG, "CREDENTIALID=" + mCredentialId);
            KeyStore.Entry entry = mKeystore.getEntry(mCredentialId, null);
            if (entry != null) {
                if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                    String mErrorString = contextWrapper.getString(R.string.ERROR_NOT_PRIVATE_KEY);
                    Log.w(TAG, mErrorString);
                    return Common.JsonError(TAG, MTAG, Constants.ERROR_NOT_PRIVATE_KEY, mErrorString);
                } else {
                    mPrivateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                    KeyFactory factory;
                    factory = KeyFactory.getInstance(mPrivateKey.getAlgorithm(), Constants.FIDO2_KEYSTORE_PROVIDER);
                    mKeyInfo = factory.getKeySpec(mPrivateKey, KeyInfo.class);
                }
            }

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

            // Initialize objects for digital signature
            byte[] toBeSigned;
            String b64urlSignature;

            // Are we signing a business transaction or doing just FIDO authentication?
            if (signatureObject == null) {
                // Doing FIDO authentication
                // Initialize digital signature
                Signature s = Signature.getInstance(Constants.FIDO2_SIGNATURE_ALGORITHM);
                try {
                    s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
                } catch (UserNotAuthenticatedException e) {
                    String mErrorString = contextWrapper.getString(R.string.ERROR_UNAUTHENTICATED_USER);
                    Log.w(TAG, mErrorString);
                    return Common.JsonError(TAG, MTAG, Constants.ERROR_UNAUTHENTICATED_USER, mErrorString);
                }

                byte[] clientDataHashBytes = Common.urlDecode(clientDataHash);
                toBeSigned = new byte[authenticatorDataBytes.length + clientDataHashBytes.length];
                System.arraycopy(authenticatorDataBytes, 0, toBeSigned, 0, authenticatorDataBytes.length);
                System.arraycopy(clientDataHashBytes, 0, toBeSigned, authenticatorDataBytes.length, clientDataHashBytes.length);
                s.update(toBeSigned);
                byte[] digitalSignature = s.sign();
                b64urlSignature = Common.urlEncode(digitalSignature);
            } else {
                // Signing a business transaction - but confirm by checking something in the object
                if (signatureObject.getAlgorithm().equalsIgnoreCase(Constants.FIDO2_SIGNATURE_ALGORITHM)) {
                    byte[] clientDataHashBytes = Common.urlDecode(clientDataHash);
                    toBeSigned = new byte[authenticatorDataBytes.length + clientDataHashBytes.length];
                    System.arraycopy(authenticatorDataBytes, 0, toBeSigned, 0, authenticatorDataBytes.length);
                    System.arraycopy(clientDataHashBytes, 0, toBeSigned, authenticatorDataBytes.length, clientDataHashBytes.length);
                    signatureObject.update(toBeSigned);
                    byte[] digitalSignature = signatureObject.sign();
                    b64urlSignature = Common.urlEncode(digitalSignature);
                } else {
                    String mErrorString = contextWrapper.getString(R.string.ERROR_SIGNATURE_OBJECT_NOT_INITIALIZED);
                    Log.w(TAG, mErrorString);
                    return Common.JsonError(TAG, MTAG, Constants.ERROR_SIGNATURE_OBJECT_NOT_INITIALIZED, mErrorString);
                }
            }

            // Log and return the signature
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_tbs) + Hex.toHexString(toBeSigned) + "\n" +
                    contextWrapper.getString(R.string.vmessage_signature) + b64urlSignature);
            return b64urlSignature;

        } catch (KeyStoreException |
                CertificateException |
                IOException |
                NoSuchAlgorithmException |
                InvalidKeyException |
                SignatureException |
                NoSuchProviderException |
                InvalidKeySpecException |
                UnrecoverableEntryException |
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
