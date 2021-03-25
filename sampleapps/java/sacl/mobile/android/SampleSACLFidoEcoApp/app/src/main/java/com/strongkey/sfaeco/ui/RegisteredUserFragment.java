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
 * Displays details about the registered user
 */

package com.strongkey.sfaeco.ui;

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

import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.utilities.SfaConstants;

public class RegisteredUserFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = RegisteredUserFragment.class.getSimpleName();
    private static String MTAG;

    // Local resources
    private SfaSharedDataModel sfaSharedDataModel;
    private View root;

    /**
     * Usual onCreateView method
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        sfaSharedDataModel = new ViewModelProvider(requireActivity()).get(SfaSharedDataModel.class);
        root = inflater.inflate(R.layout.fragment_registered_user, container, false);

        User mUser = sfaSharedDataModel.getCurrentUserObject();
        SfaSharedDataModel.ReadOnlyDisplayAction displayAction = sfaSharedDataModel.getCURRENT_READONLY_DISPLAY_ACTION();

        if (mUser != null) {

            // Populate User information
            TextView userDetail = root.findViewById(R.id.user_detail);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(SfaConstants.JSON_KEY_DID + ": " + mUser.getDid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_UID + ": " + mUser.getUid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_USERNAME + ": " + mUser.getUsername() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS + ": " + mUser.getEmail() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER + ": " + mUser.getUserMobileNumber());
            userDetail.setText(stringBuffer.toString());
            
        } else {
            Toast.makeText(requireContext(), R.string.error_null_user, Toast.LENGTH_SHORT).show();
        }

        // Go to home page
        Button buttonOk = root.findViewById(R.id.fido_registration_button);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                registerFidoKey(mUser);
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

    // Navigates to ReadOnlyContent page to display Notes from UserDevice
    private void displayReadOnlyContent() {
        NavController navController = Navigation.findNavController(root);
        navController.navigate(R.id.nav_display_readonly_content);
    }

    /**
     * Register a new FIDO key with the SFA's FIDO server
     * @param mUser User
     */
    public void registerFidoKey(User mUser) {
        MTAG = "registerFidoKey";
        Log.d(TAG, MTAG + getString(R.string.message_register_fido_key));

        if (MainActivity.mSfaChildHandler != null) {
            Bundle mBundle = new Bundle();
            Message msg = MainActivity.mSfaChildHandler.obtainMessage();
            mBundle.putString(SfaConstants.SFA_TASK, SfaConstants.SFA_TASKS.SFA_TASK_REGISTER_FIDO_KEY.name());
            mBundle.putInt(SfaConstants.JSON_KEY_DID, mUser.getDid());
            mBundle.putLong(SfaConstants.JSON_KEY_UID, mUser.getUid());
            mBundle.putString(SfaConstants.JSON_KEY_USER_USERNAME, mUser.getUsername());
            msg.setData(mBundle);
            MainActivity.mSfaChildHandler.sendMessage(msg);
        } else {
            Log.d(TAG, MTAG + R.string.error_null_handler + "mSaclHandler");
        }
    }
}
