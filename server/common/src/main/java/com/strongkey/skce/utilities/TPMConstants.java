/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.utilities;

public class TPMConstants {

    public static final int TPM_GENERATED_VALUE = 0xff544347;
    public static final short TPM_ST_ATTEST_CERTIFY = (short) 0x8017;

    public static final short TPM_ALG_ERROR = (short) 0x0000;
    public static final short TPM_ALG_RSA = (short) 0x0001;
    public static final short TPM_ALG_NULL = (short) 0x0010;
    public static final short TPM_ALG_ECC = (short) 0x0023;

    public static final short TPM_ECC_NIST_P256 = (short) 0x0003;
    public static final short TPM_ECC_NIST_P384 = (short) 0x0004;
    public static final short TPM_ECC_NIST_P521 = (short) 0x0005;

    public static final short TPM_ALG_SHA1 = (short) 0x0004;
    public static final short TPM_ALG_SHA256 = (short) 0x000B;
    public static final short TPM_ALG_SHA384 = (short) 0x000C;
    public static final short TPM_ALG_SHA512 = (short) 0x000D;

    public static final int SIZEOFLONG = 8;
    public static final int SIZEOFINT = 4;
    public static final int SIZEOFSHORT = 2;
    public static final int SIZEOFBYTE = 1;
    public static final int SIZEOFTAG = 2;
}
