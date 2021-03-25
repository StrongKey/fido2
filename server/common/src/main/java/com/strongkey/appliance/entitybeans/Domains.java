/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.entitybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import java.io.Serializable;
import java.util.logging.Level;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

/**********************************************
8888888888          888    d8b 888
888                 888    Y8P 888
888                 888        888
8888888    88888b.  888888 888 888888 888  888
888        888 "88b 888    888 888    888  888
888        888  888 888    888 888    888  888
888        888  888 Y88b.  888 Y88b.  Y88b 888
8888888888 888  888  "Y888 888  "Y888  "Y88888
                                           888
                                      Y8b d88P
                                       "Y88P"
**********************************************/


@Entity
@Table(name = "domains", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"})
})
@NamedQueries({
    @NamedQuery(name = "Domains.findAll", query = "SELECT d FROM Domains d"),
    @NamedQuery(name = "Domains.findByDid", query = "SELECT d FROM Domains d WHERE d.did = :did"),
    @NamedQuery(name = "Domains.findByName", query = "SELECT d FROM Domains d WHERE d.name = :name"),
    @NamedQuery(name = "Domains.findByStatus", query = "SELECT d FROM Domains d WHERE d.status = :status"),
    @NamedQuery(name = "Domains.findByReplicationStatus", query = "SELECT d FROM Domains d WHERE d.replicationStatus = :replicationStatus"),
    @NamedQuery(name = "Domains.maxPK", query = "SELECT max(d.did) FROM Domains d"),
    @NamedQuery(name = "Domains.count", query = "SELECT count(d) FROM Domains d")
})

public class Domains implements Serializable
{
    private static final long serialVersionUID = 1L;

    /**
     ** This class's name - used for logging & not persisted
     **/
    @Transient
    private final String classname = this.getClass().getName();

    /**
     ** Placeholders for testing input before persisting
     **/
    @Transient
    private Long tmpLong = null;
    @Transient
    private String tmpStr = null;

    /**
     ** Primary key - Domain ID
     **/
    @Id
    private Long did;

/********************************************************************************
       d8888 888    888            d8b 888               888
      d88888 888    888            Y8P 888               888
     d88P888 888    888                888               888
    d88P 888 888888 888888 888d888 888 88888b.  888  888 888888  .d88b.  .d8888b
   d88P  888 888    888    888P"   888 888 "88b 888  888 888    d8P  Y8b 88K
  d88P   888 888    888    888     888 888  888 888  888 888    88888888 "Y8888b.
 d8888888888 Y88b.  Y88b.  888     888 888 d88P Y88b 888 Y88b.  Y8b.          X88
d88P     888  "Y888  "Y888 888     888 88888P"   "Y88888  "Y888  "Y8888   88888P'
********************************************************************************/

    /**
     ** Domain's name
     **/
    @Basic(optional = false)
    @Column(name = "name", nullable = false, length = 512)
    private String name;

    /**
     ** Current status
     **/
    @Basic(optional = false)
    @Column(name = "status", nullable = false, length = 8)
    private String status;

    /**
     ** Current status of this domain in the replication subsystem
     **/
    @Basic(optional = false)
    @Column(name = "replication_status")
    private String replicationStatus;

    /**
     ** Encrytpion digital certificate of the domain
     **/
    @Basic(optional = false)
    @Column(name = "encryption_certificate", nullable = false, length = 4096)
    private String encryptionCertificate;

    /**
     ** Optional signing digital certificate of the domain
     **/
    @Column(name = "signing_certificate", length = 4096)
    private String signingCertificate;

    /**
     ** Encrytpion digital certificate UUID (if using a TPM)
     **/
    @Column(name = "encryption_certificate_uuid", length = 64)
    private String encryptionCertificateUuid;

    /**
     ** Optional signing digital certificate UUID (if using a TPM)
     **/
    @Column(name = "signing_certificate_uuid", length = 64)
    private String signingCertificateUuid;

    /**
     ** Optional signing digital certificate key-scheme
     **/
    @Column(name = "skce_signingdn")
    private String skceSigningdn;

