/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import java.util.List;

public class AuthenticatorSelection {
    private final List<String> authenticatorAttachment;
    private final List<Boolean> requireResidentKey;
    private final List<String> userVerification;

    private AuthenticatorSelection(List<String> authenticatorAttachment,
            List<Boolean> requireResidentKey,
            List<String> userVerification){
        this.authenticatorAttachment = authenticatorAttachment;
        this.requireResidentKey = requireResidentKey;
        this.userVerification = userVerification;
    }

    public List<String> getAuthenticatorAttachment() {
        return authenticatorAttachment;
    }

    public List<Boolean> getRequireResidentKey() {
        return requireResidentKey;
    }

    public List<String> getUserVerification() {
        return userVerification;
    }



    public static class AuthenticatorSelectionBuilder{
        private final List<String> builderAuthenticatorAttachment;
        private final List<Boolean> builderRequireResidentKey;
        private final List<String> builderUserVerification;

        public AuthenticatorSelectionBuilder(List<String> authenticatorAttachment,
            List<Boolean> requireResidentKey,
            List<String> userVerification){
            this.builderAuthenticatorAttachment = authenticatorAttachment;
            this.builderRequireResidentKey = requireResidentKey;
            this.builderUserVerification = userVerification;
        }

        public AuthenticatorSelection build(){
            return new AuthenticatorSelection(builderAuthenticatorAttachment,
                    builderRequireResidentKey,
                    builderUserVerification);
        }
    }
}
