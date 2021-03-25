/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.policybeans;

import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.utilities.SKFEException;
import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface verifyFido2AuthenticationPolicyLocal {
    public void execute(UserSessionInfo userInfo, long did, JsonObject clientJson,
            FIDO2AuthenticatorData authData, FidoKeys key, String format) throws SKFEException;
}
