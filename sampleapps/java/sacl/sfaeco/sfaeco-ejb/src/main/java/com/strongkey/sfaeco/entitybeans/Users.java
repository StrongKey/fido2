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
 * Entity bean for the USERS table
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
@Table(name = "users")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "Users.findAll", query = "SELECT u FROM Users u"),
    @NamedQuery(name = "Users.findByDid", query = "SELECT u FROM Users u WHERE u.usersPK.did = :did"),
    @NamedQuery(name = "Users.findByDidUid", query = "SELECT u FROM Users u WHERE u.usersPK.did = :did and u.usersPK.uid = :uid"),
    @NamedQuery(name = "Users.findByPK", query = "SELECT u FROM Users u WHERE u.usersPK.did = :did and u.usersPK.sid = :sid and u.usersPK.uid = :uid"),
    @NamedQuery(name = "Users.findByUsername", query = "SELECT u FROM Users u WHERE u.usersPK.did = :did and u.username= :username"),
    @NamedQuery(name = "Users.findByCredentialid", query = "SELECT u FROM Users u WHERE u.credentialid = :credentialid"),
    @NamedQuery(name = "Users.findByGivenName", query = "SELECT u FROM Users u WHERE u.givenName = :givenName"),
    @NamedQuery(name = "Users.findByFamilyName", query = "SELECT u FROM Users u WHERE u.familyName = :familyName"),
    @NamedQuery(name = "Users.findByEmailAddress", query = "SELECT u FROM Users u WHERE u.usersPK.did = :did and u.emailAddress = :emailAddress"),
    @NamedQuery(name = "Users.findByMobileNumber", query = "SELECT u FROM Users u WHERE u.mobileNumber = :mobileNumber"),
    @NamedQuery(name = "Users.findByEnrollmentDate", query = "SELECT u FROM Users u WHERE u.enrollmentDate = :enrollmentDate"),
    @NamedQuery(name = "Users.findByStatus", query = "SELECT u FROM Users u WHERE u.status = :status"),
    @NamedQuery(name = "Users.findByCreateDate", query = "SELECT u FROM Users u WHERE u.createDate = :createDate"),
    @NamedQuery(name = "Users.maxPK", query = "SELECT max(u.usersPK.uid) FROM Users u WHERE u.usersPK.did = :did and u.usersPK.sid = :sid")
})

public class Users implements Serializable {

    private static final long serialVersionUID = 1L;

    @EmbeddedId
    protected UsersPK usersPK;
    
    @Basic(optional = false)
    @Column(name = "username")
    private String username;
    
    @Column(name = "credentialid")
    private String credentialid;
    
    @Basic(optional = false)
    @Column(name = "given_name")
    private String givenName;
    
    @Basic(optional = false)
    @Column(name = "family_name")
    private String familyName;
    
    @Basic(optional = false)
    @Column(name = "email_address")
    private String emailAddress;
    
    @Column(name = "mobile_number")
    private String mobileNumber;
    
    @Column(name = "enrollment_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date enrollmentDate;
    
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

    public Users() {
    }

    public Users(UsersPK usersPK) {
        this.usersPK = usersPK;
    }

    public Users(UsersPK usersPK, String username, String givenName, String familyName, String emailAddress, String mobileNumber, String status, Date createDate) {
        this.usersPK = usersPK;
        this.username = username;
        this.givenName = givenName;
        this.familyName = familyName;
        this.emailAddress = emailAddress;
        this.mobileNumber = mobileNumber;
        this.status = status;
        this.createDate = createDate;
    }

    public Users(short did, short sid, long uid) {
        this.usersPK = new UsersPK(did, sid, uid);
    }

    public UsersPK getUsersPK() {
        return usersPK;
    }

    public void setUsersPK(UsersPK usersPK) {
        this.usersPK = usersPK;
    }
    
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getCredentialid() {
        return credentialid;
    }

    public void setCredentialid(String credentialid) {
        this.credentialid = credentialid;
    }

    public String getGivenName() {
        return givenName;
    }

    public void setGivenName(String givenName) {
        this.givenName = givenName;
    }

    public String getFamilyName() {
        return familyName;
    }

    public void setFamilyName(String familyName) {
        this.familyName = familyName;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    public String getMobileNumber() {
        return mobileNumber;
    }

    public void setMobileNumber(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public Date getEnrollmentDate() {
        return enrollmentDate;
    }

    public void setEnrollmentDate(Date enrollmentDate) {
        this.enrollmentDate = enrollmentDate;
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
        hash += (usersPK != null ? usersPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof Users)) {
            return false;
        }
        Users other = (Users) object;
        if ((this.usersPK == null && other.usersPK != null) || (this.usersPK != null && !this.usersPK.equals(other.usersPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "Users[ usersPK=" + usersPK + " ]";
    }
    
    public String toStringPK() {
        return usersPK.getDid() +"-"+ usersPK.getSid()+"-"+usersPK.getUid();
    }

}
