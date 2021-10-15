/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
* $URL:
 * https://svn.strongauth.com/repos/jade/trunk/skce/skcebeans/src/main/java/com/strongauth/skce/txbeans/u2fServletHelperBean.java
 * $
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
 * Helper class for all seskcerolets that seskceroe u2f protocol methods. The
 * motive of this class is to centralize the processing of all methods, no
 * matter how many seskcerolets are there as a web seskceroice interface for
 * client applications to call. So, all the seskcerolets (SOAP based, REST based
 * or web sockets), for every request they receive will invoke this class object
 * and call the respective method.
 *
 * As the classname indicates, this class caters to functionality of fido u2f
 * protocol ONLY.
 *
 * The list of methods in this class are
 *
 * 1. preregister - Step-1 for fido authenticator registration. This methods
 * generates a challenge and returns the same to the caller. 2. register -
 * Step-2 or last step of fido authenticator registration process. This method
 * receives the u2f registration response parameters which is processed and the
 * registration result is notified back to the caller.
 *
 * Both preregister and register methods are time linked. Meaning, register
 * should happen with in a certain time limit after the preregister is finished;
 * otherwise, the user session would be invalidated.
 *
 * 3. preauth - Step-1 for fido authenticator authentication. This methods
 * generates a challenge and returns the same to the caller. 4. authenticate -
 * Step-2 or last step of fido authenticator authentication process. This method
 * receives the u2f authentication response parameters which is processed and
 * the authentication result is notified back to the caller.
 *
 * Both preauth and authenticate methods are time linked. Meaning, authenticate
 * should happen with in a certain time limit after the preauth is finished;
 * otherwise, the user session would be invalidated.
 *
 * 6. getkeysinfo - Method to return a list of user registered fido
 * authenticator information; In short, registered keys information.
 * 'Information' includes the meta data of the key like the place and time it
 * was registered and used (last modified) from, a random id (which has a
 * time-to-live) that has to be sent back as a token during de-registration.
 *
 * 5. deregister - The process of deleting or de-registering an already
 * registered fido authenticator. The inputs needed are the name of the user and
 * the randomid to point to a unique registered key for that user. This randomid
 * can be obtained by calling getkeysinfo method.
 *
 * 6. getseskceroerinfo - Not implemented yet. Added just as a placeholder
 *
 */
package com.strongkey.skfs.txbeans.v1;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.saka.web.Encryption;
import com.strongkey.saka.web.EncryptionService;
import com.strongkey.saka.web.StrongKeyLiteException_Exception;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.SAKAConnector;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.core.U2FAuthenticationChallenge;
import com.strongkey.skfs.core.U2FAuthenticationResponse;
import com.strongkey.skfs.core.U2FRegistrationChallenge;
import com.strongkey.skfs.core.U2FUtility;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.policybeans.generateFido2PreauthenticateChallengeLocal;
import com.strongkey.skfs.txbeans.FIDO2AuthenticateBeanLocal;
import com.strongkey.skfs.txbeans.FIDO2RegistrationBeanLocal;
import com.strongkey.skfs.txbeans.U2FRegistrationBeanLocal;
import com.strongkey.skfs.txbeans.addFidoKeysLocal;
import com.strongkey.skfs.txbeans.deleteFidoKeysLocal;
import com.strongkey.skfs.txbeans.getDomainsBeanLocal;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import com.strongkey.skfs.txbeans.getFidoUserLocal;
import com.strongkey.skfs.txbeans.originVerfierBeanLocal;
import com.strongkey.skfs.txbeans.u2fAuthenticateBeanLocal;
import com.strongkey.skfs.txbeans.u2fGetKeysInfoBeanLocal;
import com.strongkey.skfs.txbeans.u2fPreauthBeanLocal;
import com.strongkey.skfs.txbeans.u2fPreregisterBeanLocal;
import com.strongkey.skfs.txbeans.u2fRegisterBeanLocal;
import com.strongkey.skfs.txbeans.u2fServletHelperBean;
import com.strongkey.skfs.txbeans.updateFidoKeysLocal;
import com.strongkey.skfs.utilities.FEreturn;
import com.strongkey.skfs.utilities.SKCEReturnObject;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;

/**
 * EJB that helps the SKFE Servlet classes (SOAP, REST & WebSocket)
 */
@Stateless
public class u2fServletHelperBean_v1 implements u2fServletHelperBeanLocal_v1 {

    /*
     * Enterprise Java Beans used in this class.
     */
    //  u2f method beans
    @EJB
    u2fPreregisterBeanLocal u2fpreregbean;
    @EJB
    u2fRegisterBeanLocal u2fregisterbean;
    @EJB
    u2fPreauthBeanLocal u2fpreauthbean;
    @EJB
    u2fAuthenticateBeanLocal u2fauthbean;
    @EJB
    u2fDeregisterBeanLocal_v1 u2fderegbean;
    @EJB
    u2fDeactivateBeanLocal_v1 u2fdeactbean;
    @EJB
    u2fActivateBeanLocal_v1 u2factbean;
    @EJB
    u2fGetKeysInfoBeanLocal u2fgetkeysbean;
    @EJB
    originVerfierBeanLocal originverifierbean;

    // fido2 method beans
    @EJB
    generateFido2PreregisterChallengeLocal_v1 fido2preregbean;
    @EJB
    generateFido2PreauthenticateChallengeLocal fido2preauthbean;

    //  Beans to access local database
    @EJB
    addFidoKeysLocal addkeybean;
    @EJB
    getFidoKeysLocal getkeybean;
    @EJB
    updateFidoKeysLocal updatekeybean;
    @EJB
    deleteFidoKeysLocal deletekeybean;

    @EJB
    getFidoUserLocal getFidouserbean;

    @EJB
    FIDO2RegistrationBeanLocal FIDO2Regejb;
    @EJB
    FIDO2AuthenticateBeanLocal FIDO2Authejb;
    @EJB
    U2FRegistrationBeanLocal U2FRegejb;
    @EJB
    replicateSKFEObjectBeanLocal replObj;
    @EJB
    getDomainsBeanLocal getdomainejb;

//    String ldapusermetadata_loc = SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido.usermetadata");


