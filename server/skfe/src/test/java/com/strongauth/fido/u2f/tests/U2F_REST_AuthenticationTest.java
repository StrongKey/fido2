// /**
// * Copyright StrongAuth, Inc. All Rights Reserved.
// *
// * Use of this source code is governed by the GNU Lesser General Public License v2.1
// * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
// *
// * ************************************************
// *
// *  888b    888          888
// *  8888b   888          888
// *  88888b  888          888
// *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
// *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
// *  888  Y88888 888  888 888    88888888 "Y8888b.
// *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
// *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
// *
// * ************************************************
// *
// * JUnit test class that performs authentication tests using REST based web
// * services.
// *
// */
//package com.strongauth.fido.u2f.tests;
//
//import com.strongauth.skce.utilities.Common;
//import com.strongauth.skce.utilities.Constants;
//import com.sun.jersey.api.client.Client;
//import com.sun.jersey.api.client.ClientResponse;
//import com.sun.jersey.api.client.WebResource;
//import com.sun.jersey.api.client.config.ClientConfig;
//import com.sun.jersey.api.client.config.DefaultClientConfig;
//import com.sun.jersey.api.uri.UriComponent;
////import com.strongauth.fido.u2f.clientsimulator.SoapFidoClient;
//import fidosoap.client.SoapFidoClient;
//import com.sun.jersey.api.client.ClientHandlerException;
//import com.sun.jersey.api.client.UniformInterfaceException;
//import java.io.StringReader;
//import java.io.UnsupportedEncodingException;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.SignatureException;
//import java.security.spec.InvalidKeySpecException;
//import javax.crypto.BadPaddingException;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.NoSuchPaddingException;
//import javax.crypto.ShortBufferException;
//import javax.json.Json;
//import javax.json.JsonArray;
//import javax.json.JsonObject;
//import javax.json.JsonReader;
//import javax.json.stream.JsonParser;
//import javax.json.stream.JsonParserFactory;
//import javax.ws.rs.core.MediaType;
//import static junit.framework.Assert.assertNotNull;
//import static junit.framework.Assert.assertNotSame;
//import static junit.framework.Assert.assertSame;
//import static junit.framework.Assert.assertTrue;
//import junit.framework.TestCase;
//import org.apache.commons.codec.DecoderException;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//
///**
// * JUnit test class that performs authentication tests using REST based web
// * services
// */
//public class U2F_REST_AuthenticationTest extends TestCase {
//
//    //  SKCE host url and port object declaration
//    private String  fidoserverURI   = Common.getConfigurationProperty("skfe.cfg.property.junit.skfe.url.rest");
//
//    //  Test user in LDAP to be used for junit tests.
//    private String  testusername   = Common.getConfigurationProperty("skfe.cfg.property.junit.skfe.testuser");
//
//    public U2F_REST_AuthenticationTest(String testName) {
//        super(testName);
//    }
//
//    /*******************************************************************************
//                        888    888     888
//                        888    888     888
//                        888    888     888
//      .d8888b   .d88b.  888888 888     888 88888b.
//      88K      d8P  Y8b 888    888     888 888 "88b
//      "Y8888b. 88888888 888    888     888 888  888
//           X88 Y8b.     Y88b.  Y88b. .d88P 888 d88P
//       88888P'  "Y8888   "Y888  "Y88888P"  88888P"
//                                           888
//                                           888
//                                           888
//     *******************************************************************************/
//    /**
//     *
//     * @throws Exception
//     */
//    @Override
//    @BeforeClass
//    protected void setUp() throws Exception {
//        super.setUp();
//    }
//
//    /*******************************************************************************
//    888                              8888888b.
//    888                              888  "Y88b
//    888                              888    888
//    888888  .d88b.   8888b.  888d888 888    888  .d88b.  888  888  888 88888b.
//    888    d8P  Y8b     "88b 888P"   888    888 d88""88b 888  888  888 888 "88b
//    888    88888888 .d888888 888     888    888 888  888 888  888  888 888  888
//    Y88b.  Y8b.     888  888 888     888  .d88P Y88..88P Y88b 888 d88P 888  888
//     "Y888  "Y8888  "Y888888 888     8888888P"   "Y88P"   "Y8888888P"  888  888
//     *
//     *******************************************************************************/
//    /**
//     *
//     * @throws Exception
//     */
//    @Override
//    @AfterClass
//    protected void tearDown() throws Exception {
//        super.tearDown();
//    }
//
//    /*******************************************************************************
//       d8888          888    888                        888    d8b                   888    d8b
//      d88888          888    888                        888    Y8P                   888    Y8P
//     d88P888          888    888                        888                          888
//    d88P 888 888  888 888888 88888b.   .d88b.  88888b.  888888 888  .d8888b  8888b.  888888 888  .d88b.  88888b.
//   d88P  888 888  888 888    888 "88b d8P  Y8b 888 "88b 888    888 d88P"        "88b 888    888 d88""88b 888 "88b
//  d88P   888 888  888 888    888  888 88888888 888  888 888    888 888      .d888888 888    888 888  888 888  888
// d8888888888 Y88b 888 Y88b.  888  888 Y8b.     888  888 Y88b.  888 Y88b.    888  888 Y88b.  888 Y88..88P 888  888
//d88P     888  "Y88888  "Y888 888  888  "Y8888  888  888  "Y888 888  "Y8888P "Y888888  "Y888 888  "Y88P"  888  888
//
//    *******************************************************************************/
//
//
//    /**
//     * Test of preauth and authenticate methods, of class restfido.
//     * @throws java.lang.Exception
//     */
//    public void test_REST_Authentication_GoodSignature() throws Exception {
//        System.out.println("\nExecuting test_REST_Authentication_GoodSignature");
//
//        String exmsg = "";
//        boolean res = false;
//
//        try {
//            System.out.println("\n***************** START - Authentication test (REST calls) *******************");
//
//            ClientConfig config = new DefaultClientConfig();
//            Client client = Client.create(config);
//            WebResource service = client.resource(fidoserverURI);
//
//            WebResource preAuth = service.path("/" + Constants.FIDO_METHOD_PREAUTH)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("username", testusername);
//
//            System.out.println("\n Calling preauth ... @ " + preAuth.getURI().toString());
//            String response = preAuth.accept(MediaType.APPLICATION_JSON).get(String.class);
//            assertNotNull("Pre-auth response from the fido server is null;  ", response);
//            assertNotSame("Pre-auth response from the fido server is empty; ", "", response);
//
//            System.out.println("\n U2F authentication challenge parameters :\n");
//            StringReader s = new StringReader(response);
//            JsonReader jsonReader = Json.createReader(s);
//            JsonObject responseJSON = jsonReader.readObject();
//            jsonReader.close();
//            String s1 = responseJSON.getJsonObject("Challenge").toString();
//            s = new StringReader(s1);
//
//            jsonReader = Json.createReader(s);
//            JsonObject resJsonObj = jsonReader.readObject();
//            jsonReader.close();
//            String s2 = resJsonObj.getJsonArray("signData").toString();
//            s = new StringReader(s2);
//            JsonParserFactory factory = Json.createParserFactory(null);
//            JsonParser parser = factory.createParser(s);
//            while (parser.hasNext()) {
//                JsonParser.Event e = parser.next();
//                switch (e) {
//                    case KEY_NAME: {
//                        System.out.print("\t" + parser.getString() + " = ");
//                        break;
//                    }
//                    case VALUE_STRING: {
//                        System.out.println(parser.getString());
//                        break;
//                    }
//                }
//            }
//            System.out.println("\n Pre-Authentication Complete.");
//
//            System.out.println("\n Generating Authentication response...\n");
//
//            JsonArray ja = resJsonObj.getJsonArray("signData");
//            String s3 = ja.getJsonObject(0).toString();
//            String input = null;
//            try {
//                input = SoapFidoClient.generateAuthenticationResponse2fs(s3, true);
//            } catch (NoSuchAlgorithmException |
//                    NoSuchProviderException |
//                    UnsupportedEncodingException |
//                    DecoderException |
//                    NoSuchPaddingException |
//                    InvalidKeyException |
//                    InvalidAlgorithmParameterException |
//                    ShortBufferException |
//                    IllegalBlockSizeException |
//                    BadPaddingException |
//                    InvalidKeySpecException |
//                    SignatureException ex) {
//                System.out.println("\n Exception : " + ex.getLocalizedMessage());
//            }
//            StringReader regresreader = new StringReader(input);
//            parser = factory.createParser(regresreader);
//            while (parser.hasNext()) {
//                JsonParser.Event e = parser.next();
//                switch (e) {
//                    case KEY_NAME: {
//                        System.out.print("\t" + parser.getString() + " = ");
//                        break;
//                    }
//                    case VALUE_STRING: {
//                        System.out.println(parser.getString());
//                        break;
//                    }
//                }
//            }
//            System.out.println("\n Finished Generating Authentication Response.");
//            System.out.println("\n nAuthenticating with fido server ...");
//
//            //  test authenticate
//            JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
//                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
//                    .add("last_used_location", "Bangalore, India").
//                    build();
//            WebResource auth = service.path(Constants.FIDO_METHOD_AUTHENTICATE)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("auth_metadata", UriComponent.encode(auth_metadata.toString(), UriComponent.Type.QUERY_PARAM));
//            ClientResponse clresponse = auth.accept(MediaType.TEXT_HTML).type("application/json").post(ClientResponse.class, input);
//            String authresponse = clresponse.getEntity(String.class);
//            assertNotNull("Authentication response from the fido server is null;  ", authresponse);
//            assertNotSame("Authentication response from the fido server is empty; ", "", authresponse);
//
//            StringReader regs = new StringReader(authresponse);
//            JsonReader jr = Json.createReader(regs);
//            JsonObject authrespJson = jr.readObject();
//            jr.close();
//            String authres = authrespJson.getJsonString("Response").getString();
//            assertNotSame("Authentication is unsuccessfull; ", "Successfully processed registration response", authres);
//            res = true;
//
//            System.out.println("\n Response from fido server : " + authres);
//            System.out.println("\n Authentication Complete.");
//            System.out.println("\n***************** END - Authentication test (REST calls) *******************");
//
//        } catch (UniformInterfaceException | ClientHandlerException ex) {
//            exmsg = ex.getLocalizedMessage();
//        }
//
//        //  prompt exceptions using this assert
//        assertTrue("Exception occured during Authentication; " + exmsg, res);
//    }
//
//    public void test_REST_Authentication_BadSignature() throws Exception {
//        System.out.println("\nExecuting test_REST_Authentication_BadSignature");
//
//        String exmsg = "";
//        boolean res = false;
//
//        try {
//            System.out.println("\n***************** START - Authentication test (REST calls) *******************");
//
//            ClientConfig config = new DefaultClientConfig();
//            Client client = Client.create(config);
//            WebResource service = client.resource(fidoserverURI);
//
//            WebResource preAuth = service.path("/" + Constants.FIDO_METHOD_PREAUTH)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("username", testusername);
//
//            System.out.println("\n Calling preauth ... @ " + preAuth.getURI().toString());
//            String response = preAuth.accept(MediaType.APPLICATION_JSON).get(String.class);
//            assertNotNull("Pre-auth response from the fido server is null;  ", response);
//            assertNotSame("Pre-auth response from the fido server is empty; ", "", response);
//
//            System.out.println("\n U2F authentication challenge parameters :\n");
//            StringReader s = new StringReader(response);
//            JsonReader jsonReader = Json.createReader(s);
//            JsonObject responseJSON = jsonReader.readObject();
//            jsonReader.close();
//            String s1 = responseJSON.getJsonObject("Challenge").toString();
//            s = new StringReader(s1);
//
//            jsonReader = Json.createReader(s);
//            JsonObject resJsonObj = jsonReader.readObject();
//            jsonReader.close();
//            String s2 = resJsonObj.getJsonArray("signData").toString();
//            s = new StringReader(s2);
//            JsonParserFactory factory = Json.createParserFactory(null);
//            JsonParser parser = factory.createParser(s);
//            while (parser.hasNext()) {
//                JsonParser.Event e = parser.next();
//                switch (e) {
//                    case KEY_NAME: {
//                        System.out.print("\t" + parser.getString() + " = ");
//                        break;
//                    }
//                    case VALUE_STRING: {
//                        System.out.println(parser.getString());
//                        break;
//                    }
//                }
//            }
//            System.out.println("\n Pre-Authentication Complete.");
//
//            System.out.println("\n Generating Authentication response...\n");
//
//            JsonArray ja = resJsonObj.getJsonArray("signData");
//            String s3 = ja.getJsonObject(0).toString();
//            String input = null;
//            try {
//                input = SoapFidoClient.generateAuthenticationResponse2fs(s3, false);
//            } catch (NoSuchAlgorithmException |
//                    NoSuchProviderException |
//                    UnsupportedEncodingException |
//                    DecoderException |
//                    NoSuchPaddingException |
//                    InvalidKeyException |
//                    InvalidAlgorithmParameterException |
//                    ShortBufferException |
//                    IllegalBlockSizeException |
//                    BadPaddingException |
//                    InvalidKeySpecException |
//                    SignatureException ex) {
//                System.out.println("\n Exception : " + ex.getLocalizedMessage());
//            }
//
//            StringReader regresreader = new StringReader(input);
//            parser = factory.createParser(regresreader);
//            while (parser.hasNext()) {
//                JsonParser.Event e = parser.next();
//                switch (e) {
//                    case KEY_NAME: {
//                        System.out.print("\t" + parser.getString() + " = ");
//                        break;
//                    }
//                    case VALUE_STRING: {
//                        System.out.println(parser.getString());
//                        break;
//                    }
//                }
//            }
//            System.out.println("\n Finished Generating Authentication Response.");
//            System.out.println("\n Authenticating with fido server ...");
//
//            //  test authenticate
//            JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
//                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
//                    .add("last_used_location", "Bangalore, India").
//                    build();
//            WebResource auth = service.path(Constants.FIDO_METHOD_AUTHENTICATE)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("auth_metadata", UriComponent.encode(auth_metadata.toString(), UriComponent.Type.QUERY_PARAM));
//            ClientResponse clresponse = auth.accept(MediaType.TEXT_HTML).type("application/json").post(ClientResponse.class, input);
//            String authresponse = clresponse.getEntity(String.class);
//            assertNotNull("Authentication response from the fido server is null;  ", authresponse);
//            assertNotSame("Authentication response from the fido server is empty; ", "", authresponse);
//
//            StringReader regs = new StringReader(authresponse);
//            JsonReader jr = Json.createReader(regs);
//            JsonObject authrespJson = jr.readObject();
//            jr.close();
//            String authres = authrespJson.getJsonString("Response").getString();
//            assertSame("Authentication is unsuccessfull; ", "Successfully processed sign response", authres);
//            res = true;
//
//            System.out.println("\n Response from fido server : " + authres);
//            System.out.println("\n Authentication Complete.");
//            System.out.println("\n***************** END - Authentication test (REST calls) *******************");
//        } catch (UniformInterfaceException | ClientHandlerException ex) {
//            exmsg = ex.getLocalizedMessage();
//        }
//
//        //  prompt exceptions using this assert
//        assertTrue("Exception occured during Authentication; " + exmsg, res);
//    }
//}
