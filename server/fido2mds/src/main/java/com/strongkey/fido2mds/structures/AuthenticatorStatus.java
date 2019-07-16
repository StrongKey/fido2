/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds.structures;

public enum AuthenticatorStatus {
    NOT_FIDO_CERTIFIED,
    FIDO_CERTIFIED,
    USER_VERIFICATION_BYPASS,
    ATTESTATION_KEY_COMPROMISE,
    USER_KEY_REMOTE_COMPROMISE,
    USER_KEY_PHYSICAL_COMPROMISE,
    UPDATE_AVAILABLE,
    REVOKED,
    SELF_ASSERTION_SUBMITTED,
    FIDO_CERTIFIED_L1,
    FIDO_CERTIFIED_L1plus,
    FIDO_CERTIFIED_L2,
    FIDO_CERTIFIED_L2plus,
    FIDO_CERTIFIED_L3,
    FIDO_CERTIFIED_L3plus,
    FIDO_CERTIFIED_L4,
    FIDO_CERTIFIED_L4plus,
    FIDO_CERTIFIED_L5,
    FIDO_CERTIFIED_L5plus
    
}
