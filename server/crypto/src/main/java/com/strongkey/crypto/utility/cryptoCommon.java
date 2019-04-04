/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.crypto.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECGenParameterSpec;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPoint;
import java.security.spec.ECPublicKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.crypto.EntropySourceProvider;
import org.bouncycastle.crypto.fips.FipsDRBG;
import org.bouncycastle.crypto.util.BasicEntropySourceProvider;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Base64;

@SuppressWarnings("StaticNonFinalUsedInInitialization")
public final class cryptoCommon {

    private static final String classname = "cryptoCommon";
    public static final String fs = System.getProperty("file.separator");

    // Property files used by this application for configuration info
    private static final ResourceBundle defaultConfig = ResourceBundle.getBundle("resources.crypto-configuration");

    // Crypto home-directory specific property file
    private static ResourceBundle cryhrb = null;

    // Property files used by this application for application messages
    private static final ResourceBundle msgrb = ResourceBundle.getBundle("resources.crypto-messages");

    // Version of Cryptolib (Build #)
    private static final ResourceBundle vrb = ResourceBundle.getBundle("resources.crypto-version");

    //Resource bundle for the appliance config
    private static ResourceBundle appliancehrb = null;

    private static final String appliancehome = "/usr/local/strongkey/appliance";

    // Logger for the application
    private static final Logger logger = Logger.getLogger("CRYPTO", "resources.crypto-messages");

    // Map for the maximum length values
    private static Map<String, Integer> maxlenmap = new ConcurrentHashMap<>();

    // Location where StrongKey Lite is installed on this machine
    private static String cryptohome;

    /**
     * Private SortedMap of Configuration Maps. Each Configuration map * has the
     * customized configuration of a domain. If a domain has not * customized
     * its configuration, the getProperty() method will get the * value of the
     * property-key from the defaultProperties object.
     *
     */
    
    private static SortedMap<Long, Map<String, String>> configmap = new ConcurrentSkipListMap<>();
    
    public static final int EC_POINTSIZE               = 32;

    private static BouncyCastleFipsProvider BC_FIPS_PROVIDER = new BouncyCastleFipsProvider();
    private static final SecureRandom FIPS_DRBG;

    /**
     * *****************************************
     * d8b d8b 888 Y8P Y8P 888 888 888 88888b. 888 888888 888 888 "88b 888 888
     * 888 888 888 888 888 888 888 888 888 Y88b. 888 888 888 888 "Y888
*******************************************
     */
    /**
     * A static initializer block to get the ResourceBundle, setup logging and
     * get other parameters initialized
     */
    static {
        String personalizationString = getConfigurationProperty("crypto.cfg.property.fipsdrbg.personalizationstring");
        if (personalizationString == null || personalizationString.length() == 0) {
            try {
                personalizationString = InetAddress.getLocalHost().getCanonicalHostName() + new Date().toString();
            } catch (UnknownHostException ex) {
                personalizationString = new Date().toString();
            }
        }

        byte[] nonce;
        int securitystrength = Integer.parseInt(getConfigurationProperty("crypto.cfg.property.fipsdrbg.securitystrength"));
        nonce = new byte[securitystrength];
        new SecureRandom().nextBytes(nonce);

        EntropySourceProvider entSource = new BasicEntropySourceProvider(new SecureRandom(), true);
        FipsDRBG.Builder drgbBldr = FipsDRBG.SHA512.fromEntropySource(entSource)
                .setSecurityStrength(securitystrength)
                .setEntropyBitsRequired(securitystrength)
                .setPersonalizationString(personalizationString.getBytes());
        FIPS_DRBG = drgbBldr.build(nonce, true);

        CryptoServicesRegistrar.setSecureRandom(FIPS_DRBG);

        setupMaxLenMap();

        /**
         * Print out the values of the central configuration properties built
         * into the application - sort it for readability
         */
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        Enumeration<String> enm = defaultConfig.getKeys();
        List<String> keys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            keys.add(enm.nextElement());
        }

