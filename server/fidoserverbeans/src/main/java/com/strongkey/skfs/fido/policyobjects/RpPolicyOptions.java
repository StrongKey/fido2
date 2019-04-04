/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.skfsConstants;
import javax.json.JsonObject;

public class RpPolicyOptions {
    private final String name;
    private final String id;
    private final String icon;
    
    private RpPolicyOptions(String name, String id, String icon){
        this.name = name;
        this.id = id;
        this.icon = icon;
    }

    public String getName() {
        return name;
    }

    public String getId() {
        return id;
    }

    public String getIcon() {
        return icon;
    }
    
    public static RpPolicyOptions parse(JsonObject rpJson) {
        return new RpPolicyOptions.RpPolicyOptionsBuilder(
                rpJson.getString(skfsConstants.POLICY_RP_NAME, null))
                .setId(rpJson.getString(skfsConstants.POLICY_RP_ID, null))
                .setIcon(rpJson.getString(skfsConstants.POLICY_RP_ICON, null))
                .build();
    }
    
    public static class RpPolicyOptionsBuilder{
        private final String builderName;
        private String builderId;
        private String builderIcon;
        
        public RpPolicyOptionsBuilder(String name){
            this.builderName = name;
        }
        
        public RpPolicyOptionsBuilder setId(String id) {
            this.builderId = id;
            return this;
        }
        
        public RpPolicyOptionsBuilder setIcon(String icon) {
            this.builderIcon = icon;
            return this;
        }
        
        public RpPolicyOptions build(){
            return new RpPolicyOptions(builderName, builderId, builderIcon);
        }
    }
}
