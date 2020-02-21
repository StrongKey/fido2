/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.requests;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class ServiceInfo {

    private Long did;
    private String protocol;
    private String svcusername;
    private String svcpassword;
    private String authtype;
    private Long timestamp;
    private String authorization;
    private final String contentType = "application/json";
    private String requestURI ="/skfs/soap?wsdl";
    private String strongkeyAPIversion;
    private String contentSHA256;

    public String getContentType() {
        return contentType;
    }

    public String getRequestURI() {
        return requestURI;
    }


    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Long getDid() {
        return did;
    }

    public void setDid(Long did) {
        this.did = did;
    }

    public String getSvcusername() {
        return svcusername;
    }

    public void setSvcusername(String svcusername) {
        this.svcusername = svcusername;
    }

    public String getSvcpassword() {
        return svcpassword;
    }

    public void setSvcpassword(String svcpassword) {
        this.svcpassword = svcpassword;
    }

    public String getAuthtype() {
        return authtype;
    }

    public void setAuthtype(String authtype) {
        this.authtype = authtype;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getStrongkeyAPIversion() {
        return strongkeyAPIversion;
    }

    public void setStrongkeyAPIversion(String strongkeyAPIversion) {
        this.strongkeyAPIversion = strongkeyAPIversion;
    }

    public String getContentSHA256() {
        return contentSHA256;
    }

    public void setContentSHA256(String contentSHA256) {
        this.contentSHA256 = contentSHA256;
    }


}
