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
 * Authenticator Data structure from the W3C WebAuthn specification:
 * https://www.w3.org/TR/webauthn/#sec-authenticator-data
 */

package com.strongauth.skfs.fido2.artifacts;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import org.bouncycastle.util.encoders.Hex;

public class FIDO2AuthenticatorData
{
    private final byte[] rpIdHash;
    private final byte flags;
    private boolean isUserPresent;
    private boolean isUserVerified;
    private final boolean isAttestedCredentialData;
    private boolean isExtensionData;
    private byte[] counterValue;
    private final Integer counter;
    private FIDO2AttestedCredentialData attCredData;
    private final FIDO2Extensions ext;

    public static int COUNTER_VALUE_BYTES = 4;

    public FIDO2AuthenticatorData(byte[] rpIdHash, byte flags, boolean isAttestedCredentialData, Integer counter, FIDO2Extensions ext) {
        this.rpIdHash = rpIdHash;
        this.flags = flags;
        this.isAttestedCredentialData = isAttestedCredentialData;
        this.counter = counter;
        this.ext = ext;
    }

    // Getters and Setters
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

    // Creates binary data-structure
    public byte[] encodeAuthData(byte[] aaguid, byte[] credentialId, PublicKey pubKey)
    {
        byte[] attestedCredentialData = null;
        // Sets ATTESTED CREDENTIAL DATA, variable-byte array
        if (isAttestedCredentialData) {
            attCredData = new FIDO2AttestedCredentialData(aaguid, credentialId, pubKey);
            attestedCredentialData = attCredData.encodeAttCredData();
        }
        // 32-byte hash + 1-byte flags + 4 bytes signCount = 37 bytes
        ByteBuffer authndata = ByteBuffer.allocate(
                37 + (attestedCredentialData == null? 0: attestedCredentialData.length));

        authndata.put(rpIdHash);
        authndata.put(flags);
        authndata.putInt(counter);
        if (isAttestedCredentialData) {
            authndata.put(attestedCredentialData);
        }
        return authndata.array();
    }
}
