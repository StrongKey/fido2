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
 * Copyright (c) 2001-2018 StrongAuth, Inc.
 *
 * $Date: 
 * $Revision:
 * $Author: mishimoto $
 * $URL: 
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
 *
 *
 */
package com.strongkey.skfews.fido2.rest;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.policybeans.addFidoPolicyLocal;
import com.strongkey.skfs.policybeans.deleteFidoPolicyLocal;
import com.strongkey.skfs.policybeans.getFidoPolicyLocal;
import com.strongkey.skfs.policybeans.updateFidoPolicyLocal;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.everit.json.schema.ValidationException;
import org.json.JSONObject;

@Path("")
public class FidoAdminServlet {
    
    //TODO work on a unified return object
    
       
    @EJB
    authorizeLdapUserBeanLocal authorizebean = lookup_authorizeLdapUserBeanLocal();
    @EJB
    addFidoPolicyLocal addpolicybean = lookup_addFidoPolicyLocal();
    @EJB
    getFidoPolicyLocal getpolicybean = lookup_getFidoPolicyLocal();
    @EJB
    updateFidoPolicyLocal updatepolicybean = lookup_updateFidoPolicyLocal();
    @EJB
    deleteFidoPolicyLocal deletepolicybean = lookup_deleteFidoPolicyLocal();
    
