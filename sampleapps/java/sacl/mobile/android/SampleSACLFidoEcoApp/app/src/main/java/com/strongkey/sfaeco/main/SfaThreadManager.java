/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, as published by the Free
 * Software Foundation and available at
 * http://www.fsf.org/licensing/licenses/lgpl.html, version 2.1.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (d/b/a StrongKey)
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
 * Thread manager for SFA to handle long-running tasks in the background.
 */

package com.strongkey.sfaeco.main;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.strongkey.sacl.impl.FidoAuthenticatorImpl;
import com.strongkey.sacl.interfaces.FidoAuthenticator;
import com.strongkey.sacl.roomdb.AuthorizationSignature;
import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sfaeco.interfaces.SfaEcoService;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;
import com.strongkey.sfaeco.webservices.SfaEcoServiceImpl;
import com.strongkey.sacl.impl.FidoServiceImpl;
import com.strongkey.sacl.interfaces.FidoService;
import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.ref.WeakReference;
import java.security.Signature;

public class SfaThreadManager extends Thread {

    private static final String TAG = SfaThreadManager.class.getSimpleName();
    private static String MTAG = null;

    private static WeakReference<Handler> sfaMainHandlerRef;
    private SfaEcoService sfaEcoService;
    private FidoService fidoService;

    private final String PARAMETER_MESSAGE = ": Task Parameters: ";
    private final String HANDLER_ERROR = "ERROR: mainHandler is NULL";
    private final String UNKNOWN_TASK = ": Received unknown task: ";
    private final String RECEIVED_TASK = ": Received task: ";

    // TODO: Get this from Settings
    private String mDid = "1";
    private int mDidInt = 1;

    /**
     * Need this constructor so the MainActivity can pass the Handler it
     * created to this child thread for sending messages to Main.
     *
     * @param handler Handler created by MainActivity
     */
    public SfaThreadManager(Handler handler, Context context) {
        sfaMainHandlerRef = new WeakReference<>(handler);
        this.sfaEcoService = new SfaEcoServiceImpl(context);
        this.fidoService = new FidoServiceImpl(context);
    }

    /******************************
     * 888d888 888  888 88888b.
     * 888P"   888  888 888 "88b
     * 888     888  888 888  888
     * 888     Y88b 888 888  888
     * 888      "Y88888 888  888
     *****************************/

    @SuppressLint("HandlerLeak")
    @Override
    public void run() {

        // Entry log
        MTAG = "run";
        long timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        // Make sure thread is running in the background
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_BACKGROUND);

        this.setName("SfaThreadManager");
        Looper.prepare();

