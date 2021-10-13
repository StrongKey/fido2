/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.utilities.SKCEReturnObject;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * This EJB is responsible for executing the user registered keys retrieval
 * process of a specific user, bind the key meta data information and return
 * back the same as a meta data array.
 */
@Stateless
public class u2fGetKeysInfoBean implements u2fGetKeysInfoBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    /*
     * Enterprise Java Beans used in this EJB.
     */
    @EJB
    getFidoKeysLocal getkeybean;

    /**
     * ***********************************************************************
     * 888 888 888 .d88b. 888 888 .d88b. .d8888b 888 888 888888 .d88b. d8P Y8b
     * `Y8bd8P' d8P Y8b d88P" 888 888 888 d8P Y8b 88888888 X88K 88888888 888 888
     * 888 888 88888888 Y8b. .d8""8b. Y8b. Y88b. Y88b 888 Y88b. Y8b. "Y8888 888
     * 888 "Y8888 "Y8888P "Y88888 "Y888 "Y8888 *
     * ***********************************************************************
     */
    /**
     * This method is responsible for fetching the user registered key from the
     * persistent storage and return back the metadata.
     *
     * If the user has registered multiple fido authenticators, this method will
     * return an array of registered key metadata, each entry mapped to a random
     * id. These random ids have a 'ttl (time-to-live)' associated with them.
     * The client applications have to cache these random ids if they wish to
     * de-register keys.
     *
     * @param did - FIDO domain id
     * @param username - username
     * @return - returns SKCEReturnObject in both error and success cases. In
     * error case, an error key and error msg would be populated In success
     * case, a simple msg saying that the process was successful would be
     * populated.
     */
    @Override
    public SKCEReturnObject execute(Long did,
            String username) {

        //  Log the entry and inputs
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER, classname, "execute");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5001"),
                " EJB name=" + classname
                + " did=" + did
                + " username=" + username);

        SKCEReturnObject skcero = new SKCEReturnObject();

        //  input checks
        if (did == null || did < 1) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " did=" + did);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " did=" + did);
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        if (username == null || username.isEmpty()) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " username=" + username);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username=" + username);
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        if (username.trim().length() > Integer.parseInt(applianceCommon.getApplianceConfigurationProperty("appliance.cfg.maxlen.256charstring"))) {
            skcero.setErrorkey("FIDO-ERR-0027");
            skcero.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0027") + " username should be limited to 256 characters");
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0027", " username should be limited to 256 characters");
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        //  With the username, fetch all the keys registered for the account.
        JsonArrayBuilder keysArrayBuilder = Json.createArrayBuilder();
        try {
            Collection<FidoKeys> kh_coll = getkeybean.getByUsername(did, username);
            if (kh_coll != null){
                Iterator it = kh_coll.iterator();

                //  Initialize a map to store the randomid to the regkeyid
//                Map<String, String> userkeypointerMap = new ConcurrentSkipListMap<>();
                //  for every key registered,
                while (it.hasNext()) {
                    FidoKeys key = (FidoKeys) it.next();
                    if (key != null) {
                        //  Create a json object out of this key information
                        String mapkey = key.getFidoKeysPK().getSid() + "-" + key.getFidoKeysPK().getDid() + "-" + key.getFidoKeysPK().getFkid();
                        FidoKeysInfo fkinfoObj = new FidoKeysInfo(key);
                        skceMaps.getMapObj().put(SKFSConstants.MAP_FIDO_KEYS, mapkey, fkinfoObj);
                        long modifytime = 0L;
                        if (key.getModifyDate() != null) {
                            modifytime = key.getModifyDate().getTime();
                        }

                        String modifyloc = "Not used yet";
                        if (key.getModifyLocation() != null) {
                            modifyloc = key.getModifyLocation();
                        }

                        //  Generate a unique randomid for this key to be user
                        //  as a pointer for the key data base index.
                        String randomid = key.getFidoKeysPK().getSid() + "-" + key.getFidoKeysPK().getDid() + "-" + key.getFidoKeysPK().getFkid();
//                        String time_to_live = SKFSCommon.getConfigurationProperty("skfs.cfg.property.userkeypointers.flush.cutofftime.seconds");
//                        if (time_to_live == null || time_to_live.isEmpty()) {
//                            time_to_live = "300";
//                        }

                        String regSettings = key.getRegistrationSettings();
                        JsonObjectBuilder keyJsonBuilder = Json.createObjectBuilder()
                                .add("keyid", randomid)
//                                .add("randomid_ttl_seconds", time_to_live)
                                .add("fidoProtocol", key.getFidoProtocol())
//                                .add("fidoVersion", key.getFidoVersion())
                                .add("credentialId", key.getKeyhandle())
                                .add("createLocation", key.getCreateLocation())
                                .add("createDate", key.getCreateDate().getTime())
                                .add("lastusedLocation", modifyloc)
                                .add("modifyDate", modifytime)
                                .add("status", key.getStatus());
                        if (regSettings != null) {
                            byte[] regSettingsBytes = Base64.getUrlDecoder().decode(regSettings);
                            String regSettingsString = new String(regSettingsBytes, "UTF-8");
                            String displayName = SKFSCommon.getJsonObjectFromString(regSettingsString).getString("DISPLAYNAME");
                            if (displayName != null) {
                                keyJsonBuilder.add("displayName", displayName);
                            }
                            String attestationFormat = SKFSCommon.getJsonObjectFromString(regSettingsString).getString("attestationFormat");
                            if (displayName != null) {
                                keyJsonBuilder.add("attestationFormat", attestationFormat);
                            }
                        }
                        keysArrayBuilder.add(keyJsonBuilder.build());
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            skcero.setErrorkey("FIDO-ERR-0001");
            skcero.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + " Could not parse user keys; " + ex.getLocalizedMessage());
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0001"), " Could not parse user keys; " + ex.getLocalizedMessage());
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        JsonObject keysJsonObject;
        try {
            JsonArray keysJsonArray = keysArrayBuilder.build();
            keysJsonObject = Json.createObjectBuilder()
                    .add("keys", keysJsonArray).
                    build();
        } catch (Exception ex) {
            skcero.setErrorkey("FIDO-ERR-0001");
            skcero.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + ex.getLocalizedMessage());
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0001"), ex.getLocalizedMessage());
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        //  on success, return the keys info as a json string.
        skcero.setReturnval(keysJsonObject.toString());

        //  log the exit and return
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
        return skcero;
    }
}
