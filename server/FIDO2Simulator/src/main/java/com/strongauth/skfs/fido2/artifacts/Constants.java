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

package com.strongauth.skfs.fido2.artifacts;

public class Constants
{
    // Constants related to FIDO2
    public static final Boolean EXTENSION_DATA_INCLUDED = false;
    public static final Boolean ATTESTED_DATA_INCLUDED = true;
    public static final Boolean USER_VERIFIED = true;
    public static final Boolean USER_PRESENT = false;

    public static final int FIDO_REGISTER_COUNTER = 0;
    public static final Boolean[] REGISTRATION_FLAGS = new Boolean[] {Constants.USER_PRESENT, Constants.USER_VERIFIED, Constants.ATTESTED_DATA_INCLUDED, Constants.EXTENSION_DATA_INCLUDED} ;
    public static final Boolean[] AUTHN_FLAGS = new Boolean[] {Constants.USER_PRESENT, Constants.USER_VERIFIED, Boolean.FALSE, Constants.EXTENSION_DATA_INCLUDED} ;
//    public static final String AAGUID = "7a98c250-6808-11cf-b73b-00aa00b677a7";
    public static final String AAGUID = "3b1adb99-0dfe-46fd-90b8-7f7614a4de2a";

    // Constants related to the Attestation Key
    public static final String ATTESTATION_KEYSTORE_FILE = "/resources/attestation.jceks";
    public static final String ATTESTATION_KEYSTORE_PASSWORD = "changeit";
    public static final String ATTESTATION_KEYSTORE_PVK_ALIAS = "mykey";
    public static final String ATTESTATION_KEYSTORE_FIDO2PVK_ALIAS = "fido2key";

    //ATTESTATION FIDO2 Private Key
    public static final String ATTESTATION_FIDO2PVK_BASE64 = "MEECAQAwEwYHKoZIzj0CAQYIKoZIzj0DAQcEJzAlAgEBBCB9CnWdVADQS3FF6Y8oZH43LnxlA8l5VTRQnXYbsWA1_g==";


    //ATTESTATION CERT
    public static final String ATTESTATION_FILE_BASE64 = "MIIB4DCCAYOgAwIBAgIEbCtY8jAMBggqhkjOPQQDAgUAMGQxCzAJBgNVBAYTAlVTMRcwFQYDVQQKEw5TdHJvbmdBdXRoIEluYz"
            + "EiMCAGA1UECxMZQXV0aGVudGljYXRvciBBdHRlc3RhdGlvbjEYMBYGA1UEAwwPQXR0ZXN0YXRpb25fS2V5MB4XDTE5MDcxODE3MTEyN1oXDTI5MDcxNTE3MTEyN1owZDELMAkGA1UEBhM"
            + "CVVMxFzAVBgNVBAoTDlN0cm9uZ0F1dGggSW5jMSIwIAYDVQQLExlBdXRoZW50aWNhdG9yIEF0dGVzdGF0aW9uMRgwFgYDVQQDDA9BdHRlc3RhdGlvbl9LZXkwWTATBgcqhkjOPQIBBggq"
            + "hkjOPQMBBwNCAAQx9IY-uvfEvZ9HaJX3yaYmOqSIYQxS3Oi3Ed7iw4zXGR5C4RaKyOQeIu1hK2QCgoq210KjwNFU3TpsqAMZLZmFoyEwHzAdBgNVHQ4EFgQUNELQ4HBDjTWzj9E0Z719E"
            + "4EeLxgwDAYIKoZIzj0EAwIFAANJADBGAiEA7RbR2NCtyMQwiyGGOADy8rDHjNFPlZG8Ip9kr9iAKisCIQCi3cNAFjTL03-sk7C1lij7JQ6mO7rhfdDMfDXSjegwuQ==";

    // Constants related to JSON keys for decoding
    public static final int JSON_KEY_APPID = 0;
    public static final int JSON_KEY_CHALLENGE = 1;
    public static final int JSON_KEY_CHANNELID = 2;
    public static final int JSON_KEY_CLIENTDATA = 3;
    public static final int JSON_KEY_KEYHANDLE = 4;
    public static final int JSON_KEY_REGISTRATIONDATA = 5;
    public static final int JSON_KEY_REQUEST_TYPE = 6;
    public static final int JSON_KEY_SERVER_CHALLENGE = 7;
    public static final int JSON_KEY_SERVER_ORIGIN = 8;
    public static final int JSON_KEY_SESSIONID = 9;
    public static final int JSON_KEY_SIGNATURE = 10;
    public static final int JSON_KEY_VERSION = 11;
    public static final int JSON_KEY_RP = 12;

    // Constants related to JSON keys of the key-value pairs
    public static final String JSON_KEY_APPID_LABEL = "appId";
    public static final String JSON_KEY_CHALLENGE_LABEL = "challenge";
    public static final String JSON_KEY_CHANNELID_LABEL = "cid_pubkey";
    public static final String JSON_KEY_CLIENTDATA_LABEL = "clientData";
    public static final String JSON_KEY_KEYHANDLE_LABEL = "keyHandle";
    public static final String JSON_KEY_REGISTRATIONDATA_LABEL = "registrationData";
    public static final String JSON_KEY_REQUEST_TYPE_LABEL = "typ";
    public static final String JSON_KEY_SERVER_CHALLENGE_LABEL = "challenge";
    public static final String JSON_KEY_SERVER_ORIGIN_LABEL = "origin";
    public static final String JSON_KEY_SESSIONID_LABEL = "sessionId";
    public static final String JSON_KEY_SIGNATURE_LABEL = "signatureData";
    public static final String JSON_KEY_VERSION_LABEL = "version";
    public static final String JSON_KEY_RP_LABEL = "rp";
    public static final String JSON_KEY_RPID_LABEL = "rpId";

    // Constants related to FIDO Client (Chrome browser for now)
    public static final String REGISTER_CLIENT_BAD_APPID = "TESTBADSIGNATUREWITHINVALIDAPPID";
    public static final String REGISTER_CLIENT_DATA_CHANNELID = "NOT IMPLEMENTED YET";
    public static final String REGISTER_CLIENT_DATA_OPTYPE = "navigator.id.finishEnrollment";
    public static final String AUTHENTICATE_CLIENT_DATA_CHANNELID = "NOT IMPLEMENTED YET";
    public static final String AUTHENTICATE_CLIENT_DATA_OPTYPE = "navigator.id.getAssertion";

    // Constants related to cryptography
    public static final String EC_P256_CURVE = "secp256r1";
    public static final String FIXED_AES256_WRAPPING_KEY = "0123456789ABCDEF0123456789ABCDEF";

    // Constants related to Authenticator
    public static final byte AUTHENTICATOR_CONTROL_BYTE = 0x03;
    public static final byte AUTHENTICATOR_USERPRESENCE_BYTE = 0X01;

    // Constants related to sizes
    public static final int APPLICATION_PARAMETER_LENGTH = 32;
    public static final int AUTHENTICATOR_COUNTER_LENGTH = 4;
    public static final int AUTHENTICATOR_KEY_HANDLE_LENGTH = 1;
    public static final int CHALLENGE_PARAMETER_LENGTH = 32;
    public static final int ECDSA_P256_PUBLICKEY_LENGTH = 65;
    public static final int ENCRYPTION_MODE_CBC_IV_LENGTH = 16;
}
