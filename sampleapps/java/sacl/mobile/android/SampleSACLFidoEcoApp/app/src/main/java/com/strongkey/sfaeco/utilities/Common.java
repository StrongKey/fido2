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
 * Copyright (c) 2001-2021 StrongAuth, Inc. (d/b/a StrongKey)
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
 * Utility functions and objects used by the app
 */

package com.strongkey.sfaeco.utilities;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.strongkey.sacl.roomdb.SaclSharedDataModel;
import com.strongkey.sfaeco.main.SfaSharedDataModel;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public abstract class Common {

    private static SaclSharedDataModel saclSharedDataModel;
    private static SfaSharedDataModel sfaSharedDataModel;

    public static <T extends View> List<T> findViewsWithType(View root, Class<T> type) {
        List<T> views = new ArrayList<>();
        findViewsWithType(root, type, views);
        return views;
    }

    private static <T extends View> void findViewsWithType(View view, Class<T> type, List<T> views) {
        if (type.isInstance(view)) {
            views.add(type.cast(view));
        }

        if (view instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) view;
            for (int i = 0; i < viewGroup.getChildCount(); i++) {
                findViewsWithType(viewGroup.getChildAt(i), type, views);
            }
        }
    }

    /**
     *  Generates the current time, as based on the clock of the EJB Tier machine.
     *  NOTE:  The reason for the math with 1000 in the function is because MySQL
     *  does not store the milliseconds, while TimeStamp uses it.  However the
     *  XMLSignatures will fail if the milliseconds are not removed from the time
     *  before creating the time.  This implies that all timestamps are accurate
     *  only upto the second - not millisecond.  Good enough for this application,
     *  we believe.
     *
     * @return java.sql.Timestamp
     */
    public static java.sql.Timestamp now() {
        return new java.sql.Timestamp(((new java.util.GregorianCalendar().getTimeInMillis())/1000)*1000);
    }

    /**
     * Returns the current date-time as milliseconds
     * @return long value of the current date-time since 1/1/1970 (UNIX epoch)
     */
    public static long nowms() {
        return new java.util.Date().getTime();
    }


    // Method to get resources from the SACL library
    public static Object getResource(Context c, String rname, String rtype, String rpkg) {
        if (rpkg == null)
            return c.getResources().getIdentifier(rname, rtype, "com.strongkey.sacl");
        else
            return c.getResources().getIdentifier(rname, rtype, rpkg);
    }

    /**
     * Returns a JSON object with an error message, as well as the classname and methodname
     * where the error originated in the library
     *
     * @param c String with the classname
     * @param m String with the methodname from the class
     * @param k String with the JSON key
     * @param v String with the JSON value
     * @return JsonObject as follows:
     *  {
     *      "error" : {
     *          "c": "classname",
     *          "m": "methodname",
     *          "k": "v"
     *      }
     *  }
     * @throws JSONException
     */
    public static JSONObject JsonError(String c, String m, String k, String v) throws JSONException {
        return new JSONObject()
                .put("error", new JSONObject()
                    .put("c", c)
                    .put("m", m)
                    .put(k, v));
    }

    // Holds a static SaclSharedDataModel object for fragments to use
    public static void putSaclSharedDataModel(SaclSharedDataModel ssdm) {
        if (saclSharedDataModel == null)
            saclSharedDataModel = ssdm;
    }

    public static SaclSharedDataModel getSaclSharedDataModel() {
        return saclSharedDataModel;
    }

    // Holds a static SfaSharedDataModel object for fragments to use
    public static void putSfaSharedViewModel(SfaSharedDataModel ssvm) {
        if (sfaSharedDataModel == null)
            sfaSharedDataModel = ssvm;
    }

    public static SfaSharedDataModel getSfaSharedDataModel() {
        return sfaSharedDataModel;
    }

    // Status values for Users
    public static enum UserStatus { Authorized, Registered, Failed, Active, Inactive, Other }

    // Status values for UserDevices
    public static enum UserDeviceStatus { Authorized, Registered, Failed, Active, Inactive, Other }

    // Status values for UserDeviceAuthorization
    public static enum UserDeviceAuthorizationStatus { Authorized, Registered, Failed, Other }


    // Status values for RegisteredDevices
    public static enum RegisteredDeviceStatus {
        Active,
        Inactive,
        Revoked,
        Other
    }

}
