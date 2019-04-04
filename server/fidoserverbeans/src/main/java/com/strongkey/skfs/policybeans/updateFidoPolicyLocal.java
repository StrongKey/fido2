/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.utilities.SKFEException;
import java.util.Date;
import javax.ejb.Local;

/**
 *
 * @author mishimoto
 */
@Local
public interface updateFidoPolicyLocal {
    public void excecute(Long did,
                        Long pid,
                        Date startDate, 
                        Date endDate,
                        String Policy, 
                        Integer version, 
                        String status, 
                        String notes) throws SKFEException;
}
