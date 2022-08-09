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
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * The implementation of generating an AndroidKeystore attestation that
 * will be sent to the FIDO server for verification.
 */

package com.strongkey.sacl.crypto;

import android.content.ContextWrapper;
import android.security.keystore.KeyInfo;
import android.security.keystore.KeyProperties;
import android.security.keystore.UserNotAuthenticatedException;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.cbor.CborEncoder;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.spongycastle.util.encoders.Hex;
import org.spongycastle.util.io.pem.PemObject;
import org.spongycastle.util.io.pem.PemWriter;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;

@SuppressWarnings("DanglingJavadoc")
class AndroidKeystoreAttestation {

    // TAGs for logging
    private static final String TAG = AndroidKeystoreAttestation.class.getSimpleName();
    private static String MTAG = "attest";

    /******************************************************************
                                                  888
                                                  888
                                                  888
      .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     88888888   X88K   88888888 888      888  888 888    88888888
     Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
      "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *****************************************************************/

    /**
     * The code that tests the mobile device implementation for an SE or a TEE. Default values used
     * for key-generation constants are:
     *
     *      KEY_ALGORITHM_EC = ECDSA
     *      FIDO2_KEYSTORE_PROVIDER = AndroidKeystore
     *      PURPOSE_SIGN = Create a digital signature with the private key
     *      PURPOSE_VERIFY = Verify a digital signature with the public key
     *      FIDO2_KEY_ECDSA_CURVE = secp256r1, a NIST standard
     *      DIGEST_SHA256, _SHA384 and _SHA512 = Message digests for ECDSA signatures
     *      .setIsStrongBoxBacked(Boolean.TRUE) = Use a dedicated secure element - requires SDK 28 (Android P)
     *      .setUserAuthenticationRequired(Boolean.TRUE) = User must have device locked and require authentication to unlock
     *      FIDO2_USER_AUTHENTICATION_VALIDITY = 1 minutes
     *
     * If the hardware secure element is not available, the code will throw a StrongBoxUnavailableException,
     * upon which, the catch method will attempt to use the Trusted Execution Environment (TEE) to
     * generate the key-pair. Should usually work with TEE, but if there is no TEE, a key will be
     * generated anyway; the attestation validation will indicate if the key was generated in a
     * secure element, TEE or in software.
     *
     * @param authenticatorDataBytes byte[] with calculated information for signing with PrivateKey
     * @param mCredentialId String containing the FIDO credential ID
     * @param clientDataHash String containing a Base64Url encoded SHA256 digest of components
     * that make up the CollectedClientData JSON object described in the WebAuthn spec at
     * (https://www.w3.org/TR/webauthn/#dictdef-collectedclientdata)
     *
     *  dictionary CollectedClientData {
     *     required DOMString           type;       // Hard-coded to "public-key"
     *     required DOMString           challenge;  // Sent by FIDO2 server
     *     required DOMString           origin;     // Must be identical to rpid (verified by SACL)
     *     TokenBinding                 tokenBinding; // Optional - empty for now
     * };
     *
     * @param contextWrapper An instance of LocalContextWrapper passed in to be able to get to
     * resource files without convoluted code
     * @return JSONObject with the attestation details from the app's server, or error messages
     */

