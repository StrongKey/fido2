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
 * Primary Key object for the MERCHANTS table
 */

package com.strongkey.sfaeco.entitybeans;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;


@Embeddable
public class MerchantsPK implements Serializable {

    @Basic(optional = false)
    @Column(name = "did")
    private short did;
    
    @Basic(optional = false)
    @Column(name = "merchant_id")
    private short merchantId;

    public MerchantsPK() {
    }

    public MerchantsPK(short did, short merchantId) {
        this.did = did;
        this.merchantId = merchantId;
    }

    public short getDid() {
        return did;
    }

    public void setDid(short did) {
        this.did = did;
    }

    public short getMerchantId() {
        return merchantId;
    }

    public void setMerchantId(short merchantId) {
        this.merchantId = merchantId;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) did;
        hash += (int) merchantId;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof MerchantsPK)) {
            return false;
        }
        MerchantsPK other = (MerchantsPK) object;
        if (this.did != other.did) {
            return false;
        }
        if (this.merchantId != other.merchantId) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "MerchantsPK[ did=" + did + ", merchantId=" + merchantId + " ]";
    }

}
