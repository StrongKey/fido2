/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.core;

import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.SKFEException;
import java.io.Serializable;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;

/**
 * Derived class for U2F Authentication response that comes back to the FIDO
 * server from the RP application.
 */
public class U2FAuthenticationResponse extends U2FResponse implements Serializable {

    /**
     * This class' name - used for logging
     */
    private String classname = this.getClass().getName();

    private String challenge;
    private String appid;
    private String signdata;

    //Class internal use
    private final String userpublickeybytes;

    private int counter;
    private int usertouch;

    /**
     * The constructor of this class takes the U2F authentication response
     * parameters in the form of stringified Json. The method parses the Json to
     * extract needed fileds compliant with the u2fversion specified.
     *
     * @param u2fversion - Version of the U2F protocol being communicated in;
     * example : "U2F_V2"
     * @param authresponseJson - U2F Auth Response params in stringified Json
     * form
     * @param userPublicKeyBytes - User public key in bytes form
     * @param challenge
     * @param appid
     * @throws SKFEException - In case of any error
     */
    public U2FAuthenticationResponse(String u2fversion, String authresponseJson, String userPublicKeyBytes, String challenge, String appid) throws SKFEException {

        //  Input checks
        if (u2fversion == null || u2fversion.trim().isEmpty()) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FAuthenticationResponse", skfsCommon.getMessageProperty("FIDO-ERR-5001"), " u2f version");
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5001") + " username");
        }

