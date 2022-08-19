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
 * Data Access Object (DAO) interface to access the USERS table in the
 * local database on the device.
 */

package com.strongkey.sfaeco.roomdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SfaDao {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insertUser(User user);

    @Query("SELECT * FROM users")
    List<User> getAllUsers();

    @Query("SELECT * FROM users WHERE id = :id")
    User getUserById(int id);

    @Query("SELECT * FROM users WHERE did = :did AND uid = :uid")
    User getUserByUid(int did, long uid);

    @Query("SELECT * FROM users WHERE uid in (SELECT MAX(uid) FROM users WHERE did = :did)")
    User getUserByMaxUid(int did);

    @Query("SELECT * FROM users WHERE did = :did AND username = :username")
    User getUserByUsername(int did, String username);

    @Query("DELETE FROM users")
    int deleteAllUsers();

    @Query("DELETE FROM users WHERE did = :did AND uid = :uid")
    int deleteUser(int did, long uid);

    @Query("UPDATE users SET status = :status WHERE did = :did AND uid = :uid")
    int updateUserStatus(int did, long uid, String status);

}
