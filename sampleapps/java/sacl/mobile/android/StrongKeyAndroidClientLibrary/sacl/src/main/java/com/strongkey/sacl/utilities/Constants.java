/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * Constants used within the library
 */

package com.strongkey.sacl.utilities;

public class Constants {

    public static final String AKS_CERTIFICATE_CHAIN = "AKSCertificateChain";
    public static final String AKS_CERTIFICATE_CHAIN_SIZE = "Size";
    public static final String AKS_CERTIFICATE_CHAIN_CERTIFICATES = "Certificates";
    public static final String AKS_ENTRY_ALIAS = "alias";
    public static final String ATTESTATION_CONNECTION_TYPE = "TLS";
    final public static String SECURE_ELEMENT = "SE";
    final public static String TRUSTED_EXECUTION_ENVIRONMENT = "SE";

    // Error keys
    final public static String ERROR_CERTIFICATE_NOT_FOUND = "ERROR_CERTIFICATE_NOT_FOUND";
    final public static String ERROR_EMPTY_KEYSTORE = "ERROR_EMPTY_KEYSTORE";
    final public static String ERROR_EMULATOR = "ERROR_EMULATOR";
    final public static String ERROR_EXCEPTION = "ERROR_EXCEPTION";
    final public static String ERROR_INVALID_FUNCTION = "ERROR_INVALID_FUNCTION";
    final public static String ERROR_INVALID_WEBSERVICE = "ERROR_INVALID_WEBSERVICE";
    final public static String ERROR_KEY_NOT_FOUND = "ERROR_KEY_NOT_FOUND";
    final public static String ERROR_NETWORK_UNAVAILABLE = "ERROR_NETWORK_UNAVAILABLE";
    final public static String ERROR_NOT_IMPLEMENTED_YET = "ERROR_NOT_IMPLEMENTED_YET";
    final public static String ERROR_NOT_PRIVATE_KEY = "ERROR_NOT_PRIVATE_KEY";
    final public static String ERROR_NULL_CONTEXT = "ERROR_NULL_CONTEXT";
    final public static String ERROR_SIGNATURE_OBJECT_NOT_INITIALIZED = "ERROR_SIGNATURE_OBJECT_NOT_INITIALIZED";
    final public static String ERROR_SINGLE_CERTIFICATE_IN_CHAIN = "ERROR_SINGLE_CERTIFICATE_IN_CHAIN";
    final public static String ERROR_TLS_CONNECTION = "ERROR_TLS_CONNECTION";
    final public static String ERROR_UNAUTHENTICATED_USER = "ERROR_UNAUTHENTICATED_USER";

    // FIDO2 key and signature related parameters
    final public static int    FIDO2_KEY_ECDSA_KEYSIZE = 256;
    final public static String FIDO2_KEY_ECDSA_CURVE = "secp256r1";
    final public static String FIDO2_KEYSTORE_PROVIDER = "AndroidKeyStore";
    final public static String FIDO2_SIGNATURE_ALGORITHM = "SHA256withECDSA";
    final public static String FIDO2_SIGNATURE_PLAINTEXT = "Be the change that you wish to see in the world. - M.K. Gandhi";
    final public static int    FIDO2_USER_AUTHENTICATION_VALIDITY = 5;  // Minutes - For PSD2 RTS
    final public static String FIDO2_KEY_LABEL_KEYNAME = "keyname";
    final public static String FIDO2_KEY_LABEL_ORIGIN = "origin";
    final public static String FIDO2_KEY_LABEL_ALGORITHM = "algorithm";
    final public static String FIDO2_KEY_LABEL_SIZE = "size";
    final public static String FIDO2_KEY_LABEL_USER_AUTH = "userauth";
    final public static String FIDO2_KEY_LABEL_SEMODULE = "semodule";
    final public static String FIDO2_KEY_LABEL_SEMODULE_TYPE = "semoduleType";
    final public static String FIDO2_KEY_LABEL_HEX_PUBLIC_KEY = "publickey";

    final public static String FIDO2_TASK_PARAM_CHALLENGE = "FIDO2_TASK_PARAM_CHALLENGE";
    final public static String FIDO2_TASK_PARAM_RPID = "FIDO2_TASK_PARAM_RPID";
    final public static String FIDO2_TASK_PARAM_USERID = "FIDO2_TASK_PARAM_USERID";
    final public static String FIDO2_TASK_PARAM_CLIENTDATAHASH = "FIDO2_TASK_PARAM_CLIENTDATAHASH";
    final public static String FIDO2_TASK_PARAM_EXCLUDE_CREDENTIALS = "FIDO2_TASK_PARAM_EXCLUDE_CREDENTIALS";
    final public static String FIDO2_TASK_PARAM_ALLOW_CREDENTIALS = "FIDO2_TASK_PARAM_ALLOW_CREDENTIALS";
    final public static String FIDO2_TASK_PARAM_CREDENTIALID = "FIDO2_TASK_PARAM_CREDENTIALID";

