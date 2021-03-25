/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.utilities;

import com.strongkey.skce.hashmaps.SAConcurrentHashMapImpl;
import com.strongkey.skce.hashmaps.SAHashmap;
import com.strongkey.skce.pojos.FIDOSecretKeyInfo;
import com.strongkey.skce.pojos.FidoKeysInfo;
import com.strongkey.skce.pojos.FidoPolicyMDS;
import com.strongkey.skce.pojos.UserSessionInfo;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class skceMaps {

    private static final String classname = "skceMaps";
    /**
     * Map that stores the sessionid to a simple pojo (username and challenge)
     */
    public final static Map<String, UserSessionInfo> sessionMap = new ConcurrentSkipListMap<>();
    public final static SortedMap<String, FidoKeysInfo> FIDOkeysmap = new ConcurrentSkipListMap<>();
    /**
     * Map that stores the FIDO secret key
     */
    public static final Map<String, FIDOSecretKeyInfo> FSKMap = new ConcurrentHashMap<>();
    /**
     * Map that stores FIDO policies
     */
    public static final Map<String, FidoPolicyMDS> FPMap = new ConcurrentHashMap<>();

    static {
    }

    public skceMaps() {

    }

    public static SAHashmap getMapObj() {
        return SAConcurrentHashMapImpl.getInstance();
    }
}
