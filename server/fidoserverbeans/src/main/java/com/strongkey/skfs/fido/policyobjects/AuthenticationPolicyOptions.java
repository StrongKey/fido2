/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import javax.json.JsonObject;


public class AuthenticationPolicyOptions {
    private final String allowCredentials;


    public AuthenticationPolicyOptions(String allowCredentials){
        this.allowCredentials = allowCredentials;
    }

    public String getAllowCredentials() {
        return allowCredentials;
    }

    public static AuthenticationPolicyOptions parse(JsonObject authenticationJson) {
        return new AuthenticationPolicyOptions.AuthenticationPolicyOptionsBuilder()
                .setAllowCredentials(authenticationJson.getString(SKFSConstants.POLICY_AUTHENTICATION_ALLOWCREDENTIALS, null))
                .build();
    }

    public static class AuthenticationPolicyOptionsBuilder{
        private String builderAllowCredentials;


        public AuthenticationPolicyOptionsBuilder setAllowCredentials(String allowCredentials){
            this.builderAllowCredentials = allowCredentials;
            return this;
        }

        public AuthenticationPolicyOptions build(){
            return new AuthenticationPolicyOptions(builderAllowCredentials);
        }
    }
}
