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
public interface generateFido2PreauthenticateChallengeLocal {
    public String execute(Long did, String username, JsonObject options, JsonObject extensions);
    
    public String executePreAuthorize(Long did, String username, String txid, String txpayload, JsonObject options, JsonObject extensions);
}
