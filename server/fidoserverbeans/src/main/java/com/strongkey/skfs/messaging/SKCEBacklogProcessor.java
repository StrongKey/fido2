/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.messaging;

import com.strongkey.appliance.utilities.applianceCommon;
import com.strongkey.appliance.utilities.applianceConstants;
import com.strongkey.appliance.utilities.strongkeyLogger;
import com.strongkey.replication.messaging.ZMQBacklogProcessor;
import com.strongkey.skce.utilities.skceCommon;
import com.strongkey.skce.utilities.skceConstants;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.SortedMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.annotation.PreDestroy;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.persistence.NoResultException;
import javax.sql.DataSource;

public class SKCEBacklogProcessor implements BacklogProcessor
{
    /**
     ** This class's name - used for logging
     **/
    private final String classname = this.getClass().getName();

    // ZMQ-related objects
    static  int zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
    static  SKCEBacklogProcessor zmqblproc = null;
    private Thread processor;

    // Number of subscribers in cluster and local SID
    private int ssize           = 0;
    private Long localsid       = applianceCommon.getServerId();
    private String localhost    = applianceCommon.getHostname();

    final Integer TIMEDIFF      = Integer.parseInt(skceCommon.getConfigurationProperty("skce.cfg.property.messaging.timediff"));
    final Integer BLPSLEEPTIME  = Integer.parseInt(skceCommon.getConfigurationProperty("skce.cfg.property.messaging.blpsleeptime"));
    private static final SortedMap<Long, BacklogProcessorHelper> blphelpers = new ConcurrentSkipListMap<>();

     // JDBC objects
    private Context    ctx;
    private DataSource ds;
    private Connection ctorconn;

    private String JNDINAME     = skceCommon.getConfigurationProperty("skce.cfg.property.jdbc.jndiname");

/**********************************************************************************************
 .d8888b.                             888                              888
d88P  Y88b                            888                              888
888    888                            888                              888
888         .d88b.  88888b.  .d8888b  888888 888d888 888  888  .d8888b 888888  .d88b.  888d888
888        d88""88b 888 "88b 88K      888    888P"   888  888 d88P"    888    d88""88b 888P"
888    888 888  888 888  888 "Y8888b. 888    888     888  888 888      888    888  888 888
Y88b  d88P Y88..88P 888  888      X88 Y88b.  888     Y88b 888 Y88b.    Y88b.  Y88..88P 888
 "Y8888P"   "Y88P"  888  888  88888P'  "Y888 888      "Y88888  "Y8888P  "Y888  "Y88P"  888
**********************************************************************************************/
    // Constructor
    @SuppressWarnings("CallToThreadStartDuringObjectConstruction")
    protected SKCEBacklogProcessor()
    {
        if (zmqBacklogProcessorState == skceConstants.ZMQ_SERVICE_STARTING || zmqBacklogProcessorState == skceConstants.ZMQ_SERVICE_STOPPING) {
            recoverstate();
        }
        // Set state
        zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STARTING;
        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6130", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));

        try {
             // Verify that localsid is not zero
            if (localsid.equals(0L)) {
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.SEVERE, classname, "constructor", "SKCE-ERR-6099", localhost + " [" + localsid + "]");
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                return;
            }

            // Verify that localhost is not null
            if (localhost == null) {
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.SEVERE, classname, "constructor", "SKCE-ERR-6099", localhost + " [" + localsid + "]");
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                return;
            }

            // Establish JDBC connection
            ctx = (Context) new InitialContext().lookup("");
            ds  = (DataSource) ctx.lookup(JNDINAME);
            ctorconn = ds.getConnection();

            // See if there is more than one Server in the cluster to replicate to
            PreparedStatement localserver = ctorconn.prepareStatement(
                    "SELECT STATUS, REPLICATION_STATUS, REPLICATION_ROLE FROM SERVERS WHERE SID = ?");
            localserver.setLong(1, localsid);
            ResultSet lsrs = localserver.executeQuery();
            lsrs.next();

            localserver.close();
            // Check status
            String status = lsrs.getString("status");
            if (!status.equalsIgnoreCase("Active")) {
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "constructor", "SKCE-ERR-6094", localhost);
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                return;
            }

            // Check replication status
            String replstatus = lsrs.getString("replication_status");
            if (!replstatus.equalsIgnoreCase("Active")) {
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-ERR-6095", localhost);
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                return;
            }

            // Check replication role
            String replrole = lsrs.getString("replication_role");
            if (!replrole.equalsIgnoreCase("Publisher")) {
                if (!replrole.equalsIgnoreCase("Both")) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-ERR-6096", localhost);
                    zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                    return;
                }
            }

            lsrs.close();
            // See if there is more than one Server in the cluster to replicate to
            PreparedStatement servercount = ctorconn.prepareStatement(
                    "SELECT COUNT(*) FROM SERVERS");
            ResultSet srs = servercount.executeQuery();
            srs.next();
            // Get count
            ssize = srs.getInt("count(*)");
            if (ssize == 1) {
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6050", localhost);
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                servercount.close();
                return;
            }
            srs.close();
            
            servercount.close();
            // Start the thread
            processor = new Thread(this);
            processor.start();
            if (ctorconn != null)
                ctorconn.close();

            zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_RUNNING;
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6132", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));

         
        } catch (NamingException ex) {
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "constructor", "SKCE-ERR-6000", "Could not configure JDBC");
            Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "constructor", "SKCE-ERR-6000", "Could not configure JDBC");
            Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
            // Prevent connection leak in case we throw a SQL Exception
            
            try {
                if (ctorconn != null)
                    ctorconn.close();
            } catch (SQLException e) {}
        } catch (NoResultException ex) {
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "constructor", "SKCE-MSG-6041", null);
        }
    }

