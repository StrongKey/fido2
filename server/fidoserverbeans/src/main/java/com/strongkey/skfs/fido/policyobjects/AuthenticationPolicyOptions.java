/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.skfsConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

public class AuthenticationPolicyOptions {
    private final String allowCredentials;
    private final List<String> userVerification;
    
    public AuthenticationPolicyOptions(String allowCredentials, List<String> userVerification){
        this.allowCredentials = allowCredentials;
        this.userVerification = userVerification;
    }

    public String getAllowCredentials() {
        return allowCredentials;
    }

    public List<String> getUserVerification() {
        return userVerification;
    }
    
    public static AuthenticationPolicyOptions parse(JsonObject authenticationJson) {
        return new AuthenticationPolicyOptions.AuthenticationPolicyOptionsBuilder(
                new ArrayList<>(authenticationJson.getJsonArray(skfsConstants.POLICY_AUTHENTICATION_USERVERIFICATION).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())))
                .setAllowCredentials(authenticationJson.getString(skfsConstants.POLICY_AUTHENTICATION_ALLOWCREDENTIALS, null))
                .build();
    }
    
    public static class AuthenticationPolicyOptionsBuilder{
        private String builderAllowCredentials;
        private final List<String> builderUserVerification;
        
        public AuthenticationPolicyOptionsBuilder(List<String> userVerification) {
            this.builderUserVerification = userVerification;
        }
        
        public AuthenticationPolicyOptionsBuilder setAllowCredentials(String allowCredentials){
            this.builderAllowCredentials = allowCredentials;
            return this;
        }
        
        public AuthenticationPolicyOptions build(){
            return new AuthenticationPolicyOptions(builderAllowCredentials, builderUserVerification);
        }
    }
}
