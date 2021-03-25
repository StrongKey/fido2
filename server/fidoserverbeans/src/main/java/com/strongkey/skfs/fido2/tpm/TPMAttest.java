/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido2.tpm;

import com.strongkey.skce.utilities.TPMConstants;
import java.util.Arrays;

public class TPMAttest implements TPMMarshallable {
    private final int magic;
    private final short type;
    private final TPM2B qualifiedSigner;
    private final TPM2B extraData;
    private final TPMClockInfo clockInfo;
    private final long firmwareVersion;
    private final TPMCertifyInfo attested; //TODO fix hardcoded type (implementation is more restrictive
                             //than the specification).

    public TPMAttest(int magic, short type, TPM2B qualifiedSigner,
            TPM2B extraData, TPMClockInfo clockInfo, long firmwareVersion,
            TPMCertifyInfo attested){
        this.magic = magic;
        this.type = type;
        this.qualifiedSigner = qualifiedSigner;
        this.extraData = extraData;
        this.clockInfo = clockInfo;
        this.firmwareVersion = firmwareVersion;
        this.attested = attested;
    }

    public static TPMAttest unmarshal(byte[] bytes){
        int pos = 0;
        int magic = Marshal.stream32ToInt(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFINT));
        pos += TPMConstants.SIZEOFINT;
        short type = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFTAG));
        pos += TPMConstants.SIZEOFTAG;
        short qualifiedSignerSize = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;
        TPM2B qualifiedSigner = new TPM2B(Arrays.copyOfRange(bytes, pos, pos+qualifiedSignerSize));
        pos += qualifiedSignerSize;
        short extraDataSize = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;
        TPM2B extraData = new TPM2B(Arrays.copyOfRange(bytes, pos, pos+extraDataSize));
        pos += extraDataSize;
        int sizeOfClockInfo = TPMConstants.SIZEOFLONG+TPMConstants.SIZEOFINT+TPMConstants.SIZEOFINT+TPMConstants.SIZEOFBYTE;
        TPMClockInfo clockInfo = TPMClockInfo.unmarshal(Arrays.copyOfRange(bytes, pos, pos+sizeOfClockInfo));
        pos += sizeOfClockInfo;
        long firmwareVersion = Marshal.stream64ToLong(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFLONG));
        pos += TPMConstants.SIZEOFLONG;
        TPMCertifyInfo attested = TPMCertifyInfo.unmarshal(Arrays.copyOfRange(bytes, pos, bytes.length));
        return new TPMAttest(magic, type, qualifiedSigner, extraData, clockInfo, firmwareVersion, attested);
    }

    public int getMagic() {
        return magic;
    }

    public short getType() {
        return type;
    }

    public TPM2B getExtraData() {
        return extraData;
    }

    public TPMCertifyInfo getAttested() {
        return attested;
    }

    @Override
    public byte[] marshalData() {
        return Marshal.marshalObjects(
                magic,
                type,
                qualifiedSigner,
                extraData,
                clockInfo,
                firmwareVersion,
                attested);
    }
}
