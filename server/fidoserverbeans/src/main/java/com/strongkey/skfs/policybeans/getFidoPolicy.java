/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.entitybeans.FidoPolicies;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Stateless
public class getFidoPolicy implements getFidoPolicyLocal {
    /**
     * Persistence context for derby
     */
    @PersistenceContext
    private EntityManager em; 
    
    
    @Override
    public FidoPolicies getbyPK(Long did, Long sid, Long pid) {
        try {
            Query q = em.createNamedQuery("FidoPolicies.findBySidDidPid");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("did", did);
            q.setParameter("sid", sid);
            q.setParameter("pid", pid);
            FidoPolicies fp = (FidoPolicies) q.getSingleResult();
//            if (fp != null) {                       //TODO verify signature
//                verifyDBRecordSignature(did, fp);
//            }
            return fp;
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public Collection<FidoPolicies> getAllActive() {
        try {
            Query q = em.createNamedQuery("FidoPolicies.findByStatus");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("status", "Active");
            Collection<FidoPolicies> fidoPoliciesColl = q.getResultList();
            Collection<FidoPolicies> validPoliciesColl = fidoPoliciesColl;
//            if(!fidoPoliciesColl.isEmpty()){      //TODO verify signature
//                for (FidoPolicies fp : fidoPoliciesColl) {
//                    if(fp!=null){
//                        try {
//                            verifyDBRecordSignature(did, fp);
//                        } catch (SKFEException ex) {
//                            validPoliciesColl.remove(fp);
//                        }
//                    }
//                }
//            }
            return validPoliciesColl;
        } catch (NoResultException ex) {
            return null;
        }
    }
}
