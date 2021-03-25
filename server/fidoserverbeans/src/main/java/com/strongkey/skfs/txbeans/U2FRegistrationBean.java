/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.core.U2FRegistrationResponse;
import com.strongkey.skfs.utilities.FEreturn;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

@Stateless
public class U2FRegistrationBean implements U2FRegistrationBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    @EJB
    addFidoKeysLocal addkeybean;
    @EJB
    originVerfierBeanLocal originverifierbean;
    @EJB
    u2fRegisterBeanLocal u2fregisterbean;
    @EJB
    updateFidoUserBeanLocal updateldapbean;

   @Override
    public String execute(Long did, String registrationresponse, String registrationmetadata, String protocol) {
        String wsresponse="";
        //  check for needed fields in registrationresponse and metadata
        //  fetch needed fields from registrationresponse
        String browserdata = (String) applianceCommon.getJsonValue(registrationresponse,
                SKFSConstants.JSON_KEY_CLIENTDATA, "String");
        if (browserdata == null || browserdata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0005", " Missing 'clientData'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005") + " Missing 'clientData'"));
        }
        //parse browserdata

        try {
            String browserdataJson = new String(Base64.getUrlDecoder().decode(browserdata), "UTF-8");
            JsonReader jsonReader = Json.createReader(new StringReader(browserdataJson));
            JsonObject jsonObject = jsonReader.readObject();
            jsonReader.close();

            String bdreqtype = jsonObject.getString(SKFSConstants.JSON_KEY_REQUESTTYPE);
            String bdnonce = jsonObject.getString(SKFSConstants.JSON_KEY_NONCE);
            String bdorigin = jsonObject.getString(SKFSConstants.JSON_KEY_SERVERORIGIN);
            if (bdreqtype == null || bdnonce == null || bdorigin == null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE,
                        SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Missing 'registrationData'");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Missing 'registrationData'"));
            }
        } catch (IOException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE,
                    SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'clientDATA'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'clientDATA'"));
        }
        ////
        String regdata = (String) applianceCommon.getJsonValue(registrationresponse,
                SKFSConstants.JSON_KEY_REGSITRATIONDATA, "String");
        if (regdata == null || regdata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0005", " Missing 'registrationData'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'registrationData'"));
        }

        //  fetch version and modifylocation from metadata
        String version = (String) applianceCommon.getJsonValue(registrationmetadata,
                SKFSConstants.FIDO_METADATA_KEY_VERSION, "String");
        if (version == null || version.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0018", " Missing metadata - version");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0018")
                    + " Missing metadata - version"));
        }
        String createloc = (String) applianceCommon.getJsonValue(registrationmetadata,
                SKFSConstants.FIDO_METADATA_KEY_CREATE_LOC, "String");
        if (createloc == null || createloc.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0018", " Missing metadata - createlocation");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0018")
                    + " Missing metadata - createlocation"));
        }
        String username_received = (String) applianceCommon.getJsonValue(registrationmetadata,
                SKFSConstants.FIDO_METADATA_KEY_USERNAME, "String");
        if (username_received == null || username_received.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0018", " Missing metadata - username");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0018")
                    + " Missing metadata - username"));
        }

        String session_username;

        //  5. Validate user session
        //  Look for the sessionid in the sessionmap and retrieve the username
        String ch = SKFSCommon.getChallengefromBrowserdata(browserdata);
        String chDigest;
        try {
            chDigest = SKFSCommon.getDigest(ch, "SHA-256");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0001", " Error generating hash");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + " Error generating hash"));
        }
        UserSessionInfo user = (UserSessionInfo) skceMaps.getMapObj().get(SKFSConstants.MAP_USER_SESSION_INFO, chDigest);
        if (user == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0006", "");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0006")));
        } else {
            session_username = user.getUsername();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-MSG-0022", " username=" + session_username);
        }

        //verify that the call is for the right user
        if (!session_username.equalsIgnoreCase(username_received)) {
            //throw erro saying wrong username sent
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0037")));
        }

        //  6. Verify appid
        String appid = user.getAppid();
        String origin = SKFSCommon.getOriginfromBrowserdata(browserdata);
        if (!originverifierbean.execute(appid, origin)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0032", "");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0032")
                    + " : " + appid + "-" + origin));
        }

        //  7.  Do actual registration; handover the job to an ejb
        try {
            FEreturn ret = u2fregisterbean.execute(did.toString(), protocol, registrationresponse);
            if (ret != null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-MSG-0046", ret.toString());
                U2FRegistrationResponse regResponse = (U2FRegistrationResponse) ret.getResponse();

                if (regResponse != null) {
                    //  Fetch the needed information from the reg wsresponse output.
                    String keyhandle = regResponse.getKeyhandle();
                    String publickey = regResponse.getUserpublickey();

                    /**
                     * TO BE DONE - Attestation cert validation
                     */
                    //  Get attestation cert from the local truststore
                    //  Do cert validation
                    //  If everything is found valid,
                    //  8.  Persist key info to the database
                    addkeybean.execute(did, null, session_username, keyhandle, publickey, appid,
                            (short) SKFSConstants.FIDO_TRANSPORT_USB, null, null, null, 0,
                            SKFSConstants.FIDO_PROTOCOL_VERSION_U2F_V2, SKFSConstants.FIDO_PROTOCOL_U2F,
                            null, null, null, createloc);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-MSG-0024", "");

                    if (SKFSCommon.updateFidoUsers()) {
                        //  Update the "FIDOKeysEnabled" attribute of the user to 'true'
                        try {
                            String result = updateldapbean.execute(did, session_username,
                                    SKFSConstants.LDAP_ATTR_KEY_FIDOENABLED, "true", false);
                            JsonObject jo;
                            try (JsonReader jr = Json.createReader(new StringReader(result))) {
                                jo = jr.readObject();
                            }
                            Boolean status = jo.getBoolean(SKFSConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                            if (status) {
                                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0029", "true");
                            } else {
                                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0024", "true");
                            }
                        } catch (SKFEException ex) {
                            //  Just throw an err msg and proceed.
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0024", "true");
                        }
                    }
                    //  Remove the sessionid from the sessionmap
                    ch = SKFSCommon.getChallengefromBrowserdata(regResponse.getBrowserdata());
                    String challhash = null;
                    try {
                        challhash = SKFSCommon.getDigest(ch, "SHA-256");
                    } catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0001", " Error generating hash");
                        throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + " Error generating hash");
                    }
                    skceMaps.getMapObj().remove(SKFSConstants.MAP_USER_SESSION_INFO, challhash);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-MSG-0023", user.toString());

                    wsresponse = "Successfully processed registration response";
                } else {
                    throw new SKIllegalArgumentException(SKFSCommon.buildReturn("Failed to process registration response"));
                }
            }
        } catch (SKFEException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0001", ex.getLocalizedMessage());
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + ex.getLocalizedMessage()));
        }
        return SKFSCommon.buildReturn(wsresponse);
    }
}
