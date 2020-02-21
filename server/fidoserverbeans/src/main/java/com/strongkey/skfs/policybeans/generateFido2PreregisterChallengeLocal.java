/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.policybeans;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface generateFido2PreregisterChallengeLocal {

    public String execute(Long did, String username, String displayName, JsonObject options, JsonObject extensions);
}
