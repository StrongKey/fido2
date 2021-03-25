/*
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 *
 * USERS table for MariaDB
 *
 * The table is a list of users in the StrongKey Android Client Library (SACL)
 * FIDO App eCommerce (SFAECO) module.
 *
 * A distinction to note is that uid in this table is merely a numerical
 * id that uniquely identifies a human user in this cryptographic domain 
 * (identified by a unique did). However, credentialid is a FIDO Authenticator
 * generated UUID-like string that is, usually, not visible to the human, but
 * used by FIDO applications to uniquely identify users within the system.
 *
 */

create table USERS (
        did                         smallint(5) unsigned not null,
        sid                         tinyint unsigned not null,
        uid                         bigint unsigned not null,
        username                    varchar(32) not null,
        credentialid                varchar(64),
        given_name                  varchar(512) not null,
        family_name                 varchar(64) not null,
        email_address               varchar(128) not null,
        mobile_number               varchar(32) not null,
        enrollment_date             datetime,
        status                      enum('Active', 'Inactive', 'Registered', 'Other') not null,
        create_date                 datetime not null default current_timestamp,
        modify_date                 datetime,
        notes                       varchar(2048),
        signature                   varchar(2048),
	primary key (did, sid, uid),
        unique index (did, username),
        unique index (did, email_address),
        unique index (did, mobile_number)
    )
    engine=innodb;

/* EOF */

