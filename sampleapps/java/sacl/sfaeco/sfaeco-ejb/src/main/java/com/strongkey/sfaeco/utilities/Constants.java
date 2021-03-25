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

package com.strongkey.sfaeco.utilities;


public class Constants {

    public static final int MIN_ENTROPY_SIZE = 16;
    public static final int MAX_ENTROPY_SIZE = 256;
    public static final String EXECUTE_METHODNAME = "execute";
    public static final String VERIFIED_LABEL = "verified";
    
    /***************************************************
                                                d8b          
                                                Y8P          

     .d88b.   .d88b.  88888b.   .d88b.  888d888 888  .d8888b 
    d88P"88b d8P  Y8b 888 "88b d8P  Y8b 888P"   888 d88P"    
    888  888 88888888 888  888 88888888 888     888 888      
    Y88b 888 Y8b.     888  888 Y8b.     888     888 Y88b.    
     "Y88888  "Y8888  888  888  "Y8888  888     888  "Y8888P 
         888                                                 
    Y8b d88P                                                 
     "Y88P"                                                  
     ****************************************************/
    
    // Generic Json Object Keys
    public static final String JSON_KEY_DID = "did";
    public static final String JSON_KEY_SID = "sid";
    public static final String JSON_KEY_UID = "uid";
    public static final String JSON_KEY_TXID = "txid";
    public static final String JSON_KEY_UTXID = "utxid";
    public static final String JSON_KEY_FARID = "farid";
    public static final String JSON_KEY_RDID = "rdid";
    public static final String JSON_KEY_DEVID = "devid";
    public static final String JSON_KEY_AUTHORIZATION_ID = "azid";
    public static final String JSON_KEY_USERNAME = "username";
    public static final String JSON_KEY_PASSWORD = "password";
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
    public static final String JSON_KEY_SACL_FIDO_TRANSACTION_PAYLOAD = "payload";
    
    // FIDO2 Service Information Keys
    public static final String JSON_KEY_FIDO_SERVICE_INFO = "svcinfo";
    public static final String JSON_KEY_SERVICE_INFO_DID = "did";
    public static final String JSON_KEY_SERVICE_INFO_PROTOCOL = "protocol";
    public static final String JSON_KEY_SERVICE_INFO_PROTOCOL_FIDO2 = "FIDO2_0";
    public static final String JSON_KEY_SERVICE_INFO_AUTHTYPE = "authtype";
    public static final String JSON_KEY_SERVICE_INFO_AUTHTYPE_HMAC = "HMAC";
    public static final String JSON_KEY_SERVICE_INFO_AUTHTYPE_PASSWORD = "PASSWORD";
    public static final String JSON_KEY_SERVICE_INFO_USERNAME = "svcusername";
    public static final String JSON_KEY_SERVICE_INFO_PASSWORD = "svcpassword";
    
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
    
    // SFA eCommerce related constants
    public static final String SFA_ECO_LABEL = "SFAECO";
    public static final String SFA_ECO_STRONGKEY_LABEL = "StrongKey";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_LABEL = "PaymentTransaction";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_AMOUNT = "amount";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CART = "cart";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CURRENCY = "currency";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CURRENCY_AUD = "AUD";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CURRENCY_EURO = "EURO";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CURRENCY_INR = "INR";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CURRENCY_SGD = "SGD";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CURRENCY_USD = "USD";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_MERCHANT_LABEL = "merchant";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_PAYMENT_INSTRUMENT = "paymentInstrument";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_PAYMENT_INSTRUMENT_NUMBER = "piNumber";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_TXID = "txid";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_TXDATE = "txdate";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_TXPAYLOAD = "txpayload";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_TXPAYLOAD_LABEL = "TX Payload";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_NONCE = "nonce";
    public static final String SFA_ECO_PAYMENT_TRANSACTION_CHALLENGE = "challenge";
    
