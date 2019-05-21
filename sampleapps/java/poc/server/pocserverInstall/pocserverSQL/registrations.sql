/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */
create table IF NOT EXISTS Registrations (
        id                              bigint unsigned AUTO_INCREMENT not null,
        email                      	varchar(64) not null,
        nonce                        	varchar(64) not null,
                primary key (id),
                unique index (email),
                unique index (nonce)
        )
engine=innodb;
