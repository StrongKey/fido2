/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.utilities;

import com.strongkey.poc.SKFSClient;
import java.io.StringReader;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.stream.JsonParsingException;
import javax.xml.ws.WebServiceException;

public class Common {
    private static final String CLASSNAME = Common.class.getName();
    public static final ConcurrentHashMap<Integer, String> didRPID = new ConcurrentHashMap<>();

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
    
    public static void getRPIDFromPreRegResponse(String responseString, String policy) {
        JsonReader jsonReader = Json.createReader(new StringReader(responseString));
        JsonObject data = jsonReader.readObject();
        JsonObject preRegResponse = data.getJsonObject("Response");
        JsonObject RP = preRegResponse.getJsonObject("rp");
        String id = RP.getString("id");
//        System.out.println("RP ID: " + id);
        didRPID.putIfAbsent(SKFSClient.getDid(policy), id);
    }

    public static void getRPIDFromPreAuthenticateResponse(String responseString, String policy) {
        JsonReader jsonReader = Json.createReader(new StringReader(responseString));
        JsonObject data = jsonReader.readObject();
        JsonObject preAuthResponse = data.getJsonObject("Response");
        String id = preAuthResponse.getString("rpId");
//        System.out.println("RP ID: " + id);
        didRPID.putIfAbsent(SKFSClient.getDid(policy), id);
    }
    
    public static int checkNumberOfKeysInPreAuthResponse(String responseString) {

        JsonReader jsonReader = Json.createReader(new StringReader(responseString));
        JsonObject data = jsonReader.readObject();
        JsonObject preAuthResponse = data.getJsonObject("Response");
        JsonArray credentials = preAuthResponse.getJsonArray("allowCredentials");
        System.out.println("Credentials Size: " + credentials.size());

        return credentials.size();
    }
}
