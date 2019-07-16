/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skce.pojos.LDAPUserMetadata;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.entitybeans.FidoUsers;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;

@Stateless
public class getDBUserInfoBean implements getDBUserInfoBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();
    
    @EJB
    getFidoUserLocal getfidouserbean;
     @EJB
    addFidoUserBeanLocal addfidouserbean;
     
    @Override
    public LDAPUserMetadata execute(Long did, String username) throws SKFEException {
        //  Inputs check
        skfsCommon.inputValidateSKCEDid(Long.toString(did));
        if (username == null || username.trim().isEmpty()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.WARNING, "SKCE-ERR-1000", "NULL or empty argument for username : " + username);
            throw new SKFEException("NULL or empty argument for username : " + username);
        }

        LDAPUserMetadata authres = null;

        FidoUsers FIDOUser = getfidouserbean.GetByUsername(did, username);
        if (FIDOUser == null) {
            addfidouserbean.execute(did, username);
            FIDOUser = getfidouserbean.GetByUsername(did, username);
        }
        //  Build the auth result object
        authres = new LDAPUserMetadata(username,
                FIDOUser.getUserdn(),
                "",
                "",
                "",
                FIDOUser.getRegisteredEmails(),
                FIDOUser.getPrimaryEmail(),
                FIDOUser.getRegisteredPhoneNumbers(),
                FIDOUser.getPrimaryPhoneNumber(),
                FIDOUser.getTwoStepTarget(),
                FIDOUser.getFidoKeysEnabled(),
                FIDOUser.getTwoStepVerification(),
                did);

        skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
        return authres;
    }
}
