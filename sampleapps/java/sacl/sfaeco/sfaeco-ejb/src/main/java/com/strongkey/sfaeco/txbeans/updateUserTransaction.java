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
 * Updates a user transaction in the database, after a business 
 * transaction has been authorized by the user within the app. Also
 * adds a FidoAuuthenticatorReference object to the DB with 
 * cryptographic details of the FIDO digital signature on the user
 * transaction. This is maintained in the business application so
 * verifications of such digital signatures can occur in the
 * business application without having to go back to the FIDO server.
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.UserTransactions;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class updateUserTransaction implements updateUserTransactionLocal {
    
     // Resources used by this bean
    @PersistenceContext private EntityManager   em;     // For JPA management
    @EJB private sequenceGeneratorLocal         seq;    // Sequence numbers
    @EJB private getUserTransactionLocal        getutx;
    @EJB private addFidoAuthenticatorReferencesLocal addfar;
    
    private final short sid = Common.getSid();
    private final String classname = "updateUserTransaction";
    private UserTransactions utx;
  
    
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
     * Updates a business transaction in the UserTransactions table after it
     * has been verified by the SKFS with the user's FIDO digital signature.
     * 
     * @param did short containing the cryptographic domain id
     * @param fidoresponse JsonObject containing the FIDO Authorize Response 
     * @param txid String with the transaction id - but this is not the UTXID
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    @Override
    public JsonObject updateTransactionDetails(short did, Long uid, String fidoresponseString, String txid) {
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", classname, String.valueOf(did), txid);
        
        // Extract Json objects from response
        JsonObject fidoresponse = Common.stringToJson(fidoresponseString);
        String response = fidoresponse.getString(Constants.JSON_KEY_FAR_RESPONSE_LABEL);
        JsonObject utxdetail = fidoresponse.getJsonObject(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXDETAIL);
        JsonArray fararray = fidoresponse.getJsonArray(Constants.JSON_KEY_FAR_LABEL);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "Number of JSON FAR in array: " + fararray.size());
        String sfatxid = utxdetail.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXID);
        
        // Retrieve the UTXID
        if (response.startsWith("Successfully")) {
            utx = getutx.byUidTransactionId(did, uid, sfatxid, txid);
            if (utx == null) {
                Common.log(Level.WARNING, "SFAECO-ERR-1023", "UserTransaction: " + did+"-"+uid+"-"+sfatxid);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
                return Common.jsonError(classname, "execute", "SFAECO-ERR-1023", "UserTransaction: " + did+"-"+uid+"-"+sfatxid);
            }
        } else {
            Common.log(Level.WARNING, "SFAECO-ERR-3011", "UserTransaction: " + did+"-"+uid+"-"+sfatxid+" ["+response+"]");
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3011", "UserTransaction Response: " + 
                    did+"-"+uid+"-"+sfatxid+" ["+response+"]");
        }
        
        // Found it - check transactions's status for "Inflight"
        String utxstatus = utx.getStatus();
        if (!utxstatus.equals(Constants.STATUS_INFLIGHT)) {
            Common.log(Level.INFO, "SFAECO-ERR-1024", "UserTransaction Status: " + did+"-"+uid+"-"+sfatxid+"-"+utxstatus);
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1024", "UserTransactions Status: " + did+"-"+uid+"-"+sfatxid+"-"+utxstatus);
        }
        Common.log(Level.INFO, "SFAECO-MSG-2023", "UserTransaction: " + did+"-"+uid+"-"+sfatxid);
        
        // Update UserTransaction
        utx.setChallenge(utxdetail.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CHALLENGE));
        utx.setNonce(utxdetail.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_NONCE));
        utx.setTxpayload(utxdetail.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXPAYLOAD));
        utx.setTxtime(utxdetail.getJsonNumber(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXTIME).longValue());
        utx.setStatus(Constants.STATUS_SUCCEEDED);
        utx.setModifyDate(Common.now());
        
        try {
        // Save it
        em.persist(utx);
        em.flush();
        em.clear();
        } catch (Exception ex) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1008", ex.getLocalizedMessage());
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1008", "Duplicate value exception");
        }
        
        // Add FidoAuthenticatorReference object(s) to DB to hold
        // cryptographic primitives of the user's digital signature affirming
        // the business transaction
        for (int i=0; i < fararray.size(); i++) {
            JsonObject farjson = fararray.getJsonObject(i);
            JsonObject farresponse = addfar.execute(did, uid, utx.getUserTransactionsPK().getUtxid(), farjson, txid);
            Common.log(Level.INFO, "SFAECO-MSG-1000", "FAR object["+i+"]: " + farresponse);
        }
        
        // TODO: Replicate the object to the cluster

        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
        return fidoresponse;
    }

}
