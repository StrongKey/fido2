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

public class ECKeyObject extends FIDO2KeyObject {

    private byte[] x, y;
    private long crv;
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
         Map<Object, Object> pkObjectMap = new HashMap<>();
            for (long i = 0; len < 0 || i < len; i++) {
                Object key = SKFSCommon.readGenericItem(m_stream);
                if (len < 0 && (key == null)) {
                    // break read...
                    break;
                }
                Object value = SKFSCommon.readGenericItem(m_stream);
                pkObjectMap.put(key, value);
            }

        for (Map.Entry<Object,Object> entry : pkObjectMap.entrySet()) {
//        for (Object key : pkObjectMap.keySet()) {
            Object key = entry.getKey();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "key : " + key);
            switch (Long.toString((long)key)) {
                case "1":
                    kty = (long) entry.getValue();
                    break;
                case "-1":
                    crv = (long) entry.getValue();
                    break;
                case "3":
                    alg = (long) entry.getValue();
                    break;
                case "-2":
                    x = (byte[]) entry.getValue();
                    break;
                case "-3":
                    y = (byte[]) entry.getValue();
                    break;
                default :
                    break;
            }
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            CborEncoder cbe = new CborEncoder(baos);
            cbe.writeMapStart(5);

            cbe.writeInt(1);
            cbe.writeInt((int)kty);

            cbe.writeInt(3);
            cbe.writeInt((int)alg);

            cbe.writeInt(-1);
            cbe.writeInt((int)crv);

            cbe.writeInt(-2);
            cbe.writeByteString(x);

            cbe.writeInt(-3);
            cbe.writeByteString(y);

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

    public byte[] getX() {
        return x;
    }

    public byte[] getY() {
        return y;
    }

    public long getCrv() {
        return crv;
    }

}
