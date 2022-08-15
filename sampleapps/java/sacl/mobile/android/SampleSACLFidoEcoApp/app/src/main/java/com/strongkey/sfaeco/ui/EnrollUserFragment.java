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
 * Enroll User page fragment with input form for user data.
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
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;
import com.strongkey.sfaeco.main.SfaSharedDataModel;

import java.util.List;

public class EnrollUserFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = EnrollUserFragment.class.getSimpleName();
    private static String MTAG;

    // Variables for the fragment's view, resources and user data
    private View root;
    private Resources mResources;
    private String mUsername;
    private String mPassword;
    private String mGivenName;
    private String mFamilyName;
    private String mEmail;
    private String mEnrollDate;
    private String mOtherNumber;
    private String mPersonalNumber;
    private String mStatus;
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
        sfaSharedDataModel = new ViewModelProvider(requireActivity()).get(SfaSharedDataModel.class);
        root = inflater.inflate(R.layout.fragment_enroll_user, container, false);
        mResources = root.getResources();

        // Find all TextInputLayout fields and returns them in a List
        final List<TextInputLayout> textInputLayouts = Common.findViewsWithType(root, TextInputLayout.class);

        // Check for errors on all text input fields
        Button buttonSave = root.findViewById(R.id.submit_button);
        buttonSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean noErrors = true;
                for (TextInputLayout textInputLayout : textInputLayouts) {
                    int keyid = textInputLayout.getEditText().getId();
                    if (keyid != R.id.uid_edit_text) {
                        String editTextString = textInputLayout.getEditText().getText().toString();
                        if (editTextString.isEmpty()) {
                            textInputLayout.setError(getResources().getString(R.string.error_string));
                            noErrors = false;
                        } else {
                            textInputLayout.setError(null);
                        }
                    }
                }

                // All fields are valid - put them in a Bundle and send to SFA server
                if (noErrors) {
                    Bundle mBundle = null;
                    mBundle = new Bundle();
                    for (TextInputLayout til : textInputLayouts) {
                        int keyid = til.getEditText().getId();
                        String key = mResources.getResourceEntryName(keyid);
                        String value = til.getEditText().getText().toString();
                        switch(keyid) {
                            case R.id.email_edit_text:
                                mEmail = value;
                                break;
                            case R.id.family_name_edit_text:
                                mFamilyName = value;
                                break;
                            case R.id.given_name_edit_text:
                                mGivenName = value;
                                break;
                            case R.id.personal_mobile_edit_text:
                                mPersonalNumber = value;
                                break;
                            case R.id.uid_edit_text:
                                mUid = value;
                                break;
                            case R.id.username_edit_text:
                                mUsername = value;
                                break;
                            default:
                                Log.w(TAG, getString(R.string.error_invalid_key) + key + "/" + value);
                        }
                        mBundle.putString(key, value);
                        Log.d(TAG, getString(R.string.message_bundled) + key + "/" + value);
                    }
                    registerUser(root);
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
     * Registers a new user by sending data to a background thread for execution
     * @param view
     */
    public void registerUser(View view) {
        MTAG = "registerUser";
        Log.d(TAG, MTAG + getString(R.string.message_register_user));

        if (MainActivity.mSfaChildHandler != null) {
            Bundle mBundle = new Bundle();
            Message msg = MainActivity.mSfaChildHandler.obtainMessage();
            mBundle.putString(SfaConstants.SFA_TASK, SfaConstants.SFA_TASKS.SFA_TASK_REGISTER_USER.name());
            mBundle.putString(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS, mEmail);
            mBundle.putString(SfaConstants.JSON_KEY_USER_FAMILY_NAME, mFamilyName);
            mBundle.putString(SfaConstants.JSON_KEY_USER_GIVEN_NAME, mGivenName);
            mBundle.putString(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER, mPersonalNumber);
            mBundle.putString(SfaConstants.JSON_KEY_USER_USERNAME, mUsername);
            msg.setData(mBundle);
            MainActivity.mSfaChildHandler.sendMessage(msg);
        } else {
            Log.d(TAG, MTAG + R.string.error_null_handler + "mSaclHandler");
        }
    }
}
