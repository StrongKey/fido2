/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

public class RegistrationPolicyOptions {
    private final String displayName;
    private final String excludeCredentials;
    private final List<String> authenticatorAttachment;
    private final List<String> requireResidentKey;

    private RegistrationPolicyOptions( String displayName,
             String excludeCredentials,
            List<String> authenticatorAttachment, List<String> requireResidentKey){
        this.displayName = displayName;
        this.excludeCredentials = excludeCredentials;
        this.authenticatorAttachment = authenticatorAttachment;
        this.requireResidentKey = requireResidentKey;
    }

    public String getDisplayName() {
        return displayName;
    }


    public String getExcludeCredentials() {
        return excludeCredentials;
    }

    public List<String> getAuthenticatorAttachment() {
        return authenticatorAttachment;
    }

    public List<String> getRequireResidentKey() {
        return requireResidentKey;
    }

    public static RegistrationPolicyOptions parse(JsonObject registrationJson) {


        return new RegistrationPolicyOptions.RegistrationPolicyOptionsBuilder(
                registrationJson.getString(SKFSConstants.POLICY_REGISTRATION_DISPLAYNAME),
                registrationJson.getString(SKFSConstants.POLICY_REGISTRATION_EXCLUDECREDENTIALS),
                new ArrayList<>(registrationJson.getJsonArray(SKFSConstants.POLICY_REGISTRATION_REQUIRERESIDENTKEY).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
                new ArrayList<>(registrationJson.getJsonArray(SKFSConstants.POLICY_REGISTRATION_AUTHENTICATORATTACHMENT).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())))
                .build();
    }

    public static class RegistrationPolicyOptionsBuilder{
        private final String builderDisplayName;

        private final String builderExcludeCredentials;
        private final List<String> builderRequireResidentKey;
        private final List<String> builderAuthenticatorAttachment;

        public RegistrationPolicyOptionsBuilder(String displayName, String excludeCredentials, List<String> requireResidentKey, List<String> authenticatorAttachment){
            this.builderDisplayName = displayName;
            this.builderExcludeCredentials = excludeCredentials;
            this.builderAuthenticatorAttachment = authenticatorAttachment;
            this.builderRequireResidentKey = requireResidentKey;
        }


        public RegistrationPolicyOptions build(){
            return new RegistrationPolicyOptions(builderDisplayName,
                    builderExcludeCredentials,
                    builderAuthenticatorAttachment, builderRequireResidentKey);
        }
    }
}
