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
 * Asynchronous task to retrieve a Java object from the Room database
 */

package com.strongkey.sfaeco.asynctasks;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.roomdb.SaclSharedDataModel;

import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.SfaRepository;
import com.strongkey.sfaeco.roomdb.User;

public class RetrieveObjectTask extends AsyncTask<Object, Void, Object[]> {

    private final String TAG = RetrieveObjectTask.class.getSimpleName();

    private int did;
    private String objectType;
    private String searchKey;
    private String searchValue;
    private SfaSharedDataModel sfaSharedDataModel;
    private SaclSharedDataModel saclSharedDataModel;
    private Context context;

    @Override
    protected Object[] doInBackground(Object... params) {

        // Get parameters from function call
        did = (int) params[0];
        objectType = (String) params[1];
        searchKey = (String) params[2];
        searchValue = (String) params[3];
        sfaSharedDataModel = (SfaSharedDataModel) params[4];
        context = (Context) params[5];

        Log.d(TAG, "Parameters: did: " + did + '\n' +
                "objectType: " + objectType + '\n' +
                "searchKey: " + searchKey + '\n' +
                "searchValue: " + searchValue);

        // Get shared data models
        saclSharedDataModel = com.strongkey.sfaeco.utilities.Common.getSaclSharedDataModel();
        sfaSharedDataModel = com.strongkey.sfaeco.utilities.Common.getSfaSharedDataModel();

        // Get repositories
        SfaRepository sfaRepository = sfaSharedDataModel.getSfaRepository();
        SaclRepository saclRepository = Common.getRepository(new LocalContextWrapper(context));
        Log.d(TAG, "Retrieved SFA and SACL Repository");

        // Search and return response
        Object[] response = new Object[2];
        if (objectType.equalsIgnoreCase("User")) {

            User user;
            PublicKeyCredential publicKeyCredential = null;

            if (searchKey.equalsIgnoreCase("username")) {
                user = sfaRepository.getUserByUsername(did, searchValue);
                response[0] = user;
            } else if (searchKey.equalsIgnoreCase("uid")) {
                if (searchValue.equalsIgnoreCase("") || searchValue == null)
                    user = sfaRepository.getUserByMaxUid(did);
                else
                    user = sfaRepository.getUserByUid(did, Long.parseLong(searchValue));

                if (user != null) {
                    publicKeyCredential = saclRepository.getPublicKeyCredentialByUid(did, user.getUid());
                }

                response[0] = user;
                response[1] = publicKeyCredential;
            }
        }
        Log.i(TAG, "Found " + objectType + ": " + response[0]);
        return response;
    }

    // onPostExecute displays the results of the AsyncTask.
    @Override
    protected void onPostExecute(Object[] result) {
        if (result != null) {
            sfaSharedDataModel.setCurrentUser((User) result[0]);
            sfaSharedDataModel.setCurrentUserPublicKeyCredential((PublicKeyCredential) result[1]);
            saclSharedDataModel.setCurrentPublicKeyCredential((PublicKeyCredential) result[1]);
            Common.printVeryLongLogMessage("INFO",
                    "Set " + objectType + " to: " + result[0] + '\n' +
                            "Set SFA.PublicKeyCredential to: " + result[1] + '\n' +
                            "Set SACL.PublicKeyCredential to: " + result[1]);
        }
    }
}
