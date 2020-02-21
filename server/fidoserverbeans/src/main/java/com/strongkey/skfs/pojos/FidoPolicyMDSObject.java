/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.pojos;

import com.strongkey.skce.pojos.FidoPolicyMDS;
import com.strongkey.skce.pojos.MDSClient;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;

public class FidoPolicyMDSObject implements FidoPolicyMDS {
    private final FidoPolicyObject fp;
    private final MDSClient mds;

    public FidoPolicyMDSObject(FidoPolicyObject fp, MDSClient mds){
        this.fp = fp;
        this.mds = mds;
    }

    public FidoPolicyObject getFp() {
        return fp;
    }

    public MDSClient getMds() {
        return mds;
    }
}
