/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.webauthntutorial;

import com.strongkey.utilities.Configurations;
import com.strongkey.utilities.Constants;
import com.strongkey.utilities.WebauthnTutorialLogger;
import java.io.IOException;
import java.io.StringReader;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.WebServiceException;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

//  Call the StrongKey Fido Engine (SKFS) via REST API to handle Webauthn/FIDO2
//  key management.
public class SKFSClient {
    private static final String CLASSNAME = SKFSClient.class.getName();

    // StrongKey API information
    private static final int SKFSDID
            = Integer.parseInt(Configurations.getConfigurationProperty("webauthntutorial.cfg.property.did"));
    private static final String ACCESSKEY
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.accesskey");
    private static final String SECRETKEY
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.secretkey");
    private static final String PROTOCOL
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.protocol");
    private static final String PROTOCOL_VERSION
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.protocol.version");
    private static final String APIURI
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.apiuri");

    // Registration/Authentication options
    private static final String AUTHENTICATORATTACHMENT
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.fido.reg.option.authenticatorattachment");
    private static final String REQUIRERESIDENTKEY
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.fido.reg.option.requireresidentkey");
    private static final String REG_USERVERIFICATION
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.fido.reg.option.userverification");
    private static final String ATTESTATION
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.fido.reg.option.attestation");
    private static final String AUTH_USERVERIFICATION
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.fido.auth.option.userverification");

    private static JsonObject regOptions = null;
    private static JsonObject authOptions = null;

