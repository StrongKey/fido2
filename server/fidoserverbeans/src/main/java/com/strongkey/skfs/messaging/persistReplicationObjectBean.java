/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.messaging;

import com.strongkey.appliance.entitybeans.Servers;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.replication.entitybeans.Replication;
import com.strongkey.replication.entitybeans.ReplicationPK;
import com.strongkey.replication.txbeans.getReplicationIDLocal;
import com.strongkey.skce.utilities.skceConstants;
import com.strongkey.skfs.txbeans.getServerBeanLocal;
import com.strongkey.skfs.utilities.SKFEException;
import java.util.Collection;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@Stateless
public class persistReplicationObjectBean implements persistReplicationObjectBeanLocal, persistReplicationObjectRemote {

   // Classname for logging
    private final String classname = this.getClass().getName();

    // Resources
    @PersistenceContext private EntityManager   em;     // For JPA management
    Long SID = applianceCommon.getServerId();

    @EJB getServerBeanLocal getserver;
    @EJB getReplicationIDLocal getreplIDejb;

    Collection<Servers> subscribers = null;
     @TransactionAttribute(value = TransactionAttributeType.REQUIRED)

    @Override
    @SuppressWarnings("SleepWhileHoldingLock")
    public String execute(int objectype, int objectop, String objectpk)
    {

        //get replication id
        String repobjpk;
        try {
            subscribers = getserver.byActiveSubscribers(applianceCommon.getHostname());
        } catch (SKFEException ex) {
            Logger.getLogger(persistReplicationObjectBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "constructor", "SKCE-MSG-6003", subscribers.size());


        if (objectop != applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD && objectop != applianceConstants.REPLICATION_OPERATION_HASHMAP_UPDATE && objectop != applianceConstants.REPLICATION_OPERATION_HASHMAP_DELETE) {
            // Save a REP record for each target SID; need a new object each time
            // because JPA holds onto references for existing objects
            Long rpid = getreplIDejb.nextReplicationID(SID);
            repobjpk = SID + "-" + rpid;
            for (Servers s : subscribers) {
                // Save a Replication object for each active subscriber
                Replication replobj = new Replication();
                replobj.setReplicationPK(new ReplicationPK(SID, rpid, s.getSid()));
                replobj.setObjectype(objectype);
                replobj.setObjectop(objectop);
                replobj.setObjectpk(objectpk);
                replobj.setScheduled(new Date());
                try {
                    // Save object
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-1048", "Replication [" + replobj.getPrimaryKey() + "]");
                    em.persist(replobj);
                    em.flush();
                    em.clear();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    int i = ex.getCause().toString().indexOf("SQLIntegrityConstraintViolationException");
                    if (i != -1) {
                        // Found a duplicate
                        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6090", "Replication [" + replobj.getPrimaryKey() + "]");
                    } else {
                        return null;
                    }
                }
            }
        } else {
            repobjpk = SID.toString();
        }

        return repobjpk;
    }

    // Remote method for JUnit testing
    @Override
    public String remoteExecute(int objectype, int objectop, String objectpk) {
        return execute(objectype, objectop, objectpk);
    }
}
