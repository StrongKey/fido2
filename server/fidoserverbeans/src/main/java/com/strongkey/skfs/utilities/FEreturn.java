/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.utilities;

import java.io.Serializable;
import java.io.StringReader;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.json.stream.JsonParserFactory;

public class FEreturn implements Serializable {

    private String jsonResponse = null;
    private Object response = null;
    private String logmsg = "";

    public void setJsonResponse(String jsonResponse) {
        this.jsonResponse = jsonResponse;
    }

    public String getJsonResponse() {
        return jsonResponse;
    }

    public Object getResponse() {
        return response;
    }

    public void setResponse(Object response) {
        this.response = response;
    }

    public String getLogmsg() {
        return logmsg;
    }

    public void append(String msg) {

        if (logmsg == null) {
            logmsg = "\n" + msg + "\n";
        } else {
            logmsg += (msg + "\n");
        }

    }

    /**
     * Clean-up method to make sure RV's are correct
     */
    public void cleanUp()
    {
        this.logmsg = "";
        this.jsonResponse = null;
        this.response = null;
    }

    /**
     * Returns a pretty Json
     * @param Input
     * @return
     */
    public String returnJSON(String Input) {
        String JSON_FORMAT_STRING = "%-10s";
        JsonParserFactory factory = Json.createParserFactory(null);

        StringBuilder sb;
        try (JsonParser parser = factory.createParser(new StringReader(Input))) {
            sb = new StringBuilder();
            sb.append("\n\t{\n");
            while (parser.hasNext()) {
                JsonParser.Event event = parser.next();

                switch (event) {
                    case KEY_NAME: {
                        sb.append("\t\t\"");
                        sb.append(String.format(JSON_FORMAT_STRING, parser.getString()));
                        sb.append("\" : \"");
                        break;
                    }
                    case VALUE_STRING: {
                        sb.append(parser.getString()).append("\",\n");
                        break;
                    }
                    default:
                        break;
                }
            }   sb.append("\t}");
        }

        return sb.toString();
    }
}
