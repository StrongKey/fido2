/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.entitybeans.AttestationCertificates;
import com.strongkey.skfs.entitybeans.AttestationCertificatesPK;
import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
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
    SequenceGeneratorBeanLocal seqgenejb;
    
    @Override
    public AttestationCertificatesPK execute(Long did, X509Certificate attCert, 
            AttestationCertificatesPK parentPK) throws CertificateEncodingException, SKFEException{
        skfsLogger.entering(skfsConstants.SKFE_LOGGER, classname, "execute");

        Long sid = applianceCommon.getServerId();
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
        attestationCertificate.setCertificate(Base64.getUrlEncoder().encodeToString(attCert.getEncoded()));
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
            ex.getConstraintViolations().stream().forEach(x -> skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    x.toString()));
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.SEVERE, "FIDOJPA-ERR-2006", ex.getLocalizedMessage());
            throw new SKFEException(skfsCommon.getMessageProperty("FIDOJPA-ERR-2006") + "Check server logs for details.");
        }

        //TODO Replicate
        String primarykey = sid + "-" + did + "-" + attcid;
        try {
            if (applianceCommon.replicate()) {
                String response = replObj.execute(applianceConstants.ENTITY_TYPE_ATTESTATION_CERTIFICATES, applianceConstants.REPLICATION_OPERATION_ADD, primarykey, attestationCertificate);
                if(response != null){
                    throw new SKFEException(skfsCommon.getMessageProperty("FIDOJPA-ERR-1001") + response);
                }
            }
        } catch (Exception e) {
            sc.setRollbackOnly();
            skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");
            throw new RuntimeException(e.getLocalizedMessage());
        }
        skfsLogger.exiting(skfsConstants.SKFE_LOGGER, classname, "execute");

        return attestationCertificatePK;
    }
}
