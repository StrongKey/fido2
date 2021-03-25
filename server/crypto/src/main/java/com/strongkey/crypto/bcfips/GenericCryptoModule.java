/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.crypto.bcfips;

import com.strongkey.crypto.interfaces.CryptoModule;
import com.strongkey.crypto.utility.CryptoException;
import com.strongkey.crypto.utility.cryptoCommon;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.Provider;
import java.security.PublicKey;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.UnrecoverableEntryException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.crypto.CryptoServicesRegistrar;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.util.encoders.Base64;

public class GenericCryptoModule {

    /**
     * This class's name - used for logging & not persisted
     */
    private final String classname = this.getClass().getName();

    /**
     ** Local variables
     *
     */
    private CryptoModule cryptomodule = null;

    private static final boolean fipsmode = Boolean.parseBoolean(cryptoCommon.getConfigurationProperty("crypto.cfg.property.fipsmode"));


//    private final SecureRandom FIPS_DRBG = cryptoCommon.getSecureRandom();
    private final Provider BC_FIPS_PROVIDER = Security.getProvider("BCFIPS");

    /**
     * Constructor for the class.
     *
     * @param cryptomodule - The hardware cryptographic module
     */
    public GenericCryptoModule(CryptoModule cryptomodule) {
        Security.addProvider(new BouncyCastleFipsProvider());
        if (fipsmode) {
            CryptoServicesRegistrar.setApprovedOnlyMode(true);
        }
        this.cryptomodule = cryptomodule;
    }

    /**
     * Constructor for the class.
     *
     * @param cryptomodule - The hardware cryptographic module
     * @param fipsmode - The fipsmode to set
     */
    public GenericCryptoModule(CryptoModule cryptomodule, Boolean fipsmode) {
        Security.addProvider(new BouncyCastleFipsProvider());
        if (fipsmode) {
            CryptoServicesRegistrar.setApprovedOnlyMode(true);
        }
        this.cryptomodule = cryptomodule;
    }


    public String signDBRow(String did,
            String signingdn,
            String input,
            Boolean standalone,
            String password)
            throws CryptoException {
        String signedData = null;
        String sigalg;
        try {

            PrivateKey pvkey;
            if (standalone) {
                pvkey = cryptoCommon.getPvKey(did);
                sigalg = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.ec.signaturealgorithm");
            } else {
                pvkey = getXMLSignatureSigningKey(signingdn);
                sigalg = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.ec.signaturealgorithm");
            }

            // Get signature object
            Signature signature = Signature.getInstance(sigalg);
            signature.initSign(pvkey);
            signature.update(input.getBytes("UTF-8"));

            byte[] signbytes = signature.sign();
            signedData = new String(Base64.encode(signbytes), "UTF-8");
            cryptoCommon.logp(Level.FINE, classname, "signDBRow", "CRYPTO-MSG-1000", signedData + " [Length: " + signbytes.length + "]");

        } catch (NoSuchAlgorithmException | IOException
                | IllegalArgumentException | InvalidKeyException
                | SignatureException ex) {
            ex.printStackTrace();
            cryptoCommon.logp(Level.SEVERE, classname, "signDBRow", "CRYPTO-ERR-1000", ex.toString());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-1000", ex.getLocalizedMessage()));
        }
        return signedData;
    }

    public Boolean verifyDBRow(String did, String input, String signingdn, Boolean standalone, String password, String signaturetype, String currentSignature)
            throws CryptoException {
        Signature signature;
        Boolean verified;
        String sigalg;
        PublicKey pubKey;
        if (standalone) {
            
            if(signaturetype.equalsIgnoreCase("EC")){
                sigalg = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.ec.signaturealgorithm");
                pubKey = cryptoCommon.getPublicKey(did);
            }else{
                sigalg = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.rsa.signaturealgorithm");
                pubKey = getXMLSignatureVerificationKey(password, signingdn);
            }
        } else {
            pubKey = getXMLSignatureVerificationKey("", signingdn);
            sigalg = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.ec.signaturealgorithm");
        }
        try {
            signature = Signature.getInstance(sigalg);
            signature.initVerify(pubKey);
            signature.update(input.getBytes("UTF-8"));
            verified = signature.verify(Base64.decode(currentSignature));
        } catch (NoSuchAlgorithmException | InvalidKeyException | SignatureException | UnsupportedEncodingException ex) {
            ex.printStackTrace();
            cryptoCommon.logp(Level.SEVERE, classname, "signDBRow", "CRYPTO-ERR-1000", ex.toString());
            return false;
        }

        return verified;
    }

    private PrivateKey getXMLSignatureSigningKey(String signingdn) throws CryptoException {
        return cryptomodule.getXMLSignatureSigningKey("", signingdn);
    }

//    private PrivateKey getXMLSignatureSigningKeyLocal(String password, String signingdn) throws CryptoException {
//        return getXMLSignatureSigningKey(password, signingdn);
//    }

