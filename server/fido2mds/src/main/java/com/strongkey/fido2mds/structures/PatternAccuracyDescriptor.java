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

public class PatternAccuracyDescriptor {
    private BigInteger minComplexity;
    private Integer maxRetries;
    private Integer blockSlowdown;
    
    public PatternAccuracyDescriptor (JsonObject jsonInput) {
        if (jsonInput.containsKey("minComplexity")) minComplexity = jsonInput.getJsonNumber("minComplexity").bigIntegerValueExact();
        if (jsonInput.containsKey("maxRetries")) maxRetries = jsonInput.getInt("maxRetries");
        if (jsonInput.containsKey("blockSlowdown")) blockSlowdown = jsonInput.getInt("legalHblockSlowdownader");
    }

    public BigInteger getMinComplexity() {
        return minComplexity;
    }

    public void setMinComplexity(BigInteger minComplexity) {
        this.minComplexity = minComplexity;
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
        if (this.minComplexity != null) job.add("minComplexity", minComplexity);
        if (this.maxRetries != null) job.add("maxRetries", maxRetries);
        if (this.blockSlowdown != null) job.add("blockSlowdown", blockSlowdown);
        return job.build();
    }
}
