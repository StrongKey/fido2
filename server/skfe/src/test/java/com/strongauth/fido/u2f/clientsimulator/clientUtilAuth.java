/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
 *
 */
package com.strongauth.fido.u2f.clientsimulator;

import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.bouncycastle.util.encoders.Base64;

/**
 *
 * @author smehta
 */
public class clientUtilAuth {

    //Constatns used suring authentication
    clientUtil cu = new clientUtil();

    // returnType = 0 keyhandle
    // returnType = 1 sessionid
    // returnType = 2 challenge
    // returnType = 3 version
    // returnType = 4 appid
    public String decodePreauth(String input, int returntype) {

        JsonReader jsonReader = Json.createReader(new StringReader(input));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        if (returntype == 0) {
            return jsonObject.getString(CSConstants.JSON_USER_KEY_HANDLE);
        } else if (returntype == 1) {
            return jsonObject.getString(CSConstants.JSON_KEY_SESSIONID);
        } else if (returntype == 2) {
            return jsonObject.getString(CSConstants.JSON_KEY_CHALLENGE);
        } else if (returntype == 3) {
            return jsonObject.getString(CSConstants.JSON_KEY_VERSION);
        } else {
            return jsonObject.getString(CSConstants.JSON_KEY_APP_ID);
        }

    }

    public String encodeAuth(String clientData, String challenge, String appid, String sessionID, String signaturebytes) {

        JsonObject authResp2s = Json.createObjectBuilder()
                .add(CSConstants.JSON_KEY_BROWSERDATA, clientData)
                .add(CSConstants.JSON_KEY_CHALLENGE, challenge)
                .add(CSConstants.JSON_KEY_APP_ID, appid)
                .add(CSConstants.JSON_KEY_SESSIONID, sessionID)
                .add(CSConstants.JSON_KEY_SIGNATURE, signaturebytes)
                .build();
        return authResp2s.toString();

    }

    public String makeAuthenticationRequest(byte controlbyte, String challengeparam, String applicationparam, String keyHandle) {
        byte[] chalparam = Base64.decode(challengeparam);
        int cpL = chalparam.length;
        byte[] appparam = Base64.decode(applicationparam);
        int apL = appparam.length;
        byte[] keyHandlebytes = Base64.decode(keyHandle);
        int khL = keyHandlebytes.length;

        if (khL > 255) {
            System.out.println("Fatal error Key handle > 255");
            return null;
        }
        byte khLb = (byte) khL;

        //makeAuthenticationRequest
        byte[] authreq = new byte[1 + cpL + apL + 1 + khL];

        int tot = 0;
        authreq[0] = controlbyte;
        tot += 1;
        System.arraycopy(chalparam, 0, authreq, tot, cpL);
        tot += cpL;
        System.arraycopy(appparam, 0, authreq, tot, apL);
        tot += apL;
        authreq[tot] = khLb;
        tot++;
        System.arraycopy(keyHandlebytes, 0, authreq, tot, khL);

        return Base64.toBase64String(authreq);

    }

