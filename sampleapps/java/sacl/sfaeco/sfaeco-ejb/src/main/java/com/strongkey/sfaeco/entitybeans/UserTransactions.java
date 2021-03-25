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
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 * Entity bean for the USER_TRANSACTIONS table
 */

package com.strongkey.sfaeco.entitybeans;

import java.io.Serializable;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;


@Entity
@Table(name = "user_transactions")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UserTransactions.findAll", query = "SELECT u FROM UserTransactions u"),
    @NamedQuery(name = "UserTransactions.findByDid", query = "SELECT u FROM UserTransactions u WHERE u.userTransactionsPK.did = :did"),
    @NamedQuery(name = "UserTransactions.findByUserid", query = "SELECT u FROM UserTransactions u WHERE u.userTransactionsPK.did = :did and u.userTransactionsPK.uid = :uid"),
    @NamedQuery(name = "UserTransactions.findByUtxid", query = "SELECT u FROM UserTransactions u WHERE u.userTransactionsPK.utxid = :utxid"),
    @NamedQuery(name = "UserTransactions.findByDidUidTxid", query = "SELECT u FROM UserTransactions u WHERE u.userTransactionsPK.did = :did and "
            + "u.userTransactionsPK.uid = :uid and u.txid = :txid"),
    @NamedQuery(name = "UserTransactions.findByPK", query = "SELECT u FROM UserTransactions u WHERE u.userTransactionsPK.did = :did "
            + "and u.userTransactionsPK.uid = :uid and u.userTransactionsPK.sid = :sid and u.userTransactionsPK.utxid = :utxid"),
    @NamedQuery(name = "UserTransactions.findByStatus", query = "SELECT u FROM UserTransactions u WHERE u.status = :status"),
    @NamedQuery(name = "UserTransactions.findByCreateDate", query = "SELECT u FROM UserTransactions u WHERE u.createDate = :createDate"),
    @NamedQuery(name = "UserTransactions.maxPK", query = "SELECT max(u.userTransactionsPK.utxid) FROM UserTransactions u "
            + "WHERE u.userTransactionsPK.did = :did and u.userTransactionsPK.sid = :sid"),
})

public class UserTransactions implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected UserTransactionsPK userTransactionsPK;
    
    @Column(name = "merchant_id")
    private short merchantId;
    
    @Column(name = "total_products")
    private short totalProducts;
    
    @Column(name = "total_price")
    private int totalPrice;
    
    @Column(name = "payment_brand")
    private String paymentBrand;
    
    @Column(name = "payment_card_number")
    private String paymentCardNumber;
    
    @Column(name = "currency")
    private String currency;
    
    @Column(name = "txtime")
    private long txtime;
    
    @Basic(optional = false)
    @Column(name = "txid")
    private String txid;
    
    @Column(name = "txpayload")
    private String txpayload;
    
    @Column(name = "nonce")
    private String nonce;
    
    @Column(name = "challenge")
    private String challenge;
    
    @Basic(optional = false)
    @Column(name = "status")
    private String status;
    
    @Basic(optional = false)
    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    
    @Column(name = "modify_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyDate;
    
    @Column(name = "notes")
    private String notes;
    
    @Column(name = "signature")
    private String signature;

    public UserTransactions() {
    }

    public UserTransactions(UserTransactionsPK userTransactionsPK) {
        this.userTransactionsPK = userTransactionsPK;
    }

     public UserTransactions(UserTransactionsPK userTransactionsPK, short merchantId, short totalProducts, int totalPrice, long txtime, String txid, String txpayload, String nonce, String challenge, String status, Date createDate) {
        this.userTransactionsPK = userTransactionsPK;
        this.merchantId = merchantId;
        this.totalProducts = totalProducts;
        this.totalPrice = totalPrice;
        this.txtime = txtime;
        this.txid = txid;
        this.txpayload = txpayload;
        this.nonce = nonce;
        this.challenge = challenge;
        this.status = status;
        this.createDate = createDate;
    }

    public UserTransactions(short did, short sid, long uid, long utxid) {
        this.userTransactionsPK = new UserTransactionsPK(did, sid, uid, utxid);
    }

    public UserTransactionsPK getUserTransactionsPK() {
        return userTransactionsPK;
    }

    public void setUserTransactionsPK(UserTransactionsPK userTransactionsPK) {
        this.userTransactionsPK = userTransactionsPK;
    }

    public short getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(short merchantId) {
        this.merchantId = merchantId;
    }
    
     public short getTotalProducts() {
        return totalProducts;
    }

    public void setTotalProducts(short totalProducts) {
        this.totalProducts = totalProducts;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getPaymentBrand() {
        return paymentBrand;
    }

    public void setPaymentBrand(String paymentBrand) {
        this.paymentBrand = paymentBrand;
    }

    public String getPaymentCardNumber() {
        return paymentCardNumber;
    }

    public void setPaymentCardNumber(String paymentCardNumber) {
        this.paymentCardNumber = paymentCardNumber;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public long getTxtime() {
        return txtime;
    }

    public void setTxtime(long txtime) {
        this.txtime = txtime;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getTxpayload() {
        return txpayload;
    }

    public void setTxpayload(String txpayload) {
        this.txpayload = txpayload;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }
    
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }
    
    public Date getModifyDate() {
        return modifyDate;
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = modifyDate;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (userTransactionsPK != null ? userTransactionsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UserTransactions)) {
            return false;
        }
        UserTransactions other = (UserTransactions) object;
        if ((this.userTransactionsPK == null && other.userTransactionsPK != null) || (this.userTransactionsPK != null && !this.userTransactionsPK.equals(other.userTransactionsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UserTransactions[ userTransactionsPK=" + userTransactionsPK + " ]";
    }

     public JsonObject toJsonObject(JsonArray authRefJarr, String username){
        JsonObjectBuilder job = Json.createObjectBuilder();
//        System.out.println(authRefJarr.toArray()[0].toString());
        job.add("sid", this.getUserTransactionsPK().getSid());
        job.add("did", this.getUserTransactionsPK().getDid());
        job.add("uid", this.getUserTransactionsPK().getUid());
        job.add("utxid", this.getUserTransactionsPK().getUtxid());
        job.add("merchantId", this.getMerchantId());
        job.add("createDate", this.getCreateDate().toString());
        job.add("modifyDate",this.getModifyDate().toString());
        job.add("status", this.getStatus());
        job.add("signature", this.getSignature());
        job.add("notes", this.getNotes());
        job.add("totalPrice", this.getTotalPrice());
        job.add("totalProducts", this.getTotalProducts());
        job.add("paymentBrand",this.getPaymentBrand());
        job.add("paymentCardNumber", this.getPaymentCardNumber());
        job.add("currency",this.getCurrency());
        job.add("txtime",this.getTxtime());
        job.add("txid",this.getTxid());
        job.add("txpayload",this.getTxpayload());
        job.add("nonce",this.getNonce());
        job.add("challenge",this.getChallenge());
        job.add("username",username);
        job.add("utxfidoauthreferences",authRefJarr);

        return job.build();
    }
}
