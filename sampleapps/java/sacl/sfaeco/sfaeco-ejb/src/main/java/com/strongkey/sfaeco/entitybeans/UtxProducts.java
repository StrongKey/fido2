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
 * Entity bean for the UTX_PRODUCTS table
 */

package com.strongkey.sfaeco.entitybeans;

import java.io.Serializable;
import java.util.Date;
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
@Table(name = "utx_products")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UtxProducts.findAll", query = "SELECT u FROM UtxProducts u"),
    @NamedQuery(name = "UtxProducts.findByDid", query = "SELECT u FROM UtxProducts u WHERE u.utxProductsPK.did = :did"),
    @NamedQuery(name = "UtxProducts.findBySid", query = "SELECT u FROM UtxProducts u WHERE u.utxProductsPK.sid = :sid"),
    @NamedQuery(name = "UtxProducts.findByUid", query = "SELECT u FROM UtxProducts u WHERE u.utxProductsPK.uid = :uid"),
    @NamedQuery(name = "UtxProducts.findByUtxid", query = "SELECT u FROM UtxProducts u WHERE u.utxProductsPK.utxid = :utxid"),
    @NamedQuery(name = "UtxProducts.findByProductId", query = "SELECT u FROM UtxProducts u WHERE u.utxProductsPK.productId = :productId"),
    @NamedQuery(name = "UtxProducts.findByProductName", query = "SELECT u FROM UtxProducts u WHERE u.productName = :productName"),
    @NamedQuery(name = "UtxProducts.findByProductPrice", query = "SELECT u FROM UtxProducts u WHERE u.productPrice = :productPrice"),
    @NamedQuery(name = "UtxProducts.findByCreateDate", query = "SELECT u FROM UtxProducts u WHERE u.createDate = :createDate"),
    @NamedQuery(name = "UtxProducts.findByModifyDate", query = "SELECT u FROM UtxProducts u WHERE u.modifyDate = :modifyDate"),
    @NamedQuery(name = "UtxProducts.findByNotes", query = "SELECT u FROM UtxProducts u WHERE u.notes = :notes"),
    @NamedQuery(name = "UtxProducts.findBySignature", query = "SELECT u FROM UtxProducts u WHERE u.signature = :signature")
})

public class UtxProducts implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected UtxProductsPK utxProductsPK;
    
    @Basic(optional = false)
    @Column(name = "product_name")
    private String productName;
    
    @Basic(optional = false)
    @Column(name = "product_price")
    private int productPrice;
    
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

    public UtxProducts() {
    }

    public UtxProducts(UtxProductsPK utxProductsPK) {
        this.utxProductsPK = utxProductsPK;
    }

    public UtxProducts(UtxProductsPK utxProductsPK, String productName, int productPrice, Date createDate) {
        this.utxProductsPK = utxProductsPK;
        this.productName = productName;
        this.productPrice = productPrice;
        this.createDate = createDate;
    }

    public UtxProducts(short did, short sid, long uid, long utxid, short productId) {
        this.utxProductsPK = new UtxProductsPK(did, sid, uid, utxid, productId);
    }

    public UtxProductsPK getUtxProductsPK() {
        return utxProductsPK;
    }

    public void setUtxProductsPK(UtxProductsPK utxProductsPK) {
        this.utxProductsPK = utxProductsPK;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getProductPrice() {
        return productPrice;
    }

    public void setProductPrice(int productPrice) {
        this.productPrice = productPrice;
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
        hash += (utxProductsPK != null ? utxProductsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UtxProducts)) {
            return false;
        }
        UtxProducts other = (UtxProducts) object;
        if ((this.utxProductsPK == null && other.utxProductsPK != null) || (this.utxProductsPK != null && !this.utxProductsPK.equals(other.utxProductsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UtxProducts[ utxProductsPK=" + utxProductsPK + " ]";
    }

}
