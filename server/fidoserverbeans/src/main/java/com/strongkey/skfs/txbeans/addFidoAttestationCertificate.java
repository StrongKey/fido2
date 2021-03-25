/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skfs.entitybeans.AttestationCertificates;
import com.strongkey.skfs.entitybeans.AttestationCertificatesPK;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.logging.Level;
import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.validation.ConstraintViolationException;

@Stateless
public class addFidoAttestationCertificate implements addFidoAttestationCertificateLocal {

    private final String classname = this.getClass().getName();
    @Resource
    private SessionContext sc;
    @PersistenceContext
    private EntityManager em;

    @EJB
    replicateSKFEObjectBeanLocal replObj;
    @EJB
    getFidoAttestationCertificateLocal getAttCertbean;
    @EJB
    SequenceGeneratorBeanLocal seqgenejb;

    @Override
    public AttestationCertificatesPK execute(Long did, X509Certificate attCert,
            AttestationCertificatesPK parentPK) throws CertificateEncodingException, SKFEException{
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER, classname, "execute");

        Long sid = applianceCommon.getServerId();

        AttestationCertificates dbcert = getAttCertbean.getByIssuerDnSerialNumber(
                        attCert.getIssuerDN().getName(), attCert.getSerialNumber().toString());
        if(dbcert != null){
            //already exists
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "SKCE-ERR-8059", "");
            return dbcert.getAttestationCertificatesPK();
        }

        Integer attcid = seqgenejb.nextAttestationCertificateID();
        AttestationCertificatesPK attestationCertificatePK = new AttestationCertificatesPK();
        AttestationCertificates attestationCertificate = new AttestationCertificates();
        attestationCertificatePK.setSid(sid.shortValue());
        attestationCertificatePK.setDid(did.shortValue());
        attestationCertificatePK.setAttcid(attcid);
        attestationCertificate.setAttestationCertificatesPK(attestationCertificatePK);
        attestationCertificate.setParentSid((parentPK != null)?parentPK.getSid():null);
        attestationCertificate.setParentDid((parentPK != null)?parentPK.getDid():null);
        attestationCertificate.setParentAttcid((parentPK != null)?parentPK.getAttcid():null);
        attestationCertificate.setCertificate(Base64.getUrlEncoder().withoutPadding().encodeToString(attCert.getEncoded()));
        attestationCertificate.setIssuerDn(attCert.getIssuerDN().getName());
//        if(attCert.getSubjectDN().getName().length() == 0){
//            attestationCertificate.setSubjectDn("Not Specified");
//        }else{
            attestationCertificate.setSubjectDn(attCert.getSubjectDN().getName());
//        }

        attestationCertificate.setSerialNumber(attCert.getSerialNumber().toString());


        //TODO add signing code(?)
        try {
            em.persist(attestationCertificate);
            em.flush();
            em.clear();
        } catch (ConstraintViolationException ex) {
            ex.getConstraintViolations().stream().forEach(x -> SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    x.toString()));
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDOJPA-ERR-2006", ex.getLocalizedMessage());
            throw new SKFEException(SKFSCommon.getMessageProperty("FIDOJPA-ERR-2006") + "Check server logs for details.");
        }

        //TODO Replicate
        String primarykey = sid + "-" + did + "-" + attcid;
        try {
            if (applianceCommon.replicate()) {
                if (!Boolean.valueOf(SKFSCommon.getConfigurationProperty("skfs.cfg.property.replicate.hashmapsonly"))) {
                    String response = replObj.execute(applianceConstants.ENTITY_TYPE_ATTESTATION_CERTIFICATES, applianceConstants.REPLICATION_OPERATION_ADD, primarykey, attestationCertificate);
                    if (response != null) {
                        throw new SKFEException(SKFSCommon.getMessageProperty("FIDOJPA-ERR-1001") + response);
                    }
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }
        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "execute");

        return attestationCertificatePK;
    }
}
