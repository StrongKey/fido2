/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

public class BiometricAccuracyDescriptor {
    private Double FAR;
    private Double FRR;
    private Double EER;
    private Double FAAR;
    private Integer maxRegerenceDataSets;
    private Integer maxRetries;
    private Integer blockSlowdown;

    public Double getFAR() {
        return FAR;
    }

    public void setFAR(Double FAR) {
        this.FAR = FAR;
    }

    public Double getFRR() {
        return FRR;
    }

    public void setFRR(Double FRR) {
        this.FRR = FRR;
    }

    public Double getEER() {
        return EER;
    }

    public void setEER(Double EER) {
        this.EER = EER;
    }

    public Double getFAAR() {
        return FAAR;
    }

    public void setFAAR(Double FAAR) {
        this.FAAR = FAAR;
    }

    public Integer getMaxRegerenceDataSets() {
        return maxRegerenceDataSets;
    }

    public void setMaxRegerenceDataSets(Integer maxRegerenceDataSets) {
        this.maxRegerenceDataSets = maxRegerenceDataSets;
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
