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
 * A class to share information between fragments while setting up
 * observers to watch for changes.
 */

package com.strongkey.sfaeco.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.strongkey.sacl.roomdb.PreauthorizeChallenge;
import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sfaeco.roomdb.Cart;
import com.strongkey.sfaeco.roomdb.UserTransaction;
import com.strongkey.sfaeco.roomdb.PaymentMethod;
import com.strongkey.sfaeco.roomdb.Product;
import com.strongkey.sfaeco.roomdb.SfaRepository;
import com.strongkey.sfaeco.roomdb.User;
import com.strongkey.sfaeco.roomdb.UserDevice;

import java.security.Signature;
import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SfaSharedDataModel extends ViewModel {

    private MutableLiveData<SfaRepository> SFA_REPOSITORY;

    private MutableLiveData<User> CURRENT_USER;
    private MutableLiveData<UserDevice> CURRENT_USER_DEVICE;
    private MutableLiveData<PublicKeyCredential> CURRENT_USER_PUBLIC_KEY_CREDENTIAL;

    private MutableLiveData<Cart> CURRENT_CART;
    private MutableLiveData<Map<String, Product>> CURRENT_PRODUCTS;
    private MutableLiveData<X509Certificate> CURRENT_X509_CERTIFICATE;
    private MutableLiveData<PaymentMethod> CURRENT_PAYMENT_METHOD;
    private int totalPrice = 0;
    private int totalProducts = 0;
    private MutableLiveData<UserTransaction> CURRENT_USER_TRANSACTION;

    public ReadOnlyDisplayAction CURRENT_READONLY_DISPLAY_ACTION;
    private Boolean BIOMETRIC_AVAILABLE = Boolean.FALSE;
    private Signature CURRENT_SIGNATURE_OBJECT;
    private PreauthorizeChallenge CURRENT_AUTHORIZATION_CHALLENGE;

    public SfaSharedDataModel() {

        CURRENT_USER = new MutableLiveData<>();
        CURRENT_USER_DEVICE = new MutableLiveData<>();
        CURRENT_USER_PUBLIC_KEY_CREDENTIAL = new MutableLiveData<>();

        Map<String, Product> productList = new ConcurrentHashMap<>();
        CURRENT_CART = new MutableLiveData<>();
        CURRENT_PRODUCTS = new MutableLiveData<>(productList);
        CURRENT_X509_CERTIFICATE = new MutableLiveData<>();
        CURRENT_PAYMENT_METHOD = new MutableLiveData<>();
        CURRENT_USER_TRANSACTION = new MutableLiveData<>();
        CURRENT_READONLY_DISPLAY_ACTION = new ReadOnlyDisplayAction();
    }

    public SfaRepository getSfaRepository() {
        return SFA_REPOSITORY.getValue();
    }

    public void setSfaRepository(SfaRepository sfa_repository) {
        this.SFA_REPOSITORY = new MutableLiveData<>(sfa_repository);
    }

    public PreauthorizeChallenge getCURRENT_AUTHORIZATION_CHALLENGE() {
        return CURRENT_AUTHORIZATION_CHALLENGE;
    }

    public void setCURRENT_AUTHORIZATION_CHALLENGE(PreauthorizeChallenge CURRENT_AUTHORIZATION_CHALLENGE) {
        this.CURRENT_AUTHORIZATION_CHALLENGE = CURRENT_AUTHORIZATION_CHALLENGE;
    }

    public Boolean isBiometricAvailable() {
        return BIOMETRIC_AVAILABLE;
    }

    public void setBiometricIsAvailable(Boolean biometricIsAvailable) {
        BIOMETRIC_AVAILABLE = biometricIsAvailable;
    }

    public Signature getCurrentSignatureObject() { return CURRENT_SIGNATURE_OBJECT; }

    public void setCurrentSignatureObject(Signature signatureObject) {
        this.CURRENT_SIGNATURE_OBJECT = signatureObject;
    }

    public LiveData<User> getCurrentUser() {
        return CURRENT_USER;
    }

    public User getCurrentUserObject() {
        return CURRENT_USER.getValue();
    }

    public void setCurrentUser(User mUser) {
        CURRENT_USER.setValue(mUser);
    }

    public LiveData<UserDevice> getCurrentUserDevice() {
        return CURRENT_USER_DEVICE;
    }

    public UserDevice getCurrentUserDeviceObject() {
        return CURRENT_USER_DEVICE.getValue();
    }

    public void setCurrentUserDevice(UserDevice mUserDevice) {
        CURRENT_USER_DEVICE.setValue(mUserDevice);
    }

    public PublicKeyCredential getCurrentUserPublicKeyCredential() {
        return CURRENT_USER_PUBLIC_KEY_CREDENTIAL.getValue();
    }

    public void setCurrentUserPublicKeyCredential(PublicKeyCredential pkc) {
        this.CURRENT_USER_PUBLIC_KEY_CREDENTIAL.setValue(pkc);
    }

    public Cart getCURRENT_CART() { return CURRENT_CART.getValue(); }

    public void setCURRENT_CART(Cart cart) {
        CURRENT_CART = new MutableLiveData<>(cart);
    }

    public int addProduct(Product mProduct) {
        CURRENT_PRODUCTS.getValue().put(mProduct.getName(), mProduct);
        totalPrice += mProduct.getPrice();
        totalProducts += 1;
        return totalPrice;
    }

    public void removeProduct(String productName) {
        Map<String, Product> productList = CURRENT_PRODUCTS.getValue();
        for (String key : productList.keySet()) {
            if (key.equalsIgnoreCase(productName)) {
                totalPrice -= productList.get(productName).getPrice();
                productList.remove(key);
            }
        }
        totalProducts -= 1;
        CURRENT_PRODUCTS.setValue(productList);
    }

    public Map<String, Product> getCurrentProductsList() {
        return CURRENT_PRODUCTS.getValue();
    }

    public String getCurrentProductsAsString() {
        Map<String, Product> productMap = CURRENT_PRODUCTS.getValue();
        StringBuilder sb = new StringBuilder();
        Set<String> keys = productMap.keySet();
        Iterator<String> iterator = keys.iterator();
        while (iterator.hasNext()) {
            String k = iterator.next();
            sb.append(productMap.get(k).toString() + "\n");
        }
        return sb.toString();
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void clearProductsPricePayment() {
        Map<String, Product> productList = CURRENT_PRODUCTS.getValue();
        for (String key : productList.keySet()) {
            productList.remove(key);
        }
        totalPrice = 0;
        totalProducts = 0;
        CURRENT_X509_CERTIFICATE.setValue(null);
        CURRENT_PAYMENT_METHOD.setValue(null);
    }

    public UserTransaction getCURRENT_USER_TRANSACTION() {
        return CURRENT_USER_TRANSACTION.getValue();
    }

    public void setCURRENT_USER_TRANSACTION(UserTransaction userTransaction) {
        CURRENT_USER_TRANSACTION.setValue(userTransaction);
    }

    public void clearCurrentUserTransaction() {
        CURRENT_CART.setValue(null);
        CURRENT_AUTHORIZATION_CHALLENGE = null;
        CURRENT_USER_TRANSACTION.setValue(null);
    }

    public void setX509Certificate(X509Certificate certificate) {
        CURRENT_X509_CERTIFICATE.setValue(certificate);
    }

    public X509Certificate getX509Certificate() {
        return CURRENT_X509_CERTIFICATE.getValue();
    }

    public PaymentMethod getCURRENT_PAYMENT_METHOD() {
        return CURRENT_PAYMENT_METHOD.getValue();
    }

    public void setCURRENT_PAYMENT_METHOD(PaymentMethod paymentMethod) {
        this.CURRENT_PAYMENT_METHOD.setValue(paymentMethod);
    }



    public ReadOnlyDisplayAction getCURRENT_READONLY_DISPLAY_ACTION() {
        return CURRENT_READONLY_DISPLAY_ACTION;
    }

    public void setCURRENT_READONLY_DISPLAY_ACTION(ReadOnlyDisplayAction CURRENT_READONLY_DISPLAY_ACTION) {
        this.CURRENT_READONLY_DISPLAY_ACTION = CURRENT_READONLY_DISPLAY_ACTION;
    }

    /**
     * Private inner class to indicate what we want to display in the ReadOnly Fragment,
     * as well as pass the String to display in the RO Fragment
     */
    public class ReadOnlyDisplayAction {
        private DisplayAction action;
        private String content;
        private String label;

        ReadOnlyDisplayAction() {
            action = DisplayAction.NULL;
        }

        public DisplayAction getAction() {
            return action;
        }

        public void setAction(DisplayAction action) {
            this.action = action;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }
    }

    public enum DisplayAction {
        AAGUID,
        ATTESTATION,
        AUTHENTICATOR_DATA,
        CBOR_ATTESTATION,
        CLIENT_DATA_JSON,
        DEVICE,
        EMAIL_SECURITY_KEY_LINK,
        ID,
        JSON_ATTESTATION,
        PUBLIC_KEY,
        RAWID,
        SIGNATURE,
        TXPAYLOAD,
        USER_HANDLE,
        NULL}
}