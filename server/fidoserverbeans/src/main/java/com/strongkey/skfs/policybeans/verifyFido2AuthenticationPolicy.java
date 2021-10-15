/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.policybeans;

import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.fido.policyobjects.DefinedExtensionsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.fido2.FIDO2Extensions;
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
        verifyCounter(fidoPolicy.getSystemOptions().getCounterRequirement(), clientJson, authData, signingKey, fidoPolicy.getVersion(), format);

        //Verify userVerification was given if required
        verifyUserVerification(fidoPolicy, authData, userInfo.getUserVerificationReq(), fidoPolicy.getVersion());

        //Verify Extensions
        verifyExtensions(authData.getExt(),fidoPolicy.getExtensionsOptions());
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
        
        //Double check that what was stored in UserSessionInfo is valid for the policy
        if (!fidoPolicy.getSystemOptions().getUserVerification().contains(userVerificationReq) && userVerificationReq != null) {
            throw new SKIllegalArgumentException("Policy Exception: Preauth userVerificationRequirement does not meet policy");
        }

        //If User Verification was required, verify it was provided
        if(userVerificationReq != null){
            if (userVerificationReq.equalsIgnoreCase(SKFSConstants.POLICY_CONST_REQUIRED) && !authData.isUserVerified()) {
                throw new SKIllegalArgumentException("User Verification required by policy");
            }
        }
    }
    private void verifyExtensions(FIDO2Extensions ext, DefinedExtensionsPolicyOptions extOp) throws SKFEException {
        
        if((extOp.getUVM() != null || extOp.getLargeBlob() != null) && ext == null){
            throw new SKFEException("Extension required by policy");
        }
        if(extOp.getUVM() != null){
            if(!ext.containsExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM)){
                throw new SKFEException("UVM Extension required by policy");//FIDO-MSG-0053
            }
            Object uvm = ext.getExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", "UVM Extension: "+ uvm.toString());
            
        }
//        if(extOp.getLargeBlob() != null){
//            if(!ext.containsExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM)){
//                throw new SKFEException("LargeBlob Extension required by policy");
//            }
//            Object largeBlob = ext.getExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM);
//            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", "LargeBlob Extension: "+largeBlob.toString());
//               
//        }
    }
}
