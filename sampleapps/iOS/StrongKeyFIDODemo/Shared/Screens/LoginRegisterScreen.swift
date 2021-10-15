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
 * This is the Initial Screen which shows the Login/Register Interface
 *
 */

import SwiftUI

/// This is the Initial Screen which shows the Login/Register Interface
struct LoginRegisterScreen: View {
    
    @Environment(\.colorScheme) var colorScheme
    @Environment(\.verticalSizeClass) var verticalSizeClass
    
    @AppStorage("username") var userFromAppStorage: String?
    @State private var userName = ""
    @State private var loginToggle = false
    @State private var showHomeScreen = false
    @State private var rotationAngle = 0.0
    
    @FocusState private var userTextFieldFocus: Bool
    
    @ObservedObject var viewModel: AccountManager
    @ObservedObject var networkMonitor = NetworkMonitor()
    
    var body: some View {
        
        
        ZStack {
            
            #if os(iOS)
            screenContent
                .fullScreenCover(isPresented: $viewModel.isLoggedIn) {
                    print("ContentView viewModel isLoggedIn: \(viewModel.isLoggedIn)")
                } content: {
                    KeyManagementScreen(viewModel: viewModel)
                }
            #else
            if viewModel.isLoggedIn != true {
                loginPanel
            } else {
                KeyManagementScreen(viewModel: viewModel)
            }
            #endif
            
            if !networkMonitor.isConnected {
                NetworkStatusView()
                    .edgesIgnoringSafeArea(.all)
                    .transition(.opacity)
                    .zIndex(2)
            }
        }
        
        
        
        
    }
    
    var loginPanel: some View {
        VStack {
            VStack(alignment: .leading) {
                Text("FIDO Demo")
                    .font(.title)
                    .fontWeight(.bold)
                
                ZStack {
                    TextField("username", text: $userName)
                        .textContentType(.username)
                        .disableAutocorrection(true)
                    #if os(iOS)
                        .focused($userTextFieldFocus)
                        .textInputAutocapitalization(.never)
                        .padding(.vertical, 14)
                        .padding(.horizontal, 8)
                        .background(
                            RoundedRectangle(cornerRadius: 8)
                                .stroke(Color("labelColor").opacity(colorScheme == .dark ? 0.4 : 0.2))
                                .background(Color("textFieldBackground"))
                                .cornerRadius(8)
                        )
                        .submitLabel(.done)
                    #endif
                }
                .zIndex(1)
                .alert("Error", isPresented: $viewModel.isASError) {
                    
                    Button("OK") {
                        // Handle acknowledgement.
                    }
                } message: {
                    Text(viewModel.errorMessage)
                }
                
                if viewModel.isSKRegisterUsernameError {
                    HStack(spacing: 4.0) {
                        Image(systemName: "exclamationmark.circle")
                        Text("username already in use").onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    viewModel.isSKRegisterUsernameError = false
                                }
                            }
                        }
                    }
                    .font(.footnote)
                    .foregroundColor(Color.red)
                }
                
                if viewModel.isSKLoginError {
                    HStack(spacing: 4.0) {
                        Image(systemName: "exclamationmark.circle")
                        Text("user does not exist").onAppear {
                            DispatchQueue.main.asyncAfter(deadline: .now() + 3.0) {
                                withAnimation(.easeInOut(duration: 0.3)) {
                                    viewModel.isSKLoginError = false
                                }
                            }
                        }
                    }
                    .font(.footnote)
                    .foregroundColor(Color.red)
                }
                
