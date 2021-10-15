/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.rest;


import com.strongkey.auth.txbeans.authenticateRestRequestBeanLocal;
import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
import com.strongkey.skce.utilities.SKCEException;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.jwt.JWTVerifyLocal;
import com.strongkey.skfs.ldap.LDAPOperations;
import com.strongkey.skfs.policybeans.addFidoPolicyLocal;
import com.strongkey.skfs.policybeans.deleteFidoPolicyLocal;
import com.strongkey.skfs.policybeans.getFidoPolicyLocal;
import com.strongkey.skfs.policybeans.updateFidoPolicyLocal;
import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
import com.strongkey.skfs.requests.PatchFidoPolicyRequest;
import com.strongkey.skfs.requests.ServiceInfo;
import com.strongkey.skfs.txbeans.deleteFIDOConfigurationsLocal;
import com.strongkey.skfs.txbeans.getAllConfigurationsBeanLocal;
import com.strongkey.skfs.txbeans.getFIDOConfigurationLocal;
import com.strongkey.skfs.txbeans.updateFIDOConfigurationLocal;
import com.strongkey.skfs.txbeans.updateFIDOKeysUsernameLocal;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;


@Path("")
public class FidoAdminServlet {

    @Context 
    private HttpServletRequest request;
    @Context
    private HttpServletResponse response;
    
    @EJB
    authorizeLdapUserBeanLocal authorizebean = lookupauthorizeLdapUserBeanLocal(); // ldap user authorization bean

    @EJB
    JWTVerifyLocal jwtverify;
    @EJB 
    addFidoPolicyLocal addpolicybean;
    @EJB 
    getFidoPolicyLocal getpolicybean;
    @EJB 
    updateFidoPolicyLocal updatepolicybean;
    @EJB 
    deleteFidoPolicyLocal deletepolicybean;
    
    @EJB
    updateFIDOConfigurationLocal updateFidoConfig;
    @EJB
    getAllConfigurationsBeanLocal getallFidoConfig;
    @EJB
    getFIDOConfigurationLocal getFidoConfig;
    @EJB
    deleteFIDOConfigurationsLocal deltefidoconfig;
    
    @EJB
    updateFIDOKeysUsernameLocal updateFidoKeysusername;
    
    SKFSServlet fidoServlet = new SKFSServlet();
    
    LDAPOperations ldapoperations = new LDAPOperations();

    public FidoAdminServlet() {
        this.jwtverify = lookupJWTVerifyLocal();
        this.addpolicybean = lookupaddFidoPolicyLocal();
        this.getpolicybean = lookupgetFidoPolicyLocal();
        this.updatepolicybean = lookupActionFidoPolicyLocal();
        this.deletepolicybean = lookupdeleteFidoPolicyLocal();
        this.updateFidoConfig = lookupupdateFidoConfigLocal();
        this.getallFidoConfig = lookupgetAllFidoConfigLocal();
        this.getFidoConfig = lookupgetFIDOConfigurationLocal();
        this.deltefidoconfig = lookupdeleteFidoConfigLocal();
        this.updateFidoKeysusername = lookupupdateFIDOKeysUsernameLocal();
    }
    