        Collections.sort(keys);
        Iterator<String> it = keys.iterator();
        try {
            while (it.hasNext()) {
                String key = it.next();
                baos.write(("\n\t" + key + ": " + defaultConfig.getString(key)).getBytes());
            }
            baos.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        log(Level.INFO, "CRYPTO-MSG-1052", baos.toString());

        /**
         * Check environment variable for installation location; if not found
         * get default location specified in the configuration properties file.
         *
         * NOTE: Cannot use getConfigurationProperty() method to get this
         * property as it will lead to an ExceptionInitializerError - the method
         * requires cryptohome to be non-null and if cryptohome itself tries to
         * use the method, it will be null.
         */
        if ((cryptohome = System.getenv("CRYPTO_HOME")) == null) {
            cryptohome = defaultConfig.getString("crypto.cfg.property.cryptohome");
        }

        log(Level.INFO, "CRYPTO-MSG-1053", "CRYPTO_HOME is: " + cryptohome);

        // See if there is an over-riding properties file in CRYPTO_HOME
        try {
            File f = new File(cryptohome + fs + "etc" + fs + "crypto-configuration.properties");
            FileInputStream fis = new FileInputStream(f);
            cryhrb = new java.util.PropertyResourceBundle(fis);
            log(Level.INFO, "CRYPTO-MSG-1053",
                    "Using crypto-configuration.properties from CRYPTO_HOME directory: "
                    + cryptohome + "/etc/crypto-configuration.properties");
            fis.close();

            // Sort properties for readability
            baos = new ByteArrayOutputStream();
            enm = cryhrb.getKeys();
            keys = new ArrayList<>();
            while (enm.hasMoreElements()) {
                keys.add(enm.nextElement());
            }

            Collections.sort(keys);
            it = keys.iterator();

            while (it.hasNext()) {
                String key = it.next();
                baos.write(("\n\t" + key + ": " + cryhrb.getString(key)).getBytes());
            }
            baos.close();

        } catch (java.io.FileNotFoundException ex) {
            log(Level.WARNING, "CRYPTO-MSG-1053", "There is no crypto-configuration.properties in the "
                    + "CRYPTO_HOME directory; using system-wide crypto-configuration.properties");
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        // Print out local configuration values from CRYPTO_HOME
        log(Level.INFO, "CRYPTO-MSG-1054", baos.toString());

        try {
            File f = new File(appliancehome + fs + "etc" + fs + "appliance-configuration.properties");
            /**
             * Using try-with-resources; which will take care of closing the
             * FileInputStream fis in any case (success or failure)
             */
            try (FileInputStream fis = new FileInputStream(f)) {
                appliancehrb = new java.util.PropertyResourceBundle(fis);
            }

            log(Level.INFO, "CRYPTO-MSG-1053",
                    "Using appliance-configuration.properties from APPLIANCE_HOME directory: "
                    + appliancehome + "/etc/appliance-configuration.properties");

            // Sort properties for readability
            baos = new ByteArrayOutputStream();
            enm = appliancehrb.getKeys();
            keys = new ArrayList<>();
            while (enm.hasMoreElements()) {
                keys.add(enm.nextElement());
            }

            Collections.sort(keys);
            it = keys.iterator();

            while (it.hasNext()) {
                String key = it.next();
                baos.write(("\n\t" + key + ": " + appliancehrb.getString(key)).getBytes());
            }
            baos.close();

        } catch (java.io.FileNotFoundException ex) {
            log(Level.WARNING, "CRYPTO-MSG-1053", "There is no appliance-configuration.properties in the "
                    + "APPLIANCE_HOME directory;");
        } catch (IOException ex) {
            printStackTrace(ex);
        }

        // Print out local configuration values from APPLIANCE_HOME
        log(Level.INFO, "CRYPTO-MSG-1131", baos.toString());

        // Print SKLES version
        log(Level.INFO, "CRYPTO-MSG-1053", "CRYPTO VERSION is: " + vrb.getString("version"));

        // Add BouncyCastleFIPS Provider 
        Security.addProvider(BC_FIPS_PROVIDER);
    }

    /**
     * Creates a new instance of common
     */
    public cryptoCommon() {
    }

    /**
     *
     * @param publickeybytes
     * @return
     * @throws java.security.spec.InvalidKeySpecException
     * @throws java.security.NoSuchAlgorithmException
     * @throws java.security.NoSuchProviderException
     * @throws java.security.spec.InvalidParameterSpecException
     */
    public static ECPublicKey getUserECPublicKey(byte[] publickeybytes) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException {

        //append the sign byte to the arrays
        byte[] processedXData = new byte[EC_POINTSIZE];
        byte[] processedYData = new byte[EC_POINTSIZE];
        System.arraycopy(publickeybytes, 1, processedXData, 0, EC_POINTSIZE);
        System.arraycopy(publickeybytes, EC_POINTSIZE + 1, processedYData, 0, EC_POINTSIZE);

        ECPoint pubPoint = new ECPoint(new BigInteger(1, processedXData), new BigInteger(1, processedYData));
        AlgorithmParameters params = AlgorithmParameters.getInstance("EC", BC_FIPS_PROVIDER);
        params.init(new ECGenParameterSpec("prime256v1"));
        ECParameterSpec ecParameters = params.getParameterSpec(ECParameterSpec.class);
        ECPublicKeySpec pubECSpec = new ECPublicKeySpec(pubPoint, ecParameters);
        return (ECPublicKey) KeyFactory.getInstance("EC", BC_FIPS_PROVIDER).generatePublic(pubECSpec);
    }

    public static ECPublicKey getUserECPublicKey(byte[] x, byte[] y, String curveString) throws InvalidKeySpecException, NoSuchAlgorithmException, NoSuchProviderException, InvalidParameterSpecException {

        //append the sign byte to the arrays
        ECPoint pubPoint = new ECPoint(new BigInteger(1, x), new BigInteger(1, y));
        AlgorithmParameters params = AlgorithmParameters.getInstance("EC", BC_FIPS_PROVIDER);
        params.init(new ECGenParameterSpec(curveString));
        ECParameterSpec ecParameters = params.getParameterSpec(ECParameterSpec.class);
        ECPublicKeySpec pubECSpec = new ECPublicKeySpec(pubPoint, ecParameters);
        return (ECPublicKey) KeyFactory.getInstance("EC", BC_FIPS_PROVIDER).generatePublic(pubECSpec);
    }

    /**
     * Verifies a digital signature using the SHA384withECDSA algorithm
     *
     * @param signature byte[] with the digital signature
     * @param publickey PublicKey using the ECDSA algorithm
     * @param signedobject String object digitally signed
     * @return boolean indicator if the signature verifies or not
     */
    public static boolean verifySignature(byte[] signature, PublicKey publickey, String signedobject) {
        try {
            Signature sig = Signature.getInstance("SHA256withECDSA", BC_FIPS_PROVIDER);
            sig.initVerify(publickey);
            sig.update(DatatypeConverter.parseBase64Binary(signedobject));
            return sig.verify(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            logp(Level.FINE, classname, "verifySignature", "CRYPTO-MSG-1000", "ex=" + printStackTrace(ex));
        }
        return false;
    }

    public static boolean verifyRSASignature(byte[] signature, PublicKey publickey, String signedobject) {
        try {
            Signature sig = Signature.getInstance("SHA256withRSA", BC_FIPS_PROVIDER);
            sig.initVerify(publickey);
            sig.update(DatatypeConverter.parseBase64Binary(signedobject));
            return sig.verify(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            logp(Level.FINE, classname, "verifySignature", "CRYPTO-MSG-1000", "ex=" + printStackTrace(ex));
        }
        return false;
    }

    public static boolean verifySignature(byte[] signature, PublicKey publickey, byte[] signedobject, String algorithm) {
        try {
            Signature sig = Signature.getInstance(algorithm, BC_FIPS_PROVIDER);
            sig.initVerify(publickey);
            sig.update(signedobject);
            return sig.verify(signature);

        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException ex) {
            logp(Level.FINE, classname, "verifySignature", "CRYPTO-MSG-1000", "ex=" + printStackTrace(ex));
        }
        return false;
    }

    public static byte[] calculateHmac(SecretKey key, byte[] data, String algorithm) {
        try {
            Mac mac = Mac.getInstance(algorithm, "BCFIPS");
            mac.init(key);
            return mac.doFinal(data);
        } catch (NoSuchAlgorithmException | InvalidKeyException | NoSuchProviderException ex) {
            logp(Level.FINE, classname, "verifySignature", "CRYPTO-MSG-1000", "ex=" + printStackTrace(ex));
        }
        return null;
    }

    /**
     * Method to verify attestation certificate
     *
     * @param attestationCertificate - the attestation cert to be verified
     * @return - boolean, based on the result of verification
     */
    public static boolean verifyU2FAttestationCertificate(X509Certificate attestationCertificate) {

        PublicKey attcertPublicKey = attestationCertificate.getPublicKey();
        byte[] attPublicKey = attcertPublicKey.getEncoded();
        SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(attPublicKey));
        spki.getAlgorithm();

        //  get algorithm from the AlgorithmIdentifier refer to RFC 5480 
        AlgorithmIdentifier sigAlgId = spki.getAlgorithm();
        ASN1ObjectIdentifier asoi = sigAlgId.getAlgorithm();

        if (!(asoi.getId().equals("1.2.840.10045.2.1"))) {
            //not an EC Public Key 
            logp(Level.SEVERE, classname, "verifyAttestationCertificate", "FIDO-ERR-5008", "Only Elliptic-Curve (EC) keys are allowed, the public key in this certificate not an EC public key");
            return false;
        }

        //  Get parameters from AlgorithmIdentifier, parameters field is optional RFC 5480,
        ASN1Encodable asne = sigAlgId.getParameters();
        if (asne == null) {
            logp(Level.WARNING, classname, "verifyAttestationCertificate", "FIDO-WARN-5001", "");
        } else {
            if (!(asne.toString().equals("1.2.840.10045.3.1.7"))) { //key not generated using curve secp256r1
                logp(Level.SEVERE, classname, "verifyAttestationCertificate", "FIDO-ERR-5009", "");
                return false;
            }
        }

        logp(Level.FINE, classname, "verifyAttestationCertificate", "FIDO-MSG-5025", "");
        return true;
    }

    public static X509Certificate generateX509FromBytes(byte[] certificateBytes) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BCFIPS");
            InputStream instr = new ByteArrayInputStream(certificateBytes);
            return (X509Certificate) certFactory.generateCertificate(instr);
        } catch (CertificateException | NoSuchProviderException ex) {
            logp(Level.SEVERE, classname, "generateX509FromBytes", "CRYPTO-MSG-1000", printStackTrace(ex));
        }
        return null;
    }

