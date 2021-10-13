/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.utilities;

import java.util.ArrayList;
import java.util.Arrays;

public class SKFSConstants {

    public static final String SKFE_LOGGER = "SKFE";

    //HASH map type constances
    public static final int MAP_USER_SESSION_INFO = 1;
    public static final int MAP_FIDO_SECRET_KEY = 2;
//    public static final int MAP_USER_KEY_POINTERS = 3;
    public static final int MAP_FIDO_KEYS = 4;
    public static final int MAP_FIDO_POLICIES = 5;
    public static final int MAP_FIDO_MDS = 6;

    /**
     * Constant for transports BT | BLE | USB | NFC
     */
    public static final int FIDO_TRANSPORT_NONE = 0;        //00000000;
    public static final int FIDO_TRANSPORT_NFC = 16;       //00010000;
    public static final int FIDO_TRANSPORT_USB = 32;       //00100000;
    public static final int FIDO_TRANSPORT_BLE = 64;       //01000000;
    public static final int FIDO_TRANSPORT_INTERNAL = 128;      //10000000;
    public static final int FIDO_TRANSPORT_USB_NFC = 48;       //00110000;
    public static final int FIDO_TRANSPORT_BLE_NFC = 80;       //01010000;
    public static final int FIDO_TRANSPORT_INTERNAL_NFC = 144;      //10010000;
    public static final int FIDO_TRANSPORT_BLE_USB = 96;       //01100000;
    public static final int FIDO_TRANSPORT_INTERNAL_USB = 160;      //10100000;
    public static final int FIDO_TRANSPORT_INTERNAL_BLE = 192;      //11000000;
    public static final int FIDO_TRANSPORT_BLE_USB_NFC = 112;      //01110000;
    public static final int FIDO_TRANSPORT_INTERNAL_BLE_NFC = 208;      //11010000;
    public static final int FIDO_TRANSPORT_INTERNAL_USB_NFC = 176;      //10110000;
    public static final int FIDO_TRANSPORT_INTERNAL_BLE_USB = 224;      //11100000;
    public static final int FIDO_TRANSPORT_INTERNAL_BLE_USB_NFC = 240;      //11110000;

    /**
     * FIDO protocols
     */
    public static final String FIDO_PROTOCOL_U2F = "U2F";
    public static final String FIDO_PROTOCOL_UAF = "UAF";

    /**
     * FIDO API Auth type
     */
    public static final String FIDO_API_AUTH_TYPE_HMAC = "hmac";
    public static final String FIDO_API_AUTH_TYPE_PASSWORD = "password";

    /**
     * FIDO protocol versions
     */
    public static final String FIDO_PROTOCOL_VERSION_U2F_V2 = "U2F_V2";
    public static final String FIDO_PROTOCOL_VERSION_2_0 = "FIDO2_0";
    public static final int USER_PRESENT_FLAG = 1;

        /**
     * Datatype min max values
     */
    public static final int TINYINT_MAX = 127;
    public static final int DID_MAX = 65535;
    public static final Long BIGINT_MAX = 9223372036854775807L;
    public static final int INT_MAX = 2147483647;

    /**
     * FIDO web-service methods
     */
    public static final String FIDO_METHOD_PREREGISTER = "preregister";
    public static final String FIDO_METHOD_REGISTER = "register";
    public static final String FIDO_METHOD_PREAUTH = "preauthenticate";
    public static final String FIDO_METHOD_AUTHENTICATE = "authenticate";
    public static final String FIDO_METHOD_PREAUTHORIZE = "preauthorize";
    public static final String FIDO_METHOD_AUTHORIZE = "authorize";
    public static final String FIDO_METHOD_DEREGISTER = "deregister";
    public static final String FIDO_METHOD_DEACTIVATE = "deactivate";
    public static final String FIDO_METHOD_ACTIVATE = "activate";
    public static final String FIDO_METHOD_GETKEYSINFO = "getkeysinfo";
    public static final String FIDO_METHOD_GETSERVERINFO = "getserverinfo";

