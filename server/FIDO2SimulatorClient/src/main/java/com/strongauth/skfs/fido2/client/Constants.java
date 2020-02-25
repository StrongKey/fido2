/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
* **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * Constants used in this program.
 */

package com.strongauth.skfs.fido2.client;

public class Constants
{
    public static final int FIDO_REGISTER_COUNTER = 0;
    public static int FIDO_AUTHENTICATE_COUNTER = 0;

    // Webservice Operation names
    public static final String REST_WEBSERVICE_SUFFIX = "/api/domains/";
    public static final String PRE_REGISTER_ENDPOINT = "/fidokeys/registration/challenge";
    public static final String REGISTER_ENDPOINT = "/fidokeys/registration";
    public static final String PRE_AUTHENTICATE_ENDPOINT = "/fidokeys/authentication/challenge";
    public static final String AUTHENTICATE_ENDPOINT = "/fidokeys/authentication";
    public static final String GETKEYSINFO_ENDPOINT = "/fidokeys";
    public static final String UPDATE_ENDPOINT = "/fidokeys";
    public static final String DEACTIVATE_ENDPOINT = "/fidokeys";
    public static final String CREATE_POLICY_ENDPOINT = "/fidopolicies";
    public static final String DELETE_POLICY_ENDPOINT = "/fidopolicies";
    public static final String PATCH_POLICY_ENDPOINT = "/fidopolicies";
    public static final String GET_POLICY_ENDPOINT = "/fidopolicies";

    public static final String COMMANDS_REGISTER = "R";
    public static final String COMMANDS_AUTHENTICATE = "A";
    public static final String COMMANDS_GETKEYS = "G";
    public static final String COMMANDS_DEACTIVATE = "D";
    public static final String COMMANDS_UPDATE = "U";
    public static final String COMMANDS_CREATE_POLICY = "CP";
    public static final String COMMANDS_DELETE_POLICY = "DP";
    public static final String COMMANDS_UPDATE_POLICY = "PP";
    public static final String COMMANDS_GET_POLICY = "GP";

    public static final String JSON_KEY_SERVLET_INPUT_USERNAME = "username";
    public static final String JSON_KEY_SERVLET_INPUT_REQUEST = "request";
    public static final String JSON_KEY_SERVLET_INPUT_RESPONSE = "response";
    public static final String JSON_KEY_SERVLET_INPUT_METADATA = "metadata";

//    public static class WebAuthn {
//
//        public static String RELYING_PARTY = "rp";
//        public static String RELYING_PARTY_NAME = "name";
//        public static String CHALLENGE = "challenge";
//        public static String RELYING_PARTY_RPID = "rpid";
//        public static String USER = "user";
//        public static String USER_NAME = "name";
//        public static String USER_ID = "id";
//        public static String USER_DISPLAY_NAME = "displayName";
//        public static String ATTESTATION_PREFERENCE = "attestation";
//        public static String PUBKEYCREDPARAMS = "pubKeyCredParams";
//        public static String PUBKEYCREDPARAMS_ALG = "alg";
//        public static String ATTESTATION_OJBECT = "attestationObject";
//        public static String CLIENT_DATA_JSON = "clientDataJSON";
//        public static String RAW_ID = "rawId";
//        public static String ID = "id";
//        public static String TYPE = "type";
//        public static String RESPONSE = "response";
//        public static String RP_ID = "rpId";
//        public static String TIMEOUT = "timeout";
//        public static String USER_HANDLE = "userHandle";
//        public static String AUTHENTICATOR_DATA = "authenticatorData";
//        public static String SIGNATURE = "signature";
//    }
}
