/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.skfs.messaging;

public interface BacklogProcessor extends Runnable
{
    @Override
    public abstract void run ();
    public abstract boolean running();
    public abstract String restart();
    public abstract void shutdown();
}

