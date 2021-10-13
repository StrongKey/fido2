/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.entitybeans.Configurations;
import com.strongkey.appliance.entitybeans.Domains;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.crypto.utility.CryptoException;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skfs.messaging.SKCEBacklogProcessor;
import com.strongkey.skfs.policybeans.cacheMDSv3Local;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class startServices {

     /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();
    @EJB
    getDomainsBeanLocal getdomejb;
    @EJB
    getFIDOConfigurationLocal getfidoconfig;
    @EJB
    cacheMDSv3Local caceMDSejb;

    @PostConstruct
    public void initialize() {

        String standalone = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.fidoengine");
//        if (standalone.equalsIgnoreCase("true")) {
        System.out.println("======Initializing domains and configurations======");
        skceCommon.getConfigurationProperty("skce.cfg.property.skcehome");
        Collection<Domains> domains = getdomejb.getAll();

        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-3000", "");
        caceMDSejb.execute();
        
        if (domains != null) {
            for (Domains d : domains) {
                Long did = d.getDid();

                // Cache domain objects
                if (standalone.equalsIgnoreCase("true")) {
                    applianceMaps.putDomain(did, d);
                }

                // Get configuration information for the domain
                if (!SKFSCommon.isConfigurationMapped(did)) {
                    Collection<Configurations> cfgcoll;
                    Collection<Configurations> skcecfgcoll;
                    try {
                        cfgcoll = getfidoconfig.byDid(did);
                        skcecfgcoll = cfgcoll;
                        if (cfgcoll != null) {
                            SKFSCommon.putConfiguration(did, cfgcoll.toArray(new Configurations[cfgcoll.size()]));

                            skceCommon.putConfiguration(did, skcecfgcoll.toArray(new Configurations[skcecfgcoll.size()]));
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(startServices.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }
                //add appl config
                Configurations cfg = getfidoconfig.getByPK(did, "ldape.cfg.property.service.ce.ldap.ldapdnsuffix");
                if (cfg != null) {
                    skceCommon.setdnSuffixConfigured(Boolean.TRUE);
                }

                cfg = getfidoconfig.getByPK(did, "ldape.cfg.property.service.ce.ldap.ldapgroupsuffix");
                if (cfg != null) {
                    skceCommon.setgroupSuffixConfigured(Boolean.TRUE);
                }

                cfg = getfidoconfig.getByPK(did, "appl.cfg.property.service.ce.ldap.ldaptype");
                if (cfg == null) {
                    skceCommon.setldaptype(did, applianceCommon.getApplianceConfigurationProperty("appl.cfg.property.service.ce.ldap.ldaptype"));
                } else {
                    skceCommon.setldaptype(did, cfg.getConfigValue());
                }

                try {
                    String didString = did.toString();
                    if (standalone.equalsIgnoreCase("true")) {
                        cryptoCommon.loadSigningKey(didString, SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.signingkeystore.password"), d.getSkceSigningdn());
                        cryptoCommon.loadVerificationKey(didString, SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.signingkeystore.password"), d.getSkceSigningdn());
                    }
                    cryptoCommon.loadJWTCACert(didString);
                    Long sid = applianceCommon.getServerId();
                    if (SKFSCommon.getConfigurationProperty("skfs.cfg.property.jwt.create").equalsIgnoreCase("true")) {
                        cryptoCommon.loadJWTSigningKeys(didString, sid.toString());
                        cryptoCommon.loadJWTVerifyKeys(didString, sid.toString());
                    }
                    
                } catch (CryptoException ex) {
                    ex.printStackTrace();
                }
            }
        }

        //set replication to false
        if (applianceCommon.getApplianceConfigurationProperty("appliance.cfg.property.replicate").equalsIgnoreCase("true")) {
            applianceCommon.setReplicateStatus(Boolean.TRUE);
            SKCEBacklogProcessor.getInstance();

        } else {
            applianceCommon.setReplicateStatus(Boolean.FALSE);
        }
//        }
    }
}
