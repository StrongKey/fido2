/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.strongkey.cbor.jacob.CborDecoder;
import com.strongkey.cbor.jacob.CborEncoder;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class RSAKeyObject extends FIDO2KeyObject {

    private byte[] n, e;
    private int encodedLength;

    public void decode(byte[] cbor) throws IOException {

//        CBORFactory f = new CBORFactory();
//        ObjectMapper mapper = new ObjectMapper(f);
//        CBORParser parser = f.createParser(cbor);
//
//        Map<String, Object> pkObjectMap = mapper.readValue(parser, new TypeReference<Map<String, Object>>() {
//        });

        ByteArrayInputStream m_bais = new ByteArrayInputStream(cbor);
        PushbackInputStream m_is = new PushbackInputStream(m_bais);
        CborDecoder m_stream = new CborDecoder(m_is);
        
        long len = m_stream.readMapLength();
         Map<String, Object> pkObjectMap = new HashMap<>();
            for (long i = 0; len < 0 || i < len; i++) {
                String key = (String) SKFSCommon.readGenericItem(m_stream);
                if (len < 0 && (key == null)) {
                    // break read...
                    break;
                }
                Object value = SKFSCommon.readGenericItem(m_stream);
                pkObjectMap.put(key, value);
            }

//        for (String key : pkObjectMap.keySet()) {
        for (Map.Entry<String,Object> entry : pkObjectMap.entrySet()) {
            String key = entry.getKey();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "key : " + key + ", Value : " + pkObjectMap.get(key).toString());
            switch (key) {
                case "1":
                    kty = (int) pkObjectMap.get(key);
                    break;
                case "3":
                    alg = (int) pkObjectMap.get(key);
                    break;
                case "-1":
                    n = (byte[]) pkObjectMap.get(key);
                    break;
                case "-2":
                    e = (byte[]) pkObjectMap.get(key);
                    break;
                default:
                    break;
            }
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            CborEncoder cbe = new CborEncoder(baos);
            cbe.writeMapStart(4);

            cbe.writeInt(1);
            cbe.writeInt(kty);

            cbe.writeInt(3);
            cbe.writeInt(alg);

            cbe.writeInt(-1);
            cbe.writeByteString(n);

            cbe.writeInt(-2);
            cbe.writeByteString(e);


        } catch (IOException e) {
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "FIDO-ERR-0001",
                e.getMessage());
        }
        
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Length of Encoded Attested Cred. Data = " + baos.toByteArray().length);
        encodedLength = baos.toByteArray().length;
    }

    public int getEncodedLength() {
        return encodedLength;
    }
    
    public byte[] getN() {
        return n;
    }

    public byte[] getE() {
        return e;
    }


}
