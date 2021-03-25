/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.appliance.utilities;

public class applianceConstants {

    // Loggers
    public static final String APPLIANCE_LOGGER = "APPL";

    public static final String KA_LOGGER = "KEYAPPLIANCE";
    public static final String REPL_LOGGER = "REPL";

    public static final String GENERIC_LOGGER = "GEN";

    /**
     * Parameter for UNKNOWN Modules
     */
    public static final int MODULE_TYPE_UNKNOWN = 0;

    /**
     * Parameter for Module SAKA
     */
    public static final int MODULE_TYPE_SAKA = 1;

    /**
     * Parameter for Module SKCE
     */
    public static final int MODULE_TYPE_SKCE = 2;

    /**
     * Parameter for Module CDO
     */
    public static final int MODULE_TYPE_CDO = 3;

    /**
     * Parameter for Module FSO
     */
    public static final int MODULE_TYPE_FSO = 4;

    /**
     * Command to disconnect a Domain Administrator's Console session from the
     * SKLES service
     */
    public static final int DAC_COMMAND_DISCONNECT = 0;

    /**
     * Command to get data about an encryption domain from the Domains entity
     */
    public static final int DAC_COMMAND_GET_DOMAIN_INFO = 1;

    /**
     * Command to update the status of an encryption domain in the Domains
     * entity
     */
    public static final int DAC_COMMAND_SET_DOMAIN_INFO = 2;

    /**
     * Command to get the current list of users within a domain
     */
    public static final int DAC_COMMAND_GET_USER_LIST = 3;

    /**
     * Command to get data about a specific user
     */
    public static final int DAC_COMMAND_GET_USERINFO = 4;

    /**
     * Command to update user information
     */
    public static final int DAC_COMMAND_SET_USER_INFO = 5;

    /**
     * Command to add a new user to a domain
     */
    public static final int DAC_COMMAND_ADD_USER = 6;

    /**
     * Command to delete an existing user from a domain
     */
    public static final int DAC_COMMAND_DELETE_USER = 7;

    /**
     * Command to run the delete-encryption-requests cron job, once
     */
    public static final int DAC_COMMAND_RUN_DELETE_REQUESTS_JOB = 8;

    /**
     * Command to cancel the scheduled delete-encryption-requests cron job
     */
    public static final int DAC_COMMAND_CANCEL_DELETE_REQUESTS_JOB = 9;

    /**
     * Command to schedule the delete-encryption-requests job
     */
    public static final int DAC_COMMAND_SCHEDULE_DELETE_REQUESTS_JOB = 10;

    /**
     * Command to get information about the delete-encryption-requests job
     */
    public static final int DAC_COMMAND_GET_DELETE_REQUESTS_JOB_INFO = 11;

    /**
     * Command to run the HMAC-rotation cron job, once
     */
    public static final int DAC_COMMAND_RUN_HMAC_ROTATION_JOB = 12;

    /**
     * Command to cancel the scheduled HMAC-rotation cron job
     */
    public static final int DAC_COMMAND_CANCEL_HMAC_ROTATION_JOB = 13;

    /**
     * Command to schedule the HMAC-rotation job
     */
    public static final int DAC_COMMAND_SCHEDULE_HMAC_ROTATION_JOB = 14;

    /**
     * Command to get information about the HMAC-rotation job
     */
    public static final int DAC_COMMAND_GET_HMAC_ROTATION_JOB_INFO = 15;

    /**
     * Command to run the key-rotation cron job, once
     */
    public static final int DAC_COMMAND_RUN_KEY_ROTATION_JOB = 16;

    /**
     * Command to cancel the scheduled key-rotation cron job
     */
    public static final int DAC_COMMAND_CANCEL_KEY_ROTATION_JOB = 17;

    /**
     * Command to schedule the key-rotation job
     */
    public static final int DAC_COMMAND_SCHEDULE_KEY_ROTATION_JOB = 18;

