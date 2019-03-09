/*
 * Copyright 2010 Proofpoint, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.proofpoint.bootcamp;

import ch.qos.logback.core.encoder.NonClosableInputStream;
import com.google.common.base.Preconditions;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ListMultimap;
import com.google.common.collect.ArrayListMultimap;
import com.google.inject.Inject;
import com.proofpoint.bootcamp.monitor.ContactMonitor;
import com.proofpoint.bootcamp.monitor.ContactStats;
import com.proofpoint.discovery.client.ServiceType;
import com.proofpoint.http.client.StatusResponseHandler;
import com.proofpoint.json.JsonCodec;
import com.proofpoint.units.Duration;
import jdk.nashorn.internal.parser.JSONParser;
import org.apache.http.client.methods.HttpGet;
import org.weakref.jmx.Flatten;
import org.weakref.jmx.Managed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;


import com.proofpoint.http.client.ApacheHttpClient;
import com.proofpoint.http.client.HttpClient;
import com.proofpoint.http.client.JsonResponseHandler;
import com.proofpoint.http.client.BodyGenerator;
import com.proofpoint.http.client.*;
import java.net.URI;
import java.util.HashMap;
import java.util.UUID;
import com.proofpoint.http.client.Request;
import java.util.Map;

@Path("/v1/contact/{ownerId}/{contactId}")
public class ContactResource
{
    private final ContactStore store;
    private final ContactMonitor monitor;
    private HttpClient client;
    private final static String PersonURL = "http://localhost:8222";
    private final JsonCodec<Map<String, String>> codec;
    private final static String OWNER_ID = "ownerId";
    private final static String CONTACT_ID = "contactId";


    @Inject
    public ContactResource(ContactStore store, ContactMonitor monitor )
    {
        Preconditions.checkNotNull(store, "store is null");
        Preconditions.checkNotNull(monitor, "monitor is null");


        this.store = store;
        this.monitor = monitor;

        client = new ApacheHttpClient();

        this.codec = JsonCodec.mapJsonCodec(String.class, String.class);
    }

    @Managed
    @Flatten
    public ContactStats getStats()
    {
        return monitor.getStats();
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response contactExists(@PathParam("ownerId") String ownerId, @PathParam("contactId") String contactId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");

        Response response = null;

        long start = System.nanoTime();
        if (!store.contactExists(ownerId, contactId)){
            return Response.status(Response.Status.NOT_FOUND).build();
        }

        Request request = Request.Builder.prepareGet().setUri(URI.create(String.format("%s/v1/person/%s", PersonURL, contactId))).build();

        response = Response.noContent().build();

        Map<String, Object> responseMap = new HashMap<String, Object>();
        responseMap.put(OWNER_ID, ownerId);
        System.err.println("url:"+request.getUri());
        try {
            if (store.contactExists(ownerId, contactId)) {
                System.err.println("starting REST Call");
                Map<String, String> contactDetails = this.client.execute(request, JsonResponseHandler.createJsonResponseHandler(codec));
                responseMap.put(CONTACT_ID, contactDetails);
                System.err.println("result:"+contactDetails.toString());
                response = Response.ok().entity(responseMap).build();
            }
            else {
                response = Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch (RuntimeException e) {
            monitor.contactExistsRequestFailed(e, ownerId, contactId);
            throw e;
        }
        catch (Exception e){
            System.err.printf("exception: %s", e);
        }

        monitor.contactExistsRequestSucceeded(ownerId, contactId, Duration.nanosSince(start));
        return response;
    }



    @PUT
    public Response contactCreate(@PathParam("ownerId") String ownerId, @PathParam("contactId") String contactId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");

        Response response = null;

        try{
            //create contact
            if(store.putContact(ownerId,contactId))
                response = Response.noContent().build();
            else
                response = Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("Contact limit exceed configuration limit").build();
        }
        catch (RuntimeException e) {
            monitor.contactExistsRequestFailed(e, ownerId, contactId);
            throw e;
        }
        monitor.contactAdded(ownerId, contactId);
        return response;
        //TODO: add monitor function here
    }

    @DELETE
    public Response contactDelete(@PathParam("ownerId") String ownerId, @PathParam("contactId") String contactId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");

        Response response = null;
        try{
            store.deleteContact(ownerId, contactId);
            response = Response.noContent().build();
        }
        catch (RuntimeException e) {
            monitor.contactExistsRequestFailed(e, ownerId, contactId);
            throw e;
        }
        monitor.contactDeleted(ownerId, contactId);
        return response;
    }
}


