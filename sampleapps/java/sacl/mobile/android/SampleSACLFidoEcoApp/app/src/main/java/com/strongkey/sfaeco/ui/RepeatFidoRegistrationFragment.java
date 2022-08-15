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
 * Repeat a FIDO registration process in case it failed the first time, or if the
 * user deleted the key and needs to reregister. It searches for the username in
 * the local RoomDB and based on previously stored metadata about the User, it
 * acquires a new challenge to generate a new FIDO key.
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
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputLayout;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.asynctasks.RetrieveObjectTask;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.roomdb.UserDevice;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;
import com.strongkey.sacl.utilities.Constants;

import java.util.List;

public class RepeatFidoRegistrationFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = RepeatFidoRegistrationFragment.class.getSimpleName();
    private static String MTAG;

    // Variables for the fragment's view, resources and user data
    private View root;
    private Resources mResources;
    private int did = 1; // TODO: Get from settings
    private String mUsername;
    private User mUser;
    private UserDevice mUserDevice;

    // ViewModel for shared data
    private SfaSharedDataModel sfaSharedDataModel;

    /**
     * Usual onCreateView method
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    public View onCreateView(@NonNull LayoutInflater inflater, final ViewGroup container, Bundle savedInstanceState)
    {
        sfaSharedDataModel = new ViewModelProvider(requireActivity()).get(SfaSharedDataModel.class);
        root = inflater.inflate(R.layout.fragment_repeat_register_fido_key, container, false);
        mResources = root.getResources();

        // Find all TextInputLayout fields and returns them in a List
        final List<TextInputLayout> textInputLayouts = Common.findViewsWithType(root, TextInputLayout.class);

        // Check for errors in text input fields
        Button buttonSearch = root.findViewById(R.id.search_button);
        buttonSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                boolean noErrors = true;
                for (TextInputLayout textInputLayout : textInputLayouts) {
                    String editTextString = textInputLayout.getEditText().getText().toString();
                    if (editTextString.isEmpty()) {
                        textInputLayout.setError(getResources().getString(R.string.error_string));
                        noErrors = false;
                    } else {
                        textInputLayout.setError(null);
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
                            case R.id.username_edit_text:
                                mUsername = value;
                                break;
                            default:
                                Log.w(TAG, getString(R.string.error_invalid_key) + key + "/" + value);
                        }
                        mBundle.putString(key, value);
                        Log.d(TAG, getString(R.string.message_bundled) + key + "/" + value);
                    }

                    // Perform the function as an AsyncTask
                    new RetrieveObjectTask().execute(
                            did, "User", "username", mUsername,
                            requireActivity().getApplication(),
                            sfaSharedDataModel);

                    // Check for user information to display it
                    mUser = sfaSharedDataModel.getCurrentUserObject();
                    mUserDevice = sfaSharedDataModel.getCurrentUserDeviceObject();
                    if (mUser != null) {
                        // Populate User information
                        TextView userText = root.findViewById(R.id.user_detail);
                        StringBuffer stringBuffer = new StringBuffer();
                        stringBuffer.append(SfaConstants.JSON_KEY_DID + ": " + mUser.getDid() + '\n');
                        stringBuffer.append(SfaConstants.JSON_KEY_SID + ": " + mUser.getSid() + '\n');
                        stringBuffer.append(SfaConstants.JSON_KEY_UID + ": " + mUser.getUid() + '\n');
                        stringBuffer.append(SfaConstants.JSON_KEY_USER_USERNAME + ": " + mUser.getUsername() + '\n');
                        stringBuffer.append(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS + ": " + mUser.getEmail() + '\n');
                        stringBuffer.append(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER + ": " + mUser.getUserMobileNumber());
                        userText.setText(stringBuffer.toString());
                    } else {
                        Log.e(TAG, "Could not get User object from shared view");
                        Toast.makeText(requireContext(), "Could not get User object from shared view", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

        // Cancels action
        Button buttonCancel = root.findViewById(R.id.cancel_button);
        buttonCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               goHome();
            }
        });

        // Register FIDO key action
        Button buttonRegister = root.findViewById(R.id.fido_registration_button);
        buttonRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUser != null) {
                    if (mUserDevice != null) {
                        repeatFidoRegistration();
                    }
                } else {
                    Log.e(TAG, "Could not call register; one or both objects are NULL:\n" +
                        "User: " + mUser.toString() + "\n" +
                        "UserDevice: " + mUserDevice.toString());
                }
            }
        });

        // Return view to app
        return root;
    }

    // Navigates back to home page
    private void goHome() {
        NavController navController = Navigation.findNavController(root);
        navController.navigate(R.id.nav_home);
    }

    /**
     * Repeats the registration of a new FIDO key
     */
    public void repeatFidoRegistration() {
        MTAG = "repeatFidoRegistration";
        Log.d(TAG, MTAG + getString(R.string.message_repeat_register_fido_key));

        if (MainActivity.mSfaChildHandler != null) {
            Bundle mBundle = new Bundle();
            Message msg = MainActivity.mSfaChildHandler.obtainMessage();
            mBundle.putString(SfaConstants.SFA_TASK, SfaConstants.SFA_TASKS.SFA_TASK_REGISTER_FIDO_KEY.name());
            mBundle.putInt(SfaConstants.JSON_KEY_DID, mUser.getDid());
            mBundle.putInt(SfaConstants.JSON_KEY_SID, mUser.getSid());
            mBundle.putLong(SfaConstants.JSON_KEY_UID, mUser.getUid());
            mBundle.putLong(Constants.JSON_KEY_DEVID, mUserDevice.getDevid());
            mBundle.putLong(Constants.JSON_KEY_RDID, mUserDevice.getRdid());
            mBundle.putString(SfaConstants.JSON_KEY_USER_USERNAME, mUser.getUsername());
            msg.setData(mBundle);
            MainActivity.mSfaChildHandler.sendMessage(msg);
        } else {
            Log.d(TAG, MTAG + R.string.error_null_handler + "mSaclHandler");
        }
    }
}
