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
 * SfaConstants used within the app
 */

package com.strongkey.sfaeco.utilities;

public class SfaConstants {

    /***********************
              .d888
             d88P"
             888
    .d8888b  888888  8888b.
    88K      888        "88b
    "Y8888b. 888    .d888888
         X88 888    888  888
     88888P' 888    "Y888888
    ************************/

    // SFA Generic Keys
    public static final String JSON_KEY_ID = "id";
    public static final String JSON_KEY_DID = "did";
    public static final String JSON_KEY_SID = "sid";
    public static final String JSON_KEY_TXID = "txid";
    public static final String JSON_KEY_UID = "uid";

    // SFA - USER Json Object Keys
    public static final String JSON_KEY_USER = "User";
    public static final String JSON_KEY_USER_USERNAME = "username";
    public static final String JSON_KEY_USER_PASSWORD = "password";
    public static final String JSON_KEY_USER_CREDENTIALID = "credentialid";
    public static final String JSON_KEY_USER_GIVEN_NAME = "givenName";
    public static final String JSON_KEY_USER_FAMILY_NAME = "familyName";
    public static final String JSON_KEY_USER_EMAIL_ADDRESS = "email";
    public static final String JSON_KEY_USER_MOBILE_NUMBER = "userMobileNumber";
    public static final String JSON_KEY_USER_STATUS = "status";
    public static final String JSON_KEY_USER_CREATE_DATE = "createDate";
    public static final String JSON_KEY_USER_ENROLLMENT_DATE = "enrollmentDate";

    // SFA - Tasks
    public static enum SFA_TASKS {
        SFA_TASK_AUTHENTICATE_FIDO_KEY,
        SFA_TASK_AUTHORIZE_FIDO_TRANSACTION,
        SFA_TASK_CHECK_USERNAME,
        SFA_TASK_DELETE_KEY,
        SFA_TASK_DISPLAY_USERDATA,
        SFA_TASK_GET_AUTHORIZATION_CHALLENGE,
        SFA_TASK_LIST_KEYS,
        SFA_TASK_REGISTER_FIDO_KEY,
        SFA_TASK_REGISTER_USER,
        SFA_TASK_RETRIEVE_USER,
        SFA_TASK_SIGN;
    }

    public static final String SFA_TASK = "SFA_TASK";
    public static final String SFA_TASK_CHECK_FOR_AUTHORIZATION_RESPONSE_JSON = "SFA_TASK_CHECK_FOR_AUTHORIZATION_RESPONSE_JSON";
    public static final String SFA_TASK_CHECK_USERNAME_RESPONSE_JSON = "SFA_TASK_CHECK_USERNAME_RESPONSE_JSON";
    public static final String SFA_TASK_DELETE_KEY_RESPONSE_JSON = "SFA_TASK_DELETE_KEY_RESPONSE_JSON";
    public static final String SFA_TASK_DISPLAY_USERDATA_RESPONSE_JSON = "SFA_TASK_DISPLAY_USERDATA_RESPONSE_JSON";
    public static final String SFA_TASK_LIST_KEYS_RESPONSE_JSON = "SFA_TASK_LIST_KEYS_RESPONSE_JSON";
    public static final String SFA_TASK_REGISTER_USER_RESPONSE_JSON = "SFA_TASK_REGISTER_USER_RESPONSE_JSON";
    public static final String SFA_TASK_RETRIEVE_USER_RESPONSE_JSON = "SFA_TASK_RETRIEVE_USER_RESPONSE_JSON";
    public static final String SFA_TASK_SIGN_RESPONSE_JSON = "SFA_TASK_SIGN_RESPONSE_JSON";

    public static final String SFA_TASK_AUTHENTICATE_FIDO_KEY_RESPONSE_OBJECT = "SFA_TASK_AUTHENTICATE_FIDO_KEY_RESPONSE_OBJECT";
    public static final String SFA_TASK_AUTHORIZE_FIDO_TRANSACTION_RESPONSE_OBJECT = "SFA_TASK_AUTHORIZE_FIDO_TRANSACTION_RESPONSE_OBJECT";
    public static final String SFA_TASK_GET_AUTHORIZATION_CHALLENGE_RESPONSE_OBJECT = "SFA_TASK_GET_AUTHORIZATION_CHALLENGE_RESPONSE_OBJECT";
    public static final String SFA_TASK_REGISTER_FIDO_KEY_RESPONSE_OBJECT = "SFA_TASK_REGISTER_FIDO_KEY_RESPONSE_OBJECT";
    public static final String SFA_TASK_RETRIEVE_USER_RESPONSE_OBJECT = "SFA_TASK_RETRIEVE_USER_RESPONSE_OBJECT";

    final public static int    FIDO2_USER_AUTHENTICATION_ALWAYS = -1;

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

    public static final String SFA_ECO_PAYMENT_TX_FIDO_REFERENCE_LABEL = "FIDOAuthenticatorReferences";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_PROTOCOL = "protocol";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_PROTOCOL_LABEL = "FIDO Protocol";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_RPID = "rpId";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_RPID_LABEL = "RPID";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_AUTHTIME = "authTime";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_AUTHORIZATION_TIME_LABEL = "Authorization Time";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_UP = "up";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_UP_LABEL = "User Present";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_UV = "uv";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_UV_LABEL = "User Verified";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_USED_FOR_THIS_TRANSACTION = "usedForThisTransaction";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_USED_FOR_THIS_TRANSACTION_LABEL = "Used for this transaction";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_ID = "id";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_ID_LABEL = "ID";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_RAWID = "rawId";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_RAWID_LABEL = "Raw ID";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_USER_HANDLE = "userHandle";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_USER_HANDLE_LABEL = "User Handle";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_AUTHENTICATOR_DATA = "authenticatorData";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_AUTHENTICATOR_DATA_LABEL = "Authenticator Data";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_CLIENT_DATA_JSON = "clientDataJSON";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_CLIENT_DATA_JSON_LABEL = "Client Data Json";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_AAGUID = "aaguid";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_AAGUID_LABEL = "AAGUID";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_USED_FOR_THIS_TX = "usedForThisTransaction";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_PUBLIC_KEY = "publicKey";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_SIGNATURE = "signature";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_SIGNATURE_OBJECT = "CurrentSignatureObject";
    public static final String SFA_ECO_PAYMENT_TX_FIDO_SIGNATURE_OBJECT_LABEL = "SignatureObject";


    public enum STRONGKEY_PRODUCTS {
        T100,
        E1000,
        FidoCloud,
        TellaroCloud
    }

    public enum TRANSACTION_CURRENCY {
        AUD,
        EURO,
        INR,
        SGD,
        USD

    }

    public enum PaymentBrand {
        Amex,
        Discover,
        JCB,
        Mastercard,
        Visa,
        SEPA,
        Other
    }
}
