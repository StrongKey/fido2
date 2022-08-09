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
 * 888b    888          888
 * 8888b   888          888
 * 88888b  888          888
 * 888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 * 888 Y88b888 d88""88b 888    d8P  Y8b 88K
 * 888  Y88888 888  888 888    88888888 "Y8888b.
 * 888   Y8888 Y88..88P Y88b.  Y8b.          X88
 * 888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * Entity class that describes the detail of a security policy.
 */

package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;

import com.strongkey.sacl.utilities.Common.AllowDigest;
import com.strongkey.sacl.utilities.Common.AllowFidoPurpose;
import com.strongkey.sacl.utilities.Common.AllowHmac;
import com.strongkey.sacl.utilities.Common.AllowKeyPurpose;
import com.strongkey.sacl.utilities.Common.AllowKeypairPurpose;
import com.strongkey.sacl.utilities.Common.AllowMode;
import com.strongkey.sacl.utilities.Common.AllowPadding;
import com.strongkey.sacl.utilities.Common.KeyType;
import com.strongkey.sacl.utilities.Common.RequireAuth;
import com.strongkey.sacl.utilities.Common.RequireSecurity;

@Entity(tableName = "policy_detail",
        primaryKeys = {"plid", "pldid"})

public class PolicyDetail {

    @NonNull
    private Integer plid;

    @NonNull
    private Integer pldid;

    private String keyClass;

    @NonNull
    private KeyType keyType;

    @ColumnInfo(name = "require_auth")
    private RequireAuth requireAuth;

    @ColumnInfo(name = "require_security")
    private RequireSecurity requireSecurity;

    @ColumnInfo(name = "allow_mode")
    private AllowMode allowMode;

    @ColumnInfo(name = "allow_padding")
    private AllowPadding allowPadding;

    @ColumnInfo(name = "allow_digest")
    private AllowDigest allowDigest;

    @ColumnInfo(name = "allow_hmac")
    private AllowHmac allowHmac;

    @ColumnInfo(name = "allow_key_purpose")
    private AllowKeyPurpose allowKeyPurpose;

    @ColumnInfo(name = "allow_keypair_purpose")
    private AllowKeypairPurpose allowKeypairPurpose;

    @ColumnInfo(name = "allow_fido_purpose")
    private AllowFidoPurpose allowFidoPurpose;


    /**
     * Constructors
     */
    public PolicyDetail() {}    // Empty object

    public PolicyDetail(@NonNull Integer plid, @NonNull Integer pldid,
                        String keyClass, @NonNull KeyType keyType,
                        RequireAuth requireAuth,
                        RequireSecurity requireSecurity,
                        AllowMode allowMode,
                        AllowPadding allowPadding,
                        AllowDigest allowDigest,
                        AllowHmac allowHmac,
                        AllowKeyPurpose allowKeyPurpose,
                        AllowKeypairPurpose allowKeypairPurpose,
                        AllowFidoPurpose allowFidoPurpose)
    {
        this.plid = plid;
        this.pldid = pldid;
        this.keyClass = keyClass;
        this.keyType = keyType;
        this.requireAuth = requireAuth;
        this.requireSecurity = requireSecurity;
        this.allowMode = allowMode;
        this.allowPadding = allowPadding;
        this.allowDigest = allowDigest;
        this.allowHmac = allowHmac;
        this.allowKeyPurpose = allowKeyPurpose;
        this.allowKeypairPurpose = allowKeypairPurpose;
        this.allowFidoPurpose = allowFidoPurpose;
    }

    @NonNull
    public Integer getPlid() {
        return plid;
    }

    public void setPlid(@NonNull Integer plid) {
        this.plid = plid;
    }

    @NonNull
    public Integer getPldid() {
        return pldid;
    }

    public void setPldid(@NonNull Integer pldid) {
        this.pldid = pldid;
    }

    public String getKeyClass() {
        return keyClass;
    }

    public void setKeyClass(String keyClass) {
        this.keyClass = keyClass;
    }

    @NonNull
    public KeyType getKeyType() {
        return keyType;
    }

    public void setKeyType(@NonNull KeyType keyType) {
        this.keyType = keyType;
    }

    public RequireAuth getRequireAuth() {
        return requireAuth;
    }

    public void setRequireAuth(RequireAuth requireAuth) {
        this.requireAuth = requireAuth;
    }

    public RequireSecurity getRequireSecurity() {
        return requireSecurity;
    }

    public void setRequireSecurity(RequireSecurity requireSecurity) {
        this.requireSecurity = requireSecurity;
    }

    public AllowMode getAllowMode() {
        return allowMode;
    }

    public void setAllowMode(AllowMode allowMode) {
        this.allowMode = allowMode;
    }

    public AllowPadding getAllowPadding() {
        return allowPadding;
    }

    public void setAllowPadding(AllowPadding allowPadding) {
        this.allowPadding = allowPadding;
    }

    public AllowDigest getAllowDigest() {
        return allowDigest;
    }

    public void setAllowDigest(AllowDigest allowDigest) {
        this.allowDigest = allowDigest;
    }

    public AllowHmac getAllowHmac() {
        return allowHmac;
    }

    public void setAllowHmac(AllowHmac allowHmac) {
        this.allowHmac = allowHmac;
    }

    public AllowKeyPurpose getAllowKeyPurpose() {
        return allowKeyPurpose;
    }

    public void setAllowKeyPurpose(AllowKeyPurpose allowKeyPurpose) {
        this.allowKeyPurpose = allowKeyPurpose;
    }

    public AllowKeypairPurpose getAllowKeypairPurpose() {
        return allowKeypairPurpose;
    }

    public void setAllowKeypairPurpose(AllowKeypairPurpose allowKeypairPurpose) {
        this.allowKeypairPurpose = allowKeypairPurpose;
    }

    public AllowFidoPurpose getAllowFidoPurpose() {
        return allowFidoPurpose;
    }

    public void setAllowFidoPurpose(AllowFidoPurpose allowFidoPurpose) {
        this.allowFidoPurpose = allowFidoPurpose;
    }

    @Override
    public String toString() {
        return "PolicyDetail{" +
                "plid=" + plid +
                ", pldid=" + pldid +
                ", keyClass='" + keyClass + '\'' +
                ", keyType=" + keyType +
                ", requireAuth=" + requireAuth +
                ", requireSecurity=" + requireSecurity +
                ", allowMode=" + allowMode +
                ", allowPadding=" + allowPadding +
                ", allowDigest=" + allowDigest +
                ", allowHmac=" + allowHmac +
                ", allowKeyPurpose=" + allowKeyPurpose +
                ", allowKeypairPurpose=" + allowKeypairPurpose +
                ", allowFidoPurpose=" + allowFidoPurpose +
                '}';
    }
}
