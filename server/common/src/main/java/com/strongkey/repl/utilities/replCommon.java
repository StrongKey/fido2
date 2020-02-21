/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.repl.utilities;

import com.strongkey.appliance.entitybeans.Servers;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;
import java.util.logging.Level;

public class replCommon {

    private static final String classname = "replCommon";
    private static final String fs = System.getProperty("file.separator");

    // Property files used by this application for configuration info
    private static final ResourceBundle defaultReplConfig = ResourceBundle.getBundle("resources.repl.replication-configuration");

        // Property files used by this application for application messages
    private static final ResourceBundle msgrb = ResourceBundle.getBundle("resources.repl.replication-messages");

    // Location where Replication is installed on this machine
    private static final String replhome;

    // StrongKey Lite home-directory specific property file
    private static ResourceBundle replhrb = null;

    /**
     * Collection of Subscriber Servers for replication
     */
    private static Collection<Servers> subscribers = null;
    private static Boolean publisher = Boolean.FALSE;   // Implies same for BacklogProcessor
    private static Boolean subscriber = Boolean.FALSE;  // Implies same for Acknowledger

    static {
        /**
         * Print out the values of the central configuration properties built
         * into the application - sort it for readability
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Enumeration<String> enm = defaultReplConfig.getKeys();
        List<String> keys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            keys.add(enm.nextElement());
        }

        Collections.sort(keys);
        Iterator<String> it = keys.iterator();
        try {
            while (it.hasNext()) {
                String key = it.next();
                baos.write(("\n\t" + key + ": ").getBytes());
                if (key.contains("password") || key.contains("secretkey") || key.contains("accesskey")) {
                    baos.write(("**********").getBytes());
                } else {
                    baos.write((defaultReplConfig.getString(key)).getBytes());
                }
            }
            baos.close();
        } catch (IOException ex) {
        }
        strongkeyLogger.log(applianceConstants.REPL_LOGGER, Level.INFO, "REPL-MSG-1052", baos.toString());

        /**
         * Check environment variable for installation location; if not found
         * get default location specified in the configuration properties file.
         *
         * NOTE: Cannot use getConfigurationProperty() method to get this
         * property as it will lead to an ExceptionInitializerError - the method
         * requires replhome to be non-null and if replhome itself tries to use
         * the method, it will be null.
         */
        if ((System.getenv("REPLICATION_HOME")) == null) {
            replhome = defaultReplConfig.getString("replication.cfg.property.replicationhome");
        } else {
            replhome = System.getenv("REPLICATION_HOME");
        }

        strongkeyLogger.log(applianceConstants.REPL_LOGGER, Level.INFO, "REPL-MSG-1053", "REPLICATION_HOME is: " + replhome);

        // See if there is an over-riding properties file in REPLICATION_HOME
        try {
            File f = new File(replhome + fs + "etc" + fs + "replication-configuration.properties");
            try (FileInputStream fis = new FileInputStream(f)) {
                replhrb = new PropertyResourceBundle(fis);
                strongkeyLogger.log(applianceConstants.REPL_LOGGER, Level.INFO, "REPL-MSG-1053", "Using replication-configuration.properties from REPLICATION_HOME directory: " + replhome + "/etc/replication-configuration.properties");
            }

            // Sort properties for readability
            baos = new ByteArrayOutputStream();
            enm = replhrb.getKeys();
            keys = new ArrayList<>();
            while (enm.hasMoreElements()) {
                keys.add(enm.nextElement());
            }

            Collections.sort(keys);
            it = keys.iterator();

            while (it.hasNext()) {
                String key = it.next();
                baos.write(("\n\t" + key + ": ").getBytes());
                if (key.contains("password") || key.contains("secretkey") || key.contains("accesskey")) {
                    baos.write(("**********").getBytes());
                } else {
                    baos.write(replhrb.getString(key).getBytes());
                }

            }
            baos.close();

        } catch (FileNotFoundException ex) {
            strongkeyLogger.log(applianceConstants.REPL_LOGGER, Level.WARNING, "REPL-MSG-1053", "There is no replication-configuration.properties in the "
                    + "REPLICATION_HOME directory; using system-wide replication-configuration.properties");
        } catch (IOException ex) {
        }

        // Print out local configuration values from REPLICATION_HOME
        strongkeyLogger.log(applianceConstants.REPL_LOGGER, Level.INFO, "REPL-MSG-1054", baos.toString());
    }

