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
 * Asynchronous task to open up the SFA Repository
 */

package com.strongkey.sfaeco.main;

import android.app.Application;
import android.os.AsyncTask;
import android.util.Log;

import com.strongkey.sfaeco.roomdb.SfaRepository;
import com.strongkey.sfaeco.roomdb.User;

import java.util.List;

class InitializeSfaRepositoryTask extends AsyncTask<Object, Void, Void> {

    private final String TAG = InitializeSfaRepositoryTask.class.getSimpleName();

    @Override
    protected Void doInBackground(Object... params) {

        // Get parameters from function call
        Application application = (Application) params[0];
        SfaSharedDataModel sfaSharedDataModel = (SfaSharedDataModel) params[1];

        // Initialize SFA repository and print out records to log
        SfaRepository sfaRepository = new SfaRepository(application);
        sfaSharedDataModel.setSfaRepository(sfaRepository);
        Log.v(TAG, "Initialized SFA Repository...");

        List<User> mAllUsers = sfaRepository.getAllUsers();
        Log.v(TAG, "All Users: \n");
        for (User u : mAllUsers) {
            Log.d(TAG, u.toString());
        }
        return null;
    }
}