    final public static String TASK = "TASK";
    final public static String TASK_GENERATE_KEYS_RESPONSE_JSON = "TASK_GENERATE_KEYS_RESPONSE_JSON";
    final public static String TASK_DECRYPT_RESPONSE_JSON = "TASK_DECRYPT_RESPONSE_JSON";
    final public static String TASK_ENCRYPT_RESPONSE_JSON = "TASK_ENCRYPT_RESPONSE_JSON";
    final public static String TASK_HMAC_RESPONSE_JSON = "TASK_HMAC_RESPONSE_JSON";
    final public static String TASK_SIGN_RESPONSE_JSON = "TASK_SIGN_RESPONSE_JSON";
    final public static String TASK_LIST_KEYS_RESPONSE_JSON = "TASK_LIST_KEYS_RESPONSE_JSON";
    final public static String TASK_DELETE_KEY_RESPONSE_BOOLEAN = "TASK_DELETE_KEY_RESPONSE_BOOLEAN";
    final public static String TASK_TEST_ANDROID_KEYSTORE_RESPONSE_JSON = "TASK_TEST_ANDROID_KEYSTORE_RESPONSE_JSON";

    final public static String ANDROID_OS_DETAILS_CONTAINER = "aosdcontainer";
    final public static String ANDROID_OS_DETAILS = "AndroidOsDetails";
    final public static String ANDROID_VERSION_RELEASE = "ANDROID_VERSION_RELEASE";
    final public static String ANDROID_VERSION_INCREMENTAL = "ANDROID_VERSION_INCREMENTAL";
    final public static String ANDROID_VERSION_SDK_NUMBER = "ANDROID_VERSION_SDK_NUMBER";
    final public static String ANDROID_BOARD = "ANDROID_BOARD";
    final public static String ANDROID_BOOTLOADER = "ANDROID_BOOTLOADER";
    final public static String ANDROID_BRAND = "ANDROID_BRAND";
    final public static String ANDROID_CPU_ABI = "ANDROID_CPU_ABI";
    final public static String ANDROID_CPU_ABI2 = "ANDROID_CPU_ABI2";
    final public static String ANDROID_DEVICE = "ANDROID_DEVICE";
    final public static String ANDROID_DISPLAY = "ANDROID_DISPLAY";
    final public static String ANDROID_FINGERPRINT = "ANDROID_FINGERPRINT";
    final public static String ANDROID_HARDWARE = "ANDROID_HARDWARE";
    final public static String ANDROID_HOST = "ANDROID_HOST";
    final public static String ANDROID_ID = "ANDROID_ID";
    final public static String ANDROID_MANUFACTURER = "ANDROID_MANUFACTURER";
    final public static String ANDROID_MODEL = "ANDROID_MODEL";
    final public static String ANDROID_PRODUCT = "ANDROID_PRODUCT";
    final public static String ANDROID_SERIAL = "ANDROID_SERIAL";
    final public static String ANDROID_SUPPORTED_32BIT_ABI = "ANDROID_SUPPORTED_32BIT_ABI";
    final public static String ANDROID_SUPPORTED_64BIT_ABI = "ANDROID_SUPPORTED_64BIT_ABI";
    final public static String ANDROID_TAGS = "ANDROID_TAGS";
    final public static String ANDROID_TIME = "ANDROID_TIME";
    final public static String ANDROID_TYPE = "ANDROID_TYPE";
    final public static String ANDROID_USER = "ANDROID_USER";

    // General JSON Keys
    public static final String JSON_KEY_ID = "id";
    public static final String JSON_KEY_DID = "did";
    public static final String JSON_KEY_SID = "sid";
    public static final String JSON_KEY_UID = "uid";
    public static final String JSON_KEY_DEVID = "devid";
    public static final String JSON_KEY_AZID = "azid";
    public static final String JSON_KEY_ARID = "arid";
    public static final String JSON_KEY_ATID = "atid";
    public static final String JSON_KEY_RDID = "rdid";
    public static final String JSON_KEY_USERNAME = "username";
    public static final String JSON_KEY_PASSWORD = "password";
    public static final String JSON_KEY_CHALLENGE = "challenge";

    // USER_DEVICES Json Object Keys
    public static final String JSON_KEY_USER_DEVICE = "UserDevice";
    public static final String JSON_KEY_DEVICE_MOBILE_NUMBER = "deviceMobileNumber";
    public static final String JSON_KEY_DEVICE_MANUFACTURER = "manufacturer";
    public static final String JSON_KEY_DEVICE_MODEL = "model";
    public static final String JSON_KEY_DEVICE_FINGERPRINT = "fingerprint";
    public static final String JSON_KEY_DEVICE_OS_RELEASE = "osRelease";
    public static final String JSON_KEY_DEVICE_OS_SDK_NUMBER = "osSdkNumber";
    public static final String JSON_KEY_DEVICE_STATUS = "status";
    public static final String JSON_KEY_DEVICE_AKS_SECURITY = "aksSecurity";
    public static final String JSON_KEY_DEVICE_SNET_SECURITY = "snetSecurity";

