/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, as published by the Free
 * Software Foundation and available at
 * http://www.fsf.org/licensing/licenses/lgpl.html, version 2.1.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <p>
 * Copyright (c) 2001-2022 StrongAuth, Inc. (d/b/a StrongKey)
 * <p>
 * **********************************************
 * <p>
 * 888b    888          888
 * 8888b   888          888
 * 88888b  888          888
 * 888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 * 888 Y88b888 d88""88b 888    d8P  Y8b 88K
 * 888  Y88888 888  888 888    88888888 "Y8888b.
 * 888   Y8888 Y88..88P Y88b.  Y8b.          X88
 * 888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 * <p>
 * **********************************************
 * <p>
 * Asynchronous task to get a preauthentication challenge from FIDO server as
 * part of the authentication process
 */

package com.strongkey.sacl.asynctasks;

import android.content.Context;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.roomdb.PreauthenticateChallenge;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sacl.webservices.CallWebservice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.concurrent.Callable;

public class FidoUserAgentPreauthenticateTask implements Callable {

    private final String TAG = FidoUserAgentPreauthenticateTask.class.getSimpleName();

    SaclRepository saclRepository;
    private LocalContextWrapper context;
    private Long uid;
    private int did;

    /**
     * Constructor
     *
     * @param context Application context
     * @param did int value of the cryptographic domain ID
     * @param uid Long value of the user ID
     */
    public FidoUserAgentPreauthenticateTask(Context context,
                                            int did,
                                            Long uid) {
        this.context = new LocalContextWrapper(context);
        this.did = did;
        this.uid = uid;
    }

    /**
     * Thread that runs in the background. Generates parameters to call the
     * preauthenticate webservice; verifies response and returns a
     * PreauthenticationChallenge, or a JSONObject with an error message.
     *
     * @return Object containing PreauthenticationChallenge or JSONObject
     */
    @Override
    public Object call() {

        // Get SACL Repository
        saclRepository = Common.getRepository(context);

        // Build input parameters into a JSON object
        JSONObject parameters = generatePreauthenticateParameters();
        if (parameters.has("error")) {
            return parameters;
        }

        // Call webservice to get a challenge
        JSONObject response = CallWebservice.execute(Constants.JSON_KEY_FIDO_SERVICE_OPERATION_GET_FIDO_AUTHENTICATION_CHALLENGE,
                parameters, context);
        if (response.has("error")) {
            return response;
        }

        // Check response; if valid, save challenge object to RoomDB
        Object object = parsePreauthenticateChallengeResponse(uid, response);
        if (object instanceof JSONObject) {
            JSONObject error = (JSONObject) object;
            Log.e(TAG, error.toString());
            return error;
        }

        // Not an error - cast object to challenge
        PreauthenticateChallenge preauthenticateChallenge = (PreauthenticateChallenge) object;
        if (preauthenticateChallenge == null) {
            String nullresponse = context.getResources().getString(R.string.message_challenge_null);
            Log.e(TAG, nullresponse);
            try {
                return Common.JsonError(TAG, "call", "error", nullresponse);
            } catch (JSONException ex) {}
            // Hopefully, we do not get to this
            return null;
        }

        // Save challenge in repository
        saclRepository.insert(preauthenticateChallenge);
        Common.setCurrentObject(Constants.SACL_OBJECT_TYPES.PREAUTHENTICATE_CHALLENGE, preauthenticateChallenge);
        return preauthenticateChallenge;
    }