    public String authenticatorProcess(String authRequest) throws  NoSuchAlgorithmException, NoSuchPaddingException, NoSuchProviderException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeySpecException, SignatureException {
        byte[] authreqbytes = Base64.decode(authRequest);
        byte controlbyte = authreqbytes[0];

        int tot = 1;
//get challenge param
        byte[] chalParam = new byte[CSConstants.CHALLENGE_PARAMETER_LENGTH];
        System.arraycopy(authreqbytes, tot, chalParam, 0, CSConstants.CHALLENGE_PARAMETER_LENGTH);
        tot += CSConstants.CHALLENGE_PARAMETER_LENGTH;

        //get Application Param
        byte[] appParam = new byte[CSConstants.APPLICATION_PARAMETER_LENGTH];
        System.arraycopy(authreqbytes, tot, appParam, 0, CSConstants.APPLICATION_PARAMETER_LENGTH);
        tot += CSConstants.APPLICATION_PARAMETER_LENGTH;

        //getkeyHandlelength
        byte keyHandlelength = authreqbytes[tot];
        tot++;
        int khL = (keyHandlelength & 0XFF);

        //getkeyhandle
        byte[] keyHandle = new byte[khL];
        System.arraycopy(authreqbytes, tot, keyHandle, 0, khL);
        tot += khL;

        //deryptkeyhandle
        String keyHandleJson = null;
        try {
            keyHandleJson = clientUtil.decryptKeyHandle(Base64.toBase64String(keyHandle));
        } catch (Exception ex) {
            System.out.println("Bad KeyHandle...");
            return null;
        }
        System.out.println("Key Handle Json : " + keyHandleJson);

        //get private key
        String privateKey = clientUtil.keyHandleDecode(keyHandleJson, 0);
        System.out.println("Private key  : " + privateKey);

        //make object to sign
        String object2sign = makeObjecttoSign(Base64.toBase64String(appParam), Base64.toBase64String(chalParam));
        System.out.println("Authenticator object to sign : " + object2sign);

        //sign object
        String signedObject = signObject(object2sign, privateKey);
        System.out.println("signed object : " + signedObject);

        //create authentication response message usf token -> fido client
        String authenticationResponse = makeauthenticationresponsemsg(signedObject);

        return authenticationResponse;
    }

    public String makeauthenticationresponsemsg(String signedObject) {

        byte[] signedObjectBytes = Base64.decode(signedObject);
        int soL = signedObjectBytes.length;
        byte[] authresp = new byte[1 + 4 + soL];
        int tot = 1;
        authresp[0] = CSConstants.AUTHENTICATOR_USERPRESENCE_BYTE;
        System.arraycopy(int2bytearray(CSConstants.AUTHENTICATOR_COUNTER_VALUE), 0, authresp, 1, 4);
        tot += 4;
        System.arraycopy(signedObjectBytes, 0, authresp, tot, soL);
        tot += soL;
        return Base64.toBase64String(authresp);

    }

    public String makeObjecttoSign(String applicationparam, String challengeParam) {
        byte[] appparam = Base64.decode(applicationparam);
        int apL = appparam.length;
        byte[] challparam = Base64.decode(challengeParam);
        int cpL = challparam.length;

        byte[] ob2sign = new byte[apL + 1 + 4 + cpL];
        int tot = 0;
        //appparam
        System.arraycopy(appparam, 0, ob2sign, 0, apL);
        tot += apL;
        ob2sign[tot] = CSConstants.AUTHENTICATOR_USERPRESENCE_BYTE;
        tot++;
        System.arraycopy(int2bytearray(CSConstants.AUTHENTICATOR_COUNTER_VALUE), 0, ob2sign, tot, 4);
        tot += 4;
        System.arraycopy(challparam, 0, ob2sign, tot, cpL);
        tot += cpL;

        return Base64.toBase64String(ob2sign);

    }

    public byte[] int2bytearray(int a) {
        if (a > 2147483647) {
            System.out.println("Counter wrap around reached...");
            a = 0;
        }
        return ByteBuffer.allocate(4).putInt(a).array();

    }

    public static String signObject(String input, String privateKeyS) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeyException, SignatureException, InvalidKeySpecException {

        ////put decrypted private key in a BCPrivate key object
        byte[] prk = Base64.decode(privateKeyS);

        //get private key into BC understandable form
        ECPrivateKeySpec ecpks = new ECPrivateKeySpec(new BigInteger(privateKeyS), null);
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BCFIPS");
        PrivateKey pvk = kf.generatePrivate(ecpks);

        //Base64 decode input
        byte[] inputbytes = Base64.decode(input);

        //sign
        Signature sig = Signature.getInstance("SHA256withECDSA", "BCFIPS");
        sig.initSign(pvk, new SecureRandom());
        sig.update(inputbytes);
        byte[] signedBytes = sig.sign();

//        //verify locally FIXME -- local verification is required // not sure how to get the public key
//        PublicKey pkey = userKeyPair.getPublic();
//        sig.initVerify(pkey);
//        sig.update(inputbytes);
//        if (sig.verify(signedBytes)) {
//            return Base64.encodeBase64String(signedBytes);
//        } else {
//            return null;
//        }
        return Base64.toBase64String(signedBytes);
    }
}
