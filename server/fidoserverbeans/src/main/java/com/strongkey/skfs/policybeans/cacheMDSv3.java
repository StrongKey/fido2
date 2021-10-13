/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1 or above.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc.
 *
 * $Date: $
 * $Revision: $
 * $Author: $
 * $URL: $
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 */
package com.strongkey.skfs.policybeans;

import com.strongkey.skfs.pojos.FIDOMetadataService;
import com.strongkey.skfs.utilities.SKFSCommon;
import static com.strongkey.skfs.utilities.SKFSCommon.getConfigurationProperty;
import com.strongkey.skfs.utilities.SKFSConstants;
import com.strongkey.skfs.utilities.SKFSLogger;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.Security;
import java.security.Signature;
import java.security.cert.CertPath;
import java.security.cert.CertPathValidator;
import java.security.cert.CertPathValidatorResult;
import java.security.cert.CertificateFactory;
import java.security.cert.PKIXParameters;
import java.security.cert.TrustAnchor;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import javax.ejb.Schedule;
import javax.ejb.Stateless;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.Response;
import org.bouncycastle.jcajce.provider.BouncyCastleFipsProvider;

@Stateless
public class cacheMDSv3 implements cacheMDSv3Local {

    /*
     * This class' name - used for logging
     */
    private final String classname = this.getClass().getName();

