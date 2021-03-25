/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * Gets the user object 
 */

package com.strongkey.sfaeco.txbeans;

import com.strongkey.sfaeco.entitybeans.Users;
import com.strongkey.sfaeco.utilities.Common;
import java.io.StringWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

@Stateless
public class getUser implements getUserLocal, getUserRemote {

    String CLASSNAME = "getUser";
            
    // Resources used by this bean
    @PersistenceContext private EntityManager   em;
    
    /**
     * Returns an Users object based on the userid
     *
     * @param did Long Domain ID
     * @param uid Long User ID
     * @return Users object
     */
    @Override
    public Users byUid(short did, Long uid, String txid) {
        
        String methodname = CLASSNAME.concat("findByDidUid");
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        try {
            TypedQuery<Users> q = em.createNamedQuery("Users.findByDidUid", Users.class);
            q.setParameter("did", did);
            q.setParameter("uid", uid);
            Users user = q.getSingleResult();
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return user;
        } catch (NoResultException ex) {
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return null;
        }
    }
    
    /**
     * Finds by Did and username
     * @param did
     * @param username
     * @param txid
     * @return 
     */
    @Override
    public Users byUsername(short did, String username, String txid) {
        String methodname = CLASSNAME.concat("byUsername");
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        try {
            TypedQuery<Users> q = em.createNamedQuery("Users.findByUsername", Users.class);
            q.setParameter("did", did);
            q.setParameter("username", username);
            Users user = q.getSingleResult();
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return user;
        } catch (NoResultException ex) {
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return null;
        }
    }   

    @Override
    public Users byEmail(short did, String email, String txid) {
        String methodname = CLASSNAME.concat("byEmail");
        
        // Entry log
        long timein = Common.entryLog(Level.INFO, "SFAECO-MSG-3003", methodname, String.valueOf(did), txid);
        
        try {
            TypedQuery<Users> q = em.createNamedQuery("Users.findByEmailAddress", Users.class);
            q.setParameter("did", did);
            q.setParameter("emailAddress", email);
            Users user = q.getSingleResult();
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return user;
        } catch (NoResultException ex) {
            Common.exitLog(Level.INFO, "SFAECO-MSG-3004", methodname, String.valueOf(did), txid, timein);
            return null;
        }
    }   
    
    
    @Override
    public String remoteByUid(short did, Long uid, String txid) {
        Users u = byUid(did, uid, txid);
        if(u ==null){
            return null;
        }
        StringWriter writer = new StringWriter();
        JAXBContext jaxbContext;
        Marshaller marshaller;
        try {
            jaxbContext = JAXBContext.newInstance(Users.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(u, writer);
        } catch (javax.xml.bind.JAXBException ex) {
            Logger.getLogger(getUser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString();
    }

    @Override
    public String remoteByUsername(short did, String username, String txid) {
        Users u = byUsername(did, username, txid);
        if(u ==null){
            return null;
        }
        StringWriter writer = new StringWriter();
            JAXBContext jaxbContext;
            Marshaller marshaller;
            try {
                jaxbContext = JAXBContext.newInstance(Users.class);
                marshaller = jaxbContext.createMarshaller();
                marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
                marshaller.marshal(u, writer);
            } catch (javax.xml.bind.JAXBException ex) {
                Logger.getLogger(getUser.class.getName()).log(Level.SEVERE, null, ex);
            }
            return writer.toString();
    }

    @Override
    public String remoteByEmail(short did, String email, String txid) {
        Users u = byEmail(did, email, txid);
        if(u ==null){
            return null;
        }
        StringWriter writer = new StringWriter();
        JAXBContext jaxbContext;
        Marshaller marshaller;
        try {
            jaxbContext = JAXBContext.newInstance(Users.class);
            marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
            marshaller.marshal(u, writer);
        } catch (javax.xml.bind.JAXBException ex) {
            Logger.getLogger(getUser.class.getName()).log(Level.SEVERE, null, ex);
        }
        return writer.toString(); 
    }
}
