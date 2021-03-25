/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

public abstract class FIDO2KeyObject {

    long alg;
    long kty;
    static final int CRV_LABEL = -1;
    static final int X_LABEL = -2;
    static final int Y_LABEL = -3;
    static final int N_LABEL = -1;
    static final int E_LABEL = -2;
    static final int KTY_LABEL = 1;
    static final int ALG_LABEL = 3;

    public long getAlg() {
        return alg;
    }

    public long getKty() {
        return kty;
    }

}