    public String hmacRequest(String password, String accesskey, String request) throws CryptoException {
        // Local variables
        Key sk = null;
        String keystoreurl;

        // Keystore location
        try {
            if ((keystoreurl = cryptoCommon.getConfigurationProperty("crypto.cfg.property.hmac.keystorelocation")) == null) {
                cryptoCommon.logp(Level.SEVERE, classname, "getHmacSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.hmac.keystorelocation");
                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.hmac.truststorelocation"));
            }
        } catch (java.util.MissingResourceException e) {
            cryptoCommon.logp(Level.SEVERE, classname, "getHmacSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.hmac.keystorelocation");
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.hmac.truststorelocation"));
        }

        InputStream is = null;
        try {
            KeyStore keystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            is = new FileInputStream(keystoreurl);
            keystore.load(is, password.toCharArray());

            sk = keystore.getKey(accesskey, password.toCharArray());

        } catch (KeyStoreException | UnrecoverableEntryException | CertificateException | NoSuchAlgorithmException | IOException ex) {
            cryptoCommon.logp(Level.SEVERE, classname, "getHmacSignatureSigningKey", "CRYPTO-ERR-2506", ex.getLocalizedMessage());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2506", ex.getLocalizedMessage()));
        }
        finally{
            try {
                if(is !=null)
                    is.close();
            } catch (IOException ex) {
                Logger.getLogger(GenericCryptoModule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (sk == null) {
            cryptoCommon.logp(Level.SEVERE, classname, "getHmacSignatureVerificationKey", "CRYPTO-ERR-2508");
            throw new CryptoException(cryptoCommon.getMessageProperty("CRYPTO-ERR-2508"));
        }

        SecretKey key = (SecretKey) sk;

        return Base64.toBase64String(cryptoCommon.calculateHmac(key, request.getBytes(), "HmacSHA256"));
    }

//    public PrivateKey getXMLSignatureSigningKey(String secret, String signingdn) throws CryptoException {
//        // Local variables
//        X509Certificate cert;               // X509 Certificate object
//        PrivateKey pvk = null;              // RSA Private key object
//        boolean[] keyusage;                // Key-usage bits of signing key
//        String keystoreurl;                 // Location of key-store (default is JCEKS file)
//
//        // Keystore location
////        try {
////            if ((keystoreurl = cryptoCommon.getConfigurationProperty("crypto.cfg.property.signing.keystorelocation")) == null) {
////                cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.signing.keystorelocation");
////                throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation"));
////            }
////        } catch (java.util.MissingResourceException e) {
////            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2505", "crypto.cfg.property.signing.keystorelocation");
////            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2505", "crypto.cfg.property.signing.truststorelocation"));
////        }
//
//        try {
////            KeyStore keystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
////            keystore.load(new FileInputStream(keystoreurl), secret.toCharArray());
//            KeyStore keystore = cryptoCommon.getKeystore();
//
//            // Convert signingdn to an BouncyCastle X500Name-compatible DN
//            X500Name xsdn = new X500Name(signingdn);
//
//            // Print out certs in the keystore
//            String alias;
//            cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2520", signingdn);
//            for (Enumeration<String> e = keystore.aliases(); e.hasMoreElements();) {
//                alias = e.nextElement();
//                cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2514", alias);
//                if (!alias.endsWith(".cert")) {
//                    continue;
//                }
//                cert = (X509Certificate) keystore.getCertificate(alias);
//                X500Name xcdn = new X500Name(cert.getSubjectX500Principal().getName());
//                cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2515", xcdn + " [" + alias + "]");
//
//                // First match the subject DN
//                if (xcdn.equals(xsdn)) {
//                    cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2516", signingdn);
//                    keyusage = cert.getKeyUsage();
//
//                    // Collect key-usages in a string buffer for logging
//                    StringWriter sw = new java.io.StringWriter();
//                    for (int i = 0; i < keyusage.length; i++) {
//                        sw.write("\nkeyusage[" + i + "]: " + keyusage[i]);
//                    }
//                    cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2517", sw.toString());
//
//                    // Now match for the signing bit
//                    if (keyusage[0]) {
//                        // If true, this is the certificate we want
//                        String pvkalias = alias.substring(0, alias.indexOf(".")); // Get rid of the .cert in alias
//                        pvk = ((KeyStore.PrivateKeyEntry) keystore.getEntry(pvkalias, new KeyStore.PasswordProtection(secret.toCharArray()))).getPrivateKey();
//                        cryptoCommon.logp(Level.FINE, classname, "getXMLSignatureSigningKey", "CRYPTO-MSG-2518", signingdn + " [" + alias + "]");
//                        break;
//                    }
//                }
//            }
//        } catch (KeyStoreException | UnrecoverableEntryException | NoSuchAlgorithmException  ex) {
//            ex.printStackTrace();
//            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureSigningKey", "CRYPTO-ERR-2506", ex.getLocalizedMessage());
//            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2506", ex.getLocalizedMessage()));
//        }
//        if (pvk == null) {
//            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureVerificationKey", "CRYPTO-ERR-2508");
//            throw new CryptoException(cryptoCommon.getMessageProperty("CRYPTO-ERR-2508"));
//        }
//        return pvk;
//    }

    /**
     * Private method that retrieves the reference to a signing key from a
     * SunJCE JCEKS
     *
     * @param signingdn String containing the DN that was used to sign the XML
     * object
     * @return java.security.PublicKey object containing the RSA public-key of
     * the signer
     */
    private PublicKey getXMLSignatureVerificationKey(String password, String signingdn) throws CryptoException {

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
            truststore.load(is, password.toCharArray());
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

        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException ex) {
            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureVerificationKey", "CRYPTO-ERR-2507", ex.getLocalizedMessage());
            throw new CryptoException(cryptoCommon.getMessageWithParam("CRYPTO-ERR-2507", ex.getLocalizedMessage()));
        }
        finally{
            try {
                if (is!=null)
                    is.close();
            } catch (IOException ex) {
                Logger.getLogger(GenericCryptoModule.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        
        if (pbk == null) {
            cryptoCommon.logp(Level.SEVERE, classname, "getXMLSignatureVerificationKey", "CRYPTO-ERR-2509");
            throw new CryptoException(cryptoCommon.getMessageProperty("CRYPTO-ERR-2509"));
        }
        return pbk;
    }
}
