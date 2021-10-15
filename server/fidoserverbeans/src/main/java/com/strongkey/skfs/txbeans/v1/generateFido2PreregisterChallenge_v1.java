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
 *
 *
 */
package com.strongkey.skfs.txbeans.v1;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.core.U2FUtility;
import com.strongkey.skfs.fido.policyobjects.AlgorithmsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.AttestationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.DefinedExtensionsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido.policyobjects.RegistrationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.RpPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2Extension;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2RegistrationExtension;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.pojos.RegistrationSettings;
import com.strongkey.skfs.policybeans.generateFido2PreregisterChallenge;
import com.strongkey.skfs.policybeans.getCachedFidoPolicyMDSLocal;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.List;
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
public class generateFido2PreregisterChallenge_v1 implements generateFido2PreregisterChallengeLocal_v1 {

    private final String classname = generateFido2PreregisterChallenge.class.getName();

    @EJB
    replicateSKFEObjectBeanLocal replObj;
    @EJB
    getFidoKeysLocal getkeybean;
    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;

    @Override
    public String execute(Long did, String payload) {   //TODO refactor method into smaller pieces
        //  fetch the username
        List<FidoKeys> fks = null ;
        String username = (String) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_USERNAME, "String");
        if (username == null || username.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " username");
        }
        String displayName = (String) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_DISPLAY_NAME, "String");
        if (displayName == null || displayName.isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " username");
        }

        // fetch options inputs if they exist (TODO refactor when options are no longer in payload)
        JsonObject authSelectCriteria = (JsonObject) applianceCommon.getJsonValue(payload,
                            SKFSConstants.FIDO2_PREREG_ATTR_AUTHENTICATORSELECT, "JsonObject");
        String attestationPref = (String) applianceCommon.getJsonValue(payload,
                            SKFSConstants.FIDO2_PREREG_ATTR_ATTESTATION, "String");
        JsonObjectBuilder optionsBuilder = Json.createObjectBuilder();
        if(authSelectCriteria != null){
            optionsBuilder.add(SKFSConstants.FIDO2_PREREG_ATTR_AUTHENTICATORSELECT, authSelectCriteria);
        }
        if(attestationPref != null){
            optionsBuilder.add(SKFSConstants.FIDO2_PREREG_ATTR_ATTESTATION, attestationPref);
        }

        JsonObject options = optionsBuilder.build();

        // fetch extension inputs if they exist
        JsonObject extensions = (JsonObject) applianceCommon.getJsonValue(payload,
                SKFSConstants.JSON_KEY_SERVLET_INPUT_EXTENSIONS, "JsonObject");

        FidoKeys fk = null;
        try {
            fks = getkeybean.getKeysByUsernameStatus(did, username, "Active");
        } catch (SKFEException ex) {
            Logger.getLogger(generateFido2PreregisterChallenge.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(fks.size() > 0) {
            fk = fks.get(0);
        }
        
        //Gather useful information
        FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(did, username, fk);
        if(fidoPolicy == null){
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", "No policy found");
            return SKFSCommon.buildPreRegisterResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0002") + "No policy found");
        }
        RegistrationPolicyOptions regOp = fidoPolicy.getRegistrationOptions();
        String userId ;
        if(fk == null) {
            userId = U2FUtility.getRandom(SKFSConstants.DEFAULT_NUM_USERID_BYTES);
        }else{
            userId = fk.getUserid();
        }
        String challenge = generateChallenge(fidoPolicy.getAlgorithmsOptions());
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
            nonceHash = SKFSCommon.getDigest(challenge, "SHA-256");
            returnObjectBuilder
                    .add(SKFSConstants.FIDO2_PREREG_ATTR_RP, generatePublicKeyCredentialRpEntity(fidoPolicy.getRpOptions()))
                    .add(SKFSConstants.FIDO2_PREREG_ATTR_USER, generatePublicKeyCredentialUserEntity(regOp,
                            did, username, userId, displayName, null)) //TODO handle user icon if it exists
                    .add(SKFSConstants.FIDO2_PREREG_ATTR_CHALLENGE, challenge)
                    .add(SKFSConstants.FIDO2_PREREG_ATTR_KEYPARAMS, generatePublicKeyCredentialParametersArray(fidoPolicy.getAlgorithmsOptions()))
                    .add(SKFSConstants.FIDO2_PREREG_ATTR_EXCLUDECRED, generateExcludeCredentialsList(regOp, did, username, fks));
        }
        catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException | SKFEException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0003", ex.getLocalizedMessage());
            return SKFSCommon.buildPreAuthResponse(null, "", SKFSCommon.getMessageProperty("FIDO-ERR-0003") + ex.getLocalizedMessage());
        }

        JsonObject authSelect = generateAuthenticatorSelection(fidoPolicy, options);
        if(authSelect != null){
            returnObjectBuilder.add(SKFSConstants.FIDO2_PREREG_ATTR_AUTHENTICATORSELECT, authSelect);
        }
        String attestPref = generateAttestationConveyancePreference(fidoPolicy.getAttestationOptions(), options);
        if(attestPref != null){
            returnObjectBuilder.add(SKFSConstants.FIDO2_PREREG_ATTR_ATTESTATION, attestPref);
        } else {
            System.out.println("attpref null so adding direct");
            attestPref = SKFSConstants.FIDO2_CONST_ATTESTATION_DIRECT;
            returnObjectBuilder.add(SKFSConstants.FIDO2_PREREG_ATTR_ATTESTATION, attestPref);
        }

        JsonObject extensionsJson = generateExtensions(fidoPolicy.getExtensionsOptions(), extensions);
        if (!extensionsJson.isEmpty()) {
            returnObjectBuilder.add(SKFSConstants.FIDO2_PREAUTH_ATTR_EXTENSIONS, extensionsJson);
        }

        JsonObject returnObject = returnObjectBuilder.build();

        //Store registration challenge info (TODO change UserSessionInfo to builder pattern)
        String userVerificationReq = (authSelect != null) ? authSelect.getString(SKFSConstants.FIDO2_ATTR_USERVERIFICATION, null) : null;
        UserSessionInfo session = new UserSessionInfo(username, challenge,
            origin, SKFSConstants.FIDO_USERSESSION_REG, "", "");
        session.setSid(applianceCommon.getServerId().shortValue());
        session.setUserId(userId);
        session.setDisplayName(displayName);
        session.setRpName(rpname);
        session.setSkid(applianceCommon.getServerId().shortValue());
        session.setuserVerificationReq(userVerificationReq);
        session.setAttestationPreferance(attestPref);
        session.setPolicyMapKey(fidoPolicy.getPolicyMapKey());
        skceMaps.getMapObj().put(SKFSConstants.MAP_USER_SESSION_INFO, nonceHash, session);
        session.setMapkey(nonceHash);

        //Replicate stored registration info
        try {
            if (applianceCommon.replicate()) {
                replObj.execute(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, applianceCommon.getServerId().toString(), session);
            }
        } catch (Exception e) {
            throw new RuntimeException(e.getLocalizedMessage());
        }

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, SKFSCommon.getMessageProperty("FIDO-MSG-0021"), " username=" + username);

        String response = SKFSCommon.buildPreRegisterResponse(returnObject, "", "");
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "FIDO 2.0 Response : " + response);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0035", "");
        return response;
    }

    //TODO move private functions to be public functions of PolicyOption Objects
    //Currently blocked by the need to move more objects to common.
    private JsonObject generatePublicKeyCredentialRpEntity(RpPolicyOptions rpOp){
        JsonObjectBuilder rpBuilder = Json.createObjectBuilder()
                .add(SKFSConstants.FIDO2_ATTR_NAME, rpOp.getName());
        if(rpOp.getId() != null)
            rpBuilder.add(SKFSConstants.FIDO2_ATTR_ID, rpOp.getId());
        if(rpOp.getIcon()!= null)
            rpBuilder.add(SKFSConstants.FIDO2_ATTR_ICON, rpOp.getIcon());

        return rpBuilder.build();
    }

    private JsonObject generatePublicKeyCredentialUserEntity(RegistrationPolicyOptions regOp,
            Long did, String username, String userId, String displayName, String userIcon) throws SKFEException {
        JsonObjectBuilder userBuilder = Json.createObjectBuilder()
                .add(SKFSConstants.FIDO2_ATTR_NAME, username)
                .add(SKFSConstants.FIDO2_ATTR_ID, userId);

        if((regOp.getDisplayName().equals(SKFSConstants.POLICY_CONST_REQUIRED)
                || regOp.getDisplayName().equals(SKFSConstants.POLICY_CONST_PREFERRED))
                && displayName != null){
            userBuilder.add(SKFSConstants.FIDO2_ATTR_DISPLAYNAME, displayName);
        }
        else if((regOp.getDisplayName().equals(SKFSConstants.POLICY_CONST_PREFERRED)
                && displayName == null) || regOp.getDisplayName().equals(SKFSConstants.POLICY_CONST_NONE)){
            userBuilder.add(SKFSConstants.FIDO2_ATTR_DISPLAYNAME, username);
        }
        else{
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-MSG-0053") + "displayName");
        }

        return userBuilder.build();
    }

