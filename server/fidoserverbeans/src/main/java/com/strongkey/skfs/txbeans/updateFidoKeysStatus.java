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
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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

@Stateless
public class updateFidoKeysStatus implements updateFidoKeysStatusLocal {

    /**
     ** This class's name - used for logging & not persisted
     *
     */
    @SuppressWarnings("FieldMayBeFinal")
    private String classname = this.getClass().getName();

    /**
     * EJB's used by the Bean
     */
    @EJB
    getFidoKeysLocal getkeysejb;
    @EJB
    replicateSKFEObjectBeanLocal replObj;

    @EJB
    getDomainsBeanLocal getdomain;

    /**
     * Persistence context for derby
     */
    @Resource
    private SessionContext sc;
    @PersistenceContext
    private EntityManager em;

    /**
     *
     * @param sid
     * @param did
     * @param fkid - Unique identifier for the key in the DB
     * @param modify_location - Location where the key was last used.
     * @param status - Updated status of the Key
     * @return - Returns a JSON string containing the status and the
     * error/success message
     */
    @Override
//    public String execute(Short sid, Long did, String username, Long fkid, String modify_location, String status) {
    public String execute(Short sid, Long did, Long fkid, String modify_location, String status) {
        //Declaring variables
        Boolean outputstatus = true;
        String errmsg;
        JsonObject retObj;

        //Input Validation
        //sid
        //NULL Argument
        if (sid == null) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "sid");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " sid";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if (sid < 1) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "sid");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " sid";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "sid=" + sid);

        //did
        //NULL Argument
        if (did == null) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "did");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " did";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if (did < 1) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "did");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " did";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "did=" + did);

        //fkid
        //NULL Argument
        if (fkid == null) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "fkid");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " fkid";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }

        // fkid is negative, zero or larger than max value (becomes negative)
        if (fkid < 1) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "fkid");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " fkid";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "fkid=" + fkid);

        //USER modify_location
        if (modify_location == null) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "MODIFY LOCATION");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        } else if (modify_location.trim().length() == 0) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1003", "MODIFY LOCATION");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1003") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        } else if (modify_location.trim().length() > 255) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "MODIFY LOCATION");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " MODIFY LOCATION";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "MODIFY LOCATION=" + modify_location);

        //key status
        if (status == null) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "STATUS");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " STATUS";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        } else if (status.trim().length() == 0) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1003", "STATUS");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1003") + " STATUS";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }

        if (status.trim().equalsIgnoreCase(applianceConstants.ACTIVE_STATUS)) {
        } else if (status.trim().equalsIgnoreCase(applianceConstants.INACTIVE_STATUS)) {
        } else {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1002", "STATUS");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1002") + " STATUS";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "STATUS=" + status);

        //  Verify if the fkid exists.
        FidoKeys rk = null;
        try {
            FidoKeysInfo fkinfo = (FidoKeysInfo) skceMaps.getMapObj().get(SKFSConstants.MAP_FIDO_KEYS, sid + "-" + did + "-" + fkid);
            if (fkinfo != null) {
                rk = fkinfo.getFk();
            }
            if (rk == null) {
                rk = getkeysejb.getByfkid(sid, did, fkid);
            }
        } catch (SKFEException ex) {
            Logger.getLogger(updateFidoKeysStatus.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (rk == null) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-2002", "");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-2002");
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }

        //modify the DB
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String moddate = df.format(new Date());
        Date modifyDateFormat = null;
        try {
            modifyDateFormat = df
                    .parse(moddate);
        } catch (ParseException e) {
        }
        String primarykey = sid + "-" + did + "-" + rk.getFidoKeysPK().getUsername() + "-" + fkid;
        rk.setModifyLocation(modify_location);
        rk.setModifyDate(modifyDateFormat);
        rk.setStatus(status);
        rk.setId(primarykey);

        if (SKFSCommon.getConfigurationProperty("skfs.cfg.property.db.signature.rowlevel.add")
                .equalsIgnoreCase("true")) {

            String standalone = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.fidoengine");
            String signingKeystorePassword = "";
            if (standalone.equalsIgnoreCase("true")) {
                signingKeystorePassword = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.signingkeystore.password");
            }
            //  convert the java object into xml to get it signed.
//            StringWriter writer = new StringWriter();
//            JAXBContext jaxbContext;
//            Marshaller marshaller;
//            try {
//                jaxbContext = JAXBContext.newInstance(FidoKeys.class);
//                marshaller = jaxbContext.createMarshaller();
//                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
//                marshaller.marshal(rk, writer);
//            } catch (javax.xml.bind.JAXBException ex) {
//                Logger.getLogger(updateFidoKeysStatus.class.getName()).log(Level.SEVERE, null, ex);
//            }
//            String efsXml = writer.toString();
            String efsXml = rk.toJsonObject();
            if (efsXml == null) {
                outputstatus = false;
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "FK Xml");
                errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " FK Xml";
                retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
                return retObj.toString();
            }

            //  get signature for the xml
            Domains d = getdomain.byDid(did);

            String signedxml = null;
            try {
                signedxml = initCryptoModule.getCryptoModule().signDBRow(did.toString(), d.getSkceSigningdn(), efsXml, Boolean.valueOf(standalone), signingKeystorePassword);
            } catch (CryptoException ex) {
                Logger.getLogger(updateFidoKeysStatus.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (signedxml == null) {
                outputstatus = false;
                SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "SignedXML");
                errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " SignedXML";
                retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
                return retObj.toString();
            } else {
                String xmlsignature = signedxml;
                rk.setSignatureKeytype("EC");
                rk.setSignature(xmlsignature);
            }
        }

        em.merge(rk);
        em.flush();
        em.clear();

        try {
            if (applianceCommon.replicate()) {
                if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                    String response = replObj.execute(applianceConstants.ENTITY_TYPE_FIDO_KEYS, applianceConstants.REPLICATION_OPERATION_UPDATE, primarykey, rk);
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
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2004", "");
        retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", SKFSCommon.getMessageProperty("FIDOJPA-MSG-2004")).build();
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
        return retObj.toString();
    }
}
