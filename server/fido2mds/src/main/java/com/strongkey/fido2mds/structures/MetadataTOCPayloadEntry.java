/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.util.List;

public class MetadataTOCPayloadEntry {
    private AAID aaid;
    private AAGUID aaguid;
    private List<String> attestationCertificateKeyIdentifiers;
    private String hash;
    private String url;
    private List<StatusReport> statusReports;
    private String timeOfLastStatusChange;
    private String rogueListURL;
    private String rougeListHash;

    public AAID getAaid() {
        return aaid;
    }

    public void setAaid(AAID aaid) {
        this.aaid = aaid;
    }

    public AAGUID getAaguid() {
        return aaguid;
    }

    public void setAaguid(AAGUID aaguid) {
        this.aaguid = aaguid;
    }

    public List<String> getAttestationCertificateKeyIdentifiers() {
        return attestationCertificateKeyIdentifiers;
    }

    public void setAttestationCertificateKeyIdentifiers(List<String> attestationCertificateKeyIdentifiers) {
        this.attestationCertificateKeyIdentifiers = attestationCertificateKeyIdentifiers;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<StatusReport> getStatusReports() {
        return statusReports;
    }

    public void setStatusReports(List<StatusReport> statusReports) {
        this.statusReports = statusReports;
    }

    public String getTimeOfLastStatusChange() {
        return timeOfLastStatusChange;
    }

    public void setTimeOfLastStatusChange(String timeOfLastStatusChange) {
        this.timeOfLastStatusChange = timeOfLastStatusChange;
    }

    public String getRogueListURL() {
        return rogueListURL;
    }

    public void setRogueListURL(String rogueListURL) {
        this.rogueListURL = rogueListURL;
    }

    public String getRougeListHash() {
        return rougeListHash;
    }

    public void setRougeListHash(String rougeListHash) {
        this.rougeListHash = rougeListHash;
    }
    
    
}
