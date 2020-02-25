/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 *
 * DOMAINS table for MySQL
 *
 * Distinguishes separate domains of control when more than one
 * company is hosted on the same instance of the StrongAuth
 * KeyAppliance (SKA).
 *
 * A domain can consist of a single company, a department of a
 * company, a workgroup within a company or even a single individual.
 * The purpose of a domain is to allow a single SKA server to serve
 * multiple domains from the same SKA server instance, while keeping
 * all information about a domain distinct and secure from each other.
 * Domains in an SKA server do not correspond to DNS-style or LDAP
 * domains, but could be considered to be equivalent for logical
 * grouping if desired.
 *
 * The replication_status field allows for turning on/off replication
 * for all servers servicing this encryption domain.  Replication to
 * and from individual servers must be managed from the SERVERS table.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 */

create table DOMAINS (
        did				smallint unsigned primary key,
        name  				varchar(512) unique,
	status                  	enum('Active', 'Inactive', 'Other') not null,
	replication_status              enum('Active', 'Inactive', 'Other') not null,
        encryption_certificate		varchar(4096) not null,
        encryption_certificate_uuid	varchar(64),
        signing_certificate		varchar(4096),
        signing_certificate_uuid	varchar(64),
        skce_signingdn          	varchar(512),
        skfe_appid              	varchar(256),
	notes				varchar(512)
        )
	engine=innodb;

/* EOF */
