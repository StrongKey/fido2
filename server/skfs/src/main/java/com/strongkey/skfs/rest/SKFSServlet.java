/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.rest;

import com.strongkey.auth.txbeans.authenticateRestRequestBeanLocal;
import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
import com.strongkey.skfs.requests.AuthenticationRequest;
import com.strongkey.skfs.requests.DeregisterRequest;
import com.strongkey.skfs.requests.GetKeysInfoRequest;
import com.strongkey.skfs.requests.PatchFidoKeyRequest;
import com.strongkey.skfs.requests.PreauthenticationRequest;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.requests.RegistrationRequest;
import com.strongkey.skfs.requests.ServiceInfo;
import com.strongkey.skfs.txbeans.pingBeanLocal;
import com.strongkey.skfs.txbeans.u2fServletHelperBeanLocal;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.json.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

/**
 * REST based web services that serve FIDO U2F protocol based functionality.
 *
 */
@Path("")
public class SKFSServlet {

    @javax.ws.rs.core.Context
    private HttpServletRequest request;
    @EJB
    u2fServletHelperBeanLocal u2fHelperBean = lookup_u2fServletHelperBeanLocal();
    @EJB
    pingBeanLocal   pingbean = lookup_pingBeanLocal();
    @EJB
    authorizeLdapUserBeanLocal authorizebean = lookupauthorizeLdapUserBeanLocal(); // ldap user authorization bean

    @EJB authenticateRestRequestBeanLocal authRest = lookupauthenticateRestRequestBeanLocall();

    public SKFSServlet() {
    }

