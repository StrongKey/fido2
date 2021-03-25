/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.entitybeans.Domains;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.Collection;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

/**
 * This ejb is meant to retrieve domain entries from the database.
 * There are multiple methods with different types of lookup.
 *
 */
@Stateless
public class getDomainsBean implements getDomainsBeanLocal {

    /**
     ** This class's name - used for logging
     **/
    private final String classname = this.getClass().getName();

    /**
     ** Resources used by this bean
     **/
    @PersistenceContext private EntityManager   em;         // For JPA management

    /************************************************************************
             888 888
             888 888
             888 888
     8888b.  888 888
        "88b 888 888
    .d888888 888 888
    888  888 888 888
    "Y888888 888 888
     *************************************************************************/

    /**
     * The method returns all Domain objects.
     * @return Collection of Domains objects if any; null otherwise
     */
    @Override
    public Collection<Domains> getAll() {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "getAll");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "getAll", SKFSCommon.getMessageProperty("SKCE-MSG-1023"),
                "createNamedQuery(Domains.getAll)");
        try {
            return (Collection<Domains>) em.createNamedQuery("Domains.findAll").getResultList();
        } catch (NoResultException ex) {
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "findAll");
            return null;
        }
    }

    /************************************************************************
     *
     *  888               8888888b.  d8b      888
     *  888               888  "Y88b Y8P      888
     *  888               888    888          888
     *  88888b.  888  888 888    888 888  .d88888
     *  888 "88b 888  888 888    888 888 d88" 888
     *  888  888 888  888 888    888 888 888  888
     *  888 d88P Y88b 888 888  .d88P 888 Y88b 888
     *  88888P"   "Y88888 8888888P"  888  "Y88888
     *                888
     *           Y8b d88P
     *            "Y88P"
     ************************************************************************/
    /**
     * The method finds a single Domains entity based on the primary
     * key - the Domain ID.  It does not return any children objects
     * in the Domain object.
     *
     * @param did Short - the unique identifier of the Domain
     * @return Domains - the entity that identifies a Domain in SKCE
     */
    @Override
    public Domains byDid(final Long did) {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "byDid");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "byDid", SKFSCommon.getMessageProperty("SKCE-MSG-1023"),
                "createNamedQuery(Domains.findByDid)");
        try {
            return (Domains) em.createNamedQuery("Domains.findByDid")
                    .setParameter("did", did).getSingleResult();
        } catch (NoResultException ex) {
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "byDid");
            return null;
        }
    }

    /**
     * Checks if a domain entry with the did exists in the database.
     *
     * @param did   Short containing the domain id to be looked up
     * @return boolean containing the search result
     */
    @Override
    public boolean domainExists(final Long did) {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "byDid");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "byDid", SKFSCommon.getMessageProperty("SKCE-MSG-1023"),
                "createNamedQuery(Domains.findByDid)");
        try {
            if(em.createNamedQuery("Domains.findByDid")
                    .setParameter("did", did)
                    .getSingleResult() !=null){
                return true;
            }
        } catch (NoResultException ex) {
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "byDid");
        }
        return false;
    }
}
