/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.utilities;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.skce.utilities.TPMConstants;
import com.strongkey.skfs.pojos.FIDOReturnObject;
import com.strongkey.skfs.pojos.FIDOReturnObjectV1;
import com.strongkey.skfs.requests.ServiceInfo;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.Security;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

public class skfsCommon {

    private static final String classname = "skfsCommon";
    public static final String fs = System.getProperty("file.separator");

    // Property files used by this application for configuration info
    private static final ResourceBundle defaultSKFEConfig = ResourceBundle.getBundle("resources.skfs-configuration");

    // Property files used by this application for application messages
    private static final ResourceBundle msgrb = ResourceBundle.getBundle("resources.skfs-messages");

    // StrongKey  home-directory specific property file
    private static ResourceBundle skcehrb = null;

    // Location where StrongKey CryproEngine  is installed on this machine
    private static String skfshome;

    public static List<String> tldList = new ArrayList<>();

    public static final skfsCron cron = new skfsCron();

    private static SortedMap<Long, Map> skfsconfigmap = new ConcurrentSkipListMap<>();

    private static SortedMap<Integer, JsonArray> transport_combinations = new ConcurrentSkipListMap<>();

    static {

        /**
         * Print out the values of the central configuration properties built
         * into the application - sort it for readability
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Enumeration<String> enm = defaultSKFEConfig.getKeys();
        List<String> keys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            keys.add(enm.nextElement());
        }

        Collections.sort((List<String>) keys);
        Iterator it = keys.iterator();
        try {
            while (it.hasNext()) {
                String key = (String) it.next();
                baos.write(("\n\t" + key + ": " + defaultSKFEConfig.getString(key)).getBytes());
            }
            baos.close();
        } catch (IOException ex) {
            skfsLogger.printStrongAuthStackTrace(skfsConstants.SKFE_LOGGER, classname, "init()", ex);
        }
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.INFO, "SKCE-MSG-1052", baos.toString());

        /**
         * Check environment variable for installation location; if not found
         * get default location specified in the configuration properties file.
         *
         * NOTE: Cannot use getConfigurationProperty() method to get this
         * property as it will lead to an ExceptionInitializerError - the method
         * requires skfshome to be non-null and if skfshome itself tries to use
         * the method, it will be null.
         */
        if ((skfshome = System.getenv("SKFS_HOME")) == null) {
            skfshome = defaultSKFEConfig.getString("skfs.cfg.property.skfshome");
        }

        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.INFO, "SKCE-MSG-1053", "SKFS_HOME is: " + skfshome);

