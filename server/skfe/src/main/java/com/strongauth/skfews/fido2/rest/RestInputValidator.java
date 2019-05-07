/*
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
 * Copyright (c) 2001-2018 StrongAuth, Inc.
 *
 * $Date: 
 * $Revision:
 * $Author: mishimoto $
 * $URL: 
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
 *
 *
 */
package com.strongauth.skfews.fido2.rest;

import java.io.IOException;
import java.io.InputStream;
import org.everit.json.schema.Schema;
import org.everit.json.schema.loader.SchemaClient;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class RestInputValidator {
    public static final RestInputValidator DEFAULT = new RestInputValidator();
    private static Schema svcinfoSchema;
    private static Schema createPayloadSchema;
    private static Schema readPayloadSchema;
    private static Schema updatePayloadSchema;
    private static Schema deletePayloadSchema;
    
    private RestInputValidator(){
        InputStream inputStream = null;
        try{
            inputStream = getClass().getClassLoader().getResourceAsStream("resources/fido2adminschema.json");
            if(inputStream == null){
                System.out.println("Unable to find JSON schema: Unable to find fido2adminschema.json");
            }
            JSONObject schemas = new JSONObject(new JSONTokener(inputStream));
            SchemaResourceClient client = new SchemaResourceClient();
            System.out.println("svcinfo schema: " + schemas.getJSONObject("svcinfo").toString());
            svcinfoSchema = SchemaLoader.load(schemas.getJSONObject("svcinfo"), client);
            createPayloadSchema = SchemaLoader.load(schemas.getJSONObject("createpayload"), client);
            readPayloadSchema = SchemaLoader.load(schemas.getJSONObject("readpayload"), client);
            updatePayloadSchema = SchemaLoader.load(schemas.getJSONObject("updatepayload"), client);
            deletePayloadSchema = SchemaLoader.load(schemas.getJSONObject("deletepayload"), client);
        }
        catch (JSONException ex) {
            System.out.println("Unable to find JSON schema: " + ex);
        }
        finally{
            if(inputStream != null){
                try{
                    inputStream.close();
                } catch (IOException ex) {
                    //Do nothing
                }
            }
        }
    }
    
    public void validateSVCInfoExistance(JSONObject svcInfo){
        svcinfoSchema.validate(svcInfo);
    }

    public void validateCreatePayloadExistance(JSONObject payload){
        createPayloadSchema.validate(payload);
    }

    public void validateReadPayloadExistance(JSONObject payload){
        readPayloadSchema.validate(payload);
    }

    public void validateUpdatePayloadExistance(JSONObject payload){
        updatePayloadSchema.validate(payload);
    }

    public void validateDeletePayloadExistance(JSONObject payload){
        deletePayloadSchema.validate(payload);
    }
    
    //Looks up references to other schemas in resources directory.
    private class SchemaResourceClient implements SchemaClient {
        @Override
        public InputStream get(String url) {
            return getClass().getClassLoader().getResourceAsStream(url);
        }

        @Override
        public InputStream apply(String url) {
            return get(url);
        }
    }
}