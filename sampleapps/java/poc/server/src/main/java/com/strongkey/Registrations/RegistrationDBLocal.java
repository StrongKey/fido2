/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.Registrations;

import javax.ejb.Local;

@Local
public interface RegistrationDBLocal {
    public boolean doesRegistrationForNonceExist(String nonce);

    public boolean doesRegistrationForEmailExist(String email);

    public String getEmailFromNonce(String nonce);

    public void addRegistration(String email, String nonce);

    public void deleteRegistration(String email);
}
