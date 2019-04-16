/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.fido2.FIDO2AttestationObject;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfs.utilities.SKFEException;
import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface verifyFido2RegistrationPolicyLocal {
    public void execute(UserSessionInfo userInfo, JsonObject clientJson, FIDO2AttestationObject attObject) throws SKFEException;
}