        if (authresponseJson == null || authresponseJson.trim().isEmpty()) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FAuthenticationResponse", skfsCommon.getMessageProperty("FIDO-ERR-5001"), " authresponseJson");
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5001") + " authresponseJson");
        }

        //  u2f protocol version specific processing.
        if (u2fversion.equalsIgnoreCase(U2F_VERSION_V2)) {

            try {
                //  Parse the reg response json string
                JsonReader jsonReader = Json.createReader(new StringReader(authresponseJson));
                JsonObject jsonObject = jsonReader.readObject();
                jsonReader.close();

                this.browserdata = jsonObject.getString(skfsConstants.JSON_KEY_CLIENTDATA);
                this.signdata = jsonObject.getString(skfsConstants.JSON_KEY_SIGNATUREDATA);
                this.challenge = challenge;
                this.appid = appid;
            } catch (Exception ex) {
                ex.printStackTrace();
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FAuthenticationResponse", skfsCommon.getMessageProperty("FIDO-ERR-5011"),
                        ex.getLocalizedMessage());
                throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5011") + ex.getLocalizedMessage());
            }

            //  Generate new browser data
            bd = new BrowserData(this.browserdata, BrowserData.AUTHENTICATION_RESPONSE);

            //  Make sure challenge from BrowserData is the same as the challenge
            //  in the Authresp
            System.out.println("BDCHALLENGE - CHALLENGE : "+ bd.getChallenge() + " - " + this.challenge);
            if (!bd.getChallenge().equals(this.challenge)) {
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FAuthenticationResponse", skfsCommon.getMessageProperty("FIDO-ERR-5012"), "");
                throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5012"));
            }

            this.userpublickeybytes = userPublicKeyBytes;

        } else {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FAuthenticationResponse", skfsCommon.getMessageProperty("FIDO-ERR-5002"), " version passed=" + u2fversion);
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5002") + " version passed=" + u2fversion);
        }
    }

    /**
     * Get methods to access the response parameters
     *
     * @return
     */
    public int getCounter() {
        return counter;
    }

    public int getUsertouch() {
        return usertouch;
    }

    public String getChallenge() {
        return challenge;
    }

    /**
     * Converts this POJO into a JsonObject and returns the same.
     *
     * @return JsonObject
     */
    public final JsonObject toJsonObject() {
        JsonObject jsonObj = Json.createObjectBuilder()
                .add(skfsConstants.JSON_USER_COUNTER_SERVLET, this.counter)
                .add(skfsConstants.JSON_USER_PRESENCE_SERVLET, this.usertouch)
                .build();

        return jsonObj;
    }

    /**
     * Converts this POJO into a JsonObject and returns the String form of it.
     *
     * @return String containing the Json representation of this POJO.
     */
    public final String toJsonString() {
        return toJsonObject().toString();
    }

    /**
     * Once this class object is successfully constructed, calling verify method
     * will actually process the authentication response params.
     *
     * The first step in verification is sessionid validation, which if found
     * valid goes ahead and processes the authentication data.
     *
     * @return
     * @throws SKFEException - In case of any error
     */
    public final boolean verify() throws SKFEException {
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "verify", skfsCommon.getMessageProperty("FIDO-MSG-5011"), "");

        try {
            return processAuthenticationData(this.signdata, this.browserdata, this.userpublickeybytes, appid);
        } catch (SKFEException ex) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "verify", skfsCommon.getMessageProperty("FIDO-ERR-5006"), ex.getLocalizedMessage());
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-5006") + ex.getLocalizedMessage());
        }
    }

    /**
     * Processes the authentication data
     *
     * @param signData
     * @param browserData
     * @param userPublicKeyB64
     * @param appid
     * @return
     * @throws FidoEngineException - In case of any error
     */
    private boolean processAuthenticationData(String signData,
            String browserData,
            String userPublicKeyB64,
            String appid) throws SKFEException {
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData", skfsCommon.getMessageProperty("FIDO-MSG-5013"), "");

        try {
            byte[] signDataBytes = Base64.decodeBase64(signData);
            int sdL = signDataBytes.length;

            //  userpresense byte
            this.usertouch = (int) signDataBytes[0];
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData",
                    skfsCommon.getMessageProperty("FIDO-MSG-5026"), this.usertouch);

            //  counter
            int tot = 1;
            byte[] counterValue = new byte[skfsConstants.COUNTER_VALUE_BYTES];
            System.arraycopy(signDataBytes, tot, counterValue, 0, skfsConstants.COUNTER_VALUE_BYTES);
            tot += skfsConstants.COUNTER_VALUE_BYTES;
            this.counter = Integer.parseInt(Hex.encodeHexString(counterValue), 16);
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData",
                    skfsCommon.getMessageProperty("FIDO-MSG-5027"), this.counter);

            //  signaturebytes
            byte[] signatureBytes = new byte[sdL - 1 - skfsConstants.COUNTER_VALUE_BYTES];
            System.arraycopy(signDataBytes, tot, signatureBytes, 0, signatureBytes.length);
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData",
                    skfsCommon.getMessageProperty("FIDO-MSG-5019"),
                    Hex.encodeHexString(signatureBytes));

            //  create the object that has been signed
            //  get challenge parameter
            String bdhash = skfsCommon.getDigest(new String(Base64.decodeBase64(browserData), "UTF-8"), "SHA-256");
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData",
                    skfsCommon.getMessageProperty("FIDO-MSG-5022"),
                    Hex.encodeHexString(Base64.decodeBase64(bdhash)));

            String appIDHash = skfsCommon.getDigest(appid, "SHA-256");
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData",
                    skfsCommon.getMessageProperty("FIDO-MSG-5021"),
                    Hex.encodeHexString(Base64.decodeBase64(appIDHash)));

            String objectSigned = objectTBS(appIDHash, signDataBytes[0], this.counter, bdhash);
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData",
                    skfsCommon.getMessageProperty("FIDO-MSG-5023"),
                    Hex.encodeHexString(Base64.decodeBase64(objectSigned)));

            //  verify signature; return counter received and userpresence or null on error
            //  convert publickey[] to PublicKey
            byte[] publickeyBytes = Base64.decodeBase64(userPublicKeyB64);
            KeyFactory kf = KeyFactory.getInstance("ECDSA", "BCFIPS");
            X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publickeyBytes);
            PublicKey pub = kf.generatePublic(pubKeySpec);
            if (cryptoCommon.verifySignature(signatureBytes, pub, objectSigned)) {
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "processAuthenticationData",
                        skfsCommon.getMessageProperty("FIDO-MSG-5024"), "");
                return true;
            } else {
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "processAuthenticationData",
                        skfsCommon.getMessageProperty("FIDO-ERR-5005"), "");
                return false;
            }
        } catch (NumberFormatException | UnsupportedEncodingException | InvalidKeySpecException |
                NoSuchAlgorithmException | NoSuchProviderException ex) {

            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "processAuthenticationData",
                    skfsCommon.getMessageProperty("FIDO-ERR-5006"), ex.getLocalizedMessage());
            throw new SKFEException(ex);
        }
    }

    /**
     *
     * @param appParam
     * @param userpresence
     * @param counterValue
     * @param challParam
     * @return
     */
    public static String objectTBS(String appParam, byte userpresence, int counterValue, String challParam) {

        byte[] appparam = Base64.decodeBase64(appParam);
        int apL = appparam.length;
        byte[] challparam = Base64.decodeBase64(challParam);
        int cpL = challparam.length;

        byte[] ob2sign = new byte[apL + 1 + skfsConstants.COUNTER_VALUE_BYTES + cpL];
        int tot = 0;

        System.arraycopy(appparam, 0, ob2sign, 0, apL);
        tot += apL;
        ob2sign[tot] = userpresence;
        tot++;
        System.arraycopy(int2bytearray(counterValue), 0, ob2sign, tot, 4);
        tot += 4;
        System.arraycopy(challparam, 0, ob2sign, tot, cpL);
        tot += cpL;

        return Base64.encodeBase64String(ob2sign);
    }

    /**
     *
     * @param a
     * @return
     */
    public static byte[] int2bytearray(int a) {
        if (a >= Integer.MAX_VALUE) {
            System.out.println("Counter wrap around reached...");
            a = 0;
        }

        return ByteBuffer.allocate(4).putInt(a).array();
    }
}
