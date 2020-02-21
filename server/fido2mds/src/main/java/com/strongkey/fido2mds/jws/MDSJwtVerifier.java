/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.fido2mds.jws;

import com.strongkey.appliance.objects.JWT;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.skce.utilities.PKIXChainValidation;
import java.io.UnsupportedEncodingException;
import java.security.NoSuchProviderException;
import java.security.cert.CertPath;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.json.JsonArray;

public class MDSJwtVerifier {
    private final X509Certificate rootCert;

    public MDSJwtVerifier(X509Certificate rootCert){
        this.rootCert = rootCert;
    }

    public void verify(JWT jwt) throws CertificateException, NoSuchProviderException, UnsupportedEncodingException {
        Set<TrustAnchor> trustAnchor = new HashSet<>();
        trustAnchor.add(new TrustAnchor(rootCert, null));

        List<Certificate> certchain = getCertificatesFromJsonArray(jwt.getHeader().getJsonArray("x5c"));
        if(certchain == null){
            throw new IllegalArgumentException("MDS JWT returned null certificate chain");
        }

        CertPath certPath = CertificateFactory.getInstance("X.509", "BCFIPS").generateCertPath(certchain);

        if (certchain.isEmpty()) {
            throw new IllegalArgumentException("MDS JWT certificate chain missing");
        }

        if (!PKIXChainValidation.pkixvalidate(certPath, trustAnchor, true, true)) {
            throw new IllegalArgumentException("MDS JWT certificate could not be validated");
        }

        System.out.println("Certificate checked:" + certchain.get(0).toString());
        if (!jwt.verifySignature(certchain.get(0).getPublicKey())) {
            throw new IllegalArgumentException("MDS JWT signature cannot be verified");
        }
    }

    private List<Certificate> getCertificatesFromJsonArray(JsonArray x5c) throws CertificateException, NoSuchProviderException{
        List<Certificate> result = new ArrayList<>();
        if(x5c == null){
            return result;
        }
        Decoder decoder = Base64.getDecoder();
        for(int i = 0; i < x5c.size(); i++){
            byte[] certBytes = decoder.decode(x5c.getString(i));
            result.add(cryptoCommon.generateX509FromBytes(certBytes));
        }
        return result;
    }
}