    /**
     * FIDO - JSON keys used as part of the response JSON sent back from every
     * method in this servlet
     */
    public static final String JSON_KEY_SERVLET_RETURN_CHALLENGE = "Challenge";
    public static final String JSON_KEY_SERVLET_RETURN_RESPONSE = "Response";
    public static final String JSON_KEY_SERVLET_RETURN_MESSAGE = "Message";
    public static final String JSON_KEY_SERVLET_RETURN_ERROR = "Error";
    public static final String JSON_KEY_SERVLET_INPUT_USERNAME = "username";
    public static final String JSON_KEY_SERVLET_INPUT_EXTENSIONS = "extensions";
    public static final String JSON_KEY_SERVLET_INPUT_DISPLAY_NAME = "displayName";
    public static final String JSON_KEY_SERVLET_INPUT_REQUEST = "request";
    public static final String JSON_KEY_SERVLET_INPUT_RESPONSE = "response";
    public static final String JSON_KEY_SERVLET_INPUT_METADATA = "metadata";

    /**
     * FIDO Registration, Authentication message JSON keys
     */
    public static final String JSON_KEY_SESSIONID = "sessionId";
    public static final String JSON_KEY_NONCE = "challenge";
    public static final String JSON_KEY_BROWSERDATA = "browserData";
    public static final String JSON_KEY_CLIENTDATA = "clientData";
    public static final String JSON_KEY_ID = "id";
    public static final String JSON_KEY_RAW_ID = "rawId";
    public static final String JSON_KEY_CLIENTDATAJSON = "clientDataJSON";
    public static final String JSON_KEY_ENROLLDATA = "enrollData";
    public static final String JSON_KEY_REGSITRATIONDATA = "registrationData";
    public static final String JSON_KEY_ATTESTATIONOBJECT = "attestationObject";
    public static final String JSON_KEY_TOKENBINDING = "tokenBinding";
    public static final String JSON_KEY_REQUESTTYPE = "typ";
    public static final String JSON_KEY_REQUEST_TYPE = "type";
    public static final String JSON_KEY_SIGNDATA = "signData";
    public static final String JSON_KEY_SIGNATUREDATA = "signatureData";
    public static final String JSON_KEY_SIGNATURE = "signature";
    public static final String JSON_KEY_USERHANDLE = "userHandle";
    public static final String JSON_KEY_AUTHENTICATORDATA = "authenticatorData";
    public static final String JSON_KEY_REGISTEREDKEY = "registeredKeys";
    public static final String JSON_KEY_ENROLLCHALLENGES = "enrollChallenges";
    public static final String JSON_KEY_REGISTERREQUEST = "registerRequests";
    public static final String JSON_KEY_SERVERORIGIN = "origin";
    public static final String JSON_KEY_CROSSORIGIN = "crossOrigin";
    public static final String JSON_KEY_HASH_ALGORITHM = "hashAlgorithm";
    public static final String JSON_KEY_CHANNELID = "cid_pubkey";
    public static final String JSON_KEY_VERSION = "version";
    public static final String JSON_KEY_TRANSPORT = "transport";
    public static final String JSON_KEY_APP_ID = "appId";
    public static final String JSON_KEY_USER_ID = "userId";
    public static final String JSON_USER_PUBLIC_KEY_SERVLET = "userPublicKey";
    public static final String JSON_USER_KEY_HANDLE_SERVLET = "keyHandle";
    public static final String JSON_USER_COUNTER_SERVLET = "counter";
    public static final String JSON_USER_PRESENCE_SERVLET = "touch";
    
    /*
    TX related constants
    */
    
