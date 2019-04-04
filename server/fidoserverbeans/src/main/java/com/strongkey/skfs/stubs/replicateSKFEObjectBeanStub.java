/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.stubs;

import com.strongkey.skfs.messaging.replicateSKFEObjectBeanLocal;
import javax.ejb.Stateless;

@Stateless
public class replicateSKFEObjectBeanStub implements replicateSKFEObjectBeanLocal {

    
    @Override
    public String execute(Integer entityType, Integer replicationOperation, String primarykey, Object obj) {
        return null;
    }
}
