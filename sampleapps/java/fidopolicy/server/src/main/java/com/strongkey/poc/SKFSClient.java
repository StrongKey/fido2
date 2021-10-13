/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.poc;

import com.strongkey.skfs.soapstubs.SKFSServlet;
import com.strongkey.skfs.soapstubs.Soap;
import com.strongkey.utilities.Common;
import com.strongkey.utilities.Configurations;
import com.strongkey.utilities.Constants;
import com.strongkey.utilities.POCLogger;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
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
@Singleton
public class SKFSClient {
    private static final String CLASSNAME = SKFSClient.class.getName();

    // StrongKey API information
    private static final String WSPROTOCOL
            = Configurations.getConfigurationProperty("poc.cfg.property.wsprotocol");
    private static final String AUTHTYPE
            = Configurations.getConfigurationProperty("poc.cfg.property.authtype");
    private static final String ACCESSKEY
            = Configurations.getConfigurationProperty("poc.cfg.property.accesskey");
    private static final String SECRETKEY
            = Configurations.getConfigurationProperty("poc.cfg.property.secretkey");
    private static final String SVCUSERNAME
            = Configurations.getConfigurationProperty("poc.cfg.property.svcusername");
    private static final String SVCPASSWORD
            = Configurations.getConfigurationProperty("poc.cfg.property.svcpassword");
    private static final String SVCADMINUSER
            = Configurations.getConfigurationProperty("poc.cfg.property.skfs.adminusername");
    private static final String SVCADMINPASSWORD
            = Configurations.getConfigurationProperty("poc.cfg.property.skfs.adminpassword");
    private static final String PROTOCOL
            = Configurations.getConfigurationProperty("poc.cfg.property.protocol");
    private static final String PROTOCOL_VERSION
            = Configurations.getConfigurationProperty("poc.cfg.property.protocol.version");
    private static final String APIURI
            = Configurations.getConfigurationProperty("poc.cfg.property.apiuri");

    // Registration/Authentication options
    private static final String AUTHENTICATORATTACHMENT
            = Configurations.getConfigurationProperty("poc.cfg.property.fido.reg.option.authenticatorattachment");
    private static final String REQUIRERESIDENTKEY
            = Configurations.getConfigurationProperty("poc.cfg.property.fido.reg.option.requireresidentkey");
    private static final String ATTESTATION
            = Configurations.getConfigurationProperty("poc.cfg.property.fido.reg.option.attestation");

    private static JsonObject regOptions = null;
    private static JsonObject authOptions = null;

    // Request a registration challenge from the SKFS for a user
    public static String preregister(String username, String displayName, String policy) throws Exception {
        regOptions = null;
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_USERNAME, username)
                .add(Constants.SKFS_JSON_KEY_DISPLAYNAME, displayName)
                .add(Constants.SKFS_JSON_KEY_POLICY, policy)
                .add(Constants.SKFS_JSON_KEY_OPTIONS, getRegOptions(policy))
                .add("extensions", Constants.JSON_EMPTY);