    // Request a registration challenge from the SKFS for a user
    public static String preregister(String username, String displayName) {
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_USERNAME, username)
                .add(Constants.SKFS_JSON_KEY_DISPLAYNAME, displayName)
                .add(Constants.SKFS_JSON_KEY_OPTIONS, getRegOptions())
                .add("extensions", Constants.JSON_EMPTY);
        return callSKFSRestApi(
            APIURI + Constants.REST_SUFFIX + Constants.PREREGISTER_ENDPOINT,
            payloadBuilder);
    }

    // Set authenticator registration preferences from properties.
    private static JsonObject getRegOptions(){
        // Construct Option Json if it has not already been parsed together
        if(regOptions == null){
            JsonObjectBuilder regOptionBuilder = Json.createObjectBuilder();
            JsonObjectBuilder authSelectBuilder = Json.createObjectBuilder();
            if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
            }
            if(REQUIRERESIDENTKEY != null && !REQUIRERESIDENTKEY.isEmpty()){
                authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, Boolean.valueOf(REQUIRERESIDENTKEY));
            }
            if(REG_USERVERIFICATION != null && !REG_USERVERIFICATION.isEmpty()){
                authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, REG_USERVERIFICATION);
            }
            JsonObject authSelect = authSelectBuilder.build();
            if(!authSelect.isEmpty()){
                regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
            }
            if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
            }
            regOptions = regOptionBuilder.build();
        }
        return regOptions;
    }

    // Return a signed registration challenge to the SKFS
    public static String register(String username, String origin, JsonObject signedResponse) {
        JsonObject reg_metadata = javax.json.Json.createObjectBuilder()
                .add("version", PROTOCOL_VERSION) // ALWAYS since this is just the first revision of the code
                .add("create_location", "Sunnyvale, CA")
                .add("username", username)
                .add("origin", origin).build();
        JsonObjectBuilder reg_inner_response = javax.json.Json.createObjectBuilder()
                .add("attestationObject", signedResponse.getJsonObject("response").getString("attestationObject"))
                .add("clientDataJSON", signedResponse.getJsonObject("response").getString("clientDataJSON"));
        JsonObject reg_response = javax.json.Json.createObjectBuilder()
                .add("id", signedResponse.getString("id"))
                .add("rawId", signedResponse.getString("rawId"))
                .add("response", reg_inner_response) // inner response object
                .add("type", signedResponse.getString("type")).build();
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add("publicKeyCredential", reg_response)
                .add("strongkeyMetadata", reg_metadata);
        return callSKFSRestApi(
            APIURI + Constants.REST_SUFFIX + Constants.REGISTER_ENDPOINT,
            payloadBuilder);
    }

    // Request an authentication challenge from the SKFS for a user
    public static String preauthenticate(String username) {
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_USERNAME, username)
                .add(Constants.SKFS_JSON_KEY_OPTIONS, getAuthOptions());
        return callSKFSRestApi(
            APIURI + Constants.REST_SUFFIX + Constants.PREAUTHENTICATE_ENDPOINT,
            payloadBuilder);
    }

    // Set a preference for "user verification" on authentication.
    private static JsonObject getAuthOptions() {
        // Construct Option Json if it has not already been parsed together
        if (authOptions == null) {
            JsonObjectBuilder authOptionBuilder = Json.createObjectBuilder();
            if (AUTH_USERVERIFICATION != null && !AUTH_USERVERIFICATION.isEmpty()) {
                authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, AUTH_USERVERIFICATION);
            }
            authOptions = authOptionBuilder.build();
        }
        return authOptions;
    }

    // Return a signed authentication challenge to the SKFS
    public static String authenticate(String username, String origin, JsonObject signedResponse) {
        JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
                .add("version", PROTOCOL_VERSION) // ALWAYS since this is just the first revision of the code
                .add("last_used_location", "Sunnyvale, CA")
                .add("username", username)
                .add("origin", origin)
                .build();
        JsonObjectBuilder auth_inner_response = javax.json.Json.createObjectBuilder()
                .add("authenticatorData", signedResponse.getJsonObject("response").getString("authenticatorData"))
                .add("signature", signedResponse.getJsonObject("response").getString("signature"))
                .add("userHandle", signedResponse.getJsonObject("response").getString("userHandle"))
                .add("clientDataJSON", signedResponse.getJsonObject("response").getString("clientDataJSON"));
        JsonObject auth_response = javax.json.Json.createObjectBuilder()
                .add("id", signedResponse.getString("id"))
                .add("rawId", signedResponse.getString("rawId"))
                .add("response", auth_inner_response) // inner response object
                .add("type", signedResponse.getString("type"))
                .build();
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add("publicKeyCredential", auth_response)
                .add("strongkeyMetadata", auth_metadata);
        return callSKFSRestApi(
            APIURI + Constants.REST_SUFFIX + Constants.AUTHENTICATE_ENDPOINT,
            payloadBuilder);
    }

    // Request for all keys associated with a user
    public static String getKeys(String username) {
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_USERNAME, username);
        return callSKFSRestApi(
            APIURI + Constants.REST_SUFFIX + Constants.GETKEYSINFO_ENDPOINT,
            payloadBuilder);
    }

    // Delete a user's key
    public static String deregisterKey(String keyid) {
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add("keyid", keyid);
        return callSKFSRestApi(
            APIURI + Constants.REST_SUFFIX + Constants.DEREGISTER_ENDPOINT,
            payloadBuilder);
    }

    // Format HTTP request for resource
    private static String callSKFSRestApi(String requestURI, JsonObjectBuilder payload){
        JsonObjectBuilder svcinfoBuilder = Json.createObjectBuilder()
                .add("did", SKFSDID)
                .add("protocol", PROTOCOL)
                .add("authtype", Constants.AUTHORIZATION_HMAC);

        JsonObject body = Json.createObjectBuilder()
                .add("svcinfo", svcinfoBuilder)
                .add("payload", payload).build();

        String contentType = MediaType.APPLICATION_JSON;
        HttpPost request = new HttpPost(requestURI);
        request.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));

        String payloadHash = (body == null)? "" : calculateHash(body.getJsonObject("payload").toString());
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String requestToHmac = request.getMethod() + "\n"
                + payloadHash + "\n"
                + contentType + "\n"
                + currentDate + "\n"
                + Constants.API_VERSION + "\n"
                + request.getURI().getPath();
        String hmac = calculateHMAC(SECRETKEY, requestToHmac);

        request.addHeader("Date", currentDate);
        request.addHeader("Authorization", "HMAC " + ACCESSKEY + ":" + hmac);
        request.addHeader("strongkey-api-version", Constants.API_VERSION);
        request.addHeader("strongkey-content-sha256", payloadHash);
        request.addHeader("Content-Type", contentType);

        return callServer(request);
    }

    // Send HTTP request
    private static String callServer(HttpRequestBase request){
        try(CloseableHttpClient httpclient = HttpClients.createDefault()){
            System.out.println(request);
            HttpResponse response = httpclient.execute(request);
            return getAndVerifySuccessfulResponse(response);
        }
        catch (IOException ex) {
            WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "callSKFSRestApi", "WEBAUTHN-ERR-5001", ex.getLocalizedMessage());
            throw new WebServiceException(WebauthnTutorialLogger.getMessageProperty("WEBAUTHN-ERR-5001"));
        }
    }

    // Verify that SKFS send back a "success"
    private static String getAndVerifySuccessfulResponse(HttpResponse skfsResponse){
        try {
            String responseJsonString = EntityUtils.toString(skfsResponse.getEntity());
            verifyJson(responseJsonString);

            if (skfsResponse.getStatusLine().getStatusCode() != 200
                    || responseJsonString == null) {
                WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "verifySuccessfulCall",
                        "WEBAUTHN-ERR-5001", skfsResponse);
                throw new WebServiceException(WebauthnTutorialLogger.getMessageProperty("WEBAUTHN-ERR-5001"));
            }

            return responseJsonString;
        } catch (IOException | ParseException ex) {
            WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "verifySuccessfulCall",
                    "WEBAUTHN-ERR-5001", ex.getLocalizedMessage());
            throw new WebServiceException(WebauthnTutorialLogger.getMessageProperty("WEBAUTHN-ERR-5001"));
        }
    }

    // Verify that SKFE response is the proper format
    private static void verifyJson(String responseJsonString){
        System.out.println(responseJsonString);
        try (JsonReader jsonReader = Json.createReader(new StringReader(responseJsonString))) {
            jsonReader.readObject();
        }
        catch(JsonParsingException ex){
            WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "getResponseFromString",
                    "WEBAUTHN-ERR-5001", ex.getLocalizedMessage());
            throw new WebServiceException(WebauthnTutorialLogger.getMessageProperty("WEBAUTHN-ERR-5001"));
        }
    }

    // Calculate message integrity hash
    private static String calculateHash(String contentToEncode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(contentToEncode.getBytes());
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "calculateHash",
                    "WEBAUTHN-ERR-5000", ex.getLocalizedMessage());
            throw new WebServiceException(WebauthnTutorialLogger.getMessageProperty("WEBAUTHN-ERR-5000"));
        }
    }

    // Calculate HMAC used for REST API authentication
    private static String calculateHMAC(String secret, String data) {
        try {
            SecretKeySpec signingKey = new SecretKeySpec(DatatypeConverter.parseHexBinary(secret), "HmacSHA256");
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(signingKey);
            byte[] rawHmac = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(rawHmac);
        } catch (NoSuchAlgorithmException | InvalidKeyException | IllegalStateException ex) {
            WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "calculateHMAC",
                    "WEBAUTHN-ERR-5000", ex.getLocalizedMessage());
            throw new WebServiceException(WebauthnTutorialLogger.getMessageProperty("WEBAUTHN-ERR-5000"));
        }
    }
}
