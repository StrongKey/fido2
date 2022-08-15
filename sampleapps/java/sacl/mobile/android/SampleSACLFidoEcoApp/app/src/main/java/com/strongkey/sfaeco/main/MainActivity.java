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
 * The Main Activity of the program.
 */

package com.strongkey.sfaeco.main;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.biometric.BiometricManager;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.strongkey.sacl.roomdb.AuthenticationSignature;
import com.strongkey.sacl.roomdb.AuthorizationSignature;
import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclSharedDataModel;
import com.strongkey.sfaeco.R;
import com.strongkey.sfaeco.asynctasks.PersistObjectTask;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.roomdb.UserTransaction;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.SfaConstants;

import org.json.JSONException;

import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private AppBarConfiguration mAppBarConfiguration;

    // Handlers for messaging between fragments in the main UI thread and background SACL thread
    public static Handler mSfaMainHandler, mSfaChildHandler;
    public static String mHandlerName;
    public static Thread mSfaThread;

    // Shared ViewModel for this app
    public static SfaSharedDataModel sfaSharedDataModel;
    public static SaclSharedDataModel saclSharedDataModel;

    // Number of threads for persisting objects to RoomDB
    private final int NUMBER_OF_THREADS = 2;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        View root = findViewById(R.id.nav_host_fragment);

        // Setup ViewModel
        sfaSharedDataModel = new ViewModelProvider(this).get(SfaSharedDataModel.class);
        saclSharedDataModel = new SaclSharedDataModel();
        Common.putSaclSharedDataModel(saclSharedDataModel);
        Common.putSfaSharedViewModel(sfaSharedDataModel);

        // AsyncTask - Initialize database repository
        new InitializeSfaRepositoryTask().execute(getApplication(), sfaSharedDataModel);

        // Create ExecutorService for background tasks
        ExecutorService executorService = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

        // Check if we have biometrics on this device
        BiometricManager biometricManager = BiometricManager.from(this);
        switch (biometricManager.canAuthenticate()) {
            case BiometricManager.BIOMETRIC_SUCCESS:
                Log.d(TAG, "App can authenticate using biometrics.");
                sfaSharedDataModel.setBiometricIsAvailable(Boolean.TRUE);
                break;
            case BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE:
                Log.e(TAG, "No biometric features available on this device");
                Snackbar.make(root, "No biometric features available on this device", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE:
                Log.e(TAG, "Biometric features are currently unavailable.");
                Snackbar.make(root, "Biometric features are currently unavailable", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
                break;
            case BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED:
                Log.e(TAG, "No biometric credentials enrolled on this device");
                Snackbar.make(root, "No biometric credentials enrolled on this device", Snackbar.LENGTH_LONG)
                        .setAction("Action", null)
                        .show();
                break;
        }
        
        // Toolbar stuff
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Navigation drawer
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);

        // Passing each menu ID as a set of Ids
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home,
                R.id.nav_retrieve_user,
                R.id.nav_enroll_user,
                R.id.nav_register_fido_key,
                R.id.nav_registered_fido_key,
                R.id.nav_fido_authentication,
                R.id.nav_gallery)
                .setOpenableLayout(drawer)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);

        // Define the Handler that receives messages from SFA thread
        mSfaMainHandler = new Handler() {
            public void handleMessage(Message msg) {

                String response;
                Bundle mBundle = msg.getData();
                Set<String> keyset = mBundle.keySet();
                for (String key : keyset) {
                    switch (key) {
                        case SfaConstants.SFA_TASK_REGISTER_USER_RESPONSE_JSON:
                            response = mBundle.getString(SfaConstants.SFA_TASK_REGISTER_USER_RESPONSE_JSON);
                            Log.d(TAG, getString(R.string.message_received) + response);
                            if (response.contains("error")) {
                                Toast.makeText(getApplicationContext(), response, Toast.LENGTH_LONG).show();
                            } else {
                                User mUser = new User();
                                mUser.setPassword("Abcd1234!"); // Not returned by server in Json
                                if (mUser.parseUserJsonString(response)) {
                                    sfaSharedDataModel.setCurrentUser(mUser);
                                    executorService.submit(new PersistObjectTask(getApplication(), "User", mUser));
                                    navigateToRegisteredUser(mBundle);
                                } else {
                                    Toast.makeText(getApplicationContext(), R.string.error_null_user, Toast.LENGTH_LONG).show();
                                }
                            }
                            break;
                        case SfaConstants.SFA_TASK_REGISTER_FIDO_KEY_RESPONSE_OBJECT:
                            response = mBundle.getString(SfaConstants.SFA_TASK_REGISTER_FIDO_KEY_RESPONSE_OBJECT);
                            Log.d(TAG, getString(R.string.message_received) + response);
                            PublicKeyCredential publicKeyCredential = new PublicKeyCredential();
                            if (publicKeyCredential.parsePublicKeyCredentialJsonString(response)) {
                                saclSharedDataModel.setCurrentPublicKeyCredential(publicKeyCredential);
                                navigateToRegisteredFidoKey(mBundle);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.fido_error_registration, Toast.LENGTH_LONG).show();
                            }
                            break;
                        case SfaConstants.SFA_TASK_AUTHENTICATE_FIDO_KEY_RESPONSE_OBJECT:
                            response = mBundle.getString(SfaConstants.SFA_TASK_AUTHENTICATE_FIDO_KEY_RESPONSE_OBJECT);
                            Log.d(TAG, getString(R.string.message_received) + response);
                            AuthenticationSignature authenticationSignature = new AuthenticationSignature();
                            if (authenticationSignature.parseAuthenticationSignatureJsonString(response)) {
                                saclSharedDataModel.setCurrentAuthenticationSignature(authenticationSignature);
                                navigateToAuthenticatedFidoKey(mBundle);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.fido_error_authentication, Toast.LENGTH_LONG).show();
                            }
                            break;
                        case SfaConstants.SFA_TASK_GET_AUTHORIZATION_CHALLENGE_RESPONSE_OBJECT:
                            response = mBundle.getString(SfaConstants.SFA_TASK_GET_AUTHORIZATION_CHALLENGE_RESPONSE_OBJECT);
                            Log.d(TAG, getString(R.string.message_received) + response);
                            PreauthorizeChallenge preauthorizeChallenge = new PreauthorizeChallenge();
                            preauthorizeChallenge.setDid(mBundle.getInt("did"));
                            preauthorizeChallenge.setUid(mBundle.getLong("uid"));
                            if (preauthorizeChallenge.parsePreauthorizeChallengeResponse(response)) {
                                saclSharedDataModel.setCurrentPreauthorizeChallenge(preauthorizeChallenge);
                                navigateToCheckout(mBundle);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.fido_error_authentication, Toast.LENGTH_LONG).show();
                            }
                            break;
                        case SfaConstants.SFA_TASK_AUTHORIZE_FIDO_TRANSACTION_RESPONSE_OBJECT:
                            response = mBundle.getString(SfaConstants.SFA_TASK_AUTHORIZE_FIDO_TRANSACTION_RESPONSE_OBJECT);
                            Log.v(TAG, getString(R.string.message_received) + response);
                            AuthorizationSignature authorizationSignature = new AuthorizationSignature();
                            if (authorizationSignature.parseAuthorizationSignatureJsonString(response)) {
                                UserTransaction userTransaction = sfaSharedDataModel.getCURRENT_USER_TRANSACTION();
                                int n = 0;
                                try {
                                    n = userTransaction.storeFidoAuthenticatorReferences(authorizationSignature.getResponseJson());
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                saclSharedDataModel.setCurrentAuthorizationSignature(authorizationSignature);
                                Log.v(TAG, "Stored FidoAuthenticatorReferences: " + n);
                                navigateToCompletion(mBundle);
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.fido_error_authentication, Toast.LENGTH_LONG).show();
                            }
                            break;
                        case SfaConstants.SFA_TASK_RETRIEVE_USER_RESPONSE_JSON:
                            response = mBundle.getString(SfaConstants.SFA_TASK_RETRIEVE_USER_RESPONSE_OBJECT);
                            Log.d(TAG, getString(R.string.message_received) + response);
                            User mUser = sfaSharedDataModel.getCurrentUser().getValue();
                            navigateToRegisteredFidoKey(mBundle);
                        default:
                            Log.d(TAG, getString(R.string.message_received_unknown) + key);
                    }
                }
            }
        };
        mHandlerName = mSfaMainHandler.getLooper().getThread().getName();
        mSfaThread = new SfaThreadManager(mSfaMainHandler, getApplicationContext());
        mSfaThread.start();
        Log.i(TAG, getString(R.string.message_started_mainhandler) + mHandlerName);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }


    // Navigates to home page when the Up button is selected
    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    /**
     * Display retrieve user layout to get user from RoomDB
     * @param mBundle Bundle carrying saved values
     */
    private void navigateToRetrieveUser(Bundle mBundle) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_retrieve_user, mBundle);
    }

    /**
     * Display registered user and prepare to register FIDO key
     * @param mBundle Bundle carrying saved values
     */
    private void navigateToRegisteredUser(Bundle mBundle) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_register_fido_key, mBundle);
    }

    /**
     * Display registered FIDO key and prepare to authenticate with FIDO key
     * @param mBundle Bundle carrying saved values
     */
    private void navigateToRegisteredFidoKey(Bundle mBundle) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_registered_fido_key, mBundle);
    }

    /**
     * Display authenticated FIDO key
     * @param mBundle Bundle carrying saved values
     */
    private void navigateToAuthenticatedFidoKey(Bundle mBundle) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_fido_authentication, mBundle);
    }

    /**
     * Navigate to the gallery of products
     * @param view View
     */
    public void navigateToGallery(View view) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_gallery, null);
    }

    /**
     * Navigate to payment method selection
     * @param view View
     */
    public void navigateToPayment(View view) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_payment, null);
    }

    /**
     * Checkout
     * @param mBundle Bundle carrying saved values
     */
    private void navigateToCheckout(Bundle mBundle) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_checkout, mBundle);
    }

    /**
     * Completion
     * @param mBundle Bundle carrying saved values
     */
    private void navigateToCompletion(Bundle mBundle) {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        navController.navigate(R.id.nav_completion, mBundle);
    }
}
