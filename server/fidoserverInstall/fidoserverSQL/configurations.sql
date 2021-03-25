/*
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License, as published by the Free Software Foundation and
 * available at https://www.gnu.org/licenses/old-licenses/lgpl-2.1.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2020 StrongAuth, Inc.  
 *
 * CONFIGURATIONS table for MySQL
 *
 * A table to maintain configuration information about domains 
 * installed on this SKA server.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 */

create table IF NOT EXISTS CONFIGURATIONS (
        did                     smallint unsigned not null,
        config_key              varchar(512) not null,
        config_value            varchar(512) not null,
        notes                   varchar(512),
                primary key (did, config_key)
        )
        engine=innodb;

/* EOF */
