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
 * Copyright (c) 2001-2022 StrongAuth, Inc. (d/b/a StrongKey)
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
 * Asynchronous task to authenticate to the FIDO server through the SACL.
 */

package com.strongkey.sacl.asynctasks;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.crypto.AuthenticatorGetAssertion;
import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.PreauthenticateChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sacl.webservices.CallWebservice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Callable;

public class FidoUserAgentAuthenticateTask implements Callable {

    private final String TAG = FidoUserAgentAuthenticateTask.class.getSimpleName();

    private SaclRepository saclRepository;
    private PublicKeyCredential publicKeyCredential;
    private LocalContextWrapper context;
    private Long uid;
    private int did;

    /**
     * Constructor
     *
     * @param context     Application Context
     * @param did         int value of the cryptographic domain ID
     * @param uid         Long value of the user ID
     */
    public FidoUserAgentAuthenticateTask(Context context,
                                         int did,
                                         Long uid) {
        this.context = new LocalContextWrapper(context);
        this.did = did;
        this.uid = uid;
    }

    @Override
    public Object call() throws JSONException {

        // Get SACL Repository
        saclRepository = Common.getRepository(context);

        // Get webservice origin
        String webserviceOrigin = context.getResources().getString(R.string.sacl_service_hostport);
        String rfcOrigin = Common.getRfc6454Origin(webserviceOrigin);
        String actualRpid = Common.getTldPlusOne(rfcOrigin);

        // Find the PreauthenticateChallenge in SaclSharedDataModel
        PreauthenticateChallenge preauthenticateChallenge = (PreauthenticateChallenge)
                Common.getCurrentObject(Constants.SACL_OBJECT_TYPES.PREAUTHENTICATE_CHALLENGE);
        if (preauthenticateChallenge == null) {
            Log.w(TAG, context.getResources().getString(R.string.message_challenge_error));
            try {
                return Common.JsonError(TAG, "call", "error", "Missing challenge");
            } catch (JSONException ex) {}
            // Hopefully, we do not get to this
            return null;
        }

        // Verify the challenge has not been consumed
        if (preauthenticateChallenge.isChallengeConsumed()) {
            Log.v(TAG, "FIDO Challenge HAS BEEN consumed: " + preauthenticateChallenge.toString());
            return "FIDO Challenge HAS BEEN consumed: " + preauthenticateChallenge.toString();
        }

        // Verify that the rpid in the PreauthenticateChallenge matches actualRpid
        String pacRpid = preauthenticateChallenge.getRpid();
        if (!pacRpid.equalsIgnoreCase(actualRpid)) {
            Log.w(TAG, "PreauthenticateChallenge RPID does NOT match webservice origin: " + pacRpid + " [" + actualRpid + "]");
            return "PreauthenticateChallenge RPID does NOT match webservice origin: " + pacRpid + " [" + actualRpid + "]";
        }

        // If the PublicKeyCredential exists in SaclSharedDataModel, get it
        PublicKeyCredential publicKeyCredential = (PublicKeyCredential)
                Common.getCurrentObject(Constants.SACL_OBJECT_TYPES.PUBLIC_KEY_CREDENTIAL);

        // If object is null, get PublicKeyCredential from DB
        boolean found = false;
        if (publicKeyCredential == null) {
            JSONArray allowedCredentials = preauthenticateChallenge.getAllowCredentialsJSONArray();
            int size = allowedCredentials.length();
            for (int i = 0; i < size; i++) {
                JSONObject credential = allowedCredentials.getJSONObject(i);
                String b64CredentialId = credential.getString("id");
                if (b64CredentialId != null) {
                    String credentialId = new String(Common.urlDecode(b64CredentialId), StandardCharsets.UTF_8);
                    publicKeyCredential = saclRepository.getPublicKeyCredentialByRpidCredentialId(did, actualRpid, credentialId);
                    if (publicKeyCredential != null) {
                        found = true;
                        break;
                    }
                }
            }

            // Do we have a PublicKeyCredential?
            if (!found) {
                Log.w(TAG, "PublicKeyCredential does not exist with any credentialId in allowedCredentials: " + allowedCredentials.toString(2));
                return "PublicKeyCredential does not exist with any credentialId in allowedCredentials";
            }
        }

        // Verify RPID in PublicKeyCredential matches rpid in challenge object
        if (!pacRpid.equalsIgnoreCase(publicKeyCredential.getRpid())) {
            Log.v(TAG, "Challenge RPID does not match [PKC]: " + pacRpid + " [" + publicKeyCredential.toString() + "]");
            return "Challenge RPID does not match [PKC]: " + pacRpid + " [" + publicKeyCredential.toString() + "]";
        }

        // Get updated counter value
        int counter = getAndIncrementCounter(saclRepository, publicKeyCredential);

        // Generate the digital signature
        Object object = AuthenticatorGetAssertion.execute(context, preauthenticateChallenge,
                publicKeyCredential, counter, webserviceOrigin);
        if (object instanceof JSONObject) {
            JSONObject error = (JSONObject) object;
            String errorMsg = error.getJSONObject("error").toString(2);
            Log.w(TAG, errorMsg);
            return "Error: " + errorMsg;
        }

        // Not an error
        if (object instanceof AuthenticationSignature) {
            AuthenticationSignature authenticationSignature = (AuthenticationSignature) object;


            // Build input parameters into a JSON object and authenticate to webservice
            if (authenticationSignature != null) {
                JSONObject input = getAuthenticateParameters(authenticationSignature, publicKeyCredential, webserviceOrigin);
                JSONObject response = CallWebservice.execute(Constants.JSON_KEY_FIDO_SERVICE_OPERATION_AUTHENTICATE_FIDO_KEY,
                        input, context);

                // Did we succeed?
                if (response.has("error")) {
                    return response;
                } else {
                    // Success - save FidoSignature to RoomDB
                    authenticationSignature.setCreateDate(Common.now());
                    int inserted = saclRepository.insert(authenticationSignature);
                    Log.v(TAG, "Save AuthenticationSignature; DB returned: " + inserted);

                    // Return URL and HTTP code to be displayed by onPostExecute method
                    Common.printVeryLongLogMessage("Saved AuthenticationSignature", authenticationSignature.toString());
                    Common.setCurrentObject(Constants.SACL_OBJECT_TYPES.AUTHENTICATION_SIGNATURE, authenticationSignature);
                    return authenticationSignature;
                }
            }
        }
        // For compiler
        return null;
    }

