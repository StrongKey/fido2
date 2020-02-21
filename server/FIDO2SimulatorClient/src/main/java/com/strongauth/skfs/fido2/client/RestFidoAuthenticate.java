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
 * Creates a FIDO2 Authentication request on StrongKey's FIDO2 Server
 * using a REST web-service call
 */

package com.strongauth.skfs.fido2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.strongauth.skfs.fido2.simulator.FIDO2AuthenticatorSimulator;
import com.strongauth.skfs.requests.AuthenticationRequest;
import com.strongauth.skfs.requests.PreauthenticationRequest;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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

public class RestFidoAuthenticate {

    public static void authenticate(String REST_URI,
            String did,
            String accesskey,
            String secretkey,
            String fidoprotocol,
            String accountname,
            String origin,
            String auth_counter)
        throws Exception
    {
        System.out.println("Authentication test");
        System.out.println("*******************************");

        // Build payload
        String version = "2.0";
        PreauthenticationRequest preauth = new PreauthenticationRequest();
        preauth.setProtocol(fidoprotocol);
        preauth.setUsername(accountname);
        preauth.setOptions("{}");
        preauth.setExtensions("{}");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(preauth);

        ContentType mimetype = ContentType.APPLICATION_JSON;
        System.out.println("json = " + json);
        StringEntity body = new StringEntity(json, mimetype);

        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String contentSHA = Common.calculateSha256(json);

        // Setup the HTTPS request for the web-service endpoint
        String wsendpoint = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.PRE_AUTHENTICATE_ENDPOINT;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(wsendpoint);
        httpPost.setEntity(body);
        String requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        String hmac = Common.calculateHMAC(secretkey, requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        // Make REST call and get response from the server
        System.out.println("\nCalling preauthenticate at: " + wsendpoint);
        CloseableHttpResponse response = httpclient.execute(httpPost);
        String result;
        try {
            StatusLine responseStatusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            result = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            switch (responseStatusLine.getStatusCode()) {
                case 200:
                    break;
                case 401:
                    System.err.println("Error during preauthenticate : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.err.println("Error during preauthenticate : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.err.println("Error during preauthenticate : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println(" Response : " + result);
        JsonObject input = null;
        JsonParserFactory factory = Json.createParserFactory(null);

        StringReader s = new StringReader(result);
        JsonReader jsonReader = Json.createReader(s);
        JsonObject responseJSON = jsonReader.readObject();
        jsonReader.close();

        System.out.println("\n Authentication Parameters:\n");
        String challenge = responseJSON.getJsonObject("Response").toString();
        s = new StringReader(challenge);

        jsonReader = Json.createReader(s);
        JsonObject resJsonObj = jsonReader.readObject();
        jsonReader.close();

        // U2F request
        if ("U2F_V2".compareTo(fidoprotocol) == 0)
        {
            //  Build a json object out of response
            String appid;
            try {
                appid = resJsonObj.getString("appId");
            } catch (NullPointerException ex) {
                appid = null;
            }
            String nonce = resJsonObj.getString("challenge");

            System.out.println("\n Pre-Authentication Complete.");
            System.out.println("\n Generating Authentication response...\n");

            JsonArray jarray = resJsonObj.getJsonArray("registeredKeys");
            String s2 = jarray.getJsonObject(0).toString();
            s = new StringReader(s2);
            JsonParser parser = factory.createParser(s);
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
            System.out.println("appid: " + appid);
            System.out.println("nonce: " + nonce);
            System.out.println("s2: " + s2);
            System.out.println("origin: " + origin);
            System.out.println("auth_counter: " + auth_counter);

            try {
                input = FIDO2AuthenticatorSimulator.generateAuthenticationResponse(appid, nonce, s2, origin, Integer.parseInt(auth_counter), true);
            } catch (NoSuchAlgorithmException
                    | NoSuchProviderException
                    | DecoderException
                    | InvalidParameterSpecException
                    | UnsupportedEncodingException
                    | NoSuchPaddingException
                    | InvalidKeyException
                    | InvalidAlgorithmParameterException
                    | ShortBufferException
                    | IllegalBlockSizeException
                    | BadPaddingException
                    | InvalidKeySpecException ex) {
                System.out.println("\n Exception : " + ex.getLocalizedMessage());
            }
        }
        // FIDO2 request
        else if ("FIDO2_0".compareTo(fidoprotocol) == 0)
        {
            try {
                input = FIDO2AuthenticatorSimulator.generateFIDO2AuthenticationResponse(resJsonObj.getJsonArray("allowCredentials").getJsonObject(0).getString("id"), result, origin, "packed", Integer.parseInt(auth_counter), true);
                System.out.println("input = " + input);
            } catch (Exception ex) {
                Logger.getLogger(RestFidoAuthenticate.class.getName()).log(Level.SEVERE, null, ex);
                System.exit(0);
            }
        }

        StringReader regresreader = new StringReader(input.toString());
        JsonParser parser = factory.createParser(regresreader);
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

        System.out.println("\n Finished Generating Authentication Response.");
        System.out.println("\n Authenticating ...");

        // Build payload
        JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
                .add("version", "1.0") // ALWAYS since this is just the first revision of the code
                .add("last_used_location", "Bangalore, India")
                .add(Constants.JSON_KEY_SERVLET_INPUT_USERNAME, accountname)
                .add("origin", origin).
                build();

        ow = new ObjectMapper().writer();
        AuthenticationRequest auth = new AuthenticationRequest();
        auth.setProtocol(fidoprotocol);
        auth.setResponse(input.toString());
        auth.setMetadata(auth_metadata.toString());
        json = ow.writeValueAsString(auth);
        body = new StringEntity(json, mimetype);
        System.out.println("json = " + json);
        currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        contentSHA = Common.calculateSha256(json);

        // Build up HTTP request for webservice
        wsendpoint = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.AUTHENTICATE_ENDPOINT;
        httpclient = HttpClients.createDefault();
        httpPost = new HttpPost(wsendpoint);
        httpPost.setEntity(body);
        requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        hmac = Common.calculateHMAC(secretkey, requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        // Make API rest call and get response from the server
        System.out.println("\nCalling authenticate at: " + wsendpoint);
        response = httpclient.execute(httpPost);
        try {
            StatusLine responseStatusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String authresponse = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            switch (responseStatusLine.getStatusCode()) {
                case 200:
                    System.out.println(" Response   : " + authresponse);
                    break;
                case 401:
                    System.err.println("Error during authenticate : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.err.println("Error during authenticate : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.err.println("Error during authenticate : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println("\nAuthentication Complete.");
        System.out.println("*******************************");
    }
}
