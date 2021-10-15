/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.appliance.utilities;

import com.strongkey.appliance.objects.applianceException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

public class applianceCommon {

    private static final String classname = "applianceCommon";
    private static final String fs = System.getProperty("file.separator");

    private static final String appliancehome = "/usr/local/strongkey/appliance";

    //Resource bundle for the appliance config
    private static ResourceBundle appliancehrb = null;

    private static final ResourceBundle vrb = ResourceBundle.getBundle("resources.appliance.appliance-version");

    private static final ResourceBundle defaultApplianceConfig = ResourceBundle.getBundle("resources.appliance.appliance-configuration");
    
    private static SortedMap<Long, Map> applconfigmap = new ConcurrentSkipListMap<>();

    // Property files used by this application for application messages
    private static final ResourceBundle msgrb = ResourceBundle.getBundle("resources.appliance.appliance-messages");

    // Map for the maximum length values
    private static Map<String, Integer> maxlenmap = new ConcurrentHashMap<>();

    //List of domains where ccs is enabled
    private static List<Long> CCSPINDOMAINS = new ArrayList<>();

    private static SortedMap<Integer, String> entitynames = new ConcurrentSkipListMap<>();
    private static SortedMap<Integer, String> repops = new ConcurrentSkipListMap<>();

    private static Boolean replicate = Boolean.TRUE;    // Configuration property default

    // Start time of SAKA
    private static final long boottime = System.currentTimeMillis();

    private static final Long serverid;

    private static String localhost;

    private static final Lock publock = new ReentrantLock(true);
    private static final Lock sublock = new ReentrantLock(true);
    private static final Lock acklock = new ReentrantLock(true);
    private static final Lock blplock = new ReentrantLock(true);

    private static final Long REP_STATE_LOCK_WAITTIME;

    /*
*****************************************
d8b          d8b 888
Y8P          Y8P 888
                 888
888 88888b.  888 888888
888 888 "88b 888 888
888 888  888 888 888
888 888  888 888 Y88b.
888 888  888 888  "Y888
*******************************************
     */
    static {
        setupMaxLenMap();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {

            File f = new File(appliancehome + fs + "etc" + fs + "appliance-configuration.properties");
            /**
             * Using try-with-resources; which will take care of closing the
             * FileInputStream fis in any case (success or failure)
             */
            try (FileInputStream fis = new FileInputStream(f)) {
                appliancehrb = new java.util.PropertyResourceBundle(fis);
            }

            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.INFO, "APPL-MSG-1053",
                    "Using appliance-configuration.properties from APPLIANCE_HOME directory: "
                    + appliancehome + "/etc/appliance-configuration.properties");

            // Sort properties for readability
            Enumeration<String> enm = appliancehrb.getKeys();
            List<String> keys = new ArrayList<>();

            while (enm.hasMoreElements()) {
                keys.add(enm.nextElement());
            }

            Collections.sort(keys);
            Iterator<String> it = keys.iterator();

            while (it.hasNext()) {
                String key = it.next();
                baos.write(("\n\t" + key + ": ").getBytes());
                if (key.contains("password") || key.contains("secretkey") || key.contains("accesskey")) {
                    baos.write(("**********").getBytes());
                } else {
                    baos.write((appliancehrb.getString(key)).getBytes());
                }

            }
            baos.close();

        } catch (java.io.FileNotFoundException ex) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, "APPL-MSG-1053", "There is no appliance-configuration.properties in the "
                    + "APPLIANCE_HOME directory;");
        } catch (IOException ex) {
            strongkeyLogger.printStrongAuthStackTrace(applianceConstants.APPLIANCE_LOGGER, classname, "init()", ex);
        }

        // Print out local configuration values from APPLIANCE_HOME
        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.INFO, "APPL-MSG-1131", baos.toString());

        // Print SKLES version
        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.INFO, "APPL-MSG-1053", "SKFS VERSION is: " + vrb.getString("version"));

        // Print this SKLES appliance's server ID
        serverid = Long.parseLong(getApplianceConfigurationProperty("appliance.cfg.property.serverid"));
        if (serverid == 0L) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, "APPL-ERR-2000", serverid);
        } else {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.INFO, "APPL-MSG-1053", "This appliance's SERVER_ID is: " + serverid);
        }

        //set replication status
