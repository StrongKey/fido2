/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.requests;

public class PatchFidoKeyRequest {

    private String status;
    private String modify_location;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getModify_location() {
        return modify_location;
    }

    public void setModify_location(String modify_location) {
        this.modify_location = modify_location;
    }

}
