/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.utilities.SKCEReturnObject;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
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
        skfsLogger.entering(skfsConstants.SKFE_LOGGER, classname, "execute");
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5001"),
                " EJB name=" + classname
                + " did=" + did
                + " username=" + username);

        SKCEReturnObject skcero = new SKCEReturnObject();

        //  input checks
        if (did == null || did < 1) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " did=" + did);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " did=" + did);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        if (username == null || username.isEmpty()) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " username=" + username);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username=" + username);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        if (username.trim().length() > Integer.parseInt(applianceCommon.getApplianceConfigurationProperty("appliance.cfg.maxlen.256charstring"))) {
            skcero.setErrorkey("FIDO-ERR-0027");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0027") + " username should be limited to 256 characters");
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0027", " username should be limited to 256 characters");
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
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
                        String mapkey = key.getFidoKeysPK().getSid() + "-" + key.getFidoKeysPK().getDid() + "-" + key.getFidoKeysPK().getUsername() + "-" + key.getFidoKeysPK().getFkid();
                        FidoKeysInfo fkinfoObj = new FidoKeysInfo(key);
                        skceMaps.getMapObj().put(skfsConstants.MAP_FIDO_KEYS, mapkey, fkinfoObj);
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
//                          String randomid = Base64.encodeBase64String(new SecureRandom().generateSeed(64));
                        String randomid = key.getFidoKeysPK().getSid() + "-" + key.getFidoKeysPK().getDid() + "-" + key.getFidoKeysPK().getUsername() + "-" + key.getFidoKeysPK().getFkid();
//                        userkeypointerMap.put(randomid, String.valueOf(key.getFidoKeysPK().getSid()) + "-" + String.valueOf(key.getFidoKeysPK().getFkid()));
                        String time_to_live = skfsCommon.getConfigurationProperty("skfs.cfg.property.userkeypointers.flush.cutofftime.seconds");
                        if (time_to_live == null || time_to_live.isEmpty()) {
                            time_to_live = "300";
                        }

                        String regSettings = key.getRegistrationSettings();
                        JsonObjectBuilder keyJsonBuilder = Json.createObjectBuilder()
                                .add("randomid", randomid)
                                .add("randomid_ttl_seconds", time_to_live)
                                .add("fidoProtocol", key.getFidoProtocol())
                                .add("fidoVersion", key.getFidoVersion())
                                .add("createLocation", key.getCreateLocation())
                                .add("createDate", key.getCreateDate().getTime())
                                .add("lastusedLocation", modifyloc)
                                .add("modifyDate", modifytime)
                                .add("status", key.getStatus());
                        if (regSettings != null) {
                            byte[] regSettingsBytes = Base64.getUrlDecoder().decode(regSettings);
                            String regSettingsString = new String(regSettingsBytes, "UTF-8");
                            String displayName = skfsCommon.getJsonObjectFromString(regSettingsString).getString("DISPLAYNAME");
                            if (displayName != null) {
                                keyJsonBuilder.add("displayName", displayName);
                            }
                        }
                        keysArrayBuilder.add(keyJsonBuilder.build());
                    }

                    //  Create a UserKeyPointers object that will bind the map 
                    //  and also track the creation time so that it can be flushed
                    //  on a periodic basis by an independent job.
//                    UserKeyPointers ukp = new UserKeyPointers(userkeypointerMap);
                    //  Add it to the map in Common.
//                    skceMaps.getMapObj().put(skfsConstants.MAP_USER_KEY_POINTERS,username, ukp);
//                    skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-0030"), "");
                }
            } 
//            else {
//                return skcero;
//            }
        } catch (Exception ex) {
            skcero.setErrorkey("FIDO-ERR-0001");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0001") + " Could not parse user keys; " + ex.getLocalizedMessage());
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0001"), " Could not parse user keys; " + ex.getLocalizedMessage());
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
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
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0001") + ex.getLocalizedMessage());
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0001"), ex.getLocalizedMessage());
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        //  on success, return the keys info as a json string.
        skcero.setReturnval(keysJsonObject.toString());

        //  log the exit and return
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
        return skcero;
    }
}
