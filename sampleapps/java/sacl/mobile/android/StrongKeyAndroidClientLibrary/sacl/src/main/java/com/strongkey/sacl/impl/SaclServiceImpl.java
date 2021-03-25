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
 * The implementation of the SaclService interface representing
 * webservice operations sent to a business application that uses
 * StrongKey's MDKMS. The sample MDBA represents such a sample
 * implementation. Please see documentation of these methods in
 * the interface file.
 */

package com.strongkey.sacl.impl;

import android.content.Context;
import android.content.res.Resources;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Base64;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.interfaces.SaclService;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sacl.webservices.CallWebservice;

import org.erdtman.jcs.JsonCanonicalizer;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.util.ASN1Dump;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.util.BigIntegers;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.util.Arrays;

public class SaclServiceImpl implements SaclService {

    // TAGs for logging
    private static final String TAG = SaclServiceImpl.class.getSimpleName();
    private static String MTAG = null;

    // Private objects
    private static LocalContextWrapper mLocalContextWrapper;
    private static Resources mResources;


    // Constructor with Context
    public SaclServiceImpl(Context context) {
        mLocalContextWrapper = new LocalContextWrapper(context);
        mResources = mLocalContextWrapper.getResources();
    }

//    /***********************************************************************************
//     *          888                        888      888     888
//     *          888                        888      888     888
//     *          888                        888      888     888
//     *  .d8888b 88888b.   .d88b.   .d8888b 888  888 888     888 .d8888b   .d88b.  888d888
//     * d88P"    888 "88b d8P  Y8b d88P"    888 .88P 888     888 88K      d8P  Y8b 888P"
//     * 888      888  888 88888888 888      888888K  888     888 "Y8888b. 88888888 888
//     * Y88b.    888  888 Y8b.     Y88b.    888 "88b Y88b. .d88P      X88 Y8b.     888     d8b d8b
//     *  "Y8888P 888  888  "Y8888   "Y8888P 888  888  "Y88888P"   88888P'  "Y8888  888     Y8P Y8P
//     *
//     ***********************************************************************************
//     * @param did String with Domain ID on MDBA/MDKMS
//     * @param username String containing a username for checking if it is available
//     *
//     * @return
//     */
//    @Override
//    public boolean checkForUsername(String did, String username) {
//        return false;
//    }
//
//    /*****************************************************************
//     *                           d8b          888
//     *                           Y8P          888
//     *                                        888
//     * 888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888
//     * 888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"
//     * 888     88888888 888  888 888 "Y8888b. 888    88888888 888
//     * 888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888
//     * 888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888
//     *                       888
//     *                  Y8b d88P
//     *                   "Y88P"
//     *****************************************************************
//     * @param did String with Domain ID on MDBA/MDKMS
//     * @param username String containing the username
//     * @param password String containing the user's password
//     * @param givenName String containing the given (first) name of the user
//     * @param familyName String containing the family (last) name of the user
//     * @param email String containing an e-mail address of the user
//     * @param personalNumber String containing the personal mobile number of the user. This is
//     * necessary so One Time Passcodes (OTP) may be sent to the user for the FIDO registration
//     * confirmation process (after the device is accepted within the system)
//     * @param otherNumber String containing the mobile number of the phone that will be used
//     * for executing the MDBA app - usually the one on which this app is running
//     *
//     * @return
//     */
//    @Override
//    public JSONObject registerUser(String did, String username, String password, String givenName, String familyName, String email, String personalNumber, String otherNumber) {
//        return null;
//    }