    /**
     * Command to get information about the key-rotation job
     */
    public static final int DAC_COMMAND_GET_KEY_ROTATION_JOB_INFO = 19;

    /**
     * Command to run the key-rotation job for a range of tokens, once
     */
    public static final int DAC_COMMAND_RUN_KEY_ROTATION_RANGE_JOB = 20;

    /**
     * Command to cancel the scheduled key-rotation job for a range of tokens
     */
    public static final int DAC_COMMAND_CANCEL_KEY_ROTATION_RANGE_JOB = 21;

    /**
     * Command to schedule the key-rotation job for a range of tokens,
     */
    public static final int DAC_COMMAND_SCHEDULE_KEY_ROTATION_RANGE_JOB = 22;

    /**
     * Command to get information about the key-rotation job for a range of
     * tokens
     */
    public static final int DAC_COMMAND_GET_KEY_ROTATION_JOB_RANGE_INFO = 23;

    /**
     * Command to get preliminary information to schedule range rotation job
     */
    public static final int DAC_COMMAND_GET_KEY_ROTATION_JOB_RANGE_SCHEDULING_INFO = 24;

    /**
     * Command to run the pseudo-number job, once
     */
    public static final int DAC_COMMAND_RUN_PSEUDO_NUMBER_JOB = 25;

    /**
     * Command to get information about all jobs
     */
    public static final int DAC_COMMAND_GET_ALL_JOBS_INFO = 26;

    /**
     * Command to get configuration properties of a domain
     */
    public static final int DAC_COMMAND_GET_DOMAIN_CONFIGURATION = 27;

    /**
     * Command to get the immutable configuration properties of a domain
     */
    public static final int DAC_COMMAND_GET_DOMAIN_CONFIGURATION_IMMUTABLE = 28;

    /**
     * Command to get the mutable configuration properties of a domain
     */
    public static final int DAC_COMMAND_GET_DOMAIN_CONFIGURATION_MUTABLE = 29;

    /**
     * Command to update configuration properties of a domain
     */
    public static final int DAC_COMMAND_SET_DOMAIN_CONFIGURATION = 30;

    /**
     * Command to refresh counter values of a domain
     */
    public static final int DAC_COMMAND_REFRESH_DOMAIN_COUNTERS = 31;

    /**
     * Size of the random bytes for a DAC nonce
     */
    public static final int DAC_NONCE_BYTES_SIZE = 32;

    /**
     * Get Server information
     */
    public static final int DAC_COMMAND_GET_SERVER_INFO = 33;

    /**
     * Set Server information
     */
    public static final int DAC_COMMAND_SET_SERVER_INFO = 34;

    /**
     * Get ServerDomain information
     */
    public static final int DAC_COMMAND_GET_SERVER_DOMAIN_INFO = 35;

    /**
     * Set ServerDomain information
     */
    public static final int DAC_COMMAND_SET_SERVER_DOMAIN_INFO = 36;

    /**
     * Get Relay information
     */
    public static final int DAC_COMMAND_GET_RELAY_INFO = 37;

    /**
     * Set Relay information
     */
    public static final int DAC_COMMAND_SET_RELAY_INFO = 38;

    /**
     * Command to restart all replication threads on a server
     */
    public static final int DAC_COMMAND_RESTART_REPLICATION = 39;

    /**
     * Command to restart Publisher on a server
     */
    public static final int DAC_COMMAND_RESTART_PUBLISHER = 40;

    /**
     * Command to restart Subscriber on a server
     */
    public static final int DAC_COMMAND_RESTART_SUBSCRIBER = 41;

    /**
     * Command to restart Acknowledger on a server
     */
    public static final int DAC_COMMAND_RESTART_ACKNOWLEDGER = 42;

    /**
     * Command to restart Backlog Processor on a server
     */
    public static final int DAC_COMMAND_RESTART_BACKLOG_PROCESSOR = 43;

