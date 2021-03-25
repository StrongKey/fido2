/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc.
 *
 * $Date: $
 * $Revision: $
 * $Author: $
 * $URL: $
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */
package com.strongkey.skfsclient.utilities;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

public class verifyAuthorization {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    private BouncyCastleFipsProvider BC_FIPS_PROVIDER = new BouncyCastleFipsProvider();

    public Boolean verify(String input) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {
        /*
        "txdetail": {
		"txid": "123123",
		"txpayload": "123132",
		"nonce": "o379ouzxr8jkuUir2dI89w",
		"txtime": 1615938867195,
		"challenge": "rqf2-IYUc6Rd0e9uKL-QHcaC295JdyBk6zoIMYLq7b4"
	}
        */
        System.out.println("Extracting txDetail");
        JsonObject txdetailJson = (JsonObject) getJsonValue(input, "txdetail", "JsonObject");
        
        String  txid = txdetailJson.getString("txid");
        String  txpayload = txdetailJson.getString("txpayload");
        String  nonce = txdetailJson.getString("nonce");
        Long  txtime = txdetailJson.getJsonNumber("txtime").longValue();
        String  challenge = txdetailJson.getString("challenge");
        
        String combinedChallenge = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(getDigestBytes((txtime + txid + txpayload + nonce).getBytes("UTF-8"),"SHA-256"));
        
        System.out.println("Verifying challange generation...");
        if(!combinedChallenge.equalsIgnoreCase(challenge)){
            System.out.println("Challenge in txdetail does not match");
            return Boolean.FALSE;
        }
        System.out.println("\t Success");
        
        /*
        "FIDOAuthenticatorReferences": [{
		"protocol": "FIDO2_0",
		"id": "JVfFNxwf6zK8WdLwJOrvDAZLdvYrryFpgJNFu-8zq75bPC7FSx47wIOyk4yDyEnQ0vlkWOKAMwYs15BW3xWJoDq0VkBIVWPHeUuRhDrqzclJ6nQJwW13M9RfdbGlgIo-aPK_Y4Wd0x6drSJIXSyJDzs7FdzFrj0PtpaanVA_1ie8qsACY5YKHgTjvp5yPxXkDu8z3nsGn6aQLmaAe5psaqJxyU3o8qofXVOCuV0HivI",
		"rawId": "JVfFNxwf6zK8WdLwJOrvDAZLdvYrryFpgJNFu-8zq75bPC7FSx47wIOyk4yDyEnQ0vlkWOKAMwYs15BW3xWJoDq0VkBIVWPHeUuRhDrqzclJ6nQJwW13M9RfdbGlgIo-aPK_Y4Wd0x6drSJIXSyJDzs7FdzFrj0PtpaanVA_1ie8qsACY5YKHgTjvp5yPxXkDu8z3nsGn6aQLmaAe5psaqJxyU3o8qofXVOCuV0HivI",
		"userHandle": "",
		"rpId": "strongkey.com",
		"authenticatorData": "WnTBrV2dI2nYtpWAzOrzVHMkwfEC46dxHD4U1RP9KKMBAAAASw",
		"clientDataJSON": "eyJ0eXBlIjoid2ViYXV0aG4uZ2V0IiwiY2hhbGxlbmdlIjoicnFmMi1JWVVjNlJkMGU5dUtMLVFIY2FDMjk1SmR5Qms2em9JTVlMcTdiNCIsIm9yaWdpbiI6Imh0dHBzOi8vZmlkb3Rlc3Quc3Ryb25na2V5LmNvbSJ9",
		"aaguid": "00000000-0000-0000-0000-000000000000",
		"authTime": 1615938868560,
		"uv": false,
		"up": true,
		"signerPublickey": "MIIBMzCB7AYHKoZIzj0CATCB4AIBATAsBgcqhkjOPQEBAiEA_____wAAAAEAAAAAAAAAAAAAAAD_______________8wRAQg_____wAAAAEAAAAAAAAAAAAAAAD_______________wEIFrGNdiqOpPns-u9VXaYhrxlHQawzFOw9jvOPD4n0mBLBEEEaxfR8uEsQkf4vOblY6RA8ncDfYEt6zOg9KE5RdiYwpZP40Li_hp_m47n60p8D54WK84zV2sxXs7LtkBoN79R9QIhAP____8AAAAA__________-85vqtpxeehPO5ysL8YyVRAgEBA0IABL-48eqENkSw13pfjbfWB1pvIdqQ7KC86e8oAIsToyDRdH_IoqNAe7OwG398UqotKwRuNjexnBJx36XrWJm5qUQ",
		"signature": "MEUCIAqH-UngQHWTUNLPoxlildYQasKfVw3mWvkExuGRXpAyAiEAu6JcZksVb3LcmptP6I3lWq5ki2wLBcKjB02CDyouEIc",
		"usedForThisTransaction": true,
		"signingKeyType": "ECDSA",
		"signingKeyAlgo": "SHA256withECDSA"
	}]
        */
        
        System.out.println("Extracting FIDOAuthenticatorReferences...");
        JsonArray FidoAuthReferencesArray = (JsonArray) getJsonValue(input, "FIDOAuthenticatorReferences", "JsonArray");
        JsonObject FidoAuthRef = FidoAuthReferencesArray.getJsonObject(0);
        String userpublickey = FidoAuthRef.getString("signerPublicKey");
        String signature = FidoAuthRef.getString("signature");
        String signingkeytype = FidoAuthRef.getString("signingKeyType");
        String signingkeyalgo = FidoAuthRef.getString("signingKeyAlgorithm");
        String browserdata = FidoAuthRef.getString("clientDataJSON");
        String browserdataJson = new String(java.util.Base64.getDecoder().decode(browserdata), "UTF-8");
        String authenticatorObject = FidoAuthRef.getString("authenticatorData");
        String bdchallenge = (String) getJsonValue(browserdataJson, "challenge", "String");
       
        System.out.println("Verifying challange inside clientData...");
        if(!combinedChallenge.equalsIgnoreCase(bdchallenge)){
            System.out.println("Challenge in clientData does not match");
            return Boolean.FALSE;
        }
        System.out.println("\t Success");
        
        System.out.println("Verifying Signature");
        try {
            byte[] publickeyBytes = java.util.Base64.getUrlDecoder().decode(userpublickey);
            Boolean isSignatureValid;
            KeyFactory kf = KeyFactory.getInstance(signingkeytype, "BCFIPS");
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publickeyBytes);
            PublicKey pub = kf.generatePublic(pubKeySpec);

            byte[] authData = java.util.Base64.getUrlDecoder().decode(authenticatorObject);
            byte[] encodedauthdata = authData;
            byte[] browserdatabytes = getDigestBytes(java.util.Base64.getDecoder().decode(browserdata), "SHA-256");
            byte[] signedBytes = new byte[encodedauthdata.length + browserdatabytes.length];
            System.arraycopy(encodedauthdata, 0, signedBytes, 0, encodedauthdata.length);
            System.arraycopy(browserdatabytes, 0, signedBytes, encodedauthdata.length, browserdatabytes.length);
            
            isSignatureValid = verifySignature(java.util.Base64.getUrlDecoder().decode(signature),
                    pub,
                    signedBytes,
                    signingkeyalgo);

            if (!isSignatureValid) {
                System.out.println("Signature invalid");
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        } catch (NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException ex) {
//            ex.printStackTrace();
            ex.printStackTrace();
            return Boolean.FALSE;
        }
    }

