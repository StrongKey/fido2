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
 * Entity class that represents the intersection table of apps that use specific
 * security policies that govern the use of keys by apps.
 */
package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "app_policies",
        primaryKeys = {"applid", "appid", "plid"})

public class AppPolicies {

    @NonNull
    private Integer applid;

    @NonNull
    private Integer appid;

    @NonNull
    private Integer plid;

    /**
     * Constructors
     */
    public AppPolicies() {}   // Empty object


    public AppPolicies(@NonNull Integer applid, @NonNull Integer appid, @NonNull Integer plid) {
        this.applid = applid;
        this.appid = appid;
        this.plid = plid;
    }

    @NonNull
    public Integer getApplid() {
        return applid;
    }

    public void setApplid(@NonNull Integer applid) {
        this.applid = applid;
    }

    @NonNull
    public Integer getAppid() {
        return appid;
    }

    public void setAppid(@NonNull Integer appid) {
        this.appid = appid;
    }

    @NonNull
    public Integer getPlid() {
        return plid;
    }

    public void setPlid(@NonNull Integer plid) {
        this.plid = plid;
    }

    @Override
    public String toString() {
        return "AppPolicies{" +
                "applid=" + applid +
                ", appid=" + appid +
                ", plid=" + plid +
                '}';
    }
}
