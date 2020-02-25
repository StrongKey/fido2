/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido2.tpm;

public class TPM2B implements TPMMarshallable {
    private byte[] data;

    public TPM2B() {
        this.data = new byte[0];
    }

    public TPM2B(byte[] data) {
        this.data = data;                                                                                          //    -> throw exception before that can happen
    }

    public final short getSize(){
        return (short) data.length;
    }

    public final byte[] getData(){
        return data;
    }

    @Override
    public byte[] marshalData() {
        return Marshal.marshalObjectsWithPrependedSizeShort(
                data);
    }
}