    public static final String TX_DETAIL = "txdetail";
    public static final String TX_TIMESTAMP = "txtime";
    public static final String TX_ID = "txid";
    public static final String TX_PAYLOAD = "txpayload";
    public static final String TX_NONCE = "nonce";
    public static final String TX_CHALLENGE = "challenge";
    public static final String TX_PUBLIC_KEY = "signerPublicKey";
    public static final String TX_AUTHORIZATION = "txauthorization";
    public static final String FIDOAuthenticatorReferences = "FIDOAuthenticatorReferences";
    public static final String FIDO_SIGNINGKEY_TYPE = "signingKeyType";
    public static final String FIDO_SIGNINGKEY_ALGO = "signingKeyAlgorithm";
    

    /* FIDO - others
     */
    public static final String REGISTER_CLIENT_DATA_OPTYPE = "navigator.id.finishEnrollment";
    public static final String AUTHENTICATE_CLIENT_DATA_OPTYPE = "navigator.id.getAssertion";
    
    public static final String APPLE_CREDENTIAL_CERT_EXTENSION_OID = "1.2.840.113635.100.8.2";

    public static final int MAX_RANDOM_NUMBER_SIZE_BITS = 1024;
    public static final int P256_PUBLIC_KEY_SIZE = 65;
    public static final int COUNTER_VALUE_BYTES = 4;

    public static final int DEFAULT_NUM_CHALLENGE_BYTES = 16;
    public static final int DEFAULT_NUM_USERID_BYTES = 32;

        /**
     * FIDO registration and authentication metadata
     */
    public static final String FIDO_METADATA_KEY_VERSION = "version";
    public static final String FIDO_METADATA_KEY_CREATE_LOC = "create_location";
    public static final String FIDO_METADATA_KEY_MODIFY_LOC = "last_used_location";
    public static final String FIDO_METADATA_KEY_USERNAME = "username";
    public static final String FIDO_METADATA_KEY_CLIENT_IP = "clientIp";
    public static final String FIDO_METADATA_KEY_CLIENT_USERAGENT = "clientUserAgent";
    public static final String FIDO_METADATA_KEY_ORIGIN = "origin";

        /**
     * FIDO user session types
     */
    public static final String FIDO_USERSESSION_REG = "register";
    public static final String FIDO_USERSESSION_AUTH = "auth";
    public static final String FIDO_USERSESSION_AUTHORIZE = "authorize";


        /**
     * FIDO JPA - JSON keys used as part of the response JSON sent back from
     * fido jpa module
     */
    public static final String JSON_KEY_FIDOJPA_RETURN_STATUS = "status";
    public static final String JSON_KEY_FIDOJPA_RETURN_MESSAGE = "message";


       /**
     * Miscellaneous json keys used
     */
    public static final String FIDO_JSON_KEY_USERNAME = "username";
    public static final String FIDO_JSON_KEY_RANDOMID = "randomid";

        // Policy Servlet Constants
    public static final String CREATE_FIDO_POLICY = "createFidoPolicy";
    public static final String READ_FIDO_POLICY = "readFidoPolicy";
    public static final String UPDATE_FIDO_POLICY = "updateFidoPolicy";
    public static final String DELETE_FIDO_POLICY = "deleteFidoPolicy";

        /**
     * Various LDAP user roles logically linked to groups in LDAP
     */
//    public static final String LDAP_ROLE_ADM = "ADM";       //  'cn=AdminAuthorized'
//    public static final String LDAP_ROLE_SRV = "SRV";       //  'cn=Services'
//    public static final String LDAP_ROLE_ENC = "ENC";       //  'cn=EncryptionAuthorized'
//    public static final String LDAP_ROLE_DEC = "DEC";       //  'cn=DecryptionAuthorized'
//    public static final String LDAP_ROLE_CMV = "CMV";       //  'cn=CloudMoveAuthorized'
//    public static final String LDAP_ROLE_LOADKEY = "LDKY";  //  'cn=LoadAuthorized'
//    public static final String LDAP_ROLE_SIGN = "SIGN";     //  'cn=SignAuthorized'
//    public static final String LDAP_ROLE_REMOVEKEY = "RMKY"; //  'cn=RemoveAuthorized'
    public static final String LDAP_ROLE_FIDO = "FIDO"; //  'cn=RemoveAuthorized'
    public static final String LDAP_ROLE_FIDO_REG = "FIDOREG"; //  'cn=FIDORegAuthorized'
    public static final String LDAP_ROLE_FIDO_SIGN = "FIDOSIGN"; //  'cn=FIDOSIGNAuthorized'
    public static final String LDAP_ROLE_FIDO_AUTHZ = "FIDOAUTHZ"; //  'cn=FIDOAUTHZAuthorized'
    public static final String LDAP_ROLE_FIDO_ADMIN = "FIDOADM"; //  'cn=RemoveAuthorized'
    public static final String LDAP_ROLE_FIDO_MONITOR = "FIDOMONITOR"; //  'cn=RemoveAuthorized'
    public static final String LDAP_ROLE_FIDO_POLICY_MANAGEMENT = "FIDOPOLICYMGT"; //  'cn=RemoveAuthorized'

