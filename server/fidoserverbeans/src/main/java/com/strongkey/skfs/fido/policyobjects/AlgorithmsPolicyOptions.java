/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.skce.utilities.skceConstants;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

public class AlgorithmsPolicyOptions {
    private final ArrayList<String> supportedEllipticCurves;
    private final ArrayList<String> allowedECSignatures;
    private final ArrayList<String> allowedRSASignatures;

    private AlgorithmsPolicyOptions(
            ArrayList<String> supportedEllipticCurves, ArrayList<String> allowedECSignatures,
            ArrayList<String> allowedRSASignatures){
        this.supportedEllipticCurves = supportedEllipticCurves;
        this.allowedECSignatures = allowedECSignatures;
        this.allowedRSASignatures = allowedRSASignatures;
    }

    public ArrayList<String> getSupportedEllipticCurves() {
        return supportedEllipticCurves;
    }

    public ArrayList<String> getAllowedECSignatures() {
        return allowedECSignatures;
    }

    public ArrayList<String> getAllowedRSASignatures() {
        return allowedRSASignatures;
    }


    public static AlgorithmsPolicyOptions parse(JsonObject algoJson) {

        AlgorithmsPolicyOptionsBuilder algoPolicyBuilder =
                new AlgorithmsPolicyOptions.AlgorithmsPolicyOptionsBuilder();
        if(algoJson.getJsonArray(SKFSConstants.POLICY_CRYPTO_ALLOWED_EC_SIGNATURES) != null){
            algoPolicyBuilder.setAllowedECSignatures(new ArrayList<>(algoJson.getJsonArray(SKFSConstants.POLICY_CRYPTO_ALLOWED_EC_SIGNATURES).stream().map(x -> ((JsonString) x).getString()).collect(Collectors.toList())));
        }
        if(algoJson.getJsonArray(SKFSConstants.POLICY_CRYPTO_ALLOWED_RSA_SIGNATURES) != null){
            algoPolicyBuilder.setAllowedRSASignatures(new ArrayList<>(algoJson.getJsonArray(SKFSConstants.POLICY_CRYPTO_ALLOWED_RSA_SIGNATURES).stream().map(x -> ((JsonString) x).getString()).collect(Collectors.toList())));
        }
        if(algoJson.getJsonArray(SKFSConstants.POLICY_CRYPTO_ELLIPTIC_CURVES) != null){
            algoPolicyBuilder.setSupportedEllipticCurves(new ArrayList<>(algoJson.getJsonArray(SKFSConstants.POLICY_CRYPTO_ELLIPTIC_CURVES).stream().map(x -> ((JsonString) x).getString()).collect(Collectors.toList())));
        }
        return algoPolicyBuilder.build();
    }

    public static class AlgorithmsPolicyOptionsBuilder{
        private ArrayList<String> builderSupportedEllipticCurves;
        private ArrayList<String> builderAllowedECSignatures;
        private ArrayList<String> builderAllowedRSASignatures;


        public AlgorithmsPolicyOptionsBuilder setSupportedEllipticCurves(ArrayList<String> supportedEllipticCurves) {
            this.builderSupportedEllipticCurves = supportedEllipticCurves;
            return this;
        }

        public AlgorithmsPolicyOptionsBuilder setAllowedECSignatures(ArrayList<String> allowedECSignatures) {
            this.builderAllowedECSignatures = allowedECSignatures;
            return this;
        }

        public AlgorithmsPolicyOptionsBuilder setAllowedRSASignatures(ArrayList<String> allowedRSASignatures) {
            this.builderAllowedRSASignatures = allowedRSASignatures;
            return this;
        }


        public AlgorithmsPolicyOptions build(){
            return new AlgorithmsPolicyOptions(builderSupportedEllipticCurves,
                builderAllowedECSignatures, builderAllowedRSASignatures);
        }
    }
}