    @Override
//    @Asynchronous
    @Schedule(minute = "0", hour = "0", dayOfMonth = "*", month = "*", dayOfWeek = "*", persistent = true)
    public void execute() {

        String mdsenabled = SKFSCommon.getConfigurationProperty("skfs.cfg.property.mds.enabled");
        if (mdsenabled.equalsIgnoreCase("true") || mdsenabled.equalsIgnoreCase("yes")) {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINE, classname, "execute", "FIDO-MSG-3001", "");

            if (SKFSCommon.getMdsrootca() == null) {
                Client client = null;
                WebTarget webTarget;
                Response rs = null;
                try {

                    client = ClientBuilder.newClient();
                    webTarget = client.target(getConfigurationProperty("skfs.cfg.property.mds.rootca.url"));

                    // Execute the method.
                    rs = webTarget.request().get();

                    if (rs.getStatus() > 299) {
                        System.err.println("Method failed: " + rs.readEntity(String.class));
                    } else {

                        CertificateFactory fac = CertificateFactory.getInstance("X509");
                        SKFSCommon.setMdsrootca((X509Certificate) fac.generateCertificate(rs.readEntity(InputStream.class)));
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    rs.close();
                    client.close();
                    // Release the connection.
                }
            }
            X509Certificate GSRootCert = SKFSCommon.getMdsrootca();

            X509Certificate EECERT = null;
            Client client = null;
            WebTarget webTarget;
            Response rs = null;
            String MDSJWTBlob = "", plaintext = "";
            String jwtsigningalgo = "";
            // 1 - FETCH MDS BLOB
            try {
                client = ClientBuilder.newClient();
                webTarget = client.target(SKFSCommon.getConfigurationProperty("skfs.cfg.property.mds.url"));

                rs = webTarget.request().get();

                if (rs.getStatus() > 299) {
                    System.err.println("Method failed: " + rs.readEntity(String.class));
                } else {
                    // Deal with the response.
                    MDSJWTBlob = rs.readEntity(String.class);
                }
            } catch (Exception e) {
                //throw error
                System.err.println("Fatal protocol violation: " + e.getMessage());
                e.printStackTrace();
            } finally {
                rs.close();
                client.close();
            }

            // 2 - DECODE JWT BLOB
            String[] jwtb64split = MDSJWTBlob.split("\\.");
            Base64.Decoder decoder = Base64.getUrlDecoder();
            JsonObject jwt = Json.createObjectBuilder()
                    .add("protected", SKFSCommon.getJsonObjectFromString(new String(decoder.decode(jwtb64split[0]), StandardCharsets.UTF_8)))
                    .add("payload", jwtb64split[1])
                    .add("signature", jwtb64split[2])
                    .build();

            try {

                jwtsigningalgo = jwt.getJsonObject("protected").getString("alg");
                if (jwtsigningalgo.equalsIgnoreCase("RS256")) {
                    jwtsigningalgo = "SHA256withRSA";
                }
                // Setup FIPS Provider
                Security.addProvider(new BouncyCastleFipsProvider());
                plaintext = new String(decoder.decode(jwt.getString("payload")), StandardCharsets.UTF_8);
                JsonArray x5carray = jwt.getJsonObject("protected").getJsonArray("x5c");
                CertificateFactory cf = CertificateFactory.getInstance("X.509");
                List<X509Certificate> certx = new ArrayList<>();
                for (int i = 0; i < x5carray.size(); i++) {
                    CertificateFactory fac = CertificateFactory.getInstance("X509");
                    String currentCertb64 = x5carray.getString(i);
                    InputStream is = new ByteArrayInputStream(Base64.getDecoder().decode(currentCertb64));
                    X509Certificate currentCert = (X509Certificate) fac.generateCertificate(is);
                    certx.add(currentCert);
                    if (i == 0) {
                        EECERT = currentCert;
                    }
                }
                certx.add(GSRootCert);

                CertPath path = cf.generateCertPath(certx);
                Set<TrustAnchor> trustAnchor = new HashSet<>();
                trustAnchor.add(new TrustAnchor(GSRootCert, null));

                CertPathValidator cpv = CertPathValidator.getInstance("PKIX");

                PKIXParameters pkix = new PKIXParameters(trustAnchor);

                pkix.setRevocationEnabled(false);

                pkix.setPolicyQualifiersRejected(true);
                pkix.setDate(new Date());
                CertPathValidatorResult cpvr = cpv.validate(path, pkix);
                if (cpvr != null) {
                    System.out.println("Certificate valid");
                } else {
                    //throw error
                    System.out.println("Certificate not valid");
                }
//            //verify payload signature
                byte[] rsbytes = decoder.decode(jwt.getString("signature"));
                Signature s = Signature.getInstance(jwtsigningalgo);
                s.initVerify(EECERT.getPublicKey());
                s.update((jwtb64split[0].concat(".").concat(jwtb64split[1])).getBytes(StandardCharsets.UTF_8));
//
                boolean verified = s.verify(rsbytes);
                if (!verified) {
                    System.out.println("Signature not valid");
                } else {
                    //throw error
                    System.out.println("Signature Verified!!");
                }
            } catch (Exception ex) {
                //throw error
                ex.printStackTrace();
            }

            // 4 - CONVERT TO JSON
            JsonObject MDSBlob = SKFSCommon.getJsonObjectFromString(plaintext);

            Boolean processMDSEntries = Boolean.FALSE;
            // 5- PARSE JSON
            FIDOMetadataService fidomds = new FIDOMetadataService();
            fidomds.setLegalHeader(MDSBlob.getString("legalHeader"));
            fidomds.setNo(MDSBlob.getInt("no"));
            fidomds.setNextUpdate(MDSBlob.getString("nextUpdate"));

            FIDOMetadataService fidomdsCommon = SKFSCommon.getMetadataservice();
            if (fidomdsCommon != null) {
                if (fidomds.getNo() > fidomdsCommon.getNo()) {
                    processMDSEntries = Boolean.TRUE;
                }
            } else {
                processMDSEntries = Boolean.TRUE;
            }

            if (processMDSEntries) {
                SKFSCommon.setMetadataservice(fidomds);

                JsonArray MDSEntriesArray = MDSBlob.getJsonArray("entries");
                int noofentries = MDSEntriesArray.size();
                //print no of entries
                for (int i = 0; i < noofentries; i++) {
                    JsonObject entry = MDSEntriesArray.getJsonObject(i);
                    if (entry.containsKey("aaguid")) {
                        String aaguid = entry.getString("aaguid");
                        SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINEST, classname, "execute", "FIDO-MSG-2001", "AAGUID added =" + aaguid);
                        SKFSCommon.setMdsentry(aaguid, entry);
                    }
                    if (entry.containsKey("attestationCertificateKeyIdentifiers")) {
                        JsonArray attcertarray = entry.getJsonArray("attestationCertificateKeyIdentifiers");
                        String attcertkey = Base64.getUrlEncoder().encodeToString(attcertarray.toString().getBytes());
                        for (int j = 0; j < attcertarray.size(); j++) {
                            String attcertentry = attcertarray.getString(j);
                            SKFSCommon.setMdsentrypointer(attcertentry, attcertkey);
                            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.FINEST, classname, "execute", "FIDO-MSG-2001", "CERT KEY added =" + attcertentry);
                        }
                        SKFSCommon.setMdsentry(attcertkey, entry);
                    }
                }
            }
        } else {
            SKFSLogger.logp(SKFSConstants.SKFE_LOGGER, Level.INFO, classname, "execute", "FIDO-MSG-3001", "");
        }

    }
}
