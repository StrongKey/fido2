/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * The implementation to the SFAECO's eCommerce REST webservices interface.
 */

package com.strongkey.sfaeco.web;

import com.strongkey.sfaeco.txbeans.addUserLocal;
import com.strongkey.sfaeco.txbeans.callFidoServiceLocal;
import com.strongkey.sfaeco.txbeans.extractFidoPayloadLocal;
import com.strongkey.sfaeco.txbeans.getUserTransactionLocal;
import com.strongkey.sfaeco.txbeans.updateUserTransactionLocal;
import com.strongkey.sfaeco.txbeans.verifyFidoServiceRequestLocal;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class SfaecoRestServlet implements SfaecoRestInterface {

    // Local resources
    private final ExecutorService executorService = Executors.newCachedThreadPool();
    private final String  classname = "SfaecoRestServlet";
    
    // EJBs used by this servlet
    private final addUserLocal                       adduser;
    private final callFidoServiceLocal               callfidosvc;
    private final verifyFidoServiceRequestLocal      verifyfidoreq;
    private final extractFidoPayloadLocal            extractpayload;
    private final updateUserTransactionLocal         updateutx;
    private final getUserTransactionLocal            getUserTxs;

    // Constructor
    public SfaecoRestServlet() {
        adduser       = lookupAddUserLocal();
        callfidosvc   = lookupCallFidoServiceLocal();
        verifyfidoreq = lookupVerifyFidoServiceRequestLocal();
        extractpayload= lookupExtractFidoPayloadLocal();
        updateutx     = lookupUpdateUserTransactionLocal();
        getUserTxs = lookupgetUserTransactionLocal();

    }
    
    // Basic ping method to check if the servlet is alive
    @Override
    public String ping() {
        return "ok";
    }
    
/***********************************************************************************
                          d8b          888                     888     888                           
                          Y8P          888                     888     888                           
                                       888                     888     888                           
888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888 888     888 .d8888b   .d88b.  888d888 
888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"   888     888 88K      d8P  Y8b 888P"   
888     88888888 888  888 888 "Y8888b. 888    88888888 888     888     888 "Y8888b. 88888888 888     
888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888     Y88b. .d88P      X88 Y8b.     888     
888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888      "Y88888P"   88888P'  "Y8888  888     
                      888                                                                            
                 Y8b d88P                                                                            
                  "Y88P"                                                                  
***********************************************************************************/    
    /**
     * This operation registers a new user into the SFAECO eCommerce system.
     * 
     * Currently having a problem with returning a JsonObject - the client-side 
     * keeps barfing. Moving forward by returning a String for now. Will figure 
     * this out later.@param input InputStream with the JsonObject containing 
     * user information.
     * 
     * @param input InputStream containing all the input parameters
     * @return String containing the Json output registered user.
     */
    
//    @Override
//    public void registerUser(@Suspended final AsyncResponse asyncResponse, final InputStream input) {
//        executorService.submit(() -> {
//            asyncResponse.resume(doRegisterUser(input));
//        });
//    }
    
    @Override
    @SuppressWarnings("null")
//    public String doRegisterUser(InputStream input) {
    public String registerUser(InputStream input) {

        String methodname = "registerUser";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        String did = jo.getString(Constants.JSON_KEY_DID);
        JsonObject userdetails = jo.getJsonObject(Constants.JSON_KEY_USER);
        
        // Entry log
        String txid = Common.nextTxid();
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, did, txid);
        
        if (did == null) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1001", "DID; Invalid request");
            return Common.jsonError(classname, methodname, "SFAECO-ERR-1001", "Invalid request - DID is NULL").toString();
        }
        
        if (userdetails == null) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1001", "User Details; Invalid request");
            return Common.jsonError(classname, methodname, "SFAECO-ERR-1001", "Invalid request - User Details is NULL").toString();
        }
       
        // Add user details to the system
        JsonObject response = adduser.execute(Short.parseShort(did), Constants.JSON_KEY_NEW_USER_REGISTRATION, userdetails.toString(), txid);
        Common.log(Level.INFO, "SFAECO-MSG-3001", response);
        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, did, txid, timein);
        return response.toString();
    }
    
