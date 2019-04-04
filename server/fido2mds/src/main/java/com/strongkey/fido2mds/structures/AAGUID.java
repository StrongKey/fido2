/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

public class AAGUID {
    private String AAGUID;
    
    public AAGUID(String aaguid){
        this.AAGUID = aaguid;
    }

    public String getAAGUID() {
        return AAGUID;
    }

    public void setAAGUID(String AAGUID) {
        this.AAGUID = AAGUID;
    }
}
