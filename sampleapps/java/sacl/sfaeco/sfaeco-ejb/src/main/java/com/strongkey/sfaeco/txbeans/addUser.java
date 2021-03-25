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
 * Adds a new user to the USERS table in the SFAECO database.
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.Users;
import com.strongkey.sfaeco.entitybeans.UsersPK;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class addUser implements addUserLocal, addUserRemote {

    // Resources used by this bean
    @PersistenceContext private EntityManager   em;     // For JPA management
    @EJB private sequenceGeneratorLocal         seq;    // Sequence numbers
    
    private final short sid = Common.getSid();
    private final String classname = "addUser";
    private Users user;
    private UsersPK userpk;
    
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
     * Adds a user to the application database
     * @param did short containing the cryptographic domain id
     * @param svccred String with the service credential's username
     * @param userdetails String containing JsonObject of user data
     * @param txid String with the transaction id
     * @return JsonObject containing the registered user's details
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    @Override
    public JsonObject execute(short did, String svccred, String userdetails, String txid) {
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", classname, String.valueOf(did), txid);
        
        // Get userdetails in a Json object
        JsonObject userjo;
        try {
            userjo = Common.stringToJson(userdetails);
        } catch (NullPointerException npe) {
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1001", "Null USER object");
        }
        
        // Get next uid
        Long uid;
        uid = seq.nextUsersID(did);
        userpk = new UsersPK(did, sid, uid);
        
        // Create the Users object
        user = new Users(userpk);
        user.setUsername(userjo.getString(Constants.JSON_KEY_USER_USERNAME));
        user.setGivenName(userjo.getString(Constants.JSON_KEY_USER_GIVEN_NAME));
        user.setFamilyName(userjo.getString(Constants.JSON_KEY_USER_FAMILY_NAME));
        user.setEmailAddress(userjo.getString(Constants.JSON_KEY_USER_EMAIL_ADDRESS));
        user.setMobileNumber(userjo.getString(Constants.JSON_KEY_USER_MOBILE_NUMBER));
        user.setStatus(Constants.STATUS_REGISTERED);
        user.setCreateDate(Common.now());
        
        try {
        // Save it
        em.persist(user);
        em.flush();
        em.clear();
        } catch (Exception ex) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1008", ex.getLocalizedMessage());
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1008", "Duplicate value exception");
        }
        
        // TODO: Replicate the object to the cluster
        
        JsonObject response = Json.createObjectBuilder()
                .add(Constants.JSON_KEY_USER, Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_DID, userpk.getDid())
                    .add(Constants.JSON_KEY_SID, userpk.getSid())    
                    .add(Constants.JSON_KEY_UID, userpk.getUid())
                    .add(Constants.JSON_KEY_USER_USERNAME, user.getUsername())
                    .add(Constants.JSON_KEY_USER_GIVEN_NAME, user.getGivenName())
                    .add(Constants.JSON_KEY_USER_FAMILY_NAME, user.getFamilyName())
                    .add(Constants.JSON_KEY_USER_EMAIL_ADDRESS, user.getEmailAddress())
                    .add(Constants.JSON_KEY_USER_MOBILE_NUMBER, user.getMobileNumber())
                    .add(Constants.JSON_KEY_STATUS, user.getStatus())
                    .add(Constants.JSON_KEY_CREATE_DATE, user.getCreateDate().toString()))
                .build();
            
        Common.log(Level.INFO, "SFAECO-MSG-5000", response);
        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
        return response;
    }

    @Override
    public String remoteExecute(short did, String svccred, String userdetails, String txid) {
        return execute(did, svccred, userdetails, txid).toString();
    }
}
