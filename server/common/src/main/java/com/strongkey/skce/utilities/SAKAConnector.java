/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.utilities;

import com.strongkey.saka.web.Encryption;

public class SAKAConnector implements Runnable {
    private static volatile SAKAConnector sakaconn = null;

    protected SAKAConnector()
    {
        createPorts();
    }

    public static SAKAConnector getSAKAConn() {

        if (sakaconn == null) {
            synchronized (SAKAConnector.class) {
                if (sakaconn == null) {
                    sakaconn = new SAKAConnector();
                }
            }
        }
        return sakaconn;
    }

    /**
     * Creates and stores SAKA port objects that are used for future when
     * connection needs to be made to SAKA.
     */
    private static void createPorts() {

    }

    /**
     * Fetches a SAKA port (SOAP binding object needed to make a web-service request)
     * for the specified host url which is a part of a SAKA cluster specified by
     * clusterid.
     * @param clusterid saka cluster id
     * @param hosturl   saka host url for which the port object is request for.
     * @return The Encryption class object, using which web-service calls can
     *          be made.
     */
    public Encryption getSAKAPort(int clusterid, String hosturl) {
        return null;
    }

    /**
     * Overridden toString method used for debugging.
     * This method actually prints out all SAKA clusters that are read from the
     * properties file and all SOAP port objects related to each saka cluster.
     *
     * @return String containing the local map information that contains port
     *          objects.
     */
    @Override
    public String toString() {
        return "";
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
