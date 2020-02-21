/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skfs.fido2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.strongkey.skfs.utilities.skfsLogger;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;

public class ECKeyObject extends FIDO2KeyObject {

    private byte[] x, y;
    private int crv;

    public void decode(byte[] cbor) throws IOException {

        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);
        CBORParser parser = f.createParser(cbor);

        Map<String, Object> pkObjectMap = mapper.readValue(parser, new TypeReference<Map<String, Object>>() {
        });

        for (String key : pkObjectMap.keySet()) {
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "key : " + key);
            switch (key) {
                case "1":
                    kty = (int) pkObjectMap.get(key);
                    break;
                case "-1":
                    crv = (int) pkObjectMap.get(key);
                    break;
                case "3":
                    alg = (int) pkObjectMap.get(key);
                    break;
                case "-2":
                    x = (byte[]) pkObjectMap.get(key);
                    break;
                case "-3":
                    y = (byte[]) pkObjectMap.get(key);
                    break;
            }
        }
    }

    public byte[] getX() {
        return x;
    }

    public byte[] getY() {
        return y;
    }

    public int getCrv() {
        return crv;
    }

}
