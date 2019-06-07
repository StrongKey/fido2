/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */
package com.strongkey.utilities;

import java.io.StringReader;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.xml.ws.WebServiceException;

public class Common {
    private static final String CLASSNAME = Common.class.getName();
    
    // Verify that SKFE response is the proper format
    public static JsonObject parseJsonFromString(String responseJsonString){
        try (JsonReader jsonReader = Json.createReader(new StringReader(responseJsonString))) {
            return jsonReader.readObject();
        }
        catch(JsonParsingException ex){
            POCLogger.logp(Level.SEVERE, CLASSNAME, "verifyJson",
                    "POC-ERR-5001", ex.getLocalizedMessage());
            throw new WebServiceException(POCLogger.getMessageProperty("POC-ERR-5001"));
        }
    }
}
