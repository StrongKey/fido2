/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido2.tpm;

public class TPMRSAParameters implements TPMParameters {
    private final TPMSymmetricStruct symmBits;

    //key scheme parameters
    private final TPMScheme scheme;

    private final short keyBits;
    private final int exponent;

    public TPMRSAParameters(TPMSymmetricStruct symmBits, TPMScheme scheme, short keyBits, int exponent){
        this.symmBits = symmBits;
        this.scheme = scheme;
        this.keyBits = keyBits;
        this.exponent = exponent;
    }

    public int getExponent() {
        return exponent;
    }

    @Override
    public byte[] marshalData() {
        return Marshal.marshalObjects(
                symmBits,
                scheme,
                keyBits,
                exponent);
    }
}
