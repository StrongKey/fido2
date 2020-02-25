/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.entitybeans.AttestationCertificates;
import javax.ejb.Local;

@Local
public interface getFidoAttestationCertificateLocal {

    public AttestationCertificates getByPK(Long did, Long sid, Long attcid);
    public AttestationCertificates getByIssuerDnSerialNumber(String issuerDn, String serialNumber);
}
