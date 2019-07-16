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
import com.strongauth.skfe.fido2.Fido2TokenSim;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.requests.RegistrationRequest;
import com.strongauth.skfe.tokensim.FIDOU2FTokenSimulator;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;
import org.apache.commons.codec.DecoderException;
import org.apache.http.HttpEntity;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

public class RestFidoRegister {

    public static void register(String REST_URI, 
                            String did, 
                            String accesskey, 
                            String secretkey, 
                            String fidoprotocol, 
                            String accountname, 
                            String origin) throws Exception
    {
        System.out.println("Registration test");
        System.out.println("*******************************");

        String version = "2.0";

        PreregistrationRequest prereg = new PreregistrationRequest();
        prereg.setProtocol(fidoprotocol);
        prereg.setUsername(accountname);
        ObjectWriter ow = new ObjectMapper().writer();
        String json = ow.writeValueAsString(prereg);

        ContentType mimetype = ContentType.APPLICATION_JSON;
        StringEntity body = new StringEntity(json, mimetype);

        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String contentSHA = common.calculateSha256(json);

        String resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.PRE_REGISTER_ENDPOINT;
        
        CloseableHttpClient httpclient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);
        String requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        String hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        //  Make API rest call and get response from the server
        System.out.println("\nCalling preregister @ " + resourceLoc);
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
                    System.out.println("Error during pre-register : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during pre-register : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during pre-register : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }
        
        //  Build a json object out of response
        StringReader s = new StringReader(result);
        JsonReader jsonReader = Json.createReader(s);
        JsonObject responseJSON = jsonReader.readObject();
        jsonReader.close();
        
        JsonObject resJsonObj = responseJSON.getJsonObject("Response");
        
        System.out.println("\n Pre-Registration Complete.");

        System.out.println("\n Generating Registration response...\n");
        
        JsonObject input = null;
        JsonParserFactory factory = Json.createParserFactory(null);
        JsonParser parser;
        
        if ("U2F_V2".compareTo(fidoprotocol) == 0) {
            
            System.out.println("\n Registration Parameters:\n");
            JsonArray jarray = resJsonObj.getJsonArray("registerRequests");
            String s2 = jarray.getJsonObject(0).toString();
            s = new StringReader(s2);
                parser = factory.createParser(s);
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
                }
            }
            
            String appidfromserver = resJsonObj.getString("appId");
            
