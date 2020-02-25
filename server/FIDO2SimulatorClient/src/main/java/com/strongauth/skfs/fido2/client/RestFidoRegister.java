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
 * Creates a FIDO2 Registration request on StrongKey's FIDO2 Server
 * using a REST web-service call
 */

package com.strongauth.skfs.fido2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.strongauth.skfs.fido2.simulator.FIDO2AuthenticatorSimulator;
import com.strongauth.skfs.requests.PreregistrationRequest;
import com.strongauth.skfs.requests.RegistrationRequest;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import org.apache.commons.codec.DecoderException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoRegister
{
    public static void register(String REST_URI,
                            String did,
                            String accesskey,
                            String secretkey,
                            String fidoprotocol,
                            String accountname,
                            String origin)
            throws Exception
    {
        System.out.println("Registration test");
        System.out.println("*******************************");

        String version = "2.0";

        PreregistrationRequest prereg = new PreregistrationRequest();
        prereg.setProtocol(fidoprotocol);
        prereg.setUsername(accountname);
        prereg.setDisplayname("Initial Registration");
        prereg.setOptions("{\"attestation\":\"direct\"}");
        prereg.setExtensions("{}");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(prereg);

        System.out.println("json = " + json);
        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String contentSHA = Common.calculateSha256(json);

        String resourceLoc = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.PRE_REGISTER_ENDPOINT;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);
        String requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        String hmac = Common.calculateHMAC(secretkey, requestToHmac);
        System.out.println("rth = " + requestToHmac);
        System.out.println("hmac = " + hmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        //  Make API rest call and get response from the server
        System.out.println("\nCalling preregister @ " + resourceLoc);
        CloseableHttpResponse response = httpclient.execute(httpPost);
        String result;
        try {
            StatusLine responseStatusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            switch (responseStatusLine.getStatusCode()) {
                case 200:
                    System.out.println(" Response : " + result);
                    break;
                case 401:
                    System.out.println("Error during pre-register : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during pre-register : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during pre-register : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        // Build a JSON object from response
        StringReader sr = new StringReader(result);
        JsonObject responseJSON;
        try (JsonReader jsonReader = Json.createReader(sr)) {
            responseJSON = jsonReader.readObject();
        }
        JsonObject resJsonObj = responseJSON.getJsonObject("Response");
        System.out.println("\n Pre-Registration Complete.\n Generating Registration response...\n");

        JsonObject input = null;
        JsonParserFactory factory = Json.createParserFactory(null);
        JsonParser parser;

        if ("U2F_V2".compareTo(fidoprotocol) == 0) {

            System.out.println("\n Registration Parameters:\n");
            JsonArray jarray = resJsonObj.getJsonArray("registerRequests");
            String s2 = jarray.getJsonObject(0).toString();
            sr = new StringReader(s2);
                parser = factory.createParser(sr);
            while (parser.hasNext()) {
                JsonParser.Event e = parser.next();
                switch (e) {
                    case KEY_NAME: {
                        System.out.print("\t" + parser.getString() + " = ");
                        break;
                    }
                    case VALUE_STRING: {
                        System.out.println(parser.getString());
                        break;
                    }
                }
            }
            String appidfromserver = resJsonObj.getString("appId");

            try {
                    input = FIDO2AuthenticatorSimulator.generateRegistrationResponse(appidfromserver, s2, origin, true);
                } catch (NoSuchAlgorithmException
                        | NoSuchProviderException
                        | KeyStoreException
                        | InvalidParameterSpecException
                        | DecoderException
                        | IOException
                        | CertificateException
                        | InvalidAlgorithmParameterException
                        | InvalidKeyException
                        | SignatureException
                        | NoSuchPaddingException
                        | IllegalBlockSizeException
                        | BadPaddingException
                        | ShortBufferException
                        | UnrecoverableKeyException
                        | InvalidKeySpecException ex) {
                System.err.println("\n Exception : " + ex.getLocalizedMessage());
            }

        } else if ("FIDO2_0".compareTo(fidoprotocol) == 0) {

//            JsonObject rpInfo = resJsonObj.getJsonObject("rp");
//            String rpName = rpInfo.getString("name");
//            String challenge = resJsonObj.getString("challenge");
//
//            Fido2TokenSim sim = new Fido2TokenSim(origin);
//            JsonObjectBuilder in = Json.createObjectBuilder();
//
//            JsonObjectBuilder rp = Json.createObjectBuilder();
//            rp.add(Constants.WebAuthn.RELYING_PARTY_NAME, rpName);
//            in.add(Constants.WebAuthn.RELYING_PARTY, rp);
//
//            JsonObjectBuilder user = Json.createObjectBuilder();
//            user.add(Constants.WebAuthn.USER_NAME,accountname);
//            user.add(Constants.WebAuthn.USER_ID,accountname);
//            in.add(Constants.WebAuthn.USER,user);
//
//            JsonArrayBuilder pubKeyParams = Json.createArrayBuilder();
//            JsonObjectBuilder alg1 = Json.createObjectBuilder();
//            alg1.add(Constants.WebAuthn.PUBKEYCREDPARAMS_ALG, -7);
//            alg1.add(Constants.WebAuthn.TYPE, "public-key");
//            pubKeyParams.add(alg1);
//
//            JsonObjectBuilder alg2 = Json.createObjectBuilder();
//            alg2.add(Constants.WebAuthn.PUBKEYCREDPARAMS_ALG, -257);
//            alg2.add(Constants.WebAuthn.TYPE, "public-key");
//            pubKeyParams.add(alg2);
//
//            in.add(Constants.WebAuthn.PUBKEYCREDPARAMS, pubKeyParams);
//
//            in.add(Constants.WebAuthn.CHALLENGE,challenge);
//            in.add(Constants.WebAuthn.ATTESTATION_PREFERENCE,"direct");
//
            /*
            {
                "rp": {
                    "name": "example.com"
                },
                "user": {
                    "name": "fidouser06110896",
                    "displayName": "fidouser06110896",
                    "id": "B16lQ8O1ZDTNX0NP0EP8dNRV6ShLlS4cbcWa72r2GyDfleYgFoJe7xZBIvST9PtZB_Jjx8als_XqggjeTQJyFw"
                },
                "challenge": "VciUZwhfiPCdElS0RygNEHAxxKtqUBkFN472KakrjsgqfFLKNm8wOkGYQFaqklFYrtNST1QLSOuaOO9r-428GH7LZ6qJ9NYkdH79jonCDptr5Pt4BfFmQDg0bTJXpc1dLAPRYsyrezVDtTWNw2FX3mibjvst9ThxfNe8deWdVsE",
                "attestation": "direct",
                "pubKeyCredParams": [{
                    "type": "public-key",
                    "alg": -7
                }, {
                    "type": "public-key",
                    "alg": -257public
                }]
            }
            */
//            input = sim.create(in.build());
            System.out.println("******************************");
            System.out.println(origin);
            System.out.println(resJsonObj.toString());
            System.out.println("******************************");
            input = FIDO2AuthenticatorSimulator.generateFIDO2RegistrationResponse(origin, resJsonObj.toString(), origin, "packed", "Basic", true);
            System.out.println("res : " + input.toString());
        }

        @SuppressWarnings("null")
        StringReader regresreader = new StringReader(input.toString());
        parser = factory.createParser(regresreader);
        while (parser.hasNext()) {
            JsonParser.Event e = parser.next();
            switch (e) {
                case KEY_NAME: {
                    System.out.print("\t" + parser.getString() + " = ");
                    break;
                }
                case VALUE_STRING: {
                    System.out.println(parser.getString());
                    break;
                }
            }
        }
        System.out.println("\n Finished Generating Registration Response.\n Registering ...");

        // Build payload
        JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
                .add("version", "1.0") // ALWAYS since this is just the first revision of the code
                .add("create_location", "Sunnyvale, CA")
                .add("origin", origin)
                .add(Constants.JSON_KEY_SERVLET_INPUT_USERNAME, accountname)
                .build();

        //  Make API rest call and get response from the server
        System.out.println("\nCalling register at: " + REST_URI + Constants.REGISTER_ENDPOINT);

        ow = new ObjectMapper().writer();
        RegistrationRequest reg = new RegistrationRequest();
        reg.setProtocol(fidoprotocol);
        reg.setResponse(input.toString());
        reg.setMetadata(reg_metadata.toString());
        json = ow.writeValueAsString(reg);

        System.out.println("json = " + json);
        body = new StringEntity(json, mimetype);

        currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        contentSHA = Common.calculateSha256(json);

        resourceLoc = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.REGISTER_ENDPOINT;

        httpclient = HttpClients.createDefault();
        httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);
        requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        hmac = Common.calculateHMAC(secretkey, requestToHmac);
        System.out.println("rth = " + requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        //  Make API rest call and get response from the server
        System.out.println("\nCalling register @ " + resourceLoc);
        response = httpclient.execute(httpPost);
        try {
            StatusLine responseStatusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String regresponse = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            switch (responseStatusLine.getStatusCode()) {
                case 200:
                    System.out.println(" Response   : " + regresponse);
                    break;
                case 401:
                    System.out.println("Error during register : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during register : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during register : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }
        System.out.println("\n Registration Complete.");
        System.out.println("*******************************");
    }
}