    protected static JSONObject execute(byte[] authenticatorDataBytes,
                                        String mCredentialId,
                                        String clientDataHash,
                                        ContextWrapper contextWrapper) {
        // Local variables
        Constants.KEY_ORIGIN mKeyOrigin;                   // Flag to indicate the origin of a key
        Constants.SECURITY_MODULE mSecurityModule = null;  // Flag to indicate if SE or TEE is present
        Certificate[] mCertificateChain;                   // Certificate chain to be attested
        PrivateKey mPrivateKey = null;                     // PrivateKey object
        KeyInfo mKeyInfo = null;                           // Information on PrivateKey


        // Retrieve newly generated key as part of attestation process
        try {
            // TODO: Check for local device authentication: PIN, Fingerprint, Face, etc.
            KeyStore mKeystore = KeyStore.getInstance(Constants.FIDO2_KEYSTORE_PROVIDER);
            mKeystore.load(null);

            // Find key inside keystore
            Log.v(TAG, "CREDENTIALID=" + mCredentialId);
            KeyStore.Entry entry = mKeystore.getEntry(mCredentialId, null);
            if (entry != null) {
                if (!(entry instanceof KeyStore.PrivateKeyEntry)) {
                    String mErrorString = contextWrapper.getString(R.string.ERROR_NOT_PRIVATE_KEY);
                    Log.w(TAG, mErrorString);
                    return Common.JsonError(TAG, MTAG, Constants.ERROR_NOT_PRIVATE_KEY, mErrorString);
                } else {
                    mPrivateKey = ((KeyStore.PrivateKeyEntry) entry).getPrivateKey();
                    KeyFactory factory;
                    factory = KeyFactory.getInstance(mPrivateKey.getAlgorithm(), Constants.FIDO2_KEYSTORE_PROVIDER);
                    mKeyInfo = factory.getKeySpec(mPrivateKey, KeyInfo.class);
                }
            }

            // Check the origin of the key - if Generated, it was generated inside AndroidKeystore
            // but not necessarily in hardware - emulators do support the AndroidKeystore in
            // software, so it can be misleading without attestation check
            assert mKeyInfo != null;
            switch (mKeyInfo.getOrigin()) {
                case KeyProperties.ORIGIN_GENERATED:
                    mKeyOrigin = Constants.KEY_ORIGIN.GENERATED;
                    break;
                case KeyProperties.ORIGIN_IMPORTED:
                    mKeyOrigin = Constants.KEY_ORIGIN.IMPORTED;
                    break;
                case KeyProperties.ORIGIN_UNKNOWN:
                default:
                    mKeyOrigin = Constants.KEY_ORIGIN.UNKNOWN;
            }

            // Log verbose key information
            String mAlgorithm = mPrivateKey.getAlgorithm() + " [" + Constants.FIDO2_KEY_ECDSA_CURVE + "]";
            String mSecureHardware = mKeyInfo.isInsideSecureHardware() + " [" + mSecurityModule + "]";
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_keyname) + mKeyInfo.getKeystoreAlias());
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_origin) + mKeyOrigin);
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_algorithm) + mAlgorithm);
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_size) + mKeyInfo.getKeySize());
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_userauth) + mKeyInfo.isUserAuthenticationRequired());
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_semodule) + mSecureHardware);

            // Initialize digital signature
            Signature s = Signature.getInstance(Constants.FIDO2_SIGNATURE_ALGORITHM);
            try {
                s.initSign(((KeyStore.PrivateKeyEntry) entry).getPrivateKey());
            } catch (UserNotAuthenticatedException e) {
                String mErrorString = contextWrapper.getString(R.string.ERROR_UNAUTHENTICATED_USER);
                Log.w(TAG, mErrorString);
                return Common.JsonError(TAG, MTAG, Constants.ERROR_UNAUTHENTICATED_USER, mErrorString);
            }
            // Generate signature
            byte[] signature = new byte[0];
            String b64urlSignature = null;
            try {
                byte[] clientDataHashBytes = Common.urlDecode(clientDataHash);
                byte[] toBeSigned = new byte[authenticatorDataBytes.length + clientDataHashBytes.length];
                System.arraycopy(authenticatorDataBytes, 0, toBeSigned, 0, authenticatorDataBytes.length);
                System.arraycopy(clientDataHashBytes, 0, toBeSigned, authenticatorDataBytes.length, clientDataHashBytes.length);
                s.update(toBeSigned);
                signature = s.sign();
                b64urlSignature = Common.urlEncode(signature);
                Log.v(TAG, contextWrapper.getString(R.string.vmessage_tbs) + Hex.toHexString(toBeSigned) + "\n" +
                        contextWrapper.getString(R.string.vmessage_signature) + b64urlSignature);
            } catch (SignatureException e) {
                e.printStackTrace();
                try {
                    return Common.JsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    return null;
                }
            }

            // Get certificate chain of newly generated key
            mCertificateChain = ((KeyStore.PrivateKeyEntry) entry).getCertificateChain();
            int numberOfCerts = mCertificateChain.length;
            if (numberOfCerts == 1) {
                String mErrorString = contextWrapper.getString(R.string.ERROR_SINGLE_CERTIFICATE_IN_CHAIN);
                Log.w(TAG, mErrorString);
                return Common.JsonError(TAG, MTAG, Constants.ERROR_SINGLE_CERTIFICATE_IN_CHAIN, mErrorString);
            }
            Log.v(TAG, contextWrapper.getString(R.string.vmessage_number_of_certificates) + numberOfCerts);

            // Extract the certificate chain into a JSONObject for the server
            StringWriter sw;
            PemWriter pemWriter;
            JSONArray jab = new JSONArray();

            /*
              Read PEM files and create a Json object that looks like this:

                   JsonObject jo = Json.createObjectBuilder()
                       .add("AKSCertificateChain", Json.createObjectBuilder()
                           .add("Size", 4)
                           .add("Certificates", Json.createArrayBuilder()
                               .add(pemcerts[0])
                               .add(pemcerts[1])
                               .add(pemcerts[2])
                               .add(pemcerts[3]))) .build();
             */
            for (int i = 0; i < numberOfCerts; i++) {
                X509Certificate x509cert = (X509Certificate) mCertificateChain[i];
                sw = new StringWriter();
                pemWriter = new PemWriter(sw);
                PemObject pemObject = new PemObject("CERTIFICATE", x509cert.getEncoded());
                pemWriter.writeObject(pemObject);
                pemWriter.close();
                sw.close();
                jab.put(i, sw.toString());
            }

            // Create the JSONArray with the certificate chain - first the end-entity certificate
            JSONArray certArray = new JSONArray()
                    .put(0, new JSONObject()
                            .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_CREDENTIAL_CERTIFICATE, jab.getString(0)));
            Log.v(TAG, "Added Credential Certificate: #0");

            // Now the certificate chain in order
            for (int n = 1; n < numberOfCerts; n++) {
                certArray.put(n, new JSONObject().put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_CA_CERTIFICATE, jab.getString(n)));
                Log.v(TAG, "Added CA Certificate: #" + n);
            }
            Log.v(TAG, "Number of JSONArray Certificates from jab: " + certArray.length());

            // This private method breaks down very long messages and prints it in sections
            Common.printVeryLongLogMessage("JSONArray of X509 Certificates", certArray.toString(2));

            // Create the CBOR attestation for FIDO - sending Certificate chain rather than a
            // JSON array to save resources converting bytes to CBOR
            String cborAttestation = buildCborAttestation(authenticatorDataBytes, mPrivateKey.getAlgorithm(), signature, mCertificateChain);

            // Create Android Key Attestation with embedded digital signature
            JSONObject aksAttestation = new JSONObject()
                    .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO, new JSONObject()
                        .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO_JSON_FORMAT, new JSONObject()
                            .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FORMAT, Constants.ANDROID_KEYSTORE_ATTESTATION_VALUE_FORMAT)
                            .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_STATEMENT, new JSONObject()
                                .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_ALGORITHM, -7)
                                .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_SIGNATURE, b64urlSignature)
                                .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_X509_CERTIFICATE_CHAIN, certArray)))
                        .put(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO_CBOR_FORMAT, cborAttestation));

            Common.printVeryLongLogMessage("AndroidKeystore Attestation", aksAttestation.toString(2));
            return aksAttestation;

        } catch (KeyStoreException |
                CertificateException |
                IOException |
                NoSuchAlgorithmException |
                InvalidKeyException |
                NoSuchProviderException |
                InvalidKeySpecException |
                UnrecoverableEntryException |
                JSONException e) {
            e.printStackTrace();
            try {
                return Common.JsonError(TAG, MTAG, Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
            } catch (JSONException e1) {
                e1.printStackTrace();
                return null;
            }
        }
    }


    /**
     * Converts information about the AndroidKeystore Attestation into a CBOR string
     * @param authenticatorData byte array containing "authData" - see for details
     * https://www.w3.org/TR/2019/REC-webauthn-1-20190304/#authenticator-data
     * @param algorithm String with algorithm type of the generated key-pair (usually EC, but
     * could be RSA too if the policy specified it - only 2 algorithms are supported by design)
     * @param signature byte array with digital signature of the concatenation of AuthenticatorData
     * and the message digest of CollectedClientData
     * @param certChain Certificate chain containing a chain with the end-entity certificate in
     * the first position and each issuer's certificate in subsequent positions, ending with the
     * self-signed Root CA certificate - all X509 data structures
     * @return String with Base64Url encoded CBOR output of the AndroidKeystore attestation - see
     * https://www.w3.org/TR/2019/REC-webauthn-1-20190304/#android-key-attestation. Result will
     * be something like this:
     *
     * {
     *     "authData": "0PhkNA1MKnPrdnd.....ac2bdIn",
     *     "fmt": "android-key",
     *     "attStmt": {
     *         "alg": -7, // for EC or -257 for RSA
     *         "sig": "MEYCIQD9...UuPdIc2ccInL0PhkNA1MKnPrdndszFGS",
     *         "x5c": [
     *                 "...", // credCert
     *                 "...", // caCert1  - issuer of credCert
     *                 "...", // caCert2  - issuer of caCert1
     *                 "..."  // rootCert - issuer of caCert2
     *         ]
     *     }
     * }
     *
     */
    private static String buildCborAttestation(byte[] authenticatorData, String algorithm, byte[] signature, Certificate[] certChain)
            throws IOException, CertificateEncodingException, NoSuchAlgorithmException
    {
        String MTAG = "buildCborAttestation";
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CborEncoder cbe = new CborEncoder(baos);

        // Map entry with 3 elements of Key/Value
        cbe.writeMapStart(3);

        // First element - authenticator data - "authData"
        cbe.writeTextString(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_AUTHENTICATOR_DATA);
        cbe.writeByteString(authenticatorData);

        // Second element - attestation format - "fmt"
        cbe.writeTextString(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_FORMAT);
        cbe.writeTextString(Constants.ANDROID_KEYSTORE_ATTESTATION_VALUE_FORMAT);

        // Third element - attestation statement - "attStmt"
        cbe.writeTextString(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_STATEMENT);
        cbe.writeMapStart(3);

        // First sub-element of attStmt - only 2 choices
        if (algorithm != null) {
            cbe.writeTextString(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_ALGORITHM);
            switch (algorithm) {
                case Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_EC_LABEL:
                    cbe.writeInt(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_ES256);
                    break;
                case Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_RSA_LABEL:
                    cbe.writeInt(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_RS256);
                    break;
                default:
                    throw new NoSuchAlgorithmException(MTAG + ": Unsupported algorithm for AKS: " + algorithm);
            }
        } else
            throw new NoSuchAlgorithmException(MTAG + ": Empty algorithm parameter");

        // Second sub-element of attStmt
        if (signature != null) {
            cbe.writeTextString(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_SIGNATURE);
            cbe.writeByteString(signature);
        } else
            throw new IOException(MTAG + ": Empty attestation signature parameter");

        // Third sub-element of attStmt
        if (certChain != null) {
            int chainlen = certChain.length;
            cbe.writeTextString(Constants.ANDROID_KEYSTORE_ATTESTATION_LABEL_X509_CERTIFICATE_CHAIN);
            cbe.writeArrayStart(chainlen);

            // First sub-sub-element (of the Certificate chain)
            X509Certificate x509cert = (X509Certificate) certChain[0];
            cbe.writeByteString(x509cert.getEncoded());

            // Remaining certificates in chain are all CA certificates
            for (int i = 1; i < chainlen; i++) {
                x509cert = (X509Certificate) certChain[i];
                cbe.writeByteString(x509cert.getEncoded());
            }
        } else
            throw new IOException(MTAG + ": Empty certificate chain parameter");

        // Convert to byte-array and hex-encode to a string for display
        byte[] result = baos.toByteArray();
        String cborstring = Common.urlEncode(result);
        Log.v(MTAG, "Cbor Attestation: " + cborstring);

        return cborstring;
    }
}