    private authenticateRestRequestBeanLocal lookupauthenticateRestRequestBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (authenticateRestRequestBeanLocal) c.lookup("java:app/authenticationBeans-4.4.0/authenticateRestRequestBean!com.strongkey.auth.txbeans.authenticateRestRequestBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    private getFidoPolicyLocal lookupgetFidoPolicyLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (getFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.4.0/getFidoPolicy!com.strongkey.skfs.policybeans.getFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    private JWTVerifyLocal lookupJWTVerifyLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (JWTVerifyLocal) c.lookup("java:app/fidoserverbeans-4.4.0/JWTVerify!com.strongkey.skfs.jwt.JWTVerifyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    private deleteFidoPolicyLocal lookupdeleteFidoPolicyLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (deleteFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.4.0/deleteFidoPolicy!com.strongkey.skfs.policybeans.deleteFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private updateFidoPolicyLocal lookupActionFidoPolicyLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (updateFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.4.0/updateFidoPolicy!com.strongkey.skfs.policybeans.updateFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private addFidoPolicyLocal lookupaddFidoPolicyLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (addFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.4.0/addFidoPolicy!com.strongkey.skfs.policybeans.addFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private updateFIDOConfigurationLocal lookupupdateFidoConfigLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (updateFIDOConfigurationLocal) c.lookup("java:app/fidoserverbeans-4.4.0/updateFIDOConfiguration!com.strongkey.skfs.txbeans.updateFIDOConfigurationLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    private getAllConfigurationsBeanLocal lookupgetAllFidoConfigLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (getAllConfigurationsBeanLocal) c.lookup("java:app/fidoserverbeans-4.4.0/getAllConfigurationsBean!com.strongkey.skfs.txbeans.getAllConfigurationsBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    private getFIDOConfigurationLocal lookupgetFIDOConfigurationLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (getFIDOConfigurationLocal) c.lookup("java:app/fidoserverbeans-4.4.0/getFIDOConfiguration!com.strongkey.skfs.txbeans.getFIDOConfigurationLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    private deleteFIDOConfigurationsLocal lookupdeleteFidoConfigLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (deleteFIDOConfigurationsLocal) c.lookup("java:app/fidoserverbeans-4.4.0/deleteFIDOConfigurations!com.strongkey.skfs.txbeans.deleteFIDOConfigurationsLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    private updateFIDOKeysUsernameLocal lookupupdateFIDOKeysUsernameLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (updateFIDOKeysUsernameLocal) c.lookup("java:app/fidoserverbeans-4.4.0/updateFIDOKeysUsername!com.strongkey.skfs.txbeans.updateFIDOKeysUsernameLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private authorizeLdapUserBeanLocal lookupauthorizeLdapUserBeanLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (authorizeLdapUserBeanLocal) c.lookup("java:app/authenticationBeans-4.4.0/authorizeLdapUserBean!com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    @POST
    @Path("/addpolicy")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response addFidoPolicy(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }
        
        CreateFidoPolicyRequest createfidopolicy = new CreateFidoPolicyRequest(); 
        createfidopolicy.setNotes(payloadjson.getString("notes"));
        createfidopolicy.setPolicy(payloadjson.getString("policy"));
        createfidopolicy.setStatus(payloadjson.getString("status"));
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_POLICY_MANAGEMENT);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return addpolicybean.execute(did, createfidopolicy);
    }

    @POST
    @Path("/getpolicy")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response getFidoPolicy(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(payloadjson.getString("did"));
        Long sid = Long.valueOf(payloadjson.getString("sid"));
        Long pid = Long.valueOf(payloadjson.getString("pid"));
        Boolean metadataonly = Boolean.valueOf(payloadjson.getString("metadataonly"));
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 did = " + did);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 sid = "+ sid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 pid = "+ pid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 metadataonly = "+metadataonly);
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized = Boolean.FALSE;
        boolean isAuthorizedPolicy = Boolean.FALSE;
        boolean isAuthorizedMonitor = Boolean.FALSE;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
                isAuthorizedPolicy = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_POLICY_MANAGEMENT);
                isAuthorizedMonitor = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_MONITOR);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                try {
                    isAuthorizedPolicy = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_POLICY_MANAGEMENT);
                } catch (SKCEException ex1) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                    try {
                        isAuthorizedMonitor = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_MONITOR);
                    } catch (SKCEException ex2) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                        return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
                    }
                }
            }
            if (!isAuthorized) {
                if (!isAuthorizedPolicy) {
                    if (!isAuthorizedMonitor) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                        return Response.status(Response.Status.UNAUTHORIZED).build();
                    }
                }
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        return getpolicybean.getPolicies(did, sid, pid, metadataonly);
    }
    
    @POST
    @Path("/getallpolicies")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response getAllFidoPolicies(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());        
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        JsonArrayBuilder jarrbldr = Json.createArrayBuilder();
        Collection<FidoPolicies> fpc = getpolicybean.getAllActive();
        if (fpc == null) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        for (FidoPolicies fp : fpc) {
            jarrbldr.add(fp.toJson());
        }
        String response = Json.createObjectBuilder()
            .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, jarrbldr)
            .build().toString();
        return Response.ok().entity(response).build();
    }

    @POST
    @Path("/updatepolicy")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response updateFidoPolicy(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        Long did = Long.valueOf(payloadjson.getString("did"));
        Long pid = Long.valueOf(payloadjson.getString("pid"));
        Long sid = Long.valueOf(payloadjson.getString("sid"));
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 did = " + did);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 sid = "+ sid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 pid = "+ pid);
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_POLICY_MANAGEMENT);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        PatchFidoPolicyRequest policyRequest = new PatchFidoPolicyRequest();
        policyRequest.setStatus(payloadjson.getString("status"));
        policyRequest.setNotes(payloadjson.getString("notes"));
        policyRequest.setPolicy(payloadjson.getString("policy"));        
                                
 

        return updatepolicybean.execute(did, sid, pid, policyRequest);
    }

    @POST
    @Path("/deletepolicy")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response deleteFidoPolicy(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        Long did = Long.valueOf(payloadjson.getString("did"));
        Long pid = Long.valueOf(payloadjson.getString("pid"));
        Long sid = Long.valueOf(payloadjson.getString("sid"));
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_POLICY_MANAGEMENT);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return deletepolicybean.execute(did, sid, pid);
    }
   /*
 .d8888b.   .d88888b.  888b    888 8888888888 8888888  .d8888b.  888     888 8888888b.         d8888 88888888888 8888888  .d88888b.  888b    888  .d8888b.  
d88P  Y88b d88P" "Y88b 8888b   888 888          888   d88P  Y88b 888     888 888   Y88b       d88888     888       888   d88P" "Y88b 8888b   888 d88P  Y88b 
888    888 888     888 88888b  888 888          888   888    888 888     888 888    888      d88P888     888       888   888     888 88888b  888 Y88b.      
888        888     888 888Y88b 888 8888888      888   888        888     888 888   d88P     d88P 888     888       888   888     888 888Y88b 888  "Y888b.   
888        888     888 888 Y88b888 888          888   888  88888 888     888 8888888P"     d88P  888     888       888   888     888 888 Y88b888     "Y88b. 
888    888 888     888 888  Y88888 888          888   888    888 888     888 888 T88b     d88P   888     888       888   888     888 888  Y88888       "888 
Y88b  d88P Y88b. .d88P 888   Y8888 888          888   Y88b  d88P Y88b. .d88P 888  T88b   d8888888888     888       888   Y88b. .d88P 888   Y8888 Y88b  d88P 
 "Y8888P"   "Y88888P"  888    Y888 888        8888888  "Y8888P88  "Y88888P"  888   T88b d88P     888     888     8888888  "Y88888P"  888    Y888  "Y8888P" 
    */
    
    
    @POST
    @Path("/deleteconfiguration")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response deleteConfiguration(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());        
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        JsonArray configarray;
        try {
            configarray = payloadjson.getJsonArray("configurations");
        } catch (Exception ex) {
               SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0001"), ex.getMessage());
               return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + ex.getMessage()).build();
        }

        return deltefidoconfig.execute(did, configarray);
    }
    
    @POST
    @Path("/getconfiguration")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response getConfiguration(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
//        JsonObject payloadjson = inputJson.getJsonObject("payload");
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }      
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());        
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized = Boolean.FALSE;
        boolean isAuthorizedMonitor = Boolean.FALSE;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
                isAuthorizedMonitor = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_MONITOR);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                try {
                        isAuthorizedMonitor = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_MONITOR);
                    } catch (SKCEException ex2) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                        return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
                    }
            }
            if (!isAuthorized) {
                if (!isAuthorizedMonitor) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                    return Response.status(Response.Status.UNAUTHORIZED).build();
                }
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return getallFidoConfig.execute(did);
    }
    
    @POST
    @Path("/updateconfiguration")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response updateConfiguration(String input) {
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }       
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());        
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
            if (!ldapoperations.isAdmin(did, username)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        JsonArray configarray;
        try {
            configarray = payloadjson.getJsonArray("configurations");
        } catch (Exception ex) {
               SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0001"), ex.getMessage());
               return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + ex.getMessage()).build();
        }
        return updateFidoConfig.execute(did, configarray);
    }
    
    @POST
    @Path("/adminpreregister")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response preregister(String input) {
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        
        JsonObjectBuilder svcinfo = Json.createObjectBuilder()
                .add("did", inputJson.getInt("did"))
                .add("protocol", "FIDO2_0")
                .add("authtype", "PASSWORD")
                .add("svcusername", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultuser"))
                .add("svcpassword", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultpassword"));
        JsonObjectBuilder payload = Json.createObjectBuilder()
                .add("username", inputJson.getString("username"))
                .add("displayname", inputJson.getString("displayname"))
                .add("options", Json.createObjectBuilder()
                                        .add("attestation", "direct"))
                .add("extensions", "{}");
        JsonObjectBuilder requestBody = Json.createObjectBuilder()
                .add("svcinfo", svcinfo)
                .add("payload", payload);
        
        return fidoServlet.preregister(requestBody.build().toString());
    }
    
    @POST
    @Path("/adminregister")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response register(String input) {
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JsonObjectBuilder svcinfo = Json.createObjectBuilder()
                .add("did", inputJson.getInt("did"))
                .add("protocol", "FIDO2_0")
                .add("authtype", "PASSWORD")
                .add("svcusername", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultuser"))
                .add("svcpassword", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultpassword"));
        JsonObjectBuilder strongkeyMetadata = Json.createObjectBuilder()
                .add("version", "1.0")
                .add("create_location", "Sunnyvale, CA")
                .add("username", inputJson.getString("username"))
                .add("origin", origin);
        JsonObjectBuilder payload = Json.createObjectBuilder()
                .add("strongkeyMetadata", strongkeyMetadata)
                .add("publicKeyCredential", inputJson.getJsonObject("payload"));
        JsonObjectBuilder requestBody = Json.createObjectBuilder()
                .add("svcinfo", svcinfo)
                .add("payload", payload);
        
        Response regresponse = fidoServlet.register(requestBody.build().toString());
        
        JsonReader jsonReader = Json.createReader(new StringReader((String) regresponse.getEntity()));
        JsonObject responseObj = jsonReader.readObject();
        jsonReader.close();
        
        JsonObjectBuilder responsewithuser = Json.createObjectBuilder()
                .add("Response", responseObj.getString("Response"))
                .add("username", inputJson.getString("username"));
        Response regresponsewithuser = Response.status(regresponse.getStatus()).entity(responsewithuser.build()).build();
        
        return regresponsewithuser;
    }
    
    @POST
    @Path("/adminpreauthenticate")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response preauthenticate(String input) {
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        
        JsonObjectBuilder svcinfo = Json.createObjectBuilder()
                .add("did", inputJson.getInt("did"))
                .add("protocol", "FIDO2_0")
                .add("authtype", "PASSWORD")
                .add("svcusername", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultuser"))
                .add("svcpassword", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultpassword"));
        JsonObjectBuilder payload = Json.createObjectBuilder()
                .add("username", inputJson.getString("username"))
                .add("options", Json.createObjectBuilder());
        JsonObjectBuilder requestBody = Json.createObjectBuilder()
                .add("svcinfo", svcinfo)
                .add("payload", payload);
        
        return fidoServlet.preauthenticate(requestBody.build().toString());
    }
    
    @POST
    @Path("/adminauthenticate")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response authenticate(String input) {
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        JsonObjectBuilder svcinfo = Json.createObjectBuilder()
                .add("did", inputJson.getInt("did"))
                .add("protocol", "FIDO2_0")
                .add("authtype", "PASSWORD")
                .add("svcusername", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultuser"))
                .add("svcpassword", SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.service.defaultpassword"));
        JsonObjectBuilder strongkeyMetadata = Json.createObjectBuilder()
                .add("version", "1.0")
                .add("last_used_location", "Sunnyvale, CA")
                .add("username", inputJson.getString("username"))
                .add("origin", origin);
        JsonObjectBuilder payload = Json.createObjectBuilder()
                .add("strongkeyMetadata", strongkeyMetadata)
                .add("publicKeyCredential", inputJson.getJsonObject("payload"));
        JsonObjectBuilder requestBody = Json.createObjectBuilder()
                .add("svcinfo", svcinfo)
                .add("payload", payload);
        Response authresponse = fidoServlet.adminauthenticatehelper(requestBody.build().toString(), request);
        
        JsonReader jsonReader = Json.createReader(new StringReader((String) authresponse.getEntity()));
        JsonObject responseObj = jsonReader.readObject();
        jsonReader.close();
        
        JsonObjectBuilder responsewithuser = Json.createObjectBuilder()
                .add("Response", responseObj.getString("Response"))
                .add("jwt", responseObj.getString("jwt"))
                .add("username", inputJson.getString("username"));
        Response authresponsewithuser = Response.status(authresponse.getStatus()).entity(responsewithuser.build()).build();
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        
        if (jwtverify.execute(String.valueOf(inputJson.getInt("did")), responseObj.getString("jwt"), inputJson.getString("username"), agent, cip, origin)) {
//            return authresponse;
            return authresponsewithuser;
        } else {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
    }
    
    
    @POST
    @Path("/updateusername")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response updateUsername(String input) {
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload"); 
        
        Long did = Long.valueOf(svcinfo.getInt("did"));
        String allowUsernameChange = SKFSCommon.getConfigurationProperty(did,"skfs.cfg.property.allow.changeusername");
        if(allowUsernameChange.equalsIgnoreCase("false")){
            return Response.status(Response.Status.BAD_REQUEST).entity("FIDO Server does not allow username change.").build();
        }
        String originalusername = payloadjson.getString("oldusername");
        String newUsername = payloadjson.getString("newusername");
        
        String agent = request.getHeader("User-Agent");
        String cip = request.getRemoteAddr();
        String username = null;
        String jwt = null;
        String origin = null;
        try {
            URI requestURL = new URI(request.getRequestURL().toString());
            origin = requestURL.getScheme() + "://" + requestURL.getAuthority();
        } catch (URISyntaxException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equalsIgnoreCase("username")) {
                    try {
                        username = URLDecoder.decode(cookie.getValue(), "UTF-8");
                    } catch (UnsupportedEncodingException ex) {
                        Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                if (cookie.getName().equalsIgnoreCase("jwt")) {
                    jwt = cookie.getValue();
                }
            }
        }       
        
        ServiceInfo svcinfoObj;
        svcinfoObj = SKFSCommon.checkSvcInfo("REST", svcinfo.toString());        
        Response svcres = SKFSCommon.checksvcinfoerror(svcinfoObj);
        if(svcres !=null){
            return svcres;
        }
        
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage()).build();
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else if (svcinfoObj.getAuthtype().equalsIgnoreCase("jwt")) {
            if (!jwtverify.execute(String.valueOf(svcinfo.getInt("did")), jwt, username, agent, cip, origin)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } else {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        JsonObject res = SKFSCommon.getJsonObjectFromString(updateFidoKeysusername.execute(did, originalusername, newUsername,"Cupertino, CA"));
        if(res.getBoolean("status")){
            return Response.ok().entity(res.toString()).build();
        }else{
            return Response.status(Response.Status.BAD_REQUEST).entity(res.getString("message")).build();
        }
        
     }
    
    @POST
    @Path("/getUserFromHash")
    @Consumes({"application/json"})
    public Response adminSetup(String input) {
        
        long did = 1;
        
        JsonObject inputJson =  SKFSCommon.getJsonObjectFromString(input);
        if (inputJson == null) {
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " input").build();
        }
        String inputHash = inputJson.getString("hash");
        if (inputHash == "" || inputHash == null) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }
        
        // Check if the hash passed in matches the hash in the database.
        String adminSetupHash = getFidoConfig.getByPK(did, "skfs.cfg.property.install.date.hash").getConfigValue();
        if (!adminSetupHash.equals(inputHash)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "Input hash matches hash created during install");
        
        // Check if FidoAdminAuthorized is empty.
        try {
            if (ldapoperations.hasAdmins(did)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        } catch (NamingException ex) {
            Logger.getLogger(FidoAdminServlet.class.getName()).log(Level.SEVERE, null, ex);
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        
        // No other admins found and hash verified, remove hash from db and respond with 200.
        JsonArray configarray;
        try {
            configarray = Json.createArrayBuilder()
                    .add("skfs.cfg.property.install.date.hash")
                    .build();
        } catch (Exception ex) {
               SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0001"), ex.getMessage());
               return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + ex.getMessage()).build();
        }
        //deltefidoconfig.execute(did, configarray); // not deleting hash just yet (for testing purposes)
        return Response.status(Response.Status.ACCEPTED).build();
    }
}
