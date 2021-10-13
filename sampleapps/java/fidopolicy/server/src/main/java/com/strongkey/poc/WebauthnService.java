/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.poc;

import com.strongkey.FIDO2JWTVerify.Verify;
import com.strongkey.database.UserDatabase;
import com.strongkey.utilities.Common;
import com.strongkey.utilities.Configurations;
import com.strongkey.utilities.Constants;
import com.strongkey.utilities.POCLogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

@Path("")
@Stateless
public class WebauthnService {

    private final String CLASSNAME = WebauthnService.class.getName();
    @Context
    private HttpServletRequest request;

    @PostConstruct
    private void init() {
    }

    @EJB
    private UserDatabase userdatabase;

    Verify v = new Verify();

    // Endpoint to request a registration challenge (for a new account).
    @POST
    @Path("/" + Constants.RP_PREGISTER_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response preregister(JsonObject input) {
        try {
            //Get user input + basic input checking
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String displayName = getValueFromInput(Constants.RP_JSON_KEY_DISPLAYNAME, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
//            String prereg = SKFSClient.preregister(username, displayName, policy);
            

            //Verify User does not already exist
            if (!doesAccountExists(username)) {
                String prereg = SKFSClient.preregister(username, displayName, policy);
                Common.getRPIDFromPreRegResponse(prereg, policy);
                return generateResponse(Response.Status.OK, prereg);
            } else {
                //If the user already exists, throw an error
                POCLogger.logp(Level.SEVERE, CLASSNAME, "preregister", "POC-WS-ERR-1001", username);
                return generateResponse(Response.Status.CONFLICT,
                        POCLogger.getMessageProperty("POC-WS-ERR-1001"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "preregister", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint to send a signed registration challenge (for a new account). On
    // successful verification of the signed challenge, create the user's account
    // and logs the user in.
    @POST
    @Path("/" + Constants.RP_REGISTER_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response register(JsonObject input) {
        try {
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
        
            if (!doesAccountExists(username)) {
                String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
                String regresponse = SKFSClient.register(username, policy, getOrigin(), input);
                System.out.println("Received from FIDO Server: " + regresponse);
                
                //On success, add user to database
                userdatabase.addUser(username);
                
                return generateResponse(Response.Status.OK, getResponseFromSKFSResponse(regresponse));
            } else {
                //If the user already exists, throw an error
                POCLogger.logp(Level.SEVERE, CLASSNAME, "register", "POC-WS-ERR-1001", username);
                return generateResponse(Response.Status.CONFLICT, POCLogger.getMessageProperty("POC-WS-ERR-1001"));
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex.getLocalizedMessage().contains("Rejected Authenticator") || ex.getLocalizedMessage().contains("User Verification required by policy")) {

                POCLogger.logp(Level.SEVERE, CLASSNAME, "register", "POC-WS-ERR-1007", ex.getLocalizedMessage());
                return generateResponse(Response.Status.BAD_REQUEST,
                        POCLogger.getMessageProperty("POC-WS-ERR-1007"));
            }
            POCLogger.logp(Level.SEVERE, CLASSNAME, "register", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage());

        }
    }

    // Endpoint that allows a signed in user to register additional FIDO2 keys to
    // their account. The endpoint will return a registration challenge that can
    // signed by a FIDO2 key that is not already associated with the user to
    // register that key.
    @POST
    @Path("/" + Constants.RP_PREGISTER_EXISTING_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response preregisterExisting(JsonObject input) {
        try {

            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
            String displayName = getValueFromInput(Constants.RP_JSON_KEY_DISPLAYNAME, input);
            String jwt = getValueFromInput(Constants.RP_JSON_KEY_JWT, input);

            String did = Integer.toString(SKFSClient.getDid(policy));
            String jwtPassword = Configurations.getConfigurationProperty("poc.cfg.property.jwtpassword");
            String jwtTrustStoreLocation = Configurations.getConfigurationProperty("poc.cfg.property.jwttruststorelocation");
            String defaultRpid = Configurations.getConfigurationProperty("poc.cfg.property.rpid");
            String rpid = Common.didRPID.getOrDefault(SKFSClient.getDid(policy), defaultRpid);
            
            Boolean isJwtVerified = v.verify( did, jwt, username, request.getHeader("User-Agent"), request.getLocalAddr(), jwtPassword, jwtTrustStoreLocation, rpid);

            if (!isJwtVerified) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "preregisterExisting", "POC-WS-ERR-1003", "");
                return generateResponse(Response.Status.FORBIDDEN, POCLogger.getMessageProperty("POC-WS-ERR-1003"));
            }
            
            if(isJwtVerified /*true*/){
                String prereg = SKFSClient.preregister(username, displayName, policy);
                return generateResponse(Response.Status.OK, prereg);
            }
            else{
//                session.invalidate();
                POCLogger.logp(Level.SEVERE, CLASSNAME, "preregisterExisting", "POC-WS-ERR-1004", username);
                return generateResponse(Response.Status.FORBIDDEN, POCLogger.getMessageProperty("POC-WS-ERR-1004"));
            }
            
        } catch (Exception ex) {
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "preregisterExisting", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint for a logged in user to send a signed registration challenge. On
    // successful verification of the signed challenge, the user will be able to
    // authenticate themselves using the FIDO2 key that signed the challenge.
    @POST
    @Path("/" + Constants.RP_REGISTER_EXISTING_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response registerExisting(JsonObject input) {
        try {
            
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);

            String jwt = getValueFromInput(Constants.RP_JSON_KEY_JWT, input);
            String did = Integer.toString(SKFSClient.getDid(policy));
            String jwtPassword = Configurations.getConfigurationProperty("poc.cfg.property.jwtpassword");
            String jwtTrustStoreLocation = Configurations.getConfigurationProperty("poc.cfg.property.jwttruststorelocation");
            String defaultRpid = Configurations.getConfigurationProperty("poc.cfg.property.rpid");
            String rpid = Common.didRPID.getOrDefault(SKFSClient.getDid(policy), defaultRpid);
            
            Boolean isJwtVerified = v.verify( did, jwt, username, request.getHeader("User-Agent"), request.getLocalAddr(), jwtPassword, jwtTrustStoreLocation, rpid);

            if (!isJwtVerified) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "registerExisting", "POC-WS-ERR-1003", "");
                return generateResponse(Response.Status.FORBIDDEN, POCLogger.getMessageProperty("POC-WS-ERR-1003"));
            }

            if (doesAccountExists(username)) {
                String regresponse = SKFSClient.register(username, policy, getOrigin(), input);
                return generateResponse(Response.Status.OK, getResponseFromSKFSResponse(regresponse));
            } else {
                //If the user already exists, throw an error
                POCLogger.logp(Level.SEVERE, CLASSNAME, "registerExisting", "POC-WS-ERR-1002", username);
                return generateResponse(Response.Status.CONFLICT, POCLogger.getMessageProperty("POC-WS-ERR-1002"));
            }

        } catch (Exception ex) {
            if (ex.getLocalizedMessage().contains("Rejected Authenticator") || ex.getLocalizedMessage().contains("User Verification required by policy")) {

                POCLogger.logp(Level.SEVERE, CLASSNAME, "register", "POC-WS-ERR-1007", ex.getLocalizedMessage());
                return generateResponse(Response.Status.BAD_REQUEST,
                        POCLogger.getMessageProperty("POC-WS-ERR-1007"));
            }
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "registerExisting", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    ex.getLocalizedMessage());
        }
    }

    // Endpoint that allows a user to request a challenge to authenicate
    // themselves
    @POST
    @Path("/" + Constants.RP_PREAUTHENTICATE_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response preauthenticate(JsonObject input) {
        try {
            // Get user input + basic input checking
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            // Verify user exists
            
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
            String preauth = SKFSClient.preauthenticate(username, policy);
            Common.getRPIDFromPreAuthenticateResponse(preauth, policy);
            int keysize = Common.checkNumberOfKeysInPreAuthResponse(preauth);
            if(keysize == 0){
                if (!userdatabase.doesUserExist(username)) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "preauthenticate", "POC-WS-ERR-1002", username);
                return generateResponse(Response.Status.NOT_FOUND, POCLogger.getMessageProperty("POC-WS-ERR-1002"));
            }
            }
            return generateResponse(Response.Status.OK, preauth);
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex.getLocalizedMessage().contains("No valid keys registered; please register first")) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "preauthenticate", "POC-WS-ERR-1002", ex.getLocalizedMessage());
                return generateResponse(Response.Status.NOT_FOUND, POCLogger.getMessageProperty("POC-WS-ERR-1002"));
            }

            POCLogger.logp(Level.SEVERE, CLASSNAME, "preauthenticate", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint to send a signed authentication challenge. On successful
    // verification of the signed challenge, the user will be logged in
    @POST
    @Path("/" + Constants.RP_AUTHENTICATE_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response authenticate(JsonObject input) {
        try {
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);

            if (doesAccountExists(username)) {
                String authresponse = SKFSClient.authenticate(username, policy, getOrigin(), input, request.getHeader("User-Agent"));
                return generateAuthenticateResponse(Response.Status.OK, getResponseFromSKFSResponse(authresponse), getJWTFromSKFSResponse(authresponse));
            } else {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "authenticate", "POC-WS-ERR-1002", username);
                return generateResponse(Response.Status.CONFLICT, POCLogger.getMessageProperty("POC-WS-ERR-1002"));
            }
            
            
        } catch (Exception ex) {
            ex.printStackTrace();
            if (ex.getLocalizedMessage().contains("No valid keys registered; please register first")) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "preauthenticate", "POC-WS-ERR-1002", ex.getLocalizedMessage());
                return generateResponse(Response.Status.NOT_FOUND, POCLogger.getMessageProperty("POC-WS-ERR-1002"));
            }
            POCLogger.logp(Level.SEVERE, CLASSNAME, "authenticate", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint that allows the frontend to check whether the user is logged in.
    @POST
    @Path("/" + Constants.RP_ISLOGGEDIN_PATH)
    @Produces({MediaType.APPLICATION_JSON})
    public Response isLoggedIn(JsonObject input) {
        try {
            
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String jwt = getValueFromInput(Constants.RP_JSON_KEY_JWT, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
            String did = Integer.toString(SKFSClient.getDid(policy));
            String jwtPassword = Configurations.getConfigurationProperty("poc.cfg.property.jwtpassword");
            String jwtTrustStoreLocation = Configurations.getConfigurationProperty("poc.cfg.property.jwttruststorelocation");
            String defaultRpid = Configurations.getConfigurationProperty("poc.cfg.property.rpid");
            String rpid = Common.didRPID.getOrDefault(SKFSClient.getDid(policy), defaultRpid);
            
            Boolean isJwtVerified = v.verify( did, jwt, username, request.getHeader("User-Agent"), request.getLocalAddr(), jwtPassword, jwtTrustStoreLocation, rpid);
            
//            HttpSession session = request.getSession(false);
            if (!isJwtVerified) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "isLoggedIn", "POC-WS-ERR-1003", "");
                return generateResponse(Response.Status.FORBIDDEN, POCLogger.getMessageProperty("POC-WS-ERR-1003"));
            }
            
            return generateResponse(Response.Status.OK, username);
        } catch (Exception ex) {
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "isLoggedIn", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a logged in user to logout
    @POST
    @Path("/" + Constants.RP_LOGOUT_PATH)
    @Produces({MediaType.APPLICATION_JSON})
    public Response logout() {
        try {
            // JWT is deleted from the Client Side
            return generateResponse(Response.Status.OK, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "isLoggedIn", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a logged in user delete their account. This endpoint
    // will delete their account and delete all keys associated with their account
    // on the SKFS
    @POST
    @Path("/" + Constants.RP_PATH_DELETEACCOUNT)
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteAccount(JsonObject input) {
        try {

            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
            String jwt = getValueFromInput(Constants.RP_JSON_KEY_JWT, input);
            String did = Integer.toString(SKFSClient.getDid(policy));
            String jwtPassword = Configurations.getConfigurationProperty("poc.cfg.property.jwtpassword");
            String jwtTrustStoreLocation = Configurations.getConfigurationProperty("poc.cfg.property.jwttruststorelocation");
            String defaultRpid = Configurations.getConfigurationProperty("poc.cfg.property.rpid");
            String rpid = Common.didRPID.getOrDefault(SKFSClient.getDid(policy), defaultRpid);
            
            Boolean isJwtVerified = v.verify( did, jwt, username, request.getHeader("User-Agent"), request.getLocalAddr(), jwtPassword, jwtTrustStoreLocation, rpid);       
                    
//            HttpSession session = request.getSession(false);
            if (!isJwtVerified) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "preregisterExisting", "POC-WS-ERR-1003", "");
                return generateResponse(Response.Status.FORBIDDEN, POCLogger.getMessageProperty("POC-WS-ERR-1003"));
            }
            
            userdatabase.deleteUser(username);
            String SKFSResponse = SKFSClient.getKeys(username, policy);
            JsonArray keyIds = getKeyIdsFromSKFSResponse(SKFSResponse);
            
            ///////////////////////////////////////////////////////////////////
            
            // Verify those keys are actually registered to that user.
            System.out.println(keyIds);
            
            JsonArrayBuilder userKeyIdSet = Json.createArrayBuilder();
            for (int keyIndex = 0; keyIndex < keyIds.size(); keyIndex++) {
                userKeyIdSet.add(keyIds.getJsonObject(keyIndex).getString("keyid"));
            }
            
            ///////////////////////////////////////////////////////////////////
            
            removeKeys(userKeyIdSet.build(), policy);
            return generateResponse(Response.Status.OK, "Success");
            
        } catch (Exception ex) {
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "deleteAccount", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a logged in user to request all keys that they
    // registered on their account
    @POST
    @Path("/" + Constants.RP_PATH_GETUSERINFO)
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserInfo(JsonObject input) {
        try {
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
            String jwt = getValueFromInput(Constants.RP_JSON_KEY_JWT, input);
            String did = Integer.toString(SKFSClient.getDid(policy));
            String jwtPassword = Configurations.getConfigurationProperty("poc.cfg.property.jwtpassword");
            String jwtTrustStoreLocation = Configurations.getConfigurationProperty("poc.cfg.property.jwttruststorelocation");
            String defaultRpid = Configurations.getConfigurationProperty("poc.cfg.property.rpid");
            String rpid = Common.didRPID.getOrDefault(SKFSClient.getDid(policy), defaultRpid);
            
            Boolean isJwtVerified = v.verify( did, jwt, username, request.getHeader("User-Agent"), request.getLocalAddr(), jwtPassword, jwtTrustStoreLocation, rpid);

            
            if (isJwtVerified) {
                String keys = SKFSClient.getKeys(username, policy);
                return generateResponse(Response.Status.OK, keys);
            } else {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "getUserInfo", "POC-WS-ERR-1004", username);
                return generateResponse(Response.Status.CONFLICT, POCLogger.getMessageProperty("POC-WS-ERR-1004"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "getUserInfo", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a logged in user to delete FIDO2 keys they have registered
    // on their account
    @POST
    @Path("/" + Constants.RP_PATH_REMOVEKEYS)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response removeKeys(JsonObject input) {
        try {
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String policy = getValueFromInput(Constants.RP_JSON_KEY_POLICY, input);
            String jwt = getValueFromInput(Constants.RP_JSON_KEY_JWT, input);
            String did = Integer.toString(SKFSClient.getDid(policy));
            String jwtPassword = Configurations.getConfigurationProperty("poc.cfg.property.jwtpassword");
            String jwtTrustStoreLocation = Configurations.getConfigurationProperty("poc.cfg.property.jwttruststorelocation");
            String defaultRpid = Configurations.getConfigurationProperty("poc.cfg.property.rpid");
            String rpid = Common.didRPID.getOrDefault(SKFSClient.getDid(policy), defaultRpid);
            
            Boolean isJwtVerified = v.verify( did, jwt, username, request.getHeader("User-Agent"), request.getLocalAddr(), jwtPassword, jwtTrustStoreLocation, rpid);
            
            if (isJwtVerified) {
                System.out.println("input = " + input);
                JsonArray keyIds = getKeyIdsFromInput(input);
                // Verify those keys are actually registered to that user.
                String keys = SKFSClient.getKeys(username, policy);
                JsonArray userKeyIds = getKeyIdsFromSKFSResponse(keys);

                System.out.println("keyids = " + keyIds);
                System.out.println("userkeyids = " + userKeyIds);

                Set<String> userKeyIdSet = new HashSet<>();
                for (int keyIndex = 0; keyIndex < userKeyIds.size(); keyIndex++) {
                    userKeyIdSet.add(userKeyIds.getJsonObject(keyIndex)
                            .getString(Constants.SKFS_RESPONSE_JSON_KEY_RANDOMID));
                }
                for (int keyIndex = 0; keyIndex < keyIds.size(); keyIndex++) {
                    if (!userKeyIdSet.contains(keyIds.getString(keyIndex))) {
                        POCLogger.logp(Level.SEVERE, CLASSNAME, "removeKeys", "POC-WS-ERR-1004", "");
                        return generateResponse(Response.Status.BAD_REQUEST, POCLogger.getMessageProperty("POC-WS-ERR-1004"));
                    }
                }
                removeKeys(keyIds, policy);
                return generateResponse(Response.Status.OK, "Success");
            } else {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "removeKeys", "POC-WS-ERR-1002", username);
                return generateResponse(Response.Status.CONFLICT, POCLogger.getMessageProperty("POC-WS-ERR-1002"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            POCLogger.logp(Level.SEVERE, CLASSNAME, "removeKeys", "POC-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    POCLogger.getMessageProperty("POC-WS-ERR-1000"));
        }
    }

    // Retrieves the protocol + domain + port (eg. https://localhost:8181) that
    // the enduser send the request to
    private String getOrigin() throws URISyntaxException {
        URI requestURL = new URI(request.getRequestURL().toString());
        return requestURL.getScheme() + "://" + requestURL.getAuthority();
    }

    private String getRegistrationTemplate() throws FileNotFoundException, IOException {
        StringBuilder sb = new StringBuilder();
        File file = new File(getClass().getClassLoader().getResource("resources/email_register.html").getFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }

    // Return whether an account with this username already exists
    private boolean doesAccountExists(String username) {
        return userdatabase.doesUserExist(username);
    }

    // Parse user input
    private String getValueFromInput(String key, JsonObject input) {
        String username = input.getString(key, null);
        if (username == null) {
            throw new IllegalArgumentException(key + " missing");
        }
        return username;
    }

    // Parse array of user keys from user input
    private JsonArray getKeyIdsFromInput(JsonObject input) {
        JsonArray keyIds = input.getJsonArray(Constants.RP_JSON_KEY_KEYIDS);
        if (keyIds == null) {
            throw new IllegalArgumentException("keyIds missing");
        }
        return keyIds;
    }

    // Parse array of user keys from SKFS response
    private JsonArray getKeyIdsFromSKFSResponse(String SKFSResponse) {
        JsonObject SKFSResponseObject = Json.createReader(new StringReader(SKFSResponse)).readObject();
        return SKFSResponseObject.getJsonObject(Constants.SKFS_RESPONSE_JSON_KEY_RESPONSE)
                .getJsonArray(Constants.SKFS_RESPONSE_JSON_KEY_KEYS);
    }

    // Parse response string from SKFS
    private String getResponseFromSKFSResponse(String SKFSResponse) {
        JsonObject SKFSResponseObject = Json.createReader(new StringReader(SKFSResponse)).readObject();
        String response = SKFSResponseObject.getString(Constants.SKFS_RESPONSE_JSON_KEY_RESPONSE, null);
        if (response == null) {
            throw new IllegalArgumentException("Unexpected Response");
        }
        return response;
    }

    // Parse response string from SKFS
    private String getJWTFromSKFSResponse(String SKFSResponse) {
        JsonObject SKFSResponseObject = Json.createReader(new StringReader(SKFSResponse)).readObject();
        String response = SKFSResponseObject.getString("jwt", null);
        if (response == null) {
            throw new IllegalArgumentException("Unexpected Response");
        }
        return response;
    }

    // Remove all keys
    private void removeKeys(JsonArray keyIds, String policy) throws Exception {
        for (int keyIndex = 0; keyIndex < keyIds.size(); keyIndex++) {
            SKFSClient.deregisterKey(keyIds.getString(keyIndex), policy);
        }
    }

    // A standard method of communicating with the frontend code. This can be
    // modified as needed to fit the needs of the site being build.
    private Response generateResponse(Status status, String responsetext) {
        String response = status.equals(Response.Status.OK) ? responsetext : "";
        String message = status.equals(Response.Status.OK) ? "" : responsetext;
        String error = status.equals(Response.Status.OK) ? Constants.RP_JSON_VALUE_FALSE_STRING : Constants.RP_JSON_VALUE_TRUE_STRING;
        String responseString = Json.createObjectBuilder()
                .add(Constants.RP_JSON_KEY_RESPONSE, response)
                .add(Constants.RP_JSON_KEY_MESSAGE, message)
                .add(Constants.RP_JSON_KEY_ERROR, error)
                .build().toString();
        return Response.status(status)
                .entity(responseString).build();
    }

    private Response generateAuthenticateResponse(Status status, String responsetext, String jwt) {
        String response = status.equals(Response.Status.OK) ? responsetext : "";
        String message = status.equals(Response.Status.OK) ? "" : responsetext;
        String error = status.equals(Response.Status.OK) ? Constants.RP_JSON_VALUE_FALSE_STRING : Constants.RP_JSON_VALUE_TRUE_STRING;
        String responseString = Json.createObjectBuilder()
                .add(Constants.RP_JSON_KEY_RESPONSE, response)
                .add(Constants.RP_JSON_KEY_MESSAGE, message)
                .add(Constants.RP_JSON_KEY_ERROR, error)
                .add(Constants.RP_JSON_KEY_JWT, jwt)
                .build().toString();
        return Response.status(status)
                .entity(responseString).build();
    }
}
