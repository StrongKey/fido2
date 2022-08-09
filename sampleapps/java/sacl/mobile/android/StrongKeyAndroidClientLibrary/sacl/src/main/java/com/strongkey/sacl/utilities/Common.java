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
 * Common routines used within the library
 */

package com.strongkey.sacl.utilities;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import androidx.room.TypeConverter;

import com.strongkey.sacl.R;
import com.strongkey.sacl.cbor.CborEncoder;
import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.AuthorizationSignature;
import com.strongkey.sacl.roomdb.PreauthenticateChallenge;
import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sacl.roomdb.PreregisterChallenge;
import com.strongkey.sacl.roomdb.ProtectedConfirmationCredential;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.roomdb.SaclSharedDataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;
import org.spongycastle.util.encoders.Hex;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECPoint;
import java.util.ArrayList;
import java.util.Base64;
import java.util.BitSet;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;

public class Common {

    public static final String TAG = Common.class.getSimpleName();

    // Number of threads for persisting objects to RoomDB
    private static final int NUMBER_OF_THREADS = 2;

    // ExecutorService for asynchronous tasks
    private static ExecutorService executorService;
    private static SaclRepository saclRepository;
    private static SaclSharedDataModel saclSharedDataModel;

    static {
        // Create ExecutorService for background tasks automatically
        executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        // Create SaclSharedDataModel for sharing objects across threads
        saclSharedDataModel = new SaclSharedDataModel();
    }

    // Method to execute Runnable tasks in the background
    public static Future performBackgroundTask(Runnable runnable) throws RejectedExecutionException {
        return executorService.submit(runnable);
    }

    // Initialize SACL Repository if not available
    public static SaclRepository getRepository(LocalContextWrapper context) {
        if (saclRepository == null) {
            saclRepository = new SaclRepository(context);

//        // Clean up
//        saclRepository.deleteAllPreregisterChallenges();
//        saclRepository.deleteAllPreauthenticateChallenges();
//        saclRepository.deleteAllPublicKeyCredentials();
//        saclRepository.deleteAllFidoAuthenticationSignatures();
//        Log.v(TAG, "CLEANED UP");
        }
        return saclRepository;
    }

    // Sets the object into the CURRENT variable for sharing
    @SuppressWarnings("unchecked")
    public static boolean setCurrentObject(Constants.SACL_OBJECT_TYPES type, Object object) {
        switch (type) {
            case PREREGISTER_CHALLENGE:
                saclSharedDataModel.setCurrentPreregisterChallenge((PreregisterChallenge) object);
                return true;
            case PREAUTHENTICATE_CHALLENGE:
                saclSharedDataModel.setCurrentPreauthenticateChallenge((PreauthenticateChallenge) object);
                return true;
            case PREAUTHORIZE_CHALLENGE:
                saclSharedDataModel.setCurrentPreauthorizeChallenge((PreauthorizeChallenge) object);
                return true;
            case PUBLIC_KEY_CREDENTIAL:
                saclSharedDataModel.setCurrentPublicKeyCredential((PublicKeyCredential) object);
                return true;
            case PUBLIC_KEY_CREDENTIAL_LIST:
                if (object instanceof List) {
                    saclSharedDataModel.setCurrentPublicKeyCredentialList((List<PublicKeyCredential>) object);
                    return true;
                }
            case PROTECTED_CONFIRMATION_CREDENTIAL:
                saclSharedDataModel.setCurrentProtectedConfirmationCredential((ProtectedConfirmationCredential) object);
                return true;
            case AUTHENTICATION_SIGNATURE:
                saclSharedDataModel.setCurrentAuthenticationSignature((AuthenticationSignature) object);
                return true;
            case AUTHORIZATION_SIGNATURE:
                saclSharedDataModel.setCurrentAuthorizationSignature((AuthorizationSignature) object);
                return true;
            case SACL_REPOSITORY:
                saclSharedDataModel.setSaclRepository((SaclRepository) object);
                Log.d(TAG, "Set SACL Repository in SaclSharedDataModel");
                return true;
            default:
                Log.w(TAG, "Invalid SACL ObjectType: " + type);
                return false;
        }
    }

    // Gets the object from the CURRENT variable for sharing
    public static Object getCurrentObject(Constants.SACL_OBJECT_TYPES type) {
        switch (type) {
            case PREREGISTER_CHALLENGE: return saclSharedDataModel.getCurrentPreregisterChallenge();
            case PREAUTHENTICATE_CHALLENGE: return saclSharedDataModel.getCurrentPreauthenticateChallenge();
            case PREAUTHORIZE_CHALLENGE: return saclSharedDataModel.getCurrentPreauthorizeChallenge();
            case PUBLIC_KEY_CREDENTIAL: return saclSharedDataModel.getCurrentPublicKeyCredential();
            case PUBLIC_KEY_CREDENTIAL_LIST: return saclSharedDataModel.getCurrentPublicKeyCredentialList();
            case PROTECTED_CONFIRMATION_CREDENTIAL: return saclSharedDataModel.getCurrentProtectedConfirmationCredential();
            case AUTHENTICATION_SIGNATURE: return saclSharedDataModel.getCurrentAuthenticationSignature();
            case AUTHORIZATION_SIGNATURE: return saclSharedDataModel.getCurrentAuthorizationSignature();
            default:
                Log.w(TAG, "Invalid SACL ObjectType: " + type);
                return null;
        }
    }

