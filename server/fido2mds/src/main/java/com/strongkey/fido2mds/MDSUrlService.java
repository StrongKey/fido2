/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strongkey.appliance.objects.JWT;
import com.strongkey.crypto.utility.cryptoCommon;
import com.strongkey.fido2mds.data.Storage;
import com.strongkey.fido2mds.jws.MDSJwtVerifier;
import com.strongkey.fido2mds.structures.MetadataStatement;
import com.strongkey.fido2mds.structures.MetadataTOC;
import com.strongkey.fido2mds.structures.MetadataTOCPayload;
import com.strongkey.fido2mds.structures.MetadataTOCPayloadEntry;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.ResolverStyle;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.util.Base64Utils;
import org.springframework.web.client.RestTemplate;

class MDSUrlService extends MDSService {

    private static final Logger logger = Logger.getLogger(MDSUrlService.class.getName());

    private final String url;
    private final String token;
    private final String namespace;

    private final RestTemplate restTemplate;
    private final MDSJwtVerifier jwtVerifier;
    private ObjectMapper objectMapper;
    private Storage storage;

    private static final String TOC_FILE = "mds_toc.ser";
    private static final String DEFAULT_FIDO_METADATA_SERVICE_ROOT_CERTIFICATE_CLASSPATH = "classpath:metadata/certs/FAKERootFAKE.crt";
    private static final String PRODUCTION_FIDO_METADATA_SERVICE_ROOT_CERTIFICATE_CLASSPATH = "classpath:metadata/certs/FIDOMetadataService.cer";
    
    public MDSUrlService(String url, String token, ObjectMapper objectMapper, Storage storage) {
        super();
        
        this.url = url;
        this.token = token;
        this.storage = storage;
        this.objectMapper = objectMapper;

        String unique = "";
        try {
            URI tempUrl = new URI(url);
            unique = tempUrl.getHost() + tempUrl.getPath();
        } catch (URISyntaxException ex) {
            logger.log(Level.SEVERE, null, ex);
        }
        namespace = Base64.getUrlEncoder().withoutPadding().encodeToString(unique.getBytes());

        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        restTemplate = new RestTemplate(httpComponentsClientHttpRequestFactory);
        
        //TODO remove hardcoded solution to use right cert for production MDS
        if(token != null){
            jwtVerifier = new MDSJwtVerifier(retriveRootX509Certificate(PRODUCTION_FIDO_METADATA_SERVICE_ROOT_CERTIFICATE_CLASSPATH));
        }
        else{
            jwtVerifier = new MDSJwtVerifier(retriveRootX509Certificate(DEFAULT_FIDO_METADATA_SERVICE_ROOT_CERTIFICATE_CLASSPATH));
        }
    }

    @Override
    public void refresh() {
        try{
            MetadataTOC toc = retrieveMetadataTOC(false);
            LocalDate update = LocalDate.parse(toc.getPayload().getNextUpdate(), 
                    DateTimeFormatter.ofPattern("y-M-d").withResolverStyle(ResolverStyle.LENIENT));
            // TODO: Could make this happen sooner or more frequent
            if (update.isEqual(LocalDate.now())||update.isBefore(LocalDate.now()))
                toc = retrieveMetadataTOC(true);

            // TODO: Other algorithms will be needed
            String algo = toc.getJwt().getHeader().getString("alg", null);
            if ("ES256".compareTo(algo) != 0) {
                logger.severe("ERROR: Unknown algo: " + algo);
            }

            MessageDigest digest;

            try {
                digest = MessageDigest.getInstance("SHA-256");
            } catch (NoSuchAlgorithmException ex) {
                logger.log(Level.SEVERE, "Could not create message digest", ex);
                return;
            }

            Map<String,MetadataTOCPayloadEntry> localTocEntryMap = new HashMap<>();
            Map<String,MetadataStatement> localMetadataStatementMap = new HashMap<>();

            for (MetadataTOCPayloadEntry entry : toc.getPayload().getEntries()) {
                if (entry.getAaguid() == null) {
                    // TODO: not a fido 2 entry...should we worry about uaf entries?
                    continue;
                }
                MetadataStatement st;
                try {
                    st = retrieveMetadataStatement(new URI(entry.getUrl()), entry.getHash(), digest, false);
                } catch (Exception e) {
                    logger.severe("Error: " + e.getMessage());
                    continue;
                }
                logger.fine(entry.getAaguid()+" "+entry.getStatusReports().get(0).getStatus().name());
                localMetadataStatementMap.put(entry.getAaguid().getAAGUID(), st);
                localTocEntryMap.put(entry.getAaguid().getAAGUID(), entry);
            }

            metadataStatementMap = localMetadataStatementMap;
            tocEntryMap = localTocEntryMap;
        }
        catch(Exception ex){
            logger.log(Level.SEVERE, "Metadata Service exception", ex);
            metadataStatementMap = new HashMap<>();
            tocEntryMap = new HashMap<>();
        }
    }
    
