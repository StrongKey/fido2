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
 * The entity class for the JSON object returned by the FIDO2 server (through the
 * business application) with the transaction authorization challenge, which looks
 * like the following:
 *
 *
 *   {
 *      "Response": {
 *          "rpid": "strongkey.com",
 *          "txdata": "Iu1Wy2iFmw........H277bGIu1WH2iFmwAyoQrldt",
 *          "challenge": "-DK8TOVK6WoVNMHsKJiv9w",
 *          "allowCredentials": [
 *             {
 *               "type": "public-key",
 *               "id": "Pu-8s87LButAPesi....BLWd5RM9zZtLoQ4-T2zRQ",
 *               "alg": -7
 *             }
 *          ]
 *      }
 *   }
 *
 */

package com.strongkey.sacl.roomdb;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;
import java.util.Iterator;

@Entity(tableName = "preauthorize_challenge",
        indices = { @Index(value = {"did", "rpid", "uid", "challenge"}, unique = true),
                    @Index(value = {"did", "rpid", "challenge"}, unique = true)})

public class PreauthorizeChallenge {

    @Ignore
    public String TAG = "PreauthorizeChallenge";

    // Not part of the FIDO2 specification - local to RoomDB
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Not part of the FIDO2 specification
    @NonNull
    private long uid;

    // Not part of the FIDO2 specification
    @NonNull
    private int did;

    // Not part of the FIDO2 specification
    @NonNull
    private boolean challengeConsumed;

    /***** The "rp" JSON sub-object *****/

    @NonNull
    private String rpid;

    /***** The "tx" JSON sub-object *****/

    @NonNull
    private String txid;

    // Base64Url encoded transaction data in application-specific format
    @NonNull
    private String txpayload;

    @NonNull
    private String challenge;

    /***** The "allowCredentials" JSON sub-object *****/

    @Nullable
    @ColumnInfo(name = "allow_credentials")
    private String allowCredentials;

    @Ignore
    private JSONArray allowCredentialsJSONArray;

    // Not part of the FIDO2 specification
    @ColumnInfo(name = "create_date")
    @NonNull
    private Long createDate;

    /**
     * Constructors
     */
    public PreauthorizeChallenge() {}   // Empty object

    /**
     * Getter & Setter methods for PreregisterChallenge
     */
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }

    @NonNull
    public String getTxid() { return txid; }

    public void setTxid(@NonNull String txid) { this.txid = txid; }

    @NonNull
    public String getTxpayload() {
        return txpayload;
    }

    public void setTxpayload(@NonNull String txpayload) {
        this.txpayload = txpayload;
    }

    public boolean isChallengeConsumed() {
        return challengeConsumed;
    }

    public void setChallengeConsumed(boolean challengeConsumed) {
        this.challengeConsumed = challengeConsumed;
    }

    public String getRpid() {
        return rpid;
    }

    public void setRpid(String rpid) {
        this.rpid = rpid;
    }

       public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    @Nullable
    public String getAllowCredentials() {
        return allowCredentials;
    }

    public void setAllowCredentials(@Nullable String allowCredentials) {
        this.allowCredentials = allowCredentials;
    }

    public JSONArray getAllowCredentialsJSONArray() {
        return allowCredentialsJSONArray;
    }

    public void setAllowCredentialsJSONArray(JSONArray allowCredentialsJSONArray) {
        this.allowCredentialsJSONArray = allowCredentialsJSONArray;
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
        return "PreauthorizeChallenge {\n" +
                "  id='" + id + '\'' +
                ",\n  did='" + did + '\'' +
                ",\n  uid='" + uid + '\'' +
                ",\n  rpid='" + rpid + '\'' +
                ",\n  txid='" + txid + '\'' +
                ",\n  txpayload='" + txpayload + '\'' +
                ",\n  challenge='" + challenge + '\'' +
                ",\n  allowCredentials='" + allowCredentials + '\'' +
                ",\n  challengeConsumed='" + challengeConsumed + '\'' +
                ",\n  createDate='" + getCreateDateFromLong() + '\'' + "\n}";
    }

    public JSONObject toJSON() throws JSONException {
        return new JSONObject()
                .put("PreauthorizeChallenge", new JSONObject()
                        .put("uid", uid)
                        .put("did", did)
                        .put("rpid", rpid)
                        .put("txid", txid)
                        .put("txpayload", txpayload)
                        .put("challenge", challenge)
                        .put("allowCredentials", allowCredentials));
    }

    public boolean parsePreauthorizeChallengeResponse(String response) {

        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject responseJson = jsonObject.getJSONObject(Constants.JSON_KEY_PREAUTH_RESPONSE);
            Iterator<String> jsonkeys = responseJson.keys();
            while (jsonkeys.hasNext()) {
                switch (jsonkeys.next()) {
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_RELYING_PARTY_ID:
                        setRpid(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_RELYING_PARTY_ID));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_TXID:
                        setTxid(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_TXID));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_TXPAYLOAD:
                        setRpid(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_TXPAYLOAD));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_CHALLENGE:
                        setChallenge(responseJson.getString(Constants.JSON_KEY_PREAUTH_RESPONSE_CHALLENGE));
                        break;
                    case Constants.JSON_KEY_PREAUTH_RESPONSE_ALLOW_CREDENTIALS:
                        JSONArray allowcred = responseJson.getJSONArray(Constants.JSON_KEY_PREAUTH_RESPONSE_ALLOW_CREDENTIALS);
                        setAllowCredentialsJSONArray(allowcred);
                        setAllowCredentials(allowcred.toString());
                        break;
                    default:
                        break;
                }
            }

            // Set id to 0 so it gets auto-filled in DB
            setId(0);
            setCreateDate(Common.now());
            Log.d(TAG, "PreauthorizeChallenge:\n" + toString());
            return true;

        } catch (JSONException ex) {
            ex.printStackTrace();
        }
        return false;
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
