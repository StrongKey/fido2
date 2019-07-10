/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */
package com.strongkey.skfs.txbeans;

import com.strongkey.skfs.pojos.FIDOUserMetadata;
import com.strongkey.skfs.utilities.SKFEException;
import javax.ejb.Local;

@Local
public interface getFIDOUserinfoBeanLocal {

    public FIDOUserMetadata execute(Long did, String username) throws SKFEException;
}
