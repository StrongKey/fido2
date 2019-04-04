/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skce.utilities;

import com.strongkey.appliance.utilities.applianceCommon;
import java.net.URL;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
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

    //  Well known number to be used to test SAKA encrypt & decrypt
    private static final String PAN = "1235711131719230";

    // Default file extension for SKCE-encrypted files
    public static final String ENC_FILE_EXT = ".zenc";

    // StrongKey  home-directory specific property file
    private static ResourceBundle skcehrb = null;

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

    public static SecureRandom securerandom;

    private static String SERVICE_OU_PREFIX;
    private static String SEARCH_OU_PREFIX;

    static {
        setupOUPrefix();
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
    public static String getSERVICE_OU_PREFIX() {
        return SERVICE_OU_PREFIX;
    }

    public static String getSEARCH_OU_PREFIX() {
        return SEARCH_OU_PREFIX;
    }

    public static void setupOUPrefix() {
        if (applianceCommon.getApplianceConfigurationProperty("appl.cfg.property.service.ce.ldap.ldaptype").equalsIgnoreCase("LDAP")) {
            SERVICE_OU_PREFIX = ",did=";
            SEARCH_OU_PREFIX = ",did=";
        } else {
            SERVICE_OU_PREFIX = ",ou=";
            SEARCH_OU_PREFIX = ",ou=";
        }

    }
//
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
