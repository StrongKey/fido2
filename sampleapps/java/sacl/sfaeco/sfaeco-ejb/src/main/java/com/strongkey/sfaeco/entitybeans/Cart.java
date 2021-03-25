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

package com.strongkey.sfaeco.entitybeans;

import com.strongkey.sfaeco.utilities.Common;
import java.util.Collection;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonException;
import javax.json.JsonObject;

public class Cart {
    
    private int merchantId;
    private Collection<Product> products;
    private int totalProducts;
    private int totalPrice;
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

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public PaymentMethod getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    @Override
    public String toString() {
        return  "merchantId: " + this.merchantId + ", " +
                "products: " + this.products + ", " +
                "totalProducts: " + this.totalProducts + ", " +
                "totalPrice: " + this.totalPrice + ", " +
                "paymentMethod: " + this.paymentMethod;
    }

    /**
     * Returns the Java object as a Json object
     * @return JsonObject
     */
    public JsonObject toJson() {
        try {
            // We assume only one each product can be ordered in a jsonCart - keeping it simple
            int size = products.size();

            // First create payment method
            JsonObject payMethod = Json.createObjectBuilder()
                .add("brand", paymentMethod.getBrand().toString())
                .add("number", paymentMethod.getNumber())
                .build();
            
            // Assemble product array
            totalPrice = 0;
            JsonArray jsonProducts = Json.createArrayBuilder().build();       
            products.forEach((p) -> {
                JsonObject jo = Json.createObjectBuilder()
                        .add("id", p.getId())
                        .add("name", p.getName())
                        .add("price", p.getPrice())
                        .build();
                totalPrice += p.getPrice();
                jsonProducts.add(jo);
            });

            // Create jsonCart
            JsonObject jsonCart = Json.createObjectBuilder()
                .add("cart", Json.createObjectBuilder()
                    .add("merchantId", merchantId)
                    .add("products", jsonProducts)
                    .add("totalProducts", size)
                    .add("totalPrice", totalPrice)
                    .add("paymentMethod", payMethod)).build();

            return jsonCart;

        } catch (JsonException e) {
            Common.log(Level.WARNING, "SFAECO-ERR-5000", e.getStackTrace());
            return null;
        }
    }
}
