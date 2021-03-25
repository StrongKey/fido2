/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.UtxFidoAuthenticatorReferences;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

/**
 *
 * @author root
 */
@Stateless
public class getUtxFidoAuthenticatorReferences implements getUtxFidoAuthenticatorReferencesLocal {

    // Add business logic below. (Right-click in editor and choose
    // "Insert Code > Add Business Method")
     @PersistenceContext
    private EntityManager em;
      
      
      
      public JsonArray byId(short did, long uid, long utxid) {
         try {
            TypedQuery<UtxFidoAuthenticatorReferences> q = em.createNamedQuery("UtxFidoAuthenticatorReferences.findByUserTransaction", UtxFidoAuthenticatorReferences.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("did", did);
            q.setParameter("uid", uid);
            q.setParameter("utxid", utxid);

                        
            Collection<UtxFidoAuthenticatorReferences> userTxFidoAuthRefColl = q.getResultList();
             System.out.println(userTxFidoAuthRefColl.size());
             JsonArrayBuilder jarr = Json.createArrayBuilder();
            if (!userTxFidoAuthRefColl.isEmpty()) {
                for (UtxFidoAuthenticatorReferences userTxFidoAuthRef : userTxFidoAuthRefColl) {
                    if (userTxFidoAuthRef != null) {
                        jarr.add(userTxFidoAuthRef.toJsonObject());
                    }
                }
            }
            return jarr.build();
        } catch (NoResultException ex) {
            return null;
        }
    }
}
