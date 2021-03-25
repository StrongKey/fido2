
/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.skce.utilities.skceConstants;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

public class FidoPolicyObject {
    private final Long did;
    private final Long sid;
    private final Long pid;
    private final String version;
    private final Date startDate;
    private final Date endDate;
    private final AlgorithmsPolicyOptions algorithmsOptions;
    private final String requireCounter;
    private final RpPolicyOptions rpOptions;
    private final Boolean isUserSettingsRequired;
    private final Boolean isStoreSignaturesRequired;
    private final RegistrationPolicyOptions registrationOptions;
    private final AuthenticationPolicyOptions authenticationOptions;
    private final ExtensionsPolicyOptions extensionsOptions;
    private final TrustedAuthenticatorPolicyOptions authenticatorOptions;
    private final ArrayList<String> userVerification;
    private final Integer userPresenceTimeout;
    private final AttestationPolicyOptions attestation;
    private final JWTPolicyOptions jwt;
    private final Integer jwtRenewalWindow;
    private final Integer jwtKeyValidity; 

    private FidoPolicyObject(
            Long did,
            Long sid,
            Long pid,
            String version,
            Date startDate,
            Date endDate,
            AlgorithmsPolicyOptions algorithmsOptions,
            RpPolicyOptions rpOptions,
            String requireCounter,
            Boolean isUserSettingsRequired,
            Boolean isStoreSignaturesRequired,
            RegistrationPolicyOptions registrationOptions,
            AuthenticationPolicyOptions authenticationOptions,
            ExtensionsPolicyOptions extensionsOptions,
            TrustedAuthenticatorPolicyOptions authenticatorOptions,
            ArrayList<String> userVerification,
            Integer userPresenceTimeout,
            AttestationPolicyOptions attestation,
            JWTPolicyOptions jwt,
            Integer jwtRenewalWindow,
            Integer jwtKeyValidity){
        this.did = did;
        this.sid = sid;
        this.pid = pid;
        this.version = version;
        this.startDate = startDate;
        this.endDate = endDate;
        this.algorithmsOptions = algorithmsOptions;
        this.rpOptions = rpOptions;
        this.requireCounter = requireCounter;
        this.isUserSettingsRequired = isUserSettingsRequired;
        this.isStoreSignaturesRequired = isStoreSignaturesRequired;
        this.registrationOptions = registrationOptions;
        this.authenticationOptions = authenticationOptions;
        this.extensionsOptions = extensionsOptions;
        this.authenticatorOptions = authenticatorOptions;
        this.userVerification = userVerification;
        this.userPresenceTimeout = userPresenceTimeout;
        this.attestation = attestation;
        this.jwt = jwt;
        this.jwtRenewalWindow = jwtRenewalWindow;
        this.jwtKeyValidity = jwtKeyValidity;
    }

    public Long getDid() {
        return did;
    }

    public Long getSid() {
        return sid;
    }

    public Long getPid() {
        return pid;
    }

    public String getPolicyMapKey(){
        return sid+"-"+did+"-"+pid;
    }

    public String getVersion(){
        return version;
    }

    public Date getStartDate() {
        return new Date(startDate.getTime());
    }

    public Date getEndDate() {
        return new Date(endDate.getTime());
    }

    public AlgorithmsPolicyOptions getAlgorithmsOptions() {
        return algorithmsOptions;
    }

      public ArrayList<String> getAllowedAAGUIDs() {
        return authenticatorOptions.getAllowedAAGUIDs();
    }
    
    public RpPolicyOptions getRpOptions() {
        return rpOptions;
    }


    public String getCounterOptions() {
        return requireCounter;
    }

    public Boolean isUserSettingsRequired() {
        return isUserSettingsRequired;
    }

    public Boolean isStoreSignatures() {
        return isStoreSignaturesRequired;
    }

    public RegistrationPolicyOptions getRegistrationOptions() {
        return registrationOptions;
    }

    public AuthenticationPolicyOptions getAuthenticationOptions() {
        return authenticationOptions;
    }

    public ExtensionsPolicyOptions getExtensionsOptions() {
        return extensionsOptions;
    }
    
