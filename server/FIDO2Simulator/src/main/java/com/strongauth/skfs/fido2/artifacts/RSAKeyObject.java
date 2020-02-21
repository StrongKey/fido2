/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * A class representing a Rivest-Shamir-Adelman (RSA) key object
 */

package com.strongauth.skfs.fido2.artifacts;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import java.io.IOException;
import java.util.Map;

public class RSAKeyObject extends FIDO2KeyObject
{
    private byte[] n, e;

    public void decode(byte[] cbor) throws IOException
    {
        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);
        CBORParser parser = f.createParser(cbor);

        Map<String, Object> pkobjectsmap = mapper.readValue(parser, new TypeReference<Map<String, Object>>() {
        });

        for (String key : pkobjectsmap.keySet()) {
//            System.out.println("key : " + key + ", Value : " + pkobjectsmap.get(key).toString());
            switch (key) {
                case "1":
                    kty = (int) pkobjectsmap.get(key);
                    break;
                case "3":
                    alg = (int) pkobjectsmap.get(key);
                    break;
                case "-1":
                    n = (byte[]) pkobjectsmap.get(key);
                    break;
                case "-2":
                    e = (byte[]) pkobjectsmap.get(key);
                    break;
            }
        }
    }

    public byte[] getN() {
        return n;
    }

    public byte[] getE() {
        return e;
    }
}