//        setReplicateStatus(Boolean.FALSE);

        REP_STATE_LOCK_WAITTIME = Long.parseLong(getApplianceConfigurationProperty("appliance.cfg.property.messaging.statechange.waittime"));

        putReplicationMaps();
    }

    /**
     * Creates a new instance of common
     */
    public applianceCommon() {
    }

    /**
     * Checks if the parameter passed is either an Active, Inactive or Other
     *
     * @param s String
     * @return boolean
     */
    public static boolean aio(java.lang.String s) {
        if (!s.trim().equals("Active")) {
            if (!s.trim().equals("Inactive")) {
                return s.trim().equals("Other");
            }
        }
        return true;
    }

    public static String getHostname() {
        if (localhost == null) {
            try {
                localhost = InetAddress.getLocalHost().getCanonicalHostName();
            } catch (UnknownHostException ex) {
                return null;
            }
        }
        return localhost;
    }

    /**
     * Gets the version number of this build of SKLES
     *
     * @return String - The version number
     */
    public static String getVersion() {
        return vrb.getString("version");
    }

    /**
     * Returns the unique ID of this key-appliance server
     *
     * @return Long
     */
    public static Long getServerId() {
        return serverid;
    }

    /**
     * Checks if the parameter passed is a valid failure reason
     *
     * @param s String
     * @return boolean
     */
    public static boolean reasonOK(java.lang.String s) {
        if (!s.trim().equals("Invalid Key")) {
            if (!s.trim().equals("Invalid Decryption")) {
                if (!s.trim().equals("Invalid Request")) {
                    if (!s.trim().equals("Invalid Requestor")) {
                        if (!s.trim().equals("Invalid Token")) {
                            return s.trim().equals("Other");
                        }
                    }
                }
            }
        }
        return true;
    }

    /**
     * Convenience method to convert a String to a byte array and not have to
     * worry about character-sets. Apparently, the method getBytes() in
     * java.lang.String depends on the default character-set of the machine the
     * JVM is running on. Since this can cause problems on different machines,
     * the following method converts a String to a byte[] the "long way" - one
     * character at a time!
     *
     * @param input - The string that needs to be converted
     * @return byte[]
     */
    public static byte[] toBytes(String input) {
        byte[] bytes = new byte[input.length()];
        char[] chars = input.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            bytes[i] = (byte) chars[i];
        }
        return bytes;
    }

    /**
     * Convenience method to convert a byte array to a String and not have to
     * worry about character-sets. Apparently, the constructor in String using
     * byte[] as a parameter, depends on the default character-set of the
     * machine the JVM is running on. Since this can cause problems on different
     * machines, the following method converts a byte[] to a String the "long
     * way" - one character at a time!
     *
     * @param input - The string that needs to be converted
     * @return byte[]
     */
    public static String toString(byte[] input) {
        char[] chars = new char[input.length];
        for (int i = 0; i < input.length; i++) {
            chars[i] = (char) (input[i] & 0xff);
        }
        return new String(chars);
    }

    /**
     * Converts the given byte array to Hex and returns it in a String.
     *
     * @param bytes array of bytes
     * @return String
     */
    public static String bytesToHex(byte[] bytes) {
        // For converting byte array to hex string
        String digits = "0123456789abcdef";

        int length = bytes.length;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i != length; i++) {
            int v = bytes[i] & 0xff;

            sb.append(digits.charAt(v >> 4));
            sb.append(digits.charAt(v & 0xf));
        }
        return sb.toString();
    }

    /**
     * Generates a SHA256 message digest for the file content specified by
     * filelocation parameter.
     *
     * @param filelocation - location of file / complete path of file.
     * @return the message digest string if it could be calculated; empty string
     * otherwise
     */
    public static String getSHA256MDForFileContent(String filelocation) {
        if (filelocation == null) {
            return "";
        }

        String hash = "";

        FileInputStream fis = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");

            fis = new FileInputStream(filelocation);
            byte[] dataBytes = new byte[1024];
            int nread;
            while ((nread = fis.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }

            // Generate digest
            byte[] digestbytes = md.digest();

            // Base64-encode digest and use it
            hash = bytesToHex(digestbytes);

        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(classname).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(classname).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(classname).log(Level.SEVERE, null, ex);
        }
        finally{
            try {
                if(fis !=null)
                    fis.close();
            } catch (IOException ex) {
                Logger.getLogger(applianceCommon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return hash;
    }

    /**
     * Convenience method to get a lock for an operation
     *
     * @param locktype
     * @return boolean - Returns true if the lock was acquired, false otherwise
     *
     * Bug ID 264 - When attempting to get a lock, threads rely on luck to
     * determine the order in which locks are obtained. This creates situations
     * where a particularly unlucky thread continuously fails to get a lock
     * until it runs out of attempts.
     *
     * We have reworked the locking mechanism to ensure threads obtain locks in
     * FIFO order. This introduces fairness to the lock and ensures no thread
     * has to wait longer than need be.
     */
    public static boolean getLock(String locktype) {
        try {
            if (locktype.equalsIgnoreCase("PUB")) {
                return publock.tryLock(REP_STATE_LOCK_WAITTIME, TimeUnit.SECONDS);
            } else if (locktype.equalsIgnoreCase("SUB")) {
                return sublock.tryLock(REP_STATE_LOCK_WAITTIME, TimeUnit.SECONDS);
            } else if (locktype.equalsIgnoreCase("ACK")) {
                return acklock.tryLock(REP_STATE_LOCK_WAITTIME, TimeUnit.SECONDS);
            } else if (locktype.equalsIgnoreCase("BLP")) {
                return blplock.tryLock(REP_STATE_LOCK_WAITTIME, TimeUnit.SECONDS);
            } else {
                return false;
            }
        } catch (InterruptedException ex) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, classname, "getLock", "APPL-ERR-1096", locktype);
            return false;
        }
    }

    /**
     * Convenience method to release a lock after completing the operation
     *
     * @param locktype
     */
    public static void releaseLock(String locktype) {
        if (locktype.equalsIgnoreCase("PUB")) {
            publock.unlock();
        } else if (locktype.equalsIgnoreCase("SUB")) {
            sublock.unlock();
        } else if (locktype.equalsIgnoreCase("ACK")) {
            acklock.unlock();
        } else if (locktype.equalsIgnoreCase("BLP")) {
            blplock.unlock();
        }
    }

    /**
     * Writes the data from the given datahandler to a file of the specified
     * name in the specified target location.
     *
     * @param is
     * @param logger
     * @return String The full path of the result file.
     * @param fileName The name of the file to be written to.
     * @param targetPath The full path of the target location.
     * @throws com.strongkey.appliance.objects.applianceException
     */
    public static String writeToFileR(InputStream is,
            String fileName,
            String targetPath,
            String logger) throws applianceException {
        //Local Variables
        String fileFullPath = "";
        InputStream inputStream = null;
        FileOutputStream fos = null;
        strongkeyLogger.log(logger, Level.FINE, "Writing to file; fileName = {0}", fileName);
        strongkeyLogger.log(logger, Level.FINE, "Writing to file; targetPath = {0}", targetPath);
        try {
            inputStream = is;
            if (inputStream != null) {
                if (targetPath != null) {
                    File outputFile = new File(targetPath + fs + fileName);
                    fos = new FileOutputStream(outputFile);
                    //write to target location
                    strongkeyLogger.log(logger, Level.FINE, "Writing to file; writing to target..", "");
                    byte[] b = new byte[1024];
                    int numread;
                    while ((numread = inputStream.read(b)) != -1) {
                        fos.write(b, 0, numread);
                        fos.flush();
                    }
                    strongkeyLogger.log(logger, Level.FINE, "Writing to file; wrote to target.", "");
                    fos.close();
                    inputStream.close();
                    fileFullPath = outputFile.getAbsolutePath();
                } else {
                    strongkeyLogger.log(logger, Level.SEVERE, "targetPath is null. Please specify the target location to write the file", "");
                    throw new applianceException("Please specify the target location to write the file");
                }
            } else {
                strongkeyLogger.log(logger, Level.SEVERE, "No data in datahandler", "");
                throw new applianceException("Datahandler has no data");
            }
        } catch (FileNotFoundException e) {
            strongkeyLogger.log(logger, Level.SEVERE, e.getLocalizedMessage(), "");
            throw new applianceException(e.getLocalizedMessage());
        } catch (IOException e) {
            strongkeyLogger.log(logger, Level.SEVERE, e.getLocalizedMessage(), "");
            throw new applianceException(e.getLocalizedMessage());
        } finally {
            strongkeyLogger.log(logger, Level.FINE, "Writing to file; closing the streams..", "");
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ex) {
            }
        }
        strongkeyLogger.log(logger, Level.FINE, "Writing to file; returning the file path..", "");
        return fileFullPath;
    }

    /**
     * Creates an unique directory under the rootPath. The folder name is a
     * timestamp in milliseconds since January 1, 1970, 00:00:00 GMT to ensure
     * uniqueness. Based on the parameter 'hidden' the directory name is
     * prefixed by a dot to make it hidden. In an unlikely event that the target
     * directory already exists, it retries with a different timestamp. Changed
     * on NOV-15-2012 : Having the timestamp as the directory name does not
     * ensure uniqueness in a situation where multiple threads come in and
     * create directories in the exact same time. To solve this, appending the
     * thread id to the timestamp.
     *
     * @param logger
     * @return the name of the newly created directory.
     * @param baseDir The directory under which the new directory will be
     * created.
     * @param hidden If true, creates a hidden directory.
     * @throws com.strongkey.appliance.objects.applianceException
     */
    public static String createUniqueDir(String logger, String baseDir, boolean hidden) throws applianceException {
        strongkeyLogger.logp(logger, Level.FINE, classname, "createUniqueDir", "APPL-MSG-1000", "Entering Common.createUniqueDir method");
        String path = null;
        boolean success = false;
        if ((baseDir != null) && (!(baseDir.equals("")))) {
            long threadId = Thread.currentThread().getId();
            if (hidden) {
                path = baseDir + fs + "." + nowMs() + "-" + threadId;
            } else {
                path = baseDir + fs + nowMs() + "-" + threadId;
            }
            File targetDir = new File(path);
            if (targetDir.exists()) {
                strongkeyLogger.logp(logger, Level.WARNING, classname, "createUniqueDir", "APPL-MSG-1000", "Target Directory already exists. Retrying with a different name");
                createUniqueDir(logger, baseDir, hidden);
            } else {
                success = targetDir.mkdirs();
            }
            if (success) {
                strongkeyLogger.logp(logger, Level.FINE, classname, "createUniqueDir", "APPL-MSG-1000", "Created Directory: " + targetDir.getPath());
            } else {
                strongkeyLogger.logp(logger, Level.SEVERE, classname, "createUniqueDir", "APPL-MSG-1000", "CANNOT create Directory:" + targetDir.getPath());
                throw new applianceException(getMaxLenProperty("APPL-ERR-1000") + "CANNOT create Directory: " + targetDir.getPath());
            }
        }
        return path;
    }

    /**
     * Get replication entity name
     *
     * @param entity - the numeric representation of the entity
     * @return String indicating the name of the entity object
     */
    public static String getEntityName(Integer entity) {
        return entitynames.get(entity);
    }

    /**
     * Get replication operation name
     *
     * @param op Integer
     * @return STring indicating the name of the operation
     */
    public static String getRepop(Integer op) {
        return repops.get(op);
    }

    public static void setReplicateStatus(boolean status) {
        replicate = status;
    }

    /**
     * Check if this server is replicating to others
     *
     * @return
     */
    public static boolean replicate() {
        return replicate;
    }

    /**
     * Get the state of the ZMQ Service in English
     *
     * @param state - integer
     * @return String from the Constants file
     */
    public static String getZMQState(int state) {
        switch (state) {
            case applianceConstants.ZMQ_SERVICE_INACTIVE:
                return "ZMQ_SERVICE_INACTIVE";
            case applianceConstants.ZMQ_SERVICE_RUNNING:
                return "ZMQ_SERVICE_RUNNING";
            case applianceConstants.ZMQ_SERVICE_STARTING:
                return "ZMQ_SERVICE_STARTING";
            case applianceConstants.ZMQ_SERVICE_STOPPED:
                return "ZMQ_SERVICE_STOPPED";
            case applianceConstants.ZMQ_SERVICE_STOPPING:
                return "ZMQ_SERVICE_STOPPING";
        }
        return null;
    }

    public static void setupCCSPINDomains() {
        String ccspindomains = getApplianceConfigurationProperty("appliance.cfg.property.enableddomains.ccspin");
        if (ccspindomains != null) {
            String ccspindomainSplit[] = ccspindomains.split(",");
            for (String ccspindomain : ccspindomainSplit) {
                CCSPINDOMAINS.add(Long.parseLong(ccspindomain));
            }
        }
    }

    public static boolean isCCSPinEnabled(Long did) {
        return CCSPINDOMAINS.contains(did);
    }

    /**
     * Generates the current time, as based on the clock of the EJB Tier machine
     * and returns it in milliseconds
     *
     * @return long
     */
    public static long nowMs() {
        return new Date().getTime();
    }

    public static java.sql.Timestamp now() {
        return new java.sql.Timestamp(((new java.util.GregorianCalendar().getTimeInMillis()) / 1000) * 1000);
    }

    /**
     * Gets the time at which the SKLES started up
     *
     * @return String - The boot time
     */
    public static long getBootTime() {
        return boottime;
    }

    /**
     * Gets the value of the message property with the specified key
     *
     * @param key - The key in the message resource file
     * @return String - The value of the specified key from the resource file
     */
    public static String getMessageProperty(String key) {
        return msgrb.getString(key);
    }

    /**
     * Gets the message string for a given key, and replaces the {0} in the
     * message string with the parameter
     *
     * @param key - The String key in the message resource file
     * @param param - The String parameter passed in
     * @return String - The value of the specified key from the resource file
     */
    public static String getMessageWithParam(String key, String param) {
        String s = null;
        if (msgrb != null) {
            try {
                String val = msgrb.getString(key);
                if (val != null) {
                    s = val.replace("{0}", "");
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }
        return s + param;
    }

    public static String getApplianceConfigurationProperty(String key) {
        if (appliancehrb != null) {
            try {
                String s = appliancehrb.getString(key);
                if (s.startsWith("APPLIANCE_HOME")) {
                    return s.replaceFirst("APPLIANCE_HOME", appliancehome);
                } else {
                    return s;
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }
        return defaultApplianceConfig.getString(key);
    }

    public static boolean reloadApplianceConfiguration() {
        ResourceBundle rb;
        //reload appliance config
        try {
            File f = new File(appliancehome + fs + "etc" + fs + "appliance-configuration.properties");
            try (FileInputStream fis = new FileInputStream(f)) {
                rb = new java.util.PropertyResourceBundle(fis);
            }

//            if (rb != null) {
                appliancehrb = rb;

                // Sort properties for readability
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                Enumeration enm = appliancehrb.getKeys();
                List<String> keys = new ArrayList<>();
                while (enm.hasMoreElements()) {
                    keys.add((String) enm.nextElement());
                }

                // Print out local configuration values from SKCE_HOME
                Collections.sort((List<String>) keys);
                for (String key : keys) {
                    baos.write(("\n\t" + key + ": " + appliancehrb.getString(key)).getBytes());
                }

                strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.INFO, "APPL-MSG-1110", baos.toString());
                return true;
//            } else {
//                strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, "APPL-ERR-1113", f.getName());
//            }
        } catch (IOException ex) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, "APPL-ERR-1113", null);
            Logger.getLogger(classname).log(Level.SEVERE, null, ex);
        }
        return false;
    }

    /**
     * Gets the value of the configuration property with the specified key from
     * the MaxLenMap - a hash-table defining the maximum lengths of attributes
     *
     * @param key - The key in the MaxLenMap
     * @return integer - The value of the specified key from the map
     */
    public static int getMaxLenProperty(String key) {
        return maxlenmap.get(key);
    }

    //common between skc and skce
    public static String reversedn(String dn) {
        String[] splitstring = dn.split(",");
        String newdn = "";
        StringBuilder sb = new StringBuilder();
        for (int i = splitstring.length - 1; i >= 0; i--) {
            if (i != 0) {
//                newdn += splitstring[i] + ",";
                sb.append(splitstring[i]).append(",");
            } else {
                sb.append(splitstring[i]);
            }
        }
        newdn = sb.toString();
        return newdn;
    }

    //common between skcc  and fso
    /**
     * DESCRIPTION: Generate a random 6-digit number
     *
     * @return String of the random 6-digit number
     */
    public static String getVerificationCode() {
        String number;
        Random generator = new Random();
        int num = generator.nextInt(899999) + 100000;
        number = Integer.toString(num);
        return number;
    }

    /**
     * Convert the JSON represented as a String into a JsonObject
     *
     * @param jsonstr String containing the JSON
     * @return JsonObject if successful, null otherwise
     */
    public static JsonObject stringToJSON(String jsonstr) {
        if (jsonstr == null || jsonstr.isEmpty()) {
            return null;
        }

        try (JsonReader jsonReader = Json.createReader(new StringReader(jsonstr))) {
            return jsonReader.readObject();
        } catch (Exception ex) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, "APPL-ERR-1003", ex.getLocalizedMessage());
        }
        return null;
    }

    /**
     * Given a JSON string and a search-key, this method looks up the 'key' in
     * the JSON and if found, returns the associated value. Returns NULL in all
     * error conditions.
     *
     * @param jsonstr String containing JSON
     * @param key String containing the search-key
     * @param datatype String containing the data-type of the value-object
     * @return Object containing the value for the specified key if valid; null
     * in all error cases.
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
                strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.FINE, "APPL-ERR-1003", "'" + key + "' does not exist in the json");
                return null;
            }

            switch (datatype) {
                case "Boolean":
                    return json.getBoolean(key);
                case "Int":
                    return json.getInt(key);
                case "Long":
                    return json.getJsonNumber(key).longValueExact();
                case "JsonArray":
                    return json.getJsonArray(key);
                case "JsonNumber":
                    return json.getJsonNumber(key);
                case "JsonObject":
                    return json.getJsonObject(key);
                case "JsonString":
                    return json.getJsonString(key);
                case "String":
                    return json.getString(key);
                default:
                    return null;
            }
        } catch (Exception ex) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, "APPL-ERR-1000", ex.getLocalizedMessage());
            return null;
        }
    }

    public static void putReplicationMaps() {

        //ka
        entitynames.put(applianceConstants.ENTITY_TYPE_KEEP_ALIVE, "ENTITY_TYPE_KEEP_ALIVE");

        //ce
        entitynames.put(applianceConstants.ENTITY_TYPE_FIDO_KEYS, "ENTITY_TYPE_FIDO_KEYS");
        entitynames.put(applianceConstants.ENTITY_TYPE_DOMAINS, "ENTITY_TYPE_DOMAINS");
        entitynames.put(applianceConstants.ENTITY_TYPE_FIDO_USERS, "ENTITY_TYPE_FIDO_USERS");
        entitynames.put(applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO, "ENTITY_TYPE_MAP_USER_SESSION_INFO");
        entitynames.put(applianceConstants.ENTITY_TYPE_FIDO_POLICIES, "ENTITY_TYPE_FIDO_POLICIES");
        entitynames.put(applianceConstants.ENTITY_TYPE_ATTESTATION_CERTIFICATES, "ENTITY_TYPE_ATTESTATION_CERTIFICATES");
        entitynames.put(applianceConstants.ENTITY_TYPE_FIDO_CONFIGURATIONS, "ENTITY_TYPE_FIDO_CONFIGURATIONS");

        repops.put(applianceConstants.REPLICATION_OPERATION_ADD, "REPLICATION_OPERATION_ADD");
        repops.put(applianceConstants.REPLICATION_OPERATION_DELETE, "REPLICATION_OPERATION_DELETE");
        repops.put(applianceConstants.REPLICATION_OPERATION_UPDATE, "REPLICATION_OPERATION_UPDATE");
        repops.put(applianceConstants.REPLICATION_OPERATION_KEEP_ALIVE, "REPLICATION_OPERATION_KEEP_ALIVE");
        repops.put(applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD, "REPLICATION_OPERATION_HASHMAP_ADD");
        repops.put(applianceConstants.REPLICATION_OPERATION_HASHMAP_DELETE, "REPLICATION_OPERATION_HASHMAP_DELETE");
        repops.put(applianceConstants.REPLICATION_OPERATION_HASHMAP_UPDATE, "REPLICATION_OPERATION_HASHMAP_UPDATE");
    }


    /*
    *****************************************************************************************
 888b     d888                   888                        888b     d888
 8888b   d8888                   888                        8888b   d8888
 88888b.d88888                   888                        88888b.d88888
 888Y88888P888  8888b.  888  888 888       .d88b.  88888b.  888Y88888P888  8888b.  88888b.
 888 Y888P 888     "88b `Y8bd8P' 888      d8P  Y8b 888 "88b 888 Y888P 888     "88b 888 "88b
 888  Y8P  888 .d888888   X88K   888      88888888 888  888 888  Y8P  888 .d888888 888  888
 888   "   888 888  888 .d8""8b. 888      Y8b.     888  888 888   "   888 888  888 888 d88P
 888       888 "Y888888 888  888 88888888  "Y8888  888  888 888       888 "Y888888 88888P"
                                                                                   888
                                                                                   888
                                                                                   888
*****************************************************************************************
     */
    private static void setupMaxLenMap() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setupMaxLenMap");

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.4charstring)");
        maxlenmap.put("appliance.cfg.maxlen.4charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.4charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.5charstring)");
        maxlenmap.put("appliance.cfg.maxlen.5charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.5charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.6charstring)");
        maxlenmap.put("appliance.cfg.maxlen.6charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.6charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.7charstring)");
        maxlenmap.put("appliance.cfg.maxlen.7charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.7charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.8charstring)");
        maxlenmap.put("appliance.cfg.maxlen.8charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.8charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.9charstring)");
        maxlenmap.put("appliance.cfg.maxlen.9charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.9charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.10charstring)");
        maxlenmap.put("appliance.cfg.maxlen.10charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.10charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.16charstring)");
        maxlenmap.put("appliance.cfg.maxlen.16charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.16charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.20charstring)");
        maxlenmap.put("appliance.cfg.maxlen.20charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.20charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.32charstring)");
        maxlenmap.put("appliance.cfg.maxlen.32charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.32charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.45charstring)");
        maxlenmap.put("appliance.cfg.maxlen.45charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.45charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.50charstring)");
        maxlenmap.put("appliance.cfg.maxlen.50charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.50charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.64charstring)");
        maxlenmap.put("appliance.cfg.maxlen.64charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.64charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.100charstring)");
        maxlenmap.put("appliance.cfg.maxlen.100charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.100charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.128charstring)");
        maxlenmap.put("appliance.cfg.maxlen.128charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.128charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.200charstring)");
        maxlenmap.put("appliance.cfg.maxlen.200charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.200charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.256charstring)");
        maxlenmap.put("appliance.cfg.maxlen.256charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.256charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.512charstring)");
        maxlenmap.put("appliance.cfg.maxlen.512charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.512charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.1024charstring)");
        maxlenmap.put("appliance.cfg.maxlen.1024charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.1024charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.2048charstring)");
        maxlenmap.put("appliance.cfg.maxlen.2048charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.2048charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.2080charstring)");
        maxlenmap.put("appliance.cfg.maxlen.2080charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.2080charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.4096charstring)");
        maxlenmap.put("appliance.cfg.maxlen.4096charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.4096charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.8192charstring)");
        maxlenmap.put("appliance.cfg.maxlen.8192charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.8192charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.10000charstring)");
        maxlenmap.put("appliance.cfg.maxlen.10000charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.8192charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.16384charstring)");
        maxlenmap.put("appliance.cfg.maxlen.16384charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.16384charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.32768charstring)");
        maxlenmap.put("appliance.cfg.maxlen.32768charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.32768charstring")));

        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setupMaxLenMap", "APPL-MSG-1023", "put(appliance.cfg.maxlen.65535charstring)");
        maxlenmap.put("appliance.cfg.maxlen.65535charstring", Integer.parseInt(getApplianceConfigurationProperty("appliance.cfg.maxlen.65535charstring")));

        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setupMaxLenMap");
    }
}
