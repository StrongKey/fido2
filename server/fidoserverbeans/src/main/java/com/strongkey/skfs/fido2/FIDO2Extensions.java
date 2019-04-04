/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido2;

import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import java.io.IOException;
import java.util.Map;


public class FIDO2Extensions {
    Map<String, Object> extensionMap;
    
    public int decodeExtensions(byte[] extensionBytes) throws IOException {
        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);
        CBORParser parser = f.createParser(extensionBytes);
        extensionMap = mapper.readValue(parser, new TypeReference<Map<String, Object>>() {});
        
        //Return size of AttestedCredentialData
        int numRemainingBytes = 0;
        JsonToken leftoverCBORToken;
        while ((leftoverCBORToken = parser.nextToken()) != null) {
            numRemainingBytes += leftoverCBORToken.asByteArray().length;
        }
        return extensionBytes.length - numRemainingBytes;
    }
    
    public Object getExtension(String extensionName){
        return extensionMap.get(extensionName);
    }
    
}