    // SFA eCommerce related constants
    public static final String SFA_ECO_CART_LABEL = "cart";
    public static final String SFA_ECO_CART_CURRENCY_LABEL = "currency";
    public static final String SFA_ECO_CART_MERCHANT_ID_LABEL = "merchantId";
    public static final String SFA_ECO_CART_MERCHANT_NAME_LABEL = "merchantName";
    public static final String SFA_ECO_CART_PRODUCT_ID_LABEL = "id";
    public static final String SFA_ECO_CART_PRODUCT_NAME_LABEL = "name";
    public static final String SFA_ECO_CART_PRODUCT_PRICE_LABEL = "price";
    public static final String SFA_ECO_CART_PRODUCTS_LABEL = "products";
    public static final String SFA_ECO_CART_TOTAL_PRODUCTS_LABEL = "totalProducts";
    public static final String SFA_ECO_CART_TOTAL_PRICE_LABEL = "totalPrice";
    public static final String SFA_ECO_CART_PAYMENT_METHOD_LABEL = "paymentMethod";
    public static final String SFA_ECO_CART_PAYMENT_METHOD_BRAND_LABEL = "brand";
    public static final String SFA_ECO_CART_PAYMENT_METHOD_NUMBER_LABEL = "number";
    public static final String SFA_ECO_CART_PAYMENT_METHOD_CARD_BRAND_LABEL = "cardBrand";
    public static final String SFA_ECO_CART_PAYMENT_METHOD_CARD_LAST4_LABEL = "cardLast4";

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
    
    // SFA FIDO_AUTHENTICATOR_REFERENCES Json Object Keys
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
    
    // Value Strings
    public static final String STATUS_CANCELED = "Canceled";
    public static final String STATUS_FAILED = "Failed";
    public static final String STATUS_INFLIGHT = "Inflight";
    public static final String STATUS_OTHER = "Other";
    public static final String STATUS_SUCCEEDED = "Succeeded";
    public static final String STATUS_UNAUTHORIZED = "Unauthorized";
    
    /***************************************************
                       888 888               
                       888 888               
                       888 888               
    88888b.d88b.   .d88888 88888b.   8888b.  
    888 "888 "88b d88" 888 888 "88b     "88b 
    888  888  888 888  888 888  888 .d888888 
    888  888  888 Y88b 888 888 d88P 888  888 
    888  888  888  "Y88888 88888P"  "Y888888 
     ****************************************************/
    
    // SACL - USER Json Object Keys
    public static final String JSON_KEY_USER = "User";
    public static final String JSON_KEY_USER_USERNAME = "username";
    public static final String JSON_KEY_USER_PASSWORD = "password";
    public static final String JSON_KEY_USER_CREDENTIALID = "credentialid";
    public static final String JSON_KEY_USER_GIVEN_NAME = "givenName";
    public static final String JSON_KEY_USER_FAMILY_NAME = "familyName";
    public static final String JSON_KEY_USER_EMAIL_ADDRESS = "email";
    public static final String JSON_KEY_USER_MOBILE_NUMBER = "userMobileNumber";
    public static final String JSON_KEY_USER_ENROLLMENT_DATE = "enrollmentDate";
    
    // SACL - USER_TRANSACTIONS Json Object Keys
    public static final String JSON_KEY_USER_TRANSACTION = "UserTransaction";
    
    // Value Strings
    public static final String STATUS_AUTHORIZED = "Authorized";
    public static final String STATUS_REGISTERED = "Registered";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";
    public static final String STATUS_REVOKED = "Revoked";
    
    public static final String SIGNATURE_FORMAT_DSIG = "DSIG";
    public static final String SIGNATURE_FORMAT_JWS = "JWS";
    public static final String SIGNATURE_FORMAT_PKCS7 = "PKCS7";
    public static final String SIGNATURE_FORMAT_HMAC = "HMAC";
    public static final String SIGNATURE_FORMAT_OTHER = "Other";


    // Enumerations
    public static enum FIDO_SERVICE {
        PREREGISTER,
        REGISTER,
        PREAUTHENTICATE,
        AUTHENTICATE,
        PREAUTHORIZE,
        AUTHORIZE,
        DEREGISTER,
        GETKEYSINFO,
        UPDATEKEYINFO,
        PING
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
    
    public static enum ENCODING {
        BASE64,
        HEX
    }
    
    public static enum PaymentBrand {
        Amex,
        Discover,
        JCB,
        Mastercard,
        Visa,
        SEPA,
        Other
    }
}
