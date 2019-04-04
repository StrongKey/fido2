/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.utilities.SKFEException;
import javax.ejb.Local;

/**
 *
 * @author mishimoto
 */
@Local
public interface deleteFidoPolicyLocal {
    public void execute(Long did, Long sid, Long pid) throws SKFEException;
}
