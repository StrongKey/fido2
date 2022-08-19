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
 * This fragment displays the completed FIDO authorized transaction
 */

package com.strongkey.sfaeco.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.strongkey.sacl.roomdb.AuthorizationSignature;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclSharedDataModel;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.roomdb.UserTransaction;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

import org.json.JSONException;

import java.util.Collection;
import java.util.Date;

import static java.lang.Thread.sleep;

public class CompletedUserTransactionFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = CompletedUserTransactionFragment.class.getSimpleName();
    private static String MTAG;

    // Local resources
    private SfaSharedDataModel sfaSharedDataModel;
    private SaclSharedDataModel saclSharedDataModel;
    UserTransaction.FidoAuthenticatorReferences userFidoAuthRef;
    private View completedTx;
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
        completedTx = inflater.inflate(R.layout.fragment_completed_transaction, container, false);

        User mUser = sfaSharedDataModel.getCurrentUserObject();
        Log.v(TAG, "User object: " + mUser.toString());
        PublicKeyCredential mPublicKeyCredential = saclSharedDataModel.getCurrentPublicKeyCredential();
        Log.v(TAG, "PKC object: " + mPublicKeyCredential.toString());
        UserTransaction mUserTransaction = sfaSharedDataModel.getCURRENT_USER_TRANSACTION();
        Log.v(TAG, "UserTransaction object: " + mUserTransaction.toString());
        AuthorizationSignature mAuthorizationSignature = saclSharedDataModel.getCurrentAuthorizationSignature();
        Log.v(TAG, "AuthorizationSignature object: " + mAuthorizationSignature.toString());
        Collection<UserTransaction.FidoAuthenticatorReferences> fidoAuthenticatorReferences = mUserTransaction.getFidoAuthenticatorReferences();
        for (UserTransaction.FidoAuthenticatorReferences far : fidoAuthenticatorReferences) {
            try {
                Log.v(TAG, "UserTransaction.FidoAuthenticatorReferences object: " + far.toJSON().toString(2));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        SfaSharedDataModel.ReadOnlyDisplayAction displayAction = sfaSharedDataModel.getCURRENT_READONLY_DISPLAY_ACTION();

        // Get TextViews
        TextView seeTxPayload = completedTx.findViewById(R.id.text_see_txpayload_detail);
        TextView seeId = completedTx.findViewById(R.id.text_see_id_detail);
        TextView seeRawId = completedTx.findViewById(R.id.text_see_rawid_detail);
        TextView seeUserHandle = completedTx.findViewById(R.id.text_see_userhandle_detail);
        TextView seeAuthenticatorData = completedTx.findViewById(R.id.text_see_authenticator_data_detail);
        TextView seeClientDataJson = completedTx.findViewById(R.id.text_see_client_data_json_detail);
        TextView seeAaguid = completedTx.findViewById(R.id.text_see_aaguid_detail);
        TextView seePublicKey = completedTx.findViewById(R.id.text_see_publickey_detail);
        TextView seeSignature = completedTx.findViewById(R.id.text_see_signature_detail);

        // Display content
        if (mUser != null) {
            // Populate User information
            TextView userDetail = completedTx.findViewById(R.id.user_detail);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(SfaConstants.JSON_KEY_DID + ": " + mUser.getDid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_UID + ": " + mUser.getUid() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_USERNAME + ": " + mUser.getUsername() + '\n');
            stringBuffer.append(SfaConstants.JSON_KEY_USER_GIVEN_NAME + " " + SfaConstants.JSON_KEY_USER_FAMILY_NAME + ": " +
                    mUser.getGivenName() + " " + mUser.getFamilyName() + '\n');
            userDetail.setText(stringBuffer.toString());

            // Populate transaction detail
            if (mUserTransaction != null) {
                TextView fidoUserTxDetail = completedTx.findViewById(R.id.fido_tx_detail_body);
                stringBuffer = new StringBuffer();
                stringBuffer.append(SfaConstants.JSON_KEY_TXID + ": " + mUserTransaction.getTxid() + '\n');
                stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXDATE + ": " + new Date(mAuthorizationSignature.getCreateDate()) + '\n');
                stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_NONCE + ": " + mAuthorizationSignature.getNonce() + '\n');
                stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_CHALLENGE + ": " + mAuthorizationSignature.getChallenge() + '\n');
                fidoUserTxDetail.setText(stringBuffer.toString());

                // Show See TXPayload Detail text
                seeTxPayload.setText(R.string.label_tx_payload_details);

            } else {
                Log.v(TAG, "User object is NULL");
                Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
            }

            // Populate FIDOAuthenticatorReferences detail
            if (mUserTransaction != null) {

                // TODO:  Should only do this for a second or 2 and then fail
                while (mUserTransaction.getFidoAuthenticatorReferences() == null) {
                    try {
                        sleep(500);
                    } catch (InterruptedException e) {}
                }

                if (mUserTransaction.getFidoAuthenticatorReferences() != null)
                {
                    userFidoAuthRef = (UserTransaction.FidoAuthenticatorReferences) mUserTransaction.getFidoAuthenticatorReferences().toArray()[0];
                    TextView farDetail = completedTx.findViewById(R.id.text_see_fido_authenticator_reference_detail);
                    stringBuffer = new StringBuffer();
                    stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_PROTOCOL_LABEL + ": " + userFidoAuthRef.getProtocol() + '\n');
                    stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_RPID_LABEL + ": " + userFidoAuthRef.getRpid() + '\n');
                    stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_AUTHORIZATION_TIME_LABEL + ": " + new Date(userFidoAuthRef.getAuthorizationTime()) + '\n');
                    stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_UP_LABEL + ": " + userFidoAuthRef.getUp() + '\n');
                    stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_UV_LABEL + ": " + userFidoAuthRef.getUv() + '\n');
                    stringBuffer.append(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_USED_FOR_THIS_TRANSACTION_LABEL + ": " +
                            userFidoAuthRef.getUsedForThisTransaction() + '\n');
                    farDetail.setText(stringBuffer.toString());

                    // Show See Detail texts
                    seeId.setText("ID detail..");
                    seeRawId.setText("Raw ID detail..");
                    seeUserHandle.setText("User Handle detail..");
                    seeAuthenticatorData.setText("Authenticator Data details..");
                    seeClientDataJson.setText("Client Data Json details..");
                    seeAaguid.setText("AAGUID detail..");
                    seePublicKey.setText("Public Key details..");
                    seeSignature.setText("Signature detail..");
                }

            } else {
                Log.v(TAG, "UserTransaction object is NULL");
                Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
            }

        } else {
            Toast.makeText(requireContext(), R.string.error_null_user, Toast.LENGTH_SHORT).show();
        }

        // Display TXPayload detail
        seeTxPayload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserTransaction != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.TXPAYLOAD);
                    displayAction.setContent(mUserTransaction.getTxpayload());
                    displayAction.setLabel(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXPAYLOAD_LABEL);
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display ID detail
        seeId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFidoAuthRef != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.ID);
                    displayAction.setContent(userFidoAuthRef.getId());
                    displayAction.setLabel(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_ID_LABEL);
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display Raw ID detail
        seeRawId.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFidoAuthRef != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.RAWID);
                    displayAction.setContent(userFidoAuthRef.getRawId());
                    displayAction.setLabel(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_RAWID_LABEL);
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display UserHandle detail
        seeUserHandle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFidoAuthRef != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.USER_HANDLE);
                    displayAction.setContent(userFidoAuthRef.getUserHandle());
                    displayAction.setLabel(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_USER_HANDLE_LABEL);
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display AuthenticatorData detail
        seeAuthenticatorData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFidoAuthRef != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.AUTHENTICATOR_DATA);
                    displayAction.setContent(userFidoAuthRef.getAuthenticatorData());
                    displayAction.setLabel(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_AUTHENTICATOR_DATA_LABEL);
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display ClientData Json detail
        seeClientDataJson.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFidoAuthRef != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.CLIENT_DATA_JSON);
                    displayAction.setContent(userFidoAuthRef.getClientDataJson());
                    displayAction.setLabel(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_CLIENT_DATA_JSON_LABEL);
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display AAGUID
        seeAaguid.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userFidoAuthRef != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.AAGUID);
                    displayAction.setContent(userFidoAuthRef.getAaguid());
                    displayAction.setLabel(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_AAGUID_LABEL);
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
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
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Display Signature detail
        seeSignature.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mUserTransaction != null) {
                    displayAction.setAction(SfaSharedDataModel.DisplayAction.SIGNATURE);
                    displayAction.setContent(userFidoAuthRef.getSignature());
                    displayAction.setLabel(requireContext().getString(R.string.label_fido_signature_detail));
                    displayReadOnlyContent();
                } else {
                    Toast.makeText(requireContext(), R.string.error_null_usertx_detail, Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Return view to app
        return completedTx;
    }


    // Navigates back to home page
    private void goHome() {
        // Clear current transaction
        sfaSharedDataModel.clearProductsPricePayment();
        sfaSharedDataModel.clearCurrentUserTransaction();

        NavController navController = Navigation.findNavController(completedTx);
        navController.navigate(R.id.nav_home);
    }

    // Navigates to ReadOnlyContent page to display Notes from UserDevice
    private void displayReadOnlyContent() {
        NavController navController = Navigation.findNavController(completedTx);
        navController.navigate(R.id.nav_display_readonly_content);
    }

}
