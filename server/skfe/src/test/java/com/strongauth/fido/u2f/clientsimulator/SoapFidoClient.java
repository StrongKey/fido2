/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
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
 * This program implements a sample fido client and an authenticator_reg
 * interation. Currently the client can only call preRegister (enroll) and
 * register (bind) on the Fido Server.
 * All calls are implemented as SOAP calls.
 *
 */
package com.strongauth.fido.u2f.clientsimulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParser.Event;
import javax.json.stream.JsonParserFactory;
import org.bouncycastle.util.encoders.Base64;

public class SoapFidoClient {

    public static final String appidforbadsignature = "TESTBADSIGNATUREWITHINVALIDAPPID";

    public static String generateAuthenticationResponse2fs(String preAuthresponse, boolean goodSignature) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, InvalidKeySpecException, SignatureException {
        clientUtilAuth ca = new clientUtilAuth();

        //get challenge
        String challenge = ca.decodePreauth(preAuthresponse, 2);
        System.out.println("Challenge received :" + challenge);

        //generate clientdata from authentication request to be sent to the u2f token by the fido client
        //String Client_Data = clientUtil.clientDataEncoder(AUTHENTICATE_CLIENT_DATA_OPTYPE, challenge, decodePreauth(resp,4), AUTHENTICATE_CLIENT_DATA_CHANNELID); FIXME python server expects http://localhost:8081 insetead of +/app-identity
        String Client_Data = clientUtil.clientDataEncoder(CSConstants.AUTHENTICATE_CLIENT_DATA_OPTYPE, challenge, CSConstants.ORIGIN, CSConstants.AUTHENTICATE_CLIENT_DATA_CHANNELID);
        System.out.println("Client data : " + Client_Data);
        //generate challenge param
        String chalparam = clientUtil.getDigest(Client_Data, "SHA-256");
        System.out.println("Challenge parameter : " + chalparam);

        //get appid
        String appid = null;
        if (!goodSignature) {
            appid = appidforbadsignature;
            System.out.println("    ");

        } else {
            appid = ca.decodePreauth(preAuthresponse, 4);
        }
        //generate App param
        String appParam = clientUtil.getDigest(appid, "SHA-256");

        //show keyhandle
        String keyHandle = ca.decodePreauth(preAuthresponse, 0);
        System.out.println("KeyHandle : " + keyHandle);

        //calculate keyhandlelength
        byte[] rawkh = Base64.decode(keyHandle);
        int keyHandlelength = rawkh.length;
        if (keyHandlelength > 255) {
            System.out.println("Fatal error Key handle > 255");
            return null;
        }
        byte keyHandleLength = (byte) keyHandlelength;
        System.out.println("Key Handle Length : " + (keyHandleLength & 0xFF));

        String authenticationRequest = ca.makeAuthenticationRequest(CSConstants.CONTROL_BYTE, chalparam, appParam, keyHandle);

        //send authentication Request to authenticator_reg
        String authenticationResponse = ca.authenticatorProcess(authenticationRequest);

        if (authenticationResponse == null) {
            System.out.println("Fatal error...");

        }
        System.out.println("Authentication response from token : " + authenticationResponse);

        //prepare authenticationresponse for the server
        //{"bd": "", "challenge": "", "app_id": "", "sessionId": "", "sign": ""}
        String authresp2server = ca.encodeAuth(Base64.toBase64String(Client_Data.getBytes("UTF-8")), challenge, appid, ca.decodePreauth(preAuthresponse, 1), authenticationResponse);
        return authresp2server;

    }

