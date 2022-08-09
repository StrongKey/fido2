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
 * Utility class
 *
 * {
 *   "rp": {
 *     "name": "StrongKey FIDO2",
 *     "id": "strongkey.com"
 *   },
 *   "user": {
 *     "displayName": "Test",
 *     "id": "...",
 *     "name": "test"
 *   },
 *   "challenge": "...",
 *   "pubKeyCredParams": [
 *     {
 *       "type": "public-key",
 *       "alg": -7
 *     }, {
 *       "type": "public-key",
 *       "alg": -257
 *     }
 *   ],
 *   "attestation": "direct",
 *   "excludeCredentials": [
 *     {
 *       "id": "...",
 *       "type": "public-key",
 *       "transports": [
 *         "internal"
 *       ]
 *     }
 *   ],
 *   "authenticatorSelection": {
 *     "authenticatorAttachment": "platform",
 *     "requireResidentKey": true,
 *     "userVerification": "required"
 *   }
 * }
 *
 */

package com.strongkey.sacl.roomdb;

/**
 * Inner class for AuthenticatorSelectionCriteria - used by authenticatorSelection option
 * to indicate whether the key should be "platform" or "cross-platform" and whether the
 * default "userPresence" is required, or its inverse "userVerification" is "required",
 * "preferred" or "discouraged". A "platform" key implies "requireResidentKey" is "true"
 * even if it is not present.
 *
 * Nullable - looks like the following when present
 *
 * "authenticatorSelection": {
 *     "authenticatorAttachment": "platform",
 *     "requireResidentKey" : true,
 *     "userVerification": "required"
 *  }
 */
public class AuthenticatorSelectionCriteria {

    private String authenticatorAttachment;
    private boolean requireResidentKey;
    private String userVerification;

    // Constructor for AuthenticatorSelectionCriteria - fixed values for SACL
    public AuthenticatorSelectionCriteria() {
        this.authenticatorAttachment = "platform";
        this.requireResidentKey = true;
        this.userVerification = "required";
    }

    public AuthenticatorSelectionCriteria(String attachment, boolean resident, String uv) {
        this.authenticatorAttachment = attachment;
        this.requireResidentKey = resident;
        this.userVerification = uv;
    }

    public String getAuthenticatorAttachment() {
        return authenticatorAttachment;
    }

    public void setAuthenticatorAttachment(String authenticatorAttachment) {
        this.authenticatorAttachment = authenticatorAttachment;
    }

    public boolean isRequireResidentKey() {
        return requireResidentKey;
    }

    public void setRequireResidentKey(boolean requireResidentKey) {
        this.requireResidentKey = requireResidentKey;
    }

    public String getUserVerification() {
        return userVerification;
    }

    public void setUserVerification(String userVerification) {
        this.userVerification = userVerification;
    }

    @Override
    public String toString() {
        return "AuthenticatorSelectionCriteria{" +
                "authenticatorAttachment='" + authenticatorAttachment + '\'' +
                ", requireResidentKey=" + requireResidentKey +
                ", userVerification='" + userVerification + '\'' +
                '}';
    }
}
