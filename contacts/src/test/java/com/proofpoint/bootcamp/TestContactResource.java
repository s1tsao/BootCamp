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

import com.proofpoint.bootcamp.monitor.ContactMonitor;
import com.proofpoint.bootcamp.monitor.ContactStats;
import com.proofpoint.event.client.InMemoryEventClient;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.ws.rs.core.Response;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TestContactResource
{
    private InMemoryEventClient eventClient;
    private ContactStore store;
    private ContactResource resource;

    @BeforeMethod
    public void setup()
    {
        eventClient = new InMemoryEventClient();
        ContactMonitor monitor = new ContactMonitor(eventClient, new ContactStats());
        store = new ContactStore(monitor);
        resource = new ContactResource(store, monitor);
    }

    @Test
    public void testNullArguments()
    {
        try { resource.contactExists(null, "bla"); assertTrue(false); } catch (NullPointerException ex) { }
        try { resource.contactExists("foo", null); assertTrue(false); } catch (NullPointerException ex) { }
    }

    @Test
    public void testContactExists()
    {
        store.putContact("foo", "bla");
        assertTrue(store.contactExists("foo", "bla"));

        Response response = resource.contactExists("foo", "bla");
        assertEquals(response.getStatus(), Response.Status.NO_CONTENT.getStatusCode());
    }

    @Test
    public void testOwnerDoesNotExists()
    {
        store.putContact("foo", "bla");
        assertTrue(store.contactExists("foo", "bla"));

        Response response = resource.contactExists("bla", "hehe");
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }

    @Test
    public void testContactDoesNotExists()
    {
        store.putContact("foo", "bla");
        assertTrue(store.contactExists("foo", "bla"));

        Response response = resource.contactExists("foo", "hehe");
        assertEquals(response.getStatus(), Response.Status.NOT_FOUND.getStatusCode());
    }
}
