/**
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License, as published by the Free
 * Software Foundation and available at
 * http://www.fsf.org/licensing/licenses/lgpl.html, version 2.1.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 *
 * Copyright (c) 2001-2020 StrongAuth, Inc. (d/b/a StrongKey)
 *
 * **********************************************
 *
 *  888b    888          888
 *  8888b   888          888
 *  88888b  888          888
 *  888Y88b 888  .d88b.  888888  .d88b.  .d8888b
 *  888 Y88b888 d88""88b 888    d8P  Y8b 88K
 *  888  Y88888 888  888 888    88888888 "Y8888b.
 *  888   Y8888 Y88..88P Y88b.  Y8b.          X88
 *  888    Y888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * **********************************************
 *
 * The data access object (DAO) for the SACL.
 */
package com.strongkey.sacl.roomdb;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface SaclDao {

    /**
     * 8888888b.                                             d8b          888
     * 888   Y88b                                            Y8P          888
     * 888    888                                                         888
     * 888   d88P 888d888  .d88b.  888d888  .d88b.   .d88b.  888 .d8888b  888888  .d88b.  888d888
     * 8888888P"  888P"   d8P  Y8b 888P"   d8P  Y8b d88P"88b 888 88K      888    d8P  Y8b 888P"
     * 888        888     88888888 888     88888888 888  888 888 "Y8888b. 888    88888888 888
     * 888        888     Y8b.     888     Y8b.     Y88b 888 888      X88 Y88b.  Y8b.     888
     * 888        888      "Y8888  888      "Y8888   "Y88888 888  88888P'  "Y888  "Y8888  888
     *                                                   888
     *                                              Y8b d88P
     *                                               "Y88P"
     *
     *
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(PreregisterChallenge preregisterChallenge);

    @Query("DELETE FROM preregister_challenge")
    void deleteAllPreregisterChallenges();

    @Query("SELECT * FROM preregister_challenge ORDER BY did, rpid, userid")
    List<PreregisterChallenge> getAllPreregisterChallenges();

    @Query("SELECT * FROM preregister_challenge WHERE did = :did AND rpid = :rpid")
    List<PreregisterChallenge> getAllChallengesByRpid(int did, String rpid);

    @Query("SELECT * FROM preregister_challenge WHERE did = :did AND userid = :userid")
    List<PreregisterChallenge> getAllChallengesByUserid(int did, String userid);

    @Query("SELECT * FROM preregister_challenge WHERE did = :did AND username = :username")
    List<PreregisterChallenge> getAllChallengesByUsername(int did, String username);

    @Query("SELECT max(id) FROM preregister_challenge WHERE uid = :uid")
    int getMaxIdByUid(long uid);

    @Query("SELECT max(id) FROM preregister_challenge WHERE username = :username")
    int getMaxIdByUsername(String username);

    @Query("SELECT id FROM preregister_challenge WHERE did = :did AND uid = :uid AND challenge = :challenge")
    int getChallengeByUidChallenge(int did, long uid, String challenge);

    @Query("SELECT * FROM preregister_challenge WHERE id = :id")
    PreregisterChallenge getChallengeById(long id);

    @Query("SELECT * FROM preregister_challenge WHERE did = :did AND challenge = :challenge")
    PreregisterChallenge getChallengeByChallengeNonce(int did, String challenge);


    /**
     * 8888888b.           888      888 d8b          888    d8P
     * 888   Y88b          888      888 Y8P          888   d8P
     * 888    888          888      888              888  d8P
     * 888   d88P 888  888 88888b.  888 888  .d8888b 888d88K      .d88b.  888  888
     * 8888888P"  888  888 888 "88b 888 888 d88P"    8888888b    d8P  Y8b 888  888
     * 888        888  888 888  888 888 888 888      888  Y88b   88888888 888  888
     * 888        Y88b 888 888 d88P 888 888 Y88b.    888   Y88b  Y8b.     Y88b 888
     * 888         "Y88888 88888P"  888 888  "Y8888P 888    Y88b  "Y8888   "Y88888
     *                                                                         888
     *                                                                    Y8b d88P
     *                                                                     "Y88P"
     *
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    long insert(PublicKeyCredential publicKeyCredential);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    int update(PublicKeyCredential publicKeyCredential);

    @Query("DELETE FROM public_key_credential")
    void deleteAllPublicKeyCredentials();

    @Query("SELECT * FROM public_key_credential WHERE did = :did AND id = :id")
    PublicKeyCredential getById(int did, int id);

    @Query("SELECT * FROM public_key_credential WHERE did = :did AND rpid = :rpid")
    List<PublicKeyCredential> getAllByRpid(int did, String rpid);

    @Query("SELECT * FROM public_key_credential ORDER BY did, rpid, userid")
    List<PublicKeyCredential> getAllPublicKeyCredentials();

    @Query("SELECT * FROM public_key_credential WHERE did = :did AND uid = :uid")
    PublicKeyCredential getPkcByUid(int did, long uid);

    @Query("SELECT * FROM public_key_credential WHERE did = :did AND rpid = :rpid AND userid = :userid")
    List<PublicKeyCredential> getAllKeysByRpidUserid(int did, String rpid, String userid);

    @Query("SELECT * FROM public_key_credential WHERE did = :did AND rpid = :rpid AND credential_id = :credentialid")
    PublicKeyCredential getByRpidCredentialId(int did, String rpid, String credentialid);

    @Query("SELECT * FROM public_key_credential WHERE did = :did AND rpid = :rpid AND credential_id = :credentialid")
    PublicKeyCredential getPkcByRpidCredentialId(int did, String rpid, String credentialid);

    @Query("SELECT * FROM public_key_credential WHERE did = :did AND rpid = :rpid AND userid = :userid AND credential_id = :credentialid")
    PublicKeyCredential getByRpidUseridCredentialId(int did, String rpid, String userid, String credentialid);



    /**
     * 8888888b.                                     888    888
     * 888   Y88b                                    888    888
     * 888    888                                    888    888
     * 888   d88P 888d888  .d88b.   8888b.  888  888 888888 88888b.  88888b.
     * 8888888P"  888P"   d8P  Y8b     "88b 888  888 888    888 "88b 888 "88b
     * 888        888     88888888 .d888888 888  888 888    888  888 888  888
     * 888        888     Y8b.     888  888 Y88b 888 Y88b.  888  888 888  888
     * 888        888      "Y8888  "Y888888  "Y88888  "Y888 888  888 888  888
     *
     */

    @Insert
    long insert(PreauthenticateChallenge preauthenticateChallenge);

    @Query("SELECT * FROM preauthenticate_challenge ORDER BY did, rpid, id")
    List<PreauthenticateChallenge> getAllPreauthenticateChallenges();

    @Query("DELETE FROM preauthenticate_challenge")
    void deleteAllPreauthenticateChallenges();


    /**
     * 8888888b.                                     888    888
     * 888   Y88b                                    888    888
     * 888    888                                    888    888
     * 888   d88P 888d888  .d88b.   8888b.  888  888 888888 88888b.  88888888
     * 8888888P"  888P"   d8P  Y8b     "88b 888  888 888    888 "88b    d88P
     * 888        888     88888888 .d888888 888  888 888    888  888   d88P
     * 888        888     Y8b.     888  888 Y88b 888 Y88b.  888  888  d88P
     * 888        888      "Y8888  "Y888888  "Y88888  "Y888 888  888 88888888
     *
     */

    @Insert
    long insert(PreauthorizeChallenge preauthorizeChallenge);

    @Query("SELECT * FROM preauthorize_challenge ORDER BY did, rpid, id")
    List<PreauthorizeChallenge> getAllPreauthorizeChallenges();

    @Query("DELETE FROM preauthorize_challenge")
    void deleteAllPreauthorizeChallenges();



    /**
     *        d8888           .d8888b.  d8b                            888
     *       d88888          d88P  Y88b Y8P                            888
     *      d88P888          Y88b.                                     888
     *     d88P 888 88888b.   "Y888b.   888  .d88b.  88888b.   8888b.  888888 888  888 888d888  .d88b.
     *    d88P  888 888 "88b     "Y88b. 888 d88P"88b 888 "88b     "88b 888    888  888 888P"   d8P  Y8b
     *   d88P   888 888  888       "888 888 888  888 888  888 .d888888 888    888  888 888     88888888
     *  d8888888888 888  888 Y88b  d88P 888 Y88b 888 888  888 888  888 Y88b.  Y88b 888 888     Y8b.
     * d88P     888 888  888  "Y8888P"  888  "Y88888 888  888 "Y888888  "Y888  "Y88888 888      "Y8888
     *                                           888
     *                                      Y8b d88P
     *                                       "Y88P"
     */

    @Insert
    long insert(AuthenticationSignature authenticationSignature);

    @Query("DELETE FROM authentication_signatures")
    void deleteAllFidoAuthenticationSignatures();

    @Query("SELECT * FROM authentication_signatures ORDER BY did, rpid, id")
    List<AuthenticationSignature> getAllFidoAuthenticationSignatures();


    /**
     *        d8888           .d8888b.  d8b                            888
     *       d88888          d88P  Y88b Y8P                            888
     *      d88P888          Y88b.                                     888
     *     d88P 888 88888888  "Y888b.   888  .d88b.  88888b.   8888b.  888888 888  888 888d888  .d88b.
     *    d88P  888    d88P      "Y88b. 888 d88P"88b 888 "88b     "88b 888    888  888 888P"   d8P  Y8b
     *   d88P   888   d88P         "888 888 888  888 888  888 .d888888 888    888  888 888     88888888
     *  d8888888888  d88P    Y88b  d88P 888 Y88b 888 888  888 888  888 Y88b.  Y88b 888 888     Y8b.
     * d88P     888 88888888  "Y8888P"  888  "Y88888 888  888 "Y888888  "Y888  "Y88888 888      "Y8888
     *                                           888
     *                                      Y8b d88P
     *                                       "Y88P"
     */

    @Insert
    long insert(AuthorizationSignature authorizationSignature);

    @Query("DELETE FROM authorization_signatures")
    void deleteAllFidoAuthorizationSignatures();

    @Query("SELECT * FROM authorization_signatures ORDER BY did, rpid, id")
    List<AuthorizationSignature> getAllFidoAuthorizationSignatures();
}
