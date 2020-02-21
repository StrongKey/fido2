/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */

package com.strongkey.utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

// Load configurations from property files
public class Configurations {

    private static final ResourceBundle DEFAULTCONFIGS = ResourceBundle.getBundle("resources.webauthntutorial-configuration");
    private static ResourceBundle customConfigs = null;
    private static final String CLASSNAME = Configurations.class.getName();

    // Load customizedConfigs from the filesystem if they exist
    static{
        try {
            logConfigurations(DEFAULTCONFIGS);
            File customConfigFile = new File(DEFAULTCONFIGS.getString("webauthntutorial.cfg.property.configlocation"));
            if(customConfigFile.exists() && customConfigFile.isFile()){
                customConfigs = new PropertyResourceBundle(new FileInputStream(customConfigFile));
                logConfigurations(customConfigs);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static String getConfigurationProperty(String configName){
        try{
            if (customConfigs != null && customConfigs.containsKey(configName)) {
                return customConfigs.getString(configName);
            }
            return DEFAULTCONFIGS.getString(configName);
        }
        catch(MissingResourceException ex){
            return null;
        }
    }

    // Debug function (prints set configurations to log)
    private static void logConfigurations(ResourceBundle configs){
        if(configs == null){
            return;
        }

        StringBuilder configString = new StringBuilder();
        for(String key: configs.keySet()){
            if(!key.contains("secretkey")){
                configString.append("\n\t")
                        .append(key)
                        .append(": ")
                        .append(configs.getString(key));
            }
        }

        WebauthnTutorialLogger.logp(Level.FINE, CLASSNAME, "logConfigurations", "WEBAUTHN-MSG-1000", configString.toString());
    }
}
