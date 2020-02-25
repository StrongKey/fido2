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

@Local
public interface u2fAuthenticateBeanLocal {

    /**
     * Method that builds a u2f auth response object and processes the same.
     * @param did       - FIDO domain id
     * @param protocol  - U2F protocol version to comply with.
     * @param authresponseJson  - U2F Auth Response parameters as a json string
     * @param userPublicKey
     * @return          - FEreturn object with result
     * @throws SKFEException
     *                  - In case of any error
     */
    FEreturn execute(Long did,
                    String protocol,
                    String authresponseJson,
                    String userPublicKey,
                    String challenge,
                    String appid) throws SKFEException;
}
