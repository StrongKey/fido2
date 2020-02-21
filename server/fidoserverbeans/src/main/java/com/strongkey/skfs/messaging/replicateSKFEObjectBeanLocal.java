/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.messaging;

import javax.ejb.Local;

@Local
public interface replicateSKFEObjectBeanLocal {

    public String execute(Integer entityType, Integer replicationOperation, String primarykey, Object obj);
}
