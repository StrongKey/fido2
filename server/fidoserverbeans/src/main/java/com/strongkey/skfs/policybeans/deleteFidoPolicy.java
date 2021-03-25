/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.policybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
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
    public Response execute(Long did, Long sid, Long pid) {

        

        FidoPolicies policy = getpolicybean.getbyPK(did, sid, pid);
        if (policy == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        em.remove(policy);
        em.flush();

        //Replicate
        String primarykey = sid + "-" + did + "-" + pid;
        if (applianceCommon.replicate()) {
            if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_DELETE, primarykey, policy);
                if (response != null) {
                    return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + response).build();
                }
            }
        }

        //remove from local map
        String fpMapkey = sid + "-" + did + "-" + pid;
        skceMaps.getMapObj().remove(SKFSConstants.MAP_FIDO_POLICIES, fpMapkey);
        String response = Json.createObjectBuilder()
                .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, "Successfully deleted policy " + sid + "-" + pid)
                .build().toString();

        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0063 "+  "successful deleteFidoPolicy output: did:" + did +" sid:"+ sid +" pid:"+pid);
        
        return Response.ok().entity(response).build();
    }
}
