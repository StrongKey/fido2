/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.entitybeans.Domains;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.crypto.interfaces.initCryptoModule;
import com.strongkey.crypto.utility.CryptoException;
import com.strongkey.skfs.entitybeans.FidoUsers;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

/**
 * EJB to perform ldap based user or group's attribute value changes
 */
@Stateless
public class updateFidoUserBean implements updateFidoUserBeanLocal {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    @PersistenceContext
    private EntityManager em;

    @Resource
    private SessionContext sc;

    @EJB
    replicateSKFEObjectBeanLocal replObj;

    @EJB
    getFidoUserLocal getFidoUserbean;
    @EJB
    addFidoUserBeanLocal addfidouserbean;
    @EJB
    getFidoKeysLocal getfidokeysbean;
    @EJB
    getDomainsBeanLocal getdomain;

    /**
     * ***********************************************************************
     * 888 888 888 .d88b. 888 888 .d88b. .d8888b 888 888 888888 .d88b. d8P Y8b
     * `Y8bd8P' d8P Y8b d88P" 888 888 888 d8P Y8b 88888888 X88K 88888888 888 888
     * 888 888 88888888 Y8b. .d8""8b. Y8b. Y88b. Y88b 888 Y88b. Y8b. "Y8888 888
     * 888 "Y8888 "Y8888P "Y88888 "Y888 "Y8888 *
     * ***********************************************************************
     */
    /**
     * This method updates the value of a specific ldap user key to a new value
     * provided. This method does not perform any authentication or
     * authorization for the user against ldap. If present in ldap, this method
     * will look for the given key presence and will update it with new value.
     *
     * @param did - short, domain id
     * @param username - String containing name of the ldap user
     * @param key - String containing the name of the user key in ldap
     * @param value - String containing the new value for the user key in ldap
     * @param deletion - boolean that indicates if it is a delete operation for
     * the LDAP attribute (key). Delete means the new value applied to the key
     * is null
     * @return boolean based on if the operation is successful or not
     * @throws SKFEException in the event there is an error of any kind.
     */
    @Override
    public String execute(Long did,
            String username,
            String key,
            String value,
            boolean deletion) throws SKFEException {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER, classname, "execute");

        //Json return object
        JsonObject retObj;

        //Declaring variables
        Boolean status = true;
        String errmsg;

