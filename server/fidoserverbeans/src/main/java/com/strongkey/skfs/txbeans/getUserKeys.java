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
 * Copyright (c) 2001-2021 StrongAuth, Inc.
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

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Base64;
import java.util.Collection;
import java.util.Iterator;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;

@Stateless
public class getUserKeys implements getUserKeysLocal {

    /**
     ** This class's name - used for logging & not persisted
     *
     */
    private final String classname = this.getClass().getName();

    @EJB
    getFidoKeysLocal getkeybean;

    @Override
    public Response execute(Long did, JsonArray usernames) {
        //  input checks
        if (did == null || did < 1) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " did=" + did);
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " did").build();
        }

        for (int i = 0; i < usernames.size(); i++) {
            String username = usernames.getString(i);
            if (username == null || username.isEmpty()) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0002", " username=" + username);
                SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " username").build();
            }

            if (username.trim().length() > Integer.parseInt(applianceCommon.getApplianceConfigurationProperty("appliance.cfg.maxlen.256charstring"))) {
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0027", " username should be limited to 256 characters");
                SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
                return Response.status(Response.Status.BAD_REQUEST).entity(SKFSCommon.getMessageProperty("FIDO-ERR-0002") + " username").build();
            }
        }

        JsonArrayBuilder userkeysArrayBuilder = Json.createArrayBuilder();
        JsonArrayBuilder keysArrayBuilder ;
        try {
            for (int i = 0; i < usernames.size(); i++) {
                String username = usernames.getString(i);
                keysArrayBuilder = Json.createArrayBuilder();
                Collection<FidoKeys> kh_coll = getkeybean.getByUsername(did, username);
                if (kh_coll != null) {
                    Iterator it = kh_coll.iterator();

                    //  Initialize a map to store the randomid to the regkeyid
//                Map<String, String> userkeypointerMap = new ConcurrentSkipListMap<>();
                    //  for every key registered,
                    while (it.hasNext()) {
                        FidoKeys key = (FidoKeys) it.next();
                        if (key != null) {
                            if (!key.getStatus().equalsIgnoreCase(applianceConstants.DELETED)) {
                                //  Create a json object out of this key information
                                String mapkey = key.getFidoKeysPK().getSid() + "-" + key.getFidoKeysPK().getDid() + "-" + key.getFidoKeysPK().getFkid();
                                FidoKeysInfo fkinfoObj = new FidoKeysInfo(key);
                                skceMaps.getMapObj().put(SKFSConstants.MAP_FIDO_KEYS, mapkey, fkinfoObj);
                                long modifytime = 0L;
                                if (key.getModifyDate() != null) {
                                    modifytime = key.getModifyDate().getTime();
                                }

                                String modifyloc = "Not used yet";
                                if (key.getModifyLocation() != null) {
                                    modifyloc = key.getModifyLocation();
                                }

                                //  Generate a unique randomid for this key to be user
                                //  as a pointer for the key data base index.
                                String randomid = key.getFidoKeysPK().getSid() + "-" + key.getFidoKeysPK().getDid() + "-" + key.getFidoKeysPK().getFkid();
//                        String time_to_live = SKFSCommon.getConfigurationProperty("skfs.cfg.property.userkeypointers.flush.cutofftime.seconds");
//                        if (time_to_live == null || time_to_live.isEmpty()) {
//                            time_to_live = "300";
//                        }

                                String regSettings = key.getRegistrationSettings();
                                JsonObjectBuilder keyJsonBuilder = Json.createObjectBuilder()
                                        .add("keyid", randomid)
                                        //                                .add("randomid_ttl_seconds", time_to_live)
                                        .add("fidoProtocol", key.getFidoProtocol())
                                        //                                .add("fidoVersion", key.getFidoVersion())
                                        .add("credentialId", key.getKeyhandle())
                                        .add("createLocation", key.getCreateLocation())
                                        .add("createDate", key.getCreateDate().getTime())
                                        .add("lastusedLocation", modifyloc)
                                        .add("modifyDate", modifytime)
                                        .add("status", key.getStatus());
                                if (regSettings != null) {
                                    byte[] regSettingsBytes = Base64.getUrlDecoder().decode(regSettings);
                                    String regSettingsString = new String(regSettingsBytes, "UTF-8");
                                    String displayName = SKFSCommon.getJsonObjectFromString(regSettingsString).getString("DISPLAYNAME");
                                    if (displayName != null) {
                                        keyJsonBuilder.add("displayName", displayName);
                                    }
                                    String attestationFormat = SKFSCommon.getJsonObjectFromString(regSettingsString).getString("attestationFormat");
                                    if (displayName != null) {
                                        keyJsonBuilder.add("attestationFormat", attestationFormat);
                                    }
                                }
                                if (SKFSCommon.getConfigurationProperty(did, "skfs.cfg.property.return.MDS").equalsIgnoreCase("true")) {
                                    if (SKFSCommon.containsMDSWSList("G")) {
                                        if (SKFSCommon.containsMdsentry(key.getAaguid())) {
                                            keyJsonBuilder.add("mdsEntry", SKFSCommon.getMdsentryfromMap(key.getAaguid()));
                                        } else {
                                            keyJsonBuilder.addNull("mdsEntry");
                                        }

                                    }
                                }
                                keysArrayBuilder.add(keyJsonBuilder.build());
                            }
                        }
                    }
                }
                JsonObject keysJsonObject = Json.createObjectBuilder()
                        .add("username", username)
                        .add("keys", keysArrayBuilder.build()).
                        build();
                userkeysArrayBuilder.add(keysJsonObject);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", SKFSCommon.getMessageProperty("FIDO-ERR-0001"), " Could not parse user keys; " + ex.getLocalizedMessage());
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " Could not parse user keys; " + ex.getLocalizedMessage()).build();
        }

        return Response.ok().entity(userkeysArrayBuilder.build().toString()).build();
    }

}
