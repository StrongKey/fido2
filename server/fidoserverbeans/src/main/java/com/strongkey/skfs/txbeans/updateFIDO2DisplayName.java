/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;


@Stateless
public class updateFIDO2DisplayName implements updateFIDO2DisplayNameLocal {

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

    @Override
//    public String execute(Short sid, Long did, String username, Long fkid, String modify_location, String displayname) {
    public String execute(Short sid, Long did, Long fkid, String modify_location, String displayname) {
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
        if (displayname == null) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1001", "displayname");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " displayname";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        } else if (displayname.trim().length() == 0) {
            outputstatus = false;
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "execute", "FIDOJPA-ERR-1003", "displayname");
            errmsg = SKFSCommon.getMessageProperty("FIDOJPA-ERR-1003") + " displayname";
            retObj = Json.createObjectBuilder().add("status", outputstatus).add("message", errmsg).build();
            return retObj.toString();
        }
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDOJPA-MSG-2001", "displayname=" + displayname);

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

        byte[] regSettingsBytes = Base64.getUrlDecoder().decode(rk.getRegistrationSettings());
        String regSettingsString = null;
        try {
            regSettingsString = new String(regSettingsBytes, "UTF-8");

        } catch (UnsupportedEncodingException ex) {
            Logger.getLogger(updateFIDO2DisplayName.class.getName()).log(Level.SEVERE, null, ex);
        }

        JsonObject regsettingsjson = SKFSCommon.getJsonObjectFromString(regSettingsString);
        JsonObjectBuilder jobj = Json.createObjectBuilder();
        if(regsettingsjson.containsKey(SKFSConstants.FIDO_REGISTRATION_SETTING_UP)){
            jobj.add(SKFSConstants.FIDO_REGISTRATION_SETTING_UP, regsettingsjson.getBoolean(SKFSConstants.FIDO_REGISTRATION_SETTING_UP));
        }

        if(regsettingsjson.containsKey(SKFSConstants.FIDO_REGISTRATION_SETTING_UV)){
            jobj.add(SKFSConstants.FIDO_REGISTRATION_SETTING_UV, regsettingsjson.getBoolean(SKFSConstants.FIDO_REGISTRATION_SETTING_UV));
        }

        if(regsettingsjson.containsKey(SKFSConstants.FIDO_REGISTRATION_SETTING_KTY)){
            jobj.add(SKFSConstants.FIDO_REGISTRATION_SETTING_KTY, regsettingsjson.getInt(SKFSConstants.FIDO_REGISTRATION_SETTING_KTY));
        }
        
        if(regsettingsjson.containsKey(SKFSConstants.FIDO_REGISTRATION_SETTING_FMT)){
            jobj.add(SKFSConstants.FIDO_REGISTRATION_SETTING_FMT, regsettingsjson.getString(SKFSConstants.FIDO_REGISTRATION_SETTING_FMT));
        }

        if(regsettingsjson.containsKey(SKFSConstants.FIDO_REGISTRATION_SETTING_ALG)){
            jobj.add(SKFSConstants.FIDO_REGISTRATION_SETTING_ALG, regsettingsjson.getInt(SKFSConstants.FIDO_REGISTRATION_SETTING_ALG));
        }

        jobj.add(SKFSConstants.FIDO_REGISTRATION_SETTING_DISPLAYNAME, displayname);

        rk.setRegistrationSettings(Base64.getUrlEncoder().withoutPadding().encodeToString(jobj.build().toString().getBytes()));
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
