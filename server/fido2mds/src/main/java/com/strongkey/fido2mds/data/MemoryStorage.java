/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.data;

import java.util.HashMap;
import java.util.Map;

public class MemoryStorage extends Storage {

    private final Map<String, Map<String, String>> mapOfMaps = new HashMap<>();

    public MemoryStorage() {
        super();
    }

    @Override
    public String loadData(String namespace, String key) {
        String data = null;
        Map<String, String> map = mapOfMaps.get(namespace);
        if (map != null) {
            data = map.get(key);
        }
        return data;
    }

    @Override
    public void saveData(String namespace, String key, String data) {
        Map<String, String> map = mapOfMaps.get(namespace);
        if (map == null) {
            map = new HashMap<>();
            mapOfMaps.put(namespace,map);
        }
        map.put(key, data);
    }

}
