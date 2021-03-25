/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.rest;


import com.strongkey.auth.txbeans.authenticateRestRequestBeanLocal;
import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
import com.strongkey.skfs.policybeans.addFidoPolicyLocal;
import com.strongkey.skfs.policybeans.deleteFidoPolicyLocal;
import com.strongkey.skfs.policybeans.getFidoPolicyLocal;
import com.strongkey.skfs.policybeans.updateFidoPolicyLocal;
import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
import com.strongkey.skfs.requests.PatchFidoPolicyRequest;
import com.strongkey.skfs.requests.ServiceInfo;
import com.strongkey.skfs.txbeans.deleteFIDOConfigurationsLocal;
import com.strongkey.skfs.txbeans.getAllConfigurationsBeanLocal;
import com.strongkey.skfs.txbeans.updateFIDOConfigurationLocal;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;


@Path("")
public class FidoAdminServlet {

    @javax.ws.rs.core.Context 
    private HttpServletRequest request;
    @EJB authenticateRestRequestBeanLocal authRest;
     @EJB
    authorizeLdapUserBeanLocal authorizebean = lookupauthorizeLdapUserBeanLocal(); // ldap user authorization bean

    @EJB 
    addFidoPolicyLocal addpolicybean;
    @EJB 
    getFidoPolicyLocal getpolicybean;
    @EJB 
    updateFidoPolicyLocal updatepolicybean ;
    @EJB 
    deleteFidoPolicyLocal deletepolicybean;
    
    @EJB
    updateFIDOConfigurationLocal updateFidoConfig;
    @EJB
    getAllConfigurationsBeanLocal getallFidoConfig;
    @EJB
    deleteFIDOConfigurationsLocal deltefidoconfig;

    public FidoAdminServlet() {
        this.authRest = lookupauthenticateRestRequestBeanLocal();
        this.getpolicybean = lookupgetFidoPolicyLocal();
        this.updatepolicybean = lookupActionFidoPolicyLocal();
        this.addpolicybean = lookupaddFidoPolicyLocal();
        this.deletepolicybean = lookupdeleteFidoPolicyLocal();
        this.updateFidoConfig = lookupupdateFidoConfigLocal();
        this.getallFidoConfig = lookupgetAllFidoConfigLocal();
        this.deltefidoconfig = lookupdeleteFidoConfigLocal();
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
    private deleteFIDOConfigurationsLocal lookupdeleteFidoConfigLocal() {
        try {
            javax.naming.Context c = new InitialContext();
            return (deleteFIDOConfigurationsLocal) c.lookup("java:app/fidoserverbeans-4.4.0/deleteFIDOConfigurations!com.strongkey.skfs.txbeans.deleteFIDOConfigurationsLocal");
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
        ServiceInfo svcinfoObj;
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        
        CreateFidoPolicyRequest createfidopolicy = new CreateFidoPolicyRequest(); 
        createfidopolicy.setNotes(payloadjson.getString("notes"));
        createfidopolicy.setPolicy(payloadjson.getString("policy"));
        createfidopolicy.setStatus(payloadjson.getString("status"));    
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
        } else {
            if (!authRest.execute(did, request, payloadjson.toString())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
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
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        Long did = Long.valueOf(payloadjson.getString("did"));
        Long sid = Long.valueOf(payloadjson.getString("sid"));
        Long pid = Long.valueOf(payloadjson.getString("pid"));
        Boolean metadataonly = Boolean.valueOf(payloadjson.getString("metadataonly"));
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 did = " + did);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 sid = "+ sid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 pid = "+ pid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 metadataonly = "+metadataonly);
        ServiceInfo svcinfoObj;
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
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
        } else {
            if (!authRest.execute(did, request, payloadjson.toString())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }
        return getpolicybean.getPolicies(did, sid, pid, metadataonly);
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
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        Long did = Long.valueOf(payloadjson.getString("did"));
        Long pid = Long.valueOf(payloadjson.getString("pid"));
        Long sid = Long.valueOf(payloadjson.getString("sid"));
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 did = " + did);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 sid = "+ sid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0062 pid = "+ pid);
        
        
        ServiceInfo svcinfoObj;
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
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
        } else {
            if (!authRest.execute(did, request, payloadjson.toString())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
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
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(payloadjson.getString("did"));
        Long pid = Long.valueOf(payloadjson.getString("pid"));
        Long sid = Long.valueOf(payloadjson.getString("sid"));
        
        ServiceInfo svcinfoObj;
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        
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
        } else {
            if (!authRest.execute(did, request, payloadjson.toString())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
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
        ServiceInfo svcinfoObj;
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        
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
        } else {
            if (!authRest.execute(did, request, payloadjson.toString())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
        }

        JsonArray configarray;
        try {
            System.out.println(payloadjson.toString());
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
        ServiceInfo svcinfoObj;
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
//        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        
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
        } else {
            if (!authRest.execute(did, request, null)) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
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
        ServiceInfo svcinfoObj;
        JsonObject svcinfo = inputJson.getJsonObject("svcinfo");
        JsonObject payloadjson = inputJson.getJsonObject("payload");
        
        Long did = Long.valueOf(svcinfo.getInt("did"));
        
        
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
        } else {
            if (!authRest.execute(did, request, payloadjson.toString())) {
                return Response.status(Response.Status.UNAUTHORIZED).build();
            }
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
}
