/**
 * Copyright StrongAuth, Inc. All Rights Reserved.
 *
 * Use of this source code is governed by the Gnu Lesser General Public License 2.3.
 * The license can be found at https://github.com/StrongKey/fido2/LICENSE
 */

package com.strongkey.apiws.rest;

import com.strongkey.apiws.utility.PATCH;
import com.strongkey.auth.txbeans.authenticateRestRequestBeanLocal;
import com.strongkey.skfs.policybeans.addFidoPolicyLocal;
import com.strongkey.skfs.policybeans.deleteFidoPolicyLocal;
import com.strongkey.skfs.policybeans.getFidoPolicyLocal;
import com.strongkey.skfs.policybeans.updateFidoPolicyLocal;
import com.strongkey.skfs.requests.CreateFidoPolicyRequest;
import com.strongkey.skfs.requests.PatchFidoPolicyRequest;
import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

@Stateless
@Path("/domains/{did}/fidopolicies")
public class FidoAdminServlet {
    
    @javax.ws.rs.core.Context private HttpServletRequest request;
    @EJB authenticateRestRequestBeanLocal authRest;
    @EJB addFidoPolicyLocal addpolicybean;
    @EJB getFidoPolicyLocal getpolicybean;
    @EJB updateFidoPolicyLocal updatepolicybean;
    @EJB deleteFidoPolicyLocal deletepolicybean;
    
    @POST
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response createFidoPolicy(CreateFidoPolicyRequest createfidopolicy,
                                     @PathParam("did") Long did) {
        
        if (!authRest.execute(did, request, createfidopolicy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return addpolicybean.execute(did, createfidopolicy);
    }
    
    @GET
    @Produces({"application/json"})
    public Response getFidoPolicy(@PathParam("did") Long did,
                                  @QueryParam("pid") String sidpid,
                                  @QueryParam("metadataonly") Boolean metadataonly) {

        if (!authRest.execute(did, request, null)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return getpolicybean.getPolicies(did, sidpid, metadataonly);
    }
    
    @PATCH
    @Path("/{pid}")
    @Consumes({"application/merge-patch+json"})
    @Produces({"application/json"})
    public Response updateFidoPolicy(PatchFidoPolicyRequest patchfidopolicy,
                                     @PathParam("did") Long did,
                                     @PathParam("pid") String pid) {

        if (!authRest.execute(did, request, patchfidopolicy)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return updatepolicybean.execute(did, pid, patchfidopolicy);
    }
    
    @DELETE
    @Path("/{pid}")
    @Produces({"application/json"})
    public Response deleteFidoPolicy(@PathParam("did") Long did,
                                     @PathParam("pid") String pid) {

        if (!authRest.execute(did, request, null)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return deletepolicybean.execute(did, pid);
    }
}
