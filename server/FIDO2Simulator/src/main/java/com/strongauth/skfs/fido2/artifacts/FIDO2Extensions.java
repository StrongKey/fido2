/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * FIDO2 Extension objects - see W3C WebAuthn specification at
 * https://www.w3.org/TR/webauthn/#sctn-defined-extensions
 */

package com.strongauth.skfs.fido2.artifacts;

import java.io.IOException;
import java.util.Map;

public class FIDO2Extensions
{
    Map<String, Object> extensionMap;

    // Decode and return length of extension(s)
    public int decodeExtensions(byte[] extensionBytes) throws IOException
    {
        return extensionBytes.length - 0;
    }

    // Return a specific extension object
    public Object getExtension(String extensionName){
        return extensionMap.get(extensionName);
    }
}
