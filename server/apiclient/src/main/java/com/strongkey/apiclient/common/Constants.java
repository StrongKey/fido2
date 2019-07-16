/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.apiclient.common;

public class Constants {

    //Operation names - for the web service
    public static final String REST_SUFFIX = "/api/domains/";
    public static final String PRE_REGISTER_ENDPOINT = "/fidokeys/challenge";
    public static final String REGISTER_ENDPOINT = "/fidokeys";
    public static final String PRE_AUTH_ENDPOINT = "/fidokeys/authenticate/challenge";
    public static final String AUTHENTICATE_ENDPOINT = "/fidokeys/authenticate";
    public static final String GETKEYSINFO_ENDPOINT = "/fidokeys";
    public static final String UPDATE_ENDPOINT = "/fidokeys";
    public static final String DEACTIVATE_ENDPOINT = "/fidokeys";
    public static final String CREATE_POLICY_ENDPOINT = "/fidopolicies";
    public static final String DELETE_POLICY_ENDPOINT = "/fidopolicies";
    public static final String PATCH_POLICY_ENDPOINT = "/fidopolicies";
    public static final String GET_POLICY_ENDPOINT = "/fidopolicies";

    public static final String COMMANDS_REG = "R";
    public static final String COMMANDS_AUTH = "A";
    public static final String COMMANDS_GETKEYS = "G";
    public static final String COMMANDS_DEACT = "D";
    public static final String COMMANDS_UP = "U";
    public static final String COMMANDS_CREATE_POLICY = "CP";
    public static final String COMMANDS_DELETE_POLICY = "DP";
    public static final String COMMANDS_PATCH_POLICY = "PP";
    public static final String COMMANDS_GET_POLICY = "GP";

    public static final String JSON_KEY_SERVLET_INPUT_USERNAME = "username";
    public static final String JSON_KEY_SERVLET_INPUT_REQUEST = "request";
    public static final String JSON_KEY_SERVLET_INPUT_RESPONSE = "response";
    public static final String JSON_KEY_SERVLET_INPUT_METADATA = "metadata";

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
