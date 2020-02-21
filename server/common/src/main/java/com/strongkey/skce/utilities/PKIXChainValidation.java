/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.skce.utilities;

import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorException;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.PKIXParameters;
import java.security.cert.PKIXRevocationChecker;
import java.security.cert.TrustAnchor;
import java.util.EnumSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PKIXChainValidation {

    final static String USE_OCSP = "false";
    private static final String classname = "PKIXChainValidation";

    // Enable choice of JCA/JCE Security Providers - Sun/SunJCE, BC or BCFIPS
    static {
        // Enable/Disable OCSP - OCSP/CRL revocation checking are on by default
        java.security.Security.setProperty("ocsp.enable", USE_OCSP);
        System.out.println("OCSP is enabled:" + USE_OCSP);
    }

    public static boolean pkixvalidate(CertPath cp, Set<TrustAnchor> trustAnchorSet,
            boolean isRevocationChecked, boolean isPolicyQualifiersRejected) {
        try {
            CertPathValidator cpv = CertPathValidator.getInstance("PKIX");  //TODO use BCFIPS when "Support for PKIXRevocationChecker
                                                                            //in the CertPath implementation" is added

            PKIXParameters pkix = new PKIXParameters(trustAnchorSet);

            if(isRevocationChecked){
                PKIXRevocationChecker prc = (PKIXRevocationChecker) cpv.getRevocationChecker();
                prc.setOptions(EnumSet.of(PKIXRevocationChecker.Option.PREFER_CRLS, PKIXRevocationChecker.Option.NO_FALLBACK));
                pkix.addCertPathChecker(prc);
            }
            else{
                pkix.setRevocationEnabled(false);
            }

            pkix.setPolicyQualifiersRejected(isPolicyQualifiersRejected);
            pkix.setDate(null);
            CertPathValidatorResult cpvr = cpv.validate(cp, pkix);
            if (cpvr != null) {
                System.out.println("Certificate validated");
                return true;
            } else {
                System.out.println("Certificate not valid");
                return false;
            }
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | CertPathValidatorException ex) {
            Logger.getLogger(PKIXChainValidation.class.getName()).log(Level.SEVERE, null, ex);
            return false;
        }
    }
}
