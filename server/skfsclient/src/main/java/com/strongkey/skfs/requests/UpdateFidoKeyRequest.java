/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.strongkey.skfsclient.common.Payload;
import com.strongkey.skfsclient.common.SVCInfo;

/*
* TODO:
* Decide whether to keep Payload and SVCInfo as separate classes, make them an inner class, or not have them at all.
*/

@JsonInclude(Include.NON_NULL)
public class UpdateFidoKeyRequest {

    private SVCInfo svcinfo;
    private Payload payload;

    public UpdateFidoKeyRequest() {
        svcinfo = new SVCInfo();
        payload = new Payload();
    }

    public SVCInfo getSVCInfo() {
        return svcinfo;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setStatus(String status) {
        payload.setStatus(status);
    }
    public void setModifyLocation(String modify_location) {
        payload.setModify_location(modify_location);
    }
    public void setDisplayname(String displayname) {
        payload.setDisplayname(displayname);
    }
    public void setKeyid(String keyid) {
        payload.setKeyid(keyid);
    }

}
