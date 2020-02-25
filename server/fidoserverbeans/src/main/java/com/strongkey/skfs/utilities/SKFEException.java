/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
