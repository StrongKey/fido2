/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
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
                rpJson.getString(SKFSConstants.POLICY_RP_NAME, null))
                .setId(rpJson.getString(SKFSConstants.POLICY_RP_ID, null))
                .setIcon(rpJson.getString(SKFSConstants.POLICY_RP_ICON, null))
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
