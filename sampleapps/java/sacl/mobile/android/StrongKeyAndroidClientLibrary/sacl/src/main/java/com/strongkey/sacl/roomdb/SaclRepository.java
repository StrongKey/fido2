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
 * The Repository class for the StrongKey Android Client Library. It abstracts
 * data access for the app so the app doesn't need to worry about whether the
 * data is coming from the local database or the network.
 */
package com.strongkey.sacl.roomdb;

import android.content.ContextWrapper;
import android.util.Log;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class SaclRepository {

    private final String TAG = "SaclRepository";

    private SaclDao mSaclDao;
    private List<PreregisterChallenge> mAllPreregisterChallenges;
    private List<PreauthenticateChallenge> mAllPreauthenticateChallenges;
    private List<PreauthorizeChallenge> mAllPreauthorizeChallenges;
    private List<PublicKeyCredential> mAllPublicKeyCredentials;
    private List<AuthenticationSignature> mAllAuthenticationSignatures;
    private List<AuthorizationSignature> mAllAuthorizationSignatures;

    /**
     * Constructor - important to note that the mAll* variables only have
     * the list of all objects as of the moment the constructor is called -
     * any changes to the database - specifically insertions and deletions -
     * are not likely to be reflected. mAll* variables should be updated
     * periodically to keep them current if necessary.
     *
     * @param context
     */
    public SaclRepository(ContextWrapper context) {
        SaclRoomDatabase database = SaclRoomDatabase.getDatabase(context);
        mSaclDao = database.saclDao();
        mAllPreregisterChallenges = mSaclDao.getAllPreregisterChallenges();
        mAllPreauthenticateChallenges = mSaclDao.getAllPreauthenticateChallenges();
        mAllPreauthorizeChallenges = mSaclDao.getAllPreauthorizeChallenges();
        mAllPublicKeyCredentials = mSaclDao.getAllPublicKeyCredentials();
        mAllAuthenticationSignatures = mSaclDao.getAllFidoAuthenticationSignatures();
        mAllAuthorizationSignatures = mSaclDao.getAllFidoAuthorizationSignatures();
    }

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
     */

    /**
     * GET ALL PREREGISTER_CHALLENGES
     */
    public List<PreregisterChallenge> getAllPreregisterChallenges() {
        return mAllPreregisterChallenges;
    }

    /**
     * GET ALL PREREGISTER_CHALLENGES BY RPID
     */
    public List<PreregisterChallenge> getAllChallengesByRpid(int did, String rpid) {
        AtomicReference<List<PreregisterChallenge>> prlist = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            prlist.set(mSaclDao.getAllChallengesByRpid(did, rpid));
            Log.v(TAG, "Returning PreRegChallenge records by RPID: " + rpid);
        });
        return prlist.get();
    }

    /**
     * GET ALL PREREGISTER_CHALLENGES BY USERID
     */
    public List<PreregisterChallenge> getAllChallengesByUserid(int did, String userid) {
        AtomicReference<List<PreregisterChallenge>> prlist = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            prlist.set(mSaclDao.getAllChallengesByUserid(did, userid));
            Log.v(TAG, "Returning PreRegChallenge records by USERID: " + userid);
        });
        return prlist.get();
    }

    /**
     * GET ALL PREREGISTER_CHALLENGES BY USERNAME
     */
    public List<PreregisterChallenge> getAllChallengesByUsername(int did, String username) {
        AtomicReference<List<PreregisterChallenge>> prlist = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            prlist.set(mSaclDao.getAllChallengesByUsername(did, username));
            Log.v(TAG, "Returning PreRegChallenge records by USERNAME: " + username);
        });
        return prlist.get();
    }

    /**
     * GET ALL PREREGISTER_CHALLENGES BY USERNAME
     */
    public List<PreregisterChallenge> getNonconsumedChallengeByUsername(int did, String username) {
        AtomicReference<List<PreregisterChallenge>> prlist = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            prlist.set(mSaclDao.getAllChallengesByUsername(did, username));
            Log.v(TAG, "Returning PreRegChallenge records by USERNAME: " + username);
        });
        return prlist.get();
    }

    /**
     * GET PREREGISTER_CHALLENGE BY ID
     */
    public PreregisterChallenge getChallengeById(int id) {
        AtomicReference<PreregisterChallenge> pr = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            pr.set(mSaclDao.getChallengeById(id));
            Log.v(TAG, "Returning PreRegChallenge by ID: " + id);
        });
        return pr.get();
    }

    /**
     * GET PREREGISTER_CHALLENGE BY CHALLENGE NONCE
     */
    public PreregisterChallenge getChallengeByChallengeNonce (int did, String nonce) {
        AtomicReference<PreregisterChallenge> pr = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            pr.set(mSaclDao.getChallengeByChallengeNonce(did, nonce));
            Log.v(TAG, "Returning PreRegChallenge by Challenge Nonce: " + nonce);
        });
        return pr.get();
    }

    /**
     * GET ID
     */
    public int getChallengeByUidChallenge(int did, long uid, String challenge) {
        AtomicReference<Integer> prid = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            prid.set(mSaclDao.getChallengeByUidChallenge(did, uid, challenge));
            Log.v(TAG, "Returning PreRegChallenge ID by UID + Challenge: " + uid + "+" + challenge);
        });
        return prid.get();
    }

    /**
     * INSERT PREREGISTER_CHALLENGE
     */
    public int insert(PreregisterChallenge preregisterChallenge) {
        AtomicInteger rowId = new AtomicInteger(0);
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            Long l = mSaclDao.insert(preregisterChallenge);
            rowId.set(l.intValue());
            Log.v(TAG, "Inserted PreRegChallenge with rowId: " + rowId);
        });
        return rowId.get();
    }

    /**
     * DELETE ALL PREREGISTER_CHALLENGES
     */
    public void deleteAllPreregisterChallenges() {
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSaclDao.deleteAllPreregisterChallenges();
            Log.v(TAG, "Deleted all PreRegChallenge records");
        });
    }

    /**
     * GET MAX(IMUM) ID BY UID
     */
    public int getMaxIdByUid(long uid) {
        AtomicReference<Integer> prid = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            prid.set(mSaclDao.getMaxIdByUid(uid));
            Log.v(TAG, "Maximum value of PreRegChallenge ID for [UID]: " + prid.get() + "["+uid+"]");
        });
        return prid.get();
    }

    /**
     * GET MAX(IMUM) ID BY USERNAME
     */
    public int getMaxIdByUsername(String username) {
        AtomicReference<Integer> prid = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            prid.set(mSaclDao.getMaxIdByUsername(username));
            Log.v(TAG, "Maximum valud of PreRegChallenge ID for [UID]: " + prid.get() + "["+username+"]");
        });
        return prid.get();
    }

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
     */
    /**
     * GET ALL PUBLIC_KEY_CREDENTIALS
     */
    public List<PublicKeyCredential> getAllPublicKeyCredentials() {
        return mAllPublicKeyCredentials;
    }

    /**
     * DELETE ALL PUBLIC_KEY_CREDENTIALS
     */
    public void deleteAllPublicKeyCredentials() {
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSaclDao.deleteAllPublicKeyCredentials();
            Log.v(TAG, "Deleted all PublicKeyCredential records");
        });
    }
    /**
     * INSERT PUBLIC_KEY_CREDENTIAL
     */
    public int insert(PublicKeyCredential publicKeyCredential) {
        AtomicInteger rowId = new AtomicInteger(0);
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            Long l = mSaclDao.insert(publicKeyCredential);
            rowId.set(l.intValue());
            Log.v(TAG, "Inserted PublicKeyCredential with rowId: " + rowId);
        });
        return rowId.get();
    }

    /**
     * UPDATE PUBLIC_KEY_CREDENTIAL
     */
    public int update(PublicKeyCredential publicKeyCredential) {
        AtomicInteger rowId = new AtomicInteger(0);
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            int i = mSaclDao.update(publicKeyCredential);
            rowId.set(i);
            Log.v(TAG, "Updated PublicKeyCredential with rowId: " + rowId);
        });
        return rowId.get();
    }

    /**
     * GET PUBLICkEY_CREDENTIAL BY ID
     */
    public PublicKeyCredential getPublicKeyCredentialById(int did, int id) {

        PublicKeyCredential pkc = mSaclDao.getById(did, id);
        if (pkc == null)
            Log.v(TAG, "No PublicKeyCredential records found for DID-ID: " + did+"-"+id);
        else
            Log.i(TAG, "Found PKC with DID-ID-CREDENTIALID: " + did+"-"+id+"-"+pkc.getCredentialId());

        return pkc;
    }

    /**
     * GET PUBLICkEY_CREDENTIAL BY UID
     */
    public PublicKeyCredential getPublicKeyCredentialByUid(int did, long uid) {

        PublicKeyCredential pkc = mSaclDao.getPkcByUid(did, uid);
        if (pkc == null)
            Log.v(TAG, "No PublicKeyCredential records found for DID-UID: " + did+"-"+uid);
        else
            Log.i(TAG, "Found PKC with DID-UID-CREDENTIALID: " + did+"-"+uid+"-"+pkc.getCredentialId());

        return pkc;
    }

    /**
     * GET ALL PUBLICkEY_CREDENTIALS BY RPID + USERID
     */
    public List<PublicKeyCredential> getPublicKeyCredentialsByRpidUserid(int did, String rpid, String userid) {
        AtomicReference<List<PublicKeyCredential>> pkclist = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                pkclist.set(mSaclDao.getAllKeysByRpidUserid(did, rpid, userid));
            } catch (NullPointerException npe) {
                Log.v(TAG, "No PublicKeyCredential records found for RPID+USERID: " + rpid + "+" + userid);
            }
        });
        try {
            return pkclist.get();
        } catch (NullPointerException npe) {
            Log.v(TAG, "No PublicKeyCredential records found in Repository for RPID+USERID: " + rpid + "+" + userid);
            return null;
        }
    }

    /**
     * GET PUBLICkEY_CREDENTIAL BY RPID + CREDENTIALID
     */
    public PublicKeyCredential getPublicKeyCredentialByRpidCredentialId(int did, String rpid, String credentialId) {

        PublicKeyCredential pkc = mSaclDao.getPkcByRpidCredentialId(did, rpid, credentialId);
        if (pkc == null)
            Log.v(TAG, "No PublicKeyCredential records found for DID-RPID-CREDENTIALID: " + did+"-"+rpid+"-"+credentialId);
        else
            Log.i(TAG, "Found PublicKeyCredential with DID-RPID-CREDENTIALID: " + did+"-"+rpid+"-"+credentialId);

        return pkc;
    }

    /**
     * GET PUBLICkEY_CREDENTIALS BY RPID + USERID + CREDENTIALID
     */
    public PublicKeyCredential getByRpidUseridCredentialId(int did, String rpid, String userid, String credentialid) {
        AtomicReference<PublicKeyCredential> pkc = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                pkc.set(mSaclDao.getByRpidUseridCredentialId(did, rpid, userid, credentialid));
            } catch (NullPointerException npe) {
                Log.v(TAG, "No PublicKeyCredential records found for RPID+USERID+CREDENTIALID: "
                        + rpid + "+" + userid + "+" + credentialid);
            }
        });
        try {
            return pkc.get();
        } catch (NullPointerException npe) {
            Log.v(TAG, "No PublicKeyCredential records found DAO for RPID+USERID+CREDENTIALID: "
                    + rpid + "+" + userid + "+" + credentialid);
            return null;
        }
    }

    /**
     * GET PUBLICkEY_CREDENTIALS BY RPID + CREDENTIALID
     */
    public PublicKeyCredential getByRpidCredentialId(int did, String rpid, String credentialid) {
        AtomicReference<PublicKeyCredential> pkc = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                pkc.set(mSaclDao.getByRpidCredentialId(did, rpid, credentialid));
            } catch (NullPointerException npe) {
                Log.v(TAG, "No PublicKeyCredential records found for RPID+CREDENTIALID: "
                        + rpid + "+" + credentialid);
            }
        });
        try {
            return pkc.get();
        } catch (NullPointerException npe) {
            Log.v(TAG, "No PublicKeyCredential records found for RPID+CREDENTIALID: "
                    + rpid + "+" + credentialid);
            return null;
        }
    }

    /**
     * GET ALL PUBLICkEY_CREDENTIALS BY RPID
     */
    public List<PublicKeyCredential> getAllByRpid(int did, String rpid) {
        AtomicReference<List<PublicKeyCredential>> pkclist = null;
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            try {
                pkclist.set(mSaclDao.getAllByRpid(did, rpid));
            } catch (NullPointerException npe) {
                Log.v(TAG, "No PublicKeyCredential records found for RPID: " + rpid);
            }
        });
        try {
            return pkclist.get();
        } catch (NullPointerException npe) {
            Log.v(TAG, "No PublicKeyCredential records found for RPID: " + rpid);
            return null;
        }
    }


    /**
     *
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

    /**
     * GET ALL PREAUTHENTICATE_CHALLENGES
     */
    public List<PreauthenticateChallenge> getAllPreauthenticateChallenges() {
        return mAllPreauthenticateChallenges;
    }

    /**
     * DELETE ALL PREAUTHENTICATE_CHALLENGES
     */
    public void deleteAllPreauthenticateChallenges() {
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSaclDao.deleteAllPreauthenticateChallenges();
            Log.v(TAG, "Deleted all PreauthenticateChallenge records");
        });
    }

    /**
     * INSERT PREAUTHENTICATE OBJECT
     */
    public int insert(PreauthenticateChallenge preauthenticateChallenge) {
        AtomicInteger rowId = new AtomicInteger(0);
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            Long l = mSaclDao.insert(preauthenticateChallenge);
            rowId.set(l.intValue());
            Log.v(TAG, "Inserted PreAuthChallenge with rowId: " + rowId);
        });
        return rowId.get();
    }


    /**
     *
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

    /**
     * GET ALL PREAUTHORIZE_CHALLENGE
     */
    public List<PreauthorizeChallenge> getAllPreauthorizeChallenges() {
        return mAllPreauthorizeChallenges;
    }

    /**
     * DELETE ALL PREAUTHORIZE_CHALLENGES
     */
    public void deleteAllPreauthorizeChallenges() {
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSaclDao.deleteAllPreauthorizeChallenges();
            Log.v(TAG, "Deleted all PreauthorizeChallenge records");
        });
    }

    /**
     * INSERT PREAUTHORIZE_CHALLENGE OBJECT
     */
    public int insert(PreauthorizeChallenge preauthorizeChallenge) {
        AtomicInteger rowId = new AtomicInteger(0);
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            Long l = mSaclDao.insert(preauthorizeChallenge);
            rowId.set(l.intValue());
            Log.v(TAG, "Inserted PreauthorizeChallenge with rowId: " + rowId);
        });
        return rowId.get();
    }



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

    /**
     * INSERT FIDO_SIGNATURE OBJECT
     */
    public int insert(AuthenticationSignature authenticationSignature) {
        AtomicInteger rowId = new AtomicInteger(0);
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            Long l = mSaclDao.insert(authenticationSignature);
            rowId.set(l.intValue());
            Log.v(TAG, "Inserted AuthenticationSignature with rowId: " + rowId);
        });
        return rowId.get();
    }

    /**
     * DELETE ALL FIDO_SIGNATURES
     */
    public void deleteAllFidoAuthenticationSignatures() {
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSaclDao.deleteAllFidoAuthenticationSignatures();
            Log.v(TAG, "Deleted all AuthenticationSignature records");
        });
    }

    /**
     * GET ALL FIDO_SIGNATURES
     */
    public List<AuthenticationSignature> getAllFidoAuthenticationSignatures() {
        return mAllAuthenticationSignatures;
    }



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

    /**
     * INSERT FIDO_SIGNATURE OBJECT
     */
    public int insert(AuthorizationSignature authorizationSignature) {
        AtomicInteger rowId = new AtomicInteger(0);
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            Long l = mSaclDao.insert(authorizationSignature);
            rowId.set(l.intValue());
            Log.v(TAG, "Inserted AuthorizationSignature with rowId: " + rowId);
        });
        return rowId.get();
    }

    /**
     * DELETE ALL FIDO_SIGNATURES
     */
    public void deleteAllFidoAuthorizationSignatures() {
        SaclRoomDatabase.databaseWriteExecutor.execute(() -> {
            mSaclDao.deleteAllFidoAuthorizationSignatures();
            Log.v(TAG, "Deleted all AuthorizationSignature records");
        });
    }

    /**
     * GET ALL FIDO_SIGNATURES
     */
    public List<AuthorizationSignature> getAllFidoAuthorizationSignatures() {
        return mAllAuthorizationSignatures;
    }
}
