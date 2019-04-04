/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

public class CodeAccuracyDescriptor {
    private Integer base;
    private Integer minLength;
    private Short maxRetries;
    private Short blockSlowdown;

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
}
