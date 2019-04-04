/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
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
