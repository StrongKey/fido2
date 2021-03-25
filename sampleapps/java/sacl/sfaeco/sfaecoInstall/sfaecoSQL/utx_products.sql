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
 * UTX_PRODUCTS table for MariaDB
 *
 * The table is a list of products purchased within a user transaction  - 
 * keeping things simple for this sample application.
 *
 */

create table UTX_PRODUCTS (
        did                         smallint(5) unsigned not null,
        sid                         tinyint unsigned not null,
        uid                         bigint unsigned not null,
        utxid                       bigint unsigned not null,
        product_id                  tinyint unsigned not null,
        product_name                varchar(128) not null,
        product_price               int not null,
        create_date                 datetime not null default current_timestamp,
        modify_date                 datetime,
        notes                       varchar(2048),
        signature                   varchar(2048),
	primary key (did, sid, uid, utxid, product_id)
    )
    engine=innodb;

/* EOF */