    public static <T extends View> List<T> findViewsWithType(View root, Class<T> type) {
        List<T> views = new ArrayList<>();
        findViewsWithType(root, type, views);
        return views;
    }

    private static <T extends View> void findViewsWithType(View view, Class<T> type, List<T> views) {
        if (type.isInstance(view)) {
            views.add(type.cast(view));
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                findViewsWithType(viewGroup.getChildAt(i), type, views);
            }
        }
    }

    /**
     *  Generates the current time, as based on the clock of the EJB Tier machine.
     *  NOTE:  The reason for the math with 1000 in the function is because MySQL
     *  does not store the milliseconds, while TimeStamp uses it.  However the
     *  XMLSignatures will fail if the milliseconds are not removed from the time
     *  before creating the time.  This implies that all timestamps are accurate
     *  only upto the second - not millisecond.  Good enough for this application,
     *  we believe.
     *
     * @return java.sql.Timestamp
     */
    public static Long now() {
        return ((new java.util.GregorianCalendar().getTimeInMillis())/1000)*1000;
    }

    /**
     * Returns the current date-time as milliseconds
     * @return long value of the current date-time since 1/1/1970 (UNIX epoch)
     */
    public static long nowms() {
        return new java.util.Date().getTime();
    }


    // Method to get resources from the SACL library
    public static Object getResource(Context c, String rname, String rtype, String rpkg) {
        if (rpkg == null)
            return c.getResources().getIdentifier(rname, rtype, "com.strongkey.jerseyfido");
        else
            return c.getResources().getIdentifier(rname, rtype, rpkg);
    }

    /**
     * Returns a JSON object with an error message, as well as the classname and methodname
     * where the error originated in the library
     *
     * @param c String with the classname
     * @param m String with the methodname from the class
     * @param k String with the JSON key
     * @param v String with the JSON value
     * @return JsonObject as follows:
     *  {
     *      "error" : {
     *          "c": "classname",
     *          "m": "methodname",
     *          "k": "v"
     *      }
     *  }
     * @throws JSONException
     */
    public static JSONObject JsonError(String c, String m, String k, String v) throws JSONException {
        return new JSONObject()
                .put("error", new JSONObject()
                        .put("c", c)
                        .put("m", m)
                        .put(k, v));
    }

    public static String JsonErrorString(String c, String m, String k, String v) {
        try {
            return JsonError(c, m, k, v).toString();
        } catch (JSONException ex) {

            return "{\"error\": {\"class\": ".concat(c).concat("\"method\": ").concat(m).concat(k).concat(v);
        }
    }


    public enum UserVerificationRequirement {REQUIRED, PREFERRED, DISCOURAGED};

    public enum AuthenticatorAttachment {PLATFORM, CROSS_PLATFORM};

    public enum AttestationConveyancePreference {NONE, INDIRECT, DIRECT};

    public enum AuthenticatorTransport {USB, NFC, BLE, INTERNAL};

    /**
     * From MDBA/MDKMS
     */
    /**
     * Generates a Base64-encoded SHA-256 digest of input
     * @param input byte[] with input to be hashed
     * @return byte[]
     */
    public static byte[] getSha256(byte[] input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input);
        } catch (NoSuchAlgorithmException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Generates a Base64-encoded SHA-256 digest of input
     * @param input String with input to be hashed
     * @return byte[]
     */
    public static byte[] getSha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            return md.digest(input.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
            return null;
        }
    }

    /**
     * Generates a Base64-encoded SHA-256 digest of input
     * @param input String with input to be hashed
     * @param encoding Constants.ENCODING indicating either BASE64 or HEX
     * @return String Base64- or Hex-encoded
     */
    public static String getSha256(byte[] input, Constants.ENCODING encoding) {
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            digest = md.digest(input);
        } catch (NoSuchAlgorithmException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
            return null;
        }
        if (encoding == Constants.ENCODING.BASE64)
            return Base64.getEncoder().encodeToString(digest);
        else
            return org.spongycastle.util.encoders.Hex.toHexString(digest);
    }

    /**
     * Generates a Base64-encoded SHA-256 digest of input
     * @param input String with input to be hashed
     * @param encoding Constants.ENCODING indicating either BASE64 or HEX
     * @return String Base64- or Hex-encoded
     */
    public static String getSha256(String input, Constants.ENCODING encoding) {
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            digest = md.digest(input.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
            return null;
        }
        if (encoding == Constants.ENCODING.BASE64)
            return Base64.getEncoder().encodeToString(digest);
        else
            return org.spongycastle.util.encoders.Hex.toHexString(digest);
    }

    /**
     * Generates a Base64- or Hex-encoded SHA384 digest of input
     * @param input String with input to be hashed
     * @param encoding Constants.ENCODING indicating either BASE64 or HEX
     * @return String Base64- or Hex-encoded
     */
    public static String getSha384(String input, Constants.ENCODING encoding) {
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-384");
            digest = md.digest(input.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
            return null;
        }
        if (encoding == Constants.ENCODING.BASE64)
            return Base64.getEncoder().encodeToString(digest);
        else
            return org.spongycastle.util.encoders.Hex.toHexString(digest);
    }

    /**
     * Generates a Base64- or Hex-encoded SHA512 digest of input
     * @param input String with input to be hashed
     * @param encoding Constants.ENCODING indicating either BASE64 or HEX
     * @return String Base64- or Hex-encoded
     */
    public static String getSha512(String input, Constants.ENCODING encoding) {
        byte[] digest;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            digest = md.digest(input.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
            return null;
        }
        if (encoding == Constants.ENCODING.BASE64)
            return Base64.getEncoder().encodeToString(digest);
        else
            return org.spongycastle.util.encoders.Hex.toHexString(digest);
    }

    /**
     * Gets a UUID-like structure (8-4-4-4-12) from hex input provided from
     * the device's fingerprint and the entropy from the KA.
     * @param input String with concatenation of fingerprint and KA entropy.
     * If input is null or 0 length, it will default to using SecureRandomstrong
     * locally to generate random bytes
     * @return String UUID-like string
     */

    public static String getUuid(String input) {
        if (input != null) {
            if (input.length() > 0) {
                String newinput = getSha256((input), Constants.ENCODING.HEX);
                return formatUuid(newinput);
            }
        }
        // If input is either null or 0 bytes - this is a failsafe return
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.putLong(nowms());
        return getUuid(buffer.array());
    }

    /**
     * Gets a UUID-like structure from byte array if bytes are provided
     * from the entropy service in the KA module
     * @param inbytes byte array - should be exactly 16-bytes
     * @return String UUID-like with 36 characters (8-4-4-4-12)
     */
    public static String getUuid(byte[] inbytes) {
        try {
            int len = inbytes.length;
            if (len < 16) {
                byte[] newbytes = new byte[16];
                SecureRandom.getInstance("SHA-256").nextBytes(newbytes);
                System.arraycopy(inbytes, 0, newbytes, 0, len);
                inbytes = newbytes;
            }
            String input = getSha256(inbytes, Constants.ENCODING.HEX);
            return formatUuid(input);
        } catch (NoSuchAlgorithmException ex) {
            Log.w(TAG, ex.getLocalizedMessage());
        }
        return null;
    }

    @SuppressWarnings("null")
    private static String formatUuid(String input) {
        int len = input.length();
        String newinput = null;

        if (len < 32) {
            return null;
        } else if (len > 32) {
            newinput = input.substring(0, 32);
        }
        return newinput.substring(0, 8)
                .concat("-")
                .concat(newinput.substring(8, 12))
                .concat("-")
                .concat(newinput.substring(12, 16))
                .concat("-")
                .concat(newinput.substring(16, 20))
                .concat("-")
                .concat(newinput.substring(20));
    }

    /**
     * Create the JSON of CollectedClientData
     * https://www.w3.org/TR/webauthn/#collectedclientdata-hash-of-the-serialized-client-data
     * @param operation Constants.FIDO_OPERATION indicating if it is a WEBAUTHN CREATE or GET
     * @param challenge String Base64-encoded
     * @param origin String - Top Level Domain (TLD) + 1 - must match rpid
     * @return JSONObject containing the encoded JSON of CollectedClientData
     * @throws JSONException
     */
    public static JSONObject getClientDataJSON(Constants.FIDO_OPERATION operation, String challenge, String origin) throws JSONException {
        String MTAG = TAG.concat("getClientDataJSON");
        Log.v(MTAG, "Input params - operation: " + operation + "\nchallenge: " + challenge + "\norigin: " + origin);

        // Assemble clientDataJson attributes into a JSON object
        JSONObject clientDataJson = null;
        if (operation.equals(Constants.FIDO_OPERATION.CREATE)) {
            clientDataJson = new JSONObject()
                    .put(Constants.WEBAUTHN_CLIENT_DATA_OPERATION_TYPE_KEY, Constants.WEBAUTHN_CLIENT_DATA_OPERATION_CREATE_VALUE)
                    .put(Constants.WEBAUTHN_CLIENT_DATA_CHALLENGE_KEY, challenge) // Has to be Base64Url encoded
                    .put(Constants.WEBAUTHN_CLIENT_DATA_ORIGIN_KEY, origin)
                    .put(Constants.WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_KEY, new JSONObject()
                            .put(Constants.WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_KEY, Constants.WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_NOT_SUPPORTED));
        } else if (operation.equals(Constants.FIDO_OPERATION.GET)) {
            clientDataJson = new JSONObject()
                    .put(Constants.WEBAUTHN_CLIENT_DATA_OPERATION_TYPE_KEY, Constants.WEBAUTHN_CLIENT_DATA_OPERATION_GET_VALUE)
                    .put(Constants.WEBAUTHN_CLIENT_DATA_CHALLENGE_KEY, challenge) // Has to be Base64Url encoded
                    .put(Constants.WEBAUTHN_CLIENT_DATA_ORIGIN_KEY, origin)
                    .put(Constants.WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_KEY, new JSONObject()
                            .put(Constants.WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_KEY, Constants.WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_NOT_SUPPORTED));
        }
        return clientDataJson;
    }

    /**
     * Create a Base64Url encoded string of a JSON of ClientData
     * https://www.w3.org/TR/webauthn/#collectedclientdata-hash-of-the-serialized-client-data
     * @param operation Constants.FIDO_OPERATION indicating if it is a WEBAUTHN CREATE or GET
     * @param challenge String Base64-encoded
     * @param origin String - Top Level Domain (TLD) + 1 - must match rpid
     * @return String containing the Base64Url encoded JSON of CollectedClientData
     * @throws JSONException
     */
    public static String getB64UrlSafeClientDataString(Constants.FIDO_OPERATION operation, String challenge, String origin) throws JSONException {
        String MTAG = "getB64UrlSafeClientDataString";
        JSONObject clientDataJson = getClientDataJSON(operation, challenge, origin);
        Log.v(MTAG, "clientDataJson: " + clientDataJson);
        String utf8EncodedClientData = new String(clientDataJson.toString().getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
        Log.v(MTAG, "utf8EncodedClientData: " + utf8EncodedClientData);
        String urlEncodedUTF8EncodedClientData = Common.urlEncode(utf8EncodedClientData);
        Log.v(MTAG, "urlEncodedUTF8EncodedClientData: " + urlEncodedUTF8EncodedClientData);
        return urlEncodedUTF8EncodedClientData;
    }

    /**
     * Create a Base64Url encoded message digest (hash) of ClientData
     * https://www.w3.org/TR/webauthn/#collectedclientdata-hash-of-the-serialized-client-data
     * @param operation Constants.FIDO_OPERATION indicating if it is a WEBAUTHN CREATE or GET
     * @param challenge String Base64-encoded
     * @param origin String - Top Level Domain (TLD) + 1 - must match rpid
     * @return String containing the Base64Url encoded SHA256 hash of CollectedClientData
     * @throws JSONException
     */
    public static String getB64UrlSafeClientDataHash(Constants.FIDO_OPERATION operation, String challenge, String origin) throws JSONException {
        String MTAG = "getB64UrlSafeClientDataHash";
        String b64UrlsafeEncodedClientDataString = getB64UrlSafeClientDataString(operation, challenge, origin);
        Log.v(MTAG, "b64UrlsafeEncodedClientDataString: " + b64UrlsafeEncodedClientDataString);
        String urlEncodedB64UrlsafeEncodedClientDataStringHash = Common.urlEncode(getSha256(Common.urlDecode(b64UrlsafeEncodedClientDataString)));
        Log.v(MTAG, "urlEncodedB64UrlsafeEncodedClientDataStringHash: " + urlEncodedB64UrlsafeEncodedClientDataStringHash);
        return urlEncodedB64UrlsafeEncodedClientDataStringHash;
    }

    /**
     * Base64 URL encoder
     */
    public static String urlEncode(String raw) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static String urlEncode(byte[] raw) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    /**
     * Base64 URL decoder
     */
    public static byte[] urlDecode(String raw) {
        return Base64.getUrlDecoder().decode(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] urlDecode(byte[] raw) {
        return Base64.getUrlDecoder().decode(raw);
    }

    /**
     * Encodes an EC public key in the COSE/CBOR format - similar to:
     *
     * new CborEncoder(baos).encode(new CborBuilder()
     *      .addMap()
     *      .put(1, 2)  // kty: EC2 key type
     *      .put(3, -7) // alg: ES256 sig algorithm
     *      .put(-1, 1) // crv: P-256 curve
     *      .put(-2, x) // x-coord
     *      .put(-3, y) // y-coord
     *      .end()
     *      .build()
     *  );
     *
     * @param publicKey The public key.
     * @return byte[] A COSE_Key-encoded public key as byte array.
     */
    public static byte[] coseEncodePublicKey(PublicKey publicKey)
    {
        ECPublicKey ecPublicKey = (ECPublicKey) publicKey;
        ECPoint point = ecPublicKey.getW();

        byte[] xVariableLength = point.getAffineX().toByteArray();
        byte[] yVariableLength = point.getAffineY().toByteArray();

        // See method below
        byte[] x = toUnsignedFixedLength(xVariableLength, 32);
        assert x.length == 32;
        byte[] y = toUnsignedFixedLength(yVariableLength, 32);
        assert y.length == 32;

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            CborEncoder cbe = new CborEncoder(baos);
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

        } catch (IOException e) {
            Log.w(TAG, e.getLocalizedMessage());
        }
        return baos.toByteArray();
    }

    /**
     * ECPoint coordinates are *unsigned* values that span the range [0, 2**32). The getAffine
     * methods return BigInteger objects, which are signed. toByteArray will output a byte array
     * containing the two's complement representation of the value, outputting only as many
     * bytes as necessary to do so. We want an unsigned byte array of length 32, but when we
     * call toByteArray, we could get:
     *
     * 1) A 33-byte array, if the point's unsigned representation has a high 1 bit.
     *    "toByteArray" will prepend a zero byte to keep the value positive.
     * 2) A <32-byte array, if the point's unsigned representation has 9 or more high zero bits.
     *
     * Due to this, we need to either chop off the high zero byte or prepend zero bytes until
     * we have a 32-length byte array.
     */
    private static byte[] toUnsignedFixedLength(byte[] arr, int fixedLength) {
        byte[] fixed = new byte[fixedLength];
        int offset = fixedLength - arr.length;
        int srcPos = Math.max(-offset, 0);
        int dstPos = Math.max(offset, 0);
        int copyLength = Math.min(arr.length, fixedLength);
        System.arraycopy(arr, srcPos, fixed, dstPos, copyLength);
        return fixed;
    }

    /**
     * Sets bit-flags indicating what is asserted in Authenticator Data
     * https://www.w3.org/TR/webauthn/#sec-authenticator-data
     *
     * @param flagConfig: Boolean[] that holds UP, UV, AT, ED
     * @return flags: byte[] containing 1 byte.
     */
    public static byte[] setFlags(Boolean[] flagConfig) {

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
        if (hasAttested) {
            flags |= (0x01 << 6); // attested credential data included
        }
        return flags;
    }

    /**
     * Helper method that converts a BitSet to byte[].
     *
     * @param bits The BitSet to be converted into a byte[] format.
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
     * @param i The bigInt to be converted to byte[] format.
     * @return bigInt.toByteArray(). byte[] that represents @param i.
     */
    public static byte[] bigIntToByteArray(int i) {
        BigInteger bigInt = BigInteger.valueOf(i);
        return bigInt.toByteArray();
    }

    /**
     * Generates 4-byte counter from int
     */
    public static byte[] getCounterBytes(int i) {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.putInt(i);
        return bb.array();
    }

    /**
     * Extract the TLD+1 from the origin of the app's webservice URL
     */
    public static String getTldPlusOne(String wsurl) {
        String MTAG = TAG.concat("getOrigin");
        String tldPlusOne = null;
        try {
            URL url = new URL(wsurl);
            String fqdn = url.getHost();
            int pos = fqdn.indexOf('.');
            tldPlusOne = fqdn.substring(pos+1);
            Log.v(MTAG, "\nTLD+1: " + tldPlusOne + '\n' +
                    "URI: " + url.toURI());
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return tldPlusOne;
    }

    /**
     * Extract the RFC6454 origin of the app's webservice URL
     * https://tools.ietf.org/html/rfc6454#page-10
     */
    public static String getRfc6454Origin(String wsurl) {
        String MTAG = TAG.concat("getRfc6454Origin");
        String origin = null;
        try {
            URL url = new URL(wsurl);
            String scheme = url.getProtocol();
            String fqdn = url.getHost();
            int port = url.getPort();
            if (port != -1) {
                String portString = String.valueOf(port);
                int portpos = wsurl.indexOf(portString);
                origin = wsurl.substring(0, (portpos + (portString.length())));
            } else {
                int fqdnpos = wsurl.indexOf(fqdn);
                origin = wsurl.substring(0, (fqdnpos + fqdn.length()));
            }
            Log.v(MTAG, "\nRFC6454-Origin: " + origin + '\n' +
                    "URI: " + url.toURI());
        } catch (MalformedURLException | URISyntaxException e) {
            e.printStackTrace();
        }
        return origin;
    }
    /**
     * Print very long long message (> 4000 chars) because logcat won't print it
     * @param message JSONArray
     */
    public static void printVeryLongLogMessage(String msgtype, String message) {

        String dashes = "\n---------------------------------------------------------------------\n";
        int msglen = message.length();
        Log.v(TAG, "Size of message: " + msglen + dashes);
        int loop = msglen/4000;
        int startIndex = 0;
        int endIndex = 4000;
        for (int i = 0; i < loop; i++) {
            Log.v(TAG, msgtype + " - Part: " + i + '\n' + message.substring(startIndex, endIndex));
            startIndex = endIndex;
            endIndex += 4000;
        }
        Log.v(TAG, msgtype + " - Final Part: " + message.substring(startIndex) + dashes);
    }


    /**
     * Generate the CBOR for the UVM extension for AndroidKeystore
     * @param extensions A string array with required extensions from Constants
     * @return byte[] CBOR output
     */
    public static byte[] getCborExtensions(String[] extensions, String secureHardware) throws IOException
    {
        String MTAG = TAG.concat("getCborExtensions");
        if (extensions == null || secureHardware == null) {
            Log.w(MTAG, "Required input parameters are null");
            throw new IOException(MTAG + ": Required input parameters are null");
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        CborEncoder cbe = new CborEncoder(baos);

        // How many extensions are we supporting? For now only UVM
        int extlen = extensions.length;

        // What type of security hardware is in use?
        String semodule = secureHardware.substring(0, 4);
        if (!(semodule.equalsIgnoreCase("true"))) {
            Log.w(MTAG, "Not using secure hardware - cannot use AndroidKeystore: " + secureHardware.substring(0, 4));
            throw new IOException(MTAG + ": Not using secure hardware - cannot use AndroidKeystore: " + secureHardware.substring(0, 4));
        }

        // Figure out key-protection: TEE or SE
        int startpos = secureHardware.indexOf('[');
        int endpos = secureHardware.indexOf(']');
        int keyProtection = 0;
        String setype = secureHardware.substring(startpos+1, endpos);
        Log.v(MTAG, "Secure Hardware: " + setype);
        if (setype.equalsIgnoreCase("SECURE_ELEMENT"))
            keyProtection = Constants.FIDO_KEY_PROTECTION_HARDWARE + Constants.FIDO_KEY_PROTECTION_SECURE_ELEMENT;
        else
            keyProtection= Constants.FIDO_KEY_PROTECTION_HARDWARE + Constants.FIDO_KEY_PROTECTION_TEE;

        // Map entry with 1 elements for uvmEntry
        cbe.writeMapStart(extlen);

        for (int i = 0; i < extlen; i++) {
            switch(extensions[i]) {
                case Constants.FIDO_EXTENSION_USER_VERIFICATION_METHOD:
                    // First element
                    cbe.writeTextString(Constants.FIDO_EXTENSION_USER_VERIFICATION_METHOD);

                    // Second element - CBOR Array of length 1
                    cbe.writeArrayStart(1);

                    // Item 1 - CBOR Array of length 3
                    cbe.writeArrayStart(3);

                    // Subitems of item 1
                    // See notes at end of file
                    cbe.writeInt(Constants.FIDO_USER_VERIFY_PASSCODE);  // 0x00000004
                    cbe.writeInt(keyProtection);
                    cbe.writeInt(1);  // MATCHER_PROTECTION_SOFTWARE 0x0001
                    break;
                default:
                    Log.w(MTAG, "No other FIDO extension currently supported yet");
                    throw new IOException(MTAG + ": No other FIDO extension currently supported yet");
            }
        }

        // Convert to byte-array and hex-encode to a string for display
        byte[] result = baos.toByteArray();
        Log.v(MTAG, new String(org.spongycastle.util.encoders.Hex.encode(result), StandardCharsets.UTF_8));
        return result;
    }

    public static String getNewCredentialId(String rpid, String userid) throws UnsupportedEncodingException, NoSuchAlgorithmException {

        String uuid = UUID.randomUUID().toString();
        Long now = new Date().getTime();
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);

        byte[] rpidbytes = rpid.getBytes("UTF-8");
        byte[] useridbytes = userid.getBytes("UTF-8");
        byte[] uuidbytes = uuid.getBytes("UTF-8");
        byte[] nowbytes = byteBuffer.putLong(now.longValue()).array();

        int inputpos = 0;
        int inputlen = rpidbytes.length + useridbytes.length + uuidbytes.length + nowbytes.length;
        byte[] input = new byte[inputlen];

        System.arraycopy(rpidbytes, 0, input, inputpos, rpidbytes.length);
        inputpos += rpidbytes.length;

        System.arraycopy(useridbytes, 0, input, inputpos, useridbytes.length);
        inputpos += useridbytes.length;

        System.arraycopy(uuidbytes, 0, input, inputpos, uuidbytes.length);
        inputpos += uuidbytes.length;

        System.arraycopy(nowbytes, 0, input, inputpos, nowbytes.length);

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] output = digest.digest(input);
        String hexdigest = Hex.toHexString(output).toUpperCase();
        if (hexdigest == null) {
            Log.w(TAG, "Could not generate new credentialId");
            return null;
        }
        StringBuilder sb = new StringBuilder();
        sb.append(hexdigest.substring(0, 16))
                .append("-")
                .append(hexdigest.substring(16, 32))
                .append("-")
                .append(hexdigest.substring(32, 48))
                .append("-")
                .append(hexdigest.substring(48));
        Log.v(TAG, "Generated new credentialId: " + sb.toString());
        return sb.toString();
    }

    /**
     * Returns a JSON object with an error message, as well as the classname and methodname
     * where the error originated in the library
     *
     * @param c String with the classname
     * @param m String with the methodname from the class
     * @param k String with the JSON key
     * @param v String with the JSON value
     * @return JSONObject as follows:
     *  {
     *      "error" : {
     *          "c": "classname",
     *          "m": "methodname",
     *          "k": "v"
     *      }
     *  }
     * @throws JSONException
     */
    public static JSONObject jsonError(String c, String m, String k, String v) throws JSONException {
        return new JSONObject()
                .put("error", new JSONObject()
                        .put("c", c)
                        .put("m", m)
                        .put(k, v)
                );
    }

    /**
     * Converts a long value with milliseconds into a Date
     * @param value
     * @return
     */
    @TypeConverter
    public static Date dateFromLong(Long value) {
        return value == null ? null : new Date(value);
    }

    // Converts a Date into milliseconds
    @TypeConverter
    public static Long dateToLong(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }

    /**
     * Validates that the Json response from the FIDO2 server matches this app's expectations
     *
     * @param prResponse PreregisterChallenge with the FIDO2 Server's response to the
     *                   preregister() webservice
     * @return JSONObject containing only pertinent information for the app to generate a key-pair
     * or a JSONObject with an error
     *
     * ValidChallenge Object: {
     *      "displayname": "Ricky Ricardo",
     *      "username": "ricky",
     *      "rpid": "strongkey.com",
     *      "credentialId": "wr9eOydvTxi2o3HVkUNJWwNV2oe3umsJSL8ibbnEtCY",
     *      "challenge": "y2A6Ic3d2_oFzbumYJze5g",
     *      "ES256": -7,
     *      "RS256": -257
     * }
     */
    public static JSONObject validateChallenge(PreregisterChallenge prResponse, LocalContextWrapper context) {

        JSONObject validJson = new JSONObject();
        try {
            if (prResponse == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "PreregisterChallenge is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            // PreregisterChallenge is not null - check displayname
            String responseDisplayname = prResponse.getDisplayName();
            if (responseDisplayname == null) {
                validJson.put(Constants.JSON_KEY_FIDO_PAYLOAD_DISPLAY_NAME, responseDisplayname);
            }

            // Check username
            String responseUsername = prResponse.getUsername();
            if (responseUsername == null) {
                validJson.put(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_USERNAME_LABEL, prResponse.getUsername());
            }

            // Check RPID
            if (prResponse.getRpid() == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "RPID is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            validJson.put(Constants.JSON_KEY_RPID, prResponse.getRpid());


            // Check if we have a non-null credentialId (userid)
            if (prResponse.getUserid() == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "CredentialID is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            validJson.put(Constants.JSON_KEY_PREREG_RESPONSE_USER_USER_ID, prResponse.getUserid());

            // Check if we have a non-null challenge nonce
            if (prResponse.getChallenge() == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "Challenge nonce is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            validJson.put(Constants.JSON_KEY_PREREG_RESPONSE_CHALLENGE, prResponse.getChallenge());

            // Check if we have ECDSA algorithm ES256 (-7) or RSA256 (-257)
            JSONArray ja = prResponse.getCredParamsJSONArray();
            if (ja == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "PublicKeyCredentialParams is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            int jalen = ja.length();
            int i = 0;
            while (i < jalen) {
                JSONObject jo = ja.getJSONObject(i);
                if (jo.getInt(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS_ALG) == -7) {
                    validJson.put(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_ES256_LABEL, jo.getInt(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS_ALG));
                } else if (jo.getInt(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS_ALG) == -257) {
                    validJson.put(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_RS256_LABEL, jo.getInt(Constants.JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS_ALG));
                }
                i++;
            }

            // Check for Attestation type
            if (prResponse.getAttestationConveyance() == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", context.getString(R.string.null_attestation));
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            // Check for ExcludeCredentials
            if (prResponse.getExcludeCredentials() != null) {
                validJson.put(Constants.JSON_KEY_PREREG_RESPONSE_EXCLUDE_CREDENTIALS, prResponse.getExcludeCredentials());
            }

            Log.d(TAG, "ValidChallenge Object: " + validJson.toString(2));
            return validJson;

        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Validates that the Json response from the FIDO2 server matches this app's expectations
     * @param paResponse PreauthenticateChallenge with the FIDO2 Server's response to the
     * preauthenticate() webservice
     * @return JSONObject containing only pertinent information for the app to generate a key-pair
     * or a JSONObject with an error
     *
     * ValidatedChallenge: {
     *       "rpid": "noorhome.net",
     *       "challenge": "rrLiqgSQoUKv7k5uKKK3zg",
     *       "pkcid": 4,
     *       "pkcDid": 1,
     *       "pkcCredentialId": "CDD35411B44314C8-489C76F89B54E6CA-9082B81AE869F87D-08D330811838BC3C"
     *     }
     */
    public static JSONObject validateChallenge(PreauthenticateChallenge paResponse) {

        JSONObject validatedJson = new JSONObject();
        try {
            if (paResponse == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "PreauthenticateChallenge is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            // Check RPID
            if (paResponse.getRpid() == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "RPID is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            validatedJson.put(Constants.JSON_KEY_RPID, paResponse.getRpid());

            // Check if we have a non-null challenge nonce
            if (paResponse.getChallenge() == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "Challenge nonce is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }
            validatedJson.put(Constants.JSON_KEY_PREAUTH_RESPONSE_CHALLENGE, paResponse.getChallenge());

            // Check if we have allowCredentials with ECDSA algorithm ES256 (-7) or RSA256 (-257)
            JSONArray ja = paResponse.getAllowCredentialsJSONArray();
            if (ja == null) {
                try {
                    return Common.JsonError(TAG, "validateChallenge", "param", "AllowCredentials is NULL");
                } catch (JSONException je) {
                    je.printStackTrace();
                }
            }

            List<String> allowedCredentialIdList = new ArrayList<>();
            int jalen = ja.length();
            int i = 0;
            while (i < jalen) {
                JSONObject jo = ja.getJSONObject(i);
                if (jo.has(Constants.JSON_KEY_PREAUTH_RESPONSE_CREDENTIAL_ID)) {
                    String cred = jo.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_CREDENTIAL_ID);
                    Log.v(TAG, "CredentialId: " + cred);
                    allowedCredentialIdList.add(cred);
                }
                i++;
            }
            Log.v(TAG, "List of cId: " + allowedCredentialIdList);

            // Check if a PublicKeyCredential exists for any of these valid credentialIds
            List<PublicKeyCredential> publicKeyCredentials = saclRepository.getAllPublicKeyCredentials();
            for (PublicKeyCredential p : publicKeyCredentials) {
                System.out.println("p: " + p.getCredentialId());
                String cred = p.getCredentialId();
                for (String s : allowedCredentialIdList) {
                    System.out.println("s: " + s);
                    String decodedCredId = new String((Base64.getUrlDecoder().decode(s)), "UTF-8");
                    System.out.println("Decoded s: " + decodedCredId);
                    if (cred.equalsIgnoreCase(decodedCredId)) {
                        Common.printVeryLongLogMessage(TAG, "Found a decoded credential for RPID on this device: " + p.toJSON().toString());
                        validatedJson.put(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_ID, p.getId());
                        validatedJson.put(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_DID, p.getDid());
                        validatedJson.put(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_CREDENTIAL_ID, p.getCredentialId());
                    }
                }
            }

            Log.v(TAG, "Validated Challenge Object: " + validatedJson.toString(2));
            return validatedJson;

        } catch (JSONException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
            return null;
        }
    }

    /**
     * Converts a JSON string to a JSON object
     * @param jsonString
     * @return JSONObject
     * @throws JSONException
     */
    public static JSONObject toJSON(String jsonString) throws JSONException {
        return (JSONObject) new JSONTokener(jsonString).nextValue();
    }

    public enum KeyType {AES, FIDO2, ECDSA, RSA, X509, OTHER}

    public enum RequireAuth {BIOMETRIC, DEVICE, NONE, ANY}

    public enum RequireSecurity {TEE, SE, ANY}

    public enum AllowMode {CBC, CTR, ECB, GCM, ANY}

    public enum AllowPadding {NONE, PKCS1, PKCS7, OAEP, ANY}

    public enum AllowDigest {SHA224, SHA256, SHA384, SHA512, ANY}

    public enum AllowHmac {SHA224, SHA256, SHA384, SHA512, ANY}

    public enum AllowKeyPurpose {ENC, DEC, ENCDEC, WRAP, ANY}

    public enum AllowKeypairPurpose {SIGN, VERIFY, ANY}

    public enum AllowFidoPurpose {AUTH, TXC, ANY}

    public static enum KeyStatus {
        Active,
        Suspended,
        Revoked,
        Expired,
        Other
    }

}