/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class MetadataTOCPayload {
    private String legalHeader;
    private BigInteger no;
    private String nextUpdate;
    private List<MetadataTOCPayloadEntry> entries;
    
    public MetadataTOCPayload(JsonObject jsonInput) {
        JsonArray jsonArray;
        if (jsonInput.containsKey("legalHeader")) legalHeader = jsonInput.getString("legalHeader");
        if (jsonInput.containsKey("no")) no = jsonInput.getJsonNumber("no").bigIntegerValueExact();
        if (jsonInput.containsKey("nextUpdate")) nextUpdate = jsonInput.getString("nextUpdate");
        if (jsonInput.containsKey("entries")) {
            entries = new ArrayList<MetadataTOCPayloadEntry>();
            jsonArray = jsonInput.getJsonArray("entries");
            if (jsonArray != null) {
                int len = jsonArray.size();
                for (int i = 0; i < len; i++) {
                    entries.add(new MetadataTOCPayloadEntry(jsonArray.getJsonObject(i)));
                }
            }
        }
    }

    public String getLegalHeader() {
        return legalHeader;
    }

    public void setLegalHeader(String legalHeader) {
        this.legalHeader = legalHeader;
    }

    public BigInteger getNo() {
        return no;
    }

    public void setNo(BigInteger no) {
        this.no = no;
    }

    public String getNextUpdate() {
        return nextUpdate;
    }

    public void setNextUpdate(String nextUpdate) {
        this.nextUpdate = nextUpdate;
    }

    public List<MetadataTOCPayloadEntry> getEntries() {
        return entries;
    }

    public void setEntries(List<MetadataTOCPayloadEntry> entries) {
        this.entries = entries;
    }
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.legalHeader != null) job.add("legalHeader", legalHeader);
        if (this.no != null) job.add("no", no);
        if (this.nextUpdate != null) job.add("nextUpdate", nextUpdate);
        if (this.entries != null) {
            JsonArrayBuilder jab = Json.createArrayBuilder();
            entries.stream().forEach(entry -> jab.add(entry.toJsonObject()));
            job.add("entries", jab);
        }
        return job.build();
    }
}
