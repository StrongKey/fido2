/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.crypto.interfaces;

import com.strongkey.crypto.bcfips.GenericCryptoModule;
import com.strongkey.crypto.utility.cryptoCommon;
import java.util.logging.Level;

public class initCryptoModule
{
    /**
     ** This class's name - used for logging and file-separator
     **/
    private static final String classname  = "initCryptoModule";

    private static GenericCryptoModule gcm = null;

    public static GenericCryptoModule getCryptoModule()
    {
        cryptoCommon.entering(classname, "initCryptoModule");

        String moduletype = cryptoCommon.getConfigurationProperty("crypto.cfg.property.cryptomodule.type");
        String modulevendor = cryptoCommon.getConfigurationProperty("crypto.cfg.property.cryptomodule.vendor");
        cryptoCommon.logp(Level.FINE, classname, "getCryptoModule", "CRYPTO-MSG-1019", moduletype + " [vendor=" + modulevendor + "]");

        if (gcm == null) {
                gcm = new GenericCryptoModule(null);
        }
        return gcm;
    }

    public static void setCryptoModule(GenericCryptoModule newgcm) {
        gcm = newgcm;
    }
}
