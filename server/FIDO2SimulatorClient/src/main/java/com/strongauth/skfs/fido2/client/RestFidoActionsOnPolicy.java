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
 * Perform actions on a previously registered FIDO2 policy: delete and updates
 */

package com.strongauth.skfs.fido2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strongauth.skfs.requests.UpdateFidoPolicyRequest;
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
                                String sidpid)
            throws IOException
    {
        System.out.println("Delete policy test");
        System.out.println("******************************************");

        // Setup HTTP request to webservice
        String wsendpoint = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.DELETE_POLICY_ENDPOINT + "/" + sidpid;
        String bodyhash = "";
        String contentType = "";
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(wsendpoint);

        // Generate hashed message authentication code (HMAC) of plaintext
        String apiversion = "2.0";
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String hmacpt = httpDelete.getMethod() + "\n"
                + bodyhash + "\n"
                + contentType + "\n"
                + currentDate + "\n"
                + apiversion + "\n"
                + httpDelete.getURI().getPath();
        String hmac = Common.calculateHMAC(secretkey, hmacpt);

        // Add HTTP headers
        httpDelete.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpDelete.addHeader("Date", currentDate);
        httpDelete.addHeader("strongkey-api-version", apiversion);

        // Make API rest call and get response from the server
        System.out.println("\nCalling delete policy at: " + wsendpoint);
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
                    System.err.println("Error during delete policy : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.err.println("Error during delete policy : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.err.println("Error during delete policy : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println(" Response : " + result);
        System.out.println("\nDelete policy test complete.");
        System.out.println("******************************************");
    }

    public static void update(String REST_URI,
                            String did,
                            String accesskey,
                            String secretkey,
                            String sidpid,
                            Long startdate,
                            Long enddate,
                            Integer version,
                            String status,
                            String notes,
                            String policy)
            throws Exception
    {
        System.out.println("Update policy test");
        System.out.println("******************************************");

        // Create HTTP request body
        UpdateFidoPolicyRequest pfpr = new UpdateFidoPolicyRequest();
        pfpr.setStartDate(startdate);
        pfpr.setEndDate(enddate);
        pfpr.setVersion(version);
        pfpr.setStatus(status);
        pfpr.setNotes(notes);
        pfpr.setPolicy(policy);
        String bodyjson = (new ObjectMapper().writer()).writeValueAsString(pfpr);
        String bodyhash = Common.calculateSha256(bodyjson);
        ContentType mimetype = ContentType.create("application/merge-patch+json");
        StringEntity body = new StringEntity(bodyjson, mimetype);

        // Setup HTTP request to webservice
        String wsendpoint = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.PATCH_POLICY_ENDPOINT + "/" + sidpid;
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(wsendpoint);
        httpPatch.setEntity(body);

        // Generate hashed message authentication code (HMAC) of plaintext
        String apiversion = "2.0";
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String hmacpt = httpPatch.getMethod() + "\n"
                + bodyhash + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + apiversion + "\n"
                + httpPatch.getURI().getPath();
        String hmac = Common.calculateHMAC(secretkey, hmacpt);

        // Add HTTP headers
        httpPatch.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPatch.addHeader("strongkey-content-sha256", bodyhash);
        httpPatch.addHeader("Content-Type", mimetype.getMimeType());
        httpPatch.addHeader("Date", currentDate);
        httpPatch.addHeader("strongkey-api-version", apiversion);

        // Make REST webservice request and get response from the server
        System.out.println("\nCalling update policy at: " + wsendpoint);
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
                    System.err.println("Error during update fido policy : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.err.println("Error during update fido policy : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.err.println("Error during update fido policy : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }

        System.out.println(" Response : " + result);
        System.out.println("\nUpdate policy test complete.");
        System.out.println("******************************************");
    }
}
