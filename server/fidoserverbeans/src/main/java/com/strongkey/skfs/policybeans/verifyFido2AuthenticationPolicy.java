/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.policybeans;

import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.JsonObject;

@Stateless
public class verifyFido2AuthenticationPolicy implements verifyFido2AuthenticationPolicyLocal {

    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;
    @EJB
    getFidoKeysLocal getfidokeysbean;

    @Override
    public void execute(UserSessionInfo userInfo, long did, JsonObject clientJson,
            FIDO2AuthenticatorData authData, FidoKeys signingKey, String format) throws SKFEException {
        //Get policy from userInfo
        FidoPolicyObject fidoPolicy = getpolicybean.getByMapKey(userInfo.getPolicyMapKey()).getFp();
//        FidoKeys fk = getfidokeysbean.getByfkid(userInfo.getSkid(), did, userInfo.getUsername(), userInfo.getFkid());

        //Verify Counter
        verifyCounter(fidoPolicy.getCounterRequirement(), clientJson, authData, signingKey, fidoPolicy.getVersion(), format);

        //Verify userVerification was given if required
        verifyUserVerification(fidoPolicy, authData, userInfo.getUserVerificationReq(), fidoPolicy.getVersion());

        //TODO add additional checks to ensure the the authentication data meets the standard of the policy

        //TODO add checks to ensure the stored information about the key (attestation certificates, MDS, etc) still meets the standard
    }

    private void verifyCounter(String counterRequire, JsonObject clientJson,
            FIDO2AuthenticatorData authData, FidoKeys signingKey, String version, String format) throws SKFEException {
        int oldCounter = signingKey.getCounter();
        int newCounter = authData.getCounterValueAsInt();
        if(format == null){
            format="";
        }
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "COUNTER TEST - OLD - NEW = " + oldCounter + " - " + newCounter);
        if(counterRequire.equals("mandatory") && !format.equalsIgnoreCase("apple")){
            if(oldCounter == 0 && newCounter <= oldCounter){
                throw new SKFEException("Policy requires counter");
            }
            if((oldCounter != 0 && newCounter <= oldCounter)){
                throw new SKFEException("Policy requires counter increase");
            }
        } else {
            if(oldCounter != 0 && newCounter <= oldCounter){
                throw new SKFEException("Policy requires counter increase");
            }
        }
    }

    private void verifyUserVerification(FidoPolicyObject fidoPolicy,
            FIDO2AuthenticatorData authData, String userVerificationReq, String version){
        //Default blank to Webauthn defined defaults
        userVerificationReq = (userVerificationReq == null) ? SKFSConstants.POLICY_CONST_PREFERRED : userVerificationReq;

        //Double check that what was stored in UserSessionInfo is valid for the policy
        if (!fidoPolicy.getUserVerification().contains(userVerificationReq)) {
            throw new SKIllegalArgumentException("Policy Exception: Preauth userVerificationRequirement does not meet policy");
        }

        //If User Verification was required, verify it was provided
        if (userVerificationReq.equalsIgnoreCase(SKFSConstants.POLICY_CONST_REQUIRED) && !authData.isUserVerified()) {
            throw new SKIllegalArgumentException("User Verification required by policy");
        }
    }
}
