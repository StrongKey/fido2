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
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import javax.xml.bind.annotation.XmlRootElement;

@Entity
@Table(name = "attestation_certificates")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "AttestationCertificates.findAll", query = "SELECT a FROM AttestationCertificates a"),
    @NamedQuery(name = "AttestationCertificates.findBySid", query = "SELECT a FROM AttestationCertificates a WHERE a.attestationCertificatesPK.sid = :sid"),
    @NamedQuery(name = "AttestationCertificates.findByDid", query = "SELECT a FROM AttestationCertificates a WHERE a.attestationCertificatesPK.did = :did"),
    @NamedQuery(name = "AttestationCertificates.findByAttcid", query = "SELECT a FROM AttestationCertificates a WHERE a.attestationCertificatesPK.attcid = :attcid"),
    @NamedQuery(name = "AttestationCertificates.findBySidDidAttcid", query = "SELECT a FROM AttestationCertificates a WHERE a.attestationCertificatesPK.sid = :sid and a.attestationCertificatesPK.did = :did and a.attestationCertificatesPK.attcid = :attcid"),
    @NamedQuery(name = "AttestationCertificates.findByParentSid", query = "SELECT a FROM AttestationCertificates a WHERE a.parentSid = :parentSid"),
    @NamedQuery(name = "AttestationCertificates.findByParentDid", query = "SELECT a FROM AttestationCertificates a WHERE a.parentDid = :parentDid"),
    @NamedQuery(name = "AttestationCertificates.findByParentAttcid", query = "SELECT a FROM AttestationCertificates a WHERE a.parentAttcid = :parentAttcid"),
    @NamedQuery(name = "AttestationCertificates.findByIssuerDn", query = "SELECT a FROM AttestationCertificates a WHERE a.issuerDn = :issuerDn"),
    @NamedQuery(name = "AttestationCertificates.findBySubjectDn", query = "SELECT a FROM AttestationCertificates a WHERE a.subjectDn = :subjectDn"),
    @NamedQuery(name = "AttestationCertificates.findBySerialNumber", query = "SELECT a FROM AttestationCertificates a WHERE a.serialNumber = :serialNumber"),
    @NamedQuery(name = "AttestationCertificates.findByIssuerDnSerialNumber", query = "SELECT a FROM AttestationCertificates a WHERE a.issuerDn = :issuerDn and a.serialNumber = :serialNumber"),
    @NamedQuery(name = "AttestationCertificates.maxattcid", query = "SELECT max(a.attestationCertificatesPK.attcid) FROM AttestationCertificates a where a.attestationCertificatesPK.sid = :sid"),
    @NamedQuery(name = "AttestationCertificates.findBySignature", query = "SELECT a FROM AttestationCertificates a WHERE a.signature = :signature")})
public class AttestationCertificates implements Serializable {

    private static final long serialVersionUID = 1L;
    @EmbeddedId
    protected AttestationCertificatesPK attestationCertificatesPK;
    @Column(name = "parent_sid")
    private Short parentSid;
    @Column(name = "parent_did")
    private Short parentDid;
    @Column(name = "parent_attcid")
    private Integer parentAttcid;
    @Basic(optional = false)
    @NotNull
    @Lob
    @Size(min = 1, max = 2147483647)
    @Column(name = "certificate")
    private String certificate;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 1024)
    @Column(name = "issuer_dn")
    private String issuerDn;
    @Basic(optional = false)
    @NotNull
    @Size(min = 0, max = 1024)
    @Column(name = "subject_dn")
    private String subjectDn;
    @Basic(optional = false)
    @NotNull
    @Size(min = 1, max = 512)
    @Column(name = "serial_number")
    private String serialNumber;
    @Size(max = 2048)
    @Column(name = "signature")
    private String signature;

    public AttestationCertificates() {
    }

    public AttestationCertificates(AttestationCertificatesPK attestationCertificatesPK) {
        this.attestationCertificatesPK = attestationCertificatesPK;
    }

    public AttestationCertificates(AttestationCertificatesPK attestationCertificatesPK, String certificate, String issuerDn, String subjectDn, String serialNumber) {
        this.attestationCertificatesPK = attestationCertificatesPK;
        this.certificate = certificate;
        this.issuerDn = issuerDn;
        this.subjectDn = subjectDn;
        this.serialNumber = serialNumber;
    }

    public AttestationCertificates(short sid, short did, int attcid) {
        this.attestationCertificatesPK = new AttestationCertificatesPK(sid, did, attcid);
    }

    public AttestationCertificatesPK getAttestationCertificatesPK() {
        return attestationCertificatesPK;
    }

    public void setAttestationCertificatesPK(AttestationCertificatesPK attestationCertificatesPK) {
        this.attestationCertificatesPK = attestationCertificatesPK;
    }

    public Short getParentSid() {
        return parentSid;
    }

    public void setParentSid(Short parentSid) {
        this.parentSid = parentSid;
    }

    public Short getParentDid() {
        return parentDid;
    }

    public void setParentDid(Short parentDid) {
        this.parentDid = parentDid;
    }

    public Integer getParentAttcid() {
        return parentAttcid;
    }

    public void setParentAttcid(Integer parentAttcid) {
        this.parentAttcid = parentAttcid;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getIssuerDn() {
        return issuerDn;
    }

    public void setIssuerDn(String issuerDn) {
        this.issuerDn = issuerDn;
    }

    public String getSubjectDn() {
        return subjectDn;
    }

    public void setSubjectDn(String subjectDn) {
        this.subjectDn = subjectDn;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
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
        hash += (attestationCertificatesPK != null ? attestationCertificatesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof AttestationCertificates)) {
            return false;
        }
        AttestationCertificates other = (AttestationCertificates) object;
        if ((this.attestationCertificatesPK == null && other.attestationCertificatesPK != null) || (this.attestationCertificatesPK != null && !this.attestationCertificatesPK.equals(other.attestationCertificatesPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "AttestationCertificates[ attestationCertificatesPK=" + attestationCertificatesPK + " ]";
    }

}
