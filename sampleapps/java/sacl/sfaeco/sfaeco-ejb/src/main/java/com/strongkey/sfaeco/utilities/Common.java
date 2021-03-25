/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 *
* *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 * Common static methods used in the application
 */

package com.strongkey.sfaeco.utilities;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.EntropySourceProvider;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;


@SuppressWarnings({"StaticNonFinalUsedInInitialization", "CallToPrintStackTrace"})
public class Common {
    
    // Property files used by this application for configuration info
    private static final ResourceBundle DEFAULT_CONFIG = ResourceBundle.getBundle("sfaeco-configuration");
    private static ResourceBundle sfaecoconfig = null ;
    
    // Location where SFAECO is installed on this machine
    private static String SFAECO_HOME;
    
    // Logger for the application
    private static final Logger LOGGER = Logger.getLogger("SFAECO", "sfaeco-messages_" + Locale.getDefault());
    
    private static final short SID;
    
    private static SecureRandom FIPS_DRBG;
    
    /**************************************************
                 888             888    d8b          
                 888             888    Y8P          
                 888             888                 
        .d8888b  888888  8888b.  888888 888  .d8888b 
        88K      888        "88b 888    888 d88P"    
        "Y8888b. 888    .d888888 888    888 888      
             X88 Y88b.  888  888 Y88b.  888 Y88b.    
         88888P'  "Y888 "Y888888  "Y888 888  "Y8888P 
     *************************************************/

