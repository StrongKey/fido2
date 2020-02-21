/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
