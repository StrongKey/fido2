/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.requests;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class CreateFidoPolicyRequest {


    private String Policy;
    private String status;
    private String notes;

    public String getPolicy() {
        return Policy;
    }

    public void setPolicy(String Policy) {
        this.Policy = Policy;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public JsonObject toJsonObject() {

        JsonObjectBuilder job = Json.createObjectBuilder();

        if (this.Policy != null) {
            job.add("Policy", Policy);
        }
        if (this.status != null) {
            job.add("status", status);
        }
        if (this.notes != null) {
            job.add("notes", notes);
        }
        return job.build();
    }

}
