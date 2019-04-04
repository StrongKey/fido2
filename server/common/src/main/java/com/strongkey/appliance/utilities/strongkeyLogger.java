/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.appliance.utilities;

import java.util.logging.Level;
import java.util.logging.Logger;

@SuppressWarnings("ClassWithMultipleLoggers")
public class strongkeyLogger {

    private static final String classname = "strongkeyLogger";

    // Logger for the application
    private static final Logger APPL_LOGGER = Logger.getLogger("APPL", "resources.appliance.appliance-messages");

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
        APPL_LOGGER.entering(sourceClass, sourceMethod);

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
        APPL_LOGGER.exiting(sourceClass, sourceMethod);
    }

    public static void log(String logger, java.util.logging.Level level, String key, Object param) {
        APPL_LOGGER.log(level, key, param);

    }

    public static void log(String logger, java.util.logging.Level level, String key, Object[] params) {
        APPL_LOGGER.log(level, key, params);
    }

    public static void log(String logger, java.util.logging.Level level, String key) {
        APPL_LOGGER.log(level, key);
    }

    public static void logp(String logger, java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key, Object param) {
        APPL_LOGGER.logp(level, sourceClass, sourceMethod, key, param);
    }

    public static void logp(String logger, java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key) {
        APPL_LOGGER.logp(level, sourceClass, sourceMethod, key);
    }

    public static void logp(String logger, java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key, Object[] params) {
        APPL_LOGGER.logp(level, sourceClass, sourceMethod, key, params);
    }

    public static void printStrongAuthStackTrace(String logger, String sourceclassname, String sourcemethod, Exception ex) {
        StackTraceElement err[] = ex.getStackTrace();
        StringBuilder sb = new StringBuilder(1024);
        for (StackTraceElement err1 : err) {
            if (err1.toString().contains("com.strongauth") || err1.toString().contains("Caused by")) {
                sb.append('\t').append(err1).append('\n');
            }
        }
        logp(logger, Level.SEVERE, sourceclassname, sourcemethod, "FSO-MSG-5000", sb.append('\n').toString());
    }

}