    /**
     * methods to look up for ejb resources
     */
    private u2fServletHelperBeanLocal lookup_u2fServletHelperBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (u2fServletHelperBeanLocal) c.lookup("java:app/fidoserverbeans-4.3.0/u2fServletHelperBean!com.strongkey.skfs.txbeans.u2fServletHelperBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private pingBeanLocal lookup_pingBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (pingBeanLocal) c.lookup("java:app/fidoserverbeans-4.3.0/pingBean!com.strongkey.skfs.txbeans.pingBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private authorizeLdapUserBeanLocal lookupauthorizeLdapUserBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (authorizeLdapUserBeanLocal) c.lookup("java:app/authenticationBeans-4.3.0/authorizeLdapUserBean!com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private authenticateRestRequestBeanLocal lookupauthenticateRestRequestBeanLocall() {
        try {
            javax.naming.Context c = new InitialContext();
            return (authenticateRestRequestBeanLocal) c.lookup("java:app/authenticationBeans-4.3.0/authenticateRestRequestBean!com.strongkey.auth.txbeans.authenticateRestRequestBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    /**
     * Step-1 for fido authenticator registration. This methods generates a
     * challenge and returns the same to the caller, which typically is a
     * Relying Party (RP) application.
     *
     * @param input
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Reg Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @POST
    @Path("/preregister")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response preregister(String input) {

        ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        PreregistrationRequest pregreq = new PreregistrationRequest();
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        JsonObject preregpayload = inputJson.getJsonObject("payload");

        if (preregpayload.containsKey("username")) {
            pregreq.setUsername(preregpayload.getString("username"));
        }

        if (preregpayload.containsKey("displayname")) {
            pregreq.setDisplayname(preregpayload.getString("displayname"));
        }

        if (preregpayload.containsKey("options")) {
            pregreq.setOptions(preregpayload.getString("options"));
        }

        if (preregpayload.containsKey("extensions")) {
            pregreq.setExtensions(preregpayload.getString("extensions"));
        }

        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, pregreq)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        pregreq.setProtocol(svcinfoObj.getProtocol());
        return u2fHelperBean.preregister(svcinfoObj.getDid(), pregreq);
    }

    /**
     * Step-2 or last step of fido authenticator registration process. This
     * method receives the u2f registration response parameters which is
     * processed and the registration result is notified back to the caller.
     *
     * Both preregister and register methods are time linked. Meaning, register
     * should happen with in a certain time limit after the preregister is
     * finished; otherwise, the user session would be invalidated.
     *
     * @param svcinfo
     * @param registration - String The full body for auth purposes
     * @param did - Long value of the domain to service this request

     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @POST
    @Path("/register")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response register(String input) {

        ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }

        //convert payload to pre reg object
        RegistrationRequest registration = new RegistrationRequest();
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        JsonObject regpayload = inputJson.getJsonObject("payload");

        if (regpayload.containsKey("metadata")) {
            registration.setMetadata(regpayload.getString("metadata"));
        }

        if (regpayload.containsKey("response")) {
            registration.setResponse(regpayload.getString("response"));
        }

        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, registration)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        registration.setProtocol(svcinfoObj.getProtocol());
        return u2fHelperBean.register(svcinfoObj.getDid(), registration);
    }

    /**
     * Step-1 for fido authenticator authentication. This methods generates a
     * challenge and returns the same to the caller.
     *
     * @param svcinfo
     * @param preauthentication- String The full body for auth purposes
     * @param did - Long value of the domain to service this request
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Auth Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @POST
    @Path("/preauthenticate")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response preauthenticate(String input) {

        ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }

        //convert payload to pre reg object
        PreauthenticationRequest pauthreq = new PreauthenticationRequest();
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        JsonObject preauthpayload = inputJson.getJsonObject("payload");

        if (preauthpayload.containsKey("username")) {
            pauthreq.setUsername(preauthpayload.getString("username"));
        }

        if (preauthpayload.containsKey("options")) {
            pauthreq.setOptions(preauthpayload.getString("options"));
        }

        if (preauthpayload.containsKey("extensions")) {
            pauthreq.setExtensions(preauthpayload.getString("extensions"));
        }

        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, pauthreq)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        pauthreq.setProtocol(svcinfoObj.getProtocol());
        return u2fHelperBean.preauthenticate(svcinfoObj.getDid(), pauthreq);
    }

    /**
     * Step-2 or last step of fido authenticator authentication process. This
     * method receives the u2f authentication response parameters which is
     * processed and the authentication result is notified back to the caller.
     *
     * Both preauthenticate and authenticate methods are time linked. Meaning,
     * authenticate should happen with in a certain time limit after the
     * preauthenticate is finished; otherwise, the user session would be
     * invalidated.
     *
     * @param svcinfo
     * @param authentication - String The full body for auth purposes
     * @param did - Long value of the domain to service this request
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @POST
    @Path("/authenticate")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response authenticate(String input) {

        ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }

        //convert payload to pre reg object
        AuthenticationRequest authentication = new AuthenticationRequest();
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        JsonObject authpayload = inputJson.getJsonObject("payload");

        if (authpayload.containsKey("metadata")) {
            authentication.setMetadata(authpayload.getString("metadata"));
        }

        if (authpayload.containsKey("response")) {
            authentication.setResponse(authpayload.getString("response"));
        }

        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, authentication)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        authentication.setProtocol(svcinfoObj.getProtocol());
        return u2fHelperBean.authenticate(svcinfoObj.getDid(), authentication);
    }

    @POST
    @Path("/deregister")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response deregister(String input) {

        ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }

        //convert payload to pre reg object
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        JsonObject deregpayload = inputJson.getJsonObject("payload");
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        DeregisterRequest deregreq = new DeregisterRequest();
        if(deregpayload.containsKey("keyid")){
            deregreq.setKeyid(deregpayload.getString("keyid"));
        }

        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, deregreq)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        return u2fHelperBean.deregister(svcinfoObj.getDid(), deregreq.getKeyid());
    }


    /**
     * The process of activating an already registered but de-activated fido
     * authenticator. This process will turn the status of the key in the
     * database back to ACTIVE. The inputs needed are the name of the user and
     * the random id to point to a unique registered key for that user. This
     * random id can be obtained by calling getkeysinfo method.
     *
     * @param svcinfo
     * @param patchkey -
     * @param did - Long value of the domain to service this request
     * @param kid - String value of the key to deregister
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in activation 3. 'Error' : String, with error
     * message incase something went wrong. Will be empty if successful.
     */
    @POST
    @Path("/updatekeyinfo")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response updatekeyinfo(String input) {

        ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }

        //convert payload to pre reg object
        PatchFidoKeyRequest patchreq = new PatchFidoKeyRequest();
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        JsonObject patchpayload = inputJson.getJsonObject("payload");

        String keyid="";

        if(patchpayload.containsKey("status")){
            patchreq.setStatus(patchpayload.getString("status"));
        }
        if(patchpayload.containsKey("modify_location")){
            patchreq.setModify_location(patchpayload.getString("modify_location"));
        }
        if(patchpayload.containsKey("displayname")){
            patchreq.setDisplayname(patchpayload.getString("displayname"));
        }
        if(patchpayload.containsKey("keyid")){
            patchreq.setKeyid(patchpayload.getString("keyid"));
            keyid = patchpayload.getString("keyid");
        }

        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, patchreq)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        return u2fHelperBean.patchfidokey(svcinfoObj.getDid(), keyid, patchreq);
    }

    /**
     * Method to return a list of user registered fido authenticator
     * information; In short, registered keys information. Information includes
     * the meta data of the key like the place and time it was registered and
     * used (last modified) from, a random id (which has a time-to-live) that
     * has to be sent back as a token during de-registration.
     *
     * @param svcinfo
     * @param did
     * @param username - The username we are finding keys for
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : A Json array, each entry signifying metadata of a key
     * registered; Metadata includes randomid and its time-to-live, creation and
     * modify location and time info etc., 2. 'Message' : Empty string since
     * there is no cryptographic work involved in this process. 3. 'Error' :
     * String, with error message incase something went wrong. Will be empty if
     * successful.
     */

    @POST
    @Path("/getkeysinfo")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response getkeysinfo(String input) {

         ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }

        //convert payload to pre reg object
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        JsonObject getkeyspayload = inputJson.getJsonObject("payload");
        GetKeysInfoRequest getkeysreq = new GetKeysInfoRequest();
        if(getkeyspayload.containsKey("username")){
            getkeysreq.setUsername(getkeyspayload.getString("username"));
        }


        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, getkeysreq)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        return u2fHelperBean.getkeysinfo(svcinfoObj.getDid(), getkeysreq.getUsername());
    }

    @POST
    @Path("/ping")
    @Consumes({"application/json"})
    public Response ping(String input) {
        ServiceInfo svcinfoObj;

        JsonObject inputJson =  skfsCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        svcinfoObj = skfsCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }

        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            if (!authRest.execute(svcinfoObj.getDid(), request, null)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        String response;
        try {
            response = pingbean.execute(svcinfoObj.getDid());
            return Response.status(Response.Status.OK).entity(response).build();
        } catch (Exception ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(ex.getLocalizedMessage()).build();
        }

    }

    @POST
    @Path("/preauthorize")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response preauthorize(String input) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("not yet implemeted").build();
    }

    @POST
    @Path("/authorize")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response authorize(String input) {
        return Response.status(Response.Status.NOT_IMPLEMENTED).entity("not yet implemeted").build();
    }

    private Response checksvcinfoerror(ServiceInfo svcinfo){
        if(svcinfo.getErrormsg() != null){
            String errormsg = svcinfo.getErrormsg();
            if(errormsg.contains("FIDO-ERR-0033")){
                return Response.status(Response.Status.UNAUTHORIZED).entity(errormsg).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).entity(errormsg).build();
            }
        }
        return null;
    }
}
