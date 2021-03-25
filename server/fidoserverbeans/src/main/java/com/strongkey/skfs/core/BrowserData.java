/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.core;

import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

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
            browserdataJson = new String(Base64.getUrlDecoder().decode(browserdataB64Encoded), "UTF-8");
        } catch (UnsupportedEncodingException ex) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData",
                        SKFSCommon.getMessageProperty("FIDO-ERR-5013"), ex.getLocalizedMessage());
            throw new SKFEException(ex);
        }

        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "processBrowserData",
                        SKFSCommon.getMessageProperty("FIDO-MSG-5028"), "");
        parseBrowserDataJson(browserdataJson);

        if (requesttype == 0 && !this.requesttype.equals(SKFSConstants.REGISTER_CLIENT_DATA_OPTYPE)) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData",
                        SKFSCommon.getMessageProperty("FIDO-ERR-5014"), this.requesttype);
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-5014") + this.requesttype);
        }

        if (requesttype == 1 && !this.requesttype.equals(SKFSConstants.AUTHENTICATE_CLIENT_DATA_OPTYPE)) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData",
                        SKFSCommon.getMessageProperty("FIDO-ERR-5014"), this.requesttype);
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-5014") + this.requesttype);
        }

        try {
            Base64.getUrlDecoder().decode(this.challenge);
        } catch (Exception ex) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "processBrowserData",
                        SKFSCommon.getMessageProperty("FIDO-ERR-5015"), ex.getLocalizedMessage());
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-5015") + ex);
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

            this.requesttype = jsonObject.getString(SKFSConstants.JSON_KEY_REQUESTTYPE);
            this.challenge = jsonObject.getString(SKFSConstants.JSON_KEY_NONCE);
            this.origin = jsonObject.getString(SKFSConstants.JSON_KEY_SERVERORIGIN);
            try {
                this.channelid = jsonObject.getString(SKFSConstants.JSON_KEY_CHANNELID);
            } catch (Exception ex) {
                //do nothing, channel ID is optional
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.WARNING, classname, "parseBrowserDataJson",
                        SKFSCommon.getMessageProperty("FIDO-WARN-5002"), " Channelid is optional; so proceeding ahead");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "parseBrowserDataJson",
                        SKFSCommon.getMessageProperty("FIDO-ERR-5011"), ex.getLocalizedMessage());
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-5011") + ex);
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