//    private String getUserId(Long did, String username, Integer useridLength, FidoKeys fk){
////        FidoKeys fk = null;
////        try {
////            fk = getkeybean.getNewestKeyByUsernameStatus(did, username, "Active");
////        } catch (SKFEException ex) {
////            Logger.getLogger(generateFido2PreregisterChallenge.class.getName()).log(Level.SEVERE, null, ex);
////        }
//        if(fk == null){
//            return (useridLength == null) ? U2FUtility.getRandom(SKFSConstants.DEFAULT_NUM_USERID_BYTES):
//                    U2FUtility.getRandom(useridLength);
//        }
//        else{
//            return fk.getUserid();
//        }
//    }

    private String generateChallenge(AlgorithmsPolicyOptions cryptoOp){
        int numBytes = SKFSConstants.DEFAULT_NUM_CHALLENGE_BYTES;
        return U2FUtility.getRandom(numBytes);
    }

    //TODO verify order is maintained
    private JsonArray generatePublicKeyCredentialParametersArray(AlgorithmsPolicyOptions cryptoOp){
        JsonArrayBuilder publicKeyBuilder = Json.createArrayBuilder();
        for(String alg :cryptoOp.getAllowedECSignatures()){
            JsonObject publicKeyCredential = Json.createObjectBuilder()
                    .add(SKFSConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                    .add(SKFSConstants.FIDO2_ATTR_ALG, SKFSCommon.getIANACOSEAlgFromPolicyAlg(alg))
                    .build();
            publicKeyBuilder.add(publicKeyCredential);
        }
        //TODO fix this hardcoded assuption that EC is preferred over RSA
        for (String alg : cryptoOp.getAllowedRSASignatures()) {
            JsonObject publicKeyCredential = Json.createObjectBuilder()
                    .add(SKFSConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                    .add(SKFSConstants.FIDO2_ATTR_ALG, SKFSCommon.getIANACOSEAlgFromPolicyAlg(alg))
                    .build();
            publicKeyBuilder.add(publicKeyCredential);
        }
        return publicKeyBuilder.build();
    }

    private JsonArray generateExcludeCredentialsList(RegistrationPolicyOptions regOp,
            Long did, String username, List<FidoKeys> fks) throws SKFEException{
        JsonArrayBuilder excludeCredentialsBuilder = Json.createArrayBuilder();

        if(regOp.getExcludeCredentials().equalsIgnoreCase(SKFSConstants.POLICY_CONST_ENABLED)){
//            Collection<FidoKeys> fks = getkeybean.getByUsernameStatus(did, username, "Active");
            for(FidoKeys fk: fks){
                if(fk.getFidoProtocol().equals(SKFSConstants.FIDO_PROTOCOL_VERSION_2_0)){
                    JsonObjectBuilder excludedCredential = Json.createObjectBuilder()
                            .add(SKFSConstants.FIDO2_ATTR_TYPE, "public-key") //TODO fix this hardcoded assumption
                            .add(SKFSConstants.FIDO2_ATTR_ID, decryptKH(fk.getKeyhandle()))
                            .add(SKFSConstants.FIDO2_ATTR_ALG, RegistrationSettings
                                    .parse(fk.getRegistrationSettings(), fk.getRegistrationSettingsVersion()).getAlg());

                    //TODO transports are a hint that not all browsers support atm.
//                    if(fk.getTransports() != null){
//                        excludedCredential.add(SKFSConstants.FIDO2_ATTR_TRANSPORTS, SKFSCommon.getTransportJson(fk.getTransports().intValue()));
//                    }

                    excludeCredentialsBuilder.add(excludedCredential);
                }
            }
        }
        return excludeCredentialsBuilder.build();
    }

    private JsonObject generateAuthenticatorSelection(FidoPolicyObject fidopolicy, JsonObject options){
        JsonObject authselectResponse;
        RegistrationPolicyOptions regOp = fidopolicy.getRegistrationOptions();
//        AttestationPolicyOptions attOp = fidopolicy.getAttestationOptions();
        JsonObject rpRequestedAuthSelect = options.getJsonObject(SKFSConstants.FIDO2_PREREG_ATTR_AUTHENTICATORSELECT);
        JsonObjectBuilder authselectBuilder = Json.createObjectBuilder();
        // Use RP requested options, assuming the policy allows.
        if(rpRequestedAuthSelect != null){
            String rpRequestedAttachment = rpRequestedAuthSelect.getString(SKFSConstants.FIDO2_ATTR_ATTACHMENT, null);
            Boolean rpRequestedRequireResidentKey = SKFSCommon.handleNonExistantJsonBoolean(rpRequestedAuthSelect, SKFSConstants.FIDO2_ATTR_RESIDENTKEY);
            String rpRequestedUserVerification = rpRequestedAuthSelect.getString(SKFSConstants.FIDO2_ATTR_USERVERIFICATION, null);

            if(regOp.getAuthenticatorAttachment().contains(rpRequestedAttachment)){
                authselectBuilder.add(SKFSConstants.FIDO2_ATTR_ATTACHMENT, rpRequestedAttachment);
            }
            else if(rpRequestedAttachment != null){
                throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_ATTR_ATTACHMENT);
            }

            if(regOp.getRequireResidentKey().contains(Boolean.toString(rpRequestedRequireResidentKey))){
                authselectBuilder.add(SKFSConstants.FIDO2_ATTR_RESIDENTKEY, rpRequestedRequireResidentKey);
            }
            else if (rpRequestedRequireResidentKey != null) {
                throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_ATTR_RESIDENTKEY);
            }

            if(fidopolicy.getSystemOptions().getUserVerification().contains(rpRequestedUserVerification)){
                authselectBuilder.add(SKFSConstants.FIDO2_ATTR_USERVERIFICATION, rpRequestedUserVerification);
            }
            else if (rpRequestedUserVerification != null) {
                throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_ATTR_USERVERIFICATION);
            }
        }
        authselectResponse = authselectBuilder.build();
        // If an option is unset, verify the policy allows for the default behavior.
        if(!authselectResponse.isEmpty()){
            if(authselectResponse.getString(SKFSConstants.FIDO2_ATTR_RESIDENTKEY, null) == null
                    && !regOp.getRequireResidentKey().contains(Boolean.toString(false))){
                throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_ATTR_RESIDENTKEY + "Missing");
            }
            if(authselectResponse.getString(SKFSConstants.FIDO2_ATTR_USERVERIFICATION, null) == null
                    && !fidopolicy.getSystemOptions().getUserVerification().contains(SKFSConstants.POLICY_CONST_PREFERRED)){
                throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_ATTR_USERVERIFICATION + "Missing");
            }
            return authselectResponse;
        }
        return null;
    }

    private String generateAttestationConveyancePreference(AttestationPolicyOptions attOp, JsonObject options){
        String attestionResponse = null;
        String rpRequestedAttestation = options.getString(SKFSConstants.FIDO2_PREREG_ATTR_ATTESTATION, null);

        // Use RP requested options, assuming the policy allows.
        if(attOp.getAttestationConveyance().contains(rpRequestedAttestation)){
            attestionResponse = rpRequestedAttestation;
        }
        else if(rpRequestedAttestation != null){
            throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_PREREG_ATTR_ATTESTATION);
        }

        // If an option is unset, verify the policy allows for the default behavior.
        if(attestionResponse == null && !attOp.getAttestationConveyance().contains(SKFSConstants.FIDO2_CONST_ATTESTATION_NONE)){
            throw new SKIllegalArgumentException("Policy violation: " + SKFSConstants.FIDO2_PREREG_ATTR_ATTESTATION + "Missing");
        }
        return attestionResponse;
    }

    private JsonObject generateExtensions(DefinedExtensionsPolicyOptions extOp, JsonObject extensionsInput){
        JsonObjectBuilder extensionJsonBuilder = Json.createObjectBuilder();

//        for (Fido2Extension ext : extOp.getExtensions()) {
//            if (ext instanceof Fido2RegistrationExtension) {
//                JsonValue extensionInput = (extensionsInput == null) ? null
//                        : extensionsInput.get(ext.getExtensionIdentifier());
//
//                Object extensionChallangeObject = ext.generateChallengeInfo(extensionInput);
//                if (extensionChallangeObject != null) {
//                    if (extensionChallangeObject instanceof String) {
//                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (String) extensionChallangeObject);
//                    }
//                    else if (extensionChallangeObject instanceof JsonObject) {
//                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonObject) extensionChallangeObject);
//                    }
//                    else if (extensionChallangeObject instanceof JsonValue) {
//                        extensionJsonBuilder.add(ext.getExtensionIdentifier(), (JsonValue) extensionChallangeObject);
//                    }
//                    else {
//                        throw new UnsupportedOperationException("Unimplemented Extension requested");
//                    }
//                }
//            }
//        }

        return extensionJsonBuilder.build();
    }

    //TODO actually decrypt keyhandle if it is encrypted
    private String decryptKH(String keyhandle){
        return keyhandle;
    }
}
