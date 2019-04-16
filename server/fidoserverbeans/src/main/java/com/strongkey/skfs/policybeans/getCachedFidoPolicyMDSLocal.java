/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import javax.ejb.Local;

/**
 *
 * @author mishimoto
 */
@Local
public interface getCachedFidoPolicyMDSLocal {
    public FidoPolicyObject getPolicyByDidUsername(Long did, String username);
    public FidoPolicyMDSObject getByMapKey(String policyMapKey);
}
