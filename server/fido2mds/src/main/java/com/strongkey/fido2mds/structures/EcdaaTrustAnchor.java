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

public class EcdaaTrustAnchor {
    private String X;
    private String Y;
    private String c;
    private String sx;
    private String sy;
    private String GlCurvce;
    
    public EcdaaTrustAnchor(JsonObject jsonInput) {
        if (jsonInput.containsKey("X")) X = jsonInput.getString("X");
        if (jsonInput.containsKey("Y")) Y = jsonInput.getString("Y");
        if (jsonInput.containsKey("c")) c = jsonInput.getString("c");
        if (jsonInput.containsKey("sx")) sx = jsonInput.getString("sx");
        if (jsonInput.containsKey("sy")) sy = jsonInput.getString("sy");
        if (jsonInput.containsKey("GlCurvce")) GlCurvce = jsonInput.getString("GlCurvce");
    }

    public String getX() {
        return X;
    }

    public void setX(String X) {
        this.X = X;
    }

    public String getY() {
        return Y;
    }

    public void setY(String Y) {
        this.Y = Y;
    }

    public String getC() {
        return c;
    }

    public void setC(String c) {
        this.c = c;
    }

    public String getSx() {
        return sx;
    }

    public void setSx(String sx) {
        this.sx = sx;
    }

    public String getSy() {
        return sy;
    }

    public void setSy(String sy) {
        this.sy = sy;
    }

    public String getGlCurvce() {
        return GlCurvce;
    }

    public void setGlCurvce(String GlCurvce) {
        this.GlCurvce = GlCurvce;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.X != null) job.add("X", X);
        if (this.Y != null) job.add("Y", Y);
        if (this.c != null) job.add("c", c);
        if (this.sx != null) job.add("sx", sx);
        if (this.sy != null) job.add("sy", sy);
        if (this.GlCurvce != null) job.add("GlCurvce", GlCurvce);
        return job.build();
    }
}
