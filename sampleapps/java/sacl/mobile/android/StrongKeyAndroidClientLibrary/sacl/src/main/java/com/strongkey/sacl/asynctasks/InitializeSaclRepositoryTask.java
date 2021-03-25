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
 * Asynchronous task to open up SACL Repository
 */

package com.strongkey.sacl.asynctasks;

import android.util.Log;

import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.PreauthenticateChallenge;
import com.strongkey.sacl.roomdb.PreregisterChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;

import java.util.List;

public class InitializeSaclRepositoryTask implements Runnable {

    private final String TAG = InitializeSaclRepositoryTask.class.getSimpleName();
    private final LocalContextWrapper context;

    public InitializeSaclRepositoryTask(LocalContextWrapper context) {
        this.context = context;
    }

    @Override
    public void run() {
        // Initialize SACL repository and print out records to log
        SaclRepository saclRepository = new SaclRepository(context);
        Common.setCurrentObject(Constants.SACL_OBJECT_TYPES.SACL_REPOSITORY, saclRepository);

//        List<PreregisterChallenge> mAllPreregisterChallenges = saclRepository.getAllPreregisterChallenges();
//        Log.d(TAG, "All Preregister Challenges: \n");
//        for (PreregisterChallenge p : mAllPreregisterChallenges) {
//            Log.d(TAG, p.toString());
//        }
//
//        List<PublicKeyCredential> mAllPublicKeyCredentials = saclRepository.getAllPublicKeyCredentials();
//        Log.d(TAG, "All Credentials: \n");
//        for (PublicKeyCredential p : mAllPublicKeyCredentials) {
//            Log.d(TAG, p.toString());
//        }
//
//        List<PreauthenticateChallenge> mAllPreauthenticateChallenges = saclRepository.getAllPreauthenticateChallenges();
//        Log.d(TAG, "All PreauthenticateChallenges: \n");
//        for (PreauthenticateChallenge p : mAllPreauthenticateChallenges) {
//            Log.d(TAG, p.toString());
//        }
//
//        List<AuthenticationSignature> mAllAuthenticationSignatures = saclRepository.getAllFidoAuthenticationSignatures();
//        Log.d(TAG, "All Credentials: \n");
//        for (AuthenticationSignature f : mAllAuthenticationSignatures) {
//            Log.d(TAG, f.toString());
//        }
    }
}
