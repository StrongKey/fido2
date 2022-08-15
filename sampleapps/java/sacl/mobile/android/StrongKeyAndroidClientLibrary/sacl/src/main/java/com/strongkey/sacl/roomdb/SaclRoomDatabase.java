/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, as published by the Free
 * Software Foundation and available at
 * http://www.fsf.org/licensing/licenses/lgpl.html, version 2.1.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Copyright (c) 2001-2022 StrongAuth, Inc. (d/b/a StrongKey)
 *
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * The database for the StrongKey Android Client Library
 */

package com.strongkey.sacl.roomdb;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {PreregisterChallenge.class, PublicKeyCredential.class,
                        PreauthenticateChallenge.class, AuthenticationSignature.class,
                        PreauthorizeChallenge.class, AuthorizationSignature.class},
                        version = 5,
                        exportSchema = false)

public abstract class SaclRoomDatabase extends RoomDatabase {

    // Local variables
    private static volatile SaclRoomDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 2;

    /**
     * Abstract constructor
     * @return SaclDao
     */
    public abstract SaclDao saclDao();

    // Service for asynchronous tasks
    static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Singleton method to get database
    static SaclRoomDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (SaclRoomDatabase.class) {
                INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                                SaclRoomDatabase.class,
                                                "sacl_database")
                    .fallbackToDestructiveMigration()
                    .addCallback(sRoomDatabaseCallback)
                    .build();
            }
        }
        return INSTANCE;
    }

    // Private method to start database
    private static Callback sRoomDatabaseCallback = new Callback() {
        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);
        }
    };
}
