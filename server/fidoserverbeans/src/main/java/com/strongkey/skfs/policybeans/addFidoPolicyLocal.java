/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/FIDO-Server/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
import javax.ejb.Local;
import javax.ws.rs.core.Response;

/**
 *
 * @author mishimoto
 */
@Local
public interface addFidoPolicyLocal {
    
    public Response execute(Long did, CreateFidoPolicyRequest request);
}
