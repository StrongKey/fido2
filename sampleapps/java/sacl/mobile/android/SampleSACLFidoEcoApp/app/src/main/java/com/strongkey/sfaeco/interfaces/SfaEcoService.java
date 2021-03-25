/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
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
 * An interface that proxies requests to the SFA webservices to
 * perform business functions in this sample app
 */

package com.strongkey.sfaeco.interfaces;

import org.json.JSONObject;


public interface SfaEcoService {

    /**
     * When a new user registers with the app, this webservice checks to see if the typed in
     * username is available or not - no sense in filling out the full form to find out that
     * the username is not available
     *
     * @param did String with Domain ID on SFA
     * @param username String containing a username for checking if it is available
     *
     * @return boolean indicating true if available, or false if not
     */
    public boolean checkUsername(
            String did,
            String username);


    /**
     * Enrolls a new user to the web-app with the minimum required information
     *
     * @param did String with Domain ID on SFA
     * @param username String containing the username
     * @param givenName String containing the given (first) name of the user
     * @param familyName String containing the family (last) name of the user
     * @param email String containing an e-mail address of the user
     * @param mobileNumber String containing the mobile number of the user. This is
     * necessary so One Time Passcodes (OTP) may be sent to the user for the FIDO registration
     * confirmation process (after the device is accepted within the system)
     *
     * @return JSONObject containing the response with the new user's saved information,
     * along with an assigned userid (UID), status and creation date/time. A sample JSON is
     * shown below:
     *
     * {
     *   "User": {
     *     "did": 1,
     *     "sid": 1,
     *     "uid": 3,
     *     "username": "johndoe",
     *     "givenName": "John",
     *     "familyName": "Doe",
     *     "email": "jdoe@internet.com",
     *     "mobileNumber": "4085551213",
     *     "status": "Other",
     *     "createDate": "2020-09-01 18:25:58.0"
     *   }
     * }
     */
    public JSONObject registerUser(
            String did,
            String username,
            String givenName,
            String familyName,
            String email,
            String mobileNumber);

}
