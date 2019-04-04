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
 * FIDO_KEYS table for MariaDB
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

create table IF NOT EXISTS FIDO_POLICIES (
        sid                             tinyint unsigned not null,
        did                             smallint unsigned not null,
        pid                             int unsigned not null,
        start_date                      DATETIME not null unique,
        end_date                        DATETIME,
        certificate_profile_name        varchar(64) not null,
        policy                          LONGTEXT not null,
        version                         int(11) not null,
        status                          enum('Active', 'Inactive') not null,
        notes                           varchar(512),
        create_date                     DATETIME not null,
        modify_date                     DATETIME,
        signature                       varchar(2048),
                primary key (sid, did, pid),
                unique index (sid, did, pid, start_date),
                unique index (did, certificate_profile_name)
        )
        engine=innodb;

/* EOF */
