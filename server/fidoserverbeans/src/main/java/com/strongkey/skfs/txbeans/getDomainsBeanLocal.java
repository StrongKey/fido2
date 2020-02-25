/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.entitybeans.Domains;
import java.util.Collection;
import javax.ejb.Local;

/**
 * Local interface to getDomainsBean EJB
 */
@Local
public interface getDomainsBeanLocal {

    /**
     * The method returns all Domain objects.
     * @return Collection<Domains>
     */
    Collection<Domains> getAll();

    /**
     * The method finds a single Domains entity based on the primary
     * key - the Domain ID.  It does not return any children objects
     * in the Domain object.
     *
     * @param did Short - the unique identifier of the Domain
     * @return Domains - the entity that identifies a Domain in the SKLES
     */
    Domains byDid(final Long did);

    /**
     * Checks if a domain entry with the did exists in the database.
     *
     * @param did   Short containing the domain id to be looked up
     * @return boolean containing the search result
     */
    boolean domainExists(final Long did);
}
