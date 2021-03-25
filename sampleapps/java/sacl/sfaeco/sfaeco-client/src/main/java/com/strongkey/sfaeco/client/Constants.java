/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * Constants used within this application
 */

package com.strongkey.sfaeco.client;


public class Constants {

    public static final int MIN_ENTROPY_SIZE = 16;
    public static final int MAX_ENTROPY_SIZE = 256;
    public static final String EXECUTE_METHODNAME = "execute";
    
    // Generic Json Object Keys
    public static final String JSON_KEY_DID = "did";
    public static final String JSON_KEY_SID = "sid";
    public static final String JSON_KEY_UID = "uid";
    public static final String JSON_KEY_RDID = "rdid";
    public static final String JSON_KEY_DEVID = "devid";
    public static final String JSON_KEY_AUTHORIZATION_ID = "azid";
    public static final String JSON_KEY_SERVICE_CREDENTIAL = "svccred";
    public static final String JSON_KEY_SERVICE_CREDENTIAL_PASSWORD = "svcpassword";
    public static final String JSON_KEY_TRANSACTION_ID = "utxid";
    public static final String JSON_KEY_OBJECT = "object";
    public static final String JSON_KEY_REQUIRE_SAFETYNET = "requiresnet";
    public static final String JSON_KEY_NEW_USER_REGISTRATION = "newuser";
    public static final String JSON_KEY_NEW_DEVICE_REGISTRATION = "newdevice";
    
    public static final String JSON_KEY_STATUS = "status";
    public static final String JSON_KEY_CREATE_DATE = "createDate";
    public static final String JSON_KEY_MODIFY_DATE = "modifyDate";
    public static final String JSON_KEY_NOTES = "notes";
    public static final String JSON_KEY_SIGNATURE = "signature";
    public static final String JSON_KEY_SIGNATURE_VERIFIER_TOKEN = "signatureVerifierToken";
    public static final String JSON_KEY_SIGNATURE_FORMAT = "signatureFormat";
    
    // SFA - USER Json Object Keys
    public static final String JSON_KEY_USER = "User";
    public static final String JSON_KEY_USER_USERNAME = "username";
    public static final String JSON_KEY_USER_PASSWORD = "password";
    public static final String JSON_KEY_USER_CREDENTIALID = "credentialid";
    public static final String JSON_KEY_USER_GIVEN_NAME = "givenName";
    public static final String JSON_KEY_USER_FAMILY_NAME = "familyName";
    public static final String JSON_KEY_USER_EMAIL_ADDRESS = "email";
    public static final String JSON_KEY_USER_MOBILE_NUMBER = "userMobileNumber";
    public static final String JSON_KEY_USER_ENROLLMENT_DATE = "enrollmentDate";
    
    // SFA - USER_TRANSACTIONS Json Object Keys
    public static final String JSON_KEY_USER_TRANSACTION = "UserTransaction";
    
    // FIDO Service Json Object Keys
    public static final String JSON_KEY_FIDO_SERVICE_INPUT_PARAMS = "fidoServiceInputParams";
    
    public static final String JSON_KEY_FIDO_SVCINFO = "svcinfo";
    public static final String JSON_KEY_FIDO_SVCINFO_PROTOCOL = "protocol";
    public static final String JSON_KEY_FIDO_SVCINFO_PROTOCOL_FIDO20 = "FIDO2_0";
    public static final String JSON_KEY_FIDO_SVCINFO_AUTHTYPE = "authtype";
    public static final String JSON_KEY_FIDO_SVCINFO_AUTHTYPE_PASSWORD = "PASSWORD";
    public static final String JSON_KEY_FIDO_SVCINFO_SVCUSERNAME = "svcusername";
    public static final String JSON_KEY_FIDO_SVCINFO_SVCPASSWORD = "svcpassword";
    
    public static final String JSON_KEY_FIDO_PAYLOAD = "payload";
    public static final String JSON_KEY_FIDO_PAYLOAD_USERNAME = "username";
    public static final String JSON_KEY_FIDO_PAYLOAD_DISPLAYNAME = "displayname";
    public static final String JSON_KEY_FIDO_PAYLOAD_OPTIONS = "options";
    public static final String JSON_KEY_FIDO_PAYLOAD_EXTENSIONS = "extensions";
    
    // Value Strings
    public static final String STATUS_AUTHORIZED = "Authorized";
    public static final String STATUS_REGISTERED = "Registered";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_OTHER = "Other";
    public static final String STATUS_SUCCEEDED = "Succeeded";
    public static final String STATUS_CANCELED = "Canceled";
    public static final String STATUS_REVOKED = "Revoked";
    
    public static final String SIGNATURE_FORMAT_DSIG = "DSIG";
    public static final String SIGNATURE_FORMAT_JWS = "JWS";
    public static final String SIGNATURE_FORMAT_PKCS7 = "PKCS7";
    public static final String SIGNATURE_FORMAT_HMAC = "HMAC";
    public static final String SIGNATURE_FORMAT_OTHER = "Other";
    
    public static enum ENCODING {
        BASE64,
        HEX
    }

    // Android OS Details
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

}
