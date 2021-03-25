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
 * Entity bean for the UTX_FIDO_AUTHENTICATOR_REFERENCES table
 */

package com.strongkey.sfaeco.entitybeans;

import java.io.Serializable;
import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
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
@Table(name = "utx_fido_authenticator_references")
@XmlRootElement
@NamedQueries({
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findAll", query = "SELECT u FROM UtxFidoAuthenticatorReferences u"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByDid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.utxFidoAuthenticatorReferencesPK.did = :did"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findBySid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.utxFidoAuthenticatorReferencesPK.sid = :sid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByUid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.utxFidoAuthenticatorReferencesPK.uid = :uid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByUtxid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.utxFidoAuthenticatorReferencesPK.utxid = :utxid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByFarid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.utxFidoAuthenticatorReferencesPK.farid = :farid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByFidoid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.fidoid = :fidoid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByRpid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.rpid = :rpid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByAaguid", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.aaguid = :aaguid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByUp", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.up = :up"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByUv", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.uv = :uv"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByUsedForThisTransaction", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.usedForThisTransaction = :usedForThisTransaction"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByCreateDate", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.createDate = :createDate"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.maxPK", query = "SELECT max(u.utxFidoAuthenticatorReferencesPK.farid) FROM UtxFidoAuthenticatorReferences u "
            + "WHERE u.utxFidoAuthenticatorReferencesPK.did = :did and u.utxFidoAuthenticatorReferencesPK.sid = :sid"),
    @NamedQuery(name = "UtxFidoAuthenticatorReferences.findByUserTransaction", query = "SELECT u FROM UtxFidoAuthenticatorReferences u WHERE u.utxFidoAuthenticatorReferencesPK.did = :did and u.utxFidoAuthenticatorReferencesPK.uid = :uid and u.utxFidoAuthenticatorReferencesPK.utxid = :utxid"),

})

public class UtxFidoAuthenticatorReferences implements Serializable {

    private static final long serialVersionUID = 1L;
    
    @EmbeddedId
    protected UtxFidoAuthenticatorReferencesPK utxFidoAuthenticatorReferencesPK;
    
    @Basic(optional = false)
    @Column(name = "protocol")
    private String protocol;
    
    @Basic(optional = false)
    @Column(name = "fidoid")
    private String fidoid;
    
    @Basic(optional = false)
    @Column(name = "rawid")
    private String rawid;
    
    @Column(name = "user_handle")
    private String userHandle;
    
    @Basic(optional = false)
    @Column(name = "rpid")
    private String rpid;
    
    @Basic(optional = false)
    @Column(name = "authenticator_data")
    private String authenticatorData;
    
    @Basic(optional = false)
    @Column(name = "client_data_json")
    private String clientDataJson;
    
    @Basic(optional = false)
    @Column(name = "aaguid")
    private String aaguid;
    
    @Basic(optional = false)
    @Column(name = "auth_time")
    @Temporal(TemporalType.TIMESTAMP)
    private Date authTime;
    
    @Basic(optional = false)
    @Column(name = "up")
    private boolean up;
    
    @Basic(optional = false)
    @Column(name = "uv")
    private boolean uv;
    
    @Basic(optional = false)
    @Column(name = "used_for_this_transaction")
    private boolean usedForThisTransaction;
    
    @Basic(optional = false)
    @Column(name = "signing_key_type")
    private String signingKeyType;
    
    @Basic(optional = false)
    @Column(name = "signing_key_algorithm")
    private String signingKeyAlgorithm;
    
    @Basic(optional = false)
    @Column(name = "signer_public_key")
    private String signerPublicKey;
    
    @Basic(optional = false)
    @Column(name = "fido_signature")
    private String fidoSignature;
    
    @Basic(optional = false)
    @Column(name = "create_date")
    @Temporal(TemporalType.TIMESTAMP)
    private Date createDate;
    
    @Column(name = "signature")
    private String signature;

    public UtxFidoAuthenticatorReferences() {
    }

    public UtxFidoAuthenticatorReferences(UtxFidoAuthenticatorReferencesPK utxFidoAuthenticatorReferencesPK) {
        this.utxFidoAuthenticatorReferencesPK = utxFidoAuthenticatorReferencesPK;
    }

    public UtxFidoAuthenticatorReferences(short did, short sid, long uid, long utxid, long farid) {
        this.utxFidoAuthenticatorReferencesPK = new UtxFidoAuthenticatorReferencesPK(did, sid, uid, utxid, farid);
    }

    public UtxFidoAuthenticatorReferencesPK getUtxFidoAuthenticatorReferencesPK() {
        return utxFidoAuthenticatorReferencesPK;
    }