    static {

        /**
         * Setup FIPS compliant Deterministic Random Bit Generator
         */
        FIPS_DRBG = buildDrbg();
        CryptoServicesRegistrar.setSecureRandom(FIPS_DRBG);

        /**
         * Print out the values of the central configuration properties built
         * into the application - sort it for readability
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Enumeration<String> enm = DEFAULT_CONFIG.getKeys();
        List<String> keys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            keys.add(enm.nextElement());
        }

        Collections.sort((List<String>) keys);
        Iterator it = keys.iterator();
        try {
            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.contains("password")) {
                    baos.write(("\n\t" + key + ": **********").getBytes());
                } else {
                    baos.write(("\n\t" + key + ": " + DEFAULT_CONFIG.getString(key)).getBytes());
                }
            }
            baos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        log(Level.INFO, "SFAECO-MSG-1001", baos.toString());

        /**
         * Check environment variable for installation location; if not found
         * get default location specified in the configuration properties file.
         */
        if ((SFAECO_HOME = System.getenv("SFAECO_HOME")) == null) {
            SFAECO_HOME = DEFAULT_CONFIG.getString("sfaeco.cfg.property.sfaeco.home");
        }
        log(Level.INFO, "SFAECO-MSG-1001", "SFAECO_HOME is: " + SFAECO_HOME);

        // See if there is an over-riding properties file in aksvalidator
        try {
            File f = new File(SFAECO_HOME + "/" + "etc" + "/" + "sfaeco-configuration.properties");
            /**
             * Using try-with-resources; which will take care of closing the
             * FileInputStream fis in any case (success or failure)
             */
            try (FileInputStream fis = new FileInputStream(f)) {
                sfaecoconfig = new java.util.PropertyResourceBundle(fis);
            }

            log(Level.INFO, "SFAECO-MSG-1001",
                    "Using sfaeco-configuration.properties from $HOME/etc/sfaeco directory: "
                    + SFAECO_HOME + "/etc/sfaeco-configuration.properties");

            // Sort properties for readability
            baos = new ByteArrayOutputStream();
            enm = sfaecoconfig.getKeys();
            keys = new ArrayList<>();
            while (enm.hasMoreElements()) {
                keys.add(enm.nextElement());
            }

            Collections.sort((List<String>) keys);
            it = keys.iterator();

            while (it.hasNext()) {
                String key = (String) it.next();
                if (key.contains("password")) {
                    baos.write(("\n\t" + key + ": **********").getBytes());
                } else {
                    baos.write(("\n\t" + key + ": " + sfaecoconfig.getString(key)).getBytes());
                }
            }
            baos.close();
            
        } catch (java.io.FileNotFoundException ex) {
            log(Level.WARNING, "SFAECO-MSG-1001", "There is no sfaeco-configuration.properties in the "
                    + "$HOME/etc/sfaeco directory; using system-wide sfaeco-configuration.properties");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // Print out local configuration values from aksvalidator
        log(Level.INFO, "SFAECO-MSG-1002", baos.toString());
        
        SID = Short.parseShort(getConfigurationProperty("sfaeco.cfg.property.sfaeco.sid"));
        log(Level.INFO, "SFAECO-MSG-1001", "SID [sfaeco.cfg.property.sfaeco.sid] of this server is: " + SID);
    }
    
    /*********************************************************
        888                            d8b                   
        888                            Y8P                   
        888                                                  
        888  .d88b.   .d88b.   .d88b.  888 88888b.   .d88b.  
        888 d88""88b d88P"88b d88P"88b 888 888 "88b d88P"88b 
        888 888  888 888  888 888  888 888 888  888 888  888 
        888 Y88..88P Y88b 888 Y88b 888 888 888  888 Y88b 888 
        888  "Y88P"   "Y88888  "Y88888 888 888  888  "Y88888 
                          888      888                   888 
                     Y8b d88P Y8b d88P              Y8b d88P 
                      "Y88P"   "Y88P"                "Y88P"  
     ********************************************************/
    
    /**
     * Creates an entry log as execution enters a class/method
     * 
     * @param level Level of the log entry
     * @param logkey String with the SFAECO messages key
     * @param source String with the source classname/method calling this function
     * @param did String containing the cryptographic domain ID
     * @param txid String with the unique transaction ID 
     * @return long value of the current time in milliseconds
     */
    public static long entryLog(Level level, String logkey, String source, String did, String txid) {
        long timein = nowms();
        log(level, logkey, source + " [TX="+SID+"-"+did+"-"+txid +"|IN="+timein+"]");
        return timein;
    }

    /**
     * Creates an exit log entry as execution exits a class/method
     * 
     * @param level Level of the log entry
     * @param logkey String with the SFAECO messages key
     * @param source String with the source classname/method calling this function
     * @param did String containing the cryptographic domain ID
     * @param txid String with the unique transaction ID 
     * @param timein long value of the time execution came into this class/method
     */
    public static void exitLog(Level level, String logkey, String source, String did, String txid, long timein) {
        long timeout = nowms();
        log(level, logkey, source + " [TX="+SID+"-"+did+"-"+txid +"|IN="+timein+"|OUT="+timeout+"|TTC="+(timeout-timein)+"]");
    }


    /**
     *  Prints the appropriate information to the application logger
     *  Databeans cannot have the java.util.logging.logger class as it
     *  is not serializable.
     *  @param level - Level at which the message should be logged
     *  @param key - Property key for this message
     *  @param param - Any parameters specified with this message
     */
    public static void log(Level level, String key, Object param)
    {
        LOGGER.log(level, key, param);
    }
    
    /**
     *  Prints the appropriate information to the application logger
     *  Databeans cannot have the java.util.logging.logger class as it
     *  is not serializable.
     *  @param level - Level at which the message should be logged
     *  @param key - Property key for this message
     *  @param param - An array of parameter objects
     */
    public static void log(Level level, String key, Object[] param)
    {
        LOGGER.log(level, key, param);
    }
    
    /***********************************
       888888                            
        "88b                            
         888                            
         888 .d8888b   .d88b.  88888b.  
         888 88K      d88""88b 888 "88b 
         888 "Y8888b. 888  888 888  888 
         88P      X88 Y88..88P 888  888 
         888  88888P'  "Y88P"  888  888 
       .d88P                            
     .d88P"                             
    888P"                                            
     ***********************************/
    
    /**
     * Convert the JSON represented as a String into a JsonObject
     *
     * @param jsonstr String containing the JSON
     * @return JsonObject if successful, null otherwise
     */
    public static JsonObject stringToJson(String jsonstr) {
        if (!(jsonstr == null || jsonstr.isEmpty())) {
            try (JsonReader jsonReader = Json.createReader(new StringReader(jsonstr))) {
                return jsonReader.readObject();
            } catch (Exception ex) {
                log(Level.WARNING, "SFAECO-ERR-1005", ex.getLocalizedMessage());
            }
        }
        log(Level.WARNING, "SFAECO-ERR-1003", "Parameter [jsonstr] is empty or null");
        return null;
    }
    
    /**
     * Given a JSON string and a search-key, this method looks up the 'key'
     * in the JSON and if found, returns the associated value.  Returns NULL
     * in all error conditions.
     *
     * @param jsonstr String containing JSON
     * @param key String containing the search-key
     * @param datatype String containing the data-type of the value-object
     * @return Object containing the value for the specified key if valid;
     * null in all error cases.
     */
    public static Object getJsonValue(String jsonstr, String key, String datatype) {
        if (jsonstr == null || jsonstr.isEmpty()) {
            if (key == null || key.isEmpty()) {
                if (datatype == null || datatype.isEmpty()) {
                    return null;
                }
            }
        }

        try (JsonReader jsonreader = Json.createReader(new StringReader(jsonstr))) {
            JsonObject json = jsonreader.readObject();

            if (!json.containsKey(key)) {
                log(Level.WARNING, "SFAECO-ERR-1003", "'" + key + "' does not exist in the json");
                return null;
            }

            switch (datatype) {
                case "Boolean": return json.getBoolean(key);
                case "Int": return json.getInt(key);
                case "JsonArray": return json.getJsonArray(key);
                case "JsonNumber": return json.getJsonNumber(key);
                case "JsonObject": return json.getJsonObject(key);
                case "JsonString": return json.getJsonString(key);
                case "String": return json.getString(key);
                default: return null;
            }
        } catch (Exception ex) {
            log(Level.WARNING, "SFAECO-ERR-1001", ex.getLocalizedMessage());
            return null;
        }
    }
    
    /**
     * Returns a JSON object with an OK message
     * @return JsonObject as follows:
     *  {
     *      "{
     *          "s": 200,
     *          "r": "ok"
     *      }
     *  }
     * @throws JsonException
     */
    public static JsonObject jsonOk() throws JsonException {
        return Json.createObjectBuilder()
                    .add("s", 200)
                    .add("r", "ok")
                .build();
    }
    
    /**
     * Returns a JSON object with an error message, as well as the classname and methodname
     * where the error originated in the library
     *
     * @param c String with the classname
     * @param m String with the methodname from the class
     * @param k String with the JSON key
     * @param v String with the JSON value
     * @return JsonObject as follows:
     *  {
     *      "error" : {
     *          "c": "classname",
     *          "m": "methodname",
     *          "k": "v"
     *      }
     *  }
     * @throws JsonException
     */
    public static JsonObject jsonError(String c, String m, String k, String v) throws JsonException {
        return Json.createObjectBuilder()
                .add("error", Json.createObjectBuilder()
                    .add("c", c)
                    .add("m", m)
                    .add(k, v))
                .build();
    }

    /**
     * Converts InputStream to JSON object
     * @param is InputStream
     * @return JsonObject
     */
    public static JsonObject inputstreamToJson(InputStream is) {
        StringBuilder sb = new StringBuilder();
        try {
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader in = new BufferedReader(isr);
                String line;
                while ((line = in.readLine()) != null) {
                    sb.append(line);
                }
        } catch (IOException ex) {
            log(Level.WARNING, "SFAECO-ERR-1005", ex.getLocalizedMessage());
        }
        String jsonString = sb.toString();
        log(Level.FINE, "SFAECO-MSG-1000", "Inputstream JSON: " + jsonString);
        return stringToJson(jsonString);
    }
    
    /**
     * Decodes the returned value from the SKCERO
     *
     * @param input String containing returned JSON
     * @param type String value denoting the type of object
     * @return String with the returned value from the JSON
     */
    public static String decodeJson(String input, String type)
    {
        JsonObject jsonObject;
        try (JsonReader jsonReader = Json.createReader(new StringReader(input))) {
            jsonObject = jsonReader.readObject();
        }

        switch (type) {
            case "uid":  return jsonObject.getString("uid");
            case "FIDOKeysEnabled":  return jsonObject.getString("FIDOKeysEnabled");
            case "TwoStepVerification":  return jsonObject.getString("TwoStepVerification");
            case "did":    return jsonObject.getString("did");
            default: return null;   // Shouldn't happen, but...
        }
    }
    
    /**
     * Pretty print digital certificate's serial number with colons (:) for
     * better readability
     * @param s String containing the certificate's serial number
     * @return String containing the certificate's serial number with : separators
     */
    public static String prettyPrint (String s) {
        StringBuilder sb = new StringBuilder();
        char[] sc = s.toCharArray();
        int size = s.length();
        int j = 0;
        
        // Prepend a zero in front when odd number of digits in serial number
        if (size % 2 == 1) {
            sb.append('0').append(sc, 0, 1).append(':');
            j = 1;
        }
            
        for (int i=j; i < size; i+=2,j+=2) {
            sb.append(sc, i, 2);
            if (i != size-2)
                sb.append(':');
        }
        return sb.toString();
    }
    
    /******************************************
                      d8b
                      Y8P

        88888b.d88b.  888 .d8888b   .d8888b
        888 "888 "88b 888 88K      d88P"
        888  888  888 888 "Y8888b. 888
        888  888  888 888      X88 Y88b.
        888  888  888 888  88888P'  "Y8888P
     ******************************************/
    
    /**
     * Returns the unique Server ID of the current appliance. Define in the
     * configuration.properties file of the Tellaro appliance
     * @return short value of the SID
     */
    public static short getSid() {
        return SID;
    }
    
    public static String getConfigurationProperty(String key) {
        if (sfaecoconfig != null) {
            try {
                String s = sfaecoconfig.getString(key);
                if (s.startsWith("SFAECO_HOME")) {
                    return s.replaceFirst("SFAECO_HOME", SFAECO_HOME);
                } else {
                    return s;
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }

        String s = DEFAULT_CONFIG.getString(key);
        if (s.startsWith("SFAECO_HOME")) {
            return s.replaceFirst("SFAECO_HOME", SFAECO_HOME);
        } else {
            return s;
        }
    }

    /** Generates the current time, as based on the clock of the EJB Tier machine.
     *
     *  NOTE:  The reason for the math with 1000 in the function is because MySQL
     *  does not store the milliseconds, while TimeStamp uses it.  However the
     *  XMLSignatures will fail if the milliseconds are not removed from the time
     *  before creating the time.  This implies that all timestamps are accurate
     *  only upto the second - not millisecond.  Good enough for this application,
     *  we believe.
     *
     * @return java.sql.Timestamp
     */
    public static java.sql.Timestamp now()
    {
        return new java.sql.Timestamp(((new java.util.GregorianCalendar().getTimeInMillis())/1000)*1000);
    }
    
    public static long nowms() {
        return new java.util.Date().getTime();
    }
    
    // Gets a unique transaction ID for assigning to each service request
    public static String nextTxid() {
        return String.valueOf(Math.abs(FIPS_DRBG.nextLong())).substring(0, 10);
    }
    
    /**
     * Base64 URL encoder
     */
    public static String urlEncode(String raw) {
        return Base64.getUrlEncoder()
                .withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static String urlEncode(byte[] raw) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw);
    }

    /**
     * Base64 URL decoder
     */
    public static byte[] urlDecode(String raw) {
        return Base64.getUrlDecoder().decode(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static byte[] urlDecode(byte[] raw) {
        return Base64.getUrlDecoder().decode(raw);
    }
    
    /**
     * Build a FIPS compliant SecureRandom that can be used for purposes like
     * nonces or transaction IDs for logging when the TPM2 is NOT available.
     * @return SecureRandom
     */
    private static SecureRandom buildDrbg() {

        String date = Long.toHexString(new Date().getTime());
        byte[] nonce = new byte[256];
        new SecureRandom().nextBytes(nonce);
        EntropySourceProvider entSource = new BasicEntropySourceProvider(new SecureRandom(), true);
        FipsDRBG.Builder drgbBldr = FipsDRBG.SHA256.fromEntropySource(entSource)
                                        .setSecurityStrength(256)
                                        .setEntropyBitsRequired(256)
                                        .setPersonalizationString(date.getBytes());
        return drgbBldr.build(nonce, true);
    }

}