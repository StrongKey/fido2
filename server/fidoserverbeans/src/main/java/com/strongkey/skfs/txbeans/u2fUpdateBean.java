/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.requests.PatchFidoKeyRequest;
import com.strongkey.skfs.utilities.SKCEReturnObject;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.io.StringReader;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * This EJB is responsible for executing the activation process of a specific
 * user registered key
 */
@Stateless
public class u2fUpdateBean implements u2fUpdateBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    /*
     * Enterprise Java Beans used in this EJB.
     */
    @EJB
    getFidoKeysLocal getkeybean;
    @EJB
    updateFidoKeysStatusLocal updatekeystatusbean;
    @EJB
    updateFIDO2DisplayNameLocal updateFIDO2dnejb;

    /**
     * This method is responsible for activating the user registered key from
     * the persistent storage. This method first checks if the given ramdom id
     * is mapped in memory to the specified user and if found yes, gets the
     * registration key id and then changes the key status to ACTIVE in the
     * database.
     *
     * Additionally, if the key being activated is the only one for the user in
     * ACTIVE status, the ldap attribute of the user called 'FIDOKeysEnabled' is
     * set to 'yes'.
     *
     * @param did - FIDO domain id
     * @param protocol - U2F protocol version to comply with.
     * @param username - username
     * @param randomid - random id that is unique to one fido registered
     * authenticator for the user.
     * @param modifyloc - Geographic location from where the activation is
     * happening
     * @return - returns SKCEReturnObject in both error and success cases. In
     * error case, an error key and error msg would be populated In success
     * case, a simple msg saying that the process was successful would be
     * populated.
     */
    @Override
    public SKCEReturnObject execute(Long did,
            String keyid,
            PatchFidoKeyRequest fidokey) {

        //  Log the entry and inputs
        skfsLogger.entering(skfsConstants.SKFE_LOGGER, classname, "execute");
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5001"),
                " EJB name=" + classname
                + " did=" + did
                + " keyid=" + keyid
                + " fidokey=" + fidokey);

        SKCEReturnObject skcero = new SKCEReturnObject();

        //  input checks
        if (did == null || did < 1) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " did=" + did);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " did=" + did);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        if (keyid == null || keyid.isEmpty()) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " keyid=" + keyid);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " keyid=" + keyid);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        //  3. fetch status and metadata fields from payload
        String status = fidokey.getStatus();
        if (status == null || status.isEmpty()) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " status=" + status);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " status=" + status);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        String modifyloc = fidokey.getModify_location();
        if (modifyloc == null || modifyloc.isEmpty()) {
            skcero.setErrorkey("FIDO-ERR-0002");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " modify_location=" + modifyloc);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " modify_location=" + modifyloc);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        Short sid_to_be_activated = null;
        int userfkidhyphen;
        String fidouser;
        Long fkid_to_be_activated = null;
        try {
            String[] mapvaluesplit = keyid.split("-", 3);
            sid_to_be_activated = Short.parseShort(mapvaluesplit[0]);
            userfkidhyphen = mapvaluesplit[2].lastIndexOf("-");

            fidouser = mapvaluesplit[2].substring(0, userfkidhyphen);
            fkid_to_be_activated = Long.parseLong(mapvaluesplit[2].substring(userfkidhyphen + 1));
        } catch (Exception ex) {
            skcero.setErrorkey("FIDO-ERR-0029");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0029") + "Invalid keyid= " + keyid);
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0029"), "Invalid keyid= " + keyid);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        String current_pk = sid_to_be_activated + "-" + did + "-" + fidouser + "-" + fkid_to_be_activated;
        if (!keyid.equalsIgnoreCase(current_pk)) {
            //user is not authorized to deactivate this key
            //  throw an error and return.
            skcero.setErrorkey("FIDO-ERR-0035");
            skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0035") + " username= " + fidouser);
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0035"), " username= " + fidouser);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            return skcero;
        }

        //  get the reg key id to be deleted based on the random id provided.
        if (fkid_to_be_activated >= 0) {

            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute",
                    skfsCommon.getMessageProperty("FIDO-MSG-5005"), "");
            try {
                //  if the fkid_to_be_activated is valid, delete the entry from the database
                String jparesult = updatekeystatusbean.execute(sid_to_be_activated, did, fidouser, fkid_to_be_activated, modifyloc, status);
                JsonObject jo;
                try (JsonReader jr = Json.createReader(new StringReader(jparesult))) {
                    jo = jr.readObject();
                }

                Boolean updatestatus = jo.getBoolean(skfsConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                if (!updatestatus) {
                    //  error deleting user key
                    //  throw an error and return.
                    skcero.setErrorkey("FIDO-ERR-0040");
                    skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0040") + " username= " + fidouser + "   randomid= " + fkid_to_be_activated);
                    skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0040"), " username= " + fidouser + "   randomid= " + fkid_to_be_activated);
                    skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
                    return skcero;
                }

            } catch (Exception ex) {
                //  error activating user key
                //  throw an error and return.
                skcero.setErrorkey("FIDO-ERR-0029");
                skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0029") + " username= " + fidouser + "   keyid= " + keyid);
                skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0029"), " username= " + fidouser + "   keyid= " + keyid);
                skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
                return skcero;
            }
        }

        if(fidokey.getDisplayname() != null && !fidokey.getDisplayname().trim().isEmpty()){
            //update displayname for key
            String updatednres = updateFIDO2dnejb.execute(sid_to_be_activated, did, fidouser, fkid_to_be_activated, modifyloc, fidokey.getDisplayname());
            JsonObject jo;
                try (JsonReader jr = Json.createReader(new StringReader(updatednres))) {
                    jo = jr.readObject();
                }

            Boolean updatename = jo.getBoolean("status");
            if(!updatename){
                skcero.setErrorkey("FIDO-ERR-0039");
                skcero.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0039") + " username= " + fidouser + "   keyid= " + keyid);
                skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0039"), " username= " + fidouser + "   keyid= " + keyid);
                skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
                return skcero;
            }
        }

        skcero.setReturnval("Successfully updated the key");

        //  log the exit and return
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
        return skcero;
    }
}
