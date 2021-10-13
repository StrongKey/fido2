/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.jwt;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.policybeans.getCachedFidoPolicyMDSLocal;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.IOException;
import java.io.StringReader;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
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
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.openssl.PEMParser;


/**
 *
 * @author dbeach
 */
@Stateless
public class JWTVerify implements JWTVerifyLocal {
    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;

    
    @Override
    public boolean execute(String did,String jwtb64, String username, String agent, String cip, String origin) {
        String plaintext;
        FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(Long.parseLong(did), username, null);

        //Reformat JWT to JSON
        String[]jwtb64split = jwtb64.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();  
        JsonObject jwt = Json.createObjectBuilder()
                    .add("protected",SKFSCommon.getJsonObjectFromString(new String(decoder.decode(jwtb64split[0]), StandardCharsets.UTF_8)) )
                    .add("payload",jwtb64split[1])
                    .add("signature",jwtb64split[2])
                    .build();  
        try {
            // Setup FIPS Provider
            Security.addProvider(new BouncyCastleFipsProvider());   
            plaintext = new String(decoder.decode(jwt.getString("payload")), StandardCharsets.UTF_8);
            String pemcert = jwt.getJsonObject("protected").getString("x5c");
            PEMParser parser = new PEMParser(new StringReader(pemcert));
            X509CertificateHolder holder = (X509CertificateHolder) parser.readObject();
            X509Certificate cert = new JcaX509CertificateConverter().getCertificate(holder);
            PublicKey pbk = cert.getPublicKey();
            
            //check if certificate is located in the truststore 
            SortedMap<BigInteger,String> jwtSerialAliasMap = cryptoCommon.getJWTSerialAliasMap();
            String alias;
            BigInteger serial = cert.getSerialNumber();
            if(jwtSerialAliasMap.get(serial)==null){
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "certificate in truststore",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"Certificate not found in truststore");
                return false;
            } else {
                alias = jwtSerialAliasMap.get(serial);
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "certificate found in truststore",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"certificate found in truststore: "+alias);
            }
            
            //verify certificate chain
            X509Certificate jwtCAcert = cryptoCommon.getJWTCAcert(did);
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
             if (cpvr != null) {
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "certificate path validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"Certificate valid");
            } else {
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "certificate path validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"Certificate not valid");
                return false;
            }
            //verify payload signature
            byte[] rsbytes = decoder.decode(jwt.getString("signature"));
            Signature signature = cryptoCommon.takeJWTVerify(alias);
            signature.update((jwtb64split[0].concat(".").concat(jwtb64split[1])).getBytes(StandardCharsets.UTF_8));
            
            boolean verified = signature.verify(rsbytes);
            cryptoCommon.putJWTVerify(alias,signature);
            if(!verified){
                 SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "signature verification",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"Signature not valid");
              return false;
            }
                
            
            //Verifythat the required entities are present in the payload
            if(!verifyRequiredPolicy(plaintext,fidoPolicy))
                return false;
            
            //Check validity of all entities in payload 
            JsonObject payloadJson = SKFSCommon.getJsonObjectFromString(plaintext);
            if(payloadJson.containsKey("exp")){
                Long expDateString = payloadJson.getJsonNumber("exp").longValue();
//                String pattern = "EEE MMM dd HH:mm:ss Z yyyy";
//                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "expDateString",
//                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"expDateString: "+expDateString);
//                Date expDate=new SimpleDateFormat(pattern).parse(expDateString);
                Date expDate=new Date(expDateString);
                Date currentDate = new Date();
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "expDate",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"expDate: "+expDate);
                if(currentDate.after(expDate)){
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "payload exp validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"past jwt expiration");
                    return false;
                } else {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "payload exp validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"before jwt expiration");
                }
            }
            if(payloadJson.containsKey("sub")){
                String uname = payloadJson.getString("sub");
                if(!uname.equals(username)){
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "payload uname does not match username",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload uname does not match: "+uname);
                    return false;
                } else {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "payload uname validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload uname: "+uname);
                }
            }
            if(plaintext.contains("cip")){
                String jcip = payloadJson.getString("cip");
                if(!jcip.equals(cip)){
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "payload cip does not match client ip",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload cip does not match: "+jcip);
                    return false;
                } else {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "payload cip validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload cip: "+jcip);
                }
            }
            if(plaintext.contains("agent")){
                String jagent = payloadJson.getString("agent");
                if(!jagent.equals(agent)){
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "payload agent does not match User Agent",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload agent does not match: "+jagent);
                    return false;
                } else {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "payload agent validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload agent: "+jagent);
                }
            }
            if(plaintext.contains("rpid")){
                String jrpid = payloadJson.getString("rpid");
                String rpid = fidoPolicy.getRpOptions().getId();
                if(rpid == null){
                    rpid = origin;
                }
                if(!jrpid.equals(rpid)){
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "payload rpid does not match server rpid",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload rpid does not match: "+jrpid);
                    return false;
                } else {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "payload rpid validation",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload rpid: "+jrpid);
                }
            }
            return true;
        } catch (IOException | NoSuchAlgorithmException | InvalidAlgorithmParameterException |
                SignatureException | CertificateException | InterruptedException | CertPathValidatorException  ex) {
            Logger.getLogger(JWTVerify.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    
    }
  
    private  Boolean verifyRequiredPolicy(String payload, FidoPolicyObject fidoPolicy ){
        List<String> required = fidoPolicy.getJWT().getRequired();
        for (String r : required){
            if(!payload.contains(r)){
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "payload required content not found",
                        SKFSCommon.getMessageProperty("FIDO-MSG-6002"),"payload required content not found: "+r);
                return false;
            }
        }
        return true;
    }

    
}
