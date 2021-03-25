/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/

package com.strongkey.skfs.fido.policyobjects;

import com.strongkey.skfs.fido.policyobjects.extensions.ExampleFido2Extension;
import com.strongkey.skfs.fido.policyobjects.extensions.Fido2Extension;
import com.strongkey.skfs.utilities.SKFSConstants;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.json.JsonObject;

public class ExtensionsPolicyOptions {
    private static final Set<String> KNOWNEXTENSIONS = new HashSet<>(
            Arrays.asList(new String[]{
                SKFSConstants.POLICY_EXTENSIONS_EXAMPLE,
                SKFSConstants.POLICY_EXTENSIONS_APPID
            })
    );

    private final Set<Fido2Extension> extensions;

    public ExtensionsPolicyOptions(Set<Fido2Extension> extensions){
        this.extensions = extensions;
    }

    public static ExtensionsPolicyOptions parse(JsonObject extensionsJson){
        Set<Fido2Extension> extensions = new HashSet<>();

        if(extensionsJson == null){
            return new ExtensionsPolicyOptions(extensions);
        }

        for(String extensionIdentifier: extensionsJson.keySet()){
            if(!KNOWNEXTENSIONS.contains(extensionIdentifier)){
                throw new IllegalArgumentException("Unknown extension defined in policy");
            }

            Fido2Extension ext;
            switch(extensionIdentifier){
                case SKFSConstants.POLICY_EXTENSIONS_EXAMPLE:
                    ext = new ExampleFido2Extension();
                    break;
                case SKFSConstants.POLICY_EXTENSIONS_APPID:
                    ext = new ExampleFido2Extension();
                    break;
                default:
                    throw new IllegalArgumentException("Unknown exception defined in policy");
            }
            extensions.add(ext);
        }
        return new ExtensionsPolicyOptions(extensions);
    }

    //TODO ensure all Extensions are immutable to prevent issues with
    //shallow copying
    public Set<Fido2Extension> getExtensions(){
        return new HashSet(extensions);
    }
}
