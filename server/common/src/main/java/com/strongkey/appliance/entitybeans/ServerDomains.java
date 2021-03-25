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
import java.math.BigInteger;
import java.util.logging.Level;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "server_domains")
@NamedQueries({
    @NamedQuery(name = "ServerDomains.findAll", query = "SELECT s FROM ServerDomains s"),
    @NamedQuery(name = "ServerDomains.findBySid", query = "SELECT s FROM ServerDomains s WHERE s.serverDomainsPK.sid = :sid"),
    @NamedQuery(name = "ServerDomains.findByDid", query = "SELECT s FROM ServerDomains s WHERE s.serverDomainsPK.did = :did"),
    @NamedQuery(name = "ServerDomains.findBySidDid", query = "SELECT s FROM ServerDomains s WHERE s.serverDomainsPK.sid = :sid and s.serverDomainsPK.did = :did"),
    @NamedQuery(name = "ServerDomains.findPSNBySidDid", query = "SELECT s.pseudoNumber FROM ServerDomains s WHERE s.serverDomainsPK.sid = :sid and s.serverDomainsPK.did = :did and s.status = :status"),
    @NamedQuery(name = "ServerDomains.findByPseudoNumber", query = "SELECT s FROM ServerDomains s WHERE s.pseudoNumber = :pseudoNumber"),
    @NamedQuery(name = "ServerDomains.findBySidDidPseudoNumber", query = "SELECT s FROM ServerDomains s WHERE s.serverDomainsPK.sid = :sid and s.serverDomainsPK.did = :did and s.pseudoNumber = :pseudoNumber"),
    @NamedQuery(name = "ServerDomains.findByStatus", query = "SELECT s FROM ServerDomains s WHERE s.status = :status"),
    @NamedQuery(name = "ServerDomains.findByMigratingKey", query = "SELECT s FROM ServerDomains s WHERE s.migratingKey = :migratingKey"),
    @NamedQuery(name = "ServerDomains.findByNotes", query = "SELECT s FROM ServerDomains s WHERE s.notes = :notes")})

public class ServerDomains implements Serializable
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
    private String tmpStr = null;
    @Transient
    private Long tmpLong = 0L;

    /**
     * Primary key - SID-DID
     */
    @EmbeddedId
    protected ServerDomainsPK serverDomainsPK;

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
     * PseudoNumber - the Token
     */
    @Basic(optional = false)
    @Column(name = "pseudo_number")
    private String pseudoNumber;

    /**
     * Current status
     */
    @Basic(optional = false)
    @Column(name = "status", nullable = false, length = 8)
    private String status;

    /**
     * If using the TPM, the encrypted domain key
     */
    @Column(name = "migrating_key")
    private String migratingKey;

    /**
     * Optional notes
     */
    @Column(name = "notes")
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
    public ServerDomains() {
    }

    /*********************************************************************/
    /**
     * Primary key object
     * @return ServerDomainsPK
     */
    public ServerDomainsPK getServerDomainsPK() {
        return serverDomainsPK;
    }

    public void setServerDomainsPK(ServerDomainsPK serverDomainsPK) {
        this.serverDomainsPK = serverDomainsPK;
    }

    /*********************************************************************/
    public String getPseudoNumber() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getPseudoNumber");
        if (pseudoNumber != null)
            return pseudoNumber;
        else
            return null;
    }

    /**
     ** PSEUDO_NUMBER - Optional
     **/
    public void setPseudoNumber(String pseudoNumber) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setPseudoNumber");
        tmpStr = pseudoNumber;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setPseudoNumber", "APPL-ERR-1003", "PSEUDO_NUMBER");
            throw new IllegalArgumentException("APPL-ERR-1003: PSEUDO_NUMBER");
        }
        // Empty argument
        tmpStr = pseudoNumber.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setPseudoNumber", "APPL-ERR-1004", "PSEUDO_NUMBER");
            throw new IllegalArgumentException("APPL-ERR-1004: PSEUDO_NUMBER");
        }
        // Check PseudoNumber length
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.64charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setPseudoNumber", "APPL-ERR-1005", "PSEUDO_NUMBER=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: PSEUDO_NUMBER=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        // Check if it is a number
        try {
            new BigInteger(tmpStr);
        } catch (NumberFormatException ex) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setPseudoNumber", "APPL-ERR-1092", "PSEUDO_NUMBER=" + tmpStr);
            throw new IllegalArgumentException("APPL-ERR-1092: PSEUDO_NUMBER=" + tmpStr);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setPseudoNumber", "APPL-MSG-1051", "PSEUDO_NUMBER=" + tmpStr);
        this.pseudoNumber = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setPseudoNumber");
    }

   /*********************************************************************/
    public String getStatus() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getStatus");
        return status;
    }

    /**
     ** STATUS - Required
     ** @param status - String consisting of one of the following values:
     ** Active, Inactive or Other.  The status must be Active for the KeyAppliance
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
            throw new IllegalArgumentException("APPL-ERR-1015: STATUS=" + tmpStr);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setStatus", "APPL-MSG-1051", "STATUS=" + tmpStr);
        this.status = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setStatus");
    }

    /*********************************************************************/
    public String getMigratingKey() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getMigratingKey");
        return migratingKey;
    }

    /**
     ** MIGRATING_KEY - Optional
     ** @param migratingKey - The XML string of a TPM's encrypted Domain Key.
     ** It is specifically encrypted under the MASK of this server and for this
     ** specific encryption-domain.
     **/
    public void setMigratingKey(String migratingKey) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setMigratingKey");
        tmpStr = migratingKey;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setMigratingKey", "APPL-MSG-1061", "MIGRATING_KEY");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setMigratingKey");
            return;
        }
        // Empty argument
        tmpStr = migratingKey.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setMigratingKey", "APPL-MSG-1061", "MIGRATING_KEY");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setMigratingKey");
            return;
        }
        // MigratingKey too long (> 4096 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.4096charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setMigratingKey", "APPL-ERR-1005", "MIGRATING_KEY=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: MIGRATING_KEY=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setMigratingKey", "APPL-MSG-1051", "MIGRATING_KEY=" + tmpStr);
        this.migratingKey = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setMigratingKey");
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
        hash += (serverDomainsPK != null ? serverDomainsPK.hashCode() : 0);
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "hashCode");
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
        if (!(object instanceof ServerDomains)) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        ServerDomains other = (ServerDomains) object;
        if ((this.serverDomainsPK == null && other.serverDomainsPK != null) || (this.serverDomainsPK != null && !this.serverDomainsPK.equals(other.serverDomainsPK))) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
        return true;
    }

    @Override
    public String toString() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "toString");
        return  "   sid: " + serverDomainsPK.getSid() + '\n' +
                "   did: " + serverDomainsPK.getDid() + '\n' +
                "   pseudoNumber: " + pseudoNumber + '\n' +
                "   status: " + status + '\n' +
                "   migratingKey: " + migratingKey + '\n' +
                "   notes: " + notes + "\n";
    }

    // Return plain vanilla primary key
    public String getPrimaryKey() {
        return serverDomainsPK.toPlainString();
    }
}
