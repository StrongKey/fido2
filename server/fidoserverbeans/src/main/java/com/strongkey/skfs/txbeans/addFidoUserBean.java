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
import com.strongkey.skfs.entitybeans.FidoUsersPK;
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


@Stateless
public class addFidoUserBean implements addFidoUserBeanLocal {

     /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    @Resource private SessionContext            sc;
    @PersistenceContext private EntityManager   em;

    @EJB
    getFidoUserLocal getfidoUserbean;
    @EJB
    replicateSKFEObjectBeanLocal replObj;
    @EJB
    getDomainsBeanLocal getdomain;

    @Override
    public String execute(Long did, String username) throws SKFEException {

        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "execute");

        //Json return object
        JsonObject retObj;

        //Declaring variables
        Boolean status = true;
        String errmsg;

        //Input Validation
         if(did == null){
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "did");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " did";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        } else if (did < 1) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "did");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " did";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
         SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "did=" + did);

        //USERNAME
        if (username == null) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "USERNAME");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " USERNAME";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        } else if (username.trim().length() == 0) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1003", "USERNAME");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1003") + " USERNAME";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        } else if (username.trim().length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.256charstring")) {
            status = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "USERNAME");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " USERNAME";
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "USERNAME=" + username);

        FidoUsers fidoUser = null;
        try {
            fidoUser = getfidoUserbean.getByUsername(did,username);
        } catch (SKFEException ex) {
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDO-ERR-0001")+ ex.getLocalizedMessage());
        }
        if(fidoUser != null){
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2004","");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-2004");
            status = false;
            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
            return retObj.toString();
        }

        Long sid = applianceCommon.getServerId();
        String primarykey = sid+"-"+did+"-"+username;
        fidoUser = new FidoUsers();
        FidoUsersPK fpk = new FidoUsersPK(sid.shortValue(), did.shortValue(), username);
        fidoUser.setFidoUsersPK(fpk);
        fidoUser.setTwoStepVerification(String.valueOf(false));
        fidoUser.setFidoKeysEnabled(String.valueOf(false));
        fidoUser.setPrimaryEmail("");
        fidoUser.setPrimaryPhoneNumber("");
        fidoUser.setRegisteredEmails("");
        fidoUser.setRegisteredPhoneNumbers("");
        fidoUser.setTwoStepTarget(null);
        fidoUser.setUserdn("");
        fidoUser.setStatus("Active");
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
//            status = false;
//            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "FK Xml");
//            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " FK Xml";
//            retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
//            return retObj.toString();
//            }
            Domains d = getdomain.byDid(did);
            //  get signature for the xml
            String signedxml = null;
            try {
                signedxml = initCryptoModule.getCryptoModule().signDBRow(did.toString(), d.getSkceSigningdn(), efsXml,Boolean.valueOf(standalone), signingKeystorePassword);
            } catch (CryptoException ex) {
                Logger.getLogger(addFidoUserBean.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (signedxml == null) {
                status = false;
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "SignedXML");
                errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " SignedXML";
                retObj = Json.createObjectBuilder().add("status", status).add("message", errmsg).build();
                return retObj.toString();
            } else {
                fidoUser.setSignature(signedxml);
            }
        }

        em.persist(fidoUser);
        em.flush();
        em.clear();

        try {
            if(applianceCommon.replicate()) {
                if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                    String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_USERS, applianceConstants.REPLICATION_OPERATION_ADD, primarykey, fidoUser);
                    if (response != null) {
                        return response;
                    }
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }

        //return a successful json string
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER,Level.INFO, classname, "execute", "FIDOJPA-MSG-2008","");
        retObj = Json.createObjectBuilder().add("status", status).add("message", SKFSCommon.getMessageProperty("FIDOJPA-MSG-2008")).build();
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "execute");
        return retObj.toString();
    }
}
