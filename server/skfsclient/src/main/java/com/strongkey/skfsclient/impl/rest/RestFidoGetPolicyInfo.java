/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfsclient.impl.rest;

import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.common;
import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
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

public class RestFidoGetPolicyInfo {

    public static void getPolicyInfo(String REST_URI,
                                String did,
                                String authtype,
                                String credential1,
                                String credential2,
                                String metadataonly,
                                String sid,
                                String pid) throws NoSuchAlgorithmException
    {
        String gkresponse = null;
        String version = "2.0";

        try {
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
            
            
             System.out.println("REST Get policy test with " + authtype);
                System.out.println("*******************************");

                
            
            JsonObject svcinfo;
            
            
             if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
                svcinfo = javax.json.Json.createObjectBuilder()
                .add("did", Long.valueOf(did))
                .add("protocol", "FIDO2_0") 
                .add("authtype", Constants.AUTHORIZATION_HMAC)
                .build();
            } else {
                svcinfo = javax.json.Json.createObjectBuilder()
                .add("did", Long.valueOf(did))
                .add("protocol", "FIDO2_0")      
                .add("authtype", Constants.AUTHORIZATION_PASSWORD)
                .add("svcusername", credential1)
                .add("svcpassword", credential2)
                .build();
            }
            
            String resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_GET_POLICY;
            
            JsonObject payload  = javax.json.Json.createObjectBuilder()
                .add("did", did)
                .add("sid", sid)
                .add("pid", pid)
                .add("metadataonly", metadataonly)
                .build();
            
            JsonObject data  = javax.json.Json.createObjectBuilder()
                .add("svcinfo", svcinfo)
                .add("payload", payload)
                .build();
            
            
            System.out.println("\nCalling getpolicyinfo @ " + resourceLoc);

            String contentType = "application/json";
            String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
            ContentType mimetype = ContentType.APPLICATION_JSON;
            StringEntity dataStringEntity = new StringEntity(data.toString(), mimetype);
            
            String payloadHash = common.calculateSha256(payload.toString());
            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(resourceLoc);
            httpPost.setEntity(dataStringEntity);
            String requestToHmac = httpPost.getMethod() + "\n"
                    + payloadHash + "\n"
                    + contentType + "\n"
                    + currentDate + "\n"
                    + version + "\n"
                    + httpPost.getURI().getPath();
            
            if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {  
                String hmac = common.calculateHMAC(credential2, requestToHmac);
                httpPost.addHeader("Authorization", "HMAC " + credential1 + ":" + hmac);
            }
            httpPost.addHeader("strongkey-content-sha256", payloadHash);
            httpPost.addHeader("Date", currentDate);
            httpPost.addHeader("Content-Type",contentType);
            httpPost.addHeader("strongkey-api-version", version);

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
                        System.out.println("Error during getpolicysinfo : 401 HMAC Authentication Failed");
                        return;
                    case 404:
                        System.out.println("Error during getpolicysinfo : 404 Resource not found");
                        return;
                    case 400:
                    case 500:
                    default:
                        System.out.println("Error during getpolicysinfo : " + responseStatusLine.getStatusCode() + " " + result);
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
                gkresponse = responseJSON.getJsonArray("Response").toString();
            } catch (Exception ex) {
            }

            System.out.println("\nGet policy information test complete.");
            System.out.println("******************************************");

        } catch (MalformedURLException ex) {
            Logger.getLogger(RestFidoGetPolicyInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(RestFidoGetPolicyInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RestFidoGetPolicyInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("Get policy response : " + gkresponse);
    }
}
