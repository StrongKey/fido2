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
 * The implementation of the FidoAuthenticator interface representing
 * operations of a FIDO2 Authenticator, and some additional methods
 * unique to StrongKey's implementation. Please see documentation of
 * these methods in the interface file.
 */

package com.strongkey.sacl.impl;

import android.content.Context;
import android.content.ContextWrapper;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.interfaces.FidoAuthenticator;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;

public class FidoAuthenticatorImpl implements FidoAuthenticator {

    // TAGs for logging
    private static final String TAG = FidoAuthenticatorImpl.class.getSimpleName();
    private static String MTAG = null;

    // Private objects
    private static LocalContextWrapper mLocalContextWrapper;
    private Constants.ANDROID_KEYSTORE_TASK mTask;


    // Constructor with Context
    public FidoAuthenticatorImpl(Context context) {
        mLocalContextWrapper = new LocalContextWrapper(context);
    }


    /********************************************************
     *                        888
     *                        888
     *                        888
     * 88888b.d88b.   8888b.  888  888  .d88b.
     * 888 "888 "88b     "88b 888 .88P d8P  Y8b
     * 888  888  888 .d888888 888888K  88888888
     * 888  888  888 888  888 888 "88b Y8b.     d8b d8b d8b
     * 888  888  888 "Y888888 888  888  "Y8888  Y8P Y8P Y8P
     ********************************************************/

    /**
     *
     * @param rpid String containing the relying party's (RP) unique identification on the internet
     * (ex: a domain name: strongkey.com)
     * @param userid String containing a unique value for the user's identification within the
     * relying party's web-application(s) (ex: johndoe)
     * @param challenge String containing the unique hex-encoded challenge (nonce - number used
     * once) which must be part of the data digitally signed by the keystore to provide an
     * AndroidKeystore attestation to the FIDO2 server
     * @param clientDataHash byte[] A byte array containing the SHA256 digest of the client data
     * structure (https://www.w3.org/TR/webauthn/#sec-client-data) containing:
     *  i) the type of operation - makeCredential or getAssertion
     *  ii) the unique challenge (nonce) for the operation from the FIDO2 server
     *  iii) the origin of the website the app is communicating with
     *  iv) Token Binding (not supported in this release, but may be included)
     * @param excludeCredentials A String array containing the list of credentials (credential ids)
     * known within the relying party's web-applications for this specific user, which implies that
     * if one of those credential ids is within this AndroidKeystore, a new credential should NOT
     * be created for this user - FIDO protocols prohibit the creation of duplicate credentials
     * for the same user on the same keystore
     *
     * @return JSONObject
     */

    @Override
    public JSONObject makeCredential(String rpid, String userid, String challenge, byte[] clientDataHash, String[] excludeCredentials) {
        MTAG = "makeCredential";
        return null;
    }


    /*****************************************************************
     *                                             888
     *                                             888
     *                                             888
     *  8888b.  .d8888b  .d8888b   .d88b.  888d888 888888
     *     "88b 88K      88K      d8P  Y8b 888P"   888
     * .d888888 "Y8888b. "Y8888b. 88888888 888     888
     * 888  888      X88      X88 Y8b.     888     Y88b.  d8b d8b d8b
     * "Y888888  88888P'  88888P'  "Y8888  888      "Y888 Y8P Y8P Y8P
     *****************************************************************/

    /**
     *
     * @param rpid String containing the relying party's (RP) unique identification on the internet
     * (ex: a domain name: strongkey.com)
     * @param credentialId String created by this library (and stored by the RP) to identify the
     * unique key-pair of the user for this RP within this keystore
     * @param challenge String containing the unique hex-encoded challenge (nonce - number used
     * once) which must be part of the data digitally signed by the keystore to provide an
     * AndroidKeystore attestation to the FIDO2 server
     * @param clientDataHash byte[] A byte array containing the SHA256 digest of the client data
     * structure (https://www.w3.org/TR/webauthn/#sec-client-data) containing:
     *  i) the type of operation - makeCredential or getAssertion
     *  ii) the unique challenge (nonce) for the operation from the FIDO2 server
     *  iii) the origin of the website the app is communicating with
     *  iv) Token Binding (not supported in this release, but may be included)
     * @param allowedCredentials A String array containing the list of credentials (credential ids)
     * known within the relying party's web-applications for this specific user, which implies that
     * if one of those credential ids is within this AndroidKeystore, then it can be used to create
     * the digital signature response to the FIDO2 server
     *
     * @return JSONObject
     */

    @Override
    public JSONObject getAssertion(String rpid, String credentialId, String challenge, byte[] clientDataHash, String[] allowedCredentials) {
        MTAG = "getAssertion";
        return null;
    }


    /**************************************
     * 888 d8b          888
     * 888 Y8P          888
     * 888              888
     * 888 888 .d8888b  888888
     * 888 888 88K      888
     * 888 888 "Y8888b. 888
     * 888 888      X88 Y88b.  d8b d8b d8b
     * 888 888  88888P'  "Y888 Y8P Y8P Y8P
     **************************************/

