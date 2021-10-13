/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import javax.json.JsonObject;


public class AuthorizationPolicyOptions {
    private final int maxdataLength;
    private final Boolean preserve;


    public AuthorizationPolicyOptions(int maxdataLength, Boolean preserve){
        this.maxdataLength = maxdataLength;
        this.preserve = preserve; 
    }

    public int getMaxdataLength(){
        return maxdataLength;
    }
    
    public Boolean getPreserve(){
        return preserve;
    }

    public static AuthorizationPolicyOptions parse(JsonObject authorizationJson) {
        return new AuthorizationPolicyOptionsBuilder(
                authorizationJson.getInt(SKFSConstants.POLICY_ATTR_AUTHORIZATION_MAXDATALENGTH),
                authorizationJson.getBoolean(SKFSConstants.POLICY_ATTR_AUTHORIZATION_PRESERVE) )
                .build();
    }

    public static class AuthorizationPolicyOptionsBuilder{
        private final int builderMaxdataLength;
        private final Boolean builderPreserve;

        public AuthorizationPolicyOptionsBuilder (int builderMaxdataLength, Boolean builderPreserve){
            this.builderMaxdataLength = builderMaxdataLength;
            this.builderPreserve = builderPreserve;
        }

        public AuthorizationPolicyOptions build(){
            return new AuthorizationPolicyOptions(builderMaxdataLength,builderPreserve);
        }
    }
}
