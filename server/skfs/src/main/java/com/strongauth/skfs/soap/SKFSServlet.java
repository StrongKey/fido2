/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
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
 * Servlet for FIDO U2F protocol based functionality. This servlet exposes
 * SOAP (Simple Object Access Protocol) based web services to the calling
 * applications.
 *
 */
package com.strongauth.skfs.soap;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.auth.txbeans.authenticateSOAPHMACRequestBeanLocal;
import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
import com.strongkey.skce.utilities.SKCEException;
import com.strongkey.skfs.requests.AuthenticationRequest;
import com.strongkey.skfs.requests.DeregisterRequest;
import com.strongkey.skfs.requests.GetKeysInfoRequest;
import com.strongkey.skfs.requests.PreauthenticationRequest;
import com.strongkey.skfs.requests.PreauthorizeRequest;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.requests.RegistrationRequest;
import com.strongkey.skfs.requests.ServiceInfo;
import com.strongkey.skfs.requests.UpdateFidoKeyRequest;
import com.strongkey.skfs.txbeans.pingBeanLocal;
import com.strongkey.skfs.txbeans.u2fServletHelperBeanLocal;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.json.JsonObject;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Response;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

/**
 * SOAP based web services that serve FIDO U2F protocol based functionality.
 *
 */
@WebService(serviceName = "soap")
public class SKFSServlet {

//    @Context
//    private HttpServletRequest request;
    @Resource WebServiceContext context;

    @EJB
    u2fServletHelperBeanLocal u2fHelper ;
    @EJB
    pingBeanLocal   pingbean;
    @EJB
    authorizeLdapUserBeanLocal authorizebean ; // ldap user authorization bean
    @EJB
    authenticateSOAPHMACRequestBeanLocal authSOAPHMAC ;
    // CTOR

    public SKFSServlet() {
    }

