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
 * Copyright (c) 2001-2022 StrongAuth, Inc. (d/b/a StrongKey)
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
 * The entity class for FIDO digital signatures created by the authenticator
 * as part of authorizing a bsuiness transaction. Used by SACL and is not part
 * of the FIDO2 specification - local to RoomDB
 */

package com.strongkey.sacl.roomdb;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.strongkey.sacl.utilities.Constants;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

@Entity(tableName = "authorization_signatures")
public class AuthorizationSignature {

    @Ignore
    final String TAG = "AuthorizationSignature";

    @PrimaryKey(autoGenerate = true)
    public int id;

    // PreauthorizeChallenge object's RoomDB ID
    @NonNull
    @ColumnInfo(name = "preauthorize_challenge_id")
    public int pazId;

    // Not part of the FIDO2 specification
    @NonNull
    private int did;

    // Not part of the FIDO2 specification
    @NonNull
    private long uid;

    @NonNull
    @ColumnInfo(name = "credential_id")
    private String credentialId;

    @NonNull
    private String rpid;

    @NonNull
    @ColumnInfo(name = "authenticator_data")
    private String authenticatorData;

    @NonNull
    @ColumnInfo(name = "client_data_json")
    private String clientDataJson;

    @NonNull
    private String txid;
    
    // Base64Url encoded transaction data in application-specific format
    @NonNull
    private String txpayload;

    @NonNull
    private String nonce;

    @NonNull
    private String challenge;
    
    @NonNull
    private String signature;

    private String responseJson;

    // Not part of the FIDO2 specification
    @ColumnInfo(name = "create_date")
    @NonNull
    private Long createDate;

    public AuthorizationSignature() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

   public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getPazId() {
        return pazId;
    }

    public void setPazId(int pazId) {
        this.pazId = pazId;
    }

    @NonNull
    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(@NonNull String credentialId) {
        this.credentialId = credentialId;
    }

    @NonNull
    public String getRpid() {
        return rpid;
    }

    public void setRpid(@NonNull String rpid) {
        this.rpid = rpid;
    }

    @NonNull
    public String getAuthenticatorData() {
        return authenticatorData;
    }

    public void setAuthenticatorData(@NonNull String authenticatorData) {
        this.authenticatorData = authenticatorData;
    }

    @NonNull
    public String getClientDataJson() {
        return clientDataJson;
    }

    public void setClientDataJson(@NonNull String clientDataJson) {
        this.clientDataJson = clientDataJson;
    }

    @NonNull
    public String getTxid() { return txid; }

    public void setTxid(@NonNull String txid) { this.txid = txid; }

    @NonNull
    public String getTxpayload() { return txpayload; }

    public void setTxpayload(@NonNull String txpayload) { this.txpayload = txpayload; }

    @NonNull
    public String getNonce() { return nonce; }

    public void setNonce(@NonNull String nonce) { this.nonce = nonce; }

    @NonNull
    public String getChallenge() { return challenge; }

    public void setChallenge(@NonNull String challenge) { this.challenge = challenge; }

    @NonNull
    public String getSignature() {
        return signature;
    }

    public void setSignature(@NonNull String signature) {
        this.signature = signature;
    }

    public String getResponseJson() {
        return responseJson;
    }

    public void setResponseJson(String responseJson) {
        this.responseJson = responseJson;
    }

    @NonNull
    public Long getCreateDate() {
        return createDate;
    }

    public Date getCreateDateFromLong() {
        return fromTimestamp(createDate);
    }

    public void setCreateDate(@NonNull Long createDate) {
        this.createDate = createDate;
    }

    public void setCreateDate(@NonNull Date createDate) {
        this.createDate = dateToTimestamp(createDate);
    }

    @Override
    public String toString() {
        return "AuthorizationSignature {" +
                "id=" + id +
                ", pazId=" + pazId +
                ", uid=" + uid +
                ", did=" + did +
                ", rpid='" + rpid + '\'' +
                ", credentialId='" + credentialId + '\'' +
                ", authenticatorData='" + authenticatorData + '\'' +
                ", clientDataJson='" + clientDataJson + '\'' +
                ", txid='" + txid + '\'' +
                ", txpayload='" + txpayload + '\'' +
                ", nonce='" + nonce + '\'' +
                ", challenge='" + challenge + '\'' +
                ", signature='" + signature + '\'' +
                ", responseJson='" + responseJson + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
            .put("AuthorizationSignature", new JSONObject()
                .put("id", id)
                .put("pazId", pazId)
                .put("uid", uid)
                .put("did", did)
                .put("rpid", rpid)
                .put("credentialId", credentialId)
                .put("authenticatorData", authenticatorData)
                .put("clientDataJson", clientDataJson)
                .put("txid", txid)
                .put("txpayload", txpayload)
                .put("nonce", nonce)
                .put("challenge", challenge)
                .put("signature", signature)
                .put("responseJson", responseJson)
                .put("createDate", createDate));
    }

    /**
     * Parses the JSON response string and returns a AuthorizationSignature object
     * @param response String with JSON
     * @return boolean
     */
    public boolean parseAuthorizationSignatureJsonString(String response) {
        boolean parsedSuccessfully = false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject azJsonObject = jsonObject.getJSONObject(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL);
            Iterator<String> jsonkeys = azJsonObject.keys();
            while (jsonkeys.hasNext()) {
                String jsonkey = jsonkeys.next();
                switch (jsonkey) {
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_ID:
                        setId(azJsonObject.getInt(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_ID));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_DID:
                        setDid(azJsonObject.getInt(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_DID));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_UID:
                        setUid(azJsonObject.getLong(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_UID));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_PAZID:
                        setPazId(azJsonObject.getInt(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_PAZID));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_RPID:
                        setRpid(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_RPID));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CREDENTIALID:
                        setCredentialId(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CREDENTIALID));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_AUTHENTICATORDATA:
                        setAuthenticatorData(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_AUTHENTICATORDATA));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CLIENTDATAJSON:
                        setClientDataJson(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CLIENTDATAJSON));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXID:
                        setTxid(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXID));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXPAYLOAD:
                        setTxpayload(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_TXPAYLOAD));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_NONCE:
                        setNonce(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_NONCE));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CHALLENGE:
                        setChallenge(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CHALLENGE));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_SIGNATURE:
                        setSignature(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_SIGNATURE));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_RESPONSE_JSON:
                        setResponseJson(azJsonObject.getString(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_RESPONSE_JSON));
                        break;
                    case Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CREATEDATE:
                        setCreateDate(azJsonObject.getLong(Constants.JSON_KEY_AUTHORIZATION_SIGNATURE_LABEL_CREATEDATE));
                        parsedSuccessfully = true;
                        break;
                    default:
                        break;
                }
            }
            Log.v(TAG, "Parsed AUTHORIZATION_SIGNATURE object: " + toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parsedSuccessfully;
    }

    @TypeConverter
    public Date fromTimestamp(Long value) {
        return value == null ? null : new Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }
}