    // USER_DEVICE_AUTHORIZATIONS Json Object Keys
    public static final String JSON_KEY_USER_DEVICE_AUTHORIZATION = "UserDeviceAuthorization";
    public static final String JSON_KEY_USER_DEVICE_AUTHORIZATION_STATUS = "status";
    public static final String JSON_KEY_AKS_AUTHORIZATION = "aksauthz";
    public static final String JSON_KEY_AKS_CHALLENGE = "aksChallenge";
    public static final String JSON_KEY_SNET_AUTHORIZATION = "snetauthz";
    public static final String JSON_KEY_SNET_CHALLENGE = "snetChallenge";

    // Attestation keys
    public static final String JSON_KEY_ANDROID_OS_DETAILS_CONTAINER = "aosdcontainer";
    public static final String JSON_KEY_AKS_CERTIFICATE_CHAIN = "AKSCertificateChain";
    public static final String JSON_KEY_AKS_CERTIFICATE_CHAIN_SIZE = "Size";
    public static final String JSON_KEY_AKS_CERTIFICATE_CHAIN_CERTIFICATES = "Certificates";

    // USER_TRANSACTIONS Json Object Keys
    public static final String JSON_KEY_USER_TRANSACTION = "UserTransaction";
    public static final String JSON_KEY_TRANSACTION_SERVICE = "service";
    public static final String JSON_KEY_TRANSACTION_TARGET_FILE = "targetFile";

    // REGISTERED_DEVICES_VIEW Json Object Keys
    public static final String JSON_KEY_REGISTERED_DEVICE = "RegisteredDevice";

    // PUBLIC_KEY_CREDENTIAL Json Object Keys
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL = "PublicKeyCredential";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_ID = "id";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_DID = "did";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_UID = "uid";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_PRCID = "prcId";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_COUNTER = "counter";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_TYPE = "type";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_RPID = "rpid";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_USERID = "userid";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_USERNAME = "username";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_DISPLAYNAME = "displayName";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_CREDENTIALID = "credentialId";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYALIAS = "keyAlias";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYORIGIN = "keyOrigin";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYSIZE = "keySize";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_SEMODULE = "seModule";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_PUBLICKEY = "publicKey";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYALGORITHM = "keyAlgorithm";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_USERHANDLE = "userHandle";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_AUTHENTICATORDATA = "authenticatorData";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_CLIENTDATAJSON = "clientDataJson";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_CBORATTESTATION = "cborAttestation";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_JSONATTESTATION = "jsonAttestation";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_CREATEDATE = "createDate";

    // AUTHENTICATION_SIGNATURE Json Object Keys
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL = "AuthenticationSignature";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_ID = "id";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_DID = "did";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_UID = "uid";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_PACID = "pacId";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_RPID = "rpid";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREDENTIALID = "credentialId";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_AUTHENTICATORDATA = "authenticatorData";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CLIENTDATAJSON = "clientDataJson";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_SIGNATURE = "signature";
    public static final String JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREATEDATE = "createDate";

    // AUTHORIZATION_SIGNATURE Json Object Keys
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL = "AuthorizationSignature";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_ID = "id";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_DID = "did";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_UID = "uid";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_PAZID = "pazId";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_RPID = "rpid";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXDETAIL = "txdetail";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXID = "txid";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXPAYLOAD = "txpayload";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXTIME = "txtime";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_NONCE = "nonce";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CHALLENGE = "challenge";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CREDENTIALID = "credentialId";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_AUTHENTICATORDATA = "authenticatorData";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CLIENTDATAJSON = "clientDataJson";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_SIGNATURE = "signature";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_RESPONSE = "Response";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_RESPONSE_JSON = "responseJson";
    public static final String JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CREATEDATE = "createDate";

    // FIDO_AUTHENTICATOR_REFERENCES Json Object Keys
    public static final String JSON_KEY_FAR_LABEL = "FIDOAuthenticatorReferences";
    public static final String JSON_KEY_FAR_RESPONSE_LABEL = "Response";
    public static final String JSON_KEY_FAR_PROTOCOL_LABEL = "protocol";
    public static final String JSON_KEY_FAR_ID_LABEL = "id";
    public static final String JSON_KEY_FAR_RAWID_LABEL = "rawId";
    public static final String JSON_KEY_FAR_USER_HANDLE_LABEL = "userHandle";
    public static final String JSON_KEY_FAR_RPID_LABEL = "rpId";
    public static final String JSON_KEY_FAR_AUTHENTICATORDATA_LABEL = "authenticatorData";
    public static final String JSON_KEY_FAR_CLIENTDATAJSON_LABEL = "clientDataJSON";
    public static final String JSON_KEY_FAR_AAGUID_LABEL = "aaguid";
    public static final String JSON_KEY_FAR_AUTHORIZATION_TIME_LABEL = "authorizationTime";
    public static final String JSON_KEY_FAR_UV_LABEL = "uv";
    public static final String JSON_KEY_FAR_UP_LABEL = "up";
    public static final String JSON_KEY_FAR_SIGNER_PUBLIC_KEY_LABEL = "signerPublicKey";
    public static final String JSON_KEY_FAR_SIGNATURE_LABEL = "signature";
    public static final String JSON_KEY_FAR_USED_FOR_THIS_TRANSACTION_LABEL = "usedForThisTransaction";
    public static final String JSON_KEY_FAR_SIGNING_KEY_TYPE_LABEL = "signingKeyType";
    public static final String JSON_KEY_FAR_SIGNING_KEY_ALGORITHM_LABEL = "signingKeyAlgorithm";



