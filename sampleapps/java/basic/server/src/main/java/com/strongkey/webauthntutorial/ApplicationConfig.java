/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the GNU Lesser General Public License v2.1
 * The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
 */

package com.strongkey.webauthntutorial;

import java.util.Set;
import javax.ws.rs.core.Application;

// Auto generated file used to configure application with JEE7 application server
@javax.ws.rs.ApplicationPath("/fido2")
public class ApplicationConfig extends Application {

    @Override
    public Set<Class<?>> getClasses() {
        Set<Class<?>> resources = new java.util.HashSet<>();
        addRestResourceClasses(resources);
        return resources;
    }

    /**
     * Do not modify addRestResourceClasses() method.
     * It is automatically populated with
     * all resources defined in the project.
     * If required, comment out calling this method in getClasses().
     */
    private void addRestResourceClasses(Set<Class<?>> resources) {
        resources.add(com.strongkey.Filters.CrossOriginResourceSharingFilter.class);
        resources.add(com.strongkey.webauthntutorial.WebauthnService.class);
    }

}
