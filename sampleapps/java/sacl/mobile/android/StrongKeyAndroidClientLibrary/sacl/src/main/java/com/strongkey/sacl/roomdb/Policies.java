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
 * Entity class that represents security policies to be enforced by the SACL. The
 * expectation is that once an app has been installed using the SACL, and if the
 * device is approved for registration, a security policy is downloaded that will
 * be enforced by the SACL when used with the app that bundled the SACL.
 */

package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.strongkey.sacl.utilities.Common;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "policies")
public class Policies {

    @PrimaryKey
    @NonNull
    private Integer plid;

    @NonNull
    private String name;

    @NonNull
    private String version;

    @NonNull
    private Integer did;

    @NonNull
    private Long effective;

    @NonNull
    private Long installed;

    @NonNull
    private String source;

    /**
     * Constructors
     */
    public Policies() {}   // Empty object


    public Policies(@NonNull Integer plid, @NonNull String name, @NonNull String version,
                    @NonNull Integer did, @NonNull Long effective,
                    @NonNull Long installed, @NonNull String source) {
        this.plid = plid;
        this.name = name;
        this.version = version;
        this.did = did;
        this.effective = effective;
        this.installed = Common.now();
        this.source = source;
    }

    @NonNull
    public Integer getPlid() {
        return plid;
    }

    public void setPlid(@NonNull Integer plid) {
        this.plid = plid;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getVersion() {
        return version;
    }

    public void setVersion(@NonNull String version) {
        this.version = version;
    }

    @NonNull
    public Integer getDid() {
        return did;
    }

    public void setDid(@NonNull Integer did) {
        this.did = did;
    }

    @NonNull
    public Long getEffective() {
        return effective;
    }

    public Date getEffectiveFromLong() {
        return Common.dateFromLong(effective);
    }

    public void setEffective(@NonNull Long effective) {
        this.effective = effective;
    }

    @NonNull
    public Long getInstalled() {
        return installed;
    }

    public Date getInstalledFromLong() {
        return Common.dateFromLong(installed);
    }

    public void setInstalled(@NonNull Long installed) {
        this.installed = installed;
    }

    @NonNull
    public String getSource() {
        return source;
    }

    public void setSource(@NonNull String source) {
        this.source = source;
    }

    @Override
    public String toString() {
        return "Policies{" +
                "plid=" + plid +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", did='" + did + '\'' +
                ", effective=" + effective +
                ", installed=" + installed +
                ", source='" + source + '\'' +
                '}';
    }
}
