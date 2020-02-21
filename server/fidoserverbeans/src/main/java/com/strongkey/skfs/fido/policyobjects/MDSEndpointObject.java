/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skce.pojos.MDSEndpoint;

public class MDSEndpointObject implements MDSEndpoint {

    private final String url;
    private final String token;

    public MDSEndpointObject(String url, String token) {
        this.url = url;
        this.token = token;
    }

    @Override
    public String getUrl() {
        return url;
    }

    @Override
    public String getToken() {
        return token;
    }
}
