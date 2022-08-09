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
 * The implementation of the FidoAuthenticator interface representing
 * operations of a FIDO2 Authenticator, and some additional methods
 * unique to StrongKey's implementation. Please see documentation of
 * these methods in the interface file.
 *
 * This class represents the method that lists the mobile device's keys
 * in the AndroidKeystore
 */

package com.strongkey.sacl.impl;

import android.content.ContextWrapper;
import android.security.keystore.KeyInfo;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.util.Enumeration;

@SuppressWarnings("DanglingJavadoc")
class ListAndroidKeystoreCredentials {

    // TAGs for logging
    private static final String TAG = ListAndroidKeystoreCredentials.class.getSimpleName();

    /**************************************************************
     *                                              888
     *                                              888
     *                                              888
     *  .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     * d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     * 88888888   X88K   88888888 888      888  888 888    88888888
     * Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     *  "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *************************************************************/

    /**
     * The code that lists out the mobile device's keys in the AndroidKeystore
     *
     * @param rpid           String containing the relying party's (RP) unique identification on the internet
     *                       (ex: a domain name: strongkey.com)
     * @param contextWrapper An instance of LocalContextWrapper passed in to be able to get to
     *                       resource files without convoluted code
     * @return JSONObject with the attestation details from the app's server, or error messages
     */

    protected static JSONObject execute(String rpid, ContextWrapper contextWrapper) {
        // Local variables
        String MTAG = "execute";

        // Response container object
        JSONObject mJSONObject;
        JSONArray mKeystoreEntries;

        // Open up AndroidKeystore and list out keys
        try {
            // Get information on the key-pair
            KeyInfo mKeyInfo;
            KeyStore mKeystore;

            // TODO: Check for local device authentication: PIN, Fingerprint, Face, etc.
            mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);
            int size = mKeystore.size();
            Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_size) + size);

            if (size > 0) {

                mKeystoreEntries = new JSONArray();
                String[] aliasesArray = new String[size];
                Enumeration<String> mKeyAliases = mKeystore.aliases();

                for (int i = 0; i < size; i++) {
                    String alias = mKeyAliases.nextElement();
                    Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_keystore_entry_name) + alias);
                    if (mKeystore.isKeyEntry(alias)) {
                        Log.v(TAG, contextWrapper.getResources().getString(R.string.message_keystore_entry) + ": " + alias);
                        mKeystoreEntries.put(new JSONObject().put("keyname", alias));

                        KeyStore.Entry mKeystoreEntry = mKeystore.getEntry(alias, null);
//                        Set<KeyStore.Entry.Attribute> attributes = mKeystoreEntry.getAttributes();
//                        for (KeyStore.Entry.Attribute attribute : attributes) {
//                            Log.v(TAG, contextWrapper.getResources().getString(R.string.message_keystore_entry_attribute) + ": " + attribute);
//                        }
                    }


//                PrivateKey privateKey = mKeyPair.getPrivate();
//                KeyFactory factory;
//                factory = KeyFactory.getInstance(privateKey.getAlgorithm(), Constants.FIDO2_KEYSTORE_PROVIDER);
//                mKeyInfo = factory.getKeySpec(privateKey, KeyInfo.class);
//
//                // Check the origin of the key - if Generated, it was generated inside AndroidKeystore
//                // but not necessarily in hardware - emulators do support the AndroidKeystore in
//                // software, so it can be misleading without attestation check
//                assert mKeyInfo != null;
//                switch (mKeyInfo.getOrigin()) {
//                    case KeyProperties.ORIGIN_GENERATED:
//                        mKeyOrigin = Constants.KEY_ORIGIN.GENERATED;
//                        break;
//                    case KeyProperties.ORIGIN_IMPORTED:
//                        mKeyOrigin = Constants.KEY_ORIGIN.IMPORTED;
//                        break;
//                    case KeyProperties.ORIGIN_UNKNOWN:
//                    default:
//                        mKeyOrigin = Constants.KEY_ORIGIN.UNKNOWN;
//                }
//
//                // Log verbose key information
//                String mAlgorithm = privateKey.getAlgorithm() + " [" + Constants.FIDO2_KEY_ECDSA_CURVE + "]";
//                Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_keyname) + mKeyInfo.getKeystoreAlias());
//                Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_origin) + mKeyOrigin);
//                Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_algorithm) + mAlgorithm);
//                Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_size) + mKeyInfo.getKeySize());
//                Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_userauth) + mKeyInfo.isUserAuthenticationRequired());
//                Log.v(TAG, contextWrapper.getResources().getString(R.string.vmessage_semodule) + mKeyInfo.isInsideSecureHardware() + " [" + mSecurityModule + "]");
                }
                mJSONObject = new JSONObject().put("keys", mKeystoreEntries);
            } else {
                String mErrorString = contextWrapper.getResources().getString(R.string.ERROR_ANDROID_KEYSTORE_EMPTY);
                Log.w(TAG, mErrorString);
                return Common.jsonError(TAG, MTAG, Constants.ERROR_EMPTY_KEYSTORE, mErrorString);
            }
        } catch (CertificateException |
                IOException |
                JSONException |
                KeyStoreException |
                NoSuchAlgorithmException |
                UnrecoverableEntryException e) {
            e.printStackTrace();
            try {
                return Common.jsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
        return mJSONObject;
    }
}
