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
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class updateFidoPolicy implements updateFidoPolicyLocal {

    @SuppressWarnings("FieldMayBeFinal")
    private String classname = this.getClass().getName();
    
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
    public void excecute(Long did,
                    Long pid,
                    Date startDate, 
                    Date endDate,
                    String policy, 
                    Integer version, 
                    String status, 
                    String notes) throws SKFEException{
        
        //get policy
        long sid = applianceCommon.getServerId();
        FidoPolicies fidopolicy = getpolicybean.getbyPK(did, sid, pid);
        
        if(fidopolicy == null){
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2005", "");
            throw new SKFEException(skfsCommon.getMessageProperty("FIDOJPA-ERR-2005"));
        }
        if(startDate != null)
            fidopolicy.setStartDate(startDate);
        if(endDate != null)
            fidopolicy.setEndDate(endDate);
        if(version != null)
            fidopolicy.setVersion(version);
        if(status != null)
            fidopolicy.setStatus(status);
        if(notes != null)
            fidopolicy.setNotes(notes);
        if (policy != null) {
            String policyBase64 = Base64.getEncoder().encodeToString(policy.getBytes());
            fidopolicy.setPolicy(policyBase64);
        }
        
        //TODO sign object
        em.merge(fidopolicy);
        em.flush();
        
        //Replicate
        String primarykey = sid + "-" + did + "-" + pid;
        try {
            if (applianceCommon.replicate()) {
                String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, applianceConstants.REPLICATION_OPERATION_UPDATE, primarykey, fidopolicy);
                if(response != null){
                    throw new SKFEException(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + response);
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }
        
        //Update local map
        String fpMapkey = sid + "-" + did + "-" + pid;
        FidoPolicyObject fidoPolicyObject = FidoPolicyObject.parse(
                fidopolicy.getPolicy(),
                fidopolicy.getVersion(),
                (long) fidopolicy.getFidoPoliciesPK().getDid(),
                (long) fidopolicy.getFidoPoliciesPK().getSid(),
                (long) fidopolicy.getFidoPoliciesPK().getPid(),
                fidopolicy.getStartDate(),
                fidopolicy.getEndDate());
        MDSClient mds = null;
        if (fidoPolicyObject.getMdsOptions() != null) {
            mds = new MDS(fidoPolicyObject.getMdsOptions().getEndpoints());
        }
        skceMaps.getMapObj().put(skfsConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));
    }
}
