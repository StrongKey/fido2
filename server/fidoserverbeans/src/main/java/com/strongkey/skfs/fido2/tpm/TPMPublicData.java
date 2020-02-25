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

public class TPMPublicData implements TPMMarshallable {
    private final short algType;
    private final short nameAlg;
    private final int objectAttributes;
    private final TPM2B policyName;
    private final TPMParameters parameters;
    private final TPMUnique unique;

    public TPMPublicData(short algType, short nameAlg, int objectAttributes,
            TPM2B policyName, TPMParameters parameters, TPMUnique unique){
        this.algType = algType;
        this.nameAlg = nameAlg;
        this.objectAttributes = objectAttributes;
        this.policyName = policyName;
        this.parameters = parameters;
        this.unique = unique;
    }

    public static TPMPublicData unmarshal(byte[] bytes){
        int pos = 0;
//        int size = Marshal.stream16ToInt(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
//        pos += TPMConstants.SIZEOFSHORT;
//        int initPos = pos;

        //algType
        short algType = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;

        //nameAlg
        short nameAlg = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;

        //objectAttributes
        int objectAttributes = Marshal.stream32ToInt(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFINT));
        pos += TPMConstants.SIZEOFINT;

        //policyName
        short policyNameSize = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
        pos += TPMConstants.SIZEOFSHORT;
        TPM2B policyName = new TPM2B(Arrays.copyOfRange(bytes, pos, pos+policyNameSize));
        pos += policyNameSize;

        TPMParameters parameters = null;
        TPMUnique unique = null;
        switch(algType){
            case(TPMConstants.TPM_ALG_RSA):
                short symmAlg = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                short symmKeyBits = 0;
                short symmMode = TPMConstants.TPM_ALG_NULL;
                if(symmAlg != TPMConstants.TPM_ALG_NULL){
                    symmKeyBits = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                    pos += TPMConstants.SIZEOFSHORT;
                    symmMode = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                    pos += TPMConstants.SIZEOFSHORT;
                }
                TPMSymmetricStruct symmBits = new TPMSymmetricStruct(symmAlg, symmKeyBits, symmMode);
                short scheme = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                short schemeHashAlg = TPMConstants.TPM_ALG_ERROR;
                if(scheme != TPMConstants.TPM_ALG_NULL){
                    schemeHashAlg = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                    pos += TPMConstants.SIZEOFSHORT;
                }
                short keyBits = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                int exponent = Marshal.stream32ToInt(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFINT));
                pos += TPMConstants.SIZEOFINT;
                parameters = new TPMRSAParameters(symmBits, new TPMScheme(scheme, schemeHashAlg), keyBits, exponent);
                short uniqueSize = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                unique = new TPMRSAUnique(Arrays.copyOfRange(bytes, pos, pos+uniqueSize));
                pos += uniqueSize;
                break;
            case(TPMConstants.TPM_ALG_ECC):
                short ECCSymmAlg = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                short ECCSymmKeyBits = 0;
                short ECCSymmMode = TPMConstants.TPM_ALG_NULL;
                if(ECCSymmAlg != TPMConstants.TPM_ALG_NULL){
                    ECCSymmKeyBits = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                    pos += TPMConstants.SIZEOFSHORT;
                    ECCSymmMode = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                    pos += TPMConstants.SIZEOFSHORT;
                }
                TPMSymmetricStruct ECCSymmBits = new TPMSymmetricStruct(ECCSymmAlg, ECCSymmKeyBits, ECCSymmMode);
                short ECCScheme = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                short ECCSchemeHashAlg = TPMConstants.TPM_ALG_ERROR;
                if(ECCScheme != TPMConstants.TPM_ALG_NULL){
                    ECCSchemeHashAlg = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                    pos += TPMConstants.SIZEOFSHORT;
                }
                short ECCCurveID = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                short ECCKdfScheme = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                pos += TPMConstants.SIZEOFSHORT;
                short ECCKdfHashAlg = TPMConstants.TPM_ALG_ERROR;
                if(ECCKdfScheme != TPMConstants.TPM_ALG_NULL){
                    ECCKdfHashAlg = Marshal.stream16ToShort(Arrays.copyOfRange(bytes, pos, pos+TPMConstants.SIZEOFSHORT));
                    pos += TPMConstants.SIZEOFSHORT;
                }
                parameters = new TPMECCParameters(ECCSymmBits, new TPMScheme(ECCScheme, ECCSchemeHashAlg), ECCCurveID, new TPMScheme(ECCKdfScheme, ECCKdfHashAlg));
                unique = TPMECCUnique.unmarshal(Arrays.copyOfRange(bytes, pos, bytes.length));
                break;
            default:
                throw new UnsupportedOperationException("Unsupported algorithm");
        }

        if(pos != bytes.length){
            throw new InputMismatchException("TPMCertifyInfo failed to unmarshal");
        }

        return new TPMPublicData(algType, nameAlg, objectAttributes,
            policyName, parameters, unique);
    }

    public short getAlgType() {
        return algType;
    }

    public TPMParameters getParameters() {
        return parameters;
    }

    public TPMUnique getUnique() {
        return unique;
    }

    public short getNameAlg(){
        return nameAlg;
    }

    @Override
    public byte[] marshalData(){
        return Marshal.marshalObjectsWithPrependedSizeShort(
                algType,
                nameAlg,
                objectAttributes,
                policyName,
                parameters,
                unique);
    }

    public byte[] getData() {
        return Marshal.marshalObjects(
                algType,
                nameAlg,
                objectAttributes,
                policyName,
                parameters,
                unique);
    }
}
