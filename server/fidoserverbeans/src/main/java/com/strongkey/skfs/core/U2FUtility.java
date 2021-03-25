/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.core;

import com.strongkey.skfs.utilities.SKFSConstants;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Utility class with generic methods. Mostly generic for FIDO based operations.
 *
 */
public class U2FUtility {

    /**
     * Generates a sesson id, using the given random number and a secret key
     * that is maintained in memory.
     *
     * Currently, a SHA-256 digest of (random number + secret key) is termed as
     * session id.
     *
     * @param randomnumber - any random number.
     * @return - String which is the session id.
     * @throws SKFEException - In case of any error.
     */
//    public static String generateSessionid(String randomnumber) throws SKFEException {
//
//        //concatenation of randomnumber,My Secret
//        // H256(random|secret) //unique sessionIDs incase challenge generated on multiple fido engines are the same
//        String sessionID = randomnumber + MY_SECRET_KEY;
//        try {
//            return Common.getDigest(sessionID, "SHA-256");
//        } catch (NoSuchAlgorithmException |
//                NoSuchProviderException |
//                UnsupportedEncodingException ex) {
//            throw new SKFEException(ex);
//        }
//    }

    /**
     * A reverse process for sessionid generation. In this method, a session is
     * validated by re-calculating the sessionid using the nonce + secret_key
     * and comparing the result with the
     *
     * @param sessionid - String, sessionid to be validated.
     * @param nonce - String, nonce using which the sessionid has been claimed
     * to be generated.
     * @param sid
     *
     * @return - boolean, true or false based on validation result.
     * @throws SKFEException - In case of any error.
     */
//    public static boolean validateSessionid(String sessionid, String nonce, String sid) throws SKFEException {
//        String secretKey = Common.getSecretKey(sid);
//        String preImage = nonce + secretKey;
//        String hash = null;
//        try {
//            hash = Common.getDigest(preImage, "SHA-256");
//        } catch (NoSuchAlgorithmException |
//                NoSuchProviderException |
//                UnsupportedEncodingException ex) {
//            throw new SKFEException(ex);
//        }
//
//        return hash.equals(sessionid);
//    }

    /**
     * Generates a series of characters which is random.
     *
     * @param size - int, is the entrophy length to be used.
     * @return - String, random character set.
     */
    public static String getRandom(int size) {

        if (size > SKFSConstants.MAX_RANDOM_NUMBER_SIZE_BITS / 8) {
            size = SKFSConstants.MAX_RANDOM_NUMBER_SIZE_BITS / 8;
        }
        //Generate seed
        SecureRandom random = new SecureRandom();
        byte seed[] = new byte[20];
        random.nextBytes(seed);

        //Generate Random number
        SecureRandom sr = new SecureRandom(seed);
        byte[] randomBytes = new byte[size];
        sr.nextBytes(randomBytes);

        return Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
    }
}
