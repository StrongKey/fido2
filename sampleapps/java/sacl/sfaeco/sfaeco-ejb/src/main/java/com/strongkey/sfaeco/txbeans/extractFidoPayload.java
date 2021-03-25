/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
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
 * Extracts the "payload" JsonObject for inclusion into the webservice
 * request to the FIDO server. 
 * 
 * In some webservices - preregister and preauthenticate - the SACL app 
 * does not need to send a "payload" sub-element since the sfaCredentials 
 * provide enough information to the SACL back-end to create the payload 
 * for relay to the FIDO server. In all other cases, it must be extracted 
 * from the sfaFidoServiceInput JsonObject for inclusion in the parameters 
 * for the FIDO webservice. 
 * 
 * Secondly, the "metadata" Json object is added to the payload on the 
 * business application side, so the FIDO server can get corroboration on
 * the origin on which the business application (SACL) is available. This
 * must match up with clientDataJson sent by the Android rich client app (RCA).
 */
package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;

@Stateless
public class extractFidoPayload implements extractFidoPayloadLocal {

    private final short sid = Common.getSid();
    private final String classname = "extractFidoPayload";
    
    @EJB private addUserTransactionLocal addutx;
    @EJB private addUserProductsLocal addutxp;
    
    /**
     * Extracts and/or creates the FIDO service request's payload element
     * @param did short Domain Id
     * @param fidoinput JsonObject containing FIDO input from the app
     * @param userinfo JsonObject with userinfo information to build payload elements
     * @param txid String for logging
     * @return JsonObject with payload 
     */
    @Override
    public JsonObject execute(short did, JsonObject fidoinput, JsonObject userinfo, String txid) {
        
        // Build the "payload" object to be sent to the FIDO server
        JsonObject payload = null;
        JsonObject payloadWithMetadata = null;
        String service = fidoinput.getString(Constants.JSON_KEY_SACL_FIDO_SERVICE);
        switch (Constants.SACL_FIDO_SERVICES.valueOf(service)) 
        {
            case SACL_FIDO_SERVICE_GET_FIDO_REGISTRATION_CHALLENGE:
                 payload = Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD, Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME, userinfo.getString(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME))
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_DISPLAY_NAME, userinfo.getString(Constants.JSON_KEY_FIDO_PAYLOAD_DISPLAY_NAME))
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_OPTIONS, Json.createObjectBuilder()
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_ATTESTATION_CONVEYANCE, Constants.JSON_KEY_FIDO_PAYLOAD_ATTESTATION_DIRECT))
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_EXTENSIONS, "{}")) // TODO: Change this to a Json object
                .build();
                break;
            case SACL_FIDO_SERVICE_REGISTER_FIDO_KEY:
                if (fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD) != null) {
                    payloadWithMetadata = Json.createObjectBuilder()
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL, 
                            fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD).getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL))
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_STRONGKEY_METADATA, Json.createObjectBuilder()
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION_LABEL, Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION)
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_CREATE_LOCATION, "Cupertino, CA")
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_ORIGIN, Common.getConfigurationProperty("sfaeco.cfg.property.fido.origin"))    
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_USERNAME_LABEL, userinfo.getString(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME)))
                    .build();
                    Common.log(Level.INFO, "SFAECO-MSG-1000", "Payload with strongKeyMetadata: " + payloadWithMetadata);
                     return payloadWithMetadata;
                } else {
                    Common.log(Level.WARNING, "SFAECO-ERR-3010", Constants.JSON_KEY_FIDO_PAYLOAD + "in " + fidoinput);
                }
                break;    
             case SACL_FIDO_SERVICE_GET_FIDO_AUTHENTICATION_CHALLENGE:
                payload = Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD, Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME, userinfo.getString(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME))
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_OPTIONS, Json.createObjectBuilder()))
                .build();
                break;
            case SACL_FIDO_SERVICE_AUTHENTICATE_FIDO_KEY:
                if (fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD) != null) {
                     payloadWithMetadata = Json.createObjectBuilder()
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL, 
                            fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD).getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL))
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_STRONGKEY_METADATA, Json.createObjectBuilder()
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION_LABEL, Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION)
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_LAST_USED_LOCATION, "Cupertino, CA")
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_ORIGIN, Common.getConfigurationProperty("sfaeco.cfg.property.fido.origin"))    
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_USERNAME_LABEL, userinfo.getString(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME)))
                    .build();
                     Common.log(Level.INFO, "SFAECO-MSG-1000", "Payload with strongKeyMetadata: " + payloadWithMetadata);
                     return payloadWithMetadata;
                } else {
                    Common.log(Level.WARNING, "SFAECO-ERR-3010", Constants.JSON_KEY_FIDO_PAYLOAD + "in " + fidoinput);
                }
                break;
            case SACL_FIDO_SERVICE_GET_FIDO_AUTHORIZATION_CHALLENGE:
                // Build payload to include Payee, Method of Payment, Amount, etc. to
                // enable getting a challenge from the FIDO server that incorporates
                // the information into the derived challenge
                Long uid = userinfo.getJsonNumber(Constants.JSON_KEY_UID).longValue();
                JsonObject cartInsidePayload = fidoinput.getJsonObject(Constants.JSON_KEY_SACL_FIDO_TRANSACTION_PAYLOAD);
                String cart = cartInsidePayload.getString(Constants.JSON_KEY_SACL_FIDO_TRANSACTION_CART);
                String decodedCart = new String(Common.urlDecode(cart), StandardCharsets.UTF_8);
                Common.log(Level.INFO, "SFAECO-MSG-1000", "decodedCart String: " + decodedCart);                    
                JsonObject txpayload = buildTxpayload(did, uid, decodedCart, txid);
                String b64uTxpayload = Common.urlEncode(txpayload.toString());
                Common.log(Level.INFO, "SFAECO-MSG-1000", "B64U Txpayload: " + b64uTxpayload);
        
                payload = Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD, Json.createObjectBuilder()
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME, userinfo.getString(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME))
                    .add(Constants.SFA_ECO_PAYMENT_TRANSACTION_TXID, txpayload.getString(Constants.SFA_ECO_PAYMENT_TRANSACTION_TXID))
                    .add(Constants.SFA_ECO_PAYMENT_TRANSACTION_TXPAYLOAD, b64uTxpayload)
                    .add(Constants.JSON_KEY_FIDO_PAYLOAD_OPTIONS, Json.createObjectBuilder()))
                .build();
                break;
            case SACL_FIDO_SERVICE_AUTHORIZE_FIDO_TRANSACTION:
                if (fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD) != null) {
                     payloadWithMetadata = Json.createObjectBuilder()
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_TXID, fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD).getString(Constants.JSON_KEY_FIDO_PAYLOAD_TXID))
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_TXPAYLOAD, fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD).getString(Constants.JSON_KEY_FIDO_PAYLOAD_TXPAYLOAD))
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL, 
                            fidoinput.getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD).getJsonObject(Constants.JSON_KEY_FIDO_PAYLOAD_PUBLIC_KEY_CREDENTIAL))
                        .add(Constants.JSON_KEY_FIDO_PAYLOAD_STRONGKEY_METADATA, Json.createObjectBuilder()
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION_LABEL, Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_VERSION)
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_LAST_USED_LOCATION, "Cupertino, CA")
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_ORIGIN, Common.getConfigurationProperty("sfaeco.cfg.property.fido.origin"))    
                            .add(Constants.JSON_KEY_FIDO_PAYLOAD_METADATA_USERNAME_LABEL, userinfo.getString(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME)))
                    .build();
                     Common.log(Level.INFO, "SFAECO-MSG-1000", "Payload with strongKeyMetadata: " + payloadWithMetadata);
                     return payloadWithMetadata;
                } else {
                    Common.log(Level.WARNING, "SFAECO-ERR-3010", Constants.JSON_KEY_FIDO_PAYLOAD + "in " + fidoinput);
                }
                break;
            default:
                Common.log(Level.WARNING, "SFAECO-ERR-1000", "Invalid FIDO Service: " + Constants.SACL_FIDO_SERVICES.valueOf(service));
                return null;
        }
        Common.log(Level.INFO, "SFAECO-MSG-1000", "Payload: " + payload);
        return payload;
    }
    
    /**
     * Creates the payload that's sent to the FIDO server for the challenge
     * @param did
     * @param decodedCart
     * @return JsonObject
     */
    private JsonObject buildTxpayload (short did, long uid, String decodedCart,String txid) 
    {
        JsonObject jsonCart = Common.stringToJson(decodedCart);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "jsonCart: " + jsonCart);
        
        // Add a UserTransaction object to the database and get SFAECO TXID
        String sfaecoTxid;
        JsonObject utxjo = addutx.execute(did, uid, jsonCart, txid);
        Common.log(Level.INFO, "SFAECO-MSG-1000", "utxjo object: " + utxjo);
        