            try {
                    input = FIDOU2FTokenSimulator.generateRegistrationResponse(appidfromserver, s2, origin, true);
                } catch (NoSuchAlgorithmException
                        | NoSuchProviderException
                        | KeyStoreException
                        | InvalidParameterSpecException
                        | DecoderException
                        | IOException
                        | CertificateException
                        | InvalidAlgorithmParameterException
                        | InvalidKeyException
                        | SignatureException
                        | NoSuchPaddingException
                        | IllegalBlockSizeException
                        | BadPaddingException
                        | ShortBufferException
                        | UnrecoverableKeyException
                        | InvalidKeySpecException ex) {
                System.out.println("\n Exception : " + ex.getLocalizedMessage());
            }
        
        } else if ("FIDO20".compareTo(fidoprotocol) == 0) {
            
            JsonObject rpInfo = resJsonObj.getJsonObject("rp");
            String rpName = rpInfo.getString("name");
            String challenge = resJsonObj.getString("challenge");
            
            Fido2TokenSim sim = new Fido2TokenSim(origin);
            JsonObjectBuilder in = Json.createObjectBuilder();
            
            JsonObjectBuilder rp = Json.createObjectBuilder();
            rp.add(Constants.WebAuthn.RELYING_PARTY_NAME, rpName);
            in.add(Constants.WebAuthn.RELYING_PARTY, rp);
            
            JsonObjectBuilder user = Json.createObjectBuilder();
            user.add(Constants.WebAuthn.USER_NAME,accountname);
            user.add(Constants.WebAuthn.USER_ID,accountname);
            in.add(Constants.WebAuthn.USER,user);
            
            JsonArrayBuilder pubKeyParams = Json.createArrayBuilder();
            JsonObjectBuilder alg1 = Json.createObjectBuilder();
            alg1.add(Constants.WebAuthn.PUBKEYCREDPARAMS_ALG, -7);
            alg1.add(Constants.WebAuthn.TYPE, "public-key");
            pubKeyParams.add(alg1);

            JsonObjectBuilder alg2 = Json.createObjectBuilder();
            alg2.add(Constants.WebAuthn.PUBKEYCREDPARAMS_ALG, -257);
            alg2.add(Constants.WebAuthn.TYPE, "public-key");
            pubKeyParams.add(alg2);

            in.add(Constants.WebAuthn.PUBKEYCREDPARAMS, pubKeyParams);
            
            in.add(Constants.WebAuthn.CHALLENGE,challenge);
            in.add(Constants.WebAuthn.ATTESTATION_PREFERENCE,"direct");
            
            /*
            {
                "rp": {
                    "name": "example.com"
                },
                "user": {
                    "name": "fidouser06110896",
                    "displayName": "fidouser06110896",
                    "id": "B16lQ8O1ZDTNX0NP0EP8dNRV6ShLlS4cbcWa72r2GyDfleYgFoJe7xZBIvST9PtZB_Jjx8als_XqggjeTQJyFw"
                },
                "challenge": "VciUZwhfiPCdElS0RygNEHAxxKtqUBkFN472KakrjsgqfFLKNm8wOkGYQFaqklFYrtNST1QLSOuaOO9r-428GH7LZ6qJ9NYkdH79jonCDptr5Pt4BfFmQDg0bTJXpc1dLAPRYsyrezVDtTWNw2FX3mibjvst9ThxfNe8deWdVsE",
                "attestation": "direct",
                "pubKeyCredParams": [{
                    "type": "public-key",
                    "alg": -7
                }, {
                    "type": "public-key",
                    "alg": -257
                }]
            }
            */
            input = sim.create(in.build());
        }

        StringReader regresreader = new StringReader(input.toString());
        parser = factory.createParser(regresreader);
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
            }
        }
            
        System.out.println("\n Finished Generating Registration Response.");
        System.out.println("\n Registering ...");
        
        //  Build payload
        JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
                .add("version", "1.0") // ALWAYS since this is just the first revision of the code
                .add("create_location", "Sunnyvale, CA")
                .add(Constants.JSON_KEY_SERVLET_INPUT_USERNAME, accountname)
                .build();

        //  Make API rest call and get response from the server
        System.out.println("\nCalling register @ " 
                + REST_URI + Constants.REGISTER_ENDPOINT);

        ow = new ObjectMapper().writer();
        RegistrationRequest reg = new RegistrationRequest();
        reg.setProtocol(fidoprotocol);
        reg.setResponse(input.toString());
        reg.setMetadata(reg_metadata.toString());
        json = ow.writeValueAsString(reg);
        body = new StringEntity(json, mimetype);

        currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        contentSHA = common.calculateSha256(json);

        resourceLoc = REST_URI + Constants.REST_SUFFIX + did + Constants.REGISTER_ENDPOINT;

        httpclient = HttpClients.createDefault();
        httpPost = new HttpPost(resourceLoc);
        httpPost.setEntity(body);
        requestToHmac = httpPost.getMethod() + "\n"
                + contentSHA + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + version + "\n"
                + httpPost.getURI().getPath();

        hmac = common.calculateHMAC(secretkey, requestToHmac);
        httpPost.addHeader("Authorization", "HMAC " + accesskey + ":" + hmac);
        httpPost.addHeader("strongkey-content-sha256", contentSHA);
        httpPost.addHeader("Content-Type", mimetype.getMimeType());
        httpPost.addHeader("Date", currentDate);
        httpPost.addHeader("strongkey-api-version", version);

        //  Make API rest call and get response from the server
        System.out.println("\nCalling register @ " + resourceLoc);
        response = httpclient.execute(httpPost);
        try {
            StatusLine responseStatusLine = response.getStatusLine();
            HttpEntity entity = response.getEntity();
            String regresponse = EntityUtils.toString(entity);
            EntityUtils.consume(entity);

            switch (responseStatusLine.getStatusCode()) {
                case 200:
                    System.out.println(" Response   : " + regresponse);
                    break;
                case 401:
                    System.out.println("Error during register : 401 HMAC Authentication Failed");
                    return;
                case 404:
                    System.out.println("Error during register : 404 Resource not found");
                    return;
                case 400:
                case 500:
                default:
                    System.out.println("Error during register : " + responseStatusLine.getStatusCode() + " " + result);
                    return;
            }
        } finally {
            response.close();
        }
        
        System.out.println("\n Registration Complete.");
        System.out.println("*******************************");
    }
}
