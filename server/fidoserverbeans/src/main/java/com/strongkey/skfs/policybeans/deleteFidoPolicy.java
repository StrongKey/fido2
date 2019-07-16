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
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;

@Stateless
public class deleteFidoPolicy implements deleteFidoPolicyLocal {

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

    @SuppressWarnings("FieldMayBeFinal")
    private String classname = this.getClass().getName();

    @Override
    public Response execute(Long did, String sidpid) {

        Long sid;
        Long pid;
        try {
            sid = Long.parseLong(sidpid.split("-")[0]);
            pid = Long.parseLong(sidpid.split("-")[1]);
        } catch (Exception ex) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        FidoPolicies policy = getpolicybean.getbyPK(did, sid, pid);
        if (policy == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        em.remove(policy);
        em.flush();

        //Replicate
        String primarykey = sid + "-" + did + "-" + pid;
        if (applianceCommon.replicate()) {

            String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_DELETE, primarykey, policy);
            if (response != null) {
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + response).build();
            }
        }

        //remove from local map
        String fpMapkey = sid + "-" + did + "-" + pid;
        skceMaps.getMapObj().remove(skfsConstants.MAP_FIDO_POLICIES, fpMapkey);
        String response = Json.createObjectBuilder()
                .add(skfsConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, "Successfully deleted policy " + sid + "-" + pid)
                .build().toString();

        skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
        return Response.ok().entity(response).build();
    }
}
