/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skce.pojos.MDSClient;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.fido2mds.MDS;
import com.strongkey.skfs.requests.PatchFidoPolicyRequest;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

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
                             String sidpid,
                             PatchFidoPolicyRequest request) {
        
        //get policy
        Long sid;
        Long pid;
        try {
            sid = Long.parseLong(sidpid.split("-")[0]);
            pid = Long.parseLong(sidpid.split("-")[1]);
        } catch (Exception ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        FidoPolicies fidopolicy = getpolicybean.getbyPK(did, sid, pid);
        
        if(fidopolicy == null){
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2005", "");
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        if(request.getStartDate() != null)
            fidopolicy.setStartDate(new Date(request.getStartDate()));
        if(request.getEndDate() != null)
            fidopolicy.setEndDate(new Date(request.getEndDate()));
        if(request.getVersion() != null)
            fidopolicy.setVersion(request.getVersion());
        if(request.getStatus() != null)
            fidopolicy.setStatus(request.getStatus());
        if(request.getNotes() != null)
            fidopolicy.setNotes(request.getNotes());
        if (request.getPolicy() != null) {
            JSONObject json;
            JSONObject policy;
            try {
                json = new JSONObject(request.getPolicy());
                policy = json.getJSONObject("policy");
            } catch (JSONException ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-1000", ex.getLocalizedMessage());
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(skfsCommon.getMessageProperty("FIDO-ERR-1000") + "Check server logs for details.").build();
            }
            String policyBase64 = Base64.getEncoder().encodeToString(policy.toString().getBytes());
            fidopolicy.setPolicy(policyBase64);
        }
        
        //TODO sign object
        em.merge(fidopolicy);
        em.flush();
        
        //Replicate
        String primarykey = sid + "-" + did + "-" + pid;
        if (applianceCommon.replicate()) {
            String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_UPDATE, primarykey, fidopolicy);
            if (response != null){
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + response).build();
            }
        }
        
        //Update local map
        String fpMapkey = sid + "-" + did + "-" + pid;
        FidoPolicyObject fidoPolicyObject;
        try {
            fidoPolicyObject = FidoPolicyObject.parse(
                    fidopolicy.getPolicy(),
                    fidopolicy.getVersion(),
                    (long) fidopolicy.getFidoPoliciesPK().getDid(),
                    (long) fidopolicy.getFidoPoliciesPK().getSid(),
                    (long) fidopolicy.getFidoPoliciesPK().getPid(),
                    fidopolicy.getStartDate(),
                    fidopolicy.getEndDate());
        } catch (SKFEException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + ex.getLocalizedMessage()).build();
        }
        MDSClient mds = null;
        if (fidoPolicyObject.getMdsOptions() != null) {
            mds = new MDS(fidoPolicyObject.getMdsOptions().getEndpoints());
        }
        skceMaps.getMapObj().put(skfsConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));

        String response = Json.createObjectBuilder()
                .add(skfsConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, "Successfully patched policy " + sid + "-" + pid)
                .build().toString();

        skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
        return Response.ok().entity(response).build();
    }
}
