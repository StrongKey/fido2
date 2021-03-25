/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
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
