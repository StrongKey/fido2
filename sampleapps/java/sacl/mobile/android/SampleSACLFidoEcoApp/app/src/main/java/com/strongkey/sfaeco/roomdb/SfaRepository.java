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
 * Repository class to interact with the local and remote SFA databases to keep
 * them synchronized (in case there is no network access to the SFA server).
 */

package com.strongkey.sfaeco.roomdb;

import android.app.Application;
import android.util.Log;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class SfaRepository {

    private final String TAG = "SfaRepository";

    private SfaDao mSfaDao;
    private List<User> mAllUsers;
    private List<UserDevice> mAllUserDevices;

    /**
     * Constructor - important to note that the mAll* variables only have
     * the list of all objects as of the moment the constructor is called -
     * any changes to the database - specifically insertions and deletions -
     * are not likely to be reflected. mAll* variables should be updated
     * periodically to keep them current if necessary.
     *
     * @param application
     */
    public SfaRepository(Application application) {
        SfaRoomDatabase database = SfaRoomDatabase.getDatabase(application);
        mSfaDao = database.sfaDao();
        mAllUsers = mSfaDao.getAllUsers();
    }

    /**
     * INSERT USER
     */
    public int insertUser(User user) {
        AtomicInteger rowId = new AtomicInteger(0);
        SfaRoomDatabase.databaseWriteExecutor.execute(() -> {
            Long l = mSfaDao.insertUser(user);
            rowId.set(l.intValue());
            Log.i(TAG, "Inserted User with ID-DID-UID-USERNAME: " + rowId.get() + "-"
                            + user.getDid() + "-" + user.getUid() + "-" + user.getUsername());
        });
        return rowId.get();
    }

    /**
     * GET ALL USER
     */
    public List<User> getAllUsers() {
        return mAllUsers;
    }

    /**
     * GET USER BY UID
     */
    public User getUserByUid(int did, long uid) {
        return mSfaDao.getUserByUid(did, uid);
    }

    /**
     * GET USER BY max(UID)
     */
    public User getUserByMaxUid(int did) {
        return mSfaDao.getUserByMaxUid(did);
    }

    /**
     * GET USER BY USERNAME
     */
    public User getUserByUsername(int did, String username) {
        return mSfaDao.getUserByUsername(did, username);
    }

    /**
     * GET USER BY ROW ID
     */
    public User getUserById(int id) {
        return mSfaDao.getUserById(id);
    }

}
