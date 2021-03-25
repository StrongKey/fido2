/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.requests.UpdateFidoKeyRequest;
import com.strongkey.skfs.utilities.SKCEReturnObject;
import javax.ejb.Local;

/**
 * Local interface for u2fActivateBean
 *
 */
@Local
public interface u2fUpdateBeanLocal {

    /**
     * This method is responsible for activating the user registered key from the
     * persistent storage. This method first checks if the given ramdom id is
     * mapped in memory to the specified user and if found yes, gets the registration
     * key id and then changes the key status to ACTIVE in the database.
     *
     * Additionally, if the key being activated is the only one for the user in
     * ACTIVE status, the ldap attribute of the user called 'FIDOKeysEnabled' is
     * set to 'yes'.
     *
     * @param did       - FIDO domain id
     * @param keyid  - U2F protocol version to comply with.
     * @param fidokey  -
     * @return          - returns SKCEReturnObject in both error and success cases.
     *                  In error case, an error key and error msg would be populated
     *                  In success case, a simple msg saying that the process was
     *                  successful would be populated.
     */
    SKCEReturnObject execute(Long did,
                             String keyid,
                             UpdateFidoKeyRequest fidokey);
}
