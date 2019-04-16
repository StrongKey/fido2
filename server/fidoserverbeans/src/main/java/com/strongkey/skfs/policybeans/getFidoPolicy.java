/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.utilities.skfsConstants;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.core.Response;

@Stateless
public class getFidoPolicy implements getFidoPolicyLocal {
    /**
     * Persistence context for derby
     */
    @PersistenceContext
    private EntityManager em; 
    
    @Override
    public Response getPolicies(Long did, String sidpid, Boolean metadataonly) {
        if (sidpid != null) {
            Long sid = Long.parseLong(sidpid.split("-")[0]);
            Long pid = Long.parseLong(sidpid.split("-")[1]);
            FidoPolicies fp;
            if (metadataonly) {
                fp = getMetadataByPK(did, sid, pid);
            } else {
                fp = getbyPK(did, sid, pid);
            }
            JsonObjectBuilder jo = fp.toJson();
            String response = Json.createObjectBuilder()
                .add(skfsConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, jo)
                .build().toString();
            return Response.ok().entity(response).build();
        } else {
           Collection<FidoPolicies> fpc; 
            if (metadataonly) {
                fpc = getMetadataByDid(did);
            } else {
                fpc = getbyDid(did);
            }
            JsonArrayBuilder jarrbldr = Json.createArrayBuilder(fpc);
            String response = Json.createObjectBuilder()
                .add(skfsConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, jarrbldr)
                .build().toString();
            return Response.ok().entity(response).build();
        }
    }
    
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
    public FidoPolicies getMetadataByPK(Long did, Long sid, Long pid) {
        try {
            Query q = em.createNamedQuery("FidoPolicies.findMetadataBySidDidPid");
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
    
    @Override
    public Collection<FidoPolicies> getbyDid(Long did) {
        try {
            Query q = em.createNamedQuery("FidoPolicies.findByDid");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("did", did);
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
    
    @Override
    public Collection<FidoPolicies> getMetadataByDid(Long did) {
        try {
            Query q = em.createNamedQuery("FidoPolicies.findMetadataByDid");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("did", did);
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
