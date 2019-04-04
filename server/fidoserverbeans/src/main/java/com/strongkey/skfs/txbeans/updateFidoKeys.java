/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skce.utilities.skceMaps;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class updateFidoKeys implements updateFidoKeysLocal {

    /**
     ** This class's name - used for logging & not persisted
     *
     */
    @SuppressWarnings("FieldMayBeFinal")
    private String classname = this.getClass().getName();

    /**
     * EJB's used by the Bean
     */
    @EJB
    getFidoKeysLocal getregkeysejb;
    @EJB
    replicateSKFEObjectBeanLocal replObj;

    /**
     * Persistence context for derby
     */
    @Resource
    private SessionContext sc;
    @PersistenceContext
    private EntityManager em;

    /**
     *
     * @param sid
     * @param did
     * @param username
     * @param fkid - Unique identifier for the key in the DB
     * @param newCounter - New counter value for the key being used
     * @param modify_location - Location where the key was last used.
     * @return - Returns a JSON string containing the status and the
     * error/success message
     */
    @Override
    public String execute(Short sid, Long did, String username, Long fkid, Integer newCounter, String modify_location) {
        skfsLogger.entering(skfsConstants.SKFE_LOGGER,classname, "execute");

        //Declaring variables
        Boolean status = true;
        String errmsg;
        JsonObject retObj;

        //Input Validation
        //sid
        //NULL Argument
        if (sid == null) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "sid");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + " sid";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if (sid < 1) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "sid");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1002") + " sid";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "sid=" + sid);

        //did
        //NULL Argument
        if (did == null) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "did");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + " did";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if (did < 1) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "did");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1002") + " did";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "did=" + did);

        //fkid
        //NULL Argument
        if (fkid == null) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "fkid");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + " fkid";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if (fkid < 1) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "fkid");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1002") + " fkid";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "fkid=" + fkid);

        //Counter
        //NULL Argument
        if (newCounter == null) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "COUNTER");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + " COUNTER";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        // Counter is negative, zero or larger than max value (becomes negative)
        if (newCounter < 1) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "COUNTER");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1002") + " COUNTER";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "COUNTER=" + newCounter);

        //USER modify_location
        if (modify_location == null) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "MODIFY LOCATION");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        } else if (modify_location.trim().length() == 0) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1003", "MODIFY LOCATION");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1003") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        } else if (modify_location.trim().length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.256charstring")) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "MODIFY LOCATION");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-1002") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "MODIFY LOCATION=" + modify_location);

        //  Verify if the fkid exists.
        FidoKeys rk = null;
        try {
            FidoKeysInfo fkinfo = (FidoKeysInfo) skceMaps.getMapObj().get(skfsConstants.MAP_FIDO_KEYS, sid + "-" + did + "-" + username + "-" + fkid);
            if (fkinfo != null) {
                rk = fkinfo.getFk();
            }
            if (rk == null) {
                rk = getregkeysejb.getByfkid(sid, did, username, fkid);
            }
        } catch (SKFEException ex) {
            Logger.getLogger(updateFidoKeys.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (rk == null) {
            status = false;
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2002", "");
            errmsg = skfsCommon.getMessageProperty("FIDOJPA-ERR-2002");
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        //modify the DB
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String moddate = df.format(new Date());
        Date modifyDateFormat = null;
        try {
            modifyDateFormat = df
                    .parse(moddate);
        } catch (ParseException e) {
        }
        String primarykey = sid + "-" + did + "-" + rk.getFidoKeysPK().getUsername() + "-" + fkid;
        rk.setCounter(newCounter);
        rk.setModifyLocation(modify_location);
        rk.setModifyDate(modifyDateFormat);
        rk.setId(primarykey);

        em.merge(rk);
        em.flush();

        try {
            if (applianceCommon.replicate()) {
                String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_KEYS, applianceConstants.REPLICATION_OPERATION_UPDATE, primarykey, rk);
                if(response != null){
                    return response;
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }

        FidoKeysInfo fkinfoObj = new FidoKeysInfo(rk);
        skceMaps.getMapObj().put(skfsConstants.MAP_FIDO_KEYS, primarykey, fkinfoObj);

        //return a success message
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2004", "");
        retObj = Json.createObjectBuilder().add("status", status).add("message", skfsCommon.getMessageProperty("FIDOJPA-MSG-2004")).build();
        skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
        return retObj.toString();
    }
}
