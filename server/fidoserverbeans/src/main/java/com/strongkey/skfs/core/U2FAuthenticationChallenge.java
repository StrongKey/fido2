/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.core;

import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import java.io.Serializable;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;

public class U2FAuthenticationChallenge extends U2FChallenge implements Serializable {

    /**
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    private final String keyhandle;

    private String appid;

    private JsonArray transports;

    /**
     * Constructor that constructs U2F authentication challenge parameters for
     * the user specified by username and complying to U2F protocol version
     * specified by u2fversion. The nonce is generated in the super class
     * 'U2FChallenge'.
     *
     * @param u2fversion - Version of the U2F protocol being communicated in;
     * example : "U2F_V2"
     * @param username - any non-empty username
     * @param keyhandlefromDB - The user could have multiple fido authenticators
     * registered successfully. An authentication challenge can pertain to only
     * one unique fido authenticator (key handle).
     * @param appidfromDB
     * @param transport_list
     * @throws IllegalArgumentException - In case of any error
     */
    public U2FAuthenticationChallenge(String u2fversion, String username, String keyhandlefromDB, String appidfromDB, JsonArray transport_list) throws IllegalArgumentException {
        super(u2fversion, username);

        if (keyhandlefromDB == null || keyhandlefromDB.trim().isEmpty()) {
            throw new IllegalArgumentException("keyhandle cannot be null or empty");
        }

        keyhandle = keyhandlefromDB;
        appid = appidfromDB;
        transports = transport_list;
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "U2FAuthenticationChallenge", SKFSCommon.getMessageProperty("FIDO-MSG-5004"), "");
    }

    /**
     * Get methods to access the challenge parameters
     *
     * @return
     */
    public String getKeyhandle() {
        return keyhandle;
    }

    public String getVersion() {
        return version;
    }

    public JsonArray getTransports() {
        return transports;
    }

//    public String getAppid() {
//        return appid;
//    }
//    public String getSessionid() {
//        return sessionid;
//    }
//
//    public String getNonce() {
//        return nonce;
//    }
    /**
     * Converts this POJO into a JsonObject and returns the same.
     *
     * @param appidfromfile
     * @return JsonObject
     */
    public final JsonObject toJsonObject(String appidfromfile) {
        JsonObject jsonObj;
        if (appid.equalsIgnoreCase(appidfromfile)) {
            jsonObj = Json.createObjectBuilder()
                    .add(SKFSConstants.JSON_USER_KEY_HANDLE_SERVLET, this.keyhandle)
                    .add(SKFSConstants.JSON_KEY_TRANSPORT, transports)
                    .add(SKFSConstants.JSON_KEY_VERSION, version)
                    .build();
        } else {
            jsonObj = Json.createObjectBuilder()
                    .add(SKFSConstants.JSON_USER_KEY_HANDLE_SERVLET, this.keyhandle)
                    .add(SKFSConstants.JSON_KEY_TRANSPORT, transports)
                    .add(SKFSConstants.JSON_KEY_VERSION, version)
                    .add(SKFSConstants.JSON_KEY_APP_ID, appid)
                    .build();
        }

        return jsonObj;
    }

    /**
     * Converts this POJO into a JsonObject and returns the String form of it.
     *
     * @param appidfromfile
     * @return String containing the Json representation of this POJO.
     */
    public final String toJsonString(String appidfromfile) {
        return toJsonObject(appidfromfile).toString();
    }
}