    /**
     * Takes as input the preRegister JSON String that the preRegister call
     * results into and outputs what a Fido client should send back to the
     * register call.
     *
     * @param preregisterStr
     * @return String
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
     */
    public static String generateRegistrationResponse(String preregisterStr, boolean goodSignature) throws NoSuchAlgorithmException, NoSuchProviderException, 
            UnsupportedEncodingException, KeyStoreException, IOException, CertificateException, InvalidAlgorithmParameterException, InvalidKeyException, 
            SignatureException, NoSuchPaddingException, FileNotFoundException, IllegalBlockSizeException, BadPaddingException, ShortBufferException, UnrecoverableKeyException, InvalidKeySpecException {
        clientUtil cu = new clientUtil();
        //generate random username
        String UserName = Base64.toBase64String(new SecureRandom().generateSeed(20));
        //call enroll webservice
        String preRegisterReply = preregisterStr;
        System.out.println("Fido Client\n\tCalling preRegister ...\n\tService returned : ");
        printJson(preRegisterReply);

        //getChallenge
        String challenge = cu.decodePreRegister(preRegisterReply, 1);

        //create Client_Data
        System.out.println("\tCreating client data...");
        String Client_Data = cu.clientDataEncoder(CSConstants.REGISTER_CLIENT_DATA_OPTYPE, challenge, CSConstants.ORIGIN, CSConstants.REGISTER_CLIENT_DATA_CHANNELID);
        System.out.println("\tClientData : ");
        printJson(Client_Data);

        //hash Client data
        System.out.println("\tCreating ClientData Hash...");
        String CD_hash = cu.getDigest(Client_Data, "SHA-256");
        System.out.println("\tClientData Hash: " + CD_hash);

        //get App_ID
        String Apid = null;
        System.out.println("\tCreating Application ID Hash...");
        if (!goodSignature) {
            Apid = appidforbadsignature;
        } else {
            Apid = cu.decodePreRegister(preRegisterReply, 3);
        }

        //hash APP ID
        String APPID_hash = cu.getDigest(Apid, "SHA-256");
        System.out.println("\tApplication ID Hash : " + APPID_hash);

        //send this to the authenticator_reg
        System.out.println("\tCalling authenticator...");

        String reg_resp = authenticator_reg(CD_hash, APPID_hash);
        if (reg_resp == null) {
            System.out.println("Fatal error in main()");
            System.exit(1);
        }
        System.out.println("\nFido Client");
        //registration data object
        System.out.println("\tRegistration Data : " + reg_resp);

        System.out.println("\tCreating Registration Response...");
        //create Registration response
        String RegistrationResponse = cu.createRegistrationResponse(Base64.toBase64String(Client_Data.getBytes("UTF-8")), cu.decodePreRegister(preRegisterReply, 0), reg_resp);

        System.out.println("\tRegistration Parameters : ");
        printJson(RegistrationResponse);

        return RegistrationResponse;
    }

    public static void printJson(String Input) {
        JsonParserFactory factory = Json.createParserFactory(null);
        JsonParser parser = factory.createParser(new StringReader(Input));

        System.out.println("\t{");
        while (parser.hasNext()) {
            Event event = parser.next();

            switch (event) {
                case KEY_NAME: {
                    System.out.print("\t\"" + parser.getString() + "\" : \"");
                    break;
                }
                case VALUE_STRING: {
                    System.out.println(parser.getString() + "\",");
                    break;
                }
            }
        }
        System.out.println("\t}");

    }

    private static String authenticator_reg(String ChallengeParam, String ApplicationParam) throws KeyStoreException, NoSuchProviderException, 
            IOException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException, InvalidKeyException, SignatureException, 
            NoSuchPaddingException, FileNotFoundException, IllegalBlockSizeException, BadPaddingException, 
            UnsupportedEncodingException, ShortBufferException, UnrecoverableKeyException, InvalidKeySpecException {

        System.out.println("\n\tFido Authenticator");

        clientUtil cu = new clientUtil();

        //generate keys
        System.out.println("\t\tGenerating ECDSA Keys...");
        KeyPair genKeys = cu.generatekeys();
        System.out.println("\t\tPublic  : " + genKeys.getPublic());
        System.out.println("\t\tPrivate : " + genKeys.getPrivate());

        //make key handle
        System.out.println("\t\tCreating KeyHandle...");
        String originHash = cu.getDigest(CSConstants.ORIGIN, "SHA-256");

        String kh = cu.makeKeyHandle(genKeys.getPrivate(), originHash);
        System.out.println("\t\tKey Handle wrapped :  " + kh);

        //make object to sign
        System.out.println("\t\tCreating byte string to sign...");
        String ob2sign = cu.getObjectToSign(ApplicationParam, ChallengeParam, kh, Base64.toBase64String(genKeys.getPublic().getEncoded()));
        System.out.println("\t\tByte String to sign : " + ob2sign);

        //sign object
        System.out.println("\t\tSigning...");
        String signatureb64 = cu.signObject(ob2sign);
        System.out.println("\t\tSignature : " + signatureb64);

        //make object to send
        //get attestation certificate
        Certificate cert = cu.getAttestationCert();
        if (cert == null) {
            System.out.println("Fatal error in creating the object to send, attestation certificate not found");
            return null;
        }
        String registrationData = cu.makeObject2Send(Base64.toBase64String(genKeys.getPublic().getEncoded()), kh, Base64.toBase64String(cert.getEncoded()), signatureb64);
        if (registrationData == null) //KH was too large
        {
            return null;
        }

        return registrationData;
    }

}
