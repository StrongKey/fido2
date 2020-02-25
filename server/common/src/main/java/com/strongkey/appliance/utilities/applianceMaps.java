/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.utilities;

import com.strongkey.appliance.entitybeans.Servers;
import com.strongkey.appliance.entitybeans.Domains;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Set;
import java.util.SortedMap;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.logging.Level;
import javax.xml.bind.DatatypeConverter;

public class applianceMaps {

    private static final String classname = "applianceMaps";

    private static final String SECRANDOM_ALG;

    private static SortedMap<Long, DomainObject> domainmap = new ConcurrentSkipListMap<>();
    private static SortedMap<Long, Servers> servermap = new ConcurrentSkipListMap<>();
    private static SortedMap<String, PublicKey>     pubkeysmap = new ConcurrentSkipListMap<>();

    /**
     * There is a need to use a SAKA encryption domain internally within the
     * appliance, but to disable all application-visible webservices on the
     * network; the SAKA will continue to work because modules on the appliance
     * will use the EJBs to get content encrypted/decrypted. The following List
     * is used to determine the list of DIDs that will NOT service webservices
     * from the network - including localhost.
     *
     */
    private static ConcurrentSkipListSet<Long> disabledDomains = new ConcurrentSkipListSet<>();

    static {
        // Configure disabled domains list
        String wsdisabled = applianceCommon.getApplianceConfigurationProperty("appliance.cfg.property.enableddomains.ccspin");
        StringTokenizer st = new StringTokenizer(wsdisabled, ",");
        while (st.hasMoreTokens()) {
            disabledDomains.add(Long.parseLong(st.nextToken()));
        }
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, "common", "init", "APPL-MSG-1000", "Following encryption domains are disabled for network webservices: " + disabledDomains.toString());

