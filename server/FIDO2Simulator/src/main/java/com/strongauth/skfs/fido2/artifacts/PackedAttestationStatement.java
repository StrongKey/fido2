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
 * A "packed" attestation statement provided from this simulator.
 */

package com.strongauth.skfs.fido2.artifacts;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;



public class PackedAttestationStatement implements FIDO2AttestationStatement
{
    private long alg;
    private byte[] signature;
    private byte[] ecdaaKeyId;
    private byte[] x5c = null;
    private String attestationType;

    static {
        Security.addProvider(new BouncyCastleFipsProvider());
    }

    public PackedAttestationStatement(long alg, String attestationType, byte[] ecdaaKeyId)
    {
        this.alg = alg;
        this.attestationType = attestationType;
        this.ecdaaKeyId = ecdaaKeyId;

        x5c = Common.getAttestationCertBytes();

    }

    @Override
    public byte[] signwithAttestationKey(byte[] tbs) {
        try {
            // Retrieve private-key
            String attpvkBase64 = Constants.ATTESTATION_FIDO2PVK_BASE64;
            byte[] attpvkSpec = Base64.getUrlDecoder().decode(attpvkBase64);

            KeyFactory kf=KeyFactory.getInstance("EC");
            PKCS8EncodedKeySpec specPriv = new PKCS8EncodedKeySpec(attpvkSpec);
            PrivateKey attpvk = kf.generatePrivate(specPriv);

            Signature sig = Signature.getInstance("SHA256withECDSA", "BCFIPS");
            sig.initSign(attpvk, new SecureRandom());
            sig.update(tbs);
            signature = sig.sign();
            return signature;
        }  catch (InvalidKeySpecException ex) {
            Logger.getLogger(PackedAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(PackedAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InvalidKeyException ex) {
            Logger.getLogger(PackedAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SignatureException ex) {
            Logger.getLogger(PackedAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
        } catch (NoSuchProviderException ex) {
            Logger.getLogger(PackedAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;

    }

    @Override
    public long getAlg() {
        return alg;
    }

    @Override
    public byte[] getX5c() {
        return x5c;
    }

    @Override
    public String getAttestationType() {
        return attestationType;
    }

    @Override
    public byte[] signwithCredentialKey(PrivateKey pvtKey, byte[] tbs) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA", "BCFIPS");
            sig.initSign(pvtKey, new SecureRandom());
            sig.update(tbs);
            signature = sig.sign();
            return signature;
        } catch (InvalidKeyException | NoSuchAlgorithmException |
                NoSuchProviderException | SignatureException ex)
        {
            Logger.getLogger(PackedAttestationStatement.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
    }
}
