/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc.
 *
 * $Date: $
 * $Revision: $
 * $Author: $
 * $URL: $
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.entitybeans.Configurations;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Collection;
import java.util.logging.Level;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;


@Stateless
public class getFIDOConfiguration implements getFIDOConfigurationLocal {
 /**
     ** This class's name - used for logging & not persisted
     *
     */
    private final String classname = this.getClass().getName();

    /**
     * Persistence context for derby
     */
    @PersistenceContext
    private EntityManager em;

    @Override
    public Collection<Configurations> byDid(Long did) {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER, classname, "byDid");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "byDid", "SKCE-MSG-1023", "createNamedQuery(Configurations.findByDid)");
        try {
            return em.createNamedQuery("Configurations.findByDid", Configurations.class).setParameter("did", did).getResultList();
        } catch (NoResultException ex) {
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "byDid");
            return null;
        }

    }

    @Override
    public Configurations getByPK(Long did, String configKey) {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER, classname, "getByPK");
        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "byKey", "SKCE-MSG-1023", "createNamedQuery(Configurations.findByConfigKey)");
        try {
            Query q = em.createNamedQuery("Configurations.findByConfigKey");
            q.setParameter("did", did);
            q.setParameter("configKey", configKey);
            return (Configurations) q.getSingleResult();
        } catch (NoResultException ex) {
            SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER, classname, "getByPK");
            return null;
        }

    }

}
