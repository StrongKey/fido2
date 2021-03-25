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
 * Checkout fragment to prompt user for confirmation of payment
 * transaction.
 */

package com.strongkey.sfaeco.ui;

import android.app.KeyguardManager;
import android.content.Intent;
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
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.google.android.material.snackbar.Snackbar;
import com.strongkey.sacl.impl.FidoAuthenticatorImpl;
import com.strongkey.sacl.interfaces.FidoAuthenticator;
import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclSharedDataModel;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.main.MainActivity;
import com.strongkey.sfaeco.main.SfaSharedDataModel;
import com.strongkey.sfaeco.roomdb.Cart;
import com.strongkey.sfaeco.roomdb.Product;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.roomdb.UserTransaction;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.nio.charset.StandardCharsets;
import java.security.Signature;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.Currency;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executor;

import static android.app.Activity.RESULT_OK;
import static android.content.Context.KEYGUARD_SERVICE;
import static java.lang.Thread.sleep;

public class CheckoutFragment extends Fragment {

    private String TAG = CheckoutFragment.class.getSimpleName();

    private SfaSharedDataModel sfaSharedDataModel;
    private SaclSharedDataModel saclSharedDataModel;
    private FidoAuthenticator fidoAuthenticator;

    // Local variables
    private Fragment signingActivity;
    private Executor callbackExecutor;
    private BiometricPrompt.CryptoObject cryptoObject;
    private PreauthorizeChallenge preauthorizeChallenge;
    private String paymentConfirmation;
    private User user;
    private Cart cart;
    private PublicKeyCredential pkc;
    private UserTransaction userTransaction;
    private String credentialId;
    private String txid;
    private JSONObject txpayload;

    View checkoutView;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        checkoutView = inflater.inflate(R.layout.fragment_checkout, container, false);
        sfaSharedDataModel = Common.getSfaSharedDataModel();
        saclSharedDataModel = Common.getSaclSharedDataModel();

        fidoAuthenticator = new FidoAuthenticatorImpl(getContext());

        // Get needed widgets from fragment
        Button submitButton = checkoutView.findViewById(R.id.submit_button);
        TextView t100QtyTextView = checkoutView.findViewById(R.id.t100Quantity);
        TextView e1000QtyTextView = checkoutView.findViewById(R.id.e1000Quantity);
        TextView fidoQtyCloudTextView = checkoutView.findViewById(R.id.fidoCloudQuantity);
        TextView tellaroQtyCloudTextView = checkoutView.findViewById(R.id.tellaroCloudQuantity);
        TextView t100NameTextView = checkoutView.findViewById(R.id.t100Name);
        TextView e1000NameTextView = checkoutView.findViewById(R.id.e1000Name);
        TextView fidoNameCloudTextView = checkoutView.findViewById(R.id.fidoCloudName);
        TextView tellaroNameCloudTextView = checkoutView.findViewById(R.id.tellaroCloudName);
        TextView totalPriceTextView = checkoutView.findViewById(R.id.checkoutTotalPrice);
        totalPriceTextView.setText("$0");;


        // Get User object and credentialId from user's PublicKeyCredential
        // object in saclSharedDataModel
        user = sfaSharedDataModel.getCurrentUserObject();
        if (user == null) {
            Toast.makeText(getContext(), R.string.error_null_user, Toast.LENGTH_SHORT).show();
            return checkoutView;
        }

        pkc = saclSharedDataModel.getCurrentPublicKeyCredential();
        if (pkc != null) {
            credentialId = pkc.getCredentialId();
            Log.v(TAG, "CREDENTIALID=" + credentialId);
        } else {
            Toast.makeText(getContext(), R.string.error_null_user, Toast.LENGTH_SHORT).show();
            return checkoutView;
        }

        // Get current Cart - remember, cart is a business application object, so it
        // will be in the SFA model, while the challenge is a SACL object, so it will
        // be in the SACL model
        cart = sfaSharedDataModel.getCURRENT_CART();

