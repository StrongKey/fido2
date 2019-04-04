/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
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
