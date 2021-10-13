/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * ClientData object - data sent from the Relying Party web-application to
 * the client application - usually, the browser; see W3C page for reference
 * https://www.w3.org/TR/webauthn/#sec-client-data
 */

package com.strongauth.skfs.fido2.artifacts;

import javax.json.Json;
import javax.json.JsonObject;

public class ClientData {
    String type;
    String challenge;
    String origin;
    Boolean crossOrigin;

    public ClientData(String type, String challenge, String origin, Boolean co) {
        this.type = type;
        this.challenge = challenge;
        this.origin = origin;
        this.crossOrigin = co;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getOrigin() {
        return origin;
    }

    public void setOrigin(String origin) {
        this.origin = origin;
    }

    public JsonObject toJson(){
        JsonObject clientData = Json.createObjectBuilder()
                .add("type", type)
                .add("challenge", challenge)
                .add("origin", origin)
                .add("crossOrigin", crossOrigin)
                .build();
        return clientData;
    }

    public String toJsonString(){
        return toJson().toString();
    }
}
