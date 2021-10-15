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
 * AccountManager is the ViewModel to interface with the AuthenticationServices APIs
 * See https://developer.apple.com/documentation/authenticationservices for details
 *
 * Public-Private Key Authentication contains all Apple Webauthn/FIDO related APIs
 * See https://developer.apple.com/documentation/authenticationservices/public-private_key_authentication
 *
 *
 */

import AuthenticationServices
import SwiftUI

/// AccountManager is the ViewModel to interface with the AuthenticationServices APIs
class AccountManager: NSObject, ObservableObject, ASAuthorizationControllerPresentationContextProviding, ASAuthorizationControllerDelegate {
    
    var user = "" // username
    var hasPlatformKey = true
    var addingNewPlatformKey = false
    var addingNewSecurityKey = false
    
    @Published var currentlyLoggedInUserJWTFromStorage: String?
    @AppStorage("username") var usernameFromAppStorage: String?
    
    @Published var keys = [Key]()
    
    @Published var isLoggedIn = false
    @Published var isRegisterSuccess = false
    
    @Published var isSKRegisterUsernameError = false
    @Published var isSKLoginError = false
    @Published var isSKTimeoutError = false
    @Published var isAddingNewKeyError = false
    @Published var isASError = false
    @Published var errorMessage = ""
    
    
    /// This function initiates the process for Registering a new account using the Passkey interface.
    /// - Parameter username: Username to register the new account with.
    func signUpWithPasskey(username: String) {
        
        // Fetching the challenge from the server (SKFS Servlet). The challengs is unique for every request.
        // The userID is the identifier for the user's account.
        FidoService().preRegister(username: username, displayName: "Initial Key" + "appleDebugPlatformKeyFlag") { data,  response, error in
            guard let data = data else {
                DispatchQueue.main.async {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        self.isSKRegisterUsernameError = true
                    }
                }
                print(String(describing: error))
                return
            }
            
            
            self.user = username
            let challenge = data.response.challenge.decodeBase64Url()!
            let userID = data.response.user.id.decodeBase64Url()!
            
            let platformKeyCredentialProvider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier: data.response.rp.id)
            let securityKeyCredentialProvider = ASAuthorizationSecurityKeyPublicKeyCredentialProvider(relyingPartyIdentifier: data.response.rp.id)
            
            // Creating Request Object for Platform Key Credential Registration
            let platformKeyRegistrationRequest = platformKeyCredentialProvider.createCredentialRegistrationRequest(challenge: challenge,name: username, userID: userID)
            
            // Platform Key Credential Request preferences
            platformKeyRegistrationRequest.attestationPreference = .none
            platformKeyRegistrationRequest.userVerificationPreference = .required
            
            // Creating Request Obejct for Security Key Credential Registration
            let securityKeyRegistrationRequest = securityKeyCredentialProvider.createCredentialRegistrationRequest(challenge: challenge, displayName: "Initial Key" + "Security", name: username , userID: userID)
            
            // Security Key Credential Request preferences
            securityKeyRegistrationRequest.attestationPreference = ASAuthorizationPublicKeyCredentialAttestationKind.init(rawValue: data.response.attestation ?? "direct")
            securityKeyRegistrationRequest.userVerificationPreference = .preferred
            var credentialParameters: [ASAuthorizationPublicKeyCredentialParameters] = []
            
            for publicKeyParam in data.response.pubKeyCredParams {
                credentialParameters.append(ASAuthorizationPublicKeyCredentialParameters(algorithm: ASCOSEAlgorithmIdentifier(rawValue: publicKeyParam.alg)))
            }
            securityKeyRegistrationRequest.credentialParameters = credentialParameters
            
            securityKeyRegistrationRequest.residentKeyPreference = .preferred
            
            
            // Using both Platform Key Credential Registration Request and Security Key Credential Registration Request at the same time is not supported.
            
