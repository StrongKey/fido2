/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.math.BigInteger;

public class VerificationMethodDescriptor {
    private BigInteger userVerification;
    private CodeAccuracyDescriptor caDesc;
    private BiometricAccuracyDescriptor baDesc;
    private PatternAccuracyDescriptor paDesc;

    public BigInteger getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(BigInteger userVerification) {
        this.userVerification = userVerification;
    }

    public CodeAccuracyDescriptor getCaDesc() {
        return caDesc;
    }

    public void setCaDesc(CodeAccuracyDescriptor caDesc) {
        this.caDesc = caDesc;
    }

    public BiometricAccuracyDescriptor getBaDesc() {
        return baDesc;
    }

    public void setBaDesc(BiometricAccuracyDescriptor baDesc) {
        this.baDesc = baDesc;
    }

    public PatternAccuracyDescriptor getPaDesc() {
        return paDesc;
    }

    public void setPaDesc(PatternAccuracyDescriptor paDesc) {
        this.paDesc = paDesc;
    }
    
    
}