     /**
     * LDAP attribute keys to fetch metadata of a user
     */
    public static final String LDAP_ATTR_KEY_SURNAME = "sn";
    public static final String LDAP_ATTR_KEY_FNAME = "givenName";
    public static final String LDAP_ATTR_KEY_UID = "uid";
    public static final String LDAP_ATTR_KEY_COMMONNAME = "cn";
    public static final String LDAP_ATTR_KEY_DN = "dn";
    public static final String LDAP_ATTR_KEY_EMAILADDRESSES = "RegisteredEmailAddresses";
    public static final String LDAP_ATTR_KEY_PRIMARYEMAIL = "PrimaryEmail";
    public static final String LDAP_ATTR_KEY_PHONENUMBERS = "RegisteredPhoneNumbers";
    public static final String LDAP_ATTR_KEY_PRIMARYPHONE = "PrimaryPhone";
    public static final String LDAP_ATTR_KEY_DEFAULTTARGET = "Defaulttarget";
    public static final String LDAP_ATTR_KEY_FIDOENABLED = "FIDOKeysEnabled";
    public static final String LDAP_ATTR_KEY_2STEPVERIFY = "TwoStepVerification";
    public static final String LDAP_ATTR_KEY_DOMAINID = "did";

    /**
     * FIDO2 registration setting parameters
     */
    public static final String FIDO_REGISTRATION_SETTING_UV = "UV";
    public static final String FIDO_REGISTRATION_SETTING_UP = "UP";
    public static final String FIDO_REGISTRATION_SETTING_KTY = "KTY";
    public static final String FIDO_REGISTRATION_SETTING_ALG = "ALG";
    public static final String FIDO_REGISTRATION_SETTING_FMT = "attestationFormat";
    public static final String FIDO_REGISTRATION_SETTING_DISPLAYNAME = "DISPLAYNAME";
    public static final String FIDO_REGISTRATION_SETTING_USERICON = "USERICON";
    public static final ArrayList<String> ALL_RSA_SIGNATURES = new ArrayList<String>( 
            Arrays.asList("RS256", "RS384", "RS512", "PS256", "PS384", "PS384")); 
    public static final ArrayList<String> ALL_EC_SIGNATURES = new ArrayList<String>( 
            Arrays.asList("ES256", "ES384", "ES512", "EdDSA", "ES256K")); 
    public static final ArrayList<String> ALL_EC_CURVES= new ArrayList<String>( 
            Arrays.asList("secp256r1", "secp384r1", "secp521r1", "curve25519")); 
    /**
     * FIDO2 Policy attribute Keys
     */
    //Cryptography
    public static final String POLICY_ATTR_ALGORITHMS = "algorithms";
    public static final String POLICY_CRYPTO_ELLIPTIC_CURVES = "curves";
    public static final String POLICY_CRYPTO_ALLOWED_RSA_SIGNATURES = "rsa";
    public static final String POLICY_CRYPTO_ALLOWED_EC_SIGNATURES = "signatures";
    public static final String POLICY_CRYPTO_ATTESTATION_TYPES = "attestation_types";
    public static final String POLICY_CRYPTO_CHALLENGE_LENGTH = "challenge_length";
    
