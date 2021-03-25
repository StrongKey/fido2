/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;


class TrustedAuthenticatorPolicyOptions {
    private final ArrayList<String> allowedAAGUIDs; 
    
    private TrustedAuthenticatorPolicyOptions(ArrayList<String> allowedAAGUIDs){
        this.allowedAAGUIDs = allowedAAGUIDs;
    }
    
    public ArrayList<String> getAllowedAAGUIDs(){
        return allowedAAGUIDs; 
    }
    
    public static TrustedAuthenticatorPolicyOptions parse(JsonObject systemJson) {
        TrustedAuthenticatorPolicyOptionsBuilder trustedAuthenticatorPolicyBuilder = new TrustedAuthenticatorPolicyOptionsBuilder();
        if(systemJson.getJsonArray(SKFSConstants.POLICY_AUTHENTICATOR_AAGUIDS) != null){
            trustedAuthenticatorPolicyBuilder.setAllowedAAGUIDs( new ArrayList<>(systemJson.getJsonArray(SKFSConstants.POLICY_AUTHENTICATOR_AAGUIDS).stream().map(x -> ((JsonString) x).getString()).collect(Collectors.toList())));
        }
        return trustedAuthenticatorPolicyBuilder.build();
            
            
        }
  
    public static class TrustedAuthenticatorPolicyOptionsBuilder{
        private ArrayList<String> builderAllowedAAGUIDs;
        public TrustedAuthenticatorPolicyOptionsBuilder setAllowedAAGUIDs(ArrayList<String> allowedAAGUIDs) {
            this.builderAllowedAAGUIDs = allowedAAGUIDs;
            return this;
        }
        public TrustedAuthenticatorPolicyOptions build(){
            return new TrustedAuthenticatorPolicyOptions(builderAllowedAAGUIDs);
        }
        
    }
}

