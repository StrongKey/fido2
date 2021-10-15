/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */
package com.strongkey.skfs.fido2;

import com.strongkey.cbor.jacob.CborDecoder;
import com.strongkey.skfs.utilities.SKFSCommon;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.Map;

public class FIDO2Extensions {

    Map<String, Object> extensionMap;

    public int decodeExtensions(byte[] extensionBytes) throws IOException {
//        CBORFactory f = new CBORFactory();
//        ObjectMapper mapper = new ObjectMapper(f);
//        CBORParser parser = f.createParser(extensionBytes);
//        extensionMap = mapper.readValue(parser, new TypeReference<Map<String, Object>>() {});

        ByteArrayInputStream m_bais = new ByteArrayInputStream(extensionBytes);
        PushbackInputStream m_is = new PushbackInputStream(m_bais);
        CborDecoder m_stream = new CborDecoder(m_is);

        long len = m_stream.readMapLength();
        extensionMap = new HashMap<>();
        for (long i = 0; len < 0 || i < len; i++) {
            String key = (String) SKFSCommon.readGenericItem(m_stream);
            if (len < 0 && (key == null)) {
                // break read...
                break;
            }
            Object value = SKFSCommon.readGenericItem(m_stream);
            extensionMap.put(key, value);
        }

        //Return size of AttestedCredentialData
        int numRemainingBytes = 0;
        if (m_stream.peekType() != null) {
            numRemainingBytes = (int) m_stream.readArrayLength();
        }
//        JsonToken leftoverCBORToken;
//        while ((leftoverCBORToken = parser.nextToken()) != null) {
//            numRemainingBytes += leftoverCBORToken.asByteArray().length;
//        }
        return extensionBytes.length - numRemainingBytes;
    }

    public Object getExtension(String extensionName) {
        return extensionMap.get(extensionName);
    }
    
    public Boolean containsExtension(String extensionName) {
        return extensionMap.containsKey(extensionName);
    }

}
