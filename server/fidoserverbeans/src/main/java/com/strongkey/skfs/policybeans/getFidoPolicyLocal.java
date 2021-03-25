/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/


package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.entitybeans.FidoPolicies;
import java.util.Collection;
import javax.ejb.Local;
import javax.ws.rs.core.Response;

@Local
public interface getFidoPolicyLocal {
    public Response getPolicies(Long did, Long sid, Long pid, Boolean metadataonly);

    public FidoPolicies getbyPK(Long did, Long sid, Long pid);

    public Collection<FidoPolicies> getAllActive();

    public Collection<FidoPolicies> getbyDid(Long did);
}
