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
public class UVMPolicydOptions {
    private final ArrayList<String> allowedMethods;
    private final ArrayList<String> allowedKeyProtections;
    private final ArrayList<String> allowedProtectionTypes;
    
    
    

    public UVMPolicydOptions(ArrayList<String> allowedMethods,
        ArrayList<String> allowedKeyProtections,
        ArrayList<String> allowedProtectionTypes){
        this.allowedMethods = allowedMethods;
        this.allowedKeyProtections = allowedKeyProtections;
        this.allowedProtectionTypes = allowedProtectionTypes;
        
    } 
    
    public  ArrayList<String> getAllowedMethods(){
        return allowedMethods;
    }
    public  ArrayList<String> getAllowedKeyProtections(){
        return allowedKeyProtections;
    }
    public  ArrayList<String> getAllowedProtectionTypes(){
        return allowedProtectionTypes;
    }
        
        
        
        public static UVMPolicydOptions parse(JsonObject uvmJson) {
            return new UVMPolicydOptionsBuilder(
                new ArrayList<>(uvmJson.getJsonArray(SKFSConstants.POLICY_ATTR_EXTENSIONS_UVM_METHODS).stream()
                    .map(x -> (JsonString) x)
                    .map(x -> x.getString())
                    .collect(Collectors.toList())),
                new ArrayList<>(uvmJson.getJsonArray(SKFSConstants.POLICY_ATTR_EXTENSIONS_UVM__KEY_PROTECTIONS).stream()
                    .map(x -> (JsonString) x)
                    .map(x -> x.getString())
                    .collect(Collectors.toList())),
                new ArrayList<>(uvmJson.getJsonArray(SKFSConstants.POLICY_ATTR_EXTENSIONS_UVM_KEY_PROTECTIONS_TYPES).stream()
                    .map(x -> (JsonString) x)
                    .map(x -> x.getString())
                    .collect(Collectors.toList()))     
            ).build();
        }
        
    
    public static class UVMPolicydOptionsBuilder{
        private  ArrayList<String> allowedMethodsBuilder;
        private  ArrayList<String> allowedKeyProtectionsBuilder;
        private  ArrayList<String> allowedProtectionTypesBuilder;
        
        public UVMPolicydOptionsBuilder(ArrayList<String> allowedMethodsBuilder,
        ArrayList<String> allowedKeyProtectionsBuilder,
        ArrayList<String> allowedProtectionTypesBuilder){
            this.allowedMethodsBuilder = allowedMethodsBuilder;
            this.allowedKeyProtectionsBuilder = allowedKeyProtectionsBuilder;
            this.allowedProtectionTypesBuilder = allowedProtectionTypesBuilder;
        
        } 
        public UVMPolicydOptions build(){
            return new UVMPolicydOptions(allowedMethodsBuilder,allowedKeyProtectionsBuilder,allowedProtectionTypesBuilder);
        }
        
        
    }
}