            // Only ASAuthorizationPlatformPublicKeyCredentialRegistrationRequests or
            // ASAuthorizationSecurityKeyPublicKeyCredentialRegistrationRequests should be used here.
            let authController = ASAuthorizationController(authorizationRequests: [  platformKeyRegistrationRequest ] )
            authController.delegate = self
            authController.presentationContextProvider = self
            authController.performRequests()
            
        }
    }
    
    /// This function initiates the process for Registering a new account using the Security Key interface.
    /// - Parameter username: Username to register the new account with.
    func signUpWithSecurityKey(username: String) {
        
        // Fetching the challenge from the server (SKFS Servlet). The challengs is unique for every request.
        // The userID is the identifier for the user's account.
        FidoService().preRegister(username: username, displayName: "Initial Key") { data,  response, error in
            guard let data = data else {
                DispatchQueue.main.async {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        self.isSKRegisterUsernameError = true
                    }
                }
                print(String(describing: error))
                return
            }
            
            
            self.user = username
            let challenge = data.response.challenge.decodeBase64Url()!
            let userID = data.response.user.id.decodeBase64Url()!
            
            let securityKeyCredentialProvider = ASAuthorizationSecurityKeyPublicKeyCredentialProvider(relyingPartyIdentifier: data.response.rp.id)
            
            // Creating Request Obejct for Security Key Credential Registration
            let securityKeyRegistrationRequest = securityKeyCredentialProvider.createCredentialRegistrationRequest(challenge: challenge, displayName: "Initial Key", name: username , userID: userID)
            
            // Security Key Credential Request preferences
            securityKeyRegistrationRequest.attestationPreference = ASAuthorizationPublicKeyCredentialAttestationKind.init(rawValue: data.response.attestation ?? "direct")
            securityKeyRegistrationRequest.userVerificationPreference = .preferred
            var credentialParameters: [ASAuthorizationPublicKeyCredentialParameters] = []
            
            for publicKeyParam in data.response.pubKeyCredParams {
                credentialParameters.append(ASAuthorizationPublicKeyCredentialParameters(algorithm: ASCOSEAlgorithmIdentifier(rawValue: publicKeyParam.alg)))
            }
            securityKeyRegistrationRequest.credentialParameters = credentialParameters
            
            securityKeyRegistrationRequest.residentKeyPreference = .preferred
            
            
            // Using both Platform Key Credential Registration Request and Security Key Credential Registration Request at the same time is not supported.
            
            // Only ASAuthorizationPlatformPublicKeyCredentialRegistrationRequests or
            // ASAuthorizationSecurityKeyPublicKeyCredentialRegistrationRequests should be used here.
            let authController = ASAuthorizationController(authorizationRequests: [  securityKeyRegistrationRequest ] )
            authController.delegate = self
            authController.presentationContextProvider = self
            authController.performRequests()
            
        }
    }
    
    
    
    
    /// This function initiates the process for Login using either the Passkey or Security Key based on what is available.
    /// - Parameter username: Username of the user who is trying to Login.
    func signInWith(username: String) {
        
        // Fetching the challenge from the server for the user. The challenge is unique for every request.
        FidoService().preauthenticate(username: username) { data, response, error in
            guard let data = data else {
                DispatchQueue.main.async {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        self.isSKLoginError = true
                    }
                }
                print(String(describing: error))
                return
            }
            
            self.user = username
            let challenge = data.response.challenge.decodeBase64Url()! // Challenge from the servlet
            
            print("Number of Keys: \(data.response.allowCredentials.capacity)")
            var allowedCredentials: [ASAuthorizationPlatformPublicKeyCredentialDescriptor] = []
            
            
            let publicKeyCredentialProvider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier: data.response.rpID)
            let securityKeyProvider = ASAuthorizationSecurityKeyPublicKeyCredentialProvider(relyingPartyIdentifier: data.response.rpID)
            
            
            // Creating obejct for Platform Key Credential Assertion
            let platformKeyAssertionRequest = publicKeyCredentialProvider.createCredentialAssertionRequest(challenge: challenge)
            
            for credential in data.response.allowCredentials {
                allowedCredentials.append(ASAuthorizationPlatformPublicKeyCredentialDescriptor(credentialID: credential.id.decodeBase64Url()!))
            }
            
            // Platform Key Assertion Request preferences
            platformKeyAssertionRequest.allowedCredentials = allowedCredentials
            
            // Creating object for Security Key Credential Assertion
            let securityKeyAssertionRequest = securityKeyProvider.createCredentialAssertionRequest(challenge: challenge)
            
            
            var allowedSecurityKeyCredentials: [ASAuthorizationSecurityKeyPublicKeyCredentialDescriptor] = []
            
            for credential in data.response.allowCredentials {
                allowedSecurityKeyCredentials.append(ASAuthorizationSecurityKeyPublicKeyCredentialDescriptor(credentialID: credential.id.decodeBase64Url()!, transports: ASAuthorizationSecurityKeyPublicKeyCredentialDescriptor.Transport.allSupported))
            }
           
            // Security Key Assertion Request preferences
            securityKeyAssertionRequest.userVerificationPreference = .required
            securityKeyAssertionRequest.allowedCredentials = allowedSecurityKeyCredentials
            
            // Pass in any mix of supported sign in request types.
            var authController = ASAuthorizationController(authorizationRequests: [platformKeyAssertionRequest] )
            
            if data.response.allowCredentials.capacity > 1 {
                authController = ASAuthorizationController(authorizationRequests: [platformKeyAssertionRequest, securityKeyAssertionRequest] )
            } else if !self.hasPlatformKey {
                authController = ASAuthorizationController(authorizationRequests: [securityKeyAssertionRequest] )
            }
            
            
            authController.delegate = self
            authController.presentationContextProvider = self
            authController.performRequests()
        }
    }
    
    /// This function is used to add new keys to existing user account to login on other devices
    /// - Parameter keyDN: Display Name of the new Key
    func addSecurityKeyToExistingUser(keyDN: String?) {
        
        FidoService().preRegisterExisting(username: usernameFromAppStorage!, displayName: keyDN ?? "Security Key", jwt: currentlyLoggedInUserJWTFromStorage!) { data, response, error in
            guard let data = data else {
                DispatchQueue.main.async {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        self.isSKRegisterUsernameError = true
                    }
                }
                print(String(describing: error))
                return
            }
            
            let challenge = data.response.challenge.decodeBase64Url()!
            let userID = data.response.user.id.decodeBase64Url()!
            
            print("Challenge for Current user: \(challenge)")
            print("userID of current user: \(userID)")
            
            let securityKeyProvider = ASAuthorizationSecurityKeyPublicKeyCredentialProvider(relyingPartyIdentifier: data.response.rp.id)
            
            // Creating Request Obejct for Security Key Credential Registration
            let securityKeyRegistrationRequest = securityKeyProvider.createCredentialRegistrationRequest(challenge: challenge, displayName: keyDN ?? "Security Key", name: self.usernameFromAppStorage! , userID: userID)
            
            // Security Key Credential Request preferences
            securityKeyRegistrationRequest.attestationPreference = ASAuthorizationPublicKeyCredentialAttestationKind.init(rawValue: data.response.attestation ?? "direct")
            
            securityKeyRegistrationRequest.userVerificationPreference = .preferred
            
            var credentialParameters: [ASAuthorizationPublicKeyCredentialParameters] = []
            for publicKeyParam in data.response.pubKeyCredParams {
                credentialParameters.append(ASAuthorizationPublicKeyCredentialParameters(algorithm: ASCOSEAlgorithmIdentifier(rawValue: publicKeyParam.alg)))
            }
            securityKeyRegistrationRequest.credentialParameters = credentialParameters
            
            securityKeyRegistrationRequest.residentKeyPreference = .preferred
            
            var excludedSecurityKeyCredentials: [ASAuthorizationSecurityKeyPublicKeyCredentialDescriptor] = []
            for credential in data.response.excludeCredentials {
                excludedSecurityKeyCredentials.append(ASAuthorizationSecurityKeyPublicKeyCredentialDescriptor(credentialID: credential.id.decodeBase64Url()!, transports: ASAuthorizationSecurityKeyPublicKeyCredentialDescriptor.Transport.allSupported))
            }
            securityKeyRegistrationRequest.excludedCredentials = excludedSecurityKeyCredentials
            
            self.addingNewSecurityKey = true
            
            // Only ASAuthorizationPlatformPublicKeyCredentialRegistrationRequests or
            // ASAuthorizationSecurityKeyPublicKeyCredentialRegistrationRequests should be used here.
            let authController = ASAuthorizationController(authorizationRequests: [  securityKeyRegistrationRequest ] )
            authController.delegate = self
            authController.presentationContextProvider = self
            authController.performRequests()
        }
        
    }
    
    /// This function is used to add new Platform key to existing user account to login on other devices synced via Passkeys
    /// - Parameter keyDN: Display Name of the new Key
    func addPlatformKeyToExistingUser(keyDN: String?) {
        
        // Fetching the challenge from the server (SKFS Servlet). The challengs is unique for every request.
        // The userID is the identifier for the user's account.
        FidoService().preRegisterExisting(username: usernameFromAppStorage!, displayName: keyDN ?? "Platform KeyappleDebugPlatformKeyFlag", jwt: currentlyLoggedInUserJWTFromStorage!) { data,  response, error in
            guard let data = data else {
                DispatchQueue.main.async {
                    withAnimation(.easeInOut(duration: 0.3)) {
                        self.isSKRegisterUsernameError = true
                    }
                }
                print(String(describing: error))
                return
            }
            
            let challenge = data.response.challenge.decodeBase64Url()!
            let userID = data.response.user.id.decodeBase64Url()!
            
            let platformKeyCredentialProvider = ASAuthorizationPlatformPublicKeyCredentialProvider(relyingPartyIdentifier: data.response.rp.id)
            
            // Creating Request Object for Platform Key Credential Registration
            let platformKeyRegistrationRequest = platformKeyCredentialProvider.createCredentialRegistrationRequest(challenge: challenge, name: self.usernameFromAppStorage!, userID: userID)
            
            // Platform Key Credential Request preferences
            platformKeyRegistrationRequest.attestationPreference = .none
            platformKeyRegistrationRequest.userVerificationPreference = .preferred
            self.addingNewPlatformKey = true
            
            // Only ASAuthorizationPlatformPublicKeyCredentialRegistrationRequests or
            // ASAuthorizationSecurityKeyPublicKeyCredentialRegistrationRequests should be used here.
            let authController = ASAuthorizationController(authorizationRequests: [  platformKeyRegistrationRequest ] )
            authController.delegate = self
            authController.presentationContextProvider = self
            authController.performRequests()
            
        }
        
    }
    
    /// This functions fetches information about all the Keys user has registered to the account
    func updateKeys() {
        FidoService().getKeysInfo(username: user, jwt: currentlyLoggedInUserJWTFromStorage!) { data, response, error in
            
            guard let data = data else {
                print(String(describing: error))
                return
            }
            
            DispatchQueue.main.asyncAfter(deadline: .now() + 0.6) {
                self.keys = data.response.keys
            }
        }
    }
    
    /// This funtions verifies the JWT for the signed in user. If JWT is not valid then the user is logged out.
    func verifyJWT() {
        
        guard let jwt = currentlyLoggedInUserJWTFromStorage else {
            return
        }
        
        FidoService().getKeysInfo(username: user, jwt: jwt) { data, response, error in
            
            print("Verifying JWT")
            
            guard let httpResponse = response as? HTTPURLResponse, (200...299).contains(httpResponse.statusCode) else {
                
                print("Register Error from SKFS Servlet")
                
                DispatchQueue.main.async {
                    self.isSKTimeoutError = true
                    self.toggleSignInStatus()
                }
                  
                return
            }
        }
    }
    
    // This Controller handles the recieved credentials from Successful ASAuthorization request
    func authorizationController(controller: ASAuthorizationController, didCompleteWithAuthorization authorization: ASAuthorization) {
        switch authorization.credential {
            
            
            // MARK: Platform Key Registration Request verification from SKFS Servlet
        case let platformKeyCredentialRegistration as ASAuthorizationPlatformPublicKeyCredentialRegistration:
            print("A new credential was registered: \(platformKeyCredentialRegistration)")
            // Verify the attestationObject and clientDataJSON with your service.
            // The attestationObject contains the user's new public key, which should be stored and used for subsequent sign ins.
            let attestationObject = platformKeyCredentialRegistration.rawAttestationObject!.toBase64Url()
            let clientDataJSON = platformKeyCredentialRegistration.rawClientDataJSON.toBase64Url()
            let id = platformKeyCredentialRegistration.credentialID.toBase64Url()
            
            print("Added this Credential ID: \(id)")
            
            // Verifying the attestation with SKFS Servlet
            if !addingNewPlatformKey {
                FidoService().register(username: user, attestationObject: attestationObject, clientDataJSON: clientDataJSON, id: id) { data, response, error in
                    
                    guard let data = data else {
                        print(String(describing: error))
                        return
                    }
                    
                    guard let httpResponse = response as? HTTPURLResponse,
                          (200...299).contains(httpResponse.statusCode) else {
                              let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                              print(errorResponseModel.message)

                              print("Register Error from SKFS Servlet")
                              
                              DispatchQueue.main.async {
                                  self.errorMessage = errorResponseModel.message
                                  self.isSKTimeoutError = true
                              }
                              
                              return
                          }
                    
                    let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                    
                    print(responseJSON)
                    
                    DispatchQueue.main.async {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            self.isRegisterSuccess = true
                        }
                    }
                }
            } else {
                addingNewPlatformKey.toggle()
                FidoService().registerExisting(username: usernameFromAppStorage!, attestationObject: attestationObject, clientDataJSON: clientDataJSON, id: id, jwt: currentlyLoggedInUserJWTFromStorage!) { data, response, error in
                    
                    guard let data = data else {
                        print(String(describing: error))
                        return
                    }
                    
                    guard let httpResponse = response as? HTTPURLResponse,
                          (200...299).contains(httpResponse.statusCode) else {
                              let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                              print(errorResponseModel.message)

                              print("Register Error from SKFS Servlet")
                              
                              DispatchQueue.main.async {
                                  self.errorMessage = errorResponseModel.message
                                  self.isSKTimeoutError = true
                              }
                              return
                          }
                    
                    let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                    print(responseJSON)
                    
                    self.updateKeys()
                    
                }
            }
            
            
            // MARK: Platform Key Assertion Request verification from SKFS Servlet
        case let platformKeyCredentialAssertion as ASAuthorizationPlatformPublicKeyCredentialAssertion:
            print("A credential was used to authenticate: \(platformKeyCredentialAssertion)")
            
            // Verify the below signature and clientDataJSON with your service for the given userID.
            let signature = platformKeyCredentialAssertion.signature.toBase64Url()
            let clientDataJSON = platformKeyCredentialAssertion.rawClientDataJSON.toBase64Url()
            let authenticatorData = platformKeyCredentialAssertion.rawAuthenticatorData.toBase64Url()
            let credentialID = platformKeyCredentialAssertion.credentialID.toBase64Url()
            
            // Verifying the signature from the SKFS Servlet
            FidoService().authenticate(username: user, authenticatorData: authenticatorData, signature: signature, userHandle: "", clientDataJSON: clientDataJSON, id: credentialID) { data, response, error in
                
                guard let data = data else {
                    print(String(describing: error))
                    DispatchQueue.main.async {
                        self.errorMessage = "The session has timed out. Please try again"
                        self.isSKTimeoutError = true
                    }
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                          print("Authenticate Error from SKFS Servlet")
                          print(data.message)
                          DispatchQueue.main.async {
                              self.errorMessage = data.message
                              self.isSKTimeoutError = true
                          }
                          return
                      }
                
                DispatchQueue.main.async {
                    self.currentlyLoggedInUserJWTFromStorage = data.jwt
                    self.updateKeys()
                }
                
                // After the server has verified the assertion, sign the user in.
                
                self.toggleSignInStatus()
            }
            
            // MARK: Security Key Registration Request verification from SKFS Servlet
        case let securityKeyCredentialRegistration as ASAuthorizationSecurityKeyPublicKeyCredentialRegistration:
            print("A new Security Key credential was registered: \(securityKeyCredentialRegistration)")
            
            let attestationObject = securityKeyCredentialRegistration.rawAttestationObject!.toBase64Url()
            let clientDataJSON = securityKeyCredentialRegistration.rawClientDataJSON.toBase64Url()
            let id = securityKeyCredentialRegistration.credentialID.toBase64Url()
            
            print("Attestation Object: \(attestationObject)")
            print("clientJSON: \(clientDataJSON)")
            
            // Verifying the attestation with SKFS Servlet
            
            if !addingNewSecurityKey {
                FidoService().register(username: user, attestationObject: attestationObject, clientDataJSON: clientDataJSON, id: id) { data, response, error in
                    
                    guard let data = data else {
                        print(String(describing: error))
                        return
                    }
                    
                    guard let httpResponse = response as? HTTPURLResponse,
                          (200...299).contains(httpResponse.statusCode) else {
                              let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                              print(errorResponseModel.message)

                              print("Register Error from SKFS Servlet")
                              
                              DispatchQueue.main.async {
                                  self.errorMessage = errorResponseModel.message
                                  self.isSKTimeoutError = true
                              }
                              return
                          }
                    
                    let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                    
                    print(responseJSON)
                    
                    DispatchQueue.main.async {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            self.isRegisterSuccess = true
                        }
                    }
                }
            } else {
                addingNewSecurityKey.toggle()
                FidoService().registerExisting(username: usernameFromAppStorage!, attestationObject: attestationObject, clientDataJSON: clientDataJSON, id: id, jwt: currentlyLoggedInUserJWTFromStorage!) { data, response, error in
                    
                    guard let data = data else {
                        print(String(describing: error))
                        return
                    }
                    
                    guard let httpResponse = response as? HTTPURLResponse,
                          (200...299).contains(httpResponse.statusCode) else {
                              let errorResponseModel = try! JSONDecoder().decode(ErrorResponseModel.self, from: data)
                              print(errorResponseModel.message)

                              print("Register Error from SKFS Servlet")
                              
                              DispatchQueue.main.async {
                                  self.errorMessage = errorResponseModel.message
                                  self.isSKTimeoutError = true
                              }
                              return
                          }
                    
                    let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                    print(responseJSON)
                    
                    self.updateKeys()
                    
                }
            }
            
            
            // MARK: Security Key Assertion Request verification from SKFS Servlet
        case let securityKeyCredentialAssertion as ASAuthorizationSecurityKeyPublicKeyCredentialAssertion:
            print("A credential was used to authenticate: \(securityKeyCredentialAssertion)")
            // Verify the below signature and clientDataJSON with your service for the given userID.
            
            let signature = securityKeyCredentialAssertion.signature.toBase64Url()
            let clientDataJSON = securityKeyCredentialAssertion.rawClientDataJSON.toBase64Url()
            let authenticatorData = securityKeyCredentialAssertion.rawAuthenticatorData.toBase64Url()
            let credentialID = securityKeyCredentialAssertion.credentialID.toBase64Url()
            
            print("Signature: \(String(describing: signature))")
            print("Client Data JSON: \(clientDataJSON)")
            
            // Verifying the signature from the SKFS Servlet
            FidoService().authenticate(username: user, authenticatorData: authenticatorData, signature: signature, userHandle: "", clientDataJSON: clientDataJSON, id: credentialID) { data, response, error in
                
                guard let data = data else {
                    print(String(describing: error))
                    DispatchQueue.main.async {
                        self.errorMessage = "The session has timed out. Please try again"
                        self.isSKTimeoutError = true
                    }
                    return
                }
                
                guard let httpResponse = response as? HTTPURLResponse,
                      (200...299).contains(httpResponse.statusCode) else {
                          print("Authenticate Error from SKFS Servlet")
                          print(data.message)
                          DispatchQueue.main.async {
                              self.errorMessage = data.message
                              self.isSKTimeoutError = true
                          }
                          return
                      }
                
                print("Response: \(data.response)")
                print("JWT: \(data.jwt ?? "")")
                
                DispatchQueue.main.async {
                    self.currentlyLoggedInUserJWTFromStorage = data.jwt
                    self.updateKeys()
                }
                
                self.toggleSignInStatus()
            }
            
            
        default:
            fatalError("Received unknown authorization type.")
        }
    }
    
    // This controller handles all the errors from ASAuthorization requests.
    func authorizationController(controller: ASAuthorizationController, didCompleteWithError error: Error) {
        guard let authorizationError = ASAuthorizationError.Code(rawValue: (error as NSError).code) else {
            print("Unexpected authorization error: \(error.localizedDescription)")
            return
        }
        
        if authorizationError == .canceled {
            // Either no credentials were found and the request silently ended, or the user canceled the request.
            // Consider asking the user to create an account.
            print("The Error we are getting: \(error.localizedDescription)")
            
            if error.localizedDescription == "The operation couldn’t be completed. No credentials available for login." {
                print("No Platform key found!")
                hasPlatformKey = false
                signInWith(username: user)
            }
            
            addingNewPlatformKey = false
            addingNewSecurityKey = false
            
            print("Request canceled.")
        } else {
            // Other ASAuthorization error.
            // The userInfo dictionary should contain useful information.
            print("Error Description: \(error.localizedDescription)")
            print("Error: \((error as NSError).userInfo)")
            
            if addingNewSecurityKey || addingNewPlatformKey {
                
                isAddingNewKeyError = true
                addingNewPlatformKey = false
                addingNewSecurityKey = false
                
            } else {
                if error.localizedDescription == "The operation couldn’t be completed. Syncing platform authenticator must be enabled to register a platform public key credential; this can be enabled in Settings > Developer." {
                    errorMessage = "In order to register a FIDO credential on this device, you must enable synchronizing credentials/keys with iCloud. Go to Settings to do so."
                    isASError.toggle()
                } else {
                    isASError.toggle()
                    errorMessage = error.localizedDescription
                }
            }
        }
    }
    
    func presentationAnchor(for controller: ASAuthorizationController) -> ASPresentationAnchor {
        
        
        // MARK: Uncomment this block to use the macOS native version of the application
        /*
         #if os(iOS)
         guard let window = UIApplication.shared.delegate?.window else {
         fatalError()
         }
         return window!
         #else
         let window = NSApplication.shared.windows.first {$0.isKeyWindow}
         return window ?? ASPresentationAnchor()
         #endif
         */
        
        return ASPresentationAnchor()
    }
    
    // Do any actions necessary after a successfull Registration/Assertion here.
    func toggleSignInStatus() {
        DispatchQueue.main.async {
//            self.hasPlatformKey = true
            self.isSKRegisterUsernameError = false
            self.isSKLoginError = false
            self.isSKTimeoutError = false
            self.isAddingNewKeyError = false
            self.isASError = false
            self.isLoggedIn.toggle()
            self.keys = []
        }
    }
    
}
