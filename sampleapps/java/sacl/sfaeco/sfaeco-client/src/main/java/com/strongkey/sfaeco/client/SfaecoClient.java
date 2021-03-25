/*
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
 * A REST client to test the REST webservice to interact with the sample
 * SACL FIDO App's eCommerce application
 */

package com.strongkey.sfaeco.client;

import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

public class SfaecoClient {
    
    public static final String RS_PARAM_DID = "did";
    public static final String RS_PARAM_SERVICE_CREDENTIAL = "svccred";
    public static final String RS_PARAM_PASSWORD = "password";
    public static final String RS_PARAM_USER_DETAILS = "User";
    
    private final WebTarget webTarget;
    private final Client client;
    private static String FQDN_PORT;
    private static final String SERVLET_URI = "/sfaeco-web/rest";
    private static String BASE_URI;

    private static final String USAGE = "\nUSAGE: java -cp CLASSPATH com.strongkey.sfaeco.client.SfaecoClient https://<host:port> <did> <svncred> <svcpassword> <OPERATION> [parameters ....]\n"
                    + "where the operation and its parameters are:\n\n"
                    + "         RU  <username> <givenname> <familyname> <email> <mobilenumber> -- Register new User\n"
                    + "         PR  ------------------------------------------------------------- FIDO Preregister\n";
    
    public static void main(String[] args) {
        
        if (args.length < 4) {
            System.err.println(USAGE);
            return;
        }
        
        // Get all parameters into variables validateAndroidKeystore
        FQDN_PORT = args[0];
        BASE_URI = FQDN_PORT.concat(SERVLET_URI);
        String did = args[1];
        String svccred = args[2];
        String svcpassword = args[3];
        String operation = args[4];
        
        // Constructor
        SfaecoClient resource = new SfaecoClient();
        
        // Operation
        JsonObject input;
        switch (operation) {
            case "RU":
                String username = args[5];
                String givenName = args[6];
                String familyName = args[7];
                String email = args[8];
                String mobile = args[9];
                
                System.out.println("\nParameters so far: \n\n"
                + "\thost: " + args[0] +'\n'
                + "\tdid: "  + args[1] +'\n'
                + "\tsvccred: "  + args[2] +'\n'
                + "\tsvcpassword: "  + args[3] +'\n'
                + "\toperation: "  + args[4] + '\n'
                + "\tusername: "  + args[5] +'\n'
                + "\tgivename: "  + args[6] +'\n'
                + "\tfamilyname: "  + args[7] +'\n'
                + "\temail: "  + args[8] +'\n'
                + "\tmobilenbr: "  + args[9] +'\n');

                input = Json.createObjectBuilder()
                .add(RS_PARAM_DID, did)
                .add(RS_PARAM_USER_DETAILS, Json.createObjectBuilder()
                        .add(Constants.JSON_KEY_USER_USERNAME, username)
                        .add(Constants.JSON_KEY_USER_GIVEN_NAME, givenName)
                        .add(Constants.JSON_KEY_USER_FAMILY_NAME, familyName)
                        .add(Constants.JSON_KEY_USER_EMAIL_ADDRESS, email)
                        .add(Constants.JSON_KEY_USER_MOBILE_NUMBER, mobile))
                .build();
            
                System.out.println("Json Input: " + input.toString());
                System.out.println("Response: " + resource.registerUser(input.toString()));
                break;
            
            case "PR":
                input = Json.createObjectBuilder()
                        .add(Constants.JSON_KEY_DID, 1)
                        .add(Constants.JSON_KEY_FIDO_SERVICE_INPUT_PARAMS, Json.createObjectBuilder()
                            .add(Constants.JSON_KEY_FIDO_SVCINFO, Json.createObjectBuilder()
                                .add(Constants.JSON_KEY_DID, 1)
                                .add(Constants.JSON_KEY_FIDO_SVCINFO_PROTOCOL, Constants.JSON_KEY_FIDO_SVCINFO_PROTOCOL_FIDO20)
                                .add(Constants.JSON_KEY_FIDO_SVCINFO_AUTHTYPE, Constants.JSON_KEY_FIDO_SVCINFO_AUTHTYPE_PASSWORD)
                                .add(Constants.JSON_KEY_FIDO_SVCINFO_SVCUSERNAME, "svcfidouser")    
                                .add(Constants.JSON_KEY_FIDO_SVCINFO_SVCPASSWORD, "Abcd1234!")    
                            )
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD, Json.createObjectBuilder()
                                .add(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME, "anoor")
                                .add(Constants.JSON_KEY_FIDO_PAYLOAD_DISPLAYNAME, "Arshad Noor")
                                .add(Constants.JSON_KEY_FIDO_PAYLOAD_OPTIONS, "{\"attestation\":\"direct\"}")    
                                .add(Constants.JSON_KEY_FIDO_PAYLOAD_EXTENSIONS, "{}")
                            )).build();
                System.out.println("Response: " + resource.getFidoRegistrationChallenge(input.toString()));
                break;        
            default:
                System.err.println("ERROR: Invalid option: " + operation + '\n');
                System.err.println(USAGE);
        }
        resource.close();
    }
        
    public SfaecoClient() {
        client = ClientBuilder.newClient();
        webTarget = client.target(BASE_URI);
    }

    public String registerUser(String jo) throws ClientErrorException {
        WebTarget resource = webTarget;
        resource = resource.path("registerUser");
        System.out.println("Calling REST webservice at: " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    public String getFidoRegistrationChallenge(String jo) throws ClientErrorException {
        WebTarget resource = webTarget;
        resource = resource.path("getFidoRegistrationChallenge");
        System.out.println("Calling REST webservice at: " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }

    public void close() {
        client.close();
    }
}