    public void setUtxFidoAuthenticatorReferencesPK(UtxFidoAuthenticatorReferencesPK utxFidoAuthenticatorReferencesPK) {
        this.utxFidoAuthenticatorReferencesPK = utxFidoAuthenticatorReferencesPK;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getFidoid() {
        return fidoid;
    }

    public void setFidoid(String fidoid) {
        this.fidoid = fidoid;
    }

    public String getRawid() {
        return rawid;
    }

    public void setRawid(String rawid) {
        this.rawid = rawid;
    }

    public String getUserHandle() {
        return userHandle;
    }

    public void setUserHandle(String userHandle) {
        this.userHandle = userHandle;
    }

    public String getRpid() {
        return rpid;
    }

    public void setRpid(String rpid) {
        this.rpid = rpid;
    }

    public String getAuthenticatorData() {
        return authenticatorData;
    }

    public void setAuthenticatorData(String authenticatorData) {
        this.authenticatorData = authenticatorData;
    }

    public String getClientDataJson() {
        return clientDataJson;
    }

    public void setClientDataJson(String clientDataJson) {
        this.clientDataJson = clientDataJson;
    }

    public String getAaguid() {
        return aaguid;
    }

    public void setAaguid(String aaguid) {
        this.aaguid = aaguid;
    }

    public Date getAuthTime() {
        return authTime;
    }

    public void setAuthTime(Date authTime) {
        this.authTime = authTime;
    }

    public boolean getUp() {
        return up;
    }

    public void setUp(boolean up) {
        this.up = up;
    }

    public boolean getUv() {
        return uv;
    }

    public void setUv(boolean uv) {
        this.uv = uv;
    }

    public boolean getUsedForThisTransaction() {
        return usedForThisTransaction;
    }

    public void setUsedForThisTransaction(boolean usedForThisTransaction) {
        this.usedForThisTransaction = usedForThisTransaction;
    }

     public String getSigningKeyType() {
        return signingKeyType;
    }

    public void setSigningKeyType(String signingKeyType) {
        this.signingKeyType = signingKeyType;
    }

    public String getSigningKeyAlgorithm() {
        return signingKeyAlgorithm;
    }

    public void setSigningKeyAlgorithm(String signingKeyAlgorithm) {
        this.signingKeyAlgorithm = signingKeyAlgorithm;
    }

    public String getSignerPublicKey() {
        return signerPublicKey;
    }

    public void setSignerPublicKey(String signerPublicKey) {
        this.signerPublicKey = signerPublicKey;
    }

    public String getFidoSignature() {
        return fidoSignature;
    }

    public void setFidoSignature(String fidoSignature) {
        this.fidoSignature = fidoSignature;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
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
        hash += (utxFidoAuthenticatorReferencesPK != null ? utxFidoAuthenticatorReferencesPK.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        // TODO: Warning - this method won't work in the case the id fields are not set
        if (!(object instanceof UtxFidoAuthenticatorReferences)) {
            return false;
        }
        UtxFidoAuthenticatorReferences other = (UtxFidoAuthenticatorReferences) object;
        if ((this.utxFidoAuthenticatorReferencesPK == null && other.utxFidoAuthenticatorReferencesPK != null) || (this.utxFidoAuthenticatorReferencesPK != null && !this.utxFidoAuthenticatorReferencesPK.equals(other.utxFidoAuthenticatorReferencesPK))) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "UtxFidoAuthenticatorReferences[ utxFidoAuthenticatorReferencesPK=" + utxFidoAuthenticatorReferencesPK + " ]";
    }
    
 
     public JsonObject toJsonObject(){
        JsonObjectBuilder job = Json.createObjectBuilder();
//        job.add("sid", this.getUtxFidoAuthenticatorReferencesPK().getSid());
//        job.add("did", this.getUtxFidoAuthenticatorReferencesPK().getDid());
//        job.add("uid", this.getUtxFidoAuthenticatorReferencesPK().getUid());
//        job.add("utxid", this.getUtxFidoAuthenticatorReferencesPK().getUtxid());
        job.add("farid", this.getUtxFidoAuthenticatorReferencesPK().getFarid());
        job.add("protocol", this.getProtocol());
        job.add("fidoId", this.getFidoid());
        job.add("rawId", this.getRawid());
        job.add("userHandle", this.getUserHandle());
        job.add("rpId", this.getRpid());
        job.add("authenticatorData", this.getAuthenticatorData());
        job.add("clientDataJson", this.getClientDataJson());
        job.add("aaguid", this.getAaguid());
        job.add("authTime", this.getAuthTime().toString());
        job.add("up", this.getUp());
        job.add("uv", this.getUv());
        job.add("usedForThisTransaction", this.getUsedForThisTransaction());
        job.add("signingKeyType",this.getSigningKeyType());
        job.add("signingKeyAlgorithm",this.getSigningKeyAlgorithm());
        job.add("signerPublicKey",this.getSignerPublicKey());
        job.add("fidoSignature", this.getFidoSignature());
        job.add("createDate", this.getCreateDate().toString());
        job.add("signature", this.getSignature());

        return job.build();
    }

}
