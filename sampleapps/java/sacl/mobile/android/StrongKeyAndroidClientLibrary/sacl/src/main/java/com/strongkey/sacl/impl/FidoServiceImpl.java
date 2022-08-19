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
 * The implementation of the FidoService interface representing
 * webservice operations sent to a business application that uses
 * StrongKey's FIDO server. Please see documentation of these
 * methods in the interface file.
 */

package com.strongkey.sacl.impl;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.asynctasks.FidoUserAgentAttestedDeviceAuthenticateTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentAttestedDevicePreauthenticateTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentAttestedDevicePreregisterTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentAttestedDeviceRegisterTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentAuthenticateTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentAuthorizeTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentPreauthenticateTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentPreauthorizeTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentPreregisterTask;
import com.strongkey.sacl.asynctasks.FidoUserAgentRegisterTask;
import com.strongkey.sacl.interfaces.FidoService;
import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.AuthorizationSignature;
import com.strongkey.sacl.roomdb.PreauthenticateChallenge;
import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sacl.roomdb.PreregisterChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.LocalContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.Signature;

public class FidoServiceImpl implements FidoService {

    // TAGs for logging
    private final String TAG = FidoServiceImpl.class.getSimpleName();
    private String MTAG = null;

    // Private objects
    private LocalContextWrapper mLocalContextWrapper;
    private Resources mResources;


    // Constructor with Context
    public FidoServiceImpl(Context context) {
        mLocalContextWrapper = new LocalContextWrapper(context);
        mResources = mLocalContextWrapper.getResources();
    }

    /**
     * Gets a FIDO challenge for the user. A successful response will return a PreregisterChallenge
     * object with necessary information to generate a new key-pair and register the public-key
     * with an AndroidKeystore Key attestation.
     *
     * @param did int value of the cryptographic domain used by this application
     * @param uid long value of the user's unique id within the SFA
     * @return Object containing either a PreregisterChallenge or JSONError
     */
    private Object getFidoRegistrationChallenge(int did, long uid) {
        MTAG = "getFidoRegistrationChallenge";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        FidoUserAgentPreregisterTask fidoUserAgentPreregisterTask =
                new FidoUserAgentPreregisterTask(mLocalContextWrapper, did, uid);
        try {
            if (fidoUserAgentPreregisterTask == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                Log.e(TAG + ":" + MTAG, mResources.getString(R.string.fido_error_task_instantiation));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_task_instantiation));
            }

