/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.pojos;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * POJO to store a map of randomid -> fidoregistrationkeyid
 */
public class UserKeyPointers {

    /**
     * map of randomid->registrationKeyId of the database. The length of this map
     * will be equal to the number of keys successfully registered for the username.
     *
     * IMPORTANT - there are no null checks for the input map during object construction.
     * It is the callers responsibility to ensure they pass in what they want.
     */
    private Map<String, String> userkeypointerMap = new ConcurrentSkipListMap<>();

    private Date creationdate = null;

    /**
     * Constructor of this class.
     * @param userkeypointerMap
     */
    public UserKeyPointers(Map<String, String> userkeypointerMap) {
        this.userkeypointerMap = userkeypointerMap;
        this.creationdate = new Date();
    }

    /**
     * Get set methods
     * @return
     */
    public Map<String, String> getUserKeyPointersMap() {
        return userkeypointerMap;
    }

    public void setUserKeyPointersMap(Map<String, String> userkeypointerMap) {
        this.userkeypointerMap = userkeypointerMap;
    }

    public void setUserkeypointerMap(Map<String, String> userkeypointerMap) {
        this.userkeypointerMap = userkeypointerMap;
    }

    public Date getCreationdate() {
        if(this.creationdate == null){
            return null;
        }
        return new Date(creationdate.getTime());
    }

    public void setCreationdate(Date creationdate) {
        if (creationdate != null)
            this.creationdate = new Date(creationdate.getTime());
        else
            this.creationdate = null;
    }

    public long getUserKeyPointersAge() {
        Date rightnow = new Date();
        long age = (rightnow.getTime()/1000) - (creationdate.getTime()/1000);
        return age;
    }

    /**
     * Over-ridden toString method to print the object content in a readable
     * manner
     * @return  String with object content laid in a readable manner.
     */
    @Override
    public String toString() {
        return    "\n    userkeypointerMap.length = " + this.userkeypointerMap.size()
                + "\n    age = " + getUserKeyPointersAge() + " seconds";
    }
}
