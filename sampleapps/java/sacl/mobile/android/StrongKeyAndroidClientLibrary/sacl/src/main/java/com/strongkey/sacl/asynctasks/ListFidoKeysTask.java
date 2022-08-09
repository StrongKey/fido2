/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, as published by the Free
 * Software Foundation and available at
 * http://www.fsf.org/licensing/licenses/lgpl.html, version 2.1.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 * <p>
 * Copyright (c) 2001-2021 StrongAuth, Inc. (d/b/a StrongKey)
 * <p>
 * **********************************************
 * <p>
 * 888b    888          888
 * 8888b   888          888
 * 88888b  888          888
 * 888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 * 888 Y88b888 d88""88b 888    d8P  Y8b 88K
 * 888  Y88888 888  888 888    88888888 "Y8888b.
 * 888   Y8888 Y88..88P Y88b.  Y8b.          X88
 * 888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 * <p>
 * **********************************************
 * <p>
 * Asynchronous task to list keys in the local device
 */

package com.strongkey.sacl.asynctasks;

import android.content.Context;

import com.strongkey.sacl.roomdb.PublicKeyCredential;
import com.strongkey.sacl.roomdb.SaclRepository;
import com.strongkey.sacl.utilities.Common;
import com.strongkey.sacl.utilities.Constants;
import com.strongkey.sacl.utilities.LocalContextWrapper;

import java.util.List;
import java.util.concurrent.Callable;

@SuppressWarnings("unused")
public class ListFidoKeysTask implements Callable {

    private final String TAG = ListFidoKeysTask.class.getSimpleName();

    SaclRepository saclRepository;
    private LocalContextWrapper context;
    private int did;
    private String rpid;
    private String userid;

    /**
     * Constructor
     * @param context Application context
     * @param did int value of the cryptographic domain ID
     * @param userid String value of the user ID
     */
    public ListFidoKeysTask(Context context, int did, String rpid, String userid) {
        this.context = new LocalContextWrapper(context);
        this.did = did;
        this.userid = userid;
        this.rpid = rpid;
    }

    @Override
    public Object call() {

        // Get SACL Repository
        saclRepository = Common.getRepository(context);

        // Get records from RoomDB
        List<PublicKeyCredential> pkclist = saclRepository.getPublicKeyCredentialsByRpidUserid(did, rpid, userid);

        // Put them in SACL SharedModel
        Common.setCurrentObject(Constants.SACL_OBJECT_TYPES.PUBLIC_KEY_CREDENTIAL_LIST, pkclist);

        return pkclist;
    }
}
