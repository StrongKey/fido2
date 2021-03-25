/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.core.U2FAuthenticationChallenge;
import com.strongkey.skfs.utilities.FEreturn;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.json.JsonArray;

@Stateless
public class u2fPreauthBean implements u2fPreauthBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    /*************************************************************************
                                                 888
                                                 888
                                                 888
     .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
    d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
    88888888   X88K   88888888 888      888  888 888    88888888
    Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888

     *************************************************************************/
    /**
     * Executes the pre-authentication process which primarily includes generating
     * authentication challenge parameters for the given username complying to the
     * protocol specified. did is the FIDO domain credentials.
     *
     * NOTE : The did will be used for the production
     * version of the FIDO server software. They can be ignored for the open-source
     * version.
     * @param did       - FIDO domain id
     * @param protocol  - U2F protocol version to comply with.
     * @param username  - username
     * @param keyhandle - The user could have multiple fido authenticators registered
     *                      successfully. An authentication challenge can pertain
     *                      to only one unique fido authenticator (key handle).
     * @param appidfromDB
     * @param transports
     * @return          - FEReturn object that binds the U2F registration challenge
     *                      parameters in addition to a set of messages that explain
     *                      the series of actions happened during the process.
     */
    @Override
    public FEreturn execute(Long did,
                            String protocol,
                            String username,
                            String keyhandle,
                            String appidfromDB,
                            JsonArray transports) {

        //  Log the entry and inputs
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "execute");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5001"),
                        " EJB name=" + classname +
                        " did=" + did +
                        " protocol=" + protocol +
                        " username=" + username);

        //  Generate a new U2FAuthenticationChallenge object and returns the same
        FEreturn fer = new FEreturn();
        fer.setResponse(new U2FAuthenticationChallenge(protocol, username, keyhandle,appidfromDB, transports));

        //  log the exit and return
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
        return fer;
    }
}
