
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.KeyStore;
import java.security.KeyStore.SecretKeyEntry;
import java.security.MessageDigest;
import java.security.Provider;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Date;
import java.util.Enumeration;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x509.BasicConstraints;
import org.bouncycastle.asn1.x509.Extension;
import org.bouncycastle.asn1.x509.KeyUsage;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.X509v3CertificateBuilder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cert.jcajce.JcaX509ExtensionUtils;
import org.bouncycastle.cert.jcajce.JcaX509v3CertificateBuilder;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.operator.BufferingContentSigner;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.util.encoders.Hex;

/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
public class Main {

    private static final String usage = "\nUsage: java -jar keymanager.jar listaccesskeys <keystore location> <keystore password>\n"
            + "       java -jar keymanager.jar addaccesskey <keystore location> <keystore password>\n"
            + "       java -jar keymanager.jar deleteaccesskey <keystore location> <keystore password> <accesskey>\n"
            + "       java -jar keymanager.jar regeneratesigningkey <keystore location> <truststore location> <keystore password>\n";

    private static final Provider BC_FIPS_PROVIDER = new BouncyCastleFipsProvider();

    public static void main(String[] args) throws Exception {
        if (args.length < 3) {
            System.err.println(usage);
            return;
        }

        switch (args[0].toLowerCase()) {
            case "listaccesskeys":
                listaccesskeys(args[1], args[2]);
                return;

            case "addaccesskey":
                addaccesskey(args[1], args[2]);
                return;

            case "deleteaccesskey":
                deleteaccesskey(args[1], args[2], args[3]);
                return;

            case "regeneratesigningkey":
                regeneratesigningkey(args[1], args[2], args[3]);
                return;

            default:
                System.err.println("Invalid operation " + args[0]);
                System.err.println(usage);
        }
    }

    private static void listaccesskeys(String keystorelocation, String password) throws Exception {
        KeyStore keystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
        keystore.load(new FileInputStream(keystorelocation), password.toCharArray());
        java.util.SortedSet<String> hsmobj = new java.util.TreeSet<>();
        for (Enumeration<String> e = keystore.aliases(); e.hasMoreElements();) {
            hsmobj.add(e.nextElement());
        }
        System.out.println("===> Objects in keystore:");
        for (String s : hsmobj) {
            if (keystore.entryInstanceOf(s, SecretKeyEntry.class)) {
                System.out.println(String.format("%-24s %-20s %-48s", s, "SecretKey", "created on " + keystore.getCreationDate(s)));
            }
        }
    }

    private static void addaccesskey(String keystorelocation, String password) throws Exception {
        KeyStore keystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
        keystore.load(new FileInputStream(keystorelocation), password.toCharArray());
        KeyGenerator keygen = KeyGenerator.getInstance("AES", BC_FIPS_PROVIDER);
        keygen.init(128);
        SecretKey sk = keygen.generateKey();
        String secretkey = Hex.toHexString(sk.getEncoded());

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.update(new Date().toString().getBytes());
        String accesskey = Hex.toHexString(digest.digest()).substring(0, 16);

        keystore.setKeyEntry(accesskey, sk, password.toCharArray(), null);
        keystore.store(new FileOutputStream(keystorelocation), password.toCharArray());
        System.out.println("Created new access/secret key:");
        System.out.println("Access key:" + accesskey);
        System.out.println("Secret key:" + secretkey);
    }

    private static void deleteaccesskey(String keystorelocation, String password, String alias) throws Exception {
        KeyStore keystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
        keystore.load(new FileInputStream(keystorelocation), password.toCharArray());
        keystore.deleteEntry(alias);
        keystore.store(new FileOutputStream(keystorelocation), password.toCharArray());
        System.out.println("Removed access key: " + alias);
    }

    private static void regeneratesigningkey(String keystorelocation, String truststorelocation, String password) throws Exception {
        KeyStore keystore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
        keystore.load(new FileInputStream(keystorelocation), password.toCharArray());

        KeyPairGenerator kpgen = KeyPairGenerator.getInstance("RSA", BC_FIPS_PROVIDER);
        kpgen.initialize(2048);
        KeyPair keypair = kpgen.generateKeyPair();

        BigInteger serialnum = new BigInteger(64, new SecureRandom());
        while (serialnum.compareTo(BigInteger.ZERO) <= 0) {
            serialnum = new BigInteger(64, new SecureRandom());
        }

        X509v3CertificateBuilder certgen = new JcaX509v3CertificateBuilder(new X500Name("CN=SKFS Signing Key,OU=DID 1,OU=SKFS Signing Certificate 1,O=StrongKey"),
                serialnum,
                new Date(System.currentTimeMillis()),
                new Date(System.currentTimeMillis() + (3650 * 86400000)),
                new X500Name("CN=SKFS Signing Key,OU=DID 1,OU=SKFS Signing Certificate 1,O=StrongKey"),
                keypair.getPublic());

        certgen.addExtension(Extension.basicConstraints, true, new BasicConstraints(true));
        certgen.addExtension(Extension.keyUsage, true, new KeyUsage(KeyUsage.digitalSignature));
        certgen.addExtension(Extension.authorityKeyIdentifier, false, new JcaX509ExtensionUtils().createAuthorityKeyIdentifier(keypair.getPublic()));
        certgen.addExtension (Extension.subjectKeyIdentifier, false, new JcaX509ExtensionUtils().createSubjectKeyIdentifier(keypair.getPublic()));

        ContentSigner contentSigner = new BufferingContentSigner(new JcaContentSignerBuilder("SHA256withRSA").setProvider(BC_FIPS_PROVIDER).build(keypair.getPrivate()), 20480);
        X509CertificateHolder cert = certgen.build(contentSigner);
        X509Certificate x509cert = new JcaX509CertificateConverter().getCertificate(cert);

        X509Certificate[] chain = new X509Certificate[1];
        chain[0] = x509cert;

        keystore.deleteEntry("1-zenc-signing-key");
        keystore.setKeyEntry("1-zenc-signing-key", keypair.getPrivate(), password.toCharArray(), chain);

        keystore.deleteEntry("1-zenc-signing-key.cert");
        keystore.setCertificateEntry("1-zenc-signing-key.cert", x509cert);

        KeyStore truststore = KeyStore.getInstance("BCFKS", BC_FIPS_PROVIDER);
        truststore.load(new FileInputStream(truststorelocation), password.toCharArray());

        truststore.deleteEntry("1-zenc-signing-key.cert");
        truststore.setCertificateEntry("1-zenc-signing-key.cert", x509cert);

        keystore.store(new FileOutputStream(keystorelocation), password.toCharArray());
        truststore.store(new FileOutputStream(truststorelocation), password.toCharArray());
        System.out.println("Regenerated signing key");
    }

}
