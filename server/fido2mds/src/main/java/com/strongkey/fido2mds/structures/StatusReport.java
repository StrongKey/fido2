/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

public class StatusReport {
    private AuthenticatorStatus status;
    private String effectiveDate;
    private String certificate;
    private String url;
    private String certificationDescriptor;
    private String certificateNumber;
    private String certificationPolicyVersion;
    private String certificationRequirementsVersion;

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
    
    
}
