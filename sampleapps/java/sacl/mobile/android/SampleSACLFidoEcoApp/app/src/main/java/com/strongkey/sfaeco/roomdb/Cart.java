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
 * Entity class to represent a simple Cart in which products and the payment
 * method are collected by the app
 */

package com.strongkey.sfaeco.roomdb;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.strongkey.sfaeco.utilities.SfaConstants;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;

//@Entity(tableName = "cart")

public class Cart {

    @Ignore
    String TAG = "Cart";

    @PrimaryKey(autoGenerate = true)
    @NonNull
    private int id;

    @NonNull
    private int merchantId;

    @NonNull
    private Collection<Product> products;

    @NonNull
    private int totalProducts;

    @NonNull
    private int totalPrice;

    @NonNull
    private PaymentMethod paymentMethod;

    /**
     * Empty Constructor
     */
    public Cart() {}


    /**
     * Getters and Setters
     */

    public int getMerchantId() { return merchantId; }

    public void setMerchantId(int merchantId) { this.merchantId = merchantId; }

    public Collection<Product> getProducts() {
        return products;
    }

    public void setProducts(Collection<Product> products) {
        this.products = products;
    }

    public int getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(int totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalPrice() { return totalPrice; }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }


    public String toString() {
        return  "merchantId: " + this.merchantId + ", " +
                "products: " + this.products + ", " +
                "totalProducts: " + this.totalProducts + ", " +
                "totalPrice: " + this.totalPrice + ", " +
                "paymentMethod: " + this.paymentMethod;
    }


    public JSONObject toJson() {
        try {
            // We assume only one each product can be ordered in a cart - keeping it simple
            int size = products.size();

            // First create payment method
            JSONObject payMethod = new JSONObject()
                    .put(SfaConstants.SFA_ECO_CART_PAYMENT_METHOD_CARD_BRAND_LABEL, paymentMethod.getBrand())
                    .put(SfaConstants.SFA_ECO_CART_PAYMENT_METHOD_CARD_LAST4_LABEL, paymentMethod.getNumber());

            // Create cart
            JSONObject cart = new JSONObject()
                    .put(SfaConstants.SFA_ECO_CART_LABEL, new JSONObject()
                            .put(SfaConstants.SFA_ECO_CART_MERCHANT_ID_LABEL, merchantId)
                            .put(SfaConstants.SFA_ECO_CART_PRODUCTS_LABEL, new JSONArray())
                            .put(SfaConstants.SFA_ECO_CART_TOTAL_PRODUCTS_LABEL, size)
                            .put(SfaConstants.SFA_ECO_CART_TOTAL_PRICE_LABEL, 0)
                            .put(SfaConstants.SFA_ECO_CART_CURRENCY_LABEL, "USD")
                            .put(SfaConstants.SFA_ECO_CART_PAYMENT_METHOD_LABEL, payMethod));

            // Finally, products

            int n = 0, totalPrice = 0;
            for (Product p : products) {
                JSONObject jo = new JSONObject()
                        .put(SfaConstants.SFA_ECO_CART_PRODUCT_ID_LABEL, p.getId())
                        .put(SfaConstants.SFA_ECO_CART_PRODUCT_NAME_LABEL, p.getName())
                        .put(SfaConstants.SFA_ECO_CART_PRODUCT_PRICE_LABEL, p.getPrice());
                totalPrice += p.getPrice();
                cart.getJSONObject(SfaConstants.SFA_ECO_CART_LABEL).getJSONArray(SfaConstants.SFA_ECO_CART_PRODUCTS_LABEL).put(n++, jo);
            }

            // Update price and return cart
            cart.getJSONObject(SfaConstants.SFA_ECO_CART_LABEL).put(SfaConstants.SFA_ECO_CART_TOTAL_PRICE_LABEL, totalPrice);
            Log.v(TAG, cart.toString(2));
            return cart;

        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}