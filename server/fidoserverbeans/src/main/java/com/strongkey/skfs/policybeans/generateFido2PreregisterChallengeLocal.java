/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface generateFido2PreregisterChallengeLocal {
    
    public String execute(Long did, String username, String displayName, JsonObject options, JsonObject extensions);
}
