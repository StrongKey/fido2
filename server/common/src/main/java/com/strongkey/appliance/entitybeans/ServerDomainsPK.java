/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.entitybeans;

import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import java.io.Serializable;
import java.util.logging.Level;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

@Embeddable
public class ServerDomainsPK implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     ** This class's name - used for logging
     **/
    @Transient
    private final String classname = this.getClass().getName();
    @Transient
    private Long tmpLong;

    /**
     ** Server ID - Primary key
     **/
    @Basic(optional = false)
    @Column(name = "sid", nullable = false)
    private Long sid;

     /**
     ** Domain ID - Primary key
     **/
    @Basic(optional = false)
    @Column(name = "did", nullable = false)
    private Long did;

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
    public ServerDomainsPK() {
    }

        /**
     ** Default Constructor
     **/
    public ServerDomainsPK(Long sid, Long did)
    {
        // Check SID
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "ServerDomainsPK");
        tmpLong = sid;
        // NULL argument
        if (sid == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "ServerDomainsPK", "APPL-ERR-1003", "SID");
            throw new IllegalArgumentException("APPL-ERR-1003: SID");
        }
        // SID is negative, zero or larger than 9223372036854775807
        if ((tmpLong < 1) || (tmpLong > Long.MAX_VALUE)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "ServerDomainsPK", "APPL-ERR-1007", "SID=" + tmpLong);
            throw new IllegalArgumentException("APPL-ERR-1007: SID=" + tmpLong);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "ServerDomainsPK", "APPL-MSG-1051", "SID=" + tmpLong);
        this.sid = tmpLong;

        // Check DID
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "ServerDomainsPK");
        tmpLong = did;
        // NULL argument
        if (did == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "ServerDomainsPK", "APPL-ERR-1003", "DID");
            throw new IllegalArgumentException("APPL-ERR-1003: DID");
        }
        // DID is negative, zero or larger than 9223372036854775807
        if ((tmpLong < 1) || (tmpLong > Long.MAX_VALUE)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "ServerDomainsPK", "APPL-ERR-1007", "DID=" + tmpLong);
            throw new IllegalArgumentException("APPL-ERR-1007: DID=" + tmpLong);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "ServerDomainsPK", "APPL-MSG-1051", "DID=" + tmpLong);
        this.did = tmpLong;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "ServerDomainsPK");
    }

/*****************************************************************
888b     d888          888    888                    888
8888b   d8888          888    888                    888
88888b.d88888          888    888                    888
888Y88888P888  .d88b.  888888 88888b.   .d88b.   .d88888 .d8888b
888 Y888P 888 d8P  Y8b 888    888 "88b d88""88b d88" 888 88K
888  Y8P  888 88888888 888    888  888 888  888 888  888 "Y8888b.
888   "   888 Y8b.     Y88b.  888  888 Y88..88P Y88b 888      X88
888       888  "Y8888   "Y888 888  888  "Y88P"   "Y88888  88888P'
******************************************************************/

    public Long getSid() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "getSid");
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
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "setSid");
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
    public Long getDid() {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "getDid");
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
    public void setDid(Long did)
    {
        // Check DID first
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "setDid");
        tmpLong = did;
        // NULL argument
        if (did == null) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setDid", "APPL-ERR-1003", "DID");
            throw new IllegalArgumentException("APPL-ERR-1003: DID");
        }
        // DID is negative, zero or larger than 9223372036854775807
        if ((tmpLong < 1) || (tmpLong > Long.MAX_VALUE)) {
            strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.WARNING, classname, "setDid", "APPL-ERR-1007", "DID=" + tmpLong);
            throw new IllegalArgumentException("APPL-ERR-1007: DID=" + tmpLong);
        }
        strongkeyLogger.logp(applianceConstants.APPLIANCE_LOGGER, Level.FINE, classname, "setDid", "APPL-MSG-1051", "DID=" + tmpLong);
        this.did = tmpLong;
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "setDid");
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
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "hashCode");
        int hash = 0;
        hash += sid.longValue();
        hash += did.longValue();
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "hashCode");
        return hash;
    }

    @Override
    public boolean equals(Object object)
    {
        strongkeyLogger.entering(applianceConstants.APPLIANCE_LOGGER,classname, "equals");
        if (!(object instanceof ServerDomainsPK)) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        ServerDomainsPK other = (ServerDomainsPK) object;
        if (!this.sid.equals(other.sid)) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        if (!this.did.equals(other.did)) {
            strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
            return false;
        }
        strongkeyLogger.exiting(applianceConstants.APPLIANCE_LOGGER, classname, "equals");
        return true;
    }

    @Override
    public String toString() {
        return "SID-DID=" + sid.longValue() + "-" + did.longValue();
    }

    // String of the PK without the labels
    public String toPlainString() {
        return sid.longValue() + "-" + did.longValue();
    }
}
