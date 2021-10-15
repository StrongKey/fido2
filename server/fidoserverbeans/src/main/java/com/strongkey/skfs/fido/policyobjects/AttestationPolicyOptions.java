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

public class AttestationPolicyOptions {

    private final List<String> conveyance;
    private final ArrayList<String> formats;
    private final EnterpriseAttestationOptions enterpriseAttestationOptions;

    private AttestationPolicyOptions(List<String> conveyance, ArrayList<String> formats,  EnterpriseAttestationOptions enterpriseAttestationOptions){

        this.conveyance = conveyance;
        this.formats = formats;
        this.enterpriseAttestationOptions = enterpriseAttestationOptions;
    }

    
    public List<String> getAttestationConveyance() {
        return conveyance;
    }

    public ArrayList<String> getAttestationFormats() {
        return formats;
    }
    
    public EnterpriseAttestationOptions getEnterpriseOptions(){
        return enterpriseAttestationOptions;
    }
    public static AttestationPolicyOptions parse(JsonObject attestationJson) {
        EnterpriseAttestationOptions enterprise = null;
        if(attestationJson.containsKey(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE)){
             enterprise = EnterpriseAttestationOptions.parse(attestationJson.getJsonObject(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE));
        }
       
        return new AttestationPolicyOptions.AttestationPolicyOptionsBuilder(
                new ArrayList<>(attestationJson.getJsonArray(SKFSConstants.POLICY_ATTESTATION_CONVEYANCE).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
                new ArrayList<>(attestationJson.getJsonArray(SKFSConstants.POLICY_ATTESTATION_FORMATS).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
                enterprise        
                ).build();
    }

    public static class AttestationPolicyOptionsBuilder{
        private final List<String> builderConveyance;
        private final ArrayList<String> builderFormats;
        private final EnterpriseAttestationOptions builderEnterpriseAttesationOptions;

        public AttestationPolicyOptionsBuilder( List<String> builderConveyance, ArrayList<String> builderFormats, EnterpriseAttestationOptions builderEnterpriseAttesationOptions){
            this.builderConveyance = builderConveyance;
            this.builderFormats = builderFormats;
            this.builderEnterpriseAttesationOptions=builderEnterpriseAttesationOptions;
        }


        public AttestationPolicyOptions build(){
            return new AttestationPolicyOptions(
                    builderConveyance, builderFormats, builderEnterpriseAttesationOptions);
        }
    }
}