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
import com.strongkey.skfs.requests.PatchFidoKeyRequest;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpPatch;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoActionsOnKey {

    public static void deregister(String REST_URI, 
                                String did, 
                                String accesskey, 
                                String secretkey, 
                                String keyid) throws IOException 
    {
        System.out.println("Deactivate key test");
        System.out.println("******************************************");

        String version = "2.0";

        //  Make API rest call and get response from the server
        String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.DEACTIVATE_ENDPOINT + "/" + keyid;
        System.out.println("\nCalling deactivate @ " + resourceLoc);
            
        String contentSHA = "";
        String contentType = "";
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(resourceLoc);
        String requestToHmac = httpDelete.getMethod() + "\n"
                + contentSHA + "\n"
                + contentType + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpDelete.getURI().getPath();

        String hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpDelete.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpDelete.addHeader("Date", currentDate);
        httpDelete.addHeader("strongkey-api-version", version);
        CloseableHttpResponse response = httpclient.execute(httpDelete);
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
                    System.out.println("Error during deactive key : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during deactive key : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during deactive key : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }

        } finally {
            response.close();
        }
        
        System.out.println(" Response : " + result);

        System.out.println("\nDeactivate key test complete.");
        System.out.println("******************************************");
    }

    public static void patch(String REST_URI, 
                            String did, 
                            String accesskey, 
                            String secretkey, 
                            String keyid,
                            String status) throws Exception 
    {
        System.out.println("Update key test");
        System.out.println("******************************************");

        String version = "2.0";

        //  Make API rest call and get response from the server
        String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.UPDATE_ENDPOINT + "/" + keyid;
        System.out.println("\nCalling update @ " + resourceLoc);

        PatchFidoKeyRequest patch = new PatchFidoKeyRequest();
        patch.setStatus(status);
        patch.setModify_location("Sunnyvale, CA");
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(patch);

        ContentType mimetype = ContentType.create("application/merge-patch+json");
        StringEntity body = new StringEntity(json, mimetype);

        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String contentSHA = common.calculateSha256(json);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(resourceLoc);
        httpPatch.setEntity(body);
        String requestToHmac = httpPatch.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPatch.getURI().getPath();

        String hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpPatch.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPatch.addHeader("strongkey-content-sha256", contentSHA);
        httpPatch.addHeader("Content-Type", mimetype.getMimeType());
        httpPatch.addHeader("Date", currentDate);
        httpPatch.addHeader("strongkey-api-version", version);
        CloseableHttpResponse response = httpclient.execute(httpPatch);
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
                    System.out.println("Error during patch key : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during patch key : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during patch key : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }

        } finally {
            response.close();
        }
        
        System.out.println(" Response : " + result);

        System.out.println("\nActivate key test complete.");
        System.out.println("******************************************");
    }
}
