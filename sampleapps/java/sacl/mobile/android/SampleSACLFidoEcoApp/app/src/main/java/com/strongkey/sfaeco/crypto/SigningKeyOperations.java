/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2022 StrongAuth, Inc. (DBA StrongKey)
 *
 * **********************************************
 *
 * 888b    888          888
 * 8888b   888          888
 * 88888b  888          888
 * 888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 * 888 Y88b888 d88""88b 888    d8P  Y8b 88K
 * 888  Y88888 888  888 888    88888888 "Y8888b.
 * 888   Y8888 Y88..88P Y88b.  Y8b.          X88
 * 888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * Signing key operations
 */

package com.strongkey.sfaeco.crypto;

import android.util.Log;

import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sfaeco.main.SfaSharedDataModel;

import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.asn1.ASN1Encodable;
import org.spongycastle.asn1.ASN1Integer;
import org.spongycastle.asn1.ASN1Sequence;
import org.spongycastle.asn1.DERSequence;
import org.spongycastle.asn1.util.ASN1Dump;
import org.spongycastle.cert.X509CertificateHolder;
import org.spongycastle.cert.jcajce.JcaX509CertificateConverter;
import org.spongycastle.openssl.PEMParser;
import org.spongycastle.openssl.jcajce.JcaPEMWriter;
import org.spongycastle.util.Arrays;
import org.spongycastle.util.BigIntegers;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Base64;

public class SigningKeyOperations {

    private static String TAG = SigningKeyOperations.class.getSimpleName();
    static SfaSharedDataModel sfaSharedDataModel = MainActivity.sfaSharedDataModel;

