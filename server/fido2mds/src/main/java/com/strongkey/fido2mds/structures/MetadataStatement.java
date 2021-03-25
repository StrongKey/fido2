/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

//https://fidoalliance.org/specs/fido-v2.0-id-20180227/fido-metadata-statement-v2.0-id-20180227.pdf

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

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
    
    public MetadataStatement(JsonObject jsonInput) {
        
        JsonArray jsonArray;
        
        if (jsonInput.containsKey("legalHeader")) legalHeader = jsonInput.getString("legalHeader");
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
        if (jsonInput.containsKey("description")) description = jsonInput.getString("description");
        if (jsonInput.containsKey("alternativeDescriptions")) {
            alternativeDescriptions = new AlternativeDescriptions();
            jsonInput.getJsonObject("alternativeDescriptions").keySet().forEach(key -> alternativeDescriptions.setAlternativeDescriptions(key, jsonInput.getJsonObject("alternativeDescriptions").getString(key)));
        }
        if (jsonInput.containsKey("authenticatorVersion")) authenticatorVersion = jsonInput.getInt("authenticatorVersion");
        if (jsonInput.containsKey("protocolFamily")) protocolFamily = jsonInput.getString("protocolFamily");
        if (jsonInput.containsKey("upv")) {
            upv = new ArrayList<Version>();
            jsonArray = jsonInput.getJsonArray("upv");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    upv.add(new Version(jsonArray.getJsonObject(i)));
                }
            }
        }
        if (jsonInput.containsKey("assertionScheme")) assertionScheme = jsonInput.getString("assertionScheme");
        if (jsonInput.containsKey("authenticationAlgorithm")) authenticationAlgorithm = jsonInput.getInt("authenticationAlgorithm");
        if (jsonInput.containsKey("authenticationAlgorithms")) {
            authenticationAlgorithms = new ArrayList<Integer>();
            jsonArray = jsonInput.getJsonArray("authenticationAlgorithms");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    authenticationAlgorithms.add(jsonArray.getInt(i));
                }
            }
        }
        if (jsonInput.containsKey("publicKeyAlgAndEncoding")) publicKeyAlgAndEncoding = jsonInput.getInt("publicKeyAlgAndEncoding");
        if (jsonInput.containsKey("publicKeyAlgAndEncodings")) {
            publicKeyAlgAndEncodings = new ArrayList<Integer>();
            jsonArray = jsonInput.getJsonArray("publicKeyAlgAndEncodings");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    publicKeyAlgAndEncodings.add(jsonArray.getInt(i));
                }
            }
        }
        if (jsonInput.containsKey("attestationTypes")) {
            attestationTypes = new ArrayList<Integer>();
            jsonArray = jsonInput.getJsonArray("attestationTypes");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    attestationTypes.add(jsonArray.getInt(i));
                }
            }
        }
        if (jsonInput.containsKey("userVerificationDetails")) {
            userVerificationDetails = new ArrayList<List<VerificationMethodDescriptor>>();
            jsonArray = jsonInput.getJsonArray("userVerificationDetails");
            if (jsonArray != null) {
                int outerlen = jsonArray.size();
                for (int i = 0; i < outerlen; i++) {
                    ArrayList<VerificationMethodDescriptor> innerList = new ArrayList<VerificationMethodDescriptor>();
                    if (jsonArray.getJsonArray(i) != null) {
                        int innerlen = jsonArray.size();
                        for (int j = 0; j < innerlen; j++) {
                            innerList.add(new VerificationMethodDescriptor(jsonArray.getJsonArray(i).getJsonObject(j)));
                        }
                    }
                    userVerificationDetails.add(innerList);
                }
            }
        }
        if (jsonInput.containsKey("keyProtection")) keyProtection = jsonInput.getInt("keyProtection");
        if (jsonInput.containsKey("isKeyRestricted")) isKeyRestricted = jsonInput.getBoolean("isKeyRestricted");
        if (jsonInput.containsKey("isFreshUserVerificationRequired")) isFreshUserVerificationRequired = jsonInput.getBoolean("isFreshUserVerificationRequired");
        if (jsonInput.containsKey("matcherProtection")) matcherProtection = jsonInput.getInt("matcherProtection");
        if (jsonInput.containsKey("cryptoStrength")) cryptoStrength = jsonInput.getInt("cryptoStrength");
        if (jsonInput.containsKey("operatingEnv")) operatingEnv = jsonInput.getString("operatingEnv");
        if (jsonInput.containsKey("attachmentHint")) attachmentHint = jsonInput.getJsonNumber("attachmentHint").bigIntegerValueExact();
        if (jsonInput.containsKey("isSecondFactorOnly")) isSecondFactorOnly = jsonInput.getBoolean("isSecondFactorOnly");
        if (jsonInput.containsKey("tcDisplay")) tcDisplay = jsonInput.getInt("tcDisplay");
        if (jsonInput.containsKey("tcDisplayContentType")) tcDisplayContentType = jsonInput.getString("tcDisplayContentType");
        if (jsonInput.containsKey("tcDisplayPNGCharacteristics")) {
            tcDisplayPNGCharacteristics = new ArrayList<DisplayPNGCharacteristicsDescriptor>();
            jsonArray = jsonInput.getJsonArray("tcDisplayPNGCharacteristics");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    tcDisplayPNGCharacteristics.add(new DisplayPNGCharacteristicsDescriptor(jsonArray.getJsonObject(i)));
                }
            }
        }
        if (jsonInput.containsKey("attestationRootCertificates")) {
            attestationRootCertificates = new ArrayList<String>();
            jsonArray = jsonInput.getJsonArray("attestationRootCertificates");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    attestationRootCertificates.add(jsonArray.getString(i));
                }
            }
        }
        if (jsonInput.containsKey("ecdaaTrustAnchors")) {
            ecdaaTrustAnchors = new ArrayList<EcdaaTrustAnchor>();
            jsonArray = jsonInput.getJsonArray("ecdaaTrustAnchors");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    ecdaaTrustAnchors.add(new EcdaaTrustAnchor(jsonArray.getJsonObject(i)));
                }
            }
        }
        if (jsonInput.containsKey("icon")) icon = jsonInput.getString("icon");
        if (jsonInput.containsKey("supportedExtensions")) {
            supportedExtensions = new ArrayList<ExtensionDescriptor>();
            jsonArray = jsonInput.getJsonArray("supportedExtensions");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    supportedExtensions.add(new ExtensionDescriptor(jsonArray.getJsonObject(i)));
                }
            }
        }
    }

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
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.legalHeader != null) job.add("legalHeader", legalHeader);
        if (this.aaid != null) job.add("aaid", aaid.toJsonObject());
        if (this.aaguid != null) job.add("aaguid", this.aaguid.toJsonObject());
        if (this.attestationCertificateKeyIdentifiers != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            attestationCertificateKeyIdentifiers.stream().forEach(identifier -> jab.add(identifier));
            job.add("attestationCertificateKeyIdentifiers", jab);
        }
        if (this.description != null) job.add("description", description);
        if (this.alternativeDescriptions != null) {
            JsonObjectBuilder mapJob = Json.createObjectBuilder();
            alternativeDescriptions.getAlternativeDescriptions().entrySet().stream().forEach(entry -> mapJob.add(entry.getKey(), entry.getValue()));
            job.add("alternativeDescriptions", mapJob);
        }
        if (this.authenticatorVersion != null) job.add("authenticatorVersion", authenticatorVersion);
        if (this.protocolFamily != null) job.add("protocolFamily", protocolFamily);
        if (this.upv != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            upv.stream().forEach(version -> jab.add(version.toJsonObject()));
            job.add("upv", jab);
        }
        if (this.assertionScheme != null) job.add("assertionScheme", assertionScheme);
        if (this.authenticationAlgorithm != null) job.add("authenticationAlgorithm", authenticationAlgorithm);
        if (this.authenticationAlgorithms != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            authenticationAlgorithms.stream().forEach(algorithm -> jab.add(algorithm));
            job.add("authenticationAlgorithms", jab);
        }
        if (this.publicKeyAlgAndEncoding != null) job.add("publicKeyAlgAndEncoding", publicKeyAlgAndEncoding);
        if (this.publicKeyAlgAndEncodings != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            publicKeyAlgAndEncodings.stream().forEach(algAndEncodings -> jab.add(algAndEncodings));
            job.add("publicKeyAlgAndEncodings", jab);
        }
        if (this.attestationTypes != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            attestationTypes.stream().forEach(attestationType -> jab.add(attestationType));
            job.add("attestationTypes", jab);
        }
        if (this.userVerificationDetails != null) {
            JsonArrayBuilder outerjab = Json.createArrayBuilder();
            userVerificationDetails.stream().forEach(
                verificationDetailList -> {
                    JsonArrayBuilder innerjab = Json.createArrayBuilder();
                    verificationDetailList.stream().forEach(verificationDetail -> innerjab.add(verificationDetail.toJsonObject()));
                    outerjab.add(innerjab);
            });
            job.add("userVerificationDetails", outerjab);
        }
        if (this.keyProtection != null) job.add("keyProtection", keyProtection);
        if (this.isKeyRestricted != null) job.add("isKeyRestricted", isKeyRestricted);
        if (this.isFreshUserVerificationRequired != null) job.add("isFreshUserVerificationRequired", isFreshUserVerificationRequired);
        if (this.matcherProtection != null) job.add("matcherProtection", matcherProtection);
        if (this.cryptoStrength != null) job.add("cryptoStrength", cryptoStrength);
        if (this.operatingEnv != null) job.add("operatingEnv", operatingEnv);
        if (this.attachmentHint != null) job.add("attachmentHint", attachmentHint);
        if (this.isSecondFactorOnly != null) job.add("isSecondFactorOnly", isSecondFactorOnly);
        if (this.tcDisplay != null) job.add("tcDisplay", tcDisplay);
        if (this.tcDisplayContentType != null) job.add("tcDisplayContentType", tcDisplayContentType);
        if (this.tcDisplayPNGCharacteristics != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            tcDisplayPNGCharacteristics.stream().forEach(characteristic -> jab.add(characteristic.toJsonObject()));
            job.add("tcDisplayPNGCharacteristics", jab);
        }
        if (this.attestationRootCertificates != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            attestationRootCertificates.stream().forEach(cert -> jab.add(cert));
            job.add("attestationRootCertificates", jab);
        }
        if (this.ecdaaTrustAnchors != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            ecdaaTrustAnchors.stream().forEach(trustAnchor -> jab.add(trustAnchor.toJsonObject()));
            job.add("ecdaaTrustAnchors", jab);
        }
        if (this.icon != null) job.add("icon", icon);
        if (this.supportedExtensions != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            supportedExtensions.stream().forEach(extension -> jab.add(extension.toJsonObject()));
            job.add("supportedExtensions", jab);
        }
        return job.build();
    }
}
