<<<<<<< HEAD
///**
// * This program is free software; you can redistribute it and/or
// * modify it under the terms of the GNU Lesser General Public
// * License, as published by the Free Software Foundation and
// * available at http://www.fsf.org/licensing/licenses/lgpl.html,
// * version 2.1 or above.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * Copyright (c) 2001-2018 StrongAuth, Inc.
// *
// * $Date$
// * $Revision$
// * $Author$
// * $URL$
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
// * JUnit test class that performs authentication tests using SOAP based web 
// * services.
// * 
// */
//package com.strongauth.fido.u2f.tests;
//
//import com.strongkey.skfs.utilities.skfsCommon;
//import junit.framework.TestCase;
//import org.junit.AfterClass;
//import org.junit.BeforeClass;
//
///**
// * JUnit test class that performs authentication tests using SOAP based web 
// * services
// */
//public class U2F_SOAP_AuthenticationTest extends TestCase {
//    //  SKCE host url and port object declaration
//    private String  fidoserverURI   = skfsCommon.getConfigurationProperty("skfe.cfg.property.junit.skfe.url.soap");
//    
//    //  Test user in LDAP to be used for junit tests.
//    private String  testusername   = skfsCommon.getConfigurationProperty("skfe.cfg.property.junit.skfe.testuser");
//    private String  testskcedid = "1";
//    private String  testsvcusername = "svcfidouser";
//    private String  testsvcpassword = "Abcd1234!";
//    
//    public U2F_SOAP_AuthenticationTest(String testName) {
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
////    /**
////     * Test of preauth and authenticate methods, of class soapfido.
////     * @throws java.lang.Exception
////     */
////    public void test_SOAP_Authentication_GoodSignature() throws Exception {
////        System.out.println("\nExecuting test_SOAP_Authentication_GoodSignature");
////        
////        String exmsg = "";
////        boolean res = false;
////
////        try {
////            System.out.println("\n***************** START - Authentication test (SOAP calls) *******************");
////            
////            URL soapurl = new URL(fidoserverURI);
////            Soap service = new Soap(soapurl);
////            SKFEServlet port = service.getSKFEServletPort();
////
////            System.out.println("\n Calling preauth ... @ " + fidoserverURI);
////            String response = port.preauthenticate("", "U2F_V2", testusername);
////            assertNotNull("Pre-auth response from the fido server is null;  ", response);
////            assertNotSame("Pre-auth response from the fido server is empty; ", "", response);
////            
////            System.out.println("\n U2F authentication challenge parameters :\n");
////            StringReader s = new StringReader(response);
////            JsonReader jsonReader = Json.createReader(s);
////            JsonObject responseJSON = jsonReader.readObject();
////            jsonReader.close();
////            String s1 = responseJSON.getJsonObject("Challenge").toString();
////            s = new StringReader(s1);
////
////            jsonReader = Json.createReader(s);
////            JsonObject resJsonObj = jsonReader.readObject();
////            jsonReader.close();
////            String s2 = resJsonObj.getJsonArray("signData").toString();
////            s = new StringReader(s2);
////            JsonParserFactory factory = Json.createParserFactory(null);
////            JsonParser parser = factory.createParser(s);
////            while (parser.hasNext()) {
////                JsonParser.Event e = parser.next();
////                switch (e) {
////                    case KEY_NAME: {
////                        System.out.print("\t" + parser.getString() + " = ");
////                        break;
////                    }
////                    case VALUE_STRING: {
////                        System.out.println(parser.getString());
////                        break;
////                    }
////                }
////            }
////            System.out.println("\n Pre-Authentication Complete.");
////
////            System.out.println("\n Generating Authentication response...\n");
////
////            JsonArray ja = resJsonObj.getJsonArray("signData");
////            String s3 = ja.getJsonObject(0).toString();
////            String input = null;
////            try {
////                input = SoapFidoClient.generateAuthenticationResponse2fs(s3, true);
////            } catch (NoSuchAlgorithmException | 
////                    NoSuchProviderException | 
////                    UnsupportedEncodingException | 
////                    NoSuchPaddingException | 
////                    InvalidKeyException | 
////                    InvalidAlgorithmParameterException | 
////                    ShortBufferException | 
////                    IllegalBlockSizeException | 
////                    BadPaddingException | 
////                    InvalidKeySpecException | 
////                    SignatureException ex) {
////                System.out.println("\n Exception : " + ex.getLocalizedMessage());
////            }catch (Exception ex){
////                System.out.println("\n Exception : " + ex.getLocalizedMessage());
////            }
////            
////            StringReader regresreader = new StringReader(input);
////            parser = factory.createParser(regresreader);
////            while (parser.hasNext()) {
////                JsonParser.Event e = parser.next();
////                switch (e) {
////                    case KEY_NAME: {
////                        System.out.print("\t" + parser.getString() + " = ");
////                        break;
////                    }
////                    case VALUE_STRING: {
////                        System.out.println(parser.getString());
////                        break;
////                    }
////                }
////            }
////            
////            System.out.println("\n Finished Generating Authentication Response.");
////            System.out.println("\n nAuthenticating with fido server ...");
////            
////            //  test authenticate
////            JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
////                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
////                    .add("last_used_location", "Bangalore, India").
////                    build();
////            
////            String authresponse = port.authenticate("", input);
////            assertNotNull("Authentication response from the fido server is null;  ", authresponse);
////            assertNotSame("Authentication response from the fido server is empty; ", "", authresponse);
////        
////            StringReader regs = new StringReader(authresponse);
////            JsonReader jr = Json.createReader(regs);
////            JsonObject authrespJson = jr.readObject();
////            jr.close();
////            String authres = authrespJson.getJsonString("Response").getString();
////            assertNotSame("Authentication is unsuccessfull; ", "Successfully processed registration response", authres);
////            res = true;
////            
////            System.out.println("\n Response from fido server : " + authres);
////            System.out.println("\n Authentication Complete.");
////            System.out.println("\n***************** END - Authentication test (SOAP calls) *******************");
////
////        } catch (MalformedURLException ex) {
////            exmsg = ex.getLocalizedMessage();
////        }
////        
////        //  prompt exceptions using this assert
////        assertTrue("Exception occured during Authentication; " + exmsg, res);
////    }
////    
////    public void test_SOAP_Authentication_BadSignature() throws Exception {
////        System.out.println("\test_SOAP_Authentication_BadSignature test_Authentication_BadSignature");
////        
////        String exmsg = "";
////        boolean res = false;
////
////        try {
////            System.out.println("\n***************** START - Authentication test (SOAP calls) *******************");
////            
////            URL soapurl = new URL(fidoserverURI);
////            Soap service = new Soap(soapurl);
////            SKFEServlet port = service.getSKFEServletPort();
////
////            System.out.println("\n Calling preauth ... @ " + fidoserverURI);
////            String response = port.preauthenticate("", "U2F_V2", testusername);
////            assertNotNull("Pre-auth response from the fido server is null;  ", response);
////            assertNotSame("Pre-auth response from the fido server is empty; ", "", response);
////            
////            System.out.println("\n U2F authentication challenge parameters :\n");
////            StringReader s = new StringReader(response);
////            JsonReader jsonReader = Json.createReader(s);
////            JsonObject responseJSON = jsonReader.readObject();
////            jsonReader.close();
////            String s1 = responseJSON.getJsonObject("Challenge").toString();
////            s = new StringReader(s1);
////
////            jsonReader = Json.createReader(s);
////            JsonObject resJsonObj = jsonReader.readObject();
////            jsonReader.close();
////            String s2 = resJsonObj.getJsonArray("signData").toString();
////            s = new StringReader(s2);
////            JsonParserFactory factory = Json.createParserFactory(null);
////            JsonParser parser = factory.createParser(s);
////            while (parser.hasNext()) {
////                JsonParser.Event e = parser.next();
////                switch (e) {
////                    case KEY_NAME: {
////                        System.out.print("\t" + parser.getString() + " = ");
////                        break;
////                    }
////                    case VALUE_STRING: {
////                        System.out.println(parser.getString());
////                        break;
////                    }
////                }
////            }
////            System.out.println("\n Pre-Authentication Complete.");
////
////            System.out.println("\n Generating Authentication response...\n");
////
////            JsonArray ja = resJsonObj.getJsonArray("signData");
////            String s3 = ja.getJsonObject(0).toString();
////            String input = null;
////            try {
////                input = SoapFidoClient.generateAuthenticationResponse2fs(s3, false);
////            } catch (NoSuchAlgorithmException | 
////                    NoSuchProviderException | 
////                    UnsupportedEncodingException | 
////                    NoSuchPaddingException | 
////                    InvalidKeyException | 
////                    InvalidAlgorithmParameterException | 
////                    ShortBufferException | 
////                    IllegalBlockSizeException | 
////                    BadPaddingException | 
////                    InvalidKeySpecException | 
////                    SignatureException ex ) {
////                System.out.println("\n Exception : " + ex.getLocalizedMessage());
////            } catch (Exception ex){
////                System.out.println("\n Exception : " + ex.getLocalizedMessage());
////            }
////            
////            StringReader regresreader = new StringReader(input);
////            parser = factory.createParser(regresreader);
////            while (parser.hasNext()) {
////                JsonParser.Event e = parser.next();
////                switch (e) {
////                    case KEY_NAME: {
////                        System.out.print("\t" + parser.getString() + " = ");
////                        break;
////                    }
////                    case VALUE_STRING: {
////                        System.out.println(parser.getString());
////                        break;
////                    }
////                }
////            }
////            
////            System.out.println("\n Finished Generating Authentication Response.");
////            System.out.println("\n Authenticating with fido server ...");
////            
////            //  test authenticate
////            JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
////                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
////                    .add("last_used_location", "Bangalore, India").
////                    build();
////            
////            String authresponse = port.authenticate("", input);
////            assertNotNull("Authentication response from the fido server is null;  ", authresponse);
////            assertNotSame("Authentication response from the fido server is empty; ", "", authresponse);
////            
////            StringReader regs = new StringReader(authresponse);
////            JsonReader jr = Json.createReader(regs);
////            JsonObject authrespJson = jr.readObject();
////            jr.close();
////            String authres = authrespJson.getJsonString("Error").getString();
////            System.out.println(authres);
////            assertEquals("FIDO-ERR-0015: User signature could not be verified: {0}", authres);
////            res = true;
////            
////            System.out.println("\n Response from fido server : " + authres);
////            System.out.println("\n Authentication Complete.");
////            System.out.println("\n***************** END - Authentication test (SOAP calls) *******************");
////        } catch (MalformedURLException ex) {
////            exmsg = ex.getLocalizedMessage();
////        }
////        
////        //  prompt exceptions using this assert
////        assertTrue("Exception occured during Authentication; " + exmsg, res);
////    }
//}
=======
/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2018 StrongAuth, Inc.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 * ************************************************
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
 * ************************************************
 *
 * JUnit test class that performs authentication tests using SOAP based web 
 * services.
 * 
 */