    /*
     * Methods that serve Rest/Soap/Websocket based web-services
     */
 /*
     * ***********************************************************************
     *                          8888888b.                    d8b          888
     *                          888   Y88b                   Y8P          888
     *                          888    888                                888
     *88888b.  888d888  .d88b.  888   d88P  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888
     *888 "88b 888P"   d8P  Y8b 8888888P"  d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"
     *888  888 888     88888888 888 T88b   88888888 888  888 888 "Y8888b. 888    88888888 888
     *888 d88P 888     Y8b.     888  T88b  Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888
     *88888P"  888      "Y8888  888   T88b  "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888
     *888                                                888
     *888                                           Y8b d88P
     *888                                            "Y88P"
     * ***********************************************************************
     */
    /**
     * Method that performs pre-registration process in FIDO U2F protocol. This
     * method receives the request, performs basic input checks and then hands
     * over the pre-registration process to an EJB and waits for the wsresponse.
     * Up on receiving U2F Registration nonce parameters from the EJB, this
     * method adds a user session entry to the session map maintained in memory.
     *
     * Additionally, this method also makes silent pre-auth calls to build the
     * sign data array (each entry of it signifies that a unique FIDO
     * authenticator has been registered for this username).
     *
     * @param did - FIDO domain id
     * @param protocol - U2F protocol version to comply with.
     * @param payload - A stringified json with username field embedded into it.
     *
     * Example: { "username" : "johndoe" }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Reg Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @Override
    @SuppressWarnings("null")
    public String preregister(String did, String protocol, String payload) {

        String appid = getdomainejb.byDid(Long.parseLong(did)).getSkfeAppid();
        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0001", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " payload");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0014"), " Invalid json");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " Invalid json");
        }

        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " protocol");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " protocol");
        }
        if (!SKFSCommon.isFIDOProtocolSupported(protocol)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-5002"), protocol);
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-5002") + protocol);
        }

//check for protocol and call separate EJB
        if (protocol.equalsIgnoreCase(SKFSConstants.FIDO_PROTOCOL_VERSION_U2F_V2)) {
            //  fetch the username
            String username = (String) applianceCommon.getJsonValue(payload,
                    SKFSConstants.JSON_KEY_SERVLET_INPUT_USERNAME, "String");
            if (username == null || username.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
                return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " username");
            }

            //  3. Do processing - Do not do it here.. instead call preauthenticate on every key handle
            String logs = "";
            String errmsg = "";

            U2FRegistrationChallenge regChallenge = null;
            try {
                FEreturn fer = u2fpreregbean.execute(Long.parseLong(did), protocol, username);
                if (fer != null) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0046"), fer.toString());

                    logs = fer.getLogmsg();
                    regChallenge = (U2FRegistrationChallenge) fer.getResponse();
                    if (regChallenge == null) {
                        //  Chould not generate registration nonce.
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0025"), "");
                        return SKFSCommon.buildPreRegisterResponse(null, logs, SKFSCommon.getMessageProperty("FIDO-ERR-0025") + "");
                    } else {
                        //  Fetch sessionid, nonce & appid from the Challenge object
                        //  then store it in the sessionMap for further use.
                        String nonce = regChallenge.getNonce();
                        String nonceHash = SKFSCommon.getDigest(nonce, "SHA-256");

                        UserSessionInfo session = new UserSessionInfo(username, nonce, appid, "register", "", "");
                        session.setSid(applianceCommon.getServerId().shortValue());
                        session.setSkid(applianceCommon.getServerId().shortValue());
                        skceMaps.getMapObj().put(SKFSConstants.MAP_USER_SESSION_INFO, nonceHash, session);
                        //replicate map to other server
                        session.setMapkey(nonceHash);
                        try {
                            if (applianceCommon.replicate()) {
                                replObj.execute(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, applianceCommon.getServerId().toString(), session);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e.getLocalizedMessage());
                        }
                        //end publish

                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0021"), " username=" + username);
                    }
                }
            } catch (SKFEException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, SKFSCommon.getMessageProperty("FIDO-ERR-0003"), ex.getMessage());
                return SKFSCommon.buildPreRegisterResponse(null, logs, SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getMessage());
            } 

            //  Make a silent preauthenticate call to fetch all the key handles.
            //  Look into the database to check for key handles
            String[] authresponses = null;
            try {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0031"), "");
                Collection<FidoKeys> kh_coll = getkeybean.getByUsername(Long.parseLong(did), username);
                if (kh_coll != null) {
                    authresponses = new String[kh_coll.size()];
                    Iterator it = kh_coll.iterator();
                    int i = 0;

                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0032"), "");
                    while (it.hasNext()) {
                        FidoKeys key = (FidoKeys) it.next();
                        if (key != null) {
                            String kh = decryptKH(key.getKeyhandle());

                            // Do a silent preauthenticate call to get auth wsresponse for this key handle.
                            // Fetch transports from the child table and create a jsonarray and pass it on to the auth challenge object
                            FEreturn feskcero = u2fpreauthbean.execute(Long.parseLong(did), protocol, username, kh, key.getAppid(), SKFSCommon.getTransportJson(key.getTransports().intValue()));
                            if (feskcero != null) {
                                U2FAuthenticationChallenge authChallenge = (U2FAuthenticationChallenge) feskcero.getResponse();
                                if (authChallenge != null) {
                                    authresponses[i] = authChallenge.toJsonString(appid);
                                    i++;
                                }
                            }
                        }
                    }

                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0033"), "");
                }
            } catch (SKFEException ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0008", ex.getMessage());
                return SKFSCommon.buildPreRegisterResponse(null, "",
                        SKFSCommon.getMessageProperty("FIDO-ERR-0008") + ex.getMessage());
            }

            JsonObject combined_regresponse;
            JsonArray signDataArray = null;
            try {
                if (authresponses != null) {

                    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                    for (String authresponse : authresponses) {
                        JsonObject ar;
                        try (JsonReader jsonReader = Json.createReader(new StringReader(authresponse))) {
                            ar = jsonReader.readObject();
                        }
                        arrayBuilder.add(ar);
                    }

                    signDataArray = arrayBuilder.build();

                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0034", " signdata array length = "
                            + authresponses.length);
                }

                //building regreq array
                JsonArrayBuilder regarrayBuilder = Json.createArrayBuilder();
                JsonObject regobj;
                try (JsonReader jsonReader = Json.createReader(new StringReader(regChallenge.toJsonString()))) {
                    regobj = jsonReader.readObject();
                }
                regarrayBuilder.add(regobj);

                if (signDataArray == null) {
                    combined_regresponse = Json.createObjectBuilder()
                            .add(SKFSConstants.JSON_KEY_APP_ID, appid)
                            .add(SKFSConstants.JSON_KEY_REGISTERREQUEST, regarrayBuilder.build())
                            .build();
                } else {
                    combined_regresponse = Json.createObjectBuilder()
                            .add(SKFSConstants.JSON_KEY_APP_ID, appid)
                            .add(SKFSConstants.JSON_KEY_REGISTERREQUEST, regarrayBuilder.build())
                            .add(SKFSConstants.JSON_KEY_REGISTEREDKEY, signDataArray)
                            .build();
                }
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", ex.getMessage());
                return SKFSCommon.buildPreRegisterResponse(null, "",
                        SKFSCommon.getMessageProperty("FIDO-ERR-0014") + ex.getMessage());
            }

            // Build the output json object
            String response = SKFSCommon.buildPreRegisterResponse(combined_regresponse, logs, errmsg);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0035", "");

            out = new Date();
            long rt = out.getTime() - in.getTime();
            //  4. Log output and return
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0002", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]"
                    + "\nU2FRegistration Challenge parameters = " + response);
            return response;
        } else {
            String response = fido2preregbean.execute(Long.parseLong(did), payload);
            out = new Date();
            long rt = out.getTime() - in.getTime();
            //  4. Log output and return
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0002", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]"
                    + "\nFIDO2Registration Challenge parameters = " + response);
            return response;
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
     * Method that performs registration process in FIDO U2F protocol. This
     * method receives the request, performs basic input checks, parses the
     * sessionid from the U2F registration response parameters passed. Based on
     * the sessionid, the user session is validated. If found valid, the
     * registration process is handed over to an EJB. Upon getting response from
     * the EJB, this method persists the registration information to the
     * database. The user session at this point is removed or invalidated even
     * if the registration fails.
     *
     * NOTE : The U2F protocol version that is being used is decided based on
     * the structure of the regresponseJson; mostly relying on json keys.
     *
     * @param did - FIDO domain id
     * @param protocol
     * @param payload - A stringified json with registrationresponse and
     * reistration metadata embedded into it.
     *
     * Example: { "response": { "clientData": "...", "sessionId": "...",
     * "registrationData": "..." }, "metadata": { "version": "1.0",
     * "create_location": "Sunnyvale, CA" } }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @Override
    public String register(String did, String protocol, String payload) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0003", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0004", " payload");
            return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0004")
                    + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0014")
                    + " Invalid json");
        }

        //  3. Retrieve data from payload
        //  fetch response and metadata fields from payload
        JsonObject response = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_RESPONSE, "JsonObject");
        if (response == null || response.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0004", "");
            return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0004"));
        }

        JsonObject metadata = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_METADATA, "JsonObject");
        if (metadata == null || metadata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0016", "");
            return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0016"));
        }

        //  based on the structure of the regresponse, decide the protocol version.
        if (protocol == null || protocol.trim().isEmpty()) {
            protocol = SKFSConstants.FIDO_PROTOCOL_VERSION_U2F_V2;
        }

        String registrationresponse = response.toString();
        String registrationmetadata = metadata.toString();

        String responseJSON;

        //begine u2f code
        try {
            if (protocol.equalsIgnoreCase(SKFSConstants.FIDO_PROTOCOL_VERSION_U2F_V2)) {
                responseJSON = U2FRegejb.execute(Long.parseLong(did), registrationresponse, registrationmetadata, protocol);
            } else {
                responseJSON = FIDO2Regejb.execute(Long.parseLong(did), registrationresponse, registrationmetadata);
            }
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0001", ex.getMessage());
            return SKFSCommon.buildRegisterResponse(null, "", ex.getMessage());
        }

        //  9.  Build the output json object

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0037", "");

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  10. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0004", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]" + "\nResponse = " + responseJSON);
        return responseJSON;
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
     * Method that performs pre-auth process in FIDO U2F protocol. This method
     * receives the request, performs basic input checks and then looks into the
     * database for the number of FIDO authenticators the user has already
     * successfully registered. For each registered key, this method hands over
     * the pre-authenticate process to an EJB and waits for the response. For
     * each key, up on receiving U2F Authentication nonce parameters from the
     * EJB, there is a user session entry added to the session map maintained in
     * memory.
     *
     * @param did - FIDO domain id
     * @param protocol - U2F protocol version to comply with.
     * @param payload - A stringified json with username field embedded into it.
     *
     * Example: { "username" : "johndoe" }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Auth Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @Override
    public String preauthenticate(String did, String protocol, String payload) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0005", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " payload");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002")
                    + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0014")
                    + " Invalid json");
        }

        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " protocol");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002")
                    + " protocol");
        }
        if (!SKFSCommon.isFIDOProtocolSupported(protocol)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5002", protocol);
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-5002")
                    + protocol);
        }

        //  3. Retrieve data from payload
        //  fetch the username
        //  fetch extensions (if they exist)
        String username = (String) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_USERNAME, "String");
        JsonObject extensions = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_EXTENSIONS, "JsonObject");
        // fetch options inputs if they exist (TODO refactor when options are no longer in payload)
        String userVerification = (String) applianceCommon.getJsonValue(payload,
                SKFSConstants.FIDO2_ATTR_USERVERIFICATION, "String");
        JsonObjectBuilder optionsBuilder = Json.createObjectBuilder();
        if (userVerification != null) {
            optionsBuilder.add(SKFSConstants.FIDO2_ATTR_USERVERIFICATION, userVerification);
        }
        JsonObject options = optionsBuilder.build();

        if (username == null || username.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002")
                    + " username");
        }

        //  4. Process pre-authentication
        String responseJSON = twofpreauth(did, protocol, username, options, extensions, "preauthenticate");

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  5. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0006", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]"
                + "\nU2FAuthentication Challenge parameters = " + responseJSON);
        return responseJSON;
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
     * Method that performs authentication process in FIDO U2F protocol. This
     * method receives the request, performs basic input checks, parses the
     * sessionid from the U2F registration response parameters passed. Based on
     * the sessionid, the user session is validated. If found valid, the
     * authentication process is handed over to an EJB. Upon getting response
     * from the EJB, this method persists the Authentication information to the
     * database. This would contain user key modified location, modification
     * time and the counter. The user session at this point is removed or
     * invalidated even if the registration fails.
     *
     * @param did - FIDO domain id
     * @param payload - A stringified json with signresponse and sign metadata
     * embedded into it.
     *
     * Example: { "response": { "clientData": "...", "keyHandle": "...",
     * "signatureData": "..." }, "metadata": { "version": "1.0",
     * "modify_location": "Sunnyvale, CA" } }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @Override
    public String authenticate(String did, String protocol, String payload, String agent, String cip) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0007", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0010", " payload");
            return SKFSCommon.buildAuthenticateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0010") + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildAuthenticateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " Invalid json");
        }

        //  3. Retrieve data from payload
        //  fetch response and metadata fields from payload
        JsonObject response = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_RESPONSE, "JsonObject");
        if (response == null || response.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0010", "");
            return SKFSCommon.buildAuthenticateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0010"));
        }

        JsonObject metadata = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_METADATA, "JsonObject");
        if (metadata == null || metadata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0017", "");
            return SKFSCommon.buildAuthenticateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0017"));
        }

        String authenticationresponse = response.toString();
        String authenticationmetadata = metadata.toString();

        //  4. Finish authentication
        String responseJSON;
        try {
            responseJSON = twofauthenticate(did,
                    authenticationresponse,
                    authenticationmetadata,
                    protocol,
                    "authentication",
                    agent,
                    cip);
        } catch (SKFEException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0034", "");
            return SKFSCommon.buildAuthenticateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0034"));
        }

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  5. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0008", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]" + "\nResponse = " + responseJSON);
        return responseJSON;
    }

    private String twofpreauth(String did, String protocol, String username, JsonObject options, JsonObject extensions, String method) {
        String appid = applianceMaps.getDomain(Long.parseLong(did)).getSkfeAppid();
        String logs = "";
        String errmsg = "";
        JsonObject jsonObject = null;

        //  3. Do processing
        //  Look into the database to check for key handles
        String[] keyhandles;
        String[] upkeys;
        String[] appids;
        JsonArray[] transports;
        Long[] regkeyids;
        Short[] serverids;
        String responseJSON;
        if (protocol.equalsIgnoreCase(SKFSConstants.FIDO_PROTOCOL_VERSION_U2F_V2)) {
            try {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0031", "");
                Collection<FidoKeys> kh_coll
                        = getkeybean.getByUsernameStatus(Long.parseLong(did), username, applianceConstants.ACTIVE_STATUS);
                if (kh_coll == null || kh_coll.size() <= 0) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0007", "");
                    return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0007"));
                }

                keyhandles = new String[kh_coll.size()];
                upkeys = new String[kh_coll.size()];
                appids = new String[kh_coll.size()];
                regkeyids = new Long[kh_coll.size()];
                serverids = new Short[kh_coll.size()];
                transports = new JsonArray[kh_coll.size()];

                Iterator it = kh_coll.iterator();
                int i = 0;

                //  Populate all key handles and their respective origins.
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0032", "");
                while (it.hasNext()) {
                    FidoKeys key = (FidoKeys) it.next();
                    if (key != null) {
                        String mapkey = key.getFidoKeysPK().getSid() + "-" + key.getFidoKeysPK().getDid() + "-" + key.getFidoKeysPK().getFkid();
                        FidoKeysInfo fkinfoObj = new FidoKeysInfo(key);
                        skceMaps.getMapObj().put(SKFSConstants.MAP_FIDO_KEYS, mapkey, fkinfoObj);
                        keyhandles[i] = decryptKH(key.getKeyhandle());
                        upkeys[i] = key.getPublickey();
                        regkeyids[i] = key.getFidoKeysPK().getFkid();
                        serverids[i] = key.getFidoKeysPK().getSid();
                        appids[i] = key.getAppid();
                        transports[i] = SKFSCommon.getTransportJson(key.getTransports().intValue());
                        i++;
                    }
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0008", ex.getMessage());
                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0008")
                        + ex.getMessage());
            }

            U2FAuthenticationChallenge[] authresponses
                    = new U2FAuthenticationChallenge[keyhandles.length];
            JsonArray signDataArray;
            try {
                //  For every keyhandle found, make a preauthenticate call and get authresponse
                //  also add a user session for every preauthenticate call.
                StringBuffer buf = new StringBuffer();
                buf.append(logs);
                for (int j = 0; j < keyhandles.length; j++) {
                    FEreturn fer = u2fpreauthbean.execute(Long.parseLong(did), protocol, username, keyhandles[j], appids[j], transports[j]);
                    if (fer != null) {
                        authresponses[j] = (U2FAuthenticationChallenge) fer.getResponse();
                        buf.append(fer.getLogmsg());
                    }
                }
                logs = buf.toString();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0026",
                        " key handles count = " + keyhandles.length);

                if (authresponses != null) {
                    String nonce = U2FUtility.getRandom(Integer.parseInt(SKFSCommon.getConfigurationProperty("skfs.cfg.property.entropylength")));

                    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                    JsonArrayBuilder allowedCredBuilder = Json.createArrayBuilder();
                    for (int k = 0; k < authresponses.length; k++) {
                        U2FAuthenticationChallenge authChallenge = authresponses[k];

                        //  fetch the details of auth nonce
                        String keyhandle = authChallenge.getKeyhandle();
                        JsonArray trasnportArrsy = authChallenge.getTransports();
                        String KHHash = SKFSCommon.getDigest(keyhandle, "SHA-256");

                        //                    String khurlsafe = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(Base64.decode(keyhandle));
                        //                    System.out.println("KH b64 = " + keyhandle);
                        //                    System.out.println("KH b64URL = " + khurlsafe);
                        //  add a user session of type preauthenticate.
                        UserSessionInfo session = new UserSessionInfo(username,
                                nonce, appid, SKFSConstants.FIDO_USERSESSION_AUTH, upkeys[k], "");
                        session.setFkid(regkeyids[k]);
                        session.setSkid(serverids[k]);
                        session.setSid(applianceCommon.getServerId().shortValue());
                        skceMaps.getMapObj().put(SKFSConstants.MAP_USER_SESSION_INFO, KHHash, session);

                        //replicate map to other server
                        session.setMapkey(KHHash);
                        try {
                            if (applianceCommon.replicate()) {
                                replObj.execute(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, applianceCommon.getServerId().toString(), session);
                            }
                        } catch (Exception e) {
                            throw new RuntimeException(e.getLocalizedMessage());
                        }
                        //end publish
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0021", " username=" + username);

                        arrayBuilder.add(authChallenge.toJsonObject(appid));
                        //                    allowedCredBuilder.add(Json.createObjectBuilder().add("type", "public-key").add("id", keyhandle).add("transports", trasnportArrsy));
                        allowedCredBuilder.add(Json.createObjectBuilder().add("type", "public-key").add("id", keyhandle)); // TODO: fix transport array (removed it)
                    }
                    signDataArray = arrayBuilder.build();
                    if (signDataArray == null) {
                        jsonObject = Json.createObjectBuilder()
                                .add(SKFSConstants.JSON_KEY_NONCE, nonce)
                                .add(SKFSConstants.JSON_KEY_APP_ID, appid)
                                .add(SKFSConstants.JSON_KEY_REGISTEREDKEY, "").
                                build();
                    } else {
                        jsonObject = Json.createObjectBuilder()
                                .add(SKFSConstants.JSON_KEY_NONCE, nonce)
                                .add(SKFSConstants.JSON_KEY_APP_ID, appid)
                                .add(SKFSConstants.JSON_KEY_REGISTEREDKEY, signDataArray).
                                build();
                    }
                }
//            } catch (SKFEException ex) {
//                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0009", ex.getMessage());
//                return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0009") + ex.getMessage());
            } catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
                Logger.getLogger(u2fServletHelperBean.class.getName()).log(Level.SEVERE, null, ex);
            }
            responseJSON = SKFSCommon.buildPreAuthResponse(jsonObject, logs, errmsg);
        } else {
            responseJSON = fido2preauthbean.execute(Long.parseLong(did), username, options, extensions);
        }

        // Build the output json object
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0036", "");

        return responseJSON;
    }

    private String twofauthenticate(String did,
            String authresponse,
            String authmetadata,
            String protocol,
            String method, String agent, String cip) throws SKFEException {
        /* Check for needed fields in authresponse and metadata */
        String logs = "";
        String errmsg = "";
        String response = null;
        String responseJSON;

