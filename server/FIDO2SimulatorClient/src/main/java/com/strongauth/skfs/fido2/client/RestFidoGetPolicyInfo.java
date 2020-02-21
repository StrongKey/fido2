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
 * Gets a FIDO2 Policy from StrongKey's FIDO2 Server using a REST web-service
 */

package com.strongauth.skfs.fido2.client;

import java.io.IOException;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.ProtocolException;
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
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoGetPolicyInfo {

    public static void getPolicyInfo(String REST_URI,
                                String did,
                                String accesskey,
                                String secretkey,
                                String metadataonly,
                                String sidpid)
    {
        String gkresponse = null;
        String version = "2.0";

        try {
            System.out.println("Get policy information test");
            System.out.println("******************************************");

            String pid = "";
            if (!sidpid.equalsIgnoreCase("null")) {
                pid = "&pid=" + sidpid;
            }
            String resourceLoc = REST_URI + Constants.REST_WEBSERVICE_SUFFIX + did + Constants.GET_POLICY_ENDPOINT + "?metadataonly=" + metadataonly + pid;

            System.out.println("\nCalling getpolicyinfo @ " + resourceLoc);

            String contentMD5 = "";
            String contentType = "";
            String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpGet httpGet = new HttpGet(resourceLoc);
            String requestToHmac = httpGet.getMethod() + "\n"
                    + contentMD5 + "\n"
                    + contentType + "\n"
                    + currentDate + "\n"
                    + version + "\n"
                    + httpGet.getURI().getPath() + "?" + httpGet.getURI().getQuery();

            String hmac = Common.calculateHMAC(secretkey, requestToHmac);
            httpGet.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
            httpGet.addHeader("Date", currentDate);
            httpGet.addHeader("strongkey-api-version", version);
            CloseableHttpResponse response = httpclient.execute(httpGet);
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

            // Check to see if there is any error
            try {
                gkresponse = responseJSON.getJsonArray("Response").toString();
                System.out.println("\nGet policy information test complete.");
                System.out.println("******************************************");
            } catch (Exception ex) {
                Logger.getLogger(RestFidoGetPolicyInfo.class.getName()).log(Level.SEVERE, null, ex);
            }
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
