/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.entitybeans.FidoPolicies;
import java.util.Collection;
import javax.ejb.Local;
import javax.ws.rs.core.Response;

@Local
public interface getFidoPolicyLocal {
    public Response getPolicies(Long did, String sidpid, Boolean metadataonly);

    public FidoPolicies getbyPK(Long did, Long sid, Long pid);

    public FidoPolicies getMetadataByPK(Long did, Long sid, Long pid);
    
    public Collection<FidoPolicies> getAllActive();

    public Collection<FidoPolicies> getbyDid(Long did);

    public Collection<FidoPolicies> getMetadataByDid(Long did);
}
