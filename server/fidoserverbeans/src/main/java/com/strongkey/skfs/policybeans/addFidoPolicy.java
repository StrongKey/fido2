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
import com.strongkey.skfs.entitybeans.FidoPoliciesPK;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido.policyobjects.MDSAuthenticatorStatusPolicy;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
import com.strongkey.skfs.txbeans.SequenceGeneratorBeanLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;
import javax.ws.rs.core.Response;

@Stateless
public class addFidoPolicy implements addFidoPolicyLocal {

    private final String classname = this.getClass().getName();
    @Resource
    private SessionContext sc;
    @PersistenceContext
    private EntityManager em;

    @EJB
    replicateSKFEObjectBeanLocal replObj;
    @EJB
    SequenceGeneratorBeanLocal seqgenejb;

    @Override
    public Response execute(Long did, CreateFidoPolicyRequest request) {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER, classname, "execute");

        StringReader stringreader = new StringReader(request.getPolicy());
        JsonReader jsonreader = Json.createReader(stringreader);
        JsonObject policy = jsonreader.readObject();

        
        //Base64 Policy
        String policyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(policy.toString().getBytes());

        Long sid = applianceCommon.getServerId();
        Integer pid = seqgenejb.nextPolicyID();
        FidoPoliciesPK fidopolicyPK = new FidoPoliciesPK();
        FidoPolicies fidopolicy = new FidoPolicies();
        fidopolicyPK.setDid(did.shortValue());
        fidopolicyPK.setPid(pid.shortValue());
        fidopolicyPK.setSid(sid.shortValue());
        fidopolicy.setFidoPoliciesPK(fidopolicyPK);
        fidopolicy.setPolicy(policyBase64);
        fidopolicy.setStatus(request.getStatus());
        fidopolicy.setNotes(request.getNotes());
        fidopolicy.setCreateDate(new Date());

        //TODO add signing code(?)
        try {
            //add to local map
            String fpMapkey = sid + "-" + did + "-" + pid;
            FidoPolicyObject fidoPolicyObject;
                fidoPolicyObject = FidoPolicyObject.parse(
                        policyBase64,
                        did,
                        sid,
                        pid.longValue());
            MDSClient mds = null;
            

            skceMaps.getMapObj().put(SKFSConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));

            em.persist(fidopolicy);
            em.flush();
            em.clear();

            //Replicate
            String primarykey = sid + "-" + did + "-" + pid;
            if (applianceCommon.replicate()) {
                if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                    String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_ADD, primarykey, fidopolicy);
                    if (response != null) {
                        return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + response).build();
                    }
                }
            }

        } catch (SKFEException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + ex.getLocalizedMessage()).build();
        } catch (PersistenceException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDOJPA-ERR-2006", ex.getLocalizedMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-2006") + "Check server logs for details.").build();
        }


        String response = Json.createObjectBuilder()
                .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, sid + "-" + pid)
                .build().toString();

        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0063 "+ "successful addFidoPolicy output: did:" + did +" sid:"+ sid +" pid:"+pid);

        return Response.ok().entity(response).build();
    }
}