    /**
     ** Optional signing digital certificate key-scheme
     **/
    @Column(name = "skfe_appid")
    private String skfeAppid;

    /**
     ** Optional notes
     **/
    @Column(name = "notes", length = 512)
    private String notes;

/**********************************************************************************************
 .d8888b.                             888                              888
d88P  Y88b                            888                              888
888    888                            888                              888
888         .d88b.  88888b.  .d8888b  888888 888d888 888  888  .d8888b 888888  .d88b.  888d888
888        d88""88b 888 "88b 88K      888    888P"   888  888 d88P"    888    d88""88b 888P"
888    888 888  888 888  888 "Y8888b. 888    888     888  888 888      888    888  888 888
Y88b  d88P Y88..88P 888  888      X88 Y88b.  888     Y88b 888 Y88b.    Y88b.  Y88..88P 888
 "Y8888P"   "Y88P"  888  888  88888P'  "Y888 888      "Y88888  "Y8888P  "Y888  "Y88P"  888
**********************************************************************************************/

    /**
     ** Empty constructor (required by JPA)
     **/
    public Domains() {
    }

/****************************************************************
888b     d888          888    888                    888
8888b   d8888          888    888                    888
88888b.d88888          888    888                    888
888Y88888P888  .d88b.  888888 88888b.   .d88b.   .d88888 .d8888b
888 Y888P 888 d8P  Y8b 888    888 "88b d88""88b d88" 888 88K
888  Y8P  888 88888888 888    888  888 888  888 888  888 "Y8888b.
888   "   888 Y8b.     Y88b.  888  888 Y88..88P Y88b 888      X88
888       888  "Y8888   "Y888 888  888  "Y88P"   "Y88888  88888P'
*****************************************************************/

