/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.utilities;

import com.strongkey.skce.utilities.skceMaps;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;


public class SKFSCron {
/**
     * This class's name - used for logging
     */
    private final String classname = this.getClass().getName();

    /**
     * Scheduler service object that is capable of running jobs after a certain
     * delay and/or in periodic intervals with a specified time difference
     */
    public final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);


    /*
     ******************************************************************************
     * .d888 888                   888      888     888                            .d8888b.                             d8b
     *d88P"  888                   888      888     888                           d88P  Y88b                            Y8P
     *888    888                   888      888     888                           Y88b.
     *888888 888 888  888 .d8888b  88888b.  888     888 .d8888b   .d88b.  888d888  "Y888b.    .d88b.  .d8888b  .d8888b  888  .d88b.
     *888    888 888  888 88K      888 "88b 888     888 88K      d8P  Y8b 888P"       "Y88b. d8P  Y8b 88K      88K      888 d88""88b
     *888    888 888  888 "Y8888b. 888  888 888     888 "Y8888b. 88888888 888           "888 88888888 "Y8888b. "Y8888b. 888 888  888
     *888    888 Y88b 888      X88 888  888 Y88b. .d88P      X88 Y8b.     888     Y88b  d88P Y8b.          X88      X88 888 Y88..88P
     *888    888  "Y88888  88888P' 888  888  "Y88888P"   88888P'  "Y8888  888      "Y8888P"   "Y8888   88888P'  88888P' 888  "Y88P"
     *
     ******************************************************************************
     */
    public void flushUserSessionsJob() {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "flushUserSessionsJob");

        final Runnable flushUserSessionsJob = new Runnable() {
            @Override
            public void run() {
                //Common.cleanSessionMapInfo();
                skceMaps.getMapObj().clean(SKFSConstants.MAP_USER_SESSION_INFO);
            }
        };

        /**
         * Pick up site configured frequency - but cannot be more frequent than
         * 5 seconds between runs; otherwise we risk bogging down the machine
         * with cleanup activity
         *
         * At the same time, we don't want too long between each run which might
         * bloat up memory used
         */
        long runfrequency;
        try {
            runfrequency = Long.parseLong(SKFSCommon.getConfigurationProperty("skfs.cfg.property.usersession.flush.frequency.seconds"));
            if (runfrequency < 5L) {
                runfrequency = 5L;
            } else if (runfrequency > 300L) {
                runfrequency = 5L;
            }
        } catch (NumberFormatException ex) {
            runfrequency = 5L;  //  by default - in case of any error
        }

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-MSG-0044", runfrequency);
        scheduler.scheduleAtFixedRate(flushUserSessionsJob, 0, runfrequency, TimeUnit.SECONDS);

        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "flushUserSessionsJob");
    }

    //flush fidokeys
    public void flushFIDOKeysJob() {
        SKFSLogger.entering(SKFSConstants.SKFE_LOGGER,classname, "flushFIDOKeysJob");

        final Runnable flushUserSessionsJob = new Runnable() {
            @Override
            public void run() {
                //Common.cleanSessionMapInfo();
                skceMaps.getMapObj().clean(SKFSConstants.MAP_FIDO_KEYS);
            }
        };

        /**
         * Pick up site configured frequency - but cannot be more frequent than
         * 5 seconds between runs; otherwise we risk bogging down the machine
         * with cleanup activity
         *
         * At the same time, we don't want too long between each run which might
         * bloat up memory used
         */
        long runfrequency;
        try {
            runfrequency = Long.parseLong(SKFSCommon.getConfigurationProperty("skfs.cfg.property.fidokeys.flush.frequency.seconds"));
            if (runfrequency < 5L) {
                runfrequency = 5L;
            } else if (runfrequency > 300L) {
                runfrequency = 5L;
            }
        } catch (NumberFormatException ex) {
            runfrequency = 5L;  //  by default - in case of any error
        }

        SKFSLogger.log(SKFSConstants.SKFE_LOGGER,Level.FINE, "FIDO-MSG-0044", runfrequency);
        scheduler.scheduleAtFixedRate(flushUserSessionsJob, 0, runfrequency, TimeUnit.SECONDS);

        SKFSLogger.exiting(SKFSConstants.SKFE_LOGGER,classname, "flushFIDOKeysJob");
    }
}
