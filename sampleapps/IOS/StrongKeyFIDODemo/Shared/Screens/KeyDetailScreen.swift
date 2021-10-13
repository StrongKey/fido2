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
 * This screen shows details of the selected key.
 *
 */

import SwiftUI

struct KeyDetailScreen: View {
    
    var key: Key
    
    var body: some View {
        
        List {
            Section("INFO") {
                VStack(alignment: .leading, spacing: 8.0) {
                    Text("Key Credential ID")
                    Text("\(key.credentialID)").foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                VStack(alignment: .leading, spacing: 8.0) {
                    Text("Display Name")
                    Text("\(key.displayName.replacingOccurrences(of: "appleDebugPlatformKeyFlag", with: ""))").foregroundColor(Color(UIColor.secondaryLabel))
                }
            }
            
            Section("ADDITIONAL DETAILS") {
                
                HStack {
                    Text("Status")
                    Spacer()
                    Text("\(key.status)").foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                
                HStack {
                    Text("Create Date")
                    Spacer()
                    Text(convertDate(dateValue: key.createDate)).foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                HStack {
                    Text("Create Location")
                    Spacer()
                    Text("\(key.createLocation)").foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                HStack {
                    Text("Last Used Date")
                    Spacer()
                    Text(convertDate(dateValue: key.modifyDate)).foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                HStack {
                    Text("Last Used Location")
                    Spacer()
                    Text("\(key.lastusedLocation)").foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                HStack {
                    Text("Attestation Format")
                    Spacer()
                    Text("\(key.attestationFormat)").foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                HStack {
                    Text("FIDO Protocol")
                    Spacer()
                    Text("\(key.fidoProtocol)").foregroundColor(Color(UIColor.secondaryLabel))
                }
                
                
                
                
            }
        }
        .navigationTitle("Key Details")
        .navigationBarTitleDisplayMode(.inline)
        
        
    }
}

struct KeyDetail_Previews: PreviewProvider {
    static var previews: some View {
        KeyDetailScreen(key: Key(fidoProtocol: "", randomid: "1-10-absk17-20", randomidTTLSeconds: "", fidoVersion: "", keyid: "", credentialID: "sdfsfsdfswsefrwfsefsefsefsefsefsefssefsefsefsefsdfqwe", createLocation: "", createDate: 0, lastusedLocation: "", modifyDate: 0, status: "Active", displayName: "absk_DN", attestationFormat: "none"))
            .preferredColorScheme(.dark)
    }
}
