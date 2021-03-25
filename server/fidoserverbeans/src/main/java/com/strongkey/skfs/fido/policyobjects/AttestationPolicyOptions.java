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

    private AttestationPolicyOptions(List<String> conveyance, ArrayList<String> formats){

        this.conveyance = conveyance;
        this.formats = formats;
    }

    
    public List<String> getAttestationConveyance() {
        return conveyance;
    }

    public ArrayList<String> getAttestationFormats() {
        return formats;
    }

    public static AttestationPolicyOptions parse(JsonObject attestationJson) {


        return new AttestationPolicyOptions.AttestationPolicyOptionsBuilder(
                new ArrayList<>(attestationJson.getJsonArray(SKFSConstants.POLICY_ATTESTATION_CONVEYANCE).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
                new ArrayList<>(attestationJson.getJsonArray(SKFSConstants.POLICY_ATTESTATION_FORMATS).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())))
                .build();
    }

    public static class AttestationPolicyOptionsBuilder{
        private final List<String> builderConveyance;
        private final ArrayList<String> builderFormats;

        public AttestationPolicyOptionsBuilder( List<String> builderConveyance, ArrayList<String> builderFormats){
            this.builderConveyance = builderConveyance;
            this.builderFormats = builderFormats;
        }


        public AttestationPolicyOptions build(){
            return new AttestationPolicyOptions(
                    builderConveyance, builderFormats);
        }
    }
}