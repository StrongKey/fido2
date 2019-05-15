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
package com.strongauth.skfews.fido2.soap;


import com.strongauth.skfews.fido2.rest.FIDOResponse;
import com.strongauth.skfews.fido2.rest.RestInputValidator;
import com.strongauth.skfews.fido2.soap.jaxb.ObjectFactory;
import com.strongauth.skfews.fido2.soap.jaxb.SvcInfoType;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
import com.strongkey.skce.utilities.SKCEException;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.policybeans.addFidoPolicyLocal;
import com.strongkey.skfs.policybeans.deleteFidoPolicyLocal;
import com.strongkey.skfs.policybeans.getFidoPolicyLocal;
import com.strongkey.skfs.policybeans.updateFidoPolicyLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.io.File;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.everit.json.schema.ValidationException;
import org.json.JSONException;
import org.json.JSONObject;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

@WebService(serviceName = "adminsoap")
public class FidoAdminServlet {

    @EJB
    authorizeLdapUserBeanLocal authorizebean;
    @EJB
    addFidoPolicyLocal addpolicybean;
    @EJB
    getFidoPolicyLocal getpolicybean;
    @EJB
    updateFidoPolicyLocal updatepolicybean;
    @EJB
    deleteFidoPolicyLocal deletepolicybean;

