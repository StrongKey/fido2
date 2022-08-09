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
 * An interface that proxies requests to the application implementing the server-side
 * implementation of these webservices.
 */

package com.strongkey.sacl.interfaces;

import org.json.JSONObject;


public interface SaclService {

//    /**
//     * When a new user registers with the app, this webservice checks to see if the typed in
//     * username is available or not - no sense in filling out the full form to find out that
//     * the username is not available
//     *
//     * @param did String with Domain ID on MDBA/MDKMS
//     * @param username String containing a username for checking if it is available
//     *
//     * @return boolean indicating true if available, or false if not
//     */
//    public boolean checkForUsername(
//            String did,
//            String username);
//
//
//    /**
//     * Enrolls a new user to the web-app with the minimum required information
//     *
//     * @param did String with Domain ID on MDBA/MDKMS
//     * @param username String containing the username
//     * @param password String containing the user's password
//     * @param givenName String containing the given (first) name of the user
//     * @param familyName String containing the family (last) name of the user
//     * @param email String containing an e-mail address of the user
//     * @param personalNumber String containing the personal mobile number of the user. This is
//     * necessary so One Time Passcodes (OTP) may be sent to the user for the FIDO registration
//     * confirmation process (after the device is accepted within the system)
//     * @param otherNumber String containing the mobile number of the phone that will be used
//     * for executing the MDBA app - usually the one on which this app is running
//     *
//     * @return JSONObject containing the response with the new user's saved information,
//     * along with an assigned userid (UID), status and creation date/time. A sample JSON is
//     * shown below:
//     *
//     * {
//     *   "User": {
//     *     "did": 1,
//     *     "sid": 1,
//     *     "uid": 3,
//     *     "username": "johndoe",
//     *     "givenName": "John",
//     *     "familyName": "Doe",
//     *     "email": "jdoe@internet.com",
//     *     "personalMobileNumber": "4085551213",
//     *     "otherMobileNumber": "2123456789",
//     *     "status": "Other",
//     *     "createDate": "2020-09-01 18:25:58.0"
//     *   }
//     * }
//     */
//    public JSONObject registerUser(
//            String did,
//            String username,
//            String password,
//            String givenName,
//            String familyName,
//            String email,
//            String personalNumber,
//            String otherNumber);


    /**
     * Propose the mobile device on which the user has enrolled, along with select elements of
     * the device's Build information so an MDBA Administrator may determine if the device may
     * be authorized for sending an attestation about the device's keystore.
     *
     * If accepted, and upon receiving an authorization token, the device will generate a
     * sample key-pair within the AndroidKeystore and send the key's certificate chain as an
     * attestation of its security. The attestation (created by Android) will provide details
     * on whether it has a Trusted Execution Environment (TEE) or a dedicated Secure Element (SE),
     * as well as other detail about the state of the device.
     *
     * The MDBA Administrator is expected to review this against the MDBA's security policy and
     * choose to accept or deny the proposed device within the MDBA. Once accepted, the device
     * may participate in the MDBA's business functionality (which will be governed by
     * key-management policies specified by the StrongKey MDKMS).
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID
     * @param mobileNumber String with device's official (other) mobile number
     * @param manufacturer String with the name of the device's manufacturer
     * @param model String with model name of the device
     * @param osRelease String the release number of the Android OS installed on the device
     * @param osSdkNumber String with the Android SDK number supported on the device
     * @param fingerprint String containing a unique (to the extent Android claims) fingerprint
     * of the mobile device
     *
     * @return JSONObject with a response indicating if the proposed device was accepted for
     * verification of its keystore security. A sample JSON response is shown below:
     *
     * {
     *   "UserDevice": {
     *     "did": 1,
     *     "sid": 1,
     *     "uid": 3,
     *     "devid": 3,
     *     "manufacturer": "Google",
     *     "model": "Pixel3a",
     *     "osRelease": "9.0.0",
     *     "osSdkNumber": 28,
     *     "fingerprint": "google/sargo/sargo:9/PQ3B.190705.003/5622519:user/release-keys",
     *     "status": "Other",
     *     "createDate": "2020-06-21 12:26:35.0"
     *   }
     * }
     */
    public JSONObject proposeMobileDevice(
            String did,
            String uid,
            String mobileNumber,
            String manufacturer,
            String model,
            String osRelease,
            String osSdkNumber,
            String fingerprint);

    /**
     * When a new user proposes a mobile device to be used with the MDBA, it must be
     * approved by the MDBA application, and authorized to send an AndroidKeystore
     * attestation. There may be a lag in getting that authorization; this webservice
     * checks the MDBA server to determine if that authorization has been granted.
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID on MDBA
     * @param devid String containing the unique Device ID assigned when the proposed
     * device was accepted by the MDBA
     *
     * @return JSONObject indicating if the proposed device is pending review, rejected
     * or authoriztion has been granted. If granted, the authorization details are sent
     * back in the JSON response.
     */
    public JSONObject checkForAttestationAuthorization(
            String did,
            String uid,
            String devid);


