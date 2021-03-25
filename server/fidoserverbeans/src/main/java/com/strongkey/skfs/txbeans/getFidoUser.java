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
import com.strongkey.skfs.entitybeans.FidoUsers;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

@Stateless
public class getFidoUser implements getFidoUserLocal {

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

    @Override
    public FidoUsers getByUsername(Long did, String username) throws SKFEException {
        try {
            TypedQuery<FidoUsers> q = em.createNamedQuery("FidoUsers.findByDidUsername", FidoUsers.class);
            q.setHint("javax.persistence.cache.storeMode", "REFRESH");
            q.setParameter("username", username);
            q.setParameter("did", did);
            FidoUsers fidoUser = q.getSingleResult();
            if (fidoUser != null) {
                verifyDBRecordSignature(did, fidoUser);
            }
            return fidoUser;
        } catch (NoResultException ex) {
            return null;
        }
    }

    private void verifyDBRecordSignature(Long did, FidoUsers FidoUser)
            throws SKFEException {
        if (FidoUser != null) {
            if (SKFSCommon.getConfigurationProperty("skfs.cfg.property.db.signature.rowlevel.verify")
                    .equalsIgnoreCase("true")) {
                Domains d = getdomejb.byDid(did);
                String standalone = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.fidoengine");
                String signingKeystorePassword = "";
                if (standalone.equalsIgnoreCase("true")) {
                    signingKeystorePassword = SKFSCommon.getConfigurationProperty("skfs.cfg.property.standalone.signingkeystore.password");
                }

                String documentid = FidoUser.getFidoUsersPK().getSid()
                        + "-" + FidoUser.getFidoUsersPK().getDid()
                        + "-" + FidoUser.getFidoUsersPK().getUsername();
                FidoUser.setId(documentid);

                //jaxB conversion
                //converting the databean object to xml
                StringWriter writer = new StringWriter();
                JAXBContext jaxbContext;
                Marshaller marshaller;
                try {
                    jaxbContext = JAXBContext.newInstance(FidoUsers.class);
                    marshaller = jaxbContext.createMarshaller();
                    marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                    marshaller.marshal(FidoUser, writer);
                } catch (JAXBException ex) {
                    Logger.getLogger(getFidoKeys.class.getName()).log(Level.SEVERE, null, ex);
                }

                //  verify row level signature
                boolean verified = false;
                try {
                    verified = initCryptoModule.getCryptoModule().verifyDBRow(did.toString(), writer.toString(), d.getSkceSigningdn(), Boolean.valueOf(standalone), signingKeystorePassword, "RSA", FidoUser.getSignature());
                } catch (CryptoException ex) {
                    Logger.getLogger(getFidoKeys.class.getName()).log(Level.SEVERE, null, ex);
                }
                if (!verified) {
                    SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "verifyDBRecordSignature",
                            "SKCE-ERR-5001", "er sid-did-erqid="
                            + FidoUser.getFidoUsersPK().getSid()
                            + "-" + FidoUser.getFidoUsersPK().getDid()
                            + "-" + FidoUser.getFidoUsersPK().getUsername());
                    throw new SKFEException(SKFSCommon.getMessageProperty("SKCE-ERR-5001")
                            + "FidoUser sid-did-erqid="
                            + FidoUser.getFidoUsersPK().getSid()
                            + "-" + FidoUser.getFidoUsersPK().getDid()
                            + "-" + FidoUser.getFidoUsersPK().getUsername());
                }
            }
        } else {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.SEVERE, classname, "verifyDBRecordSignature",
                    "FIDOJPA-ERR-1001", " er object");
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + " fk object");
        }
    }
}
