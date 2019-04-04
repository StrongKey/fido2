/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.apiws.fido2.rest;

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