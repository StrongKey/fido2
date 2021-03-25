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

public class CodeAccuracyDescriptor {
    private Integer base;
    private Integer minLength;
    private Short maxRetries;
    private Short blockSlowdown;
    
    public CodeAccuracyDescriptor (JsonObject jsonInput) {
        if (jsonInput.containsKey("base")) base = jsonInput.getInt("base");
        if (jsonInput.containsKey("minLength")) minLength = jsonInput.getInt("minLength");
        if (jsonInput.containsKey("maxRetries")) maxRetries = ((Integer)jsonInput.getInt("maxRetries")).shortValue();
        if (jsonInput.containsKey("blockSlowdown")) blockSlowdown = ((Integer)jsonInput.getInt("blockSlowdown")).shortValue();
    }

    public Integer getBase() {
        return base;
    }

    public void setBase(Integer base) {
        this.base = base;
    }

    public Integer getMinLength() {
        return minLength;
    }

    public void setMinLength(Integer minLength) {
        this.minLength = minLength;
    }

    public Short getMaxRetries() {
        return maxRetries;
    }

    public void setMaxRetries(Short maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Short getBlockSlowdown() {
        return blockSlowdown;
    }

    public void setBlockSlowdown(Short blockSlowdown) {
        this.blockSlowdown = blockSlowdown;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.base != null) job.add("base", base);
        if (this.minLength != null) job.add("minLength", minLength);
        if (this.maxRetries != null) job.add("maxRetries", maxRetries);
        if (this.blockSlowdown != null) job.add("blockSlowdown", blockSlowdown);
        return job.build();
    }
}
