/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.crypto.interfaces;

import com.strongkey.crypto.utility.CryptoException;
import java.security.PrivateKey;

public interface CryptoModule
{

    public PrivateKey getXMLSignatureSigningKey(
                String secret,
                String signingdn)
            throws
                CryptoException;

    }
