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
 * Copyright (c) 2001-2020 StrongAuth, Inc. (d/b/a StrongKey)
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

public class FidoUserAgentAttestedDeviceRegisterTask implements Callable {

    private final String TAG = FidoUserAgentPreregisterTask.class.getSimpleName();
    private SaclRepository saclRepository;
    private PublicKeyCredential publicKeyCredential;
    private LocalContextWrapper context;
    private Resources resources;
    private Long uid, devid, rdid;
    private int did;

    /**
     * Constructor
     *
     * @param context Application context
     * @param did int value of the cryptographic domain ID
     * @param uid Long value of the user ID
     * @param devid Long value of the mobile device ID
     * @param rdid Long value of the registered mobile device in MDKMS
     */
    public FidoUserAgentAttestedDeviceRegisterTask(Context context,
                                                   int did,
                                                   Long uid,
                                                   Long devid,
                                                   Long rdid) {
        this.context = new LocalContextWrapper(context);
        this.resources = context.getResources();
        this.did = did;
        this.uid = uid;
        this.devid = devid;
        this.rdid = rdid;
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
     * parameters to the FIDO server looks like the following (currently):
     * NOTE: Technically, the origin in metadata should be sent by the MDBA server to
     * the FIDO server - this app should only send the origin in clientDataJSON
     *
     * {
     *     "svcinfo": {
     *         "did": 1,
     *         "protocol": "FIDO2_0",
     *         "authtype": "PASSWORD",
     *         "svcusername": "svcfidouser",
     *         "svcpassword": "Abcd1234!"
     *     },
     *     "payload": {
     *         "response": {
     * 			"id":"Pu-8s87LButAPesiSzQE7GniKw9UinXCW6ZkGQZfssAMOd2PIFhQqiHV3e8Q25VyjBBUPvvd5oi-8uJEt_Mc3ifEkOMTUpzjTYrNjnT6-5ZFRpBotxY-cSqs4zE59YzyDBLWdHH277bGIuX5bXHDs3EMloYgLkWqUPBhDr5KKAJxhD-Ix-6tsaC555pdOkTAXGvVfSL1Wy2iFmwA5RM9zZcCzG1oQrldtLoQ4-T2zRQ",
     * 			"rawId": "Pu-8s87LButAPesiSzQE7GniKw9UinXCW6ZkGQZfssAMOd2PIFhQqiHV3e8Q25VyjBBUPvvd5oi-8uJEt_Mc3ifEkOMTUpzjTYrNjnT6-5ZFRpBotxY-cSqs4zE59YzyDBLWdHH277bGIuX5bXHDs3EMloYgLkWqUPBhDr5KKAJxhD-Ix-6tsaC555pdOkTAXGvVfSL1Wy2iFmwA5RM9zZcCzG1oQrldtLoQ4-T2zRQ",
     * 			"response": {
     * 				"attestationObject": "o2NmbXRmcGFja2VkZ2F0dFN0bXSjY2FsZyZjc2lnWEcwRQIhAJOQlct30pjoAyg2iDPTta1BB66KZSXAeYJo4ss8gGdeAiAmx2QYM8bDAQY84r4rPaJ58PdVTpC5DaIUPIj3U_imWWN4NWOBWQHkMIIB4DCCAYOgAwIBAgIEbCtY8jAMBggqhkjOPQQDAgUAMGQxCzAJBgNVBAYTAlVTMRcwFQYDVQQKEw5TdHJvbmdBdXRoIEluYzEiMCAGA1UECxMZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEYMBYGA1UEAwwPQXR0ZXN0YXRpb25fS2V5MB4XDTE5MDcxODE3MTEyN1oXDTI5MDcxNTE3MTEyN1owZDELMAkGA1UEBhMCVVMxFzAVBgNVBAoTDlN0cm9uZ0F1dGggSW5jMSIwIAYDVQQLExlBdXRoZW50aWNhdG9yIEF0dGVzdGF0aW9uMRgwFgYDVQQDDA9BdHRlc3RhdGlvbl9LZXkwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQx9IY-uvfEvZ9HaJX3yaYmOqSIYQxS3Oi3Ed7iw4zXGR5C4RaKyOQeIu1hK2QCgoq210KjwNFU3TpsqAMZLZmFoyEwHzAdBgNVHQ4EFgQUNELQ4HBDjTWzj9E0Z719E4EeLxgwDAYIKoZIzj0EAwIFAANJADBGAiEA7RbR2NCtyMQwiyGGOADy8rDHjNFPlZG8Ip9kr9iAKisCIQCi3cNAFjTL03-sk7C1lij7JQ6mO7rhfdDMfDXSjegwuWhhdXRoRGF0YVkBNECgxxu-LYdOyztzOcLYYZnmZWksKNevaLtANyku7b3LQQAAAAAAAAAAAAAAAAAAAAAAAAAAALA-77yzzssG60A96yJLNATsaeIrD1SKdcJbpmQZBl-ywAw53Y8gWFCqIdXd7xDblXKMEFQ--93miL7y4kS38xzeJ8SQ4xNSnONNis2OdPr7lkVGkGi3Fj5xKqzjMTn1jPIMEtZ0cfbvtsYi5fltccOzcQyWhiAuRapQ8GEOvkooAnGEP4jH7q2xoLnnml06RMBca9V9IvVbLaIWbADlEz3NlwLMbWhCuV20uhDj5PbNFKUBAgMmIAEhWCCjFmMA6lDdSYsUo-xGbj9sabZfoisS_J8JAHcXdYaOcSJYIMTAgcAqRLXnZwlco8dkMi-1YsUyxFZ7FH0_mERReTZf",
     * 				"clientDataJSON":"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoidUdIVlZrNUh6ODl5bkQxakV3VjRhQSIsIm9yaWdpbiI6Im5vb3Job21lLm5ldCJ9"
     *                        },
     * 		    "type": "public-key"* 		},
     *          "metadata": {
     * 			"version": "1.0",
     * 			"create_location": "Sunnyvale, CA",
     * 			"username": "hercules2",
     * 			"origin": "https:\/\/sakasmb.noorhome.net:8181"
     *        }"
     *     }
     * }
     *
     * Input to the MDBA back-end application looks different - much like the following since
     * it needs to validate the user, the device ID and the registered device in MDKMS
     *
     * {
     *    "mdbaFidoServiceInput": {
     *        "did": 1,
     *        "service": Constants.MDBA_FIDO_SERVICES.MDBA_FIDO_SERVICE_REGISTER_FIDO_KEY,
     *        "mdbaCredentials": {
     *          "uid": 87,
     *          "devid": 61,
     *          "rdid": 37
     *        }
     *        "payload": {
     *           "response": {
     *              "type": "public-key",
     * 			    "id":"Pu-8s87LButAPesiSzQE7GniKw9UinXCW6ZkGQZfssAMOd2PIFhQqiHV3e8Q25VyjBBUPvvd5oi-8uJEt_Mc3ifEkOMTUpzjTYrNjnT6-5ZFRpBotxY-cSqs4zE59YzyDBLWdHH277bGIuX5bXHDs3EMloYgLkWqUPBhDr5KKAJxhD-Ix-6tsaC555pdOkTAXGvVfSL1Wy2iFmwA5RM9zZcCzG1oQrldtLoQ4-T2zRQ",
     * 			    "rawId": "Pu-8s87LButAPesiSzQE7GniKw9UinXCW6ZkGQZfssAMOd2PIFhQqiHV3e8Q25VyjBBUPvvd5oi-8uJEt_Mc3ifEkOMTUpzjTYrNjnT6-5ZFRpBotxY-cSqs4zE59YzyDBLWdHH277bGIuX5bXHDs3EMloYgLkWqUPBhDr5KKAJxhD-Ix-6tsaC555pdOkTAXGvVfSL1Wy2iFmwA5RM9zZcCzG1oQrldtLoQ4-T2zRQ",
     * 			    "response": {
     * 			       "clientDataJSON":"eyJ0eXBlIjoid2ViYXV0aG4uY3JlYXRlIiwiY2hhbGxlbmdlIjoidUdIVlZrNUh6ODl5bkQxakV3VjRhQSIsIm9yaWdpbiI6Im5vb3Job21lLm5ldCJ9"
     * 			       "attestationObject": "o2NmbXRmcGFja2VkZ2F0dFN0bXSjY2FsZyZjc2lnWEcwRQIhAJOQlct30pjoAyg2iDPTta1BB66KZSXAeYJo4ss8gGdeAiAmx2QYM8bDAQY84r4rPaJ58PdVTpC5DaIUPIj3U_imWWN4NWOBWQHkMIIB4DCCAYOgAwIBAgIEbCtY8jAMBggqhkjOPQQDAgUAMGQxCzAJBgNVBAYTAlVTMRcwFQYDVQQKEw5TdHJvbmdBdXRoIEluYzEiMCAGA1UECxMZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEYMBYGA1UEAwwPQXR0ZXN0YXRpb25fS2V5MB4XDTE5MDcxODE3MTEyN1oXDTI5MDcxNTE3MTEyN1owZDELMAkGA1UEBhMCVVMxFzAVBgNVBAoTDlN0cm9uZ0F1dGggSW5jMSIwIAYDVQQLExlBdXRoZW50aWNhdG9yIEF0dGVzdGF0aW9uMRgwFgYDVQQDDA9BdHRlc3RhdGlvbl9LZXkwWTATBgcqhkjOPQIBBggqhkjOPQMBBwNCAAQx9IY-uvfEvZ9HaJX3yaYmOqSIYQxS3Oi3Ed7iw4zXGR5C4RaKyOQeIu1hK2QCgoq210KjwNFU3TpsqAMZLZmFoyEwHzAdBgNVHQ4EFgQUNELQ4HBDjTWzj9E0Z719E4EeLxgwDAYIKoZIzj0EAwIFAANJADBGAiEA7RbR2NCtyMQwiyGGOADy8rDHjNFPlZG8Ip9kr9iAKisCIQCi3cNAFjTL03-sk7C1lij7JQ6mO7rhfdDMfDXSjegwuWhhdXRoRGF0YVkBNECgxxu-LYdOyztzOcLYYZnmZWksKNevaLtANyku7b3LQQAAAAAAAAAAAAAAAAAAAAAAAAAAALA-77yzzssG60A96yJLNATsaeIrD1SKdcJbpmQZBl-ywAw53Y8gWFCqIdXd7xDblXKMEFQ--93miL7y4kS38xzeJ8SQ4xNSnONNis2OdPr7lkVGkGi3Fj5xKqzjMTn1jPIMEtZ0cfbvtsYi5fltccOzcQyWhiAuRapQ8GEOvkooAnGEP4jH7q2xoLnnml06RMBca9V9IvVbLaIWbADlEz3NlwLMbWhCuV20uhDj5PbNFKUBAgMmIAEhWCCjFmMA6lDdSYsUo-xGbj9sabZfoisS_J8JAHcXdYaOcSJYIMTAgcAqRLXnZwlco8dkMi-1YsUyxFZ7FH0_mERReTZf",
     * 			       "clientExtensions": {}
     *              }
     *           }
     *        }
     *     }
     * }
     *
     */
    private JSONObject getRegisterParameters(PublicKeyCredential publicKeyCredential) {

        try {
//            JSONObject mJSONObjectInput = new JSONObject()
//                    .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE_INPUT, new JSONObject()
//                            .put(Constants.JSON_KEY_MDBA_FIDO_DID, publicKeyCredential.getDid())
//                            .put(Constants.JSON_KEY_MDBA_FIDO_SERVICE, Constants.MDBA_FIDO_SERVICES.MDBA_FIDO_SERVICE_REGISTER_FIDO_KEY)
//                            .put(Constants.JSON_KEY_MDBA_CREDENTIALS, new JSONObject()
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_UID, uid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_DEVID, devid)
//                                    .put(Constants.JSON_KEY_MDBA_FIDO_RDID, rdid))
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT, new JSONObject()
                            .put(Constants.JSON_KEY_SACL_FIDO_DID, publicKeyCredential.getDid())
                            .put(Constants.JSON_KEY_SACL_FIDO_SERVICE, Constants.SACL_FIDO_SERVICES.SACL_FIDO_SERVICE_REGISTER_FIDO_KEY)
                            .put(Constants.JSON_KEY_SACL_CREDENTIALS, new JSONObject()
                                    .put(Constants.JSON_KEY_SACL_FIDO_UID, uid)
                                    .put(Constants.JSON_KEY_SACL_FIDO_DEVID, devid)
                                    .put(Constants.JSON_KEY_SACL_FIDO_RDID, rdid))
                            .put(Constants.JSON_KEY_FIDO_PAYLOAD, new JSONObject()
                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_RESPONSE, new JSONObject()
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_TYPE, Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY)
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_ID_LABEL, Common.urlEncode(publicKeyCredential.getUserid()))
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_RAW_ID_LABEL, publicKeyCredential.getUserHandle())
                                            .put(Constants.JSON_KEY_FIDO_PAYLOAD_RESPONSE, new JSONObject()
                                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_CLIENT_DATA_JSON, publicKeyCredential.getClientDataJson())
                                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_ATTESTATION_OBJECT_LABEL, publicKeyCredential.getCborAttestation())
                                                    .put(Constants.JSON_KEY_FIDO_PAYLOAD_CLIENT_EXTENSIONS, JSONObject.NULL)))
                            ));

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
