/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.utilities.SKFSConstants;

import javax.json.JsonObject;

public class DefinedExtensionsPolicyOptions {
    private final UVMPolicydOptions uvm;
    private final String largeBlob;
    
    public DefinedExtensionsPolicyOptions(UVMPolicydOptions uvm,
    String largeBlob){
        this.uvm = uvm;
        this.largeBlob = largeBlob;
    }
    
    public UVMPolicydOptions getUVM(){
        return uvm;
    }
    public String getLargeBlob(){
        return largeBlob;
    }

    public static DefinedExtensionsPolicyOptions parse(JsonObject extensionsJson){
        UVMPolicydOptions uvm = null;
        if(extensionsJson.containsKey(SKFSConstants.POLICY_ATTR_EXTENSIONS_UVM)){
            uvm = UVMPolicydOptions.parse(extensionsJson.getJsonObject(SKFSConstants.POLICY_ATTR_EXTENSIONS_UVM));
        }
        String largeBlob = null;
        if(extensionsJson.containsKey(SKFSConstants.POLICY_ATTR_EXTENSIONS_LARGE_BLOB)){
            largeBlob = extensionsJson.getString(SKFSConstants.POLICY_ATTR_EXTENSIONS_LARGE_BLOB);
        }
            
        return new ExtensionsPolicyOptionsBuilder(uvm,largeBlob).build();
    }
    
    public static class ExtensionsPolicyOptionsBuilder{
        private final UVMPolicydOptions uvmBuilder;
        private final String largeBlobBuilder;

        public ExtensionsPolicyOptionsBuilder(UVMPolicydOptions uvmBuilder,
            String largeBlobBuilder){
            this.uvmBuilder = uvmBuilder;
            this.largeBlobBuilder = largeBlobBuilder;
        }
        
        public DefinedExtensionsPolicyOptions build(){
            return new DefinedExtensionsPolicyOptions(uvmBuilder,largeBlobBuilder);
        }
    
        
        
        
    }
    
}
