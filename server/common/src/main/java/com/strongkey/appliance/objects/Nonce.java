/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.objects;

import java.util.Date;

public class Nonce {

    private final byte[] nonce;
    private final long timestamp;

    public Nonce(byte[] nonce) {
        this.nonce = new byte[nonce.length];
        System.arraycopy(nonce, 0, this.nonce, 0, nonce.length);
        this.timestamp = new Date().getTime();
    }

    public Nonce(byte[] nonce, long timestamp) {
        this.nonce = new byte[nonce.length];
        System.arraycopy(nonce, 0, this.nonce, 0, nonce.length);
        this.timestamp = new Date().getTime();
    }

    public byte[] getNonce() {
        byte[] b = new byte[this.nonce.length];
        System.arraycopy(this.nonce, 0, b, 0, this.nonce.length);
        return b;
    }

    protected long getTimestamp() {
        return this.timestamp;
    }
}
