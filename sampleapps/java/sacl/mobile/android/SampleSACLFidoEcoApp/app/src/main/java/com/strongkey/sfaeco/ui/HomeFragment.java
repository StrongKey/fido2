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
 * The Home page fragment. Nothing to do but display welcome text. The forward
 * arrow button automatically displays the current user account details and their
 * registered FIDO key, if it exists.
 */

package com.strongkey.sfaeco.ui;

import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.asynctasks.RetrieveObjectTask;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

public class HomeFragment extends Fragment {

    // ViewModel for sharing information between fragments
    private SfaSharedDataModel sfaSharedDataModel;
    private View root;
    private int mDid = 1;
    private String mUid;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        sfaSharedDataModel = Common.getSfaSharedDataModel();
        if (sfaSharedDataModel == null) {
            sfaSharedDataModel = new ViewModelProvider(requireActivity()).get(SfaSharedDataModel.class);
        }
        root = inflater.inflate(R.layout.fragment_home, container, false);

        // Check for errors in all text input fields
        ImageView buttonRight = root.findViewById(R.id.button_right);
        buttonRight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mUid = "";
                retrieveUser(root, mUid);
            }
        });
        return root;
    }

    /**
     * Retrieves an existing user record from the RoomDB (if exists) and displays data
     * with a button to register a new FIDO key if a FIDO key does not exist; else the
     * it prompts the user to authenticate with their existing FIDO key
     *
     * @param view
     */
    public void retrieveUser(View view, String mUid) {
        String TAG = "retrieveUser";
        Log.d(TAG, getString(R.string.message_retreiving_user));

        // Perform the search function as an AsyncTask -
        // parameters are DID, "User", "uid", mUid, sfaSharedDataModel (to get SfaRepository)
        new RetrieveObjectTask().execute(mDid,
                SfaConstants.JSON_KEY_USER,
                SfaConstants.JSON_KEY_UID,
                mUid,
                sfaSharedDataModel,
                this.getContext());

        // RoomDB needs at least a half second to get data for display
        try {
            // Sleep for 1/2 second
            Log.v(TAG, "Retrieving user data..." );
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (MainActivity.mSfaMainHandler != null) {
            Bundle mBundle = new Bundle();
            Message msg = MainActivity.mSfaMainHandler.obtainMessage();
            mBundle.putString(SfaConstants.SFA_TASK_RETRIEVE_USER_RESPONSE_JSON,
                    SfaConstants.SFA_TASK_RETRIEVE_USER_RESPONSE_OBJECT);
            msg.setData(mBundle);
            MainActivity.mSfaMainHandler.sendMessage(msg);
        } else {
            Log.d(TAG, R.string.error_null_handler + "sfaHandler");
        }
    }
}
