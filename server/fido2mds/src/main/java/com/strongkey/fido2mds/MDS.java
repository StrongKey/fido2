/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.strongkey.skce.pojos.MDSClient;
import com.strongkey.skce.pojos.MDSEndpoint;
import com.strongkey.fido2mds.data.MemoryStorage;
import com.strongkey.fido2mds.data.Storage;
import com.strongkey.fido2mds.structures.AuthenticatorStatus;
import com.strongkey.fido2mds.structures.EcdaaTrustAnchor;
import com.strongkey.fido2mds.structures.MetadataStatement;
import com.strongkey.fido2mds.structures.MetadataTOCPayloadEntry;
import com.strongkey.fido2mds.structures.StatusReport;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Schedule;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

public class MDS implements MDSClient {

    private List<MDSService> mdsList;
    private ObjectMapper objectMapper;
    private Storage storage;
    
    public MDS(List<MDSEndpoint> endpoints){
        storage = new MemoryStorage();
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mdsList = endpoints.stream()
                .map(x -> new MDSUrlService(x.getUrl(), x.getToken(), objectMapper, storage))
                .collect(Collectors.toList());
        mdsList.add(new MDSResourceMetadataService(objectMapper));
        refresh();
    }

//    private void initCommon() {
//        objectMapper = new ObjectMapper();
//        objectMapper.registerModule(new JavaTimeModule());
//
//        //TODO: Read these from config file
//        mdsList = Arrays.asList(
//                new MDSUrlService("https://fidoalliance.co.nz/mds/execute/630b8b46f87c56aab2283bf3a7ab100bc03972a20f675f8c90becb8e09b2a66c",
//                        objectMapper, storage),
//                new MDSUrlService("https://fidoalliance.co.nz/mds/execute/642155410783c5f44e21b75926e4ba0bed0f44d656d556ade382ffdbd93ddfa1",
//                        objectMapper, storage),
//                //                new MDSService("https://mds2.fidoalliance.org/",
//                //                        "b1e8c9030cf0e30f097d7032c009347f6693210d931a3027",
//                //                        "classpath:metadata/certs/MDS2Cert.cer",
//                //                        objectMapper, storage),
//                new MDSUrlService("https://fidoalliance.co.nz/mds/execute/8a001d4eb41df169c792b241b59b4c4404812bdec4a7880be9eaa7569ed6ec29",
//                        objectMapper, storage),
//                new MDSUrlService("https://fidoalliance.co.nz/mds/execute/b67ac8a96f3f560879e20729f74249a798e43c56de775457caa70dbd23ebe637",
//                        objectMapper, storage),
//                new MDSUrlService("https://fidoalliance.co.nz/mds/execute/fedc050e4f90ada3d3bfcab6eb157b1d3b48e86cb251c2aaeb72711df8e569d8",
//                        objectMapper, storage),
//                new MDSResourceMetadataService(objectMapper)
//        );
//        refresh();
//    }

    // TODO: make this configurable
    @Schedule(hour = "3")
    @Lock(LockType.WRITE)
    private void refresh() {
        List<MDSService> newList = new ArrayList<>();
        
        for (MDSService service : mdsList) {
            try {
                service.refresh();
                newList.add(service);
            } catch (Exception e) {
                // if there's an exception, it won't get added to the new list
            }
        }
        mdsList = newList;
    }

