/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.crypto.utility;

public class CryptoException extends Exception {
    private static final long serialVersionUID = 1L;

    /**
     * Creates a new instance of <code>StrongKeyLiteException</code> without detail message.
     */
    public CryptoException() {
    }

    /**
     * Constructs an instance of <code>StrongKeyLiteException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public CryptoException(String msg) {
        super(msg);
    }
}
