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
 * Fragment to retrieve a User from the local Room DB and display on screen.
 * Technically, there should only be a single user record in the DB for a
 * user that does not share the phone/app with anyone else - but its an
 * Alpha release. If the UID is empty, it will retrieve the most current UID
 * in the database for display.
 */

package com.strongkey.sfaeco.ui;

import android.content.res.Resources;
import android.os.Bundle;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.textfield.TextInputLayout;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.asynctasks.RetrieveObjectTask;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

import java.util.List;

public class RetrieveUserFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = RetrieveUserFragment.class.getSimpleName();
    private static String MTAG;

    // Variables for the fragment's view, resources and user data
    private View root;
    private Resources mResources;
    private int mDid = 1;
    private String mUid;

    // ViewModel for shared data
    private SfaSharedDataModel sfaSharedDataModel;

    /**
     * Usual onCreateView method
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
    {
        sfaSharedDataModel = Common.getSfaSharedDataModel();
        if (sfaSharedDataModel == null) {
            sfaSharedDataModel = new ViewModelProvider(requireActivity()).get(SfaSharedDataModel.class);
        }
        root = inflater.inflate(R.layout.fragment_retrieve_user, container, false);
        mResources = root.getResources();

        // Find all TextInputLayout fields and returns them in a List
        final List<TextInputLayout> textInputLayouts = Common.findViewsWithType(root, TextInputLayout.class);

        // Check for errors in all text input fields
        Button buttonSearch = root.findViewById(R.id.search_button);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean noErrors = true;

                // All fields are valid - put them in a Bundle and send to SFA server
                if (noErrors) {
                    Bundle mBundle = null;
                    mBundle = new Bundle();
                    for (TextInputLayout til : textInputLayouts) {
                        int keyid = til.getEditText().getId();
                        String key = mResources.getResourceEntryName(keyid);
                        String value = til.getEditText().getText().toString();
                        switch(keyid) {
                            case R.id.uid_edit_text:
                                if (value != null)
                                    mUid = value;
                                else
                                    mUid = "0";
                                break;
                            default:
                                Log.w(TAG, getString(R.string.error_invalid_key) + key + "/" + value);
                        }
                        mBundle.putString(key, value);
                        Log.d(TAG, getString(R.string.message_bundled) + key + "/" + value);
                    }
                    retrieveUser(root, mUid);
                }
            }
        });

        // Clears all text input fields
        Button buttonReset = root.findViewById(R.id.reset_button);
        buttonReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (TextInputLayout textInputLayout : textInputLayouts) {
                    textInputLayout.setError(null);
                    textInputLayout.getEditText().setText(null,null);
                }
            }
        });

        // Return view to app
        return root;
    }



    /**
     * Retrieves an existing user record from the RoomDB (if exists) and displays data on screen
     * with a button to register a new FIDO key if a FIDO key does not exist; else the Register
     * button will be disabled
     * TODO: Display all data including public-key information about FIDO key if it exists
     * @param view
     */
    public void retrieveUser(View view, String mUid) {
        MTAG = "retrieveUser";
        Log.d(TAG, MTAG + getString(R.string.message_retreiving_user));

        // Perform the search function as an AsyncTask -
        // parameters are DID, "User", "uid", mUid, sfaSharedDataModel (to get SfaRepository)
        new RetrieveObjectTask().execute(mDid,
                                         SfaConstants.JSON_KEY_USER,
                                         SfaConstants.JSON_KEY_UID,
                                         mUid,
                sfaSharedDataModel,
                                         this.getContext());

        if (MainActivity.mSfaMainHandler != null) {
            Bundle mBundle = new Bundle();
            Message msg = MainActivity.mSfaMainHandler.obtainMessage();
            mBundle.putString(SfaConstants.SFA_TASK_RETRIEVE_USER_RESPONSE_JSON, SfaConstants.SFA_TASK_RETRIEVE_USER_RESPONSE_OBJECT);
            msg.setData(mBundle);
            MainActivity.mSfaMainHandler.sendMessage(msg);
        } else {
            Log.d(TAG, MTAG + R.string.error_null_handler + "sfaHandler");
        }
    }
}