    /**
     * *********************************************************************
     */
    /**
     * Parameter for All Users in the command to get users
     */
    public static final int DAC_PARAM_USER_ALL_USERS = 0;

    /**
     * Parameter for All Administrators in the command to get users
     */
    public static final int DAC_PARAM_USER_ALL_ADMINISTRATORS = 1;

    /**
     * Parameter for all users who are authorized for Encryption
     */
    public static final int DAC_PARAM_USER_ENCRYPTION = 2;

    /**
     * Parameter for all users who are authorized for Decryption
     */
    public static final int DAC_PARAM_USER_DECRYPTION = 3;

    /**
     * Parameter for all users who are authorized for Encryption & Decryption
     */
    public static final int DAC_PARAM_USER_ENCRYPTION_AND_DECRYPTION = 4;

    /**
     * Parameter for a specific user in the command to get users
     */
    public static final int DAC_PARAM_USER_SPECIFIC_USER = 5;

    /**
     * Parameter for users like the specified username
     */
    public static final int DAC_PARAM_USER_LIKE_USER = 6;

    /**
     * Parameter for all users who are NOT authorized for any operation
     */
    public static final int DAC_PARAM_USER_NO_OPERATION = 7;

    /**
     * Parameter for all users who are authorized for Deletion
     */
    public static final int DAC_PARAM_USER_DELETION = 8;

    /**
     * Parameter for all users who are authorized for relaying payments
     */
    public static final int DAC_PARAM_USER_RELAY = 9;

    /**
     * Parameter for all users who are authorized for Search
     */
    public static final int DAC_PARAM_USER_SEARCH = 10;

    /**
     * Parameter for all users who are authorized for Encryption, Decryption &
     * Deletion
     */
    public static final int DAC_PARAM_USER_ENCRYPTION_DECRYPTION_AND_DELETION = 11;

    /**
     * Parameter for all users who are authorized for Encryption, Decryption,
     * Deletion & Search
     */
    public static final int DAC_PARAM_USER_ENCRYPTION_DECRYPTION_DELETION_AND_SEARCH = 12;

    /**
     * *********************************************************************
     */
    /**
     * *********************************************************************
     */
    /**
     * Parameter for BatchRequests entity bean
     */
    public static final int ENTITY_TYPE_BATCH_REQUESTS = 0;

    /**
     * Parameter for Configurations entity bean
     */
    public static final int ENTITY_TYPE_CONFIGURATIONS = 1;

    /**
     * Parameter for DecryptionRequests entity bean
     */
    public static final int ENTITY_TYPE_DECRYPTION_REQUESTS = 2;

    /**
     * Parameter for DeletionRequests entity bean
     */
    public static final int ENTITY_TYPE_DELETION_REQUESTS = 3;

    /**
     * Parameter for Domains entity bean
     */
    public static final int ENTITY_TYPE_DOMAINS = 4;

    /**
     * Parameter for EncryptionRequests entity bean
     */
    public static final int ENTITY_TYPE_ENCRYPTION_REQUESTS = 5;

    /**
     * Parameter for Jobs entity bean
     */
    public static final int ENTITY_TYPE_JOBS = 6;

    /**
     * Parameter for KeyCustodians entity bean
     */
    public static final int ENTITY_TYPE_KEY_CUSTODIANS = 7;

    /**
     * Parameter for RelayRequests entity bean
     */
    public static final int ENTITY_TYPE_RELAY_REQUESTS = 8;

    /**
     * Parameter for Replication bean
     */
    public static final int ENTITY_TYPE_REPLICATION = 9;

    /**
     * Parameter for SearchRequests entity bean
     */
    public static final int ENTITY_TYPE_SEARCH_REQUESTS = 10;

    /**
     * Parameter for Servers entity bean
     */
    public static final int ENTITY_TYPE_SERVERS = 11;

    /**
     * Parameter for ServerDomains entity bean
     */
    public static final int ENTITY_TYPE_SERVER_DOMAINS = 12;