    @WebMethod(operationName = skfsConstants.CREATE_FIDO_POLICY)
    public String createFidoPolicy(@WebParam(name = "svcinfo") SvcInfoType svcinfo,
            @WebParam(name = "payload") JSONObject payload) {
        try {
            //Validate XML via marshalling
            JAXBContext context = JAXBContext.newInstance(SvcInfoType.class);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File("jaxb.FidoSvcInfo.fido2adminschema.xsd"));
            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.marshal((new ObjectFactory()).createSvcInfo(svcinfo), new DefaultHandler());

            RestInputValidator.DEFAULT.validateCreatePayloadExistance(payload);

            String didString = String.valueOf(svcinfo.getDid());
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
                    svcinfo.getSvcUsername(),
                    svcinfo.getSvcPassword(),
                    skfsConstants.LDAP_ROLE_ADM);

            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString();
            }

            Long did = (long) svcinfo.getDid();
            JSONObject metadata = payload.getJSONObject("metadata");
            Date startDate = new Date(metadata.getLong("startdate"));
            long endDateLong = metadata.optLong("enddate");
            Date endDate = (endDateLong != 0) ? new Date(endDateLong) : null;
            String certificateProfileName = metadata.getString("name");
            JSONObject policy = payload.getJSONObject("policy");
            int version = metadata.getInt("version");
            String status = metadata.getString("status");
            String notes = metadata.optString("notes", null);
            Integer pid = addpolicybean.execute(did, startDate, endDate, certificateProfileName, policy.toString(), version, status, notes);
            return new FIDOResponse(pid.toString(), "", "").toString();
        } catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (JAXBException | SAXException | SKCEException | SKFEException | JSONException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return new FIDOResponse("", "", ex.getLocalizedMessage()).toString();
        }
    }

    @WebMethod(operationName = skfsConstants.READ_FIDO_POLICY)
    public String readFidoPolicy(@WebParam(name = "svcinfo") SvcInfoType svcinfo,
            @WebParam(name = "payload") JSONObject payload) {
        try {
            //Validate XML via marshalling
            JAXBContext context = JAXBContext.newInstance(SvcInfoType.class);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File("jaxb.FidoSvcInfo.fido2adminschema.xsd"));
            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.marshal((new ObjectFactory()).createSvcInfo(svcinfo), new DefaultHandler());

            RestInputValidator.DEFAULT.validateReadPayloadExistance(payload);

            String didString = String.valueOf(svcinfo.getDid());
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
                    svcinfo.getSvcUsername(),
                    svcinfo.getSvcPassword(),
                    skfsConstants.LDAP_ROLE_ADM);

            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString();
            }

            Long did = (long) svcinfo.getDid();
            Long sid = applianceCommon.getServerId();
            JSONObject metadata = payload.getJSONObject("metadata");
            Long pid = metadata.getLong("pid");
            FidoPolicies policy = getpolicybean.getbyPK(did, sid, pid);
            return new FIDOResponse(printFidoPolicy(policy).toString(), "", "").toString();
        } catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (SKCEException | SKFEException | SAXException | JAXBException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return new FIDOResponse("", "", ex.getLocalizedMessage()).toString();
        }
    }

    @WebMethod(operationName = skfsConstants.UPDATE_FIDO_POLICY)
    public String updateFidoPolicy(@WebParam(name = "svcinfo") SvcInfoType svcinfo,
            @WebParam(name = "payload") JSONObject payload) {
        try {
            //Validate XML via marshalling
            JAXBContext context = JAXBContext.newInstance(SvcInfoType.class);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File("jaxb.FidoSvcInfo.fido2adminschema.xsd"));
            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.marshal((new ObjectFactory()).createSvcInfo(svcinfo), new DefaultHandler());

            RestInputValidator.DEFAULT.validateUpdatePayloadExistance(payload);

            String didString = String.valueOf(svcinfo.getDid());
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
                    svcinfo.getSvcUsername(),
                    svcinfo.getSvcPassword(),
                    skfsConstants.LDAP_ROLE_ADM);

            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString();
            }

            Long did = (long) svcinfo.getDid();
            JSONObject metadata = payload.getJSONObject("metadata");
            Long pid = metadata.getLong("pid");
            long startDateLong = metadata.optLong("startdate", 0);
            Date startDate = (startDateLong != 0) ? new Date(startDateLong) : null;
            long endDateLong = metadata.optLong("enddate", 0);
            Date endDate = (endDateLong != 0) ? new Date(endDateLong) : null;
            JSONObject policy = payload.optJSONObject("policy");
            Integer version = metadata.optInt("version");
            String status = metadata.optString("status", null);
            String notes = metadata.optString("notes", null);
            updatepolicybean.excecute(did, pid, startDate, endDate, policy.toString(), version, status, notes);
            return new FIDOResponse("Updated", "", "").toString();
        } catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (SKCEException | SKFEException | SAXException | JAXBException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return new FIDOResponse("", "", ex.getLocalizedMessage()).toString();
        }
    }

    @WebMethod(operationName = skfsConstants.DELETE_FIDO_POLICY)
    public String deleteFidoPolicy(@WebParam(name = "svcinfo") SvcInfoType svcinfo,
            @WebParam(name = "payload") JSONObject payload) {
        try {
            //Validate XML via marshalling
            JAXBContext context = JAXBContext.newInstance(SvcInfoType.class);
            SchemaFactory schemaFactory = SchemaFactory.newInstance(W3C_XML_SCHEMA_NS_URI);
            Schema schema = schemaFactory.newSchema(new File("jaxb.FidoSvcInfo.fido2adminschema.xsd"));
            Marshaller marshaller = context.createMarshaller();
            marshaller.setSchema(schema);
            marshaller.marshal((new ObjectFactory()).createSvcInfo(svcinfo), new DefaultHandler());

            RestInputValidator.DEFAULT.validateDeletePayloadExistance(payload);

            String didString = String.valueOf(svcinfo.getDid());
            skfsCommon.inputValidateSKCEDid(didString);
            boolean isAuthorized = authorizebean.execute(Long.parseLong(didString),
                    svcinfo.getSvcUsername(),
                    svcinfo.getSvcPassword(),
                    skfsConstants.LDAP_ROLE_ADM);

            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return new FIDOResponse("", "", skfsCommon.getMessageProperty("FIDO-ERR-0033")).toString();
            }

            Long did = (long) svcinfo.getDid();
            Long sid = applianceCommon.getServerId();
            JSONObject metadata = payload.getJSONObject("metadata");
            Long pid = metadata.getLong("pid");
            deletepolicybean.execute(did, sid, pid);
            return new FIDOResponse("Deleted pid = " + pid, "", "").toString();
        } catch (ValidationException ex) {
            return handleValidationException(ex);
        } catch (SKCEException | SKFEException | JAXBException | SAXException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", ex.getLocalizedMessage());
            return new FIDOResponse("", "", ex.getLocalizedMessage()).toString();
        }
    }

    private String handleValidationException(ValidationException ex) {
        String error = ex.getAllMessages().stream().collect(Collectors.joining(", "));
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCEWS-ERR-3000", error);
        return new FIDOResponse("", "", error).toString();
    }

    private JSONObject printFidoPolicy(FidoPolicies policy) {
        JSONObject result = new JSONObject();
        String policyBase64 = new String(Base64.getUrlDecoder().decode(policy.getPolicy()));
        JSONObject policyJSON = new JSONObject(policyBase64);
        result.put("name", policy.getCertificateProfileName());
        result.put("startdate", policy.getStartDate());
        result.put("enddate", policy.getEndDate());
        result.put("policy", policyJSON);
        result.put("version", policy.getVersion());
        result.put("status", policy.getStatus());
        result.put("notes", policy.getNotes());
        return result;
    }
}
