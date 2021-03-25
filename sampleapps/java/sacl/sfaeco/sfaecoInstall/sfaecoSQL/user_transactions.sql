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
 * USER_TRANSACTIONS table for MariaDB
 *
 * The table is a log of user transactions executed within the SFAECO
 * module. This is intended to demonstrate a user performing a business 
 * transaction within the application, with a FIDO digital signature.
 *
 */

create table USER_TRANSACTIONS (
        did                         smallint(5) unsigned not null,
        sid                         tinyint unsigned not null,
        uid                         bigint unsigned not null,
        utxid                       bigint unsigned not null,
        merchant_id                 smallint(5) unsigned,
        total_products              tinyint unsigned,
        total_price                 int unsigned,
        payment_brand               varchar(32),
        payment_card_number         varchar(32),
        currency                    varchar(8),
        txtime                      bigint unsigned,
        txid                        varchar(32) not null,
        txpayload                   varchar(2048),
        nonce                       varchar(128),
        challenge                   varchar(256),
        status                      enum('Canceled', 'Failed', 'Inflight', 'Succeeded', 'Unauthorized', 'Other') not null,
        create_date                 datetime not null default current_timestamp,
        modify_date                 datetime,
        notes                       varchar(2048),
        signature                   varchar(2048),
	primary key (did, sid, uid, utxid)
    )
    engine=innodb;

/* EOF */

