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
@Table(name = "servers", uniqueConstraints = {@UniqueConstraint(columnNames = {"did", "sid", "pseudo_number"}),
                         @UniqueConstraint(columnNames = {"did", "sid", "fqdn"})
})
@NamedQueries({
    @NamedQuery(name = "Servers.findAll", query = "SELECT s FROM Servers s"),
    @NamedQuery(name = "Servers.findByActiveSubscribers", query = "SELECT s FROM Servers s WHERE s.fqdn <> :fqdn and s.status = 'Active' and "
                        + "s.replicationStatus = 'Active' and s.replicationRole in ('Subscriber', 'Both')"),
    @NamedQuery(name = "Servers.findBySid", query = "SELECT s FROM Servers s WHERE s.sid = :sid"),
    @NamedQuery(name = "Servers.findByNotThisFqdn", query = "SELECT s FROM Servers s WHERE s.fqdn <> :fqdn and s.status = :status"),
    @NamedQuery(name = "Servers.findByNotThisSid", query = "SELECT s FROM Servers s WHERE s.sid <> :sid and s.status = :status"),
    @NamedQuery(name = "Servers.findByFqdn", query = "SELECT s FROM Servers s WHERE s.fqdn = :fqdn"),
    @NamedQuery(name = "Servers.findByStatus", query = "SELECT s FROM Servers s WHERE s.status = :status"),
    @NamedQuery(name = "Servers.findByReplicationRole", query = "SELECT s FROM Servers s WHERE s.replicationRole = :replicationRole"),
    @NamedQuery(name = "Servers.findByReplicationStatus", query = "SELECT s FROM Servers s WHERE s.replicationStatus = :replicationStatus"),
    @NamedQuery(name = "Servers.findByMask", query = "SELECT s FROM Servers s WHERE s.mask = :mask"),
    @NamedQuery(name = "Servers.maxPK", query = "SELECT max(s.sid) FROM Servers s"),
    @NamedQuery(name = "Servers.count", query = "SELECT count(s.sid) FROM Servers s")
})

