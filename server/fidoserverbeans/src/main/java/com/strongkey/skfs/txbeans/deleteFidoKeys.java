/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
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
public class deleteFidoKeys implements deleteFidoKeysLocal {
 /**
     ** This class's name - used for logging & not persisted
     **/
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
    @Resource private SessionContext            sc;
    @PersistenceContext private EntityManager   em;

    /**
     *
     * @param sid
     * @param did
     * @param fkid  - The key identifier for the registered key to be deleted
     *                from the derby DB
     * @return      - Returns a JSON string containing the status and the
     *                error/success message
     */
    @Override
    public String execute(Short sid, Long did, Long fkid) {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "execute");

        //Declating variables
        Boolean status = true;
        String errmsg ;
        JsonObject retObj;

        //Input Validation

        //fkid
        //NULL Argument

        if(sid == null){
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "sid");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " sid";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        if(did == null){
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "did");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " did";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        if(fkid == null){
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "fkid");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " fkid";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if(fkid < 1){
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "fkid");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " fkid";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "fkid=" + fkid);

        //  Verify if the fkid exists.
        FidoKeys rk = null;
        try {
            rk = getregkeysejb.getByfkid(sid, did, fkid);
        } catch (SKFEException ex) {
            Logger.getLogger(deleteFidoKeys.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(rk == null){
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2002","");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-2002");
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        String primarykey = sid+"-"+did+"-"+rk.getFidoKeysPK().getUsername()+"-"+fkid;
        //delete the key
        em.remove(rk);
        em.flush();

        try {
            if(applianceCommon.replicate()) {
                if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                    String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_KEYS, applianceConstants.REPLICATION_OPERATION_DELETE, primarykey, rk);
                    if (response != null) {
                        return response;
                    }
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }

        //return a success message
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2003","");
        retObj = Json.createObjectBuilder().add("status", status).add("message", SKFSCommon.getMessageProperty("FIDOJPA-MSG-2003")).build();
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
        return retObj.toString();
    }
}
