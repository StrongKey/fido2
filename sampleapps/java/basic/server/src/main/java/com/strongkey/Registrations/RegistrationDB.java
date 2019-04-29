/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */
package com.strongkey.Registrations;

import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class RegistrationDB implements RegistrationDBLocal {

    @PersistenceContext
    private EntityManager em;
    
    @Override
    public boolean doesRegistrationForNonceExist(String nonce) {
        try {
            return getRegistrationFromNonce(nonce) != null;
        } catch (NoResultException ex) {
            return false;
        }
    }
    
    @Override
    public boolean doesRegistrationForEmailExist(String email) {
        try {
            return getRegistrationFromEmail(email) != null;
        } catch (NoResultException ex) {
            return false;
        }
    }
    
    @Override
    public String getEmailFromNonce(String nonce){
        try {
            return getRegistrationFromNonce(nonce).getEmail();
        } catch (NoResultException ex) {
            return null;
        }
    }

    @Override
    public void addRegistration(String email, String nonce) {
        Registrations reg = new Registrations();
        reg.setEmail(email);
        reg.setNonce(nonce);
        em.persist(reg);
        em.flush();
        em.clear();
    }

    @Override
    public void deleteRegistration(String email) {
        em.remove(getRegistrationFromEmail(email));
        em.flush();
        em.clear();
    }
    
    
    private Registrations getRegistrationFromNonce(String nonce) {
        TypedQuery<Registrations> q = em.createNamedQuery("Registrations.findByNonce", Registrations.class);
        q.setParameter("nonce", nonce);
        Registrations registration = (Registrations) q.getSingleResult();
        return registration;
    }

    private Registrations getRegistrationFromEmail(String email) {
        TypedQuery<Registrations> q = em.createNamedQuery("Registrations.findByEmail", Registrations.class);
        q.setParameter("email", email);
        Registrations registration = (Registrations) q.getSingleResult();
        return registration;
    }
}
