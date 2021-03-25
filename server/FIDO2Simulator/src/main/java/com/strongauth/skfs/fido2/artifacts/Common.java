/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE

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
 * Common class with static helper functions
 */

package com.strongauth.skfs.fido2.artifacts;

import com.strongkey.cbor.jacob.CborEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.interfaces.ECPrivateKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.BitSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonGenerator;
import javax.json.stream.JsonGeneratorFactory;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import org.bouncycastle.asn1.ASN1InputStream;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERBitString;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Hex;

public final class Common {

    // Json factories for creating Json parsers/generators
    private static final JsonGeneratorFactory JGF;
    private static final JsonParserFactory JPF;
    private static final PrivateKey _PVK = null;

    /* getter method to return private key. */
    public static PrivateKey getPVK() {
        return _PVK;
    }

    /**
     * *****************************************
     * d8b d8b 888 Y8P Y8P 888 888 888 88888b. 888 888888 888 888 "88b 888 888
     * 888 888 888 888 888 888 888 888 888 Y88b. 888 888 888 888 "Y888
     * *******************************************
     *
     * A static initializer block to get stuff initialized
     */
    static {
        // Add BouncyCastle JCE provider
        if (Security.getProvider("BCFIPS") == null) {
            Security.addProvider(new BouncyCastleFipsProvider());
        }

        // Setup JSON factories
        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);
        JGF = Json.createGeneratorFactory(properties);
        JPF = Json.createParserFactory(null);
    }



    public static Signature generateSignatureObject(PrivateKey pvk) {
        Signature sig;
        try {
            sig = Signature.getInstance("SHA256withECDSA");
            sig.initSign(pvk);
        } catch (InvalidKeyException | NoSuchAlgorithmException e) {
            throw new RuntimeException("BAD!");
        }
        return sig;
    }

    /**
     * Gets the SHA-256 message digest of the Relying Party Identifier's string
     *
     * @param rpid: String containing the relying party identifier
     * @return ByteArray: byte[] of the hashed, hex-encoded rpid
     */
    public static byte[] getRPID(String rpid)
    {
        try {
            final MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] rpdigest = md.digest(rpid.getBytes(StandardCharsets.UTF_8));

            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < rpdigest.length; i++) {
                String hex = Integer.toHexString(0xff & rpdigest[i]);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }

            String rpidhash = sb.toString();
//            System.out.println("rpidhash: " + rpidhash + '\n');
            return hexStringToByteArray(rpidhash);
        } catch (NoSuchAlgorithmException ex) {
            System.err.println("ERROR: " + ex);
            return null;
        }
    }

    /**
     * Gets the FLAGS.
     *
     * @param flagConfig: Boolean[] that holds UP, UV, AT, ED (not necessarily
     * in order).
     * @return flags: byte[] containing 1 byte.
     */
    public static byte[] getFlags(Boolean[] flagConfig) {

        /* Sets the FLAGS, an 8-bit array, according to specifications. */
        BitSet flagBits = new BitSet(8);

        Boolean userPresent = flagConfig[0];
        Boolean userVerified = flagConfig[1];
        Boolean hasAttested = flagConfig[2];
        Boolean hasExtensions = flagConfig[3];

        flagBits.set(0, userPresent);   // user present: 1 if true, 0 else
        flagBits.set(1, false);         // RFU, default 0
        flagBits.set(2, userVerified);  // user verified: 1 if true, 0 else
        flagBits.set(3, false);         // RFU, default 0
        flagBits.set(4, false);         // RFU, default 0
        flagBits.set(5, false);         // RFU, default 0
        flagBits.set(6, hasAttested);   // has attested credential data: 1 if true, 0 else
        flagBits.set(7, hasExtensions); // has extensions: 1 if true, 0 else

        /* Uses the helper method to convert BitSet to byte[]. */
        byte[] flags = bitSetToByteArray(flagBits);
        return flags;
    }

    /**
     * Gets the FLAGS.
     *
     * @param flagConfig: Boolean[] that holds UP, UV, AT, ED (not necessarily
     * in order).
     * @return flags: byte[] containing 1 byte.
     */
    public static byte getFlag(Boolean[] flagConfig) {

        /* Sets the FLAGS, an 8-bit array, according to specifications. */
        BitSet flagBits = new BitSet(8);

        Boolean userPresent = flagConfig[0];
        Boolean userVerified = flagConfig[1];
        Boolean hasAttested = flagConfig[2];
        Boolean hasExtensions = flagConfig[3];

        byte flags = 0x00;
        flags |= 0x01;
        if(userVerified){
            flags |= (0x01 << 2);
        }
        if (hasAttested) {
            flags |= (0x01 << 6); // attested credential data included
        }
        return flags;
    }

    /**
     * Gets the COUNTER.
     *
     * @param counter: int of how many authentications have been performed.
     * @return counterArray: byte[] form of int counter.
     */
    public static byte[] getCounter(int counter) {
        int value = counter;
        byte[] counterArray = bigIntToByteArray(value);
        return counterArray;
    }


    public static String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder();
        for (int i = 0; i < hash.length; i++) {
            String hex = Integer.toHexString(0xff & hash[i]);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
    return hexString.toString();
    }