            // Call the async task
            Object object = fidoUserAgentPreregisterTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_null_challenge));
            } else {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (PreregisterChallenge) object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a FIDO public key with the StrongKey FIDO server as part of
     * the FIDO Registration process.
     *
     * @param did integer value representing the cryptographic domain ID to
     *            which this webservice is directed (see https://strongkey.com/resources
     *            for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the SFA eCommerce app
     *            making this service request. Since the SFA manages the registration
     *            process and relays the service request to the FIDO server, it knows what
     *            to send to the FIDO server
     * @return Object A Java object that is either a PublicKeyCredential, which
     * represents the newly minted key-pair within AndroidKeystore, along with
     * metadata about the transaction, or a JSONError indicating a problem.
     * The private key of the key-pair is always stored within AndroidKeystore,
     * protected either by the Trusted Execution Environment (TEE) or a
     * hardware security module aka Secure Element (SE)
     */
    @Override
    public Object registerFidoKey(int did, long uid) {
        MTAG = "registerFidoKey";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        try {
            // Get a PreregisterChallenge
            Object object  = getFidoRegistrationChallenge(did, uid);
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_registration));
            } else {
                PreregisterChallenge preregisterChallenge = (PreregisterChallenge) object;
                Log.d(TAG, preregisterChallenge.toString());
            }

            // Instantiate a registration task
            FidoUserAgentRegisterTask fidoUserAgentRegisterTask =
                    new FidoUserAgentRegisterTask(mLocalContextWrapper, did, uid);

            // Call the async task
            object = fidoUserAgentRegisterTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_registration));
            } else {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (PublicKeyCredential) object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a FIDO challenge for the user. A successful response will return a PreregisterChallenge
     * object with necessary information to generate a new key-pair and register the public-key
     * with an AndroidKeystore Key attestation. This method MUST be called by registerAttestedDeviceFidoKey as a
     * precursor to registering the new FIDO key
     *
     * @param did int value of the cryptographic domain used by this application
     * @param uid long value of the user's unique id within the MDBA
     * @param devid long value of the user's unique device id within the MDBA
     * @param rdid long value of the registered mobile device within the MDKMS
     * @return Object containing either a PreregisterChallenge or JSONError
     */
    private Object getAttestedDeviceFidoRegistrationChallenge(int did, long uid, long devid, long rdid) {
        MTAG = "getAttestedDeviceFidoRegistrationChallenge";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        FidoUserAgentAttestedDevicePreregisterTask fidoUserAgentAttestedDevicePreregisterTask =
                new FidoUserAgentAttestedDevicePreregisterTask(mLocalContextWrapper, did, uid, devid, rdid);
        try {
            if (fidoUserAgentAttestedDevicePreregisterTask == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                Log.e(TAG + ":" + MTAG, mResources.getString(R.string.fido_error_task_instantiation));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_task_instantiation));
            }

            // Call the async task
            Object object = fidoUserAgentAttestedDevicePreregisterTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_null_challenge));
            } else {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (PreregisterChallenge) object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Registers a FIDO key for the user. A successful response will return a PublicKeyCredential
     * object with metadata about the new key-pair - the private key is protected within the
     * AndroidKeystore
     *
     * @param did int value of the cryptographic domain used by this application
     * @param uid long value of the user's unique id within the MDBA
     * @param devid long value of the user's unique device id within the MDBA
     * @param rdid long value of the registered mobile device within the MDKMS
     * @return Object containing either a PublicKeyCredential or JSONError
     */
    @Override
    public Object registerAttestedDeviceFidoKey(int did, long uid, long devid, long rdid) {
        MTAG = "registerAttestedDeviceFidoKey";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        try {
            // Get a PreregisterChallenge
            Object object  = getAttestedDeviceFidoRegistrationChallenge(did, uid, devid, rdid);
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_registration));
            } else {
                PreregisterChallenge preregisterChallenge = (PreregisterChallenge) object;
                Log.d(TAG, preregisterChallenge.toString());
            }

            // Instantiate a registration task
            FidoUserAgentAttestedDeviceRegisterTask fidoUserAgentAttestedDeviceRegisterTask =
                    new FidoUserAgentAttestedDeviceRegisterTask(mLocalContextWrapper, did, uid, devid, rdid);

            // Call the async task
            object = fidoUserAgentAttestedDeviceRegisterTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_registration));
            } else {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (PublicKeyCredential) object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a FIDO challenge for the user. A successful response will return a
     * PreauthenticateChallenge object with necessary information to digitally sign the challenge
     * with a previously registered AndroidKeystore-based FIDO key.
     *
     * @param did int value of the cryptographic domain used by this application
     * @param uid long value of the user's unique id within the SFA
     * @return Object containing either a PreauthenticateChallenge or JSONError
     */
    private Object getFidoAuthenticationChallenge(int did, long uid) {
        MTAG = "getFidoAuthenticationChallenge";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        FidoUserAgentPreauthenticateTask fidoUserAgentPreauthenticateTask =
                new FidoUserAgentPreauthenticateTask(mLocalContextWrapper, did, uid);
        try {
            if (fidoUserAgentPreauthenticateTask == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                Log.e(TAG + ":" + MTAG, mResources.getString(R.string.fido_error_task_instantiation));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_task_instantiation));
            }

            // Call the async task
            Object object = fidoUserAgentPreauthenticateTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authentication));
            } else if (object instanceof PreauthenticateChallenge) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (PreauthenticateChallenge) object;
            } else {
                Log.e(TAG, "Received some error: " + object);
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Authenticates with a FIDO key. A successful response will return a AuthenticationSignature
     * object with metadata about the digital signature
     *
     * @param did int value of the cryptographic domain used by this application
     * @param uid long value of the user's unique id within the SFA
     * @return Object containing either a AuthenticationSignature or JSONError
     */
    @Override
    public Object authenticateFidoKey(int did, long uid) {
        MTAG = "authenticateFidoKey";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        try {
            // Get a PreauthenticateChallenge
            Object object  = getFidoAuthenticationChallenge(did, uid);
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authentication));
            } else {
                PreauthenticateChallenge preauthenticateChallenge = (PreauthenticateChallenge) object;
            }

            // Instantiate an authentication task
            FidoUserAgentAuthenticateTask fidoUserAgentAuthenticateTask =
                    new FidoUserAgentAuthenticateTask(mLocalContextWrapper, did, uid);

            // Call the async task
            object = fidoUserAgentAuthenticateTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authentication));
            } else if (object instanceof AuthenticationSignature) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (AuthenticationSignature) object;
            } else {
                Log.d(TAG +"-"+ MTAG, "Got String error: " + object);
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a FIDO challenge for the user. A successful response will return a
     * PreauthenticateChallenge object with necessary information to digitally sign the challenge
     * with a previously regisntered AndroidKeystore-based FIDO key. This method will be called by
     * authenticateAttestedDeviceFidoKey as a precursor to authenticating with the new FIDO key
     *
     * @param did int value of the cryptographic domain used by this application
     * @param uid long value of the user's unique id within the MDBA
     * @param devid long value of the user's unique device id within the MDBA
     * @param rdid long value of the registered mobile device within the MDKMS
     * @return Object containing either a PreauthenticateChallenge or JSONError
     */
    private Object getAttestedDeviceFidoAuthenticationChallenge(int did, long uid, long devid, long rdid) {
        MTAG = "getAttestedDeviceFidoAuthenticationChallenge";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        FidoUserAgentAttestedDevicePreauthenticateTask fidoUserAgentAttestedDevicePreauthenticateTask =
                new FidoUserAgentAttestedDevicePreauthenticateTask(mLocalContextWrapper, did, uid, devid, rdid);
        try {
            if (fidoUserAgentAttestedDevicePreauthenticateTask == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                Log.e(TAG + ":" + MTAG, mResources.getString(R.string.fido_error_task_instantiation));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_task_instantiation));
            }

            // Call the async task
            Object object = fidoUserAgentAttestedDevicePreauthenticateTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authentication));
            } else if (object instanceof PreauthenticateChallenge) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (PreauthenticateChallenge) object;
            } else {
                Log.e(TAG, "Received some error: " + object);
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Authenticates with a FIDO key. A successful response will return a AuthenticationSignature
     * object with metadata about the digital signature
     *
     * @param did int value of the cryptographic domain used by this application
     * @param uid long value of the user's unique id within the MDBA
     * @param devid long value of the user's unique device id within the MDBA
     * @param rdid long value of the registered mobile device within the MDKMS
     * @return Object containing either a AuthenticationSignature or JSONError
     */
    @Override
    public Object authenticateAttestedDeviceFidoKey(int did, long uid, long devid, long rdid) {
        MTAG = "authenticateAttestedDeviceFidoKey";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        try {
            // Get a PreauthenticateChallenge
            Object object  = getAttestedDeviceFidoAuthenticationChallenge(did, uid, devid, rdid);
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authentication));
            } else {
                PreauthenticateChallenge preauthenticateChallenge = (PreauthenticateChallenge) object;
            }

            // Instantiate an authentication task
            FidoUserAgentAttestedDeviceAuthenticateTask fidoUserAgentAttestedDeviceAuthenticateTask =
                    new FidoUserAgentAttestedDeviceAuthenticateTask(mLocalContextWrapper, did, uid, devid, rdid);

            // Call the async task
            object = fidoUserAgentAttestedDeviceAuthenticateTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authentication));
            } else if (object instanceof AuthenticationSignature) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (AuthenticationSignature) object;
            } else {
                Log.d(TAG +"-"+ MTAG, "Got String error: " + object);
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Gets a challenge to initiate a FIDO Transaction Confirmation process. It presupposes
     * that the user has a FIDO key registered with the FIDO server for this to work. This
     * is the only pre-* methods that requires a payload parameter (the cart) since it needs
     * to pass on elements of the transaction to the FIDO server to derive a challenge; this
     * is what ties the transaction to the FIDO digital signature - the "dynamic linking"
     * for Strong Customer Authentication (SCA) in PSD2.
     *
     * @param did  integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid  long value representing the User ID within the MDBA that is
     * making this service request. Since the MDBA manages the authorization
     * process and relays the service request to the FIDO server, it knows what
     * to send to the FIDO server
     * @param cart String with a Base64Url encoded JSONObject containing elements
     * of the transaction for which the RP desires a transaction confirmation. The
     * sample cart has a collection of products and the payment method selected by
     * the user
     * @return An Object that is either a PreauthorizeChallenge that represents
     * the FIDO challenge provided by the SKFS's preauthorize() webservice, or
     * a JSONObject with an error message
     */
    @Override
    public Object getFidoAuthorizationChallenge(int did, long uid, String cart) {
        MTAG = "getFidoAuthorizationChallenge";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);


        FidoUserAgentPreauthorizeTask fidoUserAgentPreauthorizeTask =
                new FidoUserAgentPreauthorizeTask(mLocalContextWrapper, did, uid, Common.urlEncode(cart));
        try {
            if (fidoUserAgentPreauthorizeTask == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                Log.e(TAG + ":" + MTAG, mResources.getString(R.string.fido_error_task_instantiation));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_task_instantiation));
            }

            // Call the async task
            Object object = fidoUserAgentPreauthorizeTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authentication));
            } else if (object instanceof PreauthorizeChallenge) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (PreauthorizeChallenge) object;
            } else {
                Log.e(TAG, "Received some error: " + object);
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }

    /**
     * Authorizes a business transaction using the FIDO key. The RP may use
     * the transaction data and digital signature for a variety of use-cases:
     * compliance with the EU PSD2 for SCA, conform to the EMVCo 3DS2 protocol
     * or other private transaction protocol that leverages FIDO
     *
     * @param did integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the MDBA making
     * this service request. Since the MDBA manages the authentication process
     * and relays the service request to the FIDO server, it knows what to send
     * to the FIDO server
     * @param txid String containing a locally generated transaction ID. This
     * should be replaced with a TXID generated by the business application
     * @param txplayload String containing a JSONObject with products and payment
     * information
     * @param credentialId String containing unique key identifier in keystore
     * @param challenge String with the FIDO challenge to sign
     * @param signature JCE Signature object initialized with the user's FIDO key
     * to generate the digital signature over the transaction
     *
     * @return Object A Java object, AuthorizationSignature, that represents the
     * digital signature, along with metadata about the challenge and the
     * transaction, or a JSONError indicating a problem.
     */
    @Override
    public Object authorizeFidoTransaction(int did, long uid, String txid, String txplayload,
                                    String credentialId, String challenge, Signature signature) {
        MTAG = "authorizeFidoTransaction";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        try {
            // Instantiate an authorization task
            FidoUserAgentAuthorizeTask fidoUserAgentAuthorizeTask =
                    new FidoUserAgentAuthorizeTask(mLocalContextWrapper, did, uid, txid,
                            txplayload, credentialId, challenge, signature);

            // Call the async task
            Object object = fidoUserAgentAuthorizeTask.call();
            if (object == null) {
                timeout = Common.nowms();
                Log.w(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.JsonError(TAG, MTAG, "error", mResources.getString(R.string.fido_error_authorization));
            } else if (object instanceof AuthorizationSignature) {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (AuthorizationSignature) object;
            } else if (object instanceof JSONObject) {
                Log.v(TAG +"-"+ MTAG, "Got JSONObject: " + object);
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return (JSONObject) object;
            } else {
                Log.v(TAG +"-"+ MTAG, "Got String error: " + object);
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return object;
            }
        } catch (JSONException je) {
            // Hopefully we don't get here
            je.printStackTrace();
        }
        return null;
    }
}
