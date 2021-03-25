/*
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License, as published by the Free Software Foundation and
 * available at http://www.fsf.org/licensing/licenses/lgpl.html,
 * version 2.1.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * Copyright (c) 2001-2021 StrongAuth, Inc. (DBA StrongKey)
 *
 * *********************************************
 *                    888
 *                    888
 *                    888
 *  88888b.   .d88b.  888888  .d88b.  .d8888b
 *  888 "88b d88""88b 888    d8P  Y8b 88K
 *  888  888 888  888 888    88888888 "Y8888b.
 *  888  888 Y88..88P Y88b.  Y8b.          X88
 *  888  888  "Y88P"   "Y888  "Y8888   88888P'
 *
 * *********************************************
 *
 * The interface to the SFA's eCommerce REST webservices.
 * 
 * The commented @Suspended ... method signature enables the REST servlet to
 * respond to each request asynchronously, but there are some bizarre bugs
 * that couldn't be resolved at this time. Going to synchronous responses
 * for now. Can manage performance with HTTP threads in the application server.
 *
 * @OMG - This is the most bizarre bug I've run into - unless the @Path("")
 * is OUTSIDE the "public interface" section, and unless the second 
 * @Path("/method....") is INSIDE the "public interface" section - not in the 
 * implementation file - the stupid REST webservice just DOES NOT WORK!!!! 
 * Gah!!!
 */

package com.strongkey.sfaeco.web;

import java.io.InputStream;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path("")
public interface SfaecoRestInterface {
    
    @GET
    @Path("/ping")
    @Produces(MediaType.TEXT_PLAIN)
    public String ping();
    
    @POST
    @Path("/registerUser")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void registerUser(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String registerUser(final InputStream input);
    
    /**
     * These are the FIDO related webservices. This servlet merely relays
     * the call parameters to the appropriate FIDO server configured within
     * this servlets - see the sfa-configuration.properties file and the
     * CallFidoService EJB for details.
     */
    
    @POST
    @Path("/pingFidoService")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void pingFidoService(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String pingFidoService(final InputStream input);
    
    @POST
    @Path("/getFidoRegistrationChallenge")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void getFidoRegistrationChallenge(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String getFidoRegistrationChallenge(final InputStream input);
    
    @POST
    @Path("/registerFidoKey")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void registerFidoKey(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String registerFidoKey(final InputStream input);
    
    @POST
    @Path("/getFidoAuthenticationChallenge")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void getFidoAuthenticationChallenge(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String getFidoAuthenticationChallenge(final InputStream input);
    
    @POST
    @Path("/authenticateFidoKey")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void authenticateFidoKey(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String authenticateFidoKey(final InputStream input);
    
    @POST
    @Path("/getFidoAuthorizationChallenge")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void getFidoAuthorizationChallenge(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String getFidoAuthorizationChallenge(final InputStream input);
    
    @POST
    @Path("/authorizeFidoTransaction")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void authorizeFidoTransaction(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String authorizeFidoTransaction(final InputStream input);
    
    @POST
    @Path("/deregisterFidoKey")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void deregisterFidoKey(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String deregisterFidoKey(final InputStream input);
    
    @POST
    @Path("/getFidoKeysInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void getFidoKeysInfo(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String getFidoKeysInfo(final InputStream input);
    
    @POST
    @Path("/updateFidoKeyInfo")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
//    public void updateFidoKeyInfo(@Suspended final AsyncResponse asyncResponse, final InputStream input);
    public String updateFidoKeyInfo(final InputStream input);
        
    @GET
    @Path("/userTransactions")
    @Produces(MediaType.TEXT_PLAIN)
    public String getUserTransations();
}