//    /**
//     * Helper method that converts a STRING to byte[].
//     *
//     * @param string. The input string to be converted into byte[] format.
//     * @return data. The byte[] that represents @param string.
//     */
    public static byte[] hexStringToByteArray(String string) {
        int len = string.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(string.charAt(i), 16) << 4)
                    + Character.digit(string.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * Helper method that converts a BitSet to byte[].
     *
     * @param bits. The BitSet to be converted into a byte[] format.
     * @return bytes. The byte[] that represents @param bits.
     */
    public static byte[] bitSetToByteArray(BitSet bits) {
        byte[] bytes = new byte[(bits.length() + 7) / 8];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;

    }

    /**
     * Helper method that converts a bigInt to byte[].
     *
     * @param i. The bigInt to be converted to byte[] format.
     * @return bigInt.toByteArray(). byte[] that represents @param i.
     */
    public static byte[] bigIntToByteArray(int i) {
        BigInteger bigInt = BigInteger.valueOf(i);
        return bigInt.toByteArray();
    }

    /**
     * createClientData Decodes the returned value from a preregister webservice
     * request
     *
     * @param input String containing JSON object
     * @param type int value denoting the element we want from the JSON
     * @return String with the returned value from the JSON
     */
    public static Object decodeRegistrationSignatureRequest(String input, int type) {
        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(input))) {
            jsonObject = jsonReader.readObject();
        }

        switch (type) {
            case Constants.JSON_KEY_SESSIONID:
                return jsonObject.getString(Constants.JSON_KEY_SESSIONID_LABEL);
            case Constants.JSON_KEY_CHALLENGE:
                return jsonObject.getString(Constants.JSON_KEY_CHALLENGE_LABEL);
            case Constants.JSON_KEY_VERSION:
                return jsonObject.getString(Constants.JSON_KEY_VERSION_LABEL);
            case Constants.JSON_KEY_APPID:
                return jsonObject.getString(Constants.JSON_KEY_APPID_LABEL);
                case Constants.JSON_KEY_RP:
                return jsonObject.getJsonObject(Constants.JSON_KEY_RP_LABEL);
            default:
                return null; // Shouldn't happen, but....
        }
    }


    /**
     * Function to create the packed FIDO U2F data-structure to sign when
     * registering a new public-key with a FIDO U2F server. See the U2F Raw
     * Messages specification for details:
     *
     * https://fidoalliance.org/specs/fido-u2f-v1.0-nfc-bt-amendment-20150514/fido-u2f-raw-message-formats.html
     *
     * @param ApplicationParam String The application parameter is the SHA-256
     * hash of the application identity of the application requesting the
     * registration; it is 32-bytes in length
     * @param ChallengeParam String The challenge parameter is the SHA-256 hash
     * of the Client Data, a string JSON data structure the FIDO Client
     * prepares. Among other things, the Client Data contains the challenge from
     * the relying party (hence the name of the parameter)
     * @param kh String Base64-encoded, encrypted JSON data-structure of the
     * private-key, origin and the message-digest of the private-key
     * @param PublicKey String Base64-encoded public-key of the ECDSA key-pair
     * @return String Base64-encoded data-structure of the object being signed
     * as per the FIDO U2F protocol for a new-key registration
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     * @throws java.io.IOException
     */
    public static String createRegistrationObjectToSign(String ApplicationParam,
            String ChallengeParam,
            String kh,
            String PublicKey)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {
        // U2F Signed Registration constant
        final byte[] constant = {(byte) 0x00};
        int constantL = constant.length;

        // 32-byte challenge parameter
        byte[] Challenge = Base64.getUrlDecoder().decode(ChallengeParam);
        int ChanllengeL = Challenge.length;

        // 32-byte application parameter
        byte[] Application = Base64.getUrlDecoder().decode(ApplicationParam);
        int ApplicationL = Application.length;

        // Variable length encrypted key-handle JSON data-structure
        byte[] keyHandle = Base64.getUrlDecoder().decode(kh);
        int keyHandleL = keyHandle.length;

        // Fixed-length ECDSA public key
        byte[] publicKey = Base64.getUrlDecoder().decode(PublicKey);
        int pbkL = Constants.ECDSA_P256_PUBLICKEY_LENGTH;

        // Test the public key for sanity
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BCFIPS");
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
        PublicKey pub = kf.generatePublic(pubKeySpec);
        ECPublicKey ecpub = (ECPublicKey) pub;

        ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(pub.getEncoded()));
        ASN1Primitive obj = bIn.readObject();
        Enumeration e = ((ASN1Sequence) obj).getObjects();

        byte[] q = null;
        while (e.hasMoreElements()) {
            ASN1Primitive o = (ASN1Primitive) e.nextElement();
            if (o instanceof DERBitString) {
                DERBitString bt = (DERBitString) o;
                q = bt.getBytes();
            }
        }

        // Create byte[] for to-be-signed (TBS) object
        // Could have also used  ByteBuffer for this
        int currpos = 0;
        byte[] tbs = new byte[constantL + ChanllengeL + ApplicationL + keyHandleL + pbkL];

        // Copy the Signed Registration constant to TBS
        System.arraycopy(constant, 0, tbs, currpos, constantL);
        currpos += constantL;

        // Copy ApplicationParameters to TBS
        System.arraycopy(Application, 0, tbs, currpos, ApplicationL);
        currpos += ApplicationL;

        // Copy ChallengeParameters to TBS
        System.arraycopy(Challenge, 0, tbs, currpos, ChanllengeL);
        currpos += ChanllengeL;

        // Copy encrypted KeyHandle JSON to TBS
        System.arraycopy(keyHandle, 0, tbs, currpos, keyHandleL);
        currpos += keyHandleL;

        // Copy public-key to TBS
        System.arraycopy(q, 0, tbs, currpos, pbkL);

        // Return Base64-encoded TBS
        return Base64.getUrlEncoder().withoutPadding().encodeToString(tbs);
    }



    public static String signWithCredKey(PrivateKey pkey, String tbs)
            throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, NoSuchProviderException, InvalidKeyException, SignatureException {
        // Base64-decode TBS input
        byte[] tbsbytes = Base64.getUrlDecoder().decode(tbs);

        // Sign the TBS bytes
        Signature sig = Signature.getInstance("SHA256withECDSA", "BCFIPS");
        sig.initSign(pkey, new SecureRandom());
        sig.update(tbsbytes);
        byte[] signedBytes = sig.sign();

        return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);

        // Verify before responding (to be sure its accurate); this will slow
        // down responses - comment it out if faster responses are desired
