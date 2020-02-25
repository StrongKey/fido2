/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.utilities;

/**
 * This class represents the exception thrown due to application-level errors.
 */
public class SKCEException extends Exception{

    /**
     * Different types of constructors
     */
    public SKCEException() {
        super();
    }

    public SKCEException(String message) {
        super(message);
    }

    public SKCEException(Exception e) {
        super(e);
    }
}
