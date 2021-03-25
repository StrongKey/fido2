/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
package com.strongauth.skfews.u2f.soap;

import com.strongkey.auth.txbeans.authorizeLdapUserBeanLocal;
import com.strongkey.skce.jaxb.SKCEServiceInfoType;
import com.strongkey.skce.utilities.SKCEException;
import com.strongkey.skfs.txbeans.v1.u2fServletHelperBeanLocal_v1;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.core.Context;

/**
 * SOAP based web services that serve FIDO U2F protocol based functionality.
 *
 */
@WebService(serviceName = "soap")
public class SKFEServlet {

    @Context
    private HttpServletRequest request;

    @EJB
    u2fServletHelperBeanLocal_v1 u2fHelper;
    @EJB
    authorizeLdapUserBeanLocal authorizebean; // ldap user authorization bean
    // CTOR

    public SKFEServlet() {
    }

    /**
     * Basic null checks before the compound input objects are accessed.
     *
     * @param svcinfo       SKCEServiceInfoType; service credentials information
     *
     * @return void; if all checks passed
     * @throws SKCEException; in case any check fails
     */
    private void basicInputChecks(String methodname,
                                  SKCEServiceInfoType svcinfo) throws SKCEException
    {
        String prefix = methodname + " web-service; ";

        if ( svcinfo==null ) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "SKCEWS-ERR-3014",
                    "svcinfo");
            throw new SKCEException(SKFSCommon.getMessageProperty("SKCEWS-ERR-3014")
                    .replace("{0}", "") + prefix + "svcinfo");
        }
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
            @WebParam(name = "svcinfo")  SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("preregister", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildPreRegisterResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO_REG);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }

        return u2fHelper.preregister(did, protocol, payload);
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
            @WebParam(name = "svcinfo")  SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("register", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildRegisterResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO_REG);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.register(did, protocol, payload);
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
            @WebParam(name = "svcinfo")  SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("preauthenticate", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildPreAuthResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO_SIGN);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.preauthenticate(did, protocol, payload);
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
            @WebParam(name = "svcinfo")  SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("authenticate", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildAuthenticateResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO_SIGN);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        
         String agent = request.getHeader("User-Agent");
        //Example for getting cip
        String cip = request.getRemoteAddr();
        
        return u2fHelper.authenticate(did, protocol, payload, agent, cip);
    }

    /*
     ************************************************************************
     *                                                888    888                       d8b
     *                                                888    888                       Y8P
     *                                               888    888
     *    88888b.  888d888  .d88b.   8888b.  888  888 888888 88888b.   .d88b.  888d888 888 88888888  .d88b.
     *    888 "88b 888P"   d8P  Y8b     "88b 888  888 888    888 "88b d88""88b 888P"   888    d88P  d8P  Y8b
     *    888  888 888     88888888 .d888888 888  888 888    888  888 888  888 888     888   d88P   88888888
     *    888 d88P 888     Y8b.     888  888 Y88b 888 Y88b.  888  888 Y88..88P 888     888  d88P    Y8b.
     *    88888P"  888      "Y8888  "Y888888  "Y88888  "Y888 888  888  "Y88P"  888     888 88888888  "Y8888
     *    888
     *    888
     *    888
     ************************************************************************
     */
    /**
     * Step-1 for fido based transaction confirmation using u2f authenticator.
     * This methods generates a challenge and returns the same to the caller.
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
     * @param payload - String indicating transaction reference.
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Auth Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @WebMethod(operationName = "preauthorize")
    public String preauthorize(
            @WebParam(name = "svcinfo")  SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("preauthorize", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildPreAuthResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO_AUTHZ);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.preauthorize(did, protocol, payload);
    }

    /*
     ************************************************************************
     *                      888    888                       d8b
     *                      888    888                       Y8P
     *                      888    888
     *     8888b.  888  888 888888 88888b.   .d88b.  888d888 888 88888888  .d88b.
     *        "88b 888  888 888    888 "88b d88""88b 888P"   888    d88P  d8P  Y8b
     *    .d888888 888  888 888    888  888 888  888 888     888   d88P   88888888
     *    888  888 Y88b 888 Y88b.  888  888 Y88..88P 888     888  d88P    Y8b.
     *    "Y888888  "Y88888  "Y888 888  888  "Y88P"  888     888 88888888  "Y8888
     *
     ************************************************************************
     */
    /**
     * Step-2 or last step for fido based transaction confirmation using a u2f
     * authenticator. This method receives the u2f authentication response
     * parameters which is processed and the authorization result is notified
     * back to the caller.
     *
     * Both preauthorize and authorize methods are time linked. Meaning,
     * authorize should happen with in a certain time limit after the
     * preauthorize is finished; otherwise, the user session would be
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
    @WebMethod(operationName = "authorize")
    public String authorize(
            @WebParam(name = "svcinfo") SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("authorize", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildAuthenticateResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO_AUTHZ);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildAuthenticateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.authorize(did, protocol, payload);
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
            @WebParam(name = "svcinfo") SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("deregister", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildDeregisterResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildDeregisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildDeregisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildDeregisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildDeregisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.deregister(did, protocol, payload);
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
    @WebMethod(operationName = "deactivate")
    public String deactivate(
            @WebParam(name = "svcinfo") SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("deactivate", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildDeactivateResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildDeactivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildDeactivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildDeactivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildDeactivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.deactivate(did, protocol, payload);
    }

    /*
     ************************************************************************
     *                        888    d8b                   888
     *                        888    Y8P                   888
     *                        888                          888
     *       8888b.   .d8888b 888888 888 888  888  8888b.  888888  .d88b.
     *          "88b d88P"    888    888 888  888     "88b 888    d8P  Y8b
     *      .d888888 888      888    888 Y88  88P .d888888 888    88888888
     *      888  888 Y88b.    Y88b.  888  Y8bd8P  888  888 Y88b.  Y8b.
     *      "Y888888  "Y8888P  "Y888 888   Y88P   "Y888888  "Y888  "Y8888
     ************************************************************************
     */
    /**
     * The process of activating an already registerd but de-activated fido
     * authenticator. This process will turn the status of the key in the
     * database back to ACTIVE. The inputs needed are the name of the user and
     * the random id to point to a unique registered key for that user. This
     * random id can be obtained by calling getkeysinfo method.
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
     * @param payload - U2F activation parameters in Json form. Should contain
     * username and randomid.
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in activation 3. 'Error' : String, with error
     * message incase something went wrong. Will be empty if successful.
     */
    @WebMethod(operationName = "activate")
    public String activate(
            @WebParam(name = "svcinfo") SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("activate", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildActivateResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildActivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildActivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildActivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildActivateResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.activate(did, protocol, payload);
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
            @WebParam(name = "svcinfo") SKCEServiceInfoType svcinfo,
            @WebParam(name = "payload") String payload)
    {
        //  Local variables
        //  Service credentials
        String did;
        String svcusername;
        String svcpassword;
        String protocol;

        //  SKCE domain id validation
        try {
            basicInputChecks("getkeysinfo", svcinfo);

            did             = Integer.toString(svcinfo.getDid());
            svcusername     = svcinfo.getSvcusername();
            svcpassword     = svcinfo.getSvcpassword();
            protocol        = svcinfo.getProtocol();

            SKFSCommon.inputValidateSKCEDid(did);
        } catch (Exception ex) {
            return SKFSCommon.buildGetKeyInfoResponse(null, "", ex.getLocalizedMessage());
        }

        //  2. Input checks
        if (svcusername == null || svcusername.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        if (svcpassword == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " credential");
            return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " credential");
        }
        //authenticate
        boolean isAuthorized;
        try {
            isAuthorized = authorizebean.execute(Long.parseLong(did), svcusername, svcpassword, SKFSConstants.LDAP_ROLE_FIDO);
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
            return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
        }
        if (!isAuthorized) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0033", "");
            return SKFSCommon.buildGetKeyInfoResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0033"));
        }
        return u2fHelper.getkeysinfo(did, protocol, payload);
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
    @WebMethod(operationName = "getserverinfo")
    public String getserverinfo(
            @WebParam(name = "svcinfo") SKCEServiceInfoType svcinfo) {
        return "not implemented yet";
    }
}
