/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.entitybeans.FidoUsers;
import com.strongkey.skfs.utilities.SKFEException;
import javax.ejb.Local;

@Local
public interface getFidoUserLocal {
    FidoUsers getByUsername(Long did, String username) throws SKFEException;
}
