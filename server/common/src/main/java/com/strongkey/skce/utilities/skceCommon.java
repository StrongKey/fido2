/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.utilities;

import com.strongkey.appliance.entitybeans.Configurations;
import com.strongkey.appliance.utilities.strongkeyLogger;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;

public class skceCommon {

    private static final String classname = "skceCommon";
    public static final String fs = System.getProperty("file.separator");

    // Property files used by this application for configuration info
    private static final ResourceBundle defaultSKCEConfig = ResourceBundle.getBundle("resources.skce.skce-configuration");

    // Property files used by this application for application messages
    private static final ResourceBundle msgrb = ResourceBundle.getBundle("resources.skce.skce-messages");

    // Default file extension for SKCE-encrypted files
    public static final String ENC_FILE_EXT = ".zenc";

    // StrongKey  home-directory specific property file
    private static ResourceBundle skcehrb = null;
    
    private static SortedMap<Long, Map> skceconfigmap = new ConcurrentSkipListMap<>();
    private static SortedMap<Long, String> ldaptypemap = new ConcurrentSkipListMap<>();

    // Location where StrongKey CryproEngine  is installed on this machine
    private static String skcehome;

    public static List<String> tldList = new ArrayList<>();

    /**
     * The Cron object has the actual tasks executed by the background threads;
     * and the map of Cron objects to keep track of so that they don't keep
     * running forever if there are no objects to delete.
     */
    public static final Cron cron = new Cron();            //  Not being used anymore

    /**
     * Date-time when default encryption keys are generated for each SAKA
     * domain. The keys are regenerated periodically (daily/weekly/monthly)
     * based on the initial generation date-time
     */
    public static Date DEFAULT_KEYS_GENERATION_DATE = null;

    //  flag that indicates whether the default enc keys are being generated for the first time or not.
    public static Boolean DEFAULTKEYS_GENERATION_FIRSTTIME = true;
    
    private static Boolean DNSUFFIX_CONFIGURE_PER_DOMAIN = false;
    private static Boolean GROUPSUFFIX_CONFIGURE_PER_DOMAIN = false;
    

