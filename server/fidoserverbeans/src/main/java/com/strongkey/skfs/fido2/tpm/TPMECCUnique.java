/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2.tpm;

import com.strongkey.skce.utilities.TPMConstants;
import java.util.Arrays;
import java.util.InputMismatchException;

public class TPMECCUnique implements TPMUnique {
    private TPM2B x;
    private TPM2B y;

    private TPMECCUnique(TPM2B x, TPM2B y){
        this.x = x;
        this.y = y;
    }

    public static TPMECCUnique unmarshal(byte[] bytes){
        int pos = 0;
        short xSize = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;
        TPM2B x = new TPM2B(Arrays.copyOfRange(bytes, pos, pos+xSize));
        pos += xSize;

        short ySize = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;
        TPM2B y = new TPM2B(Arrays.copyOfRange(bytes, pos, pos+ySize));
        pos += ySize;

        if(pos != bytes.length){
            throw new InputMismatchException("TPMCertifyInfo failed to unmarshal");
        }

        return new TPMECCUnique(x, y);
    }

    @Override
    public boolean equals(TPMUnique unique) {
        if(!(unique instanceof TPMECCUnique)){
            return false;
        }

        TPMECCUnique eccunique = (TPMECCUnique) unique;

        return Arrays.equals(this.x.getData(), eccunique.getX().getData())
                && Arrays.equals(this.y.getData(), eccunique.getY().getData());

    }

    public TPM2B getX(){
        return x;
    }

    public TPM2B getY(){
        return y;
    }

    @Override
    public byte[] marshalData() {
        return Marshal.marshalObjects(
                x,
                y);
    }
}
