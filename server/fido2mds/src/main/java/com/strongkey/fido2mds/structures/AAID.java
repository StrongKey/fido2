/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class AAID {
    private String AAID;

    public AAID(String aaid){
        this.AAID = aaid;
    }
    
    public String getAAID() {
        return AAID;
    }

    public void setAAID(String aaid) {
        this.AAID = aaid;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.AAID != null) job.add("AAID", AAID);
        return job.build();
    }
}