    static {
        
        /**
         * Print out the values of the central configuration properties built
         * into the application - sort it for readability
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Enumeration<String> enm = defaultSKCEConfig.getKeys();
        List<String> keys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            keys.add(enm.nextElement());
        }

        Collections.sort((List<String>) keys);
        Iterator it = keys.iterator();
        try {
            while (it.hasNext()) {
                String key = (String) it.next();
                baos.write(("\n\t" + key + ": ").getBytes());
                if (key.contains("password") || key.contains("secretkey") || key.contains("accesskey")) {
                    baos.write(("**********").getBytes());
                } else {
                    baos.write((defaultSKCEConfig.getString(key)).getBytes());
                }
            }
            baos.close();
        } catch (IOException ex) {
            strongkeyLogger.printStrongAuthStackTrace(skceConstants.SKEE_LOGGER, classname, "init()", ex);
        }
        strongkeyLogger.log(skceConstants.SKEE_LOGGER, Level.INFO, "SKCE-MSG-1052", baos.toString());

        /**
         * Check environment variable for installation location; if not found
         * get default location specified in the configuration properties file.
         *
         * NOTE: Cannot use getConfigurationProperty() method to get this
         * property as it will lead to an ExceptionInitializerError - the method
         * requires skcehome to be non-null and if skcehome itself tries to use
         * the method, it will be null.
         */
        if ((skcehome = System.getenv("SKCE_HOME")) == null) {
            skcehome = defaultSKCEConfig.getString("skce.cfg.property.skcehome");
        }

        strongkeyLogger.log(skceConstants.SKEE_LOGGER, Level.INFO, "SKCE-MSG-1053", "SKCE_HOME is: " + skcehome);

        // See if there is an over-riding properties file in SKCE_HOME
        try {
            File f = new File(skcehome + fs + "etc" + fs + "skce-configuration.properties");
            /**
             * Using try-with-resources; which will take care of closing the
             * FileInputStream fis in any case (success or failure)
             */
            try (FileInputStream fis = new FileInputStream(f)) {
                skcehrb = new java.util.PropertyResourceBundle(fis);
            }

            strongkeyLogger.log(skceConstants.SKEE_LOGGER, Level.INFO, "SKCE-MSG-1053",
                    "Using skce-configuration.properties from SKCE_HOME directory: "
                    + skcehome + "/etc/skce-configuration.properties");

            // Sort properties for readability
            baos = new ByteArrayOutputStream();
            enm = skcehrb.getKeys();
            keys = new ArrayList<>();
            while (enm.hasMoreElements()) {
                keys.add(enm.nextElement());
            }

            Collections.sort((List<String>) keys);
            it = keys.iterator();

            while (it.hasNext()) {
                String key = (String) it.next();
                baos.write(("\n\t" + key + ": ").getBytes());
                if (key.contains("password") || key.contains("secretkey") || key.contains("accesskey")) {
                    baos.write(("**********").getBytes());
                } else {
                    baos.write((skcehrb.getString(key)).getBytes());
                }
            }
            baos.close();

        } catch (java.io.FileNotFoundException ex) {
            strongkeyLogger.log(skceConstants.SKEE_LOGGER, Level.WARNING, "SKCE-MSG-1053", "There is no skce-configuration.properties in the "
                    + "SKCE_HOME directory; using system-wide skce-configuration.properties");
        } catch (IOException ex) {
            strongkeyLogger.printStrongAuthStackTrace(skceConstants.SKEE_LOGGER, classname, "init()", ex);
        }
        // Print out local configuration values from SKCE_HOME
        strongkeyLogger.log(skceConstants.SKEE_LOGGER, Level.INFO, "SKCE-MSG-1054", baos.toString());

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
     * Gets the value of the property for the specified domain with the
     * specified key from either the Configuration map or the default Properties
     * object (if not found in the configuration map).
     *
     * @param did - Long value of the domain ID
     * @param k - The key in the configuration property map
     * @return String - The value of the specified key
     */
    public static String getConfigurationProperty(Long did, String k) {
        strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1056", did + "-" + k);

        // First check for the domain in the configmap
        if (skceconfigmap.containsKey(did)) {
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1057", did);
            Map m = skceconfigmap.get(did);
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1058", k + " [DID=" + did + "]");
            if (m != null) {
                if (m.containsKey(k)) {
                    return (String) m.get(k);
                }
            }
        }

        // Default - in case returned map and DB have no value with the key k
        strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1059", k + " [DID=" + did + ", KEY=" + k + ", VALUE=" + getConfigurationProperty(k) + "]");
        return getConfigurationProperty(k);
    }
    
    /**
     * Gets the value of the property with the specified key from either the
     * SKCE_HOME home-directory - if the property file exists there - or from
     * the system-wide properties file.
     *
     * Additionally, if property-value has the $SKCE_HOME variable embedded in
     * it, then it replaces the variable with the actual value of SKCE_HOME in
     * the property-value and returns it.
     *
     * @param key - The key in the resource file
     * @return String - The value of the specified key from the resource file
     */
    public static String getConfigurationProperty(String key) {
              if (skcehrb != null) {
            try {
                String s = skcehrb.getString(key);
                if (s.startsWith("SKCE_HOME")) {
                    return s.replaceFirst("SKCE_HOME", skcehome);
                } else {
                    return s;
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }

        String s = defaultSKCEConfig.getString(key);
        if (s.startsWith("SKCE_HOME")) {
            return s.replaceFirst("SKCE_HOME", skcehome);
        } else {
            return s;
        }

    }
    
    public static boolean isConfigurationMapped(Long did)
    {
        return skceconfigmap.containsKey(did);
    }
    
    /**
     * Puts a customized configuration map of properties for the specified
     * domain into the Configuration map.
     *
     * @param did - Long value of the domain ID.  This value serves as the
     * key in the configmap object (which points to the configuration Map )
     * @param cfgarray - The array of configuration objects that need to be
     * put into a domain-specific map and then loaded into the system-wide
     * configuration map.
     */
    public static void putConfiguration(Long did, Configurations[] cfgarray) {
        // First convert the array into a map
        SortedMap<String, String> newconfigs = new TreeMap<>();
        for (Configurations c : cfgarray) {
            if (c.getConfigurationsPK().getConfigKey().startsWith("ldape") || c.getConfigurationsPK().getConfigKey().startsWith("skce")) {
                newconfigs.put(c.getConfigurationsPK().getConfigKey(), c.getConfigValue());
            }
        }

        // If the map already exists, need to add its unique values to the map
        if (skceconfigmap.containsKey(did)) {
            Map<String, String> currentconfigs = skceconfigmap.get(did);

            Set<String> newkeys = newconfigs.keySet();
            for (String key : newkeys) {
                currentconfigs.put(key, newconfigs.get(key));
            }
        } else {
            skceconfigmap.put(did, newconfigs);
        }
    }

    public static void removeConfiguration(Long did){
        skceconfigmap.remove(did);
    }
    
    public static void removeConfiguration(Long did, Configurations[] cfgarray) {

        SortedMap<String, String> newconfigs = new TreeMap<>();
        for (Configurations c : cfgarray) {
            if (c.getConfigurationsPK().getConfigKey().startsWith("skfs")) {
                newconfigs.put(c.getConfigurationsPK().getConfigKey(), c.getConfigValue());
            }
        }

        if (skceconfigmap.containsKey(did)) {
            Map<String, String> currentconfigs = skceconfigmap.get(did);
            Set<String> newkeys = newconfigs.keySet();
            for (String key : newkeys) {
                currentconfigs.remove(key);
            }
        }
    }
    
    public static void setldaptype(Long did, String ldaptype){
        ldaptypemap.put(did, ldaptype);
    }
    public static String getldaptype(Long did){
        return ldaptypemap.get(did);
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
     *
     * @param key - The key in the message resource file
     * @return String - The value of the specified key from the resource file
     */
    public static String getMessageProperty(String key) {
        return msgrb.getString(key);
    }

    /*
    ***********************************************************************
888     888 888    d8b 888 d8b 888    d8b
888     888 888    Y8P 888 Y8P 888    Y8P
888     888 888        888     888
888     888 888888 888 888 888 888888 888  .d88b.  .d8888b
888     888 888    888 888 888 888    888 d8P  Y8b 88K
888     888 888    888 888 888 888    888 88888888 "Y8888b.
Y88b. .d88P Y88b.  888 888 888 Y88b.  888 Y8b.          X88
 "Y88888P"   "Y888 888 888 888  "Y888 888  "Y8888   88888P'
    ***********************************************************************
     */
    /**
     * This method looks through the list of SAKA host urls configured for a
     * specific SAKA cluster and *RAMDONLY* picks one of them and then acts
     * based on the retrial property.
     *
     * If retrial = true, it first verifies if the picked url works or not; if
     * it works, it returns the url and if not, it does retry for another in the
     * set of urls until it finds one or all are proven non-serviceable.
     *
     * If retrial = false, it returns the first picked url without even checking
     * its serviceability.
     *
     * The look-up for the list of possible host urls is the configuration file.
     *
     * @param clusterid SAKA cluster id
     * @param domainid SAKA domain id
     * @return the url of a SAKA server; null if no url is specified for the
     * cluster in the properties file.
     */
    public static String getWorkingHostURLInCluster(Long clusterid, Long domainid) {
        return null;
    }
    /**
     * Gets a specific property for the saka cluster-domain. The properties are
     * limited to 'username' or 'password' for this release (SKCE V1.0 B19);
     * More properties might get added in future releases like domain specific
     * key specification etc.,
     *
     * @param clusterid saka cluster id
     * @param domainid saka domain id
     * @param propertyname property name to be fetched (username | password)
     * @return the value of the property for that saka cluster-domain. null if
     * not found.
     */
    public static String getClusterDomainProperty(Long clusterid, Long domainid, String propertyname) {
        return "";
    }
    /**
     * Checks to see if the specified java.net.URL is accessible or not. The
     * check is done based on the response code sent back when tried to open an
     * HTTPUrlConnection on it.
     *
     * @param url - java.net.URL object
     * @return return a boolean flag to indicate if the url has been accessible
     * or not
     */
    public static boolean isURLAccessible(URL url) {
            return true;
    }

    /*
    ***********************************************************************
888      8888888b.         d8888 8888888b.  8888888888
888      888  "Y88b       d88888 888   Y88b 888
888      888    888      d88P888 888    888 888
888      888    888     d88P 888 888   d88P 8888888
888      888    888    d88P  888 8888888P"  888
888      888    888   d88P   888 888        888
888      888  .d88P  d8888888888 888        888
88888888 8888888P"  d88P     888 888        8888888888
    ***********************************************************************
     */
//
    
    public static void setdnSuffixConfigured(Boolean input){
        DNSUFFIX_CONFIGURE_PER_DOMAIN = input;
    }
    
    public static Boolean isdnSuffixConfigured(){
        return DNSUFFIX_CONFIGURE_PER_DOMAIN;
    }
    
    public static void setgroupSuffixConfigured(Boolean input){
        GROUPSUFFIX_CONFIGURE_PER_DOMAIN = input;
    }
    
    public static Boolean isgroupSuffixConfigured(){
        return GROUPSUFFIX_CONFIGURE_PER_DOMAIN;
    }
    
    // LDAP related look-ups
    public static String lookupGroupCN(String groupDN) {
        return "";
    }

    /**
     *
     * @param url
     * @param binduser
     * @param password
     * @return
     * @throws NamingException
     */
    public static DirContext getInitiallookupContext(String module, String url, String binduser, String password)
            throws NamingException {

        String INITIAL_CONTEXT_FACTORY = "com.sun.jndi.ldap.LdapCtxFactory";
        String SECURITY_AUTHENTICATION = "simple";

        Properties props = new Properties();
        props.put(Context.INITIAL_CONTEXT_FACTORY, INITIAL_CONTEXT_FACTORY
        );
        props.put(Context.PROVIDER_URL, url);

        props.put("com.sun.jndi.ldap.connect.pool", "true");

        if ((binduser != null) && (!binduser.equals(""))) {
            props.put(Context.SECURITY_AUTHENTICATION, SECURITY_AUTHENTICATION);
            props.put(Context.SECURITY_PRINCIPAL, binduser);
            props.put(Context.SECURITY_CREDENTIALS,
                    ((password == null) ? "" : password));
        }

        return new InitialDirContext(props);
    }
}