    /**
     * Create the JSON object with input parameters that goes to the webservice. Input
     * parameters to the FIDO server looks like the following (currently v3 API).
     * Note that the strongKeyMetadata sub-JSON should be sent by the app to its business
     * application, which in turn, relays it to the FIDO server - this library should only
     * send the origin in clientDataJSON. The svcinfo sub-JSON is handled by the business
     * application the mobile app connects to - its only shown here for completeness.
     *
     * NOTE: Large value objects are abbreviated for keeping the comments readable without
     * having to have their content going off the edge of the screen.
     *
     * {
     *    "svcinfo": {
     *       "did": 1,
     *       "protocol": "FIDO2_0",
     *       "authtype": "PASSWORD",
     *       "svcusername": "svcfidouser",
     *       "svcpassword": "Abcd1234!"
     *    },
     *    "payload": {
     *       "publicKeyCredential": {
     *          "type": "public-key",
     *          "id": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
     *          "rawId": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
     *          "response": {
     *              "clientDataJSON": "eyJ0eXBlIjoid2...vdC1zdXBwb3J0ZWQifX0",
     *              "authenticatorData": "QKDHG74th07LO3M5wth..9ou0A3KS7tvcuEqFjdXZtgYMECgE",
     *              "signature": "MEUCIClubX7vK0XuUiv5ZzQl0d...uCLa3f3zCI10lqbL7ciihtpXHHYE",
     *              "clientExtensions": {}
     *          }
     *       },
     *       "strongkeyMetadata": {
     *          "version": "1.0",
     *          "create_location": "Sunnyvale, CA",
     *          "origin": "https://demo.strongkey.com",
     *          "username": "johndoe"
     *       }
     *    }
     * }
     *
     * Input to the SACL back-end application looks different - much like the following since
     * it needs to validate the user
     *
     * {
     *    "saclFidoServiceInput": {
     *        "did": 1,
     *        "service": Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_AUTHENTICATE_FIDO_KEY,
     *        "saclCredentials": {
     *          "uid": 87
     *        }
     *        "payload": {
     *            "publicKeyCredential": {
     *                "type": "public-key",
     *                "id": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
     *                "rawId": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
     *                "response": {
     *                    "clientDataJSON": "eyJ0eXBlIjoid2...vdC1zdXBwb3J0ZWQifX0",
     *                    "authenticatorData": "QKDHG74th07LO3M5wth..9ou0A3KS7tvcuEqFjdXZtgYMECgE",
     *                    "signature": "MEUCIClubX7vK0XuUiv5ZzQl0d...uCLa3f3zCI10lqbL7ciihtpXHHYE",
     *                    "clientExtensions": {}
     *                }
     *            }
     *        }
     *    }
     * }
     */
    private JSONObject getAuthenticateParameters(AuthenticationSignature authenticationSignature, PublicKeyCredential publicKeyCredential, String webserviceUrl) {

        try {
//            JSONObject mJSONObjectInput = new JSONObject()
//                    .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE_INPUT, new JSONObject()
//                            .put(Constants.JSON_KEY_MDBA_FIDO_DID, publicKeyCredential.getDid())
//                            .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE, Constants.MDBA_FIDO_SERVICES.MDBA_FIDO_SERVICE_AUTHENTICATE_FIDO_KEY)
//                            .put(Constants.JSON_KEY_MDBA_CREDENTIALS, new JSONObject()
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_UID, uid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_DEVID, devid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_RDID, rdid))
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT, new JSONObject()
                            .put(Constants.JSON_KEY_SACL_FIDO_DID, publicKeyCredential.getDid())
                            .put(Constants.JSON_KEY_SACL_FIDO_SERVICE, Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_AUTHENTICATE_FIDO_KEY)
                            .put(Constants.JSON_KEY_SACL_CREDENTIALS, new JSONObject()
                                    .put(Constants.JSON_KEY_SACL_FIDO_UID, uid))
                            .put(Constants.JSON_KEY_FIDO_PAYLOAD, new JSONObject()
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL, new JSONObject()
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_TYPE, Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY)
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_ID_LABEL, Common.urlEncode(publicKeyCredential.getCredentialId()))
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_RAW_ID_LABEL, publicKeyCredential.getUserHandle())
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_RESPONSE, new JSONObject()
                                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_CLIENT_DATA_JSON, authenticationSignature.getClientDataJson())
                                                    .put(Constants.ANDROID_KEYSTORE_ASSERTION_LABEL_AUTHENTICATOR_DATA, authenticationSignature.getAuthenticatorData())
                                                    .put(Constants.ANDROID_KEYSTORE_ASSERTION_LABEL_SIGNATURE, authenticationSignature.getSignature())
                                                    .put(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_USERHANDLE, "")
                                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_CLIENT_EXTENSIONS, new JSONObject())))
                            ));

            Common.printVeryLongLogMessage(TAG, "mJSONObjectInput for Authentication: " + mJSONObjectInput.toString(2));
            return mJSONObjectInput;

        } catch (JSONException | RuntimeException ex) {
            ex.printStackTrace();
            Log.w(TAG, ex.getLocalizedMessage());
            try {
                return Common.JsonError(TAG, "call", "error", ex.getLocalizedMessage());
            } catch (JSONException je) {}
            // Hopefully, we do not get to this
            return null;
        }
    }

    /**
     * Gets the next counter value for the PublicKeyCredential and updates it within
     * the RoomDB
     *
     * @param saclRepository      SaclRepository object
     * @param publicKeyCredential PublicKeyCredential of the user
     * @return int Updated value of counter for this credential
     * <p>
     * TODO: Do we need a lock around this method? Could there be 2 instances of the app executing?
     */
    private int getAndIncrementCounter(SaclRepository saclRepository, PublicKeyCredential publicKeyCredential) {
        int nextCounter = publicKeyCredential.getCounter() + 1;
        publicKeyCredential.setCounter(nextCounter);
        saclRepository.update(publicKeyCredential);
        Log.d(TAG, "Current counter value for DID-CREDENTIALID [" + did+"-"+publicKeyCredential.getCredentialId() + "]: " + nextCounter);
        return nextCounter;
    }
}
