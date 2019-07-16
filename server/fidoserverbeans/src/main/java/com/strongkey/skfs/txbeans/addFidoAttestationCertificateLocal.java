/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.entitybeans.AttestationCertificatesPK;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import javax.ejb.Local;

@Local
public interface addFidoAttestationCertificateLocal {
    public AttestationCertificatesPK execute(Long did, X509Certificate attCert, 
            AttestationCertificatesPK parentPK) throws CertificateEncodingException, SKFEException;
}
