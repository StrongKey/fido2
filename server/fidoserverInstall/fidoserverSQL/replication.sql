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
 * REPLICATION table for MySQL
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 */

create table REPLICATION (
        ssid		tinyint unsigned not null,
        rpid		bigint unsigned not null,
        tsid		tinyint unsigned not null,
        objectype	tinyint unsigned not null,
        objectop	tinyint unsigned not null,
	objectpk        varchar(520) not null,
        scheduled       datetime,
		primary key (ssid, rpid, tsid)
        )
	engine=innodb;

/* EOF */
