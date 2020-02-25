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
// * JUnit test class that performs registration tests using REST based web
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
//import java.io.IOException;
//import java.io.StringReader;
//import java.security.InvalidAlgorithmParameterException;
//import java.security.InvalidKeyException;
//import java.security.KeyStoreException;
//import java.security.NoSuchAlgorithmException;
//import java.security.NoSuchProviderException;
//import java.security.SignatureException;
//import java.security.UnrecoverableKeyException;
//import java.security.cert.CertificateException;
//import java.security.spec.InvalidKeySpecException;
//import javax.crypto.BadPaddingException;
//import javax.crypto.IllegalBlockSizeException;
//import javax.crypto.NoSuchPaddingException;
//import javax.crypto.ShortBufferException;
//import javax.json.Json;
//import javax.json.JsonObject;
//import javax.json.JsonReader;
//import javax.json.stream.JsonParser;
//import javax.json.stream.JsonParserFactory;
//import javax.ws.rs.core.MediaType;
//import junit.framework.TestCase;
//import org.apache.commons.codec.DecoderException;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//
///**
// * JUnit test class that performs registration tests using REST based web
// * services
// */
//public class U2F_REST_RegistrationTest extends TestCase {
//
//    //  SKCE host url and port object declaration
//    private String  fidoserverURI   = Common.getConfigurationProperty("skfe.cfg.property.junit.skfe.url.rest");
//
//    //  Test user in LDAP to be used for junit tests.
//    private String  testusername   = Common.getConfigurationProperty("skfe.cfg.property.junit.skfe.testuser");
//
//    public U2F_REST_RegistrationTest(String testName) {
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
//    8888888b.                    d8b          888                     888    d8b
//    888   Y88b                   Y8P          888                     888    Y8P
//    888    888                                888                     888
//    888   d88P  .d88b.   .d88b.  888 .d8888b  888888 888d888  8888b.  888888 888  .d88b.  88888b.
//    8888888P"  d8P  Y8b d88P"88b 888 88K      888    888P"       "88b 888    888 d88""88b 888 "88b
//    888 T88b   88888888 888  888 888 "Y8888b. 888    888     .d888888 888    888 888  888 888  888
//    888  T88b  Y8b.     Y88b 888 888      X88 Y88b.  888     888  888 Y88b.  888 Y88..88P 888  888
//    888   T88b  "Y8888   "Y88888 888  88888P'  "Y888 888     "Y888888  "Y888 888  "Y88P"  888  888
//                             888
//                        Y8b d88P
//                         "Y88P"
//    *******************************************************************************/
//
//
//    /**
//     * Tests for pre-register & register methods, of class restfido.
//     * @throws java.lang.Exception
//     */
//    public void test_REST_Registration_GoodSignature() throws Exception {
//        System.out.println("\nExecuting test_REST_Registration_GoodSignature");
//
//        String exmsg = "";
//        boolean res = false;
//
//        try {
//            System.out.println("\n***************** START - Registration test (REST calls) *******************");
//
//            ClientConfig config = new DefaultClientConfig();
//            Client client = Client.create(config);
//            WebResource service = client.resource(fidoserverURI);
//
//            WebResource preRegister = service.path("/" + Constants.FIDO_METHOD_PREREGISTER)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("username", testusername);
//
//            System.out.println("\n Calling preregister ... @ " + preRegister.getURI().toString());
//            String response = preRegister.accept(MediaType.APPLICATION_JSON).get(String.class);
//            assertNotNull("Pre-registration response from the fido server is null;  ", response);
//            assertNotSame("Pre-registration response from the fido server is empty; ", "", response);
//
//            System.out.println("\n U2F registration challenge parameters :\n");
//            StringReader s = new StringReader(response);
//            JsonReader jsonReader = Json.createReader(s);
//            JsonObject responseJSON = jsonReader.readObject();
//            jsonReader.close();
//            String s1 = responseJSON.getJsonObject("Challenge").toString();
//
//            s = new StringReader(s1);
//            jsonReader = Json.createReader(s);
//            JsonObject resJsonObj = jsonReader.readObject();
//            jsonReader.close();
//
//            String s2 = resJsonObj.getJsonObject("enrollChallenges").toString();
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
//            System.out.println("\n Pre-Registration Complete.");
//            System.out.println("\n Generating Registration response...\n");
//
//            String input = null;
//            try {
//                input = SoapFidoClient.generateRegistrationResponse(s2, true);
//            } catch (NoSuchAlgorithmException |
//                    NoSuchProviderException |
//                    KeyStoreException |
//                    IOException |
//                    CertificateException |
//                    InvalidAlgorithmParameterException |
//                    InvalidKeyException |
//                    SignatureException |
//                    NoSuchPaddingException |
//                    DecoderException |
//                    IllegalBlockSizeException |
//                    BadPaddingException |
//                    ShortBufferException |
//                    UnrecoverableKeyException |
//                    InvalidKeySpecException ex) {
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
//            System.out.println("\n Finished Generating Registration Response.");
//            System.out.println("\n Registering with fido server ...");
//
//            //  test register
//            JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
//                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
//                    .add("create_location", "Sunnyvale, CA").
//                    build();
//
//            WebResource register = service.path(Constants.FIDO_METHOD_REGISTER)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("reg_metadata", UriComponent.encode(reg_metadata.toString(), UriComponent.Type.QUERY_PARAM));
//
//            System.out.println("\n Calling register ... @ " + register.getURI().toString());
//            ClientResponse clresponse = register.accept(MediaType.TEXT_HTML).type("application/json").post(ClientResponse.class, input);
//            String regresponse = clresponse.getEntity(String.class);
//            assertNotNull("Registration response from the fido server is null;  ", regresponse);
//            assertNotSame("Registration response from the fido server is empty; ", "", regresponse);
//
//            StringReader regs = new StringReader(regresponse);
//            JsonReader jr = Json.createReader(regs);
//            JsonObject regrespJson = jr.readObject();
//            jr.close();
//            String regres = regrespJson.getJsonString("Response").getString();
//            assertNotSame("Registration is unsuccessfull; ", "Successfully processed registration response", regres);
//            res = true;
//
//            System.out.println("\n Response from fido server : " + regres);
//            System.out.println("\n Registeration Complete.");
//            System.out.println("\n***************** END - Registration test (REST calls) *******************");
//
//        } catch (UniformInterfaceException | ClientHandlerException ex) {
//            exmsg = ex.getLocalizedMessage();
//        }
//
//        //  prompt exceptions using this assert
//        assertTrue("Exception occured during registration; " + exmsg, res);
//    }
//
//    public void test_REST_Registration_BadSignature() throws Exception {
//        System.out.println("\nExecuting test_REST_Registration_BadSignature");
//
//        String exmsg = "";
//        boolean res = false;
//
//        try {
//            System.out.println("\n***************** START - Registration test (REST calls) *******************");
//
//            ClientConfig config = new DefaultClientConfig();
//            Client client = Client.create(config);
//            WebResource service = client.resource(fidoserverURI);
//
//            WebResource preRegister = service.path("/" + Constants.FIDO_METHOD_PREREGISTER)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("username", testusername);
//
//            System.out.println("\n Calling preregister ... @ " + preRegister.getURI().toString());
//            String response = preRegister.accept(MediaType.APPLICATION_JSON).get(String.class);
//            assertNotNull("Pre-registration response from the fido server is null;  ", response);
//            assertNotSame("Pre-registration response from the fido server is empty; ", "", response);
//
//            System.out.println("\n U2F registration challenge parameters :\n");
//            StringReader s = new StringReader(response);
//            JsonReader jsonReader = Json.createReader(s);
//            JsonObject responseJSON = jsonReader.readObject();
//            jsonReader.close();
//            String s1 = responseJSON.getJsonObject("Challenge").toString();
//
//            s = new StringReader(s1);
//            jsonReader = Json.createReader(s);
//            JsonObject resJsonObj = jsonReader.readObject();
//            jsonReader.close();
//
//            String s2 = resJsonObj.getJsonObject("enrollChallenges").toString();
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
//            System.out.println("\n Pre-Registration Complete.");
//
//            System.out.println("\n Generating Registration response...\n");
//
//            String input = null;
//            try {
//                input = SoapFidoClient.generateRegistrationResponse(s2, false);
//            } catch (NoSuchAlgorithmException |
//                    NoSuchProviderException |
//                    KeyStoreException |
//                    IOException |
//                    CertificateException |
//                    InvalidAlgorithmParameterException |
//                    InvalidKeyException |
//                    SignatureException |
//                    NoSuchPaddingException |
//                    DecoderException |
//                    IllegalBlockSizeException |
//                    BadPaddingException |
//                    ShortBufferException |
//                    UnrecoverableKeyException |
//                    InvalidKeySpecException ex) {
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
//            System.out.println("\n Finished Generating Registration Response.");
//            System.out.println("\n Registering with fido server ...");
//
//            //  test register
//            JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
//                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
//                    .add("create_location", "Sunnyvale, CA").
//                    build();
//
//            WebResource register = service.path(Constants.FIDO_METHOD_REGISTER)
//                    .queryParam("did", "dummy_did")
//                    .queryParam("secretkey", "dummy_secretkey")
//                    .queryParam("protocol", "U2F_V2")
//                    .queryParam("reg_metadata", UriComponent.encode(reg_metadata.toString(), UriComponent.Type.QUERY_PARAM));
//
//            System.out.println("\n Calling register ... @ " + register.getURI().toString());
//            ClientResponse clresponse = register.accept(MediaType.TEXT_HTML).type("application/json").post(ClientResponse.class, input);
//            String regresponse = clresponse.getEntity(String.class);
//            assertNotNull("Registration response from the fido server is null;  ", regresponse);
//            assertNotSame("Registration response from the fido server is empty; ", "", regresponse);
//
//            StringReader regs = new StringReader(regresponse);
//            JsonReader jr = Json.createReader(regs);
//            JsonObject regrespJson = jr.readObject();
//            jr.close();
//            String regres = regrespJson.getJsonString("Response").getString();
//            assertSame("Registration is unsuccessfull; ", "Successfully processed registration response", regres);
//            res = true;
//
//            System.out.println("\n Response from fido server : " + regres);
//            System.out.println("\n Registeration Complete.");
//            System.out.println("\n***************** END - Registration test (REST calls) *******************");
//
//        } catch (UniformInterfaceException | ClientHandlerException ex) {
//            exmsg = ex.getLocalizedMessage();
//        }
//
//        //  prompt exceptions using this assert
//        assertTrue("Exception occured during registration; " + exmsg, res);
//    }
//}
