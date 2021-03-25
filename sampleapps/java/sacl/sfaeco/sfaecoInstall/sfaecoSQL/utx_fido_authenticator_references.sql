/*
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public 
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 *
 * UTX_FIDO_AUTHENTICATOR_REFERENCES table for MariaDB
 *
 * The table with the FIDO digital signature, if any, upon the
 * completion of a succecssful transaction.
 *
 */

create table UTX_FIDO_AUTHENTICATOR_REFERENCES (
        did                         smallint(5) unsigned not null,
        sid                         tinyint unsigned not null,
        uid                         bigint unsigned not null,
        utxid                       bigint unsigned not null,
        farid                       bigint unsigned not null,
        protocol                    enum ('FIDO2_0') not null,
        fidoid                      varchar(512) not null,
        rawid                       varchar(1024) not null,
        user_handle                 varchar(512),
        rpid                        varchar(256) not null,
        authenticator_data          varchar(128) not null,
        client_data_json            varchar(256) not null,
        aaguid                      varchar(128) not null,
        auth_time                   datetime not null,
        up                          boolean not null,
        uv                          boolean not null,
        used_for_this_transaction   boolean not null,
	signing_key_type	    enum ("RSA","ECDSA") not null,
	signing_key_algorithm	    enum ("SHA1withRSA", "SHA256withRSA", "SHA384withRSA", 
                                            "SHA512withRSA", "SHA256withRSAandMGF1", 
                                            "SHA384withRSAandMGF1", "SHA512withRSAandMGF1", 
                                            "SHA256withECDSA", "SHA384withECDSA", 
                                            "SHA512withECDSA", "NONEwithECDSA", 
                                            "SHA256withEDDSA") not null,
        signer_public_key           varchar(512) not null,
        fido_signature              varchar(256) not null,
        create_date                 datetime not null default current_timestamp,
        signature                   varchar(2048),
	primary key (did, sid, uid, utxid, farid)
    )
    engine=innodb;

/* EOF */

