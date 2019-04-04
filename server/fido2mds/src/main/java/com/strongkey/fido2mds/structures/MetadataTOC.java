/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

import com.strongkey.appliance.objects.JWT;

public class MetadataTOC {
    
    JWT jwt;
    MetadataTOCPayload payload;

    public MetadataTOC() {
    }

    public void setJWT(JWT jwt) {
        this.jwt = jwt;
    }

    public void setPayload(MetadataTOCPayload payload) {
        this.payload = payload;
    }

    public JWT getJwt() {
        return jwt;
    }

    public MetadataTOCPayload getPayload() {
        return payload;
    }
    
}
