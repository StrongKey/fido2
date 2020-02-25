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
 * Perform actions on a previously registered FIDO2 key: delete and updates
 */

package com.strongauth.skfs.fido2.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strongauth.skfs.requests.UpdateFidoKeyRequest;
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
                                String keyid)
            throws IOException
    {
        System.out.println("Deactivate key test");
        System.out.println("******************************************");

        // Setup HTTP request to webservice
        String version = "2.0";
        String wsendpoint = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.DEACTIVATE_ENDPOINT + "/" + keyid;
        System.out.println("\nCalling deactivate at: " + wsendpoint);

        String bodyhash = "";
        String contentType = "";
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpDelete httpDelete = new HttpDelete(wsendpoint);
        String hmacpt = httpDelete.getMethod() + "\n"
                + bodyhash + "\n"
                + contentType + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpDelete.getURI().getPath();
        String hmac = Common.calculateHMAC(secretkey, hmacpt);
        httpDelete.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpDelete.addHeader("Date", currentDate);
        httpDelete.addHeader("strongkey-api-version", version);

        // Make REST webservice request and get response from the server
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
                    System.err.println("Error during deactivate : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.err.println("Error during deactivate : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.err.println("Error during deactivate : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }

        } finally {
            response.close();
        }
        System.out.println(" Response : " + result);
        System.out.println("\nDeactivate key test complete.");
        System.out.println("******************************************");
    }

    public static void update(String REST_URI,
                            String did,
                            String accesskey,
                            String secretkey,
                            String keyid,
                            String status)
            throws Exception
    {
        System.out.println("Update key test");
        System.out.println("******************************************");

        // Setup HTTP request and request-body to webservice
        String wsendpoint = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.UPDATE_ENDPOINT + "/" + keyid;
        UpdateFidoKeyRequest patch = new UpdateFidoKeyRequest();
        patch.setStatus(status);
        patch.setModify_location("Sunnyvale, CA");
        String bodyjson = (new ObjectMapper()).writer().writeValueAsString(patch);

        ContentType mimetype = ContentType.create("application/merge-patch+json");
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String bodyhash = Common.calculateSha256(bodyjson);

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPatch httpPatch = new HttpPatch(wsendpoint);
        httpPatch.setEntity(new StringEntity(bodyjson, mimetype));

        // Generate hashed message authentication code (HMAC) of plaintext
        String version = "2.0";
        String hmacpt = httpPatch.getMethod() + "\n"
                + bodyhash + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPatch.getURI().getPath();
        String hmac = Common.calculateHMAC(secretkey, hmacpt);

        // Add HTTP headers
        httpPatch.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPatch.addHeader("strongkey-content-sha256", bodyhash);
        httpPatch.addHeader("Content-Type", mimetype.getMimeType());
        httpPatch.addHeader("Date", currentDate);
        httpPatch.addHeader("strongkey-api-version", version);

        // Make REST webservice request and get response from the server
        System.out.println("\nCalling update at: " + wsendpoint);
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
                    System.err.println("Error during patch key : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.err.println("Error during patch key : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.err.println("Error during patch key : " + responseStatusLine.getStatusCode() + " " + result);
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