        SECRANDOM_ALG = applianceCommon.getApplianceConfigurationProperty("appliance.cfg.property.prngalgorithm");
    }

    public applianceMaps() {

    }


    /** Put a PublicKey object into the pubkeysmap
     * @param k String key in the map
     * @param v PublicKey value to put in the map
     * @return PublicKey
     */
    public static PublicKey putPublicKey(String k, PublicKey v)
    {
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "putPublicKey", "SKL-MSG-1063", k);
        return pubkeysmap.put(k, v);
    }

    /** Gets a mapped PublicKey object
     * @param key String key in the map; the key is typically of the
     * form "#-SIGN" or "#-ENC" where "#" is the Domain ID
     * @return PublicKey
     */
    public static PublicKey getPublicKey(String key)
    {
        return pubkeysmap.get(key);
    }

    /** Check if a PublicKey is mapped
     * @param key String key in the map
     * @return boolean
     */
    public static boolean isPublicKeyMapped(String key)
    {
        return pubkeysmap.containsKey(key);
    }

    /*
    *******************************************************************
8888888b.                                  d8b
888  "Y88b                                 Y8P
888    888
888    888  .d88b.  88888b.d88b.   8888b.  888 88888b.  .d8888b
888    888 d88""88b 888 "888 "88b     "88b 888 888 "88b 88K
888    888 888  888 888  888  888 .d888888 888 888  888 "Y8888b.
888  .d88P Y88..88P 888  888  888 888  888 888 888  888      X88
8888888P"   "Y88P"  888  888  888 "Y888888 888 888  888  88888P'
    *******************************************************************
     */
    /**
     * Get a Domains object from the domainmap. If it isn't in the map see if it
     * is in the database and try to get it from there, add it to the map and
     * then return the domains object.
     *
     * @param did Long value of key in the map - the unique DID
     * @return Domains entity, if it exists
     */
    public static Domains getDomain(Long did) {
        try {
            if (domainmap.containsKey(did)) {
                DomainObject dobj = domainmap.get(did);
                dobj.setLastUsed(System.currentTimeMillis());
                return dobj.getDomain();
            }
        } catch (NullPointerException ex) {
            return null;
        }
        return null;
    }

    /**
     * Check if a Domain is mapped
     *
     * @param did Long value of DID key in the map
     * @return boolean
     */
    public static boolean isDomainMapped(Long did) {
        return domainmap.containsKey(did);
    }

    /**
     * Put a Domains object into the domainmap
     *
     * @param k String key in the map - the unique DID
     * @param v Domains object value to put in the map
     * @return Domains
     */
    public static Domains putDomain(Long k, Domains v) {
        // Create the domain object & put it in the map
        DomainObject dobj = new DomainObject(v);
        domainmap.put(k, dobj);

        // Check if it got put correctly and return
        if (domainmap.containsKey(k)) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "putDomain", "SKL-MSG-1063", k);
            return v;
        } else {
            return null;
        }
    }


    /**
     * Check if a Domain object (looked up by did as key) is active or not.
     *
     * @param k
     * @return boolean - true, if it exists, false otherwise
     */
    public static boolean domainActive(Long did) {
        if (domainmap.containsKey(did)) {
            DomainObject dobj = domainmap.get(did);
            Domains dom = dobj.getDomain();
            return dom.getStatus().equalsIgnoreCase(applianceConstants.ACTIVE_STATUS);
        } else {
            return false;
        }
    }

    public static void removeDomain(Long did) {
        domainmap.remove(did);
    }

    /**
     * Get an initialization vector for an encryption request from a
     * Domain-specific SecureRandom object in the DomainObject in the domainmap.
     * If it isn't in the map
     *
     * @param did Long value of key in the map - the unique DID
     * @param size int value of the size of the requested IV
     * @return String IV
     */
    public static String getIv(Long did, int size) {
        // Check parameters
        switch (size) {
            case 16:
                break;
            case 20:
                break;
            case 32:
                break;
            case 64:
                break;
            default:
                strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.SEVERE, classname, "getIv", "SKL-ERR-1004", size);
                throw new IllegalArgumentException("SKL-ERR-1004: Invalid argument: " + size);
        }

        // Get DomainObject from map and return IV if useprng is true
        try {
            if (domainmap.containsKey(did)) {
                DomainObject dobj = domainmap.get(did);
                if (dobj.getUsePrng()) {
                    dobj.setLastUsed(System.currentTimeMillis());
                    return dobj.getIv(size);
                }
            }
        } catch (NullPointerException ex) {
            return null;
        }
        return null;
    }

    /**
     * Check if a Domain is disabled for webservices on the network
     *
     * @param did Long value of DID
     * @return boolean
     */
    public static boolean isDomainWSDisabled(Long did) {
        return disabledDomains.contains(did);
    }

    /**
     * Set SecureRandom for a Domain
     * @param did Long - Domain ID
     * @param seed String - Seed value
     * @return Boolean - Whether the seed was set successfully
     * @throws java.security.NoSuchAlgorithmException
     */
    public static Boolean setSecureRandom(Long did, String seed) throws NoSuchAlgorithmException {
        byte[] seedbytes = DatatypeConverter.parseBase64Binary(seed);
        DomainObject dobj = domainmap.get(did);
        if (dobj != null) {
            dobj.setSecureRandom(SECRANDOM_ALG, seedbytes);
            dobj.setLastUsed(System.currentTimeMillis());
            return Boolean.TRUE;
        } else
            return Boolean.FALSE;
    }

    public static Set<Long> getDomainKeyset(){
        return domainmap.keySet();
    }

    public static DomainObject getDomainObject(Long did){
        return domainmap.get(did);
    }

    /*
    *************************************************************************
 .d8888b.
d88P  Y88b
Y88b.
 "Y888b.    .d88b.  888d888 888  888  .d88b.  888d888 .d8888b
    "Y88b. d8P  Y8b 888P"   888  888 d8P  Y8b 888P"   88K
      "888 88888888 888     Y88  88P 88888888 888     "Y8888b.
Y88b  d88P Y8b.     888      Y8bd8P  Y8b.     888          X88
 "Y8888P"   "Y8888  888       Y88P    "Y8888  888      88888P'
    *************************************************************************
     */
    /**
     * Put a Servers object into the servermap
     *
     * @param k String key in the map - the unique SID
     * @param v Servers object value to put in the map
     * @return Servers
     */
    public static Servers putServer(Long k, Servers v) {
        // Put the object in the map
        servermap.put(k, v);

        // Check if it got put correctly and return
        if (servermap.containsKey(k)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "putServer", "APPL-MSG-1063", k);
            return v;
        } else {
            return null;
        }
    }

    /**
     * Get a Servers object from the servermap.
     *
     * @param sid Long value of key in the map - the unique SID
     * @return Servers entity, if it exists
     */
    public static Servers getServer(Long sid) {
        try {
            if (servermap.containsKey(sid)) {
                return servermap.get(sid);
            }
        } catch (NullPointerException ex) {
            return null;
        }
        return null;
    }

}