    public enum ANDROID_KEYSTORE_TASK {
        ANDROID_KEYSTORE_GENERATE_KEYS,
        ANDROID_KEYSTORE_DECRYPT,
        ANDROID_KEYSTORE_ENCRYPT,
        ANDROID_KEYSTORE_HMAC,
        ANDROID_KEYSTORE_SIGN,
        ANDROID_KEYSTORE_LIST_KEYS,
        ANDROID_KEYSTORE_DELETE_KEY,
        ANDROID_KEYSTORE_TEST_CAPABILITIES
    }

    // Miscellaneous constants
    public static final int HTTP_SUCCESS = 200;

    public enum KEY_ORIGIN {
        GENERATED,
        IMPORTED,
        UNKNOWN
    }

    public enum SECURITY_MODULE {
        SECURE_ELEMENT,
        TRUSTED_EXECUTION_ENVIRONMENT
    }

    public enum SACL_OBJECT_TYPES {
        PREREGISTER_CHALLENGE,
        PREAUTHENTICATE_CHALLENGE,
        PREAUTHORIZE_CHALLENGE,
        AUTHENTICATION_SIGNATURE,
        AUTHORIZATION_SIGNATURE,
        PUBLIC_KEY_CREDENTIAL,
        PUBLIC_KEY_CREDENTIAL_LIST,
        PROTECTED_CONFIRMATION_CREDENTIAL,
        SACL_REPOSITORY
    }
    // ------------------------------------------------------------------------------------------ //

    // FIDO Service Json Object Keys
    public static final String JSON_KEY_SACL_FIDO_SERVICE_INPUT = "saclFidoServiceInput";

    // SACL's FIDO Input JSON Keys
    public static final String JSON_KEY_SACL_CREDENTIALS = "saclCredentials";
    public static final String JSON_KEY_SACL_FIDO_DID = "did";
    public static final String JSON_KEY_SACL_FIDO_SERVICE = "service";
    public static final String JSON_KEY_SACL_FIDO_UID = "uid";
    public static final String JSON_KEY_SACL_FIDO_USERNAME = "username";
    public static final String JSON_KEY_SACL_FIDO_DEVID = "devid";
    public static final String JSON_KEY_SACL_FIDO_RDID = "rdid";
    public static final String JSON_KEY_SACL_FIDO_DISPLAY_NAME = "displayname";
    public static final String JSON_KEY_SACL_FIDO_TRANSACTION = "transaction";
    public static final String JSON_KEY_SACL_FIDO_TRANSACTION_CART = "cart";
    public static final String JSON_KEY_SACL_FIDO_TRANSACTION_ID = "id";
    public static final String JSON_KEY_SACL_FIDO_TRANSACTION_PAYLOAD = "payload";

    // FIDO2 Payload JSON Keys
    public static final String JSON_KEY_FIDO_PAYLOAD = "payload";
    public static final String JSON_KEY_FIDO_PAYLOAD_ATTESTATION_CONVEYANCE = "attestation";
    public static final String JSON_KEY_FIDO_PAYLOAD_ATTESTATION_DIRECT = "direct";
    public static final String JSON_KEY_FIDO_PAYLOAD_ATTESTATION_OBJECT_LABEL = "attestationObject";
    public static final String JSON_KEY_FIDO_PAYLOAD_CLIENT_DATA_JSON = "clientDataJSON";
    public static final String JSON_KEY_FIDO_PAYLOAD_CLIENT_EXTENSIONS = "clientExtensions";
    public static final String JSON_KEY_FIDO_PAYLOAD_CREATE_LOCATION = "publicKeyCredential";
    public static final String JSON_KEY_FIDO_PAYLOAD_DISPLAY_NAME = "displayname";
    public static final String JSON_KEY_FIDO_PAYLOAD_EXTENSIONS = "extensions";
    public static final String JSON_KEY_FIDO_PAYLOAD_ID_LABEL = "id";
    public static final String JSON_KEY_FIDO_PAYLOAD_KEYID = "keyid";
    public static final String JSON_KEY_FIDO_PAYLOAD_METADATA_CREATE_LOCATION = "create_location";
    public static final String JSON_KEY_FIDO_PAYLOAD_METADATA_LAST_USED_LOCATION = "last_used_location";
    public static final String JSON_KEY_FIDO_PAYLOAD_METADATA_ORIGIN = "origin";
    public static final String JSON_KEY_FIDO_PAYLOAD_METADATA_USERNAME_LABEL = "username";
    public static final String JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION = "1.0";
    public static final String JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION_LABEL = "version";
    public static final String JSON_KEY_FIDO_PAYLOAD_MODIFY_LOCATION = "modify_location";
    public static final String JSON_KEY_FIDO_PAYLOAD_OPTIONS = "options";
    public static final String JSON_KEY_FIDO_PAYLOAD_ORIGIN = "origin";
    public static final String JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY = "public-key";
    public static final String JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL = "publicKeyCredential";
    public static final String JSON_KEY_FIDO_PAYLOAD_RAW_ID_LABEL = "rawId";
    public static final String JSON_KEY_FIDO_PAYLOAD_RESPONSE = "response";
    public static final String JSON_KEY_FIDO_PAYLOAD_STATUS = "status";
    public static final String JSON_KEY_FIDO_PAYLOAD_STRONGKEY_METADATA = "strongkeyMetadata";
    public static final String JSON_KEY_FIDO_PAYLOAD_TXID = "txid";
    public static final String JSON_KEY_FIDO_PAYLOAD_TXPAYLOAD = "txpayload";
    public static final String JSON_KEY_FIDO_PAYLOAD_TYPE = "type";
    public static final String JSON_KEY_FIDO_PAYLOAD_USERNAME = "username";