                HStack {
                    
                    
                    if loginToggle == false {
                        
                        StrongButton(buttonTitle: "Register *Platform* Key", scaleFactor: 0.6) {
                            userTextFieldFocus = false
                            viewModel.hasPlatformKey = true
                            viewModel.signUpWithPasskey(username: userName)
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(userName.isEmpty)
                        
                        StrongButton(buttonTitle: "Register *Security* Key", scaleFactor: 0.6) {
                            userTextFieldFocus = false
                            viewModel.signUpWithSecurityKey(username: userName)
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(userName.isEmpty)
                        
                        
                    } else {
                        StrongButton(buttonTitle:"Login") {
                            userTextFieldFocus = false
                            viewModel.hasPlatformKey = true
                            viewModel.signInWith(username: userName)
                        }
                        .buttonStyle(.borderedProminent)
                        .disabled(userName.isEmpty)
                    }
                    
                    
                }
                .alert(isPresented: $viewModel.isSKTimeoutError) {
                    Alert(
                        title: Text(viewModel.errorMessage)
                    )
                }
                
                Rectangle()
                    .frame(height: 1)
                    .foregroundColor(Color.gray.opacity(0.3))
                    .padding(.vertical, 8)
                
                
                
                HStack(spacing: 4.0) {
                    Text(loginToggle ? "Don't have an account?" : "Already have an account?")
                        .font(.footnote)
                    
                    Button {
                        withAnimation(.easeInOut(duration: 0.3)) {
                            loginToggle.toggle()
                            self.rotationAngle += 180
                        }
                        
                    } label: {
                        Text(loginToggle ? "Register" : "Login")
                            .font(.footnote.bold())
                    }
                    
                }
                .alert(isPresented: $viewModel.isRegisterSuccess) {
                    Alert(
                        title: Text("Successfully Registered"),
                        message: Text("Login to start using the app"),
                        dismissButton: .default(Text("Ok"), action: {
                            withAnimation(.easeInOut(duration: 0.3)) {
                                loginToggle.toggle()
                                self.rotationAngle += 180
                            }
                        }))
                }
                
                
            }
            .padding(20)
        }
        .rotation3DEffect(Angle(degrees: self.rotationAngle), axis: (x: 0, y: 1, z: 0))
        .background(
            RoundedRectangle(cornerRadius: 16)
                .stroke(Color.white.opacity(0.2))
                .background(.thinMaterial)
        )
        .cornerRadius(16)
        .padding(.horizontal)
        .rotation3DEffect(Angle(degrees: self.rotationAngle), axis: (x: 0, y: 1, z: 0))
        .frame(minWidth: 300, maxWidth: 600)
        .padding(.bottom, 32)
    }
    
    var screenContent: some View {
        GeometryReader { metric in
            ZStack{
                Color("Background")
                    .edgesIgnoringSafeArea(.all)
                
                HStack {
                    if verticalSizeClass == .regular {
                        Spacer()
                        VStack() {
                            Button {
                                
                                #if os(iOS)
                                UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                                #endif
                                
                            } label: {
                                Label("Language", systemImage: "globe")
                                    .labelStyle(.iconOnly)
                                    .foregroundColor(.white)
                                    .padding(8)
                                    .background(RoundedRectangle(cornerRadius: 8))
                                    .padding(.trailing)
                            }
                            Spacer()
                        }
                    } else {
                        VStack() {
                            Button {
                                
                                #if os(iOS)
                                UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                                #endif
                                
                            } label: {
                                Label("Language", systemImage: "globe")
                                    .labelStyle(.iconOnly)
                                    .foregroundColor(.white)
                                    .padding(8)
                                    .background(RoundedRectangle(cornerRadius: 8))
                                    .padding()
                            }
                            Spacer()
                        }
                        Spacer()
                    }
                    
                    
                    
                }
                
                
                if verticalSizeClass == .regular {
                    VStack {
                        TitleView()
                        loginPanel
                    }
                } else {
                    HStack(alignment: .center) {
                        TitleView()
                            .frame(width: metric.size.width * 0.3)
                        loginPanel
                            .frame(width: metric.size.width * 0.6)
                    }
                    .padding(.bottom, -32)
                }
                
            }
        }
        
        .onAppear {
            guard let user = userFromAppStorage else {
                print("No previous stored user")
                return
            }
            loginToggle = true
            userName = user
//            viewModel.signInWith(username: user) // Can be tested for failed signInWith request
        }
        
    }
    
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            LoginRegisterScreen(viewModel: AccountManager())
                .previewInterfaceOrientation(.portrait)
        }
    }
}
