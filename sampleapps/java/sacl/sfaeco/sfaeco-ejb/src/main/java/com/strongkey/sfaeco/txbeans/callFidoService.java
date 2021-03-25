/**
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
 * Calls a REST webservice on the FIDO Servlet to perform NON-administrative 
 * functions, such as preregister, register, preauthenticate, authenticate, etc.
 * 
 * The StrongKey v3 API of the SKFS generates JSON similar to the following for
 * each request type:
 * 
 * PREREGISTER:
 * ------------------------------------------
 * {
      "svcinfo": {
        "did": 1,
        "protocol": "FIDO2_0",
        "authtype": "PASSWORD",
        "svcusername": "svcfidouser",
        "svcpassword": ""*************"
      },
      "payload": {
        "username": "johndoe",
        "displayName": "Initial Registration",
        "options": {
          "attestation": "direct"
        },
        "extensions": "{}"
      }
    }
 * 
 * 
 * REGISTER:
 * ------------------------------------------
 * {
      "svcinfo": {
        "did": 1,
        "protocol": "FIDO2_0",
        "authtype": "PASSWORD",
        "svcusername": "svcfidouser",
        "svcpassword": ""*************"
      },
      "payload": {
        "publicKeyCredential": {
          "type": "public-key",
          "id": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
          "rawId": "MBDVxPOZ5To939FLGuhTP...aaMA1jqTvajZrqWKbnIc8wA",
          "response": {
            "attestationObject": "o2NmbXRmcGFj...a2VkZ2uivtzxnwOKYOPHMmTcyRW4",
            "clientDataJSON": "eyJ0eXBlIjoid2V...YTIwOS5zdHJvbmdhdXRoLmNvbSJ9",
            "clientExtensions": {}
          }
        },
        "strongkeyMetadata": {
          "version": "1.0",
          "create_location": "Sunnyvale, CA",
          "origin": "https://demo.strongkey.com",
          "username": "johndoe"
        }
      }
    }
 * 
 * 
 * PREAUTHENTICATE:
 * ------------------------------------------
 * {
        "svcinfo": {
            "did": 1,
            "protocol": "FIDO2_0",
            "authtype": "PASSWORD",
            "svcusername": "svcfidouser",
            "svcpassword": "*************"
        },
        "payload": {
            "username": "rose",
            "options": "{}"
        }
    }
 *
 * 
 * AUTHENTICATE:
 * ------------------------------------------
 * {
        "svcinfo": {
            "did": 1
        },
        "payload": {
            "publicKeyCredential": {
                "type": "public-key",
                "id": "MTBGNTQ4MzFENzc1RDMzQy...MzgzNTg2tMTBGNDhDQzMzNDkyOTI3MA",
                "rawId": "eyJrZXluYW1lIjoiMTB...RjZWM1YWEzMzVmNWEzNTM3ZiJ9",
                "response": {
                    "clientDataJSON": "eyJ0eXBlIjoid2....ViYXV0aG4ub3J0ZWQifX0",
                    "authenticatorData": "QKDHG74th...tvcuEAAAAEqFjdXZtgYMEBgE",
                    "signature": "MEYCIQD6_xkGT...rqnLltOtd0OkoUfaXuViw-Yr9qfqGwp",
                    "clientExtensions": {}
                }
            },
            "strongkeyMetadata": {
                "version": "1.0",
                "last_used_location": "Cupertino, CA",
                "origin": "https://sakasmb.noorhome.net:8181",
                "username": "anoor"
            }
        }
    }
 * 
 * 
 * PREAUTHORIZE:
 * ------------------------------------------
 * {
        "svcinfo": {
            "did": 1
        },
        "payload": {
            "username": "anoor",
            "txid": "SFAECO-10",
            "txpayload": "eyJtZXJjaGFudE5iOiJTdHJvbmdL...wOjE1IFBEVCAyMDIxIn0",
            "options": {}
        }
    }
 *  
 * 
 * AUTHORIZE:
 * ------------------------------------------
 * {
        "svcinfo": {
            "did": 1,
            "protocol": "FIDO2_0",
            "authtype": "PASSWORD",
            "svcusername": "svcfidouser",
            "svcpassword": "*************"
        },
        "payload": {
            "username": "rose",
            "options": "{}"
        }
    }
 */
package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;

@Stateless
public class callFidoService implements callFidoServiceLocal {

    private WebTarget webTarget;
    private Client client;
    private static int fidodid;
    private static String fidofqdn, fidoprotocol, fidoauthtype, fidosvccred, fidosvcpass;
    private static String SERVLET_URI;
    private static final String CLASSNAME = "callFidoService";
    
    @PostConstruct
    protected void initialize() {
        
        // Get configured values for FIDO
        SERVLET_URI = Common.getConfigurationProperty("sfaeco.cfg.property.fido.servlet.resturi");
        fidofqdn = Common.getConfigurationProperty("sfaeco.cfg.property.fido.fqdn");
        fidoprotocol = Common.getConfigurationProperty("sfaeco.cfg.property.fido.protocol");
        fidoauthtype = Common.getConfigurationProperty("sfaeco.cfg.property.fido.authtype");
        fidodid = Integer.parseInt(Common.getConfigurationProperty("sfaeco.cfg.property.fido.did"));
        fidosvccred = Common.getConfigurationProperty("sfaeco.cfg.property.fido.svccred");
        fidosvcpass = Common.getConfigurationProperty("sfaeco.cfg.property.fido.svcpassword");
        
        Common.log(Level.INFO, "SFAECO-MSG-1000", " FIDO Configured Properties: +"
                + "\nsfaeco.cfg.property.fido.fqdn: " + fidofqdn
                + "\nsfaeco.cfg.property.fido.protocol: " + fidoprotocol
                + "\nsfaeco.cfg.property.fido.fodoauthtype: " + fidoauthtype
                + "\nsfaeco.cfg.property.fido.did: " + fidodid
                + "\nsfaeco.cfg.property.fido.svccred: " + fidosvccred
                + "\nsfaeco.cfg.property.fido.svcpassword: ***************");
        
        // Build up connection objects
        client = ClientBuilder.newClient();
        webTarget = client.target(fidofqdn.concat(SERVLET_URI));
        
//        // Connect and ping the service for availability
//        int response = pingFido(webTarget).getStatus();
//        if (response == 200) {
//           FIDO_SERVICE_AVAILABLE = true;
//           Common.log(Level.INFO, "SFAECO-MSG-1000", " FIDO service is OK");
//        } else {
//            Common.log(Level.INFO, "SFAECO-ERR-3006", "FIDO service is NOT responding: " + response);
//        }
    }
    
