/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.policybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skce.pojos.MDSClient;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.skfs.requests.PatchFidoPolicyRequest;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Base64;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;

@Stateless
public class updateFidoPolicy implements updateFidoPolicyLocal {

    private final String classname = this.getClass().getName();

    @EJB
    getFidoPolicyLocal getpolicybean;
    @EJB
    replicateSKFEObjectBeanLocal replObj;

    /**
     * Persistence context for derby
     */
    @Resource
    private SessionContext sc;
    @PersistenceContext
    private EntityManager em;

    @Override
    public Response execute(Long did,
                             Long sid,
                             Long pid,
                             PatchFidoPolicyRequest request) {

        //get policy
        FidoPolicies fidopolicy = getpolicybean.getbyPK(did, sid, pid);

        if(fidopolicy == null){
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2005", "");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(request.getStatus() != null)
            fidopolicy.setStatus(request.getStatus());
        if(request.getNotes() != null)
            fidopolicy.setNotes(request.getNotes());
        if (request.getPolicy() != null) {
              //This code block would truncate the attestation formats to only packed and tpm if unique aaguids are present in allowedAaguidss
//            JsonObject policy;
//            try {
//                policy = request.getPolicy();
//            } catch (Exception ex) {
//                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5011", ex.getLocalizedMessage());
//                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDO-ERR-5011") + "Check server logs for details.").build();
//            } 
//            try {
//                if(!policy.getJsonObject("trusted_authenticators").getJsonArray("aaguids").isEmpty()){
//                    JsonObject cyptoPolicy = policy.getJsonObject("cryptography");
//                    List<String> oldAttFmt = Arrays.asList(cyptoPolicy.getJsonArray("attestation_formats").toString().split(","));
//                    ArrayList<String> newAttFmt = new ArrayList<>();
//                    ArrayList<String> removedAttFmt = new ArrayList<>();
//                    if(cyptoPolicy.getJsonArray("attestation_formats").toString().contains("\"packed\"")){
//                        newAttFmt.add("packed");
//                    }
//                    if(cyptoPolicy.getJsonArray("attestation_formats").toString().contains("\"tpm\"")){
//                        newAttFmt.add("tpm");
//                    }
//                    if(newAttFmt.isEmpty()){
//                            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDO-ERR-5011") + "Attestation formats must be include 'packed' and/or 'tpm' when restricting Athenticator AAGUIDS").build();
//                    }
//                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0063 -"+"newAttFmt " + newAttFmt);
//                    cyptoPolicy.put("attestation_formats", newAttFmt);
//                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0063 -"+"cyptoPolicy " + cyptoPolicy);
//                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0063 -"+"Attestation formats " + removedAttFmt+ " were removed due to AAGUIDS specification");
//
//                    policy.put("cryptography", cyptoPolicy);
//                }
//            } catch (Exception ex) {
//                Logger.getLogger(updateFidoPolicy.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            String policyBase64 = Base64.getEncoder().encodeToString(policy.toString().getBytes());
            String policyBase64 = Base64.getEncoder().encodeToString(request.getPolicy().getBytes());
            fidopolicy.setPolicy(policyBase64);
        }

        //TODO sign object
        em.merge(fidopolicy);
        em.flush();

        //Replicate
        String primarykey = sid + "-" + did + "-" + pid;
        if (applianceCommon.replicate()) {
            if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_UPDATE, primarykey, fidopolicy);
                if (response != null) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + response).build();
                }
            }
        }

        //Update local map
        String fpMapkey = sid + "-" + did + "-" + pid;
        FidoPolicyObject fidoPolicyObject;
        try {
            fidoPolicyObject = FidoPolicyObject.parse(
                    fidopolicy.getPolicy(),
                    (long) fidopolicy.getFidoPoliciesPK().getDid(),
                    (long) fidopolicy.getFidoPoliciesPK().getSid(),
                    (long) fidopolicy.getFidoPoliciesPK().getPid());
        } catch (SKFEException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + ex.getLocalizedMessage()).build();
        }
        MDSClient mds = null;
        skceMaps.getMapObj().put(SKFSConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));

        String response = Json.createObjectBuilder()
                .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, "Successfully patched policy " + sid + "-" + pid)
                .build().toString();

        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0063 "+  "updateFidoPolicy successful output: did:" + did +" sid:"+ sid +" pid:"+pid);

        return Response.ok().entity(response).build();
    }
}
