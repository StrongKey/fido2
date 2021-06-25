/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * This class represents the primary key of the Configuration entity.
 * It's a concatenation of the DomainID and the ConfigKey property.
 *
 */

package com.strongkey.appliance.entitybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import java.io.Serializable;
import java.util.logging.Level;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Transient;

/**********************************************************************************************
8888888888               888                    888      888          888      888
888                      888                    888      888          888      888
888                      888                    888      888          888      888
8888888    88888b.d88b.  88888b.   .d88b.   .d88888  .d88888  8888b.  88888b.  888  .d88b.
888        888 "888 "88b 888 "88b d8P  Y8b d88" 888 d88" 888     "88b 888 "88b 888 d8P  Y8b
888        888  888  888 888  888 88888888 888  888 888  888 .d888888 888  888 888 88888888
888        888  888  888 888 d88P Y8b.     Y88b 888 Y88b 888 888  888 888 d88P 888 Y8b.
8888888888 888  888  888 88888P"   "Y8888   "Y88888  "Y88888 "Y888888 88888P"  888  "Y8888
**********************************************************************************************/

@Embeddable
public class ConfigurationsPK implements Serializable
{
    private static final long serialVersionUID = 1L;
    /**
     ** Domain ID - Primary key
     **/
    @Basic(optional = false)
    @Column(name = "did", nullable = false)
    private Long did;

    /**
     ** Configuration Key - Primary key
     **/
    @Basic(optional = false)
    @Column(name = "config_key", nullable = false, length = 512)
    private String configKey;

    /**
     ** This class's name - used for logging
     **/
    @Transient
    private final String classname = this.getClass().getName();
    @Transient
    private Long tmpLong;
    @Transient
    private String tmpStr = null;

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
    public ConfigurationsPK() {
    }

    /**
     ** Default constructor
     **/
    public ConfigurationsPK(Long did, String configKey)
    {
        // Check DID first
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "ConfigurationsPK");
        tmpLong = did;
        // NULL argument
        if (did == null) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "ConfigurationsPK", "SKL-ERR-1003", "DID");
            throw new IllegalArgumentException("SKL-ERR-1003: DID");
        }
        // DID is negative, zero or larger than 9223372036854775807
        if ((tmpLong < 1) || (tmpLong > Long.MAX_VALUE)) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "ConfigurationsPK", "SKL-ERR-1007", "DID=" + tmpLong);
            throw new IllegalArgumentException("SKL-ERR-1007: DID=" + tmpLong);
        }
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "ConfigurationsPK", "SKL-MSG-1051", "DID=" + tmpLong);
        this.did = tmpLong;

        // Now, check the configuration key
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "setConfigKey");
        tmpStr = configKey;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigKey", "SKL-ERR-1003", "CONFIG_KEY");
            throw new IllegalArgumentException("SKL-ERR-1003: CONFIG_KEY");
        }
        // Empty argument
        tmpStr = configKey.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigKey", "SKL-ERR-1004", "CONFIG_KEY");
            throw new IllegalArgumentException("SKL-ERR-1004: CONFIG_KEY");
        }
        // ConfigKey too long
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigKey", "SKL-ERR-1005", "CONFIG_KEY=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("SKL-ERR-1005: CONFIG_KEY=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "setConfigKey", "SKL-MSG-1051", "CONFIG_KEY=" + tmpStr);
        this.configKey = tmpStr;
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "ConfigurationsPK");
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

    public Long getDid() {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "getDID");
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
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "setDID");
        tmpLong = did;
        // NULL argument
        if (did == null) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setDid", "SKL-ERR-1003", "DID");
            throw new IllegalArgumentException("SKL-ERR-1003: DID");
        }
        // DID is negative, zero or larger than 9223372036854775807
        if ((tmpLong < 1) || (tmpLong > Long.MAX_VALUE)) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setDid", "SKL-ERR-1007", "DID=" + tmpLong);
            throw new IllegalArgumentException("SKL-ERR-1007: DID=" + tmpLong);
        }
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "setDid", "SKL-MSG-1051", "DID=" + tmpLong);
        this.did = tmpLong;
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "setDID");
    }

   /*********************************************************************/
    public String getConfigKey() {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "getConfigKey");
        return configKey;
    }

    /**
     ** CONFIG_KEY - Required
     **/
    public void setConfigKey(String configKey) {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "setConfigKey");
        tmpStr = configKey;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigKey", "SKL-ERR-1003", "CONFIG_KEY");
            throw new IllegalArgumentException("SKL-ERR-1003: CONFIG_KEY");
        }
        // Empty argument
        tmpStr = configKey.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigKey", "SKL-ERR-1004", "CONFIG_KEY");
            throw new IllegalArgumentException("SKL-ERR-1004: CONFIG_KEY");
        }
        // ConfigKey too long
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigKey", "SKL-ERR-1005", "CONFIG_KEY=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("SKL-ERR-1005: CONFIG_KEY=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "setConfigKey", "SKL-MSG-1051", "CONFIG_KEY=" + tmpStr);
        this.configKey = tmpStr;
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "setConfigKey");
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
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "hashCode");
        int hash = 0;
        hash += did.intValue();
        hash += (configKey != null ? configKey.hashCode() : 0);
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "hashCode");
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "equals");
        if (!(object instanceof ConfigurationsPK)) {
            return false;
        }
        ConfigurationsPK other = (ConfigurationsPK) object;
        if (!this.did.equals(other.did)) {
            return false;
        }
        if ((this.configKey == null && other.configKey != null) || (this.configKey != null && !this.configKey.equals(other.configKey))) {
            return false;
        }
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "equals");
        return true;
    }

    @Override
    public String toString() {
        return "DID-CONFIG_KEY=" + did.longValue() + "-" + configKey;
    }

    String getPrimaryKey() {
        return did.longValue() + "-" + configKey;
    }
}