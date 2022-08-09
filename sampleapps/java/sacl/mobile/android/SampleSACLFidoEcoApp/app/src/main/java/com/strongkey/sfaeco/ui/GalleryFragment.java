/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 *
 * **********************************************
 *
 * 888b    888          888
 * 8888b   888          888
 * 88888b  888          888
 * 888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 * 888 Y88b888 d88""88b 888    d8P  Y8b 88K
 * 888  Y88888 888  888 888    88888888 "Y8888b.
 * 888   Y8888 Y88..88P Y88b.  Y8b.          X88
 * 888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * Fragment to select products from a gallery and checkout
 */

package com.strongkey.sfaeco.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.material.card.MaterialCardView;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.Product;
import com.strongkey.sfaeco.utilities.SfaConstants;

import java.text.NumberFormat;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

public class GalleryFragment extends Fragment {

    private static SfaSharedDataModel sfaSharedDataModel;
    private String TAG = GalleryFragment.class.getSimpleName();
    private Product product;

    MaterialCardView materialCardViewT100;
    MaterialCardView materialCardViewE1000;
    MaterialCardView materialCardViewFidoCloud;
    MaterialCardView materialCardViewTellaroCloud;
    ImageView paymentButton;
    TextView totalPrice;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // ViewModel
        sfaSharedDataModel = MainActivity.sfaSharedDataModel;
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Right Arrow image
        paymentButton = requireActivity().findViewById(R.id.payment_button_right);

        // Products
        materialCardViewT100 = root.findViewById(R.id.cardViewT100);
        materialCardViewE1000 = root.findViewById(R.id.cardViewE1000);
        materialCardViewFidoCloud = root.findViewById(R.id.cardViewFidoCloud);
        materialCardViewTellaroCloud = root.findViewById(R.id.cardViewTellaroCloud);
        totalPrice = root.findViewById(R.id.totalPrice);

        // Get number format for total price
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);

        // T100
        materialCardViewT100.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product = new Product(1, "Tellaro T100", 9995);
                if (materialCardViewT100.isChecked()) {
                    materialCardViewT100.setChecked(false);
                    materialCardViewT100.setCardElevation(1);
                    sfaSharedDataModel.removeProduct(product.getName());
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_removed + product.toString());
                } else {
                    materialCardViewT100.setChecked(true);
                    materialCardViewT100.setCardElevation(4);
                    sfaSharedDataModel.addProduct(product);
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_added + product.toString());
                }
                Log.v(TAG, "Current Products " + sfaSharedDataModel.getCurrentProductsList().toString());
            }
        });

        // E1000
        materialCardViewE1000.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product = new Product(2, "Tellaro E1000", 19995);
                if (materialCardViewE1000.isChecked()) {
                    materialCardViewE1000.setChecked(false);
                    materialCardViewE1000.setCardElevation(1);
                    sfaSharedDataModel.removeProduct(product.getName());
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_removed + product.toString());
                } else {
                    materialCardViewE1000.setChecked(true);
                    materialCardViewE1000.setCardElevation(4);
                    sfaSharedDataModel.addProduct(product);
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_added + product.toString());
                }
                Log.v(TAG, "Current Products " + sfaSharedDataModel.getCurrentProductsList().toString());
            }
        });

        // Fido Cloud
        materialCardViewFidoCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product = new Product(3, "FIDO Cloud", 995);
                if (materialCardViewFidoCloud.isChecked()) {
                    materialCardViewFidoCloud.setChecked(false);
                    materialCardViewFidoCloud.setCardElevation(1);
                    sfaSharedDataModel.removeProduct(product.getName());
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_removed + product.toString());
                } else {
                    materialCardViewFidoCloud.setChecked(true);
                    materialCardViewFidoCloud.setCardElevation(4);
                    sfaSharedDataModel.addProduct(product);
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_added + product.toString());
                }
                Log.v(TAG, "Current Products " + sfaSharedDataModel.getCurrentProductsList().toString());
            }
        });

        // Tellaro Cloud
        materialCardViewTellaroCloud.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                product = new Product(4, "Tellaro Cloud", 11940);
                if (materialCardViewTellaroCloud.isChecked()) {
                    materialCardViewTellaroCloud.setChecked(false);
                    materialCardViewTellaroCloud.setCardElevation(1);
                    sfaSharedDataModel.removeProduct(product.getName());
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_removed + product.toString());
                } else {
                    materialCardViewTellaroCloud.setChecked(true);
                    materialCardViewTellaroCloud.setCardElevation(4);
                    sfaSharedDataModel.addProduct(product);
                    totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
                    Log.v(TAG, R.string.message_added + product.toString());
                }
                Log.v(TAG, R.string.message_current_products + sfaSharedDataModel.getCurrentProductsList().toString());
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        NumberFormat numberFormat = NumberFormat.getCurrencyInstance(Locale.US);
        Map<String, Product> products = sfaSharedDataModel.getCurrentProductsList();
        if (!products.isEmpty()) {
            Iterator<String> it = products.keySet().iterator();
            while (it.hasNext()) {
                String key = it.next();
                switch (key) {
                    case "T100":
                        materialCardViewT100.setChecked(true);
                        materialCardViewT100.setCardElevation(1);
                        break;
                    case "E1000":
                        materialCardViewE1000.setChecked(true);
                        materialCardViewE1000.setCardElevation(1);
                        break;
                    case "FidoCloud":
                        materialCardViewFidoCloud.setChecked(true);
                        materialCardViewFidoCloud.setCardElevation(1);
                        break;
                    case "TellaroCloud":
                        materialCardViewTellaroCloud.setChecked(true);
                        materialCardViewTellaroCloud.setCardElevation(1);
                        break;
                }
            }
            totalPrice.setText(numberFormat.format(sfaSharedDataModel.getTotalPrice()));
        }
    }
}
