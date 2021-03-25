/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.sfaboa;

import FIDO2JWTVerify.Verify;
import com.strongkey.Registrations.RegistrationDBLocal;
import com.strongkey.Users.UserDBLocal;
import com.strongkey.utilities.Common;
import com.strongkey.utilities.Configurations;
import com.strongkey.utilities.Constants;
import com.strongkey.utilities.EmailService;
import com.strongkey.utilities.SFABOALogger;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Enumeration;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
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
public class sfaboaService {

    private final String CLASSNAME = sfaboaService.class.getName();
    @Context
    private HttpServletRequest request;

    @EJB
    private UserDBLocal userdatabase;

    @EJB
    private EmailService emailService;

    @EJB
    private RegistrationDBLocal registrationDB;
    @EJB
    private Verify verify;
    private SecureRandom nonceGen;

    @PostConstruct
    private void init(){
        nonceGen = new SecureRandom();
    }
    private static final String JWTPASSWORD = Configurations.getConfigurationProperty("sfaboa.cfg.property.jwtpassword");
    private static final String JWTTRUSTSTORELOCATION = Configurations.getConfigurationProperty("sfaboa.cfg.property.jwttruststorelocation");
    private static final String DID = Configurations.getConfigurationProperty("sfaboa.cfg.property.did");