    /**
     * Get the signing key from AndroidKeystore initialized in a Signature object
     * @param alias String with the alias of the "shopping" signing key
     * @return Signature
     */
    public static Signature getSignatureObject(String alias) {
        KeyStore mKeystore = null;
        try {
            mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);
            if (mKeystore.containsAlias(alias)) {
                Log.v(TAG, "Shopping key exists");

                KeyStore.Entry entry = mKeystore.getEntry(alias, null);
                if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                    Log.w(TAG, "Not a PrivateKeyEntry");
                    return null;
                }
                Log.v(TAG, "Got shopping key entry: " + entry);

                // Get the certificate and place it in shared data model
                X509Certificate cert = (X509Certificate) mKeystore.getCertificate(alias);
                sfaSharedDataModel.setX509Certificate(cert);
                Log.v(TAG, "Subject DN: " + cert.getSubjectX500Principal().getName());

                // Init Signature object
                Signature signature = Signature.getInstance("SHA256withECDSA");
                signature.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
                Log.v(TAG, "Initialized signature object");
                return signature;
            } else {
                Log.w(TAG, "Shopping key does not exist: " + alias);
            }
        } catch (CertificateException | IOException | NoSuchAlgorithmException | KeyStoreException
                | UnrecoverableEntryException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Generates an RFC-7515 Json Web Signature (JWS) on the payload transaction. It implies that
     * the "shopping" signing key is already generated and stored in AndroidKeystore
     * @param signature Signature object with the preloaded signing key
     * @param payload String containing the "Dynamic Linking" payment details
     * @return String with the JWS
     */
    public static String generateX509JsonWebSignature(Signature signature, String payload) {

        String alg = null;
        String pemcert = null;
        JSONObject jws = null;

        try {
            // Get necessary objects
            Base64.Encoder encoder = Base64.getUrlEncoder();
            X509Certificate certificate = sfaSharedDataModel.getX509Certificate();
            if (certificate != null) {
                // Write PEM-encoded certificate to string
                StringWriter sWriter = new StringWriter();
                JcaPEMWriter pemWriter = new JcaPEMWriter(sWriter);
                pemWriter.writeObject(certificate);
                pemWriter.close();
                pemcert = sWriter.toString();
                Log.v(TAG, "PEM Certificate:\n" + pemcert);

                // Check algorithm - if EC, use ES256 (for now)
                String pbkalg = certificate.getPublicKey().getAlgorithm();
                if (pbkalg.equalsIgnoreCase("EC"))
                    alg = "ES256";
            } else {
                Log.e(TAG, "Could not find shopping certificate");
                return null;
            }

            // Assemble JWS elements
            JSONObject headerjson = new JSONObject()
                    .put("alg", alg)
                    .put("x5c", pemcert);
            Log.v(TAG, "protected:\n" + headerjson);

            String b64payload = encoder.withoutPadding().encodeToString(payload.getBytes(StandardCharsets.UTF_8));
            Log.v(TAG, "\nPayload:\n-------\n" + payload);
            Log.v(TAG, "B64U: " + b64payload);

            // Assemble plaintext for signing
            JSONObject plaintext = new JSONObject()
                    .put("payload", b64payload)
                    .put("protected", headerjson);
            Log.v(TAG, "\nPlaintext:\n---------\n" + plaintext);

            // Sign the plaintext
            signature.update(plaintext.toString().getBytes(StandardCharsets.UTF_8));
            byte[] signed = signature.sign();
            String b64sig = encoder.withoutPadding().encodeToString(signed);
            Log.v(TAG, "\nJava Signature:\n" + b64sig);

            ASN1Sequence sigSeq = ASN1Sequence.getInstance(signed);

            byte[] jsonSig = Arrays.concatenate(
                    BigIntegers.asUnsignedByteArray(32, ASN1Integer.getInstance(sigSeq.getObjectAt(0)).getValue()),
                    BigIntegers.asUnsignedByteArray(32, ASN1Integer.getInstance(sigSeq.getObjectAt(1)).getValue()));
            Log.v(TAG, "\njsonSig " + ASN1Dump.dumpAsString(sigSeq));
            String b64jsonSig = encoder.withoutPadding().encodeToString(jsonSig);
            Log.v(TAG, "B64U R||S:\n---------\n" + b64jsonSig);

            // Finally, the JWS
            jws = new JSONObject()
                    .put("payload", plaintext.getString("payload"))
                    .put("protected", headerjson)
                    .put("signature", b64jsonSig);
            Log.i(TAG, "\nJWS\n---\n" + jws);
            return jws.toString();

        } catch (IOException | SignatureException | JSONException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Verify the JWS object
     * @param jwsString String containing the Json Web Signature
     * @return JSONObject with certificate details and verification of signature
     */
    public static JSONObject verifyX509JsonWebSignature(String jwsString) {

        boolean verified = false;

        try {
            Base64.Decoder decoder = Base64.getUrlDecoder();

            // Get public key from certificate
            Log.v(TAG, "\nJWS\n---\n" + jwsString);
            JSONObject jws = new JSONObject(jwsString);
            Log.v(TAG, "\npayload plaintext: " + new String(decoder.decode(jws.getString("payload")), StandardCharsets.UTF_8));
            String pemcert = jws.getJSONObject("protected").getString("x5c");
            PEMParser parser = new PEMParser(new StringReader(pemcert));
            X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
            X509Certificate certificate = new JcaX509CertificateConverter().getCertificate(holder);
            PublicKey publicKey = certificate.getPublicKey();

            // Assemble plaintext from JWS
            JSONObject vplaintext = new JSONObject()
                    .put("payload", jws.getString("payload"))
                    .put("protected", jws.getJSONObject("protected"));
            Log.v(TAG, "\nvplaintext:\n----------\n" + vplaintext);

            // Verify signature
            byte[] rsbytes = decoder.decode(jws.getString("signature"));
            Signature signature = Signature.getInstance("SHA256withECDSA");
            signature.initVerify(publicKey);
            signature.update(vplaintext.toString().getBytes(StandardCharsets.UTF_8));
            byte[] ecdsaSig = new DERSequence(new ASN1Encodable[]
                    {new ASN1Integer(new BigInteger(1, Arrays.copyOfRange(rsbytes, 0, 32))),
                            new ASN1Integer(new BigInteger(1, Arrays.copyOfRange(rsbytes, 32, 64)))
                    }).getEncoded();

            verified = signature.verify(ecdsaSig);
            if (verified) {
                Log.v(TAG, "\nJWS verified payload correctly!!\n");
            } else {
                Log.e(TAG,"JWS does not verify payload");
            }

            // Get certificate details
            JSONObject response = new JSONObject()
                    .put("dn", certificate.getSubjectDN())
                    .put("serial", certificate.getSerialNumber())
                    .put("start", certificate.getNotBefore())
                    .put("end", certificate.getNotAfter())
                    .put("verified", verified);
            return response;

        } catch (NoSuchAlgorithmException | IOException | InvalidKeyException |
                SignatureException | JSONException | CertificateException ex) {
            Log.e(TAG, ex.getLocalizedMessage());
        }
        return null;
    }
}
