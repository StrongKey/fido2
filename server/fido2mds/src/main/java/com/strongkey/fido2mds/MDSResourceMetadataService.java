/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.fido2mds;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.strongkey.fido2mds.structures.MetadataStatement;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

class MDSResourceMetadataService extends MDSService {

    private static final Logger logger = Logger.getLogger(MDSResourceMetadataService.class.getName());

    private static final String METADATA_LOC = "classpath:authenticator/metadata";
    private final ObjectMapper objectMapper;
    ResourceLoader resourceLoader;

    public MDSResourceMetadataService(ObjectMapper objectMapper) {
        super();
        resourceLoader = new DefaultResourceLoader();
        this.objectMapper = objectMapper;
    }

    @Override
    public void refresh() {
        // not used for this type of service
        tocEntryMap = new HashMap<>();
        
        logger.fine("####################### MDSResourceMetadataService #######################");
        Map<String, MetadataStatement> localMetadataStatementMap = new HashMap<>();

        try {
            Resource r = resourceLoader.getResource(METADATA_LOC+"/metadata.txt");

            if (r!=null) {
                InputStreamReader directoryStream = new InputStreamReader(r.getInputStream(), StandardCharsets.UTF_8);
                BufferedReader directoryReader = new BufferedReader(directoryStream);
                String item;
                while((item = directoryReader.readLine()) != null) {
                    System.out.println(item);
                    MetadataStatement st;
                    try {
                        logger.log(Level.FINE, "Loading item {0}", item);
                        st = retrieveMetadataStatement(item);
                    } catch (IOException e) {
                        logger.log(Level.SEVERE, "ResourceMetadataError: {0}", e.getMessage());
                        continue;
                    }
                    System.out.println("Added MDSResourceMetadataService Statement: " + st.getAaguid().getAAGUID());
                    localMetadataStatementMap.put(st.getAaguid().getAAGUID(), st);
                }
            }
            else{
                System.out.println("Resource is null");
            }
        } catch (IOException ex) {
            logger.log(Level.SEVERE, "Metadata Service exception: {0}", ex);
        } finally {
            metadataStatementMap = localMetadataStatementMap;
        }
    }

    private MetadataStatement retrieveMetadataStatement(String resource) throws IOException {
        Resource r = resourceLoader.getResource(METADATA_LOC+"/"+resource);
        try (InputStream inputStream = r.getInputStream()) {
            return objectMapper.readValue(inputStream, MetadataStatement.class);
        }
    }

}
