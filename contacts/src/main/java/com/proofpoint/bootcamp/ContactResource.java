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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;

@Path("/v1/contact/{ownerId}/{contactId}")
public class ContactResource
{
    private final ContactStore store;
    private final ContactMonitor monitor;

    @Inject
    public ContactResource(ContactStore store, ContactMonitor monitor)
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
    public Response contactExists(@PathParam("ownerId") String ownerId, @PathParam("contactId") String contactId)
    {
        Preconditions.checkNotNull(ownerId, "ownerId is null");
        Preconditions.checkNotNull(contactId, "contactId is null");

        Response response = null;

        long start = System.nanoTime();
        try {
            if (store.contactExists(ownerId, contactId)) {
                response = Response.noContent().build();
            }
            else {
                response = Response.status(Response.Status.NOT_FOUND).build();
            }
        }
        catch (RuntimeException e) {
            monitor.contactExistsRequestFailed(e, ownerId, contactId);
            throw e;
        }

        monitor.contactExistsRequestSucceeded(ownerId, contactId, Duration.nanosSince(start));
        return response;
    }
}
