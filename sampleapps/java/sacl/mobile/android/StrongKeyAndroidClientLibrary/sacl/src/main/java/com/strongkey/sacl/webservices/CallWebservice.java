/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
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
 * A common class for webservice operations to connect to the MDBA
 * server to call WS operations. Callers must specify the operation
 * name and supply the JSON input parameter to the operation.
 */

package com.strongkey.sacl.webservices;

import android.content.Context;
import android.content.ContextWrapper;
import android.net.ConnectivityManager;
import android.util.Log;

import com.strongkey.sacl.R;
import com.strongkey.sacl.utilities.Common;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

@SuppressWarnings("DanglingJavadoc")
final public class CallWebservice {

    // TAGs for logging
    private static final String TAG = CallWebservice.class.getSimpleName();

    /**************************************************************
     *                                              888
     *                                              888
     *                                              888
     *  .d88b.  888  888  .d88b.   .d8888b 888  888 888888  .d88b.
     * d8P  Y8b `Y8bd8P' d8P  Y8b d88P"    888  888 888    d8P  Y8b
     * 88888888   X88K   88888888 888      888  888 888    88888888
     * Y8b.     .d8""8b. Y8b.     Y88b.    Y88b 888 Y88b.  Y8b.
     *  "Y8888  888  888  "Y8888   "Y8888P  "Y88888  "Y888  "Y8888
     *************************************************************/

    /**
     * Performs the network operation of calling the webservice and getting a response
     * @param wsoperation String with the operation part of the webservice URL - the host-port
     * part is picked up from a resource string, by default, but can be over-ridden by a
     * configuration property in user settings
     * @param mJSONObjectInput JSONObject containing input parameters for the WS operation
     * @param contextWrapper Context object wrapper to get resource strings/values
     * @return JSONObject with the response
     */
    public static JSONObject execute(String wsoperation,
                                        JSONObject mJSONObjectInput,
                                        ContextWrapper contextWrapper) {

        // Local variables
        String MTAG = "execute";
        String SERVICE_URL;

        // Are we using a self-signed certificate for internal uses?
        Boolean selfsigned = contextWrapper.getResources().getString(R.string.sacl_using_self_signed_certificate).equalsIgnoreCase("true");
        if (selfsigned) {
            SERVICE_URL = (contextWrapper.getResources().getString(R.string.sacl_self_signed_service_hostport)).concat(wsoperation);
        } else {
            SERVICE_URL = (contextWrapper.getResources().getString(R.string.sacl_service_hostport)).concat(wsoperation);
        }

//        String SERVICE_URL = (contextWrapper.getResources().getString(R.string.application_webservice_hostport)).concat(wsoperation);

        try {
            // Test for network
            ConnectivityManager connMgr = (ConnectivityManager) contextWrapper.getSystemService(Context.CONNECTIVITY_SERVICE);
            if (!connMgr.isDefaultNetworkActive()) {
                String mErrorString = contextWrapper.getResources().getString(R.string.ERROR_NETWORK_UNAVAILABLE);
                Log.w(TAG, mErrorString);
                return Common.jsonError(TAG, MTAG, com.strongkey.sacl.utilities.Constants.ERROR_NETWORK_UNAVAILABLE, mErrorString);
            }

            // Network is available; setup connection to server webservice
            Log.v(TAG, contextWrapper.getResources().getString(R.string.message_connecting).concat(SERVICE_URL));

            // Local variables for secure TLS connection
            HttpsURLConnection conn;
            SSLContext context = null;

            try {
                // Setup connection parameters
                URL url = new URL(SERVICE_URL);
                conn = (HttpsURLConnection) url.openConnection();

                // If we are using a self-signed certificate, load self-signed cert
                if (selfsigned) {
                    // Read self-signed certificate file (not necessarily in production)
                    String appfqdn = contextWrapper.getResources().getString(R.string.sacl_self_signed_service_cert);
                    int certfileid = contextWrapper.getResources().getIdentifier(appfqdn, "raw", contextWrapper.getPackageName());
                    CertificateFactory cf = CertificateFactory.getInstance("X.509");
                    InputStream is = contextWrapper.getResources().openRawResource(certfileid);
                    Certificate ca;
                    try {
                        ca = cf.generateCertificate(is);
                        Log.v(TAG, "Retrieved self-signed certificate: " + ((X509Certificate) ca).getSubjectDN());
                    } finally {
                        is.close();
                    }

                    // Insert self-signed certificate into keystore
                    KeyStore tlsKeystore = KeyStore.getInstance(KeyStore.getDefaultType());
                    tlsKeystore.load(null);
                    tlsKeystore.setCertificateEntry("ca", ca);
                    Log.v(TAG, "Setup self-signed certificate: " + ((X509Certificate) ca).getSubjectDN());

                    // Setup Trust Manager to accept self-signed cert
                    String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
                    Log.v(TAG, "TrustManagerFactor Default Algorithm: " + tmfAlgorithm);
                    TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
                    tmf.init(tlsKeystore);
                    context = SSLContext.getInstance(com.strongkey.sacl.utilities.Constants.ATTESTATION_CONNECTION_TYPE);
                    context.init(null, tmf.getTrustManagers(), null);
                    conn.setSSLSocketFactory(context.getSocketFactory());
                    Log.v(TAG, "Setup TLS context with TrustManager using self-signed certificate");
                }

                // Continue processing HTTP request
                conn.setReadTimeout(30000 /* milliseconds */);
                conn.setConnectTimeout(30000 /* milliseconds */);
                conn.setRequestMethod("POST");
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setRequestProperty("Accept", "application/json");
                conn.setRequestProperty("Content-Type", "application/json");

                // Connect and post the message and check response
                conn.setFixedLengthStreamingMode(mJSONObjectInput.toString().getBytes().length);
                conn.connect();
                Log.v(TAG, "Connected...");
                OutputStream os = conn.getOutputStream();
                os.write(mJSONObjectInput.toString().getBytes());
                os.flush();
                os.close();
                int responseCode = conn.getResponseCode();
                if (responseCode != com.strongkey.sacl.utilities.Constants.HTTP_SUCCESS) {
                    throw new RuntimeException(contextWrapper.getResources().getString(R.string.message_http_fail) + responseCode);
                }
                Log.v(TAG, "Response: HTTP_SUCCESS");

                /*
                 Read server's response. Cannot initialize response to null value
                 because the Android version of Java seems to convert it to a string
                 ("null"), which shows up in the JSON string and causes parsing issues
                */
                int chr;
                StringBuilder sb = new StringBuilder();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                while ((chr = br.read()) != -1) {
                    sb.append((char) chr);
                }
                conn.disconnect();
                String wsresponse = sb.toString();
                Log.v(TAG, "Response Output: " + wsresponse);

                // Return response
                return (JSONObject) new JSONTokener(wsresponse).nextValue();

            } catch (IOException | CertificateException | KeyStoreException |
                    NoSuchAlgorithmException | KeyManagementException e) {
                e.printStackTrace();
                try {
                    return Common.jsonError(TAG, MTAG, com.strongkey.sacl.utilities.Constants.ERROR_EXCEPTION, e.getLocalizedMessage());
                } catch (JSONException e1) {
                    e1.printStackTrace();
                    return null;
                }
            }

        } catch (JSONException ex) {
            ex.printStackTrace();
            return null;
        }
    }
}
