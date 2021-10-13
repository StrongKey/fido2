/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.poc;

import com.strongkey.Filters.CrossOriginResourceSharingFilter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.core.Application;

// Auto generated file used to configure application with JEE7 application server
@javax.ws.rs.ApplicationPath("/fido2")
public class ApplicationConfig extends Application {

    @Override
     public Set<Class<?>> getClasses() {
        return new HashSet<>(Arrays.asList(WebauthnService.class,
                CrossOriginResourceSharingFilter.class));
    }
}
