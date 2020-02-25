/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2.tpm;

import com.strongkey.skce.utilities.TPMConstants;

public class TPMScheme implements TPMMarshallable {
    private final short scheme;
    private final short schemeDetails;

    public TPMScheme(short scheme) {
        this.scheme = scheme;
        this.schemeDetails = TPMConstants.TPM_ALG_ERROR;
    }

    public TPMScheme(short scheme, short schemeDetails) {
        this.scheme = scheme;
        this.schemeDetails = schemeDetails;
    }

    @Override
    public byte[] marshalData() {
        if (scheme == TPMConstants.TPM_ALG_NULL) {       //TODO check for other schemes that do not have schemeDetails
            return Marshal.marshalObjects(
                    scheme);
        } else {
            return Marshal.marshalObjects(
                    scheme,
                    schemeDetails);
        }
    }
}
