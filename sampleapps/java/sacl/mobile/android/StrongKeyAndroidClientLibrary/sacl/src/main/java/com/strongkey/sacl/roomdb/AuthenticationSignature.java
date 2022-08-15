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
 * as part of the authentication process. Used by SACL and is not part of the
 * FIDO2 specification - local to RoomDB
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

@Entity(tableName = "authentication_signatures")
public class AuthenticationSignature {

    @Ignore
    final String TAG = "AuthenticationSignature";

    @PrimaryKey(autoGenerate = true)
    public int id;

    // PreauthenticateChallenge object's RoomDB ID
    @NonNull
    @ColumnInfo(name = "preauthenticate_challenge_id")
    public int pacId;

    // Not part of the FIDO2 specification - from the MDBA
    @NonNull
    private int did;

    // Not part of the FIDO2 specification - from the MDBA
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
    private String signature;

    // Not part of the FIDO2 specification
    @ColumnInfo(name = "create_date")
    @NonNull
    private Long createDate;

    public AuthenticationSignature() {
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

    public int getPacId() {
        return pacId;
    }

    public void setPacId(int pacId) {
        this.pacId = pacId;
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
    public String getSignature() {
        return signature;
    }

    public void setSignature(@NonNull String signature) {
        this.signature = signature;
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
        return "AuthenticationSignature {" +
                "id=" + id +
                ", pacId=" + pacId +
                ", uid=" + uid +
                ", did=" + did +
                ", rpid='" + rpid + '\'' +
                ", credentialId='" + credentialId + '\'' +
                ", authenticatorData='" + authenticatorData + '\'' +
                ", clientDataJson='" + clientDataJson + '\'' +
                ", signature='" + signature + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
                .put("AuthenticationSignature", new JSONObject()
                        .put("id", id)
                        .put("pacId", pacId)
                        .put("uid", uid)
                        .put("did", did)
                        .put("rpid", rpid)
                        .put("credentialId", credentialId)
                        .put("authenticatorData", authenticatorData)
                        .put("clientDataJson", clientDataJson)
                        .put("signature", signature)
                        .put("createDate", createDate));
    }


    /**
     * Parses the JSON response string and returns a AuthenticationSignature object
     * @param response String with JSON
     * @return boolean
     */
    public boolean parseAuthenticationSignatureJsonString(String response) {
        boolean parsedSuccessfully = false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonas = jsonObject.getJSONObject(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL);
            Iterator<String> jsonkeys = jsonas.keys();
            while (jsonkeys.hasNext()) {
                String jsonkey = jsonkeys.next();
                switch (jsonkey) {
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_ID:
                        setId(jsonas.getInt(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_ID));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_DID:
                        setDid(jsonas.getInt(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_DID));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_UID:
                        setUid(jsonas.getLong(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_UID));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_PACID:
                        setPacId(jsonas.getInt(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_PACID));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_RPID:
                        setRpid(jsonas.getString(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_RPID));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREDENTIALID:
                        setCredentialId(jsonas.getString(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREDENTIALID));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_AUTHENTICATORDATA:
                        setAuthenticatorData(jsonas.getString(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_AUTHENTICATORDATA));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CLIENTDATAJSON:
                        setClientDataJson(jsonas.getString(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CLIENTDATAJSON));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_SIGNATURE:
                        setSignature(jsonas.getString(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_SIGNATURE));
                        break;
                    case Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREATEDATE:
                        setCreateDate(jsonas.getLong(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREATEDATE));
                        parsedSuccessfully = true;
                        break;
                    default:
                        break;
                }
            }
            // Need to set ID to 0 to save it
            setId(0);
            Log.v(TAG, "Parsed AUTHENTICATION_SIGNATURE object: " + toString());
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
