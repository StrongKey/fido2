/*
 * Copyright StrongAuth, Inc. All Rights Reserved.
 * 
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */
create table IF NOT EXISTS Users (
        id                              bigint unsigned AUTO_INCREMENT not null,
        email                      	varchar(64) not null,
        username                        varchar(64) not null,
        firstname        		varchar(32) not null,
        lastname                        varchar(32) not null,
        create_date                     DATETIME not null,
                primary key (id),
                unique index (email),
                unique index (username)
        )
engine=innodb;
