/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 * Implementation of the EJB that generates sequence numbers for objects
 * to be persisted in the database.
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.utilities.Common;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import javax.ejb.Singleton;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;

@Singleton
public class sequenceGenerator implements sequenceGeneratorLocal {

    // Local values
    private final String classname = "sequenceGenerator";
    
    // To query the database for current maximum ID values
    @PersistenceContext private EntityManager em;

    // Private map of request ID numbers based on a key with DIDs.  This allows
    // requests to get unique ID numbers faster than having to query the database.
    private static final ConcurrentMap<Short, RequestID> idmap = new ConcurrentSkipListMap<>();
    
    /**
     * A method to initialize all maps memory so we get consistent request id
     * behavior when multiple threads hit this EJB simultaneously
     * @param did Short cryptographic domain id
     * @return boolean
     */

    @Override
    public boolean initializeMaps(final Short did)
    {
        populateRequestIDMap(did);
        return true;
    }

    /*****************************************************************
                               888                 8888888 8888888b.
                               888          o        888   888  "Y88b
                               888         d8b       888   888    888
    88888b.   .d88b.  888  888 888888     d888b      888   888    888
    888 "88b d8P  Y8b `Y8bd8P' 888    "Y888888888P"  888   888    888
    888  888 88888888   X88K   888      "Y88888P"    888   888    888
    888  888 Y8b.     .d8""8b. Y88b.    d88P"Y88b    888   888  .d88P
    888  888  "Y8888  888  888  "Y888  dP"     "Yb 8888888 8888888P"

    *****************************************************************/
    /**
     * These methods return the next available ID for a specified entity
     * object.  The EJB responsible for the add() transaction calls the
     * appropriate next*ID() method and uses it to create the primary-key
     * of that object.  All methods return a Long, and with the exception
     * of the nextDomainID(), all of them require the DID as an input
     * parameter to qualify the sequence number for a specific domain.
     *
     * @param did - The ID of the domain into which the object is being added
     * @return Long - The next sequence number for this object in this domain
     */
    
   
    /**
     * UsersID
     * @param did Long value of cryptographic domain ID
     * @return Long containing the next ID
     */
    @Override
    public Long nextUsersID(final Short did)
    {
        // Check if UID is in the idmap; if so return it
        if (idmap.containsKey(did)) {
            return idmap.get(did).nextUid();
        } else {
            // Populate the map and return uid
            if (populateRequestIDMap(did) == null)
                return null;
            return idmap.get(did).nextUid();
        }
    }
    
    /**
     * UserTransactionID
     * @param did Long value of cryptographic domain ID
     * @return Long containing the next ID
     */
    @Override
    public Long nextUserTransactionID(final Short did)
    {
        // Check if UTXID is in the idmap; if so return it
        if (idmap.containsKey(did)) {
            return idmap.get(did).nextUtxid();
        } else {
            // Populate the map and return utxid
            if (populateRequestIDMap(did) == null)
                return null;
            return idmap.get(did).nextUtxid();
        }
    }
    
    /**
     * nextFarID
     * @param did Long value of cryptographic domain ID
     * @return Long containing the next ID
     */
    @Override
    public Long nextFarID(final Short did)
    {
        // Check if FARID is in the idmap; if so return it
        if (idmap.containsKey(did)) {
            return idmap.get(did).nextFarid();
        } else {
            // Populate the map and return farid
            if (populateRequestIDMap(did) == null)
                return null;
            return idmap.get(did).nextFarid();
        }
    }

/***************************************************************************************
                                    888          888             888b     d888
                                    888          888             8888b   d8888
                                    888          888             88888b.d88888
88888b.   .d88b.  88888b.  888  888 888  8888b.  888888  .d88b.  888Y88888P888  8888b.  88888b.
888 "88b d88""88b 888 "88b 888  888 888     "88b 888    d8P  Y8b 888 Y888P 888     "88b 888 "88b
888  888 888  888 888  888 888  888 888 .d888888 888    88888888 888  Y8P  888 .d888888 888  888
888 d88P Y88..88P 888 d88P Y88b 888 888 888  888 Y88b.  Y8b.     888   "   888 888  888 888 d88P
88888P"   "Y88P"  88888P"   "Y88888 888 "Y888888  "Y888  "Y8888  888       888 "Y888888 88888P"
888               888                                                                   888
888               888                                                                   888
888               888                                                                   888
***************************************************************************************/

