/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido2;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.cbor.CBORFactory;
import com.fasterxml.jackson.dataformat.cbor.CBORParser;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skfs.utilities.skfsCommon;
import com.strongkey.skfs.utilities.skfsConstants;
import com.strongkey.skfs.utilities.skfsLogger;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Map;
import java.util.logging.Level;
import org.bouncycastle.util.encoders.Base64;

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

        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "AAGUID : " + Base64.toBase64String(aaguid));
        
        byte[] lengthValue = new byte[2];
        System.arraycopy(data, remainingDataIndex, lengthValue, 0, 2);
        remainingDataIndex += 2;
        length = ByteBuffer.wrap(lengthValue).getShort();

        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "length : " + length);
        
        credentialId = new byte[length];
        System.arraycopy(data, remainingDataIndex, credentialId, 0, length);
        remainingDataIndex += length;

        byte[] cbor = new byte[data.length - remainingDataIndex];
        System.arraycopy(data, remainingDataIndex, cbor, 0, data.length - remainingDataIndex);

        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "cbor (hex): \n" + bytesToHexString(cbor, cbor.length));
        CBORFactory f = new CBORFactory();
        ObjectMapper mapper = new ObjectMapper(f);
        int kty = 0;
        CBORParser parser = f.createParser(cbor);
        Map<String, Object> pkObjectMap = mapper.readValue(parser, new TypeReference<Map<String, Object>>() {
        });
        
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "pkObjectMap: ");
        for(String key: pkObjectMap.keySet()){
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001",
                    "Key: " + key + ", Object: " + pkObjectMap.get(key).toString());
        }
        kty = (int) pkObjectMap.get("1");
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "KTY = " + kty);
        if (kty == 2) {
            ECKeyObject eck = new ECKeyObject();
            eck.decode(cbor);
            
            int crv = eck.getCrv();
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "crv = " + crv);
            String curveString = skfsCommon.getCurveFromFIDOECCCurveID(crv);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "curveString = " + curveString);
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "X = " + org.bouncycastle.util.encoders.Hex.toHexString(eck.getX()));
            skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "Y = " + org.bouncycastle.util.encoders.Hex.toHexString(eck.getY()));
            publicKey = cryptoCommon.getUserECPublicKey(eck.getX(), eck.getY(), curveString);

            fko = eck;
        } else {
            RSAKeyObject rko = new RSAKeyObject();
            rko.decode(cbor);
            
            
            RSAPublicKeySpec spec = new RSAPublicKeySpec(new BigInteger(1,rko.getN()), new BigInteger(1,rko.getE()));
            publicKey = KeyFactory.getInstance("RSA").generatePublic(spec);
            
            fko = rko;
        }
        
        //Return size of AttestedCredentialData
        skfsLogger.log(skfsConstants.SKFE_LOGGER, Level.FINE, "FIDO-MSG-2001", 
                    "FIDO2AttestedCredentialData size (bytes: " + parser.getCurrentLocation().getByteOffset());
        return remainingDataIndex + (int) parser.getCurrentLocation().getByteOffset();
    }
    
    private static String bytesToHexString(byte[] rawBytes, int num) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < num; i++) {
            if (i % 16 == 0) {
                sb.append('\n');
            }
            sb.append(String.format("%02x ", rawBytes[i]));
        }
        return sb.toString();
    }
}
