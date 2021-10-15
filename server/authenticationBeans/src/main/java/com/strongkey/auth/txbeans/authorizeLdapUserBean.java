/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
     * specified operation against the configured LDAP directory.Only LDAP-based
 authentication is supported currently; however both Active Directory and a
 standards-based, open-source LDAP directories are supported.  For the latter,
 this has been tested with OpenDS 2.0 (https://docs.opends.org).
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
        String ldapurl = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapurl");
        String dnprefix = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapdnprefix");
        String dnsuffix = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapdnsuffix");
        String groupsuffix = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapgroupsuffix");

        // Setup paramters from class variables
        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", "setup principal");
        String SERVICE_OU_PREFIX;
        String ldaptype = skceCommon.getldaptype(did);
        if (ldaptype.equalsIgnoreCase("LDAP")) {
             SERVICE_OU_PREFIX = ",did=";
        } else {
            SERVICE_OU_PREFIX = ",ou=";
        }
        String principalSuffix ;
        if (skceCommon.isdnSuffixConfigured()) {
            principalSuffix = dnsuffix;
        } else {
            principalSuffix = SERVICE_OU_PREFIX + did + dnsuffix;
        }
        String principal = dnprefix + username + principalSuffix;

        String groupdnsuffix;
        if (skceCommon.isgroupSuffixConfigured()) {
            groupdnsuffix = groupsuffix;
        } else {
            groupdnsuffix = SERVICE_OU_PREFIX + did + groupsuffix;
        }

        strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", principal);

        try {
            // Instantiate context
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.FINE, "APPL-MSG-1000", "new InitialContext");
            Context ctx = getcontext("SKCE", ldapurl, principal, password);
            // What operation are we performing?
            String group = null;
            if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_ENC)) {
                // Encryption Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapencryptiongroup") +groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_DEC)) {
                // Decryption Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapdecryptiongroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_CMV)) {
                // Cloud move Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapcloudmovegroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_SRV)) {
                // Cloud move Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapservicegroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_ADM)) {
                // Admin Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapadmingroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_LOADKEY)) {
                // Load Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldaploadgroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_SIGN)) {
                // Sign Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapsigngroup")+SERVICE_OU_PREFIX+ did + groupsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_REMOVEKEY)) {
                // Removekey Group
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapremovegroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO)) {
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidogroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO_REG)) {
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidoreggroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO_SIGN)) {
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidosigngroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO_AUTHZ)) {
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidoauthzgroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO_ADMIN)) {
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidoadmingroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO_MONITOR)) {
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidomonitoringgroup")+groupdnsuffix;
            } else if (operation.equalsIgnoreCase(skceConstants.LDAP_ROLE_FIDO_POLICY_MANAGEMENT)) {
                group = skceCommon.getConfigurationProperty(did, "ldape.cfg.property.service.ce.ldap.ldapfidopolicymanagementgroup")+groupdnsuffix;
            }
            else {
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
                            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.INFO, "APPL-MSG-1000", "request user: "+group + " (" + principal + ")");
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
        } catch (AuthenticationException  ex) {
            ex.printStackTrace();
            strongkeyLogger.log(applianceConstants.APPLIANCE_LOGGER,Level.SEVERE, "APPL-ERR-1000", username + ex.getLocalizedMessage());
            throw new SKCEException(skceCommon.getMessageProperty("SKCEWS-ERR-3055").replace("{0}", "") + username);
        } catch (NamingException ex) {
            ex.printStackTrace();
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
