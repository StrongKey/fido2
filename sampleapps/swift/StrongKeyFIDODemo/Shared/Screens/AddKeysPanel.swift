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
 * This Panel shows interface for adding new keys.
 *
 */

import SwiftUI



struct AddKeysPanel: View {
    
    enum Field: Hashable {
        case keyName
    }
    
    @Environment(\.colorScheme) var colorScheme
    @Environment(\.verticalSizeClass) var verticalSizeClass
    @Environment(\.horizontalSizeClass) var horizontalSizeClass
    
    @Binding var isPresented: Bool
    @Binding var passkeyAlreadyPresent: Bool
    @ObservedObject var viewModel: AccountManager
    
    @State var keyDisplayName: String = ""
    @FocusState private var keyFieldInFocused: Field?
    
    var body: some View {
        
        ZStack(alignment: .center) {
            
            if isPresented {
                Color.black
                    .opacity(0.6)
                    .ignoresSafeArea()
                    .onTapGesture {
                        
                        withAnimation(.easeInOut) {
                            isPresented = false
                        }
                        
                    }
                
                if passkeyAlreadyPresent {
                    VStack {
                        VStack(alignment: .leading, spacing: 8.0) {
                            Label("Note", systemImage: "exclamationmark.circle")
                            HStack {
                                Text("You already have a Passkey Key registered to this account.")
                                Spacer()
                            }
                        }
                        .font(.callout)
                        .padding(8)
                        .background(Color("AccentColor").opacity(0.3))
                        .background(.ultraThickMaterial)
                        .cornerRadius(8)
                        
                        Spacer()
                    }
                    .frame(maxWidth: 600)
                    .padding(.horizontal)
                    .transition(.move(edge: .top))
                }
                
                if verticalSizeClass == .regular && horizontalSizeClass == .regular {
                    panel
                        .cornerRadius(16)
                        .transition(.opacity)
                } else {
                    VStack {
                        Spacer()
                        panel
                    }
                    .transition(.move(edge: .bottom))
                }
                
                
            }
        }
        
        
        
    }
    
    var panel: some View {
        VStack {
            VStack(alignment: .leading, spacing: 16.0) {
                Text("Key Information").font(.title2).fontWeight(.semibold)
                Text("You can use additional keys to login on other devices.").font(.subheadline).opacity(0.8)
                TextField("Key Name", text: $keyDisplayName)
                    .focused($keyFieldInFocused, equals: .keyName)
                    .textContentType(.username)
                    .disableAutocorrection(true)
                    .zIndex(2.0)
                    #if os(iOS)
                    .textInputAutocapitalization(.never)
                    .padding(.vertical, 14)
                    .padding(.horizontal, 8)
                    .background(
                        RoundedRectangle(cornerRadius: 8)
                            .stroke(Color("labelColor").opacity(colorScheme == .dark ? 0.4 : 0.2))
                            .background(Color("textFieldBackground"))
                            .cornerRadius(8)
                    )
                    .submitLabel(.return)
                
                    #endif
                
                HStack {
                    StrongButton(buttonTitle: "Security Key") {
                        print("Calling AccountManager preRegisterExiting...")
                        viewModel.addSecurityKeyToExistingUser(keyDN: keyDisplayName != "" ? keyDisplayName : "Security Key")
                        isPresented = false
                        keyDisplayName = ""
                    }
                    .buttonStyle(.borderedProminent)
                    
                    if !passkeyAlreadyPresent {
                        StrongButton(buttonTitle: "Platform Key", icon: Image(systemName: "cpu")) {
                            print("Calling AccountManager preRegisterExiting...")
                            viewModel.addPlatformKeyToExistingUser(keyDN: keyDisplayName != "" ? keyDisplayName + "appleDebugPlatformKeyFlag" : "Platform KeyappleDebugPlatformKeyFlag")
                            isPresented = false
                            keyDisplayName = ""
                        }
                        .buttonStyle(.borderedProminent)
                    }
                     
                }
                .onAppear {
                    DispatchQueue.main.async {  /// Anything over 0.5 seems to work
                        self.keyFieldInFocused = .keyName
                    }
                }
            }
            .padding()
        }
        .frame(maxWidth: 600)
        .background(.regularMaterial)
        .edgesIgnoringSafeArea(.bottom)
    }
    
}

struct SlidingUpPanel_Previews: PreviewProvider {
    static var previews: some View {
        Group {
            AddKeysPanel(isPresented: .constant(true), passkeyAlreadyPresent: .constant(true), viewModel: AccountManager())
        }
    }
}
