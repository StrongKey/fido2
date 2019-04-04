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
import com.strongkey.skfs.entitybeans.FidoPoliciesPK;
import com.strongkey.skfs.txbeans.SequenceGeneratorBeanLocal;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.fido2mds.MDS;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceException;

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
    public Integer execute(Long did,
            Date startDate,
            Date endDate,
            String certificateProfileName,
            String Policy,
            Integer version,
            String status,
            String notes) throws SKFEException {
        skfsLogger.entering(skfsConstants.SKFE_LOGGER, classname, "execute");

        //Base64 Policy
        String policyBase64 = Base64.getUrlEncoder().withoutPadding().encodeToString(Policy.getBytes());

        Long sid = applianceCommon.getServerId();
        Integer pid = seqgenejb.nextPolicyID();
        FidoPoliciesPK fidopolicyPK = new FidoPoliciesPK();
        FidoPolicies fidopolicy = new FidoPolicies();
        fidopolicyPK.setDid(did.shortValue());
        fidopolicyPK.setPid(pid.shortValue());
        fidopolicyPK.setSid(sid.shortValue());
        fidopolicy.setFidoPoliciesPK(fidopolicyPK);
        fidopolicy.setStartDate(startDate);
        fidopolicy.setEndDate(endDate);
        fidopolicy.setCertificateProfileName(certificateProfileName);
        fidopolicy.setPolicy(policyBase64);
        fidopolicy.setVersion(version);
        fidopolicy.setStatus(status);
        fidopolicy.setNotes(notes);
        fidopolicy.setCreateDate(new Date());
        fidopolicy.setModifyDate(null);

        //TODO add signing code(?)
        try {
            em.persist(fidopolicy);
            em.flush();
            em.clear();
        } catch (PersistenceException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDOJPA-ERR-2006", ex.getLocalizedMessage());
            throw new SKFEException(skfsCommon.getMessageProperty("FIDOJPA-ERR-2006") + "Check server logs for details.");
        }

        //Replicate
        String primarykey = sid + "-" + did + "-" + pid;
        try {
            if (applianceCommon.replicate()) {

                String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_ADD, primarykey, fidopolicy);
                if (response != null) {
                    throw new SKFEException(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + response);
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }

        //add to local map
        String fpMapkey = sid + "-" + did + "-" + pid;
        FidoPolicyObject fidoPolicyObject = FidoPolicyObject.parse(
                policyBase64,
                version,
                did,
                sid,
                pid.longValue(),
                startDate,
                endDate);
        MDSClient mds = null;
        if (fidoPolicyObject.getMdsOptions() != null) {
            mds = new MDS(fidoPolicyObject.getMdsOptions().getEndpoints());
        }
        skceMaps.getMapObj().put(skfsConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));

        skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");

        return pid;
    }
}
