/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.core.U2FUtility;
import com.strongkey.skfs.fido.policyobjects.AuthenticationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.CryptographyPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.ExtensionsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido.policyobjects.RpPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2AuthenticationExtension;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2Extension;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.RegistrationSettings;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

/**
 *
 * @author mishimoto
 */
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
        String userId;
        try{
            userId = getUserId(did, username);
        } catch (SKFEException ex) {
            throw new IllegalArgumentException(skfsCommon.buildReturn(ex.getLocalizedMessage()));
        }
        
        //Gather useful information
        FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(did, username);
        if (fidoPolicy == null) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", "No policy found");
            throw new IllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0009") + "No policy found"));
        }
        String challenge = generateChallenge(fidoPolicy.getCryptographyOptions());
        
        //Create response object
        JsonObjectBuilder returnObjectBuilder = Json.createObjectBuilder();
        try {
            Collection<FidoKeys> fks = getkeybean.getByUsernameStatus(did, username, "Active");
            returnObjectBuilder.add(skfsConstants.FIDO2_PREAUTH_ATTR_CHALLENGE, challenge)
                    .add(skfsConstants.FIDO2_PREAUTH_ATTR_ALLOWCREDENTIALS,
                            generateAllowCredentialsList(fidoPolicy.getAuthenticationOptions(), fks));
        
            //Add optionals
            if (fidoPolicy.getTimeout() != null) {
                returnObjectBuilder.add(skfsConstants.FIDO2_PREAUTH_ATTR_TIMEOUT, fidoPolicy.getTimeout());
            }
            String rpId = generateRpId(fidoPolicy.getRpOptions());
            if (rpId != null) {
                returnObjectBuilder.add(skfsConstants.FIDO2_PREAUTH_ATTR_RPID, rpId);
            }
            String userVerificationPref = generateUserVerification(fidoPolicy.getAuthenticationOptions(), options);
            if (userVerificationPref != null) {
                returnObjectBuilder.add(skfsConstants.FIDO2_PREAUTH_ATTR_UV, userVerificationPref);
            }

            JsonObject extensionsJson = generateExtensions(fidoPolicy.getExtensionsOptions(), extensions);
            if(!extensionsJson.isEmpty()){
                returnObjectBuilder.add(skfsConstants.FIDO2_PREAUTH_ATTR_EXTENSIONS, extensionsJson);
            }

            //Place challenge in map.
            for(FidoKeys fk : fks){
                String KHHash = skfsCommon.getDigest(fk.getKeyhandle(), "SHA-256");
                UserSessionInfo session = new UserSessionInfo(username,
                        challenge, fk.getAppid(), skfsConstants.FIDO_USERSESSION_AUTH, fk.getPublickey(), "");
                session.setFkid(fk.getFidoKeysPK().getFkid());
                session.setSkid(fk.getFidoKeysPK().getSid());
                session.setSid(applianceCommon.getServerId().shortValue());
                session.setuserVerificationReq(userVerificationPref);
                session.setPolicyMapKey(fidoPolicy.getPolicyMapKey());
                skceMaps.getMapObj().put(skfsConstants.MAP_USER_SESSION_INFO, KHHash, session);

                //replicate map to other server
                session.setMapkey(KHHash);
                if (applianceCommon.replicate()) {
                    replObj.execute(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, applianceCommon.getServerId().toString(), session);
                }
            }

            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, skfsCommon.getMessageProperty("FIDO-MSG-0021"), " username=" + username);

            String response = Json.createObjectBuilder()
                    .add(skfsConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, returnObjectBuilder.build())
                    .build().toString();
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "FIDO 2.0 Response : " + response);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0035", "");
            return response;
        } catch (SKFEException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", ex.getLocalizedMessage());
            throw new IllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0009") + ex.getLocalizedMessage()));
        }
    }
    
    private String generateChallenge(CryptographyPolicyOptions cryptoOp) {
        Integer challengeLength = cryptoOp.getChallengeLength();
        int numBytes = (challengeLength == null) ? skfsConstants.DEFAULT_NUM_CHALLENGE_BYTES : challengeLength;
        return U2FUtility.getRandom(numBytes);
    }
    
    private String getUserId(Long did, String username) throws SKFEException {
        FidoKeys fk = getkeybean.getNewestKeyByUsernameStatus(did, username, "Active");
        if (fk == null) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0007", "");
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-ERR-0007"));
        } else {
            return fk.getUserid();
        }
    }
    
    private String generateRpId(RpPolicyOptions rpOp){
        return rpOp.getId();
    }
    
    
    private JsonArray generateAllowCredentialsList(AuthenticationPolicyOptions authOp, Collection<FidoKeys> fks) throws SKFEException {
        JsonArrayBuilder allowCredentialsBuilder = Json.createArrayBuilder();
        
        for (FidoKeys fk : fks) {
            if (authOp.getAllowCredentials() != null && 
                    authOp.getAllowCredentials().equalsIgnoreCase(skfsConstants.POLICY_CONST_ENABLED)) {
                if (fk.getFidoProtocol().equals(skfsConstants.FIDO_PROTOCOL_VERSION_2_0)) {
                    JsonObjectBuilder excludedCredential = Json.createObjectBuilder()
                            .add(skfsConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                            .add(skfsConstants.FIDO2_ATTR_ID, decryptKH(fk.getKeyhandle()))
                            .add(skfsConstants.FIDO2_ATTR_ALG, RegistrationSettings
                                    .parse(fk.getRegistrationSettings(), fk.getRegistrationSettingsVersion()).getAlg());

                    //TODO transports is just an hint so we are not adding it right now. Fix this
//                    if (fk.getTransports() != null) {
//                        excludedCredential.add(skfsConstants.FIDO2_ATTR_TRANSPORTS, skfsCommon.getTransportJson(fk.getTransports().intValue()));
//                    }

                    allowCredentialsBuilder.add(excludedCredential);
                }
                else{}   //TODO use APPID extension if false
            }
        }
        return allowCredentialsBuilder.build();
    }
    
    private String generateUserVerification(AuthenticationPolicyOptions authOp, JsonObject options){
        String userVerificationResponse = null;
        String rpRequestedUserVerification = options.getString(skfsConstants.FIDO2_ATTR_USERVERIFICATION, null);

        // Use RP requested options, assuming the policy allows.
        if (authOp.getUserVerification().contains(rpRequestedUserVerification)) {
            userVerificationResponse = rpRequestedUserVerification;
        } else if (rpRequestedUserVerification != null) {
            throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_ATTR_USERVERIFICATION);
        }

        // If an option is unset, verify the policy allows for the default behavior.
        if (userVerificationResponse == null && !authOp.getUserVerification().contains(skfsConstants.POLICY_CONST_PREFERRED)) {
            throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_ATTR_USERVERIFICATION + "Missing");
        }
        return userVerificationResponse;
    }
    
    private JsonObject generateExtensions(ExtensionsPolicyOptions extOp, JsonObject extensionsInput){
        JsonObjectBuilder extensionJsonBuilder = Json.createObjectBuilder();
        
        for(Fido2Extension ext: extOp.getExtensions()){
            if(ext instanceof Fido2AuthenticationExtension){
                JsonValue extensionInput = (extensionsInput == null) ? null 
                        : extensionsInput.get(ext.getExtensionIdentifier());
                
                Object extensionChallangeObject = ext.generateChallengeInfo(extensionInput);
                if(extensionChallangeObject != null){
                    if(extensionChallangeObject instanceof String){
                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (String) extensionChallangeObject);
                    }
                    else if(extensionChallangeObject instanceof JsonObject){
                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonObject) extensionChallangeObject);
                    }
                    else if (extensionChallangeObject instanceof JsonValue) {
                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonValue) extensionChallangeObject);
                    }
                    else{
                        throw new UnsupportedOperationException("Unimplemented Extension requested");
                    }
                }
            }
        }
        
        return extensionJsonBuilder.build();
    }
    
    //TODO actually decrypt keyhandle if it is encrypted
    private String decryptKH(String keyhandle) {
        return keyhandle;
    }
}
