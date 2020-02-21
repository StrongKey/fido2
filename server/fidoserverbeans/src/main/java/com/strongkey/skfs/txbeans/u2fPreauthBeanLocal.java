/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.FEreturn;
import javax.ejb.Local;
import javax.json.JsonArray;

/**
 * Local interface for u2fPreauthBean
 *
 */
@Local
public interface u2fPreauthBeanLocal {

    /**
     * Executes the pre-authentication process which primarily includes generating
     * authentication challenge parameters for the given username complying to the
     * protocol specified. did is the FIDO domain credentials.
     *
     * NOTE : The did and secretkey will be used for the production
     * version of the FIDO server software. They can be ignored for the open-source
     * version.
     * @param did       - FIDO domain id
     * @param protocol  - U2F protocol version to comply with.
     * @param username  - username
     * @param KeyHandle - The user could have multiple fido authenticators registered
     *                      successfully. An authentication challenge can pertain
     *                      to only one unique fido authenticator (key handle).
     * @param appidfromDB
     * @param transports
     * @return          - FEReturn object that binds the U2F registration challenge
     *                      parameters in addition to a set of messages that explain
     *                      the series of actions happened during the process.
     */
    FEreturn execute(Long did,
                    String protocol,
                    String username,
                    String KeyHandle,
                    String appidfromDB,
                    JsonArray transports);
}
