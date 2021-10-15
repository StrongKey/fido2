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
 * Attested credential data object
 */

package com.strongauth.skfs.fido2.artifacts;

import java.nio.ByteBuffer;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FIDO2AttestedCredentialData
{
    private byte[] aaguid;
    private byte[] credentialId;
    private PublicKey publicKey;

    public FIDO2AttestedCredentialData(byte[] aaguid, byte[] credentialId, PublicKey publicKey) {
        this.aaguid = aaguid;
        this.credentialId = credentialId;
        this.publicKey = publicKey;
    }

    // Getters and Setters
    public void setAaguid(byte[] aaguid) {
        this.aaguid = aaguid;
    }

    public void setCredentialId(byte[] credentialId) {
        this.credentialId = credentialId;
    }

    public void setPublicKey(PublicKey publicKey) {
        this.publicKey = publicKey;
    }

    public byte[] getAaguid() {
        return aaguid;
    }

    public byte[] getCredentialId() {
        return credentialId;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    // Returns size of FIDO2AttestedCredentialData
    public byte[] encodeAttCredData()
    {
//        System.out.println("credid length = " + credentialId.length);
        byte[] credentialPublicKey = null;
        try {
            credentialPublicKey = Common.coseEncodePublicKey(publicKey);
        } catch (Exception ex) {
            Logger.getLogger(FIDO2AttestedCredentialData.class.getName()).log(Level.SEVERE, null, ex);
        }

        int pklen = 0;
        if (credentialPublicKey != null) {
            pklen = credentialPublicKey.length;
//            System.out.println("pk length = " + pklen);
        }

        ByteBuffer credentialData = ByteBuffer.allocate(16 + 2 + credentialId.length + pklen);
        credentialData.put(aaguid);
        credentialData.position(16);
        credentialData
            .putShort((short) credentialId.length)
            .put(credentialId)
            .put(credentialPublicKey);
        return credentialData.array();
    }
}