        // Update checkOutView with quantities and total price
        Collection<Product> products =  cart.getProducts();
        Log.d(TAG, "TextViewSize of Products: " + products.size());
        for (Product p : products) {
            Log.d(TAG, "p.getName()TextView: " + p.getName());
            if (p.getName().equalsIgnoreCase(t100NameTextView.getText().toString())) {
                t100QtyTextView.setText(getString(R.string.product_quantity).concat(" 1"));
                Log.d(TAG, "t100QtyTextView: " + t100QtyTextView.getText().toString());
            } else if (p.getName().equalsIgnoreCase(e1000NameTextView.getText().toString())) {
                e1000QtyTextView.setText(getString(R.string.product_quantity).concat(" 1"));
                Log.d(TAG, "e1000QtyTextView: " + e1000QtyTextView.getText().toString());
            } else if (p.getName().equalsIgnoreCase(fidoNameCloudTextView.getText().toString())) {
                fidoQtyCloudTextView.setText(getString(R.string.product_quantity).concat(" 1"));
                Log.d(TAG, "fidoQtyCloudTextView: " + fidoQtyCloudTextView.getText().toString());
            } else if (p.getName().equalsIgnoreCase(tellaroNameCloudTextView.getText().toString())) {
                tellaroQtyCloudTextView.setText(getString(R.string.product_quantity).concat(" 1"));
                Log.d(TAG, "tellaroQtyCloudTextView: " + tellaroQtyCloudTextView.getText().toString());
            }
        }

        NumberFormat formatter = NumberFormat.getCurrencyInstance();
        formatter.setCurrency(Currency.getInstance(Locale.US));
        formatter.setMaximumFractionDigits(0);
        totalPriceTextView.setText(formatter.format(cart.getTotalPrice()));

        // Initialize Executor and Activity
        callbackExecutor = ContextCompat.getMainExecutor(getContext());
        signingActivity = this;

