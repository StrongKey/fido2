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

public class RGBPaletteEntry {
    private Integer r;
    private Integer g;
    private Integer b;
    
    public RGBPaletteEntry (JsonObject jsonInput) {
        if (jsonInput.containsKey("r")) r = jsonInput.getInt("r");
        if (jsonInput.containsKey("g")) g = jsonInput.getInt("g");
        if (jsonInput.containsKey("b")) b = jsonInput.getInt("b");
    }

    public Integer getR() {
        return r;
    }

    public void setR(Integer r) {
        this.r = r;
    }

    public Integer getG() {
        return g;
    }

    public void setG(Integer g) {
        this.g = g;
    }

    public Integer getB() {
        return b;
    }

    public void setB(Integer b) {
        this.b = b;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.r != null) job.add("r", r);
        if (this.g != null) job.add("g", g);
        if (this.b != null) job.add("b", b);
        return job.build();
    }
}
