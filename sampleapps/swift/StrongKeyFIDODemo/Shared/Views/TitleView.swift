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
 * A reusable view with StrongKey's Title and Logo
 *
 */

import SwiftUI

/// A reusable view with StrongKey's Logo and Title
struct TitleView: View {
    
    var body: some View {
        VStack {
            Image("SKLogo")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(height: 180)
            Image("SKTitle")
                .resizable()
                .aspectRatio(contentMode: .fit)
                .frame(maxWidth: 280, minHeight: 22)
                .padding(.bottom, 22)
        }
    }
}

struct TitleView_Previews: PreviewProvider {
    static var previews: some View {
        TitleView()
            .preferredColorScheme(.dark)
            
    }
}
