/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.messaging;

import javax.ejb.Remote;

@Remote
public interface persistReplicationObjectRemote {
    String remoteExecute(int objectype, int objectop, String objectpk);
}
