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
 * APIEndpointManager is used to create type safe endpoints to the FidoServer Servlet.
 * This can be edited to support more endpoints.
 *
 */

import Foundation

fileprivate let fidoServiceBaseURL:String = "https://fidotest.strongkey.com/fidopolicyboa/fido2"

protocol Endpoint {
    var httpMethod: String { get }
    var baseURLString: String { get }
    var path: String { get }
    var headers: [String: Any]? { get }
    var body: [String: Any]? { get }
}

extension Endpoint {
    // a default extension that creates the full URL
    var url: String {
        return baseURLString + path
    }
}

enum APICases: Endpoint {
    
    case preRegister(username: String, displayName: String)
    case register(username: String, attestationObject: String, clientDataJSON: String, id: String)
    case preauthenticate(username: String)
    case authenticate(username: String, authenticatorData: String, signature: String, userHandle: String, clientDataJSON: String, id: String)
    case getKeysInfo(username: String, jwt: String)
    case preRegisterExisting(username: String, displayName: String, jwt: String)
    case registerExisting(username: String, attestationObject: String, clientDataJSON: String, id: String, jwt: String)
    case removeKeys(username: String, keyIDs: [String], jwt: String)
    case deleteAccount(username: String, jwt: String)
    
    var httpMethod: String {
        switch self {
        case .preRegister:
            return "POST"
        case .register:
            return "POST"
        case .preauthenticate:
            return "POST"
        case .authenticate:
            return "POST"
        case .getKeysInfo:
            return "POST"
        case .preRegisterExisting:
            return "POST"
        case .registerExisting:
            return "POST"
        case .removeKeys:
            return "POST"
        case .deleteAccount:
            return "POST"
        }
    }
    
    var baseURLString: String {
        switch self {
        case .preRegister:
            return fidoServiceBaseURL
        case .register:
            return fidoServiceBaseURL
        case .preauthenticate:
            return fidoServiceBaseURL
        case .authenticate:
            return fidoServiceBaseURL
        case .getKeysInfo:
            return fidoServiceBaseURL
        case .preRegisterExisting:
            return fidoServiceBaseURL
        case .registerExisting:
            return fidoServiceBaseURL
        case .removeKeys:
            return fidoServiceBaseURL
        case .deleteAccount:
            return fidoServiceBaseURL
        }
    }
    
    var path: String {
        switch self {
        case .preRegister:
            return "/preregister"
        case .register:
            return "/register"
        case .preauthenticate:
            return "/preauthenticate"
        case .authenticate:
            return "/authenticate"
        case .getKeysInfo:
            return "/getuserinfo"
        case .preRegisterExisting:
            return "/preregisterExisting"
        case .registerExisting:
            return "/registerExisting"
        case .removeKeys:
            return "/removeKeys"
        case .deleteAccount:
            return "/deleteAccount"
        }
    }
    
    var headers: [String : Any]? {
        switch self {
        case .preRegister:
            return ["Content-Type": "Application/json"]
        case .register:
            return ["Content-Type": "Application/json"]
        case .preauthenticate:
            return ["Content-Type": "Application/json"]
        case .authenticate:
            return ["Content-Type": "Application/json"]
        case .getKeysInfo:
            return ["Content-Type": "Application/json"]
        case .preRegisterExisting:
            return ["Content-Type": "Application/json"]
        case .registerExisting:
            return ["Content-Type": "Application/json"]
        case .removeKeys:
            return ["Content-Type": "Application/json"]
        case .deleteAccount:
            return ["Content-Type": "Application/json"]
        }
    }
    
    var body: [String : Any]? {
        switch self {
        case .preRegister(let username, let displayName):
            return ["username": username,
                    "displayName" : displayName,
                    "policy" : "restrictedApple"
            ]
        case .register(let username, let attestationObject, let clientDataJSON, let id):
            return ["username": username,
                    "response" : [ "attestationObject" : attestationObject,
                                   "clientDataJSON" : clientDataJSON],
                    "id" : id,
                    "rawId" : id,
                    "type" : "public-key",
                    "policy" : "restrictedApple"
            ]
        case .preauthenticate(let username):
            return ["username": username,
                    "policy" : "restrictedApple"
            ]
        case .authenticate(let username, let authenticatorData, let signature, let userHandle, let clientDataJSON, let id):
            return [ "username" : username,
                     "response" : [ "authenticatorData" : authenticatorData,
                                    "signature" : signature,
                                    "userHandle" : userHandle,
                                    "clientDataJSON" : clientDataJSON,
                                    "policy" : "restrictedApple"
                                  ],
                     "id" : id,
                     "rawId" : id,
                     "type" : "public-key",
                     "policy" : "restrictedApple"
            ]
        case .getKeysInfo(let username, let jwt):
            return ["username": username,
                    "jwt" : jwt,
                    "policy" : "restrictedApple"
            ]
        case .preRegisterExisting(let username, let displayName, let jwt):
            return ["username": username,
                    "displayName" : displayName,
                    "jwt" : jwt,
                    "policy" : "restrictedApple"
            ]
        case .registerExisting(let username, let attestationObject, let clientDataJSON, let id, let jwt):
            return ["username": username,
                    "jwt" : jwt,
                    "response" : [ "attestationObject" : attestationObject,
                                   "clientDataJSON" : clientDataJSON],
                    "id" : id,
                    "rawId" : id,
                    "type" : "public-key",
                    "policy" : "restrictedApple"
            ]
        case .removeKeys(let username, let keyIDs, let jwt):
            return [ "username" : username,
                     "jwt" : jwt,
                     "keyIds" : keyIDs,
                     "policy" : "restrictedApple"
            ]
        case .deleteAccount(let username, let jwt):
            return ["username": username,
                    "jwt" : jwt,
                    "policy" : "restrictedApple"
            ]
        }
    }
}