package com.strongauth.fido.u2f.tests;

import com.strongkey.skfs.utilities.skfsCommon;
import junit.framework.TestCase;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * JUnit test class that performs authentication tests using SOAP based web 
 * services
 */
public class U2F_SOAP_AuthenticationTest extends TestCase {
    //  SKCE host url and port object declaration
    private String  fidoserverURI   = skfsCommon.getConfigurationProperty("skfe.cfg.property.junit.skfe.url.soap");
    
    //  Test user in LDAP to be used for junit tests.
    private String  testusername   = skfsCommon.getConfigurationProperty("skfe.cfg.property.junit.skfe.testuser");
    private String  testskcedid = "1";
    private String  testsvcusername = "svcfidouser";
    private String  testsvcpassword = "Abcd1234!";
    
    public U2F_SOAP_AuthenticationTest(String testName) {
        super(testName);
    }
    
    /*******************************************************************************
                        888    888     888          
                        888    888     888          
                        888    888     888          
      .d8888b   .d88b.  888888 888     888 88888b.  
      88K      d8P  Y8b 888    888     888 888 "88b 
      "Y8888b. 88888888 888    888     888 888  888 
           X88 Y8b.     Y88b.  Y88b. .d88P 888 d88P 
       88888P'  "Y8888   "Y888  "Y88888P"  88888P"  
                                           888      
                                           888      
                                           888      
     *******************************************************************************/
    /**
     * 
     * @throws Exception 
     */
    @Override
    @BeforeClass
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    /*******************************************************************************
    888                              8888888b.                                  
    888                              888  "Y88b                                 
    888                              888    888                                 
    888888  .d88b.   8888b.  888d888 888    888  .d88b.  888  888  888 88888b.  
    888    d8P  Y8b     "88b 888P"   888    888 d88""88b 888  888  888 888 "88b 
    888    88888888 .d888888 888     888    888 888  888 888  888  888 888  888 
    Y88b.  Y8b.     888  888 888     888  .d88P Y88..88P Y88b 888 d88P 888  888 
     "Y888  "Y8888  "Y888888 888     8888888P"   "Y88P"   "Y8888888P"  888  888 
     * 
     *******************************************************************************/
    /**
     * 
     * @throws Exception 
     */
    @Override
    @AfterClass
    protected void tearDown() throws Exception {
        super.tearDown();
    }
    
