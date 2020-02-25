/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido2.tpm;

import com.strongkey.skce.utilities.TPMConstants;
import org.bouncycastle.util.Arrays;

public class TPMClockInfo implements TPMMarshallable {
    private final long clock;
    private final int resetCount;
    private final int restartCount;
    private final byte safe;

    public TPMClockInfo(long clock, int resetCount, int restartCount, byte safe){
        this.clock = clock;
        this.resetCount = resetCount;
        this.restartCount = restartCount;
        this.safe = safe;
    }

    public static TPMClockInfo unmarshal(byte[] bytes){
        int pos = 0;
        long clock = Marshal.stream64ToLong(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFLONG));
        pos += TPMConstants.SIZEOFLONG;
        int resetCount = Marshal.stream32ToInt(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFINT));
        pos += TPMConstants.SIZEOFINT;
        int restartCount = Marshal.stream32ToInt(Arrays.copyOfRange(bytes, pos, pos + TPMConstants.SIZEOFINT));
        pos += TPMConstants.SIZEOFINT;
        byte safe = Arrays.copyOfRange(bytes, pos, pos + TPMConstants.SIZEOFBYTE)[0];
        pos += TPMConstants.SIZEOFBYTE;
        return new TPMClockInfo(clock, resetCount, restartCount, safe);
    }

    @Override
    public byte[] marshalData() {
        return Marshal.marshalObjects(
                clock,
                resetCount,
                restartCount,
                safe);
    }
}