    // FIDO2 Preregister Response JSON Keys
    public static final String JSON_KEY_PREREG_RESPONSE_ROOMDB_ID = "id";
    public static final String JSON_KEY_PREREG_RESPONSE = "Response";
    public static final String JSON_KEY_PREREG_RESPONSE_RP_OBJECT = "rp";
    public static final String JSON_KEY_PREREG_RESPONSE_RP_OBJECT_NAME = "name";
    public static final String JSON_KEY_PREREG_RESPONSE_RP_OBJECT_ID = "id";
    public static final String JSON_KEY_RPID = "rpid";

    public static final String JSON_KEY_PREREG_RESPONSE_USER_OBJECT = "user";
    public static final String JSON_KEY_PREREG_RESPONSE_USER_OBJECT_NAME = "name";
    public static final String JSON_KEY_PREREG_RESPONSE_USER_OBJECT_ID = "id";
    public static final String JSON_KEY_PREREG_RESPONSE_USER_OBJECT_DISPLAYNAME = "displayName";
    public static final String JSON_KEY_PREREG_RESPONSE_USER_CREDENTIAL_ID = "credentialId";
    public static final String JSON_KEY_PREREG_RESPONSE_USER_USER_ID = "userId";

    public static final String JSON_KEY_PREREG_RESPONSE_CHALLENGE = "challenge";
    public static final String JSON_KEY_PREREG_RESPONSE_ATTESTATION_CONVEYANCE = "attestation";

    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS = "pubKeyCredParams";
    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS_TYPE = "type";
    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_CRED_PARAMS_ALG = "alg";

    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_ES256_LABEL = "ES256";
    public static final int JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_ES256 = -7;
    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_EC_LABEL = "EC";
    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_RS256_LABEL = "RS256";
    public static final int JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_RS256 = -257;
    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_ALG_RSA_LABEL = "RSA";

    public static final String JSON_KEY_PREREG_RESPONSE_PUBLIC_KEY_TYPE = "public-key";

    public static final String JSON_KEY_PREREG_RESPONSE_EXCLUDE_CREDENTIALS = "excludeCredentials";
    public static final String JSON_KEY_PREREG_RESPONSE_EXCLUDE_CREDENTIALS_TYPE = "type";
    public static final String JSON_KEY_PREREG_RESPONSE_EXCLUDE_CREDENTIALS_ID = "id";
    public static final String JSON_KEY_PREREG_RESPONSE_EXCLUDE_CREDENTIALS_TRANSPORTS = "transports";

    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_SELECTION = "authenticatorSelection";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT = "authenticatorAttachment";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_PLATFORM = "platform";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_CROSS_PLATFORM = "cross-platform";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_RESIDENT_KEY = "requireResidentKey";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV = "userVerification";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV_REQUIRED = "required";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV_PREFERRED = "preferred";
    public static final String JSON_KEY_PREREG_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV_DISCOURAGED = "discouraged";

