/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skce.utilities;

public class skceConstants {

    public static final String SKFE_LOGGER = "SKFE";
    public static final String SKSE_LOGGER = "SKSE";
    public static final String LDAPE_LOGGER = "LDAPE";
    public static final String SKEE_LOGGER = "SKEE";

    public static final String DC = "DC";

    /**
     * Datatype min max values
     */
    public static final int TINYINT_MAX = 127;
    public static final Long BIGINT_MAX = 9223372036854775807L;
    public static final int INT_MAX = 2147483647;

    /**
     * Cloud credential keys
     */
    public static final String CLOUDCRED_NAME = "cloudname";
    public static final String CLOUDCRED_ACCESSKEY = "accesskey";
    public static final String CLOUDCRED_SECRETKEY = "secretkey";

    // Cloud types allowed
    public static final String CLOUDTYPE_EUCALYPTUS = "Eucalyptus";
    public static final String CLOUDTYPE_AWS = "AWS";
    public static final String CLOUDTYPE_AZURE = "Azure";

    /**
     * Encryption algorithm names
     */
    public static final String AES = "AES";
    public static final String DESEDE = "DESede";

    // XML Signature types
    public static final int XML_SIGNATURE_ENVELOPING = 0;  // Default
    public static final int XML_SIGNATURE_ENVELOPED = 1;
    public static final int XML_SIGNATURE_DETACHED = 2;

    /**
     * Encryption key retrieval methods
     */
    public static final String KEY_RETRIEVAL_METHOD_XMLFILE = "xmlfile";
    public static final String KEY_RETRIEVAL_METHOD_PROPFILE = "propertiesfile";
    public static final String KEY_RETRIEVAL_METHOD_BOTH = "both";

    /**
     * Encrypted file extensions.
     */
    public static final String FILE_EXTENSION_CIPHERTEXT = ".ciphertext";
    public static final String FILE_EXTENSION_XML = ".xml";
    public static final String FILE_EXTENSION_ZENC = ".zenc";

    /**
     * Domain configuration options
     */
    public static final String DOMAINCONFIG_GUI = "gui";
    public static final String DOMAINCONFIG_PROPFILE = "propertiesfile";

    //HASH map type constances
    public static final int MAP_USER_SESSION_INFO = 1;
    public static final int MAP_FIDO_SECRET_KEY = 2;
//    public static final int MAP_USER_KEY_POINTERS = 3;
    public static final int MAP_FIDO_KEYS = 4;
    public static final int MAP_FIDO_POLICIES = 5;
    public static final int MAP_FIDO_MDS = 6;

    /**
     * Various LDAP user roles logically linked to groups in LDAP
     */
    public static final String LDAP_ROLE_ADM = "ADM";       //  'cn=AdminAuthorized'
    public static final String LDAP_ROLE_SRV = "SRV";       //  'cn=Services'
    public static final String LDAP_ROLE_ENC = "ENC";       //  'cn=EncryptionAuthorized'
    public static final String LDAP_ROLE_DEC = "DEC";       //  'cn=DecryptionAuthorized'
    public static final String LDAP_ROLE_CMV = "CMV";       //  'cn=CloudMoveAuthorized'
    public static final String LDAP_ROLE_LOADKEY = "LDKY";  //  'cn=LoadAuthorized'
    public static final String LDAP_ROLE_SIGN = "SIGN";     //  'cn=SignAuthorized'
    public static final String LDAP_ROLE_REMOVEKEY = "RMKY"; //  'cn=RemoveAuthorized'
    public static final String LDAP_ROLE_FIDO = "FIDO"; //  'cn=FidoAuthorized'
    public static final String LDAP_ROLE_FIDO_REG = "FIDOREG"; //  'cn=FidoRegAuthorized'
    public static final String LDAP_ROLE_FIDO_SIGN = "FIDOSIGN"; //  'cn=FidoSignAuthorized'
    public static final String LDAP_ROLE_FIDO_AUTHZ = "FIDOAUTHZ"; //  'cn=FidoAuthzAuthorized'
    public static final String LDAP_ROLE_FIDO_ADMIN = "FIDOADM"; //  'cn=FidoAdminAuthorized'
    public static final String LDAP_ROLE_FIDO_POLICY_MANAGEMENT = "FIDOPOLICYMGT"; //  'cn=FidoAdminAuthorized'
    public static final String LDAP_ROLE_FIDO_MONITOR = "FIDOMONITOR"; //  'cn=FidoAdminAuthorized'

    /**
     * *********************************************************************
     */
    /**
     * Parameter for ZMQ service when its starting
     */
    public static final int ZMQ_SERVICE_STARTING = 0;

    /**
     * Parameter for ZMQ service when its running
     */
    public static final int ZMQ_SERVICE_RUNNING = 1;

    /**
     * Parameter for ZMQ service when its shutting down
     */
    public static final int ZMQ_SERVICE_STOPPING = 2;

    /**
     * Parameter for ZMQ service when its stopped
     */
    public static final int ZMQ_SERVICE_STOPPED = 3;

    /**
     * Parameter for ZMQ service when its inactive
     */
    public static final int ZMQ_SERVICE_INACTIVE = 4;

    /**
     * LDAP attribute keys to fetch metadata of a user
     */
    public static final String LDAP_ATTR_KEY_SURNAME = "sn";
    public static final String LDAP_ATTR_KEY_FNAME = "givenName";
    public static final String LDAP_ATTR_KEY_UID = "uid";
    public static final String LDAP_ATTR_KEY_COMMONNAME = "cn";
    public static final String LDAP_ATTR_KEY_DN = "dn";
    public static final String LDAP_ATTR_KEY_EMAILADDRESSES = "RegisteredEmailAddresses";
    public static final String LDAP_ATTR_KEY_PRIMARYEMAIL = "PrimaryEmail";
    public static final String LDAP_ATTR_KEY_PHONENUMBERS = "RegisteredPhoneNumbers";
    public static final String LDAP_ATTR_KEY_PRIMARYPHONE = "PrimaryPhone";
    public static final String LDAP_ATTR_KEY_DEFAULTTARGET = "Defaulttarget";
    public static final String LDAP_ATTR_KEY_FIDOENABLED = "FIDOKeysEnabled";
    public static final String LDAP_ATTR_KEY_2STEPVERIFY = "TwoStepVerification";
    public static final String LDAP_ATTR_KEY_DOMAINID = "did";
}
