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
 * Adds a new FidoAuthenticatorReference object to DB to hold
 * cryptographic primitives of the user's digital signature affirming
 * the business transaction
 */
package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.UtxFidoAuthenticatorReferences;
import com.strongkey.sfaeco.entitybeans.UtxFidoAuthenticatorReferencesPK;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.util.Date;
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
public class addFidoAuthenticatorReferences implements addFidoAuthenticatorReferencesLocal {

    // Resources used by this bean
    @PersistenceContext private EntityManager   em;     // For JPA management
    @EJB private sequenceGeneratorLocal         seq;    // Sequence numbers
    
    private final short sid = Common.getSid();
    private final String classname = "addUserTransaction";
    private UtxFidoAuthenticatorReferences utxfar;
    private UtxFidoAuthenticatorReferencesPK utxfarpk;
    
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
     * Adds a FidoAuthenticatorReferences object to the table to store
     * cryptographic details of the authorized transaction.
     * 
     * @param did short containing the cryptographic domain id
     * @param uid long containing the user's ID
     * @param utxid String with the user transaction ID
     * @param farjson JsonObject containing the FIDO Authorize Response 
     * JsonObject from the FIDO server
     * @param txid String with the transaction id - but this is not the UTXID
     * @return JsonObject containing the newly generated FARID
     */
    @TransactionAttribute(value = TransactionAttributeType.REQUIRED)
    @Override
    public JsonObject execute(short did, long uid, long utxid, JsonObject farjson, String txid) {
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", classname, String.valueOf(did), txid);
        
        // Get next farid
        Long farid = seq.nextFarID(did);
        utxfarpk = new UtxFidoAuthenticatorReferencesPK(did, sid, uid, utxid, farid);
        
        // Create the Users object
        utxfar = new UtxFidoAuthenticatorReferences(utxfarpk);
        utxfar.setAaguid(farjson.getString(Constants.JSON_KEY_FAR_AAGUID_LABEL));
        utxfar.setAuthTime(new Date(farjson.getJsonNumber(Constants.JSON_KEY_FAR_AUTHORIZATION_TIME_LABEL).longValue()));
        utxfar.setAuthenticatorData(farjson.getString(Constants.JSON_KEY_FAR_AUTHENTICATORDATA_LABEL));
        utxfar.setClientDataJson(farjson.getString(Constants.JSON_KEY_FAR_CLIENTDATAJSON_LABEL));
        utxfar.setFidoSignature(farjson.getString(Constants.JSON_KEY_FAR_SIGNATURE_LABEL));
        utxfar.setFidoid(farjson.getString(Constants.JSON_KEY_FAR_ID_LABEL));
        utxfar.setProtocol(farjson.getString(Constants.JSON_KEY_FAR_PROTOCOL_LABEL));
        utxfar.setRawid(farjson.getString(Constants.JSON_KEY_FAR_RAWID_LABEL));
        utxfar.setRpid(farjson.getString(Constants.JSON_KEY_FAR_RPID_LABEL));
        utxfar.setSignerPublicKey(farjson.getString(Constants.JSON_KEY_FAR_SIGNER_PUBLIC_KEY_LABEL));
        utxfar.setSigningKeyAlgorithm(farjson.getString(Constants.JSON_KEY_FAR_SIGNING_KEY_ALGORITHM_LABEL));
        utxfar.setSigningKeyType(farjson.getString(Constants.JSON_KEY_FAR_SIGNING_KEY_TYPE_LABEL));
        utxfar.setUp(farjson.getBoolean(Constants.JSON_KEY_FAR_UP_LABEL));
        utxfar.setUv(farjson.getBoolean(Constants.JSON_KEY_FAR_UV_LABEL));
        utxfar.setUsedForThisTransaction(farjson.getBoolean(Constants.JSON_KEY_FAR_USED_FOR_THIS_TRANSACTION_LABEL));
        utxfar.setUserHandle(farjson.getString(Constants.JSON_KEY_FAR_USER_HANDLE_LABEL));
        utxfar.setCreateDate(Common.now());
        
        try {
        // Save it
        em.persist(utxfar);
        em.flush();
        em.clear();
        } catch (Exception ex) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1008", ex.getLocalizedMessage());
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1008", "Duplicate value exception");
        }
        
        // TODO: Replicate the object to the cluster
        
        // Return only what is needed with the primary key of the object
        JsonObject response = Json.createObjectBuilder()
                .add(Constants.JSON_KEY_FAR_LABEL, Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_DID, did)
                    .add(Constants.JSON_KEY_SID, sid)    
                    .add(Constants.JSON_KEY_UID, uid)
                    .add(Constants.JSON_KEY_UTXID, utxid)
                    .add(Constants.JSON_KEY_FARID, farid)
                    .add(Constants.JSON_KEY_CREATE_DATE, utxfar.getCreateDate().toString()))
                .build();
            
        Common.log(Level.INFO, "SFAECO-MSG-5000", response);
        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
        return response;
    }
}
