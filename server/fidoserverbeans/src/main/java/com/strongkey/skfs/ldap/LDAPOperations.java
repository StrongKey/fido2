/*
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License, as published by the Free Software Foundation and
 *  available at https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html,
 *  version 2.1.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 * *********************************************
 *                     888
 *                     888
 *                     888
 *   88888b.   .d88b.  888888  .d88b.  .d8888b
 *   888 "88b d88""88b 888    d8P  Y8b 88K
 *   888  888 888  888 888    88888888 "Y8888b.
 *   888  888 Y88..88P Y88b.  Y8b.          X88
 *   888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 *  *********************************************
 *  DESCRIPTION Java Class to get groups for a username when configured for LDAP
 */
package com.strongkey.skfs.ldap;

import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.skce.utilities.skceCommon;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.AttributeInUseException;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import javax.naming.directory.SearchControls;
import javax.naming.directory.SearchResult;

public class LDAPOperations {

    /**
     * The OU (organizational unit) to add groups to
     */
    /**
     * The connection, through a <code>DirContext</code>, to LDAP
     */
    private DirContext context;

    public List<String> getGroupsLDAP(Long did, String username) {
        String ldapurl = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapurl");
        String binduser = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapbinddn");
        String password = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapbinddn.password");
        String ldaptype = skceCommon.getldaptype(did);
        String dnprefix = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapdnprefix");
        String dnsuffix = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapdnsuffix");
        try {

            String SERVICE_OU_PREFIX;
            if (ldaptype.equalsIgnoreCase("LDAP")) {
                SERVICE_OU_PREFIX = ",did=";
            } else {
                SERVICE_OU_PREFIX = ",ou=";
            }

            String principalSuffix;
            if (skceCommon.isdnSuffixConfigured()) {
                principalSuffix = dnsuffix;
            } else {
                principalSuffix = SERVICE_OU_PREFIX + did + dnsuffix;
            }
            String userdn = dnprefix + username + principalSuffix;

            List<String> groups = new ArrayList<>();
            //property for LDAP URL: url
            String[] params = {"ldape.cfg.property.service.ce.ldap.ldapurl", ldapurl};
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.FINE, "APPL-MSG-1000", params);
            //property for bindusername: bindname
            String[] params1 = {"ldape.cfg.property.service.ce.ldap.ldapbinddn", binduser};
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.FINE, "APPL-MSG-1000", params1);
            try {
                context = skceCommon.getInitiallookupContext("SKCE", ldapurl, binduser, password);
            } catch (Exception e) {
                strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1000", e.getLocalizedMessage());
                return null;
            }
            // Set up criteria to search on
            String filter;
            //property for bindusername: bindname
            String[] params2 = {"ldaptype", ldaptype};
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.FINE, "APPL-MSG-1000", params2);
            if (ldaptype.equalsIgnoreCase("AD")) {
                try {
                    filter = new StringBuffer()
                            .append("(Member=")
                            //.append(Common.getlookupUserDN(username))
                            .append(userdn)
                            .append(")")
                            .toString();
                } catch (Exception e) {
                    return null;
                }
            } else {
                try {
                    filter = new StringBuffer()
                            .append("(uniqueMember=")
                            .append(userdn)
                            .append(")")
                            .toString();
                } catch (Exception e) {
                    return null;
                }
            }
            // Set up search constraints
            SearchControls cons = new SearchControls();
            cons.setSearchScope(SearchControls.SUBTREE_SCOPE);
            NamingEnumeration results
                    = context.search("", filter, cons);
            while (results.hasMore()) {
                SearchResult result = (SearchResult) results.next();
//                groups.add(Common.getlookupGroupCN(result.getName()));
                groups.add(result.getName());
            }
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.FINE, "APPL-MSG-1000", "Groups: \n" + groups);
            return groups;
        } catch (NamingException ex) {
            Logger.getLogger(LDAPOperations.class.getName()).log(Level.SEVERE, null, ex);
            ex.printStackTrace();
        }
        return null;
    }

    public Boolean isAdmin(Long did, String username) {
        List<String> userGroups = getGroupsLDAP(did, username);
        String groupsuffix = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapgroupsuffix");

        String SERVICE_OU_PREFIX;
        String ldaptype = skceCommon.getldaptype(did);
        if (ldaptype.equalsIgnoreCase("LDAP")) {
            SERVICE_OU_PREFIX = ",did=";
        } else {
            SERVICE_OU_PREFIX = ",ou=";
        }

        String groupdnsuffix;
        if (skceCommon.isgroupSuffixConfigured()) {
            groupdnsuffix = groupsuffix;
        } else {
            groupdnsuffix = SERVICE_OU_PREFIX + did + groupsuffix;
        }

        String admingroup = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidoadmingroup") + groupdnsuffix;
        Boolean isadmin = userGroups.contains(admingroup);
        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.FINE, "APPL-MSG-1000", isadmin);

        return isadmin;
    }

    
    public Boolean hasAdmins(Long did) throws NamingException {
        
        String ldapurl = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapurl");
        String binduser = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapbinddn");
        String bindpassword = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapbinddn.password");
        String groupsuffix = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapgroupsuffix");

        String SERVICE_OU_PREFIX;
        String ldaptype = skceCommon.getldaptype(did);
        if (ldaptype.equalsIgnoreCase("LDAP")) {
            SERVICE_OU_PREFIX = ",did=";
        } else {
            SERVICE_OU_PREFIX = ",ou=";
        }

        String groupdnsuffix;
        if (skceCommon.isgroupSuffixConfigured()) {
            groupdnsuffix = groupsuffix;
        } else {
            groupdnsuffix = SERVICE_OU_PREFIX + did + groupsuffix;
        }

        String admingroup = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidoadmingroup") + groupdnsuffix;
        
        // create ldap context
        try {
            context = skceCommon.getInitiallookupContext("SKCE", ldapurl, binduser, bindpassword);
        } catch (Exception e) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1000", e.getLocalizedMessage());
            return Boolean.FALSE;
        }
        
        // Set up attributes to search for
        String[] searchAttributes = new String[1];
        if (ldaptype.equalsIgnoreCase("LDAP")) {
            searchAttributes[0] = "uniqueMember";
        } else {
            searchAttributes[0] = "Memberof";
        }
        
        Attributes attributes = context.getAttributes(admingroup, searchAttributes);
        
        if (attributes != null) {
            Attribute memberAtts = attributes.get("uniqueMember");
            if (memberAtts != null) {
                System.out.println("FidoAdminAuthorized has members");
                return Boolean.FALSE;
            }else{
                System.out.println("FidoAdminAuthorized does NOT have members yet");
                return Boolean.TRUE;
            }
        }
        return Boolean.FALSE;
    }
    
    public void assignUsertoGroupforDid(String userdn, Long did, String groupdn) throws NamingException {

        try {
            ModificationItem[] mods = new ModificationItem[1];

            Attribute mod
                    = new BasicAttribute("uniqueMember", userdn);
            mods[0]
                    = new ModificationItem(DirContext.ADD_ATTRIBUTE, mod);
            context.modifyAttributes(groupdn, mods);
        } catch (AttributeInUseException e) {
            // If user is already added, ignore exception
        }
    }
    
    // Can be modified to be a more general method for user creation
    public Boolean addAdminUser(Long did, String username, String password) {
        
        String ldapurl = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapurl");
        String binduser = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapbinddn");
        String bindpassword = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapbinddn.password");
        String newAdminUserdn = "cn=" + username + ",did=" + did + skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapdnsuffix");
        String fidoAdminAuthorizeddn = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidoadmingroup") + ",did=" + did + skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapgroupsuffix");
        
        // Create a container set of attributes
        Attributes container = new BasicAttributes();

        // Create the objectclass to add
        Attribute objClasses = new BasicAttribute("objectClass");
        objClasses.add("top");
        objClasses.add("person");
        objClasses.add("organizationalPerson");
        objClasses.add("inetOrgPerson");

        // Assign the username, first name, and last name
        Attribute cn = new BasicAttribute("cn", username);
        Attribute givenName = new BasicAttribute("givenName", username);
        Attribute sn = new BasicAttribute("sn", username);
        Attribute uid = new BasicAttribute("uid", username);

        // Add password
        Attribute userPassword = new BasicAttribute("userpassword", password);

        // Add these to the container
        container.put(objClasses);
        container.put(cn);
        container.put(sn);
        container.put(givenName);
        container.put(uid);
        container.put(userPassword);
        
        // create ldap context
        try {
            context = skceCommon.getInitiallookupContext("SKCE", ldapurl, binduser, bindpassword);
        } catch (Exception e) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1000", e.getLocalizedMessage());
            return Boolean.FALSE;
        }
        
        // create admin user and add to group
        try {
            context.createSubcontext(newAdminUserdn, container);
            assignUsertoGroupforDid(newAdminUserdn, did, fidoAdminAuthorizeddn);
            return Boolean.TRUE;
        } catch (NamingException ex) {
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER, Level.SEVERE, "APPL-ERR-1000", ex.getLocalizedMessage());
            return Boolean.FALSE;
        }
    }
}
