/**
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
 * MERCHANTS table for MariaDB
 *
 * Table to just track the RP's name for dynamic linking in the
 * transaction instead of hard-coding it.
 *
 */

create table MERCHANTS (
        did                         smallint(5) unsigned not null,
        merchant_id                 smallint(5) unsigned not null,
        merchant_name               varchar(256) not null,
        create_date                 datetime not null default current_timestamp,
        modify_date                 datetime,
        notes                       varchar(2048),
        signature                   varchar(2048),
	primary key (did, merchant_id),
        unique index (did, merchant_name)
    )
    engine=innodb;

/**
 * Adding just one merchant for now
 */
insert into MERCHANTS (did, merchant_id, merchant_name) values (1, 1, "StrongKey");
/* EOF */

