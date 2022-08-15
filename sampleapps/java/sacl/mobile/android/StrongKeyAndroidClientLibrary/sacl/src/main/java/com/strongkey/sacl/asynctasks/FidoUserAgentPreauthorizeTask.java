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
 * Asynchronous task to get a preauthorization challenge from FIDO server as
 * part of the transaction confirmation process. It is unlike the FIDO user
 * authentication webservice request in that it must also send in a unique
 * transaction ID (TxID) and a Base64-encoded blob containing the transaction
 * detail. This will come from the SACL once the user has confirmed the
 * transaction with either a biometric prompt confirmation or a PIN/Pattern
 * confirmation (all of which are equivalent for user verification in FIDO).
 */

package com.strongkey.sacl.asynctasks;

import android.content.Context;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sacl.webservices.CallWebservice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.Iterator;
import java.util.concurrent.Callable;

public class FidoUserAgentPreauthorizeTask implements Callable {

    private final String TAG = FidoUserAgentPreauthorizeTask.class.getSimpleName();

    SaclRepository saclRepository;
    private LocalContextWrapper context;
    private Long uid;
    private int did;
    private String cart;

    /**
     * Constructor
     *
     * @param context Application context
     * @param did int value of the cryptographic domain ID
     * @param uid Long value of the user ID
     * @param cart String containing the Base64Url encoded transaction cart
     */
    public FidoUserAgentPreauthorizeTask(Context context,
                                         int did,
                                         Long uid,
                                         String cart) {
        this.context = new LocalContextWrapper(context);
        this.did = did;
        this.uid = uid;
        this.cart = cart;
    }

    /**
     * Thread that runs in the background. Generates parameters to call the
     * preauthorize webservice; verifies response and returns a
     * PreauthorizeChallenge, or a JSONObject with an error message.
     *
     * @return Object containing PreauthorizeChallenge or JSONObject
     */
    @Override
    public Object call() {

        // Get SACL Repository
        saclRepository = Common.getRepository(context);

        // Build input parameters into a JSON object
        JSONObject parameters = generatePreauthorizeParameters();
        if (parameters.has("error")) {
            return parameters;
        }

        // Call webservice to get a challenge
        JSONObject response = CallWebservice.execute(Constants.JSON_KEY_FIDO_SERVICE_OPERATION_GET_FIDO_AUTHORIZATION_CHALLENGE, parameters, context);
        if (response.has("error")) {
            Log.e(TAG, response.toString());
            return response;
        }

        // Not an error
        PreauthorizeChallenge preauthorizeChallenge = (PreauthorizeChallenge) parsePreauthorizeChallengeResponse(uid, response);

        // Save challenge in repository
        saclRepository.insert(preauthorizeChallenge);
        Common.setCurrentObject(Constants.SACL_OBJECT_TYPES.PREAUTHORIZE_CHALLENGE, preauthorizeChallenge);
        return preauthorizeChallenge;
    }