    // Send an email to a specified email address with a registration link
    @POST
    @Path("/" + Constants.RP_REGISTER_EMAIL_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response registerEmail(JsonObject input){
        try{
            String email = getValueFromInput(Constants.RP_JSON_KEY_EMAIL, input);

            //Verify valid email (by checking that an account with this email DNE)
            if (doesEmailExist(email)) {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "registerEmail", "SFABOA-WS-ERR-1005", email);
                return generateResponse(Response.Status.CONFLICT,
                        SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1005"));
            }

            //If a registration link exists already for this account, delete it
            if(registrationDB.doesRegistrationForEmailExist(email)){
                registrationDB.deleteRegistration(email);
            }

            //Store pending registration in DB
            String nonce = generateNonce();

            //Send out registration email
            registrationDB.addRegistration(email, nonce);
            String reglink = getOrigin()
                    + Configurations.getConfigurationProperty("sfaboa.cfg.property.registration.path")
                    + nonce;
            emailService.sendEmail(
                    email,
                    Configurations.getConfigurationProperty("sfaboa.cfg.property.email.subject"),
                    getRegistrationTemplate().replace("$URLLine$", reglink));
            return generateResponse(Response.Status.OK, "");
        }
        catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "registerEmail", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Endpoint to request a registration challenge (for a new account).
    @POST
    @Path("/" + Constants.RP_PREGISTER_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response preregister(JsonObject input){
        try{
            //Get user input + basic input checking
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String displayName = getValueFromInput(Constants.RP_JSON_KEY_DISPLAYNAME, input);
            String firstName = getValueFromInput(Constants.RP_JSON_KEY_FIRSTNAME, input);
            String lastName = getValueFromInput(Constants.RP_JSON_KEY_LASTNAME, input);
            String nonce = getValueFromInput(Constants.RP_JSON_KEY_NONCE, input);

            //Verify valid registration link (by checking that the registration OTP (nonce) is valid)
            if(!isValidNonce(nonce)){
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "preregister", "SFABOA-WS-ERR-1006", nonce);
                return generateResponse(Response.Status.CONFLICT,
                        SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1006"));
            }

            //Verify User does not already exist
            if (!doesAccountExist(username)){
                String prereg = SKFSClient.preregister(username, displayName);
                HttpSession session = request.getSession(true);
                session.setAttribute(Constants.SESSION_USERNAME, username);
                session.setAttribute(Constants.SESSION_ISAUTHENTICATED, false);
                session.setAttribute(Constants.SESSION_FIRSTNAME, firstName);
                session.setAttribute(Constants.SESSION_LASTNAME, lastName);
                session.setAttribute(Constants.SESSION_EMAIL, registrationDB.getEmailFromNonce(nonce));
                session.setMaxInactiveInterval(Constants.SESSION_TIMEOUT_VALUE);
                return generateResponse(Response.Status.OK, prereg);
            }
            else{
                //If the user already exists, throw an error
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "preregister", "SFABOA-WS-ERR-1001", username);
                return generateResponse(Response.Status.CONFLICT,
                        SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1001"));
            }
        }
        catch(Exception ex){
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "preregister", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
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
        try{
            HttpSession session = request.getSession(false);
            if(session == null){
                return generateResponse(Response.Status.FORBIDDEN, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1003"));
            }

            //Get information stored in session
            String email = (String) session.getAttribute(Constants.SESSION_EMAIL);
            String username = (String) session.getAttribute(Constants.SESSION_USERNAME);
            String firstName = (String) session.getAttribute(Constants.SESSION_FIRSTNAME);
            String lastName = (String) session.getAttribute(Constants.SESSION_LASTNAME);

            //Verify email was not used to generate another account
            if (doesEmailExist(email)) {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "register", "SFABOA-WS-ERR-1005", email);
                return generateResponse(Response.Status.CONFLICT,
                        SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1005"));
            }

            if (!doesAccountExist(username)) {
                String regresponse = SKFSClient.register(username, getOrigin(), input);
                //On success, add user to database
                userdatabase.addUser(email, username, firstName, lastName);

                //Remove registration request from DB
                registrationDB.deleteRegistration(email);
                session.removeAttribute(Constants.SESSION_FIRSTNAME);
                session.removeAttribute(Constants.SESSION_LASTNAME);
                session.removeAttribute(Constants.SESSION_EMAIL);

                session.setAttribute(Constants.SESSION_USERNAME, username);
                session.setAttribute(Constants.SESSION_ISAUTHENTICATED, true);
                session.setMaxInactiveInterval(Constants.SESSION_TIMEOUT_VALUE);
                System.out.println("Received from FIDO Server: " + regresponse);
                return generateResponse(Response.Status.OK, getResponseFromSKFSResponse(regresponse));
            } else {
                //If the user already exists, throw an error
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "register", "SFABOA-WS-ERR-1001", username);
                return generateResponse(Response.Status.CONFLICT, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1001"));
            }
        }
        catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "register", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a user to request a challenge to authenicate
    // themselves
    @POST
    @Path("/" + Constants.RP_PREAUTHENTICATE_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response preauthenticate(JsonObject input){
        try {
            // Get user input + basic input checking
            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);

            // Verify user exists
            if(!userdatabase.doesUserExist(username)){
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "preauthenticate", "SFABOA-WS-ERR-1002", username);
                return generateResponse(Response.Status.NOT_FOUND, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1002"));
            }

            String preauth = SKFSClient.preauthenticate(username);
            HttpSession session = request.getSession(true);
            session.setAttribute(Constants.SESSION_USERNAME, username);
            session.setAttribute(Constants.SESSION_ISAUTHENTICATED, false);
            return generateResponse(Response.Status.OK, preauth);
        } catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "preauthenticate", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Endpoint to send a signed authentication challenge. On successful
    // verification of the signed challenge, the user will be logged in
    @POST
    @Path("/" + Constants.RP_AUTHENTICATE_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response authenticate(JsonObject input){
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "authenticate", "SFABOA-WS-ERR-1003", "");
                return generateResponse(Response.Status.FORBIDDEN, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1003"));
            }

            String agent = request.getHeader("User-Agent");
            String cip = request.getRemoteAddr();
            String username = (String) session.getAttribute(Constants.SESSION_USERNAME);
            if (doesAccountExist(username)) {
                String authresponse = SKFSClient.authenticate(username, getOrigin(), input, cip, agent);
//                session.setAttribute("username", username);
//                session.setAttribute("isAuthenticated", true);
                return generateAuthenticateResponse(Response.Status.OK, getResponseFromSKFSResponse(authresponse),getJWTFromSKFSResponse(authresponse));
            } else {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "authenticate", "SFABOA-WS-ERR-1002", username);
                return generateResponse(Response.Status.CONFLICT, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1002"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "authenticate", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Endpoint that allows the frontend to check whether the user is logged in.
    @POST
    @Path("/" + Constants.RP_ISLOGGEDIN_PATH)
    @Consumes({MediaType.APPLICATION_JSON})
    @Produces({MediaType.APPLICATION_JSON})
    public Response isLoggedIn(JsonObject input) {
        try {
//            String username = getValueFromInput(Constants.RP_JSON_KEY_USERNAME, input);
            String username = input.getString(Constants.RP_JSON_KEY_USERNAME, null);
            if (username == null) {
                username = "";
            }

            Enumeration headers = request.getHeaderNames();
            System.out.println("-------------------------> is logged in");
            while(headers.hasMoreElements()){
            System.out.println((String)headers.nextElement()+" "+request.getHeader((String)headers.nextElement()));
}           
            String jwt64="";
            if(request.getHeader("Authorization")!=null){
               jwt64 = request.getHeader("Authorization").split(" ")[1];

            }
                
            //String username = request.getReader().lines().collect(Collectors.joining());
            String agent = request.getHeader("User-Agent");
            String cip = request.getRemoteAddr();
            String rpid = request.getHeader("Host").split(":")[0].split("\\.",2)[1];
//            truststore location= "/usr/local/strongkey/skfs/keystores"
//            HttpSession session = request.getSession(false);
//            if (session == null) {
//                return generateResponse(Response.Status.OK, "");
//            }
//            String username = (String) session.getAttribute(Constants.SESSION_USERNAME);
//            Boolean isAuthenticated = (Boolean) session.getAttribute(Constants.SESSION_ISAUTHENTICATED);
//            if(username == null || isAuthenticated == null || !isAuthenticated){
//                return generateResponse(Response.Status.OK, "");
//            }
            if(jwt64.isEmpty() || username.isEmpty()){
                return generateResponse(Response.Status.OK, "");
            }
                boolean isValidUser = verify.verify(DID, jwt64, username, agent, cip, JWTPASSWORD, JWTTRUSTSTORELOCATION, rpid);
            System.out.println(isValidUser);
            if(!isValidUser){
                return generateResponse(Response.Status.OK, "");
            //return generateResponse(Response.Status.OK, username);


            }
            
            return generateResponse(Response.Status.OK, username);
        } catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "isLoggedIn", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a logged in user to logout
    @POST
    @Path("/" + Constants.RP_LOGOUT_PATH)
    @Produces({MediaType.APPLICATION_JSON})
    public Response logout() {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                return generateResponse(Response.Status.OK, "");
            }
            session.invalidate();
            return generateResponse(Response.Status.OK, "");
        } catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "isLoggedIn", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a logged in user delete their account. This endpoint
    // will delete their account and delete all keys associated with their account
    // on the SKFS
    @POST
    @Path("/" + Constants.RP_PATH_DELETEACCOUNT)
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteAccount() {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "deleteAccount", "SFABOA-WS-ERR-1003", "");
                return generateResponse(Response.Status.FORBIDDEN, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1003"));
            }

            String username = (String) session.getAttribute(Constants.SESSION_USERNAME);
            Boolean isAuthenticated = (Boolean) session.getAttribute(Constants.SESSION_ISAUTHENTICATED);
            if (isAuthenticated) {
                userdatabase.deleteUser(username);
                String SKFSResponse = SKFSClient.getKeys(username);
                JsonArray keyIds = getKeyIdsFromSKFSResponse(SKFSResponse);
                System.out.println(keyIds);
                for (int keyIndex = 0; keyIndex < keyIds.size(); keyIndex++) {
                    SKFSClient.deregisterKey(keyIds.getJsonObject(keyIndex).getString(Constants.SKFS_RESPONSE_JSON_KEY_RANDOMID));
                }
                session.invalidate();
                return generateResponse(Response.Status.OK, "Success");
            } else {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "deleteAccount", "SFABOA-WS-ERR-1002", username);
                return generateResponse(Response.Status.CONFLICT, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1002"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "deleteAccount", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Endpoint that allows a logged in user to request all keys that they
    // registered on their account
    @POST
    @Path("/" + Constants.RP_PATH_GETUSERINFO)
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserInfo() {
        try {
            HttpSession session = request.getSession(false);
            if (session == null) {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "getUserInfo", "SFABOA-WS-ERR-1003", "");
                return generateResponse(Response.Status.FORBIDDEN, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1003"));
            }

            String username = (String) session.getAttribute(Constants.SESSION_USERNAME);
            Boolean isAuthenticated = (Boolean) session.getAttribute(Constants.SESSION_ISAUTHENTICATED);
            if (isAuthenticated) {
                String keys = SKFSClient.getKeys(username);
                JsonObject keysJson = Common.parseJsonFromString(keys);
                JsonObject userJson = userdatabase.getUserInfoFromUsername(username);

                return generateResponse(Response.Status.OK,
                        Json.createObjectBuilder()
                            .add(Constants.RP_JSON_KEY_KEYS, keysJson)
                            .add(Constants.RP_JSON_KEY_USERINFO, userJson)
                            .build().toString());
            } else {
                SFABOALogger.logp(Level.SEVERE, CLASSNAME, "getUserInfo", "SFABOA-WS-ERR-1002", username);
                return generateResponse(Response.Status.CONFLICT, SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1002"));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "getUserInfo", "SFABOA-WS-ERR-1000", ex.getLocalizedMessage());
            return generateResponse(Response.Status.INTERNAL_SERVER_ERROR,
                    SFABOALogger.getMessageProperty("SFABOA-WS-ERR-1000"));
        }
    }

    // Retrieves the protocol + domain + port (eg. https://localhost:8181) that
    // the enduser send the request to
    private String getOrigin() throws URISyntaxException{
        URI requestURL = new URI(request.getRequestURL().toString());
        return requestURL.getScheme() + "://" + requestURL.getAuthority();
    }

    private String getRegistrationTemplate() throws FileNotFoundException, IOException{
        StringBuilder sb = new StringBuilder();
        File file = new File(getClass().getClassLoader().getResource("resources/email_register.html").getFile());
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while((line = br.readLine()) != null){
            sb.append(line);
        }
        return sb.toString();
    }

    // Return whether an account with this username already exists
    private boolean doesAccountExist(String username){
        return userdatabase.doesUserExist(username);
    }

    // Return whether request comes from a valid registration link
    private boolean isValidNonce(String nonce){
        return registrationDB.doesRegistrationForNonceExist(nonce);
    }

    // Return whether an account with this email already exists
    private boolean doesEmailExist(String email) {
        return userdatabase.doesEmailExist(email);
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
    private JsonArray getKeyIdsFromInput(JsonObject input){
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

    private String generateNonce(){
        byte[] result = new byte[32];
        nonceGen.nextBytes(result);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(result);
    }


    // A standard method of communicating with the frontend code. This can be
    // modified as needed to fit the needs of the site being build.
    private Response generateResponse(Status status, String responsetext) {
        String response = status.equals(Response.Status.OK) ? responsetext : "";
        String message = status.equals(Response.Status.OK) ? "": responsetext;
        String error = status.equals(Response.Status.OK) ? Constants.RP_JSON_VALUE_FALSE_STRING: Constants.RP_JSON_VALUE_TRUE_STRING;
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
        String message = status.equals(Response.Status.OK) ? "": responsetext;
        String error = status.equals(Response.Status.OK) ? Constants.RP_JSON_VALUE_FALSE_STRING: Constants.RP_JSON_VALUE_TRUE_STRING;
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
