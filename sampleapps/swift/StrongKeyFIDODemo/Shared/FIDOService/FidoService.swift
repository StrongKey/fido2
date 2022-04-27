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
 * FidoService handles all the communication with the SKFS Servlet
 *
 * See SKFS FIDO Server https://github.com/StrongKey/fido2 for details.
 *
 */

import SwiftUI

/// LoginService handles all the communication with the SKFS Servlet
class FidoService {
    
    /// PreRegister is used to fetch the challenge from SKFS for the user
    /// - Parameters:
    ///   - username: Unique User Name set by the account user
    ///   - displayName: Display name can be the Full Name of the user or any other name of their choice
    ///   - completion: Passes the Data, Response and Error to the View where the function is called
    func preRegister(username: String, displayName: String, completion: @escaping (PreRegResponseModel?, URLResponse?, Error?) -> Void) {
        let preRegisterEndpoint = APICases.preRegister(username: username, displayName: displayName)
        request(endpoint: preRegisterEndpoint) { data, response, error in
            
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                      let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                      print(errorResponseModel.message)
                      return completion(nil, response, error)
                  }
            
            let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
            let responseDataString = responseJSON["Response"] as! String
            let responseData = Data(responseDataString.utf8)
            
            let dataFromPreRegModel: PreRegResponseModel = try! JSONDecoder().decode(PreRegResponseModel.self, from: responseData)
            
            // MARK: Change the completion to NOT send the preRegResponseModel data to the view controller
            completion(dataFromPreRegModel, response, error)
        }
    }
    
    /// Register endpoint verifies the attestation from the Platform Authenticator
    /// - Parameters:
    ///   - username: username input from the user
    ///   - attestationObject: attestationObject from the Sign Up Request
    ///   - clientDataJSON: clientDataJSON has the information about the authenticator
    ///   - id: Credential ID from the Platform Authenticator
    ///   - completion: completion handler provides the response from the SKFS
    func register(username: String, attestationObject: String, clientDataJSON: String, id: String, completion: @escaping (Data?, URLResponse?, Error?) -> Void) {
        
        let registerEndpoint = APICases.register(username: username, attestationObject: attestationObject, clientDataJSON: clientDataJSON, id: id)
        
        request(endpoint: registerEndpoint) { data, response, error in
            completion(data, response, error)
        }
        
    }
    
    /// Register Existing is used to verify attestation for adding additional Keys for the user to login on other devices or other security reasons
    /// - Parameters:
    ///   - username: username of the currently logged in user
    ///   - attestationObject: attestationObject from the authenticator
    ///   - clientDataJSON: clientDataJSON from the authenticator
    ///   - id: Credential ID
    ///   - jwt: Stored JWT of the currently logged in user from AppStorage
    ///   - completion: completion handler provides the response from the SKFS
    func registerExisting(username: String, attestationObject: String, clientDataJSON: String, id: String, jwt: String, completion: @escaping (Data?, URLResponse?, Error?) -> Void) {
        
        let registerExistingEndpoint = APICases.registerExisting(username: username, attestationObject: attestationObject, clientDataJSON: clientDataJSON, id: id, jwt: jwt)
        
        request(endpoint: registerExistingEndpoint) { data, response, error in
            completion(data, response, error)
        }
        
    }
    
    /// Pre Authenticate gets the challenge for the specified user from the server
    /// - Parameters:
    ///   - username: username of the user
    ///   - completion: completion returns PreAuthResponseModel which has the challenge for attestation
    func preauthenticate(username: String, completion: @escaping (PreAuthResponseModel?, URLResponse?, Error?) -> Void) {
        
        let preAuthenticateEndpoint = APICases.preauthenticate(username: username)
        
        request(endpoint: preAuthenticateEndpoint) { data, response, error in
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                      let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                      print(errorResponseModel.message)
                      return completion(nil, response, error)
                  }
            
            //            print(data)
            
            let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
            let responseDataString = responseJSON["Response"] as! String
            let responseData = Data(responseDataString.utf8)
            
            //            print(responseDataString)
            
            let dataFromPreAuthModel: PreAuthResponseModel = try! JSONDecoder().decode(PreAuthResponseModel.self, from: responseData)
            
            completion(dataFromPreAuthModel, response, error)
            
        }
        
    }
    
    /// Authenticate the user by verifying the signature from the server
    /// - Parameters:
    ///   - username: username of the currently logged in user
    ///   - authenticatorData: attestationObject from the authenticator
    ///   - signature: signature from the authenticator
    ///   - userHandle: Empty string in U2F
    ///   - clientDataJSON: clientDataJSON from the authenticator
    ///   - id: Credential ID on device
    ///   - completion: completion handler returns AuthResponseModel which has the JWT in it which needs to be stored in App Storage for future use
    func authenticate(username: String, authenticatorData: String, signature: String, userHandle: String, clientDataJSON: String, id: String, completion: @escaping (AuthResponseModel?, URLResponse?, Error?) -> Void) {
        
        let authenticateEndpoint = APICases.authenticate(username: username, authenticatorData: authenticatorData, signature: signature, userHandle: userHandle, clientDataJSON: clientDataJSON, id: id)
        
        request(endpoint: authenticateEndpoint) { data, response, error in
            
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                      let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                      print(errorResponseModel.message)
                      return completion(nil, response, error)
                  }
            
            let dataFromAuthModel: AuthResponseModel = try! JSONDecoder().decode(AuthResponseModel.self, from: data)
            
            completion(dataFromAuthModel, response, error)
        }
        
    }
    
    /// Get details of all the keys registered to the account
    /// - Parameters:
    ///   - username: username of the currently logged in user
    ///   - jwt: JWT for the user saved during Authentication
    ///   - completion: completion handler returns a KeyInfoModel which has all keys registered to an account and their details
    func getKeysInfo(username: String, jwt: String, completion: @escaping (KeyInfoModel?, URLResponse?, Error?) -> Void) {
        let getkeysinfoEndpoint = APICases.getKeysInfo(username: username, jwt: jwt)
        
        request(endpoint: getkeysinfoEndpoint) { data, response, error in
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                      let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                      print(errorResponseModel.message)
                      return completion(nil, response, error)
                  }
            
            let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
            
            let responseDataString = responseJSON["Response"] as! String
            let responseData = Data(responseDataString.utf8)
            
            let dataFromPreAuthModel: KeyInfoModel = try! JSONDecoder().decode(KeyInfoModel.self, from: responseData)
            
            completion(dataFromPreAuthModel, response, error)
            
        }
    }
    
    
    
    /// Get challenge for currently logged in user using JWT verification to add additional FIDO Keys
    /// - Parameters:
    ///   - username: username of currently logged in user
    ///   - displayName: displayName of the currently logged in user
    ///   - jwt: Stored JWT of the currently logged in user from AppStorage
    ///   - completion: Get the challenge from completion handler to send in registerExisitng endpoint
    func preRegisterExisting(username: String, displayName: String, jwt: String, completion: @escaping (PreRegResponseModel?, URLResponse?, Error?) -> Void) {
        let preRegisterExistingEndpoint = APICases.preRegisterExisting(username: username, displayName: displayName, jwt: jwt)
        
        request(endpoint: preRegisterExistingEndpoint) { data, response, error in
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse,
                  (200...299).contains(httpResponse.statusCode) else {
                      let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                      print(responseJSON)
                      return
                  }
            
            let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
            let responseDataString = responseJSON["Response"] as! String
            let responseData = Data(responseDataString.utf8)
            
            let dataFromPreRegModel: PreRegResponseModel = try! JSONDecoder().decode(PreRegResponseModel.self, from: responseData)
            
            completion(dataFromPreRegModel, response, error)
            
        }
    }
    
    /// Remove the selected key from a user account
    /// - Parameters:
    ///   - username: username of currently logged in user
    ///   - keyIds: randomID of the key
    ///   - jwt: Stored JWT of the currently logged in user from AppStorage
    ///   - completion: completion handler returns the response from SKFS Servlet
    func removeKeys(username: String, keyIds: [String], jwt: String, completion: @escaping (Data?, URLResponse?, Error?) -> Void ) {
        
        let removeKeysEnpoint = APICases.removeKeys(username: username, keyIDs: keyIds, jwt: jwt)
        
        request(endpoint: removeKeysEnpoint) { data, response, error in
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
                let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                print(responseJSON)
                return
            }
            
            completion(data, response, error)
            
            
        }
        
    }
    
    
    /// Deletes the user account and all its registered keys
    /// - Parameters:
    ///   - username: username of currently logged in user
    ///   - jwt: Stored JWT of the currently logged in user from AppStorage
    ///   - completion: completion handler returns the response from SKFS Servlet
    func deleteAccount(username: String, jwt: String, completion: @escaping (Data?, URLResponse?, Error?) -> Void) {
        
        let deleteAccountEndpoint = APICases.deleteAccount(username: username, jwt: jwt)
        
        request(endpoint: deleteAccountEndpoint) { data, response, error in
            
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
                let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                print(responseJSON)
                return
            }
           
            completion(data, response, error)
            
        }
    }
    
    
    
    
    
    /// This is used to create endpoint request and make the actual URLSession Request and send the data back in the completion handler
    /// - Parameters:
    ///   - endpoint: Enum to which Endpoint to make the URLSession request
    ///   - completion: completion handler sends back data, response and error back to the caller
    private func request(endpoint: Endpoint, completion: @escaping (Data?, URLResponse?, Error?) -> Void) {
        
        // URL
        let url = URL(string: endpoint.url)!
        var urlRequest = URLRequest(url: url)
        
        // HTTP Method
        urlRequest.httpMethod = endpoint.httpMethod
        
        // Header fields
        endpoint.headers?.forEach({ header in
            urlRequest.setValue(header.value as? String, forHTTPHeaderField: header.key)
        })
        
        // HTTP Body
        //        urlRequest.setValue("Application/json", forHTTPHeaderField: "Content-Type")
        guard let httpBody = try? JSONSerialization.data(withJSONObject: endpoint.body!, options: []) else {
            return
        }
        
        urlRequest.httpBody = httpBody
        
        let task = URLSession.shared.dataTask(with: urlRequest) { data, response, error in
            completion(data, response, error)
        }
        
        task.resume()
    }
    
    // MARK: Uncomment this when running the FIDO2 server locally to test internal endpoints
    //    func urlSession(_ session: URLSession, didReceive challenge: URLAuthenticationChallenge, completionHandler: @escaping (URLSession.AuthChallengeDisposition, URLCredential?) -> Void) {
    //        if challenge.protectionSpace.serverTrust == nil {
    //            completionHandler(.useCredential, nil)
    //        } else {
    //            let trust: SecTrust = challenge.protectionSpace.serverTrust!
    //            let credential = URLCredential(trust: trust)
    //            completionHandler(.useCredential, credential)
    //        }
    //    }
    
}


