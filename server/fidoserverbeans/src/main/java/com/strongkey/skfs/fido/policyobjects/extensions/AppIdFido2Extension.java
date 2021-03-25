/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido.policyobjects.extensions;

import com.strongkey.skfs.utilities.SKFSConstants;
import javax.json.JsonValue;

public class AppIdFido2Extension implements Fido2AuthenticationExtension {
    private final String appId;

    public AppIdFido2Extension(String appId){
        this.appId = appId;
    }

    @Override
    public String getExtensionIdentifier() {
        return SKFSConstants.POLICY_EXTENSIONS_EXAMPLE;
    }

    @Override
    public Object generateChallengeInfo(JsonValue extraInfo) {
        return appId;
    }

    @Override
    public boolean verifyFido2Extension(String extensionResponse) {
        return false;   //TODO
    }

}
