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
 * Copyright (c) 2001-2020 StrongAuth, Inc. (d/b/a StrongKey)
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
 * Asynchronous task to persist a Java object in the Room database
 */

package com.strongkey.sfaeco.asynctasks;

import android.app.Application;
import android.util.Log;

import com.strongkey.sfaeco.roomdb.SfaRepository;
import com.strongkey.sfaeco.roomdb.User;

public class PersistObjectTask implements Runnable {

    private final String TAG = PersistObjectTask.class.getSimpleName();

    private Application application;
    private String objectType;
    private Object object;

    public PersistObjectTask(Application application, String objectType, Object object) {
        this.application = application;
        this.objectType = objectType;
        this.object = object;
    }

    @Override
    public void run() {
        SfaRepository sfaRepository = new SfaRepository(application);
        // What object are we saving?
        switch (objectType) {
            case "User":
                User user = (User) object;
                long rowid = sfaRepository.insertUser(user);
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {}
                String msg;
                if (rowid < 0) {
                    msg = "Failed to insertUser object: " + objectType + " [" + rowid + "]";
                    Log.w(TAG, msg);
                } else {
                    msg = "Rowid for new: " + objectType + " [" + rowid + "]";
                    Log.d(TAG, msg);
                }
                break;
            default:
                Log.w(TAG, "Invalid object class: " + objectType);
        }
    }
}
