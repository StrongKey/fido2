/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */
package com.strongkey.skfs.txbeans;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.util.Date;
import java.util.logging.Level;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.ws.rs.core.Response;

@Stateless
public class pingBean implements pingBeanLocal {

    @EJB
    u2fServletHelperBeanLocal u2fHelper;

    @Override
    public String execute(
            Long did) {

        Date in = new Date();
        Date out;
        long thId = Thread.currentThread().getId();
        String ID = thId + "-" + in.getTime();
        //  1. Receive request and print inputs
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0060", "[TXID=" + ID + "]"
                + "\n did=" + did);

        StringBuilder sbuf = new StringBuilder(1024);
        sbuf.append("StrongKey, Inc. FIDO Server ").append(applianceCommon.getVersion()).append('\n');
        sbuf.append("Hostname: ").append(applianceCommon.getHostname()).append(" (ServerID: ").append(applianceCommon.getServerId()).append(')').append('\n');
        sbuf.append("Current time: ").append(new Date()).append('\n');
        sbuf.append("Up since: ").append(new Date(applianceCommon.getBootTime())).append('\n');

        PreregistrationRequest pregreq = new PreregistrationRequest();

        pregreq.setUsername("pinguser" + new Date().getTime());

        pregreq.setDisplayName("pinguserkey");
        pregreq.setOptions(Json.createObjectBuilder().add("attestation","direct").build());
        pregreq.getSVCInfo().setProtocol("FIDO2_0");

        Response res = u2fHelper.preregister(did, pregreq);
        if (res.getStatus() == 200) {
            sbuf.append("FIDO Server Domain ").append(did).append(" is alive!\n");
        } else {
            return "FIDO server is unavailable";
        }

        String resp = sbuf.toString();
        out = new Date();
        long rt = out.getTime() - in.getTime();
        SKFSLogger.log(SKFSConstants.SKFE_LOGGER, Level.INFO, "FIDO-MSG-0061", "[TXID=" + ID + ", START=" + in.getTime() + ", FINISH=" + out.getTime() + ", TTC=" + rt + "]"
                + "\nPing response = " + resp);
        return resp;
    }

}
