/**
* Copyright StrongAuth, Inc. All Rights Reserved.
*
* Use of this source code is governed by the GNU Lesser General Public License v2.1
* The license can be found at https://github.com/StrongKey/fido2/blob/master/LICENSE
*/
package com.strongkey.apiws.rest;

import com.strongkey.apiws.utility.PATCH;
import com.strongkey.auth.txbeans.authenticateRestRequestBeanLocal;
import com.strongkey.skfs.requests.AuthenticationRequest;
import com.strongkey.skfs.requests.PreauthenticationRequest;
import com.strongkey.skfs.requests.PreregistrationRequest;
import com.strongkey.skfs.requests.RegistrationRequest;
import com.strongkey.skfs.requests.UpdateFidoKeyRequest;
import com.strongkey.skfs.txbeans.u2fServletHelperBeanLocal;
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

/**
 * REST based web services that serve FIDO U2F protocol based functionality.
 *
 */
@Stateless
@Path("/domains/{did}/fidokeys")
public class APIServlet {

    @javax.ws.rs.core.Context private HttpServletRequest request;
    @EJB u2fServletHelperBeanLocal u2fHelperBean;
    @EJB authenticateRestRequestBeanLocal authRest;

    public APIServlet() {
    }

    /**
     * Step-1 for fido authenticator registration. This methods generates a
     * challenge and returns the same to the caller, which typically is a
     * Relying Party (RP) application.
     *
     * @param preregistration - String The full body for auth purposes
     * @param did - Long value of the domain to service this request
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Reg Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @POST
    @Path("/registration/challenge")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response preregister(PreregistrationRequest preregistration,
                                @PathParam("did") Long did) {

        if (!authRest.execute(did, request, preregistration)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return u2fHelperBean.preregister(did, preregistration);
    }

    /**
     * Step-2 or last step of fido authenticator registration process. This
     * method receives the u2f registration response parameters which is
     * processed and the registration result is notified back to the caller.
     *
     * Both preregister and register methods are time linked. Meaning, register
     * should happen with in a certain time limit after the preregister is
     * finished; otherwise, the user session would be invalidated.
     *
     * @param registration - String The full body for auth purposes
     * @param did - Long value of the domain to service this request

     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @POST
    @Path("/registration")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response register(RegistrationRequest registration,
                             @PathParam("did") Long did) {

        if (!authRest.execute(did, request, registration)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return u2fHelperBean.register(did, registration);
    }

    /**
     * Step-1 for fido authenticator authentication. This methods generates a
     * challenge and returns the same to the caller.
     *
     * @param preauthentication- String The full body for auth purposes
     * @param did - Long value of the domain to service this request
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Challenge' : 'U2F Auth Challenge parameters; a json again' 2.
     * 'Message' : String, with a list of messages that explain the process. 3.
     * 'Error' : String, with error message incase something went wrong. Will be
     * empty if successful.
     */
    @POST
    @Path("/authentication/challenge")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response preauthenticate(PreauthenticationRequest preauthentication,
                                    @PathParam("did") Long did) {

        if (!authRest.execute(did, request, preauthentication)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return u2fHelperBean.preauthenticate(did, preauthentication);
    }

    /**
     * Step-2 or last step of fido authenticator authentication process. This
     * method receives the u2f authentication response parameters which is
     * processed and the authentication result is notified back to the caller.
     *
     * Both preauthenticate and authenticate methods are time linked. Meaning,
     * authenticate should happen with in a certain time limit after the
     * preauthenticate is finished; otherwise, the user session would be
     * invalidated.
     *
     * @param authentication - String The full body for auth purposes
     * @param did - Long value of the domain to service this request
     *
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : String, with a list of messages that
     * explain the process. 3. 'Error' : String, with error message incase
     * something went wrong. Will be empty if successful.
     */
    @POST
    @Path("/authentication")
    @Consumes({"application/json"})
    @Produces({"application/json"})
    public Response authenticate(AuthenticationRequest authentication,
                                 @PathParam("did") Long did) {

        if (!authRest.execute(did, request, authentication)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return u2fHelperBean.authenticate(did, authentication);
    }

    /**
     * The process of deleting or de-registering an already registered fido
     * authenticator. The inputs needed are the name of the user and the random
     * id to point to a unique registered key for that user. This random id can
     * be obtained by calling getkeysinfo method.
     *
     * @param did - Long value of the domain to service this request
     * @param kid - String value of the key to deregister
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in de-registration 3. 'Error' : String, with
     * error message incase something went wrong. Will be empty if successful.
     */
    @DELETE
    @Path("/{kid}")
    @Produces({"application/json"})
    public Response deregister(@PathParam("did") Long did,
                               @PathParam("kid") String kid) {

        if (!authRest.execute(did, request, null)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return u2fHelperBean.deregister(did, kid);
    }

    /**
     * The process of activating an already registered but de-activated fido
     * authenticator. This process will turn the status of the key in the
     * database back to ACTIVE. The inputs needed are the name of the user and
     * the random id to point to a unique registered key for that user. This
     * random id can be obtained by calling getkeysinfo method.
     *
     * @param patchkey -
     * @param did - Long value of the domain to service this request
     * @param kid - String value of the key to deregister
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : String, with a simple message telling if the process was
     * successful or not. 2. 'Message' : Empty string since there is no
     * cryptographic work involved in activation 3. 'Error' : String, with error
     * message incase something went wrong. Will be empty if successful.
     */
    @PATCH
    @Path("/{kid}")
    @Consumes({"application/merge-patch+json"})
    @Produces({"application/json"})
    public Response patchkey(UpdateFidoKeyRequest patchkey,
                           @PathParam("did") Long did,
                           @PathParam("kid") String kid) {

        if (!authRest.execute(did, request, patchkey)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return u2fHelperBean.patchfidokey(did, kid, patchkey);
    }

    /**
     * Method to return a list of user registered fido authenticator
     * information; In short, registered keys information. Information includes
     * the meta data of the key like the place and time it was registered and
     * used (last modified) from, a random id (which has a time-to-live) that
     * has to be sent back as a token during de-registration.
     *
     * @param did
     * @param username - The username we are finding keys for
     * @return - A Json in String format. The Json will have 3 key-value pairs;
     * 1. 'Response' : A Json array, each entry signifying metadata of a key
     * registered; Metadata includes randomid and its time-to-live, creation and
     * modify location and time info etc., 2. 'Message' : Empty string since
     * there is no cryptographic work involved in this process. 3. 'Error' :
     * String, with error message incase something went wrong. Will be empty if
     * successful.
     */
    @GET
    @Produces({"application/json"})
    public Response getkeysinfo(@PathParam("did") Long did,
                                @QueryParam("username") String username) {

        if (!authRest.execute(did, request, null)) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }

        return u2fHelperBean.getkeysinfo(did, username);
    }
}
