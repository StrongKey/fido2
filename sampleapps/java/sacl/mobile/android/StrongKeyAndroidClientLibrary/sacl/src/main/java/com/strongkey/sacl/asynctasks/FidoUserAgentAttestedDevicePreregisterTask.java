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
 * Asynchronous task to preregister with a FIDO2 server to get a challenge from
 * the FIDO server.
 */

package com.strongkey.sacl.asynctasks;

import android.content.Context;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.roomdb.AuthenticatorSelectionCriteria;
import com.strongkey.sacl.roomdb.PreregisterChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sacl.webservices.CallWebservice;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Callable;

public class FidoUserAgentAttestedDevicePreregisterTask implements Callable {

    private final String TAG = FidoUserAgentAttestedDevicePreregisterTask.class.getSimpleName();
    private SaclRepository saclRepository;
    private PublicKeyCredential publicKeyCredential;
    private LocalContextWrapper context;
    private Long uid, devid, rdid;
    private int did;

    /**
     * Constructor
     * @param context Application (for the context)
     * @param did int value of the cryptographic domain ID
     * @param uid Long value of the user ID
     * @param devid Long value of the mobile device ID
     * @param rdid Long value of the registered mobile device in MDKMS
     */
    public FidoUserAgentAttestedDevicePreregisterTask(Context context,
                                                      int did,
                                                      Long uid,
                                                      Long devid,
                                                      Long rdid) {

        this.context = new LocalContextWrapper(context);
        this.did = did;
        this.uid = uid;
        this.devid = devid;
        this.rdid = rdid;
    }

    /**
     * Thread that runs in the background. Generates parameters to call the
     * preregister webservice; verifies response and returns a PreregisterChallenge,
     * or a JSONObject with an error message.
     *
     * @return Object containing PreregisterChallenge or JSONObject
     */
    @Override
    public Object call() {
        String MTAG = "call";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        // Get SACL Repository
        saclRepository = Common.getRepository(context);

        // Build parameters parameters into a JSON object
        JSONObject parameters = generatePreregisterParameters();
        if (parameters.has("error")) {
            return parameters;
        }

        // Call webservice to get the challenge
        JSONObject response = CallWebservice.execute(Constants.JSON_KEY_FIDO_SERVICE_OPERATION_GET_FIDO_REGISTRATION_CHALLENGE,
                parameters, context);
        if (response.has("error")) {
            return response;
        }

        // Check response; if valid, save challenge object to RoomDB
        Object object = parsePreregisterChallengeResponse(uid, response);
        if (object instanceof JSONObject) {
            JSONObject error = (JSONObject) object;
            Log.e(TAG, error.toString());
            return error;
        }

        // Not an error - cast object to challenge
        PreregisterChallenge mPreregisterChallenge = (PreregisterChallenge) object;
        if (mPreregisterChallenge == null) {
            String nullresponse = context.getResources().getString(R.string.message_challenge_null);
            Log.e(TAG, nullresponse);
            try {
                return Common.JsonError(TAG, "call", "error", nullresponse);
            } catch (JSONException ex) {}
            // Hopefully, we do not get to this
            return null;
        }

        // Save challenge in repository - need to set ID to 0;
        // the real value is substituted as record is persisted
        mPreregisterChallenge.setId(0);
        mPreregisterChallenge.setDid(did);
        saclRepository.insert(mPreregisterChallenge);
        Common.setCurrentObject(Constants.SACL_OBJECT_TYPES.PREREGISTER_CHALLENGE, mPreregisterChallenge);

        // Check if a key already exists for this rpid + userid combination
        if (keyExists(mPreregisterChallenge)) {
            return publicKeyCredential;
        } else {
            timeout = Common.nowms();
            Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
            return mPreregisterChallenge;
        }
    }


