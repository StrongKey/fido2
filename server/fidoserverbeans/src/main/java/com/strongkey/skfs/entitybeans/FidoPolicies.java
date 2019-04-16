/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.entitybeans;

import java.io.Serializable;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
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
    @NamedQuery(name = "FidoPolicies.findMetadataByDid", query = "SELECT f.fidoPoliciesPK.sid, f.fidoPoliciesPK.did, f.fidoPoliciesPK.pid, f.certificateProfileName, f.version, f.status, f.endDate, f.createDate, f.modifyDate, f.notes FROM FidoPolicies f WHERE f.fidoPoliciesPK.did = :did"),
    @NamedQuery(name = "FidoPolicies.findByPid", query = "SELECT f FROM FidoPolicies f WHERE f.fidoPoliciesPK.pid = :pid"),
    @NamedQuery(name = "FidoPolicies.findBySidDidPid", query = "SELECT f FROM FidoPolicies f WHERE f.fidoPoliciesPK.sid = :sid and f.fidoPoliciesPK.did = :did and f.fidoPoliciesPK.pid = :pid"),
    @NamedQuery(name = "FidoPolicies.findMetadataBySidDidPid", query = "SELECT f.fidoPoliciesPK.sid, f.fidoPoliciesPK.did, f.fidoPoliciesPK.pid, f.certificateProfileName, f.version, f.status, f.endDate, f.createDate, f.modifyDate, f.notes FROM FidoPolicies f WHERE f.fidoPoliciesPK.sid = :sid and f.fidoPoliciesPK.did = :did and f.fidoPoliciesPK.pid = :pid"),
    @NamedQuery(name = "FidoPolicies.findByStartDate", query = "SELECT f FROM FidoPolicies f WHERE f.startDate = :startDate"),
    @NamedQuery(name = "FidoPolicies.findByEndDate", query = "SELECT f FROM FidoPolicies f WHERE f.endDate = :endDate"),
    @NamedQuery(name = "FidoPolicies.findByCertificateProfileName", query = "SELECT f FROM FidoPolicies f WHERE f.certificateProfileName = :certificateProfileName"),
    @NamedQuery(name = "FidoPolicies.findByVersion", query = "SELECT f FROM FidoPolicies f WHERE f.version = :version"),
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
    @Column(name = "start_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date startDate;
    @Column(name = "end_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date endDate;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 64)
    @Column(name = "certificate_profile_name")
    private String certificateProfileName;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "policy")
    private String policy;
    @Basic(optional = false)
    @NotNull
    @Column(name = "version")
    private int version;
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

    public FidoPolicies(FidoPoliciesPK fidoPoliciesPK, Date startDate, String certificateProfileName, String policy, int version, String status, Date createDate) {
        this.fidoPoliciesPK = fidoPoliciesPK;
        this.startDate = startDate;
        this.certificateProfileName = certificateProfileName;
        this.policy = policy;
        this.version = version;
        this.status = status;
        this.createDate = createDate;
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

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getCertificateProfileName() {
        return certificateProfileName;
    }

    public void setCertificateProfileName(String certificateProfileName) {
        this.certificateProfileName = certificateProfileName;
    }

    public String getPolicy() {
        return policy;
    }

    public void setPolicy(String policy) {
        this.policy = policy;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
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

    public JsonObjectBuilder toJson() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("did", getFidoPoliciesPK().getDid());
        job.add("sid", getFidoPoliciesPK().getSid());
        job.add("pid", getFidoPoliciesPK().getPid());
        job.add("startdate", getStartDate().getTime());
        job.add("enddate", getEndDate().getTime());
        job.add("name", getCertificateProfileName());
        if (getPolicy()!= null)
            job.add("policy", getPolicy());
        job.add("version", getVersion());
        job.add("status", getStatus());
        job.add("notes", getNotes());
        job.add("createDate", getCreateDate().getTime());
        job.add("modifyDate", getModifyDate().getTime());
        if (getSignature() != null)
            job.add("signature", getSignature());
        return job;
    }

    @Override
    public String toString() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("did", getFidoPoliciesPK().getDid());
        job.add("sid", getFidoPoliciesPK().getSid());
        job.add("pid", getFidoPoliciesPK().getPid());
        job.add("startdate", getStartDate().getTime());
        job.add("enddate", getEndDate().getTime());
        job.add("name", getCertificateProfileName());
        job.add("policy", getPolicy());
        job.add("version", getVersion());
        job.add("status", getStatus());
        job.add("notes", getNotes());
        job.add("createDate", getCreateDate().getTime());
        job.add("modifyDate", getModifyDate().getTime());
        job.add("signature", getSignature());
        return job.build().toString();
    }
}
