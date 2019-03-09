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

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.proofpoint.bootcamp.monitor.ContactMonitor;
import com.proofpoint.bootcamp.monitor.ContactStats;
import com.proofpoint.units.Duration;
import org.weakref.jmx.Flatten;
import org.weakref.jmx.Managed;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Collection;

@Path("/v1/contact/{ownerId}")
public class ContactsResource
{
    private final ContactStore store;
    private final ContactMonitor monitor;

    @Inject
    public ContactsResource(ContactStore store, ContactMonitor monitor)
    {
        Preconditions.checkNotNull(store, "store is null");
        Preconditions.checkNotNull(monitor, "monitor is null");

        this.store = store;
        this.monitor = monitor;
    }

    @Managed
    @Flatten
    public ContactStats getStats()
    {
        return monitor.getStats();
    }

    @GET
    public Response contactsGet(@PathParam("ownerId") String ownerId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");


        Response response = null;

        long start = System.nanoTime();
        try {
            Collection<String> contacts = store.getContacts(ownerId);
            response = Response.ok(contacts, MediaType.APPLICATION_JSON).build();
        }
        catch (RuntimeException e) {
            throw e;
        }

        monitor.contactsRequested( ownerId, Duration.nanosSince(start));
        return response;
    }



}


