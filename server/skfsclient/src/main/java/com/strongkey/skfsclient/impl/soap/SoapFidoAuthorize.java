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

import com.strongauth.skfs.fido2.simulator.FIDO2AuthenticatorSimulator;
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
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import javax.xml.ws.WebServiceException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;

public class SoapFidoAuthorize  {

    public static void authorize(String SOAP_URI,
                                    int did,
                                    String authtype,
                                    String credential1,
                                    String credential2,
                                    String username,
                                    String txid,
                                    String txpayload,
                                    String origin,
                                    int auth_counter,
                                    String verify,
                                    String crossOrigin)
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

            System.out.println("SOAP Authentication test with " + authtype);
            System.out.println("*******************************");
            // Build payload
            Payload payloadObj = new Payload();
            payloadObj.setUsername(username);
            payloadObj.setTxid(txid);
            payloadObj.setTxpayload(txpayload);
            payloadObj.setDisplayname(username + "_dn");
            payloadObj.setOptions(Constants.JSON_ATTESTATION_DIRECT);
            payloadObj.setExtensions(Constants.JSON_EMPTY);
            String payload = payloadObj.toJsonObject().toString();
            String payloadHash = common.calculateSha256(payloadObj.toJsonObject().toString());


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

            //  Make pre-authenticate call
            System.out.println("\nCalling preauthenticate @ " + SOAP_URI + Constants.SKFS_WSDL_SUFFIX);
            String response = port.preauthorize(svcinfo, payload);
            System.out.println(" Response : " + response);

            //  Build a json object out of response
            StringReader s = new StringReader(response);
            JsonReader jsonReader = Json.createReader(s);
            JsonObject responseJSON = jsonReader.readObject();
            jsonReader.close();
            JsonObject resJsonObj = responseJSON.getJsonObject("Response");

            System.out.println("\nPre-Authentication Complete.");

            System.out.println("\nGenerating Authentication response...\n");

            // Make call to simulator
//            System.out.println("******************************");
//            System.out.println(origin);
//            System.out.println(resJsonObj.toString());
//            System.out.println("******************************");
            Boolean co = Boolean.FALSE;
            if (crossOrigin.equalsIgnoreCase("true") || crossOrigin.equalsIgnoreCase("yes")) {
                co = Boolean.TRUE;
            }
            JsonObject input = FIDO2AuthenticatorSimulator.generateFIDO2AuthenticationResponse(resJsonObj.getJsonArray("allowCredentials").getJsonObject(0).getString("id"),
                    resJsonObj.toString(), origin, "packed", auth_counter, true, co);
            System.out.println("Simulator Response : ");

            StringReader regresreader = new StringReader(input.toString());
            JsonParserFactory factory = Json.createParserFactory(null);
            JsonParser parser = factory.createParser(regresreader);
            while (parser.hasNext()) {
                JsonParser.Event e = parser.next();
                switch (e) {
                    case KEY_NAME: {
                        System.out.print("\t" + parser.getString() + " = ");
                        break;
                    }
                    case VALUE_STRING: {
                        System.out.println(parser.getString());
                        break;
                    }
                    default:
                        break;
                }
            }
            System.out.println("\nFinished Generating Authentication Response.");
            System.out.println("\nAuthenticating ...");

            JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
                .add("version", "1.0") // ALWAYS since this is just the first revision of the code
                .add("last_used_location", "Sunnyvale, CA")
                .add(Constants.JSON_KEY_SERVLET_INPUT_USERNAME, username)
                .add("origin", origin)
                .build();
            JsonObjectBuilder auth_inner_response = javax.json.Json.createObjectBuilder()
                .add("authenticatorData", input.getJsonObject("response").getString("authenticatorData"))
                .add("signature", input.getJsonObject("response").getString("signature"))
                .add("userHandle", input.getJsonObject("response").getString("userHandle"))
                .add("clientDataJSON", input.getJsonObject("response").getString("clientDataJSON"));
            JsonObject auth_response = javax.json.Json.createObjectBuilder()
                .add("id", input.getString("id"))
                .add("rawId", input.getString("rawId"))
                .add("response", auth_inner_response) // inner response object
                .add("type", input.getString("type"))
                .build();

            payloadObj = new Payload();
            payloadObj.setMetadata(auth_metadata);
            payloadObj.setResponse(auth_response);
            payloadObj.setTxid(txid);
            payloadObj.setTxpayload(txpayload);
            payload = payloadObj.toJsonObject().toString();
            payloadHash = common.calculateSha256(payloadObj.toJsonObject().toString());

            // Build HMAC
            currentDate = System.currentTimeMillis();
            if(authtype.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
                ContentType mimetype = ContentType.APPLICATION_JSON;
                StringEntity body = new StringEntity(payload, mimetype);
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
            svcinfoJOB = javax.json.Json.createObjectBuilder()
                    .add("did", did)
                    .add("protocol", Constants.PROTOCOL_FIDO);
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

            //  Make authenticate call
            System.out.println("\nCalling authenticate @ " + SOAP_URI + Constants.SKFS_WSDL_SUFFIX);
            String regresponse = port.authorize(svcinfo, payload);
            System.out.println(" Response   : " + regresponse);

            System.out.println("\n Authentication Complete.");
            System.out.println("*******************************");

        } catch (Exception ex) {
            ex.printStackTrace();
            System.out.println(" Exception: " + ex.getLocalizedMessage());
        }
    }
}
