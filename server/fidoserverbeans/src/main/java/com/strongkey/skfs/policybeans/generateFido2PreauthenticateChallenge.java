/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.policybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.core.U2FUtility;
import com.strongkey.skfs.fido.policyobjects.AlgorithmsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.AuthenticationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.DefinedExtensionsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido.policyobjects.RpPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2AuthenticationExtension;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2Extension;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.RegistrationSettings;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

@Stateless
public class generateFido2PreauthenticateChallenge implements generateFido2PreauthenticateChallengeLocal {

    @EJB
    replicateSKFEObjectBeanLocal replObj;
    @EJB
    getFidoKeysLocal getkeybean;
    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;

    //TODO refactor method into smaller pieces.
    @Override
    public String execute(Long did, String username, JsonObject options, JsonObject extensions) {
        //If unable to get UserId, there are no Active keys registered under the username
        String sendfakekeys = SKFSCommon.getConfigurationProperty(did, "skfs.cfg.property.fido2.user.sendfakeKH");
        Boolean sendfakekeyhandles = Boolean.FALSE;
        List<FidoKeys> fks = null ;
        try {
            fks = getkeybean.getKeysByUsernameStatus(did, username, "Active");
            if (fks.size() < 1) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0007", "");
                throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0007"));
            }else{
                
            }
//            getUserId(did, username);
        } catch (SKFEException ex) {
            //here we decide what to do whether to send fake key handles back or not
            if (sendfakekeys.equalsIgnoreCase("true")) {
                sendfakekeyhandles = Boolean.TRUE;
            } else {
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(ex.getLocalizedMessage()));
            }
        }
        
        FidoKeys fkey = null;
        if(fks.size() > 0) {
            fkey = fks.get(0);
        }
        //Gather useful information
        FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(did, username, fkey);
        if (fidoPolicy == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", "No policy found");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0009") + "No policy found"));
        }
        String challenge = generateChallenge(fidoPolicy.getAlgorithmsOptions());

        //Create response object
        JsonObjectBuilder returnObjectBuilder = Json.createObjectBuilder();
        
        try {
            if (sendfakekeyhandles) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_CHALLENGE, challenge)
                        .add(SKFSConstants.FIDO2_PREAUTH_ATTR_ALLOWCREDENTIALS,generatefakeAllowCredentialsList());
            } else {
//                fks = getkeybean.getByUsernameStatus(did, username, "Active");
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_CHALLENGE, challenge)
                        .add(SKFSConstants.FIDO2_PREAUTH_ATTR_ALLOWCREDENTIALS,
                                generateAllowCredentialsList(fidoPolicy.getAuthenticationOptions(), fks));
            }

            String rpId = generateRpId(fidoPolicy.getRpOptions());
            if (rpId != null) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_RPID, rpId);
            }
            String userVerificationPref = generateUserVerification(fidoPolicy, options);
            if (userVerificationPref != null) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_UV, userVerificationPref);
            }

            JsonObject extensionsJson = generateExtensions(fidoPolicy.getExtensionsOptions(), extensions);
            if (!extensionsJson.isEmpty()) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_EXTENSIONS, extensionsJson);
            }

            if (!sendfakekeyhandles) {
                //Place challenge in map.
                for (FidoKeys fk : fks) {
                    String KHHash = SKFSCommon.getDigest(fk.getKeyhandle(), "SHA-256");
                    UserSessionInfo session = new UserSessionInfo(username,
                            challenge, fk.getAppid(), SKFSConstants.FIDO_USERSESSION_AUTH, fk.getPublickey(), "");
                    session.setFkid(fk.getFidoKeysPK().getFkid());
                    session.setSkid(fk.getFidoKeysPK().getSid());
                    session.setSid(applianceCommon.getServerId().shortValue());
                    session.setuserVerificationReq(userVerificationPref);
                    session.setPolicyMapKey(fidoPolicy.getPolicyMapKey());
                    skceMaps.getMapObj().put(SKFSConstants.MAP_USER_SESSION_INFO, KHHash, session);

                    //replicate map to other server
                    session.setMapkey(KHHash);
                    if (applianceCommon.replicate()) {
                        replObj.execute(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, applianceCommon.getServerId().toString(), session);
                    }
                }
            }

            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0021"), " username=" + username);

            String response = Json.createObjectBuilder()
                    .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, returnObjectBuilder.build())
                    .build().toString();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "FIDO 2.0 Response : " + response);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0035", "");
            return response;
        } catch (SKFEException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", ex.getLocalizedMessage());
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0009") + ex.getLocalizedMessage()));
        }
    }

    
    
    @Override
    public String executePreAuthorize(Long did, String username, String txid, String txpayload, JsonObject options, JsonObject extensions) {
        String sendfakekeys = SKFSCommon.getConfigurationProperty(did, "skfs.cfg.property.fido2.user.sendfakeKH");
        Boolean sendfakekeyhandles = Boolean.FALSE;
        List<FidoKeys> fks = null ;
        try {
            fks = getkeybean.getKeysByUsernameStatus(did, username, "Active");
            if (fks.size() < 1) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0007", "");
                throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0007"));
            }else{
                
            }
//            getUserId(did, username);
        } catch (SKFEException ex) {
            //here we decide what to do whether to send fake key handles back or not
            if (sendfakekeys.equalsIgnoreCase("true")) {
                sendfakekeyhandles = Boolean.TRUE;
            } else {
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(ex.getLocalizedMessage()));
            }
        }

        //Gather useful information
        FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(did, username, fks.get(0));
        if (fidoPolicy == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", "No policy found");
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0009") + "No policy found"));
        }
        String nonce = generateChallenge(fidoPolicy.getAlgorithmsOptions());
        
        long txtime = new Date().getTime();
        
        String combinedchallenge = txtime + txid + txpayload + nonce;
        String challenge ;

        //Create response object
        JsonObjectBuilder returnObjectBuilder = Json.createObjectBuilder();
        
        try {
            challenge = SKFSCommon.getDigest(combinedchallenge, "SHA-256");
            if (sendfakekeyhandles) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_CHALLENGE, challenge)
                        .add(SKFSConstants.FIDO2_PREAUTH_ATTR_ALLOWCREDENTIALS,generatefakeAllowCredentialsList());
            } else {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_CHALLENGE, challenge)
                        .add(SKFSConstants.FIDO2_PREAUTH_ATTR_ALLOWCREDENTIALS,
                                generateAllowCredentialsList(fidoPolicy.getAuthenticationOptions(), fks));
            }

            returnObjectBuilder.add(SKFSConstants.TX_ID, txid);
            returnObjectBuilder.add(SKFSConstants.TX_PAYLOAD, txpayload);

            String rpId = generateRpId(fidoPolicy.getRpOptions());
            if (rpId != null) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_RPID, rpId);
            }
            String userVerificationPref = generateUserVerification(fidoPolicy, options);
            if (userVerificationPref != null) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_UV, userVerificationPref);
            }

            JsonObject extensionsJson = generateExtensions(fidoPolicy.getExtensionsOptions(), extensions);
            if (!extensionsJson.isEmpty()) {
                returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_EXTENSIONS, extensionsJson);
            }

            if (!sendfakekeyhandles) {
                //Place challenge in map.
                for (FidoKeys fk : fks) {
                    String KHHash = SKFSCommon.getDigest(fk.getKeyhandle(), "SHA-256");
                    UserSessionInfo session = new UserSessionInfo(username,
                            challenge, fk.getAppid(), SKFSConstants.FIDO_USERSESSION_AUTHORIZE, fk.getPublickey(), "");
                    session.setFkid(fk.getFidoKeysPK().getFkid());
                    session.setSkid(fk.getFidoKeysPK().getSid());
                    session.setSid(applianceCommon.getServerId().shortValue());
                    session.setuserVerificationReq(userVerificationPref);
                    session.setPolicyMapKey(fidoPolicy.getPolicyMapKey());
                    session.setInitnonce(nonce);
                    session.setTxpayload(txpayload);
                    session.setTxid(txid);
                    session.setTxtimestamp(txtime);
                    skceMaps.getMapObj().put(SKFSConstants.MAP_USER_SESSION_INFO, KHHash, session);

                    //replicate map to other server
                    session.setMapkey(KHHash);
                    if (applianceCommon.replicate()) {
                        replObj.execute(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, applianceCommon.getServerId().toString(), session);
                    }
                }
            }

            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0021"), " username=" + username);

            String response = Json.createObjectBuilder()
                    .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, returnObjectBuilder.build())
                    .build().toString();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "FIDO 2.0 Response : " + response);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0035", "");
            return response;
        } catch (SKFEException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", ex.getLocalizedMessage());
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0009") + ex.getLocalizedMessage()));
        }
    }
    
    
    private String generateChallenge(AlgorithmsPolicyOptions cryptoOp) {
        int numBytes = SKFSConstants.DEFAULT_NUM_CHALLENGE_BYTES;
        return U2FUtility.getRandom(numBytes);
    }
    