    //Authenticators
    public static final String POLICY_AUTHENTICATOR_AAGUIDS = "allowedAaguids";


    //RP
    public static final String POLICY_ATTR_RP = "rp";
    public static final String POLICY_RP_NAME = "name";
    public static final String POLICY_RP_ID = "id";
    public static final String POLICY_RP_ICON = "icon";

    //Timeout
    public static final String POLICY_ATTR_TIMEOUT = "timeout";

    //MDS
    public static final String POLICY_ATTR_MDS = "mds";
    public static final String POLICY_MDS_ENDPOINTS = "endpoints";
    public static final String POLICY_MDS_ENDPOINT_URL = "url";
    public static final String POLICY_MDS_ENDPOINT_TOKEN = "token";
    public static final String POLICY_MDS_CERTIFICATION = "certification";

    //Tokenbinding
    public static final String POLICY_ATTR_TOKENBINDING = "tokenbinding";

    //Counter
    public static final String POLICY_ATTR_COUNTER = "requireCounter";
    public static final String POLICY_COUNTER_REQUIRECOUNTER = "requireCounter";
    public static final String POLICY_COUNTER_REQUIRECOUNTERINCREASE = "requireIncrease";


    //Store Signatures
    public static final String POLICY_ATTR_STORESIGNATURES = "integritySignatures";

    //jwt
    public static final String POLICY_JWT = "jwt";
    public static final String POLICY_JWT_VALIDITY = "jwtKeyValidity";
    public static final String POLICY_JWT_ALGO = "algorithms";
    public static final String POLICY_JWT_DURATION = "duration";
    public static final String POLICY_JWT_REQUIRED = "required";
    public static final String POLICY_JWT_RENEWAL = "jwtRenewalWindow";
    public static final String POLICY_JWT_KEY_VALIDITY = "jwtKeyValidity";
    public static final String POLICY_JWT_SIGN_CERT_DN = "DN";
    public static final String POLICY_JWT_SIGN_CERT_COUNT =  "certsPerServer";  
    public static final String POLICY_JWT_SIGN = "signingCerts";
    
    //Registration
    public static final String POLICY_ATTR_REGISTRATION = "registration";
    public static final String POLICY_REGISTRATION_ICON = "icon";
    public static final String POLICY_REGISTRATION_DISPLAYNAME = "displayName";
    public static final String POLICY_REGISTRATION_USERID_LENGTH = "userid_length";
    public static final String POLICY_REGISTRATION_EXCLUDECREDENTIALS = "excludeCredentials";
    public static final String POLICY_REGISTRATION_AUTHENTICATORSELECTION = "authenticatorSelection";
    public static final String POLICY_REGISTRATION_AUTHENTICATORATTACHMENT = "attachment";
    public static final String POLICY_REGISTRATION_REQUIRERESIDENTKEY = "discoverableCredential";
    public static final String POLICY_REGISTRATION_USERVERIFICATION = "userVerification";
    public static final String POLICY_REGISTRATION_ATTESTATION = "attestation";
    
    //Attestation
    public static final String POLICY_ATTESTATION_CONVEYANCE = "conveyance";
    public static final String POLICY_ATTESTATION_FORMATS = "formats";
    public static final String POLICY_ATTESTATION_ENTERPRISE = "enterprise";
    public static final String POLICY_ATTESTATION_ENTERPRISE_ATTESTATION_TYPE = "attestationType";
    public static final String POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_RPID = "authorizedRpid";
    public static final String POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_TRUSTSTORE = "authorizedTruststore";
    public static final String POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_SERIAL = "authorizedSerial";
    public static final String POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_DN = "authorizedDN";
    public static final String POLICY_ATTESTATION_ENTERPRISE_REQUIRED_OID = "requiredOID";
    
