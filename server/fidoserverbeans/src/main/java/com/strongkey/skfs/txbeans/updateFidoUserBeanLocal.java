/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.SKFEException;
import javax.ejb.Local;

/**
 * Local interface for updateLdapKeyBean
 */
@Local
public interface updateFidoUserBeanLocal {

    /**
     * This method updates the value of a specific ldap user key to a new
     * value provided. This method does not perform any authentication or
     * authorization for the user against ldap. If present in ldap, this method
     * will look for the given key presence and will update it with new
     * value.
     *
     * @param did       - short, domain id
     * @param username  - String containing name of the ldap user
     * @param key       - String containing the name of the user key
     *                     in ldap
     * @param value     - String containing the new value for the user
     *                     key in ldap
     * @param deletion  - boolean that indicates if it is a delete operation for
     *                      the LDAP attribute (key). Delete means the new value
     *                      applied to the key is null
     * @return boolean based on if the operation is successful or not
     * @throws SKFEException in the event there is an error of any kind.
     */
    String execute(Long did,
                  String username,
                  String key,
                  String value,
                  boolean deletion) throws SKFEException;
}
