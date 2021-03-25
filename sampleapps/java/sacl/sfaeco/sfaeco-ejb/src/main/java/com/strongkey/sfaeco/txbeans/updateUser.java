/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * Updates user information
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.Users;
import com.strongkey.sfaeco.utilities.Common;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class updateUser implements updateUserLocal {

    // Resources used by this bean
    @PersistenceContext private EntityManager   em;     // For JPA management
    @EJB private getUserLocal                   getuser;
    
    private final short sid = Common.getSid();
    private final String classname = "updateUser";
    private Users user;
    
    /****************************************************************
     *                                               888
     *                                               888
     *                                               888
     *   .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     *  d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     *  88888888   X88K   88888888 888      888  888 888    88888888
     *  Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     *   "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *
     ****************************************************************/
    
    /**
     * Updates a user's mobile device in the application database
     * @param did short containing the cryptographic domain id
     * @param svccred String with the service credential's username
     * @param uid Long value of the unique user id
     * @param status String with changed status
     * @param txid String with the transaction id
     * @return JsonObject
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    @Override
    public JsonObject updateStatus(short did, String svccred, Long uid, String status, String txid) {
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", classname, String.valueOf(did), txid);
        
        // Get the UserDevice
        user = getuser.byUid(did, uid, txid);
        if (user == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-1023", "UserDevice: " + did+"-"+uid);
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1023", "User: " + did+"-"+uid);
        }
        
        // Found it - update the status
        user.setStatus(status);
        user.setModifyDate(Common.now());
        
        try {
            // Save it
            em.merge(user);
            em.flush();
            em.clear();
        } catch (Exception ex) {
            String err = ex.getLocalizedMessage();
            if (err.startsWith("Duplicate")) {
                Common.log(Level.SEVERE, "SFAECO-ERR-1008", err);
                return Common.jsonError(classname, "execute", "SFAECO-ERR-1008", "Duplicate value exception");
            } else if (err.startsWith("Field")) {
                Common.log(Level.SEVERE, "SFAECO-ERR-1009", err);
                return Common.jsonError(classname, "execute", "SFAECO-ERR-1009", "Missing field value exception");
            } else {
                Common.log(Level.SEVERE, "SFAECO-ERR-1010", err);
                return Common.jsonError(classname, "execute", "SFAECO-ERR-1010", "Other Persistence exception");
            }
        }
        Common.log(Level.INFO, "SFAECO-MSG-2019", "STATUS=" + status);
        
        // TODO: Replicate the object to the cluster
        
        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
        return Common.jsonOk();
    }

    
}