/**********************************************************************************************
                  888    8888888                   888
                  888      888                     888
                  888      888                     888
 .d88b.   .d88b.  888888   888   88888b.  .d8888b  888888  8888b.  88888b.   .d8888b  .d88b.
d88P"88b d8P  Y8b 888      888   888 "88b 88K      888        "88b 888 "88b d88P"    d8P  Y8b
888  888 88888888 888      888   888  888 "Y8888b. 888    .d888888 888  888 888      88888888
Y88b 888 Y8b.     Y88b.    888   888  888      X88 Y88b.  888  888 888  888 Y88b.    Y8b.
 "Y88888  "Y8888   "Y888 8888888 888  888  88888P'  "Y888 "Y888888 888  888  "Y8888P  "Y8888
     888
Y8b d88P
 "Y88P"
**********************************************************************************************/
    /**
     * Static method to return a running instance - or create it and return a reference
     * @return
     */
    public static BacklogProcessor getInstance()
    {
        // If we are not replicating - as in a single-machine DEVP appliance - return null
        if (!applianceCommon.replicate()) {
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, "ZMQBacklogProcessor", "getInstance", "SKCE-MSG-6132", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
            return null;
        }

        if (zmqBacklogProcessorState == skceConstants.ZMQ_SERVICE_RUNNING) {
            return zmqblproc;
        }

        try {
            if (applianceCommon.getLock("BLP")) {
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, "ZMQBacklogProcessor", "getInstance", "SKCE-MSG-6130", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                switch (zmqBacklogProcessorState) {
                    case skceConstants.ZMQ_SERVICE_RUNNING: return zmqblproc;
                    case skceConstants.ZMQ_SERVICE_STOPPED: return zmqblproc = new SKCEBacklogProcessor();
                    case skceConstants.ZMQ_SERVICE_STARTING: return zmqblproc = new SKCEBacklogProcessor();
                    case skceConstants.ZMQ_SERVICE_STOPPING: return zmqblproc = new SKCEBacklogProcessor();
                    default: return null;
                }
            }
        } finally {
            try {
                applianceCommon.releaseLock("BLP");
            } catch (IllegalMonitorStateException ex) {}
        }
        return null;
    }

/**************************************************************************
         888               888         888
         888               888         888
         888               888         888
.d8888b  88888b.  888  888 888888  .d88888  .d88b.  888  888  888 88888b.
88K      888 "88b 888  888 888    d88" 888 d88""88b 888  888  888 888 "88b
"Y8888b. 888  888 888  888 888    888  888 888  888 888  888  888 888  888
     X88 888  888 Y88b 888 Y88b.  Y88b 888 Y88..88P Y88b 888 d88P 888  888
 88888P' 888  888  "Y88888  "Y888  "Y88888  "Y88P"   "Y8888888P"  888  888
***************************************************************************/
    // Method to close sockets and terminate the context
    @PreDestroy
    @Override
    public void shutdown()
    {
        switch (zmqBacklogProcessorState) {
            case skceConstants.ZMQ_SERVICE_RUNNING:
                stopservice();
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6042", null);
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6132", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                break;
            case skceConstants.ZMQ_SERVICE_STOPPED:
                break;
            case skceConstants.ZMQ_SERVICE_STARTING:
                recoverstate();
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6042", null);
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6132", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                break;
            case skceConstants.ZMQ_SERVICE_STOPPING:
                recoverstate();
                zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPED;
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6042", null);
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6132", "[ZMQBacklogProcessor): " + applianceCommon.getZMQState(zmqBacklogProcessorState));
                break;
            default:
        }
    }

    // Private method called by shutdown()
    private boolean stopservice()
    {
        // Indicate our intent
        zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPING;
        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));

        // Stop BLP Thread
        processor.interrupt();
        try {
            processor.join(5000);
            if(processor.isAlive()) {
               // System.out.println("Failed to stop Backlog Processor thread.");
            }
            processor = null;
        } catch (InterruptedException ex) {}

        // Close JDBC connection
        if (ctorconn != null) {
            try {
                if (!ctorconn.isClosed())
                    ctorconn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    ctorconn.close();
                } catch (SQLException se) {}
            }
        }

        // Finally clear SKCEBacklogProcessor object
        zmqblproc = null;
        return true;
    }

    // Private method called by shutdown()
    private boolean recoverstate()
    {
        // Indicate our intent
        zmqBacklogProcessorState = skceConstants.ZMQ_SERVICE_STOPPING;
        strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "shutdown", "SKCE-MSG-6131", "[ZMQBacklogProcessor]: " + applianceCommon.getZMQState(zmqBacklogProcessorState));

        // Stop BLP Thread
        if (processor != null) {
            processor.interrupt();
            try {
                processor.join(5000);
                if(processor.isAlive()) {
                    //System.out.println("Failed to stop Backlog Processor thread.");
                }
                processor = null;
            } catch (InterruptedException ex) {}
        }

        // Close JDBC connection
        if (ctorconn != null) {
            try {
                if (!ctorconn.isClosed())
                    ctorconn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
            } finally {
                try {
                    ctorconn.close();
                } catch (SQLException se) {}
            }
        }

        // Finally clear SKCEBacklogProcessor object
        zmqblproc = null;
        return true;
    }

    /**************************************************************************
                                   d8b
                                   Y8P

888d888 888  888 88888b.  88888b.  888 88888b.   .d88b.
888P"   888  888 888 "88b 888 "88b 888 888 "88b d88P"88b
888     888  888 888  888 888  888 888 888  888 888  888
888     Y88b 888 888  888 888  888 888 888  888 Y88b 888
888      "Y88888 888  888 888  888 888 888  888  "Y88888
                                                     888
                                                Y8b d88P
                                                 "Y88P"
***************************************************************************/
    /**
     * Method to check if BacklogProcessor thread is running
     * @return
     */
    @Override
    public boolean running() {
        return zmqBacklogProcessorState == skceConstants.ZMQ_SERVICE_RUNNING && zmqblproc != null;
    }
