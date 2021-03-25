/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.requests;

import com.strongkey.skfsclient.common.Payload;
import com.strongkey.skfsclient.common.SVCInfo;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/*
* TODO:
* Decide whether to keep Payload and SVCInfo as separate classes, make them an inner class, or not have them at all.
 */
public class DeregisterRequest {

    private SVCInfo svcinfo;
    private Payload payload;

    public DeregisterRequest() {
        svcinfo = new SVCInfo();
        payload = new Payload();
    }

    public SVCInfo getSVCInfo() {
        return svcinfo;
    }

    public Payload getPayload() {
        return payload;
    }

    public void setKeyid(String keyid) {
        payload.setKeyid(keyid);
    }

    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("svcinfo", svcinfo.toJsonObject());
        job.add("payload", payload.toJsonObject());
        return job.build();
    }
}
