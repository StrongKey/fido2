/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import javax.ejb.Local;

/**
 *
 * @author mishimoto
 */
@Local
public interface getCachedPolicyLocal {
    public FidoPolicyObject getByDidUsername(Long did, String username);
    public FidoPolicyObject getMapKey(String policyMapKey);
}
