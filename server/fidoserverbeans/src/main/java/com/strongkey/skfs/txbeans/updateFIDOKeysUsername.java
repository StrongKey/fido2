/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc.
 *
 * $Date: $
 * $Revision: $
 * $Author: $
 * $URL: $
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.io.StringReader;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class updateFIDOKeysUsername implements updateFIDOKeysUsernameLocal {

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
    addFidoKeysLocal addFidoKeyejb;
    @EJB
    deleteFidoKeysLocal delFidoKeyejb;

    /**
     * Persistence context for derby
     */
    @Resource
    private SessionContext sc;
    @PersistenceContext
    private EntityManager em;

    
    @Override
    public String execute(Long did, String username, String newUsername, String modify_location) {
       SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "execute");

        //Declaring variables
        Boolean status = true;
        String errmsg;
        JsonObject retObj;

        //Input Validation
        
        //did
        //NULL Argument
        if (did == null) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "did");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " did";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if (did < 1) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "did");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " did";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "did=" + did);

        //Counter
        //NULL Argument
        if (newUsername == null) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "COUNTER");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " COUNTER";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "newUsername=" + newUsername);

        //USER modify_location
        if (modify_location == null) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "MODIFY LOCATION");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        } else if (modify_location.trim().length() == 0) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1003", "MODIFY LOCATION");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1003") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        } else if (modify_location.trim().length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.256charstring")) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "MODIFY LOCATION");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "MODIFY LOCATION=" + modify_location);

        try {
            //check if the new username already exsits
            Collection<FidoKeys> kh_coll_newusername = getregkeysejb.getByUsername(did, newUsername);
            if (kh_coll_newusername != null && !kh_coll_newusername.isEmpty()) {
                status = false;
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2004", "");
                errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-2004");
                retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
                return retObj.toString();
            }
            //fetch all keys and call ejb to update each of them

            Collection<FidoKeys> kh_coll = getregkeysejb.getByUsername(did, username);
            if (kh_coll != null) {
                Iterator it = kh_coll.iterator();
                while (it.hasNext()) {
                    FidoKeys fk = (FidoKeys) it.next();
                    if (fk != null) {
                        //modify the DB
                        //add a new key
                        String addkeyresponse = addFidoKeyejb.execute(did, fk.getUserid(), newUsername, fk.getKeyhandle(), fk.getPublickey(), fk.getAppid(), fk.getTransports(),
                                fk.getAttsid(), fk.getAttdid(), fk.getAttcid(), fk.getCounter(), fk.getFidoVersion(), fk.getFidoProtocol(), fk.getAaguid(), fk.getRegistrationSettings(),
                                fk.getRegistrationSettingsVersion(), modify_location);

                        JsonObject addkeyres = SKFSCommon.getJsonObjectFromString(addkeyresponse);
                        if (!addkeyres.getBoolean("status")) {
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                                    SKFSCommon.getMessageProperty("FIDO-ERR-2001"), addkeyres.getString("message"));
                            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                    + addkeyres.getString("message")));
                        }
                        // delete old key
                        String jparesult = delFidoKeyejb.execute(fk.getFidoKeysPK().getSid(), (long) fk.getFidoKeysPK().getDid(), fk.getFidoKeysPK().getFkid());
                        JsonObject jo;
                        try (JsonReader jr = Json.createReader(new StringReader(jparesult))) {
                            jo = jr.readObject();
                        }

                        Boolean delstatus = jo.getBoolean(SKFSConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                        if (!delstatus) {
                            //  error deleting user key
                            //  throw an error and return.
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                                    SKFSCommon.getMessageProperty("FIDO-ERR-0023"), " username= " + username + "   keyid= " + fk.getFidoKeysPK().getFkid());
                            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0023")
                                    + " username= " + username + "   keyid= " + fk.getFidoKeysPK().getFkid()));
                        } else {
                            //  Successfully deleted key from the database
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0028"), "key id = " + fk.getFidoKeysPK().getFkid());
                        }
                    }
                }
            }else{
                status = false;
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDO-ERR-0036", "");
                errmsg = SKFSCommon.getMessageProperty("FIDO-ERR-0036");
                retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
                return retObj.toString();
            }
        } catch (Exception ex) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0001"), " Could not parse user keys; " + ex.getLocalizedMessage());
            errmsg = SKFSCommon.getMessageProperty("FIDO-ERR-0001") + " Could not parse user keys; " + ex.getLocalizedMessage();
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        //return a success message
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2015", "");
        retObj = Json.createObjectBuilder().add("status", status).add("message", SKFSCommon.getMessageProperty("FIDOJPA-MSG-2015")).build();
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
        return retObj.toString();
    }
}
