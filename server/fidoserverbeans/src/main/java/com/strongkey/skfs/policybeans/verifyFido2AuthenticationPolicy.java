/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido.policyobjects.AuthenticationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.CounterPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
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
            FIDO2AuthenticatorData authData, FidoKeys signingKey) throws SKFEException {
        //Get policy from userInfo
        FidoPolicyObject fidoPolicy = getpolicybean.getByMapKey(userInfo.getPolicyMapKey()).getFp();
        FidoKeys fk = getfidokeysbean.getByfkid(userInfo.getSkid(), did, userInfo.getUsername(), userInfo.getFkid());
        
        //Verify Counter
        verifyCounter(fidoPolicy.getCounterOptions(), clientJson, authData, signingKey, fidoPolicy.getVersion());
        
        //Verify userVerification was given if required
        verifyUserVerification(fidoPolicy.getAuthenticationOptions(), authData, userInfo.getUserVerificationReq(), fidoPolicy.getVersion());
        
        //TODO add additional checks to ensure the the authentication data meets the standard of the policy
        
        //TODO add checks to ensure the stored information about the key (attestation certificates, MDS, etc) still meets the standard
    }
    
    private void verifyCounter(CounterPolicyOptions counterOp, JsonObject clientJson,
            FIDO2AuthenticatorData authData, FidoKeys signingKey, Integer version) throws SKFEException {
        int oldCounter = signingKey.getCounter();
        int newCounter = authData.getCounterValueAsInt();
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "COUNTER TEST - OLD - NEW = " + oldCounter + " - " + newCounter);
        if(counterOp.getIsCounterRequired()){
            if(oldCounter == 0 && newCounter <= oldCounter){
                throw new SKFEException("Policy requires counter");
            }
        }
        if(counterOp.getIsCounterIncreaseRequired()){
            if((oldCounter != 0 && newCounter <= oldCounter)){
                throw new SKFEException("Policy requires counter increase");
            }
        }
    }
    
    private void verifyUserVerification(AuthenticationPolicyOptions authOp, 
            FIDO2AuthenticatorData authData, String userVerificationReq, Integer version){
        //Default blank to Webauthn defined defaults
        userVerificationReq = (userVerificationReq == null) ? skfsConstants.POLICY_CONST_PREFERRED : userVerificationReq;
        
        //Double check that what was stored in UserSessionInfo is valid for the policy
        if (!authOp.getUserVerification().contains(userVerificationReq)) {
            throw new IllegalArgumentException("Policy Exception: Preauth userVerificationRequirement does not meet policy");
        }

        //If User Verification was required, verify it was provided
        if (userVerificationReq.equalsIgnoreCase(skfsConstants.POLICY_CONST_REQUIRED) && !authData.isUserVerified()) {
            throw new IllegalArgumentException("User Verification required by policy");
        }
    }
}
