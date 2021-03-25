/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skce.pojos.MDSEndpoint;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

public class MdsPolicyOptions {
    private final ArrayList<MDSEndpoint> endpoints;
    private final ArrayList<String> allowedCertificationLevel;

    private MdsPolicyOptions(ArrayList<MDSEndpoint> endpoints, ArrayList<String> allowedCertificationLevel){
        this.endpoints = endpoints;
        this.allowedCertificationLevel = allowedCertificationLevel;
    }

    public ArrayList<MDSEndpoint> getEndpoints() {
        return endpoints;
    }

    public ArrayList<String> getAllowedCertificationLevel() {
        return allowedCertificationLevel;
    }

    public static MdsPolicyOptions parse(JsonObject mdsJson) {
        if(mdsJson == null){
            return null;
        }

        return new MdsPolicyOptions.MdsPolicyOptionsBuilder(
                new ArrayList<>(mdsJson.getJsonArray(SKFSConstants.POLICY_MDS_ENDPOINTS).stream()
                        .map(x -> (JsonObject) x)
                        .map(x -> new MDSEndpointObject(x.getString(SKFSConstants.POLICY_MDS_ENDPOINT_URL, null),
                                x.getString(SKFSConstants.POLICY_MDS_ENDPOINT_TOKEN, null)))
                        .collect(Collectors.toList())),
                new ArrayList<>(mdsJson.getJsonArray(SKFSConstants.POLICY_MDS_CERTIFICATION).stream().map(x -> ((JsonString) x).getString()).collect(Collectors.toList())))
                .build();
    }

    public static class MdsPolicyOptionsBuilder{
        private final ArrayList<MDSEndpoint> builderEndpoints;
        private final ArrayList<String> builderAllowedCertificateLevel;

        public MdsPolicyOptionsBuilder(ArrayList<MDSEndpoint> endpoints, ArrayList<String> allowedCertificationLevel){
            this.builderEndpoints = endpoints;
            this.builderAllowedCertificateLevel = allowedCertificationLevel;

        }

        public MdsPolicyOptions build(){
            return new MdsPolicyOptions(builderEndpoints, builderAllowedCertificateLevel);
        }
    }
}