    public static final String POLICY_ATTESTATION = "attestation";
    
    
    //System
    public static final String POLICY_SYSTEM_FIDO_POLICY = "FidoPolicy";
    public static final String POLICY_SYSTEM =  "system";        
    public static final String POLICY_SYSTEM_START_DATE =  "startDate";
    public static final String POLICY_SYSTEM_END_DATE =  "endDate";
    public static final String POLICY_SYSTEM_VERSION =  "version";
    public static final String POLICY_SYSTEM_USER_PRESENCE_TIMEOUT =  "userPresenceTimeout";
    public static final String POLICY_SYSTEM_USER_VERIFICATION =  "userVerification";
    public static final String POLICY_SYSTEM_TRANSPORT =  "transport";

    //Authentication
    public static final String POLICY_ATTR_AUTHENTICATION = "authentication";
    public static final String POLICY_AUTHENTICATION_ALLOWCREDENTIALS = "allowCredentials";
    public static final String POLICY_AUTHENTICATION_USERVERIFICATION = "userVerification";

    //Authorization 
    public static final String POLICY_ATTR_AUTHORIZATION= "authorization";
    public static final String POLICY_ATTR_AUTHORIZATION_MAXDATALENGTH= "maxdataLength";
    public static final String POLICY_ATTR_AUTHORIZATION_PRESERVE= "preserve";
    
    //Extensions
    public static final String POLICY_ATTR_EXTENSIONS = "extensions";
    public static final String POLICY_ATTR_EXTENSIONS_UVM = "uvm";
    public static final String POLICY_ATTR_EXTENSIONS_UVM_METHODS = "allowedMethods";
    public static final String POLICY_ATTR_EXTENSIONS_UVM__KEY_PROTECTIONS = "allowedKeyProtections";
    public static final String POLICY_ATTR_EXTENSIONS_UVM_KEY_PROTECTIONS_TYPES = "allowedProtectionTypes";
    public static final String POLICY_ATTR_EXTENSIONS_LARGE_BLOB = "largeBlobSupport";
    public static final String POLICY_EXTENSIONS_EXAMPLE = "example.extension";
    public static final String POLICY_EXTENSIONS_APPID = "appid";
    public static final String POLICY_ATTR_EXTENSIONS_INPUT_UVM = "uvm";
    public static final String POLICY_ATTR_EXTENSIONS_INPUT_LARGEBLOB = "largeBlob";
    public static final String POLICY_ATTR_EXTENSIONS_INPUT_LARGEBLOB_SUPPORT = "support";
    public static final String POLICY_ATTR_EXTENSIONS_INPUT_LARGEBLOB_READ = "read";
    public static final String POLICY_ATTR_EXTENSIONS_INPUT_LARGEBLOB_WRITE = "write";
    public static final String POLICY_ATTR_EXTENSIONS_OUTPUT_UVM = "uvm";
    public static final String POLICY_ATTR_EXTENSIONS_OUTPUT_LARGEBLOB = "largeBlob";
    
    //MDS
     public static final String POLICY_ATTR_MDS_STATUS = "authenticatorStatus";
     public static final String POLICY_ATTR_MDS_STATUS_REPORT = "authenticatorStatusReport";
     public static final String POLICY_ATTR_MDS_STATUS_REPORT_STATUS = "status";
     public static final String POLICY_ATTR_MDS_STATUS_REPORT_PRIORITY = "priority";
     public static final String POLICY_ATTR_MDS_STATUS_REPORT_DECISION = "decision";
     
     

    //Misc constant values defined in FIDO2 policies
    public static final String POLICY_CONST_ENABLED = "enabled";
    public static final String POLICY_CONST_DISABLED = "disabled";
    public static final String POLICY_CONST_REQUIRED = "required";
    public static final String POLICY_CONST_DISCOURAGED = "discouraged";
    public static final String POLICY_CONST_PREFERRED = "preferred";
    public static final String POLICY_CONST_DIRECT = "direct";
    public static final String POLICY_CONST_INDIRECT = "indirect";
    public static final String POLICY_CONST_ENTERPRISE = "enterprise";
       
