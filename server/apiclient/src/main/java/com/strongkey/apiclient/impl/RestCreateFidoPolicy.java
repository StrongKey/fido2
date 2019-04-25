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
import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestCreateFidoPolicy {

    public static void create(String REST_URI, 
                            String did, 
                            String accesskey, 
                            String secretkey, 
                            Long startdate, 
                            Long enddate, 
                            String certificateProfileName,
                            Integer version,
                            String status,
                            String notes,
                            String policy) throws Exception
    {

        String apiversion = "2.0";

        CreateFidoPolicyRequest cfpr = new CreateFidoPolicyRequest();
        cfpr.setStartDate(startdate);
        cfpr.setEndDate(enddate);
        cfpr.setCertificateProfileName(certificateProfileName);
        cfpr.setVersion(version);
        cfpr.setStatus(status);
        cfpr.setNotes(notes);
        cfpr.setPolicy(policy);
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(cfpr);
        System.out.println(json);

        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String contentSHA = common.calculateSha256(json);

        String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.CREATE_POLICY_ENDPOINT;
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);
        String requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + apiversion + "\n"
                + httpPost.getURI().getPath();

        String hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", apiversion);

        //  Make API rest call and get response from the server
        System.out.println("\nCalling create policy @ " + resourceLoc);
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
                    System.out.println("Error during create policy : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during create policy : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during create policy : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println("Result of create policy: " + result);
    }
}
