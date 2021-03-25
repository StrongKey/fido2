/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

@SuppressWarnings("FieldMayBeFinal")
@Singleton
public class SequenceGeneratorBean implements SequenceGeneratorBeanLocal {

    /**
     * To query the database for current maximum ID values
     */
    @PersistenceContext
    private EntityManager em;

    /**
     ** Private map of request ID numbers. This allows encryption and *
     * decryption requests to get unique ID numbers faster than having * to
     * query the database.
     *
     */
    private static ConcurrentMap<Long, FIDOKeyID> idmap = new ConcurrentSkipListMap<>();

    private static ConcurrentMap<Short, PolicyID> pidmap = new ConcurrentSkipListMap<>();

    private static ConcurrentMap<Short, AttestationCertificateID> attcidmap = new ConcurrentSkipListMap<>();

    /**
     * The server id of the server executing this code.
     */
    private static Short ssid = applianceCommon.getServerId().shortValue();

    /**
     ** RequestID
     *
     * @return
     *
     */
    @Override
    synchronized public Long nextFIDOKeyID(Long did) {
        // Check if rqid is in the idmap; if so return it
        if (idmap.containsKey(did)) {
            return idmap.get(did).nextFKid();
        } else {
            // Populate the map and return rqid
            if (populateFIDOKeyIDMap(did) == null) {
                return null;
            }
            return idmap.get(did).nextFKid();
        }
    }

    @Override
    synchronized public Integer nextPolicyID() {
        // Check if pid is in the idmap; if so return it
        if (pidmap.containsKey(ssid)) {
            return pidmap.get(ssid).nextPid();
        } else {
            // Populate the map and return pid
            if (populatePolicyIDMap() == null) {
                return null;
            }
            return pidmap.get(ssid).nextPid();
        }
    }

    @Override
    synchronized public Integer nextAttestationCertificateID() {
        // Check if pid is in the idmap; if so return it
        if (attcidmap.containsKey(ssid)) {
            return attcidmap.get(ssid).nextAttcid();
        } else {
            // Populate the map and return pid
            if (populateAttestationCertificateIDMap() == null) {
                return null;
            }
            return attcidmap.get(ssid).nextAttcid();
        }
    }

    /**
     ** A method to initialize the RequestID maps in memory so that * requests
     * after the first one will get their ID value quickly. * Since access to
     * the map is synchronized, there will be no * collisions. If the system
     * crashes, then the first request * after a restart will pick up where the
     * last one left off since * this map gets its last-used value from the
     * database.
     *
     */
    synchronized private FIDOKeyID populateFIDOKeyIDMap(Long did) {
        // Initialize local variables
        Long fkid;
        FIDOKeyID Keyid = new FIDOKeyID();

        try {
            fkid = (Long) em.createNamedQuery("FidoKeys.maxpk")
                    .setParameter("sid", ssid)
                    .setParameter("did", did)
                    .setHint("javax.persistence.cache.storeMode", "REFRESH")
                    .getSingleResult() + 1;
        } catch (NullPointerException ex) { // First request for the server
            fkid = 1L;
        }

        Keyid.setFKid(fkid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.INFO, SKFSCommon.getMessageProperty("SKCE-MSG-1085"), "SID-FKID=" + ssid + "-" + fkid);

        idmap.put(did, Keyid);
        return idmap.get(did);

    }

    /**
     ** A method to initialize the RequestID maps in memory so that * requests
     * after the first one will get their ID value quickly. * Since access to
     * the map is synchronized, there will be no * collisions. If the system
     * crashes, then the first request * after a restart will pick up where the
     * last one left off since * this map gets its last-used value from the
     * database.
     *
     */
    synchronized private PolicyID populatePolicyIDMap() {
        // Initialize local variables
        Integer pid;
        PolicyID Keyid = new PolicyID();

        try {
            pid = (Integer) em.createNamedQuery("FidoPolicies.maxpid")
                    .setParameter("sid", ssid)
                    .setHint("javax.persistence.cache.storeMode", "REFRESH")
                    .getSingleResult() + 1;
        } catch (NullPointerException ex) { // First request for the server
            pid = 1;
        }

        Keyid.setPid(pid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, SKFSCommon.getMessageProperty("SKCE-MSG-1085"), "SID-PID=" + ssid + "-" + pid);

        pidmap.put(ssid, Keyid);
        return pidmap.get(ssid);
    }

    /**
     ** A method to initialize the RequestID maps in memory so that * requests
     * after the first one will get their ID value quickly. * Since access to
     * the map is synchronized, there will be no * collisions. If the system
     * crashes, then the first request * after a restart will pick up where the
     * last one left off since * this map gets its last-used value from the
     * database.
     *
     */
    synchronized private AttestationCertificateID populateAttestationCertificateIDMap() {
        // Initialize local variables
        Integer attcid;
        AttestationCertificateID Certid = new AttestationCertificateID();

        try {
            attcid = (Integer) em.createNamedQuery("AttestationCertificates.maxattcid")
                    .setParameter("sid", ssid)
                    .setHint("javax.persistence.cache.storeMode", "REFRESH")
                    .getSingleResult() + 1;
        } catch (NullPointerException ex) { // First request for the server
            attcid = 1;
        }

        Certid.setAttcid(attcid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, SKFSCommon.getMessageProperty("SKCE-MSG-1085"), "SID-ATTCID=" + ssid + "-" + attcid);

        attcidmap.put(ssid, Certid);
        return attcidmap.get(ssid);
    }
}

/**
 ** Local class to maintain the two ID values in a map that can be retrieved *
 * rapidly for quick returns on ID values; otherwise, each call to this bean *
 * will involve a database access, which will progressively take longer as *
 * more requests come in.
 */
@SuppressWarnings("FieldMayBeFinal")
class FIDOKeyID {

    private AtomicLong kid = new AtomicLong();

    // REQUEST
    Long nextFKid() {
        return this.kid.getAndIncrement();
    }

    void setFKid(Long kid) {
        this.kid.set(kid);
    }

}

/**
 ** Local class to maintain the two ID values in a map that can be retrieved *
 * rapidly for quick returns on ID values; otherwise, each call to this bean *
 * will involve a database access, which will progressively take longer as *
 * more requests come in.
 */
@SuppressWarnings("FieldMayBeFinal")
class PolicyID {

    private AtomicInteger pid = new AtomicInteger();

    // REQUEST
    Integer nextPid() {
        return this.pid.getAndIncrement();
    }

    void setPid(Integer pid) {
        this.pid.set(pid);
    }

}

/**
 ** Local class to maintain the two ID values in a map that can be retrieved *
 * rapidly for quick returns on ID values; otherwise, each call to this bean *
 * will involve a database access, which will progressively take longer as *
 * more requests come in.
 */
@SuppressWarnings("FieldMayBeFinal")
class AttestationCertificateID {

    private AtomicInteger attcid = new AtomicInteger();

    // REQUEST
    Integer nextAttcid() {
        return this.attcid.getAndIncrement();
    }

    void setAttcid(Integer attcid) {
        this.attcid.set(attcid);
    }

}
