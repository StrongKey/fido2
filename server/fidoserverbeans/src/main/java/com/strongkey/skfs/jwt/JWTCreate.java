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
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.Signature;
import java.security.SignatureException;
import java.security.cert.X509Certificate;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Base64.Encoder;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;
import org.bouncycastle.openssl.jcajce.JcaPEMWriter;
/**
 *
 * @author dbeach
 */
@Stateless
public class JWTCreate implements JWTCreateLocal {
    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;
    @Override
    public String execute(String did,  String username, String agent, String cip, String origin){
        
        FidoPolicyObject fidoPolicy = getpolicybean.getPolicyByDidUsername(Long.parseLong(did), username, null);
        
        //generate iat
        String pattern = "EEE MMM dd HH:mm:ss Z yyyy";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        Date currentDate = new Date();
        String iat = simpleDateFormat.format(currentDate);
        
        //generate exp
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentDate);   
        int time =  fidoPolicy.getJWT().getDuration(); 
        cal.add(Calendar.MINUTE, time);  
        Date expDate = new Date();
        expDate.setTime(cal.getTimeInMillis());
        String exp = simpleDateFormat.format(expDate);
        String rpid = fidoPolicy.getRpOptions().getId();
        if(rpid == null){
            rpid = origin;
        }
        JsonObject jsonPayload = Json.createObjectBuilder()
                .add("rpid", rpid)
                .add("iat", currentDate.getTime())
                .add("exp", expDate.getTime())
                .add("cip", cip)
                .add("sub", username)
                .add("agent", agent)
                .build();
        //Get signing key and certificate from queue
        List<Object> signinglist = null;
        try {
            signinglist = cryptoCommon.takeJWTSignList(did);
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, JWTVerify.class.getName(), "signing list",
                    SKFSCommon.getMessageProperty("FIDO-MSG-6003"),"signing list: "+ signinglist);
        } catch (InterruptedException ex) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, JWTVerify.class.getName(), "signing list retieval failed",
                    SKFSCommon.getMessageProperty("FIDO-MSG-6003"),ex.getMessage());
        }
        String alg = cryptoCommon.getJwtSignAlgorithm();
        if(alg.equalsIgnoreCase("SHA256withECDSA")){
            alg = "ES256";
        }
        Signature ecsig = (Signature) signinglist.get(0);
        X509Certificate cert = (X509Certificate) signinglist.get(1);
        StringWriter sWriter = new StringWriter();
        JcaPEMWriter pemWriter = new JcaPEMWriter(sWriter);
        String pemcert = null;
        try {
            pemWriter.writeObject(cert);
            pemWriter.close();
            pemcert = sWriter.toString();
        } catch (IOException ex) {
            Logger.getLogger(JWTCreate.class.getName()).log(Level.SEVERE, null, ex);
        }
        // Setup FIPS Provider
        Security.addProvider(new BouncyCastleFipsProvider());
        Encoder encoder = Base64.getUrlEncoder();
        //Set header
        JsonObject headerjson = Json.createObjectBuilder()
                .add("alg", alg)
                .add("x5c", pemcert)
                .build();
        String b64header = encoder.withoutPadding().encodeToString(headerjson.toString().getBytes(StandardCharsets.UTF_8));
        String b64payload = encoder.withoutPadding().encodeToString(jsonPayload.toString().getBytes(StandardCharsets.UTF_8));
        
        
        String input = b64header.concat(".").concat(b64payload);
        
        byte[] dsig;
        String jwt = null ;
        //sign plaintext
        try {
            ecsig.update(input.getBytes(StandardCharsets.UTF_8));
            dsig = ecsig.sign();
            String b64jsonSig = encoder.withoutPadding().encodeToString(dsig);
            // Finally, the JWS
             jwt = input.concat(".").concat(b64jsonSig);
            
            cryptoCommon.putJWTSignList(did, signinglist);
        } catch (SignatureException | InterruptedException ex) {
            Logger.getLogger(JWTCreate.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return jwt;
    }
} 