//        sig.initVerify(pubkey);
//        sig.update(tbsbytes);
//        if (sig.verify(signedBytes)) {
//            return Base64.getUrlEncoder().withoutPadding().encodeToString(signedBytes);
//        } else {
//            return null;
//        }
    }

    /**
     * Function to create the Base64-encoded packed data-structure of the
     * Registration Data object for response to a FIDO U2F server. See:
     *
     * https://fidoalliance.org/specs/fido-u2f-v1.0-nfc-bt-amendment-20150514/fido-u2f-raw-message-formats.html#response-message-framing
     *
     * Note: Could have used ByteBuffer instead of copying arrays, but will plan
     * on updating to ByteBuffer in the next release.
     *
     * @param userPublicKey String with Base64-encoded public-key being
     * registered
     * @param keyHandle String with Base64-encoded JSON structure with encrypted
     * private key
     * @param AttestationCertificate String with Base64-encoded certificate of
     * Attestation key
     * @param Signature String with Base64-encoded digital signature of response
     * @return String with Base64-encoded message of packed Registration
     * Response
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     * @throws java.io.IOException
     */
    public static String createRegistrationData(String userPublicKey,
            String keyHandle,
            String AttestationCertificate,
            String Signature)
            throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, IOException {
        // Required reserved legacy-byte for U2F response
        byte constant = 0x05;

        // User's Public key
        byte[] upk = Base64.getUrlDecoder().decode(userPublicKey);
        int upklen = Constants.ECDSA_P256_PUBLICKEY_LENGTH;    //ECDSA secp256r1 publickey length

        // Key Handle and its length
        byte[] kh = Base64.getUrlDecoder().decode(keyHandle);
        int khlen = kh.length;

        // Registration Response allows 1-byte for KH length; problem if more than 255
        if (khlen > 255) {
            System.err.println("Fatal error: Key-handle length greater than 255");
            return null;
        }

        // Attestation certificate and its length
        byte[] ac = Base64.getUrlDecoder().decode(AttestationCertificate);
        int aclen = ac.length;

        // Attestation digital signature of response with length
        byte[] acsig = Base64.getUrlDecoder().decode(Signature);
        int acsiglen = acsig.length;

        // Test the public key for sanity
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BCFIPS");
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(upk);
        PublicKey pbk = kf.generatePublic(pubKeySpec);

        ASN1InputStream bIn = new ASN1InputStream(new ByteArrayInputStream(pbk.getEncoded()));
        ASN1Primitive obj = bIn.readObject();
        Enumeration e = ((ASN1Sequence) obj).getObjects();

        byte[] q = null;
        while (e.hasMoreElements()) {
            ASN1Primitive o = (ASN1Primitive) e.nextElement();
            if (o instanceof DERBitString) {
                DERBitString bt = (DERBitString) o;
                q = bt.getBytes();
            }
        }

        // Create byte array for Registration Response's raw-message
        byte[] regresp = new byte[1 + upklen + 1 + khlen + aclen + acsiglen];

        // Copy the reserved legacy-byte constant to regresp; set currpos to 1
        regresp[0] = constant;
        int currpos = 1;

        // Copy public-key to regresp; update currpos
        System.arraycopy(q, 0, regresp, currpos, upklen);
        currpos += upklen;

        // Copy key-handle length byte to regresp
        regresp[currpos] = (byte) khlen;
        currpos += 1;

        // Copy key-handle to regresp
        System.arraycopy(kh, 0, regresp, currpos, khlen);
        currpos += khlen;

        // Copy attestation certificate to regresp
        System.arraycopy(ac, 0, regresp, currpos, aclen);
        currpos += aclen;

        // Finally, copy signature to regresp
        System.arraycopy(acsig, 0, regresp, currpos, acsiglen);

        // Return URL-safe Base64-encoded response
        return Base64.getUrlEncoder().withoutPadding().encodeToString(regresp);
    }

    public static JsonObject createRegistrationResponse(String clientdata, String regdata)
            throws UnsupportedEncodingException {
        JsonObject jo = Json.createObjectBuilder()
                .add(Constants.JSON_KEY_CLIENTDATA_LABEL, Base64.getUrlEncoder().withoutPadding().encodeToString(clientdata.getBytes("UTF-8")))
                //                .add(Constants.JSON_KEY_SESSIONID_LABEL, sessionid)
                .add(Constants.JSON_KEY_REGISTRATIONDATA_LABEL, regdata)
                .build();
        return jo;
    }

    public static byte[] getDigestBytes(byte[] input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {
        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(input);
        return digestbytes;
    }


    /**
     * *****************************************
     * 888 888 .d888 888 888 d88P" 888 888 888 8888b. 888 888 888888 88888b.
     * 888888 88888b. "88b 888 888 888 888 "88b 888 888 "88b .d888888 888 888
     * 888 888 888 888888 888 888 888 888 888 Y88b 888 Y88b. 888 888 888 888 888
     * "Y888888 "Y88888 "Y888 888 888 888 888 888
     * *******************************************
     *
     * /**
     * Decodes the returned value from a preauthenticate webservice request
     *
     * @param input String containing returned JSON
     * @param type int value denoting the type of object
     * @return String with the returned value from the JSON
     */
    public static String decodeAuthRequestJsonKeys(String input, int type) {
        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(input))) {
            jsonObject = jsonReader.readObject();
        }

        switch (type) {
            case Constants.JSON_KEY_KEYHANDLE:
                return jsonObject.getString(Constants.JSON_KEY_KEYHANDLE_LABEL);
            case Constants.JSON_KEY_SESSIONID:
                return jsonObject.getString(Constants.JSON_KEY_SESSIONID_LABEL);
            case Constants.JSON_KEY_CHALLENGE:
                return jsonObject.getString(Constants.JSON_KEY_CHALLENGE_LABEL);
            case Constants.JSON_KEY_VERSION:
                return jsonObject.getString(Constants.JSON_KEY_VERSION_LABEL);
            case Constants.JSON_KEY_APPID:
                return jsonObject.getString(Constants.JSON_KEY_APPID_LABEL);
            default:
                return null;   // Shouldn't happen, but...
        }
    }

    /**
     * Function to create the U2F Authentication response in the software
     * authenticator.
     *
     * @param chalparam String containing the Base64-encoded hash of the
     * challenge nonce sent by SKCE (U2F server) from the preregister call
     * @param appparam String containing the Base64-encoded hash of the facet-id
     * (application parameter)
     * @param keyhandle String containing the Base64-encoded encrypted KeyHandle
     * @param counter
     * @return String containing the base64-encoded signed authentication
     * response
     * @throws java.security.spec.InvalidParameterSpecException
     */
    public static String createAuthenticationSignatureResponse(String chalparam, String appparam, String keyhandle, int counter) throws InvalidParameterSpecException {
        // Recover challenge parameter
        byte[] cpbytes = Base64.getUrlDecoder().decode(chalparam);
        int cplen = cpbytes.length;

        // Recover application parameter
        byte[] apbytes = Base64.getUrlDecoder().decode(appparam);
        int aplen = apbytes.length;

        // Create a new byte-array to-be-signed.  The 1 is for user-presence-byte
        // while the 4 is for the byte-array of the (authenticator) counter value
        byte[] tbs = new byte[aplen + 1 + Constants.AUTHENTICATOR_COUNTER_LENGTH + cplen];

        // Initialize current position
        int currpos = 0;

        // Copy application parameter into TBS
        System.arraycopy(apbytes, 0, tbs, currpos, aplen);
        currpos += aplen;

        // Copy user-presence-byte into TBBS
        tbs[currpos] = Constants.AUTHENTICATOR_USERPRESENCE_BYTE;
        currpos += 1;

        // Copy counter value into TBS - verify if less than Integer.MAX_VALUE
        if (counter > 2147483647) {
            System.err.println("Authenticator Counter MAX value reached; wrapping around...");
            counter = 1;
        }
        byte[] counterbytes = ByteBuffer.allocate(Constants.AUTHENTICATOR_COUNTER_LENGTH).putInt(counter).array();
        System.arraycopy(counterbytes, 0, tbs, currpos, Constants.AUTHENTICATOR_COUNTER_LENGTH);
        currpos += Constants.AUTHENTICATOR_COUNTER_LENGTH;

        // Copy challenge parameter into TBS; done with curpos here
        System.arraycopy(cpbytes, 0, tbs, currpos, cplen);

        // Decrypt KeyHandle
        @SuppressWarnings("UnusedAssignment")
        String khjson = null;
        byte[] signedbytes;
        try {
            khjson = decryptKeyHandle(keyhandle);
//            System.out.println("PlaintextKeyHandle:   " + khjson);

            // Extract user's private-key
            PrivateKey pvk = getUserPrivateKey(decodeKeyHandle(khjson, 0));

            // Sign TBS with private-key
            Signature sig = Signature.getInstance("SHA256withECDSA", "BCFIPS");
            sig.initSign(pvk, new SecureRandom());
            sig.update(tbs);
            signedbytes = sig.sign();

        } catch (NoSuchAlgorithmException
                | NoSuchProviderException | NoSuchPaddingException
                | InvalidKeyException | InvalidAlgorithmParameterException
                | ShortBufferException | IllegalBlockSizeException
                | BadPaddingException | UnsupportedEncodingException
                | InvalidKeySpecException | SignatureException ex) {
            System.err.println("Fatal Error: KeyHandle exception: " + ex.getLocalizedMessage());
            return null;
        }

        // Create Signature Data byte-array and reset current position
        // The 1 byte in signdata is for the user-presence-byte
        byte[] signdata = new byte[1 + Constants.AUTHENTICATOR_COUNTER_LENGTH + signedbytes.length];
        currpos = 0;

        // Copy user-presence byte into first position of signdata
        signdata[currpos] = Constants.AUTHENTICATOR_USERPRESENCE_BYTE;
        currpos += 1;

        // Copy counter bytes into signdata
        System.arraycopy(counterbytes, 0, signdata, currpos, Constants.AUTHENTICATOR_COUNTER_LENGTH);
        currpos += Constants.AUTHENTICATOR_COUNTER_LENGTH;

        // Copy signed-bytes into signdata
        System.arraycopy(signedbytes, 0, signdata, currpos, signedbytes.length);

        // Return Base64-encoded signature response
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signdata);
    }

    /**
     * Function to create a JSON object for the Authentication response to the
     * SKCE's FIDOEngine. Need to re-encode clientdata into URL-safe string just
     * in case its not already so.
     *
     * @param clientdata
     * @param keyhandle
     * @param signresponse
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    public static JsonObject encodeAuthenticationSignatureResponse(String clientdata,
            String keyhandle,
            String signresponse)
            throws UnsupportedEncodingException {
        return Json.createObjectBuilder()
                .add(Constants.JSON_KEY_CLIENTDATA_LABEL, Base64.getUrlEncoder().withoutPadding().encodeToString(clientdata.getBytes("UTF-8")))
                //                .add(Constants.JSON_KEY_SESSIONID_LABEL, sessionid)
                .add(Constants.JSON_KEY_KEYHANDLE_LABEL, keyhandle)
                .add(Constants.JSON_KEY_SIGNATURE_LABEL, signresponse)
                .build();
    }

    /**
     * *****************************************
     * d8b Y8P      *
     * 88888b.d88b. 888 .d8888b .d8888b 888 "888 "88b 888 88K d88P" 888 888 888
     * 888 "Y8888b. 888 888 888 888 888 X88 Y88b. 888 888 888 888 88888P'
     * "Y8888P *******************************************
     *
     * /**
     * Returns message digest of a byte-array of the specified algorithm
     *
     * @param input byte[] containing content that must be digested (hashed)
     * @param algorithm String indicating digest algorithm
     * @return String Base64-encoded digest of specified input
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws UnsupportedEncodingException
     */
    public static String getDigest(byte[] input, String algorithm)
            throws
            NoSuchAlgorithmException,
            NoSuchProviderException,
            UnsupportedEncodingException {
        MessageDigest digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(input);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(digestbytes);
    }

    /**
     * Returns the message digest of the specified input string - calls the
     * getDigest function
     *
     * @param input String containing content that must be digested (hashed)
     * @param algorithm String indicating digest algorithm
     * @return String Base64-encoded message digest of specified input
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws UnsupportedEncodingException
     */
    public static String getDigest(String input, String algorithm)
            throws
            NoSuchAlgorithmException,
            NoSuchProviderException,
            UnsupportedEncodingException {
        return getDigest(input.getBytes("UTF-8"), algorithm);
    }


    /**
     * Function to return a JSON object from inputs
     *
     * @param optype String indicating the typ of operation
     * @param challenge String containing the U2F server challenge
     * @param facetID String containing the facetid
     * @param cid String containing channelid information (not currently
     * supported)
     * @return String with the JSON data-structure
     */
    public static String clientDataEncoder(String optype, String challenge, String facetID, String cid) {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (cid == null) {
            job.add(Constants.JSON_KEY_REQUEST_TYPE_LABEL, optype)
                    .add(Constants.JSON_KEY_SERVER_CHALLENGE_LABEL, challenge)
                    .add(Constants.JSON_KEY_SERVER_ORIGIN_LABEL, facetID);
        } else {
            job.add(Constants.JSON_KEY_REQUEST_TYPE_LABEL, optype)
                    .add(Constants.JSON_KEY_SERVER_CHALLENGE_LABEL, challenge)
                    .add(Constants.JSON_KEY_SERVER_ORIGIN_LABEL, facetID)
                    .add(Constants.JSON_KEY_CHANNELID_LABEL, cid);
        }
        return job.build().toString();
    }

    /**
     * Function to encode a keyHandle as a JSON object
     *
     * @param pvk String containing the Base64-encoded private-key
     * @param origin String containing the origin with which the key is
     * associated
     * @param sha1hash String containing the SHA1 digest of the key
     * @return String containing the JSON of the keyHandle
     */
    public static String encodeKeyHandle(String pvk, String origin, String sha1hash) {
        return Json.createObjectBuilder()
                .add("key", pvk)
                .add("sha1", sha1hash)
                .add("origin_hash", origin)
                .build().toString();
    }

    /**
     * Function to decode the return-values of a keyHandle
     *
     * @param input
     * @param type
     * @return
     */
    public static String decodeKeyHandle(String input, int type) {
        JsonObject jsonObject = Json.createReader(new StringReader(input)).readObject();
        switch (type) {
            case 0:
                return jsonObject.getString("key");
            case 1:
                return jsonObject.getString("sha1");
            default:
                return jsonObject.getString("origin_hash");
        }
    }

    /**
     * Function to make a key-handle for transporting to the FIDO U2F server
     *
     * @param pvk PrivateKey of the ECDSA key-pair
     * @param originHash String Message digest of the origin for which this
     * private-key is valid
     * @return String Base64-encoded key-handle
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws FileNotFoundException
     * @throws DecoderException
     * @throws InvalidKeyException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws InvalidKeySpecException
     * @throws SignatureException
     * @throws java.security.spec.InvalidParameterSpecException
     */
    public static String makeKeyHandle(PrivateKey pvk, String originHash)
            throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, FileNotFoundException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, ShortBufferException, InvalidKeySpecException, SignatureException, InvalidParameterSpecException {
        // Get wrapping key
        byte[] Seckeybytes = Hex.decode(Constants.FIXED_AES256_WRAPPING_KEY);
        SecretKeySpec sks = new SecretKeySpec(Seckeybytes, "AES");
        ECPrivateKey ecpk = (ECPrivateKey) pvk;
        byte[] s = org.bouncycastle.util.encoders.Hex.decode(String.format("%064x", ecpk.getS()));

        // Encode plaintext key-handle into JSON structure
        String ptkh = encodeKeyHandle(Base64.getUrlEncoder().withoutPadding().encodeToString(s), originHash, getDigest(pvk.getEncoded(), "SHA1"));
//        System.out.println("PlaintextKeyHandle:     " + ptkh);

        // Encrypt key handle to create ciphertext
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BCFIPS");
        cipher.init(Cipher.ENCRYPT_MODE, sks, new SecureRandom());
        byte[] ctkh = cipher.doFinal(ptkh.getBytes("UTF-8"));

        // Recover IV from cipher and prepend to encrypted keyhandle in new array
        byte[] iv = cipher.getIV();
        byte[] ctkhiv = new byte[ctkh.length + Constants.ENCRYPTION_MODE_CBC_IV_LENGTH];
        System.arraycopy(iv, 0, ctkhiv, 0, Constants.ENCRYPTION_MODE_CBC_IV_LENGTH);              // Copy IV to new array
        System.arraycopy(ctkh, 0, ctkhiv, Constants.ENCRYPTION_MODE_CBC_IV_LENGTH, ctkh.length);  // Append ciphertext KH to IV

        // Base64-encode ciphertext keyhandle + IV
        String ctkhivb64 = Base64.getUrlEncoder().withoutPadding().encodeToString(ctkhiv);

        // Test recovery of plaintext key-handle before returning
        //String ptkh2 = decryptKeyHandle(ctkhivb64);
        //if (!ptkh2.trim().equalsIgnoreCase(ptkh.trim())) {
        //    System.err.println("Decryption of keyhandle failed during test");
        //    return null;
        //}
        // Decryption succeeded - return Base64-encoded, encrypted keyhandle + IV
        return ctkhivb64;
    }

    /**
     * Function to decrypt a private-key and return it from a Base64-encoded
     * key-handle (which has a 16-byte IV prepended to it)
     *
     * @param s String containing a 16-byte IV plus the encrypted keyhandle
     * @return String containing the Base64-encoded plaintext JSON structure of
     * the key-handle
     * @throws DecoderException
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeySpecException
     * @throws SignatureException
     * @throws java.security.spec.InvalidParameterSpecException
     */
    public static String decryptKeyHandle(String s)
            throws NoSuchAlgorithmException,
            NoSuchProviderException, NoSuchPaddingException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            ShortBufferException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException,
            InvalidKeySpecException, SignatureException,
            InvalidParameterSpecException
    {
        // Get wrapping key
        byte[] Seckeybytes = Hex.decode(Constants.FIXED_AES256_WRAPPING_KEY);
        SecretKeySpec sks = new SecretKeySpec(Seckeybytes, "AES");

        // Decode IV + ciphertext and extract components into new arrays
        byte[] ctkhiv = Base64.getUrlDecoder().decode(s);
        byte[] iv = new byte[16];
//        System.out.println(s);
        byte[] ctkh = new byte[ctkhiv.length - iv.length];
        System.arraycopy(ctkhiv, 0, iv, 0, Constants.ENCRYPTION_MODE_CBC_IV_LENGTH);
        System.arraycopy(ctkhiv, Constants.ENCRYPTION_MODE_CBC_IV_LENGTH, ctkh, 0, ctkh.length);

        // Decrypt keyhandle using IV in input string
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BCFIPS");
        IvParameterSpec ivspec = new IvParameterSpec(iv);
        cipher.init(Cipher.DECRYPT_MODE, sks, ivspec);
        byte[] ptkh = new byte[cipher.getOutputSize(ctkh.length)];
        int p = cipher.update(ctkh, 0, ctkh.length, ptkh, 0);
        cipher.doFinal(ptkh, p);

        return new String(ptkh, "UTF-8");
    }



    /**
     * Generates a new ECDSA key-pair using the fixed curve secp256r1
     *
     * @return KeyPair containing a new ECDSA 256-bit key-pair
     * @throws KeyStoreException
     * @throws NoSuchProviderException
     * @throws IOException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws SignatureException
     */
    public static KeyPair generateKeypair()
            throws KeyStoreException, NoSuchProviderException, IOException,
            NoSuchAlgorithmException, CertificateException,
            InvalidAlgorithmParameterException, InvalidKeyException,
            SignatureException
    {
        ECGenParameterSpec paramSpec = new ECGenParameterSpec((Constants.EC_P256_CURVE));
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "BCFIPS");
        kpg.initialize(paramSpec, new SecureRandom());
        return kpg.generateKeyPair();
    }

    /**
     * Function to generate a PrivateKey object from a byte-array containing the
     * ECDSA private-key
     *
     * @param pvk String with Base64-encoded private key
     * @return PrivateKey
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws InvalidKeySpecException
     * @throws java.security.spec.InvalidParameterSpecException
     */
    public static PrivateKey getUserPrivateKey(String pvk)
            throws NoSuchAlgorithmException, NoSuchProviderException,
            InvalidKeySpecException, InvalidParameterSpecException
    {
        AlgorithmParameters parameters = AlgorithmParameters.getInstance("EC");
        parameters.init(new ECGenParameterSpec("secp256r1"));

        ECParameterSpec ecParameterSpec = parameters.getParameterSpec(ECParameterSpec.class);
        ECPrivateKeySpec ecPrivateKeySpec = new ECPrivateKeySpec(new BigInteger(1, Base64.getUrlDecoder().decode(pvk)), ecParameterSpec);

        return KeyFactory.getInstance("EC").generatePrivate(ecPrivateKeySpec);
    }

    /**
     * Encode an EC public key in the COSE/CBOR format.
     *
     * @param publicKey The public key.
     * @return A COSE_Key-encoded public key as byte array.
     * @throws java.lang.Exception
     */
    public static byte[] coseEncodePublicKey(PublicKey publicKey) throws Exception
    {
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        ECPoint point = ecPublicKey.getW();
        // ECPoint coordinates are *unsigned* values that span the range [0, 2**32). The getAffine
        // methods return BigInteger objects, which are signed. toByteArray will output a byte array
        // containing the two's complement representation of the value, outputting only as many
        // bytes as necessary to do so. We want an unsigned byte array of length 32, but when we
        // call toByteArray, we could get:
        // 1) A 33-byte array, if the point's unsigned representation has a high 1 bit.
        //    toByteArray will prepend a zero byte to keep the value positive.
        // 2) A <32-byte array, if the point's unsigned representation has 9 or more high zero
        //    bits.
        // Due to this, we need to either chop off the high zero byte or prepend zero bytes
        // until we have a 32-length byte array.
        byte[] xVariableLength = point.getAffineX().toByteArray();
        byte[] yVariableLength = point.getAffineY().toByteArray();

        byte[] x = toUnsignedFixedLength(xVariableLength, 32);
        assert x.length == 32;
        byte[] y = toUnsignedFixedLength(yVariableLength, 32);
        assert y.length == 32;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CborEncoder cbe = new CborEncoder(baos);
        try {
            cbe.writeMapStart(5);

            cbe.writeInt(1);
            cbe.writeInt(2);
            
            cbe.writeInt(3);
            cbe.writeInt(-7);
            
            cbe.writeInt(-1);
            cbe.writeInt(1);
            
            cbe.writeInt(-2);
            cbe.writeByteString(x);
            
            cbe.writeInt(-3);
            cbe.writeByteString(y);

//            new CborEncoder(baos).encode(new CborBuilder()
//                    .addMap()
//                    .put(1, 2)  // kty: EC2 key type
//                    .put(3, -7) // alg: ES256 sig algorithm
//                    .put(-1, 1) // crv: P-256 curve
//                    .put(-2, x) // x-coord
//                    .put(-3, y) // y-coord
//                    .end()
//                    .build()
//            );
        } catch (Exception e) {
            System.err.println(e.getLocalizedMessage());
            throw new Exception("couldn't serialize to cbor", e);
        }
        return baos.toByteArray();
    }

    private static byte[] toUnsignedFixedLength(byte[] arr, int fixedLength) {
        byte[] fixed = new byte[fixedLength];
        int offset = fixedLength - arr.length;
        int srcPos = Math.max(-offset, 0);
        int dstPos = Math.max(offset, 0);
        int copyLength = Math.min(arr.length, fixedLength);
        System.arraycopy(arr, srcPos, fixed, dstPos, copyLength);
        return fixed;
    }

    public static byte[] getDigestBytes(String Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {

        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(Input.getBytes("UTF-8"));
        return digestbytes;
    }

    /**
     * Function to retrieve the Attestation Key's digital certificate in Base64
     * from Constants and returns as a byte array.
     *
     * @return byte[]

     */
    public static byte[] getAttestationCertBytes()
    {
        String certBase64 = Constants.ATTESTATION_FILE_BASE64;
        byte[] certbytes = Base64.getUrlDecoder().decode(certBase64);
        return certbytes;
    }

    /**
     * Function to print the contents of a JSON object - doesn't seem to work
     * right now; will fix in future build.
     *
     * @param jsonstring String input containing the JSON object
     * @param label String containing the label for the JSON object
     */
    public static void printJson(String jsonstring, String label) {
        String key = null;
        System.out.print(label);

        // Setup generator for pretty-printing JSON
        try (JsonGenerator jg = JGF.createGenerator(System.out)) {
            try (JsonParser parser = JPF.createParser(new StringReader(jsonstring))) {
                JsonParser.Event event;
                while (parser.hasNext()) {
                    event = parser.next();
                    switch (event) {
                        case START_OBJECT:
                            try {
                                jg.writeStartObject();
                            } catch (Exception e) {
                                jg.writeStartObject(key);
                            }
                            break;
                        case END_OBJECT:
                            jg.writeEnd();
                            break;
                        case KEY_NAME:
                            key = parser.getString();
                            break;
                        case VALUE_STRING:
                            try {
                                jg.write(parser.getString());
                            } catch (Exception e) {
                                jg.write(key, parser.getString());
                            }
                            break;
                        default:
                            System.err.println("Unexpected parser-event in JSON: " + event.name());
                    }
                }
            }
        }
    }
}
