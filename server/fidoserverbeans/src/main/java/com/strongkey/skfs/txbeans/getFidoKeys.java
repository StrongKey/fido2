/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.entitybeans.Domains;
import com.strongkey.crypto.interfaces.initCryptoModule;
import com.strongkey.crypto.utility.CryptoException;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.oldentitybean.FidoKeysPK;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

@Stateless
public class getFidoKeys implements getFidoKeysLocal {

    /**
     ** This class's name - used for logging & not persisted
     *
     */
    private final String classname = this.getClass().getName();

    final private String SIGN_SUFFIX = SKFSCommon.getConfigurationProperty("skfs.cfg.property.signsuffix");
    /**
     * Persistence context for derby
     */
    @PersistenceContext
    private EntityManager em;

    @EJB
    getDomainsBeanLocal getdomejb;

    /**
     *
     * @param did
     * @param username - Name of the user whose keys need to be fetched.
     * @return - List of keys for the supplied username
     */
    @Override
    public Collection<FidoKeys> getByUsername(Long did, String username) throws SKFEException {
        try {
            TypedQuery<FidoKeys> q = em.createNamedQuery("FidoKeys.findByUsername", FidoKeys.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("username", username);
            q.setParameter("did", did);
            Collection<FidoKeys> fidoKeysColl = q.getResultList();
            Collection<FidoKeys> validFidoKeysColl = fidoKeysColl;
            if (!fidoKeysColl.isEmpty()) {
                for (FidoKeys fk : fidoKeysColl) {
                    if (fk != null) {
                        try {
                            verifyDBRecordSignature(did, fk);
                        } catch (SKFEException ex) {
                            validFidoKeysColl.remove(fk);
                        }
                    }
                }
            }
            return validFidoKeysColl;
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     *
     * @param did
     * @return - all keys in the DB
     */
    @Override
    public Collection<FidoKeys> getAll(Long did) throws SKFEException {
        try {
            TypedQuery<FidoKeys> q = em.createNamedQuery("FidoKeys.findAllbyDid", FidoKeys.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("did", did);
            Collection<FidoKeys> fidoKeysColl = q.getResultList();
            Collection<FidoKeys> validFidoKeysColl = fidoKeysColl;
            if (!fidoKeysColl.isEmpty()) {
                for (FidoKeys fk : fidoKeysColl) {
                    if (fk != null) {
                        try {
                            verifyDBRecordSignature(did, fk);
                        } catch (SKFEException ex) {
                            validFidoKeysColl.remove(fk);
                        }
                    }
                }
            }
            return validFidoKeysColl;
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     *
     * @param did
     * @param username - Name of the user whose keys have been requested
     * @param KH - User Key Handle for the key to be retrieved
     * @return - Returns the unique registered key identified by the parameters
     * passed
     * @throws com.strongkey.skce.utilities.SKFEException
     */
    @Override
    public FidoKeys getByUsernameKH(Long did, String username, String KH) throws SKFEException {
        try {
            Query q = em.createNamedQuery("FidoKeys.findByUsernameKH");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("username", username);
            q.setParameter("did", did);
            q.setParameter("keyhandle", KH);
            FidoKeys rk = (FidoKeys) q.getSingleResult();
            if (rk != null) {
                verifyDBRecordSignature(did, rk);
            }
            return rk;
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     *
     * @param sid
     * @param did
     * @param username
     * @param fkid - Unique identifier for a key
     * @return - Returns a Key identified by the fkid parameter.
     */
    @Override
    public FidoKeys getByUsernamefkid(Short sid, Long did, String username, Long fkid) throws SKFEException {
        try {
            Query q = em.createNamedQuery("FidoKeys.findBySidDidUsernameFkid");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("fkid", fkid);
            q.setParameter("did", did);
            q.setParameter("username", username);
            q.setParameter("sid", sid);
            FidoKeys rk = (FidoKeys) q.getSingleResult();
            if (rk != null) {
                verifyDBRecordSignature(did, rk);
            }
            return rk;
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public FidoKeys getByfkid(Short sid, Long did, Long fkid) throws SKFEException {
        try {
            Query q = em.createNamedQuery("FidoKeys.findBySidDidFkid");
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("fkid", fkid);
            q.setParameter("did", did);
            q.setParameter("sid", sid);
            FidoKeys rk = (FidoKeys) q.getSingleResult();
            if (rk != null) {
                verifyDBRecordSignature(did, rk);
            }
            return rk;
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     *
     * @param did
     * @param username - Name of the user whose keys need to be fetched.
     * @param status - Status of keys that belong to the user.
     * @return - List of keys for the supplied username with the supplied
     * password
     */
    @Override
    public Collection<FidoKeys> getByUsernameStatus(Long did, String username, String status) throws SKFEException {
        try {
            TypedQuery<FidoKeys> q = em.createNamedQuery("FidoKeys.findByUsernameStatus", FidoKeys.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("username", username);
            q.setParameter("did", did);
            q.setParameter("status", status);
            Collection<FidoKeys> fidoKeysColl = q.getResultList();
            Collection<FidoKeys> validFidoKeysColl = new ArrayList<>();
            validFidoKeysColl.addAll(fidoKeysColl);
            if (!fidoKeysColl.isEmpty()) {
                for (FidoKeys fk : fidoKeysColl) {
                    if (fk != null) {
                        try {
                            verifyDBRecordSignature(did, fk);
                        } catch (SKFEException ex) {
                            validFidoKeysColl.remove(fk);
                        }
                    }
                }
            }
            return validFidoKeysColl;
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     *
     * @param did
     * @param username - Name of the user whose keys need to be fetched.
     * @param status - status of Fido key (Active vs Inactive)
     * @return - List of keys for the supplied username
     */
    @Override
    public FidoKeys getNewestKeyByUsernameStatus(Long did, String username, String status) throws SKFEException {
        try {
            Query q = em.createNamedQuery("FidoKeys.findNewestKeyByUsernameStatus", FidoKeys.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("username", username);
            q.setParameter("did", did);
            q.setParameter("status", status);

            List<FidoKeys> fkList = q.getResultList();
            if (fkList == null || fkList.isEmpty()) {
                return null;
            }
            FidoKeys rk = fkList.get(0);
            if (rk != null) {
                verifyDBRecordSignature(did, rk);
            }
            return rk;
        } catch (NoResultException ex) {
            return null;
        }
    }
    
    @Override
    public List<FidoKeys> getKeysByUsernameStatus(Long did, String username, String status) throws SKFEException {
        try {
            Query q = em.createNamedQuery("FidoKeys.findNewestKeyByUsernameStatus", FidoKeys.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("username", username);
            q.setParameter("did", did);
            q.setParameter("status", status);

            List<FidoKeys> fidoKeysColl = q.getResultList();
//            if (!fidoKeysColl.isEmpty()) {
//                for (FidoKeys fk : fidoKeysColl) {
//                    if (fk != null) {
//                        try {
//                            verifyDBRecordSignature(did, fk);
//                        } catch (SKFEException ex) {
//                            fidoKeysColl.remove(fk);
//                        }
//                    }
//                }
//            }
//            return fidoKeysColl;
            List<FidoKeys> validFidoKeysColl = new ArrayList<>();
            validFidoKeysColl.addAll(fidoKeysColl);
            if (!fidoKeysColl.isEmpty()) {
                for (FidoKeys fk : fidoKeysColl) {
                    if (fk != null) {
                        try {
                            verifyDBRecordSignature(did, fk);
                        } catch (SKFEException ex) {
                            validFidoKeysColl.remove(fk);
                        }
                    }
                }
            }
            return validFidoKeysColl;
        } catch (NoResultException ex) {
            return null;
        }
    }

    /**
     * Verifies the database row level signature of the given object and returns
     * successfully if verified and throws exception if not verified.
     *
     * @param did CDA domain id
     * @param er Object whose signature has to be verified
     * @throws CDAException
     */
    private void verifyDBRecordSignature(Long did, FidoKeys fk)
            throws SKFEException {
        if (fk != null) {
            if (SKFSCommon.getConfigurationProperty("skfs.cfg.property.db.signature.rowlevel.verify")
                    .equalsIgnoreCase("true")) {
                Domains d = getdomejb.byDid(did);
                String standalone = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.fidoengine");
                String signingKeystorePassword = "";
                if (standalone.equalsIgnoreCase("true")) {
                    signingKeystorePassword = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.signingkeystore.password");
                }

                String rsainput = "", ecinput="";
                String signingDN = "";
                String signatureType = fk.getSignatureKeytype();
                String documentid = fk.getFidoKeysPK().getSid()
                            + "-" + fk.getFidoKeysPK().getDid()
                            + "-" + fk.getFidoKeysPK().getUsername()
                            + "-" + fk.getFidoKeysPK().getFkid();
                    fk.setId(documentid);

                    //convert fk to old fk v1
                    com.strongkey.skfs.oldentitybean.FidoKeys fkv1 = new com.strongkey.skfs.oldentitybean.FidoKeys();
                    FidoKeysPK fkpkv1 = new FidoKeysPK();
                    fkpkv1.setSid(fk.getFidoKeysPK().getSid());
                    fkpkv1.setDid(fk.getFidoKeysPK().getDid());
                    fkpkv1.setFkid(fk.getFidoKeysPK().getFkid());
                    fkpkv1.setUsername(fk.getFidoKeysPK().getUsername());
                    fkv1.setFidoKeysPK(fkpkv1);
                    
                    fkv1.setUserid(fk.getUserid());
                    fkv1.setKeyhandle(fk.getKeyhandle());
                    fkv1.setPublickey(fk.getPublickey());
                    fkv1.setAppid(fk.getAppid());
                    fkv1.setTransports(fk.getTransports());
                    fkv1.setAttsid(fk.getAttsid());
                    fkv1.setAttdid(fk.getAttdid());
                    fkv1.setAttcid(fk.getAttcid());
                    fkv1.setCounter(fk.getCounter());
                    fkv1.setFidoVersion(fk.getFidoVersion());
                    fkv1.setFidoProtocol(fk.getFidoProtocol());
                    if (fk.getAaguid() != null) {
                        fkv1.setAaguid(fk.getAaguid());
                    }
                    if (fk.getRegistrationSettings() != null) {
                        fkv1.setRegistrationSettings(fk.getRegistrationSettings());
                    }
                    if (fk.getRegistrationSettingsVersion()!= null) {
                        fkv1.setRegistrationSettingsVersion(fk.getRegistrationSettingsVersion());
                    }
                    fkv1.setCreateLocation(fk.getCreateLocation());
                    fkv1.setCreateDate(fk.getCreateDate());
                    fkv1.setStatus(fk.getStatus());
                    fkv1.setId(fk.getId());
                   
                    //jaxB conversion
                    //converting the databean object to xml
                    StringWriter writer = new StringWriter();
                    JAXBContext jaxbContext;
                    Marshaller marshaller;
                    try {
                        jaxbContext = JAXBContext.newInstance(com.strongkey.skfs.oldentitybean.FidoKeys.class);
                        marshaller = jaxbContext.createMarshaller();
                        marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                        marshaller.marshal(fkv1, writer);
                        rsainput = writer.toString();
                    } catch (JAXBException ex) {
                        Logger.getLogger(getFidoKeys.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                    ecinput = fk.toJsonObject();
                    String input ="";
                if (signatureType.equalsIgnoreCase("RSA")) {
                    input = rsainput;
                    signingDN = "CN=SKFS Signing Key,OU=DID 1,OU=SKFS Signing Certificate 1,O=StrongKey";
                } else {
                    input = ecinput;
                    signingDN = d.getSkceSigningdn();
                }

                
                //  verify row level signature
                boolean verified = false;
                try {
//                    verified = initCryptoModule.getCryptoModule().verifyDBRow(did.toString(), writer.toString(), d.getSkceSigningdn(), Boolean.valueOf(standalone), signingKeystorePassword, fk.getSignature());
                    verified = initCryptoModule.getCryptoModule().verifyDBRow(did.toString(), input, signingDN, Boolean.valueOf(standalone), signingKeystorePassword, fk.getSignatureKeytype(), fk.getSignature());
                } catch (CryptoException ex) {
                    Logger.getLogger(getFidoKeys.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!verified) {
                    if (signatureType.equalsIgnoreCase("EC")) {
                        input = rsainput;
                        try {
                            verified = initCryptoModule.getCryptoModule().verifyDBRow(did.toString(), input, signingDN, Boolean.valueOf(standalone), signingKeystorePassword, fk.getSignatureKeytype(), fk.getSignature());
                        } catch (CryptoException ex) {
                            Logger.getLogger(getFidoKeys.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    if (!verified) {
                        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "verifyDBRecordSignature",
                                "SKCE-ERR-5001", "er sid-did-erqid="
                                + fk.getFidoKeysPK().getSid()
                                + "-" + fk.getFidoKeysPK().getDid()
                                + "-" + fk.getFidoKeysPK().getUsername()
                                + "-" + fk.getFidoKeysPK().getFkid());
                        throw new SKFEException(SKFSCommon.getMessageProperty("SKCE-ERR-5001")
                                + "fk sid-did-erqid="
                                + fk.getFidoKeysPK().getSid()
                                + "-" + fk.getFidoKeysPK().getDid()
                                + "-" + fk.getFidoKeysPK().getUsername()
                                + "-" + fk.getFidoKeysPK().getFkid());
                    }
                }
            }
        } else {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "verifyDBRecordSignature",
                    "FIDOJPA-ERR-1001", " er object");
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " fk object");
        }
    }
}
