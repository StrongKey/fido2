/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.entitybeans.FidoUsers;
import com.strongkey.skfs.pojos.FIDOUserMetadata;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;


@Stateless
public class getFIDOUserinfoBean implements getFIDOUserinfoBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    @EJB
    getFidoUserLocal getfidouserbean;
     @EJB
    addFidoUserBeanLocal addfidouserbean;

    @Override
    public FIDOUserMetadata execute(Long did, String username) throws SKFEException {
        //  Inputs check
        SKFSCommon.inputValidateSKCEDid(Long.toString(did));
        if (username == null || username.trim().isEmpty()) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.WARNING, "SKCE-ERR-1000", "NULL or empty argument for username : " + username);
            throw new SKFEException("NULL or empty argument for username : " + username);
        }

        FIDOUserMetadata authres = null;

        FidoUsers FIDOUser = getfidouserbean.getByUsername(did, username);
        if (FIDOUser == null) {
            addfidouserbean.execute(did, username);
            FIDOUser = getfidouserbean.getByUsername(did, username);
        }
        //  Build the auth result object
        authres = new FIDOUserMetadata(username,
                FIDOUser.getUserdn(),
                FIDOUser.getRegisteredEmails(),
                FIDOUser.getPrimaryEmail(),
                FIDOUser.getRegisteredPhoneNumbers(),
                FIDOUser.getPrimaryPhoneNumber(),
                FIDOUser.getTwoStepTarget(),
                FIDOUser.getFidoKeysEnabled(),
                FIDOUser.getTwoStepVerification(),
                did);

        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
        return authres;
    }
}
