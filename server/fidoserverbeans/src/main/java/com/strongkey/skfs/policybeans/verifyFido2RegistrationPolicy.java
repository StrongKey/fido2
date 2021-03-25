/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.policybeans;

import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.pojos.MDSClient;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.PKIXChainValidation;
import com.strongkey.skfs.fido.policyobjects.AlgorithmsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.AttestationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.CounterPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido.policyobjects.MdsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.RpPolicyOptions;
import com.strongkey.skfs.fido2.ECKeyObject;
import com.strongkey.skfs.fido2.FIDO2AttestationObject;
import com.strongkey.skfs.fido2.FIDO2AttestationStatement;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.nio.ByteBuffer;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;

@Stateless
public class verifyFido2RegistrationPolicy implements verifyFido2RegistrationPolicyLocal {

    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;

    @Override
    public void execute(UserSessionInfo userInfo, JsonObject clientJson, FIDO2AttestationObject attObject) throws SKFEException {
        try{
            //Get policy from userInfo
            FidoPolicyMDSObject fidoPolicyMDS = getpolicybean.getByMapKey(userInfo.getPolicyMapKey());
            FidoPolicyObject fidoPolicy = fidoPolicyMDS.getFp();

            verifyCryptographyOptions(fidoPolicy, clientJson, attObject, fidoPolicy.getVersion());
            verifyAuthenticatorTrust(fidoPolicy.getAllowedAAGUIDs(), clientJson, attObject);
            verifyRegistration(fidoPolicy, clientJson, attObject,
                    userInfo.getUserVerificationReq(), userInfo.getAttestationPreferance(), fidoPolicy.getVersion());
        }
        catch(Exception ex){
            ex.printStackTrace();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-MSG-0053", ex.getLocalizedMessage());
            throw new SKFEException(ex.getLocalizedMessage());
        }
    }
    public static String getAAGUID(FIDO2AttestationObject attObject){
        byte[] aaguidbytes = attObject.getAuthData().getAttCredData().getAaguid();
        byte[] aaguidbytes1 = new byte[8];
        byte[] aaguidbytes2 = new byte[8];
        System.arraycopy(aaguidbytes, 0, aaguidbytes1, 0, 8);
        System.arraycopy(aaguidbytes, 8, aaguidbytes2, 0, 8);
        UUID uuid = new UUID(bytesToLong(aaguidbytes1), bytesToLong(aaguidbytes2));
        return uuid.toString();
    }
    
    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

    private void verifyAuthenticatorTrust( ArrayList<String> allowedAaguids, JsonObject clientJson,
            FIDO2AttestationObject attObject )throws SKFEException {
        
        //if all aaguids allowed then skip aaguid check
        if(allowedAaguids.contains("all")){
            return;
        }
        //get aaguid from attestation object;
        String aaguid = getAAGUID(attObject);
       
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "AAGUID: " + aaguid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "Allowed AAGUID: " + allowedAaguids);
        
