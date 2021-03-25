/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */
package com.strongkey.skfsclient.impl.soap;

import com.strongkey.skfs.soapstubs.*;
import com.strongkey.skfsclient.common.Constants;
import com.strongkey.skfsclient.common.Payload;
import com.strongkey.skfsclient.common.common;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.xml.ws.WebServiceException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class SoapFidoGetKeysInfo {

    public static void getKeysInfo(String SOAP_URI,
                                    int did,
                                    String authtype,
                                    String credential1,
                                    String credential2,
                                    String username)
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

        try {

            //  set up the port
            SKFSServlet port = null;
            try {
                // Set up the URL and webService variables
                //  Create port object
                URL soapurl = new URL(SOAP_URI + Constants.SKFS_WSDL_SUFFIX);
                Soap service = new Soap(soapurl);
                port = service.getSKFSServletPort();
            } catch (MalformedURLException ex) {
                throw new Exception("Malformed hostport - " + SOAP_URI);
            } catch (WebServiceException ex) {
                throw new Exception("It appears that the site - " + SOAP_URI
                        + " - is (1) either down or (2) has no access over specified port or (3) has a digital certificate that is not in your JVM's truststore.  "
                        + "In case of (3), Please include it in the JAVA_HOME/jre/lib/security/cacerts file with "
                        + "the keytool -import command before attempting this operation again.  "
                        + "Please refer to the documentation on skceclient.jar at the "
                        + "above-mentioned URL on how to accomplish this.");
            }

            System.out.println("SOAP Get user keys information test with " + authtype);
            System.out.println("******************************************");

            // Build payload
            Payload payloadObj = new Payload();
            payloadObj.setUsername(username);
            String payload = payloadObj.toJsonObject().toString();
            String payloadHash = common.calculateSha256(payload);

            String resourceLoc = SOAP_URI + Constants.SKFS_WSDL_SUFFIX;

            // Build HMAC
            long currentDate = System.currentTimeMillis();
            String hmac = null;
            HttpPost httpPost = null;
            String requestToHmac;
            if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
                ContentType mimetype = ContentType.APPLICATION_JSON;
                StringEntity body = new StringEntity(payload, mimetype);
                httpPost = new HttpPost(resourceLoc);
                httpPost.setEntity(body);
                requestToHmac = httpPost.getMethod() + "\n"
                    + payloadHash + "\n"
                    + mimetype.getMimeType() + "\n"
                    + currentDate + "\n"
                    + Constants.API_VERSION + "\n"
                    + Constants.SKFS_WSDL_SUFFIX;
                hmac = common.calculateHMAC(credential2, requestToHmac);
            }

            // Build service info
            JsonObjectBuilder svcinfoJOB = javax.json.Json.createObjectBuilder()
                    .add("did", did)
                    .add("protocol", Constants.PROTOCOL_FIDO);
            String svcinfo;
            if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
                svcinfo = svcinfoJOB
                        .add("authtype", Constants.AUTHORIZATION_HMAC)
                        .add("strongkey-api-version", Constants.API_VERSION)
                        .add("strongkey-content-sha256", common.calculateSha256(payload))
                        .add("authorization", "HMAC " + credential1 + ":" + hmac)
                        .add("timestamp", currentDate)
                        .build().toString();
            } else {
                svcinfo = svcinfoJOB
                        .add("authtype", Constants.AUTHORIZATION_PASSWORD)
                        .add("svcusername", credential1)
                        .add("svcpassword", credential2)
                        .build().toString();
            }

            //  Make getkeysinfo call
            System.out.println("\nCalling getkeysinfo @ " + SOAP_URI + Constants.SKFS_WSDL_SUFFIX);
            String response = port.getkeysinfo(svcinfo, payload);
            System.out.println(" Response : " + response);

            //  Build a json object out of response
            StringReader s = new StringReader(response);
            JsonObject responseJSON;
            try (JsonReader jsonReader = Json.createReader(s)) {
                responseJSON = jsonReader.readObject();
            }

            //  Check to see if there is any
            try {
                String error = responseJSON.getString("Error");
                if ( error != null && !error.equalsIgnoreCase("")) {
                    System.out.println("*******************************");
                    System.out.println("Error during getkeysinfo : " + error);
                }
            } catch (Exception ex) {
                //  continue since there is no error
            }

            System.out.println("\nGet user keys information test complete.");
            System.out.println("******************************************");
        } catch (Exception ex) {
            System.out.println("Exception: " + ex.getLocalizedMessage());
        }
    }
}
