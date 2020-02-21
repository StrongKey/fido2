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
 * Local interface for u2fGetKeysInfoBean
 *
 */
@Local
public interface u2fGetKeysInfoBeanLocal {

    /**
     * This method is responsible for fetching the user registered key from the
     * persistent storage and return back the metadata.
     *
     * If the user has registered multiple fido authenticators, this method will
     * return an array of registered key metadata, each entry mapped to a random id.
     * These random ids have a 'ttl (time-to-live)' associated with them. The client
     * applications have to cache these random ids if they wish to de-register keys.
     *
     * @param did       - FIDO domain id
     * @param username  - username
     * @return          - returns SKCEReturnObject in both error and success cases.
     *                  In error case, an error key and error msg would be populated
     *                  In success case, a simple msg saying that the process was
     *                  successful would be populated.
     */
    SKCEReturnObject execute(Long did,
                            String username);
}
