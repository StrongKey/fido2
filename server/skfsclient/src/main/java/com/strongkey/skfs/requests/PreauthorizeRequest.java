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

public class PreauthorizeRequest {

    private SVCInfo svcinfo;
    private Payload payload;
//    private Transaction transaction;

    public PreauthorizeRequest() {
        svcinfo = new SVCInfo();
        payload = new Payload();
//        transaction = new Transaction();
    }

    public SVCInfo getSVCInfo() {
        return svcinfo;
    }

    public Payload getPayload() {
        return payload;
    }
    
//    public Transaction gettx() {
//        return transaction;
//    }

    public void setUsername(String username) {
        payload.setUsername(username);
    }

    public void setOptions(JsonObject options) {
        payload.setOptions(options);
    }

    public void setTxpayload(String txpayload) {
        payload.setTxpayload(txpayload);
    }
    
    public void setTxid(String txId) {
        payload.setTxid(txId);
    }

    public JsonObject toJsonObject(){
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("svcinfo", svcinfo.toJsonObject());
        job.add("payload", payload.toJsonObject());
//        job.add("transaction", transaction.toJsonObject());
        return job.build();
    }
}
