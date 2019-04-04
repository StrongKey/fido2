/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.utilities;

/**
 * This class represents the exception thrown due to application-level errors.
 */
public class SKFEException extends Exception{
    
    /**
     * Different types of constructors
     */
    public SKFEException() {
        super();
    }

    public SKFEException(String message) {
        super(message);
    }

    public SKFEException(Exception e) {
        super(e);
    }
}
