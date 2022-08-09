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
 * The entity class for the JSON object returned by the FIDO2 server (through the
 * MDBA server) with the challenge, which is likely to look like the following:
 *
 * {
 *   "rp": {
 *     "name": "StrongKey FIDO2",
 *     "id": "strongkey.com"
 *   },
 *   "user": {
 *     "displayName": "Test",
 *     "id": "...",
 *     "name": "test"
 *   },
 *   "challenge": "...",
 *   "pubKeyCredParams": [
 *     {
 *       "type": "public-key",
 *       "alg": -7
 *     }, {
 *       "type": "public-key",
 *       "alg": -257
 *     }
 *   ],
 *   "attestation": "direct",
 *   "excludeCredentials": [
 *     {
 *       "id": "...",
 *       "type": "public-key",
 *       "transports": [
 *         "internal"
 *       ]
 *     }
 *   ],
 *   "authenticatorSelection": {
 *     "authenticatorAttachment": "platform",
 *     "requireResidentKey": true,
 *     "userVerification": "required"
 *   }
 * }
 *
 */

package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Date;

@Entity(tableName = "preregister_challenge",
        indices = { @Index(value = {"did", "rpid", "uid"}, unique = true),
                    @Index(value = {"did", "rpid", "userid"}, unique = true),
                    @Index(value = {"did", "rpid", "username"}, unique = true)})
public class PreregisterChallenge {

    // Not part of the FIDO2 specification - local to RoomDB
    @PrimaryKey(autoGenerate = true)
    public int id;

    // Not part of the FIDO2 specification - from the MDBA
    @NonNull
    private long uid;

    // Not part of the FIDO2 specification - from the MDBA
    @NonNull
    private int did;

    // Not part of the FIDO2 specification - local to RoomDB
    @NonNull
    private boolean challengeConsumed;

    /***** The "rp" JSON sub-object *****/

    @NonNull
    private String rpname;

    @NonNull
    private String rpid;


    /***** The "user" JSON sub-object *****/

    @NonNull
    private String username;

    @NonNull
    private String userid;

    @NonNull
    @ColumnInfo(name = "display_name")
    private String displayName;


    /***** Just two JSON attributes *****/

    @NonNull
    private String challenge;

    @NonNull
    @ColumnInfo(name = "attestation_conveyance")
    private String attestationConveyance;


    /***** The "pubKeyCredParams" JSON sub-object *****/

    @NonNull
    @ColumnInfo(name = "publickey_credential_params")
    private String publicKeyCredentialParams;

    @Ignore
    private JSONArray credParamsJSONArray;


    /***** The "excludeCredentials" JSON sub-object *****/

    // Nullable
    @ColumnInfo(name = "publickey_credential_descriptor")
    private String excludeCredentials;

    @Ignore
    private JSONArray excludeCredJSONArray;

    /***** The "authenticatorSelection" JSON sub-object *****/

    // Nullable
    @ColumnInfo(name = "authenticator_selection")
    private String authenticatorSelection;

    @Ignore
    private JSONObject authenticatorSelectionJSONObject;

    // Not part of the FIDO2 specification
    @ColumnInfo(name = "create_date")
    @NonNull
    private Long createDate;

    /**
     * Constructors
     */
    public PreregisterChallenge() {}   // Empty object

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

    public boolean isChallengeConsumed() {
        return challengeConsumed;
    }

    public void setChallengeConsumed(boolean challengeConsumed) {
        this.challengeConsumed = challengeConsumed;
    }

    public String getRpname() {
        return rpname;
    }

    public void setRpname(String rpname) {
        this.rpname = rpname;
    }

    public String getRpid() {
        return rpid;
    }

