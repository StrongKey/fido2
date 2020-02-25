/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.Registrations;

import java.io.Serializable;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Index;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

@Entity
@Table(name = "registrations", schema = "", indexes = {
    @Index(columnList = "email", unique = true),
    @Index(columnList = "nonce", unique = true)
})
@NamedQueries({
    @NamedQuery(name = "Registrations.findByNonce", query = "SELECT r FROM Registrations r WHERE r.nonce = :nonce"),
    @NamedQuery(name = "Registrations.findByEmail", query = "SELECT r FROM Registrations r WHERE r.email = :email")
})
public class Registrations implements Serializable {

    private static final long serialVersionUID = 1L;
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Basic(optional = false)
    @Column(name = "email", nullable = false, length = 64)
    private String email;

    @Basic(optional = false)
    @Column(name = "nonce", nullable = false, length = 64)
    private String nonce;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getNonce() {
        return nonce;
    }

    public void setNonce(String nonce) {
        this.nonce = nonce;
    }

    @Override
    public int hashCode() {
        int hash = 0;
        hash += (id != null ? id.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Registrations)) {
            return false;
        }
        Registrations other = (Registrations) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return  "   id: " + id + '\n'
            +   "   email: " + email + '\n'
            +   "   nonce: " + nonce + '\n';
    }

}
