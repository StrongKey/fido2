/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class StatusReport {
    private AuthenticatorStatus status;
    private String effectiveDate;
    private String certificate;
    private String url;
    private String certificationDescriptor;
    private String certificateNumber;
    private String certificationPolicyVersion;
    private String certificationRequirementsVersion;
    
    public StatusReport(JsonObject jsonInput) {
        if (jsonInput.containsKey("status")) status = AuthenticatorStatus.valueOf(jsonInput.getString("status"));
        if (jsonInput.containsKey("effectiveDate")) effectiveDate = jsonInput.getString("effectiveDate");
        if (jsonInput.containsKey("certificate")) certificate = jsonInput.getString("certificate");
        if (jsonInput.containsKey("url")) url = jsonInput.getString("url");
        if (jsonInput.containsKey("certificationDescriptor")) certificationDescriptor = jsonInput.getString("certificationDescriptor");
        if (jsonInput.containsKey("certificateNumber")) certificateNumber = jsonInput.getString("certificateNumber");
        if (jsonInput.containsKey("certificationPolicyVersion")) certificationPolicyVersion = jsonInput.getString("certificationPolicyVersion");
        if (jsonInput.containsKey("certificationRequirementsVersion")) certificationRequirementsVersion = jsonInput.getString("certificationRequirementsVersion");
    }

    public AuthenticatorStatus getStatus() {
        return status;
    }

    public void setStatus(AuthenticatorStatus status) {
        this.status = status;
    }

    public String getEffectiveDate() {
        return effectiveDate;
    }

    public void setEffectiveDate(String effectiveDate) {
        this.effectiveDate = effectiveDate;
    }

    public String getCertificate() {
        return certificate;
    }

    public void setCertificate(String certificate) {
        this.certificate = certificate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCertificationDescriptor() {
        return certificationDescriptor;
    }

    public void setCertificationDescriptor(String certificationDescriptor) {
        this.certificationDescriptor = certificationDescriptor;
    }

    public String getCertificateNumber() {
        return certificateNumber;
    }

    public void setCertificateNumber(String certificateNumber) {
        this.certificateNumber = certificateNumber;
    }

    public String getCertificationPolicyVersion() {
        return certificationPolicyVersion;
    }

    public void setCertificationPolicyVersion(String certificationPolicyVersion) {
        this.certificationPolicyVersion = certificationPolicyVersion;
    }

    public String getCertificationRequirementsVersion() {
        return certificationRequirementsVersion;
    }

    public void setCertificationRequirementsVersion(String certificationRequirementsVersion) {
        this.certificationRequirementsVersion = certificationRequirementsVersion;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.status != null) job.add("status", status.toString());
        if (this.effectiveDate != null) job.add("effectiveDate", effectiveDate);
        if (this.certificate != null) job.add("certificate", certificate);
        if (this.url != null) job.add("url", url);
        if (this.certificationDescriptor != null) job.add("certificationDescriptor", certificationDescriptor);
        if (this.certificateNumber != null) job.add("certificateNumber", certificateNumber);
        if (this.certificationPolicyVersion != null) job.add("certificationPolicyVersion", certificationPolicyVersion);
        if (this.certificationRequirementsVersion != null) job.add("certificationRequirementsVersion", certificationRequirementsVersion);
        return job.build();
    }
}