        final Handler mainHandler = sfaMainHandlerRef.get();
        MainActivity.mSfaChildHandler = new Handler() {

            public void handleMessage(Message msg) {
                try {
                    MTAG = "mSfaChildHandler";

                    if (mainHandler != null) {

                        Message mainMessage = mainHandler.obtainMessage();
                        Bundle msgBundle = msg.getData();
                        String task = msgBundle.getString(SfaConstants.SFA_TASK);
                        Log.i(TAG, MTAG + RECEIVED_TASK + task);

                        // Response container object
                        JSONObject mJsonObject;

                        switch (SfaConstants.SFA_TASKS.valueOf(task)) {
                            case SFA_TASK_AUTHENTICATE_FIDO_KEY:
                                mDidInt = msgBundle.getInt(SfaConstants.JSON_KEY_DID);
                                Long mUidLong = msgBundle.getLong(SfaConstants.JSON_KEY_UID);
                                String mUsername = msgBundle.getString(SfaConstants.JSON_KEY_USER_USERNAME);
                                Log.i(TAG, MTAG + PARAMETER_MESSAGE + "[" + mUsername + "]");
                                Log.d(TAG, MTAG + PARAMETER_MESSAGE + "[" +
                                        mDidInt+ '\n' +
                                        mUidLong+ '\n' +
                                        mUsername + "]");
                                Object object = fidoService.authenticateFidoKey(mDidInt, mUidLong);
                                if (object instanceof AuthenticationSignature) {
                                    AuthenticationSignature authenticationSignature = (AuthenticationSignature) object;
                                    try {
                                        sendMessageToMain(SfaConstants.SFA_TASK_AUTHENTICATE_FIDO_KEY_RESPONSE_OBJECT, authenticationSignature.toJSON().toString());
                                    } catch (JSONException je) {
                                        sendMessageToMain(SfaConstants.SFA_TASK_AUTHENTICATE_FIDO_KEY_RESPONSE_OBJECT, authenticationSignature.toString());
                                    }
                                } else if (object instanceof JSONObject) {
                                    JSONObject jsonObject = (JSONObject) object;
                                    sendMessageToMain(SfaConstants.SFA_TASK_AUTHENTICATE_FIDO_KEY_RESPONSE_OBJECT, jsonObject.toString());
                                }
                                break;
                            case SFA_TASK_GET_AUTHORIZATION_CHALLENGE:
                                mDidInt = msgBundle.getInt(SfaConstants.JSON_KEY_DID);
                                mUidLong = msgBundle.getLong(SfaConstants.JSON_KEY_UID);
                                String mCart = msgBundle.getString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_CART);
                                Log.i(TAG, MTAG + PARAMETER_MESSAGE + "[" + mUidLong + "]");
                                Log.d(TAG, MTAG + PARAMETER_MESSAGE + "[" +
                                        mDidInt+ '\n' +
                                        mUidLong+ '\n' +
                                        mCart+ '\n' + "]");
                                object = fidoService.getFidoAuthorizationChallenge(mDidInt, mUidLong, mCart);
                                /*
                                 * Because we need to bring the challenge all the way back into the UX, we have
                                 * to populate the DID and UID into the PreauthorizeChallenge JSON response so
                                 * it does not get rejected when being stored in RoomDB (all other FIDO
                                 * webservices complete the pre-function and function before returning to the UX)
                                 */
                                try {
                                    JSONObject azChallenge = new JSONObject();
                                    if (object instanceof PreauthorizeChallenge) {
                                        PreauthorizeChallenge preauthorizeChallenge = (PreauthorizeChallenge) object;
                                        azChallenge.put("did", mDidInt)
                                                   .put("uid", mUidLong)
                                                   .put("Response", preauthorizeChallenge.toJSON());
                                        sendMessageToMain(SfaConstants.SFA_TASK_GET_AUTHORIZATION_CHALLENGE_RESPONSE_OBJECT, azChallenge.toString());
                                    } else if (object instanceof JSONObject) {
                                        azChallenge.put("did", mDidInt)
                                                   .put("uid", mUidLong)
                                                   .put("Response", (JSONObject) object);
                                        sendMessageToMain(SfaConstants.SFA_TASK_GET_AUTHORIZATION_CHALLENGE_RESPONSE_OBJECT, azChallenge.toString());
                                    }
                                } catch (JSONException je) {
                                    je.printStackTrace();
                                }
                                break;
                            case SFA_TASK_AUTHORIZE_FIDO_TRANSACTION:
                                mDidInt = msgBundle.getInt(SfaConstants.JSON_KEY_DID);
                                mUidLong = msgBundle.getLong(SfaConstants.JSON_KEY_UID);
                                mUsername = msgBundle.getString(SfaConstants.JSON_KEY_USER_USERNAME);
                                String mTxid = msgBundle.getString(SfaConstants.JSON_KEY_TXID);
                                String mTxPayload = msgBundle.getString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXPAYLOAD);
                                String mSigObjectLabel = msgBundle.getString(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_SIGNATURE_OBJECT_LABEL);
                                String mCredentialId = msgBundle.getString(SfaConstants.JSON_KEY_USER_CREDENTIALID);
                                String mChallenge = msgBundle.getString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_CHALLENGE);
                                Log.i(TAG, MTAG + PARAMETER_MESSAGE + "[" + mUsername + "]");
                                Log.d(TAG, MTAG + PARAMETER_MESSAGE + "[" +
                                        mDidInt+ '\n' +
                                        mUidLong+ '\n' +
                                        mUsername+ '\n' +
                                        mTxid+ '\n' +
                                        mTxPayload+ '\n' +
                                        mCredentialId + '\n' +
                                        mChallenge + '\n' +
                                        mSigObjectLabel + "]");
                                Signature signatureObject = Common.getSfaSharedDataModel().getCurrentSignatureObject();
                                object = fidoService.authorizeFidoTransaction(mDidInt, mUidLong, mTxid, mTxPayload, mCredentialId, mChallenge, signatureObject);
                                if (object instanceof AuthorizationSignature) {
                                    AuthorizationSignature authorizationSignature = (AuthorizationSignature) object;
                                    try {
                                        sendMessageToMain(SfaConstants.SFA_TASK_AUTHORIZE_FIDO_TRANSACTION_RESPONSE_OBJECT, authorizationSignature.toJSON().toString());
                                    } catch (JSONException je) {
                                        sendMessageToMain(SfaConstants.SFA_TASK_AUTHORIZE_FIDO_TRANSACTION_RESPONSE_OBJECT, authorizationSignature.toString());
                                    }
                                } else if (object instanceof JSONObject) {
                                    JSONObject jsonObject = (JSONObject) object;
                                    sendMessageToMain(SfaConstants.SFA_TASK_AUTHORIZE_FIDO_TRANSACTION_RESPONSE_OBJECT, jsonObject.toString());
                                }
                                break;
                            case SFA_TASK_CHECK_USERNAME:
                                break;
                            case SFA_TASK_DELETE_KEY:
                                break;
                            case SFA_TASK_DISPLAY_USERDATA:
                                break;
                            case SFA_TASK_LIST_KEYS:
                                break;
                            case SFA_TASK_REGISTER_FIDO_KEY:
                                mDidInt = msgBundle.getInt(SfaConstants.JSON_KEY_DID);
                                mUidLong = msgBundle.getLong(SfaConstants.JSON_KEY_UID);
                                mUsername = msgBundle.getString(SfaConstants.JSON_KEY_USER_USERNAME);
                                Log.i(TAG, MTAG + PARAMETER_MESSAGE + "[" + mUsername + "]");
                                Log.d(TAG, MTAG + PARAMETER_MESSAGE + "[" +
                                        mDidInt+ '\n' +
                                        mUidLong+ '\n' +
                                        mUsername + "]");
                                object = fidoService.registerFidoKey(mDidInt, mUidLong);
                                if (object instanceof PublicKeyCredential) {
                                    PublicKeyCredential publicKeyCredential = (PublicKeyCredential) object;
                                    try {
                                        sendMessageToMain(SfaConstants.SFA_TASK_REGISTER_FIDO_KEY_RESPONSE_OBJECT, publicKeyCredential.toJSON().toString());
                                    } catch (JSONException je) {
                                        sendMessageToMain(SfaConstants.SFA_TASK_REGISTER_FIDO_KEY_RESPONSE_OBJECT, publicKeyCredential.toString());
                                    }
                                } else if (object instanceof JSONObject) {
                                    JSONObject jsonObject = (JSONObject) object;
                                    sendMessageToMain(SfaConstants.SFA_TASK_REGISTER_FIDO_KEY_RESPONSE_OBJECT, jsonObject.toString());
                                }
                                break;
                            case SFA_TASK_REGISTER_USER:
                                String mEmail = msgBundle.getString(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS);
                                String mFamilyName = msgBundle.getString(SfaConstants.JSON_KEY_USER_FAMILY_NAME);
                                String mGivenName = msgBundle.getString(SfaConstants.JSON_KEY_USER_GIVEN_NAME);
                                String mPersonalNumber = msgBundle.getString(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER);
                                mUsername = msgBundle.getString(SfaConstants.JSON_KEY_USER_USERNAME);
                                Log.i(TAG, MTAG + PARAMETER_MESSAGE + "[" + mUsername + "]");
                                Log.d(TAG, MTAG + PARAMETER_MESSAGE + "[" +
                                        mUsername + '\n' +
                                        mGivenName+ '\n' +
                                        mFamilyName+ '\n' +
                                        mEmail+ '\n' +
                                        mPersonalNumber + "]");
                                mJsonObject = sfaEcoService.registerUser(
                                        mDid, mUsername, mGivenName, mFamilyName, mEmail, mPersonalNumber);
                                sendMessageToMain(SfaConstants.SFA_TASK_REGISTER_USER_RESPONSE_JSON, mJsonObject.toString());
                                break;
                            case SFA_TASK_SIGN:
                                break;
                            default:
                                Log.i(TAG, MTAG + UNKNOWN_TASK + task);
                        }
                    } else {
                        long timeout = Common.nowms();
                        Log.i(TAG, MTAG + "-OUT-" + timein + "-" + timeout + "|TTC=" + (timeout - timein));
                        Log.i(TAG, MTAG + HANDLER_ERROR);
                    }
                    long timeout = Common.nowms();
                    Log.i(TAG, MTAG + "-OUT-" + timein + "-" + timeout + "|TTC=" + (timeout - timein));
                    sleep(500);
                } catch (InterruptedException e) {
                    long timeout = Common.nowms();
                    Log.i(TAG, MTAG + "-OUT-" + timein + "-" + timeout + "|TTC=" + (timeout - timein));
                    return;
                }
            }
        };
        Looper.loop();
    }

    /**
     * Sends messages to the UI Thread from this child thread
     *
     * @param key     String with the key of the response of the key-value pair in the Bundle
     * @param message String with the value of the response of the key-value pair in the Bundle
     */
    private void sendMessageToMain(String key, String message) {
        Message msg = sfaMainHandlerRef.get().obtainMessage();
        Bundle responseBundle = new Bundle();
        responseBundle.putString(key, message);
        msg.setData(responseBundle);
        sfaMainHandlerRef.get().sendMessage(msg);
    }
}
