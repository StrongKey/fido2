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

public class Version {
    private Integer major;
    private Integer minor;
    
    public Version (JsonObject jsonInput) {
        if (jsonInput.containsKey("major")) major = jsonInput.getInt("major");
        if (jsonInput.containsKey("minor")) minor = jsonInput.getInt("minor");
    }

    public Integer getMajor() {
        return major;
    }

    public void setMajor(Integer major) {
        this.major = major;
    }

    public Integer getMinor() {
        return minor;
    }

    public void setMinor(Integer minor) {
        this.minor = minor;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.major != null) job.add("major", major);
        if (this.minor != null) job.add("minor", minor);
        return job.build();
    }
}
