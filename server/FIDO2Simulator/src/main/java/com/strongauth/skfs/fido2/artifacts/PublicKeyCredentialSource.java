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
 * A class that represents the FIDO2 Authenticator
 */

package com.strongauth.skfs.fido2.artifacts;

public class PublicKeyCredentialSource
{
    private static String _keyHandle;
    private static String _rpId;
    private static String _id;
    private static String _userHandle;
    private static String _type;
    private static int _alg;

    public PublicKeyCredentialSource(String type, String keyHandle, int alg) {
        _keyHandle = keyHandle;
        _rpId = "";
        _id = "";
        _userHandle = "";
        _alg = alg;
        _type = type;
    }

    // Getters and Setters
    public int getAlg() {
        return _alg;
    }

    public String getType() {
        return _type;
    }

    public String getRpId() {
        return _rpId;
    }

    public String getKeyHandle() {
        return _keyHandle;
    }

    public String getId() {
        return _id;
    }

    public String getUserHandle() {
        return _userHandle;
    }

    public void setRpId(String rpid) {
       _rpId = rpid;
    }

    public void setKeyHandle(String keyHandle) {
        _keyHandle = keyHandle;
    }

    public void setId(String id) {
        _id = id;
    }

    public void setUserHandle(String userHandle) {
        _userHandle = userHandle;
    }
}
