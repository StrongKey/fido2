/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfsclient.impl.rest;

import com.strongkey.skfs.requests.DeleteConfigurationRequest;
import com.strongkey.skfs.requests.UpdateConfigurationRequest;
import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.common;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoActionsOnConfiguration {
    
    public static void update(String hostport,
                            int did,
                            String authtype,
                            String credential1,
                            String credential2,
                            String configkey,
                            String configvalue,
                            String notes) throws Exception
    {
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

        System.out.println("REST Update configuration test with " + authtype);
        System.out.println("******************************************");

        // Build request
        UpdateConfigurationRequest update = new UpdateConfigurationRequest();

        // Build request svcinfo
        update.getSVCInfo().setDid(did);
        update.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            update.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
        } else {
            update.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
            update.getSVCInfo().setSVCUsername(credential1);
            update.getSVCInfo().setSVCPassword(credential2);
        }

        // Build request payload
        JsonObjectBuilder configuration = Json.createObjectBuilder();
        configuration.add("configkey", configkey);
        configuration.add("configvalue", configvalue);
        configuration.add("notes", notes);
        JsonArrayBuilder configurations = Json.createArrayBuilder();
        configurations.add(configuration.build());
        update.addConfiguration(configurations.build());

        // Prepare for POST call
        String json = update.toJsonObject().toString();
        System.out.println(json);
        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        String resourceLoc = hostport + Constants.REST_SUFFIX + Constants.REST_UPDATE_CONFIGURATION_ENDPOINT;

        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);

        // Build HMAC and add headers
        if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
        String payloadHash = common.calculateSha256(update.getPayload().toJsonObject().toString());
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String requestToHmac = httpPost.getMethod() + "\n"
                + payloadHash + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + Constants.API_VERSION + "\n"
                + httpPost.getURI().getPath();
            String hmac = common.calculateHMAC(credential2, requestToHmac);
            httpPost.addHeader("Authorization", "HMAC " + credential1 + ":" + hmac);
            httpPost.addHeader("strongkey-content-sha256", payloadHash);
            httpPost.addHeader("Date", currentDate);
            httpPost.addHeader("strongkey-api-version", Constants.API_VERSION);
        }
        httpPost.addHeader("Content-Type", mimetype.getMimeType());

        System.out.println("\nCalling updateconfiguration @ " + resourceLoc);
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
                    System.out.println("Error during update configuration : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during update configuration : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during update configuration : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }

        } finally {
            response.close();
        }

        System.out.println(" Response : " + result);

        System.out.println("\nUpdate configuration test complete.");
        System.out.println("******************************************");
    }

    public static void delete(String hostport,
                                int did,
                                String authtype,
                                String credential1,
                                String credential2,
                                String keyid) throws Exception
    {
        /*
        *               |HMAC     |PASSWORD   |
        *               |---------|-----------|
        * credential1   |accesskey|svcusername|
        * credential2   |secretkey|svcpassword|
        */
        if (!authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC) && !authtype.equalsIgnoreCase(Constants.AUTHORIZATION_PASSWORD)) {
            System.out.println("Invalid Authentication Type...\n");
            return;
        }

        try {
            System.out.println("REST Delete configuration test with " + authtype);
            System.out.println("******************************************");

            // Build request
            DeleteConfigurationRequest delete = new DeleteConfigurationRequest();

            // Build request svcinfo
            delete.getSVCInfo().setDid(did);
            delete.getSVCInfo().setProtocol(Constants.PROTOCOL_FIDO);
            if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
                delete.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_HMAC);
            } else {
                delete.getSVCInfo().setAuthtype(Constants.AUTHORIZATION_PASSWORD);
                delete.getSVCInfo().setSVCUsername(credential1);
                delete.getSVCInfo().setSVCPassword(credential2);
            }

            // Build request payload
            JsonArrayBuilder configurations = Json.createArrayBuilder();
            configurations.add(keyid);
            delete.addConfiguration(configurations.build());

            // Prepare for POST call
            String json = delete.toJsonObject().toString();
            ContentType mimetype = ContentType.APPLICATION_JSON;
            StringEntity body = new StringEntity(json, mimetype);

            String resourceLoc = hostport + Constants.REST_SUFFIX + Constants.REST_DELETE_CONFIGURATION_ENDPOINT;

            CloseableHttpClient httpclient = HttpClients.createDefault();
            HttpPost httpPost = new HttpPost(resourceLoc);
            httpPost.setEntity(body);

            // Build HMAC and add headers
            if (authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
                String payloadHash = common.calculateSha256(delete.getPayload().toJsonObject().toString());
                String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
                String requestToHmac = httpPost.getMethod() + "\n"
                        + payloadHash + "\n"
                        + mimetype.getMimeType() + "\n"
                        + currentDate + "\n"
                        + Constants.API_VERSION + "\n"
                        + httpPost.getURI().getPath();
                String hmac = common.calculateHMAC(credential2, requestToHmac);
                httpPost.addHeader("Authorization", "HMAC " + credential1 + ":" + hmac);
                httpPost.addHeader("strongkey-content-sha256", payloadHash);
                httpPost.addHeader("Date", currentDate);
                httpPost.addHeader("strongkey-api-version", Constants.API_VERSION);
            }
            httpPost.addHeader("Content-Type", mimetype.getMimeType());

            //  Make API rest call and get response from the server
            System.out.println("\nCalling deleteconfiguration @ " + resourceLoc);
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
                        System.out.println("Error during delete configuration : 401 HMAC Authentication Failed");
                        return;
                    case 404:
                        System.out.println("Error during delete configuration : 404 Resource not found");
                        return;
                    case 400:
                    case 500:
                    default:
                        System.out.println("Error during delete configuration : " + responseStatusLine.getStatusCode() + " " + result);
                        return;
                }

            } finally {
                response.close();
            }

            System.out.println(" Response : " + result);

            System.out.println("\nDelete configuration test complete.");
            System.out.println("******************************************");
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(RestFidoActionsOnKey.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
