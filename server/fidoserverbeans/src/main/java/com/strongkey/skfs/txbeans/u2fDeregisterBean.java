/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.SKCEReturnObject;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.StringReader;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * This EJB is responsible for executing the de-registration process of a specific
 * user registered key
 */
@Stateless
public class u2fDeregisterBean implements u2fDeregisterBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    /*
     * Enterprise Java Beans used in this EJB.
     */
    @EJB getFidoKeysLocal         getkeybean;
    @EJB deleteFidoKeysLocal      deletekeybean;
    @EJB updateFidoUserBeanLocal  updateldapbean;

    /*************************************************************************
                                                 888
                                                 888
                                                 888
     .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
    d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
    88888888   X88K   88888888 888      888  888 888    88888888
    Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888

     *************************************************************************/
    /**
     * This method is responsible for deleting the user registered key from the
     * persistent storage. This method first checks if the given ramdom id is
     * mapped in memory to the specified user and if found yes, gets the registration
     * key id and deletes that entry from the database.
     *
     * Additionally, if the key being deleted is the last one for the user, the
     * ldap attribute of the user called 'FIDOKeysEnabled' is set to 'no'.
     *
     * @param did       - FIDO domain id
     * @param keyid  - random id that is unique to one fido registered authenticator
     *                      for the user.
     * @return          - returns SKCEReturnObject in both error and success cases.
     *                  In error case, an error key and error msg would be populated
     *                  In success case, a simple msg saying that the process was
     *                  successful would be populated.
     */
    @Override
    public SKCEReturnObject execute(Long did,
                                    String keyid) {

        //  Log the entry and inputs
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "execute");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5001"),
                        " EJB name=" + classname +
                        " did=" + did +
                        " keyid=" + keyid);

        SKCEReturnObject rv = new SKCEReturnObject();

        //  input checks
        if (did == null || did < 1) {
            rv.setErrorkey("FIDO-ERR-0002");
            rv.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " did=" + did);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " did=" + did);
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }

        if (keyid == null || keyid.isEmpty() ) {
            rv.setErrorkey("FIDO-ERR-0002");
            rv.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " keyid=" + keyid);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " keyid=" + keyid);
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }

        Short sid_to_be_deleted = null;
//        String did_to_be_deactivated = null;
//        int userfkidhyphen;
//        String fidouser;
        Long fkid_to_be_deleted = null;
        try {
            String[] mapvaluesplit = keyid.split("-", 3);
            sid_to_be_deleted = Short.parseShort(mapvaluesplit[0]);
//            did_to_be_deactivated = mapvaluesplit[1];
//            userfkidhyphen = mapvaluesplit[2].lastIndexOf("-");

//            fidouser = mapvaluesplit[2].substring(0, userfkidhyphen);
            fkid_to_be_deleted = Long.parseLong(mapvaluesplit[2]);
        } catch (Exception ex) {
            rv.setErrorkey("FIDO-ERR-0023");
            rv.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0023") + "Invalid keyid= " + keyid);
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0023"), "Invalid keyid= " + keyid);
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }

        String current_pk = sid_to_be_deleted + "-" + did + "-" + fkid_to_be_deleted;
        if(!keyid.equalsIgnoreCase(current_pk)){
            //user is not authorized to deactivate this key
            //  throw an error and return.
            rv.setErrorkey("FIDO-ERR-0035");
            rv.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0035") + " keyid= " + keyid );
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0035"), " keyid= " + keyid );
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }
        if ( fkid_to_be_deleted != null ) {
            if (fkid_to_be_deleted >= 0) {

                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute",
                        SKFSCommon.getMessageProperty("FIDO-MSG-5005"), "");
                try {
                    //  if the fkid_to_be_deleted is valid, delete the entry from the database
                    String jparesult = deletekeybean.execute(sid_to_be_deleted, did, fkid_to_be_deleted);
                    JsonObject jo;
                    try (JsonReader jr = Json.createReader(new StringReader(jparesult))) {
                        jo = jr.readObject();
                    }

                    Boolean status = jo.getBoolean(SKFSConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                    if ( !status ) {
                        //  error deleting user key
                        //  throw an error and return.
                        rv.setErrorkey("FIDO-ERR-0023");
                        rv.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0023") + "   keyid= " + keyid);
                        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0023"), "   keyid= " + keyid);
                        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
                        return rv;
                    } else {
                        //  Successfully deleted key from the database
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0028"), "key id = " + fkid_to_be_deleted);
                    }

                    if (SKFSCommon.updateFidoUsers()) {
//                        Collection<FidoKeys> keys = getkeybean.getByUsernameStatus(did, fidouser, applianceConstants.ACTIVE_STATUS);
//                        if (keys == null || keys.isEmpty()) {
//                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-5006"), "");
//                            //  Update the "FIDOKeysEnabled" attribute of the user to 'false'
//                            //  if the key that was just deleted is the last key registered
//                            //  for the user
//                            try {
//                                String result = updateldapbean.execute(did, fidouser, SKFSConstants.LDAP_ATTR_KEY_FIDOENABLED, "false", false);
//                                try (JsonReader jr = Json.createReader(new StringReader(result))) {
//                                    jo = jr.readObject();
//                                }
//                                status = jo.getBoolean(SKFSConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
//                                if (status) {
//                                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0029"), "false");
//                                } else {
//                                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0024"), "false");
//                                }
//                            } catch (SKFEException ex) {
//                                //  Do we need to return with an error at this point?
//                                //  Just throw an err msg and proceed.
//                                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0024"), "false");
//                            }
//                        }
                    }

                } catch (Exception ex) {
                    //  error deleting user key
                    //  throw an error and return.
                    rv.setErrorkey("FIDO-ERR-0023");
                    rv.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0023") + "   keyid= " + keyid);
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0023"), "   keyid= " + keyid);
                    SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
                    return rv;
                } 
            }
        } else {
            //  user key information does not exist or has been timed out (flushed away).
            //  throw an error and return.
            rv.setErrorkey("FIDO-ERR-0022");
            rv.setErrormsg(SKFSCommon.getMessageProperty("FIDO-ERR-0022") + "   keyid= " + keyid);
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0022"),"   keyid= " + keyid);
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }

        rv.setReturnval("Successfully de-registered the key");

        //  log the exit and return
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
        return rv;
    }
}
