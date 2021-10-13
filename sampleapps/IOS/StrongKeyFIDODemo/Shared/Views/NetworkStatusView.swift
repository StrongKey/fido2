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
 *  Network monitor publishes network changes to the app.
 *
 */

import SwiftUI

struct NetworkStatusView: View {
    
    @Environment(\.verticalSizeClass) var verticalSizeClass
    
    var body: some View {
        ZStack {
            if verticalSizeClass == .regular {
                VStack(spacing: 16) {
                    TitleView()
                    Image(systemName: "wifi.slash").font(.system(size: 56))
                    Text("No network connection").font(.title).fontWeight(.bold)
                    Text("Please check you network connection and try again")
                    Button {
                        UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                    } label: {
                        Text("Go to Settings").fontWeight(.semibold)
                    }
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding()
                .background(.ultraThinMaterial)
            } else {
                HStack(spacing: 32) {
                    TitleView()
                    VStack(spacing: 16) {
                        Image(systemName: "wifi.slash").font(.system(size: 56))
                        Text("No network connection").font(.title).fontWeight(.bold)
                        Text("Please check you network connection and try again")
                        Button {
                            UIApplication.shared.open(URL(string: UIApplication.openSettingsURLString)!)
                        } label: {
                            Text("Go to Settings").fontWeight(.semibold)
                        }
                    }
                    
                }
                .frame(maxWidth: .infinity, maxHeight: .infinity)
                .padding()
                .background(.ultraThinMaterial)
            }
        }
    }
}

struct NetworkStatusView_Previews: PreviewProvider {
    static var previews: some View {
        NetworkStatusView()
    }
}
