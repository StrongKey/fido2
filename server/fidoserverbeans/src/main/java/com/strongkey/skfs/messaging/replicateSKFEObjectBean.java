/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.messaging;

import com.strongkey.appliance.entitybeans.Configurations;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skce.utilities.skceConstants;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.entitybeans.AttestationCertificates;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.entitybeans.FidoUsers;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.LocalBean;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;

@Stateless
@LocalBean
public class replicateSKFEObjectBean implements replicateSKFEObjectBeanLocal {

    /**
     ** This class's name - used for logging & not persisted
     *
     */
    private final String classname = this.getClass().getName();

    @EJB
    publishSKCEObjectLocal publishSKCEObj;
    @EJB
    persistReplicationObjectBeanLocal persistro;

    @Resource
    private SessionContext sc;

    @Override
    public String execute(Integer entityType, Integer replicationOperation, String primarykey, Object obj) {

        //Json return object
        JsonObject retObj;

        //Declaring variables
        Boolean status = true;
        String errmsg;

        try {
            String replpk = persistro.execute(entityType, replicationOperation, primarykey);
            if (replpk == null) {
                sc.setRollbackOnly();
                strongkeyLogger.exiting(skceConstants.SKEE_LOGGER, classname, "execute");
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "persist repl object");
                errmsg = skceCommon.getMessageProperty("FIDOJPA-ERR-1001") + " persist repl object";
                retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
                return retObj.toString();
            }
            switch (entityType) {
                case applianceConstants.ENTITY_TYPE_FIDO_KEYS:
                    FidoKeys fk = (FidoKeys) obj;
                    publishSKCEObj.execute(replpk, entityType, replicationOperation, primarykey, fk);
                    break;
                case applianceConstants.ENTITY_TYPE_FIDO_POLICIES:
                    FidoPolicies fp = (FidoPolicies) obj;
                    publishSKCEObj.execute(replpk, entityType, replicationOperation, primarykey, fp);
                    break;
                case applianceConstants.ENTITY_TYPE_ATTESTATION_CERTIFICATES:
                    AttestationCertificates ak = (AttestationCertificates) obj;
                    publishSKCEObj.execute(replpk, entityType, replicationOperation, primarykey, ak);
                    break;
                case applianceConstants.ENTITY_TYPE_FIDO_USERS:
                    FidoUsers user = (FidoUsers) obj;
                    publishSKCEObj.execute(replpk, entityType, replicationOperation, primarykey, user);
                    break;
                case applianceConstants.ENTITY_TYPE_FIDO_CONFIGURATIONS:
                    Configurations config = (Configurations) obj;
                    publishSKCEObj.execute(replpk, entityType, replicationOperation, primarykey, config);
                    break;
                case applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO:
                    UserSessionInfo session = (UserSessionInfo) obj;
                    publishSKCEObj.execute(replpk, entityType, replicationOperation, primarykey, session);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            strongkeyLogger.exiting(skceConstants.SKEE_LOGGER, classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }
        return null;
    }
}
