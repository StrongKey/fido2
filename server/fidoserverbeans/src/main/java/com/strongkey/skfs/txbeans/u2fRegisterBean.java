/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.core.U2FRegistrationResponse;
import com.strongkey.skfs.utilities.FEreturn;
import java.util.logging.Level;
import javax.ejb.Stateless;

@Stateless
public class u2fRegisterBean implements u2fRegisterBeanLocal {

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
     * Method that builds a u2f registration response object and processes the
     * same.
     * @param did       - FIDO domain id
     * @param protocol  - U2F protocol version to comply with.
     * @param regresponseJson  - U2F Reg Response parameters as a json string
     * @return          - FEreturn object with result
     * @throws SKFEException
     *                          - In case of any error
     */
    @Override
    public FEreturn execute(String did,
                            String protocol,
                            String regresponseJson) throws SKFEException {

        String appid = applianceMaps.getDomain(Long.parseLong(did)).getSkfeAppid();
        //  Log the entry and inputs
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "execute");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5001"),
                        " EJB name=" + classname +
                        " did=" + did +
                        " protocol=" + protocol +
                        " regresponseJson=" + regresponseJson);

        FEreturn fr = new FEreturn();

        //  Input checks
        if (protocol == null || protocol.isEmpty()) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-5001"), protocol);
            fr.append(SKFSCommon.getMessageProperty("FIDO-ERR-5001") + "protocol=" + protocol);
            return fr;
        }

        if (regresponseJson == null || regresponseJson.isEmpty()) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-5001"), regresponseJson);
            fr.append(SKFSCommon.getMessageProperty("FIDO-ERR-5001") + "registration response =" + regresponseJson);
            return fr;
        }

        //  Build a U2FRegistrationResponse object and process the same
        U2FRegistrationResponse regresp = new U2FRegistrationResponse(protocol, regresponseJson);
        if (regresp.verify(appid)) {
            fr.setResponse(regresp);
        }

        //  log the exit and return
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
        return fr;
    }
}
