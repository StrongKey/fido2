/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 * $Revision:
 * $Author: mishimoto $
 * $URL:
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
 *
 *
 */
package com.strongauth.fido.fido2.rest.tests;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

public class JsonSchemaTests {

    private final String minimalPolicy = "{\n" +
        "	\"cryptography\": {\n" +
        "		\"ec_enabled\": true,\n" +
        "		\"elliptic_curves\": [\"secp256r1\"],\n" +
        "		\"allowed_ec_signatures\": [\"ecdsa-p256-sha256\"],\n" +
        "		\"attestation_formats\": [\"none\"],\n" +
        "		\"attestation_types\": [\"none\"]\n" +
        "	},\n" +
        "	\"rp\": {\n" +
        "		\"name\": \"demo.strongkey.com\"\n" +
        "	},\n" +
        "	\"mds\": {\n" +
        "		\"endpoints\": [\"demo.strongkey.com/mds\"],\n" +
        "		\"certificatation\": [\"FIDO_CERTIFIED\"]\n" +
        "	},\n" +
        "	\"counter\": {\n" +
        "		\"requireCounter\": true,\n" +
        "		\"requireIncrease\": true\n" +
        "	},\n" +
        "	\"userSettings\": true,\n" +
        "	\"storeSignatures\": false,\n" +
        "	\"registration\": {\n" +
        "		\"displayName\": \"required\",\n" +
        "		\"excludeCredentials\": \"enabled\",\n" +
        "		\"attestation\": \"none\"\n" +
        "	},\n" +
        "	\"authentication\": {\n" +
        "		\"userVerification\": \"preferred\"\n" +
        "	}\n" +
        "}";

    private final String fullerPolicy = "{\n" +
        "	\"cryptography\": {\n" +
        "		\"ec_enabled\": true,\n" +
        "		\"elliptic_curves\": [\"secp256r1\"],\n" +
        "		\"allowed_ec_signatures\": [\"ecdsa-p256-sha256\"],\n" +
        "		\"attestation_formats\": [\"none\"],\n" +
        "		\"attestation_types\": [\"none\"]\n" +
        "	},\n" +
        "	\"rp\": {\n" +
        "		\"name\": \"demo.strongkey.com\"\n" +
        "	},\n" +
        "	\"mds\": {\n" +
        "		\"endpoints\": [\"demo.strongkey.com/mds\"],\n" +
        "		\"certificatation\": [\"FIDO_CERTIFIED\"]\n" +
        "	},\n" +
        "	\"counter\": {\n" +
        "		\"requireCounter\": true,\n" +
        "		\"requireIncrease\": true\n" +
        "	},\n" +
        "	\"userSettings\": true,\n" +
        "	\"storeSignatures\": false,\n" +
        "	\"registration\": {\n" +
        "		\"displayName\": \"required\",\n" +
        "		\"excludeCredentials\": \"enabled\",\n" +
        "		\"attestation\": \"none\"\n" +
        "	},\n" +
        "	\"authentication\": {\n" +
        "		\"userVerification\": \"preferred\"\n" +
        "	}\n" +
        "}";

    /*
     *********************************************************************
     *                                      888    d8b
     *                                      888    Y8P
     *                                      888
     *  88888b.   .d88b.   .d88b.   8888b.  888888 888 888  888  .d88b.
     *  888 "88b d8P  Y8b d88P"88b     "88b 888    888 888  888 d8P  Y8b
     *  888  888 88888888 888  888 .d888888 888    888 Y88  88P 88888888
     *  888  888 Y8b.     Y88b 888 888  888 Y88b.  888  Y8bd8P  Y8b.
     *  888  888  "Y8888   "Y88888 "Y888888  "Y888 888   Y88P    "Y8888
     *                         888
     *                    Y8b d88P
     *                     "Y88P"
     *********************************************************************/
    @Test
    public void voidargs () {

    }

    /*
     *********************************************************************
     *                              d8b 888    d8b
     *                              Y8P 888    Y8P
     *                                  888
     *   88888b.   .d88b.  .d8888b  888 888888 888 888  888  .d88b.
     *   888 "88b d88""88b 88K      888 888    888 888  888 d8P  Y8b
     *   888  888 888  888 "Y8888b. 888 888    888 Y88  88P 88888888
     *   888 d88P Y88..88P      X88 888 Y88b.  888  Y8bd8P  Y8b.
     *   88888P"   "Y88P"   88888P' 888  "Y888 888   Y88P    "Y8888
     *   888
     *   888
     *   888
     *********************************************************************/
    @Test
    public void minimalFidoPolicyShouldVerify() {

    }
}
