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

public class TPMCertifyInfo implements TPMMarshallable {
    TPM2B name;
    TPM2B qualifiedName;

    public TPMCertifyInfo(TPM2B name, TPM2B qualifiedName){
        this.name = name;
        this.qualifiedName = qualifiedName;
    }

    public static TPMCertifyInfo unmarshal(byte[] bytes){
        int pos = 0;
        int sizeOfName = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;
        TPM2B name = new TPM2B(Arrays.copyOfRange(bytes, pos, pos+sizeOfName));
        pos += sizeOfName;
        int sizeOfQualifiedName = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos + TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;
        TPM2B qualifiedName = new TPM2B(Arrays.copyOfRange(bytes, pos, pos + sizeOfQualifiedName));
        pos += sizeOfQualifiedName;

        if(pos != bytes.length){
            throw new InputMismatchException("TPMCertifyInfo failed to unmarshal");
        }

        return new TPMCertifyInfo(name, qualifiedName);
    }

    public TPM2B getName() {
        return name;
    }

    @Override
    public byte[] marshalData() {
        return Marshal.marshalObjects(
                name,
                qualifiedName);
    }
}
