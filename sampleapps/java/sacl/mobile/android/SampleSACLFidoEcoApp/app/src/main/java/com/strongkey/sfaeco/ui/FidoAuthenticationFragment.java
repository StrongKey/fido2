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
 * This fragment authenticates a user with a previously generated FIDO key to
 * the app's service.
 */

package com.strongkey.sfaeco.ui;

import android.os.Bundle;
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
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;
import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.SaclSharedDataModel;
import com.strongkey.sacl.utilities.Constants;

public class FidoAuthenticationFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = FidoAuthenticationFragment.class.getSimpleName();
    private static String MTAG;

    // Local resources
    private SfaSharedDataModel sfaSharedDataModel;
    private SaclSharedDataModel saclSharedDataModel;
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
        saclSharedDataModel = Common.getSaclSharedDataModel();
        sfaSharedDataModel = Common.getSfaSharedDataModel();
        if (sfaSharedDataModel == null) {
            sfaSharedDataModel = new ViewModelProvider(requireActivity()).get(SfaSharedDataModel.class);
        }
        root = inflater.inflate(R.layout.fragment_authenticated_fido_key, container, false);

        User mUser = sfaSharedDataModel.getCurrentUserObject();
        AuthenticationSignature authenticationSignature = saclSharedDataModel.getCurrentAuthenticationSignature();
        SfaSharedDataModel.ReadOnlyDisplayAction displayAction = sfaSharedDataModel.getCURRENT_READONLY_DISPLAY_ACTION();

        // Get TextViews
        TextView seeSignature = root.findViewById(R.id.text_see_signature_detail);
        TextView seeClientDataJson = root.findViewById(R.id.text_see_client_data_json_detail);
        TextView seeAuthenticatorData = root.findViewById(R.id.text_see_authenticator_data_detail);
        TextView emailSecurityKeyLink = root.findViewById(R.id.text_email_security_key_link);

        // Display content
        if (mUser != null) {
            // Populate User information
            TextView userDetail = root.findViewById(R.id.user_detail);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(SfaConstants.JSON_KEY_DID + ": " + mUser.getDid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_SID + ": " + mUser.getSid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_UID + ": " + mUser.getUid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_USERNAME + ": " + mUser.getUsername() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_EMAIL_ADDRESS + ": " + mUser.getEmail() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_MOBILE_NUMBER + ": " + mUser.getUserMobileNumber());
            userDetail.setText(stringBuffer.toString());

            // Populate registered FIDO key details
            if (authenticationSignature != null) {
                TextView fidoAuthenticationDetail = root.findViewById(R.id.fido_authentication_detail_body);
                stringBuffer = new StringBuffer();
                stringBuffer.append(SfaConstants.JSON_KEY_DID + ": " + authenticationSignature.getDid() + '\n');
                stringBuffer.append(SfaConstants.JSON_KEY_UID + ": " + authenticationSignature.getUid() + '\n');
                stringBuffer.append(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_RPID + ": " + authenticationSignature.getRpid() + '\n');
                stringBuffer.append(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREDENTIALID + ": " + authenticationSignature.getCredentialId() + '\n');
                stringBuffer.append(Constants.JSON_KEY_AUTHENTICATION_SIGNATURE_LABEL_CREATEDATE + ": " + authenticationSignature.getCreateDateFromLong());
                fidoAuthenticationDetail.setText(stringBuffer.toString());

                // Show See Detail texts
                seeSignature.setText(R.string.label_digital_signature_detail);
                seeClientDataJson.setText("Client Data Json details...");
                seeAuthenticatorData.setText("Authenticator Data details...");
                emailSecurityKeyLink.setText(R.string.label_send_security_key_link);

            } else {
                Toast.makeText(requireContext(), R.string.error_null_authentication_signature, Toast.LENGTH_SHORT).show();
            }

        }

        // OK button
        Button buttonOk = root.findViewById(R.id.fido_ok_button);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToGallery();
            }
        });

        // Display Signature detail
        seeSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authenticationSignature != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.SIGNATURE);
                    displayAction.setContent(authenticationSignature.getSignature());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_signature_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_authentication_signature, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display ClientData Json detail
        seeClientDataJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authenticationSignature != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.CLIENT_DATA_JSON);
                    displayAction.setContent(authenticationSignature.getClientDataJson());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_client_data_json_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_authentication_signature, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display AuthenticatorData detail
        seeAuthenticatorData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (authenticationSignature != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.AUTHENTICATOR_DATA);
                    displayAction.setContent(authenticationSignature.getAuthenticatorData());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_authenticaor_data_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_authentication_signature, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display Send Security Key registration e-mail detail
        emailSecurityKeyLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                displayAction.setAction(SfaSharedDataModel.DisplayAction.EMAIL_SECURITY_KEY_LINK);
                displayAction.setContent(requireContext().getString(R.string.label_email_security_key_link));
                displayAction.setLabel(requireContext().getString(R.string.label_send_security_key_link));
                displayReadOnlyContent();
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

    // Navigates to the product gallery page
    private void goToGallery() {
        NavController navController = Navigation.findNavController(root);
        navController.navigate(R.id.nav_gallery);
    }

    // Navigates to ReadOnlyContent page to display Notes from UserDevice
    private void displayReadOnlyContent() {
        NavController navController = Navigation.findNavController(root);
        navController.navigate(R.id.nav_display_readonly_content);
    }
}