        //  based on the structure of the regresponse, decide the protocol version.
        if (protocol == null || protocol.trim().isEmpty()) {
            protocol = SKFSConstants.FIDO_PROTOCOL_VERSION_U2F_V2;
        }
        if (protocol.equalsIgnoreCase(SKFSConstants.FIDO_PROTOCOL_VERSION_U2F_V2)) {
            //  fetch needed fields from authresponse
            String browserdata = (String) applianceCommon.getJsonValue(authresponse,
                    SKFSConstants.JSON_KEY_CLIENTDATA, "String");
            if (browserdata == null || browserdata.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0011", " Missing 'clientData'");
                return SKFSCommon.buildAuthenticateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0011")
                        + " Missing 'clientData'");
            }

            //parse browserdata
            try {
                String browserdataJson = new String(Base64.getUrlDecoder().decode(browserdata), "UTF-8");
                JsonReader jsonReader = Json.createReader(new StringReader(browserdataJson));
                JsonObject jsonObject = jsonReader.readObject();
                jsonReader.close();

                String bdreqtype = jsonObject.getString(SKFSConstants.JSON_KEY_REQUESTTYPE);
                String bdnonce = jsonObject.getString(SKFSConstants.JSON_KEY_NONCE);
                String bdorigin = jsonObject.getString(SKFSConstants.JSON_KEY_SERVERORIGIN);
                if (bdreqtype == null || bdnonce == null || bdorigin == null) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                            SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Missing 'registrationData'");
                    return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                            + " Missing 'registrationData'");
                }
            } catch (Exception ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                        SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'clientDATA'");
                return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'clientDATA'");
            }

            String signdata = (String) applianceCommon.getJsonValue(authresponse,
                    SKFSConstants.JSON_KEY_SIGNATUREDATA, "String");
            if (signdata == null || signdata.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0011", " Missing 'signatureData'");
                return SKFSCommon.buildAuthenticateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0011")
                        + " Missing 'signatureData'");
            }

            String keyhandle = (String) applianceCommon.getJsonValue(authresponse,
                    SKFSConstants.JSON_USER_KEY_HANDLE_SERVLET, "String");
            if (keyhandle == null || keyhandle.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0011", " Missing 'keyHandle'");
                return SKFSCommon.buildAuthenticateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0011")
                        + " Missing 'keyHandle'");
            }

            //  fetch version and modifylocation from metadata
            String version = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_VERSION, "String");
            if (version == null || version.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - version");
                return SKFSCommon.buildAuthenticateResponse("", "",
                        SKFSCommon.getMessageProperty("FIDO-ERR-0019") + " Missing metadata - version");
            }

            String modifyloc = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_MODIFY_LOC, "String");
            if (modifyloc == null || modifyloc.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - modifylocation");
                return SKFSCommon.buildAuthenticateResponse("", "",
                        SKFSCommon.getMessageProperty("FIDO-ERR-0019") + " Missing metadata - modifylocation");
            }

            String username_received = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_USERNAME, "String");
            if (username_received == null || username_received.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - username");
                return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0019")
                        + " Missing metadata - username");
            }

            long regkeyid;
            short serverid;
            String username = "";
            String KHhash;
            String challenge = null;
            String appid_Received = "";

            try {
                //  calculate the hash of keyhandle received
                KHhash = SKFSCommon.getDigest(keyhandle, "SHA-256");
            } catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0001", " Error generating hash");
                return SKFSCommon.buildAuthenticateResponse("", "",
                        SKFSCommon.getMessageProperty("FIDO-ERR-0001") + " Error generating hash");
            }

            //  Look for the sessionid in the sessionmap and retrieve the username
            UserSessionInfo user = (UserSessionInfo) skceMaps.getMapObj().get(SKFSConstants.MAP_USER_SESSION_INFO, KHhash);
            if (user == null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0006", "");
                return SKFSCommon.buildAuthenticateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0006"));
            } else if (user.getSessiontype().equalsIgnoreCase(SKFSConstants.FIDO_USERSESSION_AUTH)) {
                username = user.getUsername();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0022", " username=" + username);

                appid_Received = user.getAppid();
                challenge = user.getNonce();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0042", " appid=" + appid_Received);
            }

            //verify that the call is for the right user
            if (!username.equalsIgnoreCase(username_received)) {
                //throw erro saying wrong username sent
                return SKFSCommon.buildAuthenticateResponse("", "",
                        SKFSCommon.getMessageProperty("FIDO-ERR-0037"));
            }

            //appid verifier
            String origin = SKFSCommon.getOriginfromBrowserdata(browserdata);
            if (!originverifierbean.execute(appid_Received, origin)) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0032", "");
                return SKFSCommon.buildRegisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0032")
                        + " : " + appid_Received + "-" + origin);
            }

            //  3. Do processing
            //  fetch the user public key from the session map.
            String userpublickey = user.getUserPublicKey();
            regkeyid = user.getFkid();
            serverid = user.getSkid();

            //  instantiate the fido interface and send the information for processing
            FEreturn fer;

            try {
                fer = u2fauthbean.execute(Long.parseLong(did), protocol, authresponse, userpublickey, challenge, appid_Received);
            } catch (SKFEException ex) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0001", ex.getLocalizedMessage());
                return SKFSCommon.buildAuthenticateResponse("", "",
                        SKFSCommon.getMessageProperty("FIDO-ERR-0001") + ex.getLocalizedMessage());
            }

            if (fer != null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0046", fer.getResponse());
                logs = fer.getLogmsg();
                U2FAuthenticationResponse authResponse = (U2FAuthenticationResponse) fer.getResponse();
                if (authResponse != null) {
                    //  Fetch the needed info from auth wsresponse return
                    int newCounter = authResponse.getCounter();
                    int userpresence = authResponse.getUsertouch();
//                String chall = authResponse.getChallenge();
//                try {
//                    KHhash = SKFSCommon.getDigest(chall, "SHA-256");
//                } catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
//                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0001", " Error generating hash");
//                    return SKFSCommon.buildAuthenticateResponse("", logs, SKFSCommon.getMessageProperty("FIDO-ERR-0001") + " Error generating hash");
//                }
                    if (userpresence != SKFSConstants.USER_PRESENT_FLAG) {
                        //  Remove the sessionid from the sessionmap
                        skceMaps.getMapObj().remove(SKFSConstants.MAP_USER_SESSION_INFO, KHhash);
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0023", "");

                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0031", "");
                        return SKFSCommon.buildAuthenticateResponse("", logs, SKFSCommon.getMessageProperty("FIDO-ERR-0031"));
                    }
                    //  Persist sign counter info & the user presence bytes to the database - TBD
                    FidoKeys key = null;
                    FidoKeysInfo fkinfo = (FidoKeysInfo) skceMaps.getMapObj().get(SKFSConstants.MAP_FIDO_KEYS, serverid + "-" + did + "-" + regkeyid);
                    if (fkinfo != null) {
                        key = fkinfo.getFk();
                    }
                    if (key == null) {
                        key = getkeybean.getByfkid(serverid, Long.parseLong(did), regkeyid);
                    }
                    if (key != null) {
                        int oldCounter = key.getCounter();
                        if (oldCounter != 0) {
                            if (newCounter <= oldCounter) {
                                /**
                                 * Ideally should not happen. Neither does U2F
                                 * protocol specifies how to handle this issue.
                                 * So, just throw a warning msg in the logs and
                                 * proceed ahead.
                                 */
                                //  Remove the user session from the sessionmap
                                skceMaps.getMapObj().remove(SKFSConstants.MAP_USER_SESSION_INFO, KHhash);
                                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0023", "");

                                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0030", "");
                                return SKFSCommon.buildAuthenticateResponse("", logs, SKFSCommon.getMessageProperty("FIDO-ERR-0030"));
                            }
                        }
                        //  update the sign counter value in the database with the new counter value.
                        String jparesult = updatekeybean.execute(serverid, Long.parseLong(did), regkeyid, newCounter, modifyloc);
                        JsonObject jo;
                        try (JsonReader jr = Json.createReader(new StringReader(jparesult))) {
                            jo = jr.readObject();
                        }
                        Boolean status = jo.getBoolean(SKFSConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                        if (status) {
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0027", "");
                        } else {
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0026", " new value=" + newCounter);
                        }
                    }

                    //  Remove the sessionid from the sessionmap
                    skceMaps.getMapObj().remove(SKFSConstants.MAP_USER_SESSION_INFO, KHhash);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0023", " username=" + username);
                } else {
                    //  Remove the sessionid from the sessionmap
                    skceMaps.getMapObj().remove(SKFSConstants.MAP_USER_SESSION_INFO, KHhash);
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0023", "");

                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0015", "");
                    return SKFSCommon.buildAuthenticateResponse("", logs, SKFSCommon.getMessageProperty("FIDO-ERR-0015") + "");
                }

                switch (method) {
                    case "authentication":
                        response = "Successfully processed sign response";
                        break;
                    case "authorization":
                        response = "Successfully processed authorization response";
                        break;
                    default:
                        break;
                }
            } else {
                switch (method) {
                    case "authentication":
                        errmsg = "Failed to process sign response";
                        break;
                    case "authorization":
                        errmsg = "Failed to process authorization response";
                        break;
                    default:
                        break;
                }
            }
            responseJSON = SKFSCommon.buildAuthenticateResponse(response, logs, errmsg);
        } else {
            responseJSON = FIDO2Authejb.execute(Long.parseLong(did), authresponse, authmetadata, method, null,null, agent, cip);
        }

        // Build the output json object
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0038", "");
        return responseJSON;
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
     * Method that performs pre-authorize process in FIDO U2F protocol. This
     * method receives the request, performs basic input checks and then looks
     * into the database for the number of FIDO authenticators the user has
     * already successfully registered. For each registered key, this method
     * hands over the pre-authorize process to an EJB and waits for the
     * wsresponse. For each key, up on receiving U2F Authentication nonce
     * parameters from the EJB, there is a user session entry added to the
     * session map maintained in memory.
     *
     * @param did - FIDO domain id
     * @param protocol - U2F protocol version to comply with.
     * @param payload - A stringified json with username field embedded into it.
     *
     * Example: { "username" : "johndoe" }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Auth Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @Override
    public String preauthorize(String did, String protocol, String payload) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0013", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " payload");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002")
                    + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014",
                    " Invalid json");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0014")
                    + " Invalid json");
        }

        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " protocol");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002")
                    + " protocol");
        }
        if (!SKFSCommon.isFIDOProtocolSupported(protocol)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5002", protocol);
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-5002")
                    + " protocol");
        }

        //  3. Retrieve data from payload
        //  fetch the username
        String username = (String) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_USERNAME, "String");
        JsonObject extensions = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_EXTENSIONS, "JsonObject");

        // fetch options inputs if they exist (TODO refactor when options are no longer in payload)
        String userVerification = (String) applianceCommon.getJsonValue(payload,
                SKFSConstants.FIDO2_ATTR_USERVERIFICATION, "String");
        JsonObjectBuilder optionsBuilder = Json.createObjectBuilder();
        if (userVerification != null) {
            optionsBuilder.add(SKFSConstants.FIDO2_ATTR_USERVERIFICATION, userVerification);
        }
        JsonObject options = optionsBuilder.build();

        if (username == null || username.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002")
                    + " username");
        }

        String responseJSON = twofpreauth(did, protocol, username, options, extensions, "preauthorize");

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  4. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0014", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]"
                + "\nU2FAuthorization Challenge parameters = " + responseJSON);
        return responseJSON;
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
     * Method that performs authorization process (not in FIDO U2F protocol).
     * This method receives the request, performs basic input checks, parses the
     * sessionid from the U2F authentication response parameters passed. Based
     * on the sessionid, the user session is validated. If found valid, the
     * authorization process is handed over to an EJB. Upon getting response
     * from the EJB, this method persists the authorization information to the
     * database. This would contain user key modified location, modification
     * time and the counter. The user session at this point is removed or
     * invalidated even if the authorization fails.
     *
     * @param did - FIDO domain id
     * @param payload - A stringified json with signresponse and sign metadata
     * embedded into it.
     *
     * Example: { "response": { "clientData": "...", "keyHandle": "...",
     * "signatureData": "..." }, "metadata": { "version": "1.0",
     * "modify_location": "Sunnyvale, CA" } }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @Override
    public String authorize(String did, String protocol, String payload) {
        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, SKFSCommon.getMessageProperty("FIDO-MSG-0015"), "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0010", " payload");
            return SKFSCommon.buildAuthenticateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0010") + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildAuthenticateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " Invalid json");
        }

        //  3. Fetch response and metadata fields from payload
        JsonObject response = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_RESPONSE, "JsonObject");
        if (response == null || response.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0010", "");
            return SKFSCommon.buildAuthenticateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0010"));
        }

        JsonObject metadata = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_METADATA, "JsonObject");
        if (metadata == null || metadata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0017", "");
            return SKFSCommon.buildAuthenticateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0017"));
        }

        String authorizationresponse = response.toString();
        String authorizationmetadata = metadata.toString();

        String responseJSON;
        try {
            responseJSON = twofauthenticate(did,
                    authorizationresponse,
                    authorizationmetadata,
                    protocol,
                    "authorization",
                    "",
                    "");
        } catch (SKFEException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0034", "");
            return SKFSCommon.buildAuthenticateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0034"));
        }

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  4. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0016", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]" + "\nResponse = " + responseJSON);
        return responseJSON;
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
     * Method that performs de-registration process in FIDO U2F protocol. The
     * protocol as such does not specify any de-registration process. So, this
     * method just removes the user registered fido authenticator information
     * from the persistent storage.
     *
     * If the user has registered multiple fido authenticators, to identify
     * which key has to be deleted, there is a random id to be passed. This
     * random id for every registered fido authenticator can be obtained by
     * making the getkeysinfo call, which will return an array of registered key
     * metadata, each entry mapped to a random id. These random ids have a 'ttl
     * (time-to-live)' associated with them. This information is also sent back
     * with the key metadata during getkeysinfo call. The client applications
     * have to cache these random ids if they wish to de-register keys.
     *
     * @param did - FIDO domain id
     * @param protocol - U2F protocol version to comply with.
     * @param payload - A stringified json with deregistration request embedded
     * into it.
     *
     * Example: { "request": { "username": "...", "randomid": "..." } }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in de-registration 3. 'Error' : String, with
     * error message incase something went wrong. Will be empty if successful.
     */
    @Override
    public String deregister(String did, String protocol, String payload) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0009", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " payload");
            return SKFSCommon.buildDeregisterResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildDeregisterResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " Invalid json");
        }

        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " protocol");
            return SKFSCommon.buildDeregisterResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " protocol");
        }
        if (!SKFSCommon.isFIDOProtocolSupported(protocol)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5002", protocol);
            return SKFSCommon.buildDeregisterResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-5002") + protocol);
        }

        //  3. fetch the deregistrationrequest
        JsonObject request = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_REQUEST,
                "JsonObject");
        if (request == null || request.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0021", "");
            return SKFSCommon.buildDeregisterResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0021"));
        }

        String deregistrationrequest = request.toString();

        //  4. fetch uername and randomid from request.
        String username = (String) applianceCommon.getJsonValue(deregistrationrequest, SKFSConstants.FIDO_JSON_KEY_USERNAME, "String");
        if (username == null || username.trim().isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0020", " Missing 'username'");
            return SKFSCommon.buildDeregisterResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0020") + " Missing 'username'");
        }

        String randomid = (String) applianceCommon.getJsonValue(deregistrationrequest, SKFSConstants.FIDO_JSON_KEY_RANDOMID, "String");
        if (randomid == null || randomid.trim().isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0020", " Missing 'randomid'");
            return SKFSCommon.buildDeregisterResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0020") + " Missing 'randomid'");
        }

        //  5. handover the job to an ejb
        String responseJSON;
        SKCEReturnObject skcero = u2fderegbean.execute(did, protocol, username, randomid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0046", skcero);
        if (skcero.getErrorkey() != null) {
            responseJSON = SKFSCommon.buildDeregisterResponse("", "", skcero.getErrormsg());
        } else {
            // Build the output
            String response = "Successfully deleted";
            responseJSON = SKFSCommon.buildDeregisterResponse(response, "", "");
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0039", "");
        }

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  6. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0010", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]" + "\nResponse" + responseJSON);
        return responseJSON;
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
     * Method that brings a specific user registered key stored in the
     * persistent storage into 'ACTIVE' status. The protocol as such does not
     * specify any activation process. So, this method just changes the user
     * registered fido authenticator information in the persistent storage to
     * bear an 'ACTIVE' status so that it could be used for fido-authentication.
     *
     * Since the user could have registered multiple fido authenticators, to
     * identify which key has to be activated, there is a random id to be
     * passed. This random id for every registered fido authenticator can be
     * obtained by making the getkeysinfo call, which will return an array of
     * registered key metadata, each entry mapped to a random id. These random
     * ids have a 'ttl (time-to-live)' associated with them. This information is
     * also sent back with the key metadata during getkeysinfo call. The client
     * applications have to cache these random ids if they wish to de-activate /
     * activate / de-register keys.
     *
     * @param did - FIDO domain id
     * @param protocol U2F protocol version to comply with.
     * @param payload - A stringified json with activate request and metadata
     * embedded into it.
     *
     * Example: { "request": { "username": "...", "randomid": "..." },
     * "metadata": { "version": "1.0", "modify_location": "Sunnyvale, CA" } }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in activation 3. 'Error' : String, with error
     * message incase something went wrong. Will be empty if successful.
     */
    @Override
    public String activate(String did, String protocol, String payload) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, SKFSCommon.getMessageProperty("FIDO-MSG-0019"), "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " payload");
            return SKFSCommon.buildActivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildActivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " Invalid json");
        }

        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " protocol");
            return SKFSCommon.buildActivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " protocol");
        }
        if (!SKFSCommon.isFIDOProtocolSupported(protocol)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5002", protocol);
            return SKFSCommon.buildActivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-5002") + protocol);
        }

        //  3. fetch request and metadata fields from payload
        JsonObject request = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_REQUEST, "JsonObject");
        if (request == null || request.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0021", "");
            return SKFSCommon.buildActivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0021"));
        }

        JsonObject metadata = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_METADATA, "JsonObject");
        if (metadata == null || metadata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", "");
            return SKFSCommon.buildActivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0019"));
        }

        String activationrequest = request.toString();
        String activationmetadata = metadata.toString();

        //  4. fetch username and randomid from request
        String username = (String) applianceCommon.getJsonValue(activationrequest,
                SKFSConstants.FIDO_JSON_KEY_USERNAME, "String");
        if (username == null || username.trim().isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0020", " Missing username");
            return SKFSCommon.buildActivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0020") + " Missing username");
        }

        String randomid = (String) applianceCommon.getJsonValue(activationrequest,
                SKFSConstants.FIDO_JSON_KEY_RANDOMID, "String");
        if (randomid == null || randomid.trim().isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0020", " Missing randomid");
            return SKFSCommon.buildActivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0020") + " Missing randomid");
        }

        //  5. fetch version and modifylocation from metadata
        String version = (String) applianceCommon.getJsonValue(activationmetadata,
                SKFSConstants.FIDO_METADATA_KEY_VERSION, "String");
        if (version == null || version.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - version");
            return SKFSCommon.buildActivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0019")
                    + " Missing metadata - version");
        }

        String modifyloc = (String) applianceCommon.getJsonValue(activationmetadata,
                SKFSConstants.FIDO_METADATA_KEY_MODIFY_LOC, "String");
        if (modifyloc == null || modifyloc.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - modifylocation");
            return SKFSCommon.buildActivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0019")
                    + " Missing metadata - modifylocation");
        }

        //  6. handover job to an ejb
        String responseJSON;
        SKCEReturnObject skcero = u2factbean.execute(did, protocol, username, randomid, modifyloc);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0046", skcero);
        if (skcero.getErrorkey() != null) {
            responseJSON = SKFSCommon.buildActivateResponse("", "", skcero.getErrormsg());
        } else {
            // Build the output
            String response = "Successfully activated user registered security key";
            responseJSON = SKFSCommon.buildActivateResponse(response, "", "");
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0052", "");
        }

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  7. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0020", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]" + "\nResponse" + responseJSON);
        return responseJSON;
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
     * Method that puts a specific user registered key stored in the persistent
     * storage into 'INACTIVE' status. The protocol as such does not specify any
     * de-activation process. So, this method just changes the user registered
     * fido authenticator information in the persistent storage to bear an
     * 'INACTIVE' status so that it COULD NOT be used for fido-authentication.
     * However, such inactive keys can be made active by calling activate
     * method.
     *
     * Since the user could have registered multiple fido authenticators, to
     * identify which key has to be activated, there is a random id to be
     * passed. This random id for every registered fido authenticator can be
     * obtained by making the getkeysinfo call, which will return an array of
     * registered key metadata, each entry mapped to a random id. These random
     * ids have a 'ttl (time-to-live)' associated with them. This information is
     * also sent back with the key metadata during getkeysinfo call. The client
     * applications have to cache these random ids if they wish to de-activate /
     * activate / de-register keys.
     *
     * @param did - FIDO domain id
     * @param protocol - U2F protocol version to comply with.
     * @param payload - A stringified json with deactivate request and metadata
     * embedded into it.
     *
     * Example: { "request": { "username": "...", "randomid": "..." },
     * "metadata": { "version": "1.0", "modify_location": "Sunnyvale, CA" } }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in de-activation 3. 'Error' : String, with
     * error message incase something went wrong. Will be empty if successful.
     */
    @Override
    public String deactivate(String did, String protocol, String payload) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0017", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " payload");
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " Invalid json");
        }

        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " protocol");
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " protocol");
        }
        if (!SKFSCommon.isFIDOProtocolSupported(protocol)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5002", protocol);
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-5002") + protocol);
        }

        //  3. fetch request and metadata fields from payload
        JsonObject request = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_REQUEST, "JsonObject");
        if (request == null || request.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0021", "");
            return SKFSCommon.buildDeactivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0021"));
        }

        JsonObject metadata = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_METADATA, "JsonObject");
        if (metadata == null || metadata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", "");
            return SKFSCommon.buildDeactivateResponse("", "", SKFSCommon.getMessageProperty("FIDO-ERR-0019"));
        }

        String deactivationrequest = request.toString();
        String deactivationmetadata = metadata.toString();

        //  4. fetch username and randomid from request
        String username = (String) applianceCommon.getJsonValue(deactivationrequest,
                SKFSConstants.FIDO_JSON_KEY_USERNAME, "String");
        if (username == null || username.trim().isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0020", " Missing username");
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0020") + " Missing username");
        }

        String randomid = (String) applianceCommon.getJsonValue(deactivationrequest,
                SKFSConstants.FIDO_JSON_KEY_RANDOMID, "String");
        if (randomid == null || randomid.trim().isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0020", " Missing randomid");
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0020") + " Missing randomid");
        }

        //  5. fetch version and modifylocation from metadata
        String version = (String) applianceCommon.getJsonValue(deactivationmetadata,
                SKFSConstants.FIDO_METADATA_KEY_VERSION, "String");
        if (version == null || version.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - version");
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0019") + " Missing metadata - version");
        }

        String modifyloc = (String) applianceCommon.getJsonValue(deactivationmetadata,
                SKFSConstants.FIDO_METADATA_KEY_MODIFY_LOC, "String");
        if (modifyloc == null || modifyloc.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - modifylocation");
            return SKFSCommon.buildDeactivateResponse("", "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0019") + " Missing metadata - modifylocation");
        }

        //  6. handover job to an ejb
        String responseJSON;
        SKCEReturnObject skcero = u2fdeactbean.execute(did, protocol, username, randomid, modifyloc);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0046", skcero);
        if (skcero.getErrorkey() != null) {
            responseJSON = SKFSCommon.buildDeactivateResponse("", "", skcero.getErrormsg());
        } else {
            // Build the output
            String response = "Successfully de-activated user registered security key";
            responseJSON = SKFSCommon.buildDeactivateResponse(response, "", "");
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0051", "");
        }

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  7. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0018", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]" + "\nResponse" + responseJSON);
        return responseJSON;
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
     * Method that returns back the metadata about registered fido
     * authenticators for the given user.
     *
     * If the user has registered multiple fido authenticators, this method will
     * return an array of registered key metadata, each entry mapped to a random
     * id. These random ids have a 'ttl (time-to-live)' associated with them.
     * The client applications have to cache these random ids if they wish to
     * de-register keys.
     *
     * @param did - FIDO domain id
     * @param protocol - U2F protocol version to comply with.
     * @param payload - A stringified json with username whose keys information
     * has to be fetched.
     *
     * Example: { "username": "johndoe" }
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : A Json array, each entry signifying metadata of a key
     * registered; Metadata includes randomid and its time-to-live, creation and
     * modify location and time info etc., 2. 'Message' : Empty string since
     * there is no cryptographic work involved in this process. 3. 'Error' :
     * String, with error message incase something went wrong. Will be empty if
     * successful.
     */
    @Override
    public String getkeysinfo(String did, String protocol, String payload) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0011", "[TXID=" + ID + "]"
                + "\n did=" + did
                + "\n protocol=" + protocol
                + "\n payload=" + payload);

        //  2. Input checks
        if (payload == null || payload.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " payload");
            return SKFSCommon.buildPreRegisterResponse(null, "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " payload");
        }
        if (!SKFSCommon.isValidJsonObject(payload)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0014", " Invalid json");
            return SKFSCommon.buildPreRegisterResponse(null, "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0014") + " Invalid json");
        }

        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " protocol");
            return SKFSCommon.buildPreRegisterResponse(null, "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " protocol");
        }
        if (!SKFSCommon.isFIDOProtocolSupported(protocol)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5002", protocol);
            return SKFSCommon.buildPreRegisterResponse(null, "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-5002") + protocol);
        }

        //  fetch the username
        String username = (String) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_USERNAME, "String");
        if (username == null || username.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
            return SKFSCommon.buildPreRegisterResponse(null, "",
                    SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " username");
        }

        //  3. Hand over the job to an ejb.
        String responseJSON;
        SKCEReturnObject skcero = u2fgetkeysbean.execute(Long.parseLong(did), username);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0046", skcero);
        if (skcero.getErrorkey() != null) {
            responseJSON = SKFSCommon.buildGetKeyInfoResponse(null, "", skcero.getErrormsg());
        } else {
            // Build the output
            String keysJsonString = (String) skcero.getReturnval();
            JsonObject keysJsonObj;
            try (JsonReader jr = Json.createReader(new StringReader(keysJsonString))) {
                keysJsonObj = jr.readObject();
            }

            responseJSON = SKFSCommon.buildGetKeyInfoResponse(keysJsonObj, "", "");
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0040", "");
        }

        out = new Date();
        long rt = out.getTime() - in.getTime();
        //  4. Print output and Return
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0012", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]" + "\nResponse" + responseJSON);
        return responseJSON;
    }

    private String decryptKH(String token) {
        String retvalue = token;
        if (SKFSCommon.getConfigurationProperty("skfs.cfg.property.db.keyhandle.encrypt").equalsIgnoreCase("true")) {
            String clusterid = "1";
            String domainid = SKFSCommon.getConfigurationProperty("skfs.cfg.property.db.keyhandle.encrypt.saka.domainid");
            String sakausername = skceCommon.getClusterDomainProperty(Long.parseLong(clusterid), Long.parseLong(domainid), "username");
            String sakapassword = skceCommon.getClusterDomainProperty(Long.parseLong(clusterid), Long.parseLong(domainid), "password");
            String hosturl = skceCommon.getWorkingHostURLInCluster(Long.parseLong(clusterid), Long.parseLong(domainid));
            Encryption port = SAKAConnector.getSAKAConn().getSAKAPort(Integer.parseInt(clusterid), hosturl);
            if (port == null) {
                // Create URL for calling web-service
                URL baseUrl = com.strongkey.saka.web.EncryptionService.class.getResource(".");
                String ENCRYPTION_SERVICE_WSDL_SUFFIX = SKFSCommon.getConfigurationProperty("skfs.cfg.property.saka.encryption.wsdlsuffix");
                URL url = null;
                try {
                    url = new URL(baseUrl, hosturl + ENCRYPTION_SERVICE_WSDL_SUFFIX);
                } catch (MalformedURLException ex) {
                    Logger.getLogger(FidoKeys.class.getName()).log(Level.SEVERE, null, ex);
                }
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "SKCE-MSG-4028", hosturl);

                //  Check to see if the url is available.
                if (skceCommon.isURLAccessible(url)) {
                    // Create EncryptionService and Encryption objects
                    EncryptionService cryptosvc = new EncryptionService(url);
                    port = cryptosvc.getEncryptionPort();
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "SKCE-MSG-4013", hosturl);
                }
            }

            // Escrow the key and place the key/token in local map
            if (port != null) {
                try {
                    retvalue = port.decrypt(Long.parseLong(domainid), sakausername, sakapassword, token);

                } catch (StrongKeyLiteException_Exception ex) {
                    Logger.getLogger(FidoKeys.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        return retvalue;
    }
}
