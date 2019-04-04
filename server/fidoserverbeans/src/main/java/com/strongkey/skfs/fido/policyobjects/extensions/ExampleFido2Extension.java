/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido.policyobjects.extensions;

import com.strongkey.skfs.utilities.skfsConstants;
import javax.json.JsonValue;

public class ExampleFido2Extension implements Fido2AuthenticationExtension, Fido2RegistrationExtension {
    
    public ExampleFido2Extension(){
        
    }
    
    @Override
    public String getExtensionIdentifier() {
        return skfsConstants.POLICY_EXTENSIONS_EXAMPLE;
    }
    
    @Override
    public Object generateChallengeInfo(JsonValue extraInfo) {
        return extraInfo;
    }

    @Override
    public boolean verifyFido2Extension(String extensionResponse) {
        return true;
    }
}