    /**
     * Parameter for SymmetricKeys entity bean
     */
    public static final int ENTITY_TYPE_SYMMETRIC_KEYS = 13;

    /**
     * Parameter for Users entity bean
     */
    public static final int ENTITY_TYPE_USERS = 14;

    /**
     * Parameter for KeepAlive dummy object to keep ZMQ alive
     */
    public static final int ENTITY_TYPE_KEEP_ALIVE = 15;

    /**
     * Parameter for AnsiX9241Key entity bean
     */
    public static final int ENTITY_TYPE_ANSI_X9241_KEY = 16;

    /**
     * Parameter for KeepAlive dummy object to keep ZMQ alive
     */
    public static final int ENTITY_TYPE_SAKA_UPPER_LIMIT = 49;

    /**
     * Lower limit for SKCE Entity Types
     */
    public static final int ENTITY_TYPE_CDO_LOWER_LIMIT = 50;
    /**
     * Upper limit for SKCE Entity Types
     */
    public static final int ENTITY_TYPE_CDO_UPPER_LIMIT = 99;

    /**
     * Lower limit for CDO Entity Types
     */
    public static final int ENTITY_TYPE_SKCE_LOWER_LIMIT = 100;

    /**
     * Parameter for Fido Keys entity bean
     */
    public static final int ENTITY_TYPE_FIDO_KEYS = 101;

    /**
     * Parameter for Fido Users entity bean
     */
    public static final int ENTITY_TYPE_FIDO_USERS = 102;

    /**
     * Parameter for Fido Attestation Certificates entity bean
     */
    public static final int ENTITY_TYPE_ATTESTATION_CERTIFICATES = 103;

    /**
     * Parameter for User Session Info SKCE Hashmap
     */
    public static final int ENTITY_TYPE_MAP_USER_SESSION_INFO = 104;

    /**
     * Parameter for Fido Policies entity bean
     */
    public static final int ENTITY_TYPE_FIDO_POLICIES = 105;
    /**
     * Parameter for Fido CONFIGURATIONS entity bean
     */
    public static final int ENTITY_TYPE_FIDO_CONFIGURATIONS = 106;

//    /**
//     * Lower limit for CDO Entity Types
//     */
//    public static final int ENTITY_TYPE_DOMAINS = 121;
    /**
     * Upper limit for CDO Entity Types
     */
    public static final int ENTITY_TYPE_SKCE_UPPER_LIMIT = 124;

    //**********************************************************
    /**
     * Lower limit for CDO Entity Types
     */
    public static final int ENTITY_TYPE_FSO_LOWER_LIMIT = 125;

    public static final int ENTITY_TYPE_COMPANIES = 126;

    public static final int ENTITY_TYPE_USER_APPL_AUTHORIZATIONS = 127;

    public static final int ENTITY_TYPE_APPLICATIONS = 128;

    public static final int ENTITY_TYPE_FSO_USERS = 129;

    public static final int ENTITY_TYPE_APPLICATION_LOGIN_FIELDS = 130;

    public static final int ENTITY_TYPE_SAFT_FILE_INFO = 131;

    public static final int ENTITY_TYPE_SAFT_ACTIVE_REQUESTS = 132;

    public static final int ENTITY_TYPE_SAFT_SUCCESSFUL_REQUESTS = 133;

    public static final int ENTITY_TYPE_SAFT_FAILED_REQUESTS = 134;

    public static final int ENTITY_TYPE_NOTIFICATION_REQUESTS = 135;

    public static final int ENTITY_TYPE_MANAGED_GROUPS = 136;

    public static final int ENTITY_TYPE_UNREGISTERED_ACCOUNTS_INFO = 137;

    public static final int ENTITY_TYPE_FSO_CONFIGURATIONS = 138;

    public static final int ENTITY_TYPE_OTP_INFO = 139;

