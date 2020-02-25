/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */
package com.strongkey.skfs.utilities;

import javax.ejb.ApplicationException;

@ApplicationException(rollback = true)
public class SKIllegalArgumentException extends RuntimeException {

    /**
     * Different types of constructors
     */
    public SKIllegalArgumentException() {
        super();
    }

    public SKIllegalArgumentException(String message) {
        super(message);
    }

    public SKIllegalArgumentException(Exception e) {
        super(e);
    }
}
