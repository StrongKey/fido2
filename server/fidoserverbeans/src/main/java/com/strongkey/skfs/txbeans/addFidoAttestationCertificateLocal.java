/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
