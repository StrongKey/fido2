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

import java.util.concurrent.Callable;

public class FidoUserAgentAttestedDeviceAuthenticateTask implements Callable {

    private final String TAG = FidoUserAgentAttestedDeviceAuthenticateTask.class.getSimpleName();

    private SaclRepository saclRepository;
    private PublicKeyCredential publicKeyCredential;
    private LocalContextWrapper context;
    private Long uid, devid, rdid;
    private int did;

    /**
     * Constructor
     *
     * @param context     Application Context
     * @param did         int value of the cryptographic domain ID
     * @param uid         Long value of the user ID
     * @param devid       Long value of the mobile device ID
     * @param rdid        Long value of the registered mobile device in MDKMS
     */
    public FidoUserAgentAttestedDeviceAuthenticateTask(Context context,
                                                       int did,
                                                       Long uid,
                                                       Long devid,
                                                       Long rdid) {
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
            Log.v(TAG, "PreauthenticateChallenge RPID does not match webservice origin: " + pacRpid + " [" + actualRpid + "]");
            return "PreauthenticateChallenge RPID does not match webservice origin: " + pacRpid + " [" + actualRpid + "]";
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
                String credentialId = credential.getString("id");
                if (credentialId != null) {
                    publicKeyCredential = saclRepository.getByRpidCredentialId(did, actualRpid, credentialId);
                    if (publicKeyCredential != null) {
                        found = true;
                        break;
                    }
                }
            }

            // Do we have a PublicKeyCredential?
            if (!found) {
                Log.w(TAG, "PublicKeyCredentil does not exist with any credentialId in allowedCredentials: " + allowedCredentials.toString(2));
                return "PublicKeyCredentil does not exist with any credentialId in allowedCredentials";
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
     * parameters to the FIDO server looks like the following (currently):
     * NOTE: Technically, the origin in metadata should be sent by the MDBA server to
     * the FIDO server - this app should only send the origin in clientDataJSON
     *
     * {
     *      "svcinfo": {
     *          "did": 1
     *      },
     *      "payload": {
     *          "response": {
     *              "type": "public-key",
     *              "id": "MTFFOTFBQzRBMDhDNTA4OS04OTYzMDk0RjcwMDZERTAyLTkxQzAzRkE4NkIxNzZFQzUtRUQ2NEI3OEE2REU3QTI3Mw",
     *              "rawId": "eyJrZXluYW1lIjoiMT...2NWM1YTNhMzUzOWI1NGJkZmFmNWVhMTM3NWMwZSJ9",
     *              "response": {
     *                  "clientDataJSON": "eyJ0eXBlIjoid2...vdC1zdXBwb3J0ZWQifX0",
     *                  "authenticatorData": "QKDHG74th07LO3M5wthhmeZlaSwo169ou0A3KS7tvcuEAAAAAqFjdXZtgYMECgE",
     *                  "signature": "MEUCIClubX7vK0XuUiv5ZzQl0d-prd0Yc0g2eA5fRoTN9OohAiEAltWGg5YgzPuCLa3f3zCI10lqtn5FvbL7ciihtpXHHYE",
     *                  "clientExtensions": null
     *              }
     *          },
     *          "metadata": {
     *              "version": "1.0",
     *              "last_used_location": "Cupertino, CA",
     *              "username": "orion",
     *              "origin": "https://sakasmb.noorhome.net:8181"
     *          }
     *      }
     * }
     *
     * Input to the MDBA back-end application looks different - much like the following since
     * it needs to validate the user, the device ID and the registered device in MDKMS
     *
     * "mdbaFidoServiceInput": {
     *      "did": 1,
     *      "service": "MDBA_FIDO_SERVICE_AUTHENTICATE_FIDO_KEY",
     *      "mdbaCredentials": {
     *          "uid": 91,
     *          "devid": 65,
     *          "rdid": 41
     *      },
     *      "payload": {
     *          "response": {
     *          "type": "public-key",
     *          "id": "MTFFOTFBQzRBMDhDNTA4OS04OTYzMDk0RjcwMDZERTAyLTkxQzAzRkE4NkIxNzZFQzUtRUQ2NEI3OEE2REU3QTI3Mw",
     *          "rawId": "eyJrZXluYW1lIjoiMT...2NWM1YTNhMzUzOWI1NGJkZmFmNWVhMTM3NWMwZSJ9",
     *              "response": {
     *                  "clientDataJSON": "eyJ0eXBlIjoid2...vdC1zdXBwb3J0ZWQifX0",
     *                  "authenticatorData": "QKDHG74th07LO3M5wthhmeZlaSwo169ou0A3KS7tvcuEAAAAAqFjdXZtgYMECgE",
     *                  "signature": "MEUCIClubX7vK0XuUiv5ZzQl0d-prd0Yc0g2eA5fRoTN9OohAiEAltWGg5YgzPuCLa3f3zCI10lqtn5FvbL7ciihtpXHHYE",
     *                  "clientExtensions": null
     *              }
     *          }
     *      }
     *  }
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
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_ID_LABEL, Common.urlEncode(publicKeyCredential.getUserid()))
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_RAW_ID_LABEL, publicKeyCredential.getUserHandle())
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_RESPONSE, new JSONObject()
                                        .put(Constants.JSON_KEY_FIDO_PAYLOAD_CLIENT_DATA_JSON, authenticationSignature.getClientDataJson())
                                        .put(Constants.ANDROID_KEYSTORE_ASSERTION_LABEL_AUTHENTICATOR_DATA, authenticationSignature.getAuthenticatorData())
                                        .put(Constants.ANDROID_KEYSTORE_ASSERTION_LABEL_SIGNATURE, authenticationSignature.getSignature())
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
        return nextCounter;
    }
}