    /**
     * Validate an AndroidKeystore attestation to determine if it meets the security policy of
     * the Mobile Device Key Management System (MDKMS)
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param sid String with Server ID of the MDBA/MDKMS cluster
     * @param uid String with User ID
     * @param devid String with Device ID
     * @param azid String with device's Authorization ID to attempt attestation
     * @param arid String with Authorized to Register ID on MDKMS
     * @param username String with user's username
     * @param password String with user's password
     * @param challenge String containing the hex-encoded challenge supplied by the MDKMS (through
     * the business web-application this app communicates with. The MDKMS requires this challenge
     * to be used in the key-generation process so it can determine that the attestation is a valid
     * one for this device.
     *
     * @return JSONObject with a response indicating if the attestation sent by this method's
     * implementation was successful or not
     */
    public JSONObject validateAndroidKeystore(
            String did,
            String sid,
            String uid,
            String devid,
            String azid,
            String arid,
            String username,
            String password,
            String challenge);


    /**
     * Generates a signing key using the AndroidKeystore (AKS) provider. Keys are generated by
     * this library based on policies specified by the centralized MDKMS. Policies may specify
     * the following requirements for signing keys:
     *
     * Algorithm - EC by default; also RSA
     * Alias - Unique identifier for the key in AKS
     * Attestation Challenge (if required) for AndroidKeystore Attestation (null by default)
     * Certificate Not After (if required to generate X509 certificate) - Validity end date
     * Certificate Not Before (if required to generate X509 certificate) - Validity start date
     * Certificate Serial Number (if required to generate X509 certificate) - Random, by default
     * Certificate Subject (if required to generate X509 certificate)
     * Invalidate by Biometric Enrollment - True by default
     * Key Size - 256 by default for EC; 2048 for RSA
     * Message Digest - SHA256 by default; SHA384 and SHA512 optionally
     * Signature Padding scheme (for RSA keys only) - PKCS1 by default
     * StrongBox Backing - True by default if available
     * UserAuthenticationRequired - True by default
     * User Authentication Validity Duration in Seconds - Always (-1) by default
     * User Authentication Parameters - Supersedes UserAuthenticationValidityDuration in API 30
     * User Confirmation Required - False by default (this is Android Protected Confirmation)
     * Validity Start Date - Immediately upon generation by default
     * Validity End Date for Origination- Always valid by default (when not specified)
     * Validity End Date for Consumption- Always valid by default (when not specified)
     *
     * Once generated, with the exception of FIDO protocols, the key may be used for all purposes;
     * FIDO keys are generated through a different part of this library keeping FIDO-specific
     * requirements into account.
     *
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID of the user performing the signing operation
     * @param plid String with the Policy ID from the MDKMS
     * @param alias String with the alias of the signing key within AndroidKeystore
     *
     * @return JSONObject containing the newly generated key
     */
    public JSONObject generateSigningKey(
            String did,
            String uid,
            String plid,
            String alias);


    /**
     * Generates a JSON Web Signature (RFC-7515) based on a JSONObject plaintext object provided
     * as input to the process. The method also needs to know the alias of the Signing key in
     * AndroidKeystore. It is assumed that the app is enabled with fingerprint biometrics and
     * the user has enrolled at least one fingerprint that protects access to the signing key. At
     * this time the library only supports the use of JSON Serialization with AndroidKeystore keys
     * that have X509 digital certificates associated with them.
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID of the user performing the signing operation
     * @param alias String with the alias of the signing key within AndroidKeystore
     * @param payload String containing the business application payload to be signed. It is
     * important to understand that the payload will undergo JSON Canonicalization (RFC-8785) if
     * the string has an embedded JSON. This implies that the signature will be performed on a
     * JSON object that will most likely be transformed for RFC-8785 conformance before it
     * undergoes cryptographic processing.
     *
     * @return JSONObject containing the RFC-8785 JSON Web Signature
     */
    public JSONObject generateJsonWebSignature(
            String did,
            String uid,
            String alias,
            String payload);


    /**
     * Generates a JSON Web Signature (RFC-7515) based on a JSONObject plaintext object provided
     * as input to the process. The method also needs to know the alias of the Signing key in
     * AndroidKeystore. It is assumed that the app is enabled with fingerprint biometrics and
     * the user has enrolled at least one fingerprint that protects access to the signing key. At
     * this time the library only supports the use of JSON Serialization with AndroidKeystore keys
     * that have X509 digital certificates associated with them.
     *
     * @param did String with Domain ID on MDBA/MDKMS
     * @param uid String with User ID of the user performing the signing operation
     * @param signedObject JSONObject containing the signed JSON with its associated X509 digital
     * certificate and signature object. It is important to recognize that the plaintext in the
     * JWS will undergo JSON Canonicalization (RFC-8785) before the signature is verified. This
     * implies that verification is performed on a JSON object that will most likely be
     * transformed for RFC-8785 conformance before it undergoes cryptographic processing.
     *
     * @return JSONObject containing results of verification along with relevant details of the
     * digital certificate that signed the plaintext
     */
    public JSONObject verifyJsonWebSignature(
            String did,
            String uid,
            JSONObject signedObject);
}
