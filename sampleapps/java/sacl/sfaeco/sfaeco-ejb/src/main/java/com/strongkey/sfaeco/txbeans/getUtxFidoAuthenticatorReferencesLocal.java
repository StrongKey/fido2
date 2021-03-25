/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.sfaeco.txbeans;

import javax.ejb.Local;
import javax.json.JsonArray;

/**
 *
 * @author root
 */
@Local
public interface getUtxFidoAuthenticatorReferencesLocal {
        public JsonArray byId(short did, long uid, long utxid);

}
