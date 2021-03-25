/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.messaging;

import com.strongkey.appliance.entitybeans.Configurations;
import com.strongkey.appliance.entitybeans.Domains;
import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.replication.messaging.ZMQPublisher;
import com.strongkey.skce.pojos.UserSessionInfo;
import com.strongkey.skce.utilities.skceConstants;
import com.strongkey.skfe.entitybeans.FidoKeys;
import com.strongkey.skfs.entitybeans.AttestationCertificates;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.entitybeans.FidoUsers;
import java.util.logging.Level;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;

@Stateless
public class publishSKCEObject implements publishSKCEObjectLocal, publishSKCEObjectRemote {

    /**
     ** This class's name - used for logging & not persisted
     **/
    private final String classname = this.getClass().getName();

    @Override
    @Asynchronous
    public void execute(String repobjpk, int objectype, int objectop, String objectpk, Object obj) {
        strongkeyLogger.entering(skceConstants.SKEE_LOGGER,classname, "execute");
        byte[] objectbytes = null;

        //switch between object types and create proto buffer object
        switch (objectype) {
            case applianceConstants.ENTITY_TYPE_FIDO_KEYS:
                FidoKeys fkbean = (FidoKeys) obj;
                ZMQSKCEReplicationProtos.FidoKeys.Builder fkbuilder = ZMQSKCEReplicationProtos.FidoKeys.newBuilder();
                // First deal with attributes that might be null
                if (fkbean.getUserid() != null){
                    fkbuilder.setUserid(fkbean.getUserid());
                }
                if (fkbean.getTransports() != null) {
                    fkbuilder.setTransports(fkbean.getTransports().longValue());
                }
                if (fkbean.getModifyDate() != null) {
                    fkbuilder.setModifyDate(fkbean.getModifyDate().getTime());
                }
                if (fkbean.getModifyLocation() != null) {
                    fkbuilder.setModifyLocation(fkbean.getModifyLocation());
                }
                if (fkbean.getSignature() != null) {
                    fkbuilder.setSignature(fkbean.getSignature());
                }
                if (fkbean.getSignatureKeytype()!= null) {
                    fkbuilder.setSignatureKeytype(fkbean.getSignatureKeytype());
                }
                if (fkbean.getAttsid() != null) {
                    fkbuilder.setAttsid(fkbean.getAttsid());
                }
                if (fkbean.getAttdid() != null) {
                    fkbuilder.setAttdid(fkbean.getAttdid());
                }
                if (fkbean.getAttcid() != null) {
                    fkbuilder.setAttcid(fkbean.getAttcid());
                }
                if (fkbean.getAaguid()!= null) {
                    fkbuilder.setAaguid(fkbean.getAaguid());
                }
                if (fkbean.getRegistrationSettings()!= null) {
                    fkbuilder.setRegistrationSettings(fkbean.getRegistrationSettings());
                }
                if (fkbean.getRegistrationSettingsVersion()!= null) {
                    fkbuilder.setRegistrationSettingsVersion(fkbean.getRegistrationSettingsVersion());
                }
                // Now build the proto with all non-null values
                ZMQSKCEReplicationProtos.FidoKeys fkproto
                        = fkbuilder
                        .setAppid(fkbean.getAppid())
                        .setCounter(fkbean.getCounter())
                        .setCreateDate(fkbean.getCreateDate().getTime())
                        .setCreateLocation(fkbean.getCreateLocation())
                        .setDid(fkbean.getFidoKeysPK().getDid())
                        .setFidoProtocol(fkbean.getFidoProtocol())
                        .setFidoVersion(fkbean.getFidoVersion())
                        .setFkid(fkbean.getFidoKeysPK().getFkid())
                        .setKeyhandle(fkbean.getKeyhandle())
                        .setPublickey(fkbean.getPublickey())
                        .setSid(fkbean.getFidoKeysPK().getSid())
                        .setStatus(fkbean.getStatus())
                        .setUsername(fkbean.getFidoKeysPK().getUsername())
                        .build();
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6007", fkproto.toString());
                objectbytes = fkproto.toByteArray();
                break;

            case applianceConstants.ENTITY_TYPE_FIDO_USERS:
                FidoUsers fubean = (FidoUsers) obj;
                ZMQSKCEReplicationProtos.FidoUsers.Builder fubuilder = ZMQSKCEReplicationProtos.FidoUsers.newBuilder();
                // First deal with attributes that might be null
                if (fubean.getPrimaryEmail() != null) {
                    fubuilder.setPrimaryEmail(fubean.getPrimaryEmail());
                }
                if (fubean.getRegisteredEmails() != null) {
                    fubuilder.setRegisteredEmails(fubean.getRegisteredEmails());
                }
                if (fubean.getPrimaryPhoneNumber() != null) {
                    fubuilder.setPrimaryPhoneNumber(fubean.getPrimaryPhoneNumber());
                }
                if (fubean.getRegisteredPhoneNumbers() != null) {
                    fubuilder.setRegisteredPhoneNumbers(fubean.getRegisteredPhoneNumbers());
                }
                if (fubean.getSignature() != null) {
                    fubuilder.setSignature(fubean.getSignature());
                }
                if (fubean.getTwoStepTarget() != null && fubean.getTwoStepTarget().trim().length()>0) {
                    fubuilder.setTwoStepTarget(fubean.getTwoStepTarget());
                }
                if (fubean.getUserdn() != null) {
                    fubuilder.setUserdn(fubean.getUserdn());
                }
                // Now build the proto with all non-null values
                ZMQSKCEReplicationProtos.FidoUsers fuproto
                        = fubuilder
                        .setDid(fubean.getFidoUsersPK().getDid())
                        .setFidoKeysEnabled(fubean.getFidoKeysEnabled())
                        .setSid(fubean.getFidoUsersPK().getSid())
                        .setStatus(fubean.getStatus())
                        .setTwoStepVerification(fubean.getTwoStepVerification())
                        .setUsername(fubean.getFidoUsersPK().getUsername())
                        .build();
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6007", fuproto.toString());
                objectbytes = fuproto.toByteArray();
                break;

            case applianceConstants.ENTITY_TYPE_MAP_USER_SESSION_INFO:
                UserSessionInfo usibean = (UserSessionInfo) obj;
                ZMQSKCEReplicationProtos.UserSessionInfo.Builder usibuilder = ZMQSKCEReplicationProtos.UserSessionInfo.newBuilder();
                // First deal with attributes that might be null
                if (usibean.getSkid() != null) {
                    usibuilder.setSkid(usibean.getSkid());
                }
                if (usibean.getDisplayName()!= null) {
                    usibuilder.setDisplayName(usibean.getDisplayName());
                }
                if (usibean.getRpName()!= null) {
                    usibuilder.setRpName(usibean.getRpName());
                }
                if (usibean.getSid() != null) {
                    usibuilder.setSid(usibean.getSid());
                }
                if (usibean.getSessionid() != null) {
                    usibuilder.setSessionid(usibean.getSessionid());
                }
                if (usibean.getUserId() != null){
                    usibuilder.setUserid(usibean.getUserId());
                }
                if (usibean.getUserIcon() != null){
                    usibuilder.setUsericon(usibean.getUserIcon());
                }
                if (usibean.getPolicyMapKey() != null){
                    usibuilder.setFidopolicymapkey(usibean.getPolicyMapKey());
                }
                if (usibean.getAttestationPreferance()!= null){
                    usibuilder.setAttestationPreferance(usibean.getAttestationPreferance());
                }
                if (usibean.getUserVerificationReq()!= null){
                    usibuilder.setUserVerificationReq(usibean.getUserVerificationReq());
                }
                ZMQSKCEReplicationProtos.UserSessionInfo usiproto
                        = usibuilder
                        .setUsername(usibean.getUsername())
                        .setNonce(usibean.getNonce())
                        .setAppid(usibean.getAppid())
                        .setSessiontype(usibean.getSessiontype())
                        .setCreationdate(usibean.getCreationdate().getTime())
                        .setUserPublicKey(usibean.getUserPublicKey())
                        .setFkid(usibean.getFkid())
                        .setMapkey(usibean.getMapkey())
                        .build();
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6007", usiproto.toString());
                objectbytes = usiproto.toByteArray();
                break;

               case applianceConstants.ENTITY_TYPE_DOMAINS:
                Domains dbean = (Domains) obj;
                // First deal with attributes that might be null
                ZMQSKCEReplicationProtos.Domains.Builder dbuilder = ZMQSKCEReplicationProtos.Domains.newBuilder();
                if (dbean.getEncryptionCertificateUuid() != null)
                    dbuilder.setEncryptionCertificateUuid(dbean.getEncryptionCertificateUuid());
                if (dbean.getEncryptionCertificate() != null)
                    dbuilder.setEncryptionCertificate(dbean.getEncryptionCertificate());
                if (dbean.getName() != null)
                    dbuilder.setName(dbean.getName());
                if (dbean.getNotes() != null)
                    dbuilder.setNotes(dbean.getNotes());
                if (dbean.getSigningCertificate() != null)
                    dbuilder.setSigningCertificate(dbean.getSigningCertificate());
                if (dbean.getSigningCertificateUuid() != null)
                    dbuilder.setSigningCertificateUuid(dbean.getSigningCertificateUuid());
                if (dbean.getSkceSigningdn() != null)
                    dbuilder.setSkceSigningdn(dbean.getSkceSigningdn());
                if (dbean.getSkfeAppid() != null)
                    dbuilder.setSkfeAppid(dbean.getSkfeAppid());
                // Now build the proto with all non-null values
                ZMQSKCEReplicationProtos.Domains dproto =
                        dbuilder
                                .setDid(dbean.getDid())
                                .setReplicationStatus(dbean.getReplicationStatus())
                                .setStatus(dbean.getStatus())
                                .build();
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6007", dproto.toString());
                objectbytes = dproto.toByteArray();
                break;

            case applianceConstants.ENTITY_TYPE_FIDO_POLICIES:
                FidoPolicies fpbean = (FidoPolicies) obj;
                ZMQSKCEReplicationProtos.FidoPolicies.Builder fpbuilder = ZMQSKCEReplicationProtos.FidoPolicies.newBuilder();
                // First deal with attributes that might be null
                if (fpbean.getNotes() != null) {
                    fpbuilder.setNotes(fpbean.getNotes());
                }
                if (fpbean.getModifyDate() != null) {
                    fpbuilder.setModifyDate(fpbean.getModifyDate().getTime());
                }
                if (fpbean.getSignature() != null) {
                    fpbuilder.setSignature(fpbean.getSignature());
                }

                // Now build the proto with all non-null values
                ZMQSKCEReplicationProtos.FidoPolicies fpproto
                        = fpbuilder
                        .setSid(fpbean.getFidoPoliciesPK().getSid())
                        .setDid(fpbean.getFidoPoliciesPK().getDid())
                        .setPid(fpbean.getFidoPoliciesPK().getPid())
                        .setPolicy(fpbean.getPolicy())
                        .setStatus(fpbean.getStatus())
                        .setCreateDate(fpbean.getCreateDate().getTime())
                        .build();
                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "run", "SKCE-MSG-6007", fpproto.toString());
                objectbytes = fpproto.toByteArray();
                break;

            case applianceConstants.ENTITY_TYPE_ATTESTATION_CERTIFICATES:
                AttestationCertificates acbean = (AttestationCertificates) obj;
                ZMQSKCEReplicationProtos.AttestationCertificates.Builder acbuilder = ZMQSKCEReplicationProtos.AttestationCertificates.newBuilder();
                // First deal with attributes that might be null
                if (acbean.getParentSid() != null) {
                    acbuilder.setParentSid(acbean.getParentSid());
                }
                if (acbean.getParentDid() != null) {
                    acbuilder.setParentDid(acbean.getParentDid());
                }
                if (acbean.getParentAttcid() != null) {
                    acbuilder.setParentAttcid(acbean.getParentAttcid());
                }
                if (acbean.getSignature()!= null) {
                    acbuilder.setSignature(acbean.getSignature());
                }

                // Now build the proto with all non-null values
                ZMQSKCEReplicationProtos.AttestationCertificates acproto
                        = acbuilder
                        .setSid(acbean.getAttestationCertificatesPK().getSid())
                        .setDid(acbean.getAttestationCertificatesPK().getDid())
                        .setAttcid(acbean.getAttestationCertificatesPK().getAttcid())
                        .setCertificate(acbean.getCertificate())
                        .setIssuerDn(acbean.getIssuerDn())
                        .setSubjectDn(acbean.getSubjectDn())
                        .setSerialNumber(acbean.getSerialNumber())
                        .build();
                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "run", "SKCE-MSG-6007", acproto.toString());
                objectbytes = acproto.toByteArray();
                break;

