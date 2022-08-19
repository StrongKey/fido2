/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2022 StrongAuth, Inc.
 *
 * $Date: $
 * $Revision: $
 * $Author: $
 * $URL: $
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.fido2.FIDO2AuthenticatorData;
import com.strongkey.skfs.utilities.SKFSConstants;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;


@Stateless
public class getAuthenticationDetailsDefault implements getAuthenticationDetailsLocal {

    @Override
    public JsonObject execute(String id, String rawId, String type, String userhandle, String signature, JsonObject cdj, FIDO2AuthenticatorData authData) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        job.add(SKFSConstants.JSON_RESPONSE_DETAIL_FORMAT,"default");
        // add id, rawid, and type and optionally attachment
        if (id != null) {
            job.add("credentialId", id);
        }
        if (type != null) {
            job.add(SKFSConstants.JSON_KEY_REQUEST_TYPE, type);
        }
        job.add("clientDataJson", cdj);
        job.add(SKFSConstants.JSON_KEY_AUTHENTICATORDATA, authData.toJson());
        job.add(SKFSConstants.JSON_KEY_SIGNATURE, signature);
        if(!userhandle.trim().isEmpty()){
            job.add(SKFSConstants.JSON_KEY_USERHANDLE, userhandle);
        }
        

        
        return job.build();
    }
}
