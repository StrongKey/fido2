/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skce.pojos;

import java.util.Date;

public class FIDOSecretKeyInfo {

    private String secretkey = null;
    private Date creationdate = null;
    private int sid = 0;

    public FIDOSecretKeyInfo(String secretK, Integer sid) {
        this.secretkey = secretK;
        this.sid = sid;
        this.creationdate = new Date();
    }

    @Override
    public String toString() {
        return "FIDOSecretKeyInfo{" + "secretkey=" + secretkey + ", creationdate=" + creationdate + ", sid=" + sid + '}';
    }

    public String getSecretkey() {
        return secretkey;
    }

    public void setSecretkey(String secretkey) {
        this.secretkey = secretkey;
    }

    public Date getCreationdate() {
        if(creationdate == null){
            return null;
        }
        return new Date(creationdate.getTime());
    }

    public void setCreationdate(Date creationdate) {
        if(creationdate == null){
            this.creationdate = null;
        }else{
            this.creationdate = new Date(creationdate.getTime());    
        }
    }

    public int getSid() {
        return sid;
    }

    public void setSid(int sid) {
        this.sid = sid;
    }


}
