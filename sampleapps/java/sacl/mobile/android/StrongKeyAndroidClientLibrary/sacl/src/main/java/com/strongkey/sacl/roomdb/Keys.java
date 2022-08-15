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
 * Entity class that represents cryptographic keys in the SACL.
 */

package com.strongkey.sacl.roomdb;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Index;

import com.strongkey.sacl.utilities.Common.KeyStatus;
import com.strongkey.sacl.utilities.Common.KeyType;

import java.sql.Timestamp;

@Entity(tableName = "keys",
    primaryKeys = {"appid", "keyid"},
    indices = { @Index(value = {"alias"}, unique = true)})

public class Keys {

    @NonNull
    private Integer appid;

    @NonNull
    private Integer keyid;

    @NonNull
    private String alias;

    @NonNull
    private KeyType keytype;

    @NonNull
    private Integer counter;

    @NonNull
    private Timestamp created;

    @NonNull
    private KeyStatus status;

    private Timestamp expires;

    private Timestamp lastused;


}
