/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

//https://fidoalliance.org/specs/fido-v2.0-id-20180227/fido-metadata-statement-v2.0-id-20180227.pdf

import java.math.BigInteger;
import java.util.List;

public class MetadataStatement {
    private String legalHeader;
    private AAID aaid;
    private AAGUID aaguid;
    private List<String> attestationCertificateKeyIdentifiers;
    private String description;
    private AlternativeDescriptions alternativeDescriptions;
    private Integer authenticatorVersion;
    private String protocolFamily;
    private List<Version> upv;
    private String assertionScheme;
    private Integer authenticationAlgorithm;
    private List<Integer> authenticationAlgorithms;
    private Integer publicKeyAlgAndEncoding;
    private List<Integer> publicKeyAlgAndEncodings;
    private List<Integer> attestationTypes;
    private List<List<VerificationMethodDescriptor>> userVerificationDetails;
    private Integer keyProtection;
    private Boolean isKeyRestricted;
    private Boolean isFreshUserVerificationRequired;
    private Integer matcherProtection;
    private Integer cryptoStrength;
    private String operatingEnv;
    private BigInteger attachmentHint;
    private Boolean isSecondFactorOnly;
    private Integer tcDisplay;
    private String tcDisplayContentType;
    private List<DisplayPNGCharacteristicsDescriptor> tcDisplayPNGCharacteristics;
    private List<String> attestationRootCertificates;
    private List<EcdaaTrustAnchor> ecdaaTrustAnchors;
    private String icon;
    private List<ExtensionDescriptor> supportedExtensions;

    public String getLegalHeader() {
        return legalHeader;
    }

    public void setLegalHeader(String legalHeader) {
        this.legalHeader = legalHeader;
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public AlternativeDescriptions getAlternativeDescriptions() {
        return alternativeDescriptions;
    }

    public void setAlternativeDescriptions(AlternativeDescriptions alternativeDescriptions) {
        this.alternativeDescriptions = alternativeDescriptions;
    }

    public Integer getAuthenticatorVersion() {
        return authenticatorVersion;
    }

    public void setAuthenticatorVersion(Integer authenticatorVersion) {
        this.authenticatorVersion = authenticatorVersion;
    }

    public String getProtocolFamily() {
        return protocolFamily;
    }

    public void setProtocolFamily(String protocolFamily) {
        this.protocolFamily = protocolFamily;
    }

    public List<Version> getUpv() {
        return upv;
    }

    public void setUpv(List<Version> upv) {
        this.upv = upv;
    }

    public String getAssertionScheme() {
        return assertionScheme;
    }

    public void setAssertionScheme(String assertionScheme) {
        this.assertionScheme = assertionScheme;
    }

    public Integer getAuthenticationAlgorithm() {
        return authenticationAlgorithm;
    }

    public void setAuthenticationAlgorithm(Integer authenticationAlgorithm) {
        this.authenticationAlgorithm = authenticationAlgorithm;
    }

    public List<Integer> getAuthenticationAlgorithms() {
        return authenticationAlgorithms;
    }

    public void setAuthenticationAlgorithms(List<Integer> authenticationAlgorithms) {
        this.authenticationAlgorithms = authenticationAlgorithms;
    }

    public Integer getPublicKeyAlgAndEncoding() {
        return publicKeyAlgAndEncoding;
    }

    public void setPublicKeyAlgAndEncoding(Integer publicKeyAlgAndEncoding) {
        this.publicKeyAlgAndEncoding = publicKeyAlgAndEncoding;
    }

    public List<Integer> getPublicKeyAlgAndEncodings() {
        return publicKeyAlgAndEncodings;
    }

    public void setPublicKeyAlgAndEncodings(List<Integer> publicKeyAlgAndEncodings) {
        this.publicKeyAlgAndEncodings = publicKeyAlgAndEncodings;
    }

    public List<Integer> getAttestationTypes() {
        return attestationTypes;
    }

    public void setAttestationTypes(List<Integer> attestationTypes) {
        this.attestationTypes = attestationTypes;
    }

    public List<List<VerificationMethodDescriptor>> getUserVerificationDetails() {
        return userVerificationDetails;
    }

    public void setUserVerificationDetails(List<List<VerificationMethodDescriptor>> userVerificationDetails) {
        this.userVerificationDetails = userVerificationDetails;
    }

    public Integer getKeyProtection() {
        return keyProtection;
    }

    public void setKeyProtection(Integer keyProtection) {
        this.keyProtection = keyProtection;
    }

    public Boolean getIsKeyRestricted() {
        return isKeyRestricted;
    }

    public void setIsKeyRestricted(Boolean isKeyRestricted) {
        this.isKeyRestricted = isKeyRestricted;
    }

    public Boolean getIsFreshUserVerificationRequired() {
        return isFreshUserVerificationRequired;
    }

    public void setIsFreshUserVerificationRequired(Boolean isFreshUserVerificationRequired) {
        this.isFreshUserVerificationRequired = isFreshUserVerificationRequired;
    }

    public Integer getMatcherProtection() {
        return matcherProtection;
    }

    public void setMatcherProtection(Integer matcherProtection) {
        this.matcherProtection = matcherProtection;
    }

    public Integer getCryptoStrength() {
        return cryptoStrength;
    }

    public void setCryptoStrength(Integer cryptoStrength) {
        this.cryptoStrength = cryptoStrength;
    }

    public String getOperatingEnv() {
        return operatingEnv;
    }

    public void setOperatingEnv(String operatingEnv) {
        this.operatingEnv = operatingEnv;
    }

    public BigInteger getAttachmentHint() {
        return attachmentHint;
    }

    public void setAttachmentHint(BigInteger attachmentHint) {
        this.attachmentHint = attachmentHint;
    }

    public Boolean getIsSecondFactorOnly() {
        return isSecondFactorOnly;
    }

    public void setIsSecondFactorOnly(Boolean isSecondFactorOnly) {
        this.isSecondFactorOnly = isSecondFactorOnly;
    }

    public Integer getTcDisplay() {
        return tcDisplay;
    }

    public void setTcDisplay(Integer tcDisplay) {
        this.tcDisplay = tcDisplay;
    }

    public String getTcDisplayContentType() {
        return tcDisplayContentType;
    }

    public void setTcDisplayContentType(String tcDisplayContentType) {
        this.tcDisplayContentType = tcDisplayContentType;
    }

    public List<DisplayPNGCharacteristicsDescriptor> getTcDisplayPNGCharacteristics() {
        return tcDisplayPNGCharacteristics;
    }

    public void setTcDisplayPNGCharacteristics(List<DisplayPNGCharacteristicsDescriptor> tcDisplayPNGCharacteristics) {
        this.tcDisplayPNGCharacteristics = tcDisplayPNGCharacteristics;
    }

    public List<String> getAttestationRootCertificates() {
        return attestationRootCertificates;
    }

    public void setAttestationRootCertificates(List<String> attestationRootCertificates) {
        this.attestationRootCertificates = attestationRootCertificates;
    }

    public List<EcdaaTrustAnchor> getEcdaaTrustAnchors() {
        return ecdaaTrustAnchors;
    }

    public void setEcdaaTrustAnchors(List<EcdaaTrustAnchor> ecdaaTrustAnchors) {
        this.ecdaaTrustAnchors = ecdaaTrustAnchors;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public List<ExtensionDescriptor> getSupportedExtensions() {
        return supportedExtensions;
    }

    public void setSupportedExtensions(List<ExtensionDescriptor> supportedExtensions) {
        this.supportedExtensions = supportedExtensions;
    }
    
    
    
}
