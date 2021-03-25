/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
package com.strongkey.skfs.pojos;

import com.strongkey.skfs.utilities.SKFSConstants;
import javax.json.Json;
import javax.json.JsonObject;

public class FIDOReturnObjectV1 {

    /**
     * Local variables
     */
    private String method = "";
    private JsonObject challenge = null;
    private String response = "";
    private String message = "";
    private String error = "";

    /**
     * Constructor of this class.
     *
     * @param method
     * @param challenge
     * @param response
     * @param message
     * @param error
     */
    public FIDOReturnObjectV1(String method, JsonObject challenge, String response, String message, String error) {

        if (method != null) {
            this.method = method;
        }

        if (challenge != null) {
            this.challenge = challenge;
        }

        if (response != null) {
            this.response = response;
        }

        if (message != null) {
            this.message = message;
        }

        if (error != null) {
            this.error = error;
        }
    }

    /**
     * Get and Set methods
     *
     * @return
     */
    public JsonObject getChallenge() {
        return challenge;
    }

    public void setChallenge(JsonObject challenge) {
        this.challenge = challenge;
    }

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    /**
     * Over-ridden toString method to print the object content in a readable
     * manner
     *
     * @return String with object content laid in a readable manner.
     */
    @Override
    public String toString() {
        return "\n\tchallenge   = " + this.challenge
                + "\n\tresponse    = " + this.response
                + "\n\tmessage     = " + this.message
                + "\n\terror       = " + this.error;
    }

    /**
     * Constructs this class object as a Json to be passed back to the client.
     *
     * @return - String object of the Json representation of this object
     */
    public String toJsonString() {

        if (message == null) {
            message = "";
        }

        if (error == null) {
            error = "";
        }

        if (response == null) {
            response = "";
        }

        //  Convert the message string into html compatible format.
        message = message.replaceAll("\"", "").replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
        error = error.replaceAll("\"", "").replaceAll("\n", "<br>").replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");

        // Build the output json object
        JsonObject responseJSON = null;

        if (method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_PREREGISTER)
                || method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_PREAUTH)) {
            if (challenge == null) {
                responseJSON = Json.createObjectBuilder()
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_CHALLENGE, "")
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_MESSAGE, message)
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_ERROR, error).
                        build();
            } else {
                responseJSON = Json.createObjectBuilder()
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_CHALLENGE, challenge)
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_MESSAGE, message)
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_ERROR, error).
                        build();
            }
        } else if (method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_REGISTER)
                || method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_AUTHENTICATE)
                || method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_DEREGISTER)
                || method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_DEACTIVATE)
                || method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_ACTIVATE)) {
            responseJSON = Json.createObjectBuilder()
                    .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, response)
                    .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_MESSAGE, message)
                    .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_ERROR, error).
                    build();
        } else if (method.equalsIgnoreCase(SKFSConstants.FIDO_METHOD_GETKEYSINFO)) {
            if (challenge == null) {
                responseJSON = Json.createObjectBuilder()
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, "")
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_MESSAGE, message)
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_ERROR, error).
                        build();
            } else {
                responseJSON = Json.createObjectBuilder()
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, challenge)
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_MESSAGE, message)
                        .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_ERROR, error).
                        build();
            }
        }

        if (responseJSON != null) {
            return responseJSON.toString();
        } else {
            return null;
        }
    }

}
