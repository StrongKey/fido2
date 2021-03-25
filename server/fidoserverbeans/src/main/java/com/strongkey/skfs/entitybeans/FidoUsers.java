/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.entitybeans;

import java.io.Serializable;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;


@Entity
@Table(name = "fido_users")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FidoUsers.findAll", query = "SELECT f FROM FidoUsers f"),
    @NamedQuery(name = "FidoUsers.findBySid", query = "SELECT f FROM FidoUsers f WHERE f.fidoUsersPK.sid = :sid"),
    @NamedQuery(name = "FidoUsers.findByDid", query = "SELECT f FROM FidoUsers f WHERE f.fidoUsersPK.did = :did"),
    @NamedQuery(name = "FidoUsers.findByUsername", query = "SELECT f FROM FidoUsers f WHERE f.fidoUsersPK.username = :username"),
    @NamedQuery(name = "FidoUsers.findByDidUsername", query = "SELECT f FROM FidoUsers f WHERE f.fidoUsersPK.did = :did and f.fidoUsersPK.username = :username"),
    @NamedQuery(name = "FidoUsers.findByUserdn", query = "SELECT f FROM FidoUsers f WHERE f.userdn = :userdn"),
    @NamedQuery(name = "FidoUsers.findByFidoKeysEnabled", query = "SELECT f FROM FidoUsers f WHERE f.fidoKeysEnabled = :fidoKeysEnabled"),
    @NamedQuery(name = "FidoUsers.findByTwoStepVerification", query = "SELECT f FROM FidoUsers f WHERE f.twoStepVerification = :twoStepVerification"),
    @NamedQuery(name = "FidoUsers.findByPrimaryEmail", query = "SELECT f FROM FidoUsers f WHERE f.primaryEmail = :primaryEmail"),
    @NamedQuery(name = "FidoUsers.findByRegisteredEmails", query = "SELECT f FROM FidoUsers f WHERE f.registeredEmails = :registeredEmails"),
    @NamedQuery(name = "FidoUsers.findByPrimaryPhoneNumber", query = "SELECT f FROM FidoUsers f WHERE f.primaryPhoneNumber = :primaryPhoneNumber"),
    @NamedQuery(name = "FidoUsers.findByRegisteredPhoneNumbers", query = "SELECT f FROM FidoUsers f WHERE f.registeredPhoneNumbers = :registeredPhoneNumbers"),
    @NamedQuery(name = "FidoUsers.findByTwoStepTarget", query = "SELECT f FROM FidoUsers f WHERE f.twoStepTarget = :twoStepTarget"),
    @NamedQuery(name = "FidoUsers.findByStatus", query = "SELECT f FROM FidoUsers f WHERE f.status = :status"),
    @NamedQuery(name = "FidoUsers.findBySignature", query = "SELECT f FROM FidoUsers f WHERE f.signature = :signature")})
