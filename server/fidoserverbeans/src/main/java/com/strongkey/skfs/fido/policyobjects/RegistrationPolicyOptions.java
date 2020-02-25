/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.fido.policyobjects.AuthenticatorSelection.AuthenticatorSelectionBuilder;
import com.strongkey.skfs.utilities.skfsConstants;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;
import javax.json.JsonValue;

public class RegistrationPolicyOptions {
    private final String icon;
    private final String displayName;
    private final Integer useridLength;
    private final String excludeCredentials;
    private final AuthenticatorSelection authenticatorSelection;
    private final List<String> attestation;

    private RegistrationPolicyOptions(String icon, String displayName,
            Integer useridLength, String excludeCredentials,
            AuthenticatorSelection authenticatorSelection, List<String> attestation){
        this.icon = icon;
        this.displayName = displayName;
        this.useridLength = useridLength;
        this.excludeCredentials = excludeCredentials;
        this.authenticatorSelection = authenticatorSelection;
        this.attestation = attestation;
    }

    public String getIcon() {
        return icon;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Integer getUseridLength(){
        return useridLength;
    }

    public String getExcludeCredentials() {
        return excludeCredentials;
    }

    public AuthenticatorSelection getAuthenticatorSelection() {
        return authenticatorSelection;
    }

    public List<String> getAttestation() {
        return attestation;
    }

    public static RegistrationPolicyOptions parse(JsonObject registrationJson) {
        JsonObject authenticatorSelectionJson = registrationJson.getJsonObject(skfsConstants.POLICY_REGISTRATION_AUTHENTICATORSELECTION);
        AuthenticatorSelection authenticatorSelection = new AuthenticatorSelectionBuilder(
                new ArrayList<>(authenticatorSelectionJson.getJsonArray(skfsConstants.POLICY_REGISTRATION_AUTHENTICATORATTACHMENT).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
                new ArrayList<>(authenticatorSelectionJson.getJsonArray(skfsConstants.POLICY_REGISTRATION_REQUIRERESIDENTKEY).stream()
                        .map(x -> x.equals(JsonValue.TRUE))         //TODO find a better method of getting the value
                        .collect(Collectors.toList())),
                new ArrayList<>(authenticatorSelectionJson.getJsonArray(skfsConstants.POLICY_REGISTRATION_USERVERIFICATION).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList()))).build();

        return new RegistrationPolicyOptions.RegistrationPolicyOptionsBuilder(
                registrationJson.getString(skfsConstants.POLICY_REGISTRATION_DISPLAYNAME),
                registrationJson.getString(skfsConstants.POLICY_REGISTRATION_EXCLUDECREDENTIALS),
                new ArrayList<>(registrationJson.getJsonArray(skfsConstants.POLICY_REGISTRATION_ATTESTATION).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())))
                .setIcon(registrationJson.getString(skfsConstants.POLICY_REGISTRATION_ICON, null))
                .setAuthenticatorSelection(authenticatorSelection)
                .build();
    }

    public static class RegistrationPolicyOptionsBuilder{
        private String builderIcon;
        private final String builderDisplayName;
        private Integer builderUseridLength;
        private final String builderExcludeCredentials;
        private AuthenticatorSelection builderAuthenticatorSelection;
        private final List<String> builderAttestation;

        public RegistrationPolicyOptionsBuilder(String displayName, String excludeCredentials, List<String> attestation){
            this.builderDisplayName = displayName;
            this.builderExcludeCredentials = excludeCredentials;
            this.builderAttestation = attestation;
        }

        public RegistrationPolicyOptionsBuilder setIcon(String icon){
            this.builderIcon = icon;
            return this;
        }

        public RegistrationPolicyOptionsBuilder setUseridLength(Integer useridLength){
            this.builderUseridLength = useridLength;
            return this;
        }

        public RegistrationPolicyOptionsBuilder setAuthenticatorSelection(AuthenticatorSelection authenticatorSelection){
            this.builderAuthenticatorSelection = authenticatorSelection;
            return this;
        }

        public RegistrationPolicyOptions build(){
            return new RegistrationPolicyOptions(builderIcon, builderDisplayName,
                    builderUseridLength, builderExcludeCredentials,
                    builderAuthenticatorSelection, builderAttestation);
        }
    }
}
