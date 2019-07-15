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
 * Copyright (c) 2001-2018 StrongAuth, Inc.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
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
 * This EJB is responsible for executing the de-registration process of a specific
 * user registered key. FIDO U2F protocol does not provide any specification for 
 * user key de-registration. 
 *
 */

package com.strongkey.skfs.txbeans.v1;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.txbeans.deleteFidoKeysLocal;
import com.strongkey.skfs.txbeans.getFidoKeysLocal;
import com.strongkey.skfs.txbeans.updateFidoUserBeanLocal;
import com.strongkey.skfs.utilities.SKCEReturnObject;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.io.StringReader;
import java.util.Collection;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 * This EJB is responsible for executing the de-registration process of a specific
 * user registered key
 */
@Stateless
public class u2fDeregisterBean_v1 implements u2fDeregisterBeanLocal_v1, u2fDeregisterBeanRemote_v1 {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();
    
    /*
     * Enterprise Java Beans used in this EJB.
     */
    @EJB getFidoKeysLocal         getkeybean;
    @EJB deleteFidoKeysLocal      deletekeybean;
    @EJB updateFidoUserBeanLocal         updateldapbean;
    
    /*************************************************************************
                                                 888             
                                                 888             
                                                 888             
     .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.  
    d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b 
    88888888   X88K   88888888 888      888  888 888    88888888 
    Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.     
     "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888  

     *************************************************************************/
    /**
     * This method is responsible for deleting the user registered key from the 
     * persistent storage. This method first checks if the given ramdom id is
     * mapped in memory to the specified user and if found yes, gets the registration
     * key id and deletes that entry from the database.
     * 
     * Additionally, if the key being deleted is the last one for the user, the
     * ldap attribute of the user called 'FIDOKeysEnabled' is set to 'no'.
     * 
     * @param did       - FIDO domain id
     * @param protocol  - U2F protocol version to comply with.
     * @param username  - username
     * @param randomid  - random id that is unique to one fido registered authenticator
     *                      for the user.
     * @return          - returns SKCEReturnObject in both error and success cases.
     *                  In error case, an error key and error msg would be populated
     *                  In success case, a simple msg saying that the process was
     *                  successful would be populated.
     */
    @Override
    public SKCEReturnObject execute(String did, 
                                    String protocol,
                                    String username, 
                                    String randomid) {
        
        //  Log the entry and inputs
        skfsLogger.entering(skfsConstants.SKFE_LOGGER,classname, "execute"); 
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5001"), 
                        " EJB name=" + classname + 
                        " did=" + did + 
                        " protocol=" + protocol + 
                        " username=" + username +
                        " randomid=" + randomid);
        
        SKCEReturnObject rv = new SKCEReturnObject();
        
