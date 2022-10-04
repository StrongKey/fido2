package com.strongkey.FIDO2SSOVerify;
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

import com.strongkey.JWTVerify.Verify;
import com.strongkey.SAMLVerify.ValidateSAMLResponse;

public class Main {

    private static final String usage = 
              "\nUsage: java -jar FIDO2SSOVerify.jar <SSO-TYPE>"
            + "\n    Accepted Values:"
            + "\n        SSO-TYPE : Which type of SSO is being verified/validated? Values: JWT | SAML";
    private static final String JWTUsage = "\nJWT Verify Usage: java -jar FIDO2SSOVerify.jar JWT <did> <jwt> <username> <agent> <cip> <rpid> <jwt-truststore-location> <jwt-truststore-password> \n";
    private static final String SAMLUsage = "\nSAML Verify Usage: java -jar FIDO2SSOVerify.jar SAML <base64-encoded-saml-assertion> \n";
    
    

    public static void main(String[] args) throws Exception {
        
        if (args.length == 0) {
            System.out.println(usage);
            return;
        }
        
        switch (args[0].toUpperCase()) {
            case "JWT": 
                if (args.length < 9) {
                    System.out.println(JWTUsage);
                } else {
                    String did = args[1];
                    String jwt = args[2];
                    String username = args[3];
                    String agent = args[4];
                    String cip = args[5];
                    String rpid = args[6];
                    String jwttruststorelocation = args[7];
                    String jwtpassword = args[8];
                    Verify v = new Verify();
                    System.out.println("JWT Verified = " + v.verify(did, jwt, username, agent, cip, jwtpassword, jwttruststorelocation, rpid));
                }
                break;
            case "SAML": 
                if (args.length < 2) {
                    System.out.println(SAMLUsage);
                } else {
                    String saml = args[1];
                    ValidateSAMLResponse v = new ValidateSAMLResponse();
                    System.out.println("SAML Verified = " + v.validate(saml));
                }
                break;
            default: 
            
        }
    }
}