public class Servers implements Serializable
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
     * Primary key - SID
     */
    @Id
    @Basic(optional = false)
    @Column(name = "sid")
    private Long sid;

    /**
     ** Server's fqdn
     **/
    @Basic(optional = false)
    @Column(name = "fqdn", nullable = false, length = 512)
    private String fqdn;

    /**
     ** Current status
     **/
    @Basic(optional = false)
    @Column(name = "status", nullable = false, length = 8)
    private String status;

    /**
     ** Role of this server in the replication subsystem
     **/
    @Basic(optional = false)
    @Column(name = "replication_role")
    private String replicationRole;

    /**
     ** Current status of this server in the replication subsystem
     **/
    @Basic(optional = false)
    @Column(name = "replication_status")
    private String replicationStatus;

    /**
     ** The server TPM's MASK XML content
     **/
    @Column(name = "mask", length = 2048)
    private String mask;

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
    public Servers() {
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

    /**
     ** The Primary Key
     **/
    /*********************************************************************/
    public Long getSid() {
        //strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getSid");
        return sid;
    }

    /**
     ** SID - Required
     ** @param sid - Long object containing the unique server
     ** identifier for this object.
     **/
    public void setSid(Long sid)
    {
        // Check SID next
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setSid");
        tmpLong = sid;
        // NULL argument
        if (sid == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setSid", "APPL-ERR-1003", "SID");
            throw new IllegalArgumentException("APPL-ERR-1003: SID");
        }
        // SID is negative, zero or larger than 9223372036854775807
        if ((tmpLong < 1) || (tmpLong > Long.MAX_VALUE)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setSid", "APPL-ERR-1007", "SID=" + tmpLong);
            throw new IllegalArgumentException("APPL-ERR-1007: SID=" + tmpLong);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setSid", "APPL-MSG-1051", "SID=" + tmpLong);
        this.sid = tmpLong;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setSid");
    }

    /*********************************************************************/
    public String getFqdn() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getFqdn");
        return fqdn;
    }

    /**
     ** FQDN - Required
     ** @param fqdn - String containing the fully qualified domain name of the
     ** computer
     **/
    public void setFqdn(String fqdn) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setFqdn");
        tmpStr = fqdn;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setFqdn", "APPL-ERR-1003", "FQDN");
            throw new IllegalArgumentException("APPL-ERR-1003: FQDN");
        }
        // Empty argument
        tmpStr = fqdn.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setFqdn", "APPL-ERR-1004", "FQDN");
            throw new IllegalArgumentException("APPL-ERR-1004: FQDN");
        }
        // Fqdn too long
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setFqdn", "APPL-ERR-1005", "FQDN=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: FQDN=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setFqdn", "APPL-MSG-1051", "FQDN=" + tmpStr);
        this.fqdn = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setFqdn");
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
    public String getReplicationRole() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getReplicationRole");
        return replicationRole;
    }

    /**
     ** REPLICATION_ROLE - Required
     ** @param replicationRole - String consisting of one of the following values:
     ** Publisher, Subscriber or Both.
     **/
    public void setReplicationRole(String replicationRole) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setReplicationRole");
        tmpStr = replicationRole;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setReplicationRole", "APPL-ERR-1003", "REPLICATION_ROLE");
            throw new IllegalArgumentException("APPL-ERR-1003: REPLICATION_ROLE");
        }
        // Empty argument
        tmpStr = replicationRole.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setReplicationRole", "APPL-ERR-1004", "REPLICATION_ROLE");
            throw new IllegalArgumentException("APPL-ERR-1004: REPLICATION_ROLE");
        }
        // Incorrect replicationRole - not Publisher, Subscriber or Both
         if (!tmpStr.trim().equals("Publisher"))
            if (!tmpStr.trim().equals("Subscriber"))
                if (!tmpStr.trim().equals("Both")) {
                    strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setReplicationRole", "APPL-ERR-1015", "REPLICATION_ROLE=" + tmpStr);
                    throw new IllegalArgumentException("APPL-ERR-1015: REPLICATION_ROLE=" + tmpStr);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setReplicationRole", "APPL-MSG-1051", "REPLICATION_ROLE=" + tmpStr);
        this.replicationRole = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setReplicationRole");
    }

     /*********************************************************************/
    public String getReplicationStatus() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getReplicationStatus");
        return replicationStatus;
    }

     /**
     ** REPLICATION_STATUS - Required
     ** @param replicationStatus - String consisting of one of the following values:
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

    /*********************************************************************/
    public String getMask() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "getMask");
        return mask;
    }

    /**
     ** MASK - Optional
     ** @param mask - String value of this server TPM's Public Key of the
     ** Migrating Authority Storage Key (MASK).  Used to "migrate" encrypted
     ** private keys between TPMs of a cluster.
     **/
    public void setMask(String mask) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "setMask");
        tmpStr = mask;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setMask", "APPL-MSG-1061", "MASK");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setMask");
            return;
        }
        // Empty argument
        tmpStr = mask.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setMask", "APPL-MSG-1061", "MASK");
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setMask");
            return;
        }
        // Mask too long (> 2048 characters)
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.2048charstring")) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setMask", "APPL-ERR-1005", "MASK=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("APPL-ERR-1005: MASK=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setMask", "APPL-MSG-1051", "MASK=" + tmpStr);
        this.mask = tmpStr;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setMask");
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
        hash += (sid != null ? sid.hashCode() : 0);
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "hashCode");
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
        if (!(object instanceof Servers)) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        Servers other = (Servers) object;
        if ((this.sid == null && other.sid != null) || (this.sid != null && !this.sid.equals(other.sid))) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
        return true;
    }

    @Override
    public String toString() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER, classname, "toString");
        return  "   sid: " + sid + '\n' +
                "   fqdn: " + fqdn + '\n' +
                "   mask: " + mask + '\n' +
                "   replicationRole: " + replicationRole + '\n' +
                "   replicationStatus: " + replicationStatus + '\n' +
                "   status: " + status + '\n';
    }

    // Return plain vanilla primary key
    public String getPrimaryKey() {
        return sid.toString();
    }
}
