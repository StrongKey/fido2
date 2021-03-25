/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class BiometricAccuracyDescriptor {
    private Double FAR;
    private Double FRR;
    private Double EER;
    private Double FAAR;
    private Integer maxRegerenceDataSets;
    private Integer maxRetries;
    private Integer blockSlowdown;
    
    public BiometricAccuracyDescriptor (JsonObject jsonInput) {
        if (jsonInput.containsKey("FAR")) FAR = jsonInput.getJsonNumber("FAR").doubleValue();
        if (jsonInput.containsKey("FRR")) FRR = jsonInput.getJsonNumber("FRR").doubleValue();
        if (jsonInput.containsKey("EER")) EER = jsonInput.getJsonNumber("EER").doubleValue();
        if (jsonInput.containsKey("FAAR")) FAAR = jsonInput.getJsonNumber("FAAR").doubleValue();
        if (jsonInput.containsKey("maxRegerenceDataSets")) maxRegerenceDataSets = jsonInput.getInt("maxRegerenceDataSets");
        if (jsonInput.containsKey("maxRetries")) maxRetries = jsonInput.getInt("maxRetries");
        if (jsonInput.containsKey("blockSlowdown")) blockSlowdown = jsonInput.getInt("blockSlowdown");
    }

    public Double getFAR() {
        return FAR;
    }

    public void setFAR(Double FAR) {
        this.FAR = FAR;
    }

    public Double getFRR() {
        return FRR;
    }

    public void setFRR(Double FRR) {
        this.FRR = FRR;
    }

    public Double getEER() {
        return EER;
    }

    public void setEER(Double EER) {
        this.EER = EER;
    }

    public Double getFAAR() {
        return FAAR;
    }

    public void setFAAR(Double FAAR) {
        this.FAAR = FAAR;
    }

    public Integer getMaxRegerenceDataSets() {
        return maxRegerenceDataSets;
    }

    public void setMaxRegerenceDataSets(Integer maxRegerenceDataSets) {
        this.maxRegerenceDataSets = maxRegerenceDataSets;
    }

    public Integer getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Integer maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Integer getBlockSlowdown() {
        return blockSlowdown;
    }

    public void setBlockSlowdown(Integer blockSlowdown) {
        this.blockSlowdown = blockSlowdown;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.FAR != null) job.add("FAR", FAR);
        if (this.FRR != null) job.add("FRR", FRR);
        if (this.EER != null) job.add("EER", EER);
        if (this.FAAR != null) job.add("FAAR", FAAR);
        if (this.maxRegerenceDataSets != null) job.add("maxRegerenceDataSets", maxRegerenceDataSets);
        if (this.maxRetries != null) job.add("maxRetries", maxRetries);
        if (this.blockSlowdown != null) job.add("blockSlowdown", blockSlowdown);
        return job.build();
    }
}
