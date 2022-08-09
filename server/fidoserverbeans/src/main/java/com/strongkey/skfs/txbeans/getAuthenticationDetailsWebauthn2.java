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
public class getAuthenticationDetailsWebauthn2 implements getAuthenticationDetailsLocal {

    @Override
    public JsonObject execute(String id, String rawId, String type, String userhandle, String signature, JsonObject cdj, FIDO2AuthenticatorData authData) {
        JsonObjectBuilder job = Json.createObjectBuilder();

        job.add(SKFSConstants.JSON_RESPONSE_DETAIL_FORMAT,"webauthn2");
        // add id, rawid, and type and optionally attachment
        job.addNull(SKFSConstants.JSON_KEY_CLIENT_EXTENSION_RESULTS);
        if (id != null) {
            job.add(SKFSConstants.JSON_KEY_ID, id);

        }
        job.add(SKFSConstants.JSON_KEY_RAW_ID, rawId);
        
        JsonObjectBuilder respobj = Json.createObjectBuilder();
        respobj.add(SKFSConstants.JSON_KEY_AUTHENTICATORDATA, authData.toJson());
        respobj.add("clientDataJson", cdj);
        respobj.add(SKFSConstants.JSON_KEY_SIGNATURE, signature);
        respobj.add(SKFSConstants.JSON_KEY_USERHANDLE, userhandle);

        job.add(SKFSConstants.JSON_KEY_SERVLET_INPUT_RESPONSE, respobj.build());
        if (type != null) {
            job.add(SKFSConstants.JSON_KEY_REQUEST_TYPE, type);
        }
        return job.build();
    }
}
