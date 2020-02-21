/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * Abstract class to carry predetermined static information
 */

package com.strongauth.skfs.fido2.artifacts;

public abstract class FIDO2KeyObject
{
    int alg;
    int kty;

    static final int CRV_LABEL  = -1;
    static final int X_LABEL    = -2;
    static final int Y_LABEL    = -3;
    static final int N_LABEL    = -1;
    static final int E_LABEL    = -2;
    static final int KTY_LABEL  =  1;
    static final int ALG_LABEL  =  3;

    public int getAlg() {
        return alg;
    }

    public int getKty() {
        return kty;
    }
}