    private X509Certificate retriveRootX509Certificate(String rootCertificateClassPath){
        try{
            ResourceLoader resourceLoader = new DefaultResourceLoader();
            return cryptoCommon.generateX509FromInputStream(resourceLoader
                    .getResource(rootCertificateClassPath)
                    .getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
            Logger.getLogger(MDSUrlService.class.getName()).log(Level.SEVERE, null, ex);
            throw new IllegalArgumentException(ex);
        }
    }

    private MetadataTOC retrieveMetadataTOC(boolean forceUpdate) throws CertificateException, NoSuchProviderException, UnsupportedEncodingException {
        MetadataTOC toc = new MetadataTOC();
        String data = null;
        System.out.println("MDS url: " + url);

        if (!forceUpdate) {
            data = storage.loadData(namespace, TOC_FILE);
        }

        if (data == null) {
            String fullUrl = addToken(url);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(fullUrl, String.class);
            data = responseEntity.getBody();
            storage.saveData(namespace, TOC_FILE, data);
        }
        JWT jwt = new JWT(data);

        //Verify JWS Signature
        jwtVerifier.verify(jwt);
        toc.setJWT(jwt);

        String payloadString = jwt.getBody().toString();
        System.out.println("Payload String: " + payloadString);
        MetadataTOCPayload payload = null;
        try {
            payload = objectMapper.readValue(payloadString, MetadataTOCPayload.class);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        toc.setPayload(payload);
        return toc;
    }

    private MetadataStatement retrieveMetadataStatement(URI uri, String entryHash, MessageDigest digest, boolean forceUpdate) throws Exception {
        String filename = filenameFromURI(uri);
        String data = null;

        if (!forceUpdate) {
            data = storage.loadData(namespace, filename);
        }

        if (data == null) {
            String url = URLDecoder.decode(uri.toString(), StandardCharsets.UTF_8.name());
            url = addToken(url);
            url = url.replaceAll("#", "%23");
            URI realURI = URI.create(url);
            ResponseEntity<String> responseEntity = restTemplate.getForEntity(realURI, String.class);
            data = responseEntity.getBody();
            
            if(data == null){
                logger.severe("Null hash.");
                throw new Exception("Null hash");
            }

            String hash = Base64.getUrlEncoder().withoutPadding().encodeToString(digest.digest(data.getBytes()));
            if (!hash.equals(entryHash)) {
                logger.log(Level.SEVERE, "Bad hash. {0} != {1}  Skipping ", new Object[]{hash, entryHash});
                throw new Exception("Bad hash");
            }
            storage.saveData(namespace, filename, data);
        }

        String decoded;
        try {
            decoded = new String(Base64Utils.decodeFromUrlSafeString(data), StandardCharsets.UTF_8);
        } catch (Exception e) {
            // TODO: known bug: test server does not base64 this info
            decoded = data;
        }
        return objectMapper.readValue(decoded, MetadataStatement.class);

    }

    private String filenameFromURI(URI uri) {
        String path = uri.getPath();
        int index = path.lastIndexOf("/");
        if (index > -1) {
            path = path.substring(index + 1);
        }
        return path; //+".html";
    }

    private String addToken(String url) {
        if (token != null && token.length() != 0) {
            url += "?token=" + token;
        }
        return url;
    }

}