    /**
     * Retrieves all credential within the AndroidKeystore for a specific RP. This should, in theory,
     * only return one credential since the app should have a single username associated with it in
     * the mobile device, and based on Android authentication to the device, only that user's keys
     * will be visible in the AndroidKeystore. Since a keystore may only have one key per userid
     * for a specific RP, it will return only a single credential public-key - the private-key
     * cannot be extracted out of the AndroidKeystore.
     *
     * @param rpid String containing the relying party's (RP) unique identification on the internet
     * (ex: a domain name: strongkey.com)
     *
     * @return JSONObject containing public information about the credential
     */

    @Override
    public JSONObject listCredentials(String rpid) {

        // Entry log
        MTAG = "listCredentials";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        // Create or Get LocalContextWrapper to access app resources
        if (mLocalContextWrapper == null) {
            String mErrorString = mLocalContextWrapper.getString(R.string.ERROR_NULL_CONTEXT);
            Log.w(TAG, mErrorString);
            try {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.jsonError(TAG, MTAG, Constants.ERROR_NULL_CONTEXT, mErrorString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Not an emulator - generate the key-pair and get an attestation
        JSONObject jo = ListAndroidKeystoreCredentials.execute(rpid, mLocalContextWrapper);
        timeout = Common.nowms();
        Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
        return jo;
    }


    /*************************************************************
     *      888          888          888
     *      888          888          888
     *      888          888          888
     *  .d88888  .d88b.  888  .d88b.  888888  .d88b.
     * d88" 888 d8P  Y8b 888 d8P  Y8b 888    d8P  Y8b
     * 888  888 88888888 888 88888888 888    88888888
     * Y88b 888 Y8b.     888 Y8b.     Y88b.  Y8b.     d8b d8b d8b
     *  "Y88888  "Y8888  888  "Y8888   "Y888  "Y8888  Y8P Y8P Y8P
     *************************************************************/

    /**
     *
     * @param rpid String containing the relying party's (RP) unique identification on the internet
     * (ex: a domain name: strongkey.com)
     * @param credentialId String created by this library (and stored by the RP) to identify the
     * unique key-pair of the user for this RP within this keystore
     *
     * @return boolean value indicating the object was deleted successfully
     */

    @Override
    public boolean deleteCredential(String rpid, String credentialId) {
        MTAG = "deleteCredential";
        return false;
    }

    /***********************************************************************************************
     *                   888     .d8888b.  d8b           .d88888b.  888         d8b                   888
     *                   888    d88P  Y88b Y8P          d88P" "Y88b 888         Y8P                   888
     *                   888    Y88b.                   888     888 888                               888
     *  .d88b.   .d88b.  888888  "Y888b.   888  .d88b.  888     888 88888b.    8888  .d88b.   .d8888b 888888
     * d88P"88b d8P  Y8b 888        "Y88b. 888 d88P"88b 888     888 888 "88b   "888 d8P  Y8b d88P"    888
     * 888  888 88888888 888          "888 888 888  888 888     888 888  888    888 88888888 888      888
     * Y88b 888 Y8b.     Y88b.  Y88b  d88P 888 Y88b 888 Y88b. .d88P 888 d88P    888 Y8b.     Y88b.    Y88b.
     *  "Y88888  "Y8888   "Y888  "Y8888P"  888  "Y88888  "Y88888P"  88888P"     888  "Y8888   "Y8888P  "Y888
     *      888                                     888                         888
     * Y8b d88P                                Y8b d88P                        d88P
     *  "Y88P"                                  "Y88P"                       888P"
     ***********************************************************************************************/
    /**
     * In order to use the Android BiometricPrompt, it requires an initialized JCE Signature
     * object to instantiate the CryptoObject used by the biometric API. This method performs
     * that function.
     *
     * @param credentialId   String to identify the key alias in AndroidKeystore
     * @param contextWrapper ContextWrapper to resolve resource strings for logging
     * @return Object - either a Signature object or a JSONObject with an error
     */
    @Override
    public Object getSignatureObject(String credentialId, ContextWrapper contextWrapper) {
        MTAG = "getSignatureObject";

        // Local variables
        Constants.KEY_ORIGIN mKeyOrigin;                   // Flag to indicate the origin of a key
        Constants.SECURITY_MODULE mSecurityModule = null;  // Flag to indicate if SE or TEE is present
        PrivateKey mPrivateKey = null;                     // PrivateKey object
        KeyInfo mKeyInfo = null;                           // Information on PrivateKey


        // Retrieve newly generated key as part of attestation process
        try {
            // TODO: Check for local device authentication: PIN, Fingerprint, Face, etc.
            KeyStore mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);

            // Find key inside keystore
            Log.v(TAG, "CREDENTIALID=" + credentialId);
            KeyStore.Entry entry = mKeystore.getEntry(credentialId, null);
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

            // Initialize Signature object
            Signature s = Signature.getInstance(Constants.FIDO2_SIGNATURE_ALGORITHM);
            try {
                s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            } catch (UserNotAuthenticatedException e) {
                String mErrorString = contextWrapper.getString(R.string.ERROR_UNAUTHENTICATED_USER);
                Log.w(TAG, mErrorString);
                return Common.JsonError(TAG, MTAG, Constants.ERROR_UNAUTHENTICATED_USER, mErrorString);
            }
            return s;

        } catch (KeyStoreException |
                CertificateException |
                IOException |
                NoSuchAlgorithmException |
                InvalidKeyException |
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
