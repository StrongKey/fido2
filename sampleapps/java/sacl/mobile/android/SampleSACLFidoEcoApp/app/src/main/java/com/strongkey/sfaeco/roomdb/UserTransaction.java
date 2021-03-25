/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, as published by the Free
 * Software Foundation and available at
 * http://www.fsf.org/licensing/licenses/lgpl.html, version 2.1.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (d/b/a StrongKey)
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
 * Entity class to represent to represent the User Transaction within the
 * app, and between the server and the app.
 */

package com.strongkey.sfaeco.roomdb;

import androidx.annotation.NonNull;
import androidx.room.PrimaryKey;

import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sfaeco.utilities.SfaConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Date;
import java.util.HashSet;

//@Entity(tableName = "user_transactions",
//        indices = { @Index(value = {"did", "sid", "uid", "utxid"}, unique = true),
//                @Index(value = {"did", "uid", "txid"}, unique = true)
//})

public class UserTransaction {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @NonNull
    private int  did;

    @NonNull
    private int sid;

	@NonNull
    private Long uid;

    @NonNull
    private Long utxid;

    @NonNull
    private String txid;

    @NonNull
    private Long txdate;

    @NonNull
    private int merchantId;

    @NonNull
    private Collection<Product> products;

    @NonNull
    private int totalProducts;

    @NonNull
    private int totalPrice;

    @NonNull
    private PaymentMethod paymentMethod;

    @NonNull
    private SfaConstants.TRANSACTION_CURRENCY currency;

    @NonNull
    private String txpayload;

    private String nonce;

    private String challenge;

    // Available only after calling authorize webservice on FIDO server
    private Collection<FidoAuthenticatorReferences> fidoAuthenticatorReferences;

    /**
     * Empty Constructor
     */
    public UserTransaction() {}

    /**
     * Getters and Setters
     */
    @NonNull
    public Integer getId() {
        return id;
    }

    public void setId(@NonNull Integer id) {
        this.id = id;
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

	public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }

    @NonNull
    public Long getUid() { return uid; }

    public void setUid(@NonNull Long uid) { this.uid = uid; }

    @NonNull
    public Long getUtxid() { return utxid; }

    public void setUtxid(@NonNull Long utxid) { this.utxid = utxid; }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public Long getTxdate() {
        return txdate;
    }

    public void setTxdate(Long txdate) {
        this.txdate = txdate;
    }

    public int getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(int merchantId) {
        this.merchantId = merchantId;
    }

    public Collection<Product> getProducts() {
        return products;
    }

