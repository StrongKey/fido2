/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
 * Local interface for authorizeLdapUserBean
 *
 */
package com.strongkey.auth.txbeans;

import com.strongkey.skce.utilities.SKCEException;
import javax.ejb.Local;

/**
 * Local interface for authorizeLdapUserBean
 */
@Local
public interface authorizeLdapUserBeanLocal {

    /**
     * This method authenticates a credential - username and password - for a
     * specified operation against the configured LDAP directory.  Only LDAP-based
     * authentication is supported currently; however both Active Directory and a
     * standards-based, open-source LDAP directories are supported.  For the latter,
     * this has been tested with OpenDS 2.0 (https://docs.opends.org).
     *
     * @param username - String containing the credential's username
     * @param password - String containing the user's password
     * @param operation - String describing the operation being requested by the
     * user - either ENC or DEC for encryption and decryption respectively
     * @return boolean value indicating either True (for authenticated) or False
     * (for unauthenticated or failure in processing)
     * @throws com.strongkey.skce.utilities.SKCEException
     */
    boolean execute(Long did,
            String username,
                    String password,
                    String operation) throws SKCEException;
}
