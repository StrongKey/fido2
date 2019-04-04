/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.math.BigInteger;

public class DisplayPNGCharacteristicsDescriptor {
    private BigInteger width;
    private BigInteger height;
    private Short bitDepth;
    private Short colorType;
    private Short compression;
    private Short filter;
    private Short interlace;
    private RGBPaletteEntry plte;

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
}
