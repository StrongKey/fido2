/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfsclient.impl.rest;

import com.strongauth.skfs.fido2.simulator.FIDO2AuthenticatorSimulator;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.requests.RegistrationRequest;
import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.common;
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

public class RestFidoRegister {

    public static void register(String REST_URI,
                                int did,
                                String authtype,
                                String credential1,
                                String credential2,
                                String username,
                                String origin,
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

        System.out.println("REST Registration test with " + authtype);
        System.out.println("*******************************");

        // Build request
        PreregistrationRequest prereg = new PreregistrationRequest();

        // Build request svcinfo
        prereg.getSVCInfo().setDid(did);
        prereg.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
        if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            prereg.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
        } else {
            prereg.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
            prereg.getSVCInfo().setSVCUsername(credential1);
            prereg.getSVCInfo().setSVCPassword(credential2);
        }
        
        // Build request payload
        prereg.setUsername(username);
        prereg.setDisplayName(username);
//        prereg.setOptions(Constants.JSON_ATTESTATION_DIRECT);
        prereg.setOptions(Json.createObjectBuilder().build());
        prereg.setExtensions(Constants.JSON_EMPTY);

        // Prepare for POST call
        String json = prereg.toJsonObject().toString();
        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        System.out.println("preregjson = ");
        System.out.println(json);
        String resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_PRE_REGISTER_ENDPOINT;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);

        // Build HMAC and add headers
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            String payloadHash = common.calculateSha256(prereg.getPayload().toJsonObject().toString());
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

        //  Build a json object out of response
        StringReader s = new StringReader(result);
        JsonReader jsonReader = Json.createReader(s);
        JsonObject responseJSON = jsonReader.readObject();
        jsonReader.close();
        JsonObject resJsonObj = responseJSON.getJsonObject("Response");

        System.out.println("\nPre-Registration Complete.");

        System.out.println("\nGenerating Registration response...\n");

        // Make call to simulator
//        System.out.println("******************************");
//        System.out.println(origin);
//        System.out.println(resJsonObj.toString());
//        System.out.println("******************************");
        Boolean co = Boolean.FALSE;
        if(crossOrigin.equalsIgnoreCase("true") || crossOrigin.equalsIgnoreCase("yes")){
            co = Boolean.TRUE;
        }
        JsonObject input = FIDO2AuthenticatorSimulator.generateFIDO2RegistrationResponse(origin, resJsonObj.toString(), origin, "packed", "Basic", true, co);
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

        System.out.println("\nFinished Generating Registration Response.");
        System.out.println("\nRegistering ...");

        //  Build request
        RegistrationRequest reg = new RegistrationRequest();

        //  Build request svcinfo
        reg.getSVCInfo().setDid(did);
        reg.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
        if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            reg.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
        } else {
            reg.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
            reg.getSVCInfo().setSVCUsername(credential1);
            reg.getSVCInfo().setSVCPassword(credential2);
        }

        //  Build request payload
        JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
                .add("version", "1.0") // ALWAYS since this is just the first revision of the code
                .add("create_location", "Sunnyvale, CA")
                .add(Constants.JSON_KEY_SERVLET_INPUT_USERNAME, username)
                .add("origin", origin)
                .build();
        JsonObjectBuilder reg_inner_response = javax.json.Json.createObjectBuilder()
                .add("attestationObject", input.getJsonObject("response").getString("attestationObject"))
                .add("clientDataJSON", input.getJsonObject("response").getString("clientDataJSON"));
        JsonObject reg_response = javax.json.Json.createObjectBuilder()
                .add("id", input.getString("id"))
                .add("rawId", input.getString("rawId"))
                .add("response", reg_inner_response) // inner response object
                .add("type", input.getString("type"))
                .build();
        reg.setMetadata(reg_metadata);
        reg.setResponse(reg_response);

        // Prepare for POST call
        json = reg.toJsonObject().toString();
        body = new StringEntity(json, mimetype);

        System.out.println("regjson = ");
        System.out.println(json);
        
        resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_REGISTER_ENDPOINT;

        httpclient = HttpClients.createDefault();
        httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);

        // Build HMAC and add headers
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            System.out.println("payload = "+ reg.getPayload().toJsonObject().toString());
            String payloadHash = common.calculateSha256(reg.getPayload().toJsonObject().toString());
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
                    System.out.println("Error during register : " + responseStatusLine.getStatusCode() + " " + regresponse);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println("\n Registration Complete.");
        System.out.println("*******************************");
    }
}
