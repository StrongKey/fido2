/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds;

import com.strongkey.fido2mds.structures.MetadataStatement;
import com.strongkey.fido2mds.structures.MetadataTOCPayloadEntry;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public abstract class MDSService {
    protected Map<String,MetadataTOCPayloadEntry> tocEntryMap;
    protected Map<String,MetadataStatement> metadataStatementMap;

    public abstract void refresh();
    
    public MetadataTOCPayloadEntry getTOCEntry(String key) {
        return tocEntryMap.get(key);   
    }
    
    public MetadataStatement getMetadataStatement(String key) {
        return metadataStatementMap.get(key);
    }
    
    public Set<String> getKeys() {
        HashSet<String> ret = new HashSet<>();
        ret.addAll(tocEntryMap.keySet());
        ret.addAll(metadataStatementMap.keySet());
        return ret;
    }
}
