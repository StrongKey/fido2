/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 *
 */
package com.strongauth.fido.u2f.clientsimulator;

/**
 *
 * @author smehta
 */
public class CSConstants {


    // OTHER CONSTANTS AFFECTED
    public static final String JSON_KEY_APP_ID = "appId";
    public static final String JSON_KEY_BROWSERDATA = "browserData";
    public static final String JSON_KEY_REGISTRATIONDATA = "enrollData";
    public static final String JSON_USER_KEY_HANDLE = "keyHandle";
    public static final String JSON_KEY_SIGNATURE = "signData";

    public static final int MAX_RANDOM_NUMBER_SIZE_BITS = 1024;

    //registration
    // CSConstants for the secure element
    public static final String SECURE_ELEMENT_SECRET_KEY = "0123456789ABCDEF0123456789ABCDEF";
    public static final String ATTESTATION_KEYSTORE = "test/resources/attestation.jceks";
    public static final String ATTESTATION_PRIVATE_KEYALIAS = "mykey";
    public static final String ATTESTATION_KEYSTORE_TOUCH_PASSWORD = "changeit";
    //keys
    public static final String JSON_PROPERTY_REQUEST_TYPE = "typ";
    public static final String JSON_PROPERTY_SERVER_CHALLENGE_BASE64 = "challenge";
    public static final String JSON_PROPERTY_SERVER_ORIGIN = "origin";
    public static final String JSON_PROPERTY_CHANNEL_ID = "cid_pubkey";
    public static final String JSON_KEY_SESSIONID = "sessionId";
    public static final String JSON_KEY_CHALLENGE = "challenge";
    public static final String JSON_KEY_VERSION = "version";
    //values

    public static final String REGISTER_CLIENT_DATA_OPTYPE = "navigator.id.finishEnrollment";
    public static final String REGISTER_CLIENT_DATA_CHANNELID = "not implemented yet";
    public static final String ORIGIN = "http://localhost:8081";
    //control
    public static int EC_P256_PUBLICKKEYSIZE = 65;

//authentication
    public static final int CHALLENGE_PARAMETER_LENGTH = 32;
    public static final int APPLICATION_PARAMETER_LENGTH = 32;

    //control
    public static final byte CONTROL_BYTE = 0x03;
    public static final byte AUTHENTICATOR_USERPRESENCE_BYTE = 0X01;

    //Keys


    //values
    public static final String AUTHENTICATE_CLIENT_DATA_OPTYPE = "navigator.id.getAssertion";
    public static final String AUTHENTICATE_CLIENT_DATA_CHANNELID = "NOT IMPLEMENTED YET";
    public static final int AUTHENTICATOR_COUNTER_VALUE = 1;
}