    /**
     * Create the JSON object with input parameters that goes to the webservice. Input
     * parameter to the FIDO server looks like the following:
     *
     * {
     *   "svcinfo": {
     *     "did": 1,
     *     "protocol": "FIDO2_0",
     *     "authtype": "PASSWORD",
     *     "svcusername": "svcfidouser",
     *     "svcpassword": "Abcd1234!"
     *   },
     *   "payload": {
     *     "username": "hercules2",
     *     "options": "{}"
     *   }
     * }
     *
     * However, backend application gets the preauthenticate request, retrieves information
     * from the application's database and relays the request to the FIDO server. Input to
     * the FIDO server looks like the following:
     *
     * {
     *   "saclFidoServiceInput": {
     *     "did": 1,
     *     "service": Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_GET_FIDO_AUTHENTICATION_CHALLENGE,
     *     "saclCredentials": {
     *       "uid": 87,
     *     }
     *   }
     * }
     */
    private JSONObject generatePreauthenticateParameters() {
        try {
//            JSONObject mJSONObjectInput = new JSONObject()
//                    .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE_INPUT, new JSONObject()
//                            .put(Constants.JSON_KEY_MDBA_FIDO_DID, did)
//                            .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE, Constants.MDBA_FIDO_SERVICES.MDBA_FIDO_SERVICE_GET_FIDO_AUTHENTICATION_CHALLENGE)
//                            .put(Constants.JSON_KEY_MDBA_CREDENTIALS, new JSONObject()
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_UID, uid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_DEVID, devid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_RDID, rdid)));
            // Buggy version because of strings inside FIDO2 server's JSON sub-objects
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT, new JSONObject()
                            .put(Constants.JSON_KEY_SACL_FIDO_DID, did)
                            .put(Constants.JSON_KEY_SACL_FIDO_SERVICE, Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_GET_FIDO_AUTHENTICATION_CHALLENGE)
                            .put(Constants.JSON_KEY_SACL_CREDENTIALS, new JSONObject()
                                    .put(Constants.JSON_KEY_SACL_FIDO_UID, uid)));

            Log.d(TAG, "mJSONObjectInput: " + mJSONObjectInput.toString(2));
            return mJSONObjectInput;

        } catch (JSONException | RuntimeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Parses the JSON response string and return a FIDO2 Server's Preauthenticate Challenge object.
     * The "alg" attribute is incorrect - WebAuthn requires a transports sequence
     * @param uid Long value of the unique ID of the MDBA user
     * @param jsonObject JSON that resembles the following:
     *
     * {
     *   "Response": {
     *     "challenge": "-DK8TOVK6WoVNMHsKJiv9w",
     *     "allowCredentials": [
     *       {
     *         "type": "public-key",
     *         "id": "Pu-8s87LButAPesi....BLWdHH277bGIu1Wy2iFmwA5RM9zZcCzG1oQrldtLoQ4-T2zRQ",
     *         "alg": -7
     *       }
     *     ],
     *     "rpId": "noorhome.net"
     *   }
     * }
     *
     * @return Object with PreauthenticateChallenge or JSONObject with error
     */
    private Object parsePreauthenticateChallengeResponse(Long uid, JSONObject jsonObject) {

        PreauthenticateChallenge preauthenticateChallenge = new PreauthenticateChallenge();
        try {
            JSONObject responseJson = jsonObject.getJSONObject(Constants.JSON_KEY_PREAUTH_RESPONSE);
            Iterator<String> jsonkeys = responseJson.keys();
            while (jsonkeys.hasNext()) {
                switch (jsonkeys.next()) {
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_CHALLENGE:
                        preauthenticateChallenge.setChallenge(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_CHALLENGE));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_RELYING_PARTY_ID:
                        preauthenticateChallenge.setRpid(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_RELYING_PARTY_ID));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_ALLOW_CREDENTIALS:
                        JSONArray allowcred = responseJson.getJSONArray(Constants.JSON_KEY_PREAUTH_RESPONSE_ALLOW_CREDENTIALS);
                        preauthenticateChallenge.setAllowCredentialsJSONArray(allowcred);
                        preauthenticateChallenge.setAllowCredentials(allowcred.toString());
                        break;
                    default:
                        break;
                }
            }

            // Set id to 0 before saving so it gets auto-filled in the DB + set other attributes
            preauthenticateChallenge.setId(0);
            preauthenticateChallenge.setDid(did);
            preauthenticateChallenge.setUid(uid);
            preauthenticateChallenge.setCreateDate(Common.now());
            Log.d(TAG, "PreauthenticateChallenge Response Object:\n" + preauthenticateChallenge.toString());
            return preauthenticateChallenge;

        } catch (JSONException ex) {
            ex.printStackTrace();
            try {
                return Common.JsonError(TAG, "parsePreauthenticateChallengeResponse", "error", ex.getLocalizedMessage());
            } catch (JSONException je) {
                return null;
            }
        }
    }
}
