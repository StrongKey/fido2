/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import javax.ejb.Local;

@Local
public interface addFidoKeysLocal {

    public String execute(Long did,
            String userid,
            String username,
            String UKH,
            String UPK,
            String appid,
            Short transports,
            Short attsid,
            Short attdid,
            Integer attcid,
            int counter,
            String fido_version,
            String fido_protocol,
            String aaguid,
            String registrationSettings,
            Integer registrationSettingsVersion,
            String create_location);
}