        if( !(allowedAaguids.isEmpty()) && !allowedAaguids.contains(aaguid)){

            throw new SKFEException("Rejected Authenticator: AAGUID: " + aaguid);
        }
           
    }
    
    //TODO move private functions to be public functions of PolicyOption Objects
    //Currently blocked by the need to move more objects to common.

    //TODO refactor exceptions to have standard error messages
    private void verifyCryptographyOptions(FidoPolicyObject fidoPolicy, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version) throws SKFEException {
        AlgorithmsPolicyOptions algoOp = fidoPolicy.getAlgorithmsOptions();
        AttestationPolicyOptions attOp = fidoPolicy.getAttestationOptions();
        ArrayList<String> allowedRSASignatures = algoOp.getAllowedRSASignatures();
        ArrayList<String> allowedECSignatures = algoOp.getAllowedECSignatures();
        ArrayList<String> supportedCurves = algoOp.getSupportedEllipticCurves();
        ArrayList<String> allowedAttestationFormats = attOp.getAttestationFormats();

        //if policy set of RSA signatures is set to 'all' then populate allowed RSA signatures with all possible options
        if (allowedRSASignatures.contains("all")){
            allowedRSASignatures = SKFSConstants.ALL_RSA_SIGNATURES;
        }
         //if policy set of EC signatures is set to 'all' then populate allowed EC signatures with all possible options
        if (allowedECSignatures.contains("all")){
            allowedECSignatures = SKFSConstants.ALL_EC_SIGNATURES;
        }
        if (supportedCurves.contains("all")){
            supportedCurves = SKFSConstants.ALL_EC_CURVES;
        }
      
        //Verify attestation key
        ArrayList certificateChain = attObject.getAttStmt().getX5c();
        if(certificateChain != null){
            X509Certificate attestationCert = cryptoCommon.generateX509FromBytes((byte[]) certificateChain.get(0));

            if(attestationCert == null){
                throw new SKFEException("Failed to parse X509Certificate. Check logs for details");
            }
            PublicKey attestationKey = attestationCert.getPublicKey();
            String attestationAlgType = attestationKey.getAlgorithm();
            if(!attestationAlgType.equalsIgnoreCase("RSA") && !attestationAlgType.equalsIgnoreCase("EC")){
                throw new SKFEException("Unknown key algorithm (Attestation)");
            }
            
            // we dont need to check the signing algorithm of the attestation certificate but we will add a new policy option in the future for the same
//            if((allowedRSASignatures == null
//                    || !allowedRSASignatures.contains(SKFSCommon.getPolicyAlgFromAlg(attestationCert.getSigAlgName())))
//                    && (allowedECSignatures == null
//                    || !allowedECSignatures.contains(SKFSCommon.getPolicyAlgFromAlg(attestationCert.getSigAlgName())))){
//                throw new SKFEException("Signature Algorithm not supported by policy (Attestation): " + attestationCert.getSigAlgName());
//            }
            
            //Verify that the curve used by the attestation key is approved
            if(attestationAlgType.equalsIgnoreCase("EC")){
                byte[] enc = attestationKey.getEncoded();
                SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(enc));
                AlgorithmIdentifier algid = spki.getAlgorithm();
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) algid.getParameters();
                if(!supportedCurves.contains(SKFSCommon.getPolicyCurveFromOID(oid))){
                    throw new SKFEException("EC Curve not supported by policy (Attestation)");
                }
            }
        }

        //Verify signing key
        PublicKey signingKey = attObject.getAuthData().getAttCredData().getPublicKey();
        String signingAlgType = signingKey.getAlgorithm();
        if(!signingAlgType.equalsIgnoreCase("RSA") && !signingAlgType.equalsIgnoreCase("EC")){
            throw new SKFEException("Unknown attestation key algorithm (Signing)");
        }
        if((allowedRSASignatures == null
                || !allowedRSASignatures.contains(SKFSCommon.getPolicyAlgFromIANACOSEAlg(attObject.getAuthData().getAttCredData().getFko().getAlg())))
                && (allowedECSignatures == null
                ||!allowedECSignatures.contains(SKFSCommon.getPolicyAlgFromIANACOSEAlg(attObject.getAuthData().getAttCredData().getFko().getAlg())))){
            throw new SKFEException("Rejected key algorithm (Signing): " +
                    SKFSCommon.getPolicyAlgFromIANACOSEAlg(attObject.getAuthData().getAttCredData().getFko().getAlg()));
        }
        if(signingAlgType.equalsIgnoreCase("EC")){
            ECKeyObject eckey = (ECKeyObject) attObject.getAuthData().getAttCredData().getFko();
            if(!supportedCurves.contains(SKFSCommon.getPolicyCurveFromFIDOECCCurveID(eckey.getCrv()))){
                throw new SKFEException("EC Curve not supported by policy (Signing)");
            }
        }
        
        

        

        //Verify allowed AttestationFormat
        if(!allowedAttestationFormats.contains(attObject.getAttFormat())){
            throw new SKFEException("Attestation format not supported by policy: " + attObject.getAttFormat());
        }

    }

    //Currently no checks on the RP options
    private void verifyRpOptions(RpPolicyOptions rpOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version) throws SKFEException {
    }

    private void verifyTimeout(Integer timeoutOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, Integer version) throws SKFEException {
    }

    //TODO simplify logic
    private void verifyMDS(MdsPolicyOptions mdsOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, MDSClient mds, Integer version) throws SKFEException, CertificateException, NoSuchProviderException{
        //MDS not configured, skipping checks
        if(mdsOp == null || mds == null){
            return;
        }

        boolean isPolicyQualifiersRejected = true;
        byte[] aaguidbytes = attObject.getAuthData().getAttCredData().getAaguid();
        byte[] aaguidbytes1 = new byte[8];
        byte[] aaguidbytes2 = new byte[8];
        System.arraycopy(aaguidbytes, 0, aaguidbytes1, 0, 8);
        System.arraycopy(aaguidbytes, 8, aaguidbytes2, 0, 8);
//        UUID uuid = new UUID(Longs.fromByteArray(aaguidbytes1),Longs.fromByteArray(aaguidbytes2));
        UUID uuid = new UUID(ByteBuffer.wrap(aaguidbytes1).getLong(), ByteBuffer.wrap(aaguidbytes2).getLong());
        JsonObject trustAnchors = mds.getTrustAnchors(uuid.toString(), mdsOp.getAllowedCertificationLevel());

        FIDO2AttestationStatement attStmt = attObject.getAttStmt();
        if(attStmt == null){
            return;
        }

        if(attObject.getAttFormat().equals("fido-u2f")){
            return;
        }

        if (attObject.getAttFormat().equals("tpm")) {
            isPolicyQualifiersRejected = false;
        }

        //TODO if no certificate chain returned, check/implement ECDAA
        ArrayList attBytesChain = attObject.getAttStmt().getX5c();
        if(attBytesChain == null || attBytesChain.isEmpty()){
            return;
        }

        List<Certificate> certchain = new ArrayList<>();
        X509Certificate leafCert = cryptoCommon.generateX509FromBytes((byte[]) attBytesChain.get(0)); //check leaf if it is self signed
        certchain.add(leafCert);
        if(leafCert.getSubjectDN().equals(leafCert.getIssuerDN())){
            //TODO verify certificate properly self-signs itself
            return;
        }


        //Create certificate path
        if (!attBytesChain.isEmpty()) {
            for (int attCertIndex = 1; attCertIndex < attBytesChain.size(); attCertIndex++) {
                X509Certificate attestationCert = cryptoCommon.generateX509FromBytes((byte[]) attBytesChain.get(attCertIndex));
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                        "CertPath " + attCertIndex + ": " + attestationCert);
                certchain.add(attestationCert);
            }
        } else {
            throw new SKIllegalArgumentException("Expected Certificate chain missing");
        }
        CertPath certPath = CertificateFactory.getInstance("X.509", "BCFIPS").generateCertPath(certchain);

        //Create list of possible roots from MDS
        Set<TrustAnchor> rootAnchors = new HashSet<>();
        JsonArray roots = trustAnchors.getJsonArray("attestationRootCertificates");

        JsonArray errors = trustAnchors.getJsonArray("errors");
        if(!errors.isEmpty()){
            throw new SKIllegalArgumentException("MDS error(s): " + errors.toString());
        }

        if(roots == null){
            throw new SKIllegalArgumentException("Root certificates not found in MDS");
        }
        for(int rootIndex = 0; rootIndex < roots.size(); rootIndex++) {
            byte[] certBytes = java.util.Base64.getDecoder().decode(roots.getString(rootIndex));
            rootAnchors.add(new TrustAnchor(cryptoCommon.generateX509FromBytes(certBytes), null));
        }

        //Verify chain chains up to one of the roots.
        if(!PKIXChainValidation.pkixvalidate(certPath, rootAnchors, false, isPolicyQualifiersRejected)){    //TODO check CRLs if they exist, otherwise don't
            throw new SKIllegalArgumentException("Failed to verify certificate path");
        }
    }

    //TODO expand checks as token binding spec changes
    private void verifyTokenBinding(String tokenBindingOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, Integer version) throws SKFEException {
        JsonObject tokenBinding = clientJson.getJsonObject(SKFSConstants.JSON_KEY_TOKENBINDING);
        if(tokenBindingOp != null){
            if(tokenBinding == null){
                throw new SKFEException("Policy requires Token Binding");
            }
            String tokenBindingStatus = tokenBinding.getString("status", null);
            if(!tokenBindingOp.equalsIgnoreCase(tokenBindingStatus)){
                throw new SKFEException("Returned token binding does not match policy ("
                        + tokenBindingStatus + " != " + tokenBindingOp + ")");
            }
        }
    }

    //Currently no checks on the Counter options
    private void verifyCounter(CounterPolicyOptions counterOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version) throws SKFEException {
    }

    //Currently no checks on the UserSettings options
    private void verifyUserSettings(Boolean userOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version){
    }

    //TODO store registration signature if required
    private void handleSignature(Boolean signatureOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version){
    }

    private void verifyRegistration(FidoPolicyObject fidoPolicy, JsonObject clientJson,
            FIDO2AttestationObject attObject, String userVerificationReq, String attestationPreference,
            String version) throws SKFEException {
        //Default blank to Webauthn defined defaults
        userVerificationReq = (userVerificationReq == null) ? SKFSConstants.POLICY_CONST_PREFERRED : userVerificationReq;
        attestationPreference = (attestationPreference == null) ? SKFSConstants.POLICY_CONST_NONE : attestationPreference;


        if(!fidoPolicy.getUserVerification().contains(userVerificationReq)){
            throw new SKIllegalArgumentException("Policy Exception: Prereg userVerificationRequirement does not meet policy");
        }
        if(!fidoPolicy.getAttestationOptions().getAttestationConveyance().contains(attestationPreference)){
            throw new SKIllegalArgumentException("Policy Exception: Prereg AttestationConveyancePreference does not meet policy");
        }

        //If None attestation was requested (or defaulted to), ensure None attestation is given
        //+ no attestation data is given. Conformance requirement.
        if (attestationPreference.equalsIgnoreCase(SKFSConstants.POLICY_CONST_NONE)
                && !attObject.getAttFormat().equalsIgnoreCase(attestationPreference)) {
            throw new SKFEException("Policy requested none attestation, was given attestation");
        }

        //If User Verification was required, verify it was provided
        if(userVerificationReq.equalsIgnoreCase(SKFSConstants.POLICY_CONST_REQUIRED) && !attObject.getAuthData().isUserVerified()){
            throw new SKFEException("User Verification required by policy");
        }

        //TODO other checks?
    }
}