    public static final String JSON_KEY_PREAUTH_RESPONSE = "Response";
    public static final String JSON_KEY_PREAUTH_RESPONSE_CHALLENGE = "challenge";
    public static final String JSON_KEY_PREAUTH_RESPONSE_RELYING_PARTY_ID = "rpId";
    public static final String JSON_KEY_PREAUTH_RESPONSE_ALLOW_CREDENTIALS = "allowCredentials";
    public static final String JSON_KEY_PREAUTH_RESPONSE_PUBLIC_KEY_PARAMS_ALG = "alg";
    public static final int JSON_KEY_PREAUTH_RESPONSE_PUBLIC_KEY_ALG_ES256 = -7;
    public static final int JSON_KEY_PREAUTH_RESPONSE_PUBLIC_KEY_ALG_RS256 = -257;
    public static final String JSON_KEY_PREAUTH_RESPONSE_CREDENTIAL_ID = "id";
    public static final String JSON_KEY_PREAUTH_RESPONSE_CREDENTIAL_ID_LIST = "idList";
    public static final String JSON_KEY_PREAUTH_RESPONSE_TXID = "txid";
    public static final String JSON_KEY_PREAUTH_RESPONSE_TXPAYLOAD = "txpayload";

    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_SELECTION = "authenticatorSelection";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT = "authenticatorAttachment";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT_PLATFORM = "platform";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT_CROSS_PLATFORM = "cross-platform";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT_RESIDENT_KEY = "requireResidentKey";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV = "userVerification";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV_REQUIRED = "required";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV_PREFERRED = "preferred";
    public static final String JSON_KEY_PREAUTH_RESPONSE_AUTHENTICATOR_ATTACHMENT_UV_DISCOURAGED = "discouraged";

    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_PING_FIDO_SERVICE = "pingFidoService";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_GET_FIDO_REGISTRATION_CHALLENGE = "getFidoRegistrationChallenge";
//    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_REGISTER_FIDO_KEY = "registerAttestedDeviceFidoKey";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_REGISTER_FIDO_KEY = "registerFidoKey";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_GET_FIDO_AUTHENTICATION_CHALLENGE = "getFidoAuthenticationChallenge";
//    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_AUTHENTICATE_FIDO_KEY = "authenticateAttestedDeviceFidoKey";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_AUTHENTICATE_FIDO_KEY = "authenticateFidoKey";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_GET_FIDO_AUTHORIZATION_CHALLENGE = "getFidoAuthorizationChallenge";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_AUTHORIZE_FIDO_TRANSACTION = "authorizeFidoTransaction";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_DEREGISTER_FIDO_KEY = "deregisterFidoKey";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_GET_FIDO_KEYS_INFO = "getFidoKeysInfo";
    public static final String JSON_KEY_FIDO_SERVICE_OPERATION_UPDATE_FIDO_KEY_INFO = "updateFidoKeyInfo";

    public static final String JSON_KEY_SACL_SERVICE_OPERATION_CHECK_ATTESTATION_AUTHORIZATION = "checkForAttestationAuthorization";
    public static final String JSON_KEY_SACL_SERVICE_OPERATION_CHECK_USERNAME = "checkUsername";
    public static final String JSON_KEY_SACL_SERVICE_OPERATION_REGISTER_USER = "registerUser";
    public static final String JSON_KEY_SACL_SERVICE_OPERATION_PROPOSE_DEVICE = "proposeDevice";
    public static final String JSON_KEY_SACL_SERVICE_OPERATION_VALIDATE_ANDROID_KEYSTORE = "validateAndroidKeystore";

    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_ID = "pkcid";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_DID = "pkcDid";
    public static final String JSON_KEY_PUBLIC_KEY_CREDENTIAL_CREDENTIAL_ID = "pkcCredentialId";

    // SACL - Tasks
    public static enum SACL_TASKS {
        SACL_TASK_CHECK_FOR_AUTHORIZATION,
        SACL_TASK_CHECK_USERNAME,
        SACL_TASK_DELETE_KEY,
        SACL_TASK_DISPLAY_USERDATA,
        SACL_TASK_LIST_KEYS,
        SACL_TASK_PROPOSE_DEVICE,
        SACL_TASK_REGISTER_USER,
        SACL_TASK_SEND_ATTESTATION,
        SACL_TASK_SIGN;
    }

    // SACL - FIDO Services
    public static enum SACL_FIDO_SERVICES {
        SACL_FIDO_SERVICE_PING_FIDO_SERVICE,
        SACL_FIDO_SERVICE_GET_FIDO_REGISTRATION_CHALLENGE,
        SACL_FIDO_SERVICE_REGISTER_FIDO_KEY,
        SACL_FIDO_SERVICE_GET_FIDO_AUTHENTICATION_CHALLENGE,
        SACL_FIDO_SERVICE_AUTHENTICATE_FIDO_KEY,
        SACL_FIDO_SERVICE_GET_FIDO_AUTHORIZATION_CHALLENGE,
        SACL_FIDO_SERVICE_AUTHORIZE_FIDO_TRANSACTION,
        SACL_FIDO_SERVICE_DEREGISTER_FIDO_KEY,
        SACL_FIDO_SERVICE_GET_FIDO_KEYS_INFO,
        SACL_FIDO_SERVICE_UPDATE_FIDO_KEY_INFO;
    }

    // WEBAUTHN JSON Keys
    public static final String WEBAUTHN_CLIENT_DATA_OPERATION_TYPE_KEY = "type";
    public static final String WEBAUTHN_CLIENT_DATA_OPERATION_CREATE_VALUE = "webauthn.create";
    public static final String WEBAUTHN_CLIENT_DATA_OPERATION_GET_VALUE = "webauthn.get";
    public static final String WEBAUTHN_CLIENT_DATA_CHALLENGE_KEY = "challenge";
    public static final String WEBAUTHN_CLIENT_DATA_ORIGIN_KEY = "origin";
    public static final String WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_KEY = "tokenBinding";
    public static final String WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_KEY = "status";
    public static final String WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_PRESENT = "present";
    public static final String WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_SUPPORTED = "supported";
    public static final String WEBAUTHN_CLIENT_DATA_TOKEN_BINDING_STATUS_NOT_SUPPORTED = "not-supported";

