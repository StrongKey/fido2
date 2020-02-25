/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.utilities;

import java.util.logging.Level;

public class applianceInputChecks {

    private static final String classname = "applianceInputChecks";

    static {
    }

    public applianceInputChecks() {

    }

    public static boolean checkDid(Long did) {

        if (did == null) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1003", "did");
            throw new NullPointerException(applianceCommon.getMessageProperty("APPL-ERR-1003").replace("{0}", "") + "did");
        } else if (did < 1 || did > Long.MAX_VALUE) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1002", "did");
            throw new IllegalArgumentException(applianceCommon.getMessageProperty("APPL-ERR-1002").replace("{0}", "") + "did");
        } else if (!applianceMaps.domainActive(did)) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1011", "did");
            throw new IllegalArgumentException(applianceCommon.getMessageProperty("APPL-ERR-1011").replace("{0}", "") + did);
        }
        return true;
    }

    public static boolean checkServiceCredentails(String username, String password) {

        if (username == null) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1003", "username");
            throw new NullPointerException(applianceCommon.getMessageProperty("APPL-ERR-1003").replace("{0}", "") + "username");
        } else if (username.trim().length() == 0 || username.trim().equalsIgnoreCase("")) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1002", "username");
            throw new IllegalArgumentException(applianceCommon.getMessageProperty("APPL-ERR-1002").replace("{0}", "") + "username");
        } else if (username.trim().length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.128charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setUsername", "APPL-ERR-1005", "username");
            throw new IllegalArgumentException(applianceCommon.getMessageProperty("APPL-ERR-1005").replace("{0}", "") + "username");
        }

        if (password == null) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1003", "password");
            throw new NullPointerException(applianceCommon.getMessageProperty("APPL-ERR-1003").replace("{0}", "") + "password");
        } else if (password.trim().length() == 0 || password.trim().equalsIgnoreCase("")) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1002", "password");
            throw new IllegalArgumentException(applianceCommon.getMessageProperty("APPL-ERR-1002").replace("{0}", "") + "password");
        } else if (password.trim().length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.64charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setUsername", "APPL-ERR-1005", "password");
            throw new IllegalArgumentException(applianceCommon.getMessageProperty("APPL-ERR-1005").replace("{0}", "") + "password");
        }

        return true;
    }

    public static boolean checkOperation(String operation) {
        if (operation == null) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1003", "operation");
            throw new NullPointerException(applianceCommon.getMessageProperty("APPL-ERR-1003").replace("{0}", "") + "operation");
        } else if (operation.trim().length() == 0 || operation.trim().equalsIgnoreCase("")) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1002", "operation");
            throw new IllegalArgumentException(applianceCommon.getMessageProperty("APPL-ERR-1002").replace("{0}", "") + "operation");
        }
        return true;
    }
}
