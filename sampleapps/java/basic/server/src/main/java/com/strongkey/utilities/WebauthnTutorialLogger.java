/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */

package com.strongkey.utilities;

import java.util.Locale;
import java.util.ResourceBundle;

// Provides basic logging functionality
public class WebauthnTutorialLogger {
    // Logger for the application
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger("WEBAUTHNTUTORIAL", "resources.webauthntutorial-messages");

    // Load messages for Exceptions
    private static final ResourceBundle MESSAGEBUNDLE = ResourceBundle.getBundle("resources.webauthntutorial-messages");

    public static final String getMessageProperty(String key) {
        return MESSAGEBUNDLE.getString(key);
    }

    public static void logp(java.util.logging.Level level,
            String sourceClass, String sourceMethod, String key, Object param) {
        LOGGER.logp(level, sourceClass, sourceMethod, key, param);
    }
}
