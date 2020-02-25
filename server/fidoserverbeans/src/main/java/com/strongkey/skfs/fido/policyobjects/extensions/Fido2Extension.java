/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido.policyobjects.extensions;

import javax.json.JsonValue;

public interface Fido2Extension {
    public String getExtensionIdentifier();
    public Object generateChallengeInfo(JsonValue extraInfo);
    public boolean verifyFido2Extension(String extensionResponse);
}
