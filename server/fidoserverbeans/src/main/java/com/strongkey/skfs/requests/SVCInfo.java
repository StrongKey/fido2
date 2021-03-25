/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.skfs.requests;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


/**
 *
 * @author pleung
 */
public class SVCInfo {
    
    private int did;
    private String protocol;
    private String authtype;
    private String svcusername;
    private String svcpassword;
//    private String strongkey-api-version;
//    private String strongkey-content-sha256;
//    private String authorization;
//    private String timestamp;
    
    public SVCInfo() {
        
    }

    public int getDid() {
        return did;
    }

    public void setDid(int did) {
        this.did = did;
    }
    
    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
    
    public String getAuthtype() {
        return authtype;
    }

    public void setAuthtype(String authtype) {
        this.authtype = authtype;
    }
    
    public String getSVCUsername() {
        return svcusername;
    }

    public void setSVCUsername(String svcusername) {
        this.svcusername = svcusername;
    }

    public String getSVCPassword() {
        return svcpassword;
    }

    public void setSVCPassword(String svcpassword) {
        this.svcpassword = svcpassword;
    }
    
    public JsonObject toJsonObject(){
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("did", this.did);
        job.add("protocol", this.protocol);
        job.add("authtype", this.authtype);
        if(this.svcusername != null){
            job.add("svcusername", this.svcusername);
        }
        if(this.svcpassword != null){
            job.add("svcpassword", this.svcpassword);
        }
        return job.build();
    }
//    public String getTimestamp() {
//        return timestamp;
//    }
//
//    public void setTimestamp(String timestamp) {
//        this.timestamp = timestamp;
//    }
//
//    public String getAuthorization() {
//        return Authorization;
//    }
//
//    public void setAuthorization(String Authorization) {
//        this.Authorization = Authorization;
//    }
//
//    public String getContentSHA256() {
//        return contentSHA256;
//    }
//
//    public void setContentSHA256(String contentSHA256) {
//        this.contentSHA256 = contentSHA256;
//    }
//
//    public String getStrongkeyAPIversion() {
//        return strongkeyAPIversion;
//    }
//
//    public void setStrongkeyAPIversion(String strongkeyAPIversion) {
//        this.strongkeyAPIversion = strongkeyAPIversion;
//    }

}
