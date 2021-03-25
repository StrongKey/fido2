/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfsclient.impl.rest;

import com.strongkey.skfs.requests.GetConfigurationRequest;
import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.common;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonWriter;
import javax.json.JsonWriterFactory;
import javax.json.stream.JsonGenerator;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoGetConfiguration {

    public static void getConfiguration(String REST_URI,
            int did,
            String authtype,
            String credential1,
            String credential2) throws Exception {
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

        System.out.println("REST Get configuration test with " + authtype);
        System.out.println("******************************************");

        String gcresponse = null;

        // Build request
        GetConfigurationRequest getconfiguration = new GetConfigurationRequest();

        // Build request svcinfo
        getconfiguration.getSVCInfo().setDid(did);
        getconfiguration.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            getconfiguration.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
        } else {
            getconfiguration.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
            getconfiguration.getSVCInfo().setSVCUsername(credential1);
            getconfiguration.getSVCInfo().setSVCPassword(credential2);
        }

        // Prepare for POST call
        String json = getconfiguration.toJsonObject().toString();
        System.out.println(json);
        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        String resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_GET_CONFIGURATION_ENDPOINT;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);

        // Build HMAC and add headers
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            String payloadHash = "";
            String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
            String requestToHmac = httpPost.getMethod() + "\n"
                    + payloadHash + "\n"
                    + "" + "\n"
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
        System.out.println("\nCalling getconfiguration @ " + resourceLoc);
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
                    System.out.println("Error during getconfiguration : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during getconfiguration : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during getconfiguration : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }

        } finally {
            response.close();
        }

        //  Build a json object out of response
        StringReader s = new StringReader(result);
        JsonObject responseJSON;
        try (JsonReader jsonReader = Json.createReader(s)) {
            responseJSON = jsonReader.readObject();
        }

        //  Check to see if there is any
        try {
            gcresponse = responseJSON.getJsonObject("Response").toString();
        } catch (Exception ex) {
            //  continue since there is no error
        }

        System.out.println("\nGet configuration test complete.");
        System.out.println("******************************************");

        System.out.println("GetConfiguration response : " + prettyPrint(gcresponse));
    }

    private static String prettyPrint(String json) {
        StringWriter sw = new StringWriter();

//        try {
        JsonReader jr = Json.createReader(new StringReader(json));

        JsonObject jobj = jr.readObject();

        Map<String, Object> properties = new HashMap<>(1);
        properties.put(JsonGenerator.PRETTY_PRINTING, true);

        JsonWriterFactory writerFactory = Json.createWriterFactory(properties);
        JsonWriter jsonWriter = writerFactory.createWriter(sw);

        jsonWriter.writeObject(jobj);
        jsonWriter.close();
//        } catch (Exception e) {
//        }

        String prettyPrinted = sw.toString();

        return prettyPrinted;
    }
}
