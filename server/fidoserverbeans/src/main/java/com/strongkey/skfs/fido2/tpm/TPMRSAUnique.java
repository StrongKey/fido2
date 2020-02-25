/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2.tpm;

import java.util.Arrays;

public class TPMRSAUnique extends TPM2B implements TPMUnique {

    public TPMRSAUnique(byte[] data) {
        super(data);
    }

    @Override
    public boolean equals(TPMUnique unique) {
        if (!(unique instanceof TPMRSAUnique)) {
            return false;
        }

        TPMRSAUnique rsaunique = (TPMRSAUnique) unique;

        return Arrays.equals(this.getData(), rsaunique.getData());
    }

}
