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
 * Copyright (c) 2001-2020 StrongAuth, Inc. (d/b/a StrongKey)
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
 *
 *   {
 *      "Response": {
 *          "challenge": "-DK8TOVK6WoVNMHsKJiv9w",
 *          "allowCredentials": [
 *             {
 *               "type": "public-key",
 *               "id": "Pu-8s87LButAPesi....BLWdHH277bGIu1Wy2iFmwA5RM9zZcCzG1oQrldtLoQ4-T2zRQ",
 *               "alg": -7
 *             }
 *          ],
 *          "rpId": "strongkey.com"
 *      }
 *   }
 *
 */

package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import org.json.JSONArray;

import java.util.Date;

@Entity(tableName = "preauthenticate_challenge",
        indices = { @Index(value = {"did", "rpid", "uid", "challenge"}, unique = true),
                    @Index(value = {"did", "rpid", "challenge"}, unique = true)})
public class PreauthenticateChallenge {

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
    private String rpid;

    /***** Just one JSON attribute *****/

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
    public PreauthenticateChallenge() {}   // Empty object

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
        return "PreauthenticateChallenge {\n" +
                "  id='" + id + '\'' +
                ",\n  did='" + did + '\'' +
                ",\n  uid='" + uid + '\'' +
                ",\n  rpid='" + rpid + '\'' +
                ",\n  challenge='" + challenge + '\'' +
                ",\n  allowCredentials='" + allowCredentials + '\'' +
                ",\n  challengeConsumed='" + challengeConsumed + '\'' +
                ",\n  createDate='" + getCreateDateFromLong() + '\'' + "\n}";
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
