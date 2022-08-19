/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation, version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301  USA
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * Verifies the user making the FIDO service request. Every request
 * must have the "sfaFidoServiceInput" Json sub-object with a "payload"
 * that follows it (if appropriate) that looks like this:
 *
 * {
 *   "sfaFidoServiceInput": {
 *     "did": 1,
 *     "service": "SACL_FIDO_SERVICE_GET_FIDO_REGISTRATION_CHALLENGE",
 *     "sfaCredentials": {
 *       "uid": 87
 *      },
 *      "payload": {
 *        ....
 *      }
 *   }
 * }
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.Users;
import com.strongkey.sfaeco.utilities.Common;
import com.strongkey.sfaeco.utilities.Constants;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;

@Stateless
public class verifyFidoServiceRequest implements verifyFidoServiceRequestLocal {

    // Resources used by this bean
    @EJB private getUserLocal                       getuser;

    private final short sid = Common.getSid();
    private final String classname = "verifyFidoServiceRequest";

    /**
     * Verifies content in the FIDO service request
     * @param did short Domain ID
     * @param fidoinput JsonObject containing the input from the app
     * @param txid String for logging
     * @return JsonObject with the payload for preregister (for now)
     */
    @Override
    public JsonObject execute(short did, JsonObject fidoinput, String txid) {

        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", classname, String.valueOf(did), txid);

        // Get necessary objects for verification out of Json input
        JsonObject sfacred = fidoinput.getJsonObject(Constants.JSON_KEY_SACL_CREDENTIALS);
        Long uid = sfacred.getJsonNumber(Constants.JSON_KEY_SACL_FIDO_UID).longValue();

        // Get the UserDevice
        Users user = getuser.byUid(did, uid, txid);
        if (user == null) {
            Common.log(Level.WARNING, "SFAECO-ERR-1023", "User: " + did+"-"+uid);
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
            return Common.jsonError(classname, "execute", "SFAECO-ERR-1023", "User: " + did+"-"+uid);
        }

        // Found it - check user's status for "Active or Registered"
        String status = user.getStatus();
        if (!status.equals(Constants.STATUS_ACTIVE)) {
            if (!status.equals(Constants.STATUS_REGISTERED)) {
                Common.log(Level.INFO, "SFAECO-ERR-1024", "User Status: " + did+"-"+uid+"-"+status);
                Common.exitLog(Level.INFO, "SFAECO-MSG-3004", classname, String.valueOf(did), txid, timein);
                return Common.jsonError(classname, "execute", "SFAECO-ERR-1024", "User Status: " + did+"-"+uid+"-"+status);
            }
        }
        Common.log(Level.INFO, "SFAECO-MSG-2021", "User: " +did+"-"+uid);

        // Need to return some information for use by servlet and other EJBs
        return Json.createObjectBuilder()
                .add(Constants.VERIFIED_LABEL, true)
                .add(Constants.JSON_KEY_UID, user.getUsersPK().getUid())
                .add(Constants.JSON_KEY_FIDO_PAYLOAD_USERNAME, user.getUsername())
                .add(Constants.JSON_KEY_FIDO_PAYLOAD_DISPLAY_NAME, user.getGivenName() + " " + user.getFamilyName())
                .build();
    }
}
