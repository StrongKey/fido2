/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfsclient.common;

public class Constants {

    // WSDL suffixes
    public static final String SKFS_WSDL_SUFFIX = "/skfs/soap?wsdl";

    //Operation names - for the web service
    public static final String REST_SUFFIX = "/skfs/rest";
    public static final String REST_PRE_REGISTER_ENDPOINT = "/preregister";
    public static final String REST_REGISTER_ENDPOINT = "/register";
    public static final String REST_PRE_AUTHENTICATE_ENDPOINT = "/preauthenticate";
    public static final String REST_AUTHENTICATE_ENDPOINT = "/authenticate";
    public static final String REST_GETKEYSINFO_ENDPOINT = "/getkeysinfo";
    public static final String REST_UPDATE_ENDPOINT = "/updatekeyinfo";
    public static final String REST_DEREGISTER_ENDPOINT = "/deregister";
    public static final String REST_PING_ENDPOINT = "/ping";

    //Unchanged operation endpoints
    public static final String CREATE_POLICY_ENDPOINT = "/fidopolicies";
    public static final String DELETE_POLICY_ENDPOINT = "/fidopolicies";
    public static final String PATCH_POLICY_ENDPOINT = "/fidopolicies";
    public static final String GET_POLICY_ENDPOINT = "/fidopolicies";

    public static final String COMMANDS_REGISTER = "R";
    public static final String COMMANDS_AUTHENTICATE = "A";
    public static final String COMMANDS_GETKEYSINFO = "G";
    public static final String COMMANDS_UPDATE = "U";
    public static final String COMMANDS_DEREGISTER = "D";
    public static final String COMMANDS_PING = "P";
//    public static final String COMMANDS_CREATE_POLICY = "CP";
//    public static final String COMMANDS_DELETE_POLICY = "DP";
//    public static final String COMMANDS_PATCH_POLICY = "PP";
//    public static final String COMMANDS_GET_POLICY = "GP";

    public static final String JSON_KEY_SERVLET_INPUT_USERNAME = "username";
    public static final String JSON_KEY_SERVLET_INPUT_REQUEST = "request";
    public static final String JSON_KEY_SERVLET_INPUT_RESPONSE = "response";
    public static final String JSON_KEY_SERVLET_INPUT_METADATA = "metadata";

    public static final String API_VERSION = "SK3_0";
    public static final String PROTOCOL_FIDO = "FIDO2_0";
    public static final String PROTOCOL_REST = "REST";
    public static final String PROTOCOL_SOAP = "SOAP";
    public static final String AUTHORIZATION_HMAC = "HMAC";
    public static final String AUTHORIZATION_PASSWORD = "PASSWORD";
    public static final String JSON_ATTESTATION_DIRECT = "{\"attestation\":\"direct\"}";
    public static final String JSON_EMPTY = "{}";

    public static class WebAuthn {

        public static String RELYING_PARTY = "rp";
        public static String RELYING_PARTY_NAME = "name";
        public static String CHALLENGE = "challenge";
        public static String RELYING_PARTY_RPID = "rpid";
        public static String USER = "user";
        public static String USER_NAME = "name";
        public static String USER_ID = "id";
        public static String USER_DISPLAY_NAME = "displayName";
        public static String ATTESTATION_PREFERENCE = "attestation";
        public static String PUBKEYCREDPARAMS = "pubKeyCredParams";
        public static String PUBKEYCREDPARAMS_ALG = "alg";
        public static String ATTESTATION_OJBECT = "attestationObject";
        public static String CLIENT_DATA_JSON = "clientDataJSON";
        public static String RAW_ID = "rawId";
        public static String ID = "id";
        public static String TYPE = "type";
        public static String RESPONSE = "response";
        public static String RP_ID = "rpId";
        public static String TIMEOUT = "timeout";
        public static String USER_HANDLE = "userHandle";
        public static String AUTHENTICATOR_DATA = "authenticatorData";
        public static String SIGNATURE = "signature";
    }
}
