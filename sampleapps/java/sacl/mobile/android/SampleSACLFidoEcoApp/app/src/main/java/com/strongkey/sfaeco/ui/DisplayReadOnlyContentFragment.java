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
 * Displays details about the registered device and/or attestation
 */

package com.strongkey.sfaeco.ui;

import android.os.Bundle;
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

import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.main.SfaSharedDataModel;

public class DisplayReadOnlyContentFragment extends Fragment {

    // Tags for log messages
    private static final String TAG = DisplayReadOnlyContentFragment.class.getSimpleName();

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
        root = inflater.inflate(R.layout.fragment_display_readonly_content, container, false);
        TextView roContentLabel = root.findViewById(R.id.text_readonly_content_label);
        roContentLabel.setTextAppearance(R.style.TextAppearance_MaterialComponents_Button);

        SfaSharedDataModel.ReadOnlyDisplayAction displayAction = sfaSharedDataModel.getCURRENT_READONLY_DISPLAY_ACTION();
        if (displayAction.getAction() == SfaSharedDataModel.DisplayAction.NULL) {
            Toast.makeText(requireContext(), R.string.error_null_registered_device, Toast.LENGTH_SHORT).show();
        } else {
            TextView readonlyDetail = root.findViewById(R.id.readonly_detail);
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(displayAction.getContent());
            readonlyDetail.setText(stringBuffer.toString());
            roContentLabel.setText(displayAction.getLabel());
        }

        // Return view to app
        return root;
    }


    // Navigates back to page we came from
    private void goBack() {
        NavController navController = Navigation.findNavController(root);
        navController.navigateUp();
    }
}
