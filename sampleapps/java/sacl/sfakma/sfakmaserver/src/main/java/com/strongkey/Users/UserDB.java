/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.Users;

import com.strongkey.utilities.Constants;
import java.util.Date;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonObject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Stateless
public class UserDB implements UserDBLocal {

    @PersistenceContext
    private EntityManager em;

    @Override
    public boolean doesUserExist(String username) {
        try {
            return getUserFromUsername(username) != null;
        } catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    public boolean doesEmailExist(String email){
        try {
            return getUserFromEmail(email) != null;
        } catch (NoResultException ex) {
            return false;
        }
    }

    @Override
    public JsonObject getUserInfoFromUsername(String username){
        Users user = getUserFromUsername(username);
        return Json.createObjectBuilder()
                .add(Constants.RP_JSON_KEY_EMAIL, user.getEmail())
                .add(Constants.RP_JSON_KEY_FIRSTNAME, user.getFirstName())
                .add(Constants.RP_JSON_KEY_LASTNAME, user.getLastName())
                .build();
    }

    private Users getUserFromUsername(String username) {
        TypedQuery<Users> q = em.createNamedQuery("Users.findByUsername", Users.class);
        q.setParameter("username", username);
        Users user = (Users) q.getSingleResult();
        return user;
    }

    private Users getUserFromEmail(String email) {
        TypedQuery<Users> q = em.createNamedQuery("Users.findByEmail", Users.class);
        q.setParameter("email", email);
        Users user = (Users) q.getSingleResult();
        return user;
    }

    @Override
    public void addUser(String email, String username, String firstName, String lastName) {
        Users user = new Users();
        user.setEmail(email);
        user.setUsername(username);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setCreateDate(new Date());

        em.persist(user);
        em.flush();
        em.clear();
    }

    @Override
    public void deleteUser(String username) {
        Users user = getUserFromUsername(username);

        em.remove(user);
        em.flush();
        em.clear();
    }

}
