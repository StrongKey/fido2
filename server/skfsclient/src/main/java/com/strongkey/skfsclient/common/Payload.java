/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.skfsclient.common;

import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author pleung
 */
public class Payload {

    private String username;
    private String oldusername;
    private String newusername;
    private String status;
    private String modify_location;
    private String displayname;
    private JsonObject options;
    private String extensions;
    private JsonObject response;
    private JsonObject metadata;
    private String keyid;
    private JsonArray configurations;
    private String txid;
    private String txpayload;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayname() {
        return displayname;
    }

    public void setDisplayname(String displayname) {
        this.displayname = displayname;
    }

    public JsonObject getOptions() {
        return options;
    }

    public void setOptions(JsonObject options) {
        this.options = options;
    }

    public String getExtensions() {
        return extensions;
    }

    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    public JsonObject getResponse() {
        return response;
    }

    public void setResponse(JsonObject response) {
        this.response = response;
    }

    public JsonObject getMetadata() {
        return metadata;
    }
    
    public void setMetadata(JsonObject metadata) {
        this.metadata = metadata;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getKeyid() {
        return keyid;
    }

    public void setKeyid(String keyid) {
        this.keyid = keyid;
    }

    public String getModify_location() {
        return modify_location;
    }

    public void setModify_location(String modify_location) {
        this.modify_location = modify_location;
    }

    public JsonArray getConfigurations() {
        return configurations;
    }

    public void setConfigurations(JsonArray configurations) {
        this.configurations = configurations;
    }

    public String getTxid() {
        return txid;
    }

    public void setTxid(String txid) {
        this.txid = txid;
    }

    public String getTxpayload() {
        return txpayload;
    }

    public void setTxpayload(String txpayload) {
        this.txpayload = txpayload;
    }

    public String getOldusername() {
        return oldusername;
    }

    public void setOldusername(String oldusername) {
        this.oldusername = oldusername;
    }

    public String getNewusername() {
        return newusername;
    }

    public void setNewusername(String newusername) {
        this.newusername = newusername;
    }
    
    
    public JsonObject toJsonObject() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (this.username != null) {
            job.add("username", username);
        }
        if (this.oldusername != null) {
            job.add("oldusername", oldusername);
        }
        if (this.newusername != null) {
            job.add("newusername", newusername);
        }
        if (this.txid != null) {
            job.add("txid", txid);
        }
        if (this.txpayload != null) {
            job.add("txpayload", txpayload);
        }
        if (this.status != null) {
            job.add("status", status);
        }
        if (this.modify_location != null) {
            job.add("modify_location", modify_location);
        }
        if (this.displayname != null) {
            job.add("displayname", displayname);
        }
        if (this.options != null) {
            job.add("options", options);
        }
        if (this.extensions != null) {
            job.add("extensions", extensions);
        }
        if (this.response != null) {
            job.add("publicKeyCredential", response);
        }
        if (this.metadata != null) {
            job.add("strongkeyMetadata", metadata);
        }
        if (this.keyid != null) {
            job.add("keyid", keyid);
        }
        if (this.configurations != null) {
            job.add("configurations", configurations);
        }
        return job.build();
    }
}