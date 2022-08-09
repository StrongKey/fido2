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
 * An interface that represents a FIDO Authenticator that uses
 * the AndroidKeystore to generate and store FIDO key-pairs
 */

package com.strongkey.sacl.interfaces;

import android.content.ContextWrapper;

import org.json.JSONObject;


public interface FidoAuthenticator {

    /**
     * Creates a new FIDO2 key-pair using the AndroidKeystore, for a specific relying party site.
     * Based on the WebAuthn Authenticator Model (https://www.w3.org/TR/webauthn/#op-make-cred),
     * this library forces specific constraints:
     *  i) the key-pair is restricted to ECDSA 256-bit;
     *  ii) the key-pair is resident in the AndroidKeystore and cannot be extracted;
     *  iii) use of the private-key always requires user-verification;
     *  iv) it does not support any authenticator extensions in this release.
     *
     * @param rpid String containing the relying party's (RP) unique identification on the internet
     * (ex: a domain name: strongkey.com)
     * @param userid String containing a unique value for the user's identification within the
     * relying party's web-application(s) (ex: johndoe)
     * @param challenge String containing the unique hex-encoded challenge (nonce - number used
     * once) which must be part of the data digitally signed by the keystore to provide an
     * AndroidKeystore attestation to the FIDO2 server
     * @param clientDataHash byte[] A byte array containing the SHA256 digest of the client data
     * structure (https://www.w3.org/TR/webauthn/#sec-client-data) containing:
     *  i) the type of operation - makeCredential or getAssertion
     *  ii) the unique challenge (nonce) for the operation from the FIDO2 server
     *  iii) the origin of the website the app is communicating with
     *  iv) Token Binding (not supported in this release, but may be included)
     * @param excludeCredentials A String array containing the list of credentials (credential ids)
     * known within the relying party's web-applications for this specific user, which implies that
     * if one of those credential ids is within this AndroidKeystore, a new credential should NOT
     * be created for this user - FIDO protocols prohibit the creation of duplicate credentials
     * for the same user on the same keystore
     *
     * @return JSONObject containing the response that will be returned to the app -> to be returned
     * to the web-application -> to be returned to the StrongKey FIDO2 server. Note that it includes
     * a unique credentialId created by this library which will be stored by the RP to be sent in
     * the future for the getAssertion operation. This is how the library identifies the unique
     * credential of this user for that RP.
     */
    JSONObject makeCredential(
            String rpid,
            String userid,
            String challenge,
            byte[] clientDataHash,
            String[] excludeCredentials);


    /**
     * Returns a digital signature (an assertion) from a FIDO2 private-key for a specific user for
     * a specified RP site.
     *
     * @param rpid String containing the relying party's (RP) unique identification on the internet
     * (ex: a domain name: strongkey.com)
     * @param credentialId String created by this library (and stored by the RP) to identify the
     * unique key-pair of the user for this RP within this keystore
     * @param challenge String containing the unique hex-encoded challenge (nonce - number used
     * once) which must be part of the data digitally signed by the keystore to provide an
     * AndroidKeystore attestation to the FIDO2 server
     * @param clientDataHash byte[] A byte array containing the SHA256 digest of the client data
     * structure (https://www.w3.org/TR/webauthn/#sec-client-data) containing:
     *  i) the type of operation - makeCredential or getAssertion
     *  ii) the unique challenge (nonce) for the operation from the FIDO2 server
     *  iii) the origin of the website the app is communicating with
     *  iv) Token Binding (not supported in this release, but may be included)
     * @param allowedCredentials A String array containing the list of credentials (credential ids)
     * known within the relying party's web-applications for this specific user, which implies that
     * if one of those credential ids is within this AndroidKeystore, then it can be used to create
     * the digital signature response to the FIDO2 server
     *
     * @return JSONObject containing the response that will be returned to the app -> to be returned
     * to the web-application -> to be returned to the StrongKey FIDO2 server
     */
    JSONObject getAssertion(
            String rpid,
            String credentialId,
            String challenge,
            byte[] clientDataHash,
            String[] allowedCredentials);


    /**
     * Lists cryptographic keys stored within the local AndroidKeystore
     * @param rpid String with the relying party's ID to scope the keys pertaining to that
     * specific relying party
     * @return JSONObject containing cryptographic keys generated for use with the specified RPID
     */
    JSONObject listCredentials(
            String rpid);


    /**
     * Deletes a specific credential in the AndroidKeystore that belongs to the user of the app
     * using this library, and "scoped" (registered) with a specific RP.
     *
     * @param rpid String containing the relying party's (RP) unique identification on the internet
     * (ex: a domain name: strongkey.com)
     * @param credentialId String created by this library (and stored by the RP) to identify the
     * unique key-pair of the user for this RP within this keystore
     *
     * @return boolean response indicating if the deletion was successful - true if it was deleted
     */
    boolean deleteCredential(
            String rpid,
            String credentialId);


    /**
     * In order to use the Android BiometricPrompt, it requires an initialized JCE Signature
     * object to instantiate the CryptoObject used by the biometric API. This method performs
     * that function.
     *
     * @param credentialId String to identify the key alias in AndroidKeystore
     * @param contextWrapper ContextWrapper to resolve resource strings for logging
     * @return Object - either a Signature object or a JSONObject with an error
     */
    Object getSignatureObject (String credentialId, ContextWrapper contextWrapper);
}
