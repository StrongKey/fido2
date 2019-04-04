/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.apiws.fido2.rest;

import javax.json.Json;

public class FIDOResponse {
    private final String response;
    private final String message;
    private final String error;
    
    public FIDOResponse(String response, String message, String error){
        this.response = response;
        this.message = message;
        this.error = error;
    }
    
    @Override
    public String toString(){
        return Json.createObjectBuilder()
                .add("Response", response)
                .add("Message", message)
                .add("Error", error).build().toString();
    }
}