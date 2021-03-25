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
package com.strongkey.FIDO2JWTVerify;


public class Main {

    private static final String usage = "\nUsage: java -jar FIDO2JWTVerify.jar  <did> <jwt> <username> <agent> <cip> <rpid> <jwt-truststore-location> <jwt-truststore-password> \n";
    
    private static String jwtpassword;
    private static String jwttruststorelocation;
    

    public static void main(String[] args) throws Exception {

        if (args.length < 8) {
            System.err.println(usage);
            return;
        }

        String did = args[0];
        String jwt = args[1];
        String username = args[2];
        String agent = args[3];
        String cip = args[4];
        jwtpassword = args[7];
        jwttruststorelocation = args[6];
        String rpid = args[5];
        Verify v = new Verify();
        System.out.println("JWT Verified = " + v.verify(did, jwt, username, agent, cip, jwtpassword, jwttruststorelocation, rpid));
    }
}
