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

create table IF NOT EXISTS attestation_certificates (
        sid             tinyint(4) NOT NULL,
        did             smallint(5) NOT NULL,
        attcid          mediumint(20) NOT NULL,
        parent_sid      tinyint(4) NULL,
        parent_did      smallint(5) NULL,
        parent_attcid   mediumint(20) NULL,
        certificate     LONGTEXT NOT NULL,
        issuer_dn       varchar(1024) NOT NULL,
        subject_dn      varchar(1024) NOT NULL,
        serial_number   varchar(512) NOT NULL,
        signature       varchar(2048) NULL,
                primary key(sid, did, attcid)
        )
        engine=innodb;

/* EOF */
