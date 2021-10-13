/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.policybeans;

import com.strongkey.skce.pojos.MDSClient;
import com.strongkey.skce.utilities.skceMaps;
import com.strongkey.skfs.entitybeans.FidoPolicies;
import com.strongkey.skfs.entitybeans.FidoPoliciesPK;
import com.strongkey.skfs.fido.policyobjects.FidoPolicyObject;
import com.strongkey.skfs.pojos.FidoPolicyMDSObject;
import com.strongkey.skfs.utilities.SKFEException;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Collection;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.ejb.Startup;

@Singleton
@Startup
public class cachePolicies {

    @EJB
    getFidoPolicyLocal getFidoPolicies;

    @PostConstruct
    public void initialize() {
        Collection<FidoPolicies> fpCol = getFidoPolicies.getAllActive();
        for(FidoPolicies fp: fpCol){
            FidoPoliciesPK fpPK = fp.getFidoPoliciesPK();
            try{
                FidoPolicyObject fidoPolicyObject = FidoPolicyObject.parse(
                        fp.getPolicy(),
                        (long) fpPK.getDid(),
                        (long) fpPK.getSid(),
                        (long) fpPK.getPid());

                MDSClient mds = null;

                String mapkey = fpPK.getSid() + "-" + fpPK.getDid() + "-" + fpPK.getPid();
                skceMaps.getMapObj().put(SKFSConstants.MAP_FIDO_POLICIES, mapkey, new FidoPolicyMDSObject(fidoPolicyObject, mds));
            }
            catch(SKFEException ex){
                SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.SEVERE, "SKCE-ERR-1000", "Unable to cache policy: " + ex);
            }
        }

    }
}