    /****************************************************************
     *                                               888
     *                                               888
     *                                               888
     *   .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     *  d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     *  88888888   X88K   88888888 888      888  888 888    88888888
     *  Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     *   "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *
     ****************************************************************/
    
    /**
     * Calls a webservice on the MDKMS module of a specified Tellaro appliance
     * 
     * @param did String value of domain for logging
     * @param service Currently PREREGISTER, REGISTER, PREAUTHENTICATE,
     * AUTHENTICATE, DEREGISTER, GETKEYSINFO, UPDATEKEYINFO and PING work
     * @param param String parameter is a JsonObject.toString()
     * @param txid String transaction ID for logging
     * @return String value - depends on which service was called
     */
    @Override
    public String execute(Short did, Constants.FIDO_SERVICE service, JsonObject param, String txid) 
    {
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", CLASSNAME, String.valueOf(did), txid);
        
        System.out.println("CallFidoService Params:\n"
            + "did: " + did + '\n'
            + "service: " + service + '\n'
            + "param: " + param + '\n');
        
        /**
         * Create the FIDO service and payload JSON - the payload varies 
         * depending on the service being requested; check the FIDO Reference 
         * Manual's chapter on API Mechanics for most current information.
         * 
         * {
              "svcinfo": {
                "did": 1,
                "protocol": "FIDO2_0",
                "authtype": "PASSWORD",
                "svcusername": "svcfidouser",
                "svcpassword": "Abcd1234!"
              },
              "payload": {
                ....
              }
            }
         */
        JsonObject fidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, fidodid)
                .add(Constants.JSON_KEY_SERVICE_INFO_PROTOCOL, fidoprotocol)
                .add(Constants.JSON_KEY_SERVICE_INFO_AUTHTYPE, fidoauthtype)
                .add(Constants.JSON_KEY_SERVICE_INFO_USERNAME, fidosvccred)
                .add(Constants.JSON_KEY_SERVICE_INFO_PASSWORD, fidosvcpass))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, param.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD))
            .build();
        
        // DEBUG: Log version for output with masked service password
        JsonObject debugFidoInput = Json.createObjectBuilder()
            .add(Constants.JSON_KEY_FIDO_SERVICE_INFO, Json.createObjectBuilder()
                .add(Constants.JSON_KEY_SERVICE_INFO_DID, fidodid)
                .add(Constants.JSON_KEY_SERVICE_INFO_PROTOCOL, fidoprotocol)
                .add(Constants.JSON_KEY_SERVICE_INFO_AUTHTYPE, fidoauthtype)
                .add(Constants.JSON_KEY_SERVICE_INFO_USERNAME, fidosvccred)
                .add(Constants.JSON_KEY_SERVICE_INFO_PASSWORD, "*************"))
            .add(Constants.JSON_KEY_FIDO_PAYLOAD, param.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD))
            .build();
        Common.log(Level.INFO, "SFAECO-MSG-1000", "FIDO Service Input: " + debugFidoInput.toString());
        
        
        String response = null;
        switch (service) {
            case PING:
                response = pingFido(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;
            case PREREGISTER:
                response = preregister(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;
            case REGISTER:
                response = register(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;    
            case PREAUTHENTICATE:
                response = preauthenticate(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;
            case AUTHENTICATE:
                response = authenticate(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;   
            case PREAUTHORIZE:
                response = preauthorize(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;
            case AUTHORIZE:
                response = authorize(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;       
            case DEREGISTER:
                response = deregister(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;
            case GETKEYSINFO:
                response = getkeysinfo(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;      
            case UPDATEKEYINFO:
                response = updatekeyinfo(webTarget, fidoInput.toString());
                Common.log(Level.INFO, "SFAECO-MSG-3006", response);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
                return response;          
            default:
                Common.log(Level.SEVERE, "SFAECO-ERR-3005", service);
        }
        Common.exitLog(Level.INFO, "SFAECO-MSG-3004", CLASSNAME, String.valueOf(did), txid, timein);
        return response;
    }
    
    private String pingFido(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("ping");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Ping at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String preregister(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("preregister");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Preregister at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String register(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("register");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Register at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String preauthenticate(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("preauthenticate");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Preauthenticate at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String authenticate(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("authenticate");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Authenticate at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String preauthorize(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("preauthorize");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Preauthorize at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String authorize(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("authorize");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Authorize at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String deregister(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("deregister");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Deregister at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String getkeysinfo(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("getkeysinfo");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Get Key Information at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }
    
    private String updatekeyinfo(WebTarget resource, String jo) throws ClientErrorException {
        resource = resource.path("updatekeyinfo");
        Common.log(Level.INFO, "SFAECO-MSG-3005", "Update Key Information at " + resource.getUri());
        return resource.request(MediaType.APPLICATION_JSON).post(Entity.json(jo), String.class);
    }

    @PreDestroy
    protected void close() {
        client.close();
    }
}
