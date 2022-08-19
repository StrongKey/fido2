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
 * Asynchronous task to generate a FIDO2 key-pair, and provide the necessary
 * artifacts to register the newly minted public key with the FIDO server
 */

package com.strongkey.sacl.asynctasks;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.crypto.AuthenticatorMakeCredential;
import com.strongkey.sacl.roomdb.PreregisterChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sacl.webservices.CallWebservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.Callable;

public class FidoUserAgentRegisterTask implements Callable {

    private final String TAG = FidoUserAgentPreregisterTask.class.getSimpleName();
    private SaclRepository saclRepository;
    private PublicKeyCredential publicKeyCredential;
    private LocalContextWrapper context;
    private Resources resources;
    private Long uid;
    private int did;

    /**
     * Constructor
     *
     * @param context Application context
     * @param did int value of the cryptographic domain ID
     * @param uid Long value of the user ID
     */
    public FidoUserAgentRegisterTask(Context context,
                                     int did,
                                     Long uid) {
        this.context = new LocalContextWrapper(context);
        this.resources = context.getResources();
        this.did = did;
        this.uid = uid;
    }

    @Override
    public Object call() {

        // Get SACL Repository
        saclRepository = Common.getRepository(context);

        // Find the preregister challenge
        PreregisterChallenge preregisterChallenge = (PreregisterChallenge)
                Common.getCurrentObject(Constants.SACL_OBJECT_TYPES.PREREGISTER_CHALLENGE);
        if (preregisterChallenge == null) {
            Log.w(TAG, context.getResources().getString(R.string.message_challenge_error));
            try {
                return Common.JsonError(TAG, "call", "error", resources.getString(R.string.fido_error_null_challenge));
            } catch (JSONException ex) {}
            // Hopefully, we do not get to this
            return null;
        }

        // Generate the key-pair and attestation response
        String webserviceOrigin = context.getResources().getString(R.string.sacl_service_hostport);
        Object object = AuthenticatorMakeCredential.execute(context, preregisterChallenge, webserviceOrigin);
        if (object instanceof JSONObject) {
            JSONObject error = (JSONObject) object;
            String errorMsg = error.toString();
            Log.w(TAG, errorMsg);
            return error;
        }

        // Not an error
        if (object instanceof PublicKeyCredential) {
            publicKeyCredential = (PublicKeyCredential) object;
        }

        // Build input parameters into a JSON object
        JSONObject parameters = getRegisterParameters(publicKeyCredential);
        if (parameters.has("error")) {
            return parameters;
        }

        // Call the register webservice with built-up parameters
        JSONObject response = CallWebservice.execute(Constants.JSON_KEY_FIDO_SERVICE_OPERATION_REGISTER_FIDO_KEY,
                parameters, context);

        if (response.has("error")) {
            return response;
        } else {
            // Success - save PublicKeyCredential to RoomDB
            publicKeyCredential.setCreateDate(Common.now());
            int inserted = saclRepository.insert(publicKeyCredential);
            Log.v(TAG, "Saved PKC; DB returned: " + inserted);
            Common.setCurrentObject(Constants.SACL_OBJECT_TYPES.PUBLIC_KEY_CREDENTIAL, publicKeyCredential);

            // Return URL and HTTP code to be displayed by onPostExecute method
            String pkcString = publicKeyCredential.toString();
            Common.printVeryLongLogMessage("Saved PublicKeyCredential", pkcString);
            return publicKeyCredential;
        }
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
     *             "attestationObject": "o2NmbXRmcGFj...a2VkZ2uivtzxnwOKYOPHMmTcyRW4",
     *             "clientDataJSON": "eyJ0eXBlIjoid2V...YTIwOS5zdHJvbmdhdXRoLmNvbSJ9",
     *             "clientExtensions": {}
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
     *        "service": Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_REGISTER_FIDO_KEY,
     *        "saclCredentials": {
     *          "uid": 87
     *        }
     *        "payload": {
     *            "publicKeyCredential": {
     *                "type": "public-key",
     *                "id": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
     *                "rawId": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
     *                "response": {
     *                    "attestationObject": "o2NmbXRmcGFj...a2VkZ2uivtzxnwOKYOPHMmTcyRW4",
     *                    "clientDataJSON": "eyJ0eXBlIjoid2V...YTIwOS5zdHJvbmdhdXRoLmNvbSJ9"
     *                    "clientExtensions": {}
     *                }
     *            }
     *        }
     *    }
     * }
     *
     */
    private JSONObject getRegisterParameters(PublicKeyCredential publicKeyCredential) {

        try {
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT, new JSONObject()
                            .put(Constants.JSON_KEY_SACL_FIDO_DID, publicKeyCredential.getDid())
                            .put(Constants.JSON_KEY_SACL_FIDO_SERVICE, Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_REGISTER_FIDO_KEY)
                            .put(Constants.JSON_KEY_SACL_CREDENTIALS, new JSONObject()
                                .put(Constants.JSON_KEY_SACL_FIDO_UID, uid))
                            .put(Constants.JSON_KEY_FIDO_PAYLOAD, new JSONObject()
                                .put(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL, new JSONObject()
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_TYPE, Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY)
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_ID_LABEL, Common.urlEncode(publicKeyCredential.getCredentialId()))
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_RAW_ID_LABEL, publicKeyCredential.getUserHandle())
                                .put(Constants.JSON_KEY_FIDO_PAYLOAD_RESPONSE, new JSONObject()
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_ATTESTATION_OBJECT_LABEL, publicKeyCredential.getCborAttestation())
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_CLIENT_DATA_JSON, publicKeyCredential.getClientDataJson()))
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_CLIENT_EXTENSIONS, new JSONObject())
                                ))
                            );

            Common.printVeryLongLogMessage(TAG, "mJSONObjectInput for Registration: " + mJSONObjectInput.toString(2));
            return mJSONObjectInput;

        } catch (JSONException | RuntimeException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
            try {
                return Common.JsonError(TAG, "call", "error", ex.getLocalizedMessage());
            } catch (JSONException je) {
            }
            // Hopefully, we do not get to this
            return null;
        }
    }
}
