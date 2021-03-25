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

public class ExtensionDescriptor {
    private String id;
    private Short tag;
    private String data;
    private Boolean fail_if_unknown;
    
    public ExtensionDescriptor(JsonObject jsonInput) {
        if (jsonInput.containsKey("id")) id = jsonInput.getString("id");
        if (jsonInput.containsKey("tag")) tag = ((Integer) jsonInput.getInt("tag")).shortValue();
        if (jsonInput.containsKey("data")) data = jsonInput.getString("data");
        if (jsonInput.containsKey("fail_if_unknown")) fail_if_unknown = jsonInput.getBoolean("fail_if_unknown");
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Short getTag() {
        return tag;
    }

    public void setTag(Short tag) {
        this.tag = tag;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public Boolean getFail_if_unknown() {
        return fail_if_unknown;
    }

    public void setFail_if_unknown(Boolean fail_if_unknown) {
        this.fail_if_unknown = fail_if_unknown;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.id != null) job.add("id", id);
        if (this.tag != null) job.add("tag", tag);
        if (this.data != null) job.add("data", data);
        if (this.fail_if_unknown != null) job.add("fail_if_unknown", fail_if_unknown);
        return job.build();
    }
}
