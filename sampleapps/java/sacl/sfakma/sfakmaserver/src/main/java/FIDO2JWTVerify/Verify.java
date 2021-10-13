/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package FIDO2JWTVerify;


/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.openssl.PEMParser;

/**
 *
 * @author dbeach
 */
@Stateless
public class Verify {

    private static String jwtsigningalgorithm;

    private static BouncyCastleFipsProvider BC_FIPS_PROVIDER = new BouncyCastleFipsProvider();
   
    public Boolean verify(String did,
            String jwtb64,
            String username,
            String agent,
            String cip,
            String jwtpassword,
            String jwttruststorelocation,
            String rpid) {

        String plaintext;

        List<String> requiredPayload = new ArrayList<>();
        requiredPayload.add("rpid");
        requiredPayload.add("iat");
        requiredPayload.add("exp");
        requiredPayload.add("cip");
        requiredPayload.add("agent");
        requiredPayload.add("sub");

        String[] jwtb64split = jwtb64.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        JsonObject jwt = Json.createObjectBuilder()
                .add("protected", getJsonObjectFromString(new String(decoder.decode(jwtb64split[0]), StandardCharsets.UTF_8)))
                .add("payload", jwtb64split[1])
                .add("signature", jwtb64split[2])
                .build();
        jwtsigningalgorithm = jwt.getJsonObject("protected").getString("alg");
        //TODO: when JWTCreate Implements coversion for all algorithm names implmenent conversions here
        if(jwtsigningalgorithm.equalsIgnoreCase("ES256")){
            jwtsigningalgorithm = "SHA256withECDSA";
        }
        try {
            // Setup FIPS Provider
            Security.addProvider(new BouncyCastleFipsProvider());

            plaintext = new String(decoder.decode(jwt.getString("payload")), StandardCharsets.UTF_8);
            String pemcert = jwt.getJsonObject("protected").getString("x5c");
            PEMParser parser = new PEMParser(new StringReader(pemcert));
            X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
            X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);

            //check if certificate is located in the truststore
            BigInteger serial = cert.getSerialNumber();
            String alias = getAlias(serial, jwttruststorelocation, jwtpassword);
            if (alias == null) {
                System.err.println("Certificate not found in truststore");
                return false;
            }

            //verify certificate chain
            X509Certificate jwtCAcert = loadJWTCACert(did, jwttruststorelocation, jwtpassword);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            List<X509Certificate> certx = new ArrayList<>(2);
            certx.add(cert);
            certx.add(jwtCAcert);

            CertPath path = cf.generateCertPath(certx);
            Set<TrustAnchor> trustAnchor = new HashSet<>();
            trustAnchor.add(new TrustAnchor(jwtCAcert, null));

            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");

            PKIXParameters pkix = new PKIXParameters(trustAnchor);

            pkix.setRevocationEnabled(false);

            pkix.setPolicyQualifiersRejected(true);
            pkix.setDate(new Date());
            CertPathValidatorResult cpvr = cpv.validate(path, pkix);
            if (cpvr == null) {
                System.err.println("Certificate not valid");
                return false;
            }
            byte[] rsbytes = decoder.decode(jwt.getString("signature"));
            Signature signature = loadJWTVerifySignature(alias, jwttruststorelocation, jwtpassword);
            signature.update((jwtb64split[0].concat(".").concat(jwtb64split[1])).getBytes(StandardCharsets.UTF_8));
            boolean verified = signature.verify(rsbytes);
            if (!verified) {
                System.err.println("Signature failed verification");
                return false;
            }
            //Verifythat the required entities are present in the payload
            if (!verifyRequiredPolicy(plaintext, requiredPayload)) {
                return false;
            }
            JsonObject payloadJson = getJsonObjectFromString(plaintext);
            //Check validity of all entities in payload
            if (plaintext.contains("exp")) {
                Long expDateString = payloadJson.getJsonNumber("exp").longValue();
                Date expDate = new Date(expDateString);
                Date currentDate = new Date();
                if (currentDate.after(expDate)) {
                    System.err.println("past jwt expiration");
                    return false;
                }
            }
            if (plaintext.contains("sub")) {
                String uname = payloadJson.getString("sub");
                if (!uname.equals(username)) {
                    System.err.println("payload sub does not match: " + uname);
                    return false;
                }
            }
            if (plaintext.contains("cip")) {
                String jcip = payloadJson.getString("cip");
                System.out.println(cip + " - " + jcip);
                if (!jcip.equals(cip)) {
                    System.err.println("payload cip does not match: " + jcip);
                    return false;
                }
            }
            if (plaintext.contains("agent")) {
                String jagent = payloadJson.getString("agent");
                if (!jagent.equals(agent)) {
                    System.err.println("payload agent does not match: " + jagent);
                    return false;
                }
            }
            if (plaintext.contains("rpid")) {
                String jrpid = payloadJson.getString("rpid");
                if (!jrpid.equals(rpid)) {
                    System.err.println("payload rpid does not match: " + jrpid);
                    return false;
                }
            }
            return true;
        } catch ( IOException|NoSuchAlgorithmException | InvalidAlgorithmParameterException
                | SignatureException | CertificateException | CertPathValidatorException  ex) {
            System.err.println(ex);
        }
        return false;

    }

    private static Boolean verifyRequiredPolicy(String payload, List<String> required) {
        for (String r : required) {
            if (!payload.contains(r)) {
                System.err.println("payload required content not found: " + r);
                return false;
            }
        }
        return true;
    }

    private static X509Certificate loadJWTCACert(String did, String jwttruststorelocation, String jwtpassword) {

        X509Certificate correctCert = null;
        InputStream is = null;
        try {
            KeyStore truststore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            is = new FileInputStream(jwttruststorelocation);
            truststore.load(is, jwtpassword.toCharArray());
            // Print out certs in the truststore
            String alias;
            for (Enumeration<String> e = truststore.aliases(); e.hasMoreElements();) {
                alias = e.nextElement();
                if (alias.equals("jwtCA-" + did)) {
                    X509Certificate cert = (X509Certificate) truststore.getCertificate(alias);
                    correctCert = cert;
                    break;
                }
            }
        } catch (KeyStoreException | CertificateException | NoSuchAlgorithmException | IOException | NullPointerException ex) {
            System.err.println(ex.getLocalizedMessage());
            return null;
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
        return correctCert;
    }

    private static Signature loadJWTVerifySignature(String alias, String jwttruststorelocation, String jwtpassword) {

        Signature verifyObject = null;
        InputStream ist = null;
        try {
            KeyStore truststore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            ist = new FileInputStream(jwttruststorelocation);
            truststore.load(ist, jwtpassword.toCharArray());
            X509Certificate cert = (X509Certificate) truststore.getCertificate(alias);
            Signature s = Signature.getInstance(jwtsigningalgorithm);
            s.initVerify(cert.getPublicKey());
            verifyObject = s;
        } catch (InvalidKeyException | CertificateException | KeyStoreException | NoSuchAlgorithmException | IOException ex) {
            ex.printStackTrace();
            System.err.println(ex.getLocalizedMessage());
        } finally {
            try {
                if (ist != null) {
                    ist.close();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
        return verifyObject;
    }

    private static String getAlias(BigInteger serial, String jwttruststorelocation, String jwtpassword) {

        String correctAlias = null;
        InputStream ist = null;
        try {
            KeyStore truststore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
            ist = new FileInputStream(jwttruststorelocation);
            truststore.load(ist, jwtpassword.toCharArray());
            //load each certificate to check serial
            String alias;
            for (Enumeration<String> e = truststore.aliases(); e.hasMoreElements();) {
                alias = e.nextElement();
                String[] aliasSplit = alias.split("-");
                //check if jwtsigningcert
                if (aliasSplit[0].equals("jwtsigningcert")) {
                    X509Certificate cert = (X509Certificate) truststore.getCertificate(alias);
                    if (cert.getSerialNumber().equals(serial)) {
                        correctAlias = alias;
                        break;
                    }
                }
            }
        } catch (KeyStoreException | NoSuchAlgorithmException | IOException ex) {
            ex.printStackTrace();
            System.err.println(ex.getLocalizedMessage());
        } catch (CertificateException ex) {
            ex.printStackTrace();
        } finally {
            try {
                if (ist != null) {
                    ist.close();
                }
            } catch (IOException ex) {
                System.err.println(ex);
            }
        }
        return correctAlias;
    }

    private static JsonObject getJsonObjectFromString(String jsonstr) {
        try {
            StringReader stringreader = new StringReader(jsonstr);
            JsonReader jsonreader = Json.createReader(stringreader);
            return jsonreader.readObject();
        } catch (JsonParsingException ex) {
            return null;
        }
    }

}