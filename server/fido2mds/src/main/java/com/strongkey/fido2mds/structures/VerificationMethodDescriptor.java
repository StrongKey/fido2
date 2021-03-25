/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.math.BigInteger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class VerificationMethodDescriptor {
    private BigInteger userVerification;
    private CodeAccuracyDescriptor caDesc;
    private BiometricAccuracyDescriptor baDesc;
    private PatternAccuracyDescriptor paDesc;
    
    public VerificationMethodDescriptor(JsonObject jsonInput) {
        if (jsonInput.containsKey("userVerification")) userVerification = jsonInput.getJsonNumber("userVerification").bigIntegerValueExact();
        if (jsonInput.containsKey("caDesc")) caDesc = new CodeAccuracyDescriptor(jsonInput.getJsonObject("caDesc"));
        if (jsonInput.containsKey("baDesc")) baDesc = new BiometricAccuracyDescriptor(jsonInput.getJsonObject("baDesc"));
        if (jsonInput.containsKey("paDesc")) paDesc = new PatternAccuracyDescriptor(jsonInput.getJsonObject("paDesc"));
    }

    public BigInteger getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(BigInteger userVerification) {
        this.userVerification = userVerification;
    }

    public CodeAccuracyDescriptor getCaDesc() {
        return caDesc;
    }

    public void setCaDesc(CodeAccuracyDescriptor caDesc) {
        this.caDesc = caDesc;
    }

    public BiometricAccuracyDescriptor getBaDesc() {
        return baDesc;
    }

    public void setBaDesc(BiometricAccuracyDescriptor baDesc) {
        this.baDesc = baDesc;
    }

    public PatternAccuracyDescriptor getPaDesc() {
        return paDesc;
    }

    public void setPaDesc(PatternAccuracyDescriptor paDesc) {
        this.paDesc = paDesc;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.userVerification != null) job.add("userVerification", userVerification);
        if (this.caDesc != null) job.add("caDesc", caDesc.toJsonObject());
        if (this.baDesc != null) job.add("baDesc", baDesc.toJsonObject());
        if (this.paDesc != null) job.add("paDesc", paDesc.toJsonObject());
        return job.build();
    }
}
