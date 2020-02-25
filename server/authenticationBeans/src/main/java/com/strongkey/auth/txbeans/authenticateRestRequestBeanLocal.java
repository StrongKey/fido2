/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.auth.txbeans;

import javax.ejb.Local;
import javax.servlet.http.HttpServletRequest;

/**
 * Local interface for authenticateRestRequestBean
 */
@Local
public interface authenticateRestRequestBeanLocal {

    boolean execute(Long did,
                    HttpServletRequest accesskey,
                    Object requestBody);
}
