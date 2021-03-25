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
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * The implementation of the SfaEcoService interface representing
 * webservice operations available on the SACL FIDO eCommerce App.
 * Please see documentation of these methods in the interface file.
 */

package com.strongkey.sfaeco.webservices;

import android.content.Context;
import android.util.Log;

import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.interfaces.SfaEcoService;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;

import org.json.JSONException;
import org.json.JSONObject;

public class SfaEcoServiceImpl implements SfaEcoService {

    // TAGs for logging
    private static final String TAG = SfaEcoServiceImpl.class.getSimpleName();
    private static String MTAG = null;

    // Private objects
    private static LocalContextWrapper mLocalContextWrapper;

    // Constructor with Context
    public SfaEcoServiceImpl(Context context) {
        mLocalContextWrapper = new LocalContextWrapper(context);
    }


    /**********************************************************************************
     *          888                        888      888     888
     *          888                        888      888     888
     *          888                        888      888     888
     *  .d8888b 88888b.   .d88b.   .d8888b 888  888 888     888 .d8888b   .d88b.  888d888
     * d88P"    888 "88b d8P  Y8b d88P"    888 .88P 888     888 88K      d8P  Y8b 888P"
     * 888      888  888 88888888 888      888888K  888     888 "Y8888b. 88888888 888
     * Y88b.    888  888 Y8b.     Y88b.    888 "88b Y88b. .d88P      X88 Y8b.     888
     *  "Y8888P 888  888  "Y8888   "Y8888P 888  888  "Y88888P"   88888P'  "Y8888  888
     *
     **********************************************************************************/
    /*
     * @param did String with Domain ID on SFA
     * @param username String containing a username for checking if it is available
     *
     * @return
     */
    @Override
    public boolean checkUsername(String did, String username) {
        return false;
    }


    /**********************************************************************************************
     *                           d8b          888                     888     888
     *                           Y8P          888                     888     888
     *                                        888                     888     888
     * 888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888 888     888 .d8888b   .d88b.  888d888
     * 888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"   888     888 88K      d8P  Y8b 888P"
     * 888     88888888 888  888 888 "Y8888b. 888    88888888 888     888     888 "Y8888b. 88888888 888
     * 888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888     Y88b. .d88P      X88 Y8b.     888
     * 888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888      "Y88888P"   88888P'  "Y8888  888
     *                       888
     *                  Y8b d88P
     *                   "Y88P"
     **********************************************************************************************/
    /**
     * Enrolls a new user to the web-app with the minimum required information
     *
     * @param mDid String containing the crypto-domain into which this object will be stored
     * @param mUsername String containing the username
     * @param mGivenName String containing the given (first) name of the user
     * @param mFamilyName String containing the family (last) name of the user
     * @param mEmail String containing an e-mail address of the user
     * @param mMobileNumber String containing the mobile number of the user. This is
     * necessary so One Time Passcodes (OTP) may be sent to the user for the FIDO
     * registration confirmation process (after the device is accepted within the system)
     *
     * @return JSONObject containing the response with the new user's saved information,
     * along with an assigned userid (UID), status and creation date/time.
     */

    @Override
    public JSONObject registerUser(String mDid,
                                   String mUsername,
                                   String mGivenName,
                                   String mFamilyName,
                                   String mEmail,
                                   String mMobileNumber) {
        // Entry log
        MTAG = "registerUser";
        long timeout, timein = Common.nowms();
        Log.i(TAG, MTAG + "-IN-" + timein);

        // Create or Get LocalContextWrapper to access app resources
        if (mLocalContextWrapper == null) {
            String mErrorString = mLocalContextWrapper.getString(R.string.ERROR_NULL_CONTEXT);
            Log.w(TAG, mErrorString);
            try {
                timeout = Common.nowms();
                Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
                return Common.jsonError(TAG, MTAG, Constants.ERROR_NULL_CONTEXT, mErrorString);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Call the webservice and return a response
        JSONObject jo = RegisterUser.execute(mDid, mUsername, mGivenName,
                mFamilyName, mEmail, mMobileNumber, mLocalContextWrapper);
        timeout = Common.nowms();
        Log.i(TAG, MTAG + "-OUT-" + timein +"-" + timeout +"|TTC=" + (timeout-timein));
        return jo;
    }
}
