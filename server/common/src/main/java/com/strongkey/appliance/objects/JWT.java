/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.objects;

import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.crypto.utility.cryptoCommon;
import java.io.IOException;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import javax.crypto.SecretKey;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.bouncycastle.asn1.ASN1EncodableVector;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Sequence;
import org.bouncycastle.asn1.DERSequence;


public class JWT {

    private final String header;
    private final String body;
    private final byte[] signature;

    public JWT(String input) {
        System.out.println("JWT: " + input);
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String[] jwtParts = input.split("\\.");
        this.header = jwtParts[0];
        this.body = jwtParts[1];
        this.signature = decoder.decode(jwtParts[2]);
    }

    public JsonObject getHeader() throws UnsupportedEncodingException {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return stringToJson(decoder.decode(header));
    }

    public JsonObject getBody() throws UnsupportedEncodingException {
        Base64.Decoder decoder = Base64.getUrlDecoder();
        return stringToJson(decoder.decode(body));
    }

    public boolean verifySignature(Key key) {
        try{
            String alg = getHeader().getString("alg", null);
            if (alg == null) {
                throw new NoSuchAlgorithmException("JWT algorithm not found");
            }

            byte[] signedBytes = (header + "." + body).getBytes();
            System.out.println(alg);
            switch(alg){
                case "HS256":
                    return (signature != null) && Arrays.equals(signature, cryptoCommon.calculateHmac((SecretKey) key, signedBytes, "HmacSHA256"));
                case "RS256":
                    return cryptoCommon.verifySignature(signature, (PublicKey) key, signedBytes, "SHA256withRSA");
                case "ES256":
                    //Convert JOSE signature to ASN1 signature
                    ASN1EncodableVector vector = new ASN1EncodableVector();
                    ASN1Integer r = new ASN1Integer(new BigInteger(1, org.bouncycastle.util.Arrays.copyOfRange(signature, 0, 32)));
                    ASN1Integer s = new ASN1Integer(new BigInteger(1, org.bouncycastle.util.Arrays.copyOfRange(signature, 32, 64)));
                    vector.add(r);
                    vector.add(s);
                    ASN1Sequence dr = new DERSequence(vector);
                    System.out.println(dr.toString());
                    byte[] ASN1SignatureBytes = dr.getEncoded();
                    return cryptoCommon.verifySignature(ASN1SignatureBytes, (PublicKey) key, signedBytes, "SHA256withECDSA");
                default:
                    throw new NoSuchAlgorithmException("JWT algorithm not supported");
            }
        } catch (NoSuchAlgorithmException | IOException ex) {
            strongkeyLogger.logp("APPL", Level.SEVERE, "JWT", "verifySignature", "APPL-ERR-1000", ex.getLocalizedMessage());
            return false;
        }
    }


    private static JsonObject stringToJson(byte[] input) throws UnsupportedEncodingException {
        String inputString = new String(input, "UTF-8");
        StringReader stringreader = new StringReader(inputString);
        JsonReader jsonreader = Json.createReader(stringreader);
        return jsonreader.readObject();
    }
}
