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
 * Fragment to select payment method for checkout
 */

package com.strongkey.sfaeco.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.Cart;
import com.strongkey.sfaeco.roomdb.PaymentMethod;
import com.strongkey.sfaeco.utilities.SfaConstants.PaymentBrand;

public class PaymentFragment extends Fragment {

    private static SfaSharedDataModel sfaSharedDataModel;
    private String TAG = PaymentFragment.class.getSimpleName();
    private PaymentMethod paymentMethod;

    private MaterialCardView materialCardViewAmex;
    private MaterialCardView materialCardViewDiscover;
    private MaterialCardView materialCardViewJcb;
    private MaterialCardView materialCardViewMc;
    private MaterialCardView materialCardViewVisa;
    private MaterialCardView materialCardViewSepa;

    private View paymentView;
    private int merchantId = 1;
    boolean paymentCardSelected;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        // ViewModel
        sfaSharedDataModel = MainActivity.sfaSharedDataModel;
        paymentView = inflater.inflate(R.layout.fragment_payment, container, false);

        // Checkout arrow button
        ImageView checkoutButton = paymentView.findViewById(R.id.checkout_button);

        // PaymentMethods
        materialCardViewAmex = paymentView.findViewById(R.id.cardViewAmex);
        materialCardViewDiscover = paymentView.findViewById(R.id.cardViewDiscover);
        materialCardViewJcb = paymentView.findViewById(R.id.cardViewJCB);
        materialCardViewMc = paymentView.findViewById(R.id.cardViewMC);
        materialCardViewVisa = paymentView.findViewById(R.id.cardViewVisa);
        materialCardViewSepa = paymentView.findViewById(R.id.cardViewSEPA);

        final String piNumberAmex = getString(R.string.payment_card_amex);
        final String piNumberDiscover = getString(R.string.payment_card_discover);
        final String piNumberJcb = getString(R.string.payment_card_jcb);
        final String piNumberMc = getString(R.string.payment_card_mastercard);
        final String piNumberVisa = getString(R.string.payment_card_visa);
        final String piNumberSepa = getString(R.string.payment_card_sepa);

