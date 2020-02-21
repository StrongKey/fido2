/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.fido2mds.data;

public abstract class Storage {

    public Storage() {
    }

    public abstract String loadData(String namespace, String key);

    public abstract void saveData(String namespace, String key, String data);
}
