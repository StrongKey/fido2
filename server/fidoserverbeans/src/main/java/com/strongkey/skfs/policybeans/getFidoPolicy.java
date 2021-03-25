/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Collection;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Stateless
public class getFidoPolicy implements getFidoPolicyLocal {
    
    
    /**
     * Persistence context for derby
     */
    @PersistenceContext
    private EntityManager em;

    @Override
    public Response getPolicies(Long did, Long sid, Long pid, Boolean metadataonly) {
        JsonArrayBuilder jarrbldr = Json.createArrayBuilder();
        if (sid != null & pid != null) {
            FidoPolicies fp = getbyPK(did, sid, pid);
            if (fp == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            if (metadataonly) {
                jarrbldr.add(fp.toMetaDataJson());
            } else {
                jarrbldr.add(fp.toJson());
            }
        } else {
            Collection<FidoPolicies> fpc = getbyDid(did);
            if (fpc == null) {
                return Response.status(Status.NOT_FOUND).build();
            }
            for (FidoPolicies fp : fpc) {
                if (metadataonly) {
                    jarrbldr.add(fp.toMetaDataJson());
                } else {
                    jarrbldr.add(fp.toJson());
                }
            }
        }
        String response = Json.createObjectBuilder()
            .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, jarrbldr)
            .build().toString();
        return Response.ok().entity(response).build();
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
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0063 "+  "getFidoPolicy successful output: did:" + did +" sid:"+ sid +" pid:"+pid);

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
}
