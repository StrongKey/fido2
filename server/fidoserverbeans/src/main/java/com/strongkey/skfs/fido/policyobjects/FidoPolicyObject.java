
/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSConstants;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;
import javax.json.JsonObject;

public class FidoPolicyObject {
    private final Long did;
    private final Long sid;
    private final Long pid;
    private final String version;
    private final Date startDate;
    private final Date endDate;
    private final SystemPolicyOptions systemOptions;
    private final AlgorithmsPolicyOptions algorithmsOptions;
    private final RpPolicyOptions rpOptions;
    private final RegistrationPolicyOptions registrationOptions;
    private final AuthenticationPolicyOptions authenticationOptions;
    private final DefinedExtensionsPolicyOptions extensionsOptions;
    private final AttestationPolicyOptions attestation;
    private final JWTPolicyOptions jwt;
    private final AuthorizationPolicyOptions authorizationOptions;
    private final MDSPolicyOptions mds;

    private FidoPolicyObject(
            Long did,
            Long sid,
            Long pid,
            String version,
            Date startDate,
            Date endDate,
            SystemPolicyOptions system,
            AlgorithmsPolicyOptions algorithmsOptions,
            RpPolicyOptions rpOptions,
            RegistrationPolicyOptions registrationOptions,
            AuthenticationPolicyOptions authenticationOptions,
            DefinedExtensionsPolicyOptions extensionsOptions,
            AttestationPolicyOptions attestation,
            JWTPolicyOptions jwt,
            AuthorizationPolicyOptions authorizationOptions,
            MDSPolicyOptions mds){
        this.did = did;
        this.sid = sid;
        this.pid = pid;
        this.version = version;
        this.startDate = startDate;
        this.endDate = endDate;
        this.systemOptions = system;
        this.algorithmsOptions = algorithmsOptions;
        this.rpOptions = rpOptions;
        this.registrationOptions = registrationOptions;
        this.authenticationOptions = authenticationOptions;
        this.extensionsOptions = extensionsOptions;
        this.attestation = attestation;
        this.jwt = jwt;
        this.authorizationOptions = authorizationOptions;
        this.mds = mds;
        
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
    
    public SystemPolicyOptions getSystemOptions(){
        return systemOptions;
    }
    
    public AlgorithmsPolicyOptions getAlgorithmsOptions() {
        return algorithmsOptions;
    }
    
    
    public RpPolicyOptions getRpOptions() {
        return rpOptions;
    }



    public RegistrationPolicyOptions getRegistrationOptions() {
        return registrationOptions;
    }

    public AuthenticationPolicyOptions getAuthenticationOptions() {
        return authenticationOptions;
    }

    public DefinedExtensionsPolicyOptions getExtensionsOptions() {
        return extensionsOptions;
    }
    
    public AttestationPolicyOptions getAttestationOptions(){
        return attestation;
    }
    
    public JWTPolicyOptions getJWT(){
        return jwt;
    }
    
    public AuthorizationPolicyOptions getAuthorizationPolicyOptions(){
        return authorizationOptions;
    }
    public MDSPolicyOptions getMDS(){
        return mds;
    }

    public static FidoPolicyObject parse(String base64Policy, Long did, Long sid, Long pid) throws SKFEException {
        try {
            String policyString = new String(Base64.getUrlDecoder().decode(base64Policy), "UTF-8");
            JsonObject policyJson = applianceCommon.stringToJSON(policyString);
            
            JsonObject FidoPolicyJson = policyJson.getJsonObject(SKFSConstants.POLICY_SYSTEM_FIDO_POLICY);
            
            //Policy Attributes Parsing 
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
            
            SystemPolicyOptions system = SystemPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_SYSTEM)); 
            
            AlgorithmsPolicyOptions algorithms = AlgorithmsPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_ALGORITHMS));
           
            RpPolicyOptions rp = RpPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_RP));
        
            RegistrationPolicyOptions registration = RegistrationPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_REGISTRATION));

            AuthenticationPolicyOptions authentication = AuthenticationPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_AUTHENTICATION));
            
            AuthorizationPolicyOptions authorization = AuthorizationPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_AUTHORIZATION));

            DefinedExtensionsPolicyOptions extensions = DefinedExtensionsPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_EXTENSIONS));
           
            MDSPolicyOptions mds = null;
            if(FidoPolicyJson.containsKey(SKFSConstants.POLICY_ATTR_MDS)){
                mds = MDSPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTR_MDS));
            }
            
            AttestationPolicyOptions attestation = AttestationPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_ATTESTATION));
            
            JWTPolicyOptions jwt = JWTPolicyOptions.parse(FidoPolicyJson.getJsonObject(SKFSConstants.POLICY_JWT));
            
            return new FidoPolicyObject.FidoPolicyObjectBuilder(did, sid, pid, version, 
                    startDate, endDate, system, algorithms, rp, registration, authentication, 
                    attestation,jwt,authorization,mds,extensions)
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
        private final SystemPolicyOptions builderSystem;
        private final AlgorithmsPolicyOptions builderAlgorithmsOptions;
        private final RpPolicyOptions builderRpOptions;
        private final RegistrationPolicyOptions builderRegistrationOptions;
        private final AuthenticationPolicyOptions builderAuthenticationOptions;
        private final DefinedExtensionsPolicyOptions builderExtensionsOptions;
        private final AttestationPolicyOptions builderAttestation;
        private final JWTPolicyOptions builderJWT;
        private final AuthorizationPolicyOptions builderAuthorizationOptions;
        private final MDSPolicyOptions builderMds;
                

        public FidoPolicyObjectBuilder(
                Long did,
                Long sid, 
                Long pid, 
                String version,
                Date startDate,
                Date endDate, 
                SystemPolicyOptions system, 
                AlgorithmsPolicyOptions algorithmsOptions,
                RpPolicyOptions rpOptions,
                RegistrationPolicyOptions registrationOptions,
                AuthenticationPolicyOptions authenticationOptions, 
                AttestationPolicyOptions attestation, 
                JWTPolicyOptions jwt,
                AuthorizationPolicyOptions authorizationOptions,
                MDSPolicyOptions mds,
                DefinedExtensionsPolicyOptions extensions){
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
            this.builderSystem = system;
            this.builderAlgorithmsOptions = algorithmsOptions;
            this.builderRpOptions = rpOptions;
            this.builderRegistrationOptions = registrationOptions;
            this.builderAuthenticationOptions = authenticationOptions;
            this.builderAttestation = attestation;
            this.builderJWT = jwt;
            this.builderAuthorizationOptions = authorizationOptions;
            this.builderMds = mds;
            this.builderExtensionsOptions = extensions;
        }

     

        public FidoPolicyObject build(){
            return new FidoPolicyObject(
                    builderDid, builderSid, builderPid, builderVersion, builderStartDate,
                    builderEndDate, builderSystem, builderAlgorithmsOptions, builderRpOptions, 
                    builderRegistrationOptions,builderAuthenticationOptions, builderExtensionsOptions,
                    builderAttestation, builderJWT,
                    builderAuthorizationOptions,builderMds);
        }
    }
}
