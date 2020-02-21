/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
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
