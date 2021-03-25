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
 * Helper Utility class for the fido client
 */
package com.strongauth.fido.u2f.clientsimulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class clientUtil {

    public clientUtil() {
    }

    // returntype=0 sessionID
    // returntype=1 challenge
    // returntype=2 version
    // returntype=3 appid
    public String decodePreRegister(String Input, int returntype) {

        JsonReader jsonReader = Json.createReader(new StringReader(Input));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();

        if (returntype == 0) {
            return jsonObject.getString(CSConstants.JSON_KEY_SESSIONID);
        } else if (returntype == 1) {
            return jsonObject.getString(CSConstants.JSON_KEY_CHALLENGE);
        } else if (returntype == 2) {
            return jsonObject.getString(CSConstants.JSON_KEY_VERSION);
        } else {
            return jsonObject.getString(CSConstants.JSON_KEY_APP_ID);
        }

    }

    public KeyPair generatekeys() throws KeyStoreException, NoSuchProviderException, IOException, NoSuchAlgorithmException, CertificateException, InvalidAlgorithmParameterException, InvalidKeyException, SignatureException {

        //generate ECDSA keypair
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("ECDSA", "BCFIPS");
        ECGenParameterSpec paramSpec = new ECGenParameterSpec(("secp256r1"));

        //initialize
        kpg.initialize(paramSpec, new SecureRandom());
        //generate
        KeyPair keyPair = kpg.generateKeyPair();
        Key priK = (PrivateKey) keyPair.getPrivate();
        return keyPair;
    }

    public static String getDigest(String Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {

        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(Input.getBytes("UTF-8"));
        String dig = Base64.toBase64String(digestbytes);
        return dig;

    }

    public static String getDigestRawInput(byte[] Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {

        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(Input);
        String dig = Base64.toBase64String(digestbytes);
        return dig;

    }

    public static String clientDataEncoder(String optype, String challenge, String facetID, String cid) {
        JsonObject j;
        if (cid == null) {
            j = Json.createObjectBuilder().add(CSConstants.JSON_PROPERTY_REQUEST_TYPE, optype)
                    .add(CSConstants.JSON_PROPERTY_SERVER_CHALLENGE_BASE64, challenge)
                    .add(CSConstants.JSON_PROPERTY_SERVER_ORIGIN, facetID)
                    .build();
        } else {
            j = Json.createObjectBuilder().add(CSConstants.JSON_PROPERTY_REQUEST_TYPE, optype)
                    .add(CSConstants.JSON_PROPERTY_SERVER_CHALLENGE_BASE64, challenge)
                    .add(CSConstants.JSON_PROPERTY_SERVER_ORIGIN, facetID)
                    .add(CSConstants.JSON_PROPERTY_CHANNEL_ID, cid)
                    .build();
        }
        return j.toString();
    }

    public static String keyHandleEncode(String key, String origin, String sha1sumkey) {
        JsonObject j;

        j = Json.createObjectBuilder().add("key", key)
                .add("sha1", sha1sumkey)
                .add("origin_hash", origin)
                .build();
        return j.toString();

    }

    //returnType=0 key
    //returnType=1 sha1
    //returnType=2 origin
    public static String keyHandleDecode(String Input, int returnType) {

        JsonReader jsonReader = Json.createReader(new StringReader(Input));
        JsonObject jsonObject = jsonReader.readObject();
        jsonReader.close();
        //System.out.println("Last name : "+jsonObject.getString("swair"));
        if (returnType == 0) {
            return jsonObject.getString("key");

        } else if (returnType == 1) {

            return jsonObject.getString("sha1");

        } else {
            return jsonObject.getString("origin_hash");
        }

    }

    public static String makeKeyHandle(PrivateKey key, String originHash) throws NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, FileNotFoundException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidAlgorithmParameterException, ShortBufferException, InvalidKeySpecException, SignatureException {

        //Get wrapping key
        byte[] Seckeybytes = Hex.decode(CSConstants.SECURE_ELEMENT_SECRET_KEY);
        SecretKeySpec sks = new SecretKeySpec(Seckeybytes, "AES");

        //get key sha1sum
        String keyDigest = getDigestRawInput(key.getEncoded(), "SHA1");
        byte[] keydigestbytes = DatatypeConverter.parseBase64Binary(keyDigest);

        //print originHash
        byte[] originhashbytes = Base64.decode(originHash);

        String khunwrapped = keyHandleEncode(DatatypeConverter.printBase64Binary(key.getEncoded()), originHash, keyDigest);
        System.out.println("\t\t[-] Key handle : ");
        SoapFidoClient.printJson(khunwrapped);

        //wrap key data
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS7Padding", "BCFIPS");
        cipher.init(Cipher.ENCRYPT_MODE, sks, new SecureRandom());
        byte[] iv = cipher.getIV();

        byte[] wrapped = cipher.doFinal(khunwrapped.getBytes("UTF-8"));
        //System.out.println("Key handle without IV wrapped "+org.bouncycastle.util.encoders.Base64.toBase64String(wrapped));

        //append IV to the key handle
        byte[] keyHandlewithIV = new byte[wrapped.length + iv.length];
        //copy IV bytes in the key handle
        System.arraycopy(iv, 0, keyHandlewithIV, 0, iv.length);

        //copy wrapped keyhandle bytes in the key handle (with IV)
        System.arraycopy(wrapped, 0, keyHandlewithIV, iv.length, wrapped.length);

        //base64 encode keyhandlewith IV
        String keyHandleWithIV = Base64.toBase64String(keyHandlewithIV);

        //test key handle
        String keyHandleDecrypted = decryptKeyHandle(keyHandleWithIV);

        if (keyHandleDecrypted.trim().equals(khunwrapped.trim())) {
            return keyHandleWithIV;
        } else {
            return "exit";
        }

    }

    public static String decryptKeyHandle(String keyHandleWithIV) throws  NoSuchAlgorithmException, NoSuchProviderException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, ShortBufferException, IllegalBlockSizeException, BadPaddingException, UnsupportedEncodingException, InvalidKeySpecException, SignatureException {

        //get secure element key to decrypt
        byte[] Seckeybytes = Hex.decode(CSConstants.SECURE_ELEMENT_SECRET_KEY);
        SecretKeySpec sks = new SecretKeySpec(Seckeybytes, "AES");

        byte[] receivedkeyHandle = DatatypeConverter.parseBase64Binary(keyHandleWithIV);

        //get IV
        byte[] receivedIV = new byte[16];
        System.arraycopy(receivedkeyHandle, 0, receivedIV, 0, 16);

        //unwrap the key handle
        //get the wrapped key handle bytes
        byte[] wrappedKeyHandleBytes = new byte[receivedkeyHandle.length - receivedIV.length];
        System.arraycopy(receivedkeyHandle, receivedIV.length, wrappedKeyHandleBytes, 0, wrappedKeyHandleBytes.length);

        //unwrapping received key handle
        //decrypt
        Cipher cipher1 = Cipher.getInstance("AES/CBC/PKCS7Padding", "BCFIPS");
        IvParameterSpec ivspec = new IvParameterSpec(receivedIV);
        cipher1.init(Cipher.DECRYPT_MODE, sks, ivspec);

        byte[] receivedunwrappedKeyHandle = new byte[cipher1.getOutputSize(wrappedKeyHandleBytes.length)];
        int p = cipher1.update(wrappedKeyHandleBytes, 0, wrappedKeyHandleBytes.length, receivedunwrappedKeyHandle, 0);
        cipher1.doFinal(receivedunwrappedKeyHandle, p);

        //put decrypted key in a BCPrivate key object //to test
        String privateKey = keyHandleDecode(new String(receivedunwrappedKeyHandle, "UTF-8"), 0); //0 for key
        byte[] prk = Base64.decode(privateKey);

        //get private key into BC understandable form -- test working
        ECPrivateKeySpec ecpks = new ECPrivateKeySpec(new BigInteger(prk), null);
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BCFIPS");
        PrivateKey privatetest = kf.generatePrivate(ecpks);

        return new String(receivedunwrappedKeyHandle, "UTF-8");

    }

    public static String getRandom(int size) {

        if (size > CSConstants.MAX_RANDOM_NUMBER_SIZE_BITS / 8) {
            size = CSConstants.MAX_RANDOM_NUMBER_SIZE_BITS / 8;
        }
        //Generate seed
        SecureRandom random = new SecureRandom();
        byte seed[] = new byte[20];
        random.nextBytes(seed);

        //Generate Random number
        SecureRandom sr = new SecureRandom(seed);
        byte[] randomBytes = new byte[size];
        sr.nextBytes(randomBytes);

        //Hex encode and return
        return new String(Base64.toBase64String(randomBytes));

    }
    // getObjectToSign("0x00",ApplicationParam,ChallengeParam,kh,genKeys.getPublic());

    public static String getObjectToSign(String ApplicationParam, String ChallengeParam, String kh, String PublicKey) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        byte[] constant = {(byte) 0x00};
        int constantL = constant.length;
        byte[] Challenge = Base64.decode(ChallengeParam);
        int ChanllengeL = Challenge.length;
        byte[] Application = Base64.decode(ApplicationParam);
        int ApplicationL = Application.length;
        byte[] keyHandle = Base64.decode(kh);
        int keyHandleL = keyHandle.length;
        byte[] publicKey = Base64.decode(PublicKey);
        int publicKeyL = publicKey.length;
        /////////
        //Convert back to publicKey
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BCFIPS");
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publicKey);
        PublicKey pub = kf.generatePublic(pubKeySpec);

        int pukL = CSConstants.EC_P256_PUBLICKKEYSIZE;

        //Create byte[] Object to sign
        byte[] ob2Sign = new byte[constantL + ChanllengeL + ApplicationL + keyHandleL + pukL];
        //copy constant
        int tot = 0;
        System.arraycopy(constant, 0, ob2Sign, tot, constantL);
        tot += constantL;
        System.arraycopy(Application, 0, ob2Sign, tot, ApplicationL);
        tot += ApplicationL;
        System.arraycopy(Challenge, 0, ob2Sign, tot, ChanllengeL);
        tot += ChanllengeL;
        System.arraycopy(keyHandle, 0, ob2Sign, tot, keyHandleL);
        tot += keyHandleL;
        System.arraycopy(publicKey, 0, ob2Sign, tot, pukL);
        tot += pukL;
        return Base64.toBase64String(ob2Sign);
    }

    public String signObject(String input) throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException, UnrecoverableKeyException, NoSuchProviderException, InvalidKeyException, SignatureException {

        //Base64 decode input
        byte[] inputbytes = Base64.decode(input);

        KeyStore attks = KeyStore.getInstance("JCEKS");
        attks.load(getClass().getResourceAsStream(CSConstants.ATTESTATION_KEYSTORE), CSConstants.ATTESTATION_KEYSTORE_TOUCH_PASSWORD.toCharArray());

        //get Key
        PrivateKey prk = (PrivateKey) attks.getKey(CSConstants.ATTESTATION_PRIVATE_KEYALIAS, CSConstants.ATTESTATION_KEYSTORE_TOUCH_PASSWORD.toCharArray());

        //sign
        Signature sig = Signature.getInstance("SHA256withECDSA", "BCFIPS");
        sig.initSign(prk, new SecureRandom());
        sig.update(inputbytes);
        byte[] signedBytes = sig.sign();

        //verify locally
        //get certificate
        Certificate cert = attks.getCertificate(CSConstants.ATTESTATION_PRIVATE_KEYALIAS);
        PublicKey pkey = cert.getPublicKey();
        sig.initVerify(pkey);
        sig.update(inputbytes);
        if (sig.verify(signedBytes)) {
            return Base64.toBase64String(signedBytes);
        } else {
            return null;
        }

    }

    public String makeObject2Send(String userPublicKey, String keyHandle, String AttestationCertificate, String Signature) throws NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException {
        byte constant = 0x05;
        byte[] upk = Base64.decode(userPublicKey);
        int upkL = upk.length;
        byte[] kh = Base64.decode(keyHandle);
        int khL = kh.length;
        byte[] ac = Base64.decode(AttestationCertificate);
        int acL = ac.length;
        byte[] sig = Base64.decode(Signature);
        int sigL = sig.length;

        //Convert back to publicKey
        KeyFactory kf = KeyFactory.getInstance("ECDSA", "BCFIPS");
        X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(upk);
        PublicKey pub = kf.generatePublic(pubKeySpec);

        int pukL = CSConstants.EC_P256_PUBLICKKEYSIZE; //ECDSA secp256r1 publickey length
        byte[] obj2send = new byte[1 + pukL + 1 + khL + acL + sigL];
        int tot = 1;
        obj2send[0] = constant;
        System.arraycopy(upk, 0, obj2send, tot, pukL);
        tot += pukL;

        //KH length restriction
        if (khL >= 256) {
            System.out.println("Fatal error , Key handle length > = 256, one byte cannot hold that");
            return null;
        }
        byte keyHandleLength = (byte) khL;
        obj2send[tot] = keyHandleLength;
        ++tot;

        //KH
        System.arraycopy(kh, 0, obj2send, tot, khL);
        tot += khL;
        System.arraycopy(ac, 0, obj2send, tot, acL);
        tot += acL;
        System.arraycopy(sig, 0, obj2send, tot, sigL);
        tot += sigL;

        return Base64.toBase64String(obj2send);

    }

    public Certificate getAttestationCert() throws FileNotFoundException, KeyStoreException, IOException, NoSuchAlgorithmException, CertificateException {

        KeyStore attks = KeyStore.getInstance("JCEKS");
        attks.load(getClass().getResourceAsStream(CSConstants.ATTESTATION_KEYSTORE), null);

        Certificate cert = attks.getCertificate(CSConstants.ATTESTATION_PRIVATE_KEYALIAS);
        if (cert != null) {
            return cert;
        } else {
            return null;
        }
    }

    public String createRegistrationResponse(String ClientData, String SessionId, String RegistrationData) {

        JsonObject regResp = Json.createObjectBuilder()
                .add(CSConstants.JSON_KEY_BROWSERDATA, ClientData)
                .add(CSConstants.JSON_KEY_SESSIONID, SessionId)
                .add(CSConstants.JSON_KEY_REGISTRATIONDATA, RegistrationData)
                .build();
        return regResp.toString();

    }

}
