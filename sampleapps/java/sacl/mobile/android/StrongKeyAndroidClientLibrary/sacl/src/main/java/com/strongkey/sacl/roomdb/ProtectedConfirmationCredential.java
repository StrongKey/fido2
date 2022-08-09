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
 * The entity class for FIDO PublicKeyCredentials created by the authenticator.
 * https://www.w3.org/TR/webauthn/#public-key-credential-source
 *
 */

package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

@Entity(tableName = "protected_confirmation_credential",
        indices = {@Index(value = {"did", "rpid", "userid", "credential_id"}, unique = true)})

public class ProtectedConfirmationCredential {

    // Not part of the FIDO2 specification - local to RoomDB
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Not part of the FIDO2 specification - local to RoomDB
    // PreregisterChallenge object's RoomDB ID
    @NonNull
    @ColumnInfo(name = "preregister_challenge_id")
    public int prcId;

    // Not part of the FIDO2 specification - from the MDBA
    @NonNull
    private int did;

    // Not part of the FIDO2 specification - from the MDBA
    @NonNull
    private long uid;

    // Part of the FIDO2 specification, but local to RoomDB
    @NonNull
    private int counter;

    /***** Required parts of Public Key Credential Source *****/

    @NonNull
    private String type;

    @NonNull
    private String userid;

    @NonNull
    @ColumnInfo(name = "credential_id")
    private String credentialId;

    @NonNull
    private String username;

    @NonNull
    private String rpid;

    @NonNull
    @ColumnInfo(name = "key_alias")
    private String keyAlias;

    @NonNull
    @ColumnInfo(name = "key_origin")
    private String keyOrigin;

    @NonNull
    @ColumnInfo(name = "key_algorithm")
    private String keyAlgorithm;

    @NonNull
    @ColumnInfo(name = "key_size")
    private int keySize;

    @NonNull
    @ColumnInfo(name = "se_module")
    private String seModule;

    @NonNull
    @ColumnInfo(name = "public_key")
    private String publicKey;

    // Nullable
    @ColumnInfo(name = "user_handle")
    private String userHandle;

    @NonNull
    @ColumnInfo(name = "display_name")
    private String displayName;

    @NonNull
    @ColumnInfo(name = "authenticator_data")
    private String authenticatorData;

    @NonNull
    @ColumnInfo(name = "client_data_json")
    private String clientDataJson;

    // Nullable
    @ColumnInfo(name = "json_attestation")
    private String jsonAttestation;

    // Nullable
    @ColumnInfo(name = "cbor_attestation")
    private String cborAttestation;

    // Not part of the FIDO2 specification
    @ColumnInfo(name = "create_date")
    @NonNull
    private Long createDate;

    public ProtectedConfirmationCredential() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getPrcId() {
        return prcId;
    }

    public void setPrcId(int prcId) {
        this.prcId = prcId;
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

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    @NonNull
    public String getType() {
        return type;
    }

    public void setType(@NonNull String type) {
        this.type = type;
    }

    @NonNull
    public String getUserid() {
        return userid;
    }

    @NonNull
    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(@NonNull String credentialId) {
        this.credentialId = credentialId;
    }

    public void setUserid(@NonNull String userid) {
        this.userid = userid;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getRpid() {
        return rpid;
    }

    public void setRpid(@NonNull String rpid) {
        this.rpid = rpid;
    }

    @NonNull
    public String getKeyAlias() {
        return keyAlias;
    }

    public void setKeyAlias(@NonNull String keyAlias) {
        this.keyAlias = keyAlias;
    }

    @NonNull
    public String getKeyOrigin() {
        return keyOrigin;
    }

    public void setKeyOrigin(@NonNull String keyOrigin) {
        this.keyOrigin = keyOrigin;
    }

    @NonNull
    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    public void setKeyAlgorithm(@NonNull String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public int getKeySize() {
        return keySize;
    }

    public void setKeySize(int keySize) {
        this.keySize = keySize;
    }

    @NonNull
    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(@NonNull String publicKey) {
        this.publicKey = publicKey;
    }

    @NonNull
    public String getSeModule() {
        return seModule;
    }

    public void setSeModule(@NonNull String seModule) {
        this.seModule = seModule;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }

    @NonNull
    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(@NonNull String displayName) {
        this.displayName = displayName;
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

    public String getJsonAttestation() {
        return jsonAttestation;
    }

    public void setJsonAttestation(String jsonAttestation) {
        this.jsonAttestation = jsonAttestation;
    }

    public String getCborAttestation() {
        return cborAttestation;
    }

    public void setCborAttestation(String cborAttestation) {
        this.cborAttestation = cborAttestation;
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
        return "PublicKeyCredential {" +
                "id=" + id +
                ", prcId=" + prcId +
                ", uid=" + uid +
                ", did=" + did +
                ", counter=" + counter +
                ", type='" + type + '\'' +
                ", rpid='" + rpid + '\'' +
                ", userid='" + userid + '\'' +
                ", username='" + username + '\'' +
                ", displayName='" + displayName + '\'' +
                ", credentialId='" + credentialId + '\'' +
                ", keyAlias='" + keyAlias + '\'' +
                ", keyOrigin='" + keyOrigin + '\'' +
                ", keySize='" + keySize + '\'' +
                ", seModule='" + seModule + '\'' +
                ", publicKey='" + publicKey + '\'' +
                ", keyAlgorithm='" + keyAlgorithm + '\'' +
                ", userHandle='" + userHandle + '\'' +
                ", authenticatorData='" + authenticatorData + '\'' +
                ", clientDataJson='" + clientDataJson + '\'' +
                ", cborAttestation='" + cborAttestation + '\'' +
                ", jsonAttestation='" + jsonAttestation + '\'' +
                ", createDate='" + createDate + '\'' +
                '}';
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
            .put("PublicKeyCredential", new JSONObject()
                .put("id", id)
                .put("prcId", prcId)
                .put("uid", uid)
                .put("did", did)
                .put("counter", counter)
                .put("type", type)
                .put("rpid", rpid)
                .put("userid", userid)
                .put("username", username)
                .put("displayName", displayName)
                .put("credentialId", credentialId)
                .put("keyAlias", keyAlias)
                .put("keyOrigin", keyOrigin)
                .put("keySize", keySize)
                .put("seModule", seModule)
                .put("publicKey", publicKey)
                .put("keyAlgorithm", keyAlgorithm)
                .put("userHandle", userHandle)
                .put("authenticatorData", authenticatorData)
                .put("clientDataJson", clientDataJson)
                .put("cborAttestation", cborAttestation)
                .put("jsonAttestation", jsonAttestation)
                .put("createDate", createDate));
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
