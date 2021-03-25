/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.Users;

import com.strongkey.sfaeco.txbeans.Users;
import com.strongkey.sfaeco.txbeans.addUserRemote;
import com.strongkey.sfaeco.txbeans.getUserRemote;
import com.strongkey.utilities.Configurations;
import com.strongkey.utilities.Constants;
import java.io.StringReader;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;

@Stateless
public class UserDB implements UserDBLocal {

    addUserRemote addUserRemote = lookupadduserRemote();
    getUserRemote getUserRemote = lookupgetuserRemote();

    private getUserRemote lookupgetuserRemote() {
        try {
            javax.naming.Context c = new InitialContext();
            return (getUserRemote) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/getUser!com.strongkey.sfaeco.txbeans.getUserRemote");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    private addUserRemote lookupadduserRemote() {
        try {
            javax.naming.Context c = new InitialContext();
            return (addUserRemote) c.lookup("java:global/sfaeco-ear-1.0/sfaeco-ejb-1.0/addUser!com.strongkey.sfaeco.txbeans.addUserRemote");
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        }
    }

    @Override
    public boolean doesUserExist(String username) {
        try {
            return getUserFromUsername(username) != null;
        } catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    public boolean doesEmailExist(String email) {
        try {
            return getUserFromEmail(email) != null;
        } catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    public JsonObject getUserInfoFromUsername(String username) {
//        Users user = getUserFromUsername(username);
        Users user = getUserFromUsername(username);
        return Json.createObjectBuilder()
                .add(Constants.RP_JSON_KEY_EMAIL, user.getEmailAddress())
                .add(Constants.RP_JSON_KEY_FIRSTNAME, user.getGivenName())
                .add(Constants.RP_JSON_KEY_LASTNAME, user.getFamilyName())
                .build();
    }

    private Users getUserFromUsername(String username) {
        Users user =null;
        try {
            String u = getUserRemote.remoteByUsername(Short.parseShort(Configurations.getConfigurationProperty("sfaboa.cfg.property.did")), username, "SFABOA-GU-" + new Date().getTime());
            if(u == null){
                return null;
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            user = (Users) jaxbUnmarshaller.unmarshal(new StringReader(u));
        } catch (javax.xml.bind.JAXBException ex) {
            Logger.getLogger(UserDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }

    private Users getUserFromEmail(String email) {
        Users user = null;
        try {
            String u = getUserRemote.remoteByEmail(Short.parseShort(Configurations.getConfigurationProperty("sfaboa.cfg.property.did")), email, "SFABOA-GE-" + new Date().getTime());
            if(u == null){
                return null;
            }
            JAXBContext jaxbContext = JAXBContext.newInstance(Users.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            user = (Users) jaxbUnmarshaller.unmarshal(new StringReader(u));
        } catch (javax.xml.bind.JAXBException ex) {
            Logger.getLogger(UserDB.class.getName()).log(Level.SEVERE, null, ex);
        }
        return user;
    }

    @Override
    public void addUser(String email, String username, String firstName, String lastName) {
        /*
           user.setUsername(userjo.getString(Constants.JSON_KEY_USER_USERNAME));
        user.setGivenName(userjo.getString(Constants.JSON_KEY_USER_GIVEN_NAME));
        user.setFamilyName(userjo.getString(Constants.JSON_KEY_USER_FAMILY_NAME));
        user.setEmailAddress(userjo.getString(Constants.JSON_KEY_USER_EMAIL_ADDRESS));
        user.setMobileNumber(userjo.getString(Constants.JSON_KEY_USER_MOBILE_NUMBER));
         */
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("username", username);
        job.add("givenName", firstName);
        job.add("familyName", lastName);
        job.add("email", email);
        job.add("userMobileNumber", "000-000-0000");
        String resp = addUserRemote.remoteExecute(Short.parseShort(Configurations.getConfigurationProperty("sfaboa.cfg.property.did")), null, job.build().toString(), "SFABOA-AU-" + new Date().getTime());
        System.out.println(resp);
    }

    @Override
    public void deleteUser(String username) {
        //TO DO
        Users user = getUserFromUsername(username);
    }

}
