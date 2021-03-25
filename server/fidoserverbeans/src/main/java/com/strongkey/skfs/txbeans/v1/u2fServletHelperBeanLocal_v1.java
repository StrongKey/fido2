/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 * Local interface for u2fServletHelperBean
 *
 */
package com.strongkey.skfs.txbeans.v1;

import javax.ejb.Local;


@Local
public interface u2fServletHelperBeanLocal_v1 {

    String preregister(String did, String protocol, String payload);

    String register(String did, String protocol,String payload);

    String preauthenticate(String did, String protocol, String username);

    String authenticate(String did, String protocol, String payload, String agent, String cip);

    String preauthorize(String did, String protocol, String username);

    String authorize(String did, String protocol, String payload);

    String deregister(String did, String protocol, String payload);

    String activate(String did, String protocol, String payload);

    String deactivate(String did, String protocol, String payload);

    String getkeysinfo(String did, String protocol, String payload);
}
