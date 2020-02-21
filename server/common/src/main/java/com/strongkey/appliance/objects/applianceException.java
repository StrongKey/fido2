/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.objects;

import com.strongkey.appliance.utilities.applianceCommon;
import java.io.IOException;


public class applianceException extends Exception{

    private static final long serialVersionUID = 1L;

    protected String message;

    public applianceException(String argument) {
        this.message = argument;
    }

    applianceException(IOException e) {
        throw new UnsupportedOperationException(applianceCommon.getMessageProperty("APPL-ERR-1000") + "Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    /**
     * Gets the value of the message property.
     *
     * @return
     *     possible object is
     *     {@link String }
     *
     */
    @Override
    public String getMessage() {
        return message;
    }

    /**
     * Sets the value of the message property.
     *
     * @param value
     *     allowed object is
     *     {@link String }
     *
     */
    public void setMessage(String value) {
        this.message = value;
    }
}
