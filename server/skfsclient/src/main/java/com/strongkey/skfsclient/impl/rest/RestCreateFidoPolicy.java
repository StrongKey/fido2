/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfsclient.impl.rest;

import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.common;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.json.JsonObject;
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
                            String authtype,
                            String credential1,
                            String credential2,
                            String status,
                            String notes,
                            String policy) throws Exception
    {

        String apiversion = "2.0";
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

        System.out.println("REST Create policy test with " + authtype);
        System.out.println("*******************************");

        JsonObject svcinfo;

        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
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
    JsonObject payload = javax.json.Json.createObjectBuilder()
                .add("notes", notes)
                .add("policy", policy)
                .add("status", status)
                .build();

        JsonObject data = javax.json.Json.createObjectBuilder()
                .add("svcinfo", svcinfo)
                .add("payload", payload)
                .build();
        
        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity dataStringEntity = new StringEntity(data.toString(), mimetype);

        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String payloadHash = common.calculateSha256(payload.toString());
        String resourceLoc = REST_URI + Constants.REST_SUFFIX + Constants.REST_CREATE_POLICY;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(dataStringEntity);
        String requestToHmac = httpPost.getMethod() + "\n"
                + payloadHash + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + apiversion + "\n"
                + httpPost.getURI().getPath();

        if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {  
                String hmac = common.calculateHMAC(credential2, requestToHmac);
                httpPost.addHeader("Authorization", "HMAC " + credential1 + ":" + hmac);
        }
        httpPost.addHeader("strongkey-content-sha256", payloadHash);
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
