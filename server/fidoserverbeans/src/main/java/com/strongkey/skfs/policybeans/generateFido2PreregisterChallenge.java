/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido.policyobjects.AuthenticatorSelection;
import com.strongkey.skfs.fido.policyobjects.CryptographyPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido.policyobjects.RegistrationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.RpPolicyOptions;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.core.U2FUtility;
import com.strongkey.skfs.fido.policyobjects.ExtensionsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2Extension;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2RegistrationExtension;
import com.strongkey.skfs.pojos.RegistrationSettings;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;


@Stateless
public class generateFido2PreregisterChallenge implements generateFido2PreregisterChallengeLocal {

    private final String classname = generateFido2PreregisterChallenge.class.getName();
    
    @EJB
    replicateSKFEObjectBeanLocal replObj;
    @EJB
    getFidoKeysLocal getkeybean;
    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;
    
    @Override
    public String execute(Long did, String username, String displayName, JsonObject options, JsonObject extensions) {   //TODO refactor method into smaller pieces
        //  fetch the username
        if (username == null || username.isEmpty()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
            throw new IllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " username"));
        }
        if (displayName == null || displayName.isEmpty()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
            throw new IllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " username"));
        }
        
        //Gather useful information
        FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(did, username);
        if(fidoPolicy == null){
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", "No policy found");
            throw new IllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0002") + "No policy found"));
        }
        RegistrationPolicyOptions regOp = fidoPolicy.getRegistrationOptions();
        String userId = getUserId(did, username, regOp.getUseridLength());
        String challenge = generateChallenge(fidoPolicy.getCryptographyOptions());
        String origin = applianceMaps.getDomain(Long.parseLong(String.valueOf(did))).getSkfeAppid();    //TODO verify this origin (https://demo.strongkey.com) == appid (https://demo.strongkey.com/app.json from config file) in all our logic. 
                                                                                                        //Issue: webauthn specifies origin == rpid (demo.strongkey.com, https://demo.strongkey.com:8181, https://demo.strongkey.com are all valid)
                                                                                                        //However rpid is optional and is stored in the policy file, defaulting to the rp's "effective domain"
                                                                                                        //if not supplied.
                                                                                                        //Since we cannot guess the effective domain, should the logic be use rpid if supplied, otherwise use appid(?).
        String rpname = fidoPolicy.getRpOptions().getName();
        String nonceHash = null;
        
        //Create response object
        JsonObjectBuilder returnObjectBuilder = Json.createObjectBuilder();
        try{
            nonceHash = skfsCommon.getDigest(challenge, "SHA-256");
            returnObjectBuilder
                    .add(skfsConstants.FIDO2_PREREG_ATTR_RP, generatePublicKeyCredentialRpEntity(fidoPolicy.getRpOptions()))
                    .add(skfsConstants.FIDO2_PREREG_ATTR_USER, generatePublicKeyCredentialUserEntity(regOp,
                            did, username, userId, displayName, null)) //TODO handle user icon if it exists
                    .add(skfsConstants.FIDO2_PREREG_ATTR_CHALLENGE, challenge)
                    .add(skfsConstants.FIDO2_PREREG_ATTR_KEYPARAMS, generatePublicKeyCredentialParametersArray(fidoPolicy.getCryptographyOptions()))
                    .add(skfsConstants.FIDO2_PREREG_ATTR_EXCLUDECRED, generateExcludeCredentialsList(regOp, did, username));
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException | SKFEException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0003", ex.getLocalizedMessage());
            throw new IllegalArgumentException(skfsCommon.buildReturn(skfsCommon.getMessageProperty("FIDO-ERR-0003") + ex.getLocalizedMessage()));
        }
        
        if(fidoPolicy.getTimeout() != null){
            returnObjectBuilder.add(skfsConstants.FIDO2_PREREG_ATTR_TIMEOUT, fidoPolicy.getTimeout());
        }
            JsonObject authSelect = generateAuthenticatorSelection(regOp, options);
            if(authSelect != null){
                returnObjectBuilder.add(skfsConstants.FIDO2_PREREG_ATTR_AUTHENTICATORSELECT, authSelect);
            }
            String attestPref = generateAttestationConveyancePreference(regOp, options);
            if(attestPref != null){
                returnObjectBuilder.add(skfsConstants.FIDO2_PREREG_ATTR_ATTESTATION, attestPref);
            }
        
        JsonObject extensionsJson = generateExtensions(fidoPolicy.getExtensionsOptions(), extensions);
        if (!extensionsJson.isEmpty()) {
            returnObjectBuilder.add(skfsConstants.FIDO2_PREAUTH_ATTR_EXTENSIONS, extensionsJson);
        }
        
        JsonObject returnObject = returnObjectBuilder.build();
        
        //Store registration challenge info (TODO change UserSessionInfo to builder pattern)
        String userVerificationReq = (authSelect != null) ? authSelect.getString(skfsConstants.FIDO2_ATTR_USERVERIFICATION, null) : null;
        UserSessionInfo session = new UserSessionInfo(username, challenge, 
            origin, skfsConstants.FIDO_USERSESSION_REG, "", "");
        session.setSid(applianceCommon.getServerId().shortValue());
        session.setUserId(userId);
        session.setDisplayName(displayName);
        session.setRpName(rpname);
        session.setSkid(applianceCommon.getServerId().shortValue());
        session.setuserVerificationReq(userVerificationReq);
        session.setAttestationPreferance(attestPref);
        session.setPolicyMapKey(fidoPolicy.getPolicyMapKey());
        skceMaps.getMapObj().put(skfsConstants.MAP_USER_SESSION_INFO, nonceHash, session);
        session.setMapkey(nonceHash);
        
        //Replicate stored registration info
        try {
            if (applianceCommon.replicate()) {
                replObj.execute(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, applianceCommon.getServerId().toString(), session);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }
        
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, skfsCommon.getMessageProperty("FIDO-MSG-0021"), " username=" + username);
        
        String response = Json.createObjectBuilder()
                .add(skfsConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, returnObject)
                .build().toString();
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "FIDO 2.0 Response : " + response);
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0035", "");
        return response;
    }
    
    //TODO move private functions to be public functions of PolicyOption Objects
    //Currently blocked by the need to move more objects to common.
    private JsonObject generatePublicKeyCredentialRpEntity(RpPolicyOptions rpOp){
        JsonObjectBuilder rpBuilder = Json.createObjectBuilder()
                .add(skfsConstants.FIDO2_ATTR_NAME, rpOp.getName());
        if(rpOp.getId() != null)
            rpBuilder.add(skfsConstants.FIDO2_ATTR_ID, rpOp.getId());
        if(rpOp.getIcon()!= null)
            rpBuilder.add(skfsConstants.FIDO2_ATTR_ICON, rpOp.getIcon());
        
        return rpBuilder.build();
    }
    
    private JsonObject generatePublicKeyCredentialUserEntity(RegistrationPolicyOptions regOp,
            Long did, String username, String userId, String displayName, String userIcon) throws SKFEException {
        JsonObjectBuilder userBuilder = Json.createObjectBuilder()
                .add(skfsConstants.FIDO2_ATTR_NAME, username)
                .add(skfsConstants.FIDO2_ATTR_ID, userId);
        
        if(regOp.getIcon() != null && regOp.getIcon().equals(skfsConstants.POLICY_CONST_ENABLED) && userIcon != null){
            userBuilder.add(skfsConstants.FIDO2_ATTR_ICON, userIcon);
        }
        
        if((regOp.getDisplayName().equals(skfsConstants.POLICY_CONST_REQUIRED)
                || regOp.getDisplayName().equals(skfsConstants.POLICY_CONST_PREFERRED))
                && displayName != null){
            userBuilder.add(skfsConstants.FIDO2_ATTR_DISPLAYNAME, displayName);
        }
        else if((regOp.getDisplayName().equals(skfsConstants.POLICY_CONST_PREFERRED)
                && displayName == null) || regOp.getDisplayName().equals(skfsConstants.POLICY_CONST_NONE)){
            userBuilder.add(skfsConstants.FIDO2_ATTR_DISPLAYNAME, username);
        }
        else{
            throw new SKFEException(skfsCommon.getMessageProperty("FIDO-MSG-0053") + "displayName");
        }
        
        return userBuilder.build();
    }
    
    private String getUserId(Long did, String username, Integer useridLength){
        FidoKeys fk = null;
        try {
            fk = getkeybean.getNewestKeyByUsernameStatus(did, username, "Active");
        } catch (SKFEException ex) {
            Logger.getLogger(generateFido2PreregisterChallenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(fk == null){
            return (useridLength == null) ? U2FUtility.getRandom(skfsConstants.DEFAULT_NUM_USERID_BYTES):
                    U2FUtility.getRandom(useridLength);
        }
        else{
            return fk.getUserid();
        }
    }
    
    private String generateChallenge(CryptographyPolicyOptions cryptoOp){
        Integer challengeLength = cryptoOp.getChallengeLength();
        int numBytes = (challengeLength == null)? skfsConstants.DEFAULT_NUM_CHALLENGE_BYTES : challengeLength;
        return U2FUtility.getRandom(numBytes); 
    }
    
    //TODO verify order is maintained
    private JsonArray generatePublicKeyCredentialParametersArray(CryptographyPolicyOptions cryptoOp){
        JsonArrayBuilder publicKeyBuilder = Json.createArrayBuilder();
        for(String alg :cryptoOp.getAllowedECSignatures()){
            JsonObject publicKeyCredential = Json.createObjectBuilder()
                    .add(skfsConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                    .add(skfsConstants.FIDO2_ATTR_ALG, skfsCommon.getIANACOSEAlgFromPolicyAlg(alg))
                    .build();
            publicKeyBuilder.add(publicKeyCredential);
        }
        //TODO fix this hardcoded assuption that EC is preferred over RSA
        for (String alg : cryptoOp.getAllowedRSASignatures()) {
            JsonObject publicKeyCredential = Json.createObjectBuilder()
                    .add(skfsConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                    .add(skfsConstants.FIDO2_ATTR_ALG, skfsCommon.getIANACOSEAlgFromPolicyAlg(alg))
                    .build();
            publicKeyBuilder.add(publicKeyCredential);
        }
        return publicKeyBuilder.build();
    }
    
    private JsonArray generateExcludeCredentialsList(RegistrationPolicyOptions regOp,
            Long did, String username) throws SKFEException{
        JsonArrayBuilder excludeCredentialsBuilder = Json.createArrayBuilder();
        
        if(regOp.getExcludeCredentials().equalsIgnoreCase(skfsConstants.POLICY_CONST_ENABLED)){
            Collection<FidoKeys> fks = getkeybean.getByUsernameStatus(did, username, "Active");
            for(FidoKeys fk: fks){
                if(fk.getFidoProtocol().equals(skfsConstants.FIDO_PROTOCOL_VERSION_2_0)){
                    JsonObjectBuilder excludedCredential = Json.createObjectBuilder()
                            .add(skfsConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                            .add(skfsConstants.FIDO2_ATTR_ID, decryptKH(fk.getKeyhandle()))
                            .add(skfsConstants.FIDO2_ATTR_ALG, RegistrationSettings
                                    .parse(fk.getRegistrationSettings(), fk.getRegistrationSettingsVersion()).getAlg());

                    //TODO transports are a hint that not all browsers support atm.
//                    if(fk.getTransports() != null){
//                        excludedCredential.add(skfsConstants.FIDO2_ATTR_TRANSPORTS, skfsCommon.getTransportJson(fk.getTransports().intValue()));
//                    }

                    excludeCredentialsBuilder.add(excludedCredential);
                }
            }
        }
        return excludeCredentialsBuilder.build();
    }
    
    private JsonObject generateAuthenticatorSelection(RegistrationPolicyOptions regOp, JsonObject options){
        JsonObject authselectResponse;
        AuthenticatorSelection authselect = regOp.getAuthenticatorSelection();
        JsonObject rpRequestedAuthSelect = options.getJsonObject(skfsConstants.FIDO2_PREREG_ATTR_AUTHENTICATORSELECT);
        JsonObjectBuilder authselectBuilder = Json.createObjectBuilder();
        // Use RP requested options, assuming the policy allows.
        if(rpRequestedAuthSelect != null){
            String rpRequestedAttachment = rpRequestedAuthSelect.getString(skfsConstants.FIDO2_ATTR_ATTACHMENT, null);
            Boolean rpRequestedRequireResidentKey = skfsCommon.handleNonExistantJsonBoolean(rpRequestedAuthSelect, skfsConstants.FIDO2_ATTR_RESIDENTKEY);
            String rpRequestedUserVerification = rpRequestedAuthSelect.getString(skfsConstants.FIDO2_ATTR_USERVERIFICATION, null);
            
            if(authselect.getAuthenticatorAttachment().contains(rpRequestedAttachment)){
                authselectBuilder.add(skfsConstants.FIDO2_ATTR_ATTACHMENT, rpRequestedAttachment);
            }
            else if(rpRequestedAttachment != null){
                throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_ATTR_ATTACHMENT);
            }
            
            if(authselect.getRequireResidentKey().contains(rpRequestedRequireResidentKey)){
                authselectBuilder.add(skfsConstants.FIDO2_ATTR_RESIDENTKEY, rpRequestedRequireResidentKey);
            }
            else if (rpRequestedRequireResidentKey != null) {
                throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_ATTR_RESIDENTKEY);
            }

            if(authselect.getUserVerification().contains(rpRequestedUserVerification)){
                authselectBuilder.add(skfsConstants.FIDO2_ATTR_USERVERIFICATION, rpRequestedUserVerification);
            }
            else if (rpRequestedUserVerification != null) {
                throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_ATTR_USERVERIFICATION);
            }
        }
        authselectResponse = authselectBuilder.build();
        // If an option is unset, verify the policy allows for the default behavior.
        if(!authselectResponse.isEmpty()){
            if(authselectResponse.getString(skfsConstants.FIDO2_ATTR_RESIDENTKEY, null) == null 
                    && !authselect.getRequireResidentKey().contains(false)){
                throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_ATTR_RESIDENTKEY + "Missing");
            }
            if(authselectResponse.getString(skfsConstants.FIDO2_ATTR_USERVERIFICATION, null) == null
                    && !authselect.getUserVerification().contains(skfsConstants.POLICY_CONST_PREFERRED)){
                throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_ATTR_USERVERIFICATION + "Missing");
            }
            return authselectResponse;
        }
        return null;
    }
    
    private String generateAttestationConveyancePreference(RegistrationPolicyOptions regOp, JsonObject options){
        String attestionResponse = null;
        String rpRequestedAttestation = options.getString(skfsConstants.FIDO2_PREREG_ATTR_ATTESTATION, null);
        
        // Use RP requested options, assuming the policy allows.
        if(regOp.getAttestation().contains(rpRequestedAttestation)){
            attestionResponse = rpRequestedAttestation;
        }
        else if(rpRequestedAttestation != null){
            throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_PREREG_ATTR_ATTESTATION);
        }
        
        // If an option is unset, verify the policy allows for the default behavior.
        if(attestionResponse == null && !regOp.getAttestation().contains(skfsConstants.FIDO2_CONST_ATTESTATION_NONE)){
            throw new IllegalArgumentException("Policy violation: " + skfsConstants.FIDO2_PREREG_ATTR_ATTESTATION + "Missing");
        }
        return attestionResponse;
    }
    
    private JsonObject generateExtensions(ExtensionsPolicyOptions extOp, JsonObject extensionsInput){
        JsonObjectBuilder extensionJsonBuilder = Json.createObjectBuilder();

        for (Fido2Extension ext : extOp.getExtensions()) {
            if (ext instanceof Fido2RegistrationExtension) {
                JsonValue extensionInput = (extensionsInput == null) ? null
                        : extensionsInput.get(ext.getExtensionIdentifier());

                Object extensionChallangeObject = ext.generateChallengeInfo(extensionInput);
                if (extensionChallangeObject != null) {
                    if (extensionChallangeObject instanceof String) {
                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (String) extensionChallangeObject);
                    } 
                    else if (extensionChallangeObject instanceof JsonObject) {
                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonObject) extensionChallangeObject);
                    }
                    else if (extensionChallangeObject instanceof JsonValue) {
                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonValue) extensionChallangeObject);
                    }
                    else {
                        throw new UnsupportedOperationException("Unimplemented Extension requested");
                    }
                }
            }
        }

        return extensionJsonBuilder.build();
    }
    
    //TODO actually decrypt keyhandle if it is encrypted
    private String decryptKH(String keyhandle){
        return keyhandle;
    }
}