    public static final int ENTITY_TYPE_FSO_INVITATIONS = 140;

    public static final int ENTITY_TYPE_COMPANY_KEYS = 141;

    public static final int ENTITY_TYPE_GLOBAL_PASSWORD = 142;

    public static final int ENTITY_TYPE_CERTIFICATE_AUTHORITY = 151;
    public static final int ENTITY_TYPE_CA_CERTIFICATES = 152;
    public static final int ENTITY_TYPE_USER_ACCOUNTS = 153;
    public static final int ENTITY_TYPE_DIGITAL_CERTIFICATE_ORDERS = 154;
    public static final int ENTITY_TYPE_DCO_AUTHORIZATIONS = 155;
    public static final int ENTITY_TYPE_SAN_HOSTS = 156;
    public static final int ENTITY_TYPE_FQDNS = 157;
    public static final int ENTITY_TYPE_FQDN_KEY_PAIRS = 158;
    public static final int ENTITY_TYPE_FQDN_CONTACTS = 159;
//    public static final int ENTITY_TYPE_JOBS = 160;
    public static final int ENTITY_TYPE_SCM_AGENTS = 161;
    public static final int ENTITY_TYPE_SCM_AGENT_CONFIGURATION = 162;
    public static final int ENTITY_TYPE_SCM_AGENT_LOGS = 163;
    public static final int ENTITY_TYPE_SCM_AGENT_NONCE = 164;
    public static final int ENTITY_TYPE_LOAD_BALANCERS = 165;
    public static final int ENTITY_TYPE_CREDENTIALS = 166;
    public static final int ENTITY_TYPE_CERTIFICATE_RESOURCE_NAMES = 167;

    /**
     * Parameter for KeepAlive dummy object to keep ZMQ alive
     */
    public static final int ENTITY_TYPE_FSO_UPPER_LIMIT = 174;
    //**********************************************************

    /**
     * *********************************************************************
     */
    /**
     * Parameter for ADD replication operation
     */
    public static final int REPLICATION_OPERATION_ADD = 0;

    /**
     * Parameter for DELETE replication operation
     */
    public static final int REPLICATION_OPERATION_DELETE = 1;

    /**
     * Parameter for UPDATE replication operation
     */
    public static final int REPLICATION_OPERATION_UPDATE = 2;

    /**
     * Parameter for KEEP_ALIVE replication operation for KeepAlive object
     */
    public static final int REPLICATION_OPERATION_KEEP_ALIVE = 3;

    /**
     * Parameter for HASHMAP replication for ADD operation
     */
    public static final int REPLICATION_OPERATION_HASHMAP_ADD = 4;

    /**
     * Parameter for HASHMAP replication for DELETE operation
     */
    public static final int REPLICATION_OPERATION_HASHMAP_DELETE = 5;

    /**
     * Parameter for HASHMAP replication for UPDATE operation
     */
    public static final int REPLICATION_OPERATION_HASHMAP_UPDATE = 6;

    /**
     * *********************************************************************
     */
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
     * *********************************************************************
     */
    /**
     * Parameter for Card Reader Types and ATMs
     */
    public static final int CCS_READER_IDTECH = 0;
    public static final int CCS_READER_UIC = 1;
    public static final int CCS_READER_MAGTEK = 2;
    public static final int CCS_READER_INFINITE = 3;
    public static final int CCS_READER_DEJAVOO = 4;
    public static final int CCS_READER_PAX = 5;
    public static final int CCS_READER_PADV6 = 6;

    public static final int CCS_ATM_DEFAULT = 100;

    public static final String ACTIVE_STATUS = "Active";
    public static final String INACTIVE_STATUS = "Inactive";
    public static final String OTHER_STATUS = "Other";
//    public static final String ENABLED = "Enabled";
    //   public static final String DISABLED = "Disabled";
    public static final String DELETED = "Deleted";
    public static final String OTHER = "Other";