    /**
     * Create preregister request with user/device credentials from the app; the MDBA
     * application back-end is expected to retrieve them from the MDBA's database and
     * relay the request to the FIDO server. Input here now looks like:
     *
     * {
     *  "mdbaFidoServiceInput": {
     *      "did": 1,
     *      "service": Constants.MDBA_FIDO_SERVICES.MDBA_FIDO_SERVICE_GET_FIDO_REGISTRATION_CHALLENGE,
     *      "mdbaCredentials": {
     *          "uid": 87,
     *          "devid": 61,
     *          "rdid": 37
     *      }
     *   }
     * }
     *
     * @return JSONObject
     */
    private JSONObject generatePreregisterParameters() {
        try {
            // Buggy version because of strings inside FIDO2 server's JSON sub-objects
//            JSONObject mJSONObjectInput = new JSONObject()
//                    .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE_INPUT, new JSONObject()
//                            .put(Constants.JSON_KEY_MDBA_FIDO_DID, did)
//                            .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE, Constants.MDBA_FIDO_SERVICES.MDBA_FIDO_SERVICE_GET_FIDO_REGISTRATION_CHALLENGE)
//                            .put(Constants.JSON_KEY_MDBA_CREDENTIALS, new JSONObject()
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_UID, uid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_DEVID, devid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_RDID, rdid)));
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT, new JSONObject()
                            .put(Constants.JSON_KEY_SACL_FIDO_DID, did)
                            .put(Constants.JSON_KEY_SACL_FIDO_SERVICE, Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_GET_FIDO_REGISTRATION_CHALLENGE)
                            .put(Constants.JSON_KEY_SACL_CREDENTIALS, new JSONObject()
                                    .put(Constants.JSON_KEY_SACL_FIDO_UID, uid)
                                    .put(Constants.JSON_KEY_SACL_FIDO_DEVID, devid)
                                    .put(Constants.JSON_KEY_SACL_FIDO_RDID, rdid)));

            Log.d(TAG, "mJSONObjectInput: " + mJSONObjectInput.toString(2));
            return mJSONObjectInput;

        } catch (JSONException | RuntimeException ex) {
            ex.printStackTrace();
            try {
                return Common.JsonError(TAG, "generatePreregisterParameters", "error", ex.getLocalizedMessage());
            } catch (JSONException je) {
                return null;
            }
        }
    }

    /**
     * Parses the JSON response string and return a FIDO2 Server's Preregister Challenge object
     *
     * @param jsonObject JSON that resembles the following:
     *
     *  {
     *      "Response": {
     *          "rp": {
     *              "name": "StrongKey FIDO2",
     *              "id": "strongkey.com"
     *          },
     *          "user": {
     *              "name": "joecool",
     *              "id": "-hG793jWpMbazuMeh-kULCS37sDuIkrj29HRsyGfL2s",
     *              "displayName": "joecool"
               },
     *          "challenge": "XbkgWZtHyxC_MtpNHEghmw",
     *          "pubKeyCredParams": [
     *              {
     *                  "type": "public-key",
     *                  "alg": -7
     *              },
     *
     *              ....
     *              {
     *                  "type": "public-key",
     *                  "alg": -257
     *              }],
     *          "excludeCredentials": [
     *                  {
     *                     "type": "public-key",
     *                     "id": "-giADzyMWAiIPGRJg38l889y9MJWW9r1vOc-kkzDNYf2peB6BSxKZf4FlVpDrWd_LI0nNAim3r3NS-UBdzdZRSblKLd4q0OyuzaeYumFpKv0l1hsNDjJmmNJUilcVRkIg-y5Xn10Jte_0vjB2TP15J2EZKZdA64nr0XC8FzK_9HQkmERsMnOOhGZprTNN1B5EqrYycBBGQMnhqXX9QR_Hd1tvOrYlRVLsaILCofu51M",
     *                     "alg": -7
     *                  },
     *                  {
     *                ....
     *                  {
     *                     "type": "public-key",
     *                     "id": "vvqMC2yY8FpFtRoa_ZuXBIqXF40GyKzR5Y4y5ZEXqYzqYmSjY7UU5v4mw-fRdt5SNWVxVGcTZvJ6OLtIS1f4c4ggsactjsphFMtIuWxr90qbf8ZR55dKES9xiUNxdNxFMgHuhWdYDFS1MFMm4aHj8fqiIf5y4K8xAfUUL9BY_Bhfm_FSA1CPU9-aEwcVVez1NN_V3FwSN-TwfDzP-vQRRCL-UkxVCriAU8QWPrLw5sk",
     *                     "alg": -7
     *                  }],
     *          "attestation": "direct"
     *      }
     *   }
     * @return PreregisterChallenge with a preregister response from FIDO2 server
     */
    private Object parsePreregisterChallengeResponse(Long uid, JSONObject jsonObject) {

        PreregisterChallenge preregJson = new PreregisterChallenge();
        try {
            JSONObject responseJson = jsonObject.getJSONObject(Constants.JSON_KEY_PREREG_RESPONSE);
            Iterator<String> jsonkeys = responseJson.keys();
            while (jsonkeys.hasNext()) {
                String jsonkey = jsonkeys.next();
                switch (jsonkey) {
                    case Constants.JSON_KEY_PREREG_RESPONSE_RP_OBJECT:
                        JSONObject rp = responseJson.getJSONObject(Constants.JSON_KEY_PREREG_RESPONSE_RP_OBJECT);
                        preregJson.setRpname(rp.getString(Constants.JSON_KEY_PREREG_RESPONSE_RP_OBJECT_NAME));
                        preregJson.setRpid(rp.getString(Constants.JSON_KEY_PREREG_RESPONSE_RP_OBJECT_ID));
                        break;
                    case Constants.JSON_KEY_PREREG_RESPONSE_USER_OBJECT:
                        JSONObject user = responseJson.getJSONObject(Constants.JSON_KEY_PREREG_RESPONSE_USER_OBJECT);
                        preregJson.setUsername(user.getString(Constants.JSON_KEY_PREREG_RESPONSE_USER_OBJECT_NAME));
                        preregJson.setUserid(user.getString(Constants.JSON_KEY_PREREG_RESPONSE_USER_OBJECT_ID));
                        preregJson.setDisplayName(user.getString(Constants.JSON_KEY_PREREG_RESPONSE_USER_OBJECT_DISPLAYNAME));
                        break;
                    case Constants.JSON_KEY_PREREG_RESPONSE_CHALLENGE:
                        preregJson.setChallenge(responseJson.getString(Constants.JSON_KEY_PREREG_RESPONSE_CHALLENGE));
                        break;
                    case Constants.JSON_KEY_PREREG_RESPONSE_ATTESTATION_CONVEYANCE:
                        preregJson.setAttestationConveyance(responseJson.getString(Constants.JSON_KEY_PREREG_RESPONSE_ATTESTATION_CONVEYANCE));
                        break;
                    case Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS:
                        JSONArray credparams = responseJson.getJSONArray(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS);
                        preregJson.setCredParamsJSONArray(credparams);
                        preregJson.setPublicKeyCredentialParams(credparams.toString());
                        break;
                    case Constants.JSON_KEY_PREREG_RESPONSE_EXCLUDE_CREDENTIALS:
                        JSONArray exclcred = responseJson.getJSONArray(Constants.JSON_KEY_PREREG_RESPONSE_EXCLUDE_CREDENTIALS);
                        preregJson.setExcludeCredJSONArray(exclcred);
                        preregJson.setExcludeCredentials(exclcred.toString());
                        break;
                    case Constants.JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_SELECTION:
                        JSONObject authseljson = responseJson.getJSONObject(Constants.JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_SELECTION);
                        AuthenticatorSelectionCriteria authseln = new AuthenticatorSelectionCriteria();
                        authseln.setAuthenticatorAttachment(authseljson.getString(Constants.JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT));
                        authseln.setRequireResidentKey(authseljson.getBoolean(Constants.JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_RESIDENT_KEY));
                        authseln.setUserVerification(authseljson.getString(Constants.JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV));
                        preregJson.setAuthenticatorSelectionJSONObject(authseljson);
                        preregJson.setAuthenticatorSelection(authseljson.toString());
                        break;
                    default:
                        break;
                }
            }
            // Set default authenticator policy
            if (!responseJson.has(Constants.JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_SELECTION)) {
                AuthenticatorSelectionCriteria authseln = new AuthenticatorSelectionCriteria();
                preregJson.setAuthenticatorSelection(authseln.toString());
            }

            // Set id to 0 before saving so it gets auto-filled in the DB + set other attributes
            preregJson.setId(0);
            preregJson.setUid(uid);
            preregJson.setCreateDate(Common.now());
            Log.v(TAG, "PreregisterChallengeResponse Object:\n" + preregJson.toString());
            return preregJson;

        } catch (JSONException ex) {
            ex.printStackTrace();
            try {
                return Common.JsonError(TAG, "generatePreregisterParameters", "error", ex.getLocalizedMessage());
            } catch (JSONException je) {
                return null;
            }
        }
    }

    /**
     * Checks to see if a FIDO key exists for the rpid + userid combination
     *
     * @param preregisterChallenge PreregisterChallenge object
     * @return boolean - true indicates a key already exists for this user for this rpid
     */
    private boolean keyExists(PreregisterChallenge preregisterChallenge) {
        int did = preregisterChallenge.getDid();
        String rpid = preregisterChallenge.getRpid();
        String userid = preregisterChallenge.getUserid();
        List<PublicKeyCredential> publicKeyCredentials = saclRepository.getPublicKeyCredentialsByRpidUserid(did, rpid, userid);
        if (publicKeyCredentials != null) {
            publicKeyCredential = publicKeyCredentials.get(0);
            if (publicKeyCredential != null) {
                Log.v(TAG, "Found a credential for RPID+USERID on this device: " + publicKeyCredential.toString());
                return true;
            }
        }
        return false;
    }
}
