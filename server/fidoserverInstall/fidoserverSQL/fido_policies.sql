/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*
 *
 * FIDO_KEYS table for MariaDB
 *
 * Contains encrypted symmetric keys used with the ANSI X9.24-1
 * Derived Unique Key Per Transaction (DUKPT) protocol.  Keys are
 * encrypted and stored in the SYMMETRIC_KEYS table and assigned
 * a unique token (stored here as keytoken).  To use this key, it
 * must first be decrypted using the decrypt webservice operation
 * and its KCV must be verified to ensure it was decrypted correctly
 * before its used.
 *
 * $Date$
 * $Revision$
 * $Author$
 * $URL$
 *
 */

create table IF NOT EXISTS FIDO_POLICIES (
        sid                             tinyint unsigned not null,
        did                             smallint unsigned not null,
        pid                             int unsigned not null,
        policy                          LONGTEXT not null,
        status                          enum('Active', 'Inactive') not null,
        notes                           varchar(512),
        create_date                     DATETIME not null,
        modify_date                     DATETIME,
        signature                       varchar(2048),
                primary key (sid, did, pid),
                unique index (sid, did, pid)
        )


/* EOF */
