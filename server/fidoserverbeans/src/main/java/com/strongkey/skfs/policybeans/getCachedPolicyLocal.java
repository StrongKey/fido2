/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import javax.ejb.Local;

@Local
public interface getCachedPolicyLocal {
    public FidoPolicyObject getByDidUsername(Long did, String username);
    public FidoPolicyObject getMapKey(String policyMapKey);
}
