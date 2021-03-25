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
 * Adds a user transaction to the database, thus creating a unique
 * user transaction ID that can be tracked from when the app requests
 * a FIDO challenge to authorize a business transaction
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.UserTransactions;
import com.strongkey.sfaeco.entitybeans.UserTransactionsPK;
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
public class addUserTransaction implements addUserTransactionLocal {
    
    // Resources used by this bean
    @PersistenceContext private EntityManager   em;     // For JPA management
    @EJB private sequenceGeneratorLocal         seq;    // Sequence numbers
    
    private final short sid = Common.getSid();
    private final String classname = "addUserTransaction";
    private UserTransactions utx;
    private UserTransactionsPK utxpk;
    
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
     * Adds a business transaction to the UserTransactions table to generate
     * a new UserTransaction ID (UTXID) and to derive a unqiue challenge based
     * on the payload sent by the Android app using SACL. Once the transaction
     * is authorized by the utx, and verified by SKFS, more details will be 
     * persisted in the UserTransaction table through an update. 
     * 
     * @param did short containing the cryptographic domain id
     * @param fazresponse String containing the FIDO Authorize Response 
     * JsonObject of utx transaction data from the FIDO server
     * @param txid String with the transaction id - but this is not the UTXID
     * @return JsonObject containing the newly generated UTXID and challenge
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    @Override
    public JsonObject execute(short did, long uid, JsonObject jsoncart, String txid) {
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", classname, String.valueOf(did), txid);
        
        // Get next utxid
        Long utxid;
        utxid = seq.nextUserTransactionID(did);
        utxpk = new UserTransactionsPK(did, sid, uid, utxid);
        
        // Get some JsonObject from jsoncart
        JsonObject cart = jsoncart.getJsonObject(Constants.SFA_ECO_CART_LABEL);
        JsonObject paymethod = cart.getJsonObject(Constants.SFA_ECO_CART_PAYMENT_METHOD_LABEL);
        
        // Create the Users object
        utx = new UserTransactions(utxpk);
        utx.setTxid(Constants.SFA_ECO_LABEL.concat("-").concat(Long.toString(utxid)));
        utx.setMerchantId((short) cart.getInt(Constants.SFA_ECO_CART_MERCHANT_ID_LABEL));
        utx.setTotalProducts((short) cart.getInt(Constants.SFA_ECO_CART_TOTAL_PRODUCTS_LABEL));
        utx.setTotalPrice(cart.getInt(Constants.SFA_ECO_CART_TOTAL_PRICE_LABEL));
        utx.setCurrency(cart.getString(Constants.SFA_ECO_CART_CURRENCY_LABEL));
        utx.setPaymentBrand(paymethod.getString(Constants.SFA_ECO_CART_PAYMENT_METHOD_CARD_BRAND_LABEL));
        utx.setPaymentCardNumber(paymethod.getString(Constants.SFA_ECO_CART_PAYMENT_METHOD_CARD_LAST4_LABEL));
        utx.setStatus(Constants.STATUS_INFLIGHT);
        utx.setCreateDate(Common.now());
        
        try {
        // Save it
        em.persist(utx);
        em.flush();
        em.clear();
        } catch (Exception ex) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1008", ex.getLocalizedMessage());
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1008", "Duplicate value exception");
        }
        
        // TODO: Replicate the object to the cluster
        
        // Return only what is needed for servlet to call the FIDO preauthorize service
        JsonObject response = Json.createObjectBuilder()
                .add(Constants.JSON_KEY_USER_TRANSACTION, Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_DID, utxpk.getDid())
                    .add(Constants.JSON_KEY_SID, utxpk.getSid())    
                    .add(Constants.JSON_KEY_UID, utxpk.getUid())
                    .add(Constants.JSON_KEY_UTXID, utxpk.getUtxid())
                    // This txid is different from the txid used to track TTC in code
                    .add(Constants.JSON_KEY_TXID, utx.getTxid())
                    .add(Constants.JSON_KEY_STATUS, utx.getStatus())
                    .add(Constants.JSON_KEY_CREATE_DATE, utx.getCreateDate().toString()))
                .build();
            
        Common.log(Level.INFO, "SFAECO-MSG-5000", response);
        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
        return response;
    }
 
}
