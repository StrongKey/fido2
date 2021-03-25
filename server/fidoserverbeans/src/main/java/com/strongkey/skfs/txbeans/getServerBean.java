/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.entitybeans.Servers;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Collection;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;


@Stateless
public class getServerBean implements getServerBeanLocal {

    /**
     ** This class's name - used for logging & not persisted
     **/
    private final String classname = this.getClass().getName();

        /**
     ** Resources used by this bean
     **/
    @PersistenceContext private EntityManager   em;         // For JPA management

    /**
     * The method finds a Collection of Servers that are active subscribers and NOT this FQDN
     *
     * @param fqdn String the unique name of the Server to exclude from the search
     * @return Collection - a collection of active SAKA subscribers
     * @throws com.strongkey.skce.utilities.SKFEException
     */
    @Override
     public Collection<Servers> byActiveSubscribers(String fqdn) throws SKFEException {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "byActiveSubscribers");
        try {
            TypedQuery<Servers> q = em.createNamedQuery("Servers.findByActiveSubscribers", Servers.class);
            q.setParameter("fqdn", fqdn);
            return q.getResultList();
        } catch (NoResultException ex) {
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "byActiveSubscribers");
            return null;
        }
    }

     /**
     * The method finds a single Servers entity based on the name of the
     * Server.
     *
     * @param name String the unique name of the Server
     * @return Servers - a Server in the enterprise
     */
    @Override
     public Servers byFqdn(String fqdn) throws SKFEException {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "byFqdn");
        try {
            return (Servers) em.createNamedQuery("Servers.findByFqdn").setParameter("fqdn", fqdn).getSingleResult();
        } catch (NoResultException ex) {
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "byFqdn");
            return null;
        }
    }
}
