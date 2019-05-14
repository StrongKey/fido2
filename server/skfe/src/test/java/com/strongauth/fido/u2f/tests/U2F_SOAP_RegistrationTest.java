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
// * JUnit test class that performs registration tests using SOAP based web 
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
// * JUnit test class that performs registration tests using SOAP based web 
// * services
// */
//public class U2F_SOAP_RegistrationTest extends TestCase {
//    
//     
//    //  SKCE host url and port object declaration
//    private String  fidoserverURI   = skfsCommon.getConfigurationProperty("skfe.cfg.property.junit.skfe.url.soap");
//    
//    //  Test user in LDAP to be used for junit tests.
//    private String  testusername   = skfsCommon.getConfigurationProperty("skfe.cfg.property.junit.skfe.testuser");
//    private String  testskcedid = "1";
//    private String  testsvcusername = "svcfidouser";
//    private String  testsvcpassword = "Abcd1234!";
//    
//    public U2F_SOAP_RegistrationTest(String testName) {
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
////    /**
////     * Tests for pre-register & register methods, of class soapfido.
////     * @throws java.lang.Exception
////     */
////    public void test_SOAP_Registration_GoodSignature() throws Exception {
////        System.out.println("\nExecuting test_Registration_GoodSignature");
////        
////        String exmsg = "";
////        boolean res = false;
////
////        try {
////            System.out.println("\n***************** START - Registration test (SOAP calls) *******************");
////            
////            URL soapurl = new URL(fidoserverURI);
////            Soap service = new Soap(soapurl);
////            SKFEServlet port = service.getSKFEServletPort();
////            
////            System.out.println("\n Calling preregister ... @ " + fidoserverURI);
////            String response = port.preregister("", "U2F_V2", testusername);
////            assertNotNull("Pre-registration response from the fido server is null;  ", response);
////            assertNotSame("Pre-registration response from the fido server is empty; ", "", response);
////            
////            System.out.println("\n U2F registration challenge parameters :\n");
////            StringReader s = new StringReader(response);
////            JsonReader jsonReader = Json.createReader(s);
////            JsonObject responseJSON = jsonReader.readObject();
////            jsonReader.close();
////            String s1 = responseJSON.getJsonObject("Challenge").toString();
////
////            s = new StringReader(s1);
////            jsonReader = Json.createReader(s);
////            JsonObject resJsonObj = jsonReader.readObject();
////            jsonReader.close();
////
////            String s2 = resJsonObj.getJsonObject("enrollChallenges").toString();
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
////            
////            System.out.println("\n Pre-Registration Complete.");
////            System.out.println("\n Generating Registration response...\n");
////
////            String input = null;
////            try {
////                input = SoapFidoClient.generateRegistrationResponse(s2, true);
////            } catch (NoSuchAlgorithmException | 
////                    NoSuchProviderException | 
////                    KeyStoreException | 
////                    IOException | 
////                    CertificateException | 
////                    InvalidAlgorithmParameterException | 
////                    InvalidKeyException | 
////                    SignatureException | 
////                    NoSuchPaddingException | 
////                    IllegalBlockSizeException | 
////                    BadPaddingException | 
////                    ShortBufferException | 
////                    UnrecoverableKeyException | 
////                    InvalidKeySpecException ex) {
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
////            System.out.println("\n Finished Generating Registration Response.");
////            System.out.println("\n Registering with fido server ...");
////            
////            //  test register
////            JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
////                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
////                    .add("create_location", "Sunnyvale, CA").
////                    build();
////
////            String regresponse = port.register("", input);
////            assertNotNull("Registration response from the fido server is null;  ", regresponse);
////            assertNotSame("Registration response from the fido server is empty; ", "", regresponse);
////            
////            StringReader regs = new StringReader(regresponse);
////            JsonReader jr = Json.createReader(regs);
////            JsonObject regrespJson = jr.readObject();
////            jr.close();
////            String regres = regrespJson.getJsonString("Response").getString();
////            assertNotSame("Registration is unsuccessfull; ", "Successfully processed registration response", regres);
////            res = true;
////            
////            System.out.println("\n Response from fido server : " + regres);
////            System.out.println("\n Registration Complete.");
////            System.out.println("\n***************** END - Registration test (SOAP calls) *******************");
////
////        } catch (MalformedURLException ex) {
////            exmsg = ex.getLocalizedMessage();
////        }
////        
////        //  prompt exceptions using this assert
////        assertTrue("Exception occured during registration; " + exmsg, res);
////    }
////    
////    public void test_SOAP_Registration_BadSignature() throws Exception {
////        System.out.println("\nExecuting test_SOAP_Registration_BadSignature");
////        
////        String exmsg = "";
////        boolean res = false;
////
////        try {
////            System.out.println("\n***************** START - Registration test (SOAP calls) *******************");
////            
////            URL soapurl = new URL(fidoserverURI);
////            Soap service = new Soap(soapurl);
////            SKFEServlet port = service.getSKFEServletPort();
////            
////            System.out.println("\n Calling preregister ... @ " + fidoserverURI);
////            String response = port.preregister("", "U2F_V2", testusername);
////            assertNotNull("Pre-registration response from the fido server is null;  ", response);
////            assertNotSame("Pre-registration response from the fido server is empty; ", "", response);
////            
////            System.out.println("\n U2F registration challenge parameters :\n");
////            StringReader s = new StringReader(response);
////            JsonReader jsonReader = Json.createReader(s);
////            JsonObject responseJSON = jsonReader.readObject();
////            jsonReader.close();
////            String s1 = responseJSON.getJsonObject("Challenge").toString();
////
////            s = new StringReader(s1);
////            jsonReader = Json.createReader(s);
////            JsonObject resJsonObj = jsonReader.readObject();
////            jsonReader.close();
////
////            String s2 = resJsonObj.getJsonObject("enrollChallenges").toString();
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
////            System.out.println("\n Pre-Registration Complete.");
////
////            System.out.println("\n Generating Registration response...\n");
////
////            String input = null;
////            try {
////                input = SoapFidoClient.generateRegistrationResponse(s2, false);
////            } catch (NoSuchAlgorithmException | 
////                    NoSuchProviderException | 
////                    KeyStoreException | 
////                    IOException | 
////                    CertificateException | 
////                    InvalidAlgorithmParameterException | 
////                    InvalidKeyException | 
////                    SignatureException | 
////                    NoSuchPaddingException | 
////                    IllegalBlockSizeException | 
////                    BadPaddingException | 
////                    ShortBufferException | 
////                    UnrecoverableKeyException | 
////                    InvalidKeySpecException ex) {
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
////            System.out.println("\n Finished Generating Registration Response.");
////            System.out.println("\n Registering with fido server ...");
////            
////            //  test register
////            JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
////                    .add("version", "1.0") // ALWAYS since this is just the first revision of the code
////                    .add("create_location", "Sunnyvale, CA").
////                    build();
////
////            String regresponse = port.register("", input);
////            assertNotNull("Registration response from the fido server is null;  ", regresponse);
////            assertNotSame("Registration response from the fido server is empty; ", "", regresponse);
////            
////            StringReader regs = new StringReader(regresponse);
////            JsonReader jr = Json.createReader(regs);
////            JsonObject regrespJson = jr.readObject();
////            jr.close();
////            String regres = regrespJson.getJsonString("Error").getString();
////            assertEquals("Failed to process registration response", regres);
////            res = true;
////            
////            System.out.println("\n Response from fido server : " + regres);
////            System.out.println("\n Registration Complete.");
////            System.out.println("\n***************** END - Registration test (SOAP calls) *******************");
////
////        } catch (MalformedURLException ex) {
////            exmsg = ex.getLocalizedMessage();
////        }
////        
////        //  prompt exceptions using this assert
////        assertTrue("Exception occured during registration; " + exmsg, res);
////    }
//}
