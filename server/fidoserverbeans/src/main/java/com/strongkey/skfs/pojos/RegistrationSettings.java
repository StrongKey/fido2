/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.pojos;

import com.strongkey.skfs.utilities.SKFSCommon;
import java.util.Base64;
import javax.json.JsonObject;

public class RegistrationSettings {
    private final Integer alg;
    private final Integer kty;
    private final Integer crv;
    private final Boolean up;
    private final Boolean uv;
    private final String attestationFormat;
    private final String attestationType;
    //private final String[] extensions;        //TODO

    private RegistrationSettings(Integer alg, Integer kty, Integer crv, Boolean up, Boolean uv,
            String attestationFormat, String attestationType){
        this.alg = alg;
        this.kty = kty;
        this.crv = crv;
        this.up = up;
        this.uv = uv;
        this.attestationFormat = attestationFormat;
        this.attestationType = attestationType;
    }

    public Integer getAlg() {
        return alg;
    }

    public Integer getKty() {
        return kty;
    }

    public Integer getCrv() {
        return crv;
    }

    public Boolean isUp() {
        return up;
    }

    public Boolean isUv() {
        return uv;
    }

    public String getAttestationFormat() {
        return attestationFormat;
    }

    public String getAttestationType() {
        return attestationType;
    }

    public static RegistrationSettings parse(String registrationSettings, Integer registrationVersion){
        String decodedrs = new String(Base64.getUrlDecoder().decode(registrationSettings));
        JsonObject rsJson = SKFSCommon.getJsonObjectFromString(decodedrs);
        return new RegistrationSettingsBuilder()
                .setAlg((rsJson.getJsonNumber("ALG") == null)? null : rsJson.getJsonNumber("ALG").intValue())
                .setKty((rsJson.getJsonNumber("KTY") == null)? null : rsJson.getJsonNumber("KTY").intValue())
                .setCrv((rsJson.getJsonNumber("CRV") == null)? null : rsJson.getJsonNumber("CRV").intValue())
                .setUp(rsJson.containsKey("UP") ? rsJson.getBoolean("UP") : null)        //TODO make this check more robust
                .setUv(rsJson.containsKey("UV") ? rsJson.getBoolean("UV") : null)        //TODO make this check more robust
                .setAttestationFormat(rsJson.getString("attestationFormat", null))
                .setAttestationType(rsJson.getString("attestationType", null))
                .build();
    }

    private static class RegistrationSettingsBuilder{
        private Integer builderAlg;
        private Integer builderKty;
        private Integer builderCrv;
        private Boolean builderUp;
        private Boolean builderUv;
        private String builderAttestationFormat;
        private String builderAttestationType;
        //private String[] builderExtensions;        //TODO

        public RegistrationSettingsBuilder setAlg(Integer alg) {
            this.builderAlg = alg;
            return this;
        }

        public RegistrationSettingsBuilder setKty(Integer kty) {
            this.builderKty = kty;
            return this;
        }

        public RegistrationSettingsBuilder setCrv(Integer crv) {
            this.builderCrv = crv;
            return this;
        }

        public RegistrationSettingsBuilder setUp(Boolean up) {
            this.builderUp = up;
            return this;
        }


        public RegistrationSettingsBuilder setUv(Boolean uv) {
            this.builderUv = uv;
            return this;
        }

        public RegistrationSettingsBuilder setAttestationFormat(String attestationFormat) {
            this.builderAttestationFormat = attestationFormat;
            return this;
        }


        public RegistrationSettingsBuilder setAttestationType(String attestationType) {
            this.builderAttestationType = attestationType;
            return this;
        }

        public RegistrationSettings build(){
            return new RegistrationSettings(builderAlg, builderKty, builderCrv,
                    builderUp, builderUv, builderAttestationFormat,
                    builderAttestationType);
        }
    }

}
