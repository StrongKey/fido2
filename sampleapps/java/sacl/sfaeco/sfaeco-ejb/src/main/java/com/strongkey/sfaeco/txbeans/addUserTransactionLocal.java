/**
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 * 
 * *********************************************
 *                     888
 *                     888
 *                     888
 *   88888b.   .d88b.  888888  .d88b.  .d8888b
 *   888 "88b d88""88b 888    d8P  Y8b 88K
 *   888  888 888  888 888    88888888 "Y8888b.
 *   888  888 Y88..88P Y88b.  Y8b.          X88
 *   888  888  "Y88P"   "Y888  "Y8888   88888P'
 * 
 * *********************************************
 *  
 * Local interface of an EJB
 */
package com.strongkey.sfaeco.txbeans;

import javax.ejb.Local;
import javax.json.JsonObject;

@Local
public interface addUserTransactionLocal {
    public JsonObject execute(short did, long uid, JsonObject cart, String txid);
}
