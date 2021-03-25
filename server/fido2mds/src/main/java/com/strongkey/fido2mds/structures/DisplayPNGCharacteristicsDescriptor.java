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

public class DisplayPNGCharacteristicsDescriptor {
    private BigInteger width;
    private BigInteger height;
    private Short bitDepth;
    private Short colorType;
    private Short compression;
    private Short filter;
    private Short interlace;
    private RGBPaletteEntry plte;
    
    public DisplayPNGCharacteristicsDescriptor(JsonObject jsonInput) {
        if (jsonInput.containsKey("width")) width = jsonInput.getJsonNumber("width").bigIntegerValueExact();
        if (jsonInput.containsKey("height")) height = jsonInput.getJsonNumber("height").bigIntegerValueExact();
        if (jsonInput.containsKey("bitDepth")) bitDepth = ((Integer) jsonInput.getInt("bitDepth")).shortValue();
        if (jsonInput.containsKey("colorType")) colorType = ((Integer) jsonInput.getInt("colorType")).shortValue();
        if (jsonInput.containsKey("compression")) compression = ((Integer) jsonInput.getInt("compression")).shortValue();
        if (jsonInput.containsKey("filter")) filter = ((Integer) jsonInput.getInt("filter")).shortValue();
        if (jsonInput.containsKey("interlace")) interlace = ((Integer) jsonInput.getInt("interlace")).shortValue();
        if (jsonInput.containsKey("plte")) plte = new RGBPaletteEntry(jsonInput.getJsonObject("plte"));
    }

    public BigInteger getWidth() {
        return width;
    }

    public void setWidth(BigInteger width) {
        this.width = width;
    }

    public BigInteger getHeight() {
        return height;
    }

    public void setHeight(BigInteger height) {
        this.height = height;
    }

    public Short getBitDepth() {
        return bitDepth;
    }

    public void setBitDepth(Short bitDepth) {
        this.bitDepth = bitDepth;
    }

    public Short getColorType() {
        return colorType;
    }

    public void setColorType(Short colorType) {
        this.colorType = colorType;
    }

    public Short getCompression() {
        return compression;
    }

    public void setCompression(Short compression) {
        this.compression = compression;
    }

    public Short getFilter() {
        return filter;
    }

    public void setFilter(Short filter) {
        this.filter = filter;
    }

    public Short getInterlace() {
        return interlace;
    }

    public void setInterlace(Short interlace) {
        this.interlace = interlace;
    }

    public RGBPaletteEntry getPlte() {
        return plte;
    }

    public void setPlte(RGBPaletteEntry plte) {
        this.plte = plte;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.width != null) job.add("width", width);
        if (this.height != null) job.add("height", height);
        if (this.bitDepth != null) job.add("bitDepth", bitDepth);
        if (this.colorType != null) job.add("colorType", colorType);
        if (this.compression != null) job.add("compression", compression);
        if (this.filter != null) job.add("filter", filter);
        if (this.interlace != null) job.add("interlace", interlace);
        if (this.plte != null) job.add("plte", plte.toJsonObject());
        return job.build();
    }
}