            case applianceConstants.ENTITY_TYPE_FIDO_CONFIGURATIONS:
                Configurations cfgbean = (Configurations) obj;
                ZMQSKCEReplicationProtos.Configurations.Builder cfgbuilder = ZMQSKCEReplicationProtos.Configurations.newBuilder();
                if (cfgbean.getNotes() != null)
                    cfgbuilder.setNotes(cfgbean.getNotes());
                // Now build the proto with all non-null values
                ZMQSKCEReplicationProtos.Configurations cfgproto =
                        cfgbuilder
                                .setConfigKey(cfgbean.getConfigurationsPK().getConfigKey())
                                .setConfigValue(cfgbean.getConfigValue())
                                .setDid(cfgbean.getConfigurationsPK().getDid())
                                .build();
                strongkeyLogger.logp(skceConstants.SKFE_LOGGER, Level.FINE, classname, "run", "SKCE-MSG-6007", cfgproto.toString());
                objectbytes = cfgproto.toByteArray();
                break;
                
            default:
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "run", "SKCE-ERR-6001", applianceCommon.getEntityName(objectype) + " [OBJPK=" + objectpk + "] [OBJOP=" + applianceCommon.getRepop(objectop) + "]");

        }
        ZMQPublisher.getInstance().publish(repobjpk, objectype, objectop, objectpk, objectbytes);
        strongkeyLogger.exiting(skceConstants.SKEE_LOGGER,classname, "execute");
    }

    @Override
    @Asynchronous
    public void remoteExecute(String repobjpk, int objectype, int objectop, String objectpk, Object o) {
        execute(repobjpk, objectype, objectop, objectpk, o);
    }
}
