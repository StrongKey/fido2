/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */
package com.strongkey.Users;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface UserDBLocal {
    public boolean doesUserExist(String username);
    
    public boolean doesEmailExist(String email);
    
    public JsonObject getUserInfoFromUsername(String username);

    public void addUser(String email, String username, String firstName, String lastName);

    public void deleteUser(String username);
}
