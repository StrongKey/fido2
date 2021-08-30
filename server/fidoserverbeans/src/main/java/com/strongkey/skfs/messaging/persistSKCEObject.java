/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.messaging;

import com.google.protobuf.InvalidProtocolBufferException;
import com.strongkey.appliance.entitybeans.Configurations;
import com.strongkey.appliance.entitybeans.ConfigurationsPK;
import com.strongkey.appliance.entitybeans.Domains;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.applianceMaps;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.skce.pojos.MDSClient;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.txbeans.persistSKCEObjectRemote;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skce.utilities.skceConstants;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfe.entitybeans.FidoKeysPK;
import com.strongkey.skfs.entitybeans.AttestationCertificates;
import com.strongkey.skfs.entitybeans.AttestationCertificatesPK;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.entitybeans.FidoPoliciesPK;
import com.strongkey.skfs.entitybeans.FidoUsers;
import com.strongkey.skfs.entitybeans.FidoUsersPK;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKIllegalArgumentException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class persistSKCEObject implements persistSKCEObjectLocal, persistSKCEObjectRemote {

    /**
     ** This class's name - used for logging
     **/
    private final String classname = this.getClass().getName();

    /**
     ** Resources used by this bean
     **/
    @Resource private SessionContext            sc;         // For JTA management
    @PersistenceContext private EntityManager   em;         // For JPA management

    private FidoKeys            fidokeys,           fkresult;
    private FidoUsers           fidousers,          furesult;
    private Domains             domains,            dmresult;
    private UserSessionInfo     usersessioninfo;
    private FidoPolicies        fidopolicies,       fpresult;
    private AttestationCertificates attestationcertificates, acresult;
    private Configurations config, configresult;

    private Long did, fkid, sid, pid, attcid = 0L;
    private String fidouser, pkey, config_key = null;

    @Override
    public boolean execute(String repobjpk, int objectype, int objectop, String objectpk, byte[] msg) {
        strongkeyLogger.entering(skceConstants.SKEE_LOGGER,classname, "execute");

        String[] pkarray ;

        Boolean isValid = true;
        /**
         * What kind of object did we get passed?  Parse it out from the byte
         * array and process it accordingly.
         */
        switch (objectype) {
              case applianceConstants.ENTITY_TYPE_FIDO_KEYS:
                // In case the user has a hypen, we need to split the key appropriately
                pkarray = objectpk.split("-", 3);

                sid = Long.parseLong(pkarray[0]);
                did = Long.parseLong(pkarray[1]);

                // pkarray[2] should have both the uesrname and fkid with an indeterminate amount of hyphens in the username
                int userfkidhyphen = pkarray[2].lastIndexOf("-");

                fidouser = pkarray[2].substring(0, userfkidhyphen);
                fkid = Long.parseLong(pkarray[2].substring(userfkidhyphen + 1));
                pkey = "SID-DID-FIDOUSER-FKID=" + sid + "-" + did + "-" + fidouser + "-" + fkid;

                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-6034", repobjpk + " containing ENTITY_TYPE_FIDOKEY [" + pkey + "]");

                // Parse Proto object and create entitybean
                ZMQSKCEReplicationProtos.FidoKeys fkproto;
                try {
                    fkproto = ZMQSKCEReplicationProtos.FidoKeys.parseFrom(msg);
                    fidokeys = new FidoKeys();
                    fidokeys.setFidoKeysPK(new FidoKeysPK(sid.shortValue(), did.shortValue(), fidouser, fkid.intValue()));
                    if (objectop != applianceConstants.REPLICATION_OPERATION_DELETE) {
                        // If objectop == DELETE, these values can be null so we only set them when objectop != DELETE
                        fidokeys.setAppid(fkproto.getAppid());
                        fidokeys.setCounter((int) fkproto.getCounter());
                        fidokeys.setCreateDate(new Date(fkproto.getCreateDate()));
                        fidokeys.setCreateLocation(fkproto.getCreateLocation());
                        fidokeys.setFidoProtocol(fkproto.getFidoProtocol());
                        fidokeys.setFidoVersion(fkproto.getFidoVersion());
                        fidokeys.setKeyhandle(fkproto.getKeyhandle());
                        fidokeys.setPublickey(fkproto.getPublickey());
                        fidokeys.setStatus(fkproto.getStatus());

                        if(fkproto.hasUserid()) fidokeys.setUserid(fkproto.getUserid());
                        if(fkproto.hasModifyDate()) fidokeys.setModifyDate(new Date(fkproto.getModifyDate()));
                        if(fkproto.hasModifyLocation()) fidokeys.setModifyLocation(fkproto.getModifyLocation());
                        if(fkproto.hasSignature()) fidokeys.setSignature(fkproto.getSignature());
                        if(fkproto.hasSignatureKeytype()){
                            fidokeys.setSignatureKeytype(fkproto.getSignatureKeytype());
                        }else{
                            fidokeys.setSignatureKeytype("RSA");
                        }
                        if(fkproto.hasTransports()) fidokeys.setTransports((short)fkproto.getTransports());
                        if(fkproto.hasAttsid()) fidokeys.setAttsid((short)fkproto.getAttsid());
                        if(fkproto.hasAttdid()) fidokeys.setAttdid((short)fkproto.getAttdid());
                        if(fkproto.hasAttcid()) fidokeys.setAttcid((int)fkproto.getAttcid());
                        if(fkproto.hasAaguid()) fidokeys.setAaguid(fkproto.getAaguid());
                        if(fkproto.hasRegistrationSettings()) fidokeys.setRegistrationSettings(fkproto.getRegistrationSettings());
                        if(fkproto.hasRegistrationSettingsVersion()) fidokeys.setRegistrationSettingsVersion((int) fkproto.getRegistrationSettingsVersion());
                    }
                } catch (IllegalArgumentException | SKIllegalArgumentException | InvalidProtocolBufferException ex) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6009", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey  + "]");
                    Logger.getLogger(persistSKCEObject.class.getName()).log(Level.SEVERE, null, ex);
                    isValid = false;
                    // Break from switch since we have an error in the proto message
                    break;
                }

                // Search for object in local DB if it exists
                fkresult = em.find(FidoKeys.class, fidokeys.getFidoKeysPK());
                // If not found, what operation are we performing?
                if (fkresult == null) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-6035", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                    switch (objectop) {
                        case applianceConstants.REPLICATION_OPERATION_ADD:
                            em.persist(fidokeys);
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6036", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_DELETE:
                            // Object already deleted
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6025", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                            break;
                        default:
                            // Invalid operation on a non-existent object
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6010", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                            break;
                    }
                } else {
                    switch (objectop) {
                        case applianceConstants.REPLICATION_OPERATION_ADD:
                            // Error - it already exists
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6011", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_UPDATE:
                            em.merge(fidokeys);
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6037", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_DELETE:
                            em.remove(fkresult);
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6038", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                            break;
                        default:
                            break;
                    }
                }
                break;

                /*
             * All operations allowed for Fido Users
             */
            case applianceConstants.ENTITY_TYPE_FIDO_USERS:
                // In case the user has a hypen, only split in three
                pkarray = objectpk.split("-", 3);

                sid = Long.parseLong(pkarray[0]);
                did = Long.parseLong(pkarray[1]);
                fidouser = pkarray[2];
                pkey = "SID-DID-FIDOUSER=" + sid + "-" + did + "-" + fidouser;

                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-6034", repobjpk + " containing ENTITY_TYPE_FIDOUSER [" + pkey + "]");

                // Parse Proto object and create entitybean
                ZMQSKCEReplicationProtos.FidoUsers fuproto;
                try {
                    fuproto = ZMQSKCEReplicationProtos.FidoUsers.parseFrom(msg);
                    fidousers = new FidoUsers();
                    fidousers.setFidoUsersPK(new FidoUsersPK(sid.shortValue(), did.shortValue(), fidouser));
                    if (objectop != applianceConstants.REPLICATION_OPERATION_DELETE) {
                        // If objectop == DELETE, these values can be null so we only set them when objectop != DELETE
                        fidousers.setFidoKeysEnabled(fuproto.getFidoKeysEnabled());
                        fidousers.setStatus(fuproto.getStatus());
                        fidousers.setTwoStepVerification(fuproto.getTwoStepVerification());

                        if(fuproto.hasPrimaryEmail()) fidousers.setPrimaryEmail(fuproto.getPrimaryEmail());
                        if(fuproto.hasRegisteredEmails()) fidousers.setRegisteredEmails(fuproto.getRegisteredEmails());
                        if(fuproto.hasPrimaryPhoneNumber()) fidousers.setPrimaryPhoneNumber(fuproto.getPrimaryPhoneNumber());
                        if(fuproto.hasRegisteredPhoneNumbers()) fidousers.setRegisteredPhoneNumbers(fuproto.getRegisteredPhoneNumbers());
                        if(fuproto.hasSignature()) fidousers.setSignature(fuproto.getSignature());
                        if(fuproto.hasTwoStepTarget()) fidousers.setTwoStepTarget(fuproto.getTwoStepTarget());
                        if(fuproto.hasUserdn()) fidousers.setUserdn(fuproto.getUserdn());
                    }
                } catch (IllegalArgumentException | SKIllegalArgumentException | InvalidProtocolBufferException ex) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6009", "Constants.ENTITY_TYPE_FIDOUSERS [" + pkey  + "]");
                    Logger.getLogger(persistSKCEObject.class.getName()).log(Level.SEVERE, null, ex);
                    isValid = false;
                    // Break from switch since we have an error in the proto message
                    break;
                }

                // Search for object in local DB if it exists
                furesult = em.find(FidoUsers.class, fidousers.getFidoUsersPK());
                // If not found, what operation are we performing?
                if (furesult == null) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-6035", "Constants.ENTITY_TYPE_FIDOUSERS [" + pkey + "]");
                    if (objectop == applianceConstants.REPLICATION_OPERATION_ADD) {
                        em.persist(fidousers);
                        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6036", "Constants.ENTITY_TYPE_FIDOUSERS [" + pkey + "]");
                    } else {
                        // Invalid operation on a non-existent object
                        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6010", "Constants.ENTITY_TYPE_FIDOUSERS [" + pkey + "]");
                    }
                } else {
                    switch (objectop) {
                        case applianceConstants.REPLICATION_OPERATION_ADD:
                            // Error - it already exists
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6011", "Constants.ENTITY_TYPE_FIDOUSERS [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_UPDATE:
                            em.merge(fidousers);
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6037", "Constants.ENTITY_TYPE_FIDOUSERS [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_DELETE:
                            em.remove(furesult);
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6038", "Constants.ENTITY_TYPE_FIDOUSERS [" + pkey + "]");
                            break;
                        default:
                            break;
                    }
                }
                break;

            case applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO:

                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-6034", repobjpk + " containing ENTITY_TYPE_MAP_USER_SESSION_INFO [" + objectpk + "]");

                // Parse Proto object and create entitybean
                ZMQSKCEReplicationProtos.UserSessionInfo usiproto;
                try {
                    usiproto = ZMQSKCEReplicationProtos.UserSessionInfo.parseFrom(msg);
                    usersessioninfo = new UserSessionInfo();
                    usersessioninfo.setUsername(usiproto.getUsername());
                    usersessioninfo.setNonce(usiproto.getNonce());
                    usersessioninfo.setAppid(usiproto.getAppid());
                    usersessioninfo.setSessiontype(usiproto.getSessiontype());
                    usersessioninfo.setCreationdate(new Date());
                    usersessioninfo.setUserPublicKey(usiproto.getUserPublicKey());
                    usersessioninfo.setMapkey(usiproto.getMapkey());

                    if(usiproto.hasDisplayName()) usersessioninfo.setDisplayName(usiproto.getDisplayName());
                    if(usiproto.hasRpName()) usersessioninfo.setRpName(usiproto.getRpName());
                    if(usiproto.hasFkid()) usersessioninfo.setFkid((int) usiproto.getFkid());
                    if(usiproto.hasSkid()) usersessioninfo.setSkid((short) usiproto.getSkid());
                    if(usiproto.hasSid()) usersessioninfo.setSid((short) usiproto.getSid());
                    if(usiproto.hasSessionid()) usersessioninfo.setSessionid(usiproto.getSessionid());
                    if(usiproto.hasUserid()) usersessioninfo.setUserId(usiproto.getUserid());
                    if(usiproto.hasUsericon()) usersessioninfo.setUserIcon(usiproto.getUsericon());
                    if(usiproto.hasFidopolicymapkey()) usersessioninfo.setPolicyMapKey(usiproto.getFidopolicymapkey());
                    if(usiproto.hasAttestationPreferance()) usersessioninfo.setAttestationPreferance(usiproto.getAttestationPreferance());
                    if(usiproto.hasUserVerificationReq()) usersessioninfo.setuserVerificationReq(usiproto.getUserVerificationReq());
                } catch (IllegalArgumentException | SKIllegalArgumentException | InvalidProtocolBufferException ex) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6009", "Constants.ENTITY_TYPE_MAP_USER_SESSION_INFO [" + objectpk  + "]");
                    Logger.getLogger(persistSKCEObject.class.getName()).log(Level.SEVERE, null, ex);
                    // Break from switch since we have an error in the proto message
                    isValid = false;
                    break;
                }

                if (objectop == applianceConstants.REPLICATION_OPERATION_HASHMAP_ADD) {
                    skceMaps.getMapObj().put(skceConstants.MAP_USER_SESSION_INFO, usersessioninfo.getMapkey(), usersessioninfo);
                    if(skceMaps.getMapObj().containsKey(skceConstants.MAP_USER_SESSION_INFO, usersessioninfo.getMapkey())){
                        System.out.println(" containing ENTITY_TYPE_MAP_USER_SESSION_INFO [" + objectpk + "] has been added to map");
                    }
                }
                break;

                /*
             * Only ADD and UPDATE allowed for Domains
             */
            case applianceConstants.ENTITY_TYPE_DOMAINS:
                // Figure out primary key from objectpk
                pkarray = objectpk.split("-");
                did = Long.parseLong(pkarray[0]);
                pkey = "DID=" + did;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-6034", repobjpk + " containing ENTITY_TYPE_DOMAINS [" + pkey + "]");

                // Parse Proto object and create entitybean
                ZMQSKCEReplicationProtos.Domains dproto;
                try {
                    dproto = ZMQSKCEReplicationProtos.Domains.parseFrom(msg);
                    domains = new Domains();
                    domains.setDid(did);
                    domains.setName(dproto.getName());
                    domains.setReplicationStatus(dproto.getReplicationStatus());
                    domains.setStatus(dproto.getStatus());
                    // Set optional values if not null
                    if (dproto.hasEncryptionCertificate()) domains.setEncryptionCertificate(dproto.getEncryptionCertificate());
                    if (dproto.hasEncryptionCertificateUuid()) domains.setEncryptionCertificateUuid(dproto.getEncryptionCertificateUuid());
                    if (dproto.hasNotes()) domains.setNotes(dproto.getNotes());
                    if (dproto.hasSigningCertificate()) domains.setSigningCertificate(dproto.getSigningCertificate());
                    if (dproto.hasSigningCertificateUuid()) domains.setSigningCertificateUuid(dproto.getSigningCertificateUuid());
                    if (dproto.hasSkceSigningdn()) domains.setSkceSigningdn(dproto.getSkceSigningdn());
                    if (dproto.hasSkfeAppid()) domains.setSkfeAppid(dproto.getSkfeAppid());
                } catch (IllegalArgumentException | SKIllegalArgumentException | InvalidProtocolBufferException ex) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6009", "Constants.ENTITY_TYPE_DOMAINS [" + pkey  + "]");
                    Logger.getLogger(persistSKCEObject.class.getName()).log(Level.SEVERE, null, ex);
                    isValid = false;
                    // Break from switch since we have an error in the proto message
                    break;
                }

                // Search for object in local DB if it exists
                dmresult = em.find(Domains.class, did);
                // If found, what operation are we performing?
                if (dmresult == null) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "execute", "SKCE-MSG-6035", "Constants.ENTITY_TYPE_DOMAINS [" + pkey  + "]");
                    if (objectop == applianceConstants.REPLICATION_OPERATION_ADD) {
                        em.persist(domains);
                        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6036", "Constants.ENTITY_TYPE_DOMAINS [" + pkey + "]");
                        applianceMaps.putDomain(did, domains);
                    } else {
                        // Invalid operation
                        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6012", "Constants.ENTITY_TYPE_DOMAINS [" + pkey + "]");
                    }
                } else {
                    if (objectop == applianceConstants.REPLICATION_OPERATION_ADD) {
                        // Error - it already exists
                        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "execute", "SKCE-ERR-6011", "Constants.ENTITY_TYPE_DOMAINS [" + pkey + "]");
                    } else if (objectop == applianceConstants.REPLICATION_OPERATION_UPDATE) {
                        em.merge(domains);
                        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "execute", "SKCE-MSG-6037", "Constants.ENTITY_TYPE_DOMAINS [" + pkey + "]");
                        applianceMaps.putDomain(did, domains);
                    }
                }
                break;

            /*
             * All operations allowed for Fido Policies
             */
            case applianceConstants.ENTITY_TYPE_FIDO_POLICIES:
                // In case the user has a hypen, only split in three
                pkarray = objectpk.split("-", 3);

                sid = Long.parseLong(pkarray[0]);
                did = Long.parseLong(pkarray[1]);
                pid = Long.parseLong(pkarray[2]);
                pkey = "SID-DID-FIDOPOLICY=" + sid + "-" + did + "-" + pid;

                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6034", repobjpk + " containing ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");

                // Parse Proto object and create entitybean
                ZMQSKCEReplicationProtos.FidoPolicies policyproto;
                try {
                    policyproto = ZMQSKCEReplicationProtos.FidoPolicies.parseFrom(msg);
                    fidopolicies = new FidoPolicies();
                    fidopolicies.setFidoPoliciesPK(new FidoPoliciesPK(sid.shortValue(), did.shortValue(), pid.shortValue()));
                    if (objectop != applianceConstants.REPLICATION_OPERATION_DELETE) {
                        // If objectop == DELETE, these values can be null so we only set them when objectop != DELETE

                        fidopolicies.setPolicy(policyproto.getPolicy());

                        fidopolicies.setStatus(policyproto.getStatus());
                        fidopolicies.setCreateDate(new Date(policyproto.getCreateDate()));


                        if (policyproto.hasNotes()) {
                            fidopolicies.setNotes(policyproto.getNotes());
                        }
                        if (policyproto.hasModifyDate()) {
                            fidopolicies.setModifyDate(new Date(policyproto.getModifyDate()));
                        }
                        if (policyproto.hasSignature()) {
                            fidopolicies.setSignature(policyproto.getSignature());
                        }
                    }
                } catch (IllegalArgumentException | SKIllegalArgumentException | InvalidProtocolBufferException ex) {
                    strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6009", "Constants.ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");
                    Logger.getLogger(persistSKCEObject.class.getName()).log(Level.SEVERE, null, ex);
                    isValid = false;
                    // Break from switch since we have an error in the proto message
                    break;
                }

                // Search for object in local DB if it exists
                fpresult = em.find(FidoPolicies.class, fidopolicies.getFidoPoliciesPK());
                // If not found, what operation are we performing?
                String fpMapkey = sid + "-" + did + "-" + pid;
                try {
                    if (fpresult == null) {
                        strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6035", "Constants.ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");
                        if (objectop == applianceConstants.REPLICATION_OPERATION_ADD) {
                            em.persist(fidopolicies);
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.INFO, classname, "execute", "SKCE-MSG-6036", "Constants.ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");
                            FidoPolicyObject fidoPolicyObject = FidoPolicyObject.parse(
                                    fidopolicies.getPolicy(),
                                    (long) fidopolicies.getFidoPoliciesPK().getDid(),
                                    (long) fidopolicies.getFidoPoliciesPK().getSid(),
                                    (long) fidopolicies.getFidoPoliciesPK().getPid());
                            MDSClient mds = null;

                            skceMaps.getMapObj().put(skceConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));
                        } else {
                            // Invalid operation on a non-existent object
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6010", "Constants.ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");
                        }
                    } else {
                        switch (objectop) {
                            case applianceConstants.REPLICATION_OPERATION_ADD:
                                // Error - it already exists
                                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6011", "Constants.ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");
                                break;
                            case applianceConstants.REPLICATION_OPERATION_UPDATE:
                                em.merge(fidopolicies);
                                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.INFO, classname, "execute", "SKCE-MSG-6037", "Constants.ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");
                                
                                FidoPolicyObject fidoPolicyObject = FidoPolicyObject.parse(
                                        fidopolicies.getPolicy(),

                                        (long) fidopolicies.getFidoPoliciesPK().getDid(),
                                        (long) fidopolicies.getFidoPoliciesPK().getSid(),
                                        (long) fidopolicies.getFidoPoliciesPK().getPid());
                                MDSClient mds = null;

                                skceMaps.getMapObj().put(skceConstants.MAP_FIDO_POLICIES, fpMapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));
                                break;
                            case applianceConstants.REPLICATION_OPERATION_DELETE:
                                em.remove(fpresult);
                                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.INFO, classname, "execute", "SKCE-MSG-6038", "Constants.ENTITY_TYPE_FIDOPOLICY [" + pkey + "]");
                                skceMaps.getMapObj().remove(skceConstants.MAP_FIDO_POLICIES, fpMapkey);
                                break;
                            default:
                                break;
                        }
                    }
                }
                catch(SKFEException ex){
                    strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "FIDO-ERR-0038", ex.getLocalizedMessage());
                }
                break;

            /*
             * All operations allowed for Fido Policies
             */
            case applianceConstants.ENTITY_TYPE_ATTESTATION_CERTIFICATES:
                // In case the user has a hypen, only split in three
                pkarray = objectpk.split("-", 3);

                sid = Long.parseLong(pkarray[0]);
                did = Long.parseLong(pkarray[1]);
                attcid = Long.parseLong(pkarray[2]);
                pkey = "SID-DID-ATTCERT=" + sid + "-" + did + "-" + attcid;

                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6034", repobjpk + " containing ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");

                // Parse Proto object and create entitybean
                ZMQSKCEReplicationProtos.AttestationCertificates attestationproto;
                try {
                    attestationproto = ZMQSKCEReplicationProtos.AttestationCertificates.parseFrom(msg);
                    attestationcertificates = new AttestationCertificates();
                    attestationcertificates.setAttestationCertificatesPK(
                            new AttestationCertificatesPK(sid.shortValue(), did.shortValue(), attcid.shortValue()));
                    if (objectop != applianceConstants.REPLICATION_OPERATION_DELETE) {
                        // If objectop == DELETE, these values can be null so we only set them when objectop != DELETE
                        attestationcertificates.setCertificate(attestationproto.getCertificate());
                        attestationcertificates.setIssuerDn(attestationproto.getIssuerDn());
                        attestationcertificates.setSubjectDn(attestationproto.getSubjectDn());
                        attestationcertificates.setSerialNumber(attestationproto.getSerialNumber());

                        if (attestationproto.hasParentSid()) {
                            attestationcertificates.setParentSid((short) attestationproto.getParentSid());
                        }
                        if (attestationproto.hasParentDid()) {
                            attestationcertificates.setParentDid((short) attestationproto.getParentDid());
                        }
                        if (attestationproto.hasParentAttcid()) {
                            attestationcertificates.setParentAttcid((int) attestationproto.getParentAttcid());
                        }
                        if (attestationproto.hasSignature()) {
                            attestationcertificates.setSignature(attestationproto.getSignature());
                        }
                    }
                } catch (IllegalArgumentException | SKIllegalArgumentException | InvalidProtocolBufferException ex) {
                    strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6009", "Constants.ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");
                    Logger.getLogger(persistSKCEObject.class.getName()).log(Level.SEVERE, null, ex);
                    isValid = false;
                    // Break from switch since we have an error in the proto message
                    break;
                }

                // Search for object in local DB if it exists
                acresult = em.find(AttestationCertificates.class, attestationcertificates.getAttestationCertificatesPK());
                // If not found, what operation are we performing?
                if (acresult == null) {
                    strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6035", "Constants.ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");
                    if (objectop == applianceConstants.REPLICATION_OPERATION_ADD) {
                        em.persist(attestationcertificates);
                        strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.INFO, classname, "execute", "SKCE-MSG-6036", "Constants.ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");
                    } else {
                        // Invalid operation on a non-existent object
                        strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6010", "Constants.ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");
                    }
                } else {
                    switch (objectop) {
                        case applianceConstants.REPLICATION_OPERATION_ADD:
                            // Error - it already exists
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6011", "Constants.ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_UPDATE:
                            em.merge(attestationcertificates);
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.INFO, classname, "execute", "SKCE-MSG-6037", "Constants.ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_DELETE:
                            em.remove(acresult);
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.INFO, classname, "execute", "SKCE-MSG-6038", "Constants.ENTITY_TYPE_ATTESTATION_CERTIFICATES [" + pkey + "]");
                            break;
                        default:
                            break;
                    }
                }
                break;

            case applianceConstants.ENTITY_TYPE_FIDO_CONFIGURATIONS:
                pkarray = objectpk.split("-", 2);
                did = Long.parseLong(pkarray[0]);
                config_key = pkarray[1];
                pkey = "DID-CONFIG_KEY=" + did + "-" + config_key;
                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6034", repobjpk + " containing ENTITY_TYPE_FIDO_CONFIGURATION [" + pkey + "]");

                
                // Parse Proto object and create entitybean
                ZMQSKCEReplicationProtos.Configurations cfgproto;
                try {
                    cfgproto = ZMQSKCEReplicationProtos.Configurations.parseFrom(msg);
                    config = new Configurations();
                    config.setConfigurationsPK(new ConfigurationsPK(did, config_key));
                    config.setConfigValue(cfgproto.getConfigValue());
                    // Set optional values if not null
                    if (cfgproto.hasNotes()) config.setNotes(cfgproto.getNotes());
                } catch (IllegalArgumentException | InvalidProtocolBufferException ex) {
                    strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6009", "Constants.ENTITY_TYPE_CONFIGURATIONS [" + pkey + "]");
                    Logger.getLogger(persistSKCEObject.class.getName()).log(Level.SEVERE, null, ex);
                    isValid = false;
                    // Break from switch since we have an error in the proto message
                    break;
                }

                        // Search for object in local DB if it exists
                configresult = em.find(Configurations.class, config.getConfigurationsPK());
                Configurations[] configarray = new Configurations[1];
                configarray[0] = config;
                // If not found, what operation are we performing?
                if (configresult == null) {
                    strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6035", "applianceConstants.ENTITY_TYPE_CONFIGURATIONS [" + pkey + "]");
                    if (objectop == applianceConstants.REPLICATION_OPERATION_ADD) {
                        em.persist(config);
                        strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6036", "applianceConstants.ENTITY_TYPE_CONFIGURATIONS [" + pkey + "]");
                        //update map
                        SKFSCommon.putConfiguration(did, configarray);
                        skceCommon.putConfiguration(did, configarray);
                        if(config_key.equalsIgnoreCase("appl.cfg.property.service.ce.ldap.ldaptype")){
                            skceCommon.setldaptype(did, config.getConfigValue());
                        }
                        if(config_key.equalsIgnoreCase("ldape.cfg.property.service.ce.ldap.ldapdnsuffix")){
                            skceCommon.setdnSuffixConfigured(Boolean.TRUE);
                        }
                        if(config_key.equalsIgnoreCase("ldape.cfg.property.service.ce.ldap.ldapgroupsuffix")){
                            skceCommon.setgroupSuffixConfigured(Boolean.TRUE);
                        }
                    } else {
                        strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6010", "applianceConstants.ENTITY_TYPE_CONFIGURATIONS [" + pkey + "]");
                    }
                } else {
                    switch (objectop) {
                        case applianceConstants.REPLICATION_OPERATION_ADD:
                            // Error - it already exists
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6011", "applianceConstants.ENTITY_TYPE_CONFIGURATIONS [" + pkey + "]");
                            break;
                        case applianceConstants.REPLICATION_OPERATION_UPDATE:
                            em.merge(config);
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "SKCE-MSG-6037", "applianceConstants.ENTITY_TYPE_CONFIGURATIONS [" + pkey + "]");
                            //update map
                            if (config_key.equalsIgnoreCase("appl.cfg.property.service.ce.ldap.ldaptype")) {
                                skceCommon.setldaptype(did, config.getConfigValue());
                            }
                            if (config_key.equalsIgnoreCase("ldape.cfg.property.service.ce.ldap.ldapdnsuffix")) {
                                skceCommon.setdnSuffixConfigured(Boolean.TRUE);
                            }
                            if (config_key.equalsIgnoreCase("ldape.cfg.property.service.ce.ldap.ldapgroupsuffix")) {
                                skceCommon.setgroupSuffixConfigured(Boolean.TRUE);
                            }
                            SKFSCommon.putConfiguration(did, configarray);
                            skceCommon.putConfiguration(did, configarray);
                            break;
                        case applianceConstants.REPLICATION_OPERATION_DELETE:
                            em.remove(config);
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.INFO, classname, "execute", "SKCE-MSG-6038", "Constants.ENTITY_TYPE_FIDOKEYS [" + pkey + "]");
                            if (config_key.equalsIgnoreCase("appl.cfg.property.service.ce.ldap.ldaptype")) {
                                skceCommon.setldaptype(did, applianceCommon.getApplianceConfigurationProperty(config_key));
                            }
                            if (config_key.equalsIgnoreCase("ldape.cfg.property.service.ce.ldap.ldapdnsuffix")) {
                                skceCommon.setdnSuffixConfigured(Boolean.FALSE);
                            }
                            if (config_key.equalsIgnoreCase("ldape.cfg.property.service.ce.ldap.ldapgroupsuffix")) {
                                skceCommon.setgroupSuffixConfigured(Boolean.FALSE);
                            }
                            SKFSCommon.removeConfiguration(did, configarray);
                            skceCommon.removeConfiguration(did, configarray);
                            break;
                        default:
                            // Invalid operation
                            strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.WARNING, classname, "execute", "SKCE-ERR-6038", "applianceConstants.ENTITY_TYPE_CONFIGURATIONS [" + pkey + "]");
                            break;
                    }
                }
                break;

            default:
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.SEVERE, classname, "execute", "SKCE-ERR-6008", objectype);
                return false;

        }
        return isValid;
    }

        @Override
    public boolean remoteExecute(String repobjpk, int objectype, int objectop, String objectpk, byte[] object) {
        return execute(repobjpk, objectype, objectop, objectpk, object);
    }
}
