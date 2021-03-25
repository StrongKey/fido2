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
 * Entity bean for the MERCHANTS table
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
@Table(name = "merchants")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Merchants.findAll", query = "SELECT m FROM Merchants m"),
    @NamedQuery(name = "Merchants.findByDid", query = "SELECT m FROM Merchants m WHERE m.merchantsPK.did = :did"),
    @NamedQuery(name = "Merchants.findByMerchantId", query = "SELECT m FROM Merchants m WHERE m.merchantsPK.merchantId = :merchantId"),
    @NamedQuery(name = "Merchants.findByMerchantName", query = "SELECT m FROM Merchants m WHERE m.merchantName = :merchantName"),
    @NamedQuery(name = "Merchants.findByCreateDate", query = "SELECT m FROM Merchants m WHERE m.createDate = :createDate"),
    @NamedQuery(name = "Merchants.findByModifyDate", query = "SELECT m FROM Merchants m WHERE m.modifyDate = :modifyDate"),
    @NamedQuery(name = "Merchants.findByNotes", query = "SELECT m FROM Merchants m WHERE m.notes = :notes"),
    @NamedQuery(name = "Merchants.findBySignature", query = "SELECT m FROM Merchants m WHERE m.signature = :signature")
})

public class Merchants implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected MerchantsPK merchantsPK;
    
    @Basic(optional = false)
    @Column(name = "merchant_name")
    private String merchantName;
    
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

    public Merchants() {
    }

    public Merchants(MerchantsPK merchantsPK) {
        this.merchantsPK = merchantsPK;
    }

    public Merchants(MerchantsPK merchantsPK, String merchantName, Date createDate) {
        this.merchantsPK = merchantsPK;
        this.merchantName = merchantName;
        this.createDate = createDate;
    }

    public Merchants(short did, short merchantId) {
        this.merchantsPK = new MerchantsPK(did, merchantId);
    }

    public MerchantsPK getMerchantsPK() {
        return merchantsPK;
    }

    public void setMerchantsPK(MerchantsPK merchantsPK) {
        this.merchantsPK = merchantsPK;
    }

    public String getMerchantName() {
        return merchantName;
    }

    public void setMerchantName(String merchantName) {
        this.merchantName = merchantName;
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
        hash += (merchantsPK != null ? merchantsPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Merchants)) {
            return false;
        }
        Merchants other = (Merchants) object;
        if ((this.merchantsPK == null && other.merchantsPK != null) || (this.merchantsPK != null && !this.merchantsPK.equals(other.merchantsPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Merchants[ merchantsPK=" + merchantsPK + " ]";
    }

}
