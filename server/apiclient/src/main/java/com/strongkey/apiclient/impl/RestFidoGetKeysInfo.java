/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.apiclient.impl;

import com.strongkey.apiclient.common.Constants;
import com.strongkey.apiclient.common.common;
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

public class RestFidoGetKeysInfo {

    public static void getKeysInfo(String REST_URI, 
                                String did, 
                                String accesskey, 
                                String secretkey, 
                                String accountname) 
    {
        String gkresponse = null;
        String version = "2.0";

        try {

            System.out.println("Get user keys information test");
            System.out.println("******************************************");

            String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.GETKEYSINFO_ENDPOINT + "?username=" + accountname;

            System.out.println("\nCalling getkeysinfo @ " + resourceLoc);
            
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

            String hmac = common.calculateHMAC(secretkey, requestToHmac);
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
            
        } catch (MalformedURLException ex) {
            Logger.getLogger(RestFidoGetKeysInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ProtocolException ex) {
            Logger.getLogger(RestFidoGetKeysInfo.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RestFidoGetKeysInfo.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("GetKeys response : " + gkresponse);
    }
}
