/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
import javax.ejb.Local;
import javax.ws.rs.core.Response;

@Local
public interface addFidoPolicyLocal {

    public Response execute(Long did, CreateFidoPolicyRequest request);
}
