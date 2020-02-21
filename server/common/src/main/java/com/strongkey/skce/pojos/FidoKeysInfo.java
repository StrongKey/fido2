/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skce.pojos;

import com.strongkey.skfe.entitybeans.FidoKeys;
import java.util.Date;

public class FidoKeysInfo {

    private Date creationdate;
    private FidoKeys fk;

    public FidoKeysInfo(){

    }

    public FidoKeysInfo(FidoKeys fk) {
        this.fk = fk;
        this.creationdate = new Date();
    }

    public Date getCreationdate() {
        return creationdate;
    }

    public void setCreationdate(Date creationdate) {
        this.creationdate = creationdate;
    }

    public FidoKeys getFk() {
        return fk;
    }

    public void setFk(FidoKeys fk) {
        this.fk = fk;
    }

    public long getFidoKeysInfoAge() {
        Date rightnow = new Date();
        long age = (rightnow.getTime() / 1000) - (creationdate.getTime() / 1000);
        return age;
    }
}
