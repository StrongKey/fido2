/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.webauthntutorial;

import com.strongkey.utilities.Common;
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
import javax.ejb.Singleton;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;
import javax.xml.bind.DatatypeConverter;
import javax.xml.ws.WebServiceException;
import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

//  Call the StrongKey Fido Engine (SKFS) via REST API to handle Webauthn/FIDO2
//  key management.
@Singleton
public class SKFSClient {
    private static final String CLASSNAME = SKFSClient.class.getName();

    // StrongKey API information
    private static final String SKFSDID
            = Configurations.getConfigurationProperty("webauthntutorial.cfg.property.did");
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
    private static final String SKFSFIDOKEYURI
            = APIURI + "/" + Constants.SKFS_PATH_DOMAINS + "/" + SKFSDID + "/" + Constants.SKFS_PATH_FIDOKEYS;
    
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
        JsonObjectBuilder bodyBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_PROTOCOL, PROTOCOL)
                .add(Constants.SKFS_JSON_KEY_USERNAME, username)
                .add(Constants.SKFS_JSON_KEY_DISPLAYNAME, displayName);
        JsonObject options = getRegOptions();
        bodyBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS, options.toString());
        return callSKFSRestApi(
                SKFSFIDOKEYURI + "/" + Constants.SKFS_PATH_REGISTRATION + "/" + Constants.SKFS_PATH_CHALLENGE,
                bodyBuilder.build().toString(),
                HttpMethod.POST);
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
                authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, REQUIRERESIDENTKEY);
            }
            if(REG_USERVERIFICATION != null && !REG_USERVERIFICATION.isEmpty()){
                authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, REG_USERVERIFICATION);
            }
            JsonObject authSelect = authSelectBuilder.build();
            if(!authSelect.isEmpty()){
                regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, authSelect);
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
        JsonObject metadata = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_VERSION, PROTOCOL_VERSION)
                .add(Constants.SKFS_JSON_KEY_CREATELOC, Constants.CREATE_LOCATION)
                .add(Constants.SKFS_JSON_KEY_USERNAME, username)
                .add(Constants.SKFS_JSON_KEY_ORIGIN, origin)
                .build();
        String body = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_PROTOCOL, PROTOCOL)
                .add(Constants.SKFS_JSON_KEY_RESPONSE, signedResponse.toString())
                .add(Constants.SKFS_JSON_KEY_METADATA, metadata.toString())
                .build().toString();
        return callSKFSRestApi(
                SKFSFIDOKEYURI + "/" + Constants.SKFS_PATH_REGISTRATION,
                body,
                HttpMethod.POST);
    }
    
    // Request an authentication challenge from the SKFS for a user
    public static String preauthenticate(String username) {
        JsonObjectBuilder bodyBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_PROTOCOL, PROTOCOL)
                .add(Constants.SKFS_JSON_KEY_USERNAME, username);
        JsonObject options = getAuthOptions();
        bodyBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS, options.toString());
        return callSKFSRestApi(
                SKFSFIDOKEYURI + "/" + Constants.SKFS_PATH_AUTHENTICATION + "/" + Constants.SKFS_PATH_CHALLENGE,
                bodyBuilder.build().toString(),
                HttpMethod.POST);
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
        JsonObject metadata = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_VERSION, PROTOCOL_VERSION)
                .add(Constants.SKFS_JSON_KEY_USEDLOC, Constants.LAST_USED_LOCATION)
                .add(Constants.SKFS_JSON_KEY_USERNAME, username)
                .add(Constants.SKFS_JSON_KEY_ORIGIN, origin)
                .build();
        String body = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_PROTOCOL, PROTOCOL)
                .add(Constants.SKFS_JSON_KEY_RESPONSE, signedResponse.toString())
                .add(Constants.SKFS_JSON_KEY_METADATA, metadata.toString())
                .build().toString();
        return callSKFSRestApi(
                SKFSFIDOKEYURI + "/" + Constants.SKFS_PATH_AUTHENTICATION,
                body,
                HttpMethod.POST);
    }
    
    // Request for all keys associated with a user
    public static String getKeys(String username) {
        return callSKFSRestApi(
                SKFSFIDOKEYURI + "?" + Constants.SKFS_QUERY_KEY_USERNAME + "=" + username, 
                null, 
                HttpMethod.GET);
    }
    
    // Delete a user's keyWEBAUTHN-WS-ERR-1003
    public static String deregisterKey(String keyid) {
        return callSKFSRestApi(
                SKFSFIDOKEYURI + "/" + keyid,
                null,
                HttpMethod.DELETE);
    }
    
    // Format HTTP request for resource
    private static String callSKFSRestApi(String requestURI, String body, String method){
        HttpRequestBase request;
        String contentType = MediaType.APPLICATION_JSON;
        switch(method){
            case HttpMethod.GET:
                request = new HttpGet(requestURI);
                contentType = "";
                break;
            case HttpMethod.POST:
                request = new HttpPost(requestURI);
                ((HttpPost) request).setEntity(new StringEntity(body, ContentType.APPLICATION_JSON));
                break;
            case HttpMethod.DELETE:
                request = new HttpDelete(requestURI);
                contentType = "";
                break;
            default:
                WebauthnTutorialLogger.logp(Level.SEVERE, CLASSNAME, "callSKFSRestApi",
                        "WEBAUTHN-ERR-5001", "Invalid HTTP Method");
                throw new WebServiceException(WebauthnTutorialLogger.getMessageProperty("WEBAUTHN-ERR-5001"));
        }
        String apiVersion = "2.0";
        String currentDate = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z").format(new Date());
        String bodyHash = (body == null)? "" : calculateHash(body);
        
        String queryParams = request.getURI().getQuery();
        if (queryParams != null) {
            queryParams = "?" + queryParams;
        } else {
            queryParams = "";
        }
        
        String requestToHmac = request.getMethod() + "\n"
                + bodyHash + "\n"
                + contentType + "\n"
                + currentDate + "\n"
                + apiVersion + "\n"
                + request.getURI().getPath()+queryParams;
        String hmac = calculateHMAC(SECRETKEY, requestToHmac);
        
        request.addHeader("Date", currentDate);
        request.addHeader("Authorization", "HMAC " + ACCESSKEY + ":" + hmac);
        request.addHeader("strongkey-api-version", apiVersion);
        if(body != null){
            request.addHeader("strongkey-content-sha256", bodyHash);
            request.addHeader("Content-Type", contentType);
        }
        
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
            Common.parseJsonFromString(responseJsonString);     //Response is valid JSON by parsing it
            
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
