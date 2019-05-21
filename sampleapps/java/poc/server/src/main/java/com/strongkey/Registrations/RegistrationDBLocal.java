/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
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
