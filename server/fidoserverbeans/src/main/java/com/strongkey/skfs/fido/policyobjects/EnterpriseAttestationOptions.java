/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 *
 * @author dbeach
 */
public class EnterpriseAttestationOptions {
    private final String attestationType;
    private final ArrayList<String> authorizedRpid;
    private final ArrayList<String> authorizedTruststore;
    private final ArrayList<String> authorizedSerial;
    private final ArrayList<String> authorizedDN;
    private final String requiredOID;
    
    private EnterpriseAttestationOptions( String attestationType,
        ArrayList<String> authorizedRpid,
        ArrayList<String> authorizedTruststore,
        ArrayList<String> authorizedSerial,
        ArrayList<String> authorizedDN,
        String requiredOID){
        this.attestationType = attestationType;
        this.authorizedRpid = authorizedRpid;
        this.authorizedTruststore= authorizedTruststore;
        this.authorizedSerial= authorizedSerial;
        this.authorizedDN= authorizedDN;
        this.requiredOID=requiredOID;
        
    }
    
    public String getAttestationType(){
        return attestationType;
    }
    public ArrayList<String> getAuthorizedRpid(){
        return authorizedRpid;
    }
    public ArrayList<String> getAuthorizedTruststore(){
        return authorizedTruststore;
    }
    public ArrayList<String> getAuthorizedSerial(){
        return authorizedSerial;
    }
    public ArrayList<String> getAuthorizedDN(){
        return authorizedDN;
    }
    public String getRequiredOID(){
        return requiredOID;
    }
    
    
    
    static EnterpriseAttestationOptions parse(JsonObject enterpriseOptions) {

        return new EnterpriseAttestationOptionsBuilder(
        enterpriseOptions.getString(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE_ATTESTATION_TYPE),
        new ArrayList<>(enterpriseOptions.getJsonArray(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_RPID).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
        new ArrayList<>(enterpriseOptions.getJsonArray(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_TRUSTSTORE).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
        new ArrayList<>(enterpriseOptions.getJsonArray(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_SERIAL).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
        new ArrayList<>(enterpriseOptions.getJsonArray(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE_AUTHORIZED_DN).stream()
                        .map(x -> (JsonString) x)
                        .map(x -> x.getString())
                        .collect(Collectors.toList())),
        enterpriseOptions.getString(SKFSConstants.POLICY_ATTESTATION_ENTERPRISE_REQUIRED_OID)
        ).build();
        
    }

    private static class EnterpriseAttestationOptionsBuilder{
            private final String builderAttestationType;
            private final ArrayList<String> builderAuthorizedRpid;
            private final ArrayList<String> builderAuthorizedTruststore;
            private final ArrayList<String> builderAuthorizedSerial;
            private final ArrayList<String> builderAuthorizedDN;
            private final String builderRequiredOID;
            
            
            public EnterpriseAttestationOptionsBuilder(String builderAttestationType, ArrayList<String> builderAuthorizedRpid, ArrayList<String> builderAuthorizedTruststore,
            ArrayList<String> builderAuthorizedSerial, ArrayList<String> builderAuthorizedDN, String builderRequiredOID) {
                this.builderAttestationType = builderAttestationType;
                this.builderAuthorizedRpid = builderAuthorizedRpid;
                this.builderAuthorizedTruststore= builderAuthorizedTruststore;
                this.builderAuthorizedSerial= builderAuthorizedSerial;
                this.builderAuthorizedDN= builderAuthorizedDN;
                this.builderRequiredOID=builderRequiredOID;
            }
            public EnterpriseAttestationOptions build(){
                return new EnterpriseAttestationOptions(builderAttestationType,  builderAuthorizedRpid,builderAuthorizedTruststore,
                    builderAuthorizedSerial,builderAuthorizedDN, builderRequiredOID);
            }
    }
    
}
