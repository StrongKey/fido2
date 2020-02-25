/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.SKCEReturnObject;
import javax.ejb.Local;

/**
 * Local interface for u2fDeregisterBean
 *
 */
@Local
public interface u2fDeregisterBeanLocal {

    /**
     * This method is responsible for deleting the user registered key from the
     * persistent storage. This method first checks if the given random id is
     * mapped in memory to the specified user and if found yes, gets the registration
     * key id and deletes that entry from the database.
     *
     * Additionally, if the key being deleted is the last one for the user, the
     * ldap attribute of the user called 'FIDOKeysEnabled' is set to 'no'.
     *
     * @param did       - FIDO domain id
     * @param randomid  - random id that is unique to one fido registered authenticator
     *                      for the user.
     * @return          - returns SKCEReturnObject in both error and success cases.
     *                  In error case, an error key and error msg would be populated
     *                  In success case, a simple msg saying that the process was
     *                  successful would be populated.
     */
    SKCEReturnObject execute(Long did,
                             String randomid);
}