//    private String getUserId(Long did, String username) throws SKFEException {
//        FidoKeys fk = getkeybean.getNewestKeyByUsernameStatus(did, username, "Active");
//        if (fk == null) {
//            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0007", "");
//            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0007"));
//        } else {
//            return fk.getUserid();
//        }
//    }

    private String generateRpId(RpPolicyOptions rpOp){
        return rpOp.getId();
    }


    private JsonArray generateAllowCredentialsList(AuthenticationPolicyOptions authOp, Collection<FidoKeys> fks) throws SKFEException {
        JsonArrayBuilder allowCredentialsBuilder = Json.createArrayBuilder();

        for (FidoKeys fk : fks) {
            if (authOp.getAllowCredentials() != null &&
                    authOp.getAllowCredentials().equalsIgnoreCase(SKFSConstants.POLICY_CONST_ENABLED)) {
                if (fk.getFidoProtocol().equals(SKFSConstants.FIDO_PROTOCOL_VERSION_2_0)) {
                    JsonObjectBuilder excludedCredential = Json.createObjectBuilder()
                            .add(SKFSConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                            .add(SKFSConstants.FIDO2_ATTR_ID, decryptKH(fk.getKeyhandle()))
                            .add(SKFSConstants.FIDO2_ATTR_ALG, RegistrationSettings
                                    .parse(fk.getRegistrationSettings(), fk.getRegistrationSettingsVersion()).getAlg());

                    //TODO transports is just an hint so we are not adding it right now. Fix this
//                    if (fk.getTransports() != null) {
//                        excludedCredential.add(SKFSConstants.FIDO2_ATTR_TRANSPORTS, SKFSCommon.getTransportJson(fk.getTransports().intValue()));
//                    }

                    allowCredentialsBuilder.add(excludedCredential);
                }
                else{}   //TODO use APPID extension if false
            }
        }
        return allowCredentialsBuilder.build();
    }

    private JsonArray generatefakeAllowCredentialsList() throws SKFEException {
        JsonArrayBuilder allowCredentialsBuilder = Json.createArrayBuilder();

        JsonObjectBuilder excludedCredential = Json.createObjectBuilder()
                .add(SKFSConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                .add(SKFSConstants.FIDO2_ATTR_ID, Base64.getUrlEncoder().withoutPadding().encodeToString(generateRandomAlphanumericString(162).getBytes()))
                .add(SKFSConstants.FIDO2_ATTR_ALG, -7);
        allowCredentialsBuilder.add(excludedCredential);

        return allowCredentialsBuilder.build();
    }
    
    public String generateRandomAlphanumericString(int length) {
        int leftLimit = 48; // numeral '0'
        int rightLimit = 122; // letter 'z'
        SecureRandom random = new SecureRandom();

        String generatedString = random.ints(leftLimit, rightLimit + 1)
                .filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97))
                .limit(length)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();

        return generatedString;
    }

    private String generateUserVerification(FidoPolicyObject fidoPolicy, JsonObject options){
        String userVerificationResponse = null;
        String rpRequestedUserVerification = options.getString(SKFSConstants.FIDO2_ATTR_USERVERIFICATION, null);

        // Use RP requested options, assuming the policy allows.
        if (fidoPolicy.getSystemOptions().getUserVerification().contains(rpRequestedUserVerification)) {
            userVerificationResponse = rpRequestedUserVerification;
        } else if (rpRequestedUserVerification != null) {
            throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_ATTR_USERVERIFICATION);
        } else {
             //Specifying the lowest constraint for user verfication
            if(fidoPolicy.getSystemOptions().getUserVerification().size() < 3){
                if (fidoPolicy.getSystemOptions().getUserVerification().contains(SKFSConstants.POLICY_CONST_PREFERRED) && fidoPolicy.getSystemOptions().getUserVerification().contains(SKFSConstants.POLICY_CONST_REQUIRED)){
                    userVerificationResponse =   SKFSConstants.POLICY_CONST_PREFERRED;
                } else if (fidoPolicy.getSystemOptions().getUserVerification().size() == 1){
                    userVerificationResponse =   fidoPolicy.getSystemOptions().getUserVerification().get(0);
                } else if(fidoPolicy.getSystemOptions().getUserVerification().isEmpty()){
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", "User Verfication Options Missing from FIDO Policy");
                }
            }
        }

       
        return userVerificationResponse;
    }

    private JsonObject generateExtensions(DefinedExtensionsPolicyOptions extOp, JsonObject extensionsInput){
        JsonObjectBuilder extensionJsonBuilder = Json.createObjectBuilder();

//        for(Fido2Extension ext: extOp.getExtensions()){
//            if(ext instanceof Fido2AuthenticationExtension){
//                JsonValue extensionInput = (extensionsInput == null) ? null
//                        : extensionsInput.get(ext.getExtensionIdentifier());
//
//                Object extensionChallangeObject = ext.generateChallengeInfo(extensionInput);
//                if(extensionChallangeObject != null){
//                    if(extensionChallangeObject instanceof String){
//                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (String) extensionChallangeObject);
//                    }
//                    else if(extensionChallangeObject instanceof JsonObject){
//                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonObject) extensionChallangeObject);
//                    }
//                    else if (extensionChallangeObject instanceof JsonValue) {
//                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonValue) extensionChallangeObject);
//                    }
//                    else{
//                        throw new UnsupportedOperationException("Unimplemented Extension requested");
//                    }
//                }
//            }
//        }
        if(extOp.getUVM() != null){
            extensionJsonBuilder.add(SKFSConstants.POLICY_ATTR_EXTENSIONS_INPUT_UVM ,true);
        }
        

        return extensionJsonBuilder.build();
    }

    //TODO actually decrypt keyhandle if it is encrypted
    private String decryptKH(String keyhandle) {
        return keyhandle;
    }

}
