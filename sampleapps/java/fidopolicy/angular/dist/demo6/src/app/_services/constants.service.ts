export class ConstantsService {

    // The build number corresponding to the github.
    public static buildNumber: number = 2;

    //---------------------------------------------------------------------------------------------------
    //  .d8888b.                     .d888 d8b                                    888      888
    // d88P  Y88b                   d88P"  Y8P                                    888      888
    // 888    888                   888                                           888      888
    // 888         .d88b.  88888b.  888888 888  .d88b.  888  888 888d888  8888b.  88888b.  888  .d88b.
    // 888        d88""88b 888 "88b 888    888 d88P"88b 888  888 888P"       "88b 888 "88b 888 d8P  Y8b
    // 888    888 888  888 888  888 888    888 888  888 888  888 888     .d888888 888  888 888 88888888
    // Y88b  d88P Y88..88P 888  888 888    888 Y88b 888 Y88b 888 888     888  888 888 d88P 888 Y8b.
    // " Y8888P"   "Y88P"  888  888 888    888  "Y88888  "Y88888 888     "Y888888 88888P"  888  "Y8888
    //                                             888
    //                                        Y8b d88P
    //                                         "Y88P"
    //---------------------------------------------------------------------------------------------------

    // The name of the company which owns this application.

    // Application language
    public static language: string = 'en';
    // flag that determines if browser is webauthn supported or not

    public static isWebauthnSupported: boolean = false;

    // self-register property used on FSO demo
    public static isSelfRegisterOn: boolean = true;

    public static isDemo: boolean = false;

    public static isSessionValid: boolean = false;


    //-----------------------------------------------
    //  .d8888b.  888             888    d8b
    //d 88P  Y88b 888             888    Y8P
    // Y88b.      888             888
    // "Y888b.   888888  8888b.  888888 888  .d8888b
    //    "Y88b. 888        "88b 888    888 d88P"
    //      "888 888    .d888888 888    888 888
    //Y88b  d88P Y88b.  888  888 Y88b.  888 Y88b.
    // "Y8888P"   "Y888 "Y888888  "Y888 888  "Y8888P
    //-----------------------------------------------

    // the URL of the application
    public static baseURL: string = window.location.protocol + "//" + window.location.hostname;

//main policy types labels
    public static restrictedLabel: string = "Restricted";
    public static strictLabel: string = "Strict";
    public static moderateLabel: string = "Moderate";
    public static minimalLabel: string = "Minimal";

//labels for subtypes of restricted policy
    public static restrictedAndroidKey: string = "Restricted-Android-Key";
    public static restrictedApple: string = "Restricted-Apple";
    public static restrictedTpm: string = "Restricted-TPM";
    public static restrictedFips: string = "Restricted-FIPS"; 

//labels for subtypes of strict policy
    public static strictBasic: string = "Strict-All-Biometric-Devices";
    public static strictAndroidSafetyNet: string = "Strict-Android-SafetyNet";
//labels for subtypes of moderate policy
    public static moderateBasic: string = "Moderate-Specific-Authenticators";
//labels for subtypes of minimal policy
    public static minimalBasic: string = "Minimal-Any-Hardware-Authenticators";

//All labels for dropdown values
public static strictPolicy: string = "strict";
public static strictAndroidSafetyNetPolicy: string = "strictAndroidSafetyNet";
public static moderatePolicy: string = "moderate";
public static minimalPolicy: string = "minimal";
public static restrictedAndroidKeyPolicy: string = "restrictedAndroidKey";
public static restrictedApplePolicy: string = "restrictedApple";
public static restrictedTpmPolicy: string = "restrictedTpm";
public static restrictedFipsPolicy: string = "restrictedFips";
//policy Descriptions
    public static policyDesc: any = {
                restricted:`● Most secure policy
                        ● Requires specific Platform Authenticator
                        ● Requires user verification: Biometrics
                        ● Requires restricted algorithm: ECDSA
                        ● Cannot use NONE or SELF attestation
                        `,
                strict:`● Very secure policy.
                        ● Requires user verification: Biometrics, PIN or Pattern
                        ● Requires restricted algorithm: ECDSA
                        ● Cannot use NONE or SELF attestation`,
                moderate:`● Reasonably secure policy
                          ● Requires specific Authenticators
                          ● Requires user presence
                          ● Requires restricted algorithm: ECDSA
                          ● Cannot use NONE or SELF attestation`,
                minimal:`● Minimal security policy
                          ● Accepts any hardware Authenticator
                          ● Requires user presence
                          ● Prefers user verification
                          ● Accepts either ECDSA or RSA
                          ● Cannot use SELF attestation format`,

                restrictedAndroidKey:`● Most secure policy
                            ● Requires Android 9+ mobile device
                            ● Requires biometrics enabled on device
                            ● Requires app built with SACL
                            ● Uses TEE or Secure Element on device
                            ● Uses Android Key Attestation`,
                restrictedTpm:`● A very secure policy
                                ● Requires Windows 10+
                                ● Requires Windows Hello to be enabled
                                ● Requires user verification: Biometrics, PIN
                                ● Uses TPM Attestation
                                ● Uses RSA or ECDSA (based on TPM version)`,
                restrictedApple:`● A very secure policy
                                ● Requires iOS 14+ mobile device
                                ● Requires use of Safari browser
                                ● Requires TouchID or FaceID
                                ● Uses Apple Attestation
                                ● Does NOT use Passkey (Coming soon)`,
                restrictedFips:`● Very secure policy.
                                ● Requires user verification: Biometrics, PIN or Pattern
                                ● Requires restricted algorithm: ECDSA
                                ● Cannot use NONE or SELF attestatio
                                ● Requires specific FIPS Authenticator`,
                strictBasic:`● Very secure policy.
                        ● Requires user verification: Biometrics, PIN or Pattern
                        ● Requires restricted algorithm: ECDSA
                        ● Cannot use NONE or SELF attestation`,
                strictAndroidSafetyNet:`● Very secure policy.
                        ● Requires user verification: Biometrics, PIN or Pattern
                        ● Requires restricted algorithm: ECDSA
                        ● Uses Android SafetyNet Attestation`,
                moderateBasic:`● Reasonably secure policy
                          ● Requires specific Authenticators
                          ● Requires user presence
                          ● Requires restricted algorithm: ECDSA
                          ● Cannot use NONE or SELF attestation`,
                minimalBasic:`● Minimal security policy
                          ● Accepts any hardware Authenticator
                          ● Requires user presence
                          ● Prefers user verification
                          ● Accepts either ECDSA or RSA
                          ● Cannot use SELF attestation format`,
              }
    public static strictAndroidSafetyNetJson = JSON.stringify({
		"FidoPolicy": {
			"name": "RestrictedSKFSPolicy-Android-SafetyNet",
			"copyright": "StrongAuth, Inc. (DBA StrongKey) All Rights Reserved",
			"version": "2.0",
			"startDate": "1624920988",
			"endDate": "1760103870871",
			"system": {
				"requireCounter": "mandatory",
				"integritySignatures": true,
				"userVerification": ["required"],
				"userPresenceTimeout": 30,
				"allowedAaguids": ["b93fd961-f2e6-462f-b122-82002247de78"],
				"jwtKeyValidity": 365,
				"jwtRenewalWindow": 30,
				"transport": ["usb", "internal"]
			},
			"algorithms": {
				"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
				"rsa": ["none"],
				"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
			},
			"attestation": {
				"conveyance": ["direct"],
				"formats": ["android-safetynet"]
			},
			"registration": {
				"displayName": "required",
				"attachment": ["platform "],
				"discoverableCredential": ["required"],
				"excludeCredentials": "enabled"
			},
			"authentication": {
				"allowCredentials": "enabled"
			},
			"authorization": {
				"maxdataLength": 256,
				"preserve": true
			},
			"rp": {
				"id": "strongkey.com",
				"name": "FIDOServer"
			},
			"extensions": {
				"uvm": {
					"allowedMethods": [
						"presence",
						"fingerprint",
						"passcode",
						"voiceprint",
						"faceprint",
						"eyeprint",
						"pattern",
						"handprint"
					],
					"allowedKeyProtections": ["hardware", "secureElement"],
					"allowedProtectionTypes": ["tee", "chip"]
				},
				"largeBlobSupport": "preferred"
			},
			"mds": {
				"authenticatorStatusReport": [{
						"status": "FIDO_CERTIFIED_L1",
						"priority": "1",
						"decision": "IGNORE"
					},
					{
						"status": "FIDO_CERTIFIED_L2",
						"priority": "1",
						"decision": "ACCEPT"
					},
					{
						"status": "UPDATE_AVAILABLE",
						"priority": "5",
						"decision": "IGNORE"
					},
					{
						"status": "REVOKED",
						"priority": "10",
						"decision": "DENY"
					}
				]
			},
			"jwt": {
				"algorithms": ["ES256", "ES384", "ES521"],
				"duration": 30,
				"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
				"signingCerts": {
					"DN": "CN=StrongKey KeyAppliance,O=StrongKey",
					"certsPerServer": 3
				}
			}
		}
	}
    , null, " "
    );

    public static strictJson = JSON.stringify({
		"FidoPolicy": {
			"name": "SecureSKFSPolicy-AllBiometricDevices",
			"copyright": "StrongAuth, Inc. (DBA StrongKey) All Rights Reserved",
			"version": "2.0",
			"startDate": "1624920988",
			"endDate": "1760103870871",
			"system": {
				"requireCounter": "mandatory",
				"integritySignatures": true,
				"userVerification": ["required"],
				"userPresenceTimeout": 30,
				"allowedAaguids": ["all"],
				"jwtKeyValidity": 365,
				"jwtRenewalWindow": 30,
				"transport": ["usb", "internal"]
			},
			"algorithms": {
				"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
				"rsa": ["none"],
				"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
			},
			"attestation": {
				"conveyance": ["direct"],
				"formats": ["packed", "tpm", "android-key"]
			},
			"registration": {
				"displayName": "required",
				"attachment": ["platform", "cross-platform"],
				"discoverableCredential": ["required"],
				"excludeCredentials": "enabled"
			},
			"authentication": {
				"allowCredentials": "enabled"
			},
			"authorization": {
				"maxdataLength": 256,
				"preserve": true
			},
			"rp": {
				"id": "strongkey.com",
				"name": "FIDOServer"
			},
			"extensions": {
				"uvm": {
					"allowedMethods": [
						"presence",
						"fingerprint",
						"passcode",
						"voiceprint",
						"faceprint",
						"eyeprint",
						"pattern",
						"handprint"
					],
					"allowedKeyProtections": ["hardware", "secureElement"],
					"allowedProtectionTypes": ["tee", "chip"]
				},
				"largeBlobSupport": "preferred"
			},
			"mds": {
				"authenticatorStatusReport": [{
						"status": "FIDO_CERTIFIED_L1",
						"priority": "1",
						"decision": "IGNORE"
					},
					{
						"status": "FIDO_CERTIFIED_L2",
						"priority": "1",
						"decision": "ACCEPT"
					},
					{
						"status": "UPDATE_AVAILABLE",
						"priority": "5",
						"decision": "IGNORE"
					},
					{
						"status": "REVOKED",
						"priority": "10",
						"decision": "DENY"
					}
				]
			},
			"jwt": {
				"algorithms": ["ES256", "ES384", "ES521"],
				"duration": 30,
				"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
				"signingCerts": {
					"DN": "CN=StrongKey KeyAppliance,O=StrongKey",
					"certsPerServer": 3
				}
			}
		}
	}
, null, " "
);
    public static moderateJson = JSON.stringify({
		"FidoPolicy": {
			"name": "ModerateSKFSPolicy-SpecificSecurityKeys",
			"copyright": "StrongAuth, Inc. (DBA StrongKey) All Rights Reserved",
			"version": "2.0",
			"startDate": "1624920988",
			"endDate": "1760103870871",
			"system": {
				"requireCounter": "mandatory",
				"integritySignatures": true,
				"userVerification": ["preferred"],
				"userPresenceTimeout": 60,
				"allowedAaguids": ["95442b2e-f15e-4def-b270-efb106facb4e",
					"87dbc5a1-4c94-4dc8-8a47-97d800fd1f3c",
					"95442b2e-f15e-4def-b270-efb106facb4e",
					"87dbc5a1-4c94-4dc8-8a47-97d800fd1f3c",
					"da776f39-f6c8-4a89-b252-1d86137a46ba",
					"e3512a8a-62ae-11ea-bc55-0242ac130003",
					"cb69481e-8ff7-4039-93ec-0a2729a154a8",
					"ee882879-721c-4913-9775-3dfcce97072a",
					"fa2b99dc-9e39-4257-8f92-4a30d23c4118",
					"2fc0579f-8113-47ea-b116-bb5a8db9202a",
					"c1f9a0bc-1dd2-404a-b27f-8e29047a43fd",
					"cb69481e-8ff7-4039-93ec-0a2729a154a8",
					"ee882879-721c-4913-9775-3dfcce97072a",
					"73bb0cd4-e502-49b8-9c6f-b59445bf720b",
					"cb69481e-8ff7-4039-93ec-0a2729a154a8",
					"ee882879-721c-4913-9775-3dfcce97072a",
					"73bb0cd4-e502-49b8-9c6f-b59445bf720b",
					"cb69481e-8ff7-4039-93ec-0a2729a154a8",
					"ee882879-721c-4913-9775-3dfcce97072a",
					"73bb0cd4-e502-49b8-9c6f-b59445bf720b",
					"2fc0579f-8113-47ea-b116-bb5a8db9202a",
					"c1f9a0bc-1dd2-404a-b27f-8e29047a43fd",
					"c5ef55ff-ad9a-4b9f-b580-adebafe026d0",
					"85203421-48f9-4355-9bc8-8a53846e5083",
					"f8a011f3-8c0a-4d15-8006-17111f9edc7d",
					"b92c3f9a-c014-4056-887f-140a2501163b",
					"6d44ba9b-f6ec-2e49-b930-0c8fe920cb73",
					"149a2021-8ef6-4133-96b8-81f8d5b7f1f5"
				],
				"jwtKeyValidity": 365,
				"jwtRenewalWindow": 30,
				"transport": ["usb", "internal"]
			},
			"algorithms": {
				"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
				"rsa": ["none"],
				"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
			},
			"attestation": {
				"conveyance": ["direct"],
				"formats": ["packed"]
			},
			"registration": {
				"displayName": "required",
				"attachment": ["cross-platform"],
				"discoverableCredential": ["preferred", "discouraged"],
				"excludeCredentials": "enabled"
			},
			"authentication": {
				"allowCredentials": "enabled"
			},
			"authorization": {
				"maxdataLength": 256,
				"preserve": true
			},
			"rp": {
				"id": "strongkey.com",
				"name": "FIDOServer"
			},
			"extensions": {
				"uvm": {
					"allowedMethods": [
						"presence",
						"fingerprint",
						"passcode",
						"voiceprint",
						"faceprint",
						"eyeprint",
						"pattern",
						"handprint"
					],
					"allowedKeyProtections": ["hardware", "secureElement"],
					"allowedProtectionTypes": ["tee", "chip"]
				},
				"largeBlobSupport": "preferred"
			},
			"mds": {
				"authenticatorStatusReport": [{
						"status": "FIDO_CERTIFIED_L1",
						"priority": "1",
						"decision": "IGNORE"
					},
					{
						"status": "FIDO_CERTIFIED_L2",
						"priority": "1",
						"decision": "ACCEPT"
					},
					{
						"status": "UPDATE_AVAILABLE",
						"priority": "5",
						"decision": "IGNORE"
					},
					{
						"status": "REVOKED",
						"priority": "10",
						"decision": "DENY"
					}
				]
			},
			"jwt": {
				"algorithms": ["ES256", "ES384", "ES521"],
				"duration": 30,
				"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
				"signingCerts": {
					"DN": "CN=StrongKey KeyAppliance,O=StrongKey",
					"certsPerServer": 3
				}
			}
		}
	}
, null, " "
);
    public static minimalJson = JSON.stringify({
		"FidoPolicy": {
			"name": "MinimalPolicy",
			"copyright": "",
			"version": "1.0",
			"startDate": "1606957205",
			"endDate": "1760103870871",
			"system": {
				"requireCounter": "mandatory",
				"integritySignatures": false,
				"userVerification": ["required", "preferred", "discouraged"],
				"userPresenceTimeout": 0,
				"allowedAaguids": ["all"],
				"jwtKeyValidity": 365,
				"jwtRenewalWindow": 30,
				"transport": ["usb", "internal"]
			},
			"algorithms": {
				"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
				"rsa": ["RS256", "RS384", "RS512", "PS256", "PS384", "PS384"],
				"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
			},
			"attestation": {
				"conveyance": ["none", "indirect", "direct", "enterprise"],
				"formats": ["fido-u2f", "packed", "tpm", "android-key", "android-safetynet", "apple", "none"]
			},
			"registration": {
				"displayName": "required",
				"attachment": ["platform", "cross-platform"],
				"discoverableCredential": ["required", "preferred", "discouraged"],
				"excludeCredentials": "enabled"
			},
			"authentication": {
				"allowCredentials": "enabled"
			},
			"authorization": {
				"maxdataLength": 256,
				"preserve": true
			},
			"rp": {
				"id": "strongkey.com",
				"name": "FIDOServer"
			},
			"extensions": {
				"uvm": {
					"allowedMethods": [
						"presence",
						"fingerprint",
						"passcode",
						"voiceprint",
						"faceprint",
						"eyeprint",
						"pattern",
						"handprint"
					],
					"allowedKeyProtections": ["hardware", "secureElement"],
					"allowedProtectionTypes": ["tee", "chip"]
				},
				"largeBlobSupport": "preferred"
			},
			"mds": {
				"authenticatorStatusReport": [{
						"status": "FIDO_CERTIFIED_L1",
						"priority": "1",
						"decision": "IGNORE"
					},
					{
						"status": "FIDO_CERTIFIED_L2",
						"priority": "1",
						"decision": "ACCEPT"
					},
					{
						"status": "UPDATE_AVAILABLE",
						"priority": "5",
						"decision": "IGNORE"
					},
					{
						"status": "REVOKED",
						"priority": "10",
						"decision": "DENY"
					}
				]
			},
			"jwt": {
				"algorithms": ["ES256", "ES384", "ES521"],
				"duration": 30,
				"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
				"signingCerts": {
					"DN": "CN=StrongKey Key Appliance,O=StrongKey",
					"certsPerServer": 2
				}
			}
		}
	}, null, 2

);

  public static restrictedAndroidKeyJson = JSON.stringify({
	"FidoPolicy": {
		"name": "RestrictedSKFSPolicy-Android-Key",
		"copyright": "StrongAuth, Inc. (DBA StrongKey) All Rights Reserved",
		"version": "2.0",
		"startDate": "1624920988",
		"endDate": "1760103870871",
		"system": {
			"requireCounter": "mandatory",
			"integritySignatures": true,
			"userVerification": ["required"],
			"userPresenceTimeout": 30,
			"allowedAaguids": ["b93fd961-f2e6-462f-b122-82002247de78"],
			"jwtKeyValidity": 365,
			"jwtRenewalWindow": 30,
			"transport": ["usb", "internal"]
		},
		"algorithms": {
			"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
			"rsa": ["none"],
			"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
		},
		"attestation": {
			"conveyance": ["direct"],
			"formats": ["android-key"]
		},
		"registration": {
			"displayName": "required",
			"attachment": ["platform "],
			"discoverableCredential": ["required"],
			"excludeCredentials": "enabled"
		},
		"authentication": {
			"allowCredentials": "enabled"
		},
		"authorization": {
			"maxdataLength": 256,
			"preserve": true
		},
		"rp": {
			"id": "strongkey.com",
			"name": "FIDOServer"
		},
		"extensions": {
			"uvm": {
				"allowedMethods": [
					"presence",
					"fingerprint",
					"passcode",
					"voiceprint",
					"faceprint",
					"eyeprint",
					"pattern",
					"handprint"
				],
				"allowedKeyProtections": ["hardware", "secureElement"],
				"allowedProtectionTypes": ["tee", "chip"]
			},
			"largeBlobSupport": "preferred"
		},
		"mds": {
			"authenticatorStatusReport": [{
					"status": "FIDO_CERTIFIED_L1",
					"priority": "1",
					"decision": "IGNORE"
				},
				{
					"status": "FIDO_CERTIFIED_L2",
					"priority": "1",
					"decision": "ACCEPT"
				},
				{
					"status": "UPDATE_AVAILABLE",
					"priority": "5",
					"decision": "IGNORE"
				},
				{
					"status": "REVOKED",
					"priority": "10",
					"decision": "DENY"
				}
			]
		},
		"jwt": {
			"algorithms": ["ES256", "ES384", "ES521"],
			"duration": 30,
			"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
			"signingCerts": {
				"DN": "CN=StrongKey KeyAppliance,O=StrongKey",
				"certsPerServer": 3
			}
		}
	}
}
      
    , null, 2

    );

  public static restrictedTpmJson = JSON.stringify({
	"FidoPolicy": {
		"name": "RestrictedSKFSPolicy-TPM",
		"copyright": "StrongAuth, Inc. (DBA StrongKey) All Rights Reserved",
		"version": "2.0",
		"startDate": "1624920988",
		"endDate": "1760103870871",
		"system": {
			"requireCounter": "mandatory",
			"integritySignatures": true,
			"userVerification": ["required"],
			"userPresenceTimeout": 30,
			"allowedAaguids": ["08987058-cadc-4b81-b6e1-30de50dcbe96"],
			"jwtKeyValidity": 365,
			"jwtRenewalWindow": 30,
			"transport": ["usb", "internal"]
		},
		"algorithms": {
			"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
			"rsa": ["RS256", "RS384", "RS512", "PS256", "PS384", "PS384"],
			"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
		},
		"attestation": {
			"conveyance": ["direct"],
			"formats": ["tpm"]
		},
		"registration": {
			"displayName": "required",
			"attachment": ["platform "],
			"discoverableCredential": ["required"],
			"excludeCredentials": "enabled"
		},
		"authentication": {
			"allowCredentials": "enabled"
		},
		"authorization": {
			"maxdataLength": 256,
			"preserve": true
		},
		"rp": {
			"id": "strongkey.com",
			"name": "FIDOServer"
		},
		"extensions": {
			"uvm": {
				"allowedMethods": [
					"presence",
					"fingerprint",
					"passcode",
					"voiceprint",
					"faceprint",
					"eyeprint",
					"pattern",
					"handprint"
				],
				"allowedKeyProtections": ["hardware", "secureElement"],
				"allowedProtectionTypes": ["tee", "chip"]
			},
			"largeBlobSupport": "preferred"
		},
		"mds": {
			"authenticatorStatusReport": [{
					"status": "FIDO_CERTIFIED_L1",
					"priority": "1",
					"decision": "IGNORE"
				},
				{
					"status": "FIDO_CERTIFIED_L2",
					"priority": "1",
					"decision": "ACCEPT"
				},
				{
					"status": "UPDATE_AVAILABLE",
					"priority": "5",
					"decision": "IGNORE"
				},
				{
					"status": "REVOKED",
					"priority": "10",
					"decision": "DENY"
				}
			]
		},
		"jwt": {
			"algorithms": ["ES256", "ES384", "ES521"],
			"duration": 30,
			"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
			"signingCerts": {
				"DN": "CN=StrongKey KeyAppliance,O=StrongKey",
				"certsPerServer": 3
			}
		}
	}
}
      
      , null, 2

      );

  public static restrictedAppleJson = JSON.stringify({
	"FidoPolicy": {
		"name": "RestrictedSKFSPolicy-Apple",
		"copyright": "StrongAuth, Inc. (DBA StrongKey) All Rights Reserved",
		"version": "2.0",
		"startDate": "1624920988",
		"endDate": "1760103870871",
		"system": {
			"requireCounter": "mandatory",
			"integritySignatures": true,
			"userVerification": ["required"],
			"userPresenceTimeout": 30,
			"allowedAaguids": ["f24a8e70-d0d3-f82c-2937-32523cc4de5a"],
			"jwtKeyValidity": 365,
			"jwtRenewalWindow": 30,
			"transport": ["usb", "internal"]
		},
		"algorithms": {
			"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
			"rsa": ["none"],
			"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
		},
		"attestation": {
			"conveyance": ["direct"],
			"formats": ["apple"]
		},
		"registration": {
			"displayName": "required",
			"attachment": ["platform "],
			"discoverableCredential": ["required"],
			"excludeCredentials": "enabled"
		},
		"authentication": {
			"allowCredentials": "enabled"
		},
		"authorization": {
			"maxdataLength": 256,
			"preserve": true
		},
		"rp": {
			"id": "strongkey.com",
			"name": "FIDOServer"
		},
		"extensions": {
			"uvm": {
				"allowedMethods": [
					"presence",
					"fingerprint",
					"passcode",
					"voiceprint",
					"faceprint",
					"eyeprint",
					"pattern",
					"handprint"
				],
				"allowedKeyProtections": ["hardware", "secureElement"],
				"allowedProtectionTypes": ["tee", "chip"]
			},
			"largeBlobSupport": "preferred"
		},
		"mds": {
			"authenticatorStatusReport": [{
					"status": "FIDO_CERTIFIED_L1",
					"priority": "1",
					"decision": "IGNORE"
				},
				{
					"status": "FIDO_CERTIFIED_L2",
					"priority": "1",
					"decision": "ACCEPT"
				},
				{
					"status": "UPDATE_AVAILABLE",
					"priority": "5",
					"decision": "IGNORE"
				},
				{
					"status": "REVOKED",
					"priority": "10",
					"decision": "DENY"
				}
			]
		},
		"jwt": {
			"algorithms": ["ES256", "ES384", "ES521"],
			"duration": 30,
			"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
			"signingCerts": {
				"DN": "CN=StrongKey KeyAppliance,O=StrongKey",
				"certsPerServer": 3
			}
		}
	}
}
     
      , null, 2

      );
 public static restrictedFipsJson = JSON.stringify({
	"FidoPolicy": {
		"name": "RestrictedSKFSPolicy-FIPS",
		"copyright": "StrongAuth, Inc. (DBA StrongKey) All Rights Reserved",
		"version": "2.0",
		"startDate": "1624920988",
		"endDate": "1760103870871",
		"system": {
			"requireCounter": "mandatory",
			"integritySignatures": true,
			"userVerification": ["required"],
			"userPresenceTimeout": 30,
			"allowedAaguids": ["c1f9a0bc-1dd2-404a-b27f-8e29047a43fd"],
			"jwtKeyValidity": 365,
			"jwtRenewalWindow": 30,
			"transport": ["usb", "internal"]
		},
		"algorithms": {
			"curves": ["secp256r1", "secp384r1", "secp521r1", "curve25519"],
			"rsa": ["none"],
			"signatures": ["ES256", "ES384", "ES512", "EdDSA", "ES256K"]
		},
		"attestation": {
			"conveyance": ["direct"],
			"formats": ["packed"]
		},
		"registration": {
			"displayName": "required",
			"attachment": ["cross-platform"],
			"discoverableCredential": ["required","preferred","discouraged"],
			"excludeCredentials": "enabled"
		},
		"authentication": {
			"allowCredentials": "enabled"
		},
		"authorization": {
			"maxdataLength": 256,
			"preserve": true
		},
		"rp": {
			"id": "strongkey.com",
			"name": "FIDOServer"
		},
		"extensions": {
			"uvm": {
				"allowedMethods": [
					"presence",
					"fingerprint",
					"passcode",
					"voiceprint",
					"faceprint",
					"eyeprint",
					"pattern",
					"handprint"
				],
				"allowedKeyProtections": ["hardware", "secureElement"],
				"allowedProtectionTypes": ["tee", "chip"]
			},
			"largeBlobSupport": "preferred"
		},
		"mds": {
			"authenticatorStatusReport": [{
					"status": "FIDO_CERTIFIED_L1",
					"priority": "1",
					"decision": "IGNORE"
				},
				{
					"status": "FIDO_CERTIFIED_L2",
					"priority": "1",
					"decision": "ACCEPT"
				},
				{
					"status": "UPDATE_AVAILABLE",
					"priority": "5",
					"decision": "IGNORE"
				},
				{
					"status": "REVOKED",
					"priority": "10",
					"decision": "DENY"
				}
			]
		},
		"jwt": {
			"algorithms": ["ES256", "ES384", "ES521"],
			"duration": 30,
			"required": ["rpid", "iat", "exp", "cip", "uname", "agent"],
			"signingCerts": {
				"DN": "CN=StrongKey KeyAppliance,O=StrongKey",
				"certsPerServer": 3
			}
		}
	}
}
     
         , null, 2  );
}
