/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.entitybeans;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;

@Embeddable
public class AttestationCertificatesPK implements Serializable {

    @Basic(optional = false)
    @NotNull
    @Column(name = "sid")
    private short sid;
    @Basic(optional = false)
    @NotNull
    @Column(name = "did")
    private short did;
    @Basic(optional = false)
    @NotNull
    @Column(name = "attcid")
    private int attcid;

    public AttestationCertificatesPK() {
    }

    public AttestationCertificatesPK(short sid, short did, int attcid) {
        this.sid = sid;
        this.did = did;
        this.attcid = attcid;
    }

    public short getSid() {
        return sid;
    }

    public void setSid(short sid) {
        this.sid = sid;
    }

    public short getDid() {
        return did;
    }

    public void setDid(short did) {
        this.did = did;
    }

    public int getAttcid() {
        return attcid;
    }

    public void setAttcid(int attcid) {
        this.attcid = attcid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) sid;
        hash += (int) did;
        hash += (int) attcid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AttestationCertificatesPK)) {
            return false;
        }
        AttestationCertificatesPK other = (AttestationCertificatesPK) object;
        if (this.sid != other.sid) {
            return false;
        }
        if (this.did != other.did) {
            return false;
        }
        if (this.attcid != other.attcid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AttestationCertificatesPK[ sid=" + sid + ", did=" + did + ", attcid=" + attcid + " ]";
    }

}
