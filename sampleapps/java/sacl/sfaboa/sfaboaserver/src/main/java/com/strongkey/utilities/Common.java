/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
            SFABOALogger.logp(Level.SEVERE, CLASSNAME, "verifyJson",
                    "SFABOA-ERR-5001", ex.getLocalizedMessage());
            throw new WebServiceException(SFABOALogger.getMessageProperty("SFABOA-ERR-5001"));
        }
    }
}
