/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.jwt.JWTCreateLocal;
import com.strongkey.skfs.pojos.RegistrationSettings;
import com.strongkey.skfs.policybeans.getCachedFidoPolicyMDSLocal;
import com.strongkey.skfs.policybeans.verifyFido2AuthenticationPolicyLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import org.bouncycastle.util.encoders.Base64;

@Stateless
public class FIDO2AuthenticateBean implements FIDO2AuthenticateBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    @EJB
    getFidoKeysLocal getkeybean;
    @EJB
    updateFidoKeysLocal updatekeybean;
    @EJB
    verifyFido2AuthenticationPolicyLocal verifyPolicyBean;
    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;
    @EJB
    JWTCreateLocal createJWT;

    @Override
    public String execute(Long did, String authresponse, String authmetadata, String method, String txid, String txpayload, String agent, String cip) {

        String userAgent, clientIP;
        String wsresponse = "", logs = "", errmsg = "";
        String userHandle ="", jwt = "";
        JsonObject txdetail = null;
        JsonArray FIDOAuthRefs = null;
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "AuthResponse : " + authresponse);
        String id = (String) applianceCommon.getJsonValue(authresponse,
                SKFSConstants.JSON_KEY_ID, "String");
        String rawId = (String) applianceCommon.getJsonValue(authresponse,
                SKFSConstants.JSON_KEY_RAW_ID, "String");
        String credential_type = (String) applianceCommon.getJsonValue(authresponse,
                SKFSConstants.JSON_KEY_REQUEST_TYPE, "String");
        String responseObject = ((JsonObject) applianceCommon.getJsonValue(authresponse,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_RESPONSE, "JsonObject")).toString();

        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "Extracted AuthResponse : " + "\nid : " + id
                + "\nrawId : " + rawId + "\ncredential_type : " + credential_type + "\nresponseObject : " + responseObject);

        if (id == null || id.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'id'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'id'"));
        }
        String b64urlsafeId;
        try {
            b64urlsafeId = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(java.util.Base64.getUrlDecoder().decode(id));
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                    SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'id'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'id'"));
        }
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "b64urlid = " + b64urlsafeId);
        if (!id.equals(b64urlsafeId)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                    SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'id'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'id'"));
        }

        if (rawId == null || rawId.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'rawId'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'rawId'"));
        }

        String b64urlsaferawId = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(java.util.Base64.getUrlDecoder().decode(rawId));
        if (!rawId.equals(b64urlsaferawId)) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                    SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'rawId'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid rawIdid'"));
        }

        if (credential_type == null || credential_type.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'credential_type'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'credential_type'"));
        }

        if (!credential_type.equalsIgnoreCase("public-key")) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                    SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'credential_type'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'credential_type'"));
        }

        String browserdata = (String) applianceCommon.getJsonValue(responseObject,
                SKFSConstants.JSON_KEY_CLIENTDATAJSON, "String");
        if (browserdata == null || browserdata.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'clientData'");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'clientData'"));
        }

        //parse browserdata
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "browserdata : " + browserdata);

        try {
            String browserdataJson = new String(java.util.Base64.getDecoder().decode(browserdata), "UTF-8");
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "browserdataJson : " + browserdataJson);
            String bdreqtype = (String) applianceCommon.getJsonValue(browserdataJson, SKFSConstants.JSON_KEY_REQUEST_TYPE, "String"); //jsonObject.getString(SKFSConstants.JSON_KEY_REQUEST_TYPE);
            String bdnonce = (String) applianceCommon.getJsonValue(browserdataJson, SKFSConstants.JSON_KEY_NONCE, "String"); //jsonObject.getString(SKFSConstants.JSON_KEY_NONCE);
            String bdorigin = (String) applianceCommon.getJsonValue(browserdataJson, SKFSConstants.JSON_KEY_SERVERORIGIN, "String"); //jsonObject.getString(SKFSConstants.JSON_KEY_SERVERORIGIN);
            Boolean crossOrigin = (Boolean) applianceCommon.getJsonValue(browserdataJson, SKFSConstants.JSON_KEY_CROSSORIGIN, "Boolean"); //jsonObject.getString(SKFSConstants.JSON_KEY_SERVERORIGIN);
            if(crossOrigin == null){
                crossOrigin = Boolean.FALSE;
            }