    //CCS
    /**
     * *********************************************************************
     */
    /**
     * Parameter for SAKA Operation
     */
    public static final int SAKA_OPERATION_ENCRYPT = 0;
    public static final int SAKA_OPERATION_DECRYPT = 1;
    public static final int SAKA_OPERATION_DELETE = 2;
    public static final int SAKA_OPERATION_SEARCH = 3;

    /**
     * *********************************************************************
     */
    /**
     * IDTECH swipe section lengths
     */
    public static final int IDTECH_SESSION_ID_LENGTH = 8;
    public static final int IDTECH_DEVICE_SERIAL_NUMBER_LENGTH = 10;
    public static final int IDTECH_KEY_SERIAL_NUMBER_LENGTH = 10;
    public static final int IDTECH_TRACK_1_HASHED_DATA_LENGTH = 20;
    public static final int IDTECH_TRACK_2_HASHED_DATA_LENGTH = 20;
    public static final int IDTECH_TRACK_3_HASHED_DATA_LENGTH = 20;
    public static final int PAN_LAST_FOUR = 4;

    /**
     * Mask symbol for masked track data
     */
    public static final String IDTECH_TRACK_MASK = "*";
    public static final String MAGTEK_TRACK_MASK = "0";
    public static final String PAX_TRACK_MASK = "*";
    public static final String DEJAVOO_TRACK_MASK = "*";

    /**
     * MagTek specific parsing constants
     */
    public static final String MAGTEK_FIELD_SEPARATOR = "|";
    public static final String MAGTEK_NAME_SEPARATOR = "^";

    /**
     * DEJAVOO specific parsing constants
     */
    public static final String DEJAVOO_FIELD_SEPARATOR = "|";

    /**
     * PAX specific parsing constants
     */
    public static final String PAX_FIELD_SEPARATOR = "|";

    /**
     * Byte positions in IDTECH_ENHANCED_DATA_ENCRYPTED_FORMAT of card-capture
     * data
     */
    public static final int IDTECH_EDEF_BYTE_STX = 0;
    public static final int IDTECH_EDEF_BYTE_LENGTH_LOWBYTE = 1;
    public static final int IDTECH_EDEF_BYTE_LENGTH_HIGHBYTE = 2;
    public static final int IDTECH_EDEF_BYTE_CARD_ENCODING = 3;
    public static final int IDTECH_EDEF_BYTE_TRACK_STATUS = 4;
    public static final int IDTECH_EDEF_BYTE_TRACK_1_PLAINTEXT_LENGTH = 5;
    public static final int IDTECH_EDEF_BYTE_TRACK_2_PLAINTEXT_LENGTH = 6;
    public static final int IDTECH_EDEF_BYTE_TRACK_3_PLAINTEXT_LENGTH = 7;
    public static final int IDTECH_EDEF_BYTE_FIELD_1 = 8;
    public static final int IDTECH_EDEF_BYTE_FIELD_2 = 9;

    /**
     * *********************************************************************
     */
    /**
     * Bit positions in IDTECH_ENHANCED_DATA_ENCRYPTED_FORMAT Track Status Byte
     * (IDTECH_EDEF_BYTE_TRACK_STATUS)
     */
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_0_TRACK_1_DECODING = 0;
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_1_TRACK_2_DECODING = 1;
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_2_TRACK_3_DECODING = 2;
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_3_TRACK_1_SAMPLING_DATA_PRESENT = 3;
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_4_TRACK_2_SAMPLING_DATA_PRESENT = 4;
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_5_TRACK_3_SAMPLING_DATA_PRESENT = 5;
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_6_RESERVED_FOR_FUTURE_USE = 6;
    public static final int IDTECH_EDEF_TRACK_STATUS_BIT_7_RESERVED_FOR_FUTURE_USE = 7;

