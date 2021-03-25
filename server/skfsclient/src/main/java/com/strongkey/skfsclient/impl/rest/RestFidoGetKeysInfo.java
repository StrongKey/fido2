/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfsclient.impl.rest;

import com.strongkey.skfs.requests.GetKeysInfoRequest;
import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.common;
import java.io.StringReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoGetKeysInfo {

    public static void getKeysInfo(String REST_URI,
                                int did,
                                String authtype,
                                String credential1,
                                String credential2,
                                String username) throws Exception
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

        System.out.println("REST Get user keys information test with " + authtype);
        System.out.println("******************************************");

        String gkresponse = null;

        // Build request
        GetKeysInfoRequest getkeysinfo = new GetKeysInfoRequest();

        // Build request svcinfo
        getkeysinfo.getSVCInfo().setDid(did);
        getkeysinfo.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            getkeysinfo.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
        } else {
            getkeysinfo.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
            getkeysinfo.getSVCInfo().setSVCUsername(credential1);
            getkeysinfo.getSVCInfo().setSVCPassword(credential2);
        }

        // Build request payload
        getkeysinfo.setUsername(username);

        // Prepare for POST call
        String json = getkeysinfo.toJsonObject().toString();
        System.out.println(json);
        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        String resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_GETKEYSINFO_ENDPOINT;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);

        // Build HMAC and add headers
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            String payloadHash = common.calculateSha256(getkeysinfo.getPayload().toJsonObject().toString());
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
        System.out.println("\nCalling getkeysinfo @ " + resourceLoc);
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
                    System.out.println("Error during getkeysinfo : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during getkeysinfo : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during getkeysinfo : " + responseStatusLine.getStatusCode() + " " + result);
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
            gkresponse = responseJSON.getJsonObject("Response").toString();
        } catch (Exception ex) {
            //  continue since there is no error
        }

        System.out.println("\nGet user keys information test complete.");
        System.out.println("******************************************");

        System.out.println("GetKeys response : " + gkresponse);
    }
}