    /*
     ************************************************************************
     *                                                        d8b          888
     *                                                        Y8P          888
     *                                                                     888
     *    88888b.  888d888  .d88b.  888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888
     *    888 "88b 888P"   d8P  Y8b 888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"
     *    888  888 888     88888888 888     88888888 888  888 888 "Y8888b. 888    88888888 888
     *    888 d88P 888     Y8b.     888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888
     *    88888P"  888      "Y8888  888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888
     *    888                                             888
     *    888                                        Y8b d88P
     *    888                                         "Y88P"
     ************************************************************************
     */
    /**
     * Step-1 for fido authenticator registration. This methods generates a
     * challenge and returns the same to the caller, which typically is a
     * Relying Party (RP) application.
     *
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     * (4) protocol    -  U2F protocol version to comply with.
     *
     * @param payload - payload
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : U2F Reg Challenge parameters; a json again 2. 'Message'
     * : String, with a list of messages that explain the process. 3. 'Error' :
     * String, with error message incase something went wrong. Will be empty if
     * successful.
     */
    @WebMethod(operationName = "preregister")
    public String preregister(
            @WebParam(name = "svcinfo")  String svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildPreRegisterResponse(null, "", ex.getLocalizedMessage());
        }

        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_REG);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }
        //convert payload to pre reg object
        PreregistrationRequest pregreq = new PreregistrationRequest();
        JsonObject preregpayload = applianceCommon.stringToJSON(payload);

        if (preregpayload.containsKey("username")) {
            pregreq.setUsername(preregpayload.getString("username"));
        }

        if (preregpayload.containsKey("displayname")) {
            pregreq.setDisplayName(preregpayload.getString("displayname"));
        }

       if (preregpayload.containsKey("options")) {
            pregreq.setOptions(preregpayload.getJsonObject("options"));
        }

        if (preregpayload.containsKey("extensions")) {
            pregreq.setExtensions(preregpayload.getString("extensions"));
        }

        pregreq.getSVCInfo().setProtocol(svcinfoObj.getProtocol());
        Response res = u2fHelper.preregister(svcinfoObj.getDid(), pregreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildPreRegisterResponse(null, "", (String) res.getEntity());
        }
    }

    /*
     ************************************************************************
     *                                d8b          888
     *                                Y8P          888
     *                                             888
     *      888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888
     *      888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"
     *      888     88888888 888  888 888 "Y8888b. 888    88888888 888
     *      888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888
     *      888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888
     *                            888
     *                       Y8b d88P
     *                        "Y88P"
     *************************************************************************
     */
    /**
     * Step-2 or last step of fido authenticator registration process. This
     * method receives the u2f registration response parameters which is
     * processed and the registration result is notified back to the caller.
     *
     * Both preregister and register methods are time linked. Meaning, register
     * should happen with in a certain time limit after the preregister is
     * finished; otherwise, the user session would be invalidated.
     *
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     *
     * @param payload - U2F Registration Response parameters in Json form.
     * Should contain sessionid, browserData and enrollData.
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @WebMethod(operationName = "register")
    public String register(
            @WebParam(name = "svcinfo")  String svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildRegisterResponse(null, "", ex.getLocalizedMessage());
        }


        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_REG);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return SKFSCommon.buildRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return SKFSCommon.buildRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        //convert payload to pre reg object
        RegistrationRequest regreq = new RegistrationRequest();
        JsonObject regpayload = applianceCommon.stringToJSON(payload);
        regreq.getSVCInfo().setProtocol(svcinfoObj.getProtocol());

        if (regpayload.containsKey("strongkeyMetadata")) {
            regreq.setMetadata(regpayload.getJsonObject("strongkeyMetadata"));
        }

        if (regpayload.containsKey("publicKeyCredential")) {
            regreq.setResponse(regpayload.getJsonObject("publicKeyCredential"));
        }

        Response res = u2fHelper.register(svcinfoObj.getDid(), regreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildRegisterResponse(null, "", (String) res.getEntity());
        }
    }

    /*
     *************************************************************************
     *                                                888    888
     *                                                888    888
     *                                                888    888
     *    88888b.  888d888  .d88b.   8888b.  888  888 888888 88888b.
     *    888 "88b 888P"   d8P  Y8b     "88b 888  888 888    888 "88b
     *    888  888 888     88888888 .d888888 888  888 888    888  888
     *    888 d88P 888     Y8b.     888  888 Y88b 888 Y88b.  888  888
     *    88888P"  888      "Y8888  "Y888888  "Y88888  "Y888 888  888
     *    888
     *    888
     *    888
     ************************************************************************
     */
    /**
     * Step-1 for fido authenticator authentication. This methods generates a
     * challenge and returns the same to the caller.
     *
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     * (4) protocol    -  U2F protocol version to comply with.
     *
     * @param payload - username
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Auth Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @WebMethod(operationName = "preauthenticate")
    public String preauthenticate(
            @WebParam(name = "svcinfo")  String svcinfo,
            @WebParam(name = "payload") String payload)
    {
        ///  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildPreAuthResponse(null, "", ex.getLocalizedMessage());
        }

        //authenticate
        boolean isAuthorized;

        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_SIGN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }
        //convert payload to pre reg object
        PreauthenticationRequest pauthreq = new PreauthenticationRequest();
        JsonObject preauthpayload = applianceCommon.stringToJSON(payload);
        pauthreq.getSVCInfo().setProtocol(svcinfoObj.getProtocol());

        if (preauthpayload.containsKey("username")) {
            pauthreq.setUsername(preauthpayload.getString("username"));
        }

        if (preauthpayload.containsKey("options")) {
            pauthreq.setOptions(preauthpayload.getJsonObject("options"));
        }

        if (preauthpayload.containsKey("extensions")) {
            pauthreq.getPayload().setExtensions(preauthpayload.getString("extensions"));
        }

        Response res = u2fHelper.preauthenticate(svcinfoObj.getDid(), pauthreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildPreAuthResponse(null, "", (String) res.getEntity());
        }
    }

    /*
     ************************************************************************
     *                        888    888                        888    d8b                   888
     *                        888    888                        888    Y8P                   888
     *                        888    888                        888                          888
     *       8888b.  888  888 888888 88888b.   .d88b.  88888b.  888888 888  .d8888b  8888b.  888888  .d88b.
     *          "88b 888  888 888    888 "88b d8P  Y8b 888 "88b 888    888 d88P"        "88b 888    d8P  Y8b
     *      .d888888 888  888 888    888  888 88888888 888  888 888    888 888      .d888888 888    88888888
     *      888  888 Y88b 888 Y88b.  888  888 Y8b.     888  888 Y88b.  888 Y88b.    888  888 Y88b.  Y8b.
     *      "Y888888  "Y88888  "Y888 888  888  "Y8888  888  888  "Y888 888  "Y8888P "Y888888  "Y888  "Y8888
     *
     ************************************************************************
     */
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
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     *
     * @param payload
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @WebMethod(operationName = "authenticate")
    public String authenticate(
            @WebParam(name = "svcinfo")  String svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildAuthenticateResponse(null, "", ex.getLocalizedMessage());
        }


        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_SIGN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        //convert payload to pre reg object
        AuthenticationRequest authreq = new AuthenticationRequest();
        JsonObject authpayload = applianceCommon.stringToJSON(payload);
        authreq.getSVCInfo().setProtocol(svcinfoObj.getProtocol());

        if (authpayload.containsKey("strongkeyMetadata")) {
            authreq.setMetadata(authpayload.getJsonObject("strongkeyMetadata"));
        }

        if (authpayload.containsKey("publicKeyCredential")) {
            authreq.setResponse(authpayload.getJsonObject("publicKeyCredential"));
        }

        HttpServletRequest request = (HttpServletRequest)context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
        System.out.println("IP: "+request.getRemoteAddr()+", Port: "+request.getRemotePort()+", Host: "+request.getRemoteHost());
        String agent = request.getHeader("User-Agent");
        //Example for getting cip
        String cip = request.getRemoteAddr();
        
        Response res = u2fHelper.authenticate(svcinfoObj.getDid(), authreq, agent, cip);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
        }
    }

    /*
     ************************************************************************
     *        888                                    d8b          888
     *        888                                    Y8P          888
     *        888                                                 888
     *    .d88888  .d88b.  888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888
     *   d88" 888 d8P  Y8b 888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"
     *   888  888 88888888 888     88888888 888  888 888 "Y8888b. 888    88888888 888
     *   Y88b 888 Y8b.     888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888
     *    "Y88888  "Y8888  888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888
     *                                           888
     *                                      Y8b d88P
     *                                      "Y88P"
     *
     ************************************************************************
     */
    /**
     * The process of deleting or de-registering an already registered fido
     * authenticator. The inputs needed are the name of the user and the random
     * id to point to a unique registered key for that user. This random id can
     * be obtained by calling getkeysinfo method.
     *
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     * (4) protocol    -  U2F protocol version to comply with.
     *
     * @param payload - U2F de-registration parameters in Json form. Should
     * contain username and randomid.
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in de-registration 3. 'Error' : String, with
     * error message incase something went wrong. Will be empty if successful.
     */
    @WebMethod(operationName = "deregister")
    public String deregister(
            @WebParam(name = "svcinfo") String svcinfo,
            @WebParam(name = "payload") String payload)
    {
       //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildDeregisterResponse(null, "", ex.getLocalizedMessage());
        }


         //authenticate
        boolean isAuthorized = Boolean.FALSE;
        boolean isAuthorizedAdmin;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO);
                isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                try {
                    isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
                } catch (SKCEException ex1) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex1.getMessage());
                    return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex1.getMessage());
                }
            }
            if (!isAuthorized) {
                if (!isAuthorizedAdmin) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                    return SKFSCommon.buildDeregisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
                }
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildDeregisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        JsonObject deregpayload = applianceCommon.stringToJSON(payload);
        DeregisterRequest deregreq = new DeregisterRequest();
        if(deregpayload.containsKey("keyid")){
            deregreq.setKeyid(deregpayload.getString("keyid"));
        }

        Response res = u2fHelper.deregister(svcinfoObj.getDid(),deregreq.getPayload().getKeyid());
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
        }
    }

    /*
     ************************************************************************
     *        888                            888    d8b                   888
     *        888                            888    Y8P                   888
     *        888                            888                          888
     *    .d88888  .d88b.   8888b.   .d8888b 888888 888 888  888  8888b.  888888  .d88b.
     *   d88" 888 d8P  Y8b     "88b d88P"    888    888 888  888     "88b 888    d8P  Y8b
     *   888  888 88888888 .d888888 888      888    888 Y88  88P .d888888 888    88888888
     *   Y88b 888 Y8b.     888  888 Y88b.    Y88b.  888  Y8bd8P  888  888 Y88b.  Y8b.
     *    "Y88888  "Y8888  "Y888888  "Y8888P  "Y888 888   Y88P   "Y888888  "Y888  "Y8888
     ************************************************************************
     */
    /**
     * The process of de-activating an already registered fido authenticator.
     * This process will turn the status of the key in the database to INACTIVE.
     * The inputs needed are the name of the user and the random id to point to
     * a unique registered key for that user. This random id can be obtained by
     * calling getkeysinfo method.
     *
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     * (4) protocol    -  U2F protocol version to comply with.
     *
     * @param payload - U2F de-activation parameters in Json form. Should
     * contain username and randomid.
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in de-activation 3. 'Error' : String, with
     * error message incase something went wrong. Will be empty if successful.
     */
    @WebMethod(operationName = "updatekeyinfo")
    public String updatekeyinfo(
            @WebParam(name = "svcinfo") String svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildDeactivateResponse(null, "", ex.getLocalizedMessage());
        }


         //authenticate
        boolean isAuthorized = Boolean.FALSE;
        boolean isAuthorizedAdmin;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO);
                isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                try {
                    isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
                } catch (SKCEException ex1) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex1.getMessage());
                    return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex1.getMessage());
                }
            }
            if (!isAuthorized) {
                if (!isAuthorizedAdmin) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                    return SKFSCommon.buildDeactivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
                }
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildDeactivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        //convert payload to pre reg object
        UpdateFidoKeyRequest patchreq = new UpdateFidoKeyRequest();
        JsonObject patchpayload = applianceCommon.stringToJSON(payload);
        String keyid="";

        if(patchpayload.containsKey("status")){
            patchreq.setStatus(patchpayload.getString("status"));
        }
        if(patchpayload.containsKey("modify_location")){
            patchreq.setModifyLocation(patchpayload.getString("modify_location"));
        }
        if(patchpayload.containsKey("displayname")){
            patchreq.setDisplayname(patchpayload.getString("displayname"));
        }
        if(patchpayload.containsKey("keyid")){
            patchreq.setKeyid(patchpayload.getString("keyid"));
            keyid = patchpayload.getString("keyid");
        }

        Response res = u2fHelper.patchfidokey(svcinfoObj.getDid(),keyid, patchreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
        }
    }

    /*
     ************************************************************************
     *                        888    888                        d8b           .d888
     *                        888    888                        Y8P          d88P"
     *                        888    888                                     888
     *       .d88b.   .d88b.  888888 888  888  .d88b.  888  888 888 88888b.  888888  .d88b.
     *      d88P"88b d8P  Y8b 888    888 .88P d8P  Y8b 888  888 888 888 "88b 888    d88""88b
     *      888  888 88888888 888    888888K  88888888 888  888 888 888  888 888    888  888
     *      Y88b 888 Y8b.     Y88b.  888 "88b Y8b.     Y88b 888 888 888  888 888    Y88..88P
     *       "Y88888  "Y8888   "Y888 888  888  "Y8888   "Y88888 888 888  888 888     "Y88P"
     *           888                                        888
     *      Y8b d88P                                   Y8b d88P
     *       "Y88P"                                     "Y88P"
     ************************************************************************
     */
    /**
     * Method to return a list of user registered fido authenticator
     * information; In short, registered keys information. Information includes
     * the meta data of the key like the place and time it was registered and
     * used (last modified) from, a random id (which has a time-to-live) that
     * has to be sent back as a token during de-registration.
     *
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     * (4) protocol    -  U2F protocol version to comply with.
     *
     * @param payload - username
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : A Json array, each entry signifying metadata of a key
     * registered; Metadata includes randomid and its time-to-live, creation and
     * modify location and time info etc., 2. 'Message' : Empty string since
     * there is no cryptographic work involved in this process. 3. 'Error' :
     * String, with error message incase something went wrong. Will be empty if
     * successful.
     */
    @WebMethod(operationName = "getkeysinfo")
    public String getkeysinfo(
            @WebParam(name = "svcinfo") String svcinfo,
            @WebParam(name = "payload") String payload)
    {

        //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildGetKeyInfoResponse(null, "", ex.getLocalizedMessage());
        }


        //authenticate
        boolean isAuthorized = Boolean.FALSE;
        boolean isAuthorizedAdmin;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO);
                isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                try {
                    isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
                } catch (SKCEException ex1) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex1.getMessage());
                    return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex1.getMessage());
                }
            }
            if (!isAuthorized) {
                if (!isAuthorizedAdmin) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                    return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
                }
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        JsonObject getkeyspayload = applianceCommon.stringToJSON(payload);
        GetKeysInfoRequest getkeysreq = new GetKeysInfoRequest();
        if(getkeyspayload.containsKey("username")){
            getkeysreq.setUsername(getkeyspayload.getString("username"));
        }

        Response res = u2fHelper.getkeysinfo(svcinfoObj.getDid(),getkeysreq.getPayload().getUsername());
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
        }
    }

    /*
     ************************************************************************
     *                        888                                                        d8b           .d888
     *                        888                                                        Y8P          d88P"
     *                        888                                                                     888
     *       .d88b.   .d88b.  888888 .d8888b   .d88b.  888d888 888  888  .d88b.  888d888 888 88888b.  888888  .d88b.
     *      d88P"88b d8P  Y8b 888    88K      d8P  Y8b 888P"   888  888 d8P  Y8b 888P"   888 888 "88b 888    d88""88b
     *      888  888 88888888 888    "Y8888b. 88888888 888     Y88  88P 88888888 888     888 888  888 888    888  888
     *      Y88b 888 Y8b.     Y88b.       X88 Y8b.     888      Y8bd8P  Y8b.     888     888 888  888 888    Y88..88P
     *       "Y88888  "Y8888   "Y888  88888P'  "Y8888  888       Y88P    "Y8888  888     888 888  888 888     "Y88P"
     *           888
     *      Y8b d88P
     *       "Y88P"
     ************************************************************************
     */
    /**
     * A place holder to send back any server side information that could be
     * needed by the client applications.
     *
     * This method is *Not implemented yet*
     *
     * @param svcinfo   - Object that carries SKCE service information.
     *                    Information bundled is :
     *
     * (1) did          - Unique identifier for a SKCE encryption domain
     * (2) svcusername  - SKCE service credentials : username requesting the
     *                    service. The service credentials are looked up in
     *                    the 'service' setup of authentication system based
     *                    on LDAP / AD.
     *                    The user should be authorized to encrypt.
     * (3) svcpassword -  SKCE service credentials : password of the service
     *                    username specified above
     * (4) protocol    -  U2F protocol version to comply with.
     *
     * @return
     */
    @WebMethod(operationName = "ping")
    public String ping(
            @WebParam(name = "svcinfo") String svcinfo) {
        //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildPreRegisterResponse(null, "", ex.getLocalizedMessage());
        }

        //authenticate
        boolean isAuthorized = Boolean.FALSE;
        boolean isAuthorizedAdmin;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_MONITOR);
                isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                try {
                    isAuthorizedAdmin = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_ADMIN);
                } catch (SKCEException ex1) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex1.getMessage());
                    return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex1.getMessage());
                }
            }
            if (!isAuthorized) {
                if (!isAuthorizedAdmin) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                    return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
                }
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),null)) {
                return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        String response ;
        try{
            response = pingbean.execute(svcinfoObj.getDid());
            return response;
        }catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return ex.getLocalizedMessage();
        }
    }

    @WebMethod(operationName = "preauthorize")
    public String preauthorize(
            @WebParam(name = "svcinfo") String svcinfo,
            @WebParam(name = "payload") String payload) {
        ///  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildPreAuthResponse(null, "", ex.getLocalizedMessage());
        }

        //authenticate
        boolean isAuthorized;

        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_SIGN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }
        //convert payload to pre reg object
        PreauthorizeRequest pauthreq = new PreauthorizeRequest();
        JsonObject preauthpayload = applianceCommon.stringToJSON(payload);
        pauthreq.getSVCInfo().setProtocol(svcinfoObj.getProtocol());

        if (preauthpayload.containsKey("username")) {
            pauthreq.setUsername(preauthpayload.getString("username"));
        }

        if (preauthpayload.containsKey("options")) {
            pauthreq.setOptions(preauthpayload.getJsonObject("options"));
        }
        
        if (preauthpayload.containsKey("txid")) {
            pauthreq.setTxid(preauthpayload.getString("txid"));
        }else{
            return SKFSCommon.buildPreAuthResponse(null, "txid", SKFSCommon.getMessageProperty("FIDO-ERR-0002"));
        }
        
        if (preauthpayload.containsKey("txpayload")) {
            pauthreq.setTxpayload(preauthpayload.getString("txpayload"));
        }else{
            return SKFSCommon.buildPreAuthResponse(null, "txpayload", SKFSCommon.getMessageProperty("FIDO-ERR-0002"));
        }


        if (preauthpayload.containsKey("extensions")) {
            pauthreq.getPayload().setExtensions(preauthpayload.getString("extensions"));
        }

        Response res = u2fHelper.preauthorize(svcinfoObj.getDid(), pauthreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildPreAuthResponse(null, "", (String) res.getEntity());
        }
    }

    @WebMethod(operationName = "authorize")
    public String authorize(
            @WebParam(name = "svcinfo") String svcinfo,
            @WebParam(name = "payload") String payload) {
         //  Local variables
        ServiceInfo svcinfoObj;

        //  SKCE domain id validation
        try {
            svcinfoObj = SKFSCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return SKFSCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            SKFSCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return SKFSCommon.buildAuthenticateResponse(null, "", ex.getLocalizedMessage());
        }


        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), SKFSConstants.LDAP_ROLE_FIDO_SIGN);
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        //convert payload to pre reg object
        AuthenticationRequest authreq = new AuthenticationRequest();
        JsonObject authpayload = applianceCommon.stringToJSON(payload);
        authreq.getSVCInfo().setProtocol(svcinfoObj.getProtocol());

        if (authpayload.containsKey("strongkeyMetadata")) {
            authreq.setMetadata(authpayload.getJsonObject("strongkeyMetadata"));
        }

        if (authpayload.containsKey("publicKeyCredential")) {
            authreq.setResponse(authpayload.getJsonObject("publicKeyCredential"));
        }

        if (authpayload.containsKey("txid")) {
            authreq.setTxid(authpayload.getString("txid"));
        }else{
            return SKFSCommon.buildAuthenticateResponse(null, "txid", SKFSCommon.getMessageProperty("FIDO-ERR-0002"));
        }
        
        if (authpayload.containsKey("txpayload")) {
            authreq.setTxpayload(authpayload.getString("txpayload"));
        }else{
            return SKFSCommon.buildAuthenticateResponse(null, "txpayload", SKFSCommon.getMessageProperty("FIDO-ERR-0002"));
        }
        
        Response res = u2fHelper.authorize(svcinfoObj.getDid(), authreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return SKFSCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
        }
    }

}