        /**
         * See if we should continue setting up BiometricPrompt.
         */
        if (sfaSharedDataModel.isBiometricAvailable()) {

            submitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    preauthorizeChallenge = (PreauthorizeChallenge)
                            com.strongkey.sacl.utilities.Common.getCurrentObject(
                            Constants.SACL_OBJECT_TYPES.PREAUTHORIZE_CHALLENGE);
                    if (cart != null) {
                        Log.v(TAG, "Got cart: " + cart.toString());
                        if (cart.getTotalProducts() < 1) {
                            Toast.makeText(getContext(), R.string.message_products_not_selected, Toast.LENGTH_SHORT).show();
                            return;
                        } else if (cart.getPaymentMethod() == null) {
                            Toast.makeText(getContext(), R.string.message_payment_method_not_selected, Toast.LENGTH_SHORT).show();
                            return;
                        } else if (preauthorizeChallenge == null) {
                            // Get a challenge for the transaction authorization from FIDO server
                            Log.v(TAG, "Need to get PreauthorizeChallenge from FIDO server");
                            getAuthorizationChallenge(user.getDid(), user.getUid(), cart);
                        }
                    } else {
                        Toast.makeText(getContext(), R.string.message_products_not_selected, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Sleep while waiting for preauthorization webservice to complete
                    // TODO:  Should only do this for a second or 2 and then fail
                    try {
                        while (preauthorizeChallenge == null) {
                            preauthorizeChallenge = (PreauthorizeChallenge) com.strongkey.sacl.utilities.Common.getCurrentObject(
                                    Constants.SACL_OBJECT_TYPES.PREAUTHORIZE_CHALLENGE);
                            sleep(500);
                        }
                    } catch (InterruptedException e) {
                        Log.v(TAG, "All OK with cart and preauthorizeChallenge..");
                    }

                    /*
                     * Parse out details of the dynamic link for signing. It should like:
                     *
                     * {
	                 *      "txpayload": {
		             *          "txid": "ECO-12345",
		             *          "txdate": "202103091210PST",
		             *          "merchantName": "StrongKey",
		             *          "currency": "USD",
		             *          "totalPrice": "6995.00",
		             *          "cardBrand": "Visa",
		             *          "cardLast4": "x-1234"
	                 *      }
                     * }
                     *
                     */
                    txid = preauthorizeChallenge.getTxid();
                    String b64utxpayload = preauthorizeChallenge.getTxpayload();
                    txpayload = parseTxpayload(b64utxpayload);

                    try {
                        // Build out UserTransaction object
                        userTransaction = new UserTransaction();
                        userTransaction.setId(0);
                        userTransaction.setDid(user.getDid());
                        userTransaction.setSid(user.getSid());
                        userTransaction.setUid(user.getUid());
                        userTransaction.setTxid(preauthorizeChallenge.getTxid());
                        userTransaction.setTxdate(new Date().getTime());
                        userTransaction.setTxpayload(preauthorizeChallenge.getTxpayload());
                        userTransaction.setMerchantId(cart.getMerchantId());
                        userTransaction.setTotalProducts(cart.getTotalProducts());
                        userTransaction.setProducts(cart.getProducts());
                        userTransaction.setTotalPrice(cart.getTotalPrice());
                        userTransaction.setCurrency(SfaConstants.TRANSACTION_CURRENCY.USD); // TODO: Fix this
                        userTransaction.setPaymentMethod(cart.getPaymentMethod());
                        userTransaction.setChallenge(preauthorizeChallenge.getChallenge());
                        sfaSharedDataModel.setCURRENT_USER_TRANSACTION(userTransaction);

                        NumberFormat numberFormat = NumberFormat.getIntegerInstance(Locale.US);
                        int txpayloadTotalPrice = Integer.valueOf(txpayload.getString(SfaConstants.SFA_ECO_CART_TOTAL_PRICE_LABEL));

                        // Build up the dynamic link the user must confirm with digital signature
                        paymentConfirmation = getString(R.string.message_pay) + " " +
                                txpayload.getString(SfaConstants.SFA_ECO_CART_MERCHANT_NAME_LABEL) + " " +
                                txpayload.getString(SfaConstants.SFA_ECO_CART_CURRENCY_LABEL) +
                                numberFormat.format(txpayloadTotalPrice) + " " + getString(R.string.label_with) + " " +
                                txpayload.getString(SfaConstants.SFA_ECO_CART_PAYMENT_METHOD_CARD_BRAND_LABEL) + " [" +
                                txpayload.getString(SfaConstants.SFA_ECO_CART_PAYMENT_METHOD_CARD_LAST4_LABEL) + "]";
//                                + '\n' +
//                                getString(R.string.message_txid_abbreviation) + ": " +
//                                txpayload.getString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXID) + '\n' +
//                                getString(R.string.message_date) + ": " +
//                                txpayload.getString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXDATE);


                    // Create the BiometricPrompt and its CryptoObject
                    Signature signatureObject = null;
                    Object response = fidoAuthenticator.getSignatureObject(credentialId, new LocalContextWrapper(getContext()));
                    if (response != null) {
                        if (response instanceof JSONObject) {
                            JSONObject jo = (JSONObject) response;
                            Toast.makeText(getContext(), jo.toString(), Toast.LENGTH_LONG);
                            return;
                        } else {
                            signatureObject = (Signature) response;
                        }
                    }
                    cryptoObject = new BiometricPrompt.CryptoObject(signatureObject);
                    sfaSharedDataModel.setCurrentSignatureObject(signatureObject);

                    BiometricPrompt biometricPrompt = createBiometricPrompt(signingActivity.getActivity(), callbackExecutor, paymentConfirmation);
                    BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                            .setTitle(getString(R.string.message_payment_authorization))
                            .setSubtitle(paymentConfirmation)
                            .setDescription(getString(R.string.message_txid) + " " +
                                    txpayload.getString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXID) +
                                    getString(R.string.label_date) + " " +
                                    txpayload.getString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXDATE))
                            .setConfirmationRequired(true)
                            .setNegativeButtonText(getString(R.string.label_cancel))
                            .build();

                    // Do the fingerprint thing
                    biometricPrompt.authenticate(promptInfo, cryptoObject);

                    } catch (JSONException je) {
                        je.printStackTrace();
                    }
                }
            });
        }
        return checkoutView;
    }

    /**
     * Gets an authorization challenge from the FIDO server
     * @param did int value of the cryptographic domain
     * @param uid long value of the user's unique ID in the application
     * @param cart Cart object which will get converted to a JSON string
     */
    private void getAuthorizationChallenge(int did, long uid, Cart cart) {
        String MTAG = "getAuthorizationChallenge";
        Log.d(TAG, MTAG + getString(R.string.message_getting_authorization_challenge));

        if (MainActivity.mSfaChildHandler != null) {
            Bundle mBundle = new Bundle();
            Message msg = MainActivity.mSfaChildHandler.obtainMessage();
            mBundle.putString(SfaConstants.SFA_TASK, SfaConstants.SFA_TASKS.SFA_TASK_GET_AUTHORIZATION_CHALLENGE.name());
            mBundle.putInt(SfaConstants.JSON_KEY_DID, did);
            mBundle.putLong(SfaConstants.JSON_KEY_UID, uid);
            mBundle.putString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_CART, cart.toJson().toString());
            msg.setData(mBundle);
            MainActivity.mSfaChildHandler.sendMessage(msg);

            // Webservice needs at least a second to authorize
            try {
                // Sleep for 1 second
                Log.v(TAG, "Calling FIDO Preauthorize webservice..." );
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            // Now loop back to this fragment to process preauthorization challenge
            NavController navController = Navigation.findNavController(checkoutView);
            navController.navigate(R.id.nav_checkout);
        } else {
            Log.d(TAG, MTAG + R.string.error_null_handler + "mSfaChildHandler");
        }
    }

    /**
     * Parse transaction payload to present user with readable prompt for providing
     * confirmation with FIDO digital signature
     * @param b64uTxpayload Base64Url-encoded JSONObject containing details that make up
     * the details of the dynamic link
     * @return JSONObject
     */
    private JSONObject parseTxpayload(String b64uTxpayload) {
        String txpayload = new String(com.strongkey.sacl.utilities.Common.urlDecode(b64uTxpayload), StandardCharsets.UTF_8);
        try {
            return (JSONObject) new JSONTokener(txpayload).nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a BiometricPrompt for signing transaction with key in AndroidKeystore
     *
     * @param fragment  Fragment that will handle the signing operation
     * @param executor  Executor that will handle callbacks from BiometricPrompt
     * @param paymentConfirmation String with JSON serialized transaction data
     * @return BiometricPrompt
     */
    private BiometricPrompt createBiometricPrompt(FragmentActivity fragment, Executor executor, String paymentConfirmation) {

        return new BiometricPrompt(fragment, executor, new BiometricPrompt.AuthenticationCallback() {
            /**
             * Error during failed biometric authentication
             * @param errorCode
             * @param errString
             */
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Log.w(TAG, getString(R.string.error_biometric_authentication) + errString);
                if (errString.equals(getString(R.string.label_use_pin))) {
                    KeyguardManager keyguardManager = (KeyguardManager) fragment.getSystemService(KEYGUARD_SERVICE);
                    if (keyguardManager.isDeviceSecure()) {
                        Intent authIntent = keyguardManager.createConfirmDeviceCredentialIntent(
                                getString(R.string.label_transaction_authorization), getString(R.string.message_use_device_pin));
                        int requestCode = 1;
                        startActivityForResult(authIntent, requestCode);
                    }
                }

                if (errString.equals(R.string.label_cancel)) {
                    Snackbar.make(checkoutView.findViewById(android.R.id.content), R.string.label_transaction_cancelled, Snackbar.LENGTH_SHORT)
                            .setAction(R.string.label_action, null)
                            .show();
                }
            }

            /**
             * Failure during biometric authentication
             */
            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Log.w(TAG, getString(R.string.message_fingerprint_auth_failure));
            }

            /**
             * Successful biometric authentication. Signs transaction and navigates to
             * checkout fragment page
             * @param result BiometricPrompt.AuthenticationResult
             */
            @Override
            public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Log.w(TAG, getString(R.string.message_fingerprint_auth_success));
                authorizeTransaction(user, userTransaction, preauthorizeChallenge);

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Log.w(TAG, getString(R.string.message_pin_pattern_auth_success));
                authorizeTransaction(user, userTransaction, preauthorizeChallenge);
            }
        }
    }

    /**
     * Authorize transaction with FIDO key to the SFA's FIDO server
     * @param mUser User object with user data
     * @param mUserTransaction UserTransaction object partial data - rest come after
     * the transaction is authorized by the user and verified by the FIDO server.
     */
    private void authorizeTransaction(User mUser, UserTransaction mUserTransaction, PreauthorizeChallenge preauthorizeChallenge) {
        String MTAG = "authorizeTransaction";
        Log.d(TAG, MTAG + getString(R.string.message_authorize_transaction));

        if (MainActivity.mSfaChildHandler != null) {
            Bundle mBundle = new Bundle();
            Message msg = MainActivity.mSfaChildHandler.obtainMessage();
            mBundle.putString(SfaConstants.SFA_TASK, SfaConstants.SFA_TASKS.SFA_TASK_AUTHORIZE_FIDO_TRANSACTION.name());
            mBundle.putInt(SfaConstants.JSON_KEY_DID, mUser.getDid());
            mBundle.putLong(SfaConstants.JSON_KEY_UID, mUser.getUid());
            mBundle.putString(SfaConstants.JSON_KEY_USER_USERNAME, mUser.getUsername());
            mBundle.putString(SfaConstants.JSON_KEY_TXID, mUserTransaction.getTxid());
            mBundle.putString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_TXPAYLOAD, mUserTransaction.getTxpayload());
            mBundle.putString(SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_SIGNATURE_OBJECT_LABEL,
                    SfaConstants.SFA_ECO_PAYMENT_TX_FIDO_SIGNATURE_OBJECT);
            mBundle.putString(SfaConstants.JSON_KEY_USER_CREDENTIALID, mUser.getCredentialId());
            mBundle.putString(SfaConstants.SFA_ECO_PAYMENT_TRANSACTION_CHALLENGE, preauthorizeChallenge.getChallenge());
            msg.setData(mBundle);
            MainActivity.mSfaChildHandler.sendMessage(msg);

            // Webservice needs at least a second to authorize
            try {
                // Sleep for 1 second
                Log.v(TAG, "Calling FIDO Transaction Authorization webservice..." );
                sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        } else {
            Log.d(TAG, MTAG + R.string.error_null_handler + "mSfaChildHandler");
        }
    }
}
