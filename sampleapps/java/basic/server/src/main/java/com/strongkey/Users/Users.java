/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */
package com.strongkey.Users;

import java.io.Serializable;
import java.util.Date;
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
import javax.persistence.Temporal;

@Entity
@Table(name = "users", schema = "", indexes = {
    @Index(columnList = "email", unique = true),
    @Index(columnList = "username", unique = true)
})
@NamedQueries({
    @NamedQuery(name = "Users.findByUsername", query = "SELECT u FROM Users u WHERE u.username = :username"),
    @NamedQuery(name = "Users.findByEmail", query = "SELECT u FROM Users u WHERE u.email = :email")
})
public class Users implements Serializable {
    private static final long serialVersionUID = 1L;
    
    @Basic(optional = false)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;
    
    @Basic(optional = false)
    @Column(name = "email", nullable = false, length = 64)
    private String email;
    
    @Basic(optional = false)
    @Column(name = "username", nullable = false, length = 64)
    private String username;
    
    @Basic(optional = false)
    @Column(name = "firstname", nullable = false, length = 32)
    private String firstName;
    
    @Basic(optional = false)
    @Column(name = "lastname", nullable = false, length = 32)
    private String lastName;
    
    @Basic(optional = false)
    @Column(name = "createDate", nullable = false)
    @Temporal(javax.persistence.TemporalType.DATE)
    private Date createDate;
    
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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.id == null && other.id != null) || (this.id != null && !this.id.equals(other.id))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return  "   id: " + id + '\n' +
                "   email: " + email + '\n' +
                "   username: " + username + '\n' +
                "   firstName: " + firstName + '\n' +
                "   lastName: " + lastName + '\n'; 
    }
    
}