    public void setRpid(String rpid) {
        this.rpid = rpid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserid() {
        return userid;
    }

    public void setUserid(String userid) {
        this.userid = userid;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getPublicKeyCredentialParams() {
        return publicKeyCredentialParams;
    }

    public void setPublicKeyCredentialParams(String publicKeyCredentialParams) {
        this.publicKeyCredentialParams = publicKeyCredentialParams;
    }

    public JSONArray getCredParamsJSONArray() {
        return credParamsJSONArray;
    }

    public void setCredParamsJSONArray(JSONArray credParamsJSONArray) {
        this.credParamsJSONArray = credParamsJSONArray;
    }

    public String getExcludeCredentials() {
        return excludeCredentials;
    }

    public void setExcludeCredentials(String excludeCredentials) {
        this.excludeCredentials = excludeCredentials;
    }

    public JSONArray getExcludeCredJSONArray() {
        return excludeCredJSONArray;
    }

    public void setExcludeCredJSONArray(JSONArray excludeCredJSONArray) {
        this.excludeCredJSONArray = excludeCredJSONArray;
    }

    public String getAttestationConveyance() {
        return attestationConveyance;
    }

    public void setAttestationConveyance(String attestationConveyance) {
        this.attestationConveyance = attestationConveyance;
    }

    public String getAuthenticatorSelection() {
        return authenticatorSelection;
    }

    public void setAuthenticatorSelection(String authenticatorSelection) {
        this.authenticatorSelection = authenticatorSelection;
    }

    public JSONObject getAuthenticatorSelectionJSONObject() {
        return authenticatorSelectionJSONObject;
    }

    public void setAuthenticatorSelectionJSONObject(JSONObject authenticatorSelectionJSONObject) {
        this.authenticatorSelectionJSONObject = authenticatorSelectionJSONObject;
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

    // attestationConveyance actually shows up as "attestation" in the specification
    @Override
    public String toString() {
        return "PreregisterChallenge {\n" +
                "  id='" + id + '\'' +
                ",\n  did='" + did + '\'' +
                ",\n  uid='" + uid + '\'' +
                ",\n  rpname='" + rpname + '\'' +
                ",\n  rpid='" + rpid + '\'' +
                ",\n  username='" + username + '\'' +
                ",\n  userid='" + userid + '\'' +
                ",\n  displayName='" + displayName + '\'' +
                ",\n  challenge='" + challenge + '\'' +
                ",\n  attestationConveyance='" + attestationConveyance + '\'' +
                ",\n  publicKeyCredentialParams='" + publicKeyCredentialParams + '\'' +
                ",\n  excludeCredentials='" + excludeCredentials + '\'' +
                ",\n  authenticatorSelection='" + authenticatorSelection + '\'' +
                ",\n  challengeConsumed='" + challengeConsumed + '\'' +
                ",\n  createDate='" + getCreateDateFromLong() + '\'' + "\n}";
    }


    /**
     * Inner class for publicKeyCredentialParams - looks like the following in the preregister
     * challenge object:
     *
     * "pubKeyCredParams": [
     *     {
     *       "type": "public-key",
     *       "alg": -7
     *     }, {
     *       "type": "public-key",
     *       "alg": -257
     *     }
     *   ]
     */
    public class PublicKeyCredentialParam {
        private String type;
        private int alg;

        /**
         * Constructor for publicKeyCredentialParams - fixed values for SACL
         * @return
         */
        public PublicKeyCredentialParam() {
            this.type = "public-key";
            this.alg = -7;              // Only other option SACL supports is -257 (RSA256)
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public int getAlg() {
            return alg;
        }

        public void setAlg(int alg) {
            this.alg = alg;
        }
    }


    /**
     * Inner class for PublicKeyCredentialDescriptor used by excludeCredentials option.
     * It may be empty, which implies this is the first challenge for that userid at that RP
     *
     * Nullable - it looks like the following when present
     *
     * "excludeCredentials": [
     *     {
     *       "id": "...",
     *       "type": "public-key",
     *       "transports": [
     *         "internal"
     *       ]
     *     }
     *   ]
     */
    public class PublicKeyCredentialDescriptor {
        private String type;
        private String id;
        private String[] transports;

        // Constructor for PublicKeyCredentialDescriptor - fixed values for SACL
        public PublicKeyCredentialDescriptor() {
            this.type = "public-key";
            this.transports = new String[]{"internal"};
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String[] getTransports() {
            return transports;
        }

        public void setTransports(String[] transports) {
            this.transports = transports;
        }
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