    /**
     * Create the JSON object with input parameters that goes to the webservice. Input
     * parameter to the FIDO server looks like the following - the backend SFAECO application
     * generates the unique transaction ID (TXID) and converts the cart JSON content into
     * the transaction payload that derives the authorization challenge.
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
     *       "username": "pushkar",
     *       "txid": "12345",
     *       "txpayload": "VFhJRDogMTIzND.....S4wMCBmsbGFybyBULTEwMCB1c2luZyBWaXNhIHgtMTIzNAo=",
     *       "options": {}
     *    }
     * }
     *
     * The back-end of the SFA eCommerce application receives the preauthorize webservice request,
     * processes information within the request and and relays the request to the FIDO server.
     * Input to the SFAECO application looks like:
     *
     * {
     *   "saclFidoServiceInput": {
     *     "did": 1,
     *     "service": Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_GET_FIDO_AUTHORIZATION_CHALLENGE,
     *     "saclCredentials": {
     *         "uid": 87
     *      },
     *      "payload": {
     *         "cart": "VFhJRDogMTIzND.....S4wMCBmsbGFybyBULTEwMCB1c2luZyBWaXNhIHgtMTIzNAo="
     *     }
     *   }
     * }
     */
    private JSONObject generatePreauthorizeParameters() {
        try {
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT, new JSONObject()
                            .put(Constants.JSON_KEY_SACL_FIDO_DID, did)
                            .put(Constants.JSON_KEY_SACL_FIDO_SERVICE, Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_GET_FIDO_AUTHORIZATION_CHALLENGE)
                            .put(Constants.JSON_KEY_SACL_CREDENTIALS, new JSONObject()
                                    .put(Constants.JSON_KEY_SACL_FIDO_UID, uid))
                            .put(Constants.JSON_KEY_SACL_FIDO_TRANSACTION_PAYLOAD, new JSONObject()
                                    .put(Constants.JSON_KEY_SACL_FIDO_TRANSACTION_CART, cart)));

            Log.d(TAG, "mJSONObjectInput: " + mJSONObjectInput.toString(2));
            return mJSONObjectInput;

        } catch (JSONException | RuntimeException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    /**
     * Parses the JSON response string and return a FIDO2 Server's PreauthorizeChallenge object
     * which looks like the following:
     *
     * {
     *    "Response": {
     *       "rpId": "strongkey.com",
     *       "txid": "ECO-123-75623433",
     *       "txpayload": "UaYBbEq9yNoxeaqv....cxRdd6pixa9Mh6UGF5IFN0cm9uZ0tleSBVU0QgNjk",
     *       "challenge": "r4sMJCsnn-Q_Rl-E-ZyuS7QaQtmifpujaUb6J_Gab0A",
     *       "allowCredentials": [
     *          {
     *             "type": "public-key",
     *             "id": "usURalPqEFNXK....dd6pixa9Mh6goBdhPGEyvYxkqmvhc4NsdKbs0feDvSD9zq6Q",
     *             "alg": -7
     *          }
     *       ]
     *    }
     * }
     *
     * @param uid Long value of the unique ID of the user
     * @param jsonObject JSON that resembles the object shown above. Note that we store
     * allowCredentials, both, as a JSONArray and a String in the Java object
     *
     * @return Object with PreauthorizeChallenge or JSONObject with error
     */
    private Object parsePreauthorizeChallengeResponse(Long uid, JSONObject jsonObject) {

        PreauthorizeChallenge preauthorizeChallenge = new PreauthorizeChallenge();
        try {
            JSONObject responseJson = jsonObject.getJSONObject(Constants.JSON_KEY_PREAUTH_RESPONSE);
            Iterator<String> jsonkeys = responseJson.keys();
            while (jsonkeys.hasNext()) {
                switch (jsonkeys.next()) {
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_RELYING_PARTY_ID:
                        preauthorizeChallenge.setRpid(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_RELYING_PARTY_ID));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_TXID:
                        preauthorizeChallenge.setTxid(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_TXID));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_TXPAYLOAD:
                        preauthorizeChallenge.setTxpayload(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_TXPAYLOAD));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_CHALLENGE:
                        preauthorizeChallenge.setChallenge(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_CHALLENGE));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_ALLOW_CREDENTIALS:
                        JSONArray allowcred = responseJson.getJSONArray(Constants.JSON_KEY_PREAUTH_RESPONSE_ALLOW_CREDENTIALS);
                        preauthorizeChallenge.setAllowCredentialsJSONArray(allowcred);
                        preauthorizeChallenge.setAllowCredentials(allowcred.toString());
                        break;
                    default:
                        break;
                }
            }

            // Set id to 0 before saving so it gets auto-filled in the DB + set other attributes
            preauthorizeChallenge.setId(0);
            preauthorizeChallenge.setDid(did);
            preauthorizeChallenge.setUid(uid);
            preauthorizeChallenge.setCreateDate(Common.now());
            Log.d(TAG, "PreauthorizeChallenge Response Object:\n" + preauthorizeChallenge.toString());
            return preauthorizeChallenge;

        } catch (JSONException ex) {
            ex.printStackTrace();
            try {
                return Common.JsonError(TAG, "parsePreauthorizeChallengeResponse", "error", ex.getLocalizedMessage());
            } catch (JSONException je) {
                return null;
            }
        }
    }
}
