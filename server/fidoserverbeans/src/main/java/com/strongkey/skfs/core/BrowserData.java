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
import com.strongkey.skfs.utilities.SKFEException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.commons.codec.binary.Base64;

/**
 * POJO that represents browser data
 */
public class BrowserData implements Serializable {

    /**
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    /**
     * Components of browser data
     */
    private String origin;
    private String challenge;
    private String channelid = null;
    
    /**
     * Possible request types
     */
    public static final int REGISTRATION_RESPONSE = 0;
    public static final int AUTHENTICATION_RESPONSE = 1;

    /**
     * Variables for internal user
     */
    private String requesttype;

    /**
     * Constructor; receives Base64 encoded browser data and the request type to
     * signify if the case is a registration or authentication.
     * 
     * This method constructs the browser data POJO by processing the browser 
     * data given for the request type.
     * 
     * @param browserdataB64Encoded - Base64 encoded browser data
     * @param requesttype           - request type
     *                                  Number 0 for registration
     *                                  Number 1 for authentication
     * @throws SKFEException  - In case of any error
     */
    public BrowserData(String browserdataB64Encoded, int requesttype) throws SKFEException {
        processBrowserData(browserdataB64Encoded, requesttype);
    }

    /**
     * 
     * @param browserdataB64Encoded
     * @param requesttype
     * @throws SKFEException 
     */
    private void processBrowserData(String browserdataB64Encoded, int requesttype) throws SKFEException {
        String browserdataJson = null;

        try {
            browserdataJson = new String(Base64.decodeBase64(browserdataB64Encoded), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData", 
                        skfsCommon.getMessageProperty("FIDO-ERR-5013"), ex.getLocalizedMessage());
            throw new SKFEException(ex);
        }

        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processBrowserData", 
                        skfsCommon.getMessageProperty("FIDO-MSG-5028"), "");
        parseBrowserDataJson(browserdataJson);

        if (requesttype == 0 && !this.requesttype.equals(skfsConstants.REGISTER_CLIENT_DATA_OPTYPE)) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData", 
                        skfsCommon.getMessageProperty("FIDO-ERR-5014"), this.requesttype);
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5014") + this.requesttype);
        }
        
        if (requesttype == 1 && !this.requesttype.equals(skfsConstants.AUTHENTICATE_CLIENT_DATA_OPTYPE)) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData", 
                        skfsCommon.getMessageProperty("FIDO-ERR-5014"), this.requesttype);
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5014") + this.requesttype);
        }
      
        try {
            byte[] challengebytes = Base64.decodeBase64(this.challenge);
        } catch (Exception ex) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData", 
                        skfsCommon.getMessageProperty("FIDO-ERR-5015"), ex.getLocalizedMessage());
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5015") + ex);
        }
    }

    /**
     * Parses the browser data supplied in the form of stringified Json.
     * Looks for the needed key-value pairs that define a correct browser data
     * and fills up BrowserData pojo object.
     * 
     * @param browserdataJson       - browser data in the form of a string
     * @throws SKFEException  - in case of any error
     */
    private void parseBrowserDataJson(String browserdataJson) throws SKFEException {

        try {
            JsonReader jsonReader = Json.createReader(new StringReader(browserdataJson));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();
        
            this.requesttype = jsonObject.getString(skfsConstants.JSON_KEY_REQUESTTYPE);
            this.challenge = jsonObject.getString(skfsConstants.JSON_KEY_NONCE);
            this.origin = jsonObject.getString(skfsConstants.JSON_KEY_SERVERORIGIN);
            try {
                this.channelid = jsonObject.getString(skfsConstants.JSON_KEY_CHANNELID);
            } catch (Exception ex) {
                //do nothing, channel ID is optional
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.WARNING, classname, "parseBrowserDataJson", 
                        skfsCommon.getMessageProperty("FIDO-WARN-5002"), " Channelid is optional; so proceeding ahead");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "parseBrowserDataJson", 
                        skfsCommon.getMessageProperty("FIDO-ERR-5011"), ex.getLocalizedMessage());
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5011") + ex);
        }
    }
    
    /**
     * Get methods to access the browser data parameters
     * @return 
     */
    public String getRequestType() {
        return requesttype;
    }

    public String getOrigin() {
        return origin;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getChannelid() {
        return channelid;
    }
}