    public ArrayList<String> getUserVerification(){
        return userVerification;
    }
    public Integer getUserPresenceTimeout(){
        return userPresenceTimeout;
    }
    public AttestationPolicyOptions getAttestationOptions(){
        return attestation;
    }
    public String getCounterRequirement(){
        return requireCounter;
    }
    public JWTPolicyOptions getJWT(){
        return jwt;
    }
    public Integer getJwtRenewalWindow(){
        return jwtRenewalWindow;
    }
    public Integer getJwtKeyValidity(){
        return jwtKeyValidity;
    }

    public static FidoPolicyObject parse(String base64Policy, Long did, Long sid, Long pid) throws SKFEException {
        try {
            String policyString = new String(Base64.getUrlDecoder().decode(base64Policy), "UTF-8");
            JsonObject policyJson = applianceCommon.stringToJSON(policyString);

                               
                               

            JsonObject FidoPolicyJson = policyJson.getJsonObject(SKFSConstants.POLICY_SYSTEM_FIDO_POLICY);
            JsonObject systemJson = FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_SYSTEM);
            
            String startDateString = FidoPolicyJson.getString(SKFSConstants.POLICY_SYSTEM_START_DATE);
            Date startDate = new Date(Long.parseLong(startDateString)); 
            

            String endDateString = FidoPolicyJson.getString(SKFSConstants.POLICY_SYSTEM_END_DATE);
            Date endDate;
            if(endDateString.equals("")){
                endDate = null;
            } else {
                endDate = new Date(Long.parseLong(endDateString));
            } 
            
            String version = FidoPolicyJson.getString(SKFSConstants.POLICY_SYSTEM_VERSION);
            
            AlgorithmsPolicyOptions algorithms = AlgorithmsPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_ALGORITHMS));
           
            RpPolicyOptions rp = RpPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_RP));
         
            String requireCounter = systemJson.getString(SKFSConstants.POLICY_ATTR_COUNTER);
            
            ArrayList<String> userVerification = new ArrayList<>(systemJson.getJsonArray(SKFSConstants.POLICY_SYSTEM_USER_VERIFICATION).stream()
                    .map(x -> (JsonString) x)
                    .map(x -> x.getString())
                    .collect(Collectors.toList()));
            
            Integer userPresenceTimeout = systemJson.getInt(SKFSConstants.POLICY_SYSTEM_USER_PRESENCE_TIMEOUT);
            

            Boolean storeSignatures = SKFSCommon.handleNonExistantJsonBoolean(systemJson, SKFSConstants.POLICY_ATTR_STORESIGNATURES);

            RegistrationPolicyOptions registration = RegistrationPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_REGISTRATION));

            AuthenticationPolicyOptions authentication = AuthenticationPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_AUTHENTICATION));

            ExtensionsPolicyOptions extensions = ExtensionsPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_EXTENSIONS));
            
            TrustedAuthenticatorPolicyOptions aaguids = TrustedAuthenticatorPolicyOptions.parse(systemJson);
            
            AttestationPolicyOptions attestation = AttestationPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTESTATION));
            
            JWTPolicyOptions jwt = JWTPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_JWT));
            
            Integer jwtRenewalWindow =  systemJson.getInt(SKFSConstants.POLICY_JWT_RENEWAL);
            Integer jwtKeyValidity =  systemJson.getInt(SKFSConstants.POLICY_JWT_KEY_VALIDITY);
                            
                            
            
            return new FidoPolicyObject.FidoPolicyObjectBuilder(did, sid, pid, version, userVerification, userPresenceTimeout,
                    startDate, endDate, algorithms, rp, requireCounter, registration, authentication, aaguids, attestation,jwt,jwtRenewalWindow,jwtKeyValidity)
                    .setIsStoreSignatureRequired(storeSignatures)
                    .setBuilderExtensionsOptions(extensions)
                    .build();
        } catch (ClassCastException | NullPointerException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
            throw new SKFEException(ex.getLocalizedMessage());      //TODO replace with standard parsing error message
        } 
    }

    public static class FidoPolicyObjectBuilder{
        private final Long builderDid;
        private final Long builderSid;
        private final Long builderPid;
        private final String builderVersion;
        private final Date builderStartDate;
        private final Date builderEndDate;
        private final AlgorithmsPolicyOptions builderAlgorithmsOptions;
        private final RpPolicyOptions builderRpOptions;
        private final String builderRequireCounter;
        private Boolean builderIsUserSettingsRequired;
        private Boolean builderIsStoreSignaturesRequired;
        private final RegistrationPolicyOptions builderRegistrationOptions;
        private final AuthenticationPolicyOptions builderAuthenticationOptions;
        private ExtensionsPolicyOptions builderExtensionsOptions;
        private TrustedAuthenticatorPolicyOptions builderAllowedAAGUIDs;
        private final ArrayList<String> builderUserVerification; 
        private final Integer builderUserPresenceTimeout;
        private final AttestationPolicyOptions builderAttestation;
        private final JWTPolicyOptions builderJWT;
        private final Integer builderJwtRenewalWindow;
        private final Integer builderJwtKeyValidity; 
                

        public FidoPolicyObjectBuilder(
                Long did, Long sid, Long pid, String version,  ArrayList<String> userVerification,
                Integer userPresenceTimeout, Date startDate,
                Date endDate, AlgorithmsPolicyOptions algorithmsOptions,
                RpPolicyOptions rpOptions,String requireCounter,
                RegistrationPolicyOptions registrationOptions,
                AuthenticationPolicyOptions authenticationOptions, 
                TrustedAuthenticatorPolicyOptions trustedAuthenticatorsOptions, 
                AttestationPolicyOptions attestation, JWTPolicyOptions jwt,Integer jwtRenewalWindow,Integer jwtKeyValidity){
            this.builderDid = did;
            this.builderSid = sid;
            this.builderPid = pid;
            this.builderVersion = version;
            this.builderStartDate = new Date(startDate.getTime());
            if (endDate != null) {
                this.builderEndDate = new Date(endDate.getTime());
            } else {
                this.builderEndDate = null;
            }
            this.builderAlgorithmsOptions = algorithmsOptions;
            this.builderRpOptions = rpOptions;
            this.builderRequireCounter = requireCounter;
            this.builderRegistrationOptions = registrationOptions;
            this.builderAuthenticationOptions = authenticationOptions;
            this.builderAllowedAAGUIDs = trustedAuthenticatorsOptions;
            this.builderUserVerification = userVerification;
            this.builderUserPresenceTimeout = userPresenceTimeout;
            this.builderAttestation = attestation;
            this.builderJWT = jwt;
            this.builderJwtRenewalWindow = jwtRenewalWindow;
            this.builderJwtKeyValidity = jwtKeyValidity;
        }

        public FidoPolicyObjectBuilder setIsUserSettingsRequired(Boolean isUserSettingsRequired) {
            this.builderIsUserSettingsRequired = isUserSettingsRequired;
            return this;
        }

        public FidoPolicyObjectBuilder setIsStoreSignatureRequired(Boolean isStoreSignaturesRequired) {
            this.builderIsStoreSignaturesRequired = isStoreSignaturesRequired;
            return this;
        }

        public FidoPolicyObjectBuilder setBuilderExtensionsOptions(ExtensionsPolicyOptions builderExtensionsOptions) {
            this.builderExtensionsOptions = builderExtensionsOptions;
            return this;
        }

        public FidoPolicyObject build(){
            return new FidoPolicyObject(
                    builderDid, builderSid, builderPid, builderVersion, builderStartDate,
                    builderEndDate, builderAlgorithmsOptions, builderRpOptions,
                    builderRequireCounter, builderIsUserSettingsRequired,
                    builderIsStoreSignaturesRequired, builderRegistrationOptions,
                    builderAuthenticationOptions, builderExtensionsOptions,
                    builderAllowedAAGUIDs,builderUserVerification,builderUserPresenceTimeout,
                    builderAttestation, builderJWT,builderJwtRenewalWindow,builderJwtKeyValidity);
        }
    }
}
