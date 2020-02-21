/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.appliance.utilities;

import com.strongkey.skce.utilities.skceConstants;
import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ClassWithMultipleLoggers")
public class strongkeyLogger {

    private static final String classname = "strongkeyLogger";

    // Logger for the application
    private static final Logger APPL_LOGGER = Logger.getLogger("APPL", "resources.appliance.appliance-messages");
    private static final Logger REPL_LOGGER = Logger.getLogger("REPL", "resources.repl.replication-messages");
    private static final Logger SKEE_LOGGER = Logger.getLogger("SKEE", "resources.skce.skce-messages");

    static {

    }

    public strongkeyLogger() {

    }

    /**
     * Prints the source-class and method names to the application logger upon
     * entering the class method
     *
     * @param logger
     * @param sourceClass - the classname of the class that called this method
     * @param sourceMethod - the name of the method in which this method is
     * called
     */
    public static void entering(String logger, String sourceClass, String sourceMethod) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.entering(sourceClass, sourceMethod);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.entering(sourceClass, sourceMethod);
        } else {
            APPL_LOGGER.entering(sourceClass, sourceMethod);
        }

    }

    /**
     * Prints the source-class and method names to the application logger before
     * exiting the class method
     *
     * @param logger
     * @param sourceClass - the classname of the class that called this method
     * @param sourceMethod - the name of the method in which this method is
     * called
     */
    public static void exiting(String logger, String sourceClass, String sourceMethod) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.exiting(sourceClass, sourceMethod);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.exiting(sourceClass, sourceMethod);
        } else {
            APPL_LOGGER.exiting(sourceClass, sourceMethod);
        }
    }

    public static void log(String logger, java.util.logging.Level level, String key, Object param) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.log(level, key, param);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.log(level, key, param);
        } else {
            APPL_LOGGER.log(level, key, param);
        }

    }

    public static void log(String logger, java.util.logging.Level level, String key, Object[] params) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.log(level, key, params);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.log(level, key,params);
        } else {
            APPL_LOGGER.log(level, key, params);
        }
    }

    public static void log(String logger, java.util.logging.Level level, String key) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.log(level, key);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.log(level, key);
        } else {
            APPL_LOGGER.log(level, key);
        }
    }

    public static void logp(String logger, java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key, Object param) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.logp(level, sourceClass, sourceMethod, key, param);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.logp(level, sourceClass, sourceMethod, key, param);
        } else {
            APPL_LOGGER.logp(level, sourceClass, sourceMethod, key, param);
        }
    }

    public static void logp(String logger, java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.logp(level, sourceClass, sourceMethod, key);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.logp(level, sourceClass, sourceMethod, key);
        } else {
            APPL_LOGGER.logp(level, sourceClass, sourceMethod, key);
        }
    }

    public static void logp(String logger, java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key, Object[] params) {
        if (logger.equalsIgnoreCase(applianceConstants.REPL_LOGGER)) {
            REPL_LOGGER.logp(level, sourceClass, sourceMethod, key, params);
        } else if (logger.equalsIgnoreCase(skceConstants.SKEE_LOGGER)) {
            SKEE_LOGGER.logp(level, sourceClass, sourceMethod, key, params);
        } else {
            APPL_LOGGER.logp(level, sourceClass, sourceMethod, key, params);
        }
    }

    public static void printStrongAuthStackTrace(String logger, String sourceclassname, String sourcemethod, Exception ex) {
        StackTraceElement err[] = ex.getStackTrace();
        StringBuilder sb = new StringBuilder(1024);
        for (StackTraceElement err1 : err) {
            if (err1.toString().contains("com.strongauth") || err1.toString().contains("com.strongkey") || err1.toString().contains("Caused by")) {
                sb.append('\t').append(err1).append('\n');
            }
        }
        logp(logger, Level.SEVERE, sourceclassname, sourcemethod, "FSO-MSG-5000", sb.append('\n').toString());
    }

}
