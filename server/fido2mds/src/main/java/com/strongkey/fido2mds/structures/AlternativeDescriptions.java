/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

//import com.fasterxml.jackson.annotation.JsonAnySetter;
import java.util.HashMap;
import java.util.Map;

public class AlternativeDescriptions {
    private final Map<String, String> alternativeDescriptions = new HashMap<>();
    
    public Map<String, String> getAlternativeDescriptions() {
        return alternativeDescriptions;
    }

    public void setAlternativeDescriptions(String locale, String description) {
        alternativeDescriptions.put(locale, description);
    }
}
