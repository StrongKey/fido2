/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.skfsclient.common;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 *
 * @author pleung
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Payload {

    private String username;
    private String status;
    private String modify_location;
    private String displayname;
    private String options;
    private String extensions;
    private String response;
    private String metadata;
    private String keyid;

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

    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    public String getExtensions() {
        return extensions;
    }

    public void setExtensions(String extensions) {
        this.extensions = extensions;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMetadata() {
        return metadata;
    }
    
    public void setMetadata(String metadata) {
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
    
}