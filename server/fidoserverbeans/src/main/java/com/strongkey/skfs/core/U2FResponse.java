/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.core;

import java.io.Serializable;

/**
 * Super class for U2F responses that comes back to the FIDO server from the
 * RP application.
 */
public class U2FResponse implements Serializable {

    /**
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    /**
     * Supported versions for U2F protocol
     */
    final static String U2F_VERSION_V2 = "U2F_V2";

    /**
     * Generic attributes
     */
    String browserdata;

    /**
     * Class internal use only
     */
    BrowserData bd;

    /**
     * Constructor - Does not need to do anything at this point. Verification is
     * mostly process (registration or authentication) specific.
     * Additionally,
     * an empty constructor since this class implements java.io.Serializable
     */
    protected U2FResponse() {
    }
}
