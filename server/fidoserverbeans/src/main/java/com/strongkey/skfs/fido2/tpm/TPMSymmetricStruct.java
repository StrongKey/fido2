/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido2.tpm;

import com.strongkey.skce.utilities.TPMConstants;

class TPMSymmetricStruct implements TPMMarshallable {
    private final short alg;
    private final short keyBits;
    private final short mode;
    
    public TPMSymmetricStruct(short alg, short keyBits, short mode) {
        this.alg = alg;
        this.keyBits = keyBits;
        this.mode = mode;
    }
    
    public short getAlg() {
        return alg;
    }

    public short getKeyBits() {
        return keyBits;
    }
    
    public short getMode() {
        return mode;
    }

    @Override
    public byte[] marshalData() {
        if (this.alg == TPMConstants.TPM_ALG_NULL) {
            return Marshal.marshalObjects(alg);
        }

        return Marshal.marshalObjects(
                alg,
                keyBits,
                mode);
    }
    
    
}
