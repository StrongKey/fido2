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
 *************************************************
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
 *************************************************
 *
 * This entity permits the creation of custom configurations of each
 * domain when mutliple domains being hosted by the same instance of
 * the SKLES server.  Standard property files will not work in this
 * situation since property files would apply to all domains.
 */

package com.strongkey.appliance.entitybeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import java.io.Serializable;
import java.util.logging.Level;
import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Transient;

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
@Table(name = "configurations")
@NamedQueries({
    @NamedQuery(name = "Configurations.findByDid", query = "SELECT c FROM Configurations c WHERE c.configurationsPK.did = :did"),
    @NamedQuery(name = "Configurations.findByConfigKey", query = "SELECT c FROM Configurations c WHERE c.configurationsPK.did = :did and c.configurationsPK.configKey = :configKey"),
    @NamedQuery(name = "Configurations.findByConfigValue", query = "SELECT c FROM Configurations c WHERE c.configurationsPK.did = :did and c.configValue = :configValue"),
    @NamedQuery(name = "Configurations.count", query = "SELECT count(c.configurationsPK.did) FROM Configurations c WHERE c.configurationsPK.did = :did")
})

public class Configurations implements Serializable
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

    /**
     ** Compund primary key - DID and CFGID
     **/
    @EmbeddedId
    protected ConfigurationsPK configurationsPK;

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
     ** Configuration Value
     **/
    @Basic(optional = false)
    @Column(name = "config_value", nullable = false, length = 512)
    private String configValue;

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
    public Configurations() {
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

    public ConfigurationsPK getConfigurationsPK() {
        return configurationsPK;
    }

    public void setConfigurationsPK(ConfigurationsPK configurationsPK) {
        this.configurationsPK = configurationsPK;
    }

    /*********************************************************************/
    public String getConfigValue() {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "getConfigValue");
        return configValue;
    }

    /**
     ** CONFIG_VALUE - Required
     **/
    public void setConfigValue(String configValue) {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "setConfigValue");
        tmpStr = configValue;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigValue", "SKL-ERR-1003", "CONFIG_VALUE");
            throw new IllegalArgumentException("SKL-ERR-1003: CONFIG_VALUE");
        }
        // Empty argument
        tmpStr = configValue.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigValue", "SKL-ERR-1004", "CONFIG_VALUE");
            throw new IllegalArgumentException("SKL-ERR-1004: CONFIG_VALUE");
        }
        // ConfigValue too long
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setConfigValue", "SKL-ERR-1005", "CONFIG_VALUE=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("SKL-ERR-1005: CONFIG_VALUE=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "setConfigValue", "SKL-MSG-1051", "CONFIG_VALUE=" + tmpStr);
        this.configValue = tmpStr;
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "setConfigValue");
    }

    /*********************************************************************/
    public String getNotes() {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "getNotes");
        return notes;
    }

    /**
     ** NOTES - Optional
     ** @param notes - String value of notes
     **/
    public void setNotes(String notes) {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "setNotes");
        tmpStr = notes;
        // NULL argument
        if (tmpStr == null) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "setNotes", "SKL-MSG-1061", "NOTES");
            strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "setNotes");
            return;
        }
        // Empty argument
        tmpStr = notes.trim();
        if (tmpStr.length() == 0) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "setNotes", "SKL-MSG-1061", "NOTES");
            strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "setNotes");
            return;
        }
        // Notes too long
        if (tmpStr.length() > applianceCommon.getMaxLenProperty("appliance.cfg.maxlen.512charstring")) {
            strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.WARNING, classname, "setNotes", "SKL-ERR-1005", "NOTES=" + tmpStr + " (" + tmpStr.length() + " chars)");
            throw new IllegalArgumentException("SKL-ERR-1005: NOTES=" + tmpStr + " (" + tmpStr.length() + " chars)");
        }
        strongkeyLogger.logp(applianceConstants.KA_LOGGER, Level.FINE, classname, "setNotes", "SKL-MSG-1051", "NOTES=" + tmpStr);
        this.notes = tmpStr;
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "setNotes");
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
        hash += (configurationsPK != null ? configurationsPK.hashCode() : 0);
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "hashCode");
        return hash;
    }

    @Override
    public boolean equals(Object object) {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "equals");
        if (!(object instanceof Configurations)) {
            strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "equals");
            return false;
        }
        Configurations other = (Configurations) object;
        if ((this.configurationsPK == null && other.configurationsPK != null) || (this.configurationsPK != null && !this.configurationsPK.equals(other.configurationsPK))) {
            strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "equals");
            return false;
        }
        strongkeyLogger.exiting(applianceConstants.KA_LOGGER, classname, "equals");
        return true;
    }

    @Override
    public String toString() {
        strongkeyLogger.entering(applianceConstants.KA_LOGGER, classname, "toString");
        return  "   did: " + configurationsPK.getDid() + '\n' +
                "   configKey: " + configurationsPK.getConfigKey() + '\n' +
                "   configValue: " + configValue + '\n' +
                "   notes: " + notes + '\n';
    }

    public String getPrimaryKey() {
        return configurationsPK.getPrimaryKey();
    }
}