public class FidoUsers implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected FidoUsersPK fidoUsersPK;
    @Size(max = 2048)
    @Column(name = "userdn")
    private String userdn;
    @Size(max = 5)
    @Column(name = "fido_keys_enabled")
    private String fidoKeysEnabled;
    @Size(max = 5)
    @Column(name = "two_step_verification")
    private String twoStepVerification;
    @Size(max = 256)
    @Column(name = "primary_email")
    private String primaryEmail;
    @Size(max = 2048)
    @Column(name = "registered_emails")
    private String registeredEmails;
    @Size(max = 32)
    @Column(name = "primary_phone_number")
    private String primaryPhoneNumber;
    @Size(max = 2048)
    @Column(name = "registered_phone_numbers")
    private String registeredPhoneNumbers;
    @Size(max = 6)
    @Column(name = "two_step_target")
    private String twoStepTarget;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 8)
    @Column(name = "status")
    private String status;
    @Size(max = 2048)
    @Column(name = "signature")
    private String signature;

    @Transient
    private String id;

    public FidoUsers() {
    }

    public FidoUsers(FidoUsersPK fidoUsersPK) {
        this.fidoUsersPK = fidoUsersPK;
    }

    public FidoUsers(FidoUsersPK fidoUsersPK, String status) {
        this.fidoUsersPK = fidoUsersPK;
        this.status = status;
    }

    public FidoUsers(short sid, short did, String username) {
        this.fidoUsersPK = new FidoUsersPK(sid, did, username);
    }

    public FidoUsersPK getFidoUsersPK() {
        return fidoUsersPK;
    }

    public void setFidoUsersPK(FidoUsersPK fidoUsersPK) {
        this.fidoUsersPK = fidoUsersPK;
    }

    public String getUserdn() {
        return userdn;
    }

    public void setUserdn(String userdn) {
        this.userdn = userdn;
    }

    public String getFidoKeysEnabled() {
        return fidoKeysEnabled;
    }

    public void setFidoKeysEnabled(String fidoKeysEnabled) {
        this.fidoKeysEnabled = fidoKeysEnabled;
    }

    public String getTwoStepVerification() {
        return twoStepVerification;
    }

    public void setTwoStepVerification(String twoStepVerification) {
        this.twoStepVerification = twoStepVerification;
    }

    public String getPrimaryEmail() {
        return primaryEmail;
    }

    public void setPrimaryEmail(String primaryEmail) {
        this.primaryEmail = primaryEmail;
    }

    public String getRegisteredEmails() {
        return registeredEmails;
    }

    public void setRegisteredEmails(String registeredEmails) {
        this.registeredEmails = registeredEmails;
    }

    public String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
        this.primaryPhoneNumber = primaryPhoneNumber;
    }

    public String getRegisteredPhoneNumbers() {
        return registeredPhoneNumbers;
    }

    public void setRegisteredPhoneNumbers(String registeredPhoneNumbers) {
        this.registeredPhoneNumbers = registeredPhoneNumbers;
    }

    public String getTwoStepTarget() {
        return twoStepTarget;
    }

    public void setTwoStepTarget(String twoStepTarget) {
        this.twoStepTarget = twoStepTarget;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @XmlTransient
    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

        public String getId() {
        return id;
    }

    @XmlAttribute
    public void setId(String id) {
        this.id = id;
    }
    @Override
    public int hashCode() {
        int hash = 0;
        hash += (fidoUsersPK != null ? fidoUsersPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FidoUsers)) {
            return false;
        }
        FidoUsers other = (FidoUsers) object;
        if ((this.fidoUsersPK == null && other.fidoUsersPK != null) || (this.fidoUsersPK != null && !this.fidoUsersPK.equals(other.fidoUsersPK))) {
            return false;
        }
        return true;
    }

    public String toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("sid", this.getFidoUsersPK().getSid());
        job.add("did", this.getFidoUsersPK().getDid());
        job.add("username", this.getFidoUsersPK().getUsername());
        if (this.userdn != null) {
            job.add("userdn", this.getUserdn());
        }
        if (this.fidoKeysEnabled != null) {
            job.add("fidoKeysEnabled", this.getFidoKeysEnabled());
        }
        if (this.twoStepVerification != null) {
            job.add("twoStepVerification", this.getTwoStepVerification());
        }
        if (this.primaryEmail != null) {
            job.add("primaryEmail", this.getPrimaryEmail());
        }
        if (this.registeredEmails != null) {
            job.add("registeredEmails", this.getRegisteredEmails());
        }
        if (this.primaryPhoneNumber != null) {
            job.add("primaryPhoneNumber", this.getPrimaryPhoneNumber());
        }
        if (this.registeredPhoneNumbers != null) {
            job.add("registeredPhoneNumbers", this.getRegisteredPhoneNumbers());
        }
        if (this.twoStepTarget != null) {
            job.add("twoStepTarget", this.getTwoStepTarget());
        }
        job.add("status", this.status);
        return job.build().toString();
    }
    
    @Override
    public String toString() {
        return "FidoUsers[ fidoUsersPK=" + fidoUsersPK + " ]";
    }

}