    public static X509Certificate generateX509FromInputStream(InputStream instr) {
        try {
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509", "BCFIPS");
            return (X509Certificate) certFactory.generateCertificate(instr);
        } catch (CertificateException | NoSuchProviderException ex) {
            logp(Level.SEVERE, classname, "generateX509FromBytes", "CRYPTO-MSG-1000", printStackTrace(ex));
        }
        return null;
    }

    public static String calculateHash(String contentToEncode, String hash) {
        try {
            MessageDigest digest = MessageDigest.getInstance(hash);
            digest.update(contentToEncode.getBytes());
            return Base64.toBase64String(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            logp(Level.SEVERE, classname, "generateX509FromBytes", "CRYPTO-MSG-1000", printStackTrace(ex));
        }
        return null;
    }

    /**
     * Prints the source-class and method names to the application logger upon
     * entering the class method
     *
     * @param sourceClass - the classname of the class that called this method
     * @param sourceMethod - the name of the method in which this method is
     * called
     */
    public static void entering(String sourceClass, String sourceMethod) {
        logger.entering(sourceClass, sourceMethod);
    }

    /**
     * Prints the source-class and method names to the application logger before
     * exiting the class method
     *
     * @param sourceClass - the classname of the class that called this method
     * @param sourceMethod - the name of the method in which this method is
     * called
     */
    public static void exiting(String sourceClass, String sourceMethod) {
        logger.exiting(sourceClass, sourceMethod);
    }

    /**
     * Prints the appropriate information to the application logger Databeans
     * cannot have the java.util.logging.logger class as it is not serializable.
     *
     * @param level - Level at which the message should be logged
     * @param key - Property key for this message
     * @param param - Any parameters specified with this message
     */
    public static void log(java.util.logging.Level level, String key, Object param) {
        logger.log(level, key, param);
    }

    /**
     * Prints the appropriate information to the application logger Databeans
     * cannot have the java.util.logging.logger class as it is not serializable.
     *
     * @param level - Level at which the message should be logged
     * @param key - Property key for this message
     * @param params - An array of parameters specified with this message
     */
    public static void log(java.util.logging.Level level, String key, Object[] params) {
        logger.log(level, key, params);
    }

    /**
     * Prints the appropriate information to the application logger with the
     * source-class and method names in the logged message Databeans cannot have
     * the java.util.logging.logger class as it is not serializable.
     *
     * @param level - Level at which the message should be logged
     * @param sourceClass - the classname of the class that called this method
     * @param sourceMethod - the name of the method in which this method is
     * called
     * @param key - Property key for this message
     * @param param - Any parameters specified with this message
     */
    public static void logp(java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key, Object param) {
        logger.logp(level, sourceClass, sourceMethod, key, param);
    }

    /**
     * Prints the appropriate information to the application logger with the
     * source class and method names in the logged message Databeans cannot have
     * the java.util.logging.logger class as it is not serializable.
     *
     * @param level - Level at which the message should be logged
     * @param sourceClass - the classname of the class that called this method
     * @param sourceMethod - the name of the method in which this method is
     * called
     * @param key - Property key for this message
     * @param params - An array of parameters specified with this message
     */
    public static void logp(java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key, Object[] params) {
        logger.logp(level, sourceClass, sourceMethod, key, params);
    }

    /**
     * Prints the appropriate information to the application logger with the
     * source class and method names in the logged message Databeans cannot have
     * the java.util.logging.logger class as it is not serializable.
     *
     * @param level - Level at which the message should be logged
     * @param sourceClass - the classname of the class that called this method
     * @param sourceMethod - the name of the method in which this method is
     * called
     * @param key - Property key for this message
     */
    public static void logp(java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key) {
        logger.logp(level, sourceClass, sourceMethod, key);
    }

    /**
     * Creates the equivalent of Java's printStackTrace() so it can be returned
     * as a String for logging with the log facility.
     *
     * @param ex - the exception caught by the calling code
     * @return String value containing the stack-trace
     */
    public static String printStackTrace(Exception ex) {
        StackTraceElement err[] = ex.getStackTrace();
        StringBuilder sb = new StringBuilder(1024);
        sb.append(ex.fillInStackTrace()).append('\n');
        for (StackTraceElement err1 : err) {
            sb.append('\t').append(err1).append('\n');
        }

        return sb.append('\n').toString();
    }

    /**
     * ***********************************************************
     * 888 888 o 888 d8b .d88b. .d88b. 888888 d888b d88P"88b d8P Y8b 888
     * "Y888888888P" 888 888 88888888 888 "Y88888P" Y88b 888 Y8b. Y88b.
     * d88P"Y88b "Y88888 "Y8888 "Y888 dP" "Yb 888 Y8b d88P "Y88P"
     ************************************************************
     */
    /**
     * Gets the value of the property with the specified key from either the
     * CRYPTO_HOME home-directory - if the property file exists there - or from
     * the system-wide properties file.
     *
     * Additionally, if property-value has the $CRYPTO_HOME variable embedded in
     * it, then it replaces the variable with the actual value of CRYPTO_HOME in
     * the property-value and returns it.
     *
     * @param key - The key in the resource file
     * @return String - The value of the specified key from the resource file
     */
    public static String getConfigurationProperty(String key) {
        if (cryhrb != null) {
            try {
                String s = cryhrb.getString(key);
                if (s.startsWith("CRYPTO_HOME")) {
                    return s.replaceFirst("CRYPTO_HOME", cryptohome);
                } else {
                    return s;
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }

        String s = defaultConfig.getString(key);
        if (s.startsWith("CRYPTO_HOME")) {
            return s.replaceFirst("CRYPTO_HOME", cryptohome);
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
        logp(Level.FINE, classname, "getConfigurationProperty", "CRYPTO-MSG-1056", did + "-" + k);

        // First check for the domain in the configmap
        if (configmap.containsKey(did)) {
            logp(Level.FINE, classname, "getConfigurationProperty", "CRYPTO-MSG-1057", did);
            Map<String, String> m = configmap.get(did);
            logp(Level.FINE, classname, "getConfigurationProperty", "CRYPTO-MSG-1058", k + " [DID=" + did + "]");
            if (m != null) {
                if (m.containsKey(k)) {
                    return m.get(k);
                }
            }
        }

        // Default - in case returned map and DB have no value with the key k
        logp(Level.FINE, classname, "getConfigurationProperty", "CRYPTO-MSG-1059", k + " [DID=" + did + ", KEY=" + k + ", VALUE=" + getConfigurationProperty(k) + "]");
        return getConfigurationProperty(k);
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
                    s = val.replace("{0}", param);
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }
        return s;
    }

    /**
     * Gets the message string for a given key, and replaces the {0} in the
     * message string with the parameter
     *
     * @param key - The String key in the message resource file
     * @param param - The String parameter passed in
     * @return String - The value of the specified key from the resource file
     */
    public static String getMessageWithParam(String key, String[] param) {
        String s = null;
        if (msgrb != null) {
            try {
                s = msgrb.getString(key);
                if (s != null) {
                    for (int i = 0; i < param.length; i++) {
                        s = s.replace("{" + i + "}", param[i]);
                    }
                }
            } catch (java.util.MissingResourceException ex) {
                // Do nothing
            }
        }
        return s;
    }

    public static SecureRandom getSecureRandom() {
        return FIPS_DRBG;
    }

    private static void setupMaxLenMap() {
        logger.entering(classname, "setupMaxLenMap");

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.4charstring)");
        maxlenmap.put("crypto.cfg.maxlen.4charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.4charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.5charstring)");
        maxlenmap.put("crypto.cfg.maxlen.5charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.5charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.6charstring)");
        maxlenmap.put("crypto.cfg.maxlen.6charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.6charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.8charstring)");
        maxlenmap.put("crypto.cfg.maxlen.8charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.8charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.9charstring)");
        maxlenmap.put("crypto.cfg.maxlen.9charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.9charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.10charstring)");
        maxlenmap.put("crypto.cfg.maxlen.10charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.10charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.16charstring)");
        maxlenmap.put("crypto.cfg.maxlen.16charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.16charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.20charstring)");
        maxlenmap.put("crypto.cfg.maxlen.20charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.20charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.32charstring)");
        maxlenmap.put("crypto.cfg.maxlen.32charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.32charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.64charstring)");
        maxlenmap.put("crypto.cfg.maxlen.64charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.64charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.128charstring)");
        maxlenmap.put("crypto.cfg.maxlen.128charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.128charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.256charstring)");
        maxlenmap.put("crypto.cfg.maxlen.256charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.256charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.512charstring)");
        maxlenmap.put("crypto.cfg.maxlen.512charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.512charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.1024charstring)");
        maxlenmap.put("crypto.cfg.maxlen.1024charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.1024charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.2048charstring)");
        maxlenmap.put("crypto.cfg.maxlen.2048charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.2048charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.2080charstring)");
        maxlenmap.put("crypto.cfg.maxlen.2080charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.2080charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.4096charstring)");
        maxlenmap.put("crypto.cfg.maxlen.4096charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.4096charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.8192charstring)");
        maxlenmap.put("crypto.cfg.maxlen.8192charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.8192charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.16384charstring)");
        maxlenmap.put("crypto.cfg.maxlen.16384charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.16384charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.32768charstring)");
        maxlenmap.put("crypto.cfg.maxlen.32768charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.32768charstring")));

        logger.logp(Level.FINE, classname, "setupMaxLenMap", "CRYPTO-MSG-1023", "put(crypto.cfg.maxlen.65535charstring)");
        maxlenmap.put("crypto.cfg.maxlen.65535charstring", Integer.parseInt(getConfigurationProperty("crypto.cfg.maxlen.65535charstring")));

        logger.exiting(classname, "setupMaxLenMap");
    }
}