    //    public static final String WEBAUTHN_STRONGKEY_ANDROIDKEYSTORE_AAGUID = "5341434C323032304b4F52D999D03ECB";
    public static final String WEBAUTHN_STRONGKEY_DEVP_AAGUID = "CAFEBABECAFEBEEF0123456789ABCDEF";

    /**
     * FIDO Extensions
     * https://www.w3.org/TR/2019/REC-webauthn-1-20190304/#sctn-defined-extensions
     */

    public static final String FIDO_EXTENSION_APPID = "appid";
    public static final String FIDO_EXTENSION_TXAUTH_SIMPLE = "txAuthSimple";
    public static final String FIDO_EXTENSION_TXAUTH_GENERIC = "txAuthGeneric";
    public static final String FIDO_EXTENSION_AUTHENTICATOR_SELECTION = "authnSel";
    public static final String FIDO_EXTENSION_SUPPORTED_EXTENSIONS = "exts";
    public static final String FIDO_EXTENSION_USER_VERIFICATION_INDEX = "uvi";
    public static final String FIDO_EXTENSION_LOCATION = "loc";
    public static final String FIDO_EXTENSION_USER_VERIFICATION_METHOD = "uvm";
    public static final String FIDO_EXTENSION_BIOMETRIC_PERFBOUNDS = "biometricPerfBounds";

    /**
     * User is always verified for AndroidKeystore use; thus "uvm" extension is always included
     * https://fidoalliance.org/specs/fido-v2.0-id-20180227/fido-registry-v2.0-id-20180227.html#user-verification-methods
     */

    public static final Boolean USER_PRESENT = true;
    public static final Boolean USER_NOT_PRESENT = false;
    public static final Boolean USER_VERIFIED = true;
    public static final Boolean USER_NOT_VERIFIED = false;
    public static final Boolean ATTESTED_DATA_INCLUDED = true;
    public static final Boolean ATTESTED_DATA_NOT_INCLUDED = false;
    public static final Boolean EXTENSION_DATA_INCLUDED = true;
    public static final Boolean EXTENSION_DATA_NOT_INCLUDED = false;

    public static final int FIDO_USER_VERIFY_PRESENCE = 1;
    public static final int FIDO_USER_VERIFY_FINGERPRINT = 2;
    public static final int FIDO_USER_VERIFY_PASSCODE = 4;
    public static final int FIDO_USER_VERIFY_VOICEPRINT = 8;
    public static final int FIDO_USER_VERIFY_FACEPRINT = 16;
    public static final int FIDO_USER_VERIFY_LOCATION = 32;
    public static final int FIDO_USER_VERIFY_EYEPRINT = 64;
    public static final int FIDO_USER_VERIFY_PATTERN = 128;
    public static final int FIDO_USER_VERIFY_HANDPRINT = 256;
    public static final int FIDO_USER_VERIFY_NONE = 512;
    public static final int FIDO_USER_VERIFY_ALL = 1024;

    public static final int FIDO_KEY_PROTECTION_SOFTWARE = 1;
    public static final int FIDO_KEY_PROTECTION_HARDWARE = 2;
    public static final int FIDO_KEY_PROTECTION_TEE = 4;
    public static final int FIDO_KEY_PROTECTION_SECURE_ELEMENT = 8;
    public static final int FIDO_KEY_PROTECTION_REMOTE_HANDLE = 16;

    public static final int FIDO_MATCHER_PROTECTION_SOFTWARE = 1;
    public static final int FIDO_MATCHER_PROTECTION_TEE = 2;
    public static final int FIDO_MATCHER_PROTECTION_ON_CHIP = 4;

    public static final Boolean[] ANDROID_KEYSTORE_DEFAULT_REGISTRATION_FLAGS =
            new Boolean[] {Constants.USER_NOT_PRESENT,
                    Constants.USER_VERIFIED,
                    Constants.ATTESTED_DATA_INCLUDED,
                    Constants.EXTENSION_DATA_INCLUDED} ;

    public static final Boolean[] ANDROID_KEYSTORE_DEFAULT_AUTHENTICATION_FLAGS =
            new Boolean[] {Constants.USER_NOT_PRESENT,
                    Constants.USER_VERIFIED,
                    Constants.ATTESTED_DATA_NOT_INCLUDED,
                    Constants.EXTENSION_DATA_INCLUDED};

    public static final Boolean[] ANDROID_KEYSTORE_DEFAULT_AUTHORIZATION_FLAGS =
            new Boolean[] {Constants.USER_PRESENT,
                    Constants.USER_VERIFIED,
                    Constants.ATTESTED_DATA_NOT_INCLUDED,
                    Constants.EXTENSION_DATA_INCLUDED};

