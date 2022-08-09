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
 * Entity class that represents Apps in the SACL. Since the SACL works below the
 * app, it needs to manage access to the Room database and the objects it holds
 * based on which app is requesting access to the database.
 */

package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import com.strongkey.sacl.utilities.Common;

import java.sql.Timestamp;
import java.util.Date;

@Entity(tableName = "apps",
    primaryKeys = {"appid"},
    indices = { @Index(value = {"appkg"}, unique = true),
                @Index(value = {"origin"}, unique = true)})

public class Apps {

    @NonNull
    private Integer appid;

    @NonNull
    private String appkg;

    @NonNull
    private String origin;

    @NonNull
    private Integer did;

    @NonNull
    private String name;

    @NonNull
    private String version;

    @NonNull
    private Long installed;

    private Long updated;

    /**
     * Constructors
     */
    public Apps() {}   // Empty object


    public Apps(@NonNull Integer appid, @NonNull String appkg, @NonNull String origin,
                @NonNull Integer did, @NonNull String name, @NonNull String version) {
        this.appid = appid;
        this.appkg = appkg;
        this.origin = origin;
        this.did = did;
        this.name = name;
        this.version = version;
        this.installed = Common.now();
    }

    @NonNull
    public Integer getAppid() {
        return appid;
    }

    public void setAppid(@NonNull Integer appid) {
        this.appid = appid;
    }

    @NonNull
    public String getAppkg() {
        return appkg;
    }

    public void setAppkg(@NonNull String appkg) {
        this.appkg = appkg;
    }

    @NonNull
    public String getOrigin() {
        return origin;
    }

    public void setOrigin(@NonNull String origin) {
        this.origin = origin;
    }

    @NonNull
    public Integer getDid() {
        return did;
    }

    public void setDid(@NonNull Integer did) {
        this.did = did;
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
    public Long getInstalled() {
        return installed;
    }

    public Date getInstalledFromLong() {
        return Common.dateFromLong(installed);
    }

    public void setInstalled(@NonNull Long installed) {
        this.installed = installed;
    }

    public Long getUpdated() {
        return updated;
    }

    public Date getUpdatedFromLong() {
        return Common.dateFromLong(updated);
    }

    public void setUpdated(Long updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        return "Apps{" +
                "appid=" + appid +
                ", appkg='" + appkg + '\'' +
                ", origin='" + origin + '\'' +
                ", did='" + did + '\'' +
                ", name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", installed=" + installed +
                ", updated=" + updated +
                '}';
    }
}
