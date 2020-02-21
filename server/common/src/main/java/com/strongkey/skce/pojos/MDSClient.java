/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.pojos;

import java.util.List;
import javax.json.JsonObject;

public interface MDSClient {
    public JsonObject getTrustAnchors(String aaguid, List<String> allowedStatusList);
}