        // See if there is an over-riding properties file in SKFS_HOME
        try {
            File f = new File(skfshome + fs + "etc" + fs + "skfs-configuration.properties");
            /**
             * Using try-with-resources; which will take care of closing the
             * FileInputStream fis in any case (success or failure)
             */
            try (FileInputStream fis = new FileInputStream(f)) {
                skcehrb = new java.util.PropertyResourceBundle(fis);
            }

            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.INFO, "SKCE-MSG-1053",
                    "Using skfs-configuration.properties from SKFS_HOME directory: "
                    + skfshome + "/etc/skfs-configuration.properties");

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
                baos.write(("\n\t" + key + ": " + skcehrb.getString(key)).getBytes());
            }
            baos.close();

        } catch (java.io.FileNotFoundException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.WARNING, "SKCE-MSG-1053", "There is no skfs-configuration.properties in the "
                    + "SKFS_HOME directory; using system-wide skfs-configuration.properties");
        } catch (IOException ex) {
            skfsLogger.printStrongAuthStackTrace(skfsConstants.SKFE_LOGGER, classname, "init()", ex);
        }
        // Print out local configuration values from SKFS_HOME
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.INFO, "SKCE-MSG-1054", baos.toString());

        if (skfsCommon.getConfigurationProperty("skfs.cfg.property.retrieve.tld").trim().equalsIgnoreCase("true")) {
            // tld
            HttpClient client = new HttpClient();

            // Create a method instance.
            GetMethod method = new GetMethod("https://publicsuffix.org/list/effective_tld_names.dat");

            // Provide custom retry handler is necessary
            method.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
                    new DefaultHttpMethodRetryHandler(3, false));

            try {
                // Execute the method.
                int statusCode = client.executeMethod(method);

                if (statusCode != HttpStatus.SC_OK) {
                    System.err.println("Method failed: " + method.getStatusLine());
                }

                // Deal with the response.
                // Use caution: ensure correct character encoding and is not binary data
                BufferedReader in = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
                String inputLine;
                while ((inputLine = in.readLine()) != null) {
                    if (!inputLine.startsWith("//") && !inputLine.isEmpty()) {
                        tldList.add(inputLine);
                    }
                }

            } catch (HttpException e) {
                System.err.println("Fatal protocol violation: " + e.getMessage());
                e.printStackTrace();
            } catch (IOException e) {
                System.err.println("Fatal transport error: " + e.getMessage());
                e.printStackTrace();
            } finally {
                // Release the connection.
                method.releaseConnection();
            }
        }

        Security.addProvider(new BouncyCastleFipsProvider());
        cron.flushUserSessionsJob();

        cron.flushFIDOKeysJob();

        putTransportsMap();
    }

    /**
     * Gets the location where the SKCE software is installed on this server.
     *
     * @return String File-system location where Cryptolib is installed
     */
    public static String getSkfeHome() {
        return skfshome;
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
     * SKFS_HOME home-directory - if the property file exists there - or from
     * the system-wide properties file.
     *
     * Additionally, if property-value has the $SKFS_HOME variable embedded in
     * it, then it replaces the variable with the actual value of SKFS_HOME in
     * the property-value and returns it.
     *
     * @param key - The key in the resource file
     * @return String - The value of the specified key from the resource file
     */
    public static String getConfigurationProperty(String key) {
        if (skcehrb != null) {
            try {
                String s = skcehrb.getString(key);
                if (s.startsWith("SKFS_HOME")) {
                    return s.replaceFirst("SKFS_HOME", skfshome);
                } else {
                    return s;
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }

        String s = defaultSKFEConfig.getString(key);
        if (s.startsWith("SKFS_HOME")) {
            return s.replaceFirst("SKFS_HOME", skfshome);
        } else {
            return s;
        }
    }

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
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1056", did + "-" + k);

        // First check for the domain in the configmap
        if (skfsconfigmap.containsKey(did)) {
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1057", did);
            Map m = skfsconfigmap.get(did);
            skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1058", k + " [DID=" + did + "]");
            if (m != null) {
                if (m.containsKey(k)) {
                    return (String) m.get(k);
                }
            }
        }

        // Default - in case returned map and DB have no value with the key k
        skfsLogger.logp(skfsConstants.SKFE_LOGGER, Level.FINE, classname, "getConfigurationProperty", "SKCE-MSG-1059", k + " [DID=" + did + ", KEY=" + k + ", VALUE=" + getConfigurationProperty(k) + "]");
        return getConfigurationProperty(k);
    }

    /**
     * Gets all the default properties for StrongKey regardless of
     * customizations made by a site. This is useful for the admin tool to
     * compare default values with current values or for resetting a customized
     * property to the default value.
     *
     * @return ResourceBundle - An object with SKCEITE's default properties
     */
    public static ResourceBundle getDefaultConfiguration() {
        return defaultSKFEConfig;
    }

    /**
     * Gets the default configuration for this machine, over-laid with the
     * customized configuration in SKFS_HOME to create a machine-specific system
     * configuration to be returned to caller.
     *
     * @return Properties object with all the list of properties from default
     * configuration.
     */
    public static Properties getSystemConfiguration() {
        // Create object to return
        Properties props = new Properties();

        // Sort out the default configuration
        Enumeration<String> enm = defaultSKFEConfig.getKeys();
        List<String> keys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            keys.add(enm.nextElement());
        }

        Collections.sort((List<String>) keys);
        keys.stream().forEach((key) -> {
            props.setProperty(key, defaultSKFEConfig.getString(key));
        });

        // Update properties object with customized configuration
        // from SKFS_HOME (if anything was customized)
        enm = skcehrb.getKeys();
        while (enm.hasMoreElements()) {
            String key = enm.nextElement();
            props.setProperty(key, skcehrb.getString(key));
        }

        // Return the system-specific properties
        return props;
    }

    /**
     * Reloads the configuration properties
     *
     * @return
     */
    public static boolean reloadConfiguration() {
        ResourceBundle rb;
        try {
            File f = new File(skfshome + fs + "etc" + fs + "skfs-configuration.properties");
            try (FileInputStream fis = new FileInputStream(f)) {
                rb = new java.util.PropertyResourceBundle(fis);
            }

            if (rb != null) {
                skcehrb = rb;

                // Sort properties for readability
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Enumeration enm = skcehrb.getKeys();
                List<String> keys = new ArrayList<>();
                while (enm.hasMoreElements()) {
                    keys.add((String) enm.nextElement());
                }

                // Print out local configuration values from SKFS_HOME
                Collections.sort((List<String>) keys);
                for (String key : keys) {
                    baos.write(("\n\t" + key + ": " + skcehrb.getString(key)).getBytes());
                }

                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.INFO, "SKCE-MSG-1110", baos.toString());
                return true;
            } else {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.WARNING, "SKCE-ERR-1112", f.getName());
            }
        } catch (IOException ex) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.WARNING, "SKCE-ERR-1112", null);
            Logger.getLogger(classname).log(Level.SEVERE, null, ex);
        }
        return false;
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

    public static JsonArray getTransportJson(Integer entity) {
        return transport_combinations.get(entity);
    }

    public static void putTransportsMap() {
        JsonArrayBuilder jab = Json.createArrayBuilder();
        jab.add("ble").add("internal").add("nfc").add("usb").build();
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_NONE, jab.build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_NFC, jab.add("nfc").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_USB, jab.add("usb").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_BLE, jab.add("ble").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL, jab.add("internal").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_USB_NFC, jab.add("usb").add("nfc").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_BLE_NFC, jab.add("ble").add("nfc").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL_NFC, jab.add("internal").add("nfc").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_BLE_USB, jab.add("ble").add("usb").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL_USB, jab.add("internal").add("usb").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL_BLE, jab.add("internal").add("ble").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_BLE_USB_NFC, jab.add("ble").add("usb").add("nfc").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL_BLE_NFC, jab.add("internal").add("ble").add("nfc").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL_USB_NFC, jab.add("internal").add("usb").add("nfc").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL_BLE_USB, jab.add("internal").add("ble").add("usb").build());
        transport_combinations.put(skfsConstants.FIDO_TRANSPORT_INTERNAL_BLE_USB_NFC, jab.add("internal").add("ble").add("usb").add("nfc").build());
    }

    /**
     * Does input validation for skce domain id.
     *
     * @param skcedid String containing skce did to be validated
     * @throws SKFEException
     */
    public static void inputValidateSKCEDid(String skcedid) throws SKFEException {
        Long did = 0L;
        try {
            did = Long.parseLong(skcedid);
        } catch (NumberFormatException nfe) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCE-ERR-1003", " skce did= " + skcedid);
            throw new SKFEException(skfsCommon.getMessageProperty("SKCE-ERR-1003") + " skce did= " + skcedid);
        }

        if (did <= 0 || did > skfsConstants.DID_MAX) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCE-ERR-1003", " skce did= " + did);
            throw new SKFEException(skfsCommon.getMessageProperty("SKCE-ERR-1003") + " skce did= " + did);
        } else if (!applianceMaps.isDomainMapped(did)) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCE-ERR-1092", " skce did= " + did);
            throw new SKFEException(skfsCommon.getMessageProperty("SKCE-ERR-1092") + " skce did= " + did);
        } else if (!applianceMaps.domainActive(did)) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "SKCE-ERR-1093", " skce did= " + did);
            throw new SKFEException(skfsCommon.getMessageProperty("SKCE-ERR-1093")
                    + " skce did= " + did);
        } else if (applianceCommon.isCCSPinEnabled(did)) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.INFO, skfsCommon.getMessageProperty("SKCE-MSG-1130"), " skce did= " + did);
            throw new SKFEException(skfsCommon.getMessageProperty("SKCE-MSG-1130")
                    + " skce did= " + did);
        }
    }

    /**
     * Checks to see if the given json string is a valid json object or not.
     * This method does not hold good for json arrays.
     *
     * Json objects start with symbol '{' and end with symbol '}' Json Arrays
     * start with symbol '[' and end with symbol ']' To validate arrays, use
     * isValidJsonArray method.
     *
     * Note : Validation is done using javax.json-1.0.4.jar library.
     *
     * @param jsonstr - Json string to be validated
     * @return - true or false based on validation result
     */
    public static boolean isValidJsonObject(String jsonstr) {
        if (jsonstr == null || jsonstr.isEmpty()) {
            return false;
        }

        try {
            StringReader stringreader = new StringReader(jsonstr);
            JsonReader jsonreader = Json.createReader(stringreader);
            JsonObject json = jsonreader.readObject();
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public static Boolean handleNonExistantJsonBoolean(JsonObject jsonObject, String key) {
        try {
            return jsonObject.getBoolean(key);
        } catch (NullPointerException ex) {
            return null;
        }
    }

    public static String getDigest(String Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {

        MessageDigest digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(Input.getBytes("UTF-8"));
        String dig = java.util.Base64.getUrlEncoder().withoutPadding().encodeToString(digestbytes);
        return dig;
    }

    public static byte[] getDigestBytes(String Input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {

        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(Input.getBytes("UTF-8"));
        return digestbytes;
    }

    public static byte[] getDigestBytes(byte[] input, String algorithm) throws NoSuchAlgorithmException, NoSuchProviderException, UnsupportedEncodingException {
        MessageDigest digest;
        digest = MessageDigest.getInstance(algorithm, "BCFIPS");
        byte[] digestbytes = digest.digest(input);
        return digestbytes;
    }

    public static JsonObject getJsonObjectFromString(String jsonstr) {
        try {
            StringReader stringreader = new StringReader(jsonstr);
            JsonReader jsonreader = Json.createReader(stringreader);
            return jsonreader.readObject();
        } catch (JsonParsingException ex) {
            return null;
        }
    }

    public static String getTLdplusone(String domain) {
        String allowedtld = domain;
        if (tldList.contains(domain)) {
            allowedtld = domain;
        }
        while (domain.contains(".")) {
            allowedtld = domain;
            domain = domain.substring(domain.indexOf(".") + 1);
            if (tldList.contains(domain)) {
                return allowedtld;
            }
        }
        return allowedtld;
    }

    /*
    ***********************************************************************
8888888888 8888888 8888888b.   .d88888b.
888          888   888  "Y88b d88P" "Y88b
888          888   888    888 888     888
8888888      888   888    888 888     888
888          888   888    888 888     888
888          888   888    888 888     888
888          888   888  .d88P Y88b. .d88P
888        8888888 8888888P"   "Y88888P"
    ***********************************************************************
     */
    /**
     *
     * @param bd
     * @return returns origin parsed from browserdata
     */
    public static String getChallengefromBrowserdata(String bd) {
        try {
            String browserdataJson = new String(java.util.Base64.getUrlDecoder().decode(bd), "UTF-8");
            JsonObject jsonObject;
            try (JsonReader jsonReader = Json.createReader(new StringReader(browserdataJson))) {
                jsonObject = jsonReader.readObject();
            }

            return jsonObject.getString(skfsConstants.JSON_KEY_NONCE);

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     *
     * @param bd
     * @return returns origin parsed from browserdata
     */
    public static String getOriginfromBrowserdata(String bd) {
        try {
            String browserdataJson = new String(java.util.Base64.getUrlDecoder().decode(bd), "UTF-8");
            JsonObject jsonObject;
            try (JsonReader jsonReader = Json.createReader(new StringReader(browserdataJson))) {
                jsonObject = jsonReader.readObject();
            }

            return jsonObject.getString(skfsConstants.JSON_KEY_SERVERORIGIN);

        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * Checks to see if the protocol provided is supported by skfs.
     *
     * @param protocol - String, protocol to be checked
     * @return - true or false based on whether the protocol is among the list
     * of supported protocols or not.
     */
    public static boolean isFIDOProtocolSupported(String protocol) {
        if (protocol == null || protocol.isEmpty()) {
            return false;
        }

        return !(!protocol.equalsIgnoreCase(skfsConstants.FIDO_PROTOCOL_VERSION_U2F_V2) && !protocol.equalsIgnoreCase(skfsConstants.FIDO_PROTOCOL_VERSION_2_0));
    }

    public static String getAlgFromIANACOSEAlg(int alg) {
        switch (alg) {
            case -65535:
                return "SHA1withRSA";
            case -257:
                return "SHA256withRSA";
            case -258:
                return "SHA384withRSA";
            case -259:
                return "SHA512withRSA";
            case -37:
                return "SHA256withRSAandMGF1";
            case -38:
                return "SHA384withRSAandMGF1";
            case -39:
                return "SHA512withRSAandMGF1";
            case -7:
                return "SHA256withECDSA";
            case -35:
                return "SHA384withECDSA";
            case -36:
                return "SHA512withECDSA";
            case -8:
                return "NONEwithECDSA";
            case -43:
                return "SHA256withECDSA";

            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Algorithm:" + alg);
                throw new UnsupportedOperationException("Unsupported Algorithm: " + alg);
        }
    }

    public static String getCurveFromFIDOECCCurveID(int curveID) {
        switch (curveID) {
            case 1:
                return "P-256";
            case 2:
                return "P-384";
            case 3:
                return "P-521";
            case 6:
                return "Curve25519";
            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Curve: " + curveID);
                throw new UnsupportedOperationException("Unsupported Curve");
        }
    }

    public static String getStringFromTPMECCCurveID(short curveID) {
        switch (curveID) {
            case TPMConstants.TPM_ECC_NIST_P256:
                return "P-256";
            case TPMConstants.TPM_ECC_NIST_P384:
                return "P-384";
            case TPMConstants.TPM_ECC_NIST_P521:
                return "P-521";
            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Curve: " + curveID);
                throw new UnsupportedOperationException("Unsupported Curve");
        }
    }

    public static String getHashAlgFromIANACOSEAlg(int alg) {
        if (alg == -65535) {
            return "SHA-1";
        } else if (alg == -257) {
            return "SHA-256";
        } else if (alg == -7) {
            return "SHA-256";
        } else {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                    "Unsupported Algorithm: " + alg);
            throw new UnsupportedOperationException("Unsupported Algorithm");
        }
    }

    public static String getHashAlgFromTPMAlg(short nameAlg) {
        switch (nameAlg) {
            case TPMConstants.TPM_ALG_SHA1:
                return "SHA-1";
            case TPMConstants.TPM_ALG_SHA256:
                return "SHA-256";
            case TPMConstants.TPM_ALG_SHA384:
                return "SHA-384";
            case TPMConstants.TPM_ALG_SHA512:
                return "SHA-512";
            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported TPM Hash Algorithm: " + nameAlg);
                throw new UnsupportedOperationException("Unsupported TPM Hash Algorithm: " + nameAlg);
        }
    }

    public static int getIANACOSEAlgFromPolicyAlg(String alg) {
        switch (alg) {
            case "rsassa-pkcs1-v1_5-sha1":
                return -65535;
            case "rsassa-pkcs1-v1_5-sha256":
                return -257;
            case "rsassa-pkcs1-v1_5-sha384":
                return -258;
            case "rsassa-pkcs1-v1_5-sha512":
                return -259;
            case "rsassa-pss-sha256":
                return -37;
            case "rsassa-pss-sha384":
                return -38;
            case "rsassa-pss-sha512":
                return -39;
            case "ecdsa-p256-sha256":
                return -7;
            case "ecdsa-p384-sha384":
                return -35;
            case "ecdsa-p521-sha512":
                return -36;
            case "eddsa":
                return -8;
            case "ecdsa-p256k-sha256":
                return -43;       //TODO remove this note when number is officially decided

            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Algorithm: " + alg);
                throw new UnsupportedOperationException("Unsupported Algorithm: " + alg);
        }
    }

    public static String getPolicyAlgFromIANACOSEAlg(int alg) {
        switch (alg) {
            case -65535:
                return "rsassa-pkcs1-v1_5-sha1";
            case -257:
                return "rsassa-pkcs1-v1_5-sha256";
            case -258:
                return "rsassa-pkcs1-v1_5-sha384";
            case -259:
                return "rsassa-pkcs1-v1_5-sha512";
            case -37:
                return "rsassa-pss-sha256";
            case -38:
                return "rsassa-pss-sha384";
            case -39:
                return "rsassa-pss-sha512";
            case -7:
                return "ecdsa-p256-sha256";
            case -35:
                return "ecdsa-p384-sha384";
            case -36:
                return "ecdsa-p521-sha512";
            case -8:
                return "eddsa";
            case -43:                       //TODO remove this note when number is officially decided
                return "ecdsa-p256k-sha256";

            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Algorithm: " + alg);
                throw new UnsupportedOperationException("Unsupported Algorithm: " + alg);
        }
    }

    public static String getPolicyCurveFromFIDOECCCurveID(int curveID) {
        switch (curveID) {
            case 1:
                return "secp256r1";
            case 2:
                return "secp384r1";
            case 3:
                return "secp521r1";
            case 6:
                return "curve25519";
            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Curve" + curveID);
                throw new UnsupportedOperationException("Unsupported Curve");
        }
    }

    public static String getPolicyCurveFromOID(ASN1ObjectIdentifier oid) {
        String curveName = ECNamedCurveTable.getName(oid);
        switch (curveName) {
            case "P-256":
                return "secp256r1";
            case "P-384":
                return "secp384r1";
            case "P-521":
                return "secp521r1";
//            case "X25519":             //TODO when BCFIPS 1.0.2 supports Curve25519, this probably will display X25519
//                return "curve25519";
            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Curve" + oid);
                throw new UnsupportedOperationException("Unsupported Curve");
        }
    }

    public static String getPolicyAlgFromAlg(String alg) {
        switch (alg.toUpperCase()) {
            case "SHA1WITHRSA":
                return "rsassa-pkcs1-v1_5-sha1";
            case "SHA256WITHRSA":
                return "rsassa-pkcs1-v1_5-sha256";
            case "SHA384WITHRSA":
                return "rsassa-pkcs1-v1_5-sha384";
            case "SHA512WITHRSA":
                return "rsassa-pkcs1-v1_5-sha512";
            case "SHA256WITHRSAandMGF1":
                return "rsassa-pss-sha256";
            case "SHA384WITHRSAandMGF1":
                return "rsassa-pss-sha384";
            case "SHA512WITHRSAandMGF1":
                return "rsassa-pss-sha512";
            case "SHA256WITHECDSA":
                return "ecdsa-p256-sha256";
            case "SHA384WITHECDSA":
                return "ecdsa-p384-sha384";
            case "SHA512WITHECDSA":
                return "ecdsa-p521-sha512";
            case "NONEWITHECDSA":
                return "eddsa";
//            case "SHA256withECDSA":
//                return "ecdsa-p256k-sha256";      //JCE does not differentiate

            default:
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2002",
                        "Unsupported Algorithm: " + alg);
                throw new UnsupportedOperationException("Unsupported Algorithm: " + alg);
        }
    }


//    // inputstream tojson
//    public static JsonObject inputstreamToJson(InputStream input){
//        StringBuilder sb = new StringBuilder();
//        try {
//            InputStreamReader isr = new InputStreamReader(input);
//            BufferedReader in = new BufferedReader(isr);
//            String line;
//            while ((line = in.readLine()) != null) {
//                sb.append(line);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            return null;
//        }
//
//        String jsonInput = sb.toString();
//
//        StringReader s = new StringReader(jsonInput);
//        JsonReader jsonReader = Json.createReader(s);
//        JsonObject responseJSON = jsonReader.readObject();
//        jsonReader.close();
//
//        return responseJSON;
//    }
    //check svcinfo
    public static ServiceInfo checkSvcInfo(String wsprotocol, String svcinfo) {

        ServiceInfo svinfo = new ServiceInfo();
        try {
            if (svcinfo == null) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                        "NULL");
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "NULL");
                return svinfo;
            }
            JsonObject svcjson = applianceCommon.stringToJSON(svcinfo);
            if(svcjson == null){
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                        "NULL");
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "NULL");
                return svinfo;
            }
            if (svcjson.getJsonNumber("did") == null || svcjson.getJsonNumber("did").longValue() < 1) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                        "DID");
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "DID");
                return svinfo;
            }
            try {
                inputValidateSKCEDid(svcjson.getJsonNumber("did").toString());
            } catch (Exception ex) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5006",
                        "invalid svcinfo = " + ex.getLocalizedMessage());
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-5006").replace("{0}", "") + "invalid svcinfo = " + ex.getLocalizedMessage());
                return svinfo;
            }
            svinfo.setDid(svcjson.getJsonNumber("did").longValue());

            if (!svcjson.containsKey("protocol") || svcjson.getString("protocol").trim().isEmpty()) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                        "Protocol");
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "Protocol");
                return svinfo;
            }
            if (!svcjson.getString("protocol").equalsIgnoreCase(skfsConstants.FIDO_PROTOCOL_VERSION_U2F_V2) && !svcjson.getString("protocol").equalsIgnoreCase(skfsConstants.FIDO_PROTOCOL_VERSION_2_0)) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                        "Protocol");
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "Protocol");
                return svinfo;
            }
            svinfo.setProtocol(svcjson.getString("protocol"));

            if (!svcjson.containsKey("authtype") || svcjson.getString("authtype").trim().isEmpty()) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                        "Authtype");
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "Authtype");
                return svinfo;
            }
            if (!svcjson.getString("authtype").equalsIgnoreCase(skfsConstants.FIDO_API_AUTH_TYPE_HMAC) && !svcjson.getString("authtype").equalsIgnoreCase(skfsConstants.FIDO_API_AUTH_TYPE_PASSWORD)) {
                skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                        "Authtype");
                svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "Authtype");
                return svinfo;
            }
            svinfo.setAuthtype(svcjson.getString("authtype"));

            if (svcjson.getString("authtype").equalsIgnoreCase(skfsConstants.FIDO_API_AUTH_TYPE_PASSWORD)) {
                if (!svcjson.containsKey("svcusername") || svcjson.getString("svcusername").trim().isEmpty()) {
                    skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " credential");
                    svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0033"));
                    return svinfo;
                }
                svinfo.setSvcusername(svcjson.getString("svcusername"));
                if (!svcjson.containsKey("svcpassword") || svcjson.getString("svcpassword").trim().isEmpty()) {
                    skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " credential");
                    svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0033"));
                    return svinfo;
                }
                svinfo.setSvcpassword(svcjson.getString("svcpassword"));
            } else {

                if (wsprotocol.equalsIgnoreCase("SOAP")) {
                    if (svcjson.getJsonNumber("timestamp") == null  || svcjson.getJsonNumber("timestamp").longValue() < 1) {
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                                "timestamp");
                        svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "timestamp");
                        return svinfo;
                    }
                    svinfo.setTimestamp(svcjson.getJsonNumber("timestamp").longValue());

                    if (!svcjson.containsKey("authorization") || svcjson.getString("authorization").trim().isEmpty()) {
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003", " authorization");
                        svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "authorization");
                        return svinfo;
                    }
                    svinfo.setAuthorization(svcjson.getString("authorization"));
                    if (!svcjson.containsKey("strongkey-content-sha256") || svcjson.getString("strongkey-content-sha256").trim().isEmpty()) {
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                                "strongkey-content-sha256");
                        svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "strongkey-content-sha256");
                        return svinfo;
                    }
                    svinfo.setContentSHA256(svcjson.getString("strongkey-content-sha256"));

                    if (!svcjson.containsKey("strongkey-api-version") || svcjson.getString("strongkey-api-version").trim().isEmpty()) {
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                                "strongkey-api-version");
                        svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "strongkey-api-version");
                        return svinfo;
                    }
                    // only check for SK3_0 as this is only used by
                    if (!svcjson.getString("strongkey-api-version").equalsIgnoreCase("SK3_0")) {
                        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2003",
                                "strongkey-api-version");
                        svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-2003").replace("{0}", "") + "strongkey-api-version");
                        return svinfo;
                    }
                    svinfo.setStrongkeyAPIversion(svcjson.getString("strongkey-api-version"));
                }
            }
        } catch (Exception ex) {
             skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-5006",
                                "invalid svcinfo = " + ex.getLocalizedMessage());
            svinfo.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-5006").replace("{0}", "") + "invalid svcinfo = " + ex.getLocalizedMessage());
            return svinfo;
        }

        svinfo.setErrormsg(null);
        return svinfo;
    }

    /*
    ***********************************************************************
8888888b.
888   Y88b
888    888
888   d88P  .d88b.  .d8888b  88888b.   .d88b.  88888b.  .d8888b   .d88b.  .d8888b
8888888P"  d8P  Y8b 88K      888 "88b d88""88b 888 "88b 88K      d8P  Y8b 88K
888 T88b   88888888 "Y8888b. 888  888 888  888 888  888 "Y8888b. 88888888 "Y8888b.
888  T88b  Y8b.          X88 888 d88P Y88..88P 888  888      X88 Y8b.          X88
888   T88b  "Y8888   88888P' 88888P"   "Y88P"  888  888  88888P'  "Y8888   88888P'
                             888
                             888
                             888
    ***********************************************************************
     */
 /*
     * Internal methods to build wsresponse json object
     */
    public static String buildReturn(String response) {
        FIDOReturnObject fro = new FIDOReturnObject(response);
        return fro.toJsonString();
    }

    /*
     * Internal methods to build wsresponse json object
     */
    public static String buildPreRegisterResponse(JsonObject challenge, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_PREREGISTER, challenge, "", message, error);
        return fro.toJsonString();
    }

    public static String buildRegisterResponse(String response, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_REGISTER, null, response, message, error);
        return fro.toJsonString();
    }

    public static String buildPreAuthResponse(JsonObject challenge, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_PREAUTH, challenge, "", message, error);
        return fro.toJsonString();
    }

    public static String buildAuthenticateResponse(String response, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_AUTHENTICATE, null, response, message, error);
        return fro.toJsonString();
    }

    public static String buildGetKeyInfoResponse(JsonObject info, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_GETKEYSINFO, info, "", message, error);
        return fro.toJsonString();
    }

    public static String buildDeregisterResponse(String response, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_DEREGISTER, null, response, message, error);
        return fro.toJsonString();
    }

    public static String buildDeactivateResponse(String response, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_DEACTIVATE, null, response, message, error);
        return fro.toJsonString();
    }

    public static String buildActivateResponse(String response, String message, String error) {
        FIDOReturnObjectV1 fro = new FIDOReturnObjectV1(skfsConstants.FIDO_METHOD_ACTIVATE, null, response, message, error);
        return fro.toJsonString();
    }

}
