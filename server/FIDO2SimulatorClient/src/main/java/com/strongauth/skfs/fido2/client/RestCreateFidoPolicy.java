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
 * Create and store a FIDO2 policy on the StrongKey FIDO2 Server
 */

package com.strongauth.skfs.fido2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strongauth.skfs.requests.CreateFidoPolicyRequest;
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
                            String policy)
            throws Exception
    {
        // Setup HTTP request body
        CreateFidoPolicyRequest cfpr = new CreateFidoPolicyRequest();
        cfpr.setStartDate(startdate);
        cfpr.setEndDate(enddate);
        cfpr.setCertificateProfileName(certificateProfileName);
        cfpr.setVersion(version);
        cfpr.setStatus(status);
        cfpr.setNotes(notes);
        cfpr.setPolicy(policy);
        String bodyjson = (new ObjectMapper()).writer().writeValueAsString(cfpr);
        System.out.println(bodyjson);
        String bodyhash = Common.calculateSha256(bodyjson);
        ContentType mimetype = ContentType.APPLICATION_JSON;

        // Setup HTTP request to webservice
        String wsendpoint = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.CREATE_POLICY_ENDPOINT;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(wsendpoint);
        httpPost.setEntity(new StringEntity(bodyjson, mimetype));

        // Generate hashed message authentication code (HMAC) of plaintext
        String apiversion = "2.0";
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String hmacpt = httpPost.getMethod() + "\n"
                + bodyhash + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + apiversion + "\n"
                + httpPost.getURI().getPath();
        String hmac = Common.calculateHMAC(secretkey, hmacpt);

        // Add HTTP headers
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", bodyhash);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", apiversion);

        // Make REST webservice request and get response from the server
        System.out.println("\nCalling create policy at: " + wsendpoint);
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
                    System.err.println("Error during create policy : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.err.println("Error during create policy : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.err.println("Error during create policy : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }
        System.out.println("Result of create policy: " + result);
    }
}
