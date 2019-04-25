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
import com.strongkey.skfs.requests.PatchFidoPolicyRequest;
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

public class RestFidoActionsOnPolicy {

    public static void delete(String REST_URI, 
                                String did, 
                                String accesskey, 
                                String secretkey, 
                                String sidpid) throws IOException 
    {
        System.out.println("Delete policy test");
        System.out.println("******************************************");

        String apiversion = "2.0";

        //  Make API rest call and get response from the server
        String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.DELETE_POLICY_ENDPOINT + "/" + sidpid;
        System.out.println("\nCalling delete policy @ " + resourceLoc);
            
        String contentSHA = "";
        String contentType = "";
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(resourceLoc);
        String requestToHmac = httpDelete.getMethod() + "\n"
                + contentSHA + "\n"
                + contentType + "\n"
                + currentDate + "\n"
                + apiversion + "\n"
                + httpDelete.getURI().getPath();

        String hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpDelete.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpDelete.addHeader("Date", currentDate);
        httpDelete.addHeader("strongkey-api-version", apiversion);
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
                    System.out.println("Error during delete policy : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during delete policy : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during delete policy : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }

        } finally {
            response.close();
        }
        
        System.out.println(" Response : " + result);

        System.out.println("\nDelete policy test complete.");
        System.out.println("******************************************");
    }

    public static void patch(String REST_URI, 
                            String did, 
                            String accesskey, 
                            String secretkey, 
                            String sidpid,
                            Long startdate,
                            Long enddate,
                            Integer version,
                            String status,
                            String notes,
                            String policy) throws Exception 
    {
        System.out.println("Patch policy test");
        System.out.println("******************************************");

        String apiversion = "2.0";

        //  Make API rest call and get response from the server
        String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.PATCH_POLICY_ENDPOINT + "/" + sidpid;
        System.out.println("\nCalling update @ " + resourceLoc);

        PatchFidoPolicyRequest pfpr = new PatchFidoPolicyRequest();
        pfpr.setStartDate(startdate);
        pfpr.setEndDate(enddate);
        pfpr.setVersion(version);
        pfpr.setStatus(status);
        pfpr.setNotes(notes);
        pfpr.setPolicy(policy);

        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(pfpr);

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
                + apiversion + "\n"
                + httpPatch.getURI().getPath();

        String hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpPatch.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPatch.addHeader("strongkey-content-sha256", contentSHA);
        httpPatch.addHeader("Content-Type", mimetype.getMimeType());
        httpPatch.addHeader("Date", currentDate);
        httpPatch.addHeader("strongkey-api-version", apiversion);
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
                    System.out.println("Error during patch fido policy : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during patch fido policy : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during patch fido policy : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }

        } finally {
            response.close();
        }
        
        System.out.println(" Response : " + result);

        System.out.println("\nPatch policy test complete.");
        System.out.println("******************************************");
    }
}