    public void setProducts(Collection<Product> products) {
        this.products = products;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public SfaConstants.TRANSACTION_CURRENCY getCurrency() {
        return currency;
    }

    public void setCurrency(SfaConstants.TRANSACTION_CURRENCY currency) {
        this.currency = currency;
    }

    @NonNull
    public String getTxpayload() { return txpayload; }

    public void setTxpayload(@NonNull String txpayload) { this.txpayload = txpayload; }

    public String getNonce() { return nonce; }

    public void setNonce(String nonce) { this.nonce = nonce; }

    public String getChallenge() { return challenge; }

    public void setChallenge(String challenge) { this.challenge = challenge; }

    public Collection<FidoAuthenticatorReferences> getFidoAuthenticatorReferences() {
        return fidoAuthenticatorReferences;
    }

    public void setFidoAuthenticatorReferences(Collection<FidoAuthenticatorReferences> fidoAuthenticatorReferences) {
        this.fidoAuthenticatorReferences = fidoAuthenticatorReferences;
    }

    public String toString() {
        return  "id: " + this.id + ", " +
                "did: " + this.did + ", " +
				"sid: " + this.sid + ", " +
                "uid: " + this.uid + ", " +
                "utxid: " + this.utxid + ", " +
                "txid: " + this.txid + ", " +
                "txdate: " + new Date(this.txdate) + ", " +
                "merchantId: " + this.merchantId + ", " +
                "products: " + this.products + ", " +
                "totalProducts: " + this.totalProducts + ", " +
                "totalPrice: " + this.totalPrice + ", " +
                "paymentMethod: " + this.paymentMethod + ", " +
                "currency: " + this.currency + ", " +
                "nonce: " + this.nonce + ", " +
                "challenge: " + this.challenge + ", " +
                "fidoAuthenticatorReferences: " + this.fidoAuthenticatorReferences;
    }

    /**********************************************************************************************
     *  .d888 d8b      888                 d8888          888    888      8888888b.            .d888
     * d88P"  Y8P      888                d88888          888    888      888   Y88b          d88P"
     * 888             888               d88P888          888    888      888    888          888
     * 888888 888  .d88888  .d88b.      d88P 888 888  888 888888 88888b.  888   d88P  .d88b.  888888
     * 888    888 d88" 888 d88""88b    d88P  888 888  888 888    888 "88b 8888888P"  d8P  Y8b 888
     * 888    888 888  888 888  888   d88P   888 888  888 888    888  888 888 T88b   88888888 888
     * 888    888 Y88b 888 Y88..88P  d8888888888 Y88b 888 Y88b.  888  888 888  T88b  Y8b.     888
     * 888    888  "Y88888  "Y88P"  d88P     888  "Y88888  "Y888 888  888 888   T88b  "Y8888  888
     *********************************************************************************************/

    public class FidoAuthenticatorReferences {

        private String protocol;
        private String id;
        private String rawId;
        private String userHandle;
        private String rpid;
        private String authenticatorData;
        private String clientDataJson;
        private String aaguid;
        private Long authorizationTime;
        private Boolean up;
        private Boolean uv;
        private Boolean usedForThisTransaction;
        private String signerPublicKey;
        private String signature;
        private String signingKeyType;
        private String signingKeyAlgorithm;

        /**
         * Empty Constructor
         */
        public FidoAuthenticatorReferences() {}

        /**
         * Getters and Setters
         */
        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getRawId() {
            return rawId;
        }

        public void setRawId(String rawId) {
            this.rawId = rawId;
        }

        public String getUserHandle() {
            return userHandle;
        }

        public void setUserHandle(String userHandle) {
            this.userHandle = userHandle;
        }

        public String getRpid() {
            return rpid;
        }

        public void setRpid(String rpid) {
            this.rpid = rpid;
        }

        public String getAuthenticatorData() {
            return authenticatorData;
        }

        public void setAuthenticatorData(String authenticatorData) {
            this.authenticatorData = authenticatorData;
        }

        public String getClientDataJson() {
            return clientDataJson;
        }

        public void setClientDataJson(String clientDataJson) {
            this.clientDataJson = clientDataJson;
        }

        public String getAaguid() {
            return aaguid;
        }

        public void setAaguid(String aaguid) {
            this.aaguid = aaguid;
        }

        public Long getAuthorizationTime() {
            return authorizationTime;
        }

        public void setAuthorizationTime(Long authorizationTime) {
            this.authorizationTime = authorizationTime;
        }

        public Boolean getUp() {
            return up;
        }

        public void setUp(Boolean up) {
            this.up = up;
        }

        public Boolean getUv() {
            return uv;
        }

        public void setUv(Boolean uv) {
            this.uv = uv;
        }

        public Boolean getUsedForThisTransaction() {
            return usedForThisTransaction;
        }

        public void setUsedForThisTransaction(Boolean usedForThisTransaction) {
            this.usedForThisTransaction = usedForThisTransaction;
        }

        public String getSignerPublicKey() {
            return signerPublicKey;
        }

        public void setSignerPublicKey(String signerPublicKey) {
            this.signerPublicKey = signerPublicKey;
        }

        public String getSignature() {
            return signature;
        }

        public void setSignature(String signature) {
            this.signature = signature;
        }

        public String getSigningKeyType() { return signingKeyType; }

        public void setSigningKeyType(String signingKeyType) { this.signingKeyType = signingKeyType; }

        public String getSigningKeyAlgorithm() { return signingKeyAlgorithm; }

        public void setSigningKeyAlgorithm(String signingKeyAlgorithm) {
            this.signingKeyAlgorithm = signingKeyAlgorithm;
        }

        @Override
        public String toString() {
            return "FidoAuthenticatorReferences{" +
                    "protocol='" + protocol + '\'' +
                    ", id='" + id + '\'' +
                    ", rawId='" + rawId + '\'' +
                    ", userHandle='" + userHandle + '\'' +
                    ", rpid='" + rpid + '\'' +
                    ", authenticatorData='" + authenticatorData + '\'' +
                    ", clientDataJson='" + clientDataJson + '\'' +
                    ", aaguid='" + aaguid + '\'' +
                    ", authorizationTime='" + authorizationTime + '\'' +
                    ", up=" + up +
                    ", uv=" + uv +
                    ", usedForThisTransaction=" + usedForThisTransaction +
                    ", signerPublicKey='" + signerPublicKey + '\'' +
                    ", signature='" + signature + '\'' +
                    ", signingKeyType='" + signingKeyType + '\'' +
                    ", signingKeyAlgorithm='" + signingKeyAlgorithm + '\'' +
                    '}';
        }

        public JSONObject toJSON() throws JSONException{

            JSONArray jsonArray = new JSONArray();
            int size = fidoAuthenticatorReferences.size();
            for (FidoAuthenticatorReferences far : fidoAuthenticatorReferences) {
                JSONObject jo = new JSONObject()
                    .put("protocol", protocol)
                    .put("id", id)
                    .put("rawId", rawId)
                    .put("userHandle", userHandle)
                    .put("rpid", rpid)
                    .put("authenticatorData", authenticatorData)
                    .put("clientDataJson", clientDataJson)
                    .put("aaguid", aaguid)
                    .put("authorizationTime", authorizationTime)
                    .put("up", up)
                    .put("uv", uv)
                    .put("usedForThisTransaction", usedForThisTransaction)
                    .put("signerPublicKey", signerPublicKey)
                    .put("signature", signature)
                    .put("signingKeyType", signingKeyType)
                    .put("signingKeyAlgorithm", signingKeyAlgorithm);
                jsonArray.put(jo);
            }

            return new JSONObject()
                .put("FidoAuthenticatorReferences", jsonArray);
        }
    }

    /**
     * Convert JSONObject returned by the FIDO server from a transaction authorization
     * to store the FIDOAuthenticatorReferences JSON objects as a Collection in this object
     * @param responseJson String
     * @return int value of the number of FAR objects in collection
     */
    public int storeFidoAuthenticatorReferences(String responseJson) throws JSONException {

        FidoAuthenticatorReferences far;
        int n = 0;

        JSONObject azResponse = com.strongkey.sacl.utilities.Common.toJSON(responseJson);
        if (azResponse != null) {
            Collection<FidoAuthenticatorReferences> fidoAuthenticatorReferences = new HashSet<>();
            JSONArray jsonArray = azResponse.getJSONArray(Constants.JSON_KEY_FAR_LABEL);
            for (int i=0; i < jsonArray.length(); i++) {
                JSONObject jo = jsonArray.optJSONObject(i);
                far = new FidoAuthenticatorReferences();
                far.setProtocol(jo.getString(Constants.JSON_KEY_FAR_PROTOCOL_LABEL));
                far.setId(jo.getString(Constants.JSON_KEY_FAR_ID_LABEL));
                far.setRawId(jo.getString(Constants.JSON_KEY_FAR_RAWID_LABEL));
                far.setUserHandle(jo.getString(Constants.JSON_KEY_FAR_USER_HANDLE_LABEL));
                far.setRpid(jo.getString(Constants.JSON_KEY_FAR_RPID_LABEL));
                far.setAuthenticatorData(jo.getString(Constants.JSON_KEY_FAR_AUTHENTICATORDATA_LABEL));
                far.setClientDataJson(jo.getString(Constants.JSON_KEY_FAR_CLIENTDATAJSON_LABEL));
                far.setAaguid(jo.getString(Constants.JSON_KEY_FAR_AAGUID_LABEL));
                far.setAuthorizationTime(jo.getLong(Constants.JSON_KEY_FAR_AUTHORIZATION_TIME_LABEL));
                far.setUp(jo.getBoolean(Constants.JSON_KEY_FAR_UP_LABEL));
                far.setUv(jo.getBoolean(Constants.JSON_KEY_FAR_UV_LABEL));
                far.setUsedForThisTransaction(jo.getBoolean(Constants.JSON_KEY_FAR_USED_FOR_THIS_TRANSACTION_LABEL));
                far.setSignerPublicKey(jo.getString(Constants.JSON_KEY_FAR_SIGNER_PUBLIC_KEY_LABEL));
                far.setSignature(jo.getString(Constants.JSON_KEY_FAR_SIGNATURE_LABEL));
                far.setSigningKeyType(jo.getString(Constants.JSON_KEY_FAR_SIGNING_KEY_TYPE_LABEL));
                far.setSigningKeyAlgorithm(jo.getString(Constants.JSON_KEY_FAR_SIGNING_KEY_ALGORITHM_LABEL));

                if (fidoAuthenticatorReferences.add(far))
                    n++;
            }
            setFidoAuthenticatorReferences(fidoAuthenticatorReferences);
        }

        return n;
    }

}
