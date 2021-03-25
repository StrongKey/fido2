/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
    
    public MetadataTOCPayloadEntry(JsonObject jsonInput) {
        JsonArray jsonArray;
        if (jsonInput.containsKey("aaid")) aaid = new AAID(jsonInput.getString("aaid"));
        if (jsonInput.containsKey("aaguid")) aaguid = new AAGUID(jsonInput.getString("aaguid"));
        if (jsonInput.containsKey("attestationCertificateKeyIdentifiers")) {
            attestationCertificateKeyIdentifiers = new ArrayList<String>();
            jsonArray = jsonInput.getJsonArray("attestationCertificateKeyIdentifiers");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    attestationCertificateKeyIdentifiers.add(jsonArray.getString(i));
                }
            }
        }
        if (jsonInput.containsKey("hash")) hash = jsonInput.getString("hash");
        if (jsonInput.containsKey("url")) url = jsonInput.getString("url");
        if (jsonInput.containsKey("statusReports")) {
            statusReports = new ArrayList<StatusReport>();
            jsonArray = jsonInput.getJsonArray("statusReports");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    statusReports.add(new StatusReport(jsonArray.getJsonObject(i)));
                }
            }
        }
        if (jsonInput.containsKey("timeOfLastStatusChange")) timeOfLastStatusChange = jsonInput.getString("timeOfLastStatusChange");
        if (jsonInput.containsKey("rogueListURL")) rogueListURL = jsonInput.getString("rogueListURL");
        if (jsonInput.containsKey("rougeListHash")) rougeListHash = jsonInput.getString("rougeListHash");
    }

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
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.aaid != null) job.add("aaid", aaid.toJsonObject());
        if (this.aaguid != null) job.add("aaguid", this.aaguid.toJsonObject());
        if (this.attestationCertificateKeyIdentifiers != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            attestationCertificateKeyIdentifiers.stream().forEach(identifier -> jab.add(identifier));
            job.add("attestationCertificateKeyIdentifiers", jab);
        }
        if (this.hash != null) job.add("hash", hash);
        if (this.url != null) job.add("url", url);
        if (this.statusReports != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            statusReports.stream().forEach(statusReport -> jab.add(statusReport.toJsonObject()));
            job.add("statusReports", jab);
        }
        if (this.timeOfLastStatusChange != null) job.add("timeOfLastStatusChange", timeOfLastStatusChange);
        if (this.rogueListURL != null) job.add("rogueListURL", rogueListURL);
        if (this.rougeListHash != null) job.add("rougeListHash", rougeListHash);
        return job.build();
    }
}
