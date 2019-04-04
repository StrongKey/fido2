/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.utilities.SKFEException;
import java.util.Collection;
import javax.ejb.Local;

@Local
public interface getFidoKeysLocal {
    Collection<FidoKeys> getByUsername(Long did, String username)throws SKFEException;
    Collection<FidoKeys> getByUsernameStatus(Long did, String username, String status)throws SKFEException;
    Collection<FidoKeys> getAll(Long did)throws SKFEException;
    FidoKeys getByUsernameKH(Long did, String username, String KH)throws SKFEException;
    FidoKeys getByfkid(Short sid, Long did, String username,Long fkid)throws SKFEException;
    FidoKeys getNewestKeyByUsernameStatus(Long did, String username, String status) throws SKFEException;
    int nextfkid(Short sid, Long did, String username);
}
