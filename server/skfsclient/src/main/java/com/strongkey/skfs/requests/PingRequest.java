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
public class PingRequest {

    private SVCInfo svcinfo;
    private String payload;

    public PingRequest() {
        svcinfo = new SVCInfo();
        payload = "";
    }

    public SVCInfo getSVCInfo() {
        return svcinfo;
    }

    public String getPayload() {
        return payload;
    }
}
