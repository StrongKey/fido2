/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.FEreturn;
import javax.ejb.Local;

/**
 * Local interface for u2fPreregisterBean
 *
 */
@Local
public interface u2fPreregisterBeanLocal {

    /**
     * Executes the pre-registration process which primarily includes generating
     * registration challenge parameters for the given username complying to the
     * protocol specified. did is the FIDO domain credentials.
     *
     * NOTE : The did and secretkey will be used for the production
     * version of the FIDO server software. They can be ignored for the open-source
     * version.
     * @param did       - FIDO domain id
     * @param protocol  - U2F protocol version to comply with.
     * @param username  - username
     * @return          - FEReturn object that binds the U2F registration challenge
     *                      parameters in addition to a set of messages that explain
     *                      the series of actions happened during the process.
     * @throws SKFEException -
     *                      Thrown in case of any error scenario.
     */
    FEreturn execute(Long did,
                    String protocol,
                    String username) throws SKFEException;
}
