/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.google.common.primitives.Bytes;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.pojos.RegistrationSettings;
import com.strongkey.skfs.policybeans.getCachedFidoPolicyMDSLocal;
import com.strongkey.skfs.policybeans.verifyFido2AuthenticationPolicyLocal;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.io.StringReader;
import java.net.URI;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
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

    @Override
    public String execute(Long did, String authresponse, String authmetadata, String method) {

        String wsresponse = "", logs = "", errmsg = "";
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "AuthResponse : " + authresponse);
        String id = (String) applianceCommon.getJsonValue(authresponse,
                skfsConstants.JSON_KEY_ID, "String");
        String rawId = (String) applianceCommon.getJsonValue(authresponse,
                skfsConstants.JSON_KEY_RAW_ID, "String");
        String credential_type = (String) applianceCommon.getJsonValue(authresponse,
                skfsConstants.JSON_KEY_REQUEST_TYPE, "String");
        String responseObject = ((JsonObject) applianceCommon.getJsonValue(authresponse,
                skfsConstants.JSON_KEY_SERVLET_INPUT_RESPONSE, "JsonObject")).toString();

        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "Extracted AuthResponse : " + "\nid : " + id
                + "\nrawId : " + rawId + "\ncredential_type : " + credential_type + "\nresponseObject : " + responseObject);

        if (id == null || id.isEmpty()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'id'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'id'"));
        }
        String b64urlsafeId;
        try {
            b64urlsafeId = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(java.util.Base64.getUrlDecoder().decode(id));
        } catch (Exception ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                    skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'id'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'id'"));
        }
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "b64urlid = " + b64urlsafeId);
        if (!id.equals(b64urlsafeId)) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                    skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'id'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'id'"));
        }

        if (rawId == null || rawId.isEmpty()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'rawId'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'rawId'"));
        }

        String b64urlsaferawId = org.apache.commons.codec.binary.Base64.encodeBase64URLSafeString(java.util.Base64.getUrlDecoder().decode(rawId));
        if (!rawId.equals(b64urlsaferawId)) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                    skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'rawId'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid rawIdid'"));
        }

        if (credential_type == null || credential_type.isEmpty()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'credential_type'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'credential_type'"));
        }

        if (!credential_type.equalsIgnoreCase("public-key")) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                    skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'credential_type'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'credential_type'"));
        }

        String browserdata = (String) applianceCommon.getJsonValue(responseObject,
                skfsConstants.JSON_KEY_CLIENTDATAJSON, "String");
        if (browserdata == null || browserdata.isEmpty()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'clientData'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                    + " Missing 'clientData'"));
        }

        //parse browserdata
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "browserdata : " + browserdata);

        try {
            String browserdataJson = new String(java.util.Base64.getDecoder().decode(browserdata), "UTF-8");
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "browserdataJson : " + browserdataJson);
            String bdreqtype = (String) applianceCommon.getJsonValue(browserdataJson, skfsConstants.JSON_KEY_REQUEST_TYPE, "String"); //jsonObject.getString(skfsConstants.JSON_KEY_REQUEST_TYPE);
            String bdnonce = (String) applianceCommon.getJsonValue(browserdataJson, skfsConstants.JSON_KEY_NONCE, "String"); //jsonObject.getString(skfsConstants.JSON_KEY_NONCE);
            String bdorigin = (String) applianceCommon.getJsonValue(browserdataJson, skfsConstants.JSON_KEY_SERVERORIGIN, "String"); //jsonObject.getString(skfsConstants.JSON_KEY_SERVERORIGIN);
            String bdhashAlgo = (String) applianceCommon.getJsonValue(browserdataJson, skfsConstants.JSON_KEY_HASH_ALGORITHM, "String"); // jsonObject.getString(skfsConstants.JSON_KEY_HASH_ALGORITHM);

            if (bdreqtype == null || bdnonce == null || bdorigin == null) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                        skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Missing 'authenticationnData'");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Missing 'authenticationnData'"));
            }
            if (!bdreqtype.equalsIgnoreCase("webauthn.get")) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                        skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'request type'");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'request type'"));
            }

            if (bdorigin.isEmpty()) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                        skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'bdorigin'");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'bdorigin'"));
            }

            String origin = (String) applianceCommon.getJsonValue(authmetadata,
                    skfsConstants.FIDO_METADATA_KEY_ORIGIN, "String");

            if (origin == null) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                        skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'origin'");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'origin'"));
            }

            URI bdoriginURI = new URI(bdorigin);
            URI originURI = new URI(origin);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "RPID - BDORIGIN : " + originURI + " - " + bdoriginURI);

            if (!bdoriginURI.equals(originURI)) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                        skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'origin'");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                        + " Invalid 'origin'"));
            }

            String authenticatorObject = (String) applianceCommon.getJsonValue(responseObject,
                    skfsConstants.JSON_KEY_AUTHENTICATORDATA, "String");
            if (authenticatorObject == null || authenticatorObject.isEmpty()) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'authenticatorObject'");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                        + " Missing 'authenticatorObject'"));
            }
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "authenticatorObject : " + authenticatorObject);

            StringReader stringreader = new StringReader(responseObject);
            JsonReader jsonreader = Json.createReader(stringreader);
            JsonObject json = jsonreader.readObject();

            if (json.containsKey(skfsConstants.JSON_KEY_USERHANDLE) && !json.isNull(skfsConstants.JSON_KEY_USERHANDLE)) {
                String userHandle = (String) applianceCommon.getJsonValue(responseObject,
                        skfsConstants.JSON_KEY_USERHANDLE, "String");
                if (userHandle == null) { //|| userHandle.isEmpty()
                    skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'userHandle'");
                    throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                            + " Missing 'userHandle'"));
                }
            }

            String signature = (String) applianceCommon.getJsonValue(responseObject,
                    skfsConstants.JSON_KEY_SIGNATURE, "String");
            if (signature == null || signature.isEmpty()) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Missing 'signature'");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                        + " Missing 'signature'"));
            }
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-2001", "Signature : " + signature);

            byte[] authData = java.util.Base64.getUrlDecoder().decode(authenticatorObject);
            FIDO2AuthenticatorData authenticatorData = new FIDO2AuthenticatorData();
            authenticatorData.decodeAuthData(authData);

            String username_received = (String) applianceCommon.getJsonValue(authmetadata,
                    skfsConstants.FIDO_METADATA_KEY_USERNAME, "String");
            if (username_received == null || username_received.isEmpty()) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - username");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0019")
                        + " Missing metadata - username"));
            }

            FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(did, username_received);
            if (fidoPolicy == null) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", "No policy found");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0009") + "No policy found"));
            }

            String rpId = fidoPolicy.getRpOptions().getId();
            String rpidServletExtracted;
            if (rpId == null) {
                rpidServletExtracted = originURI.getHost();
            } else {
                System.out.println("rpidhashfrompolicy = " + Base64.toBase64String(skfsCommon.getDigestBytes(rpId, "SHA256")));
                //check if the origin received is rpid+1 if not then reject it
                if (origin.startsWith("android")) {
                    rpidServletExtracted = rpId;
                } else {
                    String originwithoutSchemePort;
                    if (origin.startsWith("https")) {
                        originwithoutSchemePort = origin.substring(8).split(":")[0];
                    } else {
                        //reject it
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                        throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-2001")
                                + " RPID Hash invalid'"));
                    }
                    String origin2 = originwithoutSchemePort.replace(rpId, "");
                    if (origin2.split("\\.").length > 1) {
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                        throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-2001")
                                + " RPID Hash invalid'"));
                    }
                    rpidServletExtracted = rpId;
                }
            }
            if (!Base64.toBase64String(authenticatorData.getRpIdHash()).equals(Base64.toBase64String(skfsCommon.getDigestBytes(rpidServletExtracted, "SHA256")))) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-2001")
                        + " RPID Hash invalid'"));
            }
            byte[] signedBytes = Bytes.concat(authData, skfsCommon.getDigestBytes(java.util.Base64.getDecoder().decode(browserdata), "SHA-256"));

            String modifyloc = (String) applianceCommon.getJsonValue(authmetadata,
                    skfsConstants.FIDO_METADATA_KEY_MODIFY_LOC, "String");
            if (modifyloc == null || modifyloc.isEmpty()) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0019", " Missing metadata - modifylocation");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0019") + " Missing metadata - modifylocation"));
            }

            //TODO token binding verification (Currently only does basic formatting checks)
            try {
                JsonObject clientJson = skfsCommon.getJsonObjectFromString(browserdataJson);
                JsonObject tokenBinding = clientJson.getJsonObject(skfsConstants.JSON_KEY_TOKENBINDING);
                if (tokenBinding != null) {
                    String tokenBindingStatus = tokenBinding.getString("status", null);
                    Set<String> validTokenBindingStatuses = new HashSet(Arrays.asList("present", "supported", "not-supported"));
                    if (tokenBindingStatus == null || tokenBindingStatus.isEmpty()
                            || !validTokenBindingStatuses.contains(tokenBindingStatus)) {
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0005", " Invalid 'tokenBinding'");
                        throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
                                + " Invalid 'tokenBinding'"));
                    }
                }
            } catch (ClassCastException ex) {
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0005")
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
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "kh : " + kh);
            KHhash = skfsCommon.getDigest(kh, "SHA-256");

            //  Look for the sessionid in the sessionmap and retrieve the username
            UserSessionInfo user = (UserSessionInfo) skceMaps.getMapObj().get(skfsConstants.MAP_USER_SESSION_INFO, KHhash);
            if (user == null) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0006", "");
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0006")));
            } else if (user.getSessiontype().equalsIgnoreCase(skfsConstants.FIDO_USERSESSION_AUTH)) {
                username = user.getUsername();
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0022", " username=" + username);

                appid_Received = user.getAppid();
                challenge = user.getNonce();
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0042", " appid=" + appid_Received);
            }

            // Verify username received in metadata matches the username for the received challenge
            if (!username_received.equalsIgnoreCase(username)) {
                throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0037")));
            }

            //  3. Do processing
            //  fetch the user public key from the session map.
            String userpublickey = user.getUserPublicKey();
            regkeyid = user.getFkid();
            serverid = user.getSkid();

            FidoKeys key = null;
            FidoKeysInfo fkinfo = (FidoKeysInfo) skceMaps.getMapObj().get(skfsConstants.MAP_FIDO_KEYS, serverid + "-" + did + "-" + username + "-" + regkeyid);
            if (fkinfo != null) {
                key = fkinfo.getFk();
            }
            if (key == null) {
                key = getkeybean.getByfkid(serverid, did, username, regkeyid);
            }
            if (key != null) {
                RegistrationSettings rs = RegistrationSettings.parse(key.getRegistrationSettings(), key.getRegistrationSettingsVersion());
                String signingKeyType = getKeyTypeFromRegSettings(rs);
                byte[] publickeyBytes = org.apache.commons.codec.binary.Base64.decodeBase64(userpublickey);
                Boolean isSignatureValid;
                KeyFactory kf = KeyFactory.getInstance(signingKeyType, "BCFIPS");
                X509EncodedKeySpec pubKeySpec = new X509EncodedKeySpec(publickeyBytes);
                PublicKey pub = kf.generatePublic(pubKeySpec);
                isSignatureValid = cryptoCommon.verifySignature(org.apache.commons.codec.binary.Base64.decodeBase64(signature),
                        pub,
                        signedBytes,
                        skfsCommon.getAlgFromIANACOSEAlg(rs.getAlg()));

                if (!isSignatureValid) {
                    skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDO-MSG-2001", "Authentication Signature verification : " + isSignatureValid);
                    throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-2001")
                            + "Authentication Signature verification : " + isSignatureValid));
                }

                //Check authentication against policy
                verifyPolicyBean.execute(user, did, json, authenticatorData, key);

                //  update the sign counter value in the database with the new counter value.
                String jparesult = updatekeybean.execute(serverid, did, username, regkeyid, authenticatorData.getCounterValueAsInt(), modifyloc);
                JsonObject jo;
                try (JsonReader jr = Json.createReader(new StringReader(jparesult))) {
                    jo = jr.readObject();
                }
                Boolean status = jo.getBoolean(skfsConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                if (status) {
                    skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0027", "");
                } else {
                    skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0026", " new value=" + authenticatorData.getCounterValueAsInt());
                }

                //  Remove the sessionid from the sessionmap
                skceMaps.getMapObj().remove(skfsConstants.MAP_USER_SESSION_INFO, KHhash);
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0023", " username=" + username);

                switch (method) {
                    case "authentication":
                        wsresponse = "Successfully processed sign response";
                        break;
                    case "authorization":
                        wsresponse = "Successfully processed authorization response";
                        break;
                }
            } else {
                throw new IllegalStateException("Unable to retrieve FIDO key from database");
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE,
                    skfsCommon.getMessageProperty("FIDO-ERR-5011"), " Invalid 'authenticatorDATA'");
            throw new SKIllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-5011")
                    + " Invalid 'authenticatorDATA'"));
        }
        String responseJSON = skfsCommon.buildReturn(wsresponse);
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