    public static final String ANDROID_KEYSTORE_ASSERTION_LABEL_FIDO = "FidoAndroidKeystoreAssertion";
    public static final String ANDROID_KEYSTORE_ASSERTION_LABEL_AUTHENTICATOR_DATA = "authenticatorData";
    public static final String ANDROID_KEYSTORE_ASSERTION_LABEL_SIGNATURE = "signature";

    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO = "FidoAndroidKeystoreAttestation";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO_JSON_FORMAT = "JsonFormat";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_FIDO_CBOR_FORMAT = "CborFormat";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_AUTHENTICATOR_DATA = "authData";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_FORMAT = "fmt";
    public static final String ANDROID_KEYSTORE_ATTESTATION_VALUE_FORMAT = "android-key";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_STATEMENT = "attStmt";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_ALGORITHM = "alg";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_SIGNATURE = "sig";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_X509_CERTIFICATE_CHAIN = "x5c";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_CREDENTIAL_CERTIFICATE = "credCert";
    public static final String ANDROID_KEYSTORE_ATTESTATION_LABEL_CA_CERTIFICATE = "caCert";

    // FIDO related miscellaneous constants
    public static final int FIDO_COUNTER_ZERO = 0;
    public static final int FIDO_COUNTER_ONE = 1;

    public enum ENCODING {
        BASE64,
        HEX
    }

    public enum FIDO_OPERATION {
        CREATE,
        GET
    }

    // JSON Web Signature related constants
    public static final String JWS_ALGORITHM_EC = "EC";
    public static final String JWS_ALGORITHM_ES256 = "ES256";
    public static final String JWS_ALGORITHM_RSA = "RSA";
    public static final String JWS_ALGORITHM_RS256 = "RS256";

    public static final String JWS_ATTRIBUTE_ALG = "alg";
    public static final String JWS_ATTRIBUTE_PAYLOAD = "payload";
    public static final String JWS_ATTRIBUTE_PROTECTED = "protected";
    public static final String JWS_ATTRIBUTE_SIGNATURE = "signature";
    public static final String JWS_ATTRIBUTE_X5C = "x5c";

    public static final String JWS_RESPONSE_ATTRIBUTE_DN = "dn";
    public static final String JWS_RESPONSE_ATTRIBUTE_END = "end";
    public static final String JWS_RESPONSE_ATTRIBUTE_SERIAL = "serial";
    public static final String JWS_RESPONSE_ATTRIBUTE_START = "start";
    public static final String JWS_RESPONSE_ATTRIBUTE_VERIFIED = "verified";

    // MDKMS Policy Elements
    public static final int KEY_PROPERTIES_AUTH_DEVICE_CREDENTIAL = 1;
    public static final int KEY_PROPERTIES_AUTH_BIOMETRIC_STRONG = 2;
    public static final String SIGNING_KEY_POLICY_ALGORITHM = "EC";
    public static final String SIGNING_KEY_POLICY_ALIAS = "UNDEFINED";
    public static final String SIGNING_KEY_POLICY_ATTESTATION_CHALLENGE = "UNDEFINED";
    public static final String SIGNING_KEY_POLICY_CERTIFICATE_NOT_AFTER = "UNDEFINED";
    public static final String SIGNING_KEY_POLICY_CERTIFICATE_NOT_BEFORE = "UNDEFINED";
    public static final String SIGNING_KEY_POLICY_CERTIFICATE_SERIAL_NUMBER = "RANDOM";
    public static final String SIGNING_KEY_POLICY_CERTIFICATE_SUBJECT_DN = "UNDEFINED";
    public static final boolean SIGNING_KEY_POLICY_INVALIDATE_BY_BIOMETRIC_ENROLLMENT = true;
    public static final int SIGNING_KEY_POLICY_KEY_SIZE = 256;
    public static final String SIGNING_KEY_POLICY_MESSAGE_DIGEST = "SHA256";
    public static final String SIGNING_KEY_POLICY_PADDING_SCHEME = "PKCS1";
    public static final boolean SIGNING_KEY_POLICY_STRONGBOX_BACKED = true;
    public static final boolean SIGNING_KEY_POLICY_USER_AUTHENTICATION_REQUIRED = true;
    public static final int SIGNING_KEY_POLICY_USER_AUTHENTICATION_VALIDITY_DURATION = -1;
    public static final int SIGNING_KEY_POLICY_USER_AUTHENTICATION_PARAMETER_TIMEOUT = 0;
    public static final int SIGNING_KEY_POLICY_USER_AUTHENTICATION_PARAMETER_TYPE = KEY_PROPERTIES_AUTH_BIOMETRIC_STRONG;
    public static final int SIGNING_KEY_POLICY_USER_CONFIRMATION_REQUIRED = 0;
    public static final String SIGNING_KEY_POLICY_VALIDITY_START_DATE = "UNDEFINED";
    public static final String SIGNING_KEY_POLICY_VALIDITY_CONSUMPTION_END_DATE = "UNDEFINED";
    public static final String SIGNING_KEY_POLICY_VALIDITY_ORIGINATION_END_DATE = "UNDEFINED";

}
