/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import java.util.Base64;
import java.util.Date;
import javax.json.JsonObject;

public class FidoPolicyObject {
    private final Long did;
    private final Long sid;
    private final Long pid;
    private final Integer version;
    private final Date startDate;
    private final Date endDate;
    private final CryptographyPolicyOptions cryptographyOptions;
    private final RpPolicyOptions rpOptions;
    private final Integer timeout;
    private final MdsPolicyOptions mdsOptions;
    private final String tokenBindingOption;
    private final CounterPolicyOptions counterOptions;
    private final Boolean isUserSettingsRequired;
    private final Boolean isStoreSignaturesRequired;
    private final RegistrationPolicyOptions registrationOptions;
    private final AuthenticationPolicyOptions authenticationOptions;
    private final ExtensionsPolicyOptions extensionsOptions;

    private FidoPolicyObject(
            Long did,
            Long sid,
            Long pid,
            Integer version,
            Date startDate,
            Date endDate,
            CryptographyPolicyOptions cryptographyOptions,
            RpPolicyOptions rpOptions,
            Integer timeout,
            MdsPolicyOptions mdsOptions,
            String tokenBindingOption,
            CounterPolicyOptions counterOptions,
            Boolean isUserSettingsRequired,
            Boolean isStoreSignaturesRequired,
            RegistrationPolicyOptions registrationOptions,
            AuthenticationPolicyOptions authenticationOptions,
            ExtensionsPolicyOptions extensionsOptions){
        this.did = did;
        this.sid = sid;
        this.pid = pid;
        this.version = version;
        this.startDate = startDate;
        this.endDate = endDate;
        this.cryptographyOptions = cryptographyOptions;
        this.rpOptions = rpOptions;
        this.timeout = timeout;
        this.mdsOptions = mdsOptions;
        this.tokenBindingOption = tokenBindingOption;
        this.counterOptions = counterOptions;
        this.isUserSettingsRequired = isUserSettingsRequired;
        this.isStoreSignaturesRequired = isStoreSignaturesRequired;
        this.registrationOptions = registrationOptions;
        this.authenticationOptions = authenticationOptions;
        this.extensionsOptions = extensionsOptions;
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

    public Integer getVersion(){
        return version;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public CryptographyPolicyOptions getCryptographyOptions() {
        return cryptographyOptions;
    }

    public RpPolicyOptions getRpOptions() {
        return rpOptions;
    }

    public Integer getTimeout() {
        return timeout;
    }

    public MdsPolicyOptions getMdsOptions() {
        return mdsOptions;
    }

    public String getTokenBindingOption() {
        return tokenBindingOption;
    }

    public CounterPolicyOptions getCounterOptions() {
        return counterOptions;
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

    public static FidoPolicyObject parse(String base64Policy, Integer version,
            Long did, Long sid, Long pid, Date startDate, Date endDate) throws SKFEException {
        try {
            String policyString = new String(Base64.getUrlDecoder().decode(base64Policy));
            JsonObject policyJson = applianceCommon.stringToJSON(policyString);

            CryptographyPolicyOptions crypto = CryptographyPolicyOptions.parse(policyJson.getJsonObject(skfsConstants.POLICY_ATTR_CRYPTOGRAPHY));

            RpPolicyOptions rp = RpPolicyOptions.parse(policyJson.getJsonObject(skfsConstants.POLICY_ATTR_RP));

            int timeoutInt = policyJson.getInt(skfsConstants.POLICY_ATTR_TIMEOUT, -1);
            Integer timeout = (timeoutInt == -1) ? null : timeoutInt;

            MdsPolicyOptions mds = MdsPolicyOptions.parse(policyJson.getJsonObject(skfsConstants.POLICY_ATTR_MDS));

            String tokenBinding = policyJson.getString(skfsConstants.POLICY_ATTR_TOKENBINDING, null);

            CounterPolicyOptions counter = CounterPolicyOptions.parse(policyJson.getJsonObject(skfsConstants.POLICY_ATTR_COUNTER));

            Boolean userSettings = skfsCommon.handleNonExistantJsonBoolean(policyJson, skfsConstants.POLICY_ATTR_USERSETTINGS);

            Boolean storeSignatures = skfsCommon.handleNonExistantJsonBoolean(policyJson, skfsConstants.POLICY_ATTR_STORESIGNATURES);

            RegistrationPolicyOptions registration = RegistrationPolicyOptions.parse(policyJson.getJsonObject(skfsConstants.POLICY_ATTR_REGISTRATION));

            AuthenticationPolicyOptions authentication = AuthenticationPolicyOptions.parse(policyJson.getJsonObject(skfsConstants.POLICY_ATTR_AUTHENTICATION));

            ExtensionsPolicyOptions extensions = ExtensionsPolicyOptions.parse(policyJson.getJsonObject(skfsConstants.POLICY_ATTR_EXTENSIONS));

            return new FidoPolicyObject.FidoPolicyObjectBuilder(did, sid, pid, version,
                    startDate, endDate, crypto, rp, mds, counter, registration, authentication)
                    .setTimeout(timeout)
                    .setTokenBindingOption(tokenBinding)
                    .setIsUserSettingsRequired(userSettings)
                    .setIsStoreSignatureRequired(storeSignatures)
                    .setBuilderExtensionsOptions(extensions)
                    .build();
        } catch (ClassCastException | NullPointerException ex) {
            ex.printStackTrace();
            throw new SKFEException(ex.getLocalizedMessage());      //TODO replace with standard parsing error message
        }
    }

    public static class FidoPolicyObjectBuilder{
        private final Long builderDid;
        private final Long builderSid;
        private final Long builderPid;
        private final Integer builderVersion;
        private final Date builderStartDate;
        private final Date builderEndDate;
        private final CryptographyPolicyOptions builderCryptographyOptions;
        private final RpPolicyOptions builderRpOptions;
        private Integer builderTimeout;
        private final MdsPolicyOptions builderMdsOptions;
        private String builderTokenBindingOption;
        private final CounterPolicyOptions builderCounterOptions;
        private Boolean builderIsUserSettingsRequired;
        private Boolean builderIsStoreSignaturesRequired;
        private final RegistrationPolicyOptions builderRegistrationOptions;
        private final AuthenticationPolicyOptions builderAuthenticationOptions;
        private ExtensionsPolicyOptions builderExtensionsOptions;

        public FidoPolicyObjectBuilder(
                Long did, Long sid, Long pid, Integer version, Date startDate,
                Date endDate, CryptographyPolicyOptions cryptographyOptions,
                RpPolicyOptions rpOptions, MdsPolicyOptions mdsOptions,
                CounterPolicyOptions counterOptions,
                RegistrationPolicyOptions registrationOptions,
                AuthenticationPolicyOptions authenticationOptions){
            this.builderDid = did;
            this.builderSid = sid;
            this.builderPid = pid;
            this.builderVersion = version;
            this.builderStartDate = startDate;
            this.builderEndDate = endDate;
            this.builderCryptographyOptions = cryptographyOptions;
            this.builderRpOptions = rpOptions;
            this.builderMdsOptions = mdsOptions;
            this.builderCounterOptions = counterOptions;
            this.builderRegistrationOptions = registrationOptions;
            this.builderAuthenticationOptions = authenticationOptions;
        }

        public FidoPolicyObjectBuilder setTimeout(Integer timeout) {
            this.builderTimeout = timeout;
            return this;
        }

        public FidoPolicyObjectBuilder setTokenBindingOption(String tokenBindingOption) {
            this.builderTokenBindingOption = tokenBindingOption;
            return this;
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
                    builderEndDate, builderCryptographyOptions, builderRpOptions,
                    builderTimeout, builderMdsOptions, builderTokenBindingOption,
                    builderCounterOptions, builderIsUserSettingsRequired,
                    builderIsStoreSignaturesRequired, builderRegistrationOptions,
                    builderAuthenticationOptions, builderExtensionsOptions);
        }
    }
}