/*********************************************************************************
         d8b                   8888888888 d8b      888           .d8888b.                    
         Y8P                   888        Y8P      888          d88P  Y88b                   
                               888                 888          Y88b.                        
88888b.  888 88888b.   .d88b.  8888888    888  .d88888  .d88b.   "Y888b.   888  888  .d8888b 
888 "88b 888 888 "88b d88P"88b 888        888 d88" 888 d88""88b     "Y88b. 888  888 d88P"    
888  888 888 888  888 888  888 888        888 888  888 888  888       "888 Y88  88P 888      
888 d88P 888 888  888 Y88b 888 888        888 Y88b 888 Y88..88P Y88b  d88P  Y8bd8P  Y88b.    
88888P"  888 888  888  "Y88888 888        888  "Y88888  "Y88P"   "Y8888P"    Y88P    "Y8888P 
888                        888                                                               
888                   Y8b d88P                                                               
888                    "Y88P"                                                                
*********************************************************************************/
    
    /**
     * Pings the FIDO service to see if it is alive
     * @param input String with a Json object containing service credential 
     * information
     * @return String
     */
    @Override
    public String pingFidoService(InputStream input) {
        String methodname = "pingFidoService";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "InputStream for Fido Service: " + jo.toString());
        
        // Entry log
        String txid = Common.nextTxid();
        JsonObject sfaecofidoinput = jo.getJsonObject(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT);
        int did = sfaecofidoinput.getInt(Constants.JSON_KEY_DID);
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        // Not bothering to authenticate request - assemble FIDO service request
        // TODO: Move this to the callFidoService EJB
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, did))
            .build();
        
        // Call the Fido service
        String fidoresponse = callfidosvc.execute((Short.valueOf(String.valueOf(did))), Constants.FIDO_SERVICE.PING, fidoInput, txid);
        if (fidoresponse == null) {
            Common.log(Level.INFO, "SFAECO-ERR-3006", "FIDO Service is not responding");
            Common.exitLog(Level.INFO, "SFAECO-MSG-3006", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3006", "FIDO Service is not responding").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-3006", fidoresponse);
        return fidoresponse;
    }
    