    /**
     * A method to initialize the RequestID maps in memory so that
     * requests after the first one will get their ID value quickly.
     * Since access to the map is synchronized, there will be no
     * collisions.  If the system crashes, then the first request
     * after a restart will pick up where the last one left off since
     * this map gets its last-used value from the database.
     */
    @SuppressWarnings("UnusedAssignment")
    private RequestID populateRequestIDMap(final Short did)
    {
        // Initialize local variables
        Long uid = 1L;
        Long utxid = 1L;
        Long farid = 1L;
        short sid = Common.getSid();
        RequestID rqid = new RequestID();

        // Just checking for DID = 1 - TODO: change to domainexists(did)
        if ((did == 1))
        {
            // First, get next uid
            try {
                Query q = em.createNamedQuery("Users.maxPK");
                q.setParameter("did", did);
                q.setParameter("sid", sid);
                uid = (Long) q.getSingleResult() + 1;
            } catch (NullPointerException ex) { // First request for domain
                uid = 1L;
            }
            // Regardless of whether we find an object or not we set uid
            rqid.setUid(uid);
            
            // Get utxid
            try {
                Query q = em.createNamedQuery("UserTransactions.maxPK");
                q.setParameter("did", did);
                q.setParameter("sid", sid);
                utxid = (Long) q.getSingleResult() + 1;
            } catch (NullPointerException ex) { // First request for domain
                utxid = 1L;
            }
            // Regardless of whether we find an object or not we set utxid
            rqid.setUtxid(utxid);
            
            // Get farid
            try {
                Query q = em.createNamedQuery("UtxFidoAuthenticatorReferences.maxPK");
                q.setParameter("did", did);
                q.setParameter("sid", sid);
                farid = (Long) q.getSingleResult() + 1;
            } catch (NullPointerException ex) { // First request for domain
                farid = 1L;
            }
            // Regardless of whether we find an object or not we set farid
            rqid.setFarid(farid);

	    // Print out values to the log
            Common.log(Level.INFO, "SFAECO-MSG-1062", "DID-SID-UID="  + did + "-" + sid + "-" + uid);
            Common.log(Level.INFO, "SFAECO-MSG-1062", "DID-SID-UTXID="  + did + "-" + sid + "-" + utxid);
            Common.log(Level.INFO, "SFAECO-MSG-1062", "DID-SID-FARID="  + did + "-" + sid + "-" + farid);
            idmap.put(did, rqid);
            return idmap.get(did);
        }
        // Domain doesn't exist
        return null;
    }


/**********************************************************************************
8888888b.                                               888    8888888 8888888b.
888   Y88b                                              888      888   888  "Y88b
888    888                                              888      888   888    888
888   d88P  .d88b.   .d88888 888  888  .d88b.  .d8888b  888888   888   888    888
8888888P"  d8P  Y8b d88" 888 888  888 d8P  Y8b 88K      888      888   888    888
888 T88b   88888888 888  888 888  888 88888888 "Y8888b. 888      888   888    888
888  T88b  Y8b.     Y88b 888 Y88b 888 Y8b.          X88 Y88b.    888   888  .d88P
888   T88b  "Y8888   "Y88888  "Y88888  "Y8888   88888P'  "Y888 8888888 8888888P"
                         888
                         888
                         888
***********************************************************************************/
/**
 * Local class to maintain the ID values in a map that can be retrieved
 * rapidly for quick returns on ID values; otherwise, each call to this bean
 * will involve a database access, which will progressively take longer as
 * more requests come in.
 */
    private class RequestID
    {
        private final AtomicLong uid = new AtomicLong();
        private final AtomicLong utxid = new AtomicLong();
        private final AtomicLong farid = new AtomicLong();


        // USERS
        Long nextUid() {
            return this.uid.getAndIncrement();
        }

        void setUid(Long uid) {
            this.uid.set(uid);
        }

        // USER_TRANSACTIONS
        Long nextUtxid() {
            return this.utxid.getAndIncrement();
        }

        void setUtxid(Long utxid) {
            this.utxid.set(utxid);
        }
        
        // UTX_FIDO_AUTHENTICATOR_REFERENCES
        Long nextFarid() {
            return this.farid.getAndIncrement();
        }

        void setFarid(Long farid) {
            this.farid.set(farid);
        }
    }
}