        //  Inputs check
        SKFSCommon.inputValidateSKCEDid(Long.toString(did));
        if (username == null || username.trim().isEmpty()
                || key == null || key.trim().isEmpty()
                || value == null) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " ";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        if (!key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_2STEPVERIFY) && !key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_FIDOENABLED) && !key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_DEFAULTTARGET)
                && !key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_EMAILADDRESSES) && !key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_PHONENUMBERS) && !key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_PRIMARYEMAIL)
                && !key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_PRIMARYPHONE)) {
            throw new SKFEException("SKCEWS-ERR-3018: Invalid value for ldap key; ");
        }

        //  checks for deletion - There could be other attributes which are never
        // deletable, need to add them to the list
        if ((key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_2STEPVERIFY)
                || key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_FIDOENABLED))
                && deletion) {
            throw new SKFEException("SKCEWS-ERR-3018: Invalid value for ldap key; "
                    + " Value for " + key + " can not be deleted");
        }

        if (!deletion) {
            //  Checks on the new value for the key.
            if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_2STEPVERIFY)) {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new SKFEException("SKCEWS-ERR-3018: Invalid value for ldap key; "
                            + " Value for " + SKFSConstants.LDAP_ATTR_KEY_2STEPVERIFY
                            + " has to be one of 'true' or 'false'");
                }
            } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_FIDOENABLED)) {
                if (!value.equalsIgnoreCase("true") && !value.equalsIgnoreCase("false")) {
                    throw new SKFEException("SKCEWS-ERR-3018: Invalid value for ldap key; "
                            + " Value for " + SKFSConstants.LDAP_ATTR_KEY_FIDOENABLED
                            + " has to be one of 'true' or 'false'");
                }
            } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_DEFAULTTARGET)) {
                if (!value.equalsIgnoreCase("email") && !value.equalsIgnoreCase("phone")) {
                    throw new SKFEException("SKCEWS-ERR-3018: Invalid value for ldap key; "
                            + " Value for " + SKFSConstants.LDAP_ATTR_KEY_DEFAULTTARGET
                            + " has to be one of 'email' or 'phone'");
                }
            }
        }

        //get fido user
        FidoUsers fidoUser;
        try {
            fidoUser = getFidoUserbean.getByUsername(did, username);
            if (fidoUser == null) {
//                if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_2STEPVERIFY)) {
                addfidouserbean.execute(did, username);
                fidoUser = getFidoUserbean.getByUsername(did, username);
//                    SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
//                    return response;
//                } //add user with the property
//                else {
//                    SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.SEVERE, "FIDO-ERR-0036", "");
//                    throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0036"));
//                }
            }
        } catch (Exception ex) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0001", ex.getLocalizedMessage());
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0001") + ex.getLocalizedMessage());
        }

        if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_2STEPVERIFY)) {
            fidoUser.setTwoStepVerification(value);
        } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_FIDOENABLED)) {
            //check if keys exist for user
            if (value.equalsIgnoreCase("false")) {
                if (getfidokeysbean.getByUsernameStatus(did, username, applianceConstants.ACTIVE_STATUS).size() > 0) {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-6001", "");
                } else {
                    fidoUser.setFidoKeysEnabled(value);
                }
            } else {
                fidoUser.setFidoKeysEnabled(value);
            }

        } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_DEFAULTTARGET)) {
            if (deletion) {
                fidoUser.setTwoStepTarget(null);
            } else {
                fidoUser.setTwoStepTarget(value);
            }
        } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_EMAILADDRESSES)) {
            if (deletion) {
                fidoUser.setRegisteredEmails("");
            } else {
                fidoUser.setRegisteredEmails(value);
            }
        } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_PHONENUMBERS)) {
            if (deletion) {
                fidoUser.setRegisteredPhoneNumbers("");
            } else {
                fidoUser.setRegisteredPhoneNumbers(value);
            }
        } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_PRIMARYEMAIL)) {
            if (deletion) {
                fidoUser.setPrimaryEmail("");
            } else {
                fidoUser.setPrimaryEmail(value);
            }
        } else if (key.equalsIgnoreCase(SKFSConstants.LDAP_ATTR_KEY_PRIMARYPHONE)) {
            if (deletion) {
                fidoUser.setPrimaryPhoneNumber("");
            } else {
                fidoUser.setPrimaryPhoneNumber(value);
            }
        }
        String primarykey = fidoUser.getFidoUsersPK().getSid() + "-" + fidoUser.getFidoUsersPK().getDid() + "-" + fidoUser.getFidoUsersPK().getUsername();
        fidoUser.setId(primarykey);

        if (SKFSCommon.getConfigurationProperty("skfs.cfg.property.db.signature.rowlevel.add")
                .equalsIgnoreCase("true")) {

            String standalone = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.fidoengine");
            String signingKeystorePassword = "";
            if (standalone.equalsIgnoreCase("true")) {
                signingKeystorePassword = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.signingkeystore.password");
            }
            //  convert the java object into xml to get it signed.
            StringWriter writer = new StringWriter();
            JAXBContext jaxbContext;
            Marshaller marshaller;
            try {
                jaxbContext = JAXBContext.newInstance(FidoUsers.class);
                marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(fidoUser, writer);
            } catch (javax.xml.bind.JAXBException ex) {

            }
            String efsXml = writer.toString();
//            String efsXml = fidoUser.toJsonObject();
//            if (efsXml == null) {
//                status = false;
//                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "FK Xml");
//                errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " FK Xml";
//                retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
//                return retObj.toString();
//            }
            //  get signature for the xml
            Domains d = getdomain.byDid(did);

            String signedxml = null;
            try {
                signedxml = initCryptoModule.getCryptoModule().signDBRow(did.toString(), d.getSkceSigningdn(), efsXml, Boolean.valueOf(standalone), signingKeystorePassword);
            } catch (CryptoException ex) {
                Logger.getLogger(updateFidoUserBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (signedxml == null) {
                status = false;
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "SignedXML");
                errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " SignedXML";
                retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
                return retObj.toString();
            } else {
                fidoUser.setSignature(signedxml);
            }
        }

        em.merge(fidoUser);
        em.flush();

        try {
            if (applianceCommon.replicate()) {
                if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                    String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_USERS, applianceConstants.REPLICATION_OPERATION_UPDATE, primarykey, fidoUser);
                    if (response != null) {
                        return response;
                    }
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }

        //return a success message
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2010", "");
        retObj = Json.createObjectBuilder().add("status", status).add("message", SKFSCommon.getMessageProperty("FIDOJPA-MSG-2010")).build();
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
        return retObj.toString();
    }
}