//        try {
//            sfaecoTxid = utxjo.getJsonObject(Constants.JSON_KEY_USER_TRANSACTION).getString(Constants.JSON_KEY_TXID);
//            Common.log(Level.INFO, "SFAECO-MSG-1000", "TXID inside try-catch: " + sfaecoTxid);
//        } catch (NullPointerException npe) {
//             Common.log(Level.SEVERE, "SFAECO-ERR-1000", "Caught NPE getting : " + sfaecoTxid);
//             sfaecoTxid = utxjo.getString(Constants.JSON_KEY_TXID);
//             Common.log(Level.INFO, "SFAECO-MSG-1000", "TXID inside NPE: " + sfaecoTxid);
//        }

        if (utxjo != null) {
            sfaecoTxid = utxjo.getJsonObject(Constants.JSON_KEY_USER_TRANSACTION).getString(Constants.JSON_KEY_TXID);
           Common.log(Level.INFO, "SFAECO-MSG-1000", "Acquired TXID: " + sfaecoTxid);
        } else {
            Common.log(Level.SEVERE, "SFAECO-ERR-1000", "Could not add UserTransaction and get TXID");
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1000", "Could not add UserTransaction and get TXID");
        }
        
        // Add products in the cart to UTX_PRODUCTS
        long utxid = utxjo.getJsonObject(Constants.JSON_KEY_USER_TRANSACTION)
                .getJsonNumber(Constants.JSON_KEY_UTXID).longValue();
        
        JsonObject response = addutxp.execute(did, uid, utxid, jsonCart, txid);
        if (response == null) {
            Common.log(Level.SEVERE, "SFAECO-ERR-1010", "Could not add UserProducts: " + decodedCart);
        }
        
        // TODO: Change to merchantName after testing
        int merchantId = jsonCart.getJsonObject(Constants.SFA_ECO_CART_LABEL)
                .getInt(Constants.SFA_ECO_CART_MERCHANT_ID_LABEL);                
        String merchantName =  Constants.SFA_ECO_STRONGKEY_LABEL;
        String currency = jsonCart.getJsonObject(Constants.SFA_ECO_CART_LABEL)
                .getString(Constants.SFA_ECO_CART_CURRENCY_LABEL);
        String totalPrice = Integer.toString(jsonCart.getJsonObject(Constants.SFA_ECO_CART_LABEL)
                .getInt(Constants.SFA_ECO_CART_TOTAL_PRICE_LABEL));
        String cardBrand = jsonCart.getJsonObject(Constants.SFA_ECO_CART_LABEL)
                .getJsonObject(Constants.SFA_ECO_CART_PAYMENT_METHOD_LABEL)
                .getString(Constants.SFA_ECO_CART_PAYMENT_METHOD_CARD_BRAND_LABEL);
        String cardLast4 = jsonCart.getJsonObject(Constants.SFA_ECO_CART_LABEL)
                .getJsonObject(Constants.SFA_ECO_CART_PAYMENT_METHOD_LABEL)
                .getString(Constants.SFA_ECO_CART_PAYMENT_METHOD_CARD_LAST4_LABEL);
        String txDate = new Date().toString();
        
        JsonObject txpayload = Json.createObjectBuilder()
                .add(Constants.SFA_ECO_CART_MERCHANT_NAME_LABEL, merchantName)
                .add(Constants.SFA_ECO_CART_CURRENCY_LABEL, currency)
                .add(Constants.SFA_ECO_CART_TOTAL_PRICE_LABEL, totalPrice)
                .add(Constants.SFA_ECO_CART_PAYMENT_METHOD_CARD_BRAND_LABEL, cardBrand)
                .add(Constants.SFA_ECO_CART_PAYMENT_METHOD_CARD_LAST4_LABEL, cardLast4)
                .add(Constants.SFA_ECO_PAYMENT_TRANSACTION_TXID, sfaecoTxid)
                .add(Constants.SFA_ECO_PAYMENT_TRANSACTION_TXDATE, txDate)
                .build();
        
        Common.log(Level.INFO, "SFAECO-MSG-1000", "Txpayload Json: " + txpayload);
        return txpayload;
    }
}
