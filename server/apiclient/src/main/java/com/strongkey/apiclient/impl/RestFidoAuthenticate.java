/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.apiclient.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.strongkey.apiclient.common.Constants;
import com.strongkey.apiclient.common.common;
import com.strongauth.skfe.fido2.Fido2TokenSim;
import com.strongkey.skfs.requests.AuthenticationRequest;
import com.strongkey.skfs.requests.PreauthenticationRequest;
import com.strongauth.skfe.tokensim.FIDOU2FTokenSimulator;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
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
import javax.json.JsonObjectBuilder;
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
            String auth_counter) throws Exception
    {
        System.out.println("Authentication test");
        System.out.println("*******************************");

        String version = "2.0";
        // Build payload

        PreauthenticationRequest preauth = new PreauthenticationRequest();
        preauth.setProtocol(fidoprotocol);
        preauth.setUsername(accountname);
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(preauth);

        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String contentSHA = common.calculateSha256(json);

        String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.PRE_AUTH_ENDPOINT;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);
        String requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        String hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        //  Make API rest call and get response from the server
        System.out.println("\nCalling preauthorize @ " + resourceLoc);
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
                    System.out.println("Error during pre-authorize : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during pre-authorize : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during pre-authorize : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println(" Response : " + result);

        //  Build a json object out of response
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
        
        String appid ;
        try{
            appid = resJsonObj.getString("appId");
        } catch (NullPointerException ex) {
            appid = null;
        }
        String nonce = resJsonObj.getString("challenge");

        System.out.println("\n Pre-Authentication Complete.");
        System.out.println("\n Generating Authentication response...\n");
        JsonObject input = null;
        JsonParserFactory factory = Json.createParserFactory(null);

        if ("U2F_V2".compareTo(fidoprotocol) == 0) {

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
                    input = FIDOU2FTokenSimulator.generateAuthenticationResponse(appid, nonce, s2, origin, Integer.parseInt(auth_counter), true);
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
                        | InvalidKeySpecException
                        | SignatureException ex) {
                System.out.println("\n Exception : " + ex.getLocalizedMessage());
            } 
        } else if ("FIDO20".compareTo(fidoprotocol) == 0) {
            Fido2TokenSim sim = new Fido2TokenSim(origin);
            JsonObjectBuilder in = Json.createObjectBuilder();

            in.add(Constants.WebAuthn.RELYING_PARTY_RPID, "");
            in.add(Constants.WebAuthn.CHALLENGE,nonce);
            
            /*
                {
                    "challenge": "asdfasdfasdfasdf",
                    "rpId": "example.com"
                }
            */
            input = sim.get(in.build());
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
        
        //  Build payload
        JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
                .add("version", "1.0") // ALWAYS since this is just the first revision of the code
                .add("last_used_location", "Bangalore, India")
                .add(Constants.JSON_KEY_SERVLET_INPUT_USERNAME, accountname).
                build();

        ow = new ObjectMapper().writer();
        AuthenticationRequest auth = new AuthenticationRequest();
        auth.setProtocol(fidoprotocol);
        auth.setResponse(input.toString());
        auth.setMetadata(auth_metadata.toString());
        json = ow.writeValueAsString(auth);
        body = new StringEntity(json, mimetype);

        currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        contentSHA = common.calculateSha256(json);

        resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.AUTHENTICATE_ENDPOINT;

        httpclient = HttpClients.createDefault();
        httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);
        requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        //  Make API rest call and get response from the server
        System.out.println("\nCalling authorize @ " + resourceLoc);
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
                    System.out.println("Error during authorize : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during authorize : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during authorize : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println("\nAuthentication Complete.");
        System.out.println("*******************************");
    }
}
