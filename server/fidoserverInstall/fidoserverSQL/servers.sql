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
 * SERVERS table for MySQL
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 */

create table SERVERS (
        sid				tinyint unsigned not null,
        fqdn  				varchar(512) not null,
	status                  	enum('Active', 'Inactive', 'Other') not null,
        replication_role                enum('Publisher', 'Subscriber', 'Both') not null,
        replication_status              enum('Active', 'Inactive', 'Other') not null,
        mask              		varchar(2048),
	notes				varchar(512),
		primary key (sid),
		unique index (sid, fqdn)
        )
	engine=innodb;

/* EOF */
