/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.core;

import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.SKFEException;
import java.io.Serializable;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;

public class U2FRegistrationChallenge extends U2FChallenge implements Serializable {

    /**
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();
    
    private String nonce;

    /**
     * Constructor that constructs U2F registration challenge parameters for the
     * user specified by username and complying to U2F protocol version specified
     * by u2fversion. The nonce is generated in the super class 'U2FChallenge'.
     * 
     * @param u2fversion - Version of the U2F protocol being communicated in; 
     *                      example : "U2F_V2"
     * @param username  - any non-empty username
     * @throws SKFEException
     *                  - In case of any error
     */
   public U2FRegistrationChallenge(String u2fversion, String username) throws SKFEException {
        super(u2fversion, username);
        nonce = U2FUtility.getRandom(Integer.parseInt(skfsCommon.getConfigurationProperty("skfs.cfg.property.entropylength")));
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "U2FRegistrationChallenge", skfsCommon.getMessageProperty("FIDO-MSG-5003"), "");
   }

   /**
    * Get methods to access the challenge parameters
     * @return 
    */
    public String getVersion() {
        return version;
    }

    public String getNonce() {
        return nonce;
    }

//    public String getAppId() {
//        return appid;
//    }

//    public String getSessionId() {
//        return sessionid;
//    }
 
    /**
     * Converts this POJO into a JsonObject and returns the same.
     * @return JsonObject
     */
    public final JsonObject toJsonObject() {
        
        JsonObject jsonObj = Json.createObjectBuilder()
                .add(skfsConstants.JSON_KEY_NONCE, this.nonce)
                .add(skfsConstants.JSON_KEY_VERSION, this.version)
//                .add(skfsConstants.JSON_KEY_APP_ID, this.appid)
                .build();
        
        return jsonObj;
    }
    
    /**
     * Converts this POJO into a JsonObject and returns the String form of it.
     * @return String containing the Json representation of this POJO.
     */
    public final String toJsonString() {
        return toJsonObject().toString();
    }
}