    /**
     * *********************************************************************
     */
    /**
     * Bit positions in IDTECH_ENHANCED_DATA_ENCRYPTED_FORMAT Track Field 1 Byte
     * (IDTECH_EDEF_BYTE_FIELD_1)
     */
    public static final int IDTECH_EDEF_FIELD_1_BIT_0_TRACK_1_CLEAR_MASK_DATA_PRESENT = 0;
    public static final int IDTECH_EDEF_FIELD_1_BIT_1_TRACK_2_CLEAR_MASK_DATA_PRESENT = 1;
    public static final int IDTECH_EDEF_FIELD_1_BIT_2_TRACK_3_CLEAR_MASK_DATA_PRESENT = 2;
    public static final int IDTECH_EDEF_FIELD_1_BIT_3_RESERVED_FOR_FUTURE_USE = 3;
    public static final int IDTECH_EDEF_FIELD_1_BIT_4_ENRYPTION_ALGORITHM_LOWBIT = 4;
    public static final int IDTECH_EDEF_FIELD_1_BIT_5_ENRYPTION_ALGORITHM_HIGHBIT = 5;
    public static final int IDTECH_EDEF_FIELD_1_BIT_6_RESERVED_FOR_FUTURE_USE = 6;
    public static final int IDTECH_EDEF_FIELD_1_BIT_7_SERIAL_NUMBER_AVAILABLE = 7;

    /**
     * *********************************************************************
     */
    /**
     * Bit positions in IDTECH_ENHANCED_DATA_ENCRYPTED_FORMAT Track Field 2 Byte
     * (IDTECH_EDEF_BYTE_FIELD_2)
     */
    public static final int IDTECH_EDEF_FIELD_2_BIT_0_TRACK_1_ENCRYPTED_DATA_PRESENT = 0;
    public static final int IDTECH_EDEF_FIELD_2_BIT_1_TRACK_2_ENCRYPTED_DATA_PRESENT = 1;
    public static final int IDTECH_EDEF_FIELD_2_BIT_2_TRACK_3_ENCRYPTED_DATA_PRESENT = 2;
    public static final int IDTECH_EDEF_FIELD_2_BIT_3_TRACK_1_HASH_DATA_PRESENT = 3;
    public static final int IDTECH_EDEF_FIELD_2_BIT_4_TRACK_2_HASH_DATA_PRESENT = 4;
    public static final int IDTECH_EDEF_FIELD_2_BIT_5_TRACK_3_HASH_DATA_PRESENT = 5;
    public static final int IDTECH_EDEF_FIELD_2_BIT_6_SESSION_ID_PRESENT = 6;
    public static final int IDTECH_EDEF_FIELD_2_BIT_7_KEY_SERIAL_NUMBER_PRESENT = 7;

    /**
     * *********************************************************************
     */
    /**
     * Byte positions in ISO/IEC 7813 Track 1 and 2 Plaintext Masked Data
     */
    public static final String ISO_IEC_7813_TRACK_1_MASKED_DATA_START_SENTINEL = "%";
    public static final String ISO_IEC_7813_TRACK_1_MASKED_DATA_SEPARATOR = "^";
    public static final String ISO_IEC_7813_TRACK_1_MASKED_DATA_NAME_SEPARATOR = "/";
    public static final String ISO_IEC_7813_TRACK_1_MASKED_DATA_END_SENTINEL = "?";
    public static final int ISO_IEC_7813_TRACK_1_MASKED_DATA_MAX_LENGTH = 79;

    public static final String ISO_IEC_7813_TRACK_2_MASKED_DATA_START_SENTINEL = ";";
    public static final String ISO_IEC_7813_TRACK_2_MASKED_DATA_SEPARATOR = "=";
    public static final String ISO_IEC_7813_TRACK_2_MASKED_DATA_END_SENTINEL = "?";
    public static final int ISO_IEC_7813_TRACK_2_MASKED_DATA_MAX_LENGTH = 40;

