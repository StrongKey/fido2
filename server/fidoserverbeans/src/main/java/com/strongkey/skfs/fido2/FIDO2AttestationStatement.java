/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido2;

import java.util.ArrayList;

public interface FIDO2AttestationStatement {

    public void decodeAttestationStatement(Object attStmt);
    
    public Boolean verifySignature(String browserDataBase64, FIDO2AuthenticatorData authData);
    
    public ArrayList getX5c();
    
    public String getAttestationType();
}
