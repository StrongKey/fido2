/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import javax.ejb.Local;

@Local
public interface U2FRegistrationBeanLocal {

    public String execute(Long did, String registrationresponse, String registrationmetadata, String protocol);
}
