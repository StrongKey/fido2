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
 * An interface that represents FIDO webservices available on the
 * StrongKey FIDO Server.
 *
 * It should be noted that a rich-client app (RCA) using this library
 * will NOT be calling the FIDO webservices directly. RCAs, typically,
 * interact with a business application and as such, must communicate
 * with that application for its FIDO registration, authentications and
 * transaction confirmations.
 *
 * StrongKey's sample open-source implementation of a Mobile Device
 * Business Application (MDBA) is used as an example to demonstrate the
 * use of this library. While the MDBA serves no real purpose (other than
 * to demonstrate how to use this library for a variety of use-cases),
 * the MDBA can be leveraged to build other RCAs that perform useful
 * business functions while using this library for cryptographic services.
 *
 * The design goal of this library to simplify the complexity of various
 * cryptographic protocols through higher level application programming
 * interfaces (API) that serve business app developers; obviously, those
 * who want far more control over their app's cryptographic capability can
 * find their way to various standards sites/protocols for lower-level APIs.
 * Be warned: you should be prepared to get your hands really dirty.
 *
 * COMMERCIAL DECLARATION: While StrongKey has been a strong proponent of
 * the open-source movement since inception (2001), it must rely on some
 * revenue to be able to do the hard research and work that produces open-
 * source software such as our FIDO server and this library.
 *
 * As such, the MDBA is built on an assumption that security-conscious
 * relying parties (RP) such as banks, government agencies and companies in
 * the e-commerce, healthcare and other sectors dealing with sensitive data,
 * will choose to manage cryptographic capability that protects data within
 * their RCA on mobile devices, through policies dictated by a centralized
 * key management system (KMS). StrongKey does make such a KMS product
 * available to sustain its existence, and the MDBA is assumed to be
 * operating under the control of such a KMS. Consequently, there are
 * references to the KMS within the MDBA APIs.
 *
 * Developers who would like to use this library without such a KMS can
 * modify the source code in this library - operating under the control of
 * the open source license referenced above - and use/redistribute it under
 * the terms of that license.
 *
 * Thank you for your interest in downloading and using this library.
 */

package com.strongkey.sacl.interfaces;

import com.strongkey.sacl.roomdb.PreauthorizeChallenge;

import org.json.JSONObject;

import java.security.Signature;

public interface FidoService {
//    /**
//     * Gets a challenge to initiate a FIDO Registration process.
//     *
//     * @param did integer value representing the cryptographic domain ID to
//     * which this webservice is directed (see https://strongkey.com/resources
//     * for an explanation of StrongKey cryptographic domains)
//     * @param uid long value representing the User ID within the MDBA that is
//     * making this service request. Since the MDBA manages the registration
//     * process and relays the service request to the FIDO server, it knows what
//     * to send to the FIDO server
//     * @param devid long value representing the device ID of this mobile device.
//     * This is known to the MDBA and is part of the on-boarding process of the
//     * mobile device when it is enrolled in the MDKMS
//     * @param rdid long value representing the registered device ID of this
//     * mobile device within the MDKMS. This will determine the KMS policy that
//     * will guide this library's actions in concert with the RCA
//     * @return PreregisterChallenge A Java object that represents the challenge,
//     * along with metadata about the challenge and the transaction. Embedded
//     * within this Java object, are details of the challenge provided by
//     * StrongKey's preregister() webservice on the FIDO server
//     */
//    PreregisterChallenge getFidoRegistrationChallenge(int did, long uid, long devid, long rdid);

    /**
     * Registers a FIDO public key with the StrongKey FIDO server as part of
     * the FIDO Registration process.
     *
     * @param did integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the MDBA that is
     * making this service request. Since the MDBA manages the registration
     * process and relays the service request to the FIDO server, it knows what
     * to send to the FIDO server
     * @param devid long value representing the device ID of this mobile device.
     * This is known to the MDBA and is part of the on-boarding process of the
     * mobile device when it is enrolled in the MDKMS
     * @param rdid long value representing the registered device ID of this
     * mobile device within the MDKMS. This will determine the KMS policy that
     * will guide this library's actions in concert with the RCA
     * https://www.w3.org/TR/2019/REC-webauthn-1-20190304/#android-key-attestation
     * @return Object A Java object that is either a PublicKeyCredential, which
     * represents the newly minted key-pair within AndroidKeystore, along with
     * metadata about the transaction, or a JSONError indicating a problem.
     * The private key of the key-pair is always stored within AndroidKeystore,
     * protected either by the Trusted Execution Environment (TEE) or a
     * hardware security module aka Secure Element (SE)
     */
    public Object registerAttestedDeviceFidoKey(int did, long uid, long devid, long rdid);

