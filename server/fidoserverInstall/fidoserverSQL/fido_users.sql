/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 *
 * FIDO_USERS table for MariaDB
 *
 * Contains encrypted symmetric keys used with the ANSI X9.24-1
 * Derived Unique Key Per Transaction (DUKPT) protocol.  Keys are
 * encrypted and stored in the SYMMETRIC_KEYS table and assigned
 * a unique token (stored here as keytoken).  To use this key, it
 * must first be decrypted using the decrypt webservice operation
 * and its KCV must be verified to ensure it was decrypted correctly
 * before its used.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 */


create table IF NOT EXISTS fido_users (
        sid                      tinyint NOT NULL DEFAULT 1,
        did                      tinyint NOT NULL DEFAULT 1,
        username                 varchar(256) NOT NULL,
        userdn                   varchar(2048) NULL,
        fido_keys_enabled        ENUM('true','false') NULL,
        two_step_verification    ENUM('true','false') NULL,
        primary_email            varchar(256) NULL,
        registered_emails        varchar(2048) NULL,
        primary_phone_number     varchar(32) NULL,
        registered_phone_numbers varchar(2048) NULL,
        two_step_target          ENUM('email','phone') NULL,
        status                   ENUM('Active','Inactive') NOT NULL,
        signature                VARCHAR(2048) NULL,
                primary key(sid,did,username)
        )
        engine=innodb;

/* EOF */
