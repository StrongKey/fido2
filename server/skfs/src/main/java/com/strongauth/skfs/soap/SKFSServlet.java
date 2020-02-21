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
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;

/**
 * SOAP based web services that serve FIDO U2F protocol based functionality.
 *
 */
@WebService(serviceName = "soap")
public class SKFSServlet {

    @Context
    private HttpServletRequest request;

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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildPreRegisterResponse(null, "", ex.getLocalizedMessage());
        }

        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildPreRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildPreRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return skfsCommon.buildPreRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }
        //convert payload to pre reg object
        PreregistrationRequest pregreq = new PreregistrationRequest();
        JsonObject preregpayload = applianceCommon.stringToJSON(payload);

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

        pregreq.setProtocol(svcinfoObj.getProtocol());
        Response res = u2fHelper.preregister(svcinfoObj.getDid(), pregreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return skfsCommon.buildPreRegisterResponse(null, "", (String) res.getEntity());
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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildRegisterResponse(null, "", ex.getLocalizedMessage());
        }


        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return skfsCommon.buildRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        //convert payload to pre reg object
        RegistrationRequest regreq = new RegistrationRequest();
        JsonObject regpayload = applianceCommon.stringToJSON(payload);
        regreq.setProtocol(svcinfoObj.getProtocol());

        if (regpayload.containsKey("metadata")) {
            regreq.setMetadata(regpayload.getString("metadata"));
        }

        if (regpayload.containsKey("response")) {
            regreq.setResponse(regpayload.getString("response"));
        }

        Response res = u2fHelper.register(svcinfoObj.getDid(), regreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return skfsCommon.buildRegisterResponse(null, "", (String) res.getEntity());
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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildPreAuthResponse(null, "", ex.getLocalizedMessage());
        }

        //authenticate
        boolean isAuthorized;

        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildPreAuthResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildPreAuthResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return skfsCommon.buildPreAuthResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }
        //convert payload to pre reg object
        PreauthenticationRequest pauthreq = new PreauthenticationRequest();
        JsonObject preauthpayload = applianceCommon.stringToJSON(payload);
        pauthreq.setProtocol(svcinfoObj.getProtocol());

        if (preauthpayload.containsKey("username")) {
            pauthreq.setUsername(preauthpayload.getString("username"));
        }

        if (preauthpayload.containsKey("options")) {
            pauthreq.setOptions(preauthpayload.getString("options"));
        }

        if (preauthpayload.containsKey("extensions")) {
            pauthreq.setExtensions(preauthpayload.getString("extensions"));
        }

        Response res = u2fHelper.preauthenticate(svcinfoObj.getDid(), pauthreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return skfsCommon.buildPreAuthResponse(null, "", (String) res.getEntity());
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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildAuthenticateResponse(null, "", ex.getLocalizedMessage());
        }


        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildAuthenticateResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildAuthenticateResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return skfsCommon.buildAuthenticateResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        //convert payload to pre reg object
        AuthenticationRequest authreq = new AuthenticationRequest();
        JsonObject authpayload = applianceCommon.stringToJSON(payload);
        authreq.setProtocol(svcinfoObj.getProtocol());

        if (authpayload.containsKey("metadata")) {
            authreq.setMetadata(authpayload.getString("metadata"));
        }

        if (authpayload.containsKey("response")) {
            authreq.setResponse(authpayload.getString("response"));
        }

        Response res = u2fHelper.authenticate(svcinfoObj.getDid(), authreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return skfsCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildDeregisterResponse(null, "", ex.getLocalizedMessage());
        }


         //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildDeregisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildDeregisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return skfsCommon.buildDeregisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        JsonObject deregpayload = applianceCommon.stringToJSON(payload);
        DeregisterRequest deregreq = new DeregisterRequest();
        if(deregpayload.containsKey("keyid")){
            deregreq.setKeyid(deregpayload.getString("keyid"));
        }

        Response res = u2fHelper.deregister(svcinfoObj.getDid(),deregreq.getKeyid());
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return skfsCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildDeactivateResponse(null, "", ex.getLocalizedMessage());
        }


         //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildDeactivateResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildDeactivateResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return skfsCommon.buildDeactivateResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        //convert payload to pre reg object
        PatchFidoKeyRequest patchreq = new PatchFidoKeyRequest();
        JsonObject patchpayload = applianceCommon.stringToJSON(payload);
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

        Response res = u2fHelper.patchfidokey(svcinfoObj.getDid(),keyid, patchreq);
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return skfsCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildGetKeyInfoResponse(null, "", ex.getLocalizedMessage());
        }


        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildGetKeyInfoResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildGetKeyInfoResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),payload)) {
                return skfsCommon.buildGetKeyInfoResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        JsonObject getkeyspayload = applianceCommon.stringToJSON(payload);
        GetKeysInfoRequest getkeysreq = new GetKeysInfoRequest();
        if(getkeyspayload.containsKey("username")){
            getkeysreq.setUsername(getkeyspayload.getString("username"));
        }

        Response res = u2fHelper.getkeysinfo(svcinfoObj.getDid(),getkeysreq.getUsername());
        if (res.getStatus() == 200) {
            return (String) res.getEntity();
        } else {
            return skfsCommon.buildAuthenticateResponse(null, "", (String) res.getEntity());
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
            svcinfoObj = skfsCommon.checkSvcInfo("SOAP", svcinfo);

            if(svcinfoObj.getErrormsg() != null){
                String errormsg = svcinfoObj.getErrormsg();
                return skfsCommon.buildPreRegisterResponse(null, "", errormsg);
            }
            skfsCommon.inputValidateSKCEDid(svcinfoObj.getDid().toString());
        } catch (Exception ex) {
            return skfsCommon.buildPreRegisterResponse(null, "", ex.getLocalizedMessage());
        }

        //authenticate
        boolean isAuthorized;
        if (svcinfoObj.getAuthtype().equalsIgnoreCase("password")) {
            try {
                isAuthorized = authorizebean.execute(svcinfoObj.getDid(), svcinfoObj.getSvcusername(), svcinfoObj.getSvcpassword(), skfsConstants.LDAP_ROLE_FIDO);
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return skfsCommon.buildPreRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            }
            if (!isAuthorized) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0033", "");
                return skfsCommon.buildPreRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        } else {
            //check svcinfo
            if (!authSOAPHMAC.execute(svcinfoObj.getDid(), svcinfoObj.getContentSHA256(),
                    svcinfoObj.getAuthorization(), svcinfoObj.getTimestamp(), svcinfoObj.getStrongkeyAPIversion(),
                    svcinfoObj.getContentType(), svcinfoObj.getRequestURI(),null)) {
                return skfsCommon.buildPreRegisterResponse(null, "", skfsCommon.getMessageProperty("FIDO-ERR-0033"));
            }
        }

        String response ;
        try{
            response = pingbean.execute(svcinfoObj.getDid());
            return response;
        }catch (Exception ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return ex.getLocalizedMessage();
        }
    }

    @WebMethod(operationName = "preauthorize")
    public String preauthorize(
            @WebParam(name = "svcinfo") String svcinfo,
            @WebParam(name = "payload") String payload) {
        return "not implemented yet";
    }

    @WebMethod(operationName = "authorize")
    public String authorize(
            @WebParam(name = "svcinfo") String svcinfo,
            @WebParam(name = "payload") String payload) {
        return "not implemented yet";
    }
}
