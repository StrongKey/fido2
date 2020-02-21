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
public class FidoPoliciesPK implements Serializable {

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
    @Column(name = "pid")
    private int pid;

    public FidoPoliciesPK() {
    }

    public FidoPoliciesPK(short sid, short did, int pid) {
        this.sid = sid;
        this.did = did;
        this.pid = pid;
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

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (int) sid;
        hash += (int) did;
        hash += (int) pid;
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FidoPoliciesPK)) {
            return false;
        }
        FidoPoliciesPK other = (FidoPoliciesPK) object;
        if (this.sid != other.sid) {
            return false;
        }
        if (this.did != other.did) {
            return false;
        }
        if (this.pid != other.pid) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "com.strongauth.skce.entitybeans.FidoPoliciesPK[ sid=" + sid + ", did=" + did + ", pid=" + pid + " ]";
    }

}
