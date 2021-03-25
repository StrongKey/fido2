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
 * Primary Key object for the UTX_PRODUCTS table
 */

package com.strongkey.sfaeco.entitybeans;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class UtxProductsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "did")
    private short did;
    
    @Basic(optional = false)
    @Column(name = "sid")
    private short sid;
    
    @Basic(optional = false)
    @Column(name = "uid")
    private long uid;
    
    @Basic(optional = false)
    @Column(name = "utxid")
    private long utxid;
    
    @Basic(optional = false)
    @Column(name = "product_id")
    private short productId;

    public UtxProductsPK() {
    }

    public UtxProductsPK(short did, short sid, long uid, long utxid, short productId) {
        this.did = did;
        this.sid = sid;
        this.uid = uid;
        this.utxid = utxid;
        this.productId = productId;
    }

    public short getDid() {
        return did;
    }

    public void setDid(short did) {
        this.did = did;
    }

    public short getSid() {
        return sid;
    }

    public void setSid(short sid) {
        this.sid = sid;
    }

    public long getUid() {
        return uid;
    }

    public void setUid(long uid) {
        this.uid = uid;
    }

    public long getUtxid() {
        return utxid;
    }

    public void setUtxid(long utxid) {
        this.utxid = utxid;
    }

    public short getProductId() {
        return productId;
    }

    public void setProductId(short productId) {
        this.productId = productId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) did;
        hash += (int) sid;
        hash += (int) uid;
        hash += (int) utxid;
        hash += (int) productId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UtxProductsPK)) {
            return false;
        }
        UtxProductsPK other = (UtxProductsPK) object;
        if (this.did != other.did) {
            return false;
        }
        if (this.sid != other.sid) {
            return false;
        }
        if (this.uid != other.uid) {
            return false;
        }
        if (this.utxid != other.utxid) {
            return false;
        }
        if (this.productId != other.productId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UtxProductsPK[ did=" + did + ", sid=" + sid + ", uid=" + uid + ", utxid=" + utxid + ", productId=" + productId + " ]";
    }

}
