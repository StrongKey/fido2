/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

public class JWTPolicyOptions {
    private final List<String>  algorithms;
    private final Integer duration;
    private final List<String> required;
    private final JWTSigningCerts signCerts;

    private JWTPolicyOptions( List<String>  algorithms,
             Integer duration,
            List<String> required,
            JWTSigningCerts signCerts){
        this.algorithms = algorithms;
        this.duration = duration;
        this.required = required;
        this.signCerts = signCerts;
    }

    public List<String>  getAlgorithm() {
        return algorithms;
    }


    public Integer getDuration() {
        return duration;
    }

    public List<String> getRequired() {
        return required;
    }

    public JWTSigningCerts getSignCerts() {
        return signCerts;
    }

    public static JWTPolicyOptions parse(JsonObject JWTJson) {

        return new JWTPolicyOptions.JWTPolicyOptionsBuilder(
                new ArrayList<>(JWTJson.getJsonArray(SKFSConstants.POLICY_JWT_ALGO).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
                JWTJson.getInt(SKFSConstants.POLICY_JWT_DURATION),
                new ArrayList<>(JWTJson.getJsonArray(SKFSConstants.POLICY_JWT_REQUIRED).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
                JWTSigningCerts.parse(JWTJson.getJsonObject(SKFSConstants.POLICY_JWT_SIGN)))
                        
                .build();
    }

    public static class JWTPolicyOptionsBuilder{
        private final List<String>  builderAlgorithm;

        private final Integer builderDuration;
        private final List<String> builderRequired;
        private final JWTSigningCerts buildersignCerts;

        public JWTPolicyOptionsBuilder(List<String>  algorithm, Integer duration, List<String> required, JWTSigningCerts signCerts){
            this.builderAlgorithm = algorithm;
            this.builderDuration= duration;
            this.builderRequired= required;
            this.buildersignCerts = signCerts;
        }


        public JWTPolicyOptions build(){
            return new JWTPolicyOptions(builderAlgorithm,
                    builderDuration,
                    builderRequired, buildersignCerts);
        }
    }
}
