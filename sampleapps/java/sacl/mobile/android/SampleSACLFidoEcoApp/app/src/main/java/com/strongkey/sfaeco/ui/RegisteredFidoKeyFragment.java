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
 * This fragment displays the registered FIDO credential and its details.
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

import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclSharedDataModel;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

public class RegisteredFidoKeyFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = RegisteredFidoKeyFragment.class.getSimpleName();
    private static String MTAG;

    // Local resources
    private SfaSharedDataModel sfaSharedDataModel;
    private SaclSharedDataModel saclSharedDataModel;
    private View registeredKeyView;
    private int waitSeconds = 0;

    /**
     * Usual onCreateView method
     * @param inflater LayoutInflater
     * @param container ViewGroup
     * @param savedInstanceState Bundle
     * @return View
     */
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        saclSharedDataModel = Common.getSaclSharedDataModel();
        sfaSharedDataModel = Common.getSfaSharedDataModel();
        if (sfaSharedDataModel == null) {
            sfaSharedDataModel = new ViewModelProvider(requireActivity()).get(SfaSharedDataModel.class);
        }
        registeredKeyView = inflater.inflate(R.layout.fragment_registered_fido_key, container, false);

        User mUser = sfaSharedDataModel.getCurrentUserObject();
        PublicKeyCredential mPublicKeyCredential = saclSharedDataModel.getCurrentPublicKeyCredential();
        SfaSharedDataModel.ReadOnlyDisplayAction displayAction = sfaSharedDataModel.getCURRENT_READONLY_DISPLAY_ACTION();

        // Get TextViews
        TextView seePublicKey = registeredKeyView.findViewById(R.id.text_see_publickey_detail);
        TextView seeClientDataJson = registeredKeyView.findViewById(R.id.text_see_client_data_json_detail);
        TextView seeAuthenticatorData = registeredKeyView.findViewById(R.id.text_see_authenticator_data_detail);
        TextView seeCborAttestation = registeredKeyView.findViewById(R.id.text_see_cbor_attestation_detail);
        TextView seeJsonAttestation = registeredKeyView.findViewById(R.id.text_see_json_attestation_detail);

        // Display content
        if (mUser != null) {
            // Populate User information
            TextView userDetail = registeredKeyView.findViewById(R.id.user_detail);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(SfaConstants.JSON_KEY_DID + ": " + mUser.getDid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_SID + ": " + mUser.getSid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_UID + ": " + mUser.getUid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_USERNAME + ": " + mUser.getUsername() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS + ": " + mUser.getEmail() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER + ": " + mUser.getUserMobileNumber());
            userDetail.setText(stringBuffer.toString());

            // Populate registered FIDO key details
            if (mPublicKeyCredential != null) {
                TextView fidoKeyDetail = registeredKeyView.findViewById(R.id.fido_key_detail_body);
                stringBuffer = new StringBuffer();
                stringBuffer.append(SfaConstants.JSON_KEY_DID + ": " + mPublicKeyCredential.getDid() + '\n');
                stringBuffer.append(SfaConstants.JSON_KEY_UID + ": " + mPublicKeyCredential.getUid() + '\n');
                stringBuffer.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_DISPLAYNAME + ": " + mPublicKeyCredential.getDisplayName() + '\n');
                stringBuffer.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_RPID + ": " + mPublicKeyCredential.getRpid() + '\n');
                stringBuffer.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_CREDENTIALID + ": " + mPublicKeyCredential.getCredentialId() + '\n');
                stringBuffer.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_CREATEDATE + ": " + mPublicKeyCredential.getCreateDateFromLong() + '\n');
                stringBuffer.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_COUNTER + ": " + mPublicKeyCredential.getCounter() + '\n');
                stringBuffer.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_SEMODULE + ": " + mPublicKeyCredential.getSeModule());
                fidoKeyDetail.setText(stringBuffer.toString());

                // Show See Detail texts
                seePublicKey.setText("Public Key details...");
                seeClientDataJson.setText("Client Data Json details...");
                seeAuthenticatorData.setText("Authenticator Data details...");
                seeCborAttestation.setText("Cbor Attestation details...");
                seeJsonAttestation.setText("Json Attestation details...");

            } else {
                Toast.makeText(requireContext(), R.string.error_null_public_key_credential, Toast.LENGTH_LONG).show();
            }

        } else {
            Toast.makeText(requireContext(), R.string.error_null_user, Toast.LENGTH_LONG).show();
            goEnrollUser();
        }

        // Authenticate button
        Button buttonAuthenticate = registeredKeyView.findViewById(R.id.fido_authentication_button);
        buttonAuthenticate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                authenticateFidoKey(mUser);
            }
        });

        // Display PublicKey detail
        seePublicKey.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPublicKeyCredential != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.PUBLIC_KEY);
                    StringBuilder stringBuilder = new StringBuilder();
                    stringBuilder.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYALIAS + ": " + mPublicKeyCredential.getKeyAlias() + '\n');
                    stringBuilder.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYORIGIN + ": " + mPublicKeyCredential.getKeyOrigin() + '\n');
                    stringBuilder.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYALGORITHM + ": " + mPublicKeyCredential.getKeyAlgorithm() + '\n');
                    stringBuilder.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_KEYSIZE + ": " + mPublicKeyCredential.getKeySize() + '\n');
                    stringBuilder.append(Constants.JSON_KEY_PUBLIC_KEY_CREDENTIAL_LABEL_PUBLICKEY + ": " + mPublicKeyCredential.getPublicKey());
                    displayAction.setContent(stringBuilder.toString());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_public_key_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_public_key_credential, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display ClientData Json detail
        seeClientDataJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPublicKeyCredential != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.CLIENT_DATA_JSON);
                    displayAction.setContent(mPublicKeyCredential.getClientDataJson());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_client_data_json_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_public_key_credential, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display AuthenticatorData detail
        seeAuthenticatorData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPublicKeyCredential != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.AUTHENTICATOR_DATA);
                    displayAction.setContent(mPublicKeyCredential.getAuthenticatorData());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_authenticaor_data_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_public_key_credential, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display CBOR Attestation detail
        seeCborAttestation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPublicKeyCredential != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.CBOR_ATTESTATION);
                    displayAction.setContent(mPublicKeyCredential.getCborAttestation());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_cbor_attestation_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_public_key_credential, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display Json Attestation detail
        seeJsonAttestation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPublicKeyCredential != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.JSON_ATTESTATION);
                    displayAction.setContent(mPublicKeyCredential.getJsonAttestation());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_json_attestation_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_public_key_credential, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Return view to app
        return registeredKeyView;
    }


    // Navigates back to home page
    private void goHome() {
        NavController navController = Navigation.findNavController(registeredKeyView);
        navController.navigate(R.id.nav_home);
    }

    // Navigates to User enrollment page
    private void goEnrollUser() {
        try {
            NavController navController = Navigation.findNavController(registeredKeyView);
            navController.navigate(R.id.nav_enroll_user);
        } catch (IllegalStateException ex) {
            Toast.makeText(requireContext(), R.string.error_null_user, Toast.LENGTH_LONG).show();
        }
    }

    // Navigates to ReadOnlyContent page to display Notes from UserDevice
    private void displayReadOnlyContent() {
        NavController navController = Navigation.findNavController(registeredKeyView);
        navController.navigate(R.id.nav_display_readonly_content);
    }


    /**
     * Authenticate with FIDO key to the SFA's FIDO server
     * @param mUser User
     */
    public void authenticateFidoKey(User mUser) {
        MTAG = "authenticateFidoKey";
        Log.d(TAG, MTAG + getString(R.string.message_authenticate_fido_key));

        if (mUser != null) {
            if (MainActivity.mSfaChildHandler != null) {
                Bundle mBundle = new Bundle();
                Message msg = MainActivity.mSfaChildHandler.obtainMessage();
                mBundle.putString(SfaConstants.SFA_TASK, SfaConstants.SFA_TASKS.SFA_TASK_AUTHENTICATE_FIDO_KEY.name());
                mBundle.putInt(SfaConstants.JSON_KEY_DID, mUser.getDid());
                mBundle.putLong(SfaConstants.JSON_KEY_UID, mUser.getUid());
                mBundle.putString(SfaConstants.JSON_KEY_USER_USERNAME, mUser.getUsername());
                msg.setData(mBundle);
                MainActivity.mSfaChildHandler.sendMessage(msg);

                // Webservice needs at least a second to authenticate
                try {
                    // Sleep for 1 second
                    Log.v(TAG, "Calling FIDO Authentication webservice...");
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                // Now display authentication data
                NavController navController = Navigation.findNavController(registeredKeyView);
                navController.navigate(R.id.nav_fido_authentication);
            } else {
                Log.v(TAG, MTAG + R.string.error_null_handler + "mSaclHandler");
            }
        } else {
            Log.v(TAG, MTAG + R.string.error_null_user_authenticate);
            Toast.makeText(requireContext(), R.string.error_null_user_authenticate, Toast.LENGTH_LONG).show();
        }
    }
}
