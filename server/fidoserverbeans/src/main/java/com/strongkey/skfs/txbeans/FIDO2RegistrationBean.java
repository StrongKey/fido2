/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.entitybeans.AttestationCertificates;
import com.strongkey.skfs.entitybeans.AttestationCertificatesPK;
import com.strongkey.skfs.fido2.FIDO2AttestationObject;
import com.strongkey.skfs.fido2.FIDO2AttestationStatement;
import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.policybeans.verifyFido2RegistrationPolicyLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.UUID;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonException;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonValue;

@Stateless
public class FIDO2RegistrationBean implements FIDO2RegistrationBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    private final Integer RSV = Integer.parseInt(SKFSCommon.getConfigurationProperty("skfs.cfg.property.fido2.user.settings.version"));

    @EJB
    addFidoKeysLocal addkeybean;
    @EJB
    getFidoAttestationCertificateLocal getAttCertbean;
    @EJB
    addFidoAttestationCertificateLocal addAttCertBean;
    @EJB
    verifyFido2RegistrationPolicyLocal verifyRegistrationPolicyBean;

//    @EJB
//    verifyMDSCertificateChainBeanLocal verifyMDSCertificateChainBean;

    @Override
    public String execute(Long did, String registrationresponse, String registrationmetadata) {
        try{
            //Verify fields in response from authenticator are valid
            verifyRegistrationResponse(registrationresponse);
            JsonObject response = retrieveResponseFromRegistrationResponse(registrationresponse);
            verifyFIDOResponseObject(response);

            //Verify fields in clientDataJson are valid
            String browserdataBase64 = response.getString(SKFSConstants.JSON_KEY_CLIENTDATAJSON);
            JsonObject clientDataJson = retrieveBrowserdataJsonFromFIDOResponseObject(response);
            verifyClientDataJsonObject(clientDataJson);

            //Verify fields in registrationmetatdata are valid
            JsonObject metadataJson = retrieveMetadataJsonFromRegistrationMetadata(registrationmetadata);
            verifyRegistrationMetadata(metadataJson);
            String origin = metadataJson.getString(SKFSConstants.FIDO_METADATA_KEY_ORIGIN, "");

            //Additional input checks
            String challengeDigest = calculateChallengeDigest(clientDataJson.getString(SKFSConstants.JSON_KEY_NONCE));
            UserSessionInfo userInfo = (UserSessionInfo) skceMaps.getMapObj().get(SKFSConstants.MAP_USER_SESSION_INFO, challengeDigest);
            String sessionUsername = retrieveUsernameFromSessionMap(userInfo, challengeDigest);
            verifyUsernameMatch(metadataJson.getString(SKFSConstants.FIDO_METADATA_KEY_USERNAME), sessionUsername);
            
            Boolean crossOrigin = Boolean.FALSE;
            if (clientDataJson.containsKey(SKFSConstants.JSON_KEY_CROSSORIGIN)) {
                crossOrigin = clientDataJson.getBoolean(SKFSConstants.JSON_KEY_CROSSORIGIN);
            }
            if (!crossOrigin) {
                verifyOrigin(clientDataJson.getString(SKFSConstants.JSON_KEY_SERVERORIGIN), origin);
            }
            
            //TODO rpid hash matches.
            
            //Get AttestationObject
            FIDO2AttestationObject attObject = retrieveAttestationObjectFromFIDOResponseObject(response);

            //Retrieve AAGUID
            String aaguid = getAAGUID(attObject);
            
            
            //Perform Policy Verification
            verifyRegistrationPolicyBean.execute(userInfo, clientDataJson, attObject);

            //Verify Signature
            Boolean isSignatureValid = attObject.getAttStmt().verifySignature(browserdataBase64, attObject.getAuthData());
            if (!isSignatureValid) {
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDO-MSG-2001", "Registration Signature verification : " + isSignatureValid);
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                        + "Registration Signature verification : " + isSignatureValid));
            }

            AttestationCertificatesPK attCertPK = storeAttestationStatement(did, attObject.getAttStmt());

            //Save userId
            String userId = userInfo.getUserId();

            //Remove challenge from map
            skceMaps.getMapObj().remove(SKFSConstants.MAP_USER_SESSION_INFO, challengeDigest);

            //Store FIDO key in database
            String addkeyresponse = addkeybean.execute(did,
                    userId,
                    sessionUsername,
                    Base64.getUrlEncoder().withoutPadding().encodeToString(attObject.getAuthData().getAttCredData().getCredentialId()),
                    Base64.getUrlEncoder().withoutPadding().encodeToString(attObject.getAuthData().getAttCredData().getPublicKey().getEncoded()),
                    origin,
                    (short) SKFSConstants.FIDO_TRANSPORT_BLE_USB_NFC, //TODO replace with actual guess of transport (MDS)
                    (attCertPK != null)?attCertPK.getSid():null,
                    (attCertPK != null)?attCertPK.getSid():null,
                    (attCertPK != null)?attCertPK.getAttcid():null,
                    attObject.getAuthData().getCounterValueAsInt(),
                    SKFSConstants.FIDO_PROTOCOL_VERSION_2_0,
                    SKFSConstants.FIDO_PROTOCOL_VERSION_2_0,
                    aaguid,
                    parseRegistrationSettings(attObject.getAuthData(), userInfo, attObject.getAttFormat()),
                    RSV,
                    metadataJson.getString(SKFSConstants.FIDO_METADATA_KEY_CREATE_LOC));
            
            JsonObject addkeyres = SKFSCommon.getJsonObjectFromString(addkeyresponse);
            if (!addkeyres.getBoolean("status")) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                        SKFSCommon.getMessageProperty("FIDO-ERR-2001"), addkeyres.getString("message"));
                throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                        + addkeyres.getString("message")));
            }
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-0024", "");
            String responseJSON = SKFSCommon.buildReturn("Successfully processed registration response");
            return responseJSON;
        }
        catch(RuntimeException | SKFEException | CertificateException | NoSuchProviderException ex){
            ex.printStackTrace();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE,
                    SKFSCommon.getMessageProperty("FIDO-ERR-2001"), ex.getLocalizedMessage());
            throw new SKIllegalArgumentException(SKFSCommon.buildReturn(SKFSCommon.getMessageProperty("FIDO-ERR-2001")
                    + ex.getLocalizedMessage()));
        }
    }

    private void verifyRegistrationResponse(String registrationResponse){
        try{
            JsonObject registrationObject = SKFSCommon.getJsonObjectFromString(registrationResponse);
            String[] requiredFields = { SKFSConstants.JSON_KEY_ID, SKFSConstants.JSON_KEY_RAW_ID,
                SKFSConstants.JSON_KEY_REQUEST_TYPE, SKFSConstants.JSON_KEY_SERVLET_INPUT_RESPONSE};
            String[] requiredBase64UrlFields = { SKFSConstants.JSON_KEY_ID, SKFSConstants.JSON_KEY_RAW_ID };
            verifyRequiredFieldsExist(registrationObject, requiredFields);
            verifyFieldsBase64Url(registrationObject, requiredBase64UrlFields);
            verifyAcceptedValue(registrationObject, SKFSConstants.JSON_KEY_REQUEST_TYPE, new String[]{ "public-key" });
//            if(!registrationObject.getString(SKFSConstants.JSON_KEY_ID).equals(registrationObject.getString(SKFSConstants.JSON_KEY_RAW_ID))){
//                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "id does not match rawId");
//                throw new SKIllegalArgumentException("Json improperly formatted");
//            }
        }
        catch(JsonException ex){
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Json improperly formatted");
            throw new SKIllegalArgumentException("Json improperly formatted");
        }
    }

    private void verifyRequiredFieldsExist(JsonObject jsonObject, String[] requiredFields){
        for(String field: requiredFields){
            JsonValue jsonValue = jsonObject.get(field);
            if(jsonValue == null || jsonValue.toString().isEmpty()){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Missing " + field);
                throw new SKIllegalArgumentException("Missing " + field);
            }
        }
    }

    private void verifyFieldsBase64Url(JsonObject jsonObject, String[] requiredBase64Fields){
        for(String field: requiredBase64Fields){
            String base64UrlString = jsonObject.getString(field, null);
            if(base64UrlString == null || !base64UrlString.equals(
                    Base64.getUrlEncoder().withoutPadding().encodeToString(
                    Base64.getUrlDecoder().decode(base64UrlString)))){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid Base64: " + field + " value: " + base64UrlString);
                throw new SKIllegalArgumentException("Invalid Base64: " + field);
            }
//            try{
//                Base64.getUrlDecoder().decode(base64UrlString);
//            }catch(Exception ex){
//                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid Base64: " + field + " value: " + base64UrlString);
//                throw new SKIllegalArgumentException("Cannot decode Base64: " + field);
//            }
        }
    }

    private void verifyAcceptedValue(JsonObject jsonObject, String field, String[] acceptedValues){
        String returnedValue = jsonObject.getString(field, null);
        ArrayList acceptedList = new ArrayList(Arrays.asList(acceptedValues));
        if(returnedValue == null || !acceptedList.contains(returnedValue)){
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid " + field);
            throw new SKIllegalArgumentException("Invalid " + field);
        }
    }

    private JsonObject retrieveResponseFromRegistrationResponse(String registrationResponse){
        JsonObject responseObject = SKFSCommon.getJsonObjectFromString(registrationResponse);
        JsonObject response = responseObject.getJsonObject(SKFSConstants.JSON_KEY_SERVLET_INPUT_RESPONSE);
        if(response == null){
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid response");
            throw new SKIllegalArgumentException("Invalid response");
        }
        return response;
    }

    private void verifyFIDOResponseObject(JsonObject response){
        String[] requiredFields = {SKFSConstants.JSON_KEY_ATTESTATIONOBJECT, SKFSConstants.JSON_KEY_CLIENTDATAJSON};
        String[] requiredBase64UrlFields = {SKFSConstants.JSON_KEY_ATTESTATIONOBJECT, SKFSConstants.JSON_KEY_CLIENTDATAJSON};
        verifyRequiredFieldsExist(response, requiredFields);
        verifyFieldsBase64Url(response, requiredBase64UrlFields);
    }

    private JsonObject retrieveBrowserdataJsonFromFIDOResponseObject(JsonObject response){
        try {
            String browserdataString = response.getString(SKFSConstants.JSON_KEY_CLIENTDATAJSON);
            String browserdataJsonString = new String(Base64.getUrlDecoder().decode(browserdataString), "UTF-8");
            return SKFSCommon.getJsonObjectFromString(browserdataJsonString);
        } catch (UnsupportedEncodingException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid clientDataJSON");
            throw new SKIllegalArgumentException("Invalid clientDataJSON");
        }
    }

    private void verifyClientDataJsonObject(JsonObject clientDataJson){
        String[] requiredFields = {SKFSConstants.JSON_KEY_REQUEST_TYPE, SKFSConstants.JSON_KEY_NONCE,
            SKFSConstants.JSON_KEY_SERVERORIGIN};
        String[] requiredBase64UrlFields = {SKFSConstants.JSON_KEY_NONCE};
        verifyRequiredFieldsExist(clientDataJson, requiredFields);
        verifyFieldsBase64Url(clientDataJson, requiredBase64UrlFields);
        verifyAcceptedValue(clientDataJson, SKFSConstants.JSON_KEY_REQUEST_TYPE, new String[]{ "webauthn.create" });
        verifyTokenBinding(clientDataJson.getJsonObject(SKFSConstants.JSON_KEY_TOKENBINDING));
    }

    private JsonObject retrieveMetadataJsonFromRegistrationMetadata(String registrationmetadata){
        try {
            return SKFSCommon.getJsonObjectFromString(registrationmetadata);
        } catch (JsonException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Metadata Json improperly formatted");
            throw new SKIllegalArgumentException("Metadata Json improperly formatted");
        }
    }

    private void verifyRegistrationMetadata(JsonObject metadataJson){
        String[] requiredFields = {
            SKFSConstants.FIDO_METADATA_KEY_CREATE_LOC,
            SKFSConstants.FIDO_METADATA_KEY_USERNAME,
            SKFSConstants.FIDO_METADATA_KEY_ORIGIN
        };
        verifyRequiredFieldsExist(metadataJson, requiredFields);
    }

    private String calculateChallengeDigest(String challenge){
        try {
            return SKFSCommon.getDigest(challenge, "SHA-256");
        } catch (NoSuchAlgorithmException | NoSuchProviderException | UnsupportedEncodingException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0001", " Error generating challenge hash");
            throw new IllegalStateException("Error generating challenge hash: " + ex);
        }
    }

    private String retrieveUsernameFromSessionMap(UserSessionInfo userInfo, String challengeDigest){
        if (userInfo == null) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0006", "");
            throw new SKIllegalArgumentException("Request timed out, please try again");
        } else {
            String sessionUsername = userInfo.getUsername();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-MSG-0022", " username=" + sessionUsername);
            return sessionUsername;
        }
    }

    private void verifyUsernameMatch(String metadataUsername, String sessionUsername){
        if (!metadataUsername.equalsIgnoreCase(sessionUsername)) {
            throw new SKIllegalArgumentException(SKFSCommon.getMessageProperty("FIDO-ERR-0037"));
        }
    }

    private void verifyOrigin(String origin, String rporigin) {
        try{
            URI originURI = new URI(origin);
            URI rporiginURI = new URI(rporigin);
            if(!originURI.equals(rporiginURI)){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid Origin");
                throw new SKIllegalArgumentException("Invalid Origin: " + originURI + " != " + rporiginURI);
            }
        } catch (URISyntaxException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid Origin: " + ex.getLocalizedMessage());
            throw new SKIllegalArgumentException("Invalid Origin " + ex.getLocalizedMessage());
        }
    }

    private void verifyTokenBinding(JsonObject tokenbinding){
        if(tokenbinding != null){   //token binding is optional, skip if tokenbinding does not exist
            String[] requiredFields = {"status"};
            verifyRequiredFieldsExist(tokenbinding, requiredFields);
            verifyAcceptedValue(tokenbinding, "status", new String[]{ "present", "supported", "not-supported" });
        }
    }

    private FIDO2AttestationObject retrieveAttestationObjectFromFIDOResponseObject(JsonObject response){
        try {
            String attestationObjectString = response.getString(SKFSConstants.JSON_KEY_ATTESTATIONOBJECT);
            FIDO2AttestationObject attObject = new FIDO2AttestationObject();
            attObject.decodeAttestationObject(attestationObjectString);
            return attObject;
        } catch (IOException | NoSuchAlgorithmException | NoSuchProviderException | InvalidKeySpecException | InvalidParameterSpecException ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-ERR-5011", "Invalid attestaionObject: " + ex);
            throw new SKIllegalArgumentException("Invalid attestaionObject");
        }
    }

    private String getAAGUID(FIDO2AttestationObject attObject){
        byte[] aaguidbytes = attObject.getAuthData().getAttCredData().getAaguid();
        byte[] aaguidbytes1 = new byte[8];
        byte[] aaguidbytes2 = new byte[8];
        System.arraycopy(aaguidbytes, 0, aaguidbytes1, 0, 8);
        System.arraycopy(aaguidbytes, 8, aaguidbytes2, 0, 8);
//        UUID uuid = new UUID(Longs.fromByteArray(aaguidbytes1), Longs.fromByteArray(aaguidbytes2));
        UUID uuid = new UUID(ByteBuffer.wrap(aaguidbytes1).getLong(), ByteBuffer.wrap(aaguidbytes2).getLong());
        
        return uuid.toString();
    }

    //TODO rollback added certificates if something goes wrong
    //TODO add root certificate from MDS.
    private AttestationCertificatesPK storeAttestationStatement(Long did, FIDO2AttestationStatement attStmt) throws CertificateException, NoSuchProviderException, CertificateEncodingException, SKFEException{
        AttestationCertificatesPK parentPk = null;
        ArrayList attestationCerts = attStmt.getX5c();
        if(attestationCerts != null){
            //Iterate from highest level intermediate to leaf certificate.
            for(int i = attestationCerts.size() - 1; i >= 0; i--){
                byte[] certBytes = (byte[]) attestationCerts.get(i);
                X509Certificate attCert = cryptoCommon.generateX509FromBytes(certBytes);

                //Add if attestation certificate not already added
                AttestationCertificates dbcert = getAttCertbean.getByIssuerDnSerialNumber(
                        attCert.getIssuerDN().getName(), attCert.getSerialNumber().toString());
                if(dbcert == null){
                    parentPk = addAttCertBean.execute(did, attCert, parentPk);
                }
                //Otherwise save parent certificate information
                else{
                    parentPk = dbcert.getAttestationCertificatesPK();
                }
            }
        }
        return parentPk;
    }

    private String parseRegistrationSettings(FIDO2AuthenticatorData authData, UserSessionInfo userInfo, String format){
        JsonObjectBuilder registrationSettings = Json.createObjectBuilder()
                .add(SKFSConstants.FIDO_REGISTRATION_SETTING_UP, authData.isUserPresent())
                .add(SKFSConstants.FIDO_REGISTRATION_SETTING_UV, authData.isUserVerified())
                .add(SKFSConstants.FIDO_REGISTRATION_SETTING_KTY, authData.getAttCredData().getFko().getKty())
                .add(SKFSConstants.FIDO_REGISTRATION_SETTING_ALG, authData.getAttCredData().getFko().getAlg())
                .add(SKFSConstants.FIDO_REGISTRATION_SETTING_DISPLAYNAME, userInfo.getDisplayName())
                .add(SKFSConstants.FIDO_REGISTRATION_SETTING_FMT, format);

        String userIcon = userInfo.getUserIcon();
        if(userIcon != null){
            registrationSettings.add(SKFSConstants.FIDO_REGISTRATION_SETTING_USERICON, userIcon);
        }

        return Base64.getUrlEncoder().withoutPadding().encodeToString(registrationSettings.build().toString().getBytes());
    }
}
