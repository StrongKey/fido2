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
 * This class implements the registerUser method of the SfaEcoService
 * interface.
 */

package com.strongkey.sfaeco.webservices;

import android.content.ContextWrapper;
import android.util.Log;

import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

import org.json.JSONException;
import org.json.JSONObject;

@SuppressWarnings("DanglingJavadoc")
public class RegisterUser {

    // TAGs for logging
    private static final String TAG = RegisterUser.class.getSimpleName();

    /**************************************************************
     *                                              888
     *                                              888
     *                                              888
     *  .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     * d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     * 88888888   X88K   88888888 888      888  888 888    88888888
     * Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     *  "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *************************************************************/
    protected static JSONObject execute(String did,
                                        String username,
                                        String givenName,
                                        String familyName,
                                        String email,
                                        String userMobileNumber,
                                        ContextWrapper contextWrapper) {
        try {
            // Create the JSON object with input parameters that goes to the webservice
            // operation in the SFA servlet
            JSONObject mJSONObjectInput = new JSONObject()
                    .put(SfaConstants.JSON_KEY_DID, did)
                    .put(SfaConstants.JSON_KEY_USER, new JSONObject()
                        .put(SfaConstants.JSON_KEY_USER_USERNAME, username)
                        .put(SfaConstants.JSON_KEY_USER_GIVEN_NAME, givenName)
                        .put(SfaConstants.JSON_KEY_USER_FAMILY_NAME, familyName)
                        .put(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS, email)
                        .put(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER, userMobileNumber));

            Log.d(TAG, "mJSONObjectInput: " + mJSONObjectInput.toString());
            String wsoperation = contextWrapper.getResources().getString(R.string.saclfido_webservice_operation_register_user);
            return CallWebservice.execute(wsoperation, mJSONObjectInput, contextWrapper);

        } catch (JSONException | RuntimeException ex) {
            ex.printStackTrace();
            try {
                return Common.JsonError(TAG, "execute", "http", ex.getLocalizedMessage());
            } catch (JSONException e) {
                return null;
            }
        }
    }
}
