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
 * This Screen which shows Key Management interaface where you can add/delete keys and manage account.
 *
 */

import SwiftUI

/// This Screen which shows Key Management interaface where you can add/delete keys and manage account.
struct KeyManagementScreen: View {
    @Environment(\.refresh) private var refresh
    
    @ObservedObject var viewModel: AccountManager
    @ObservedObject var networkMonitor = NetworkMonitor()
    
    @AppStorage("username") var usernameFromAppStorage: String?
    
    @State private var isDeleteAccountConfirm = false
    @State private var isDeleteKeyConfirm = false
    @State private var isFinalKey = false
    @State private var showInfoPanel = false
    @State private var addKeyPanelShowing = false
    @State private var hasPasskey = false
    @State private var selectedKey = Key(fidoProtocol: "", randomid: "", randomidTTLSeconds: "", fidoVersion: "", keyid: "", credentialID: "", createLocation: "", createDate: 0, lastusedLocation: "", modifyDate: 0, status: "", displayName: "", attestationFormat: "")
    
    let layout = [
        GridItem(.flexible())
    ]
    
    
    var body: some View {
        
        ZStack {
            NavigationView {
                VStack {
                    List(viewModel.keys) { item in
                        
                        
                        
                        ZStack(alignment: .leading) {
                            NavigationLink(
                                destination: KeyDetailScreen(key: item)) {
                                    EmptyView()
                                }
                                .opacity(0)
                            
                            HStack {
                                
                                VStack {
                                    // Icon should be displayed based on attestation Format but right now it's defaulted to none for all types of keys
                                    // modifyDate or debugFlag in displayName is used to identify the platform key
                                    if item.displayName.contains("appleDebugPlatformKeyFlag") {
                                        Image(systemName: "cpu")
                                            .imageScale(.large)
                                            .foregroundColor(.accentColor)
                                    } else {
                                        Image("usbKey").resizable()
                                            .aspectRatio(contentMode: .fit)
                                            .frame(width: 26)
                                    }
                                }
                                .frame(width: 40)
                                
                                VStack(alignment: .leading, spacing: 4.0) {
                                    Text(item.displayName.replacingOccurrences(of: "appleDebugPlatformKeyFlag", with: "")).font(.headline).padding(.vertical, 2)
                                    
                                    VStack {
                                        HStack {
                                            Text("Created On:")
                                            Spacer()
                                            Text(convertDate(dateValue: item.createDate))
                                        }
                                        HStack {
                                            Text("Last used:")
                                            Spacer()
                                            Text(convertDate(dateValue: item.modifyDate))
                                        }
                                        HStack {
                                            Text("Last used location:")
                                            Spacer()
                                            Text(item.lastusedLocation)
                                        }
                                    }
                                    .font(.caption)
                                    
                                    
                                }
                                
                                Spacer()
                                Menu {
                                    Button(role: .destructive) {
                                        
                                        if viewModel.keys.capacity < 2 {
                                            isFinalKey = true
                                            return
                                        }
                                        
                                        selectedKey = item
                                        isDeleteKeyConfirm = true
                                        
                                    } label: {
                                        Label("Delete", systemImage: "minus.circle")
                                    }
                                } label: {
                                    Image(systemName: "ellipsis")
                                        .imageScale(.large)
                                        .symbolVariant(.circle)
                                        .symbolRenderingMode(.multicolor)
                                }
                            }
                            .contextMenu(ContextMenu {
                                Button(role: .destructive) {
                                    
                                    if viewModel.keys.capacity < 2 {
                                        isFinalKey = true
                                        return
                                    }
                                    selectedKey = item
                                    isDeleteKeyConfirm = true
                                } label: {
                                    Label("Delete", systemImage: "minus.circle")
                                }
                                
                            })
                            
                        }
                        #if os(iOS)
                        .actionSheet(isPresented: $isDeleteKeyConfirm) {
                            ActionSheet(title: Text("Are you sure you want to delete this key?"),
                                        message: Text("You will not be able to use this key to login on any device."),
                                        buttons: [
                                            .cancel(),
                                            .destructive(
                                                Text("Yes, delete this key."),
                                                action: {
                                                    
                                                    FidoService().removeKeys(username: viewModel.user, keyIds: [selectedKey.keyid!], jwt: viewModel.currentlyLoggedInUserJWTFromStorage!) { data, response, error in
                                                        guard let data = data else {
                                                            print(String(describing: error))
                                                            return
                                                        }
                                                        let responseJSON = try! JSONSerialization.jsonObject(with: data, options: []) as! [String: Any]
                                                        print(responseJSON)
                                                        
                                                        if selectedKey.displayName.contains("appleDebugPlatformKeyFlag") {
                                                            hasPasskey = false
                                                        }
                                                        
                                                        viewModel.updateKeys()
                                                        
                                                    }
                                                }
                                            ),
                                        ]
                            )
                        }
                        #endif
                        .alert(isPresented: $isFinalKey) {
                            Alert(
                                title: Text("Cannot delete this Key"),
                                message: Text("This is your only key for the account. If you want to delete the account select Delete Account from the Settings Menu top right!"),
                                dismissButton: .cancel(Text("Ok")))
                        }
                        
                        // TODO: Delete Keys can be implemented via Swipe Actions, Long Press (Context menu), Button Tap (Menu)
                        
                    }
                    .id(UUID())
                    .transition(.opacity)
                    .refreshable {
                        viewModel.updateKeys()
                    }
                    
                    HStack {
                        
                        StrongButton(buttonTitle: "Add Key", icon: Image(systemName: "key.fill")) {
                            
                            for key in viewModel.keys {
                                if key.displayName.contains("appleDebugPlatformKeyFlag") {
                                    hasPasskey = true
                                }
                            }
                            
                            print("Has Passkey: \(hasPasskey)")
                            
                            withAnimation(.spring())  {
                                addKeyPanelShowing = true
                            }
                            
                        }
                        .buttonStyle(.borderedProminent)
                        .ignoresSafeArea()
                        .alert("Error adding new Key", isPresented: $viewModel.isAddingNewKeyError) {
                            Button("OK", role: .cancel) {
    //                            viewModel.isAddingNewKeyError.toggle()
                            }
                        }
                        
                        StrongButton(buttonTitle: "Sign Out") {
                            print("HomeScreen viewModel isLoggedIn: \(viewModel.isLoggedIn)")
                            usernameFromAppStorage = nil // Reset usernameFromAppStorage when logging out
                            viewModel.currentlyLoggedInUserJWTFromStorage = nil
                            viewModel.hasPlatformKey = true
                            viewModel.toggleSignInStatus()
                        }
                        .buttonStyle(.bordered)
                        .ignoresSafeArea()
                        
                        .alert(isPresented: $viewModel.isSKTimeoutError) {
                            Alert(
                                title: Text(viewModel.errorMessage)
                            )
                        }
                    }
                    .padding()
                    
                }
                .onAppear {
                    viewModel.verifyJWT()
                    if viewModel.currentlyLoggedInUserJWTFromStorage != nil {
                        usernameFromAppStorage = viewModel.user
                    } else {
                        print("Prompting the user to login again")
                        usernameFromAppStorage = viewModel.user
                        viewModel.toggleSignInStatus()
                        viewModel.signInWith(username: usernameFromAppStorage!)
                    }
                }
                .navigationTitle("Key Management")
                .background(Color("listBackground").edgesIgnoringSafeArea(.bottom))
                .toolbar {
                    #if os(iOS)
                    ToolbarItemGroup(
                        placement: .navigationBarTrailing
                    ) {
                    
                        Menu {
                            
                            Button(role: .destructive, action: {
                                print("Delete Account Tapped")
                                isDeleteAccountConfirm = true
                            }) {
                                Label("Delete Account", systemImage: "trash.square.fill")
                            }
                            
                        } label: {
                            Image(systemName: "gear")
                        }
                        .confirmationDialog(
                            "Are you sure you want to delete your account?",
                            isPresented: $isDeleteAccountConfirm
                        ) {
                            Button("Yes, delete my account", role: .destructive) {
                                FidoService().deleteAccount(username: usernameFromAppStorage!, jwt: viewModel.currentlyLoggedInUserJWTFromStorage!) { data, response, error in
                                    print("HomeScreen viewModel isLoggedIn: \(viewModel.isLoggedIn)")
                                    DispatchQueue.main.async {
                                        usernameFromAppStorage = nil // Reset usernameFromAppStorage when logging out
                                        viewModel.currentlyLoggedInUserJWTFromStorage = nil
                                    }
                                    viewModel.toggleSignInStatus()
                                }
                            }
                        } message: {
                            Text("This step cannot be reversed. Your username and all your registered keys will be removed from the account.")
                        }
                        
                    }
                    #endif
                    
                    
                }
                #if os(iOS)
                .sheet(isPresented: $showInfoPanel) {
                    VStack {
                        Text("License Agreement")
                            .font(.title)
                            .padding(50)
                        Text("Terms and conditions go here.")
                            .padding(50)
                        Button("Dismiss",
                               action: { showInfoPanel.toggle() })
                    }
                }
                
                #endif
            }
            
            AddKeysPanel(isPresented: $addKeyPanelShowing, passkeyAlreadyPresent: $hasPasskey, viewModel: viewModel)
            
            if viewModel.keys.isEmpty {
                ProgressView()
                    .progressViewStyle(.circular)
                    .scaleEffect(2)
            }
            
            if !networkMonitor.isConnected {
                NetworkStatusView()
                    .edgesIgnoringSafeArea(.all)
                    .transition(.opacity)
                    .zIndex(2)
            }
            
        }
        
        
    }
    
}
//
//struct HomeScreen_Previews: PreviewProvider {
//
//    static let key = Key(randomid: "", randomidTTLSeconds: "", fidoProtocol: "", fidoVersion: "", credentialID: "", createLocation: "", createDate: 0, lastusedLocation: "", modifyDate: 0, status: "", displayName: "", attestationFormat: "")
//
//    static var previews: some View {
//        KeyManagementScreen(viewModel: AccountManager(), keys: [key])
//
//
//    }
//}
