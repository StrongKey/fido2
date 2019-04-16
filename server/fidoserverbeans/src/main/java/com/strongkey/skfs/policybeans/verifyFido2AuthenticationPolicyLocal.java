/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfs.utilities.SKFEException;
import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface verifyFido2AuthenticationPolicyLocal {
    public void execute(UserSessionInfo userInfo, long did, JsonObject clientJson,
            FIDO2AuthenticatorData authData, FidoKeys key) throws SKFEException;
}
