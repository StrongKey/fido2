/**
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License, as published by the Free Software Foundation and
 *  available at http://www.fsf.org/licensing/licenses/lgpl.html,
 *  version 2.1.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
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
 * Adds products in the user's cart to the database
 */
package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.UtxProducts;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class addUserProducts implements addUserProductsLocal {

    // Resources used by this bean
    @PersistenceContext private EntityManager   em;     // For JPA management
    
    private final short sid = Common.getSid();
    private final String classname = "addUserProducts";
    private UtxProducts utxp;
    
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
     * Adds products the user is planning to buy to the UtxProducts table
     * 
     * @param did short containing the cryptographic domain id
     * @param sid short containing the server id
     * @param uid long containing the user id
     * JsonObject of utx transaction data from the FIDO server
     * @param txid String with the transaction id - but this is not the UTXID
     * @return JsonObject containing the newly generated UTXID and challenge
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    @Override
    public JsonObject execute(short did, long uid, long utxid, JsonObject jsoncart, String txid) {
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", classname, String.valueOf(did), txid);
        
        // Get the products from the cart
        JsonObject cart = jsoncart.getJsonObject(Constants.SFA_ECO_CART_LABEL);
        JsonArray products = cart.getJsonArray(Constants.SFA_ECO_CART_PRODUCTS_LABEL);
        int size = products.size();
        
        // Add products to the DB
        for (int i = 0; i < size; i++) {
            JsonObject prodjo =  products.getJsonObject(i);
            short productId = (short)prodjo.getInt(Constants.SFA_ECO_CART_PRODUCT_ID_LABEL);
            
            // Create the Utxobject after creating its
            utxp = new UtxProducts(did, sid, uid, utxid, productId);
            utxp.setProductName(prodjo.getString(Constants.SFA_ECO_CART_PRODUCT_NAME_LABEL));
            utxp.setProductPrice(prodjo.getInt(Constants.SFA_ECO_CART_PRODUCT_PRICE_LABEL));
            utxp.setCreateDate(Common.now());

            try {
            // Save it
            em.persist(utxp);
            em.flush();
            em.clear();
            } catch (Exception ex) {
                Common.log(Level.SEVERE, "SFAECO-ERR-1008", ex.getLocalizedMessage());
                return null;
            }
        }
        
        // TODO: Replicate the object to the cluster        
        Common.log(Level.INFO, "SFAECO-MSG-5000", products);
        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
        return jsoncart;
    }
}