        // AMEX
        materialCardViewAmex.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = new PaymentMethod(PaymentBrand.Amex, piNumberAmex);
                sfaSharedDataModel.setCURRENT_PAYMENT_METHOD(paymentMethod);
                selectPaymentMethod(PaymentBrand.Amex);
                Log.v(TAG, R.string.message_selected + PaymentBrand.Amex.name());
            }
        });

        // Discover
        materialCardViewDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = new PaymentMethod(PaymentBrand.Discover, piNumberDiscover);
                sfaSharedDataModel.setCURRENT_PAYMENT_METHOD(paymentMethod);
                selectPaymentMethod(PaymentBrand.Discover);
                Log.v(TAG, R.string.message_selected + PaymentBrand.Discover.name());
            }
        });

        // JCB
        materialCardViewJcb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = new PaymentMethod(PaymentBrand.JCB, piNumberJcb);
                sfaSharedDataModel.setCURRENT_PAYMENT_METHOD(paymentMethod);
                selectPaymentMethod(PaymentBrand.JCB);
                Log.v(TAG, R.string.message_selected + PaymentBrand.JCB.name());
            }
        });

        // Mastercard
        materialCardViewMc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = new PaymentMethod(PaymentBrand.Mastercard, piNumberMc);
                sfaSharedDataModel.setCURRENT_PAYMENT_METHOD(paymentMethod);
                selectPaymentMethod(PaymentBrand.Mastercard);
                Log.v(TAG, R.string.message_selected + PaymentBrand.Mastercard.name());
            }
        });

        // Visa
        materialCardViewVisa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = new PaymentMethod(PaymentBrand.Visa, piNumberVisa);
                sfaSharedDataModel.setCURRENT_PAYMENT_METHOD(paymentMethod);
                selectPaymentMethod(PaymentBrand.Visa);
                Log.v(TAG, R.string.message_selected + PaymentBrand.Visa.name());
            }
        });

        // SEPA
        materialCardViewSepa.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                paymentMethod = new PaymentMethod(PaymentBrand.SEPA, piNumberSepa);
                sfaSharedDataModel.setCURRENT_PAYMENT_METHOD(paymentMethod);
                selectPaymentMethod(PaymentBrand.SEPA);
                Log.v(TAG, R.string.message_selected + PaymentBrand.SEPA.name());
            }
        });

        // Forward arrow (checkout button)
        checkoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (paymentCardSelected) {
                    if (sfaSharedDataModel.getCURRENT_PAYMENT_METHOD() != null) {
                        Cart cart = new Cart();
                        cart.setMerchantId(merchantId);
                        cart.setProducts(sfaSharedDataModel.getCurrentProductsList().values());
                        cart.setTotalProducts(sfaSharedDataModel.getTotalProducts());
                        cart.setTotalPrice(sfaSharedDataModel.getTotalPrice());
                        cart.setPaymentMethod(sfaSharedDataModel.getCURRENT_PAYMENT_METHOD());
                        sfaSharedDataModel.setCURRENT_CART(cart);
                        Log.v(TAG, "Set Cart: " + sfaSharedDataModel.getCURRENT_CART().toString());
                        Log.v(TAG, getString(R.string.message_check_out));
                        goToCheckout();
                    }
                } else {
                    Toast.makeText(getContext(), R.string.message_payment_method_not_selected, Toast.LENGTH_SHORT).show();
                }
            }
        });

        return paymentView;
    }

    // Navigates back to home page
    private void goHome() {
        NavController navController = Navigation.findNavController(paymentView);
        navController.navigate(R.id.nav_home);
    }

    // Navigates back to gallery page
    private void goToGallery() {
        NavController navController = Navigation.findNavController(paymentView);
        navController.navigate(R.id.nav_gallery);
    }

    // Navigates to the checkout page
    private void goToCheckout() {
        NavController navController = Navigation.findNavController(paymentView);
        navController.navigate(R.id.nav_checkout);
    }

    /**
     * Private method to select the chosen card and deselect all others in the view
     * @param brand PaymentBrand The chosen payment instrument for the transaction
     */
    private void selectPaymentMethod(PaymentBrand brand) {
        switch (brand) {
            case Amex:
                materialCardViewAmex.setChecked(true);
                materialCardViewAmex.setCardElevation(1);
                materialCardViewDiscover.setChecked(false);
                materialCardViewDiscover.setCardElevation(0);
                materialCardViewJcb.setChecked(false);
                materialCardViewJcb.setCardElevation(0);
                materialCardViewMc.setChecked(false);
                materialCardViewMc.setCardElevation(0);
                materialCardViewVisa.setChecked(false);
                materialCardViewVisa.setCardElevation(0);
                materialCardViewSepa.setChecked(false);
                materialCardViewSepa.setCardElevation(0);
                break;
            case Discover:
                materialCardViewAmex.setChecked(false);
                materialCardViewAmex.setCardElevation(0);
                materialCardViewDiscover.setChecked(true);
                materialCardViewDiscover.setCardElevation(1);
                materialCardViewJcb.setChecked(false);
                materialCardViewJcb.setCardElevation(0);
                materialCardViewMc.setChecked(false);
                materialCardViewMc.setCardElevation(0);
                materialCardViewVisa.setChecked(false);
                materialCardViewVisa.setCardElevation(0);
                materialCardViewSepa.setChecked(false);
                materialCardViewSepa.setCardElevation(0);
                break;
            case JCB:
                materialCardViewAmex.setChecked(false);
                materialCardViewAmex.setCardElevation(0);
                materialCardViewDiscover.setChecked(false);
                materialCardViewDiscover.setCardElevation(0);
                materialCardViewJcb.setChecked(true);
                materialCardViewJcb.setCardElevation(1);
                materialCardViewMc.setChecked(false);
                materialCardViewMc.setCardElevation(0);
                materialCardViewVisa.setChecked(false);
                materialCardViewVisa.setCardElevation(0);
                materialCardViewSepa.setChecked(false);
                materialCardViewSepa.setCardElevation(0);
                break;
            case Mastercard:
                materialCardViewAmex.setChecked(false);
                materialCardViewAmex.setCardElevation(0);
                materialCardViewDiscover.setChecked(false);
                materialCardViewDiscover.setCardElevation(0);
                materialCardViewJcb.setChecked(false);
                materialCardViewJcb.setCardElevation(0);
                materialCardViewMc.setChecked(true);
                materialCardViewMc.setCardElevation(1);
                materialCardViewVisa.setChecked(false);
                materialCardViewVisa.setCardElevation(0);
                materialCardViewSepa.setChecked(false);
                materialCardViewSepa.setCardElevation(0);
                break;
            case Visa:
                materialCardViewAmex.setChecked(false);
                materialCardViewAmex.setCardElevation(0);
                materialCardViewDiscover.setChecked(false);
                materialCardViewDiscover.setCardElevation(0);
                materialCardViewJcb.setChecked(false);
                materialCardViewJcb.setCardElevation(0);
                materialCardViewMc.setChecked(false);
                materialCardViewMc.setCardElevation(0);
                materialCardViewVisa.setChecked(true);
                materialCardViewVisa.setCardElevation(1);
                materialCardViewSepa.setChecked(false);
                materialCardViewSepa.setCardElevation(0);
                break;
            case SEPA:
                materialCardViewAmex.setChecked(false);
                materialCardViewAmex.setCardElevation(0);
                materialCardViewDiscover.setChecked(false);
                materialCardViewDiscover.setCardElevation(0);
                materialCardViewJcb.setChecked(false);
                materialCardViewJcb.setCardElevation(0);
                materialCardViewMc.setChecked(false);
                materialCardViewMc.setCardElevation(0);
                materialCardViewVisa.setChecked(false);
                materialCardViewVisa.setCardElevation(0);
                materialCardViewSepa.setChecked(true);
                materialCardViewSepa.setCardElevation(1);
                break;
            default:
                materialCardViewAmex.setChecked(false);
                materialCardViewAmex.setCardElevation(0);
                materialCardViewDiscover.setChecked(false);
                materialCardViewDiscover.setCardElevation(0);
                materialCardViewJcb.setChecked(false);
                materialCardViewJcb.setCardElevation(0);
                materialCardViewMc.setChecked(false);
                materialCardViewMc.setCardElevation(0);
                materialCardViewVisa.setChecked(false);
                materialCardViewVisa.setCardElevation(0);
                materialCardViewSepa.setChecked(false);
                materialCardViewSepa.setCardElevation(0);
                break;
        }
        if (materialCardViewAmex.isChecked() ||
            materialCardViewDiscover.isChecked() ||
            materialCardViewJcb.isChecked() ||
            materialCardViewMc.isChecked() ||
            materialCardViewVisa.isChecked() ||
            materialCardViewSepa.isChecked()) {
                paymentCardSelected = true;
        } else {
                paymentCardSelected = false;
        }
    }
}