    /**************************************************************
     * 88888b.  888d888 .d88b.  88888b.   .d88b.  .d8888b   .d88b.
     * 888 "88b 888P"  d88""88b 888 "88b d88""88b 88K      d8P  Y8b
     * 888  888 888    888  888 888  888 888  888 "Y8888b. 88888888
     * 888 d88P 888    Y88..88P 888 d88P Y88..88P      X88 Y8b.
     * 88888P"  888     "Y88P"  88888P"   "Y88P"   88888P'  "Y8888
     * 888                      888
     * 888                      888
     * 888                      888
     **************************************************************/
    /**
     * Propose the mobile device on which the user has enrolled, along with select elements of
     * the device's Build information so an MDBA Administrator may determine if the device may
     * be authorized for sending an attestation about the device's keystore.
     *
     * @param mDid String with Domain ID on MDBA/MDKMS
     * @param mUid String with User ID
     * @param mDeviceMobileNumber String with device's mobile number
     * @param mManufacturer String with the name of the device's manufacturer
     * @param mModel String with model name of the device
     * @param mOsRelease String the release number of the Android OS installed on the device
     * @param mOsSdkNumber String with the Android SDK number supported on the device
     * @param mFingerprint String containing a unique (to the extent Android claims) fingerprint
     * of the mobile device
     *
     * @return JSONObject with a response indicating if the proposed device was accepted for
     * verification of its keystore security.
     */
    @Override
    public JSONObject proposeMobileDevice(String mDid,
                                    String mUid,
                                    String mDeviceMobileNumber,
                                    String mManufacturer,
                                    String mModel,
                                    String mOsRelease,
                                    String mOsSdkNumber,
                                    String mFingerprint) {
        // Entry log
        MTAG = "proposeDevice";
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

        // Call the webservice and return a response
        JSONObject wsresponse;
        try {
            // Create the JSON object with input parameters that goes to the webservice
            // operation in the MDBA non-administrative servlet
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_DID, mDid)
                    .put(Constants.JSON_KEY_UID, mUid)
                    .put(Constants.JSON_KEY_USER_DEVICE, new JSONObject()
                            .put(Constants.JSON_KEY_DEVICE_MOBILE_NUMBER, mDeviceMobileNumber)
                            .put(Constants.JSON_KEY_DEVICE_MANUFACTURER, mManufacturer)
                            .put(Constants.JSON_KEY_DEVICE_MODEL, mModel)
                            .put(Constants.JSON_KEY_DEVICE_OS_RELEASE, mOsRelease)
                            .put(Constants.JSON_KEY_DEVICE_OS_SDK_NUMBER, mOsSdkNumber)
                            .put(Constants.JSON_KEY_DEVICE_FINGERPRINT, mFingerprint));

            Log.d(TAG, "mJSONObjectInput: " + mJSONObjectInput.toString());
            wsresponse = CallWebservice.execute(Constants.JSON_KEY_SACL_SERVICE_OPERATION_PROPOSE_DEVICE,
                    mJSONObjectInput, mLocalContextWrapper);

        } catch (JSONException | RuntimeException ex) {
            ex.printStackTrace();
            try {
                return Common.JsonError(TAG, "execute", "http", ex.getLocalizedMessage());
            } catch (JSONException e) {
                return null;
            }
        }
        timeout = Common.nowms();
        Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
        return wsresponse;
    }

    /**************************************************************************************
     *          888                        888             d8888 888    888             888
     *          888                        888            d88888 888    888             888
     *          888                        888           d88P888 888    888             888
     *  .d8888b 88888b.   .d88b.   .d8888b 888  888     d88P 888 888888 888888 .d8888b  888888
     * d88P"    888 "88b d8P  Y8b d88P"    888 .88P    d88P  888 888    888    88K      888
     * 888      888  888 88888888 888      888888K    d88P   888 888    888    "Y8888b. 888
     * Y88b.    888  888 Y8b.     Y88b.    888 "88b  d8888888888 Y88b.  Y88b.       X88 Y88b.  d8b d8b
     *  "Y8888P 888  888  "Y8888   "Y8888P 888  888 d88P     888  "Y888  "Y888  88888P'  "Y888 Y8P Y8P
     *  **************************************************************************************
     *
     * When a new user proposes a mobile device to be used with the MDBA, it must be
     * approved by the MDBA application, and authorized to send an AndroidKeystore
     * attestation. There may be a lag in getting that authorization; this webservice
     * checks the MDBA server to determine if that authorization has been granted.
     *
     * @param mDid String with Domain ID on MDBA/MDKMS
     * @param mUid String with User ID on MDBA
     * @param mDevid String containing the unique Device ID assigned when the proposed
     * device was accepted by the MDBA
     *
     * @return JSONObject indicating if the proposed device is pending review, rejected
     * or authorization has been granted. If granted, the authorization details are sent
     * back in the JSON response.
     */
    public JSONObject checkForAttestationAuthorization(String mDid, String mUid, String mDevid)
    {
        // Entry log
        MTAG = "checkForAttestationAuthorization";
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

        // Call the webservice and return a response
        JSONObject wsresponse;
        try {
            // Create the JSON object with input parameters that goes to the webservice
            // operation in the MDBA non-administrative servlet
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_DID, mDid)
                    .put(Constants.JSON_KEY_UID, mUid)
                    .put(Constants.JSON_KEY_DEVID, mDevid);

            Log.d(TAG, "mJSONObjectInput: " + mJSONObjectInput.toString());
            wsresponse = CallWebservice.execute(Constants.JSON_KEY_SACL_SERVICE_OPERATION_CHECK_ATTESTATION_AUTHORIZATION,
                    mJSONObjectInput, mLocalContextWrapper);

        } catch (JSONException | RuntimeException ex) {
            ex.printStackTrace();
            try {
                return Common.JsonError(TAG, "execute", "http", ex.getLocalizedMessage());
            } catch (JSONException e) {
                return null;
            }
        }
        timeout = Common.nowms();
        Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
        return wsresponse;
    }

    /********************************************************************************************
     *                   888 d8b      888          888                    d8888 888
     *                   888 Y8P      888          888                   d88888 888
     *                   888          888          888                  d88P888 888
     * 888  888  8888b.  888 888  .d88888  8888b.  888888  .d88b.      d88P 888 888  888 .d8888b
     * 888  888     "88b 888 888 d88" 888     "88b 888    d8P  Y8b    d88P  888 888 .88P 88K
     * Y88  88P .d888888 888 888 888  888 .d888888 888    88888888   d88P   888 888888K  "Y8888b.
     *  Y8bd8P  888  888 888 888 Y88b 888 888  888 Y88b.  Y8b.      d8888888888 888 "88b      X88
     *   Y88P   "Y888888 888 888  "Y88888 "Y888888  "Y888  "Y8888  d88P     888 888  888  88888P'
     **********************************************************************************************/
    /**
     * This method is used primarily to test the mobile device's capability to use either the
     * Trusted Execution Environment (TEE) or a dedicated hardware chip-backed Secure Element.
     * This must be performed when the mobile device has access to the network. The web-application
     * supporting this app must have been programmed to communicate with the Mobile Device Key
     * Management System (MDKMS) module on the Tellaro to support this capability. The user should
     * also run this test within 15 seconds of having unlocked the mobile device with their PIN,
     * password or biometric gesture.
     *
     * Generates a 256-bit ECDSA key-pair in the AndroidKeystore, extracts the certificate chain
     * and sends it to the web-application server of the app for validation, to determine if this
     * mobile device complies with the configured security policy of the site. Delegates the task
     * to a separate class to keep the tasks in their own files.
     *
     * @param mDid String with Domain ID on MDBA/MDKMS
     * @param mSid String with Server ID of the MDBA/MDKMS cluster
     * @param mUid String with User ID
     * @param mDevid String with Device ID
     * @param mAzid String with device's Authorization ID to attempt attestation
     * @param mArid String with Authorized to Register ID on MDKMS
     * @param mUsername String with user's username
     * @param mPassword String with user's password
     * @param mArid String with unique Authorization to Regiser ID within domain of the MDKMS
     * @param mChallenge String containing the hex-encoded challenge supplied by the MDKMS (through
     * the business web-application this app communicates with. The MDKMS requires this challenge
     * to be used in the key-generation process so it can determine that the attestation is a valid
     * one for this device.
     *
     * @return JSONObject with the results of the attestation that may be interpreted for display
     * to the user - usually someone who is familiar with technical details of the attestation.
     */

    @Override
    public JSONObject validateAndroidKeystore(String mDid,
                                              String mSid,
                                              String mUid,
                                              String mDevid,
                                              String mAzid,
                                              String mArid,
                                              String mUsername,
                                              String mPassword,
                                              String mChallenge) {

        // Entry log
        MTAG = "validateAndroidKeystore";
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

        // If running in an emulator, return false even though it generates a key
        boolean emulator = !(
                !Build.FINGERPRINT.contains("generic") &&
                        !Build.FINGERPRINT.startsWith("generic") &&
                        !Build.FINGERPRINT.startsWith("unknown") &&
                        !Build.MODEL.contains("google_sdk") &&
                        !Build.MODEL.contains("Emulator") &&
                        !Build.MODEL.contains("Android SDK built for x86") &&
                        !Build.BOARD.equals("QC_Reference_Phone") &&
                        !Build.MANUFACTURER.contains("Genymotion") &&
                        !Build.HOST.startsWith("Build") &&
                        !(Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic")) &&
                        !"google_sdk".equals(Build.PRODUCT));

        // If we're running in an emulator, don't waste time - we'll never get a
        // certificate chain that can be attested for an SE or a TEE
        if (emulator) {
            String mErrorString = mLocalContextWrapper.getResources().getString(R.string.ERROR_EMULATOR);
            Log.w(TAG, mErrorString);
            //sendMessage(Constants.TASK_TEST_ANDROID_KEYSTORE_RESPONSE_JSON, mErrorString);
            try {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.jsonError(TAG, MTAG, Constants.ERROR_EMULATOR, mErrorString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Not an emulator - generate the key-pair and get an attestation
        // Local variables
        Constants.KEY_ORIGIN mKeyOrigin;            // Flag to indicate the origin of a key
        Constants.SECURITY_MODULE mSecurityModule;  // Flag to indicate if SE or TEE is present
        Certificate[] mCertificateChain;            // Certificate chain to be attested
        KeyPair mKeyPair;                           // The test key-pair being generated
        KeyPairGenerator mKeyPairGenerator;         // The key-pair generator
        JSONObject wsresponse;                      // Webservice response

        // Generate key-pair in secure element, if available - note that the challenge
        // parameter is used to set the key's alias within the keystore
        try {
            mKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeyPairGenerator.initialize(new KeyGenParameterSpec.Builder(mChallenge, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                    .setAlgorithmParameterSpec(new ECGenParameterSpec(Constants.FIDO2_KEY_ECDSA_CURVE))
                    .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                    .setAttestationChallenge(mChallenge.getBytes())
//                    .setIsStrongBoxBacked(Boolean.TRUE) // TODO: Remove for Android 28 or greater
                    .setUserAuthenticationRequired(Boolean.TRUE)
                    .setUserAuthenticationValidityDurationSeconds(Constants.FIDO2_USER_AUTHENTICATION_VALIDITY * 60)
                    .build());
            mKeyPair = mKeyPairGenerator.generateKeyPair();
            mSecurityModule = Constants.SECURITY_MODULE.SECURE_ELEMENT;
            Log.i(TAG, mResources.getString(R.string.message_keygen_success_se));
        } catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
            try {
                return Common.jsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
//        } catch (StrongBoxUnavailableException | NoSuchMethodError e) {
        } catch (NoSuchMethodError e) {
            // Failed to find a secure element; attempting to use TEE
            Log.w(TAG, mResources.getString(R.string.message_keygen_failure_se));
            try {
                mKeyPairGenerator = KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, Constants.FIDO2_KEYSTORE_PROVIDER);
                mKeyPairGenerator.initialize(new KeyGenParameterSpec.Builder(mChallenge, KeyProperties.PURPOSE_SIGN | KeyProperties.PURPOSE_VERIFY)
                        .setAlgorithmParameterSpec(new ECGenParameterSpec(Constants.FIDO2_KEY_ECDSA_CURVE))
                        .setDigests(KeyProperties.DIGEST_SHA256, KeyProperties.DIGEST_SHA384, KeyProperties.DIGEST_SHA512)
                        .setAttestationChallenge(mChallenge.getBytes())
                        .setUserAuthenticationRequired(Boolean.TRUE)
                        .setUserAuthenticationValidityDurationSeconds(Constants.FIDO2_USER_AUTHENTICATION_VALIDITY * 60)
                        .build());
                mKeyPair = mKeyPairGenerator.generateKeyPair();
                mSecurityModule = Constants.SECURITY_MODULE.TRUSTED_EXECUTION_ENVIRONMENT;
                Log.i(TAG, mResources.getString(R.string.message_keygen_success_tee));
            } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchProviderException e2) {
                e2.printStackTrace();
                try {
                    return Common.jsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e2.getLocalizedMessage());
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
            PrivateKey privateKey = mKeyPair.getPrivate();
            KeyFactory factory;
            factory = KeyFactory.getInstance(privateKey.getAlgorithm(), Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeyInfo = factory.getKeySpec(privateKey, KeyInfo.class);

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
            String mAlgorithm = privateKey.getAlgorithm() + " [" + Constants.FIDO2_KEY_ECDSA_CURVE + "]";
            Log.v(TAG, mResources.getString(R.string.vmessage_keyname) + mKeyInfo.getKeystoreAlias());
            Log.v(TAG, mResources.getString(R.string.vmessage_origin) + mKeyOrigin);
            Log.v(TAG, mResources.getString(R.string.vmessage_algorithm) + mAlgorithm);
            Log.v(TAG, mResources.getString(R.string.vmessage_size) + mKeyInfo.getKeySize());
            Log.v(TAG, mResources.getString(R.string.vmessage_userauth) + mKeyInfo.isUserAuthenticationRequired());
            Log.v(TAG, mResources.getString(R.string.vmessage_semodule) + mKeyInfo.isInsideSecureHardware() + " [" + mSecurityModule + "]");

            // Find key inside keystore
            KeyStore.Entry entry = mKeystore.getEntry(mChallenge, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                String mErrorString = mResources.getString(R.string.ERROR_NOT_PRIVATE_KEY);
                Log.w(TAG, mErrorString);
                return Common.jsonError(TAG, MTAG, Constants.ERROR_NOT_PRIVATE_KEY, mErrorString);
            }

            // Initialize digital signature
            Signature s = Signature.getInstance(Constants.FIDO2_SIGNATURE_ALGORITHM);
            try {
                s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            } catch (UserNotAuthenticatedException e) {
                String mErrorString = mResources.getString(R.string.ERROR_UNAUTHENTICATED_USER);
                Log.w(TAG, mErrorString);
                return Common.jsonError(TAG, MTAG, Constants.ERROR_UNAUTHENTICATED_USER, mErrorString);
            }
            // Generate signature
            s.update(Constants.FIDO2_SIGNATURE_PLAINTEXT.getBytes());
            byte[] signature = s.sign();
            String b64signature = Base64.encodeToString(signature, Base64.DEFAULT);
            Log.v(TAG, mResources.getString(R.string.vmessage_signature) + b64signature);

            // Get certificate chain of newly generated key
            mCertificateChain = ((KeyStore.PrivateKeyEntry) entry).getCertificateChain();
            int numberOfCerts = mCertificateChain.length;
            if (numberOfCerts == 1) {
                String mErrorString = mResources.getString(R.string.ERROR_SINGLE_CERTIFICATE_IN_CHAIN);
                Log.w(TAG, mErrorString);
                return Common.jsonError(TAG, MTAG, Constants.ERROR_SINGLE_CERTIFICATE_IN_CHAIN, mErrorString);
            }

            // Extract the certificate chain into a JSONObject for the server
            StringWriter sw;
            PemWriter pemWriter;
            JSONArray jab = new JSONArray();

            /*
              Read PEM files and create a Json object that looks like this:

                   JsonObject jo = Json.createObjectBuilder()
                       .add("AKSCertificateChain", Json.createObjectBuilder()
                           .add("Size", 4)
                           .add("Certificates", Json.createArrayBuilder()
                               .add(pemcerts[0])
                               .add(pemcerts[1])
                               .add(pemcerts[2])
                               .add(pemcerts[3]))) .build();
             */
            for (int i = 0; i < numberOfCerts; i++) {
                X509Certificate x509cert = (X509Certificate) mCertificateChain[i];
                sw = new StringWriter();
                pemWriter = new PemWriter(sw);
                PemObject pemObject = new PemObject("CERTIFICATE", x509cert.getEncoded());
                pemWriter.writeObject(pemObject);
                pemWriter.close();
                sw.close();
                jab.put(i, sw.toString());
            }

            // Create the JSON object with input parameters that (eventually) goes to the
            // validateAndroidKeystore webservice in the MDKMS non-administrative servlet.
            // Technically, this goes to the MDBA first, which relays it to the MDKMS.
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_DID, mDid)
                    .put(Constants.JSON_KEY_SID, mSid)
                    .put(Constants.JSON_KEY_UID, mUid)
                    .put(Constants.JSON_KEY_DEVID, mDevid)
                    .put(Constants.JSON_KEY_AZID, mAzid)
                    .put(Constants.JSON_KEY_ARID, mArid)
                    .put(Constants.JSON_KEY_USERNAME, mUsername)
                    .put(Constants.JSON_KEY_PASSWORD, mPassword)
                    .put(Constants.JSON_KEY_CHALLENGE, mChallenge)
                    .put(Constants.JSON_KEY_ANDROID_OS_DETAILS_CONTAINER, getAndroidOsDetails())
                    .put(Constants.AKS_CERTIFICATE_CHAIN, new JSONObject()
                            .put(Constants.AKS_CERTIFICATE_CHAIN_SIZE, numberOfCerts)
                            .put(Constants.AKS_CERTIFICATE_CHAIN_CERTIFICATES, jab));

            // Get the attestation
            Log.v(TAG, "mJSONObjectInput: " + mJSONObjectInput.toString());

            // Get the attestation
            Log.v(TAG, mResources.getString(R.string.vmessage_number_of_certificates) + mCertificateChain.length);

            // Call the webservice and return a response
            wsresponse = CallWebservice.execute(Constants.JSON_KEY_SACL_SERVICE_OPERATION_VALIDATE_ANDROID_KEYSTORE,
                    mJSONObjectInput, mLocalContextWrapper);

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
                return Common.jsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
        timeout = Common.nowms();
        Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
        return wsresponse;
    }

    /**********************************************************************************************
     *                             .d8888b.  d8b                   888    d8P
     *                            d88P  Y88b Y8P                   888   d8P
     *                            Y88b.                            888  d8P
     *  .d88b.   .d88b.  88888b.   "Y888b.   888  .d88b.  88888b.  888d88K      .d88b.  888  888
     * d88P"88b d8P  Y8b 888 "88b     "Y88b. 888 d88P"88b 888 "88b 8888888b    d8P  Y8b 888  888
     * 888  888 88888888 888  888       "888 888 888  888 888  888 888  Y88b   88888888 888  888
     * Y88b 888 Y8b.     888  888 Y88b  d88P 888 Y88b 888 888  888 888   Y88b  Y8b.     Y88b 888
     *  "Y88888  "Y8888  888  888  "Y8888P"  888  "Y88888 888  888 888    Y88b  "Y8888   "Y88888
     *      888                                       888                                    888
     * Y8b d88P                                  Y8b d88P                               Y8b d88P
     *  "Y88P"                                    "Y88P"                                 "Y88P"
     **********************************************************************************************
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID of the user performing the signing operation
     * @param plid String with the Policy ID that defines attributes of the key to be generated
     * @param alias String with the alias of the new signing key within AndroidKeystore
     *
     * @return JSONObject containing the newly generated signing key
     */
    @Override
    public JSONObject generateSigningKey(String did, String uid, String plid, String alias) {
        // Entry log
        MTAG = "generateSigningKey";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        // TODO: Need to implement this
        try {
            String mErrorString = mLocalContextWrapper.getString(R.string.ERROR_NOT_IMPLEMENTED_YET);
            timeout = Common.nowms();
            Log.i(TAG, MTAG + "-OUT-" + timein + "-" + timeout + "|TTC=" + (timeout - timein));
            return Common.jsonError(TAG, MTAG, Constants.ERROR_NOT_IMPLEMENTED_YET, mErrorString);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        // For compiler
        return null;
    }

    /**********************************************************************************************
     *                                                      888            888888 888       888  .d8888b.
     *                                                      888              "88b 888   o   888 d88P  Y88b
     *                                                      888               888 888  d8b  888 Y88b.
     *  .d88b.   .d88b.  88888b.   .d88b.  888d888  8888b.  888888  .d88b.    888 888 d888b 888  "Y888b.
     * d88P"88b d8P  Y8b 888 "88b d8P  Y8b 888P"       "88b 888    d8P  Y8b   888 888d88888b888     "Y88b.
     * 888  888 88888888 888  888 88888888 888     .d888888 888    88888888   888 88888P Y88888       "888
     * Y88b 888 Y8b.     888  888 Y8b.     888     888  888 Y88b.  Y8b.       88P 8888P   Y8888 Y88b  d88P
     *  "Y88888  "Y8888  888  888  "Y8888  888     "Y888888  "Y888  "Y8888    888 888P     Y888  "Y8888P"
     *      888                                                             .d88P
     * Y8b d88P                                                           .d88P"
     *  "Y88P"                                                           888P"
     **********************************************************************************************
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID of the user performing the signing operation
     * @param alias String with the alias of the signing key within AndroidKeystore
     * @param payload String containing the business application payload to be signed. It is
     * important to understand that the payload will undergo JSON Canonicalization (RFC-8785) if
     * the string has an embedded JSON. This implies that the signature will be performed on a
     * JSON object that will most likely be transformed for RFC-8785 conformance before it
     * undergoes cryptographic processing.
     *
     * @return JSONObject containing the RFC-8785 JSON Web Signature
     */
    @Override
    public JSONObject generateJsonWebSignature(String did, String uid, String alias, String payload) {

        // Entry log
        MTAG = "generateJsonWebSignature";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        // Local variables
        String algorithm = null;
        String pemcert = null;
        JSONObject jws = null;
        KeyStore mKeystore = null;

        // Open AndroidKeystore and check for existence of the key
        try {
            mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);
            if (!mKeystore.containsAlias(alias)) {
                Log.e(TAG, mResources.getString(R.string.message_key_not_found) + alias);
                String mErrorString = mLocalContextWrapper.getString(R.string.message_key_not_found);
                Log.w(TAG, mErrorString);
                try {
                    timeout = Common.nowms();
                    Log.i(TAG, MTAG + "-OUT-" + timein + "-" + timeout + "|TTC=" + (timeout - timein));
                    return Common.jsonError(TAG, MTAG, Constants.ERROR_KEY_NOT_FOUND, mErrorString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.i(TAG, mResources.getString(R.string.message_key_found) + alias);

            // Check if the found key is a private key
            KeyStore.Entry entry = mKeystore.getEntry(alias, null);
            if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                Log.e(TAG, mResources.getString(R.string.message_key_not_private_key) + alias);
                String mErrorString = mLocalContextWrapper.getString(R.string.message_key_not_private_key);
                Log.w(TAG, mErrorString);
                try {
                    timeout = Common.nowms();
                    Log.i(TAG, MTAG + "-OUT-" + timein + "-" + timeout + "|TTC=" + (timeout - timein));
                    return Common.jsonError(TAG, MTAG, Constants.ERROR_NOT_PRIVATE_KEY, mErrorString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            Log.v(TAG, mResources.getString(R.string.message_key_acquired));

            // Initialize Signature object
            Signature signature = Signature.getInstance(Constants.FIDO2_SIGNATURE_ALGORITHM);
            signature.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            Log.v(TAG, mResources.getString(R.string.message_initialized_signature_object));

            // Get certificate associated with the key and place it in shared data model for app to use (if needed)
            X509Certificate certificate = (X509Certificate) mKeystore.getCertificate(alias);
//                saclSharedDataModel.setX509Certificate(cert); // TODO: Do we need SaclSharedDataModel here?
            Log.v(TAG, mResources.getString(R.string.label_subjectdn) + certificate.getSubjectX500Principal().getName());

            // Get necessary objects to prepare for signing
            java.util.Base64.Encoder encoder = java.util.Base64.getUrlEncoder();
            if (certificate != null) {
                // Write PEM-encoded certificate to string
                StringWriter sWriter = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(sWriter);
                pemWriter.writeObject(certificate);
                pemWriter.close();
                pemcert = sWriter.toString();
                Log.v(TAG, mResources.getString(R.string.label_pem_certificate) + pemcert);

                // Check algorithm - if EC, use ES256 (for now)
                String publicKeyAlgorithm = certificate.getPublicKey().getAlgorithm();
                if (publicKeyAlgorithm.equalsIgnoreCase(Constants.JWS_ALGORITHM_EC))
                    algorithm = Constants.JWS_ALGORITHM_ES256;
                else if (publicKeyAlgorithm.equalsIgnoreCase(Constants.JWS_ALGORITHM_RSA))
                    algorithm = Constants.JWS_ALGORITHM_RS256;
            } else {
                Log.e(TAG, mResources.getString(R.string.message_certificate_not_found) + alias);
                String mErrorString = mLocalContextWrapper.getString(R.string.message_certificate_not_found);
                Log.w(TAG, mErrorString);
                try {
                    timeout = Common.nowms();
                    Log.i(TAG, MTAG + "-OUT-" + timein + "-" + timeout + "|TTC=" + (timeout - timein));
                    return Common.jsonError(TAG, MTAG, Constants.ERROR_CERTIFICATE_NOT_FOUND, mErrorString);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // Assemble and Base64Url encode JWS elements for signing
            JSONObject headerjson = new JSONObject()
                    .put(Constants.JWS_ATTRIBUTE_ALG, algorithm)
                    .put(Constants.JWS_ATTRIBUTE_X5C, pemcert);
            Log.v(TAG, mResources.getString(R.string.message_jws_protected_unencoded) + headerjson);

            String b64payload = encoder.withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            Log.v(TAG, mResources.getString(R.string.message_jws_payload_unencoded) + payload);
            Log.v(TAG, mResources.getString(R.string.message_jws_payload_base64url) + b64payload);

            // Assemble plaintext for signing
            JSONObject plaintext = new JSONObject()
                    .put(Constants.JWS_ATTRIBUTE_PAYLOAD, b64payload)
                    .put(Constants.JWS_ATTRIBUTE_PROTECTED, headerjson);
            Log.v(TAG, mResources.getString(R.string.message_jws_tbs_unencoded) + plaintext);

            // Use JCS (RFC-8785) to canonicalize JSON plaintext
            JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(plaintext.toString());
            String jsonCanonicalizedPlaintext = jsonCanonicalizer.getEncodedString();
            Log.v(TAG, mResources.getString(R.string.message_jws_tbs_canonicalized) + jsonCanonicalizedPlaintext);

            // Sign the plaintext
            signature.update(jsonCanonicalizedPlaintext.getBytes(StandardCharsets.UTF_8));
            byte[] signed = signature.sign();
            String b64sig = encoder.withoutPadding().encodeToString(signed);
            Log.v(TAG, mResources.getString(R.string.message_jws_java_signature_base64url) + b64sig);

            // Get the R and S values from JCE signature and assemble into JWS signature
            ASN1Sequence sigSeq = ASN1Sequence.getInstance(signed);
            byte[] jsonSig = org.spongycastle.util.Arrays.concatenate(
                    BigIntegers.asUnsignedByteArray(32, ASN1Integer.getInstance(sigSeq.getObjectAt(0)).getValue()),
                    BigIntegers.asUnsignedByteArray(32, ASN1Integer.getInstance(sigSeq.getObjectAt(1)).getValue()));
            String b64jsonSig = encoder.withoutPadding().encodeToString(jsonSig);
            Log.v(TAG, mResources.getString(R.string.message_jws_signature) + ASN1Dump.dumpAsString(sigSeq));
            Log.v(TAG, mResources.getString(R.string.message_jws_signature_base64url) + b64jsonSig);

            // Finally, the JWS
            jws = new JSONObject()
                    .put(Constants.JWS_ATTRIBUTE_PAYLOAD, plaintext.getString(Constants.JWS_ATTRIBUTE_PAYLOAD))
                    .put(Constants.JWS_ATTRIBUTE_PROTECTED, headerjson)
                    .put(Constants.JWS_ATTRIBUTE_SIGNATURE, b64jsonSig);
            Log.i(TAG, mResources.getString(R.string.message_jws_json_web_signature) + jws);

            timeout = Common.nowms();
            Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
            return jws;
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException
                | UnrecoverableEntryException | InvalidKeyException | JSONException |
                SignatureException e) {
            e.printStackTrace();
        }
        // For compiler
        return null;
    }


    /************************************************************************************
     *                           d8b  .d888           888888 888       888  .d8888b.
     *                           Y8P d88P"              "88b 888   o   888 d88P  Y88b
     *                               888                 888 888  d8b  888 Y88b.
     * 888  888  .d88b.  888d888 888 888888 888  888     888 888 d888b 888  "Y888b.
     * 888  888 d8P  Y8b 888P"   888 888    888  888     888 888d88888b888     "Y88b.
     * Y88  88P 88888888 888     888 888    888  888     888 88888P Y88888       "888
     *  Y8bd8P  Y8b.     888     888 888    Y88b 888     88P 8888P   Y8888 Y88b  d88P
     *   Y88P    "Y8888  888     888 888     "Y88888     888 888P     Y888  "Y8888P"
     *                                           888   .d88P
     *                                      Y8b d88P .d88P"
     *                                       "Y88P" 888P"
     ************************************************************************************
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID of the user performing the signing operation
     * @param ciphertext JSONObject containing the signed JSON with its associated X509 digital
     * certificate and signature object. It is important to recognize that the plaintext in the
     * JWS will undergo JSON Canonicalization (RFC-8785) before the signature is verified. This
     * implies that verification is performed on a JSON object that will most likely be
     * transformed for RFC-8785 conformance before it undergoes cryptographic processing.
     *
     * @return JSONObject with results of verification
     */
    @Override
    public JSONObject verifyJsonWebSignature(String did, String uid, JSONObject ciphertext) {

        // Entry log
        MTAG = "verifyJsonWebSignature";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        // Local variables
        boolean verified = false;

        try {
            java.util.Base64.Decoder decoder = java.util.Base64.getUrlDecoder();

            // Log input to be verified
            Log.i(TAG, mResources.getString(R.string.message_jws_json_web_signature) + ciphertext);
            Log.v(TAG, "\npayload plaintext: " + new String(decoder.decode(ciphertext.getString("payload")), StandardCharsets.UTF_8));

            // Get public key from certificate
            String pemcert = ciphertext.getJSONObject(Constants.JWS_ATTRIBUTE_PROTECTED).getString(Constants.JWS_ATTRIBUTE_X5C);
            PEMParser parser = new PEMParser(new StringReader(pemcert));
            X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);
            PublicKey publicKey = certificate.getPublicKey();

            // Assemble plaintext from JWS for verification
            JSONObject vplaintext = new JSONObject()
                    .put(Constants.JWS_ATTRIBUTE_PAYLOAD, ciphertext.getString(Constants.JWS_ATTRIBUTE_PAYLOAD))
                    .put(Constants.JWS_ATTRIBUTE_PROTECTED, ciphertext.getJSONObject(Constants.JWS_ATTRIBUTE_PROTECTED));
            Log.v(TAG, mResources.getString(R.string.message_jws_tbv_unencoded) + vplaintext);

            // Use JCS (RFC-8785) to canonicalize JSON plaintext
            JsonCanonicalizer jsonCanonicalizer = new JsonCanonicalizer(vplaintext.toString());
            String jsonCanonicalizedVplaintext = jsonCanonicalizer.getEncodedString();
            Log.v(TAG, mResources.getString(R.string.message_jws_tbs_canonicalized) + jsonCanonicalizedVplaintext);

            // Verify signature
            byte[] rsbytes = decoder.decode(ciphertext.getString(Constants.JWS_ATTRIBUTE_SIGNATURE));
            Signature signature = Signature.getInstance(Constants.FIDO2_SIGNATURE_ALGORITHM);
            signature.initVerify(publicKey);
            signature.update(jsonCanonicalizedVplaintext.getBytes(StandardCharsets.UTF_8));

            // Convert JWS signature (with R and S bytes) into JCE signature encoding and verify
            byte[] ecdsaSig = new DERSequence(new ASN1Encodable[]
                    {new ASN1Integer(new BigInteger(1, org.spongycastle.util.Arrays.copyOfRange(rsbytes, 0, 32))),
                     new ASN1Integer(new BigInteger(1, org.spongycastle.util.Arrays.copyOfRange(rsbytes, 32, 64)))
                }).getEncoded();
            verified = signature.verify(ecdsaSig);
            if (verified) {
                Log.v(TAG, mResources.getString(R.string.message_jws_signature_verified));
            } else {
                Log.v(TAG, mResources.getString(R.string.message_jws_signature_not_verified));
            }

            // Get certificate details
            JSONObject response = new JSONObject()
                    .put(Constants.JWS_RESPONSE_ATTRIBUTE_DN, certificate.getSubjectDN())
                    .put(Constants.JWS_RESPONSE_ATTRIBUTE_SERIAL, certificate.getSerialNumber())
                    .put(Constants.JWS_RESPONSE_ATTRIBUTE_START, certificate.getNotBefore())
                    .put(Constants.JWS_RESPONSE_ATTRIBUTE_END, certificate.getNotAfter())
                    .put(Constants.JWS_RESPONSE_ATTRIBUTE_VERIFIED, verified);
            return response;

        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException |
                SignatureException | JSONException | CertificateException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        // For compiler
        return null;
    }


    /***********************************************************
     *                  d8b                   888
     *                  Y8P                   888
     *                                        888
     * 88888b.  888d888 888 888  888  8888b.  888888  .d88b.
     * 888 "88b 888P"   888 888  888     "88b 888    d8P  Y8b
     * 888  888 888     888 Y88  88P .d888888 888    88888888
     * 888 d88P 888     888  Y8bd8P  888  888 Y88b.  Y8b.
     * 88888P"  888     888   Y88P   "Y888888  "Y888  "Y8888
     * 888
     * 888
     * 888
     ***********************************************************/

    /**
     * Builds the JSON object that provides Android OS information
     */
    @SuppressWarnings("deprecation")
    private static JSONObject getAndroidOsDetails() {
        MTAG = "getAndroidOsDetails";
        JSONObject androidosdetails;
        try {
            androidosdetails = new JSONObject()
                    .put(Constants.ANDROID_OS_DETAILS, new JSONObject()
                            .put(Constants.ANDROID_VERSION_RELEASE, Build.VERSION.RELEASE)
                            .put(Constants.ANDROID_VERSION_INCREMENTAL, Build.VERSION.INCREMENTAL)
                            .put(Constants.ANDROID_VERSION_SDK_NUMBER, Build.VERSION.SDK_INT)
                            .put(Constants.ANDROID_BOARD, Build.BOARD)
                            .put(Constants.ANDROID_BOOTLOADER, Build.BOOTLOADER)
                            .put(Constants.ANDROID_BRAND, Build.BRAND)
                            .put(Constants.ANDROID_CPU_ABI, Build.CPU_ABI)
                            .put(Constants.ANDROID_CPU_ABI2, Build.CPU_ABI2)
                            .put(Constants.ANDROID_DEVICE, Build.DEVICE)
                            .put(Constants.ANDROID_DISPLAY, Build.DISPLAY)
                            .put(Constants.ANDROID_FINGERPRINT, Build.FINGERPRINT)
                            .put(Constants.ANDROID_HOST, Build.HARDWARE)
                            .put(Constants.ANDROID_HOST, Build.HOST)
                            .put(Constants.ANDROID_ID, Build.ID)
                            .put(Constants.ANDROID_MANUFACTURER, Build.MANUFACTURER)
                            .put(Constants.ANDROID_MODEL, Build.MODEL)
                            .put(Constants.ANDROID_PRODUCT, Build.PRODUCT)
                            .put(Constants.ANDROID_SERIAL, Build.SERIAL)
                            .put(Constants.ANDROID_SUPPORTED_32BIT_ABI, Arrays.toString(Build.SUPPORTED_32_BIT_ABIS))
                            .put(Constants.ANDROID_SUPPORTED_64BIT_ABI, Arrays.toString(Build.SUPPORTED_64_BIT_ABIS))
                            .put(Constants.ANDROID_TAGS, Build.TAGS)
                            .put(Constants.ANDROID_TIME, Build.TIME)
                            .put(Constants.ANDROID_TYPE, Build.TYPE)
                            .put(Constants.ANDROID_USER, Build.USER));
            Log.v(MTAG, androidosdetails.toString());
        } catch (JSONException e) {
            return null;
        }
        return androidosdetails;
    }
}
