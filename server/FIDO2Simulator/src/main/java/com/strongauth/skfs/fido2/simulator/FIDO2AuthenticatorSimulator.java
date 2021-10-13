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
 * This program simulates a FIDO2 Authenticator to work with StrongKeys's
 * open-source implementation of its FIDO Certified FIDO2 server (available
 * on github.org/strongkey/fido2). The simulator is designed to call
 * webservices on the FIDO2 server.
 *
 * There is no main() method to call in this project, since it isn't meant to
 * be used as a standalone program.  However, the associated SKF2ASClient
 * project makes use of this simulator to test the FIDO2 server with a
 * registration and authentication transaction
 *
 */

package com.strongauth.skfs.fido2.simulator;

import com.strongauth.skfs.fido2.artifacts.ClientData;
import com.strongauth.skfs.fido2.artifacts.Common;
import com.strongauth.skfs.fido2.artifacts.Constants;
import com.strongauth.skfs.fido2.artifacts.FIDO2AttestationObject;
import com.strongauth.skfs.fido2.artifacts.FIDO2AuthenticatorData;
import com.strongauth.skfs.fido2.artifacts.FIDO2Extensions;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class FIDO2AuthenticatorSimulator
{
    private static final String CLIENT_DATA = "";

    /* Getter for the ClientData. */
    public static String getClientData() {
        return CLIENT_DATA;
    }

    public byte[] encodeBase64(String encodeMe) {
        byte[] encodedBytes = Base64.getUrlEncoder().withoutPadding().encode(encodeMe.getBytes());
        return encodedBytes;
    }

    /*
    d88888b d888888b d8888b.  .d88b.  .d888b.        d88888b d8b   db
    88'       `88'   88  `8D .8P  Y8. VP  `8D        88'     888o  88
    88ooo      88    88   88 88    88    odD'        88ooo   88V8o 88
    88ooo      88    88   88 88    88  .88'   C8888D 88ooo   88 V8o88
    88        .88.   88  .8D `8b  d8' j88.           88      88  V888
    YP      Y888888P Y8888D'  `Y88P'  888888D        YP      VP   V8P
     */

    /**
     * Function to generate a a new ECDSA key-pair to register with the
     * StrongKey FIDO Certified FIDO2 server.
     *
     * @param appidfromserver
     * @param registrationRequest
     * @param origin
     * @param format
     * @param attestationType
     * @param goodsignature
     * @return JsonObject containing the registration response from this simulator
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws UnsupportedEncodingException
     * @throws java.security.KeyStoreException
     * @throws java.security.cert.CertificateException
     * @throws java.security.InvalidKeyException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws java.security.spec.InvalidParameterSpecException
     * @throws java.security.SignatureException
     * @throws java.io.FileNotFoundException
     * @throws javax.crypto.NoSuchPaddingException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws org.apache.commons.codec.DecoderException
     * @throws javax.crypto.BadPaddingException
     * @throws javax.crypto.ShortBufferException
     * @throws java.security.UnrecoverableKeyException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws co.nstant.in.cbor.CborException
     */
    public static JsonObject generateFIDO2RegistrationResponse(
            String appidfromserver,
            String registrationRequest,
            String origin,
            String format,
            String attestationType,
            boolean goodsignature,
            boolean crossOrigin)
        throws
            NoSuchAlgorithmException, NoSuchProviderException,
            UnsupportedEncodingException, KeyStoreException, IOException,
            CertificateException, InvalidAlgorithmParameterException,
            InvalidKeyException, InvalidKeyException, InvalidKeyException,
            InvalidKeyException, InvalidKeyException, InvalidKeyException,
            InvalidKeyException, InvalidKeyException, InvalidKeyException,
            SignatureException, SignatureException,
            InvalidParameterSpecException, FileNotFoundException,
            NoSuchPaddingException, IllegalBlockSizeException,
            BadPaddingException, ShortBufferException,
            UnrecoverableKeyException, InvalidKeySpecException
    {
        // Get the challenge nonce from the FIDO2 server's signature request
//        System.out.println("signreq = " + registrationRequest);

        JsonObject registrationJson;
        try (JsonReader jsonReader = Json.createReader(new StringReader(registrationRequest))) {
            registrationJson = jsonReader.readObject();
        }
        String challenge = registrationJson.getString(Constants.JSON_KEY_CHALLENGE_LABEL);
//        System.out.println("ChallengeNonce:        " + challenge);

        // Get the Relying Party Identifier and calculate its SHA-256 digest
        String rpid = null;
        byte[] rpidhash;

        JsonObject rpObject = registrationJson.getJsonObject(Constants.JSON_KEY_RP_LABEL);
        try {
            rpid = rpObject.getString("id");
            rpidhash = Common.getRPID(rpid);
        } catch (NullPointerException ex) {
            rpidhash = Common.getRPID(origin);
        }
//        System.out.println("rpid [rpidhash]:        " + rpid + " [" + new String(rpidhash, "UTF-8") + "]");

        // Create the ClientData object
        byte flags = Common.getFlag(Constants.REGISTRATION_FLAGS);
        ClientData cd = new ClientData("webauthn.create", challenge, origin, crossOrigin);
        String cds = cd.toJsonString();

        // Generate the challenge parameter (SHA-256 digest of nonce)
        String cdhash = Common.getDigest(cds, "SHA256");
//        System.out.println("cdhash:     " + cdhash);

        // Generate ECDSA key-pair using the secp256r1 curve
        KeyPair keypair = Common.generateKeypair();
        PublicKey pbk = keypair.getPublic();
        PrivateKey pvk = keypair.getPrivate();

        // Create encrypted, Base64-encoded key-handle
        String aaguidhash = Common.getDigest(Constants.AAGUID, "SHA-256");
        String keyhandle = Common.makeKeyHandle(pvk, aaguidhash);
        if (keyhandle != null) {
//            System.out.println("WrappedKeyHandle:       " + keyhandle);
        } else {
            System.err.println("Got a NULL key-handle; aborting..");
            return null;
        }

        // Create the authenticator data
        FIDO2AuthenticatorData authdata = new FIDO2AuthenticatorData(rpidhash, flags, Boolean.TRUE, Constants.FIDO_REGISTER_COUNTER, new FIDO2Extensions());
        byte[] AD = authdata.encodeAuthData(Constants.AAGUID.getBytes(), java.util.Base64.getUrlDecoder().decode(keyhandle), pbk);

        byte[] browserdatabytes = java.util.Base64.getUrlDecoder().decode(cdhash);
        // Create to-be-signed (TBS) object (Registration Data)

        byte[] tbs = new byte[AD.length + browserdatabytes.length];

        System.arraycopy(AD, 0, tbs, 0, AD.length);
        System.arraycopy(browserdatabytes, 0, tbs, AD.length, browserdatabytes.length);
//        System.out.println("To-Be-Signed (TBS):     " + Arrays.toString(tbs));

        // Create the attestation object - currently only supports "packed" format
        FIDO2AttestationObject attestationObject = new FIDO2AttestationObject(format, authdata);

        // Combine authdata, attStmt, and format to create AttestationObject
        String attObject = attestationObject.encodeAttestationObject(AD, tbs, attestationType, pvk, pbk);


        String base64clientData = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(cds.getBytes());
        JsonObject response = Json.createObjectBuilder()
                .add("attestationObject", attObject)
                .add("clientDataJSON", base64clientData)
                .build();

        String encodedID = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(java.util.Base64.getUrlDecoder().decode(keyhandle));
        JsonObject registrationResponse = Json.createObjectBuilder()
                .add("id", encodedID)
                .add("rawId", encodedID)
                .add("response", response)
                .add("type", "public-key")
                .build();
        return registrationResponse;
    }

    /**
     * Function to generate an attestation - an authentication response to a
     * challenge sent from the Relying Party's web-application
     *
     * @param keyhandle
     * @param challenge
     * @param origin
     * @param format
     * @param counter
     * @param goodsignature
     * @return
     * @throws KeyStoreException
     * @throws IOException
     * @throws FileNotFoundException
     * @throws NoSuchAlgorithmException
     * @throws CertificateException
     * @throws UnrecoverableKeyException
     * @throws NoSuchProviderException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws InvalidParameterSpecException
     * @throws DecoderException
     * @throws NoSuchPaddingException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws UnsupportedEncodingException
     * @throws InvalidKeySpecException
     * @throws Exception
     */
    public static JsonObject generateFIDO2AuthenticationResponse(
            String keyhandle,
            String challenge,
            String origin,
            String format,
            int counter,
            boolean goodsignature,
            boolean crossOrigin
    ) throws KeyStoreException, IOException, FileNotFoundException,
            NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException,
            NoSuchProviderException, InvalidKeyException, SignatureException,
            InvalidParameterSpecException, NoSuchPaddingException,
            InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException,
            BadPaddingException, UnsupportedEncodingException, InvalidKeySpecException,
            Exception
    {
        
        JsonObject authJson;
        try (JsonReader jsonReader = Json.createReader(new StringReader(challenge))) {
            authJson = jsonReader.readObject();
        }
        String nonce = authJson.getString(Constants.JSON_KEY_CHALLENGE_LABEL);
        byte[] rpIDHash;
        if(authJson.containsKey(Constants.JSON_KEY_RPID_LABEL)){
            String rpid = authJson.getString(Constants.JSON_KEY_RPID_LABEL);
            rpIDHash = Common.getDigestBytes(rpid,"SHA256");
       
        }else{
            rpIDHash = Common.getRPID(new URI(origin).getHost());
        }
        
        
        
        /* Build clientDataJson. */
        ClientData cd = new ClientData("webauthn.get", nonce, origin, crossOrigin);
        String cds = cd.toJsonString();

        /* Step 10. Create the almighty AUTHDATA (without attestedCredentialData). */
        /* Sets the RP ID Hash. */
//        byte[] rpIDHash = Common.getRPID(new URI(origin).getHost());

        byte flags = 0x00;
        flags |= 0x01;

        ByteBuffer authdataBuffer = ByteBuffer.allocate(37);

        authdataBuffer.put(rpIDHash);
        authdataBuffer.put(flags);
        authdataBuffer.putInt(counter);
        byte[] authdata = authdataBuffer.array();

//        String keyHandle = selectedCredential.getKeyHandle();
//        String clientDataHash = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(clientData.toString().getBytes());
        
        
        // Generate the challenge parameter (SHA-256 digest of nonce)
        String cdhash = Common.getDigest(cds, "SHA256");

         byte[] browserdatabytes = java.util.Base64.getUrlDecoder().decode(cdhash);
         
        byte[] tbs = new byte[authdata.length + browserdatabytes.length];
//
        System.arraycopy(authdata, 0, tbs, 0, authdata.length);
        System.arraycopy(browserdatabytes, 0, tbs, authdata.length, browserdatabytes.length);
//        byte[] tbs = Bytes.concat(authdata, Common.getDigestBytes(java.util.Base64.getDecoder().decode(clientDataHash), "SHA-256"));

        // Decrypt KeyHandle
        String khjson;

        khjson = Common.decryptKeyHandle(keyhandle);
//        System.out.println("PlaintextKeyHandle:   " + khjson);
//        System.out.println("counter = " + counter);

        // Extract user's private-key
        PrivateKey pvk = null;
        try {
            pvk = Common.getUserPrivateKey(Common.decodeKeyHandle(khjson, 0));
        } catch (InvalidKeySpecException ex) {
            Logger.getLogger(FIDO2AuthenticatorSimulator.class.getName()).log(Level.SEVERE, null, ex);
        }

        String signature = Common.signWithCredKey(pvk, java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(tbs));

//        /* Encode the authdata using CBOR and Base64. */
//        ByteArrayOutputStream baos = new ByteArrayOutputStream();
//        try {
//            new CborEncoder(baos).encode(new CborBuilder()
//                    .add(authdata)
//                    .build()
//            );
//        } catch (CborException e) {
//            throw new Exception("Failed to CBOR encode authdata.", e);
//        }
//        byte[] encodedAuthData = baos.toByteArray();
        String Base64AuthData = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(authdata);

        String base64clientData = Base64.getUrlEncoder().withoutPadding().encodeToString(cds.getBytes());
        /* Return the authResponse to the user in JSON format. */
        JsonObject miniResp = Json.createObjectBuilder()
                .add("authenticatorData", Base64AuthData)
                .add("signature", signature)
                .add("userHandle", "")
                .add("clientDataJSON", base64clientData)
                .build();

        JsonObject authResponse = Json.createObjectBuilder()
                .add("id", keyhandle)
                .add("rawId", keyhandle)
                .add("response", miniResp)
                .add("type", "public-key")
                .build();

        return authResponse;
    }


    /*
    db    db .d888bo d88888b        d88888b d8b   db
    88    88 VP  J8D 88'            88'     888o  88
    88    88    odDP 88ooo          88ooo   88V8o 88
    88    88  d88P   88      C8888D 88      88 V8o88
    88b  d88 j88.    88             88      88  V888
    ~Y8888P' 888888D YP             YP      VP   V8P
     */

    /**
     * Function to generate a a new ECDSA key-pair to register with the
     * StrongKey CryptoEngine (SKCE), StrongAuth's FIDO Certified U2F server.
     * While StrongAuth's FIDO Client (skceclient) is a useful test-tool to call
     * these functions, this software can be adapted to any other Java- based
     * "FIDO Client" if desired.
     *
     * @param appidfromserver
     * @param signrequest String containing the JSON blob from the SKCE. The
     * blob was sent as a result of the preregister call to the FIDOEngine in
     * the SKCE
     * @param origin String containing the URL of the RP web-application
     * @param goodsignature boolean An indicator to the software token whether
     * it should generate a normal (good) signature, or create a signature which
     * is defective. This is helpful to test if a server is able to detect the
     * bad-signature and reject it. Currently, the only thing "bad" we're doing
     * is using the wrong appid. Future versions will create more failure
     * situations with the signature.
     *
     * @return JsonObject containing the Registration Response
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws UnsupportedEncodingException
     * @throws KeyStoreException
     * @throws IOException
     * @throws CertificateException
     * @throws InvalidAlgorithmParameterException
     * @throws InvalidKeyException
     * @throws SignatureException
     * @throws NoSuchPaddingException
     * @throws FileNotFoundException
     * @throws DecoderException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws ShortBufferException
     * @throws UnrecoverableKeyException
     * @throws InvalidKeySpecException
     * @throws java.security.spec.InvalidParameterSpecException
     */
    public static JsonObject generateRegistrationResponse(String appidfromserver,
            String signrequest,
            String origin,
            //                                                        String sessionid,
            boolean goodsignature)
            throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, KeyStoreException, 
            IOException, CertificateException, InvalidAlgorithmParameterException, InvalidKeyException, SignatureException, 
            NoSuchPaddingException, FileNotFoundException, IllegalBlockSizeException, BadPaddingException, 
            ShortBufferException, UnrecoverableKeyException, InvalidKeySpecException, InvalidParameterSpecException {
        // Get the challenge nonce from the U2F server's signature request
        String challenge = (String) Common.decodeRegistrationSignatureRequest(signrequest, Constants.JSON_KEY_CHALLENGE);
        System.out.println("ChallengeNonce:         " + challenge);

        // Generate Clientdata from signature request
        String clientdata = Common.clientDataEncoder(
                Constants.REGISTER_CLIENT_DATA_OPTYPE,
                challenge,
                origin,
                Constants.REGISTER_CLIENT_DATA_CHANNELID);
        System.out.println("ClientData:             " + clientdata);

        // Generate the challenge parameter (hash of nonce)
        String chalparam = Common.getDigest(clientdata, "SHA-256");
        System.out.println("ChallengeParameter:     " + chalparam);

        // Get the appid - if the flag for goodsignature is false, use a bad
        // appid to force a flaw in the signature; used in server testing
        String appid;
        if (goodsignature) {
            appid = appidfromserver;
        } else {
            appid = Constants.REGISTER_CLIENT_BAD_APPID;
        }

        // Generate the application parameter (hash of appid)
        String appparam = Common.getDigest(appid, "SHA-256");
        System.out.println("ApplicationParameter:   " + appparam);

        // Generate new key-pair and create RegistrationData
        String regdata = "";//Common.generateFIDOKeyPair(chalparam, appparam, origin, appidfromserver);
        if (regdata.isEmpty()) {
            System.err.println("Fatal Error: Could not create RegistrationData");
            System.exit(1);
        }

        // Create Registration Response
        JsonObject RegistrationResponse = Common.createRegistrationResponse(clientdata, regdata);
        System.out.println("RegistrationResponse:   " + RegistrationResponse.toString());
        return RegistrationResponse;
    }

    /**
     * Function to generate a response to a request for a signature when a FIDO
     * Client (skceclient in StrongAuth's case) is authenticating to the
     * StrongKey CryptoEngine (SKCE), StrongAuth's FIDO Certified U2F server.
     *
     * @param challenge
     * @param appidfromserver
     * @param signrequest String containing the JSON signature request from the
     * SKCE; this was sent as a result of the preauthenticate call to the
     * FIDOEngine in the SKCE
     * @param origin String containing the URL of the RP web-application
     * @param counter int containing a numeric counter value for the number of
     * signatures the software-authenticator has created. In a real token, this
     * is a finite number, but the software token will take any value from the
     * calling application. The software token does NOT store the counter value
     * anywhere
     * @param goodsignature boolean An indicator to the software token whether
     * it should generate a normal (good) signature, or create a signature which
     * is defective. This is helpful to test if a server is able to detect the
     * bad-signature and reject it. Currently, the only thing "bad" we're doing
     * is using the wrong appid. Future versions will create more failure
     * situations with the signature.
     *
     * @return JsonObject containing the Authentication Response
     *
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     * @throws UnsupportedEncodingException
     * @throws DecoderException
     * @throws NoSuchPaddingException
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws ShortBufferException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws InvalidKeySpecException
     * @throws InvalidParameterSpecException
     */
    public static JsonObject generateAuthenticationResponse(String appidfromserver,
            String challenge,
            String signrequest,
            String origin,
            int counter,
            boolean goodsignature)
        throws
            NoSuchAlgorithmException, NoSuchProviderException,
            UnsupportedEncodingException,
            NoSuchPaddingException, InvalidKeyException,
            InvalidAlgorithmParameterException, ShortBufferException,
            IllegalBlockSizeException, BadPaddingException,
            InvalidKeySpecException, InvalidParameterSpecException
    {
        // Get the challenge nonce from the U2F server's signature request
        // String challenge = Common.decodeAuthRequestJsonKeys(signrequest, Constants.JSON_KEY_CHALLENGE);
        System.out.println("ChallengeNonce:       " + challenge);

        // Generate Clientdata from signature request
        String clientdata = Common.clientDataEncoder(
                Constants.AUTHENTICATE_CLIENT_DATA_OPTYPE,
                challenge,
                origin,
                Constants.AUTHENTICATE_CLIENT_DATA_CHANNELID);
        System.out.println("ClientData:           " + clientdata);

        // Generate the challenge parameter (hash of nonce)
        String chalparam = Common.getDigest(clientdata, "SHA-256");
        System.out.println("ChallengeParameter:   " + chalparam);

        // Get the appid - if the flag for goodsignature is flase, use a bad
        // appid to force a flaw in the signature; used in server testing
        String appid;
        if (goodsignature) {
            try {
                appid = Common.decodeAuthRequestJsonKeys(signrequest, Constants.JSON_KEY_APPID);
            } catch (NullPointerException ex) {
                appid = appidfromserver;
            }
        } else {
            appid = Constants.REGISTER_CLIENT_BAD_APPID;
        }

        // Generate the application parameter (hash of appid)
        String appparam = Common.getDigest(appid, "SHA-256");
        System.out.println("ApplicationParameter: " + appparam);

        // Recover the KeyHandle from the signature request
        String keyhandle = Common.decodeAuthRequestJsonKeys(signrequest, Constants.JSON_KEY_KEYHANDLE);
        System.out.println("CiphertextKeyHandle:  " + keyhandle);

        // Verify that KeyHandle is NOT greater than 255 bytes
        byte[] khbytes = Base64.getUrlDecoder().decode(keyhandle);
        int khlen = khbytes.length;
        if (khlen > 255) {
            System.err.println("Fatal Error: KeyHandle > 255");
            return null;
        }

        // Get the authentication signature-response from software authenticator
        String signresponse = Common.createAuthenticationSignatureResponse(chalparam, appparam, keyhandle, counter);
        System.out.println("Response from token:  " + signresponse);

        // Create and return the JSON response to "FIDO Client" application
        return Common.encodeAuthenticationSignatureResponse(
                clientdata,
                keyhandle,
                // Common.decodeAuthRequestJsonKeys(signrequest, Constants.JSON_KEY_SESSIONID),
                signresponse);
    }
}
