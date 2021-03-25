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
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.AAGUID != null) job.add("AAGUID", AAGUID);
        return job.build();
    }
}