        if (WSPROTOCOL.equalsIgnoreCase(Constants.PROTOCOL_SOAP)) {
            return callSKFSSoapApi(
                payloadBuilder,
                "preregister",
                getDid(policy));
        } else {
            return callSKFSRestApi(
                APIURI + Constants.REST_SUFFIX + Constants.PREREGISTER_ENDPOINT,
                payloadBuilder, getDid(policy), SVCUSERNAME, SVCPASSWORD);
        }
    }

    public static int getDid(String policy)
    {
        int SKFSDID=0;
        switch (policy)
            {
               case Constants.RESTRICTED_FIPS_POLICY:
                   SKFSDID = 8;
                   break;
                case Constants.RESTRICTED_APPLE_POLICY:
                    SKFSDID = 7;
                    break;
                case Constants.RESTRICTED_TPM_POLICY:
                    SKFSDID = 5;
                    break;
                case Constants.RESTRICTED_ANDROID_KEY_POLICY:
                    SKFSDID = 6;
                    break;
                case Constants.STRICT_ANDROID_SAFETYNET_POLICY:
                    SKFSDID = 4;
                    break;
                case Constants.STRICT_POLICY:
                    SKFSDID = 3;
                    break;
                case Constants.MODERATE_POLICY:
                    SKFSDID = 2;
                    break;
                case Constants.MINIMAL_POLICY:
                    SKFSDID = 1;
                    break;
                default:
                    break;
         }
        return SKFSDID;
    }

    // Set authenticator registration preferences from properties.
    private static JsonObject getRegOptions(String policy){
        // Construct Option Json if it has not already been parsed together
        if(regOptions == null){
            JsonObjectBuilder regOptionBuilder = Json.createObjectBuilder();
            JsonObjectBuilder authSelectBuilder = Json.createObjectBuilder();
            JsonObject authSelect;
            System.out.println("policy="+policy);

            switch (policy)
            {
               case Constants.RESTRICTED_FIPS_POLICY:
                   if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                       authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                   }
                       authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                       authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                   authSelect = authSelectBuilder.build();
                   if(!authSelect.isEmpty()){
                       regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                   }
                   if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                       regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                   }
                   break;

                case Constants.RESTRICTED_APPLE_POLICY:
                    if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                    }
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelect = authSelectBuilder.build();
                    if(!authSelect.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                    }
                    if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                    }
                    break;

                case Constants.RESTRICTED_TPM_POLICY:
                    if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                    }
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelect = authSelectBuilder.build();
                    if(!authSelect.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                    }
                    if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                    }
                    break;

                case Constants.RESTRICTED_ANDROID_KEY_POLICY:
                    if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                    }
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelect = authSelectBuilder.build();
                    if(!authSelect.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                    }
                    if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                    }
                    break;

                case Constants.STRICT_ANDROID_SAFETYNET_POLICY:
                    if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                    }
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelect = authSelectBuilder.build();
                    if(!authSelect.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                    }
                    if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                    }
                    break;
                case Constants.STRICT_POLICY:
                    if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                    }
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    authSelect = authSelectBuilder.build();
                    if(!authSelect.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                    }
                    if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                    }
                    break;
                case Constants.MODERATE_POLICY:
                    if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                    }
                    if(REQUIRERESIDENTKEY != null && !REQUIRERESIDENTKEY.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, REQUIRERESIDENTKEY);
                    }
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_PREFERRED);
                    authSelect = authSelectBuilder.build();
                    if(!authSelect.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                    }
                    if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                    }
                    break;
                case Constants.MINIMAL_POLICY:
                    if(AUTHENTICATORATTACHMENT != null && !AUTHENTICATORATTACHMENT.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTACHMENT, AUTHENTICATORATTACHMENT);
                    }
                    if(REQUIRERESIDENTKEY != null && !REQUIRERESIDENTKEY.isEmpty()){
                        authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_RESIDENTKEY, REQUIRERESIDENTKEY);
                    }
                    authSelectBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_PREFERRED);
                    authSelect = authSelectBuilder.build();
                    if(!authSelect.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_AUTHSELECTION, authSelect);
                    }
                    if(ATTESTATION != null && !ATTESTATION.isEmpty()){
                        regOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_ATTESTATION, ATTESTATION);
                    }
                    break;
                default:
                    break;
            }

            regOptions = regOptionBuilder.build();
        }
        return regOptions;
    }

    // Return a signed registration challenge to the SKFS
    public static String register(String username, String policy, String origin, JsonObject signedResponse) throws Exception {
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
        if (WSPROTOCOL.equalsIgnoreCase(Constants.PROTOCOL_SOAP)) {
            return callSKFSSoapApi(
                    payloadBuilder,
                    "register",
                    getDid(policy));
        } else {
            return callSKFSRestApi(
                    APIURI + Constants.REST_SUFFIX + Constants.REGISTER_ENDPOINT,
                    payloadBuilder,
                    getDid(policy), SVCUSERNAME, SVCPASSWORD);
        }
    }

    // Request an authentication challenge from the SKFS for a user
    public static String preauthenticate(String username, String policy) throws Exception {
        authOptions = null;
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_USERNAME, username)
                .add(Constants.SKFS_JSON_KEY_OPTIONS, getAuthOptions(policy));
        if (WSPROTOCOL.equalsIgnoreCase(Constants.PROTOCOL_SOAP)) {
            return callSKFSSoapApi(
                payloadBuilder,
                "preauthenticate",
                getDid(policy));
        } else {
            return callSKFSRestApi(
                APIURI + Constants.REST_SUFFIX + Constants.PREAUTHENTICATE_ENDPOINT,
                payloadBuilder,
                getDid(policy), SVCUSERNAME, SVCPASSWORD);
        }
    }

    // Set a preference for "user verification" on authentication.
    private static JsonObject getAuthOptions(String policy) {
        // Construct Option Json if it has not already been parsed together
        if (authOptions == null) {
            JsonObjectBuilder authOptionBuilder = Json.createObjectBuilder();
             switch (policy)
            {
               case Constants.RESTRICTED_FIPS_POLICY:
                 authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                 break;
                case Constants.RESTRICTED_APPLE_POLICY:
                    authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    break;
                case Constants.RESTRICTED_TPM_POLICY:
                    authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    break;
                case Constants.RESTRICTED_ANDROID_KEY_POLICY:
                    authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    break;
                case Constants.STRICT_ANDROID_SAFETYNET_POLICY:
                    authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    break;
                case Constants.STRICT_POLICY:
                    authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_REQUIRED);
                    break;
                case Constants.MODERATE_POLICY:
                    authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_PREFERRED);
                    break;
                case Constants.MINIMAL_POLICY:
                    authOptionBuilder.add(Constants.SKFS_JSON_KEY_OPTIONS_USERVERIFICATION, Constants.SKFS_JSON_KEY_OPTIONS_PREFERRED);
                    break;
                default:
                    break;
         }
            authOptions = authOptionBuilder.build();
        }
        return authOptions;
    }

    // Return a signed authentication challenge to the SKFS
    public static String authenticate(String username, String policy, String origin, JsonObject signedResponse, String clientUserAgent) throws Exception {
        JsonObject auth_metadata = javax.json.Json.createObjectBuilder()
                .add("version", PROTOCOL_VERSION) // ALWAYS since this is just the first revision of the code
                .add("last_used_location", "Sunnyvale, CA")
                .add("username", username)
                .add("origin", origin)
                .add("clientUserAgent", clientUserAgent)
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
        if (WSPROTOCOL.equalsIgnoreCase(Constants.PROTOCOL_SOAP)) {
            return callSKFSSoapApi(
                payloadBuilder,
                "authenticate",
                getDid(policy));
        } else {
            return callSKFSRestApi(
                APIURI + Constants.REST_SUFFIX + Constants.AUTHENTICATE_ENDPOINT,
                payloadBuilder, getDid(policy), SVCUSERNAME, SVCPASSWORD);
        }
    }

    // Request for all keys associated with a user
    public static String getKeys(String username, String policy) throws Exception {
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add(Constants.SKFS_JSON_KEY_USERNAME, username);
        if (WSPROTOCOL.equalsIgnoreCase(Constants.PROTOCOL_SOAP)) {
            return callSKFSSoapApi(
                payloadBuilder,
                "getkeysinfo",
                 getDid(policy));
        } else {
            return callSKFSRestApi(
                APIURI + Constants.REST_SUFFIX + Constants.GETKEYSINFO_ENDPOINT,
                payloadBuilder,
                getDid(policy), SVCADMINUSER, SVCADMINPASSWORD);
        }
    }

    // Delete a user's keyPOC-WS-ERR-1003
    public static String deregisterKey(String keyid, String policy) throws Exception {
        JsonObjectBuilder payloadBuilder = Json.createObjectBuilder()
                .add("keyid", keyid);
        if (WSPROTOCOL.equalsIgnoreCase(Constants.PROTOCOL_SOAP)) {
            return callSKFSSoapApi(
                payloadBuilder,
                "deregister",
                getDid(policy));
        } else {
            return callSKFSRestApi(
                APIURI + Constants.REST_SUFFIX + Constants.DEREGISTER_ENDPOINT,
                payloadBuilder,
                getDid(policy), SVCADMINUSER, SVCADMINPASSWORD);
        }
    }

    // Format HTTP request for resource
    private static String callSKFSRestApi(String requestURI, JsonObjectBuilder payload, int SKFSDID, String svcuser, String svcpassword) {
        JsonObjectBuilder svcinfoBuilder = Json.createObjectBuilder()
                .add("did", SKFSDID)
                .add("protocol", PROTOCOL);
        if (AUTHTYPE.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
                svcinfoBuilder.add("authtype", Constants.AUTHORIZATION_HMAC);
        } else {
            svcinfoBuilder
                .add("authtype", Constants.AUTHORIZATION_PASSWORD)
                .add("svcusername", svcuser)
                .add("svcpassword", svcpassword);
        }

        JsonObject body = Json.createObjectBuilder()
                .add("svcinfo", svcinfoBuilder)
                .add("payload", payload).build();

        String contentType = MediaType.APPLICATION_JSON;
        HttpPost request = new HttpPost(requestURI);
        request.setEntity(new StringEntity(body.toString(), ContentType.APPLICATION_JSON));

        // Build HMAC
        if (AUTHTYPE.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
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
        }
        request.addHeader("Content-Type", contentType);
        return callServer(request);
    }

    private static String callSKFSSoapApi(JsonObjectBuilder payload, String operation, int SKFSDID) throws Exception {
        String requestURI = APIURI + Constants.SKFS_WSDL_SUFFIX;
        SKFSServlet port = null;
        try {
            // Set up the URL and webService variables
            //  Create port object
            URL soapurl = new URL(requestURI);
            Soap service = new Soap(soapurl);
            port = service.getSKFSServletPort();
        } catch (MalformedURLException ex) {
            throw new Exception("Malformed hostport - " + requestURI);
        } catch (WebServiceException ex) {
            throw new Exception("It appears that the site - " + requestURI
                    + " - is (1) either down or (2) has no access over specified port or (3) has a digital certificate that is not in your JVM's truststore.  "
                    + "In case of (3), Please include it in the JAVA_HOME/jre/lib/security/cacerts file with "
                    + "the keytool -import command before attempting this operation again.  "
                    + "Please refer to the documentation on skceclient.jar at the "
                    + "above-mentioned URL on how to accomplish this.");
        }

        String payloadStr = payload.build().toString();
        String payloadHash = calculateHash(payloadStr);

        // Build HMAC
        long currentDate = System.currentTimeMillis();
        String hmac = null;
        HttpPost httpPost = null;
        String requestToHmac;
        if(AUTHTYPE.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            ContentType mimetype = ContentType.APPLICATION_JSON;
            StringEntity body = new StringEntity(payloadStr, mimetype);
            httpPost = new HttpPost(requestURI);
            httpPost.setEntity(body);
            requestToHmac = httpPost.getMethod() + "\n"
                + payloadHash + "\n"
                + mimetype.getMimeType() + "\n"
                + currentDate + "\n"
                + Constants.API_VERSION + "\n"
                + Constants.SKFS_WSDL_SUFFIX;
            hmac = calculateHMAC(SECRETKEY, requestToHmac);
        }

        // Build service info
        JsonObjectBuilder svcinfoJOB = javax.json.Json.createObjectBuilder()
                .add("did", SKFSDID)
                .add("protocol", PROTOCOL);
        String svcinfoStr;
        if(AUTHTYPE.equalsIgnoreCase(Constants.AUTHORIZATION_HMAC)) {
            svcinfoStr = svcinfoJOB
                    .add("authtype", Constants.AUTHORIZATION_HMAC)
                    .add("strongkey-api-version", Constants.API_VERSION)
                    .add("strongkey-content-sha256", calculateHash(payloadStr))
                    .add("authorization", "HMAC " + ACCESSKEY + ":" + hmac)
                    .add("timestamp", currentDate)
                    .build().toString();
        } else {
            svcinfoStr = svcinfoJOB
                    .add("authtype", Constants.AUTHORIZATION_PASSWORD)
                    .add("svcusername", SVCUSERNAME)
                    .add("svcpassword", SVCPASSWORD)
                    .build().toString();
        }

        switch (operation) {
            case "preregister":
                return port.preregister(svcinfoStr, payloadStr);
            case "register":
                return port.register(svcinfoStr, payloadStr);
            case "preauthenticate":
                return port.preauthenticate(svcinfoStr, payloadStr);
            case "authenticate":
                return port.authenticate(svcinfoStr, payloadStr);
            case "getkeysinfo":
                return port.getkeysinfo(svcinfoStr, payloadStr);
            case "deregister":
                return port.deregister(svcinfoStr, payloadStr);
            default:
                System.out.println("Invalid operation");
                return "";
        }
    }

    // Send HTTP request
    private static String callServer(HttpRequestBase request){
        try(CloseableHttpClient httpclient = HttpClients.createDefault()){
            HttpResponse response = httpclient.execute(request);
            return getAndVerifySuccessfulResponse(response);
        }
        catch (IOException ex) {

            POCLogger.logp(Level.SEVERE, CLASSNAME, "callSKFSRestApi", "POC-ERR-5001", ex.getLocalizedMessage());
            throw new WebServiceException(POCLogger.getMessageProperty("POC-ERR-5001"));
        }
    }

    // Verify that SKFS send back a "success"
    private static String getAndVerifySuccessfulResponse(HttpResponse skfsResponse){
        try {
            String responseJsonString = EntityUtils.toString(skfsResponse.getEntity());
            Common.parseJsonFromString(responseJsonString);     //Response is valid JSON by parsing it
            if (skfsResponse.getStatusLine().getStatusCode() != 200
                    || responseJsonString == null) {
                POCLogger.logp(Level.SEVERE, CLASSNAME, "verifySuccessfulCall",
                        "POC-ERR-5001", skfsResponse);
                throw new WebServiceException(POCLogger.getMessageProperty("POC-ERR-5001").replace("{0}",responseJsonString));
            }
            return responseJsonString;
        } catch (IOException | ParseException ex) {
            POCLogger.logp(Level.SEVERE, CLASSNAME, "verifySuccessfulCall",
                    "POC-ERR-5001", ex.getLocalizedMessage());
            throw new WebServiceException(POCLogger.getMessageProperty("POC-ERR-5001"));
        }
    }

    // Calculate message integrity hash
    private static String calculateHash(String contentToEncode) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            digest.update(contentToEncode.getBytes());
            return Base64.getEncoder().encodeToString(digest.digest());
        } catch (NoSuchAlgorithmException ex) {
            POCLogger.logp(Level.SEVERE, CLASSNAME, "calculateHash",
                    "POC-ERR-5000", ex.getLocalizedMessage());
            throw new WebServiceException(POCLogger.getMessageProperty("POC-ERR-5000"));
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
            POCLogger.logp(Level.SEVERE, CLASSNAME, "calculateHMAC",
                    "POC-ERR-5000", ex.getLocalizedMessage());
            throw new WebServiceException(POCLogger.getMessageProperty("POC-ERR-5000"));
        }
    }
}