/*********************************************************************************
                  888    8888888b.                              .d8888b.  888               888 
                  888    888   Y88b                            d88P  Y88b 888               888 
                  888    888    888                            888    888 888               888 
 .d88b.   .d88b.  888888 888   d88P  .d88b.   .d88b.  88888b.  888        88888b.   8888b.  888 
d88P"88b d8P  Y8b 888    8888888P"  d8P  Y8b d88P"88b 888 "88b 888        888 "88b     "88b 888 
888  888 88888888 888    888 T88b   88888888 888  888 888  888 888    888 888  888 .d888888 888 
Y88b 888 Y8b.     Y88b.  888  T88b  Y8b.     Y88b 888 888  888 Y88b  d88P 888  888 888  888 888 
 "Y88888  "Y8888   "Y888 888   T88b  "Y8888   "Y88888 888  888  "Y8888P"  888  888 "Y888888 888 
     888                                          888                                           
Y8b d88P                                     Y8b d88P                                           
 "Y88P"                                       "Y88P"                                                       
*********************************************************************************/
    
    /**
     * Verifies SFAECO credential information of user and mobile device before
     * making FIDO preregistration request
     * @param input JsonObject with the following parameters
     * @return String response
     */
    @Override
    public String getFidoRegistrationChallenge(InputStream input) {
        String methodname = "getFidoRegistrationChallenge";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "InputStream for Fido Service: " + jo.toString());
        
        // Entry log
        String txid = Common.nextTxid();
        JsonObject sfaecofidoinput = jo.getJsonObject(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT);
        int did = sfaecofidoinput.getInt(Constants.JSON_KEY_DID);
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        // Verify the user's credential and device identity for authorization
        JsonObject response = verifyfidoreq.execute(Short.valueOf(Integer.toString(did)), sfaecofidoinput, txid);
        if (response.containsKey("error")) {
            Common.log(Level.WARNING, "SFAECO-ERR-3007", sfaecofidoinput);
            Common.exitLog(Level.INFO, "SFAECO-MSG-3007", classname, String.valueOf(did), txid, timein);
            String k = response.getString("k");
            String v = response.getString("v");
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3007", "User, Device or Service Verification Error:\n" + "k: " +k+ "\nv: " +v).toString();
        }
        
        // Extract the "payload" JsonObject for relay to the FIDO server
        JsonObject payload = extractpayload.execute(Short.valueOf(Integer.toString(did)), sfaecofidoinput, response, txid);
        if (payload == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-3008", sfaecofidoinput);
            Common.exitLog(Level.INFO, "SFAECO-MSG-3008", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3008", "FIDO Service Payload Error").toString();
        }
        
        // Assemble the FIDO service request
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, did))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, payload.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD))
            .build();
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDO Service Input: " + fidoInput.toString());
        
        // Call the Fido service
        String fidoresponse = callfidosvc.execute((Short.valueOf(String.valueOf(did))), Constants.FIDO_SERVICE.PREREGISTER, fidoInput, txid);
        if (fidoresponse == null) {
            Common.log(Level.INFO, "SFAECO-ERR-3009", "FIDO Service Error");
            Common.exitLog(Level.INFO, "SFAECO-MSG-3009", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3009", "FIDO Service Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-3006", fidoresponse);
        return fidoresponse;
    }

/*********************************************************************************
                          d8b          888                     
                          Y8P          888                     
                                       888                     
888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888 
888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"   
888     88888888 888  888 888 "Y8888b. 888    88888888 888     
888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888     
888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888     
                      888                                      
                 Y8b d88P                                      
                  "Y88P"                                       
*********************************************************************************/    
    /**
     * Verifies SFAECO credential information of user and mobile device before
     * making a FIDO registration request
     * @param input JsonObject with the following parameters
     * @return String response
     */
    @Override
    public String registerFidoKey(InputStream input) {
        String methodname = "registerFidoKey";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "InputStream for Fido Register Service: " + jo.toString());
        
        // Entry log
        String txid = Common.nextTxid();
        JsonObject fidoinput = jo.getJsonObject(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT);
        int did = fidoinput.getInt(Constants.JSON_KEY_DID);
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "Input Json for Fido Register Service: " + fidoinput.toString());
        
        // Verify the user's credential and device identity for authorization
        JsonObject userinfo = verifyfidoreq.execute(Short.valueOf(Integer.toString(did)), fidoinput, txid);
        if (userinfo.containsKey("error")) {
            Common.log(Level.WARNING, "SFAECO-ERR-3007", fidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3007", classname, String.valueOf(did), txid, timein);
            String k = userinfo.getString("k");
            String v = userinfo.getString("v");
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3007", "User, Device or Service Verification Error:\n" + "k: " +k+ "\nv: " +v).toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-1000", "User Info for Fido Register Service: " + userinfo.toString());
        
        // Extract the "payload" JsonObject for relay to the FIDO server
        JsonObject payload = extractpayload.execute(Short.valueOf(Integer.toString(did)), fidoinput, userinfo, txid);
        if (payload == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-3008", fidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3008", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3008", "FIDO Register Service Payload Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-1000", "payloadWithMetadata for Fido Register Service: " + payload.toString());
        
        // Assemble the FIDO service request
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, did))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, payload)
            .build();
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDO Register Service Input with Payload: " + fidoInput.toString());
        
        // Call the Fido service
        String fidoresponse = callfidosvc.execute((Short.valueOf(String.valueOf(did))), Constants.FIDO_SERVICE.REGISTER, fidoInput, txid);
        if (fidoresponse == null) {
            Common.log(Level.INFO, "SFAECO-ERR-3009", "FIDO Register Service Error");
            Common.exitLog(Level.INFO, "SFAECO-ERR-3009", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3009", "FIDO Register Service Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-3006", fidoresponse);
        return fidoresponse;
    }

/***********************************************************************************
                  888           d8888          888    888                .d8888b.  888               888 
                  888          d88888          888    888               d88P  Y88b 888               888 
                  888         d88P888          888    888               888    888 888               888 
 .d88b.   .d88b.  888888     d88P 888 888  888 888888 88888b.  88888b.  888        88888b.   8888b.  888 
d88P"88b d8P  Y8b 888       d88P  888 888  888 888    888 "88b 888 "88b 888        888 "88b     "88b 888 
888  888 88888888 888      d88P   888 888  888 888    888  888 888  888 888    888 888  888 .d888888 888 
Y88b 888 Y8b.     Y88b.   d8888888888 Y88b 888 Y88b.  888  888 888  888 Y88b  d88P 888  888 888  888 888 
 "Y88888  "Y8888   "Y888 d88P     888  "Y88888  "Y888 888  888 888  888  "Y8888P"  888  888 "Y888888 888 
     888                                                                                                 
Y8b d88P                                                                                                 
 "Y88P"                                                                                                  
*********************************************************************************/
 
    /**
     * Verifies SFAECO credential information of user and mobile device before
     * making a FIDO preauthentication request
     * @param input JsonObject with the following parameters
     * @return String response
     */
    @Override
    public String getFidoAuthenticationChallenge(InputStream input) {
        String methodname = "getFidoAuthenticationChallenge";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "InputStream for Fido Service: " + jo.toString());
        
        // Entry log
        String txid = Common.nextTxid();
        JsonObject sfaecofidoinput = jo.getJsonObject(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT);
        int did = sfaecofidoinput.getInt(Constants.JSON_KEY_DID);
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        // Verify the user's credential and device identity for authorization
        JsonObject userinfo = verifyfidoreq.execute(Short.valueOf(Integer.toString(did)), sfaecofidoinput, txid);
        if (userinfo.containsKey("error")) {
            Common.log(Level.WARNING, "SFAECO-ERR-3007", sfaecofidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3007", classname, String.valueOf(did), txid, timein);
            String k = userinfo.getString("k");
            String v = userinfo.getString("v");
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3007", "User, Device or Service Verification Error:\n" + "k: " +k+ "\nv: " +v).toString();
        }
        
        // Extract the "payload" JsonObject for relay to the FIDO server
        JsonObject payload = extractpayload.execute(Short.valueOf(Integer.toString(did)), sfaecofidoinput, userinfo, txid);
        if (payload == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-3008", sfaecofidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3008", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3008", "FIDO Service Payload Error").toString();
        }
        
        // Assemble the FIDO service request
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, did))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, payload.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD))
            .build();
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDO Service Input: " + fidoInput.toString());
        
        // Call the Fido service
        String fidoresponse = callfidosvc.execute((Short.valueOf(String.valueOf(did))), Constants.FIDO_SERVICE.PREAUTHENTICATE, fidoInput, txid);
        if (fidoresponse == null) {
            Common.log(Level.INFO, "SFAECO-ERR-3009", "FIDO Service Error");
            Common.exitLog(Level.INFO, "SFAECO-ERR-3009", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3009", "FIDO Service Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-3006", fidoresponse);
        return fidoresponse;
    }

/*********************************************************************************
                  888    888                        888    d8b                   888             
                  888    888                        888    Y8P                   888             
                  888    888                        888                          888             
 8888b.  888  888 888888 88888b.   .d88b.  88888b.  888888 888  .d8888b  8888b.  888888  .d88b.  
    "88b 888  888 888    888 "88b d8P  Y8b 888 "88b 888    888 d88P"        "88b 888    d8P  Y8b 
.d888888 888  888 888    888  888 88888888 888  888 888    888 888      .d888888 888    88888888 
888  888 Y88b 888 Y88b.  888  888 Y8b.     888  888 Y88b.  888 Y88b.    888  888 Y88b.  Y8b.     
"Y888888  "Y88888  "Y888 888  888  "Y8888  888  888  "Y888 888  "Y8888P "Y888888  "Y888  "Y8888                           
*********************************************************************************/    
    /**
     * Verifies SFAECO credential information of user and mobile device before
     * making a FIDO authentication request
     * @param input JsonObject with the following parameters
     * @return String response
     */
    @Override
    public String authenticateFidoKey(InputStream input) {
        String methodname = "authenticateFidoKey";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "InputStream for Fido Authenticate Service: " + jo.toString());
        
        // Entry log
        String txid = Common.nextTxid();
        JsonObject fidoinput = jo.getJsonObject(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT);
        int did = fidoinput.getInt(Constants.JSON_KEY_DID);
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "Input Json for Fido Authenticate Service: " + fidoinput.toString());
        
        // Verify the user's credential and device identity for authorization
        JsonObject userinfo = verifyfidoreq.execute(Short.valueOf(Integer.toString(did)), fidoinput, txid);
        if (userinfo.containsKey("error")) {
            Common.log(Level.WARNING, "SFAECO-ERR-3007", fidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3007", classname, String.valueOf(did), txid, timein);
            String k = userinfo.getString("k");
            String v = userinfo.getString("v");
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3007", "User, Device or Service Verification Error:\n" + "k: " +k+ "\nv: " +v).toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-1000", "User Info for Fido Authenticate Service: " + userinfo.toString());
        
        // Extract the "payload" JsonObject for relay to the FIDO server
        JsonObject payload = extractpayload.execute(Short.valueOf(Integer.toString(did)), fidoinput, userinfo, txid);
        if (payload == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-3008", fidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3008", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3008", "FIDO Authenticate Service Payload Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-1000", "payloadWithMetadata for Fido Authenticate Service: " + payload.toString());
        
        // Assemble the FIDO service request
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, did))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, payload)
            .build();
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDO Authenticate Service Input with Payload: " + fidoInput.toString());
        
        // Call the Fido service
        String fidoresponse = callfidosvc.execute((Short.valueOf(String.valueOf(did))), Constants.FIDO_SERVICE.AUTHENTICATE, fidoInput, txid);
        if (fidoresponse == null) {
            Common.log(Level.INFO, "SFAECO-ERR-3009", "FIDO Authenticate Service Error");
            Common.exitLog(Level.INFO, "SFAECO-ERR-3009", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3009", "FIDO Authenticate Service Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-3006", fidoresponse);
        return fidoresponse;
    }

    @Override
    public String deregisterFidoKey(InputStream input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String getFidoKeysInfo(InputStream input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String updateFidoKeyInfo(InputStream input) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
/*********************************************************************************
                  888           d8888          888    888                .d8888b.  888               888 
                  888          d88888          888    888               d88P  Y88b 888               888 
                  888         d88P888          888    888               888    888 888               888 
 .d88b.   .d88b.  888888     d88P 888 888  888 888888 88888b.  88888888 888        88888b.   8888b.  888 
d88P"88b d8P  Y8b 888       d88P  888 888  888 888    888 "88b    d88P  888        888 "88b     "88b 888 
888  888 88888888 888      d88P   888 888  888 888    888  888   d88P   888    888 888  888 .d888888 888 
Y88b 888 Y8b.     Y88b.   d8888888888 Y88b 888 Y88b.  888  888  d88P    Y88b  d88P 888  888 888  888 888 
 "Y88888  "Y8888   "Y888 d88P     888  "Y88888  "Y888 888  888 88888888  "Y8888P"  888  888 "Y888888 888 
     888                                                                                                 
Y8b d88P                                                                                                 
 "Y88P"                                                                                                  
*********************************************************************************/
    /**
     * Verifies SFAECO credential information of user and mobile device before
     * making a FIDO preauthentication request
     * @param input JsonObject with the following parameters
     * @return String response
     */
    @Override
    public String getFidoAuthorizationChallenge(InputStream input) {
        String methodname = "getFidoAuthorizationChallenge";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "InputStream for Fido Service: " + jo.toString());
        
        // Entry log
        String txid = Common.nextTxid();
        JsonObject sfaecofidoinput = jo.getJsonObject(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT);
        int did = sfaecofidoinput.getInt(Constants.JSON_KEY_DID);
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        // Verify the user's credential and device identity for authorization
        JsonObject userinfo = verifyfidoreq.execute(Short.valueOf(Integer.toString(did)), sfaecofidoinput, txid);
        if (userinfo.containsKey("error")) {
            Common.log(Level.WARNING, "SFAECO-ERR-3007", sfaecofidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3007", classname, String.valueOf(did), txid, timein);
            String k = userinfo.getString("k");
            String v = userinfo.getString("v");
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3007", "User, Device or Service Verification Error:\n" + "k: " +k+ "\nv: " +v).toString();
        }
        
        // Extract the "payload" JsonObject for relay to the FIDO server
        JsonObject payload = extractpayload.execute(Short.valueOf(Integer.toString(did)), sfaecofidoinput, userinfo, txid);
        if (payload == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-3008", sfaecofidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3008", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3008", "FIDO Service Payload Error").toString();
        }
        
        // Assemble the FIDO service request
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, did))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, payload.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD))
            .build();
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDO Service Input: " + fidoInput.toString());
        
        // Call the Fido service
        String fidoresponse = callfidosvc.execute((Short.valueOf(String.valueOf(did))), Constants.FIDO_SERVICE.PREAUTHORIZE, fidoInput, txid);
        if (fidoresponse == null) {
            Common.log(Level.INFO, "SFAECO-ERR-3009", "FIDO Service Error");
            Common.exitLog(Level.INFO, "SFAECO-ERR-3009", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3009", "FIDO Service Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-3006", fidoresponse);
        return fidoresponse;
    }

/*********************************************************************************
                  888    888                       d8b                   
                  888    888                       Y8P                   
                  888    888                                             
 8888b.  888  888 888888 88888b.   .d88b.  888d888 888 88888888  .d88b.  
    "88b 888  888 888    888 "88b d88""88b 888P"   888    d88P  d8P  Y8b 
.d888888 888  888 888    888  888 888  888 888     888   d88P   88888888 
888  888 Y88b 888 Y88b.  888  888 Y88..88P 888     888  d88P    Y8b.     
"Y888888  "Y88888  "Y888 888  888  "Y88P"  888     888 88888888  "Y8888  
*********************************************************************************/
    /** 
     * Authorizes a business transaction with a FIDO digital signature
     * @param input JsonObject with the following parameters
     * @return String response
     */
    @Override
    public String authorizeFidoTransaction(InputStream input) {
        String methodname = "authorizeFidoTransaction";
        
        // Retrieve webservice parameters
        JsonObject jo = Common.inputstreamToJson(input);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "InputStream for Fido Authorize Service: " + jo.toString());
        
        // Entry log
        String txid = Common.nextTxid();
        JsonObject fidoinput = jo.getJsonObject(Constants.JSON_KEY_SACL_FIDO_SERVICE_INPUT);
        int did = fidoinput.getInt(Constants.JSON_KEY_DID);
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "Input Json for Fido Authorize Service: " + fidoinput.toString());
        
        // Verify the user's credential and device identity for authorization
        JsonObject userinfo = verifyfidoreq.execute(Short.valueOf(Integer.toString(did)), fidoinput, txid);
        if (userinfo.containsKey("error")) {
            Common.log(Level.WARNING, "SFAECO-ERR-3007", fidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3007", classname, String.valueOf(did), txid, timein);
            String k = userinfo.getString("k");
            String v = userinfo.getString("v");
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3007", "User, Device or Service Verification Error:\n" + "k: " +k+ "\nv: " +v).toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-1000", "User Info for Fido Authorize Service: " + userinfo.toString());
        
        // Extract the "payload" JsonObject for relay to the FIDO server; also create
        // create entry in UserTransactions table, generate unique TXID, add some metadata
        JsonObject payload = extractpayload.execute(Short.valueOf(Integer.toString(did)), fidoinput, userinfo, txid);
        if (payload == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-3008", fidoinput);
            Common.exitLog(Level.INFO, "SFAECO-ERR-3008", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3008", "FIDO Authorize Service Payload Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-1000", "payloadWithMetadata for Fido Authorize Service: " + payload.toString());
        
        // Assemble the FIDO service request
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, did))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, payload)
            .build();
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDO Authorize Service Input with Payload: " + fidoInput.toString());
        
        // Call the Fido service
        String fidoresponse = callfidosvc.execute((Short.valueOf(String.valueOf(did))), Constants.FIDO_SERVICE.AUTHORIZE, fidoInput, txid);
        if (fidoresponse == null) {
            Common.log(Level.INFO, "SFAECO-ERR-3009", "FIDO Authorize Service Error");
            Common.exitLog(Level.INFO, "SFAECO-ERR-3009", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-3009", "FIDO Authorize Service Error").toString();
        }
        Common.log(Level.INFO, "SFAECO-MSG-3006", fidoresponse);
        
        // Update authorized transaction details in the UserTransactions table
        JsonObject farjson = updateutx.updateTransactionDetails((Short.valueOf(String.valueOf(did))), 
                userinfo.getJsonNumber(Constants.JSON_KEY_UID).longValue(), fidoresponse, txid);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDOAuthenticatorReference PrimaryKey: " + farjson);
        
        // Return response from FIDO server to application
        return fidoresponse;
    }
    
      @Override
    public String getUserTransations() {
        String response =getUserTxs.getAll();
          System.out.println("Response = " + response);
          return response;
    }
    
    
    /*********************************************************************************
                    d8b 888      888                        888                        
                    Y8P 888      888                        888                        
                        888      888                        888                        
         .d88b.    8888 88888b.  888       .d88b.   .d88b.  888  888 888  888 88888b.  
        d8P  Y8b   "888 888 "88b 888      d88""88b d88""88b 888 .88P 888  888 888 "88b 
        88888888    888 888  888 888      888  888 888  888 888888K  888  888 888  888 
        Y8b.        888 888 d88P 888      Y88..88P Y88..88P 888 "88b Y88b 888 888 d88P 
         "Y8888     888 88888P"  88888888  "Y88P"   "Y88P"  888  888  "Y88888 88888P"  
                    888                                                       888      
                   d88P                                                       888      
                 888P"                                                        888  
     *********************************************************************************/
    
    // Add User EJB
    private addUserLocal lookupAddUserLocal() {
        try {
            Context c = new InitialContext();
            return (addUserLocal) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/addUser!com.strongkey.sfaeco.txbeans.addUserLocal");
        } catch (NamingException ne) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1000", ne.getCause().getLocalizedMessage());
            throw new RuntimeException(ne);
        }
    }
    
    private callFidoServiceLocal lookupCallFidoServiceLocal() {
        try {
            Context c = new InitialContext();
            return (callFidoServiceLocal) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/callFidoService!com.strongkey.sfaeco.txbeans.callFidoServiceLocal");
        } catch (NamingException ne) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1000", ne.getCause().getLocalizedMessage());
            throw new RuntimeException(ne);
        }
    }
    
    private verifyFidoServiceRequestLocal lookupVerifyFidoServiceRequestLocal() {
        try {
            Context c = new InitialContext();
            return (verifyFidoServiceRequestLocal) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/verifyFidoServiceRequest!com.strongkey.sfaeco.txbeans.verifyFidoServiceRequestLocal");
        } catch (NamingException ne) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1000", ne.getCause().getLocalizedMessage());
            throw new RuntimeException(ne);
        }
    }
    
    private extractFidoPayloadLocal lookupExtractFidoPayloadLocal() {
        try {
            Context c = new InitialContext();
            return (extractFidoPayloadLocal) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/extractFidoPayload!com.strongkey.sfaeco.txbeans.extractFidoPayloadLocal");
        } catch (NamingException ne) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1000", ne.getCause().getLocalizedMessage());
            throw new RuntimeException(ne);
        }
    }

    private updateUserTransactionLocal lookupUpdateUserTransactionLocal() {
         try {
            Context c = new InitialContext();
            return (updateUserTransactionLocal) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/updateUserTransaction!com.strongkey.sfaeco.txbeans.updateUserTransactionLocal");
        } catch (NamingException ne) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1000", ne.getCause().getLocalizedMessage());
            throw new RuntimeException(ne);
        }
    }
     private getUserTransactionLocal lookupgetUserTransactionLocal() {
        try {
            Context c = new InitialContext();
            return (getUserTransactionLocal) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/getUserTransaction!com.strongkey.sfaeco.txbeans.getUserTransactionLocal");
        } catch (NamingException ne) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1000", ne.getCause().getLocalizedMessage());
            throw new RuntimeException(ne);
        }
    }
}