    public static final String POLICY_CONST_NONE = "none";
    

    //FIDO 2 - Webauthn
    public static final String FIDO2_PREREG_ATTR_RP = "rp";
    public static final String FIDO2_PREREG_ATTR_USER = "user";
    public static final String FIDO2_PREREG_ATTR_CHALLENGE = "challenge";
    public static final String FIDO2_PREREG_ATTR_KEYPARAMS = "pubKeyCredParams";
    public static final String FIDO2_PREREG_ATTR_TIMEOUT = "timeout";
    public static final String FIDO2_PREREG_ATTR_EXCLUDECRED = "excludeCredentials";
    public static final String FIDO2_PREREG_ATTR_AUTHENTICATORSELECT = "authenticatorSelection";
    public static final String FIDO2_PREREG_ATTR_ATTESTATION = "attestation";
    public static final String FIDO2_PREREG_ATTR_EXTENSIONS = "extensions";

    public static final String FIDO2_PREAUTH_ATTR_CHALLENGE = "challenge";
    public static final String FIDO2_PREAUTH_ATTR_TIMEOUT = "timeout";
    public static final String FIDO2_PREAUTH_ATTR_RPID = "rpId";
    public static final String FIDO2_PREAUTH_ATTR_ALLOWCREDENTIALS = "allowCredentials";
    public static final String FIDO2_PREAUTH_ATTR_UV = "userVerification";
    public static final String FIDO2_PREAUTH_ATTR_EXTENSIONS = "extensions";

    public static final String FIDO2_ATTR_ID = "id";
    public static final String FIDO2_ATTR_NAME = "name";
    public static final String FIDO2_ATTR_DISPLAYNAME = "displayName";
    public static final String FIDO2_ATTR_ICON = "icon";
    public static final String FIDO2_ATTR_TYPE = "type";
    public static final String FIDO2_ATTR_ALG = "alg";
    public static final String FIDO2_ATTR_TRANSPORTS = "transports";
    public static final String FIDO2_ATTR_ATTACHMENT = "authenticatorAttachment";
    public static final String FIDO2_ATTR_RESIDENTKEY = "requireResidentKey";
    public static final String FIDO2_ATTR_USERVERIFICATION = "userVerification";

    //Constants defined by Webauhthn spec
    public static final String FIDO2_CONST_PUBLIC_CREDENTIAL_TYPE = "public-key";
    public static final String FIDO2_CONST_TRANSPORT_USB = "usb";
    public static final String FIDO2_CONST_TRANSPORT_NFC = "nfc";
    public static final String FIDO2_CONST_TRANSPORT_BLE = "ble";
    public static final String FIDO2_CONST_TRANSPORT_INTERNAL = "internal";
    public static final String FIDO2_CONST_ATTACHMENT_PLATFORM = "platform";
    public static final String FIDO2_CONST_ATTACHMENT_CROSS = "cross-platform";
    public static final String FIDO2_CONST_ATTESTATION_NONE = "none";
    public static final String FIDO2_CONST_ATTESTATION_INDIRECT = "indirect";
    public static final String FIDO2_CONST_ATTESTATION_DIRECT = "direct";


        /**
     * *********************************************************************
     */
    /**
     * Parameter for ZMQ service when its starting
     */
    public static final int ZMQ_SERVICE_STARTING = 0;

    /**
     * Parameter for ZMQ service when its running
     */
    public static final int ZMQ_SERVICE_RUNNING = 1;

    /**
     * Parameter for ZMQ service when its shutting down
     */
    public static final int ZMQ_SERVICE_STOPPING = 2;

    /**
     * Parameter for ZMQ service when its stopped
     */
    public static final int ZMQ_SERVICE_STOPPED = 3;

    /**
     * Parameter for ZMQ service when its inactive
     */
    public static final int ZMQ_SERVICE_INACTIVE = 4;

}