    public replCommon() {

    }

    /*************************************************************
                      888
                      888          o
                      888         d8b
    .d8888b   .d88b.  888888     d888b
    88K      d8P  Y8b 888    "Y888888888P"
    "Y8888b. 88888888 888      "Y88888P"
         X88 Y8b.     Y88b.    d88P"Y88b
     88888P'  "Y8888   "Y888  dP"     "Yb
     *
     *************************************************************/

     /**
     * Set this server as a replication Publisher
     * @param state The new state of Publisher
     */
    public static void setPublisher(Boolean state)
    {
        publisher = state;
    }

     /**
     * Set this server as a replication Subscriber
     * @param state The new state of Subscriber
     */
    public static void setSubscriber(Boolean state)
    {
        subscriber = state;
    }

    /*************************************************************
     d8b
     Y8P                o
                       d8b
     888 .d8888b      d888b
     888 88K      "Y888888888P"
     888 "Y8888b.   "Y88888P"
     888      X88   d88P"Y88b
     888  88888P'  dP"     "Yb
     *
     *************************************************************/

     /**
     * Check if this server is a replication Publisher
     * @return boolean
     */
    public static boolean isPublisher()
    {
        return publisher;
    }

     /** Check if this server is a replication Subscriber
     * @return boolean
     */
    public static boolean isSubscriber()
    {
        return subscriber;
    }

    /**
     * Gets the location where the Replication software is installed on this server.
     * @return String File-system location where Cryptolib is installed
     */
    public static String getReplicationHome()
    {
        return replhome;
    }

    /**
     * Make a Collection of Subscribed Servers available to application
     *
     * @param servers Collection&gt;Servers&lt; A collection of servers that does
     * NOT include the local server (since it cannot be a Subscriber to its
     * own messages)
     */
    public static void putSubscribers(Collection<Servers> servers)
    {
        subscribers = servers;
        subscribers.stream().forEach((s) -> {
            strongkeyLogger.logp(applianceConstants.REPL_LOGGER, Level.INFO, classname, "constructor", "REPL-MSG-4004", s.getFqdn());
        });
    }

     /**
     * Get a Collection of Subscribed Servers available to application
     *
     * @return A collection of servers that does NOT include the local server
     * (since it cannot be a Subscriber to its own messages)
     */
    public static Collection<Servers> getSubscribers()
    {
        return subscribers;
    }

     /**
     * Get the module which this entity type belongs to
     * @param entityname - Constants name of the entity
     * @return int - Constants representation of the module
     */
    public static int getEntityModule(int entityname) {
        if (entityname >= 0 && entityname < applianceConstants.ENTITY_TYPE_SAKA_UPPER_LIMIT) {
            return applianceConstants.MODULE_TYPE_SAKA;
        } else if (entityname > applianceConstants.ENTITY_TYPE_SKCE_LOWER_LIMIT && entityname < applianceConstants.ENTITY_TYPE_SKCE_UPPER_LIMIT) {
            return applianceConstants.MODULE_TYPE_SKCE;
        } else if (entityname > applianceConstants.ENTITY_TYPE_CDO_LOWER_LIMIT && entityname < applianceConstants.ENTITY_TYPE_CDO_UPPER_LIMIT) {
            return applianceConstants.MODULE_TYPE_CDO;
        } else if (entityname > applianceConstants.ENTITY_TYPE_FSO_LOWER_LIMIT && entityname < applianceConstants.ENTITY_TYPE_FSO_UPPER_LIMIT) {
            return applianceConstants.MODULE_TYPE_FSO;
        } else {
            return applianceConstants.MODULE_TYPE_UNKNOWN;
        }
    }