    /*******************************************************************************
       d8888          888    888                        888    d8b                   888    d8b                   
      d88888          888    888                        888    Y8P                   888    Y8P                   
     d88P888          888    888                        888                          888                          
    d88P 888 888  888 888888 88888b.   .d88b.  88888b.  888888 888  .d8888b  8888b.  888888 888  .d88b.  88888b.  
   d88P  888 888  888 888    888 "88b d8P  Y8b 888 "88b 888    888 d88P"        "88b 888    888 d88""88b 888 "88b 
  d88P   888 888  888 888    888  888 88888888 888  888 888    888 888      .d888888 888    888 888  888 888  888 
 d8888888888 Y88b 888 Y88b.  888  888 Y8b.     888  888 Y88b.  888 Y88b.    888  888 Y88b.  888 Y88..88P 888  888 
d88P     888  "Y88888  "Y888 888  888  "Y8888  888  888  "Y888 888  "Y8888P "Y888888  "Y888 888  "Y88P"  888  888 
                                                     
    *******************************************************************************/

    
//    /**
//     * Test of preauth and authenticate methods, of class soapfido.
//     * @throws java.lang.Exception
//     */
//    public void test_SOAP_Authentication_GoodSignature() throws Exception {
//        System.out.println("\nExecuting test_SOAP_Authentication_GoodSignature");
//        
//        String exmsg = "";
//        boolean res = false;
//
//        try {
//            System.out.println("\n***************** START - Authentication test (SOAP calls) *******************");
//            
//            URL soapurl = new URL(fidoserverURI);
//            Soap service = new Soap(soapurl);
//            SKFEServlet port = service.getSKFEServletPort();
//
//            System.out.println("\n Calling preauth ... @ " + fidoserverURI);
//            String response = port.preauthenticate("", "U2F_V2", testusername);
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
//                    NoSuchPaddingException | 
//                    InvalidKeyException | 
//                    InvalidAlgorithmParameterException | 
//                    ShortBufferException | 
//                    IllegalBlockSizeException | 
//                    BadPaddingException | 
//                    InvalidKeySpecException | 
//                    SignatureException ex) {
//                System.out.println("\n Exception : " + ex.getLocalizedMessage());
//            }catch (Exception ex){
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
//            
//            System.out.println("\n Finished Generating Authentication Response.");
//            System.out.println("\n nAuthenticating with fido server ...");
//            
//            //  test authenticate
//            JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
//                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
//                    .add("last_used_location", "Bangalore, India").
//                    build();
//            
//            String authresponse = port.authenticate("", input);
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
//            System.out.println("\n***************** END - Authentication test (SOAP calls) *******************");
//
//        } catch (MalformedURLException ex) {
//            exmsg = ex.getLocalizedMessage();
//        }
//        
//        //  prompt exceptions using this assert
//        assertTrue("Exception occured during Authentication; " + exmsg, res);
//    }
//    
//    public void test_SOAP_Authentication_BadSignature() throws Exception {
//        System.out.println("\test_SOAP_Authentication_BadSignature test_Authentication_BadSignature");
//        
//        String exmsg = "";
//        boolean res = false;
//
//        try {
//            System.out.println("\n***************** START - Authentication test (SOAP calls) *******************");
//            
//            URL soapurl = new URL(fidoserverURI);
//            Soap service = new Soap(soapurl);
//            SKFEServlet port = service.getSKFEServletPort();
//
//            System.out.println("\n Calling preauth ... @ " + fidoserverURI);
//            String response = port.preauthenticate("", "U2F_V2", testusername);
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
//                    NoSuchPaddingException | 
//                    InvalidKeyException | 
//                    InvalidAlgorithmParameterException | 
//                    ShortBufferException | 
//                    IllegalBlockSizeException | 
//                    BadPaddingException | 
//                    InvalidKeySpecException | 
//                    SignatureException ex ) {
//                System.out.println("\n Exception : " + ex.getLocalizedMessage());
//            } catch (Exception ex){
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
//            
//            System.out.println("\n Finished Generating Authentication Response.");
//            System.out.println("\n Authenticating with fido server ...");
//            
//            //  test authenticate
//            JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
//                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
//                    .add("last_used_location", "Bangalore, India").
//                    build();
//            
//            String authresponse = port.authenticate("", input);
//            assertNotNull("Authentication response from the fido server is null;  ", authresponse);
//            assertNotSame("Authentication response from the fido server is empty; ", "", authresponse);
//            
//            StringReader regs = new StringReader(authresponse);
//            JsonReader jr = Json.createReader(regs);
//            JsonObject authrespJson = jr.readObject();
//            jr.close();
//            String authres = authrespJson.getJsonString("Error").getString();
//            System.out.println(authres);
//            assertEquals("FIDO-ERR-0015: User signature could not be verified: {0}", authres);
//            res = true;
//            
//            System.out.println("\n Response from fido server : " + authres);
//            System.out.println("\n Authentication Complete.");
//            System.out.println("\n***************** END - Authentication test (SOAP calls) *******************");
//        } catch (MalformedURLException ex) {
//            exmsg = ex.getLocalizedMessage();
//        }
//        
//        //  prompt exceptions using this assert
//        assertTrue("Exception occured during Authentication; " + exmsg, res);
//    }
}
>>>>>>> adding old v1 api back to the fido2 server