//            String bdhashAlgo = (String) applianceCommon.getJsonValue(browserdataJson, SKFSConstants.JSON_KEY_HASH_ALGORITHM, "String"); // jsonObject.getString(SKFSConstants.JSON_KEY_HASH_ALGORITHM);

            if (bdreqtype == null || bdnonce == null || bdorigin == null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                        SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Missing 'authenticationnData'");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Missing 'authenticationnData'"));
            }
            if (!bdreqtype.equalsIgnoreCase("webauthn.get")) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                        SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'request type'");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'request type'"));
            }

            if (bdorigin.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                        SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'bdorigin'");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'bdorigin'"));
            }

            clientIP = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_CLIENT_IP, "String");
            if (clientIP == null) {
                clientIP = cip;
            }
            
            userAgent = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_CLIENT_USERAGENT, "String");
            if (userAgent == null) {
                userAgent = agent;
            }
              
              
            String origin = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_ORIGIN, "String");

            if (origin == null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                        SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'origin'");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'origin'"));
            }

            URI bdoriginURI = new URI(bdorigin);
            URI originURI = new URI(origin);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "RPID - BDORIGIN : " + originURI + " - " + bdoriginURI);

            if (!crossOrigin) {
                if (!bdoriginURI.equals(originURI)) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                            SKFSCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'origin'");
                    throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                            + " Invalid 'origin'"));
                }
            }

            String authenticatorObject = (String) applianceCommon.getJsonValue(responseObject,
                    SKFSConstants.JSON_KEY_AUTHENTICATORDATA, "String");
            if (authenticatorObject == null || authenticatorObject.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'authenticatorObject'");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                        + " Missing 'authenticatorObject'"));
            }
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "authenticatorObject : " + authenticatorObject);

            StringReader stringreader = new StringReader(responseObject);
            JsonReader jsonreader = Json.createReader(stringreader);
            JsonObject json = jsonreader.readObject();

            if (json.containsKey(SKFSConstants.JSON_KEY_USERHANDLE) && !json.isNull(SKFSConstants.JSON_KEY_USERHANDLE)) {
                userHandle = (String) applianceCommon.getJsonValue(responseObject,
                        SKFSConstants.JSON_KEY_USERHANDLE, "String");
                if (userHandle == null) { //|| userHandle.isEmpty()
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'userHandle'");
                    throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                            + " Missing 'userHandle'"));
                }
            }

            String signature = (String) applianceCommon.getJsonValue(responseObject,
                    SKFSConstants.JSON_KEY_SIGNATURE, "String");
            if (signature == null || signature.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'signature'");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                        + " Missing 'signature'"));
            }
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "Signature : " + signature);

            byte[] authData = java.util.Base64.getUrlDecoder().decode(authenticatorObject);
            FIDO2AuthenticatorData authenticatorData = new FIDO2AuthenticatorData();
            authenticatorData.decodeAuthData(authData);

            String username_received = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_USERNAME, "String");
            if (username_received == null || username_received.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - username");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0019")
                        + " Missing metadata - username"));
            }

            byte[] encodedauthdata = authData;
            byte[] browserdatabytes = SKFSCommon.getDigestBytes(java.util.Base64.getDecoder().decode(browserdata), "SHA-256");
            byte[] signedBytes = new byte[encodedauthdata.length + browserdatabytes.length];
            System.arraycopy(encodedauthdata, 0, signedBytes, 0, encodedauthdata.length);
            System.arraycopy(browserdatabytes, 0, signedBytes, encodedauthdata.length, browserdatabytes.length);

            String modifyloc = (String) applianceCommon.getJsonValue(authmetadata,
                    SKFSConstants.FIDO_METADATA_KEY_MODIFY_LOC, "String");
            if (modifyloc == null || modifyloc.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - modifylocation");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0019") + " Missing metadata - modifylocation"));
            }

            //TODO token binding verification (Currently only does basic formatting checks)
            try {
                JsonObject clientJson = SKFSCommon.getJsonObjectFromString(browserdataJson);
                JsonObject tokenBinding = clientJson.getJsonObject(SKFSConstants.JSON_KEY_TOKENBINDING);
                if (tokenBinding != null) {
                    String tokenBindingStatus = tokenBinding.getString("status", null);
                    Set<String> validTokenBindingStatuses = new HashSet(Arrays.asList("present", "supported", "not-supported"));
                    if (tokenBindingStatus == null || tokenBindingStatus.isEmpty()
                            || !validTokenBindingStatuses.contains(tokenBindingStatus)) {
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Invalid 'tokenBinding'");
                        throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                                + " Invalid 'tokenBinding'"));
                    }
                }
            } catch (ClassCastException ex) {
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0005")
                        + " Missing 'tokenBinding'"));
            }

            long regkeyid;
            short serverid;
            String username = "";
            String KHhash;
            String challenge = null;
            String appid_Received = "";
            //  calculate the hash of keyhandle received
            String kh = id;
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "kh : " + kh);
            KHhash = SKFSCommon.getDigest(kh, "SHA-256");

            //  Look for the sessionid in the sessionmap and retrieve the username
            UserSessionInfo user = (UserSessionInfo) skceMaps.getMapObj().get(SKFSConstants.MAP_USER_SESSION_INFO, KHhash);
            if (user == null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0006", "");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0006")));
            } else if (user.getSessiontype().equalsIgnoreCase(SKFSConstants.FIDO_USERSESSION_AUTH) || user.getSessiontype().equalsIgnoreCase(SKFSConstants.FIDO_USERSESSION_AUTHORIZE)) {
                username = user.getUsername();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0022", " username=" + username);

                appid_Received = user.getAppid();
                challenge = user.getNonce();
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0042", " appid=" + appid_Received);
            }

            // Verify username received in metadata matches the username for the received challenge
            if (!username_received.equalsIgnoreCase(username)) {
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0037")));
            }

            //challenge verification is missing???
            if (!bdnonce.equals(challenge)) {
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "U2FAuthenticationResponse", SKFSCommon.getMessageProperty("FIDO-ERR-5012"), "");
                throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-5012"));
            }

            //check txid and txpayload to be same?
            if (method.equalsIgnoreCase("authorization")) {
                if (!txid.equals(user.getTxid())) {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "U2FAuthorizeResponse", SKFSCommon.getMessageProperty("FIDO-ERR-0020"), "tx");
                    throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0020"));
                }

                if (!txpayload.equals(user.getTxpayload())) {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "U2FAuthorizeResponse", SKFSCommon.getMessageProperty("FIDO-ERR-0020"), "tx");
                    throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0020"));
                }
            }
            
            //  3. Do processing
            //  fetch the user public key from the session map.
            String userpublickey = user.getUserPublicKey();
            regkeyid = user.getFkid();
            serverid = user.getSkid();

            FidoKeys key = null;
            FidoKeysInfo fkinfo = (FidoKeysInfo) skceMaps.getMapObj().get(SKFSConstants.MAP_FIDO_KEYS, serverid + "-" + did + "-" + regkeyid);
            if (fkinfo != null) {
                key = fkinfo.getFk();
            }
            if (key == null) {
                key = getkeybean.getByfkid(serverid, did, regkeyid);
            }

            FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(did, username_received, key);
            if (fidoPolicy == null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", "No policy found");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0009") + "No policy found"));
            }

            String rpId = fidoPolicy.getRpOptions().getId();
            String rpidServletExtracted;
            
                if (rpId == null) {
                    rpidServletExtracted = originURI.getHost();
                } else {
                    System.out.println("rpidhashfrompolicy = " + Base64.toBase64String(SKFSCommon.getDigestBytes(rpId, "SHA256")));
                    //check if the origin received is rpid+1 if not then reject it
                    if (origin.startsWith("android")) {
                        rpidServletExtracted = rpId;
                    } else {
                        String originwithoutSchemePort;
                        if (origin.startsWith("https")) {
                            originwithoutSchemePort = origin.substring(8).split(":")[0];
                        } else {
                            //reject it
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                    + " RPID Hash invalid'"));
                        }
                        if (!crossOrigin) {
                            if (!originwithoutSchemePort.endsWith(rpId)) {
                                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                        + " RPID Hash invalid'"));
                            }

                            String origin2 = originwithoutSchemePort.replace(rpId, "");
                            if (origin2.split("\\.").length > 1) {
                                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                        + " RPID Hash invalid'"));
                            }
                        }
                        rpidServletExtracted = rpId;
                    }
                
                    if (!crossOrigin) {
                        if (!Base64.toBase64String(authenticatorData.getRpIdHash()).equals(Base64.toBase64String(SKFSCommon.getDigestBytes(rpidServletExtracted, "SHA256")))) {
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                    + " RPID Hash invalid'"));
                        }
                    }
            }
            
            if (key != null) {
                RegistrationSettings rs = RegistrationSettings.parse(key.getRegistrationSettings(), key.getRegistrationSettingsVersion());
                String signingKeyType = getKeyTypeFromRegSettings(rs);
                byte[] publickeyBytes = java.util.Base64.getUrlDecoder().decode(userpublickey);
                Boolean isSignatureValid;
                KeyFactory kf = KeyFactory.getInstance(signingKeyType, "BCFIPS");
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publickeyBytes);
                PublicKey pub = kf.generatePublic(pubKeySpec);
                isSignatureValid = cryptoCommon.verifySignature(java.util.Base64.getUrlDecoder().decode(signature),
                        pub,
                        signedBytes,
                        SKFSCommon.getAlgFromIANACOSEAlg(rs.getAlg()));

                if (!isSignatureValid) {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDO-MSG-2001", "Authentication Signature verification : " + isSignatureValid);
                    throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                            + "Authentication Signature verification : " + isSignatureValid));
                }

                //Check authentication against policy
                verifyPolicyBean.execute(user, did, json, authenticatorData, key, rs.getAttestationFormat());

                //  update the sign counter value in the database with the new counter value.
                String jparesult = updatekeybean.execute(serverid, did, regkeyid, authenticatorData.getCounterValueAsInt(), modifyloc);
                JsonObject jo;
                try (JsonReader jr = Json.createReader(new StringReader(jparesult))) {
                    jo = jr.readObject();
                }
                Boolean status = jo.getBoolean(SKFSConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                if (status) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0027", "");
                } else {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0026", " new value=" + authenticatorData.getCounterValueAsInt());
                }

                //  Remove the sessionid from the sessionmap
                skceMaps.getMapObj().remove(SKFSConstants.MAP_USER_SESSION_INFO, KHhash);
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0023", " username=" + username);

                switch (method) {
                    case "authentication":
                        wsresponse = "Successfully processed sign response";
                        if(SKFSCommon.getConfigurationProperty(did, "skfs.cfg.property.jwt.create").equalsIgnoreCase("true")){
                            jwt = createJWT.execute(did.toString(), username, userAgent, clientIP , rpidServletExtracted);
                        }
                        break;
                    case "authorization":
                        wsresponse = "Successfully processed authorization response";
                        txdetail = Json.createObjectBuilder()
                                .add(SKFSConstants.TX_ID, user.getTxid())
                                .add(SKFSConstants.TX_PAYLOAD, user.getTxpayload())
                                .add(SKFSConstants.TX_NONCE, user.getInitnonce())
                                .add(SKFSConstants.TX_TIMESTAMP, user.getTxtimestamp())
                                .add(SKFSConstants.TX_CHALLENGE, user.getNonce())
                        .build();
                        
                        JsonObjectBuilder fidorefobjb = Json.createObjectBuilder();
                        JsonArrayBuilder fidorefarrb = Json.createArrayBuilder();
                        fidorefobjb.add("protocol", SKFSConstants.FIDO_PROTOCOL_VERSION_2_0);
                        fidorefobjb.add(SKFSConstants.JSON_KEY_ID, id);
                        fidorefobjb.add(SKFSConstants.JSON_KEY_RAW_ID, rawId);
                        fidorefobjb.add(SKFSConstants.JSON_KEY_USERHANDLE, userHandle);
                        fidorefobjb.add(SKFSConstants.FIDO2_PREAUTH_ATTR_RPID, rpId);
                        fidorefobjb.add(SKFSConstants.JSON_KEY_AUTHENTICATORDATA, authenticatorObject);
                        fidorefobjb.add(SKFSConstants.JSON_KEY_CLIENTDATAJSON, browserdata);
                        fidorefobjb.add("aaguid", key.getAaguid());
                        fidorefobjb.add("authorizationTime", new Date().getTime());
                        fidorefobjb.add("uv",authenticatorData.isUserVerified());
                        fidorefobjb.add("up",authenticatorData.isUserPresent());
                        fidorefobjb.add(SKFSConstants.TX_PUBLIC_KEY, key.getPublickey());
                        fidorefobjb.add(SKFSConstants.JSON_KEY_SIGNATURE, signature);
                        fidorefobjb.add("usedForThisTransaction", Boolean.TRUE);
                        fidorefobjb.add(SKFSConstants.FIDO_SIGNINGKEY_TYPE, signingKeyType);
                        fidorefobjb.add(SKFSConstants.FIDO_SIGNINGKEY_ALGO, SKFSCommon.getAlgFromIANACOSEAlg(rs.getAlg()));
                        fidorefarrb.add(fidorefobjb.build());
                        FIDOAuthRefs = fidorefarrb.build();
                        break;
                    default:
                        break;
                }
                
                        
            } else {
                throw new IllegalStateException("Unable to retrieve FIDO key from database");
            }
        } catch (URISyntaxException | IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | InvalidParameterSpecException | SKFEException ex) {
//            ex.printStackTrace();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                    SKFSCommon.getMessageProperty("FIDO-ERR-5011"), ex.getLocalizedMessage());
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-5011")
                    + ex.getLocalizedMessage()));
        }
        String responseJSON;
        if(method.equalsIgnoreCase("authorization")){
            
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, wsresponse);
            job.add(SKFSConstants.TX_DETAIL, txdetail);
            job.add(SKFSConstants.FIDOAuthenticatorReferences, FIDOAuthRefs);
            responseJSON = job.build().toString();
        }else{
            //call jwtcreate to generate a jwt
            JsonObjectBuilder job = Json.createObjectBuilder();
            job.add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, wsresponse);
            job.add("jwt", jwt);
            responseJSON = job.build().toString();
        }
         
        return responseJSON;
    }

    private String getKeyTypeFromRegSettings(RegistrationSettings rs) {
        if (rs.getKty() == 2) {
            return "ECDSA";
        } else if (rs.getKty() == 3) {
            return "RSA";
        } else {
            throw new SKIllegalArgumentException("Unknown Key Type");
        }
    }
}