    public Long getDid() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getDID");
        return did;
    }

    /**
     ** DID - Required
     ** @param did - Long object containing the unique domain identifier for
     ** this object.  The object is first tested for NULL values before it
     ** is assigned to a temporary variable for testing.  This is different
     ** from tests for String and Date objects, which are assigned to a
     ** temporary value first before they are tested.
     **/
    public void setDid(Long did) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setDID");
        // NULL argument
        if (did == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setDid", "APPL-ERR-1003", "DID");
            throw new IllegalArgumentException("APPL-ERR-1003: DID");
        }
        tmpLong = did;
        // DID is negative, zero or larger than 9223372036854775807
        if ((tmpLong < 1) || (tmpLong > Long.MAX_VALUE)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setDid", "APPL-ERR-1007", "DID=" + tmpLong);
            throw new IllegalArgumentException("APPL-ERR-1007: DID=" + tmpLong);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setDid", "APPL-MSG-1051", "DID=" + tmpLong);
        this.did = tmpLong;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setDID");
    }

    /*********************************************************************/
    public String getName() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getName");
        return name;
    }

    /**
     ** NAME - Required
     ** @param name - String containin the name the domain; mostly for human
     ** users to recognize it - the system only uses the Domain ID.
     **/
    public void setName(String name) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setName");
        tmpStr = name;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setName", "APPL-ERR-1003", "NAME");
            throw new IllegalArgumentException("APPL-ERR-1003: NAME");
        }
        // Empty argument
        tmpStr = name.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setName", "APPL-ERR-1004", "NAME");
            throw new IllegalArgumentException("APPL-ERR-1004: NAME");
        }
        // Name too long
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setName", "APPL-ERR-1005", "NAME=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: NAME=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setName", "APPL-MSG-1051", "NAME=" + tmpStr);
        this.name = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setName");
    }

    /*********************************************************************/
    public String getStatus() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getStatus");
        return status;
    }

    /**
     ** STATUS - Required
     ** @param status - String consisting of one of the following values:
     ** Active, Inactive or Other.  The status must be Active for the SKLES
     ** to use this object.
     **/
    public void setStatus(String status) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setStatus");
        tmpStr = status;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setStatus", "APPL-ERR-1003", "STATUS");
            throw new IllegalArgumentException("APPL-ERR-1003: STATUS");
        }
        // Empty argument
        tmpStr = status.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setStatus", "APPL-ERR-1004", "STATUS");
            throw new IllegalArgumentException("APPL-ERR-1004: STATUS");
        }
        // Incorrect status - not Active, Inactive or Other
        if (!applianceCommon.aio(tmpStr)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setStatus", "APPL-ERR-1015", "STATUS=" + tmpStr);
            throw new IllegalArgumentException("APPLERR-1015: STATUS=" + tmpStr);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setStatus", "APPL-MSG-1051", "STATUS=" + tmpStr);
        this.status = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setStatus");
    }

    /*********************************************************************/
    public String getReplicationStatus() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getReplicationStatus");
        return replicationStatus;
    }

     /**
     ** REPLICATION_STATUS - Required
     ** @param status - String consisting of one of the following values:
     ** Active, Inactive or Other.  The replicationStatus must be Active for
     ** the KeyAppliance to replicate objects to other appliances.
     **/
    public void setReplicationStatus(String replicationStatus) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setReplicationStatus");
        tmpStr = replicationStatus;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setReplicationStatus", "APPL-ERR-1003", "REPLICATION_STATUS");
            throw new IllegalArgumentException("APPL-ERR-1003: REPLICATION_STATUS");
        }
        // Empty argument
        tmpStr = replicationStatus.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setReplicationStatus", "APPL-ERR-1004", "REPLICATION_STATUS");
            throw new IllegalArgumentException("APPL-ERR-1004: REPLICATION_STATUS");
        }
        // Incorrect status - not Active, Inactive or Other
        if (!applianceCommon.aio(tmpStr)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setReplicationStatus", "APPL-ERR-1015", "REPLICATION_STATUS=" + tmpStr);
            throw new IllegalArgumentException("APPL-ERR-1015: REPLICATION_STATUS=" + tmpStr);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setReplicationStatus", "APPL-MSG-1051", "REPLICATION_STATUS=" + tmpStr);
        this.replicationStatus = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setReplicationStatus");
    }

    /*********************************************************************/
    public String getEncryptionCertificate() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getEncryptionCertificate");
        return encryptionCertificate;
    }

    /**
     ** ENCRYPTION_CERTIFICATE - Required
     ** @param encryptionCertificate - String containing the PEM-encoded
     ** digital certificate for the domain with "keyEncipherment" turned on
     **/
    public void setEncryptionCertificate(String encryptionCertificate) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setEncryptionCertificate");
        tmpStr = encryptionCertificate;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setEncryptionCertificate", "APPL-ERR-1003", "ENCRYPTION_CERTIFICATE");
            throw new IllegalArgumentException("APPL-ERR-1003: ENCRYPTION_CERTIFICATE");
        }
        // Empty argument
        tmpStr = encryptionCertificate.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setEncryptionCertificate", "APPL-ERR-1004", "ENCRYPTION_CERTIFICATE");
            throw new IllegalArgumentException("APPL-ERR-1004: ENCRYPTION_CERTIFICATE");
        }
        // encryptionCertificate too long (> 4096 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.4096charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setEncryptionCertificate", "APPL-ERR-1005", "ENCRYPTION_CERTIFICATE=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: ENCRYPTION_CERTIFICATE=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setEncryptionCertificate", "APPL-MSG-1051", "ENCRYPTION_CERTIFICATE=" + tmpStr);
        this.encryptionCertificate = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setEncryptionCertificate");
    }

    /*********************************************************************/
    public String getSigningCertificate() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getSigningCertificate");
        return signingCertificate;
    }

    /**
     ** SIGNING_CERTIFICATE - Optional
     ** @param signingCertificate - String containing the PEM-encoded digital
     ** certificate for the domain with "digitalSignature" turned on
     **/
    public void setSigningCertificate(String signingCertificate) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificate");
        tmpStr = signingCertificate;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSigningCertificate", "APPL-MSG-1061", "SIGNING_CERTIFICATE");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificate");
            return;
        }
        // Empty argument
        tmpStr = signingCertificate.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSigningCertificate", "APPL-MSG-1061", "SIGNING_CERTIFICATE");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificate");
            return;
        }
        // encryptionCertificate too long (> 4096 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.4096charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setSigningCertificate", "APPL-ERR-1005", "SIGNING_CERTIFICATE=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: SIGNING_CERTIFICATE=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSigningCertificate", "APPL-MSG-1051", "SIGNING_CERTIFICATE=" + tmpStr);
        this.signingCertificate = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificate");
    }

    /*********************************************************************/
    public String getEncryptionCertificateUuid() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getEncryptionCertificateUuid");
        return encryptionCertificateUuid;
    }

    /**
     ** ENCRYPTION_CERTIFICATE_UUID - Optional
     ** @param encryptionCertificateUuid - String containing the Universally
     ** unique ID of the encryption digital certificate if this SKLES is using
     ** a Trusted Platform Module.  The UUID is easier to locate certificates
     ** in the Persistent Store of the TPM.
     **/
    public void setEncryptionCertificateUuid(String encryptionCertificateUuid) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setEncryptionCertificateUuid");
        tmpStr = encryptionCertificateUuid;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setEncryptionCertificateUuid", "APPL-MSG-1061", "ENCRYPTION_CERTIFICATE_UUID");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setEncryptionCertificateUuid");
            return;
        }
        // Empty argument
        tmpStr = encryptionCertificateUuid.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setEncryptionCertificateUuid", "APPL-MSG-1061", "ENCRYPTION_CERTIFICATE_UUID");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setEncryptionCertificateUuid");
            return;
        }
        // encryptionCertificateUuid too long (> 64 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.64charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setEncryptionCertificateUuid", "APPL-ERR-1005", "ENCRYPTION_CERTIFICATE_UUID=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: ENCRYPTION_CERTIFICATE_UUID=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setEncryptionCertificateUuid", "APPL-MSG-1051", "ENCRYPTION_CERTIFICATE_UUID=" + tmpStr);
        this.encryptionCertificateUuid = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setEncryptionCertificateUuid");
    }

    /*********************************************************************/
    public String getSigningCertificateUuid() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getSigningCertificateUuid");
        return signingCertificateUuid;
    }

    /**
     ** SIGNING_CERTIFICATE_UUID - Optional
     ** @param signingCertificateUuid - String containing the universal unique
     ** ID of the signing certificate if this SKLES is using a TPM.  The UUID
     ** is easier to locate certificates in the Persistent Store of the TPM.
     **/
    public void setSigningCertificateUuid(String signingCertificateUuid) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificateUuid");
        tmpStr = signingCertificateUuid;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSigningCertificateUuid", "APPL-MSG-1061", "SIGNING_CERTIFICATE_UUID");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificateUuid");
            return;
        }
        // Empty argument
        tmpStr = signingCertificateUuid.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSigningCertificateUuid", "APPL-MSG-1061", "SIGNING_CERTIFICATE_UUID");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificateUuid");
            return;
        }
        // signingCertificateUuid too long (> 64 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.64charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setSigningCertificateUuid", "APPL-ERR-1005", "SIGNING_CERTIFICATE_UUID=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: SIGNING_CERTIFICATE_UUID=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSigningCertificateUuid", "APPL-MSG-1051", "SIGNING_CERTIFICATE_UUID=" + tmpStr);
        this.signingCertificateUuid = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSigningCertificateUuid");
    }

    /*********************************************************************/
    public String getSkceSigningdn() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getSkceSigningdn");
        return skceSigningdn;
    }

    /**
     ** SKCE_SIGNINGDN - Optional
     ** @param skceSigningdn - String containing the universal unique
     ** ID of the signing certificate if this SKLES is using a TPM.  The UUID
     ** is easier to locate certificates in the Persistent Store of the TPM.
     **/
    public void setSkceSigningdn(String skceSigningdn) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setSkceSigningdn");
        tmpStr = skceSigningdn;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSkceSigningdn", "APPL-MSG-1061", "SKCE_SIGNINGDN");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSkceSigningdn");
            return;
        }
        // Empty argument
        tmpStr = skceSigningdn.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSkceSigningdn", "APPL-MSG-1061", "SKCE_SIGNINGDN");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSkceSigningdn");
            return;
        }
        // skceSigningdn too long (> 512 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setSkceSigningdn", "APPL-ERR-1005", "SKCE_SIGNINGDN=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: SKCE_SIGNINGDN=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSkceSigningdn", "APPL-MSG-1051", "SKCE_SIGNINGDN=" + tmpStr);
        this.skceSigningdn = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSkceSigningdn");
    }


    /*********************************************************************/
    public String getSkfeAppid() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getSkfeAppid");
        return skfeAppid;
    }

    /**
     ** SKFE_APPID - Optional
     ** @param skfeAppid - String containing the universal unique
     ** ID of the signing certificate if this SKLES is using a TPM.  The UUID
     ** is easier to locate certificates in the Persistent Store of the TPM.
     **/
    public void setSkfeAppid(String skfeAppid) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setSkfeAppid");
        tmpStr = skfeAppid;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSkfeAppid", "APPL-MSG-1061", "SKFE_APPID");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSkfeAppid");
            return;
        }
        // Empty argument
        tmpStr = skfeAppid.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSkfeAppid", "APPL-MSG-1061", "SKFE_APPID");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSkfeAppid");
            return;
        }
        // skfeAppid too long (> 256 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.256charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setSkfeAppid", "APPL-ERR-1005", "SKFE_APPID=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: SKFE_APPID=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSkfeAppid", "APPL-MSG-1051", "SKFE_APPID=" + tmpStr);
        this.skfeAppid = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSkfeAppid");
    }


    /*********************************************************************/
    public String getNotes() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getNotes");
        return notes;
    }

    /**
     ** NOTES - Optional
     ** @param notes - String value of notes
     **/
    public void setNotes(String notes) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setNotes");
        tmpStr = notes;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setNotes", "APPL-MSG-1061", "NOTES");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setNotes");
            return;
        }
        // Empty argument
        tmpStr = notes.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setNotes", "APPL-MSG-1061", "NOTES");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setNotes");
            return;
        }
        // Notes too long (> 512 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setNotes", "APPL-ERR-1005", "NOTES=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: NOTES=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setNotes", "APPL-MSG-1051", "NOTES=" + tmpStr);
        this.notes = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setNotes");
    }

