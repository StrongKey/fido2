/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.requests.AuthenticationRequest;
import com.strongkey.skfs.requests.PatchFidoKeyRequest;
import com.strongkey.skfs.requests.PreauthenticationRequest;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.requests.RegistrationRequest;
import javax.ejb.Local;
import javax.ws.rs.core.Response;


@Local
public interface u2fServletHelperBeanLocal {
    
    Response preregister(Long did, PreregistrationRequest preregistration);

    Response register(Long did, RegistrationRequest registration);

    Response preauthenticate(Long did, PreauthenticationRequest preauthentication);

    Response authenticate(Long did, AuthenticationRequest authentication);

    Response deregister(Long did, String keyid);

    Response patchfidokey(Long did, String keyid, PatchFidoKeyRequest fidokey);

    Response getkeysinfo(Long did, String username);
}