    /*
    ************************************************************************
 .d8888b.                     .d888 d8b                                    888    d8b
d88P  Y88b                   d88P"  Y8P                                    888    Y8P
888    888                   888                                           888
888         .d88b.  88888b.  888888 888  .d88b.  888  888 888d888  8888b.  888888 888  .d88b.  88888b.  .d8888b
888        d88""88b 888 "88b 888    888 d88P"88b 888  888 888P"       "88b 888    888 d88""88b 888 "88b 88K
888    888 888  888 888  888 888    888 888  888 888  888 888     .d888888 888    888 888  888 888  888 "Y8888b.
Y88b  d88P Y88..88P 888  888 888    888 Y88b 888 Y88b 888 888     888  888 Y88b.  888 Y88..88P 888  888      X88
 "Y8888P"   "Y88P"  888  888 888    888  "Y88888  "Y88888 888     "Y888888  "Y888 888  "Y88P"  888  888  88888P'
                                             888
                                        Y8b d88P
                                         "Y88P"
    ************************************************************************
    */
    /**
     * Gets the value of the property with the specified key from either
     * the REPLICATION_HOME home-directory - if the property file exists
     * there - or from the system-wide properties file.
     *
     * Additionally, if property-value has the $REPLICATION_HOME variable
     * embedded in it, then it replaces the variable with the actual value
     * of REPLICATION_HOME in the property-value and returns it.
     *
     * @param key - The key in the resource file
     * @return String - The value of the specified key from the resource file
     */
    public static String getConfigurationProperty(String key)
    {
        if (replhrb != null) {
            try {
                String s = replhrb.getString(key);
                if (s.startsWith("REPLICATION_HOME"))
                    return s.replaceFirst("REPLICATION_HOME", replhome);
                else
                    return s;
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }

        String s = defaultReplConfig.getString(key);
        if (s.startsWith("REPLICATION_HOME"))
            return s.replaceFirst("REPLICATION_HOME", replhome);
        else
            return s;
    }

    /**
     * Gets all the default properties for StrongKey Lite regardless of
     * customizations made by a site.  This is useful for the admin tool
     * to compare default values with current values or for resetting a
     * customized property to the default value.
     *
     * @return ResourceBundle - An object with SKLITE's default properties
     */
    public static ResourceBundle getDefaultConfiguration()
    {
        return defaultReplConfig;
    }

    /**
     * Gets the default configuration for this machine, overlaid with
     * the customized configuration in REPLICATIONHOME to create a
     * machine-specific system configuration to be returned to caller.
     *
     * @return Properties
     */
    public static Properties getSystemConfiguration()
    {
        // Create object to return
        Properties props = new Properties();

        // Sort out the default configuration
        Enumeration<String> enm = defaultReplConfig.getKeys();
        List<String> keys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            keys.add(enm.nextElement());
        }

        Collections.sort(keys);
        Iterator<String> it = keys.iterator();
        while (it.hasNext()) {
            String key = it.next();
            props.setProperty(key, defaultReplConfig.getString(key));
        }

        // Update properties object with customized configuration
        // from REPLICATION_HOME (if anything was customized)
        enm = replhrb.getKeys();
        while (enm.hasMoreElements()) {
            String key = enm.nextElement();
            props.setProperty(key, replhrb.getString(key));
        }

        // Return the system-specific properties
        return props;
    }


        /*
    ************************************************************************
888b     d888
8888b   d8888
88888b.d88888
888Y88888P888  .d88b.  .d8888b  .d8888b   8888b.   .d88b.   .d88b.  .d8888b
888 Y888P 888 d8P  Y8b 88K      88K          "88b d88P"88b d8P  Y8b 88K
888  Y8P  888 88888888 "Y8888b. "Y8888b. .d888888 888  888 88888888 "Y8888b.
888   "   888 Y8b.          X88      X88 888  888 Y88b 888 Y8b.          X88
888       888  "Y8888   88888P'  88888P' "Y888888  "Y88888  "Y8888   88888P'
                                                       888
                                                  Y8b d88P
                                                   "Y88P"
    ************************************************************************
    */

    /**
     * Gets the value of the message property with the specified key
     * @param key - The key in the message resource file
     * @return String - The value of the specified key from the resource file
     */
    public static String getMessageProperty(String key)
    {
        return msgrb.getString(key);
    }

}
