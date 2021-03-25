/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.requests.AuthenticationRequest;
import com.strongkey.skfs.requests.PreauthenticationRequest;
import com.strongkey.skfs.requests.PreauthorizeRequest;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.requests.RegistrationRequest;
import com.strongkey.skfs.requests.UpdateFidoKeyRequest;
import javax.ejb.Local;
import javax.ws.rs.core.Response;


@Local
public interface u2fServletHelperBeanLocal {

    Response preregister(Long did, PreregistrationRequest preregistration);

    Response register(Long did, RegistrationRequest registration);

    Response preauthenticate(Long did, PreauthenticationRequest preauthentication);
    
    Response preauthorize(Long did, PreauthorizeRequest preauthorize);

    Response authenticate(Long did, AuthenticationRequest authentication, String agent, String cip);
    
    Response authorize(Long did, AuthenticationRequest authentication);

    Response deregister(Long did, String keyid);

    Response patchfidokey(Long did, String keyid, UpdateFidoKeyRequest fidokey);

    Response getkeysinfo(Long did, String username);
}
