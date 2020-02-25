/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