/**************************************************************************
                          888                     888
                          888                     888
                          888                     888
888d888  .d88b.  .d8888b  888888  8888b.  888d888 888888
888P"   d8P  Y8b 88K      888        "88b 888P"   888
888     88888888 "Y8888b. 888    .d888888 888     888
888     Y8b.          X88 Y88b.  888  888 888     Y88b.
888      "Y8888   88888P'  "Y888 "Y888888 888      "Y888
***************************************************************************/
    /**
     * Method to restart BacklogProcessor thread
     * @return
     */
    @Override
    public String restart() {
        try {
            if (running() && applianceCommon.getLock("BLP")) {
                shutdown();
                zmqblproc = new SKCEBacklogProcessor();
                return "SKCE-MSG-6112";
            }
            return null;
        } finally {
            try {
                applianceCommon.releaseLock("BLP");
            } catch (IllegalMonitorStateException ex) {}
        }
    }

/*****************************************************
    888d888 888  888 88888b.
    888P"   888  888 888 "88b
    888     888  888 888  888
    888     Y88b 888 888  888
    888      "Y88888 888  888
******************************************************/
    @Override
    @SuppressWarnings("SleepWhileInLoop")
    public void run() {
        Connection conn = null;
        PreparedStatement countquery = null;
        try {

            try {
                // Get local Connection
                conn = ds.getConnection();
                // Setup query to count back-logged objects for Active servers only
                countquery = conn.prepareStatement(
                "SELECT R.TSID, COUNT(*) " +
                " FROM REPLICATION R, SERVERS S " +
                " WHERE TIMESTAMPDIFF(SECOND, R.SCHEDULED, NOW()) > ?" +
                " AND R.TSID = S.SID " +
                " AND S.STATUS = 'Active'" +
                " AND R.OBJECTYPE > ?" +
                " AND R.OBJECTYPE < ?" +
                " AND S.REPLICATION_STATUS = 'Active'" +
                " GROUP BY R.TSID");
                countquery.setInt(1, TIMEDIFF);
                countquery.setInt(2, applianceConstants.ENTITY_TYPE_SKCE_LOWER_LIMIT);
                countquery.setInt(3, applianceConstants.ENTITY_TYPE_SKCE_UPPER_LIMIT);

            } catch (SQLException ex) {
                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "constructor", "SKCE-ERR-6000", "Could not configure JDBC");
                Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
                return;
            }

            // Counter to display sleep message once in 10 sleeps
            int counter = 0;

            while (!Thread.currentThread().isInterrupted()) {
                try
                {
                    while (Boolean.TRUE)
                    {
                        if (Thread.currentThread().isInterrupted()) {
                            return;
                        }
                        Thread.sleep(BLPSLEEPTIME * 1000);

                        // JDBC objects
                        ResultSet cqrs;

                        // Get a count of leftover objects
                        int foundsize;
                        Long sid;

                        cqrs = countquery.executeQuery();
                        while(cqrs.next()) {
                            sid = cqrs.getLong(1);
                            foundsize = cqrs.getInt(2);
                            if (foundsize > 0) {
                                if (blphelpers.containsKey(sid)) {
                                    if (blphelpers.get(sid).isAlive()) {
                                        // blp helper is still working, let it continue
                                        counter = 0;
                                        continue;
                                    }
                                }
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "run", "SKCE-MSG-6043", "SID: " + sid + " Count: " + foundsize);
                                BacklogProcessorHelper backlogProcessorHelper = new BacklogProcessorHelper(sid);
                                backlogProcessorHelper.start();
                                blphelpers.put(sid, backlogProcessorHelper);
                                counter = 0;
                            } else {
                                if (counter == 9) {
                                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "run", "SKCE-MSG-6044", TIMEDIFF);
                                    counter = 0;
                                } else {
                                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6044", TIMEDIFF);
                                }
                            }
                        }
                        counter++;
                       
                    }
                } catch (SQLException ex) {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "constructor", "SKCE-ERR-6000", "Could not configure JDBC");
                    Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } catch (InterruptedException ex) {
        } finally {
            // Clean up helpers
            if (countquery != null) {
                try {
                    countquery.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
//            for (Long sid : blphelpers.keySet()) {
            for (Map.Entry<Long, BacklogProcessorHelper> entry : blphelpers.entrySet()) {
                Long sid = entry.getKey();
                if (blphelpers.get(sid).isAlive()) {
                    blphelpers.get(sid).interrupt();
                }
            }
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}

/*****************************************************
888888b.   888      8888888b.  888    888          888
888  "88b  888      888   Y88b 888    888          888
888  .88P  888      888    888 888    888          888
8888888K.  888      888   d88P 8888888888  .d88b.  888 88888b.   .d88b.  888d888
888  "Y88b 888      8888888P"  888    888 d8P  Y8b 888 888 "88b d8P  Y8b 888P"
888    888 888      888        888    888 88888888 888 888  888 88888888 888
888   d88P 888      888        888    888 Y8b.     888 888 d88P Y8b.     888
8888888P"  88888888 888        888    888  "Y8888  888 88888P"   "Y8888  888
                                                       888
                                                       888
                                                       888
******************************************************/

class BacklogProcessorHelper extends Thread
{
    private final String classname = this.getClass().getName();
    private final int DBFETCHSIZE     = Integer.parseInt(skceCommon.getConfigurationProperty("skce.cfg.property.jdbc.dbfetchsize"));
    private final int DBPROCESSSIZE   = Integer.parseInt(skceCommon.getConfigurationProperty("skce.cfg.property.jdbc.dbprocesssize"));
    final Integer TIMEDIFF      = Integer.parseInt(skceCommon.getConfigurationProperty("skce.cfg.property.messaging.timediff"));
    private String JNDINAME     = skceCommon.getConfigurationProperty("skce.cfg.property.jdbc.jndiname");
    private Context    ctx;
    private DataSource ds;
    private Long sid = null;

    // Constructor
    public BacklogProcessorHelper(Long sid) {
        try {
            ctx = (Context) new InitialContext().lookup("");
            ds  = (DataSource) ctx.lookup(JNDINAME);
        } catch (NamingException ex) {
            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "constructor", "SKCE-ERR-6000", "Could not configure JDBC");
            Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
        }
        this.sid = sid;
    }

    @Override
    @SuppressWarnings("SleepWhileHoldingLock")
    public void run()
    {
        /**
        * Bug ID:
        * BLP was accumulating heap memory because the JDBC driver was keeping track
        * of objects in its frequent SLQ queries even when they were not needed.  The
        * MySQL "dontTrackOpenResources" parameter when set to true released the memory.
        * But testing indicates that keeping the Connection inside the loop and coding
        * it for each iteration only adds a minute of processing in the whole year when
        * checking every minute.  Closing the Connection releases the memory and all its
        * objects from the heap.
        */
        Connection conn = null;

        try
        {
            // Get local Connection
            conn = ds.getConnection();

            // Setup query to count back-logged objects for Active servers only
            PreparedStatement countquery = conn.prepareStatement(
            "SELECT COUNT(*)" +
            " FROM REPLICATION R" +
            " WHERE TIMESTAMPDIFF(SECOND, R.SCHEDULED, NOW()) > ?" +
            " AND R.TSID = ?" +
            " AND R.OBJECTYPE > ?" +
            " AND R.OBJECTYPE < ?");
            countquery.setInt(1, TIMEDIFF);
            countquery.setLong(2, sid);
            countquery.setInt(3, applianceConstants.ENTITY_TYPE_SKCE_LOWER_LIMIT);
            countquery.setInt(4, applianceConstants.ENTITY_TYPE_SKCE_UPPER_LIMIT);

            // Setup query to get back-logged objects for Active servers only
            PreparedStatement dataquery = conn.prepareStatement(
            "SELECT SSID, RPID, TSID, OBJECTYPE, OBJECTOP, OBJECTPK, SCHEDULED " +
            " FROM REPLICATION R" +
            " WHERE TIMESTAMPDIFF(SECOND, R.SCHEDULED, NOW()) > ? " +
            " AND R.TSID = ?" +
            " AND R.RPID > ? " +
            " AND R.OBJECTYPE > ?" +
            " AND R.OBJECTYPE < ?" +
            " ORDER BY R.SSID, R.RPID, R.TSID " +
            " LIMIT ?",
            ResultSet.TYPE_FORWARD_ONLY);

            // JDBC objects
            PreparedStatement objectquery = null;
            ResultSet cqrs;
            ResultSet dqrs;

            // Local variables
            Long dbssid;
            Long dbrpid ;
            Long dbtsid;
            Integer dbobjtype;
            Integer dbobjop;
            String dbobjpk;
            Boolean invalidObject = Boolean.FALSE;

            // Get a count of leftover objects
            int foundsize;

            cqrs = countquery.executeQuery();
            cqrs.next();
            foundsize = cqrs.getInt(1);

            // Find the records to be replicated; need to process them in
            // batches to keep memory usage stable
            Long currentrecord = 0L;
            int iterations = ((foundsize / DBPROCESSSIZE) + 1);
            for (int iter = 0; iter < iterations; iter++)
            {
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }

                dataquery.setFetchSize(DBFETCHSIZE);
                dataquery.setInt(1, TIMEDIFF);
                dataquery.setLong(2, sid);
                dataquery.setLong(3, currentrecord);
                dataquery.setInt(4, applianceConstants.ENTITY_TYPE_SKCE_LOWER_LIMIT);
                dataquery.setInt(5, applianceConstants.ENTITY_TYPE_SKCE_UPPER_LIMIT);
                dataquery.setInt(6, DBPROCESSSIZE);

                // Get the records and process them individually
                dqrs = dataquery.executeQuery();
                while (dqrs.next()) {

                    if (Thread.currentThread().isInterrupted()) {
                        return;
                    }

                    // Local variables
                    Long objdid;
                    Long objsid;
                    byte[] objbytes = null;
                    String[] pkarray;
                    ResultSet objrs;

                    // Get record from result-set
                    dbssid = dqrs.getLong("ssid");
                    dbrpid = dqrs.getLong("rpid");
                    dbtsid = dqrs.getLong("tsid");
                    dbobjtype = dqrs.getInt("objectype");
                    dbobjop = dqrs.getInt("objectop");
                    dbobjpk = dqrs.getString("objectpk");

                    // Update currentrecord to remember last record processed
                    currentrecord = dbrpid;
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6045", dbssid + "-" + dbrpid + "-" + dbtsid);

                    // What object do we have? Create a Protocol Buffer message
                    switch (dbobjtype)
                    {
                        case applianceConstants.ENTITY_TYPE_FIDO_KEYS:
                            // Figure out primary key from keystring
                            pkarray = dbobjpk.split("-", 3);
                            objsid = Long.parseLong(pkarray[0]);
                            objdid = Long.parseLong(pkarray[1]);

                            // pkarray[2] should have both the uesrname and fkid with an indeterminate amount of hyphens in the username
                            int userfkidhyphen = pkarray[2].lastIndexOf("-");

                            String objfkuser = pkarray[2].substring(0, userfkidhyphen);
                            Long objfkid = Long.parseLong(pkarray[2].substring(userfkidhyphen + 1));

                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6046", "[SID-DID-FIDOUSER-FKID]: " + objsid + "-" + objdid + "-" + objfkuser + "-" + objfkid);

                            // SQL statement to get the actual object to be replicated
                            objectquery = conn.prepareStatement(
                                "SELECT SID, DID, USERNAME, FKID, USERID, KEYHANDLE, " +
                                " APPID, PUBLICKEY, " +
                                " TRANSPORTS, ATTSID, ATTDID, ATTCID, COUNTER, FIDO_VERSION, " +
                                " FIDO_PROTOCOL, AAGUID, REGISTRATION_SETTINGS,REGISTRATION_SETTINGS_VERSION, " +
                                " CREATE_DATE, CREATE_LOCATION, MODIFY_DATE, " +
                                " MODIFY_LOCATION, STATUS, SIGNATURE_KEYTYPE, SIGNATURE " +
                                " FROM FIDO_KEYS " +
                                " WHERE SID = ? " +
                                " AND DID = ? " +
                                " AND USERNAME = ? " +
                                " AND FKID = ?");
                            objectquery.setLong(1, objsid);
                            objectquery.setLong(2, objdid);
                            objectquery.setString(3, objfkuser);
                            objectquery.setLong(4, objfkid);



                            // Get object and create proto if found
                            objrs = objectquery.executeQuery();
                            ZMQSKCEReplicationProtos.FidoKeys.Builder fkbuilder = ZMQSKCEReplicationProtos.FidoKeys.newBuilder();
                            if (objrs.next()) {
                                // First deal with attributes that might be null
                                if (objrs.getString("userid") != null)
                                    fkbuilder.setUserid(objrs.getString("userid"));
                                if (objrs.getString("transports") != null)
                                    fkbuilder.setTransports(objrs.getLong("transports"));
                                if (objrs.getTimestamp("modify_date") != null)
                                    fkbuilder.setModifyDate(objrs.getTimestamp("modify_date").getTime());
                                if (objrs.getString("modify_location") != null)
                                    fkbuilder.setModifyLocation(objrs.getString("modify_location"));
                                if (objrs.getString("signature") != null)
                                    fkbuilder.setSignature(objrs.getString("signature"));
                                if (objrs.getString("signature_keytype") != null)
                                    fkbuilder.setSignatureKeytype(objrs.getString("signature_keytype"));
                                if (objrs.getString("attsid") != null)
                                    fkbuilder.setAttsid(objrs.getLong("attsid"));
                                if (objrs.getString("attdid") != null)
                                    fkbuilder.setAttdid(objrs.getLong("attdid"));
                                if (objrs.getString("attcid") != null)
                                    fkbuilder.setAttcid(objrs.getLong("attcid"));
                                if (objrs.getString("aaguid") != null)
                                    fkbuilder.setAaguid(objrs.getString("aaguid"));
                                if (objrs.getString("registration_settings") != null)
                                    fkbuilder.setRegistrationSettings(objrs.getString("registration_settings"));
                                if (objrs.getString("registration_settings_version") != null)
                                    fkbuilder.setRegistrationSettingsVersion(objrs.getLong("registration_settings_version"));
                                // Now build the proto with all non-null values
                                ZMQSKCEReplicationProtos.FidoKeys proto =
                                    fkbuilder
                                        .setAppid(objrs.getString("appid"))
                                        .setCounter(objrs.getLong("counter"))
                                        .setCreateDate(objrs.getTimestamp("create_date").getTime())
                                        .setCreateLocation(objrs.getString("create_location"))
                                        .setDid(objdid)
                                        .setFidoProtocol(objrs.getString("fido_protocol"))
                                        .setFidoVersion(objrs.getString("fido_version"))
                                        .setFkid(objfkid)
                                        .setKeyhandle(objrs.getString("keyhandle"))
                                        .setPublickey(objrs.getString("publickey"))
                                        .setSid(objsid)
                                        .setStatus(objrs.getString("status"))
                                        .setUsername(objfkuser)
                                        .build();
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6047", proto.toString());
                                objbytes = proto.toByteArray();
                            } else {
                                if (dbobjop == applianceConstants.REPLICATION_OPERATION_DELETE) {
                                    ZMQSKCEReplicationProtos.FidoKeys proto =
                                        fkbuilder
                                        .setAttcid(0L)
                                        .setAppid("")
                                        .setCounter(0L)
                                        .setCreateDate(0L)
                                        .setCreateLocation("")
                                        .setDid(objdid)
                                        .setFidoProtocol("")
                                        .setFidoVersion("")
                                        .setFkid(objfkid)
                                        .setKeyhandle("")
                                        .setPublickey("")
                                        .setSid(objsid)
                                        .setStatus("")
                                        .setSignatureKeytype("")
                                        .setUsername(objfkuser)
                                        .build();
                                    objbytes = proto.toByteArray();
                                } else {
                                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-ERR-6014", "[SID-DID-FIDOUSER-FKID]: " + objsid + "-" + objdid + "-" + objfkuser + "-" + objfkid);
                                    objectquery.close();
                                    continue;
                                }
                            }
                            objectquery.close();
                            break;

                        case applianceConstants.ENTITY_TYPE_FIDO_USERS:
                            // Figure out primary key from keystring
                            pkarray = dbobjpk.split("-", 3);
                            objsid = Long.parseLong(pkarray[0]);
                            objdid = Long.parseLong(pkarray[1]);
                            String objfuser = pkarray[2];

                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6046", "[SID-DID-FIDOUSER]: " + objsid + "-" + objdid + "-" + objfuser);

                            // SQL statement to get the actual object to be replicated
                            objectquery = conn.prepareStatement(
                                "SELECT SID, DID, USERNAME, USERDN, FIDO_KEYS_ENABLED, " +
                                " TWO_STEP_VERIFICATION, PRIMARY_EMAIL, REGISTERED_EMAILS, " +
                                " PRIMARY_PHONE_NUMBER, REGISTERED_PHONE_NUMBERS, TWO_STEP_TARGET, STATUS, SIGNATURE " +
                                " FROM FIDO_USERS " +
                                " WHERE SID = ? " +
                                " AND DID = ? " +
                                " AND USERNAME = ?");
                            objectquery.setLong(1, objsid);
                            objectquery.setLong(2, objdid);
                            objectquery.setString(3, objfuser);

                            // Get object and create proto if found
                            objrs = objectquery.executeQuery();
                            if (objrs.next()) {
                                // First deal with attributes that might be null
                                ZMQSKCEReplicationProtos.FidoUsers.Builder fubuilder = ZMQSKCEReplicationProtos.FidoUsers.newBuilder();
                                if (objrs.getString("primary_email") != null)
                                    fubuilder.setPrimaryEmail(objrs.getString("primary_email"));
                                if (objrs.getString("registered_emails") != null)
                                    fubuilder.setRegisteredEmails(objrs.getString("registered_emails"));
                                if (objrs.getString("primary_phone_number") != null)
                                    fubuilder.setPrimaryPhoneNumber(objrs.getString("primary_phone_number"));
                                if (objrs.getString("registered_phone_numbers") != null)
                                    fubuilder.setRegisteredPhoneNumbers(objrs.getString("registered_phone_numbers"));
                                if (objrs.getString("signature") != null)
                                    fubuilder.setSignature(objrs.getString("signature"));
                                if (objrs.getString("two_step_target") != null && objrs.getString("two_step_target").trim().length()>0)
                                    fubuilder.setTwoStepTarget(objrs.getString("two_step_target"));
                                if (objrs.getString("userdn") != null)
                                    fubuilder.setUserdn(objrs.getString("userdn"));
                                // Now build the proto with all non-null values
                                ZMQSKCEReplicationProtos.FidoUsers proto =
                                    fubuilder
                                        .setDid(objdid)
                                        .setFidoKeysEnabled(objrs.getString("fido_keys_enabled"))
                                        .setSid(objsid)
                                        .setStatus(objrs.getString("status"))
                                        .setTwoStepVerification(objrs.getString("two_step_verification"))
                                        .setUsername(objfuser)
                                        .build();
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6047", proto.toString());
                                objbytes = proto.toByteArray();
                            } else {
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-ERR-6014", "[SID-DID-FIDOUSER]: " + objsid + "-" + objdid + "-" + objfuser);
                                objectquery.close();
                                continue;
                            }
                            objectquery.close();
                            break;

                        case applianceConstants.ENTITY_TYPE_DOMAINS:
                            // Figure out primary key from keystring
                            pkarray = dbobjpk.split("-");
                            objdid = Long.parseLong(pkarray[0]);
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6046", "[DID]: " + objdid);

                            // SQL statement to get the actual object to be replicated
                            objectquery = conn.prepareStatement(
                                    "SELECT DID, NAME, STATUS, REPLICATION_STATUS, ENCRYPTION_CERTIFICATE, "
                                    + " ENCRYPTION_CERTIFICATE_UUID, "
                                    + " SIGNING_CERTIFICATE, SIGNING_CERTIFICATE_UUID, "
                                    + " SKCE_SIGNINGDN, "
                                    + " SKFE_APPID, NOTES "
                                    + " FROM DOMAINS "
                                    + " WHERE DID = ?");
                            objectquery.setLong(1, objdid);

                            // Get object and create proto if found
                            objrs = objectquery.executeQuery();
                            if (objrs.next()) {
                                // First deal with attributes that might be null
                                ZMQSKCEReplicationProtos.Domains.Builder dombuilder = ZMQSKCEReplicationProtos.Domains.newBuilder();
                                if (objrs.getString("encryption_certificate") != null) {
                                    dombuilder.setEncryptionCertificate(objrs.getString("encryption_certificate"));
                                }
                                if (objrs.getString("encryption_certificate_uuid") != null) {
                                    dombuilder.setEncryptionCertificateUuid(objrs.getString("encryption_certificate_uuid"));
                                }
                                if (objrs.getString("name") != null) {
                                    dombuilder.setName(objrs.getString("name"));
                                }
                                if (objrs.getString("notes") != null) {
                                    dombuilder.setNotes(objrs.getString("notes"));
                                }
                                if (objrs.getString("signing_certificate_uuid") != null) {
                                    dombuilder.setSigningCertificateUuid(objrs.getString("signing_certificate_uuid"));
                                }
                                if (objrs.getString("signing_certificate") != null) {
                                    dombuilder.setSigningCertificate(objrs.getString("signing_certificate"));
                                }
                                if (objrs.getString("skce_signingdn") != null) {
                                    dombuilder.setSkceSigningdn(objrs.getString("skce_signingdn"));
                                }
                                if (objrs.getString("skfe_appid") != null) {
                                    dombuilder.setSkfeAppid(objrs.getString("skfe_appid"));
                                }
                                // Now build the proto with all non-null values
                                ZMQSKCEReplicationProtos.Domains proto
                                        = dombuilder
                                        .setDid(objdid)
                                        .setReplicationStatus(objrs.getString("replication_status"))
                                        .setStatus(objrs.getString("status"))
                                        .build();
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6047", proto.toString());
                                objbytes = proto.toByteArray();
                            } else {
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "run", "SKCE-ERR-6014", "[DID]: " + objdid);
                                objectquery.close();
//                                deleteRow(dbssid, dbrpid, dbtsid);
                                continue;
                            }
                            objectquery.close();
                            break;

                        case applianceConstants.ENTITY_TYPE_ATTESTATION_CERTIFICATES:
                            // Figure out primary key from keystring
                            pkarray = dbobjpk.split("-", 3);
                            objsid = Long.parseLong(pkarray[0]);
                            objdid = Long.parseLong(pkarray[1]);
                            Long objattcid = Long.parseLong(pkarray[2]);

                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6046", "[SID-DID-ATTCID]: " + objsid + "-" + objdid + "-" + objattcid);

                            // SQL statement to get the actual object to be replicated
                            objectquery = conn.prepareStatement(
                                "SELECT SID, DID, ATTCID, PARENT_SID, PARENT_DID, PARENT_ATTCID, " +
                                " CERTIFICATE, ISSUER_DN, SUBJECT_DN, SERIAL_NUMBER, SIGNATURE " +
                                " FROM ATTESTATION_CERTIFICATES " +
                                " WHERE SID = ? " +
                                " AND DID = ? " +
                                " AND ATTCID = ? ");
                            objectquery.setLong(1, objsid);
                            objectquery.setLong(2, objdid);
                            objectquery.setLong(3, objattcid);

                            // Get object and create proto if found
                            objrs = objectquery.executeQuery();
                            if (objrs.next()) {
                                // First deal with attributes that might be null
                                ZMQSKCEReplicationProtos.AttestationCertificates.Builder attCertbuilder = ZMQSKCEReplicationProtos.AttestationCertificates.newBuilder();
                                if (objrs.getString("parent_sid") != null) {
                                    attCertbuilder.setParentSid(objrs.getLong("parent_sid"));
                                }
                                if (objrs.getString("parent_did") != null) {
                                    attCertbuilder.setParentDid(objrs.getLong("parent_did"));
                                }
                                if (objrs.getString("parent_attcid") != null) {
                                    attCertbuilder.setParentAttcid(objrs.getLong("parent_attcid"));
                                }
                                if (objrs.getString("signature") != null) {
                                    attCertbuilder.setSignature(objrs.getString("signature"));
                                }

                                // Now build the proto with all non-null values
                                ZMQSKCEReplicationProtos.AttestationCertificates proto
                                        = attCertbuilder
                                        .setSid(objsid)
                                        .setDid(objdid)
                                        .setAttcid(objattcid)
                                        .setCertificate(objrs.getString("certificate"))
                                        .setIssuerDn(objrs.getString("issuer_dn"))
                                        .setSubjectDn(objrs.getString("subject_dn"))
                                        .setSerialNumber(objrs.getString("serial_number"))
                                        .build();
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6047", proto.toString());
                                objbytes = proto.toByteArray();
                            } else {
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "run", "SKCE-ERR-6014", "[SID-DID-ATTCID]: " + objsid + "-" + objdid + "-" + objattcid);
                                objectquery.close();
                                continue;
                            }
                            objectquery.close();
                            break;

                        case applianceConstants.ENTITY_TYPE_FIDO_POLICIES:
                            // Figure out primary key from keystring
                            pkarray = dbobjpk.split("-", 3);
                            objsid = Long.parseLong(pkarray[0]);
                            objdid = Long.parseLong(pkarray[1]);
                            Long objpid = Long.parseLong(pkarray[2]);

                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6046", "[SID-DID-PID]: " + objsid + "-" + objdid + "-" + objpid);

                            // SQL statement to get the actual object to be replicated
                            objectquery = conn.prepareStatement(
                                "SELECT SID, DID, PID," +
                                " POLICY, STATUS, NOTES, CREATE_DATE, MODIFY_DATE, SIGNATURE " +
                                " FROM FIDO_POLICIES " +
                                " WHERE SID = ? " +
                                " AND DID = ? " +
                                " AND PID = ? ");
                            objectquery.setLong(1, objsid);
                            objectquery.setLong(2, objdid);
                            objectquery.setLong(3, objpid);

                            // Get object and create proto if found
                            objrs = objectquery.executeQuery();
                            if (objrs.next()) {
                                // First deal with attributes that might be null
                                ZMQSKCEReplicationProtos.FidoPolicies.Builder fpbuilder = ZMQSKCEReplicationProtos.FidoPolicies.newBuilder();
                                
                                if (objrs.getString("notes") != null) {
                                    fpbuilder.setNotes(objrs.getString("notes"));
                                }
                               if (objrs.getTimestamp("modify_date") != null) {
                                    fpbuilder.setModifyDate(objrs.getTimestamp("modify_date").getTime());
                                }
                                if (objrs.getString("signature") != null) {
                                    fpbuilder.setSignature(objrs.getString("signature"));
                                }

                                // Now build the proto with all non-null values
                                ZMQSKCEReplicationProtos.FidoPolicies proto
                                        = fpbuilder
                                        .setSid(objsid)
                                        .setDid(objdid)
                                        .setPid(objpid)
                                        .setPolicy(objrs.getString("policy"))
                                        .setStatus(objrs.getString("status"))
                                        .setCreateDate(objrs.getTimestamp("create_date").getTime())
                                        .build();
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-MSG-6047", proto.toString());
                                objbytes = proto.toByteArray();
                            } else {
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "run", "SKCE-ERR-6014", "[SID-DID-PID]: " + objsid + "-" + objdid + "-" + objpid);
                                objectquery.close();
                                continue;
                            }
                            objectquery.close();
                            break;

                        case applianceConstants.ENTITY_TYPE_FIDO_CONFIGURATIONS:

                            // Figure out primary key from keystring
                            pkarray = dbobjpk.split("-", 2);
                            objdid = Long.parseLong(pkarray[0]);
                            String objkey = pkarray[1];
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.FINE, classname, "run", "SKCE-MSG-6046", "[DID-CONFIG_KEY]: " + objdid + "-" + objkey);

                            // SQL statement to get the actual object to be replicated
                            objectquery = conn.prepareStatement(
                                    "SELECT DID, CONFIG_KEY, CONFIG_VALUE, NOTES "
                                    + " FROM CONFIGURATIONS "
                                    + " WHERE DID = ? "
                                    + " AND CONFIG_KEY = ?");
                            objectquery.setLong(1, objdid);
                            objectquery.setString(2, objkey);

                            // Get object and create proto if found
                            objrs = objectquery.executeQuery();
                            ZMQSKCEReplicationProtos.Configurations.Builder cfgbuilder = ZMQSKCEReplicationProtos.Configurations.newBuilder();
                            if (objrs.next()) {
                                // First deal with attributes that might be null
                                
                                if (objrs.getString("notes") != null) {
                                    cfgbuilder.setNotes(objrs.getString("notes"));
                                }
                                // Now build the proto with all non-null values
                                ZMQSKCEReplicationProtos.Configurations proto
                                        = cfgbuilder
                                                .setConfigKey(objrs.getString("config_key"))
                                                .setConfigValue(objrs.getString("config_value"))
                                                .setDid(objdid)
                                                .build();
                                strongkeyLogger.logp(skceConstants.SKEE_LOGGER, Level.FINE, classname, "run", "SKCE-MSG-6047", proto.toString());
                                objbytes = proto.toByteArray();
                            } else {
                                if (dbobjop == applianceConstants.REPLICATION_OPERATION_DELETE) {
                                    ZMQSKCEReplicationProtos.Configurations proto =
                                        cfgbuilder
                                        .setConfigKey(objkey)
                                                .setConfigValue("")
                                                .setDid(objdid)
                                                .build();
                                    objbytes = proto.toByteArray();
                                } else {
                                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.FINE, classname, "run", "SKCE-ERR-6014", "[DID-CONFIGKEY]: " + objdid + "-" + objkey);
                                    objectquery.close();
                                    continue;
                                }
                            }
                            objectquery.close();

                            break;
   
                        default:
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "run", "SKCE-ERR-6015", applianceCommon.getEntityName(dbobjtype) + " [OBJPK=" + dbobjpk + "] [OBJOP=" + applianceCommon.getRepop(dbobjop) + "]");
                            invalidObject = Boolean.TRUE;
                            break;
                    }

                    // Don't go into ZMQ code unless its a valid object
                    if (!invalidObject) {
                        /**
                        * Publish the object as a multi-frame message
                        * - first, the primary-key of the replication object with the TSID stripped out
                        * - next, the type of object being replicated
                        * - next, the operation on the object: add, delete, or modify
                        * - next, the primary-key of the object and
                        * - finally, the object itself as a Protocol Buffer message
                        */
                        String reppk = dbssid + "-" + dbrpid + "-" + dbtsid;
                        String rmsg = "REPOBJPK=" + reppk + " [OBJTYPE=" + applianceCommon.getEntityName(dbobjtype) + "] [OBJPK=" + dbobjpk + "] [OBJOP=" + applianceCommon.getRepop(dbobjop) + "]";
                        if (ZMQBacklogProcessor.getInstance().send(reppk, dbobjtype, dbobjop, dbobjpk, objbytes)) {
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.INFO, classname, "run", "SKCE-MSG-6048", rmsg);
                        } else {
                            strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "run", "SKCE-ERR-6016", rmsg);

                        }
                    } else {
                        // Reset invalid object flag for next iteration
                        invalidObject = Boolean.FALSE;
                    }
                }
            }
            countquery.close();
        }catch (SQLException ex) {
            Logger.getLogger(SKCEBacklogProcessor.class.getName()).log(Level.SEVERE, null, ex);
            if (ex.getSQLState().startsWith("08")) {
                try {
                    strongkeyLogger.logp(skceConstants.SKEE_LOGGER,Level.WARNING, classname, "run", "SKCE-MSG-6111", "");
                    conn.close();
                } catch (SQLException se) {}
            }
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException ex) {
                    Logger.getLogger(BacklogProcessorHelper.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
    }
}
