/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido2;

import java.util.ArrayList;

public interface FIDO2AttestationStatement {

    public void decodeAttestationStatement(Object attStmt);

    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData);

    public ArrayList getX5c();

    public String getAttestationType();
}
