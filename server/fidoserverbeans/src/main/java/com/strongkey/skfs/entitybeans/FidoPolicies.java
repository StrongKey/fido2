/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.entitybeans;

import java.io.Serializable;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObjectBuilder;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "fido_policies")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "FidoPolicies.findAll", query = "SELECT f FROM FidoPolicies f"),
    @NamedQuery(name = "FidoPolicies.findBySid", query = "SELECT f FROM FidoPolicies f WHERE f.fidoPoliciesPK.sid = :sid"),
    @NamedQuery(name = "FidoPolicies.findByDid", query = "SELECT f FROM FidoPolicies f WHERE f.fidoPoliciesPK.did = :did"),
    @NamedQuery(name = "FidoPolicies.findByPid", query = "SELECT f FROM FidoPolicies f WHERE f.fidoPoliciesPK.pid = :pid"),
    @NamedQuery(name = "FidoPolicies.findBySidDidPid", query = "SELECT f FROM FidoPolicies f WHERE f.fidoPoliciesPK.sid = :sid and f.fidoPoliciesPK.did = :did and f.fidoPoliciesPK.pid = :pid"),
    @NamedQuery(name = "FidoPolicies.findByStatus", query = "SELECT f FROM FidoPolicies f WHERE f.status = :status"),
    @NamedQuery(name = "FidoPolicies.findByNotes", query = "SELECT f FROM FidoPolicies f WHERE f.notes = :notes"),
    @NamedQuery(name = "FidoPolicies.findByCreateDate", query = "SELECT f FROM FidoPolicies f WHERE f.createDate = :createDate"),
    @NamedQuery(name = "FidoPolicies.findByModifyDate", query = "SELECT f FROM FidoPolicies f WHERE f.modifyDate = :modifyDate"),
    @NamedQuery(name = "FidoPolicies.maxpid", query = "SELECT max(f.fidoPoliciesPK.pid) FROM FidoPolicies f where f.fidoPoliciesPK.sid = :sid"),
    @NamedQuery(name = "FidoPolicies.findBySignature", query = "SELECT f FROM FidoPolicies f WHERE f.signature = :signature")})
public class FidoPolicies implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected FidoPoliciesPK fidoPoliciesPK;
   
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "policy")
    private String policy;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 8)
    @Column(name = "status")
    private String status;
    @Size(max = 512)
    @Column(name = "notes")
    private String notes;
    @Basic(optional = false)
    @NotNull
    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    @Column(name = "modify_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date modifyDate;
    @Size(max = 2048)
    @Column(name = "signature")
    private String signature;

    public FidoPolicies() {
    }

    public FidoPolicies(FidoPoliciesPK fidoPoliciesPK) {
        this.fidoPoliciesPK = fidoPoliciesPK;
    }

    public FidoPolicies(FidoPoliciesPK fidoPoliciesPK, String policy, String status, Date createDate) {
        this.fidoPoliciesPK = fidoPoliciesPK;
        this.policy = policy;
        this.status = status;
        this.createDate = new Date(createDate.getTime());
    }

    public FidoPolicies(short sid, short did, int pid) {
        this.fidoPoliciesPK = new FidoPoliciesPK(sid, did, pid);
    }

    public FidoPoliciesPK getFidoPoliciesPK() {
        return fidoPoliciesPK;
    }

    public void setFidoPoliciesPK(FidoPoliciesPK fidoPoliciesPK) {
        this.fidoPoliciesPK = fidoPoliciesPK;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }


    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public Date getCreateDate() {
        return new Date(createDate.getTime());
    }

    public void setCreateDate(Date createDate) {
        this.createDate = new Date(createDate.getTime());
    }

    public Date getModifyDate() {
        if (modifyDate == null) {
            return null;
        }
        return new Date(modifyDate.getTime());
    }

    public void setModifyDate(Date modifyDate) {
        this.modifyDate = new Date(modifyDate.getTime());
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
        hash += (fidoPoliciesPK != null ? fidoPoliciesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof FidoPolicies)) {
            return false;
        }
        FidoPolicies other = (FidoPolicies) object;
        if ((this.fidoPoliciesPK == null && other.fidoPoliciesPK != null) || (this.fidoPoliciesPK != null && !this.fidoPoliciesPK.equals(other.fidoPoliciesPK))) {
            return false;
        }
        return true;
    }

    public JsonObjectBuilder toMetaDataJson() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("did", getFidoPoliciesPK().getDid());
        job.add("sid", getFidoPoliciesPK().getSid());
        job.add("pid", getFidoPoliciesPK().getPid());
        job.add("status", getStatus());
        if (getNotes() != null)
            job.add("notes", getNotes());
        job.add("createDate", getCreateDate().getTime());
        if (getModifyDate() != null)
            job.add("modifyDate", getModifyDate().getTime());
        return job;
    }

    public JsonObjectBuilder toJson() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("did", getFidoPoliciesPK().getDid());
        job.add("sid", getFidoPoliciesPK().getSid());
        job.add("pid", getFidoPoliciesPK().getPid());
        if (getPolicy() != null)
            job.add("policy", getPolicy());
        job.add("status", getStatus());
        if (getNotes() != null)
            job.add("notes", getNotes());
        job.add("createDate", getCreateDate().getTime());
        if (getModifyDate() != null)
            job.add("modifyDate", getModifyDate().getTime());
        return job;
    }

    @Override
    public String toString() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("did", getFidoPoliciesPK().getDid());
        job.add("sid", getFidoPoliciesPK().getSid());
        job.add("pid", getFidoPoliciesPK().getPid());
        job.add("policy", getPolicy());
        job.add("status", getStatus());
        job.add("notes", getNotes());
        job.add("createDate", getCreateDate().getTime());
        job.add("modifyDate", getModifyDate().getTime());
        job.add("signature", getSignature());
        return job.build().toString();
    }
}