    @Override
    @Lock(LockType.READ)
    public JsonObject getTrustAnchors(String aaguid, List<String> allowedStatusList) {
        JsonObjectBuilder ret = Json.createObjectBuilder();
        JsonArrayBuilder errors = Json.createArrayBuilder();
        JsonObjectBuilder error = Json.createObjectBuilder();
        MetadataTOCPayloadEntry entry = null;
        MetadataStatement st = null;
        
        for (MDSService service : mdsList) {
            entry = service.getTOCEntry(aaguid);
            if (entry!=null) {
                for (StatusReport status : entry.getStatusReports()) {
                    if (status.getStatus()==AuthenticatorStatus.ATTESTATION_KEY_COMPROMISE || 
                            status.getStatus()==AuthenticatorStatus.REVOKED || 
                            status.getStatus()==AuthenticatorStatus.USER_KEY_PHYSICAL_COMPROMISE ||
                            status.getStatus()==AuthenticatorStatus.USER_KEY_REMOTE_COMPROMISE || 
                            status.getStatus()==AuthenticatorStatus.USER_VERIFICATION_BYPASS 
                            ) {
                        error.add("message", "Authenticator status = "+status.getStatus().name());
                        errors.add(error);
                    }
                    else if(!allowedStatusList.contains(status.getStatus().name())){
                        error.add("message", "Authenticator status = " + status.getStatus().name() + "not allowed by policy");
                        errors.add(error);
                    }
                }
            }

            st = service.getMetadataStatement(aaguid);
            if (st != null) {
                List<String> attestationRootCertificates = st.getAttestationRootCertificates();
                if (attestationRootCertificates != null) {
                    JsonArrayBuilder certs = Json.createArrayBuilder();
                    for (String c : attestationRootCertificates) {
                        certs.add(c);
                        System.out.println("Certificate found : " + c);
                    }

                    ret.add("attestationRootCertificates", certs);
                }

                List<EcdaaTrustAnchor> ecdaaTrustAnchors = st.getEcdaaTrustAnchors();
                if (ecdaaTrustAnchors != null) {
                    JsonArrayBuilder trustAnchors = Json.createArrayBuilder();
                    for (EcdaaTrustAnchor t : ecdaaTrustAnchors) {
                        try {
                            trustAnchors.add(objectMapper.writeValueAsString(t));
                        } catch (JsonProcessingException ex) {
                            Logger.getLogger(MDS.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    ret.add("ecdaaTrustAnchors", trustAnchors);
                }
                break;
            }
        }
        if (entry==null && st==null) {
            error.add("message", "Could not find metadata for aaguid "+aaguid);
            errors.add(error);
        }
        ret.add("errors", errors);
        return ret.build();
    }
}

/*

If validation is successful, obtain a list of acceptable trust anchors (attestation root certificates or ECDAA-Issuer public keys) 
for that attestation type and attestation statement format fmt, from a trusted source or from policy. For example, the FIDO Metadata 
Service [FIDOMetadataService] provides one way to obtain such information, using the aaguid in the attestedCredentialData in authData.

The FIDO Server looks up the metadata statement for the particular authenticator model. 
If the metadata statement lists an attestation certificate(s), it verifies that an 
attestation signature is present, and made with the private key corresponding to either 
    (a) one of the certificates listed in this metadata statement or 
    (b) corrsponding to the public key in a certificate that chains to one of the issuer certificates listed in the authenticator's metadata statement.


The FIDO Server MUST follow these processing rules:

    The FIDO Server MUST be able to download the latest metadata TOC object from the well-known URL, when appropriate. 
    The nextUpdate field of the Metadata TOC specifies a date when the download SHOULD occur at latest.
    If the x5u attribute is present in the JWT Header, then:
        The FIDO Server MUST verify that the URL specified by the x5u attribute has the same web-origin as the URL used 
            to download the metadata TOC from. The FIDO Server SHOULD ignore the file if the web-origin differs (in order to prevent 
            loading objects from arbitrary sites).
        The FIDO Server MUST download the certificate (chain) from the URL specified by the x5u attribute [JWS]. The certificate 
            chain MUST be verified to properly chain to the metadata TOC signing trust anchor according to [RFC5280]. 
            All certificates in the chain MUST be checked for revocation according to [RFC5280].
        The FIDO Server SHOULD ignore the file if the chain cannot be verified or if one of the chain certificates is revoked.
    If the x5u attribute is missing, the chain should be retrieved from the x5c attribute. If that attribute is missing as well, 
        Metadata TOC signing trust anchor is considered the TOC signing certificate chain.
    Verify the signature of the Metadata TOC object using the TOC signing certificate chain (as determined by the steps above). 
        The FIDO Server SHOULD ignore the file if the signature is invalid. It SHOULD also ignore the file if its 
            number (no) is less or equal to the number of the last Metadata TOC object cached locally.
    Write the verified object to a local cache as required.
    Iterate through the individual entries (of type MetadataTOCPayloadEntry). For each entry:
        Ignore the entry if the AAID, AAGUID or attestationCertificateKeyIdentifiers is not relevant to the relying party (e.g. not acceptable by any policy)
        Download the metadata statement from the URL specified by the field url. Some authenticator vendors 
            might require authentication in order to provide access to the data. Conforming FIDO Servers SHOULD support the 
            HTTP Basic, and HTTP Digest authentication schemes, as defined in [RFC2617].
        Check whether the status report of the authenticator model has changed compared to the cached entry by looking at the fields timeOfLastStatusChange and statusReport. Update the status of the cached entry. It is up to the relying party to specify behavior for authenticators with status reports that indicate a lack of certification, or known security issues. However, the status REVOKED indicates significant security issues related to such authenticators.

        Note
        Authenticators with an unacceptable status should be marked accordingly. This information is required for building registration 
            and authentication policies included in the registration request and the authentication request [UAFProtocol].

        Compute the hash value of the (base64url encoding without padding of the UTF-8 encoded) metadata statement downloaded from the 
            URL and verify the hash value to the hash specified in the field hash of the metadata TOC object. Ignore the downloaded 
            metadata statement if the hash value doesn't match.
        Update the cached metadata statement according to the dowloaded one.




3.1.  "alg" (Algorithm) Header Parameter Values for JWS

   The table below is the set of "alg" (algorithm) Header Parameter
   values defined by this specification for use with JWS, each of which
   is explained in more detail in the following sections:

   +--------------+-------------------------------+--------------------+
   | "alg" Param  | Digital Signature or MAC      | Implementation     |
   | Value        | Algorithm                     | Requirements       |
   +--------------+-------------------------------+--------------------+
   | HS256        | HMAC using SHA-256            | Required           |
   | HS384        | HMAC using SHA-384            | Optional           |
   | HS512        | HMAC using SHA-512            | Optional           |
   | RS256        | RSASSA-PKCS1-v1_5 using       | Recommended        |
   |              | SHA-256                       |                    |
   | RS384        | RSASSA-PKCS1-v1_5 using       | Optional           |
   |              | SHA-384                       |                    |
   | RS512        | RSASSA-PKCS1-v1_5 using       | Optional           |
   |              | SHA-512                       |                    |
   | ES256        | ECDSA using P-256 and SHA-256 | Recommended+       |
   | ES384        | ECDSA using P-384 and SHA-384 | Optional           |
   | ES512        | ECDSA using P-521 and SHA-512 | Optional           |
   | PS256        | RSASSA-PSS using SHA-256 and  | Optional           |
   |              | MGF1 with SHA-256             |                    |
   | PS384        | RSASSA-PSS using SHA-384 and  | Optional           |
   |              | MGF1 with SHA-384             |                    |
   | PS512        | RSASSA-PSS using SHA-512 and  | Optional           |
   |              | MGF1 with SHA-512             |                    |
   | none         | No digital signature or MAC   | Optional           |
   |              | performed                     |                    |
   +--------------+-------------------------------+--------------------+
 */
