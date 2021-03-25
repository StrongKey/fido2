/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.core;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.Serializable;
import java.util.logging.Level;

/**
 * Super class for U2F Challenges that have to be sent back to the RP application
 * from the FIDO server.
 *
 */
public class U2FChallenge implements Serializable {

    /**
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    /**
     * Supported versions for U2F protocol
     */
    final static String U2F_VERSION_V2 = "U2F_V2";
    final static String FIDO = "FIDO2_0";

    /**
     * Common parameters for a challenge in U2F
     */
    String version;
//    String sessionid;

    /**
     * Constructor that constructs U2F registration challenge parameters for the
     * user specified by username and complying to U2F protocol version specified
     * by u2fversion.
     * @param u2fversion - Version of the U2F protocol being communicated in;
     *                      example : "U2F_V2"
     * @param username   - any non-empty username
     * @throws IllegalArgumentException
     *                   - In case of any error
     */
    public U2FChallenge(String u2fversion, String username) throws IllegalArgumentException {

        //  Input checks
        if ( u2fversion==null || u2fversion.trim().isEmpty() ) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FChallenge", SKFSCommon.getMessageProperty("FIDO-ERR-5001"), " protocol");
            throw new IllegalArgumentException(SKFSCommon.getMessageProperty("FIDO-ERR-5001") + " protocol");
        }

        if (username==null || username.trim().isEmpty() ) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FChallenge", SKFSCommon.getMessageProperty("FIDO-ERR-5001"), " username");
            throw new IllegalArgumentException(SKFSCommon.getMessageProperty("FIDO-ERR-5001") + " username");
        }

        if (username.trim().length() > Integer.parseInt(applianceCommon.getApplianceConfigurationProperty("appliance.cfg.maxlen.256charstring"))) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FChallenge", SKFSCommon.getMessageProperty("FIDO-ERR-0027"), " username should be limited to 256 characters");
            throw new IllegalArgumentException(SKFSCommon.getMessageProperty("FIDO-ERR-0027") + " username should be limited to 256 characters");
        }

        //  u2f version specific code
        if ( u2fversion.equalsIgnoreCase(U2F_VERSION_V2) || u2fversion.equalsIgnoreCase(FIDO)) {
            version = u2fversion;
//            sessionid = U2FUtility.generateSessionid(this.nonce);
        } else {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "U2FChallenge", SKFSCommon.getMessageProperty("FIDO-ERR-5002"), " protocol passed=" + u2fversion);
            throw new IllegalArgumentException(SKFSCommon.getMessageProperty("FIDO-ERR-5002") + " protocol passed=" + u2fversion);
        }
    }

    /**
     * Empty constructor since this class implements java.io.Serializable
     */
    protected U2FChallenge() {
    }
}
