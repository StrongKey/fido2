/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.math.BigInteger;

public class PatternAccuracyDescriptor {
    private BigInteger minComplexity;
    private Integer maxRetries;
    private Integer blockSlowdown;

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
}