    /**
     * Registers a FIDO public key with the StrongKey FIDO server as part of
     * the FIDO Registration process.
     *
     * @param did integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the SFA eCommerce app
     * making this service request. Since the SFA manages the registration
     * process and relays the service request to the FIDO server, it knows what
     * to send to the FIDO server
     * @return Object A Java object that is either a PublicKeyCredential, which
     * represents the newly minted key-pair within AndroidKeystore, along with
     * metadata about the transaction, or a JSONError indicating a problem.
     * The private key of the key-pair is always stored within AndroidKeystore,
     * protected either by the Trusted Execution Environment (TEE) or a
     * hardware security module aka Secure Element (SE)
     */
    public Object registerFidoKey(int did, long uid);


//    /**
//     * Gets a challenge to initiate a FIDO Authentication process. It
//     * presupposes that the user has a FIDO key registered with the FIDO
//     * server for this to work.
//     *
//     * @param did integer value representing the cryptographic domain ID to
//     * which this webservice is directed (see https://strongkey.com/resources
//     * for an explanation of StrongKey cryptographic domains)
//     * @param uid long value representing the User ID within the MDBA that is
//     * making this service request.  Since the MDBA manages the authentication
//     * process and relays the service request to the FIDO server, it knows what
//     * to send to the FIDO server
//     * @param devid long value representing the device ID of this mobile device.
//     * This is known to the MDBA and is part of the on-boarding process of the
//     * mobile device when it is enrolled in the MDKMS
//     * @param rdid long value representing the registered device ID of this
//     * mobile device within the MDKMS. This will determine the KMS policy that
//     * will guide this library's actions in concert with the RCA
//     * @return PreauthenticateChallenge A Java object that represents the
//     * challenge, along with metadata about the challenge and the transaction.
//     * Embedded within this Java object, are details of the challenge provided
//     * by StrongKey's preauthenticate() webservice on the FIDO server
//     */
//    PreauthenticateChallenge getFidoAuthenticationChallenge(int did, long uid, long devid, long rdid);

    /**
     * Authenticates the user with a digital signature response from the
     * previously registered FIDO key with the StrongKey FIDO server.
     *
     * @param did integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the MDBA making
     * this service request. Since the MDBA manages the authentication process
     * and relays the service request to the FIDO server, it knows what to send
     * to the FIDO server
     * @param devid long value representing the device ID of this mobile device.
     * This is known to the MDBA and is part of the on-boarding process of the
     * mobile device when it is enrolled in the MDKMS
     * @param rdid long value representing the registered device ID of this
     * mobile device within the MDKMS. This will determine the KMS policy that
     * guides this library's actions in concert with the RCA
//     * @param payload JSONObject containing the payload response with the
//     * digital signature from the previously registered FIDO key of the user to
     * challenge posed by the FIDO server. As always, the transaction flows to
     * the FIDO server through the business application - in this case, the MDBA
     * @return Object A Java object that is either a AuthenticationSignature,
     * which represents the FIDO digital signature, along with metadata about
     * the transaction, or a JSONError indicating a problem.
     */
    public Object authenticateAttestedDeviceFidoKey(int did, long uid, long devid, long rdid);

