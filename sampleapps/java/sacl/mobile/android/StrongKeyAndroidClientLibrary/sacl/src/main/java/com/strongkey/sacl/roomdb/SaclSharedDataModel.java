/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, as published by the Free
 * Software Foundation and available at
 * http://www.fsf.org/licensing/licenses/lgpl.html, version 2.1.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (d/b/a StrongKey)
 *
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * A class to share information between fragments while setting up
 * observers to watch for changes.
 */

package com.strongkey.sacl.roomdb;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

public class SaclSharedDataModel {

    private PreregisterChallenge CURRENT_PREREGISTER_CHALLENGE;
    private PreauthenticateChallenge CURRENT_PREAUTHENTICATE_CHALLENGE;
    private PreauthorizeChallenge CURRENT_PREAUTHORIZE_CHALLENGE;
    private AuthenticationSignature CURRENT_AUTHENTICATION_SIGNATURE;
    private AuthorizationSignature CURRENT_AUTHORIZATION_SIGNATURE;
    private PublicKeyCredential CURRENT_PUBLIC_KEY_CREDENTIAL;
    private List<PublicKeyCredential> CURRENT_PUBLIC_KEY_CREDENTIAL_LIST;
    private ProtectedConfirmationCredential CURRENT_PROTECTED_CONFIRMATION_CREDENTIAL;
    private MutableLiveData<SaclRepository> SACL_REPOSITORY;


    public SaclSharedDataModel() {

    }

    public PreregisterChallenge getCurrentPreregisterChallenge() {
        return CURRENT_PREREGISTER_CHALLENGE;
    }

    public void setCurrentPreregisterChallenge(PreregisterChallenge CURRENT_PREREGISTER_CHALLENGE) {
        this.CURRENT_PREREGISTER_CHALLENGE = CURRENT_PREREGISTER_CHALLENGE;
    }

    public PreauthenticateChallenge getCurrentPreauthenticateChallenge() {
        return CURRENT_PREAUTHENTICATE_CHALLENGE;
    }

    public void setCurrentPreauthenticateChallenge(PreauthenticateChallenge CURRENT_PREAUTHENTICATE_CHALLENGE) {
        this.CURRENT_PREAUTHENTICATE_CHALLENGE = CURRENT_PREAUTHENTICATE_CHALLENGE;
    }

    public PreauthorizeChallenge getCurrentPreauthorizeChallenge() {
        return CURRENT_PREAUTHORIZE_CHALLENGE;
    }

    public void setCurrentPreauthorizeChallenge(PreauthorizeChallenge CURRENT_PREAUTHORIZE_CHALLENGE) {
        this.CURRENT_PREAUTHORIZE_CHALLENGE = CURRENT_PREAUTHORIZE_CHALLENGE;
    }

    public AuthenticationSignature getCurrentAuthenticationSignature() {
        return CURRENT_AUTHENTICATION_SIGNATURE;
    }

    public void setCurrentAuthenticationSignature(AuthenticationSignature CURRENT_AUTHENTICATION_SIGNATURE) {
        this.CURRENT_AUTHENTICATION_SIGNATURE = CURRENT_AUTHENTICATION_SIGNATURE;
    }

    public AuthorizationSignature getCurrentAuthorizationSignature() {
        return CURRENT_AUTHORIZATION_SIGNATURE;
    }

    public void setCurrentAuthorizationSignature(AuthorizationSignature CURRENT_AUTHORIZATION_SIGNATURE) {
        this.CURRENT_AUTHORIZATION_SIGNATURE = CURRENT_AUTHORIZATION_SIGNATURE;
    }

    public PublicKeyCredential getCurrentPublicKeyCredential() {
        return CURRENT_PUBLIC_KEY_CREDENTIAL;
    }

    public void setCurrentPublicKeyCredential(PublicKeyCredential CURRENT_PUBLIC_KEY_CREDENTIAL) {
        this.CURRENT_PUBLIC_KEY_CREDENTIAL = CURRENT_PUBLIC_KEY_CREDENTIAL;
    }

    public List<PublicKeyCredential> getCurrentPublicKeyCredentialList() {
        return CURRENT_PUBLIC_KEY_CREDENTIAL_LIST;
    }

    public void setCurrentPublicKeyCredentialList(List<PublicKeyCredential> CURRENT_PUBLIC_KEY_CREDENTIAL_LIST) {
        this.CURRENT_PUBLIC_KEY_CREDENTIAL_LIST = CURRENT_PUBLIC_KEY_CREDENTIAL_LIST;
    }

    public ProtectedConfirmationCredential getCurrentProtectedConfirmationCredential() {
        return CURRENT_PROTECTED_CONFIRMATION_CREDENTIAL;
    }

    public void setCurrentProtectedConfirmationCredential(ProtectedConfirmationCredential CURRENT_PROTECTED_CONFIRMATION_CREDENTIAL) {
        this.CURRENT_PROTECTED_CONFIRMATION_CREDENTIAL = CURRENT_PROTECTED_CONFIRMATION_CREDENTIAL;
    }

    public SaclRepository getSaclRepository() {
        return SACL_REPOSITORY.getValue();
    }

    public void setSaclRepository(SaclRepository saclRepository) {
        SACL_REPOSITORY = new MutableLiveData<>(saclRepository);
    }
}