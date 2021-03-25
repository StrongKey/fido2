/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.logging.Level;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class FIDO2AuthenticatorData {

    private byte[] rpIdHash;
    private byte flags;
    private boolean isUserPresent;
    private boolean isUserVerified;
    private boolean isAttestedCredentialData;
    private boolean isExtensionData;
    private byte[] counterValue;
//    private Integer counter;
    private FIDO2AttestedCredentialData attCredData;
    private FIDO2Extensions ext;
    private byte[] authDataDecoded;

    public static final int COUNTER_VALUE_BYTES = 4;

    public byte[] getRpIdHash() {
        return rpIdHash;
    }

    public byte getFlags() {
        return flags;
    }

    public boolean isUserPresent() {
        return isUserPresent;
    }

    public boolean isUserVerified() {
        return isUserVerified;
    }

    public boolean isAttestedCredentialData() {
        return isAttestedCredentialData;
    }

    public boolean isExtensionData() {
        return isExtensionData;
    }

    public byte[] getCounterValue() {
        return counterValue;
    }

    public int getCounterValueAsInt() {
        return Integer.parseInt(Hex.toHexString(counterValue), 16);
    }

    public FIDO2AttestedCredentialData getAttCredData() {
        return attCredData;
    }

    public FIDO2Extensions getExt() {
        return ext;
    }

    public byte[] getAuthDataDecoded() {
        return authDataDecoded;
    }


    public void decodeAuthData(byte[] authData) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {
        authDataDecoded = authData;
        int index = 0;
        rpIdHash = new byte[32];
        System.arraycopy(authData, 0, rpIdHash, 0, 32);
        index += 32;
        flags = authData[index++];

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "rpidHash : " + Base64.toBase64String(rpIdHash));

        isUserPresent = ((flags >> 0) & 1) == 1;
        isUserVerified = ((flags >> 2) & 1) == 1;
        isAttestedCredentialData = ((flags >> 6) & 1) == 1;
        isExtensionData = ((flags >> 7) & 1) == 1;

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "The user is " + (isUserPresent ? "present" : "not present"));
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "The user is " + (isUserVerified ? "verified" : "not verified"));
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Credential Data is " + (isAttestedCredentialData ? "present" : "not present"));
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "ExtensionData is " + (isExtensionData ? "present" : "not present"));

        counterValue = new byte[COUNTER_VALUE_BYTES];
        System.arraycopy(authData, index, counterValue, 0, COUNTER_VALUE_BYTES);
        index += COUNTER_VALUE_BYTES;
//        counter = ByteBuffer.wrap(counterValue).getInt();
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "counter Hex: " + Hex.toHexString(counterValue));

        int attestedCredentialDataLength;
        if (isAttestedCredentialData) {
            attCredData = new FIDO2AttestedCredentialData();
            byte[] remainingData = new byte[authData.length - index];
            System.arraycopy(authData, index, remainingData, 0, authData.length - index);
            attestedCredentialDataLength = attCredData.decodeAttCredData(remainingData);
            index += attestedCredentialDataLength;
        }

        //ignore extensions for now
        int extensionsLength;
        if(isExtensionData){
            ext = new FIDO2Extensions();
            byte[] remainingData = new byte[authData.length - index];
            System.arraycopy(authData, index, remainingData, 0, authData.length - index);
            extensionsLength = ext.decodeExtensions(remainingData);
            index += extensionsLength;
        }

        if(authData.length != index){
            int extraData = authData.length - index;
            throw new IllegalArgumentException("AuthenicatorData contains invalid CBOR: "
                + extraData + " unknown bytes");
        }
    }
}
