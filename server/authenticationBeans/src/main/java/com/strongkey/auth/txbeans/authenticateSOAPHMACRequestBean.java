/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.auth.txbeans;

import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.applianceInputChecks;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.crypto.interfaces.initCryptoModule;
import com.strongkey.crypto.utility.CryptoException;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.utilities.skceCommon;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ejb.Stateless;

/**
 * EJB to perform hmac based authentications and authorizations
 */
@Stateless
public class authenticateSOAPHMACRequestBean implements authenticateSOAPHMACRequestBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    private final String hmacKeystorePassword = skceCommon.getConfigurationProperty("skce.cfg.property.standalone.hmackeystore.password");
    /*
     * ****************************************************************************************
     *                                               888
     *                                               888
     *                                               888
     *   .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     *  d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     *  88888888   X88K   88888888 888      888  888 888    88888888
     *  Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     *   "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *
     *****************************************************************************************
     */
    /**
     * This method authenticates a credential - username and password - against
     *
     * @param did Long the domain identifier for which to authenticate to
     * @param contentsha256
     * @param authorization
     * @param timestamp
     * @param apiversion
     * @param contenttype
     * @param requesturi
     * @param requestbody String body of the request to be SHA'd
     * @return boolean value indicating either True (for authenticated) or False
     * (for unauthenticated or failure in processing)
     */
    @Override
    public boolean execute(Long did,
                    String contentsha256,
                    String authorization,
                    Long timestamp,
                    String apiversion,
                    String contenttype,
                    String requesturi,
                    String requestbody) {

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "execute", "APPL-MSG-1051",
                "\n EJB name=" + classname +
                "\n did=" + did);

        // Input checks
        try {
            applianceInputChecks.checkDid(did);
        } catch (NullPointerException | IllegalArgumentException ex){
            return false;
        }

        String generatedSHA = "";

        if (requestbody != null) {
            String contentSHA  = contentsha256;
            String json =requestbody;
            System.out.println("json = " + json);
            generatedSHA = cryptoCommon.calculateHash(json, "SHA-256");

            if (!generatedSHA.equals(contentSHA)) {
                strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, classname, "execute", "APPL-ERR-1041", "Received: " + contentSHA + " Expected: " + generatedSHA);
                return false;
            }
        }

        String authHeader = authorization;
        if (authHeader == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, classname, "execute", "APPL-ERR-1040", "");
            return false;
        }

        Pattern r = Pattern.compile("HMAC ([^:]+):(.*)");
        Matcher m = r.matcher(authHeader);

        String requestHmac;
        String accessKey;
        if (m.find()) {
            accessKey = m.group(1);
            requestHmac = m.group(2);
        } else {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, classname, "execute", "APPL-ERR-1039", authHeader);
            return false;
        }

        String requestToHmac = "POST" + "\n"
                + generatedSHA + "\n"
                + contenttype + "\n"
                + timestamp + "\n"
                + apiversion + "\n"
                + requesturi;

        // TODO Check if date is expired

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "execute", "APPL-MSG-1054", "\n" + requestToHmac);

        try {
            String hmac = initCryptoModule.getCryptoModule().hmacRequest(hmacKeystorePassword, accessKey, requestToHmac);
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "execute", "APPL-MSG-1015", hmac.substring(0, 4) + "****************************************");
            if (requestHmac.equals(hmac)) {
                strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINER, classname, "execute", "APPL-MSG-1016", "");
                return true;
            } else {
                strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "execute", "APPL-ERR-1016", "Expected HMAC: " + requestHmac + " Produced HMAC: " + hmac.substring(0, 4) + "****************************************");
                return false;
            }

        } catch (CryptoException ex) {
            return false;
        }
    }
}
