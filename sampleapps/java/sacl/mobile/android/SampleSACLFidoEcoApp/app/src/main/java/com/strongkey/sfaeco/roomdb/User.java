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
 * Entity class that represents Users in the MDBA. Server-side table
 * has more attributes (columns) that are not relevant on the device.
 */

package com.strongkey.sfaeco.roomdb;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.sql.Date;
import java.util.Iterator;

@Entity(tableName = "users",
indices = { @Index(value = {"did", "sid", "uid"}, unique = true),
            @Index(value = {"did", "username"}, unique = true),
            @Index(value = {"did", "email_address"}, unique = true),
            @Index(value = {"did", "user_mobile_number"},unique = true)})
public class User {

    @Ignore
    final String TAG = "User";

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @NonNull
    private int  did;

    @NonNull
    private int sid;

    @NonNull
    private long uid;

    @NonNull
    private String username;

    @NonNull
    private String password;

    // Nullable
    private String credentialId;

    @NonNull
    @ColumnInfo(name = "given_name")
    private String givenName;

    @NonNull
    @ColumnInfo(name = "family_name")
    private String familyName;

    @NonNull
    @ColumnInfo(name = "email_address")
    private String email;

    @NonNull
    @ColumnInfo(name = "user_mobile_number")
    private String userMobileNumber;

    // Nullable
    @ColumnInfo(name = "enrollment_date")
    private Long enrollmentDate;

    @NonNull
    @ColumnInfo(name = "status")
    private String status;

    @NonNull
    @ColumnInfo(name = "create_date")
    private Long createDate;

    /**
     * Constructors
     */
    public User() {}   // Empty object

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

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    @NonNull
    public String getUsername() {
        return username;
    }

    public void setUsername(@NonNull String username) {
        this.username = username;
    }

    @NonNull
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    public String getCredentialId() {
        return credentialId;
    }

    public void setCredentialId(String credentialId) {
        this.credentialId = credentialId;
    }

    @NonNull
    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(@NonNull String givenName) {
        this.givenName = givenName;
    }

    @NonNull
    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(@NonNull String familyName) {
        this.familyName = familyName;
    }

    @NonNull
    public String getEmail() {
        return email;
    }

    public void setEmail(@NonNull String email) {
        this.email = email;
    }

    @NonNull
    public String getUserMobileNumber() {
        return userMobileNumber;
    }

    public void setUserMobileNumber(@NonNull String userMobileNumber) {
        this.userMobileNumber = userMobileNumber;
    }

    public Long getEnrollmentDate() {
        return enrollmentDate;
    }

    public java.util.Date getEnrollmentDateFromLong() {
        return fromTimestamp(enrollmentDate);
    }

    public void setEnrollmentDate(Long enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
    }

    public void setEnrollmentDate(@NonNull java.util.Date enrollmentDate) {
        this.enrollmentDate = dateToTimestamp(enrollmentDate);
    }

    @NonNull
    public Common.UserStatus getStatusFromEnum() {
        return Common.UserStatus.valueOf(status);
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull Common.UserStatus status) {
        this.status = status.toString();
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    @NonNull
    public Long getCreateDate() {
        return createDate;
    }

    public java.util.Date getCreateDateFromLong() {
        return fromTimestamp(createDate);
    }

    public void setCreateDate(@NonNull Long createDate) {
        this.createDate = createDate;
    }

    public void setCreateDate(@NonNull java.util.Date createDate) {
        this.createDate = dateToTimestamp(createDate);
    }

    public String toString() {
        return  "id: " + this.id + ", " +
                "did: " + this.did + ", " +
                "sid: " + this.sid + ", " +
                "uid: " + this.uid + ", " +
                "username: " + this.username + ", " +
                "givenName: " + this.givenName + ", " +
                "familyName: " + this.familyName + ", " +
                "email: " + this.email + ", " +
                "userMobileNumber: " + this.userMobileNumber + ", " +
                "credentialId: " + this.credentialId + ", " +
                "status: " + this.status + ", " +
                "enrollmentDate: " + this.enrollmentDate + ", " +
                "createDate: " + this.createDate;
    }

    /**
     * Parse the webservice response from the MDBA, populate the User object
     * and return a boolean response. Since createDate is generally the last
     * element of the JSON response, we set a flag in that switch-case to
     * return a true response
     * @param response String containing a JSONObject of the registered user
     * @return boolean
     */
    public boolean parseUserJsonString(String response) {
        boolean parsedSuccessfully = false;
        try {
            JSONObject jsonObject = new JSONObject(response);
            JSONObject jsonUser = jsonObject.getJSONObject(SfaConstants.JSON_KEY_USER);
            Iterator<String> jsonkeys = jsonUser.keys();
            while (jsonkeys.hasNext()) {
                String jsonkey = jsonkeys.next();
                switch (jsonkey) {
                    case SfaConstants.JSON_KEY_DID:
                        setDid(jsonUser.getInt(SfaConstants.JSON_KEY_DID));
                        break;
                    case SfaConstants.JSON_KEY_SID:
                        setSid(jsonUser.getInt(SfaConstants.JSON_KEY_SID));
                        break;
                    case SfaConstants.JSON_KEY_UID:
                        setUid(jsonUser.getLong(SfaConstants.JSON_KEY_UID));
                        break;
                    case SfaConstants.JSON_KEY_USER_USERNAME:
                        setUsername(jsonUser.getString(SfaConstants.JSON_KEY_USER_USERNAME));
                        break;
                    case SfaConstants.JSON_KEY_USER_PASSWORD:
                        setPassword(jsonUser.getString(SfaConstants.JSON_KEY_USER_PASSWORD));
                        break;
                    case SfaConstants.JSON_KEY_USER_GIVEN_NAME:
                        setGivenName(jsonUser.getString(SfaConstants.JSON_KEY_USER_GIVEN_NAME));
                        break;
                    case SfaConstants.JSON_KEY_USER_FAMILY_NAME:
                        setFamilyName(jsonUser.getString(SfaConstants.JSON_KEY_USER_FAMILY_NAME));
                        break;
                    case SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS:
                        setEmail(jsonUser.getString(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS));
                        break;
                    case SfaConstants.JSON_KEY_USER_MOBILE_NUMBER:
                        setUserMobileNumber(jsonUser.getString(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER));
                        break;
                    case SfaConstants.JSON_KEY_USER_STATUS:
                        String s = jsonUser.getString(SfaConstants.JSON_KEY_USER_STATUS);
                        setStatus(Common.UserStatus.valueOf(s));
                        break;
                    case SfaConstants.JSON_KEY_USER_CREATE_DATE:
                        setCreateDate(Date.valueOf(jsonUser.getString(SfaConstants.JSON_KEY_USER_CREATE_DATE).substring(0, 10)));
                        parsedSuccessfully = true;
                        break;
                    default:
                        break;
                }
            }
            // Need to set ID to 0 to save it
            setId(0);
            Log.v(TAG, "Parsed USER object: " + toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return parsedSuccessfully;
    }

    @TypeConverter
    public java.util.Date fromTimestamp(Long value) {
        return value == null ? null : new java.util.Date(value);
    }

    @TypeConverter
    public Long dateToTimestamp(java.util.Date date) {
        if (date == null) {
            return null;
        } else {
            return date.getTime();
        }
    }
}
