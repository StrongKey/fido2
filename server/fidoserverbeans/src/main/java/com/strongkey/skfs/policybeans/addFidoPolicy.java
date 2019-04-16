/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
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
import com.strongkey.skfs.entitybeans.FidoPoliciesPK;
import com.strongkey.skfs.txbeans.SequenceGeneratorBeanLocal;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.fido2mds.MDS;
import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
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
        skfsLogger.entering(skfsConstants.SKFE_LOGGER, classname, "execute");

        //Base64 Policy
        String policyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(request.getPolicy().getBytes());

        Long sid = applianceCommon.getServerId();
        Integer pid = seqgenejb.nextPolicyID();
        FidoPoliciesPK fidopolicyPK = new FidoPoliciesPK();
        FidoPolicies fidopolicy = new FidoPolicies();
        fidopolicyPK.setDid(did.shortValue());
        fidopolicyPK.setPid(pid.shortValue());
        fidopolicyPK.setSid(sid.shortValue());
        fidopolicy.setFidoPoliciesPK(fidopolicyPK);
        fidopolicy.setStartDate(request.getStartDate());
        fidopolicy.setEndDate(request.getEndDate());
        fidopolicy.setCertificateProfileName(request.getCertificateProfileName());
        fidopolicy.setPolicy(policyBase64);
        fidopolicy.setVersion(request.getVersion());
        fidopolicy.setStatus(request.getStatus());
        fidopolicy.setNotes(request.getNotes());
        fidopolicy.setCreateDate(new Date());
        fidopolicy.setModifyDate(null);

        //TODO add signing code(?)
        try {
            em.persist(fidopolicy);
            em.flush();
            em.clear();

            //Replicate
            String primarykey = sid + "-" + did + "-" + pid;
            if (applianceCommon.replicate()) {

                String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_ADD, primarykey, fidopolicy);
                if (response != null) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + response).build();
                }
            }

            //add to local map
            String fpMapkey = sid + "-" + did + "-" + pid;
            FidoPolicyObject fidoPolicyObject;
                fidoPolicyObject = FidoPolicyObject.parse(
                        policyBase64,
                        request.getVersion(),
                        did,
                        sid,
                        pid.longValue(),
                        request.getStartDate(),
                        request.getEndDate());
            MDSClient mds = null;
            if (fidoPolicyObject.getMdsOptions() != null) {
                mds = new MDS(fidoPolicyObject.getMdsOptions().getEndpoints());
            }
            skceMaps.getMapObj().put(skfsConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));

        } catch (SKFEException ex) {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + ex.getLocalizedMessage()).build();
        } catch (PersistenceException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDOJPA-ERR-2006", ex.getLocalizedMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(skfsCommon.getMessageProperty("FIDOJPA-ERR-2006") + "Check server logs for details.").build();
        }


        String response = Json.createObjectBuilder()
                .add(skfsConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, sid + "-" + pid)
                .build().toString();

        skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
        return Response.ok().entity(response).build();
    }
}
