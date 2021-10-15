/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido2;

import com.strongkey.cbor.jacob.CborDecoder;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.bouncycastle.util.encoders.Base64;
import org.bouncycastle.util.encoders.Hex;

public class FIDO2AttestedCredentialData {

    private byte[] aaguid;

    private byte[] credentialId;

    private int length;

    private FIDO2KeyObject fko;

    private PublicKey publicKey;

    public byte[] getAaguid() {
        return aaguid;
    }

    public byte[] getCredentialId() {
        return credentialId;
    }

    public int getLength() {
        return length;
    }

    public FIDO2KeyObject getFko() {
        return fko;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    //Returns size of FIDO2AttestedCredentialData
    public int decodeAttCredData(byte[] data) throws IOException, NoSuchAlgorithmException, NoSuchProviderException, InvalidKeySpecException, InvalidParameterSpecException {
        int remainingDataIndex = 0;

        aaguid = new byte[16];
        credentialId = new byte[]{};
        System.arraycopy(data, 0, aaguid, 0, 16);
        remainingDataIndex += 16;

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "AAGUID : " + Base64.toBase64String(aaguid));
        
        //####4.3.2#### Check aaguid fits within rp policy

        byte[] lengthValue = new byte[2];
        System.arraycopy(data, remainingDataIndex, lengthValue, 0, 2);
        remainingDataIndex += 2;
        length = ByteBuffer.wrap(lengthValue).getShort();

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "length : " + length);

        credentialId = new byte[length];
        System.arraycopy(data, remainingDataIndex, credentialId, 0, length);
        remainingDataIndex += length;

        byte[] cbor = new byte[data.length - remainingDataIndex];
        System.arraycopy(data, remainingDataIndex, cbor, 0, data.length - remainingDataIndex);

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "cbor (hex): \n" + Hex.toHexString(cbor));
//        CBORFactory f = new CBORFactory();
//        ObjectMapper mapper = new ObjectMapper(f);
        long kty = 0;
//        CBORParser parser = f.createParser(cbor);
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
        
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "pkObjectMap: ");
        for (Map.Entry<Object,Object> entry : pkObjectMap.entrySet()) {
            Object key = entry.getKey();
            if ((long) key == 1) {
                kty = (long) pkObjectMap.get(key);
            }
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Key: " + key + ", Object: " + pkObjectMap.get(key).toString());
        }
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                "KTY = " + kty);
        
        int cborLength = 0;
        
        if (kty == 2) {
            ECKeyObject eck = new ECKeyObject();
            eck.decode(cbor);
            
            cborLength = eck.getEncodedLength();
            
            long crv = eck.getCrv();
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "crv = " + crv);
            String curveString = SKFSCommon.getCurveFromFIDOECCCurveID(crv);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "curveString = " + curveString);
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "X = " + org.bouncycastle.util.encoders.Hex.toHexString(eck.getX()));
            SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Y = " + org.bouncycastle.util.encoders.Hex.toHexString(eck.getY()));
            publicKey = cryptoCommon.getUserECPublicKey(eck.getX(), eck.getY(), curveString);

            fko = eck;
        } else {
            RSAKeyObject rko = new RSAKeyObject();
            rko.decode(cbor);

            cborLength = rko.getEncodedLength();

            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1,rko.getN()), new BigInteger(1,rko.getE()));
            publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);

            fko = rko;
        }

        //Return size of AttestedCredentialData
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "FIDO2AttestedCredentialData size (bytes: " + cborLength);
        return remainingDataIndex + cborLength;
    }
    
    public static long bytesToLong(byte[] bytes) {
        ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);
        buffer.put(bytes);
        buffer.flip();
        return buffer.getLong();
    }

}
