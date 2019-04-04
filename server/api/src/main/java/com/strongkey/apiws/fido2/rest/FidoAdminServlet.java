/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.apiws.fido2.rest;

//import com.strongauth.appliance.utilities.applianceCommon;
//import com.strongauth.appliance.utilities.strongkeyLogger;
//import com.strongauth.skfe.entitybeans.FidoPolicies;
//import com.strongauth.skce.utilities.SKCEException;
//import com.strongauth.skfe.utilities.skfeCommon;
//import com.strongauth.skfe.policybeans.addFidoPolicyLocal;
//import com.strongauth.skfe.policybeans.deleteFidoPolicyLocal;
//import com.strongauth.skfe.policybeans.getFidoPolicyLocal;
//import com.strongauth.skfe.policybeans.updateFidoPolicyLocal;
//import com.strongauth.skfe.utilities.SKFEException;
//import com.strongauth.skfe.utilities.skfeConstants;
//import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
//import java.util.Base64;
//import java.util.Date;
//import java.util.logging.Level;
//import java.util.stream.Collectors;
//import javax.ejb.EJB;
//import javax.naming.Context;
//import javax.naming.InitialContext;
//import javax.naming.NamingException;
//import javax.ws.rs.Consumes;
//import javax.ws.rs.FormParam;
//import javax.ws.rs.POST;
//import javax.ws.rs.Path;
//import javax.ws.rs.Produces;
//import javax.ws.rs.core.MediaType;
//import javax.ws.rs.core.Response;
//import org.everit.json.schema.ValidationException;
//import org.json.JSONObject;
//
//@Path("")
//public class FidoAdminServlet {
//    
//    //TODO work on a unified return object
//    
//       
//    @EJB
//    authorizeLdapUserBeanLocal authorizebean = lookup_authorizeLdapUserBeanLocal();
//    @EJB
//    addFidoPolicyLocal addpolicybean = lookup_addFidoPolicyLocal();
//    @EJB
//    getFidoPolicyLocal getpolicybean = lookup_getFidoPolicyLocal();
//    @EJB
//    updateFidoPolicyLocal updatepolicybean = lookup_updateFidoPolicyLocal();
//    @EJB
//    deleteFidoPolicyLocal deletepolicybean = lookup_deleteFidoPolicyLocal();
//    
//    /**
//     * methods to look up for ejb resources
//     */
//    private authorizeLdapUserBeanLocal lookup_authorizeLdapUserBeanLocal() {
//        try {
//            Context c = new InitialContext();
//            return (authorizeLdapUserBeanLocal) c.lookup("java:app/authenticationBeans-4.0/authorizeLdapUserBean!com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal");
//        } catch (NamingException ne) {
//            throw new RuntimeException(ne);
//        }
//    }
//    
//    private addFidoPolicyLocal lookup_addFidoPolicyLocal() {
//        try {
//            Context c = new InitialContext();
//            return (addFidoPolicyLocal) c.lookup("java:app/skfebeans-4.0/addFidoPolicy!com.strongauth.skfe.policybeans.addFidoPolicyLocal");
//        } catch (NamingException ne) {
//            throw new RuntimeException(ne);
//        }
//    }
//    
//    private getFidoPolicyLocal lookup_getFidoPolicyLocal() {
//        try {
//            Context c = new InitialContext();
//            return (getFidoPolicyLocal) c.lookup("java:app/skfebeans-4.0/getFidoPolicy!com.strongauth.skfe.policybeans.getFidoPolicyLocal");
//        } catch (NamingException ne) {
//            throw new RuntimeException(ne);
//        }
//    }
//    
//    private updateFidoPolicyLocal lookup_updateFidoPolicyLocal() {
//        try {
//            Context c = new InitialContext();
//            return (updateFidoPolicyLocal) c.lookup("java:app/skfebeans-4.0/updateFidoPolicy!com.strongauth.skfe.policybeans.updateFidoPolicyLocal");
//        } catch (NamingException ne) {
//            throw new RuntimeException(ne);
//        }
//    }
//    
//    private deleteFidoPolicyLocal lookup_deleteFidoPolicyLocal() {
//        try {
//            Context c = new InitialContext();
//            return (deleteFidoPolicyLocal) c.lookup("java:app/skfebeans-4.0/deleteFidoPolicy!com.strongauth.skfe.policybeans.deleteFidoPolicyLocal");
//        } catch (NamingException ne) {
//            throw new RuntimeException(ne);
//        }
//    }
//    
//    @POST
//    @Path("/" + skfeConstants.CREATE_FIDO_POLICY)
//    @Consumes({"application/x-www-form-urlencoded"})
//    @Produces({MediaType.APPLICATION_JSON})
//    public Response createFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
//            @FormParam("payload") JSONObject payload){
//        try{
//            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
//            RestInputValidator.DEFAULT.validateCreatePayloadExistance(payload);
//            
//            String didString = svcinfo.get("did").toString();
//            skfeCommon.inputValidateSKCEDid(didString);
//            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString), 
//                    svcinfo.get("svcusername").toString(), 
//                    svcinfo.get("svcpassword").toString(), 
//                    skfeConstants.LDAP_ROLE_ADM);  //TODO create a new role for FIDO admins
//            
//            if(!isAuthorized){
//                strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
//                return Response.status(Response.Status.BAD_REQUEST).entity(
//                        new FIDOResponse("", "", skfeCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
//            }
//            
//            Long did = svcinfo.getLong("did");
//            JSONObject metadata = payload.getJSONObject("metadata");
//            Date startDate = new Date(metadata.getLong("startdate") * 1000);    //seconds to ms. JPA only only stores dates to the second.
//            long endDateLong = metadata.optLong("enddate");
//            Date endDate = (endDateLong != 0)? new Date(endDateLong * 1000) : null;
//            String certificateProfileName = metadata.getString("name");
//            JSONObject policy = payload.getJSONObject("policy");
//            int version = metadata.getInt("version");
//            String status = metadata.getString("status");
//            String notes = metadata.optString("notes", null);
//            Integer pid = addpolicybean.execute(did, startDate, endDate, certificateProfileName, policy.toString(), version, status, notes);
//            return Response.status(Response.Status.OK).entity(new FIDOResponse(pid.toString(), "", "").toString()).build();
//        }
//        catch (ValidationException ex) {
//            return handleValidationException(ex);
//        } catch (SKFEException ex) {
//            ex.printStackTrace();
//            strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
//            return Response.status(Response.Status.BAD_REQUEST).entity(
//                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
//        }
//    }
//    
//    @POST
//    @Path("/" + skfeConstants.READ_FIDO_POLICY)
//    @Consumes({"application/x-www-form-urlencoded"})
//    @Produces({MediaType.APPLICATION_JSON})
//    public Response readFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
//            @FormParam("payload") JSONObject payload){
//        try{
//            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
//            RestInputValidator.DEFAULT.validateReadPayloadExistance(payload);
//            
//            String didString = svcinfo.get("did").toString();
//            skfeCommon.inputValidateSKCEDid(didString);
//            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString), 
//                    svcinfo.get("svcusername").toString(), 
//                    svcinfo.get("svcpassword").toString(), 
//                    skfeConstants.LDAP_ROLE_ADM); 
//            
//            if(!isAuthorized){
//                strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
//                return Response.status(Response.Status.BAD_REQUEST).entity(
//                        new FIDOResponse("", "", skfeCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
//            }
//            
//            Long did = svcinfo.getLong("did");
//            Long sid = applianceCommon.getServerId();
//            JSONObject metadata = payload.getJSONObject("metadata");
//            Long pid = metadata.getLong("pid");
//            FidoPolicies policy = getpolicybean.getbyPK(did, sid, pid);
//            return Response.status(Response.Status.OK).entity(new FIDOResponse(printFidoPolicy(policy).toString(), "", "").toString()).build();
//        }
//        catch (ValidationException ex) {
//            return handleValidationException(ex);
//        } catch (SKCEException ex) {
//            strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
//            return Response.status(Response.Status.BAD_REQUEST).entity(
//                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
//        }
//    }
//    
//    @POST
//    @Path("/" + skfeConstants.UPDATE_FIDO_POLICY)
//    @Consumes({"application/x-www-form-urlencoded"})
//    @Produces({MediaType.APPLICATION_JSON})
//    public Response updateFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
//            @FormParam("payload") JSONObject payload){
//        try {
//            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
//            RestInputValidator.DEFAULT.validateUpdatePayloadExistance(payload);
//
//            String didString = svcinfo.get("did").toString();
//            skfeCommon.inputValidateSKCEDid(didString);
//            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
//                    svcinfo.get("svcusername").toString(),
//                    svcinfo.get("svcpassword").toString(),
//                    skfeConstants.LDAP_ROLE_ADM); 
//
//            if (!isAuthorized) {
//                strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
//                return Response.status(Response.Status.BAD_REQUEST).entity(
//                        new FIDOResponse("", "", skfeCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
//            }
//
//            Long did = svcinfo.getLong("did");
//            JSONObject metadata = payload.getJSONObject("metadata");
//            Long pid = metadata.getLong("pid");
//            long startDateLong = metadata.optLong("startdate", 0);
//            Date startDate = (startDateLong != 0) ? new Date(startDateLong * 1000) : null;
//            long endDateLong = metadata.optLong("enddate", 0);
//            Date endDate = (endDateLong != 0) ? new Date(endDateLong * 1000) : null;
//            JSONObject policy = payload.optJSONObject("policy");
//            Integer version = metadata.optInt("version");
//            String status = metadata.optString("status", null);
//            String notes = metadata.optString("notes", null);
//            updatepolicybean.excecute(did, pid, startDate, endDate, policy.toString(), version, status, notes);
//            return Response.status(Response.Status.OK).entity(new FIDOResponse("Updated", "", "").toString()).build();
//        } catch (ValidationException ex) {
//            return handleValidationException(ex);
//        } catch (SKCEException ex) {
//            strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
//            return Response.status(Response.Status.BAD_REQUEST).entity(
//                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
//        }
//    }
//    
//    @POST
//    @Path("/" + skfeConstants.DELETE_FIDO_POLICY)
//    @Consumes({"application/x-www-form-urlencoded"})
//    @Produces({MediaType.APPLICATION_JSON})
//    public Response deleteFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
//            @FormParam("payload") JSONObject payload) {
//        try {
//            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
//            RestInputValidator.DEFAULT.validateDeletePayloadExistance(payload);
//
//            String didString = svcinfo.get("did").toString();
//            skfeCommon.inputValidateSKCEDid(didString);
//            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
//                    svcinfo.get("svcusername").toString(),
//                    svcinfo.get("svcpassword").toString(),
//                    skfeConstants.LDAP_ROLE_ADM);  
//
//            if (!isAuthorized) {
//                strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
//                return Response.status(Response.Status.BAD_REQUEST).entity(
//                        new FIDOResponse("", "", skfeCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
//            }
//
//            Long did = svcinfo.getLong("did");
//            Long sid = applianceCommon.getServerId();
//            JSONObject metadata = payload.getJSONObject("metadata");
//            Long pid = metadata.getLong("pid");
//            deletepolicybean.execute(did, sid, pid);
//            return Response.status(Response.Status.OK).entity(new FIDOResponse("Deleted", "", "").toString()).build();
//        } catch (ValidationException ex) {
//            return handleValidationException(ex);
//        } catch (SKCEException ex) {
//            strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
//            return Response.status(Response.Status.BAD_REQUEST).entity(
//                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
//        }
//    }
//    
//    private Response handleValidationException(ValidationException ex){
//        String error = ex.getAllMessages().stream().collect(Collectors.joining(", "));
//        strongkeyLogger.log(skfeConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", error);
//        return Response.status(Response.Status.BAD_REQUEST).entity(new FIDOResponse("", "", error).toString()).build();
//    }
//    
//    private JSONObject printFidoPolicy(FidoPolicies policy){
//        JSONObject result = new JSONObject();
//        String policyBase64 = new String(Base64.getUrlDecoder().decode(policy.getPolicy()));
//        JSONObject policyJSON = new JSONObject(policyBase64);
//        result.put("name", policy.getCertificateProfileName());
//        result.put("startdate", policy.getStartDate());
//        result.put("enddate", policy.getEndDate() == null ? JSONObject.NULL : policy.getEndDate());
//        result.put("policy", policyJSON);
//        result.put("version", policy.getVersion());
//        result.put("status", policy.getStatus());
//        result.put("notes", policy.getNotes() == null ? JSONObject.NULL : policy.getNotes());
//        return result;
//    }
//}
