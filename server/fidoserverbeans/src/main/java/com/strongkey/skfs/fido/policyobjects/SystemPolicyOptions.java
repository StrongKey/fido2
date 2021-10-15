/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSCommon;
import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.ArrayList;
import java.util.stream.Collectors;
import javax.json.JsonObject;
import javax.json.JsonString;

/**
 *
 * @author dbeach
 */
public class SystemPolicyOptions {
    private final String requireCounter;
    private final ArrayList<String> userVerification;
    private final Integer userPresenceTimeout;
    private final Boolean storeSignatures;
    private final TrustedAuthenticatorPolicyOptions aaguids;
    private final Integer jwtRenewalWindow;
    private final Integer jwtKeyValidity;
    private final ArrayList<String> transport;
    
    public SystemPolicyOptions(String requireCounter,ArrayList<String> userVerification,
            Integer userPresenceTimeout,Boolean storeSignatures,
            TrustedAuthenticatorPolicyOptions aaguids,Integer jwtRenewalWindow,
            Integer jwtKeyValidity,ArrayList<String> transport){
        this.requireCounter = requireCounter;
        this.userVerification = userVerification;
        this.userPresenceTimeout = userPresenceTimeout;
        this.storeSignatures = storeSignatures;
        this.aaguids = aaguids;
        this.jwtRenewalWindow = jwtRenewalWindow;
        this.jwtKeyValidity = jwtKeyValidity;
        this.transport = transport;
     
    }
    public String getCounterRequirement(){
        return requireCounter;
    }
    public  ArrayList<String> getUserVerification(){
        return userVerification;
    }
    public Integer getUserPresenceTimeout(){
        return userPresenceTimeout;
    }
    public  Boolean getStoreSignatures(){
        return storeSignatures;
    }
    public  ArrayList<String> getAllowedAAGUIDs(){
        return aaguids.getAllowedAAGUIDs();
    }
    public  Integer getJwtRenewalWindow(){
        return jwtRenewalWindow;
    }
    public  Integer getJwtKeyValidity(){
        return jwtKeyValidity;
    }
    public  ArrayList<String> getAllowedTransports(){
        return transport;
    }
    
    
    public static SystemPolicyOptions parse(JsonObject systemJson){
        ArrayList<String> transports = null;
        if (systemJson.containsKey(SKFSConstants.POLICY_SYSTEM_TRANSPORT)){
            transports = new ArrayList<>(systemJson.getJsonArray(SKFSConstants.POLICY_SYSTEM_TRANSPORT).stream()
                .map(x -> (JsonString) x)
                .map(x -> x.getString())
                .collect(Collectors.toList()));
        }
        return new SystemPolicyOptionsBuilder(
        systemJson.getString(SKFSConstants.POLICY_ATTR_COUNTER),
        new ArrayList<>(systemJson.getJsonArray(SKFSConstants.POLICY_SYSTEM_USER_VERIFICATION).stream()
                .map(x -> (JsonString) x)
                .map(x -> x.getString())
                .collect(Collectors.toList())),
        systemJson.getInt(SKFSConstants.POLICY_SYSTEM_USER_PRESENCE_TIMEOUT),
        SKFSCommon.handleNonExistantJsonBoolean(systemJson, SKFSConstants.POLICY_ATTR_STORESIGNATURES),
        TrustedAuthenticatorPolicyOptions.parse(systemJson),
        systemJson.getInt(SKFSConstants.POLICY_JWT_RENEWAL),
        systemJson.getInt(SKFSConstants.POLICY_JWT_KEY_VALIDITY),
        transports
        ).build();
    }
    public static class SystemPolicyOptionsBuilder{
        private final String requireCounterBuilder;
        private final ArrayList<String> userVerificationBuilder;
        private final Integer userPresenceTimeoutBuilder;
        private final Boolean storeSignaturesBuilder;
        private final TrustedAuthenticatorPolicyOptions aaguidsBuilder;
        private final Integer jwtRenewalWindowBuilder;
        private final Integer jwtKeyValidityBuilder;
        private final ArrayList<String> transportBuilder;
        public SystemPolicyOptionsBuilder(String requireCounterBuilder,ArrayList<String> userVerificationBuilder,
            Integer userPresenceTimeoutBuilder,Boolean storeSignaturesBuilder,
            TrustedAuthenticatorPolicyOptions aaguidsBuilder,Integer jwtRenewalWindowBuilder,
            Integer jwtKeyValidityBuilder,ArrayList<String> transportBuilder) {
            this.requireCounterBuilder = requireCounterBuilder;
            this.userVerificationBuilder = userVerificationBuilder;
            this.userPresenceTimeoutBuilder = userPresenceTimeoutBuilder;
            this.storeSignaturesBuilder = storeSignaturesBuilder;
            this.aaguidsBuilder = aaguidsBuilder;
            this.jwtRenewalWindowBuilder = jwtRenewalWindowBuilder;
            this.jwtKeyValidityBuilder = jwtKeyValidityBuilder;
            this.transportBuilder = transportBuilder;
        }
        public SystemPolicyOptions build(){
            return new SystemPolicyOptions(requireCounterBuilder, userVerificationBuilder,
                    userPresenceTimeoutBuilder,storeSignaturesBuilder,aaguidsBuilder,
                    jwtRenewalWindowBuilder,jwtKeyValidityBuilder, transportBuilder);
        }
        
    }
    
}
