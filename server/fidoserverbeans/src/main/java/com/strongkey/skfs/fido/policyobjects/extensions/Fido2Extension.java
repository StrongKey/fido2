/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.fido.policyobjects.extensions;

import javax.json.JsonValue;

public interface Fido2Extension {
    public String getExtensionIdentifier();
    public Object generateChallengeInfo(JsonValue extraInfo);
    public boolean verifyFido2Extension(String extensionResponse);
}