    public boolean verifySignature(byte[] signature, PublicKey publickey, byte[] signedobject, String algorithm){
        try {
            Signature sig = Signature.getInstance(algorithm, BC_FIPS_PROVIDER);
            sig.initVerify(publickey);
            sig.update(signedobject);
            return sig.verify(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            ex.printStackTrace();
        }
        return false;
    }
    
    public byte[] getDigestBytes(byte[] input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {
        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(input);
        return digestbytes;
    }
    
    public  Object getJsonValue(String jsonstr, String key, String datatype) {
        if (jsonstr == null || jsonstr.isEmpty()) {
            if (key == null || key.isEmpty()) {
                if (datatype == null || datatype.isEmpty()) {
                    return null;
                }
            }
        }

        try (JsonReader jsonreader = Json.createReader(new StringReader(jsonstr))) {
            JsonObject json = jsonreader.readObject();

            if (!json.containsKey(key)) {
                System.out.println( "'" + key + "' does not exist in the json");
                return null;
            }

            switch (datatype) {
                case "Boolean":
                    return json.getBoolean(key);
                case "Int":
                    return json.getInt(key);
                case "Long":
                    return json.getJsonNumber(key).longValueExact();
                case "JsonArray":
                    return json.getJsonArray(key);
                case "JsonNumber":
                    return json.getJsonNumber(key);
                case "JsonObject":
                    return json.getJsonObject(key);
                case "JsonString":
                    return json.getJsonString(key);
                case "String":
                    return json.getString(key);
                default:
                    return null;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
