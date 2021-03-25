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
public class PatchFidoPolicyRequest {

    private Long startDate;
    private Long endDate;
    private String Policy;
    private Integer version;
    private String status;
    private String notes;

   
    
    public Long getStartDate() {
        return startDate;
    }

    public void setStartDate(Long startDate) {
        this.startDate = startDate;
    }

    public Long getEndDate() {
        return endDate;
    }

    public void setEndDate(Long endDate) {
        this.endDate = endDate;
    }

    public String getPolicy() {
        return Policy;
    }

    public void setPolicy(String Policy) {
        this.Policy = Policy;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
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
        if (this.startDate != null) {
            job.add("startDate", startDate);
        }
        if (this.endDate != null) {
            job.add("endDate", endDate);
        }
        if (this.Policy != null) {
            job.add("Policy", Policy);
        }
        if (this.version != null) {
            job.add("version", version);
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
