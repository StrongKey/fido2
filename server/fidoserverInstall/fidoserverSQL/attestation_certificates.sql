/*
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
 * Copyright (c) 2001-2019 StrongAuth, Inc.  
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
