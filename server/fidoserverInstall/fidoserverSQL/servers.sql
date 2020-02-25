/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
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
