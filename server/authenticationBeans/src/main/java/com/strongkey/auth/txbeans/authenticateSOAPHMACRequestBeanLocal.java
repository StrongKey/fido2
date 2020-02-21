/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.auth.txbeans;

import javax.ejb.Local;

/**
 * Local interface for authenticateRestRequestBean
 */
@Local
public interface authenticateSOAPHMACRequestBeanLocal {

    boolean execute(Long did,
                    String contentsha256,
                    String authorization,
                    Long timestamp,
                    String apiversion,
                    String contenttype,
                    String requesturi,
                    String requestBody);
}
