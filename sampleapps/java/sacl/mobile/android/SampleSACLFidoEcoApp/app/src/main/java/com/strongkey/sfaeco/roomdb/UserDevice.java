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
 * Entity class that represents User Devices used by the User within the MDBA.
 * Server-side table has more attributes that are not relevant on the device.
 */

package com.strongkey.sfaeco.roomdb;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.Date;

@Entity(tableName = "user_devices",
        indices = {
            @Index(value = {"did", "uid", "device_mobile_number"}, unique = true),
            @Index(value = {"did", "sid", "uid", "devid"}, unique = true)})

public class UserDevice {

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @NonNull
    private Integer did;

    @NonNull
    private Integer sid;

    @NonNull
    private Long uid;

    @NonNull
    private Long devid;

    // Nullable
    private Long rdid;

    // Nullable
    @ColumnInfo(name = "device_mobile_number")
    private String deviceMobileNumber;

    // Nullable
    private String manufacturer;

    // Nullable
    private String model;

    // Nullable
    private String fingerprint;

    // Nullable
    @ColumnInfo(name = "os_release")
    private String osRelease;

    // Nullable
    @ColumnInfo(name = "os_sdk_number")
    private Integer osSdkNumber;

    @NonNull
    @ColumnInfo(name = "status")
    private String status;

    @NonNull
    @ColumnInfo(name = "create_date")
    private Long createDate;

    // Nullable
    private String notes;

    /**
     * Constructors
     */
    public UserDevice() {}   // Empty object

    /**
     * Getters and Setters
     */
    @NonNull
    public int getId() {
        return id;
    }

    public void setId(@NonNull int id) {
        this.id = id;
    }

    @NonNull
    public Integer getDid() {
        return did;
    }

    public void setDid(@NonNull Integer did) {
        this.did = did;
    }

    @NonNull
    public Integer getSid() {
        return sid;
    }

    public void setSid(@NonNull Integer sid) {
        this.sid = sid;
    }

    @NonNull
    public Long getUid() {
        return uid;
    }

    public void setUid(@NonNull Long uid) {
        this.uid = uid;
    }

    @NonNull
    public Long getDevid() {
        return devid;
    }

    public void setDevid(@NonNull Long devid) {
        this.devid = devid;
    }

    public Long getRdid() {
        return rdid;
    }

    public void setRdid(Long rdid) {
        this.rdid = rdid;
    }

    @NonNull
    public String getDeviceMobileNumber() {
        return deviceMobileNumber;
    }

    public void setDeviceMobileNumber(String deviceMobileNumber) {
        this.deviceMobileNumber = deviceMobileNumber;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getFingerprint() {
        return fingerprint;
    }

    public void setFingerprint(String fingerprint) {
        this.fingerprint = fingerprint;
    }

    public String getOsRelease() {
        return osRelease;
    }

    public void setOsRelease(String osRelease) {
        this.osRelease = osRelease;
    }

    public Integer getOsSdkNumber() {
        return osSdkNumber;
    }

    public void setOsSdkNumber(Integer osSdkNumber) {
        this.osSdkNumber = osSdkNumber;
    }

    @NonNull
    public String getStatus() {
        return status;
    }

    public void setStatus(@NonNull String status) {
        this.status = status;
    }

    public void setCreateDate(@NonNull Long createDate) {
        this.createDate = createDate;
    }

    @NonNull
    public Long getCreateDate() {
        return createDate;
    }

    @NonNull
    public Date getCreateUtilDate() {
        return fromTimestamp(createDate);
    }

    public void setCreateDate(@NonNull Date createDate) {
        this.createDate = dateToTimestamp(createDate);
    }

    @NonNull
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String toString() {
        return  "id: " + this.id + ", " +
                "did: " + this.did + ", " +
                "sid: " + this.sid + ", " +
                "uid: " + this.uid + ", " +
                "devid: " + this.devid + ", " +
                "rdid: " + this.rdid + ", " +
                "deviceMobileNumber: " + this.deviceMobileNumber + ", " +
                "manufacturer: " + this.manufacturer + ", " +
                "model: " + this.model + ", " +
                "fingerprint: " + this.fingerprint + ", " +
                "status: " + this.status + ", " +
                "createDate: " + this.createDate + ", " +
                "notes: " + this.notes;
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
