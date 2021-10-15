/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.txbeans;

import javax.ejb.Local;

@Local
public interface updateFidoKeysLocal {

    public String execute(Short sid,Long did,
            Long fkid,
            Integer newCounter,
            String modify_location);
//    public String execute(Short sid,Long did,
//            String username,Long fkid,
//            Integer newCounter,
//            String modify_location);
}