    /**
     * methods to look up for ejb resources
     */
    private authorizeLdapUserBeanLocal lookup_authorizeLdapUserBeanLocal() {
        try {
            Context c = new InitialContext();
            return (authorizeLdapUserBeanLocal) c.lookup("java:app/authenticationBeans-4.0/authorizeLdapUserBean!com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    
    private addFidoPolicyLocal lookup_addFidoPolicyLocal() {
        try {
            Context c = new InitialContext();
            return (addFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.0/addFidoPolicy!com.strongauth.skfs.policybeans.addFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    
    private getFidoPolicyLocal lookup_getFidoPolicyLocal() {
        try {
            Context c = new InitialContext();
            return (getFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.0/getFidoPolicy!com.strongauth.skfs.policybeans.getFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    
    private updateFidoPolicyLocal lookup_updateFidoPolicyLocal() {
        try {
            Context c = new InitialContext();
            return (updateFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.0/updateFidoPolicy!com.strongauth.skfs.policybeans.updateFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    
    private deleteFidoPolicyLocal lookup_deleteFidoPolicyLocal() {
        try {
            Context c = new InitialContext();
            return (deleteFidoPolicyLocal) c.lookup("java:app/fidoserverbeans-4.0/deleteFidoPolicy!com.strongauth.skfs.policybeans.deleteFidoPolicyLocal");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }
    
    @POST
    @Path("/" + skfsConstants.CREATE_FIDO_POLICY)
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response createFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
            @FormParam("payload") JSONObject payload){
        try{
            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
            RestInputValidator.DEFAULT.validateCreatePayloadExistance(payload);
            
            String didString = svcinfo.get("did").toString();
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString), 
                    svcinfo.get("svcusername").toString(), 
                    svcinfo.get("svcpassword").toString(), 
                    skfsConstants.LDAP_ROLE_ADM);  //TODO create a new role for FIDO admins
            
            if(!isAuthorized){
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
            }
            
            Long did = svcinfo.getLong("did");
            JSONObject metadata = payload.getJSONObject("metadata");
            Date startDate = new Date(metadata.getLong("startdate") * 1000);    //seconds to ms. JPA only only stores dates to the second.
            long endDateLong = metadata.optLong("enddate");
            Date endDate = (endDateLong != 0)? new Date(endDateLong * 1000) : null;
            String certificateProfileName = metadata.getString("name");
            JSONObject policy = payload.getJSONObject("policy");
            int version = metadata.getInt("version");
            String status = metadata.getString("status");
            String notes = metadata.optString("notes", null);
            Integer pid = addpolicybean.execute(did, startDate, endDate, certificateProfileName, policy.toString(), version, status, notes);
            return Response.status(Response.Status.OK).entity(new FIDOResponse(pid.toString(), "", "").toString()).build();
        }
        catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
        }
    }
    
    @POST
    @Path("/" + skfsConstants.READ_FIDO_POLICY)
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response readFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
            @FormParam("payload") JSONObject payload){
        try{
            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
            RestInputValidator.DEFAULT.validateReadPayloadExistance(payload);
            
            String didString = svcinfo.get("did").toString();
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString), 
                    svcinfo.get("svcusername").toString(), 
                    svcinfo.get("svcpassword").toString(), 
                    skfsConstants.LDAP_ROLE_ADM); 
            
            if(!isAuthorized){
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
            }
            
            Long did = svcinfo.getLong("did");
            Long sid = applianceCommon.getServerId();
            JSONObject metadata = payload.getJSONObject("metadata");
            Long pid = metadata.getLong("pid");
            FidoPolicies policy = getpolicybean.getbyPK(did, sid, pid);
            return Response.status(Response.Status.OK).entity(new FIDOResponse(printFidoPolicy(policy).toString(), "", "").toString()).build();
        }
        catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (Exception ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
        }
    }
    
    @POST
    @Path("/" + skfsConstants.UPDATE_FIDO_POLICY)
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response updateFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
            @FormParam("payload") JSONObject payload){
        try {
            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
            RestInputValidator.DEFAULT.validateUpdatePayloadExistance(payload);

            String didString = svcinfo.get("did").toString();
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
                    svcinfo.get("svcusername").toString(),
                    svcinfo.get("svcpassword").toString(),
                    skfsConstants.LDAP_ROLE_ADM); 

            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
            }

            Long did = svcinfo.getLong("did");
            JSONObject metadata = payload.getJSONObject("metadata");
            Long pid = metadata.getLong("pid");
            long startDateLong = metadata.optLong("startdate", 0);
            Date startDate = (startDateLong != 0) ? new Date(startDateLong * 1000) : null;
            long endDateLong = metadata.optLong("enddate", 0);
            Date endDate = (endDateLong != 0) ? new Date(endDateLong * 1000) : null;
            JSONObject policy = payload.optJSONObject("policy");
            Integer version = metadata.optInt("version");
            String status = metadata.optString("status", null);
            String notes = metadata.optString("notes", null);
            updatepolicybean.excecute(did, pid, startDate, endDate, policy.toString(), version, status, notes);
            return Response.status(Response.Status.OK).entity(new FIDOResponse("Updated", "", "").toString()).build();
        } catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (Exception ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
        }
    }
    
    @POST
    @Path("/" + skfsConstants.DELETE_FIDO_POLICY)
    @Consumes({"application/x-www-form-urlencoded"})
    @Produces({MediaType.APPLICATION_JSON})
    public Response deleteFidoPolicy(@FormParam("svcinfo") JSONObject svcinfo,
            @FormParam("payload") JSONObject payload) {
        try {
            RestInputValidator.DEFAULT.validateSVCInfoExistance(svcinfo);
            RestInputValidator.DEFAULT.validateDeletePayloadExistance(payload);

            String didString = svcinfo.get("did").toString();
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
                    svcinfo.get("svcusername").toString(),
                    svcinfo.get("svcpassword").toString(),
                    skfsConstants.LDAP_ROLE_ADM);  

            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return Response.status(Response.Status.BAD_REQUEST).entity(
                        new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString()).build();
            }

            Long did = svcinfo.getLong("did");
            Long sid = applianceCommon.getServerId();
            JSONObject metadata = payload.getJSONObject("metadata");
            Long pid = metadata.getLong("pid");
            deletepolicybean.execute(did, sid, pid);
            return Response.status(Response.Status.OK).entity(new FIDOResponse("Deleted", "", "").toString()).build();
        } catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (Exception ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return Response.status(Response.Status.BAD_REQUEST).entity(
                    new FIDOResponse("", "", ex.getLocalizedMessage()).toString()).build();
        }
    }
    
    private Response handleValidationException(ValidationException ex){
        String error = ex.getAllMessages().stream().collect(Collectors.joining(", "));
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", error);
        return Response.status(Response.Status.BAD_REQUEST).entity(new FIDOResponse("", "", error).toString()).build();
    }
    
    private JSONObject printFidoPolicy(FidoPolicies policy){
        JSONObject result = new JSONObject();
        String policyBase64 = new String(Base64.getUrlDecoder().decode(policy.getPolicy()));
        JSONObject policyJSON = new JSONObject(policyBase64);
        result.put("name", policy.getCertificateProfileName());
        result.put("startdate", policy.getStartDate());
        result.put("enddate", policy.getEndDate() == null ? JSONObject.NULL : policy.getEndDate());
        result.put("policy", policyJSON);
        result.put("version", policy.getVersion());
        result.put("status", policy.getStatus());
        result.put("notes", policy.getNotes() == null ? JSONObject.NULL : policy.getNotes());
        return result;
    }
}