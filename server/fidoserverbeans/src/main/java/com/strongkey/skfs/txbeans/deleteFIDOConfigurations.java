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

import com.strongkey.appliance.entitybeans.Configurations;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.ws.rs.core.Response;


@Stateless
public class deleteFIDOConfigurations implements deleteFIDOConfigurationsLocal {

    /**
     ** This class's name - used for logging
     *
     */
    private final String classname = this.getClass().getName();

    /**
     ** Resources used by this bean
     *
     */
    @EJB
    getFIDOConfigurationLocal getconfigejb;
     @EJB
    replicateSKFEObjectBeanLocal replObj;

    /**
     * Persistence context for derby
     */
    @PersistenceContext
    private EntityManager em;

    @Override
    public Response execute(Long did, JsonArray configkeys) {

        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER, classname, "execute");
        Configurations[] configarray = new Configurations[configkeys.size()];
        int i = 0;
        for (int j = 0; j < configkeys.size(); j++) {
            String key = configkeys.getString(j);
                System.out.println(key);
                Configurations config = getconfigejb.getByPK(did, key);
                if (config != null) {
                    configarray[i] = config;
                    em.remove(config);
                    em.flush();
                    i++;
                    if (config.getConfigurationsPK().getConfigKey().equalsIgnoreCase("appl.cfg.property.service.ce.ldap.ldaptype")) {
                        skceCommon.setldaptype(did, applianceCommon.getApplianceConfigurationProperty("appl.cfg.property.service.ce.ldap.ldaptype"));
                    }
                    //replicate deletion
                    String primarykey = config.getPrimaryKey();
                    if (applianceCommon.replicate()) {
                        if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                            String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_CONFIGURATIONS, applianceConstants.REPLICATION_OPERATION_DELETE, primarykey, config);
                            if (response != null) {
                                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + response).build();
                            }
                        }
                    }
                }
        }

        if (configarray.length > 0) {
            SKFSCommon.removeConfiguration(did, configarray);
            skceCommon.removeConfiguration(did, configarray);
        }

        String response = Json.createObjectBuilder()
                .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, "Successfully deleted configurations ")
                .build().toString();

        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
        
        return Response.ok().entity(response).build();
    }
}
