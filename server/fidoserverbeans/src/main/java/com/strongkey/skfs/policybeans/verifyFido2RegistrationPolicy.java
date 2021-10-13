/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.policybeans;

import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skfs.fido.policyobjects.AlgorithmsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.AttestationPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.CounterPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.DefinedExtensionsPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.fido.policyobjects.MDSAuthenticatorStatusPolicy;
import com.strongkey.skfs.fido.policyobjects.MDSPolicyOptions;
import com.strongkey.skfs.fido.policyobjects.RpPolicyOptions;
import com.strongkey.skfs.fido2.ECKeyObject;
import com.strongkey.skfs.fido2.FIDO2AttestationObject;
import com.strongkey.skfs.fido2.FIDO2Extensions;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.SortedMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.JsonArray;
import javax.json.JsonObject;
import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.x509.AlgorithmIdentifier;
import org.bouncycastle.asn1.x509.SubjectPublicKeyInfo;
import org.bouncycastle.util.encoders.Base64;

@Stateless
public class verifyFido2RegistrationPolicy implements verifyFido2RegistrationPolicyLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();
    
    @EJB
    getCachedFidoPolicyMDSLocal getpolicybean;

    @Override
    public void execute(UserSessionInfo userInfo, JsonObject clientJson, FIDO2AttestationObject attObject) throws SKFEException {
        try {
            //Get policy from userInfo
            FidoPolicyMDSObject fidoPolicyMDS = getpolicybean.getByMapKey(userInfo.getPolicyMapKey());
            FidoPolicyObject fidoPolicy = fidoPolicyMDS.getFp();

            verifyRPID(fidoPolicy, attObject, clientJson);
            verifyCryptographyOptions(fidoPolicy, clientJson, attObject, fidoPolicy.getVersion());
            verifyAuthenticatorTrust(fidoPolicy.getSystemOptions().getAllowedAAGUIDs(), clientJson, attObject);
            verifyExtensions(attObject.getAuthData().getExt(), fidoPolicy.getExtensionsOptions());
            verifyRegistration(fidoPolicy, clientJson, attObject,
                    userInfo.getUserVerificationReq(), userInfo.getAttestationPreferance(), fidoPolicy.getVersion());
            String mdsenabled = SKFSCommon.getConfigurationProperty("skfs.cfg.property.mds.enabled");
            if (mdsenabled.equalsIgnoreCase("true") || mdsenabled.equalsIgnoreCase("yes")) {
                String attformat = attObject.getAttFormat();
                String aaguid = getAAGUID(attObject);
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINEST, classname, "execute", "FIDO-MSG-2001", "AAGUID in verify = " + aaguid);
                if (attformat.equalsIgnoreCase("packed")) {
                    //check MDS and policy for AAGUID!!!!
                    //if it doesnt exist then log and continue
                    if (SKFSCommon.containsMdsentry(aaguid)) {
                        JsonObject aaguidMDS = SKFSCommon.getMdsentryfromMap(aaguid);

                        MDSPolicyOptions mdsPolicy = fidoPolicy.getMDS();
                        ArrayList mdsarray = mdsPolicy.getStatusReports();
                        ArrayList<String> aaguidList = fidoPolicy.getSystemOptions().getAllowedAAGUIDs();
                        Boolean singleaaguid = Boolean.FALSE;
                        if (aaguidList.contains("all")) {
                            singleaaguid = Boolean.FALSE;
                        } else {
                            if (aaguidList.size() == 1) {
                                singleaaguid = Boolean.TRUE;
                            } else {
                                singleaaguid = Boolean.FALSE;
                            }
                        }
                        //check to see if the aaguid is revoked or update available
                        SortedMap<String, String> authstatusreportPolicymap = new ConcurrentSkipListMap<>();
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                                "mdsarray: " + mdsarray);
                        for (int i = 0; i < mdsarray.size(); i++) {
                            MDSAuthenticatorStatusPolicy policyarrObj = (MDSAuthenticatorStatusPolicy) mdsarray.get(i);
                            authstatusreportPolicymap.put(policyarrObj.getStatus(), policyarrObj.getDecision());
                        }

                        JsonArray statusReportArray = aaguidMDS.getJsonArray("statusReports");
                        SortedMap<String, JsonObject> authstatusreportMDSmap = new ConcurrentSkipListMap<>();
                        SortedMap<String, String> authstatusreportMDSDatemap = new ConcurrentSkipListMap<>();
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                                "statusReportArray: " + statusReportArray);
                        for (int i = 0; i < statusReportArray.size(); i++) {
                            JsonObject mdsarrObj = statusReportArray.getJsonObject(i);
                            authstatusreportMDSmap.put(mdsarrObj.getString("status"), mdsarrObj);
                            authstatusreportMDSDatemap.put(mdsarrObj.getString("effectiveDate"), mdsarrObj.getString("status"));

                        }

                        //get local date
                        LocalDate today = LocalDate.now();
                        //check to see if revoked is one of the status in MDS
                        if (authstatusreportMDSmap.containsKey("REVOKED")) {
                            //get the effective date for revoked status
                            String effectiverevokedDate = authstatusreportMDSmap.get("REVOKED").getString("effectiveDate");
                            LocalDate effrevokedDate = LocalDate.parse(effectiverevokedDate);

                            //Check to see if its revoked
                            if (effrevokedDate.isBefore(today) || effrevokedDate.isEqual(today)) {
                                //check to see if update is available
                                if (authstatusreportMDSmap.containsKey("UPDATE_AVAILABLE")) {
                                    String effectiveupdateDate = authstatusreportMDSmap.get("UPDATE_AVAILABLE").getString("effectiveDate");
                                    LocalDate effupdateDate = LocalDate.parse(effectiveupdateDate);
                                    //check if the update effective date is today or before today
                                    if (effupdateDate.isBefore(today) || effupdateDate.isEqual(today)) {
                                        if (authstatusreportPolicymap.containsKey("UPDATE_AVAILABLE")) {
                                            if (authstatusreportPolicymap.get("UPDATE_AVAILABLE").equalsIgnoreCase("DENY")) {
                                                throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-3010"));
                                            }
                                        } else {
                                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-3011", "UPDATE_AVAILABLE");
                                        }
                                    } else {
                                        if (authstatusreportPolicymap.containsKey("REVOKED")) {
                                            if (authstatusreportPolicymap.get("REVOKED").equalsIgnoreCase("DENY")) {
                                                throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-3010"));
                                            }
                                        } else {
                                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-3011", "REVOKED");
                                        }
                                    }
                                } else {
                                    if (authstatusreportPolicymap.containsKey("REVOKED")) {
                                        if (authstatusreportPolicymap.get("REVOKED").equalsIgnoreCase("DENY")) {
                                            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-3010"));
                                        }
                                    } else {
                                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-3011", "REVOKED");
                                    }
                                }
                            }
                        }
                        //check status if multiple aaguids
                        if (!singleaaguid) {
                            for (String currentDate : authstatusreportMDSDatemap.keySet()) {
                                String currentStatus = authstatusreportMDSDatemap.get(currentDate);
                                JsonObject currentstatusobject = authstatusreportMDSmap.get(currentStatus);

                                if (authstatusreportPolicymap.containsKey(currentStatus)) {
                                    //check for this effective date
                                    String effectivestatusDate = currentstatusobject.getString("effectiveDate");
                                    LocalDate effstatusDate = LocalDate.parse(effectivestatusDate);
                                    if (effstatusDate.isBefore(today) || effstatusDate.isEqual(today)) {
                                        if (authstatusreportPolicymap.get(currentStatus).equalsIgnoreCase("DENY")) {
                                            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-3010"));
                                        } else if (authstatusreportPolicymap.get(currentStatus).equalsIgnoreCase("ACCEPT")) {
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        //log that doesnt exist in MDS and continue
                        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-3010", "");
                        String allowmissingentry = SKFSCommon.getConfigurationProperty("skfs.cfg.property.mds.allow.missingentry");
                        if (allowmissingentry.equalsIgnoreCase("false") || allowmissingentry.equalsIgnoreCase("no")) {
                            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-MSG-3010"));
                        }

                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-MSG-0053", ex.getLocalizedMessage());
            throw new SKFEException(ex.getLocalizedMessage());
        }
    }

    public static String getAAGUID(FIDO2AttestationObject attObject) {
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

    private void verifyAuthenticatorTrust(ArrayList<String> allowedAaguids, JsonObject clientJson,
            FIDO2AttestationObject attObject) throws SKFEException {

        //if all aaguids allowed then skip aaguid check
        if (allowedAaguids.contains("all")) {
            return;
        }
        //get aaguid from attestation object;
        String aaguid = getAAGUID(attObject);

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "AAGUID: " + aaguid);
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "Allowed AAGUID: " + allowedAaguids);

        if (!(allowedAaguids.isEmpty()) && !allowedAaguids.contains(aaguid)) {

            throw new SKFEException("Rejected Authenticator: AAGUID: " + aaguid);
        }

    }

    private void verifyRPID(FidoPolicyObject fidoPolicy, FIDO2AttestationObject attObject, JsonObject clientJson) throws SKFEException {
        try {
            if (fidoPolicy == null) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0009", "No policy found");
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-0009") + "No policy found"));
            }

            Boolean crossOrigin = Boolean.FALSE;
            if (clientJson.containsKey(SKFSConstants.JSON_KEY_CROSSORIGIN)) {
                crossOrigin = clientJson.getBoolean(SKFSConstants.JSON_KEY_CROSSORIGIN);
            }

            String originfromCD = clientJson.getString(SKFSConstants.JSON_KEY_SERVERORIGIN);
            URI originURI = new URI(originfromCD);
            String rpId = fidoPolicy.getRpOptions().getId();
            String rpidServletExtracted;
            if (!crossOrigin) {

                if (rpId == null) {
                    rpidServletExtracted = originURI.getHost();
                } else {
                    System.out.println("rpidhashfrompolicy = " + Base64.toBase64String(SKFSCommon.getDigestBytes(rpId, "SHA256")));
                    //check if the origin received is rpid+1 if not then reject it
                    if (originfromCD.startsWith("android")) {
                        rpidServletExtracted = rpId;
                    } else {
                        String originwithoutSchemePort;
                        if (originfromCD.startsWith("https")) {
                            originwithoutSchemePort = originfromCD.substring(8).split(":")[0];
                        } else {
                            //reject it
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                    + " RPID Hash invalid'"));
                        }
                        if (!originwithoutSchemePort.endsWith(rpId)) {
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid");
                            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                    + " RPID Hash invalid'"));
                        }
                        String origin2 = originwithoutSchemePort.replace(rpId, "");
                        if (origin2.split("\\.").length > 1) {
                            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid - Does not match policy '" + rpId + "'");
                            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                                    + " RPID Hash invalid - Does not match policy '" + rpId + "'"));
                        }
                        rpidServletExtracted = rpId;
                    }
                }

                if (!Base64.toBase64String(attObject.getAuthData().getRpIdHash()).equals(Base64.toBase64String(SKFSCommon.getDigestBytes(rpidServletExtracted, "SHA256")))) {
                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-2001", " RPID Hash invalid - Does not match policy '" + rpidServletExtracted + "'");
                    throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                            + " RPID Hash invalid - Does not match policy '" + rpidServletExtracted + "'"));
                }
            }

        } catch (URISyntaxException | NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
            Logger.getLogger(verifyFido2RegistrationPolicy.class.getName()).log(Level.SEVERE, null, ex);
            throw new SKFEException(ex.getLocalizedMessage());
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
        if (allowedRSASignatures.contains("all")) {
            allowedRSASignatures = SKFSConstants.ALL_RSA_SIGNATURES;
        }
        //if policy set of EC signatures is set to 'all' then populate allowed EC signatures with all possible options
        if (allowedECSignatures.contains("all")) {
            allowedECSignatures = SKFSConstants.ALL_EC_SIGNATURES;
        }
        if (supportedCurves.contains("all")) {
            supportedCurves = SKFSConstants.ALL_EC_CURVES;
        }

        //Verify attestation key
        ArrayList certificateChain = attObject.getAttStmt().getX5c();
        if (certificateChain != null) {
            X509Certificate attestationCert = cryptoCommon.generateX509FromBytes((byte[]) certificateChain.get(0));

            if (attestationCert == null) {
                throw new SKFEException("Failed to parse X509Certificate. Check logs for details");
            }
            PublicKey attestationKey = attestationCert.getPublicKey();
            String attestationAlgType = attestationKey.getAlgorithm();
            if (!attestationAlgType.equalsIgnoreCase("RSA") && !attestationAlgType.equalsIgnoreCase("EC")) {
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
            if (attestationAlgType.equalsIgnoreCase("EC")) {
                byte[] enc = attestationKey.getEncoded();
                SubjectPublicKeyInfo spki = SubjectPublicKeyInfo.getInstance(ASN1Sequence.getInstance(enc));
                AlgorithmIdentifier algid = spki.getAlgorithm();
                ASN1ObjectIdentifier oid = (ASN1ObjectIdentifier) algid.getParameters();
                if (!supportedCurves.contains(SKFSCommon.getPolicyCurveFromOID(oid))) {
                    throw new SKFEException("EC Curve not supported by policy (Attestation)");
                }
            }
        }

        //Verify signing key
        PublicKey signingKey = attObject.getAuthData().getAttCredData().getPublicKey();
        String signingAlgType = signingKey.getAlgorithm();
        if (!signingAlgType.equalsIgnoreCase("RSA") && !signingAlgType.equalsIgnoreCase("EC")) {
            throw new SKFEException("Unknown attestation key algorithm (Signing)");
        }
        if ((allowedRSASignatures == null
                || !allowedRSASignatures.contains(SKFSCommon.getPolicyAlgFromIANACOSEAlg(attObject.getAuthData().getAttCredData().getFko().getAlg())))
                && (allowedECSignatures == null
                || !allowedECSignatures.contains(SKFSCommon.getPolicyAlgFromIANACOSEAlg(attObject.getAuthData().getAttCredData().getFko().getAlg())))) {
            throw new SKFEException("Rejected key algorithm (Signing): "
                    + SKFSCommon.getPolicyAlgFromIANACOSEAlg(attObject.getAuthData().getAttCredData().getFko().getAlg()));
        }
        if (signingAlgType.equalsIgnoreCase("EC")) {
            ECKeyObject eckey = (ECKeyObject) attObject.getAuthData().getAttCredData().getFko();
            if (!supportedCurves.contains(SKFSCommon.getPolicyCurveFromFIDOECCCurveID(eckey.getCrv()))) {
                throw new SKFEException("EC Curve not supported by policy (Signing)");
            }
        }

        //Verify allowed AttestationFormat
        if (!allowedAttestationFormats.contains(attObject.getAttFormat())) {
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

    //Currently no checks on the Counter options
    private void verifyCounter(CounterPolicyOptions counterOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version) throws SKFEException {
    }

    //Currently no checks on the UserSettings options
    private void verifyUserSettings(Boolean userOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version) {
    }

    //TODO store registration signature if required
    private void handleSignature(Boolean signatureOp, JsonObject clientJson,
            FIDO2AttestationObject attObject, String version) {
    }

    private void verifyRegistration(FidoPolicyObject fidoPolicy, JsonObject clientJson,
            FIDO2AttestationObject attObject, String userVerificationReq, String attestationPreference,
            String version) throws SKFEException {
       


        if(!fidoPolicy.getSystemOptions().getUserVerification().contains(userVerificationReq) && userVerificationReq != null){
            throw new SKIllegalArgumentException("Policy Exception: Prereg userVerificationRequirement does not meet policy");
        }
        if (!fidoPolicy.getAttestationOptions().getAttestationConveyance().contains(attestationPreference)) {
            throw new SKIllegalArgumentException("Policy Exception: Prereg AttestationConveyancePreference does not meet policy");
        }

        //If None attestation was requested (or defaulted to), ensure None attestation is given
        //+ no attestation data is given. Conformance requirement.
        if (attestationPreference.equalsIgnoreCase(SKFSConstants.POLICY_CONST_NONE)
                && !attObject.getAttFormat().equalsIgnoreCase(attestationPreference)) {
            throw new SKFEException("Policy requested none attestation, was given attestation");
        }

        //If User Verification was required, verify it was provided
        if(userVerificationReq != null){
            if(userVerificationReq.equalsIgnoreCase(SKFSConstants.POLICY_CONST_REQUIRED) && !attObject.getAuthData().isUserVerified()){
                throw new SKFEException("User Verification required by policy");
            } 
        } 
        

        //TODO other checks?
        }
    

    private void verifyExtensions(FIDO2Extensions ext, DefinedExtensionsPolicyOptions extOp) throws SKFEException {
        
        if((extOp.getUVM() != null || extOp.getLargeBlob() != null) && ext == null){
            throw new SKFEException("Extension required by policy");
        }
        if(extOp.getUVM() != null){
            if(!ext.containsExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM)){
                throw new SKFEException("UVM Extension required by policy");//FIDO-MSG-0053
            }
            Object uvm = ext.getExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", "UVM Extension: "+ uvm.toString());
            
        }
//        if(extOp.getLargeBlob() != null){
//            if(!ext.containsExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM)){
//                throw new SKFEException("LargeBlob Extension required by policy");
//            }
//            Object largeBlob = ext.getExtension(SKFSConstants.POLICY_ATTR_EXTENSIONS_OUTPUT_UVM);
//            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", "LargeBlob Extension: "+largeBlob.toString());
//               
//        }
    }
}
