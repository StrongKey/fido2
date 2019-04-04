/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
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
        skfsLogger.entering(skfsConstants.SKFE_LOGGER,classname, "execute");
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5001"), 
                        " EJB name=" + classname + 
                        " did=" + did + 
                        " protocol=" + protocol + 
                        " regresponseJson=" + regresponseJson);
        
        FEreturn fr = new FEreturn();
        
        //  Input checks
        if (protocol == null || protocol.isEmpty()) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-5001"), protocol);
            fr.append(skfsCommon.getMessageProperty("FIDO-ERR-5001") + "protocol=" + protocol);
            return fr;
        }
        
        if (regresponseJson == null || regresponseJson.isEmpty()) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-5001"), regresponseJson);
            fr.append(skfsCommon.getMessageProperty("FIDO-ERR-5001") + "registration response =" + regresponseJson);
            return fr;
        }

        //  Build a U2FRegistrationResponse object and process the same
        U2FRegistrationResponse regresp = new U2FRegistrationResponse(protocol, regresponseJson);
        if (regresp.verify(appid)) {
            fr.setResponse(regresp);
        }

        //  log the exit and return
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
        return fr;
    }
}
