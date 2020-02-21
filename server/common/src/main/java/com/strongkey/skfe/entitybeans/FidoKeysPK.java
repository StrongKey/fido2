/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfe.entitybeans;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;


@Embeddable
public class FidoKeysPK implements Serializable {

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
    @Size(min = 1, max = 256)
    @Column(name = "username")
    private String username;
    @Basic(optional = false)
    @NotNull
    @Column(name = "fkid")
    private long fkid;

    public FidoKeysPK() {
    }

    public FidoKeysPK(short sid, short did, String username, long fkid) {
        this.sid = sid;
        this.did = did;
        this.username = username;
        this.fkid = fkid;
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getFkid() {
        return fkid;
    }

    public void setFkid(long fkid) {
        this.fkid = fkid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) sid;
        hash += (int) did;
        hash += (username != null ? username.hashCode() : 0);
        hash += (int) fkid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FidoKeysPK)) {
            return false;
        }
        FidoKeysPK other = (FidoKeysPK) object;
        if (this.sid != other.sid) {
            return false;
        }
        if (this.did != other.did) {
            return false;
        }
        if ((this.username == null && other.username != null) || (this.username != null && !this.username.equals(other.username))) {
            return false;
        }
        if (this.fkid != other.fkid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "FidoKeysPK[ sid=" + sid + ", did=" + did + ", username=" + username + ", fkid=" + fkid + " ]";
    }

}
