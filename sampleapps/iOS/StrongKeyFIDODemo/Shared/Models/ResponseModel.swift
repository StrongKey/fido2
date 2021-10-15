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
 * 888b    888          888
 * 8888b   888          888
 * 88888b  888          888
 * 888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 * 888 Y88b888 d88""88b 888    d8P  Y8b 88K
 * 888  Y88888 888  888 888    88888888 "Y8888b.
 * 888   Y8888 Y88..88P Y88b.  Y8b.          X88
 * 888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * ResponseModel contains data models for REST calls in FidoService
 *
 */

import Foundation

// MARK: - AuthResponseModel
struct AuthResponseModel: Codable {
    let response, message, error: String
    let jwt: String?

    enum CodingKeys: String, CodingKey {
        case response = "Response"
        case message = "Message"
        case error = "Error"
        case jwt
    }
}

// MARK: - KeyInfoModel
struct KeyInfoModel: Codable {
    let response: KeyInfoModelResponse

    enum CodingKeys: String, CodingKey {
        case response = "Response"
    }
}

// MARK: - KeyInfoModelResponse
struct KeyInfoModelResponse: Codable {
    let keys: [Key]
}

// MARK: - Key
struct Key: Codable, Identifiable {
    var id = UUID()
    let fidoProtocol: String
    let randomid: String?
    let randomidTTLSeconds: String?
    let fidoVersion: String?
    let keyid: String?
    let credentialID, createLocation: String
    let createDate: Int
    let lastusedLocation: String
    let modifyDate: Int
    let status, displayName, attestationFormat: String

    enum CodingKeys: String, CodingKey {
        case randomid, keyid
        case randomidTTLSeconds = "randomid_ttl_seconds"
        case fidoProtocol, fidoVersion
        case credentialID = "credentialId"
        case createLocation, createDate, lastusedLocation, modifyDate, status, displayName, attestationFormat
    }
}

// MARK: - PreRegResponseModel
struct PreRegResponseModel: Codable {
    let response: PreRegResponseModelResponse

    enum CodingKeys: String, CodingKey {
        case response = "Response"
    }
}

// MARK: - PreRegResponseModelResponse
struct PreRegResponseModelResponse: Codable {
    let rp: Rp
    let user: User
    let challenge: String
    let pubKeyCredParams: [PubKeyCredParam]
    let excludeCredentials: [Credential]
    let attestation: String?
}

// MARK: - Credential
struct Credential: Codable {
    let type: TypeEnum
    let id: String
    let alg: Int
}

enum TypeEnum: String, Codable {
    case publicKey = "public-key"
}

// MARK: - PubKeyCredParam
struct PubKeyCredParam: Codable {
    let type: TypeEnum
    let alg: Int
}

// MARK: - Rp
struct Rp: Codable {
    let name, id: String
}

// MARK: - User
struct User: Codable {
    let name, id, displayName: String
}

// MARK: - PreAuthResponseModel
struct PreAuthResponseModel: Codable {
    let response: PreAuthResponseModelResponse

    enum CodingKeys: String, CodingKey {
        case response = "Response"
    }
}

// MARK: - PreAuthResponseModelResponse
struct PreAuthResponseModelResponse: Codable {
    let challenge: String
    let allowCredentials: [Credential]
    let rpID: String

    enum CodingKeys: String, CodingKey {
        case challenge, allowCredentials
        case rpID = "rpId"
    }
}

// MARK: - ErrorResponseModel
struct ErrorResponseModel: Codable {
    let response, message, error: String

    enum CodingKeys: String, CodingKey {
        case response = "Response"
        case message = "Message"
        case error = "Error"
    }
}