        //  input checks
                if (did == null || Long.parseLong(did) < 1) {
            rv.setErrorkey("FIDO-ERR-0002");
            rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " did=" + did);
            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " did=" + did);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }
        if (username == null || username.isEmpty() ) {
            rv.setErrorkey("FIDO-ERR-0002");
            rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " username=" + username);
            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " username=" + username);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }
        
        if (username.trim().length() > Integer.parseInt(applianceCommon.getApplianceConfigurationProperty("appliance.cfg.maxlen.256charstring"))) {
            rv.setErrorkey("FIDO-ERR-0027");
            rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0027") + " username should be limited to 256 characters");
            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0027", " username should be limited to 256 characters");
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }
        
        if (randomid == null || randomid.isEmpty() ) {
            rv.setErrorkey("FIDO-ERR-0002");
            rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " randomid=" + randomid);
            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " randomid=" + randomid);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }
        
        if (protocol == null || protocol.isEmpty() ) {
            rv.setErrorkey("FIDO-ERR-0002");
            rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0002") + " protocol=" + protocol);
            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0002", " protocol=" + protocol);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }
        
        if (!protocol.equalsIgnoreCase(skfsConstants.FIDO_PROTOCOL_VERSION_U2F_V2) && !protocol.equalsIgnoreCase(skfsConstants.FIDO_PROTOCOL_VERSION_2_0)) {
            rv.setErrorkey("FIDO-ERR-5002");
            rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-5002") + " protocol version passed =" + protocol);
            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-5002", " protocol version passed =" + protocol);
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
            return rv;
        }
        

            Short sid_to_be_deleted = null;
            String did_to_be_deactivated = null;
            int userfkidhyphen;
            String fidouser;
            Long fkid_to_be_deleted = null;
            try {
                String[] mapvaluesplit = randomid.split("-", 3);
                sid_to_be_deleted = Short.parseShort(mapvaluesplit[0]);
                did = mapvaluesplit[1];
                userfkidhyphen = mapvaluesplit[2].lastIndexOf("-");

                fidouser = mapvaluesplit[2].substring(0, userfkidhyphen);
                fkid_to_be_deleted = Long.parseLong(mapvaluesplit[2].substring(userfkidhyphen + 1));
            } catch (Exception ex) {
                rv.setErrorkey("FIDO-ERR-0023");
                rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0023") + "Invalid randomid= " + randomid);
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0023"), "Invalid randomid= " + randomid);
                skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
                return rv;
            }
            
            String current_pk = sid_to_be_deleted + "-" + did + "-" + username + "-" + fkid_to_be_deleted;
            if(!randomid.equalsIgnoreCase(current_pk)){
                //user is not authorized to deactivate this key
                //  throw an error and return.
                rv.setErrorkey("FIDO-ERR-0035");
                rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0035") + " username= " + username );
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0035"), " username= " + username );
                skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
                return rv;
            }
            if ( fkid_to_be_deleted != null ) {
                if (fkid_to_be_deleted >= 0) {
                    
                    skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", 
                            skfsCommon.getMessageProperty("FIDO-MSG-5005"), "");
                    try {
                        //  if the fkid_to_be_deleted is valid, delete the entry from the database
                        String jparesult = deletekeybean.execute(sid_to_be_deleted, Long.parseLong(did), username, fkid_to_be_deleted);
                        JsonObject jo;
                        try (JsonReader jr = Json.createReader(new StringReader(jparesult))) {
                            jo = jr.readObject();
                        }
                        
                        Boolean status = jo.getBoolean(skfsConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                        if ( !status ) {
                            //  error deleting user key
                            //  throw an error and return.
                            rv.setErrorkey("FIDO-ERR-0023");
                            rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0023") + " username= " + username + "   randomid= " + randomid);
                            skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0023"), " username= " + username + "   randomid= " + randomid);
                            skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
                            return rv;
                        } else {
                            //  Successfully deleted key from the database
                            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.FINE, skfsCommon.getMessageProperty("FIDO-MSG-0028"), "key id = " + fkid_to_be_deleted);
                        }
                        
                        Collection<FidoKeys> keys = getkeybean.getByUsernameStatus(Long.parseLong(did),username, applianceConstants.ACTIVE_STATUS);
                        if ( keys == null || keys.isEmpty() ) {
                            skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.FINE, skfsCommon.getMessageProperty("FIDO-MSG-5006"), "");
                            //  Update the "FIDOKeysEnabled" attribute of the user to 'false'
                            //  if the key that was just deleted is the last key registered
                            //  for the user
                            try {
                                String result = updateldapbean.execute(Long.parseLong(did), username, skfsConstants.LDAP_ATTR_KEY_FIDOENABLED, "false", false);
                                try (JsonReader jr = Json.createReader(new StringReader(result))) {
                                    jo = jr.readObject();
                                }
                                status = jo.getBoolean(skfsConstants.JSON_KEY_FIDOJPA_RETURN_STATUS);
                                if (status) {
                                    skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.FINE, skfsCommon.getMessageProperty("FIDO-MSG-0029"), "false");
                                } else {
                                    skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0024"), "false");
                                }
                            } catch (SKFEException ex) {
                                //  Do we need to return with an error at this point?
                                //  Just throw an err msg and proceed.
                                skfsLogger.log(skfsConstants.SKFE_LOGGER,Level.SEVERE, skfsCommon.getMessageProperty("FIDO-ERR-0024"), "false");
                            }
                        }
                     } catch (Exception ex) {
                        //  error deleting user key
                        //  throw an error and return.
                        rv.setErrorkey("FIDO-ERR-0023");
                        rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0023") + " username= " + username + "   randomid= " + randomid);
                        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0023"), " username= " + username + "   randomid= " + randomid);
                        skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
                        return rv;
                    }
                }                
            } else {
                //  user key information does not exist or has been timed out (flushed away).
                //  throw an error and return.
                rv.setErrorkey("FIDO-ERR-0022");
                rv.setErrormsg(skfsCommon.getMessageProperty("FIDO-ERR-0022") + " username= " + username + "   randomid= " + randomid);
                skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", skfsCommon.getMessageProperty("FIDO-ERR-0022"), " username= " + username + "   randomid= " + randomid);
                skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
                return rv;
            }
//        }
        
        rv.setReturnval("Successfully de-registered the key");
        
        //  log the exit and return
        skfsLogger.logp(skfsConstants.SKFE_LOGGER,Level.FINE, classname, "execute", skfsCommon.getMessageProperty("FIDO-MSG-5002"), classname);
        skfsLogger.exiting(skfsConstants.SKFE_LOGGER,classname, "execute");
        return rv;
    }
    
    @Override
    public SKCEReturnObject remoteExecute(String did, 
                                    String protocol,
                                    String username, 
                                    String randomid) {
        return execute(did, protocol, username, randomid);
    }
}
