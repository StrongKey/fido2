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
 * Local interface to EJB
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.UserTransactions;
import com.strongkey.sfaeco.entitybeans.Users;
import com.strongkey.sfaeco.utilities.Common;
import java.util.Collection;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class getUserTransaction implements getUserTransactionLocal {

    String CLASSNAME = "getUserTransaction";
    
    // Resources used by this bean
    @PersistenceContext private EntityManager   em;
    @EJB
    private getUtxFidoAuthenticatorReferencesLocal getAuthenticatorReferences;
    /**
     * Returns a UserTransaction object based on did, uid and utxid
     *
     * @param did Long Domain ID
     * @param uid Long User ID
     * @param utxid String
     * @param txid String for performance logging
     * @return UserTransactions object
     */
    
    @Override
    public UserTransactions byUidTransactionId(short did, Long uid, String sfatxid, String txid) {
        
        String methodname = CLASSNAME.concat("byUidTransactionId");
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        try {
            TypedQuery<UserTransactions> q = em.createNamedQuery("UserTransactions.findByDidUidTxid", UserTransactions.class);
            q.setParameter("did", did);
            q.setParameter("uid", uid);
            q.setParameter("txid", sfatxid);
            UserTransactions userTransaction = q.getSingleResult();
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return userTransaction;
        } catch (NoResultException ex) {
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return null;
        }
    }
      @Override
    public String getAll() {
         try {
            TypedQuery<UserTransactions> q = em.createNamedQuery("UserTransactions.findAll", UserTransactions.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            Collection<UserTransactions> userTxColl = q.getResultList();
             JsonArrayBuilder jarr = Json.createArrayBuilder();
            if (!userTxColl.isEmpty()) {
                for (UserTransactions userTx : userTxColl) {
                    if (userTx != null) {
                        JsonArray authRefJarr= getAuthenticatorReferences.byId(userTx.getUserTransactionsPK().getDid(), userTx.getUserTransactionsPK().getUid(), userTx.getUserTransactionsPK().getUtxid());
                        System.out.println("-------------->"+authRefJarr.toString());
                      //Users.findByPK
                        TypedQuery<Users> q1 = em.createNamedQuery("Users.findByPK",Users.class);
                        q1.setHint("javax.persistence.cache.storeMode", "REFRESH");
                        q1.setParameter("did", userTx.getUserTransactionsPK().getDid());
                        q1.setParameter("sid", userTx.getUserTransactionsPK().getSid());
                        q1.setParameter("uid", userTx.getUserTransactionsPK().getUid());
                        Users user=q1.getSingleResult();
                        System.out.println(user.getUsername());
                        String username= user.getUsername();

                        jarr.add(userTx.toJsonObject(authRefJarr,username));
                    }
                }
            }
            return jarr.build().toString();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
