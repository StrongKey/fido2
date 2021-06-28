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
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;
import javax.ws.rs.core.Response;


@Stateless
public class getAllConfigurationsBean implements getAllConfigurationsBeanLocal {

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

    
    @Override
    public Response execute(Long did) {
        Collection<Configurations> configcoll = null;
        Map<String,String> configmap = new HashMap<>();
        
        //Collect configurations from db
        try {
            configcoll = getconfigejb.byDid(did);
        } catch (Exception ex) {
            Logger.getLogger(getAllConfigurationsBean.class.getName()).log(Level.SEVERE, null, ex);
        }
        if(configcoll != null){
            for (Configurations c : configcoll) {
                configmap.put(c.getConfigurationsPK().getConfigKey(), c.getConfigValue());
            }
        }
        
        //collect a list of mutable config keys
        ResourceBundle mutableprops = SKFSCommon.getmutableConfiguration();
        Enumeration<String> enm = mutableprops.getKeys();
        List<String> applkeys = new ArrayList<>();
        List<String> ldapekeys = new ArrayList<>();
        List<String> skfskeys = new ArrayList<>();
        while (enm.hasMoreElements()) {
            String key = enm.nextElement();
            if(key.startsWith("appl")){
                applkeys.add(key);
            }
            if(key.startsWith("ldape")){
                ldapekeys.add(key);
            }
            if(key.startsWith("skfs")){
                skfskeys.add(key);
            }
        }

        Collections.sort((List<String>) applkeys);
        Collections.sort((List<String>) ldapekeys);
        Collections.sort((List<String>) skfskeys);
        
        JsonObjectBuilder job;
        
        JsonArrayBuilder apparray = Json.createArrayBuilder();
        for (String applConfig : applkeys) {
             job = Json.createObjectBuilder();
             job.add("configkey", applConfig);
             if(configmap.containsKey(applConfig)){
                 job.add("configvalue", configmap.get(applConfig));
             }else{
                 job.add("configvalue", applianceCommon.getApplianceConfigurationProperty(applConfig));
             }
             job.add("hint", mutableprops.getString(applConfig));
             apparray.add(job.build());
        }
        
        JsonArrayBuilder ldaparray = Json.createArrayBuilder();
        for (String ldapConfig : ldapekeys) {
             job = Json.createObjectBuilder();
             job.add("configkey", ldapConfig);
             if(configmap.containsKey(ldapConfig)){
                 job.add("configvalue", configmap.get(ldapConfig));
             }else{
                 job.add("configvalue", skceCommon.getConfigurationProperty(ldapConfig));
             }
             job.add("hint", mutableprops.getString(ldapConfig));
             ldaparray.add(job.build());
        }
        
        
        JsonArrayBuilder skfsarray = Json.createArrayBuilder();
        for (String skfsConfig : skfskeys) {
             job = Json.createObjectBuilder();
             job.add("configkey", skfsConfig);
             if(configmap.containsKey(skfsConfig)){
                 job.add("configvalue", configmap.get(skfsConfig));
             }else{
                 job.add("configvalue", SKFSCommon.getConfigurationProperty(skfsConfig));
             }
             job.add("hint", mutableprops.getString(skfsConfig));
             skfsarray.add(job.build());
        }
        
        //now create a json object with json array of keys (one per type)
        job = Json.createObjectBuilder();
        job.add("appliance", apparray.build());
        job.add("ldap", ldaparray.build());
        job.add("skfs", skfsarray.build());
        
        
        String response = Json.createObjectBuilder()
            .add(SKFSConstants.JSON_KEY_SERVLET_RETURN_RESPONSE, job.build())
            .build().toString();
        System.out.println("response = \n" + response);
        return Response.ok().entity(response).build();
    }
}
