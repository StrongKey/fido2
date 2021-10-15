/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfsclient.impl.rest;

import com.strongauth.skfs.fido2.simulator.FIDO2AuthenticatorSimulator;
import com.strongkey.skfs.requests.AuthenticationRequest;
import com.strongkey.skfs.requests.PreauthorizeRequest;
import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.common;
import com.strongkey.skfsclient.utilities.verifyAuthorization;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoAuthorize {

    public static void authorize(String REST_URI,
                                    int did,
                                    String authtype,
                                    String credential1,
                                    String credential2,
                                    String username,
                                    String txid,
                                    String txpayload,
                                    String origin,
                                    int auth_counter, 
                                    String verify,
                                    String crossOrigin) throws Exception
    {
        /*
        * authtype    -> |HMAC     |PASSWORD   |
        *                |---------|-----------|
        * credential1 -> |accesskey|svcusername|
        * credential2 -> |secretkey|svcpassword|
        */
        if (!authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC) && !authtype.equalsIgnoreCase(Constants.AUTHORIZATION_PASSWORD)) {
            System.out.println("Invalid Authentication Type...\n");
            return;
        }

        System.out.println("REST Authorization test with " + authtype);
        System.out.println("*******************************");

        // Build request
        PreauthorizeRequest preauth = new PreauthorizeRequest();

        // Build request svcinfo
        preauth.getSVCInfo().setDid(did);
        preauth.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
        if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            preauth.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
        } else {
            preauth.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
            preauth.getSVCInfo().setSVCUsername(credential1);
            preauth.getSVCInfo().setSVCPassword(credential2);
        }

        // Build request payload
        preauth.setUsername(username);
        preauth.setTxpayload(txpayload);
        preauth.setTxid(txid);
        preauth.setOptions(Constants.JSON_EMPTY_OPTIONS);

        // Prepare for POST call
        String json = preauth.toJsonObject().toString();
        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        System.out.println("preauthorizejson = ");
        System.out.println(json);
        String resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_PRE_AUTHORIZE_ENDPOINT;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);

        // Build HMAC and add headers
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            String payloadHash = common.calculateSha256(preauth.getPayload().toJsonObject().toString());
            String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
            String requestToHmac = httpPost.getMethod() + "\n"
                    + payloadHash + "\n"
                    + mimetype.getMimeType() + "\n"
                    + currentDate + "\n"
                    + Constants.API_VERSION + "\n"
                    + httpPost.getURI().getPath();
            String hmac = common.calculateHMAC(credential2, requestToHmac);
            httpPost.addHeader("Authorization", "HMAC " + credential1 + ":" + hmac);
            httpPost.addHeader("strongkey-content-sha256", payloadHash);
            httpPost.addHeader("Date", currentDate);
            httpPost.addHeader("strongkey-api-version", Constants.API_VERSION);
        }
        httpPost.addHeader("Content-Type", mimetype.getMimeType());

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
                    System.out.println(" Response : " + result);
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

//        System.exit(0);
        //  Build a json object out of response
        StringReader s = new StringReader(result);
        JsonReader jsonReader = Json.createReader(s);
        JsonObject responseJSON = jsonReader.readObject();
        jsonReader.close();
        JsonObject resJsonObj = responseJSON.getJsonObject("Response");

        System.out.println("\nPre-Authorize Complete.");

        System.out.println("\nGenerating Authorization response...\n");

        // Make call to simulator
//        System.out.println("******************************");
//        System.out.println(origin);
//        System.out.println(resJsonObj.toString());
//        System.out.println("******************************");
        Boolean co = Boolean.FALSE;
        if (crossOrigin.equalsIgnoreCase("true") || crossOrigin.equalsIgnoreCase("yes")) {
            co = Boolean.TRUE;
        }
        JsonObject input = FIDO2AuthenticatorSimulator.generateFIDO2AuthenticationResponse(resJsonObj.getJsonArray("allowCredentials").getJsonObject(0).getString("id"),
                resJsonObj.toString(), origin, "packed", auth_counter, true, co);
        System.out.println("Simulator Response : ");

        StringReader regresreader = new StringReader(input.toString());
        JsonParserFactory factory = Json.createParserFactory(null);
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
                    default:
                        break;
            }
        }

        System.out.println("\nFinished Generating Authorization Response.");
        System.out.println("\nAuthorizinging ...");

        //  Build request
        AuthenticationRequest auth = new AuthenticationRequest();

        // Build request svcinfo
        auth.getSVCInfo().setDid(did);
        auth.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
        if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            auth.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
        } else {
            auth.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
            auth.getSVCInfo().setSVCUsername(credential1);
            auth.getSVCInfo().setSVCPassword(credential2);
        }

        // Build request payload
        JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
                .add("version", "1.0") // ALWAYS since this is just the first revision of the code
                .add("last_used_location", "Sunnyvale, CA")
                .add(Constants.JSON_KEY_SERVLET_INPUT_USERNAME, username)
                .add("origin", origin)
                .build();
        JsonObjectBuilder auth_inner_response = javax.json.Json.createObjectBuilder()
                .add("authenticatorData", input.getJsonObject("response").getString("authenticatorData"))
                .add("signature", input.getJsonObject("response").getString("signature"))
                .add("userHandle", input.getJsonObject("response").getString("userHandle"))
                .add("clientDataJSON", input.getJsonObject("response").getString("clientDataJSON"));
        JsonObject auth_response = javax.json.Json.createObjectBuilder()
                .add("id", input.getString("id"))
                .add("rawId", input.getString("rawId"))
                .add("response", auth_inner_response) // inner response object
                .add("type", input.getString("type"))
                .build();
        auth.setMetadata(auth_metadata);
        auth.setResponse(auth_response);
        auth.setTxid(txid);
        auth.setTxpayload(txpayload);

        // Prepare for POST call
        json = auth.toJsonObject().toString();
        body = new StringEntity(json, mimetype);

        System.out.println("authjson = ");
        System.out.println(json);
        resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_AUTHOTIZE_ENDPOINT;

        httpclient = HttpClients.createDefault();
        httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);

        // Build HMAC and add headers
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            String payloadHash = common.calculateSha256(auth.getPayload().toJsonObject().toString());
            String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
            String requestToHmac = httpPost.getMethod() + "\n"
                    + payloadHash + "\n"
                    + mimetype.getMimeType() + "\n"
                    + currentDate + "\n"
                    + Constants.API_VERSION + "\n"
                    + httpPost.getURI().getPath();
            String hmac = common.calculateHMAC(credential2, requestToHmac);
            httpPost.addHeader("Authorization", "HMAC " + credential1 + ":" + hmac);
            httpPost.addHeader("strongkey-content-sha256", payloadHash);
            httpPost.addHeader("Date", currentDate);
            httpPost.addHeader("strongkey-api-version", Constants.API_VERSION);
        }
        httpPost.addHeader("Content-Type", mimetype.getMimeType());

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

            System.out.println("\nAuthorization Complete.");
            System.out.println("*******************************");
            if (verify.equalsIgnoreCase("true") || verify.equalsIgnoreCase("yes")) {
                System.out.println("\n\nVerifying authorization locally with the returned information");
                verifyAuthorization vA = new verifyAuthorization();
                System.out.println("Signature Verified = " + vA.verify(authresponse));
            }

        } finally {
            response.close();
        }

        
    }
}