    /**
     * Authenticates the user with a digital signature response from the
     * previously registered FIDO key with the StrongKey FIDO server.
     *
     * @param did integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the SFA making
     * this service request. Since the SFA manages the authentication process
     * and relays the service request to the FIDO server, it knows what to send
     * to the FIDO server
     * @return Object A Java object that is either an AuthenticationSignature,
     * which represents the FIDO digital signature, along with metadata about
     * the transaction, or a JSONError indicating a problem.
     */
    public Object authenticateFidoKey(int did, long uid);

//    /**
//     * Gets a challenge to initiate a FIDO Transaction Confirmation process.
//     * It presupposes that the user has a FIDO key registered with the FIDO
//     * server for this to work. This is the only pre-* methods that requires
//     * a payload parameter since it needs to pass on a few elements of the
//     * transaction the FIDO server uses to derive a challenge; this is what
//     * ties the transaction to the FIDO digital signature
//     *
//     * @param did integer value representing the cryptographic domain ID to
//     * which this webservice is directed (see https://strongkey.com/resources
//     * for an explanation of StrongKey cryptographic domains)
//     * @param uid long value representing the User ID within the MDBA that is
//     * making this service request. Since the MDBA manages the authorization
//     * process and relays the service request to the FIDO server, it knows what
//     * to send to the FIDO server
//     * @param devid long value representing the device ID of this mobile device.
//     * This is known to the MDBA and is part of the on-boarding process of the
//     * mobile device when it is enrolled in the MDKMS
//     * @param rdid long value representing the registered device ID of this
//     * mobile device within the MDKMS. This will determine the KMS policy that
//     * will guide this library's actions in concert with the RCA
//     * @param payload JSONObject value containing elements of the transaction
//     * for which the RP desires a transaction confirmation incorporating the
//     * the user's acceptance to the transaction with a digital signature from
//     * from the previously registered FIDO key
//     * @return PreauthorizeChallenge A Java object that represents the
//     * challenge, along with metadata about the challenge and the transaction.
//     * Embedded within this Java object, are details of the challenge provided
//     * by StrongKey's preauthorize() webservice on the FIDO server
//     */
//    PreauthorizeChallenge getFidoAuthorizationChallenge(int did, long uid, long devid, long rdid, JSONObject payload);

    /**
     * Gets a challenge to initiate a FIDO Transaction Confirmation process.
     * It presupposes that the user has a FIDO key registered with the FIDO
     * server for this to work. This is the only pre-* methods that requires
     * a payload parameter since it needs to pass on a few elements of the
     * transaction the FIDO server uses to derive a challenge; this is what
     * ties the transaction to the FIDO digital signature
     *
     * @param did integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the MDBA that is
     * making this service request. Since the MDBA manages the authorization
     * process and relays the service request to the FIDO server, it knows what
     * to send to the FIDO server
     * @param cart String with a Base64Url encoded JSONObject containing elements
     * of the transaction for which the RP desires a transaction confirmation. The
     * sample cart has a collection of products and the payment method selected by
     * the user
     * @return PreauthorizeChallenge object that represents the FIDO challenge
     * provided by StrongKey's preauthorize() webservice on the FIDO server or
     * a JSONObject with an error
     */
    Object getFidoAuthorizationChallenge(int did, long uid, String cart);

    /**
     * Authorizes a business transaction using the FIDO key. The RP may use
     * the transaction data and digital signature for a variety of use-cases:
     * compliance with the EU PSD2 for SCA, conform to the EMVCo 3DS2 protocol
     * or other private transaction protocol that leverages FIDO
     *
     * @param did integer value representing the cryptographic domain ID to
     * which this webservice is directed (see https://strongkey.com/resources
     * for an explanation of StrongKey cryptographic domains)
     * @param uid long value representing the User ID within the MDBA making
     * this service request. Since the MDBA manages the authentication process
     * and relays the service request to the FIDO server, it knows what to send
     * to the FIDO server
     * @param txid String containing a locally generated transaction ID. This
     * should be replaced with a TXID generated by the business application
     * @param txplayload String containing a JSONObject with products and payment
     * information
     * @param credentialId String containing unique key identifier in keystore
     * @param challenge String with the FIDO challenge to sign
     * @param signature Signature object initialized with the user's FIDO key
     * to generate the digital signature over the transaction
     *
     * @return Object A Java object, AuthorizationSignature, that represents the
     * digital signature, along with metadata about the challenge and the
     * transaction, or a JSONError indicating a problem.
     */
    public Object authorizeFidoTransaction(int did, long uid, String txid, String txplayload,
                                    String credentialId, String challenge, Signature signature);

}
