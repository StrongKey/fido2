/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.crypto.utility;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
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
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.SortedMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x500.X500Name;
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
    
    private static KeyStore keystore;
    
     private static KeyStore jwtKeystore;

    /**
     * Private SortedMap of Configuration Maps. Each Configuration map * has the
     * customized configuration of a domain. If a domain has not * customized
     * its configuration, the getProperty() method will get the * value of the
     * property-key from the defaultProperties object.
     *
     */

    private static SortedMap<Long, Map<String, String>> configmap = new ConcurrentSkipListMap<>();
    private static SortedMap<String, PrivateKey> pvkeymap = new ConcurrentSkipListMap<>();
    private static SortedMap<String, PublicKey> publickeymap = new ConcurrentSkipListMap<>();
    private static ArrayList<PrivateKey> jwtpvkeylist ;
    private static ArrayList<X509Certificate> jwtcertlist;
    private static ArrayList<PublicKey> jwtpublickeylist = new ArrayList<>();
    private static SortedMap<String,BlockingQueue<List>> jwtsignqMap = new ConcurrentSkipListMap<>();
    private static SortedMap<String,BlockingQueue<Signature>> jwtverifyqMap =  new ConcurrentSkipListMap<>();  //new LinkedBlockingQueue<Signature>();
    private static SortedMap<String,X509Certificate> jwtCAcertMap =  new ConcurrentSkipListMap<>();
    private static SortedMap<BigInteger,String>jwtcertserialmap =  new ConcurrentSkipListMap<>();
    
    private static Integer jwtthreads;
    private static String jwtkeystorelocation;
    private static Integer certPerServer;
    private static String jwtpassword;
    private static String jwttruststorelocation;
    private static String jwtsigningalgorithm;
        

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
    public cryptoCommon() throws CryptoException {
       
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

    public static void loadVerificationKey(String did, String secret, String signingdn) throws CryptoException {
        // Keystore location
        String truststorelocation;
        try {
            if ((truststorelocation = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.truststorelocation")) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation"));
        }

        PublicKey pbk = null;
        InputStream is = null;
        try {
            KeyStore truststore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            is = new FileInputStream(truststorelocation);
            truststore.load(is, secret.toCharArray());
            cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureVerificationKey", "CRYPTO-MSG-2521", truststorelocation);

            // Print out certs in the truststore
            String alias;
            X500Name inputdn = new X500Name(signingdn);
            cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureVerificationKey", "CRYPTO-MSG-2520", signingdn);
            for (Enumeration<String> e = truststore.aliases(); e.hasMoreElements();) {
                alias = e.nextElement();
                cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureVerificationKey", "CRYPTO-MSG-2522", alias);
                X509Certificate cert = (X509Certificate) truststore.getCertificate(alias);
                X500Name xcdn = new X500Name(cert.getSubjectX500Principal().getName());
                cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureVerificationKey", "CRYPTO-MSG-2515", xcdn + " [" + alias + "]");

                // Match using the X500Names
                if (xcdn.equals(inputdn)) {
                    cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureVerificationKey", "CRYPTO-MSG-2523", signingdn);
                    boolean[] keyusage = cert.getKeyUsage();

                    // Collect key-usages in a string buffer for logging
                    java.io.StringWriter sw = new java.io.StringWriter();
                    for (int i = 0; i < keyusage.length; i++) {
                        sw.write("\nkeyusage[" + i + "]: " + keyusage[i]);
                    }
                    cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureVerificationKey", "CRYPTO-MSG-2517", sw.toString());

                    // Now match for the signing bit
                    if (keyusage[0]) {
                        // If true, this is the certificate we want
                        pbk = cert.getPublicKey();
                        cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureVerificationKey", "CRYPTO-MSG-2524", signingdn + " [" + alias + "]");
                        break;
                    }
                }
            }

            if(pbk!=null){
                X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(pbk.getEncoded());
//            KeyFactory kf = KeyFactory.getInstance("RSA");
                KeyFactory kf = KeyFactory.getInstance("EC");

                PublicKey pKey = kf.generatePublic(X509publicKey);
                publickeymap.put(did, pKey);
            }
            
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | InvalidKeySpecException | IOException | NullPointerException ex) {
            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureVerificationKey", "CRYPTO-ERR-2507", ex.getLocalizedMessage());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2507", ex.getLocalizedMessage()));
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(cryptoCommon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
    
    public static void loadSigningKey(String did, String secret, String signingdn) throws CryptoException {
        String keystoreurl;

        try {
            if ((keystoreurl = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.keystorelocation")) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.signing.keystorelocation");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.signing.keystorelocation");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation"));
        }

        InputStream is = null;
        try {
            keystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            is = new FileInputStream(keystoreurl);
            keystore.load(is, secret.toCharArray());
        

            // get the private key
            // Convert signingdn to an BouncyCastle X500Name-compatible DN
            X509Certificate cert;               // X509 Certificate object
            PrivateKey pvk = null;
                     // RSA Private key object
            boolean[] keyusage;
            X500Name xsdn = new X500Name(signingdn);

            // Print out certs in the keystore
            String alias;
            cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2520", signingdn);
            for (Enumeration<String> e = keystore.aliases(); e.hasMoreElements();) {
                alias = e.nextElement();
                cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2514", alias);
                if (!alias.endsWith(".cert")) {
                    continue;
                }
                cert = (X509Certificate) keystore.getCertificate(alias);
                X500Name xcdn = new X500Name(cert.getSubjectX500Principal().getName());
                cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2515", xcdn + " [" + alias + "]");

                // First match the subject DN
                if (xcdn.equals(xsdn)) {
                    cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2516", signingdn);
                    keyusage = cert.getKeyUsage();

                    // Collect key-usages in a string buffer for logging
                    StringWriter sw = new java.io.StringWriter();
                    for (int i = 0; i < keyusage.length; i++) {
                        sw.write("\nkeyusage[" + i + "]: " + keyusage[i]);
                    }
                    cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2517", sw.toString());

                    // Now match for the signing bit
                    if (keyusage[0]) {
                        // If true, this is the certificate we want
                        String pvkalias = alias.substring(0, alias.indexOf(".")); // Get rid of the .cert in alias
                        pvk = ((KeyStore.PrivateKeyEntry) keystore.getEntry(pvkalias, new KeyStore.PasswordProtection(secret.toCharArray()))).getPrivateKey();
                        cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2518", signingdn + " [" + alias + "]");
                        break;
                    }
                }
            }
            if (pvk != null) {
                PKCS8EncodedKeySpec X509privateKey = new PKCS8EncodedKeySpec(pvk.getEncoded());
//            KeyFactory kf = KeyFactory.getInstance("RSA");
                KeyFactory kf = KeyFactory.getInstance("EC");

                PrivateKey pKey = kf.generatePrivate(X509privateKey);
                pvkeymap.put(did, pKey);
            }
            
        } catch (KeyStoreException | NoSuchAlgorithmException | IOException ex) {
            ex.printStackTrace();
            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2506", ex.getLocalizedMessage());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2506", ex.getLocalizedMessage()));
        } catch (CertificateException | UnrecoverableEntryException | InvalidKeySpecException ex) {
            ex.printStackTrace();
        }
        finally{
            try {
                if(is!=null)
                    is.close();
            } catch (IOException ex) {
                Logger.getLogger(cryptoCommon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
       
    public static void loadJWTCACert(String did) throws CryptoException {
       
        if(jwttruststorelocation==null || jwtpassword==null || 
                jwtthreads==null || jwtkeystorelocation==null ||
                jwtsigningalgorithm==null){
            loadJWTProperties();
        }
        

        InputStream is = null;
        try {
            KeyStore truststore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            is = new FileInputStream(jwttruststorelocation);
            truststore.load(is, jwtpassword.toCharArray());
            cryptoCommon.logp(Level.FINE, classname, "getJWTSignatureVerificationKeys", "CRYPTO-MSG-2521", jwttruststorelocation);
            // Print out certs in the truststore
            String alias;
            for (Enumeration<String> e = truststore.aliases(); e.hasMoreElements();) {
                alias = e.nextElement();
                cryptoCommon.logp(Level.FINE, classname, "getJWTSignatureVerificationKeys", "CRYPTO-MSG-2522", alias);
                if(alias.equals("jwtCA-"+did)){
                    X509Certificate cert = (X509Certificate) truststore.getCertificate(alias);
                    jwtCAcertMap.put(did,cert);
              }       
            }      
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | NullPointerException ex) {
            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureVerificationKey", "CRYPTO-ERR-2507", ex.getLocalizedMessage());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2507", ex.getLocalizedMessage()));
        }
        finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                Logger.getLogger(cryptoCommon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
    }
     public static void loadJWTVerifyKeys(String did, String sid) throws CryptoException {
        
        if(jwttruststorelocation==null || jwtpassword==null || 
                jwtthreads==null || jwtkeystorelocation==null ||
                jwtsigningalgorithm==null){
            loadJWTProperties();
        }
        
       

        InputStream ist = null;
        try {
            KeyStore truststore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            ist = new FileInputStream(jwttruststorelocation);
            truststore.load(ist, jwtpassword.toCharArray());
            //load each certificate
            String alias;
            for (Enumeration<String> e = truststore.aliases(); e.hasMoreElements();) {
                alias = e.nextElement();
                cryptoCommon.logp(Level.FINE, classname, "getJWTSignatureVerificationKeys", "CRYPTO-MSG-2522", alias);
                String[] aliasSplit = alias.split("-");
                //check if jwtsigningcert 
                if(aliasSplit[0].equals("jwtsigningcert")){
                    X509Certificate cert = (X509Certificate) truststore.getCertificate(alias);
                    BlockingQueue<Signature> jwtsignq = new LinkedBlockingQueue<Signature>();
                    for (int i = 0; i < (jwtthreads/3); i++) {
                        try {
                            Signature s = Signature.getInstance(jwtsigningalgorithm);
                            s.initVerify(cert.getPublicKey());
                            jwtsignq.put(s);                          
                        } catch (NoSuchAlgorithmException | InvalidKeyException  ex) {
                            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2506", ex.getLocalizedMessage()));
                        }
                        
                     }
                    jwtverifyqMap.put(alias,jwtsignq);
                    jwtcertserialmap.put(cert.getSerialNumber(),alias);
                }
              }
        } catch (KeyStoreException | NoSuchAlgorithmException | IOException | InterruptedException ex) {
            ex.printStackTrace();
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTSignatureSigningKeys", "CRYPTO-ERR-2506", ex.getLocalizedMessage());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2506", ex.getLocalizedMessage()));
        } catch (CertificateException  ex) {
            ex.printStackTrace();
        }
        finally{
            try {
                if(ist!=null)
                    ist.close();
            } catch (IOException ex) {
                Logger.getLogger(cryptoCommon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    public static void loadJWTSigningKeys(String did, String sid) throws CryptoException{
        
        jwtpvkeylist = new ArrayList<>();
        jwtcertlist = new ArrayList<>();
        if(jwttruststorelocation==null || jwtpassword==null || 
                jwtthreads==null || jwtkeystorelocation==null ||
                jwtsigningalgorithm==null){
            loadJWTProperties();
        }
       

        InputStream is = null;
        InputStream ist = null;
        try {
            jwtKeystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            is = new FileInputStream(jwtkeystorelocation);
            jwtKeystore.load(is, jwtpassword.toCharArray());    
            //load each certificate
            String alias;
            for (Enumeration<String> e = jwtKeystore.aliases(); e.hasMoreElements();) {
                alias = e.nextElement();
                cryptoCommon.logp(Level.FINE, classname, "getJWTSignatureVerificationKeys", "CRYPTO-MSG-2522", alias);
                String[] aliasSplit = alias.split("-");
                //check for did and sid 
                if(aliasSplit[0].equals("jwtsigningcert")){
                    String[] idSplit = aliasSplit[1].split("\\.");
                    if( idSplit[0].equals(sid) && idSplit[1].equals(did)){
                        PrivateKey pvk = ((KeyStore.PrivateKeyEntry) jwtKeystore.getEntry(alias, new KeyStore.PasswordProtection(jwtpassword.toCharArray()))).getPrivateKey();
                        PKCS8EncodedKeySpec X509privateKey = new PKCS8EncodedKeySpec(pvk.getEncoded());
                        KeyFactory kf = KeyFactory.getInstance("EC");
                        PrivateKey pKey = kf.generatePrivate(X509privateKey);
                        jwtpvkeylist.add(pKey);
                        X509Certificate cert = (X509Certificate) jwtKeystore.getCertificate(alias);
                        jwtcertlist.add(cert);
                    }
                }
              }
            // Setup signing instances
            int pvki = 0;
            BlockingQueue<List> jwtsignq = new LinkedBlockingQueue<List>();
            for (int i = 0; i < jwtthreads; i++) {
                try {
                    Signature s = Signature.getInstance(jwtsigningalgorithm);
                    s.initSign(jwtpvkeylist.get(pvki));
                    List<Object> list = new ArrayList<Object>();
                    list.add(s);
                    list.add(jwtcertlist.get(pvki));
                    jwtsignq.put(list);

                } catch (NoSuchAlgorithmException | InvalidKeyException  ex) {
                    throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2506", ex.getLocalizedMessage()));
                }
                pvki++;
                if(pvki >= certPerServer){
                    pvki=0;
                } 
             }
             jwtsignqMap.put(did, jwtsignq);
        } catch (KeyStoreException | InvalidKeySpecException | NoSuchAlgorithmException | UnrecoverableEntryException | IOException |  InterruptedException  ex) {
            ex.printStackTrace();
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTSignatureSigningKeys", "CRYPTO-ERR-2506", ex.getLocalizedMessage());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2506", ex.getLocalizedMessage()));
        } catch (CertificateException  ex) {
            ex.printStackTrace();
        }
        finally{
            try {
                if(is!=null)
                    is.close();
            } catch (IOException ex) {
                Logger.getLogger(cryptoCommon.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    
    
    
    
    public static PrivateKey getPvKey(String did)
    {
        return pvkeymap.get(did);
    }
    
    public static PublicKey getPublicKey(String did)
    {
        return publickeymap.get(did);
    }

    @SuppressWarnings("unchecked")
    public static List<Object> takeJWTSignList(String did) throws InterruptedException
    {
        return jwtsignqMap.get(did).take();
    }
    public static void putJWTSignList(String did, List<Object> signer) throws InterruptedException
    {
        jwtsignqMap.get(did).put(signer);
    }
    public static String getJwtSignAlgorithm(){
        return jwtsigningalgorithm;
    }
    
    public static Signature takeJWTVerify(String alias) throws InterruptedException
    {
        return jwtverifyqMap.get(alias).take();
    }
    public static void putJWTVerify(String alias, Signature verifier) throws InterruptedException
    {
        jwtverifyqMap.get(alias).put(verifier);
    }
    public static X509Certificate getJWTCAcert(String did){
        cryptoCommon.logp(Level.FINE, classname, "getJWTSignatureVerificationKeys", "CRYPTO-MSG-2522", "#################"+jwtCAcertMap);

        return jwtCAcertMap.get(did);
    }
    public static SortedMap<BigInteger,String> getJWTSerialAliasMap(){
        return jwtcertserialmap;
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
//        spki.getAlgorithm();

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
    private static void loadJWTProperties() throws CryptoException{
        try {
            if ((jwttruststorelocation = cryptoCommon.getConfigurationProperty("crypto.cfg.property.jwtsigning.truststorelocation")) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.truststorelocation");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.truststorelocation"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.truststorelocation");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.truststorelocation"));
        }
        try {
            if ((jwtpassword = cryptoCommon.getConfigurationProperty("crypto.cfg.property.jwtsigning.password")) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.password");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.password"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.password");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.password"));
        }
         try {
            if ((jwtthreads = Integer.parseInt(cryptoCommon.getConfigurationProperty("crypto.cfg.property.jwtsigning.threads"))) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.threads");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.threads"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.threads");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.threads"));
        }
        try {
            if ((jwtkeystorelocation = cryptoCommon.getConfigurationProperty("crypto.cfg.property.jwtsigning.keystorelocation")) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.keystorelocation");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.keystorelocation"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.keystorelocation");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.keystorelocation"));
        }
        try {
            if ((jwtsigningalgorithm = cryptoCommon.getConfigurationProperty("crypto.cfg.property.jwtsigning.algorithm")) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.algorithm");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.algorithm"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.algorithm");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.algorithm"));
        }
        try {
            if ((certPerServer = Integer.parseInt(cryptoCommon.getConfigurationProperty("crypto.cfg.property.jwtsigning.certsperserver"))) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.certsperserver");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.certsperserver"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getJWTKeys", "CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.certsperserver");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.jwtsigning.certsperserver"));
        }
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