    public static final int ISO_IEC_7813_TRACK_N_MASKED_DATA_MAX_PAN_DIGITS = 19;
    public static final int ISO_IEC_7813_TRACK_N_MASKED_DATA_MAX_ED_DIGITS = 4;
    public static final int ISO_IEC_7813_TRACK_N_MASKED_DATA_MAX_SC_DIGITS = 3;

    /**
     * *********************************************************************
     */
    public static final int ISO_IEC_7813_TRACK_1_MASKED_DATA_BYTE_STX = 0;
    public static final int ISO_IEC_7813_TRACK_1_MASKED_DATA_BYTE_FC = 1;
    public static final int ISO_IEC_7813_TRACK_1_MASKED_DATA_BYTE_PAN = 2;

    public static final int ISO_IEC_7813_TRACK_2_MASKED_DATA_BYTE_STX = 0;
    public static final int ISO_IEC_7813_TRACK_2_MASKED_DATA_BYTE_PAN = 1;

    /**
     * Byte positions in UIC MagString Reader card-capture data
     */
    public static final int UIC_BYTE_STX = 0;
    public static final int UIC_BYTE_TWO = 1;
    public static final int UIC_BYTE_LENGTH_HIGHBYTE = 2;
    public static final int UIC_BYTE_CARD_ENCODING = 3;
    public static final int UIC_BYTE_TRACK_STATUS = 4;
    public static final int UIC_BYTE_TRACK_1_PLAINTEXT_LENGTH = 5;
    public static final int UIC_BYTE_TRACK_2_PLAINTEXT_LENGTH = 6;
    public static final int UIC_BYTE_ALWAYS_ZERO = 7;
    public static final int UIC_BYTE_FIELD_1 = 8;
    public static final int UIC_BYTE_FIELD_2 = 9;

    /**
     * *********************************************************************
     */
    /**
     * Byte positions in Magtek Reader card-capture data
     */
    public static final int MAGTEK_BYTE_STX = 0;
    public static final int MAGTEK_BYTE_TWO = 1;

    public static final int RESPONSE_ENCODING_CBOR = 0;
    public static final int RESPONSE_ENCODING_JSON = 1;
    public static final int RESPONSE_ENCODING_XML = 2;

    /**
     * Parameter for CreditCardData from JAXB
     */
    public static final int ENTITY_TYPE_CC_DATA = 0;

    /**
     * Parameter for CreditCardDigest entity bean
     */
    public static final int ENTITY_TYPE_CC_DIGEST = 1;

    /**
     * Parameter for CreditCardEncryptedObject entity bean
     */
    public static final int ENTITY_TYPE_CC_ENCRYPTED_OBJECT = 2;

    /**
     * Parameter for CreditCardEncryptedObjects entity bean
     */
    public static final int ENTITY_TYPE_CC_DECRYPTED_OBJECT = 3;

    /**
     * Parameter for CreditCardEncryptedSymmetricKey entity bean
     */
    public static final int ENTITY_TYPE_CC_ENCRYPTED_SYMMETRIC_KEY = 4;

    /**
     * Parameter for a list of CreditCardEncryptedSymmetricKey entity bean
     */
    public static final int ENTITY_TYPE_CC_ENCRYPTED_SYMMETRIC_KEYS = 5;

    /**
     * Parameter for a Token
     */
    public static final int ENTITY_TYPE_TOKEN = 6;

    /**
     * Parameter for decrypted XML
     */
    public static final int ENTITY_TYPE_DECRYPTED_XML = 7;

    /**
     * Parameter for re-encrypted PIN
     */
    public static final int ENTITY_TYPE_REENCRYPTED_PIN = 8;

    /**
     * Parameter for a key check value
     */
    public static final int ENTITY_TYPE_KEY_CHECK_VALUE = 9;

    /**
     * Parameter for a key check value
     */
    public static final int ENTITY_TYPE_RSA_PUBLIC_KEY = 12;

    /**
     * *********************************************************************
     */
}
