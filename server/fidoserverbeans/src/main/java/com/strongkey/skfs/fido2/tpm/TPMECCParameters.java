/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2.tpm;

public class TPMECCParameters implements TPMParameters {
    private final TPMSymmetricStruct symmBits;

    private final TPMScheme scheme;

    private final short curveID;

    //These variables are a bit of a stub in the specification, as they do not
    //really do anything and are suppose to be hardcoded to null values
    private final TPMScheme kdfScheme;


    public TPMECCParameters(TPMSymmetricStruct symmBits, TPMScheme scheme,
            short curveID, TPMScheme kdfScheme) {
        this.symmBits = symmBits;
        this.scheme = scheme;
        this.curveID = curveID;
        this.kdfScheme = kdfScheme;
    }

    public short getCurveID() {
        return curveID;
    }

    @Override
    public byte[] marshalData() {
        return Marshal.marshalObjects(
                symmBits,
                scheme,
                curveID,
                kdfScheme);
    }
}
