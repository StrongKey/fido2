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
 * EJB to perform ldap based authorizations.
 * Has been tested against OpenDJ 2.0
 *
 */
package com.strongkey.auth.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.applianceInputChecks;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.skce.utilities.SKCEException;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skce.utilities.skceConstants;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.naming.AuthenticationException;
import javax.naming.Context;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.ldap.LdapContext;

/**
 * EJB to perform ldap based authorizations
 */
@Stateless
public class authorizeLdapUserBean implements authorizeLdapUserBeanLocal, authorizeLdapUserBeanRemote {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();
       
    /******************************************************************************************
                                                    888             
                                                    888             
                                                    888             
        .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.  
       d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b 
       88888888   X88K   88888888 888      888  888 888    88888888 
       Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.     
        "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888 
 
    ******************************************************************************************/

    /**
     * This method authenticates a credential - username and password - for a
     * specified operation against the configured LDAP directory.  Only LDAP-based
     * authentication is supported currently; however both Active Directory and a
     * standards-based, open-source LDAP directories are supported.  For the latter,
     * this has been tested with OpenDS 2.0 (https://docs.opends.org).
     *
     * @param did
     * @param username - String containing the credential's username
     * @param password - String containing the user's password
     * @param operation - String describing the operation being requested by the
     * user - either ENC or DEC for encryption and decryption respectively
     * @return boolean value indicating either True (for authenticated) or False
     * (for unauthenticated or failure in processing)
     * @throws SKCEException - in case of any error
     */
    @Override
    public boolean execute(
            Long did,
            String username,
            String password,
            String operation) throws SKCEException 
    {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "execute");
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER,Level.FINE, classname, "execute", applianceCommon.getMessageProperty("APPL-MSG-1000"), 
                        " Input received :\nEJB name=" + classname + 
                        " username=" + username + 
                        " operation=" + operation);
        
        //  Input checks
         try{
             applianceInputChecks.checkDid(did);
             applianceInputChecks.checkServiceCredentails(username, password);
             applianceInputChecks.checkOperation(operation);
         }catch(NullPointerException | IllegalArgumentException ex){
             throw new SKCEException(ex.getLocalizedMessage());
         }
        
        
        // Get configured parameters for this domain
        String ldapurl = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapurl");
        String dnprefix = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapdnprefix");
        String dnsuffix = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapdnsuffix");
        String groupsuffix = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapgroupsuffix");

        // Setup paramters from class variables
        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", "setup principal");
        String principal = dnprefix + username + skceCommon.getSERVICE_OU_PREFIX() + did + dnsuffix;

        
        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", principal);

        try {
            // Instantiate context
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", "new InitialContext");
            Context ctx = getcontext("SKCE", ldapurl, principal, password);
            // What operation are we performing?
            String group = null;
            if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_ENC)) {
                // Encryption Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapencryptiongroup") +skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_DEC)) {
                // Decryption Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapdecryptiongroup")+skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_CMV)) {
                // Cloud move Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapcloudmovegroup")+skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_SRV)) {
                // Cloud move Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapservicegroup")+skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_ADM)) {
                // Admin Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapadmingroup")+skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_LOADKEY)) {
                // Load Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldaploadgroup")+skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_SIGN)) {
                // Sign Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapsigngroup")+skceCommon.getSERVICE_OU_PREFIX()+ did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_REMOVEKEY)) {
                // Removekey Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapremovegroup")+skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO)) {
                // Removekey Group
                group = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldapfidogroup")+skceCommon.getSERVICE_OU_PREFIX() + did + groupsuffix;
            } else {
                // Invalid operation
                strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.SEVERE, "APPL-MSG-1000", "User-Operation=" + username + "-" + operation + "]");
                return false;
            }
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", group);
            LdapContext lc = (LdapContext) ctx.lookup(group);
            // If return value is not null, credentials are valid
            if (lc != null) {
                /**
                 * Now check if user is in the group as a uniqueMember. *
                 * Unfortunately, AD doesn't use standard LDAP * objectclasses,
                 * so we have to test for "member" instead * of "uniqueMember"
                 * when testing against AD.
                 */
                String ldaptype = skceCommon.getConfigurationProperty("ldape.cfg.property.service.ce.ldap.ldaptype");
                Attributes attrs ;
                if (ldaptype.equalsIgnoreCase("AD")) {
                    String[] attrIDs = {"member"};
                    attrs = lc.getAttributes("", attrIDs);
                } else{
                    String[] attrIDs = {"uniqueMember"};
                    attrs = lc.getAttributes("", attrIDs);
                }
                // Check attributes of the group object
                for (NamingEnumeration<?> ne = attrs.getAll(); ne.hasMore();) {
                    Attribute attr = (Attribute) ne.next();
                    NamingEnumeration<?> e = attr.getAll();
                    
                    while (e.hasMore()) {
                        String unqmem = (String) e.next();
                        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", unqmem);
                        if (unqmem.equalsIgnoreCase(principal)) {
                            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", group + " (" + principal + ")");
                            ctx.close();
                            lc.close();
                            return true;
                        }
                    }
                    // User is not in the d group
                    strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.INFO, "APPL-MSG-1000", group + " (" + principal + ")");
                    ctx.close();
                    lc.close();
                    
                    //return false;
                    throw new SKCEException("Invalid User : " + principal);
                }
            }
            // Failed authentication with supplied credentials;
        } catch (AuthenticationException ex) {
            ex.printStackTrace();
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.SEVERE, "APPL-ERR-1000", username + ex.getLocalizedMessage());
            throw new SKCEException(skceCommon.getMessageProperty("SKCEWS-ERR-3055").replace("{0}", "") + username);
        } catch (NamingException ex) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.SEVERE, "APPL-ERR-1000", username + ex.getLocalizedMessage());
            throw new SKCEException(skceCommon.getMessageProperty("SKCEWS-ERR-3055").replace("{0}", "") + username);
        }
        // Should not reach here, but...
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER,classname, "authenticateUser");
        return false;
    }

    @Override
    public boolean remoteExecute(Long did,String username, String password, String operation) throws SKCEException {
        return execute(did, username, password, operation);
    }
    
    protected Context getcontext(String module, String ldapurl, String principal, String password) throws NamingException{
        return skceCommon.getInitiallookupContext("SKCE", ldapurl, principal, password);
    }
}