/******************************************************************************************
888     888 888    d8b 888 888b     d888          888    888                    888
888     888 888    Y8P 888 8888b   d8888          888    888                    888
888     888 888        888 88888b.d88888          888    888                    888
888     888 888888 888 888 888Y88888P888  .d88b.  888888 88888b.   .d88b.   .d88888 .d8888b
888     888 888    888 888 888 Y888P 888 d8P  Y8b 888    888 "88b d88""88b d88" 888 88K
888     888 888    888 888 888  Y8P  888 88888888 888    888  888 888  888 888  888 "Y8888b.
Y88b. .d88P Y88b.  888 888 888   "   888 Y8b.     Y88b.  888  888 Y88..88P Y88b 888      X88
 "Y88888P"   "Y888 888 888 888       888  "Y8888   "Y888 888  888  "Y88P"   "Y88888  88888P'
********************************************************************************************/

    @Override
    public int hashCode() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "hashCode");
        int hash = 0;
        hash += (did != null ? did.hashCode() : 0);
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "hashCode");
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
        if (!(object instanceof Domains)) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        Domains other = (Domains) object;
        if ((this.did == null && other.did != null) || (this.did != null && !this.did.equals(other.did))) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
        return true;
    }

    @Override
    public String toString() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "toString");
        return  "   did: " + did + '\n' +
                "   name: " + name + '\n' +
                "   status: " + status + '\n' +
                "   replicationStatus: " + replicationStatus + '\n' +
                "   encryptionCertificate: " + encryptionCertificate + '\n' +
                "   encryptionCertificateUuid: " + encryptionCertificateUuid + '\n' +
                "   notes: " + notes + '\n' +
                "   signingCertificate: " + signingCertificate + '\n' +
                "   signingCertificateUuid: " + signingCertificateUuid + '\n' +
                "   skceSigningdn: " + skceSigningdn + '\n' +
                "   skfeAppid: " + skfeAppid + '\n';
    }
}
